import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.x12plus.base.core.extractors.X11plusExtractor;
import jdplus.x12plus.base.core.extractors.X12plusExtractor;
import jdplus.x12plus.base.core.x12.X12plusFactory;

module jdplus.x12plus.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.api;
    requires jdplus.sa.base.core;
    requires jdplus.advancedsa.base.core;
    requires jdplus.filters.base.core;
    requires jdplus.x12plus.base.api;

    provides SaProcessingFactory with
            X12plusFactory;
    provides InformationExtractor with
            X12plusExtractor, X11plusExtractor;
    
    exports jdplus.x12plus.base.core;
    exports jdplus.x12plus.base.core.extractors;
    exports jdplus.x12plus.base.core.x12;
}


