/**
 * Assignment 4
 *
 * parallelization of the bitonic loop sort with CyclicBarrier
 * Granularity is used for barrier, it can be modified
 *
 * */
import java.util.concurrent.CyclicBarrier;

public class Homework4 {
    private final static int N = 1 << 22;
    private final static int N_THREAD = 8;
    private final static int TIME_ALLOWED = 10;
    private final static int GRANULARITY = 4;
    /**
     * process bitonic sort
     * */
    private static void process(double[] arr){
        try{
            Thread[] mythreads = new Thread[N_THREAD];
            int nob = (1 << (GRANULARITY) )- 1;
            CyclicBarrier[] barriers = new CyclicBarrier[nob];
            fillBarrier(barriers);
            for(int i = 0; i < N_THREAD; i++) {
                mythreads[i] = new Thread(new BitonicSortLoop(arr, i,
                        N_THREAD, arr.length, barriers, TIME_ALLOWED));
                mythreads[i].start();
            }
            for(Thread mt: mythreads){
                mt.join();
            }
        } catch (InterruptedException e){
            return;
        }
    }

    /**
     * Fill barrier array with barrier that is responsible for
     * specific amount of threads.
     * */
    private static void fillBarrier(CyclicBarrier[] barriers){
        int tpb = N_THREAD;
        int next = 1;
        for(int i = 0; i < barriers.length; i ++) {
            barriers[i] = new CyclicBarrier(tpb);
            int check = (1 << next) - 2;
            if (i == check && tpb > 1) {
                tpb /= 2;
                next ++ ;
            }

        }
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        long end = start + TIME_ALLOWED * 1000;
        int work = 0;

        while(System.currentTimeMillis() < end){
            double[] arr = RandomArrayGenerator.getArray(N);
            process(arr);
            //System.out.println(Arrays.toString(arr));
            boolean res = RandomArrayGenerator.isSorted(arr);
            if(!res){
                System.out.println("failed");
            } else {
                work ++;
            }
        }

        System.out.println("sorted " + work + " arrays (each: " + N + " doubles) in "
                + TIME_ALLOWED + " seconds with " + N_THREAD + " threads and granularity of "
                + GRANULARITY);
    }
}
