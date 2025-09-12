import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.advancedsa.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.advancedsa.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.advancedsa.base.core.regarima;
    exports jdplus.advancedsa.base.core.movingtd;
    exports jdplus.advancedsa.base.core.tdarima;
    exports jdplus.advancedsa.base.core.tdarima.extractors;
    
        provides InformationExtractor with
            jdplus.advancedsa.base.core.tdarima.extractors.LtdArimaResultsExtractor;

}


