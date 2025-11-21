module jdplus.sts.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires transitive jdplus.toolkit.base.api;

    requires jdplus.sa.base.api;
    requires jdplus.advancedsa.base.api;

    exports jdplus.sts.base.api;
}