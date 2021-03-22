/**
 * Ruoyang Qiu
 * BitonicStage take two bitonic sequences and do a bitonic sort to
 * create a bitonic sequence as an output
 * */
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class BitonicStage implements Runnable{

    private double[] data;
    private SynchronousQueue<double[]> input1, input2;
    private SynchronousQueue<double[]> output;
    private String name;
    private static final int timeout = 10;

    /**
     * Default Constructor
     * */
    public BitonicStage() { }

    /***
     * Constructor
     */

    public BitonicStage(SynchronousQueue<double[]> input1,
                        SynchronousQueue<double[]> input2,
                        SynchronousQueue<double[]> output,
                        String name){
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.name = name;
    }

    /**
     * Implement Run in Runnable
     * */
    @Override
    public void run() {
        double[] arr1 = new double[1];
        double[] arr2 = new double[1];
        while(arr1 != null && arr2 != null){
            try{
                arr1 = input1.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                arr2 = input2.poll(timeout * 1000, TimeUnit.MILLISECONDS);
                if(arr1 != null && arr2 != null){
                    //boolean check = RandomArrayGenerator.isSorted(arr2);
                    double[] bitonic_seq = process(arr1, arr2);
                    //System.out.println(name + ": " + check);
                    output.offer(bitonic_seq, timeout * 1000, TimeUnit.MILLISECONDS);
                    //System.out.println(name);
                } else {
                    if(arr1 == null) {
                        System.out.println(getClass().getName() + " " + name + " got null UP array");
                    }

                    if(arr2 == null) {
                        System.out.println(getClass().getName() + " " + name + " got null DOWN array");
                    }

                }
            }catch (InterruptedException e){
                return;
            }
        }

    }

    private void printoutput(double[] a) {
        if(name.charAt(name.length() - 1) == '1'){
            for(int i = 0; i < a.length; i ++){
                System.out.println(name + " #" + i + " " + a[i]);
            }
            System.out.println();
        }
    }
    /**
     * Process Bitonic Sort for a bitonic sequence:
     * @param arr1 UP sequence
     * @param arr2 DOWN sequence in UP order, inversion is required
     **/
    public double[] process(double[] arr1, double[] arr2) {
        double[] bitonic_seq = new double[arr1.length + arr2.length];

        for(int i = 0; i < arr1.length; i ++){
            bitonic_seq[i] = arr1[i];
        }

        reverse(arr2);

        for(int i = 0; i < arr2.length; i ++){
            bitonic_seq[i + arr1.length] = arr2[i];
        }
        bitonic_sort(bitonic_seq, 0, bitonic_seq.length, 0);

        return bitonic_seq;
    }

    /**
     * Bitonic Sort
     * @param bitonic_seq   Array store the bitonic sequence
     * @param start         starting index of bitonic sort
     * @param n             size of sequence need to be sorted
     * @param direction     direction 1 for DOWN, 0 for UP
    **/
    private void bitonic_sort(double[] bitonic_seq, int start, int n, int direction){
        if(n > 1){
            bitonic_merge(bitonic_seq, start, n, direction);
            bitonic_sort(bitonic_seq, start, n/2, direction);
            bitonic_sort(bitonic_seq, start + n / 2, n / 2, direction);
        }
    }

    /**
     * Bitonic Merge
     * @param bitonic_seq   Array store the bitonic sequence
     * @param start         starting index of bitonic merge
     * @param n             size of sequence need to be merged
     * @param direction     direction 1 for DOWN, 0 for UP
     * */
    private  void  bitonic_merge(double[] bitonic_seq, int start, int n, int direction){
        if(direction == 1){
            for(int i = start; i < start + n / 2; i ++){
                if(bitonic_seq[i] < bitonic_seq[i + n/2]){
                    swap(bitonic_seq, i, i + n/2);
                }
            }
        } else {
            for(int i = start; i < start + n / 2; i ++){
                if(bitonic_seq[i] > bitonic_seq[i + n/2]){
                    swap(bitonic_seq, i, i + n/2);
                }
            }
        }
    }

    /**
     * Reverse the input array
     * @param arr: Input array
     **/
    private void reverse(double[] arr){
        int length = arr.length;
        for(int i = 0; i < length / 2; i++){
            double temp = arr[length - i - 1];
            arr[length - i - 1] = arr[i];
            arr[i] = temp;
        }
    }

    /**
     * swap position s of two double variables in the array
     * @param arr:  Array contain the swapping elements.
     * @param i:    index of first element
     * @param j:    index of second element
     * */
    private void swap(double[] arr, int i, int j){
        double temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
