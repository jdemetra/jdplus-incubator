
module jdplus.x12plus.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    
    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;
    requires jdplus.advancedsa.base.api;
    requires jdplus.highfreq.base.api;


    exports jdplus.x12plus.base.api;
}


