import jdplus.highfreq.base.api.ExtendedAirlineAlgorithms;
import jdplus.highfreq.base.api.HighFreqDiagnosticsFactory;
import jdplus.highfreq.base.api.HighFreqOutputFactory;
import jdplus.highfreq.base.api.HighFreqProcessingFactory;

module jdplus.highfreq.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires jdplus.toolkit.base.api;
    requires jdplus.sa.base.api;

    exports jdplus.highfreq.base.api;

    uses HighFreqProcessingFactory;
    uses HighFreqDiagnosticsFactory;
    uses HighFreqOutputFactory;
    uses jdplus.sa.base.api.diagnostics.SeasonalityTests.Factory;
    uses ExtendedAirlineAlgorithms.Processor;
}