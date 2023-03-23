module jdplus.sts.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.sts.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.api;

    exports jdplus.msts;
    exports jdplus.msts.internal;
    exports jdplus.msts.survey;
    exports jdplus.sts;
    exports jdplus.sts.extractors;

    provides demetra.information.InformationExtractor with
            jdplus.sts.extractors.BasicStructuralModelExtractor;
}