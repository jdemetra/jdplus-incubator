module jdplus.stl.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires jdplus.highfreq.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;

    exports jdplus.stl.base.api;
}