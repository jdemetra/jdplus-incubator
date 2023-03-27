module jdplus.highfreq.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.highfreq.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.highfreq.extendedairline;
    exports jdplus.highfreq.extractors;
    exports jdplus.highfreq.regarima;
    exports jdplus.ssf.extractors;
    exports jdplus.highfreq.extendedairline.decomposiiton;

    provides demetra.information.InformationExtractor with
            jdplus.highfreq.extractors.ExtendedAirlineExtractor,
            jdplus.highfreq.extractors.FractionalAirlineDecompositionExtractor,
            jdplus.highfreq.extractors.HighFreqRegArimaExtractor,
            jdplus.highfreq.extractors.FractionalAirlineEstimationExtractor;
}