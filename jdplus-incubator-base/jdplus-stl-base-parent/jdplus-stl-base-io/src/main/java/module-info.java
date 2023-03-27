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

    exports jdplus.stl.io.information;
    exports jdplus.stl.workspace;

    provides demetra.sa.io.information.SaSpecificationMapping with
            jdplus.stl.io.information.StlPlusSpecMapping.Serializer;

    provides demetra.workspace.file.spi.FamilyHandler with
            jdplus.stl.workspace.StlHandlers.DocStlPlus;
}