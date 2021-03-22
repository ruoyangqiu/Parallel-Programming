/***
 * Create uniform spray data set
 */


import java.io.*;

public class CreateDataSet {

    private static void uniformData(){
        final String FILENAME = "observation_uniform_test.dat";
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME));
            long t = 0;
            int count = 0;
            for( t = 0; t < 4; t ++){
                for(int i = 0; i < 16; i ++){
                    double r = i * 0.1 + (-0.8);
                    for(int j = 0; j < 16; j++){
                        double c = j * 0.1 + (-0.8);
                        out.writeObject(new Observation(t, r, c));
                        count ++;
                    }
                }
            }
            System.out.println(count);
            out.writeObject(new Observation());  // to mark EOF
            out.close();
        } catch (IOException e) {
            System.out.println("writing to " + FILENAME + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
            int count = 0;
            Observation obs = (Observation) in.readObject();
            while (!obs.isEOF()) {
                System.out.println(++count + ": " + obs);
                obs = (Observation) in.readObject();
            }
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("reading from " + FILENAME + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        uniformData();
    }
}
