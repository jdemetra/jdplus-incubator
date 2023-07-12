import jdplus.highfreq.base.io.workspace.ExtendedAirlineHandlers;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;

module jdplus.highfreq.base.io {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.sa.base.information;
    requires jdplus.highfreq.base.api;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.highfreq.base.core;
    
    provides FamilyHandler with
            ExtendedAirlineHandlers.DecompositionModel, ExtendedAirlineHandlers.DocModel;
    exports jdplus.highfreq.base.io.information;
    exports jdplus.highfreq.base.io.workspace;
}

