package org.opentripplanner.standalone;

import java.util.ArrayList;
import java.util.List;

import org.opentripplanner.standalone.ZSampleGrid.ZSamplePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Helper class to fill-in a ZSampleGrid from a given loosely-defined set of sampling points. The
 * process is customized by an "accumulative" metric which gives the behavior of cumulating several
 * values onto one sampling point. To use this class, create an instance giving an
 * AccumulativeMetric implementation as parameter. Then for each source sample, call "addSample"
 * with the its TZ value. At the end, call close() to close the sample grid (ie add grid node at the
 * edge to make sure borders are correctly defined, the definition of correct is left to the
 * client).
 *
 * @author laurent
 */
public class AccumulativeGridSampler<TZ> {

    /**
     * An accumulative metric give the behavior of combining several samples to a regular sample
     * grid, ie how we should weight and add several TZ values from inside a cell to compute the
     * cell corner TZ values.
     *
     * @author laurent
     * @param <TZ>
     */
    public interface AccumulativeMetric<TZ> {
        /**
         * Callback function to handle a new added sample.
         *
         * @param C0 The initial position of the sample, as given in the addSample() call.
         * @param Cs The position of the sample on the grid, never farther away than (dX,dY)
         * @param z The z value of the initial sample, as given in the addSample() call.
         * @param zS The previous z value of the sample. Can be null if this is the first time, it's
         *        up to the caller to initialize the z value.
         * @return The modified z value for the sample.
         */
        public TZ cumulateSample(Coordinate C0, Coordinate Cs, TZ z, TZ zS);

        /**
         * Callback function to handle a "closing" sample (that is a sample post-created to surround
         * existing samples and provide nice and smooth edges for the algorithm).
         *
         * @param zUp Sampled value of the up neighbor. Can be null if undefined.
         * @param zDown Idem
         * @param zRight Idem
         * @param zLeft Idem
         * @return The z value for the closing sample.
         */
        public TZ closeSample(TZ zUp, TZ zDown, TZ zRight, TZ zLeft);
    }

    private static final Logger LOG = LoggerFactory.getLogger(AccumulativeGridSampler.class);

    private final AccumulativeMetric<TZ> metric;

    private final ZSampleGrid<TZ> sampleGrid;

    private boolean closed = false;

    /**
     * @param metric TZ data "behavior" and "metric".
     * @param size Estimated grid size
     */
    public AccumulativeGridSampler(ZSampleGrid<TZ> sampleGrid, AccumulativeMetric<TZ> metric) {
        this.metric = metric;
        this.sampleGrid = sampleGrid;
    }

    public final void addSamplingPoint(Coordinate C0, TZ z) {
        if (this.closed) { throw new IllegalStateException("Can't add a sample after closing."); }
        int[] xy = this.sampleGrid.getLowerLeftIndex(C0);
        int x = xy[0];
        int y = xy[1];
        @SuppressWarnings("unchecked")
        ZSamplePoint<TZ>[] ABCD = new ZSamplePoint[4];
        ABCD[0] = this.sampleGrid.getOrCreate(x, y);
        ABCD[1] = this.sampleGrid.getOrCreate(x + 1, y);
        ABCD[2] = this.sampleGrid.getOrCreate(x, y + 1);
        ABCD[3] = this.sampleGrid.getOrCreate(x + 1, y + 1);
        for (ZSamplePoint<TZ> P : ABCD) {
            Coordinate C = this.sampleGrid.getCoordinates(P);
            P.setZ(this.metric.cumulateSample(C0, C, z, P.getZ()));
        }
    }

    /**
     * Surround all existing samples on the edge by a layer of closing samples.
     */
    public final void close() {
        if (this.closed) { return; }
        this.closed = true;
        List<ZSamplePoint<TZ>> processList = new ArrayList<ZSamplePoint<TZ>>(this.sampleGrid.size());
        for (ZSamplePoint<TZ> A : this.sampleGrid) {
            processList.add(A);
        }
        int n = 0;
        /*
         * TODO The magic "2" below should be automatically computed according to some return value
         * from the metric.
         */
        for (int i = 0; i < 2; i++) {
            List<ZSamplePoint<TZ>> newProcessList = new ArrayList<ZSamplePoint<TZ>>(processList.size());
            for (ZSamplePoint<TZ> A : processList) {
                if (A.right() == null) {
                    newProcessList.add(closeSample(A.getX() + 1, A.getY()));
                    n++;
                }
                if (A.left() == null) {
                    newProcessList.add(closeSample(A.getX() - 1, A.getY()));
                    n++;
                }
                if (A.up() == null) {
                    newProcessList.add(closeSample(A.getX(), A.getY() + 1));
                    n++;
                }
                if (A.down() == null) {
                    newProcessList.add(closeSample(A.getX(), A.getY() - 1));
                    n++;
                }
            }
            processList = newProcessList;
        }
        LOG.info("Added {} closing samples to get a total of {}.", n, this.sampleGrid.size());
    }

    private final ZSamplePoint<TZ> closeSample(int x, int y) {
        ZSamplePoint<TZ> A = this.sampleGrid.getOrCreate(x, y);
        A.setZ(this.metric.closeSample(A.up() != null ? A.up().getZ() : null, A.down() != null ? A.down().getZ() : null, A
                .right() != null ? A.right().getZ() : null, A.left() != null ? A.left().getZ() : null));
        return A;
    }
}
