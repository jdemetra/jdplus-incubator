import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.x11plus.base.core.extractors.X11plusExtractor;
import jdplus.x11plus.base.core.extractors.X13plusExtractor;
import jdplus.x11plus.base.core.x13.X13plusFactory;

module jdplus.x11plus.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.api;
    requires jdplus.sa.base.core;
    requires jdplus.filters.base.core;
    requires jdplus.x11plus.base.api;

    provides SaProcessingFactory with
            X13plusFactory;
    provides InformationExtractor with
            X13plusExtractor, X11plusExtractor;
    
    exports jdplus.x11plus.base.core;
    exports jdplus.x11plus.base.core.extractors;
    exports jdplus.x11plus.base.core.x13;
}


