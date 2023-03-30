import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.stl.base.io.information.StlPlusSpecMapping;
import jdplus.stl.base.io.workspace.StlHandlers;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;

module jdplus.stl.base.io {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.stl.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.stl.base.core;
    requires jdplus.sa.base.api;
    requires jdplus.sa.base.information;

    exports jdplus.stl.base.io.information;
    exports jdplus.stl.base.io.workspace;

    provides SaSpecificationMapping with
            StlPlusSpecMapping.Serializer;

    provides FamilyHandler with
            StlHandlers.DocStlPlus;
}