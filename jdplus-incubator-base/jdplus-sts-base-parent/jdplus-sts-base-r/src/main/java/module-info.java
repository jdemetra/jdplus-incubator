module jdplus.sts.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.sts.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.sts.base.core;
    requires jdplus.sts.base.io;
    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.protobuf;

    exports jdplus.sts.base.r;
}