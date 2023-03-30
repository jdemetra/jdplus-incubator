module jdplus.stl.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.stl.base.api;
    requires jdplus.stl.base.core;
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;

    exports jdplus.stl.base.r;
}