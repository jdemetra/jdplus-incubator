import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.sts.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.sts.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.api;
    requires jdplus.sa.base.core;

    exports jdplus.sts.base.core.msts;
    exports jdplus.sts.base.core.msts.internal;
    exports jdplus.sts.base.core.msts.survey;
    exports jdplus.sts.base.core.extractors;
    exports jdplus.sts.base.core;

    provides InformationExtractor with
            jdplus.sts.base.core.extractors.BasicStructuralModelExtractor;
}