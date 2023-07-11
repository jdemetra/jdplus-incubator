module jdplus.x11plus.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.api;
    requires jdplus.filters.base.api;
    requires jdplus.filters.base.core;
    requires jdplus.x11plus.base.api;
    requires jdplus.x11plus.base.core;

    exports jdplus.x11plus.base.r;
}