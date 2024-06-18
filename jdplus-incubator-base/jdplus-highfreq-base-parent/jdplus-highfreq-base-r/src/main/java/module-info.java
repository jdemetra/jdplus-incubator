module jdplus.highfreq.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.highfreq.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.highfreq.base.core;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.highfreq.base.r;
}