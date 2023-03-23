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

    exports jdplus.mstlplus;
    exports jdplus.stl;
    exports jdplus.stlplus;
    exports jdplus.stlplus.extractors;

    provides demetra.information.InformationExtractor with
            jdplus.stlplus.extractors.StlExtractor,
            jdplus.stlplus.extractors.StlPlusExtractor,
            jdplus.stlplus.extractors.MStlPlusExtractor,
            jdplus.stlplus.extractors.MStlExtractor;

    provides demetra.sa.SaProcessingFactory with
            jdplus.stlplus.StlPlusFactory,
            jdplus.mstlplus.MStlPlusFactory;
}