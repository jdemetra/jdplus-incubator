package jdplus.filters.base.api;

import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;

@lombok.Value
@lombok.Builder(builderClassName="Builder", toBuilder=true)
public class DFAFilterSpec implements FilterSpec{
    
    private double w0, w1;
    private double accuracyWeight, smoothnessWeight, timelinessWeight;
    private int lags, leads;
    private int polynomialPreservationDegree;
    private SpectralDensity density;
    // positive weights of a symmetric filter
    private double[] target;
    
    public static Builder builder(){
        return new Builder()
                .w0(0)
                .w1( Math.PI / 8)
                .accuracyWeight(1.0/3.0)
                .smoothnessWeight(1.0/3.0)
                .timelinessWeight(1.0/3.0)
                .lags(6)
                .leads(0)
                .polynomialPreservationDegree(0)
                .density(SpectralDensity.RandomWalk);
    }
}
