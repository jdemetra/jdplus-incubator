module jdplus.x12plus.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.api;
    requires jdplus.filters.base.api;
    requires jdplus.filters.base.core;
    requires jdplus.x12plus.base.api;
    requires jdplus.x12plus.base.core;

    exports jdplus.x12plus.base.r;
}