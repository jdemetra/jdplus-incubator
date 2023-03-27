module jdplus.sts.base.io {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.sts.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.protobuf;
    requires jdplus.sts.base.core;
    requires jdplus.sa.base.protobuf;

    exports demetra.sts.io.protobuf;
    exports demetra.sts.outliers.io.protobuf;
}