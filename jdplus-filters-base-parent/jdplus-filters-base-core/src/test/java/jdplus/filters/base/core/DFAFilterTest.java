package jdplus.filters.base.core;

import java.util.function.DoubleUnaryOperator;
import jdplus.filters.base.api.SpectralDensity;
import jdplus.toolkit.base.core.math.linearfilters.IFiltering;
import jdplus.toolkit.base.core.math.linearfilters.ISymmetricFiltering;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.IQuasiSymmetricFiltering;

import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import org.junit.jupiter.api.Test;

public class DFAFilterTest {

    public DFAFilterTest() {
    }

    @Test
    public void testSymmetric() {
        int len = 6;
        LocalPolynomialFilterSpec spec = LocalPolynomialFilterSpec.builder()
                .filterHorizon(len)
                .build();
        IQuasiSymmetricFiltering lf = LocalPolynomialFilters.of(spec);
        SymmetricFilter sf = lf.centralFilter();
        DFAFilter ff = DFAFilter.builder()
                .nlags(len)
                .nleads(len)
                .symetricFilter(sf)
                .density(SpectralDensity.WhiteNoise.asFunction())
                .build();
        DFAFilter.Results rslt = ff.make(0.33, 0.33, 0.33);
//        System.out.println(DoubleSeq.of(rslt.getFilter().weightsToArray()));
        DoubleUnaryOperator sd = x -> 1;
        MSEDecomposition.of(sd, sf.frequencyResponseFunction(), rslt.getFilter().frequencyResponseFunction(), Math.PI / 12);
        MSEDecomposition d = MSEDecomposition.of(sd, sf.frequencyResponseFunction(), rslt.getFilter().frequencyResponseFunction(), Math.PI / 12);
        //       System.out.println(DoubleSeq.of(d.getAccuracy(), d.getSmoothness(), d.getTimeliness(), d.getResidual(), d.getTotal()));
    }
}
