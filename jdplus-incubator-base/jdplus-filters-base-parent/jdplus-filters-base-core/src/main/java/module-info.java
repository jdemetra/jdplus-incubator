module jdplus.filters.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.filters.base.api;

    exports jdplus.filters.base.core;
}
