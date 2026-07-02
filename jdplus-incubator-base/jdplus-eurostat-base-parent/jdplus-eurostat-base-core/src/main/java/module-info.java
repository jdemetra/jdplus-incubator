import jdplus.eurostat.base.core.JvsMatrixOutputFactory;
import jdplus.sa.base.api.SaOutputFactory;

module jdplus.eurostat.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires nbbrd.picocsv;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.eurostat.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.eurostat.base.core;

    provides SaOutputFactory with JvsMatrixOutputFactory;
}


