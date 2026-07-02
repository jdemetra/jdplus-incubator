module jdplus.eurostat.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires static jdplus.toolkit.base.api;
    requires static jdplus.sa.base.api;
   
    exports jdplus.eurostat.base.api;
}