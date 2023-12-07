/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.filters.base.core;

import java.util.function.DoubleUnaryOperator;
import jdplus.filters.base.api.DFAFilterSpec;
import jdplus.filters.base.api.FSTFilterSpec;
import jdplus.filters.base.api.RKHSFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.FiltersToolkit;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AdvancedFiltersToolkit {

    public static void register() {
        FiltersToolkit.register(DFAFilterSpec.class, spec -> DFAFilterFactory.of((DFAFilterSpec) spec));
        FiltersToolkit.register(FSTFilterSpec.class, spec -> FSTFilterFactory.of((FSTFilterSpec) spec));
        FiltersToolkit.register(RKHSFilterSpec.class, spec -> RKHSFilterFactory.of((RKHSFilterSpec) spec));
    }

    public static void unregister() {
        FiltersToolkit.unregister(DFAFilterSpec.class);
        FiltersToolkit.unregister(FSTFilterSpec.class);
        FiltersToolkit.unregister(RKHSFilterSpec.class);

    }

    public double[] mseDecomposition(double[] sfilter, double[] afilter, String density, double passband) {
        SymmetricFilter sf = SymmetricFilter.ofInternal(sfilter);
        FiniteFilter af = FiniteFilter.of(afilter, -sfilter.length + 1);
        DoubleUnaryOperator sd;
        switch (density) {
            case "uniform":
                sd = x -> 1;
                break;
            default:
                sd = null;
        }
        MSEDecomposition d = MSEDecomposition.of(sd, sf.frequencyResponseFunction(), af.frequencyResponseFunction(), passband);
        return new double[]{d.getAccuracy(), d.getSmoothness(), d.getTimeliness(), d.getResidual(), d.getTotal()};
    }

    public FSTResult fstfilter(int nlags, int nleads, int pdegree, double scriterion, int sdegree, double tcriterion, double bandwith, boolean antiphase) {

        FSTFilter fst = FSTFilter.builder()
                .nlags(nlags)
                .nleads(nleads)
                .degreeOfSmoothness(sdegree)
                .polynomialPreservation(pdegree)
                .timelinessAntiphaseCriterion(antiphase)
                .timelinessLimits(0, bandwith)
                .build();

        FSTFilter.Results rslt = fst.make(scriterion, tcriterion, true);
        return FSTResult.builder()
                .filter(rslt.getFilter())
                .criterions(new double[]{rslt.getF(), rslt.getS(), rslt.getT()})
                .gain(FiltersToolkit.gain(rslt.getFilter()))
                .phase(FiltersToolkit.phase(rslt.getFilter()))
                .build();
    }

    @lombok.Value
    @lombok.Builder
    public static class FSTResult {

        private FiniteFilter filter;
        private double[] gain;
        private double[] phase;
        private double[] criterions;

        public double[] weights() {
            return filter.weightsToArray();
        }

        public int lb() {
            return filter.getLowerBound();
        }

        public int ub() {
            return filter.getUpperBound();
        }
    }

    public FSTResult fst(double[] filter, int startPos, double passband) {
        FiniteFilter ff = FiniteFilter.of(filter, startPos);

        double f = FSTFilter.FidelityCriterion.fidelity(ff);
        double s = FSTFilter.SmoothnessCriterion.smoothness(ff);
        double t = FSTFilter.TimelinessCriterion.timeliness(ff, passband);

        return FSTResult.builder()
                .filter(ff)
                .criterions(new double[]{f, s, t})
                .gain(FiltersToolkit.gain(ff))
                .phase(FiltersToolkit.phase(ff))
                .build();
    }
}
