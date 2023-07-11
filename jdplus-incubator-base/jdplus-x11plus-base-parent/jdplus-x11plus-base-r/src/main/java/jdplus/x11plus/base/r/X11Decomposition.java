package jdplus.x11plus.base.r;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.math.linearfilters.HendersonFilters;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.function.IntToDoubleFunction;
import jdplus.filters.base.api.AsymmetricCriterion;
import jdplus.filters.base.api.SpectralDensity;
import jdplus.filters.base.core.RKHSFilterFactory;
import jdplus.filters.base.api.RKHSFilterSpec;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.math.linearfilters.AsymmetricFilterOption;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.math.linearfilters.Filtering;
import jdplus.toolkit.base.core.math.linearfilters.ISymmetricFiltering;
import jdplus.toolkit.base.api.math.linearfilters.KernelOption;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.x11plus.base.core.AsymmetricEndPoints;
import jdplus.x11plus.base.core.MusgraveFilterFactory;
import jdplus.x11plus.base.core.SeasonalFilterOption;
import jdplus.x11plus.base.core.SeriesEvolution;
import jdplus.x11plus.base.core.X11Context;
import jdplus.x11plus.base.core.X11Kernel;
import jdplus.x11plus.base.core.X11SeasonalFiltersFactory;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class X11Decomposition {

    private static IntToDoubleFunction kernel(String seasKernel, int h) {
        switch (seasKernel){
            case "Trapezoidal":
                return DiscreteKernel.trapezoidal(h);
            case "Henderson":
                return DiscreteKernel.henderson(h);
            case "BiWeight":
                return DiscreteKernel.biweight(h);
            case "TriWeight":
                return DiscreteKernel.triweight(h);
            case "TriCube":
                return DiscreteKernel.tricube(h);
            case "Uniform":
                return DiscreteKernel.uniform(h);
            case "Triangular":
                return DiscreteKernel.triangular(h);
            case "Epanechnikov":
                return DiscreteKernel.epanechnikov(h);
            default:
                throw new IllegalArgumentException(seasKernel);
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class Results implements GenericExplorable {

        boolean multiplicative;
        DoubleSeq y;
        X11Kernel kernel;

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa", MUL = "mul";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<Results>() {
            @Override
            public Class getSourceClass() {
                return Results.class;
            }
        };

        static {
            MAPPING.set(Y, double[].class, source -> source.getY().toArray());
            MAPPING.set("b1", double[].class, source -> source.getKernel().getBstep().getB1().toArray());
            MAPPING.set("b2", double[].class, source -> source.getKernel().getBstep().getB2().toArray());
            MAPPING.set("b3", double[].class, source -> source.getKernel().getBstep().getB3().toArray());
            MAPPING.set("b4", double[].class, source -> source.getKernel().getBstep().getB4().toArray());
            MAPPING.set("b4a", double[].class, source -> source.getKernel().getBstep().getB4a().toArray());
            MAPPING.set("b4d", double[].class, source -> source.getKernel().getBstep().getB4d().toArray());
            MAPPING.set("b5", double[].class, source -> source.getKernel().getBstep().getB5().toArray());
            MAPPING.set("b6", double[].class, source -> source.getKernel().getBstep().getB6().toArray());
            MAPPING.set("b7", double[].class, source -> source.getKernel().getBstep().getB7().toArray());
            MAPPING.set("b8", double[].class, source -> source.getKernel().getBstep().getB8().toArray());
            MAPPING.set("b9", double[].class, source -> source.getKernel().getBstep().getB9().toArray());
            MAPPING.set("b10", double[].class, source -> source.getKernel().getBstep().getB10().toArray());
            MAPPING.set("b11", double[].class, source -> source.getKernel().getBstep().getB11().toArray());
            MAPPING.set("b13", double[].class, source -> source.getKernel().getBstep().getB13().toArray());
            MAPPING.set("b17", double[].class, source -> source.getKernel().getBstep().getB17().toArray());
            MAPPING.set("b20", double[].class, source -> source.getKernel().getBstep().getB20().toArray());
            MAPPING.set("c1", double[].class, source -> source.getKernel().getCstep().getC1().toArray());
            MAPPING.set("c2", double[].class, source -> source.getKernel().getCstep().getC2().toArray());
            MAPPING.set("c4", double[].class, source -> source.getKernel().getCstep().getC4().toArray());
            MAPPING.set("c5", double[].class, source -> source.getKernel().getCstep().getC5().toArray());
            MAPPING.set("c6", double[].class, source -> source.getKernel().getCstep().getC6().toArray());
            MAPPING.set("c7", double[].class, source -> source.getKernel().getCstep().getC7().toArray());
            MAPPING.set("c9", double[].class, source -> source.getKernel().getCstep().getC9().toArray());
            MAPPING.set("c10", double[].class, source -> source.getKernel().getCstep().getC10().toArray());
            MAPPING.set("c11", double[].class, source -> source.getKernel().getCstep().getC11().toArray());
            MAPPING.set("c12", double[].class, source -> source.getKernel().getCstep().getC12().toArray());
            MAPPING.set("c13", double[].class, source -> source.getKernel().getCstep().getC13().toArray());
            MAPPING.set("c17", double[].class, source -> source.getKernel().getCstep().getC17().toArray());
            MAPPING.set("c20", double[].class, source -> source.getKernel().getCstep().getC20().toArray());
            MAPPING.set("d1", double[].class, source -> source.getKernel().getDstep().getD1().toArray());
            MAPPING.set("d2", double[].class, source -> source.getKernel().getDstep().getD2().toArray());
            MAPPING.set("d4", double[].class, source -> source.getKernel().getDstep().getD4().toArray());
            MAPPING.set("d5", double[].class, source -> source.getKernel().getDstep().getD5().toArray());
            MAPPING.set("d6", double[].class, source -> source.getKernel().getDstep().getD6().toArray());
            MAPPING.set("d7", double[].class, source -> source.getKernel().getDstep().getD7().toArray());
            MAPPING.set("d8", double[].class, source -> source.getKernel().getDstep().getD8().toArray());
            MAPPING.set("d9", double[].class, source -> source.getKernel().getDstep().getD9().toArray());
            MAPPING.set("d10", double[].class, source -> source.getKernel().getDstep().getD10().toArray());
            MAPPING.set("d11", double[].class, source -> source.getKernel().getDstep().getD11().toArray());
            MAPPING.set("d12", double[].class, source -> source.getKernel().getDstep().getD12().toArray());
            MAPPING.set("d13", double[].class, source -> source.getKernel().getDstep().getD13().toArray());
            MAPPING.set("d10bis", double[].class, source -> source.getKernel().getDstep().getD10bis().toArray());
            MAPPING.set("d11bis", double[].class, source -> source.getKernel().getDstep().getD11bis().toArray());
            MAPPING.set(MUL, Boolean.class, source -> source.isMultiplicative());
        }
    }

    public Results process(double[] data, double period, boolean mul, int trendHorizon, int pdegree, 
            String pkernel, String asymmetric, String seas0, String seas1, double lsig, double usig) {
        int iperiod = (int) period;
        Number P;
        if (Math.abs(period - iperiod) < 1e-9) {
            P = iperiod;
        } else {
            P = period;
        }
        LocalPolynomialFilterSpec tspec= LocalPolynomialFilterSpec.builder()
                .filterHorizon(trendHorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.valueOf(asymmetric))
                .build();
        
        X11Context context = X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(P)
                .trendFiltering(LocalPolynomialFilters.of(tspec))
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(P, SeasonalFilterOption.valueOf(seas0)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(P, SeasonalFilterOption.valueOf(seas1)))
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        X11Kernel kernel = new X11Kernel();
        DoubleSeq y = DoubleSeq.of(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }

    public Results lpX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel, 
            int adegree, double[] aparams, double tweight, double passBand,
            int shorizon, String seasKernel, double lsig, double usig) {
        LocalPolynomialFilterSpec tspec= LocalPolynomialFilterSpec.builder()
                .filterHorizon(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.MMSRE)
                .asymmetricPolynomialDegree(adegree)
                .linearModelCoefficients(aparams)
                .timelinessWeight(tweight)
                .passBand(passBand)
                .build();
        
        ISymmetricFiltering sfilter = X11SeasonalFiltersFactory.filter(period, shorizon, kernel(seasKernel, shorizon));
        
        X11Context context = X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFiltering(LocalPolynomialFilters.of(tspec))
                .initialSeasonalFiltering(sfilter)
                .finalSeasonalFiltering(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        X11Kernel kernel = new X11Kernel();
        DoubleSeq y = DoubleSeq.of(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }

    public Results dafX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel, 
            int shorizon, String seasKernel, double lsig, double usig) {
        LocalPolynomialFilterSpec tspec= LocalPolynomialFilterSpec.builder()
                .filterHorizon(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.Direct)
                .build();
        
        ISymmetricFiltering sfilter = X11SeasonalFiltersFactory.filter(period, shorizon, kernel(seasKernel, shorizon));
        
        X11Context context = X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFiltering(LocalPolynomialFilters.of(tspec))
                .initialSeasonalFiltering(sfilter)
                .finalSeasonalFiltering(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        X11Kernel kernel = new X11Kernel();
        DoubleSeq y = DoubleSeq.of(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }

    public Results cnX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel, 
            int shorizon, String seasKernel, double lsig, double usig) {
        LocalPolynomialFilterSpec tspec= LocalPolynomialFilterSpec.builder()
                .filterHorizon(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.CutAndNormalize)
                .build();
        
        ISymmetricFiltering sfilter = X11SeasonalFiltersFactory.filter(period, shorizon, kernel(seasKernel, shorizon));
        
        X11Context context = X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFiltering(LocalPolynomialFilters.of(tspec))
                .initialSeasonalFiltering(sfilter)
                .finalSeasonalFiltering(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        X11Kernel kernel = new X11Kernel();
        DoubleSeq y = DoubleSeq.of(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }

    public Results rkhsX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel, 
            boolean optimalbw, String criterion, boolean rwdensity, double passBand,
            int shorizon, String seasKernel, double lsig, double usig) {
        RKHSFilterSpec tspec=RKHSFilterSpec.builder()
                .filterLength(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .optimalBandWidth(optimalbw)
                .asymmetricBandWith(AsymmetricCriterion.valueOf(criterion))
                .density(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined)
                .passBand(passBand)
                .minBandWidth(thorizon)
                .maxBandWidth(3*thorizon)
                .build();
        
        ISymmetricFiltering sfilter = X11SeasonalFiltersFactory.filter(period, shorizon, kernel(seasKernel, shorizon));
        
        X11Context context = X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFiltering(RKHSFilterFactory.of(tspec))
                .initialSeasonalFiltering(sfilter)
                .finalSeasonalFiltering(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        X11Kernel kernel = new X11Kernel();
        DoubleSeq y = DoubleSeq.of(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }
    public Results trendX11(double[] data, double period, boolean mul,
    		DoubleSeq ctrendf, Matrix ltrendf, String seas0, String seas1, double lsig, double usig) {
        int iperiod = (int) period;
        Number P;
        if (Math.abs(period - iperiod) < 1e-9) {
            P = iperiod;
        } else {
            P = period;
        }
        Filtering trendFilter=Filtering.of(ctrendf, ltrendf);
        
        X11Context context = X11Context.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(P)
                .trendFiltering(trendFilter)
                .initialSeasonalFiltering(X11SeasonalFiltersFactory.filter(P, SeasonalFilterOption.valueOf(seas0)))
                .finalSeasonalFiltering(X11SeasonalFiltersFactory.filter(P, SeasonalFilterOption.valueOf(seas1)))
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        X11Kernel kernel = new X11Kernel();
        DoubleSeq y = DoubleSeq.of(data);
        kernel.process(y, context);

        return Results.builder()
                .y(y)
                .kernel(kernel)
                .multiplicative(mul)
                .build();

    }


    // diagnostics
    public double icratio(double[] s, double[] sc, boolean mul) {
        DoubleSeq SC = DoubleSeq.of(sc);
        double gc = SeriesEvolution.calcAbsMeanVariation(SC, 1, mul);
        double gi = SeriesEvolution.calcAbsMeanVariation(mul ? DoubleSeq.onMapping(s.length, i -> s[i] / sc[i])
                : DoubleSeq.onMapping(s.length, i -> s[i] - sc[i]), 1, mul);
        return gi / gc;
    }

    public double[] icratios(double[] s, double[] sc, int n, boolean mul) {
        DoubleSeq SC = DoubleSeq.of(sc);
        double[] gc = SeriesEvolution.calcAbsMeanVariations(SC, n, mul);
        double[] gi = SeriesEvolution.calcAbsMeanVariations(mul ? DoubleSeq.onMapping(s.length, i -> s[i] / sc[i])
                : DoubleSeq.onMapping(s.length, i -> s[i] - sc[i]), n, mul);
        double[] icr = new double[n];
        for (int i = 0; i < n; ++i) {
            icr[i] = gi[i] / gc[i];
        }
        return icr;
    }

    public double[] henderson(double[] s, int length, boolean musgrave, double ic) {
        SymmetricFilter filter = HendersonFilters.ofLength(length);
        int ndrop = filter.length() / 2;

        double[] x = new double[s.length];
        Arrays.fill(x, Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(DoubleSeq.of(s), out);
        if (musgrave) {
            // apply the musgrave filters
            IFiniteFilter[] f = MusgraveFilterFactory.makeFilters(filter, ic);
            AsymmetricEndPoints aep = new AsymmetricEndPoints(f, 0);
            aep.process(DoubleSeq.of(s), DataBlock.of(x));
        }
        return x;
    }
}
