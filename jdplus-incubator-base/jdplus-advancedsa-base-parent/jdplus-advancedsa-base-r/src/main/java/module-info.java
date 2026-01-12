import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.advancedsa.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.advancedsa.base.api;
    requires jdplus.advancedsa.base.core;
    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.r;

    exports jdplus.advancedsa.base.r;
    
    provides InformationExtractor with
            jdplus.advancedsa.base.r.TdArimaDecompositionExtractor;

}


