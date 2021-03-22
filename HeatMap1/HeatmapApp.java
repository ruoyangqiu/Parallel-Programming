/***
 * Homework 5
 *
 * Ruoyang Qiu
 * Using Schwartz approach to reduce and scan the hits collected by sensor
 * to generate heatmaps and display heatmaps with animation
 *
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class HeatmapApp {
    private static final int DIM = 150;
    private static final String REPLAY = "Replay";
    private static JFrame application;
    private static JButton button;
    private static Color[][] grid;
    private static List<HeatMapTally> heatmap;
    private static int maxHit;

    public static void main(String[] args) throws InterruptedException {
        final String FILENAME = "observation_uniform_test.dat";
        java.util.List<Observation> raw = new ArrayList<>();
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
            int count = 0;
            Observation obs = (Observation) in.readObject();
            while (!obs.isEOF()) {
                //System.out.println(++count + ": " + obs);
                raw.add(obs);
                obs = (Observation) in.readObject();
            }
            //System.out.println(raw.size());

            in.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("reading from " + FILENAME + "failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        HeatMapScan scanner = new HeatMapScan(raw, 16, DIM);
        HeatMapTally reduce = scanner.getReduction();
        heatmap = scanner.getScan();
        maxHit = maxhit(reduce.heatmap);
        /*for (int i = 0; i < DIM; i++){
            System.out.print("[ ");
            for(int j = 0; j < DIM; j++){

                System.out.print(reduce.getHit(i, j) + " ");
            }
            System.out.print(" ]\n");
        }*/

        grid = new Color[DIM][DIM];
        application = new JFrame();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fillGrid(grid);
        ColoredGrid gridPanel = new ColoredGrid(grid);
        application.add(gridPanel, BorderLayout.CENTER);

        button = new JButton(REPLAY);
        button.addActionListener(new BHandler());
        application.add(button, BorderLayout.PAGE_END);

        application.setSize(DIM * 4, (int)(DIM * 4.4));
        application.setVisible(true);
        application.repaint();
        animate();
    }

    private static void animate() throws InterruptedException {
        button.setEnabled(false);
        cur = 0;
        for (cur = 0; cur < heatmap.size(); cur++) {
            fillGrid(grid);
            application.repaint();
            Thread.sleep(50);
        }
        button.setEnabled(true);
        application.repaint();
    }

    static class BHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (REPLAY.equals(e.getActionCommand())) {
                new Thread() {
                    public void run() {
                        try {
                            animate();
                        } catch (InterruptedException e) {
                            System.exit(0);
                        }
                    }
                }.start();
            }
        }
    };

    static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;
    static private int offset = 0;
    private static int cur = 0;
    private static void fillGrid(Color[][] grid) {

        int pixels = grid.length * grid[0].length;
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++) {
                double ratio = (double)((double)heatmap.get(cur).getHit(r, c) / (double)maxHit);
                if(heatmap.get(cur).getHit(r, c) != 0){
                    //System.out.println(cur + ": " + heatmap.get(cur).getHit(r, c));
                }
                //System.out.println(cur + ": " + heatmap.get(cur).getHit(r, c));
                grid[r][c] = interpolateColor(ratio, COLD, HOT);

            }
        offset += DIM;
        //cur += 1;
    }

    /**
     * Get max hit in a heatmap
     * @param heatmap target heatmap
     * */
    private static int maxhit(int[][] heatmap){
        int max = 0;
        int dim = heatmap.length;
        for(int r = 0; r < dim; r++){
            for(int c = 0; c < dim; c ++){
                max = Math.max(heatmap[r][c], max);
            }
        }
        return max;
    }

    private static Color interpolateColor(double ratio, Color a, Color b) {
        int ax = a.getRed();
        int ay = a.getGreen();
        int az = a.getBlue();
        int cx = ax + (int) ((b.getRed() - ax) * ratio);
        int cy = ay + (int) ((b.getGreen() - ay) * ratio);
        int cz = az + (int) ((b.getBlue() - az) * ratio);
        //System.out.println(cx+" "+ cy+" "+ cz + " ratio" + ratio);
        //System.out.println("ax: " + ax + "ay: " + ay + "az: " + az);
        //System.out.println(b.toString());
        return new Color(cx, cy, cz);
    }
}
