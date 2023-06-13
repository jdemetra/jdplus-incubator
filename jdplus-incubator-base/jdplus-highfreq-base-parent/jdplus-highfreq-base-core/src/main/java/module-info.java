import jdplus.highfreq.base.core.extractors.ExtendedAirlineExtractor;
import jdplus.highfreq.base.core.extractors.FractionalAirlineDecompositionExtractor;
import jdplus.highfreq.base.core.extractors.FractionalAirlineEstimationExtractor;
import jdplus.highfreq.base.core.extractors.HighFreqRegArimaExtractor;
import jdplus.toolkit.base.api.information.InformationExtractor;

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

    exports jdplus.highfreq.base.core.extendedairline;
    exports jdplus.highfreq.base.core.extractors;
    exports jdplus.highfreq.base.core.regarima;
    exports jdplus.highfreq.base.core.ssf.extractors;
    exports jdplus.highfreq.base.core.extendedairline.decomposition;

    provides InformationExtractor with
            ExtendedAirlineExtractor,
            FractionalAirlineDecompositionExtractor,
            HighFreqRegArimaExtractor,
            FractionalAirlineEstimationExtractor;
}