module jdplus.advancedsa.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires jdplus.toolkit.base.api;

    exports jdplus.advancedsa.base.api.modelling;
    exports jdplus.advancedsa.base.api.movingtd;
}