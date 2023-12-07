module jdplus.advancedsa.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.api;
    requires jdplus.advancedsa.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.advancedsa.base.core.regarima;
    exports jdplus.advancedsa.base.core.movingtd;
}
