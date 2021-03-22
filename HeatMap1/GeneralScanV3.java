/**
 * GeneralScan Version 3 Using Schwartz approach
 * */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public abstract class GeneralScanV3 <ElemType, TallyType, ResultType> {

    public static final int N_THREAD = 16;
    private ForkJoinPool pool;
    private int n_thread;

    public GeneralScanV3(List<ElemType> rawData){
        this(rawData, N_THREAD);
    }

    public GeneralScanV3(List<ElemType> rawData, int n_thread){
        this.reduced = false;
        this.rawData = rawData;
        this.n = rawData.size();
        this.n_thread = n_thread;
        this.height = (int) Math.ceil(Math.log(n) / Math.log(2));

        /*if(1 << height != n){
            throw new IllegalArgumentException("data size must be power of 2 for now");
        }*/

        if(n_thread >= n){
            throw new IllegalArgumentException("must be more data than threads!");
        }

        interior = new ArrayList<>(2 * n_thread);
        for(int i = 0; i < 2 * n_thread; i ++) interior.add(init());
        pool = new ForkJoinPool(n_thread);
    }

    /**
     * Parallelized getting reduction
     */
    public ResultType getReduction(){
        if(!reduced){
            pool.invoke(new ComputeReduction(ROOT));
            reduced = true;
        }
        return gen(value(ROOT));
    }

    /**
     * Parallelized getting scan
     */
    public List<ResultType> getScan(){
        if(!reduced){
            getReduction();
        }
        List<ResultType> output = new ArrayList<>(n);
        for(int i = 0; i < n; i ++) output.add(gen(init()));
        pool.invoke(new ComputeScan(ROOT, init(), output));
        return output;
    }

    protected abstract TallyType init();

    protected abstract TallyType prepare(ElemType datum);

    protected abstract TallyType combine(TallyType left, TallyType right);

    protected abstract ResultType gen(TallyType tally);

    protected TallyType accum(TallyType accumulator, TallyType right){
        return combine(accumulator, right);
    }

    private int ROOT = 0;
    private boolean reduced;
    private int n;
    private List<ElemType> rawData;
    private List<TallyType> interior;
    private int height;

    private int size() {
        return (n - 1) + n;
    }

    private TallyType value(int i){
        if(i < n - 1){
            return interior.get(i);
        } else {
            return prepare(rawData.get(i - (n - 1)));
        }
    }
    private int parent(int i){
        return (i - 1) / 2;
    }

    private int left(int i) {
        return i*2+1;
    }
    private int right(int i) {
        return left(i)+1;
    }
    private boolean isLeaf(int i) {
        return right(i) >= size();
    }

    private int leftmost(int i){
        while(!isLeaf(i))
            i = left(i);
        return i;
    }

    private int rightmost(int i){
        while(!isLeaf(i))
            i = right(i);
        return i;
    }

    private boolean reduce(int i) {
        TallyType tally = init();
        int rm = rightmost(i);
        //System.out.println("reduce(" + i + ") from " + rightmost(i) + " to " + leftmost(i));
        for(int j = leftmost(i); j <= rm; j ++){
            //System.out.println("Combine " + tally.toString() + ", " + value(j).toString() );
            //TallyType temp = tally;
            tally = accum(tally, value(j));
            //System.out.println("Combine " + temp.toString()  + ", " + value(j).toString()  + " is " + tally.toString()  );
        }
        interior.set(i, tally);
        return true;
    }

    private void scan(int i, TallyType prior, List<ResultType> output){
        TallyType tally = prior;
        int rm = rightmost(i);
        for(int j = leftmost(i); j <= rm; j ++){
            tally = combine(tally, value(j));
            //System.out.println(j - (n - 1) + ": " + tally);
            output.set(j - (n - 1), gen(tally));
        }
    }

    class ComputeReduction extends RecursiveAction{

        private int i;

        public ComputeReduction(int i){
            this.i = i;
        }
        @Override
        protected void compute() {
            if(i < n_thread - 1){
                invokeAll(new GeneralScanV3.ComputeReduction(left(i)),
                        new GeneralScanV3.ComputeReduction(right(i))
                );
                interior.set(i, combine(value(left(i)), value(right(i))));
            } else {
                reduce(i);
            }

        }
    }

    class ComputeScan extends RecursiveAction{
        private int i;
        private TallyType prior;
        private List<ResultType> output;

        public ComputeScan(int i, TallyType prior, List<ResultType> output) {
            this.i = i;
            this.prior = prior;
            this.output = output;
        }
        @Override
        protected void compute() {
            if(i < n_thread - 1){
                invokeAll(new GeneralScanV3.ComputeScan(left(i), prior, output),
                        new GeneralScanV3.ComputeScan(right(i), combine(prior, value(left(i))), output)
                );
            } else {
                scan(i, prior, output);
            }
        }
    }
}
