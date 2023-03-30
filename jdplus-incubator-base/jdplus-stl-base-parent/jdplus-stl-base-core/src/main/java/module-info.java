import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.stl.base.core.mstlplus.MStlPlusFactory;
import jdplus.stl.base.core.stlplus.StlPlusFactory;
import jdplus.stl.base.core.stlplus.extractors.MStlExtractor;
import jdplus.stl.base.core.stlplus.extractors.MStlPlusExtractor;
import jdplus.stl.base.core.stlplus.extractors.StlExtractor;
import jdplus.stl.base.core.stlplus.extractors.StlPlusExtractor;
import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.stl.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.stl.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;
    requires jdplus.highfreq.base.core;
    requires jdplus.sa.base.core;
    requires jdplus.toolkit.base.core;

    exports jdplus.stl.base.core.mstlplus;
    exports jdplus.stl.base.core.stlplus;
    exports jdplus.stl.base.core.stlplus.extractors;
    exports jdplus.stl.base.core;

    provides InformationExtractor with
            StlExtractor,
            StlPlusExtractor,
            MStlPlusExtractor,
            MStlExtractor;

    provides SaProcessingFactory with
            StlPlusFactory,
            MStlPlusFactory;
}