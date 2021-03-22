/**
 * BitonicSortLoop
 * A parallelized Bitonic Loop Sort
 *
 * */
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BitonicSortLoop implements Runnable{
    private double[] arr;
    private int thread_id;
    private int thread_count;
    private int n;
    private CyclicBarrier[] cBarrier;
    private int timeout;


    public BitonicSortLoop(double[] arr, int thread_id, int thread_count,
                           int n, CyclicBarrier[] barriers, int timeout) {
        this.arr = arr;
        this.thread_id = thread_id;
        this.thread_count = thread_count;
        this.n = n;
        this.cBarrier = barriers;
        this.timeout = timeout;
    }

    @Override
    public void run(){
        try{
            for(int k = 2; k <= n; k *= 2){
                //System.out.println();
                //System.out.println("k: "  + k + Arrays.toString(arr));
                for(int j = k / 2; j > 0; j /= 2){
                    int index = getBarrierIndex(j);
                    if(index >= 0) {
                        //System.out.println("thread-" + thread_id + "->" + index + " j: " + j + ", k: " + k);
                        cBarrier[index].await();
                    }
                    int piece = n/thread_count;
                    int start = thread_id * piece;
                    int end = start + piece;
                    //System.out.println("j: "  + j);
                    for(int i = start; i < end; i ++){
                        int ixj = i ^ j;
                        if(ixj > i){
                            //System.out.println(k + " " + j + " thread "+thread_id + ": "+i + " -> " + ixj);
                            if ((i & k) == 0 && arr[i] > arr[ixj]){
                                swap(i, ixj);
                            }
                            if ((i & k) != 0 && arr[i] < arr[ixj]){
                                swap(i, ixj);
                            }
                        }
                    }
                }

            }

        } catch (InterruptedException e){
            return;
        } catch (BrokenBarrierException e){
            return;
        }
    }

    /**
     * Find the index of the barrier responsible for this thread
     *
     * @param j the yarn for bitonic sort
     * */
    private int getBarrierIndex(int j) {
        int piece = arr.length /thread_count;
        if(j*2 < piece){
            return - 1;
        }

        int index = getIndexInBarrierHeap(j);


        return index;
    }

    /** The barriers are stored like a heap
     *  We need to know the level of the heap
     *  and then find the index of the node in the array
     *
     * */
    private int getIndexInBarrierHeap(int j) {

        /*
            The root node of barrier heap store the barrier works for all thread
            And total number of thread cover all wires in the bitonic network.
            To find the smallest possible barrier can be used for certain number
            of wires and task, we want to know the level of our barrier. Thus, we
            need to know Log2(array length) - Log2(2 * j) = Log2(array.length/(2*j))
        * */

        int diff = arr.length / (j * 2);
        int level = (int)(Math.log(diff) / Math.log(2));

        /* level - 1: if j = 4, we need 8 wires done their previous work to make
            sure they are synchronized
        * */
        if(level > 0){
            level -= 1;
        }

        /*
            The size of barrier array are determined by grabularity.
            Hence, if the index is out of range, we need to use a bigger barrier
        * */
        int granularity = (int)(Math.log(cBarrier.length + 1) / Math.log(2));

        if(level >= granularity){
            level = granularity - 1;
        }

        // Get offset
        int div = thread_count ;

        for(int i = 0; i < level; i ++){
            div /= 2;
        }

        int  offset = thread_id / div;
        int index = (1<<level) - 1 + offset;
        return index;
    }


    private void swap(int i, int j){
        double temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
