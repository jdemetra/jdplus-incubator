import jdplus.jvsqr.base.core.JvsMatrixOutputFactory;
import jdplus.sa.base.api.SaOutputFactory;

module jdplus.jvsqr.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires nbbrd.picocsv;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.jvsqr.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.jvsqr.base.core;

    provides SaOutputFactory with JvsMatrixOutputFactory;
}


