module jdplus.advancedsa.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires jdplus.sa.base.api;

    exports jdplus.advancedsa.base.api.modelling;
    exports jdplus.advancedsa.base.api.movingtd;
    exports jdplus.advancedsa.base.api.tdarima;
}