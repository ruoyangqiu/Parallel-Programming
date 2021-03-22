/**
 * Ruoyang Qiu
 *
 * Assignment 3
 * Impelementing a Parallelizd Pipelined Bitonic Sort and test it
 *
 * */

import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class BitonicPipeline {
    public static final int N = 1 << 22;  // size of the final sorted array (power of two)
    public static final int TIME_ALLOWED = 10;  // seconds

    public static final int N_STAGE_ONE = 4;
    public static final int N_STAGE_TWO = 2;
    public static final int N_STAGE_FINAL = 1;
    public static final int N_ARRAY = N_STAGE_ONE;
    public static final int N_THREAD = N_ARRAY + N_STAGE_ONE + N_STAGE_TWO + N_STAGE_FINAL;

    /**
     * Run Bitonic sort in pipeline
     * **/
    private static void runPipeline(){
        ArrayList<SynchronousQueue<double[]>> myQueues = new ArrayList<>();
        Thread[] myThreads = new Thread[N_THREAD];
        SynchronousQueue<double[]> output = new SynchronousQueue<>();
        for(int i = 0; i < N_THREAD - 1; i++){
            myQueues.add(new SynchronousQueue<>());
        }

        // Create 4 Threads to generate unsorted arrays
        for(int i = 0; i < N_ARRAY; i ++){
            myThreads[i] = new Thread(new RandomArrayGenerator(N/4, myQueues.get(i)));
        }

        // Create 4 Threads for Stage One
        for(int i = 0; i < N_STAGE_ONE; i++){
            int threadIndex = i + N_ARRAY;
            myThreads[threadIndex] = new Thread(new StageOne(myQueues.get(i),
                    myQueues.get(threadIndex),
                    "StageOne " + i));
            //System.out.println(i + " -> " + threadIndex);
        }

        // Create 2 Threads for first part of Bitonic Stage
        for(int i = 0; i < N_STAGE_TWO; i++){
            int threadIndex = i + N_ARRAY + N_STAGE_ONE;
            int arr1Index = N_ARRAY + i * N_STAGE_TWO;
            int arr2Index = N_ARRAY + i * N_STAGE_TWO + 1;
            myThreads[threadIndex] = new Thread(new BitonicStage(myQueues.get(arr1Index),
                    myQueues.get(arr2Index), myQueues.get(threadIndex), "StageTwo " + i));
            //System.out.println(arr1Index + ", " + arr2Index + " -> " + threadIndex);
        }

        // Create 1 Threads for final part of Bitonic Stage
        for(int i = 0; i < N_STAGE_FINAL; i++){
            int threadIndex = i + N_ARRAY + N_STAGE_ONE + N_STAGE_TWO;
            int arr1Index = i + N_ARRAY + N_STAGE_ONE + i * N_STAGE_FINAL;
            int arr2Index = i + N_ARRAY + N_STAGE_ONE + i * N_STAGE_FINAL + 1;
            myThreads[threadIndex] = new Thread(new BitonicStage(myQueues.get(arr1Index),
                    myQueues.get(arr2Index), output, "StageFinal " + i));
        }

        for(int i = 0; i < N_THREAD; i++){
            myThreads[i].start();
        }

        checkOutput(output);

        cleanup(myThreads);
    }

    /**
     * Check if output is sorted and count total sorted array in 10 sec
     * @param output SynchronousQueue stores the output of final BitonicStage
     * **/
    private static void checkOutput(SynchronousQueue<double[]> output) {
        long start = System.currentTimeMillis();
        long endTime = start + TIME_ALLOWED * 1000;
        int work = 0;
        //int fail = 0;
        while(System.currentTimeMillis() < endTime){
            try{
                double[] ult = output.poll(TIME_ALLOWED * 1000, TimeUnit.MILLISECONDS);
                if(!RandomArrayGenerator.isSorted(ult)){
                    System.out.println("failed");
                    //fail ++;
                } else {
                    work ++;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("sorted " + work + " arrays (each: " + N + " doubles) in "
                + TIME_ALLOWED + " seconds");
        //System.out.println( fail + " failed arrays (each: " + N + " doubles) in "
        //        + TIME_ALLOWED + " seconds");
    }

    /**
     * Cleanup all threads
     * @param myThreads array stores all threads
     **/
    private static void cleanup(Thread[] myThreads){
        for(Thread mt : myThreads){
            if(mt.isAlive()){
                mt.interrupt();
            }
        }
    }

    public static void main(String[] args){
        runPipeline();
    }
}
