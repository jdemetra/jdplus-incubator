package jdplus.x12plus.base.r;

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
import jdplus.filters.base.api.AsymmetricCriterion;
import jdplus.filters.base.api.SpectralDensity;
import jdplus.filters.base.api.RKHSFilterSpec;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.math.linearfilters.AsymmetricFilterOption;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.math.linearfilters.KernelOption;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.UserDefinedSymmetricFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.Filtering;
import jdplus.toolkit.base.core.math.linearfilters.FiltersToolkit;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFiltering;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.x12plus.base.api.GenericSeasonalFilterSpec;
import jdplus.x12plus.base.core.AsymmetricEndPoints;
import jdplus.x12plus.base.core.MusgraveFilterFactory;
import jdplus.x12plus.base.api.SeasonalFilterOption;
import jdplus.x12plus.base.api.X11SeasonalFilterSpec;
import jdplus.x12plus.base.api.X11plusSpec;
import jdplus.x12plus.base.core.SeriesEvolution;
import jdplus.x12plus.base.core.RawX11Kernel;
import jdplus.x12plus.base.core.RawX11Results;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class X11Decomposition {

    // TODO: suppress in version 3.1.2 of the main
    private static SymmetricFiltering of(UserDefinedSymmetricFilterSpec uspec) {
        DoubleSeq cf = uspec.getCentralFilter();
        Matrix rf = uspec.getEndPointsFilters();
        int l = rf.getColumnsCount();
        if (cf.length() != 2 * l + 1) {
            throw new IllegalArgumentException();
        }
        SymmetricFilter fcf = SymmetricFilter.of(cf);
        IFiniteFilter[] frf = new IFiniteFilter[l];
        for (int i = 0; i < l; ++i) {
            double[] p = rf.column(i).drop(0, i).toArray();
            FiniteFilter f = FiniteFilter.of(p, -l);
            frf[i] = f;
        }
        return new SymmetricFiltering(fcf, frf);
    }
    

    static {
        FiltersToolkit.register(UserDefinedSymmetricFilterSpec.class, spec -> {
            UserDefinedSymmetricFilterSpec uspec = spec;
            return of(uspec);
        });
    }
    
    // END TODO

    @lombok.Value
    @lombok.Builder
    public static class Results implements GenericExplorable {

        boolean multiplicative;
        DoubleSeq y;
        RawX11Results details;

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
            MAPPING.set("b1", double[].class, source -> source.getDetails().getB1().toArray());
            MAPPING.set("b2", double[].class, source -> source.getDetails().getB2().toArray());
            MAPPING.set("b3", double[].class, source -> source.getDetails().getB3().toArray());
            MAPPING.set("b4", double[].class, source -> source.getDetails().getB4().toArray());
            MAPPING.set("b5", double[].class, source -> source.getDetails().getB5().toArray());
            MAPPING.set("b6", double[].class, source -> source.getDetails().getB6().toArray());
            MAPPING.set("b7", double[].class, source -> source.getDetails().getB7().toArray());
            MAPPING.set("b8", double[].class, source -> source.getDetails().getB8().toArray());
            MAPPING.set("b9", double[].class, source -> source.getDetails().getB9().toArray());
            MAPPING.set("b10", double[].class, source -> source.getDetails().getB10().toArray());
            MAPPING.set("b11", double[].class, source -> source.getDetails().getB11().toArray());
            MAPPING.set("b13", double[].class, source -> source.getDetails().getB13().toArray());
            MAPPING.set("b17", double[].class, source -> source.getDetails().getB17().toArray());
            MAPPING.set("b20", double[].class, source -> source.getDetails().getB20().toArray());
            MAPPING.set("c1", double[].class, source -> source.getDetails().getC1().toArray());
            MAPPING.set("c2", double[].class, source -> source.getDetails().getC2().toArray());
            MAPPING.set("c4", double[].class, source -> source.getDetails().getC4().toArray());
            MAPPING.set("c5", double[].class, source -> source.getDetails().getC5().toArray());
            MAPPING.set("c6", double[].class, source -> source.getDetails().getC6().toArray());
            MAPPING.set("c7", double[].class, source -> source.getDetails().getC7().toArray());
            MAPPING.set("c9", double[].class, source -> source.getDetails().getC9().toArray());
            MAPPING.set("c10", double[].class, source -> source.getDetails().getC10().toArray());
            MAPPING.set("c11", double[].class, source -> source.getDetails().getC11().toArray());
            MAPPING.set("c13", double[].class, source -> source.getDetails().getC13().toArray());
            MAPPING.set("c17", double[].class, source -> source.getDetails().getC17().toArray());
            MAPPING.set("c20", double[].class, source -> source.getDetails().getC20().toArray());
            MAPPING.set("d1", double[].class, source -> source.getDetails().getD1().toArray());
            MAPPING.set("d2", double[].class, source -> source.getDetails().getD2().toArray());
            MAPPING.set("d4", double[].class, source -> source.getDetails().getD4().toArray());
            MAPPING.set("d5", double[].class, source -> source.getDetails().getD5().toArray());
            MAPPING.set("d6", double[].class, source -> source.getDetails().getD6().toArray());
            MAPPING.set("d7", double[].class, source -> source.getDetails().getD7().toArray());
            MAPPING.set("d8", double[].class, source -> source.getDetails().getD8().toArray());
            MAPPING.set("d10", double[].class, source -> source.getDetails().getD10().toArray());
            MAPPING.set("d11", double[].class, source -> source.getDetails().getD11().toArray());
            MAPPING.set("d12", double[].class, source -> source.getDetails().getD12().toArray());
            MAPPING.set("d13", double[].class, source -> source.getDetails().getD13().toArray());
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
        LocalPolynomialFilterSpec tspec = LocalPolynomialFilterSpec.builder()
                .filterHorizon(trendHorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.valueOf(asymmetric))
                .build();

        X11plusSpec spec = X11plusSpec.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(P)
                .trendFilter(tspec)
                .initialSeasonalFilter(new X11SeasonalFilterSpec(P, SeasonalFilterOption.valueOf(seas0)))
                .finalSeasonalFilter(new X11SeasonalFilterSpec(P, SeasonalFilterOption.valueOf(seas1)))
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        RawX11Kernel kernel = new RawX11Kernel(spec);
        DoubleSeq y = DoubleSeq.of(data);
        RawX11Results rslts = kernel.process(y);

        return Results.builder()
                .y(y)
                .details(rslts)
                .multiplicative(mul)
                .build();

    }

    public Results lpX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel,
            int adegree, double[] aparams, double tweight, double passBand,
            int shorizon, String seasKernel, double lsig, double usig) {
        LocalPolynomialFilterSpec tspec = LocalPolynomialFilterSpec.builder()
                .filterHorizon(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.MMSRE)
                .asymmetricPolynomialDegree(adegree)
                .linearModelCoefficients(aparams)
                .timelinessWeight(tweight)
                .passBand(passBand)
                .build();

        GenericSeasonalFilterSpec sfilter = new GenericSeasonalFilterSpec(period,
                LocalPolynomialFilterSpec.defaultSeasonalSpec(shorizon, KernelOption.valueOf(seasKernel)));

        X11plusSpec spec = X11plusSpec.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFilter(tspec)
                .initialSeasonalFilter(sfilter)
                .finalSeasonalFilter(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        RawX11Kernel kernel = new RawX11Kernel(spec);
        DoubleSeq y = DoubleSeq.of(data);
        RawX11Results rslts = kernel.process(y);

        return Results.builder()
                .y(y)
                .details(rslts)
                .multiplicative(mul)
                .build();

    }

    public Results dafX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel,
            int shorizon, String seasKernel, double lsig, double usig) {
        LocalPolynomialFilterSpec tspec = LocalPolynomialFilterSpec.builder()
                .filterHorizon(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.Direct)
                .build();

        GenericSeasonalFilterSpec sfilter = new GenericSeasonalFilterSpec(period,
                LocalPolynomialFilterSpec.defaultSeasonalSpec(shorizon, KernelOption.valueOf(seasKernel)));

        X11plusSpec spec = X11plusSpec.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFilter(tspec)
                .initialSeasonalFilter(sfilter)
                .finalSeasonalFilter(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        RawX11Kernel kernel = new RawX11Kernel(spec);
        DoubleSeq y = DoubleSeq.of(data);
        RawX11Results rslts = kernel.process(y);

        return Results.builder()
                .y(y)
                .details(rslts)
                .multiplicative(mul)
                .build();

    }

    public Results cnX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel,
            int shorizon, String seasKernel, double lsig, double usig) {
        LocalPolynomialFilterSpec tspec = LocalPolynomialFilterSpec.builder()
                .filterHorizon(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .asymmetricFilters(AsymmetricFilterOption.CutAndNormalize)
                .build();

        GenericSeasonalFilterSpec sfilter = new GenericSeasonalFilterSpec(period,
                LocalPolynomialFilterSpec.defaultSeasonalSpec(shorizon, KernelOption.valueOf(seasKernel)));

        X11plusSpec spec = X11plusSpec.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFilter(tspec)
                .initialSeasonalFilter(sfilter)
                .finalSeasonalFilter(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        RawX11Kernel kernel = new RawX11Kernel(spec);
        DoubleSeq y = DoubleSeq.of(data);
        RawX11Results rslts = kernel.process(y);

        return Results.builder()
                .y(y)
                .details(rslts)
                .multiplicative(mul)
                .build();

    }

    public Results rkhsX11(double[] data, int period, boolean mul, int thorizon, int pdegree, String pkernel,
            boolean optimalbw, String criterion, boolean rwdensity, double passBand,
            int shorizon, String seasKernel, double lsig, double usig) {
        RKHSFilterSpec tspec = RKHSFilterSpec.builder()
                .filterLength(thorizon)
                .polynomialDegree(pdegree)
                .kernel(KernelOption.valueOf(pkernel))
                .optimalBandWidth(optimalbw)
                .asymmetricBandWith(AsymmetricCriterion.valueOf(criterion))
                .density(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined)
                .passBand(passBand)
                .minBandWidth(thorizon)
                .maxBandWidth(3 * thorizon)
                .build();

        GenericSeasonalFilterSpec sfilter = new GenericSeasonalFilterSpec(period,
                LocalPolynomialFilterSpec.defaultSeasonalSpec(shorizon, KernelOption.valueOf(seasKernel)));

        X11plusSpec spec = X11plusSpec.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .trendFilter(tspec)
                .initialSeasonalFilter(sfilter)
                .finalSeasonalFilter(sfilter)
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        RawX11Kernel kernel = new RawX11Kernel(spec);
        DoubleSeq y = DoubleSeq.of(data);
        RawX11Results rslts = kernel.process(y);

        return Results.builder()
                .y(y)
                .details(rslts)
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
        UserDefinedSymmetricFilterSpec tspec = new UserDefinedSymmetricFilterSpec(ctrendf, ltrendf);

        X11plusSpec spec = X11plusSpec.builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(P)
                .trendFilter(tspec)
                .initialSeasonalFilter(new X11SeasonalFilterSpec(P, SeasonalFilterOption.valueOf(seas0)))
                .finalSeasonalFilter(new X11SeasonalFilterSpec(P, SeasonalFilterOption.valueOf(seas1)))
                .lowerSigma(lsig)
                .upperSigma(usig)
                .build();
        RawX11Kernel kernel = new RawX11Kernel(spec);
        DoubleSeq y = DoubleSeq.of(data);
        RawX11Results rslts = kernel.process(y);

        return Results.builder()
                .y(y)
                .details(rslts)
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
