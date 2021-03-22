/***
 *  HeatMapScan
 *
 *  Extend GeneralScanV3
 *
 *  Take List of Observation, generate them into HeatMapTally
 *  reduce and scan with HeatMapTally and output result with HeatMapTally
 */

import java.util.List;

public class HeatMapScan extends GeneralScanV3<Observation, HeatMapTally, HeatMapTally>{
    private int dim;
    public HeatMapScan(List<Observation> rawData, int n_thread, int dim) {
        super(rawData, n_thread);
        this.dim = dim;
    }

    @Override
    protected HeatMapTally init() {
        return new HeatMapTally(dim);
    }

    @Override
    protected HeatMapTally prepare(Observation datum) {
        HeatMapTally tally = new HeatMapTally(dim);
        tally.addObservation(datum);
        return tally;
    }

    @Override
    protected HeatMapTally combine(HeatMapTally left, HeatMapTally right) {
        return HeatMapTally.combine(left, right);
    }

    @Override
    protected HeatMapTally gen(HeatMapTally tally) {
        return tally;
    }
}
