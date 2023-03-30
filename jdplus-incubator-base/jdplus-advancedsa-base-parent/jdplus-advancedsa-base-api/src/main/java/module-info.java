module jdplus.advancedsa.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;
    requires jdplus.sts.base.api;

    exports jdplus.advancedsa.base.api;
}