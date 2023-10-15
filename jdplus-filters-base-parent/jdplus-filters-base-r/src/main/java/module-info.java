module jdplus.experimentalsa.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.filters.base.api;
    requires jdplus.filters.base.core;
    requires jdplus.toolkit.base.core;

    exports jdplus.filters.base.r;
}