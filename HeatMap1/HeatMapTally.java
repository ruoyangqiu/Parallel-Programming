/***
 * HeatMapTally
 *
 * Data structure stored heatmap used for reduce and scan
 */


import java.util.Arrays;

public class HeatMapTally {
    public int[][] heatmap;
    public int dim;
    public static final int DIM = 150;
    public static final double SUFD_HEIGHT = 2.0;
    public static final double SUFD_WIDTH = 2.0;
    public static final double OFFSET = 1.0;

    public HeatMapTally(){
        this(DIM);
    }

    public HeatMapTally(int dim){
        heatmap = new int[dim][dim];
        this.dim = dim;
    }

    /**
     * add an Observation to heatmap
     */
    public void addObservation(Observation o){
        int r = getRow(o.y);
        int c = getCol(o.x);
        heatmap[r][c] ++;
    }

    public int getHit(int r, int c){
        return heatmap[r][c];
    }

    public void setHit(int r, int c, int value){
        heatmap[r][c] = value;
    }

    /**
     * Combine two heatmap
     */

    public static HeatMapTally combine(HeatMapTally a, HeatMapTally b){
        if(a.dim != b.dim){
            throw new IllegalArgumentException("Heatmaps with different dimension cannot be combined");
        }
        HeatMapTally res = new HeatMapTally(a.dim);
        for(int r = 0; r < res.dim; r ++){
            for(int c = 0; c < res.dim; c ++){
                res.heatmap[r][c] = a.heatmap[r][c] + b.heatmap[r][c];
            }
        }
        return res;
    }

    /***
     * get row index by using data collected from sensor
     */
    private int getRow(double r){
        return (int)(dim * ((r + OFFSET) / SUFD_WIDTH));
    }

    /***
     * get column index by using data collected from sensor
     */
    private int getCol(double c){
        return (int)(dim * ((c + OFFSET) / SUFD_HEIGHT));
    }



    @Override
    public String toString() {
        return "HeatMapTally{" +
                "heatmap=" + Arrays.toString(heatmap[0]) +
                Arrays.toString(heatmap[1])+
        '}';
    }
}
