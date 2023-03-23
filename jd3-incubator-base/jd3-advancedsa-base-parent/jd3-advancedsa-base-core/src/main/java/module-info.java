module jdplus.advancedsa.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.advancedsa.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.sts.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sts.base.core;
    requires jdplus.sa.base.core;
    requires jdplus.x13.base.api;
    requires jdplus.tramoseats.base.api;

    exports jdplus.sa.advanced;
}