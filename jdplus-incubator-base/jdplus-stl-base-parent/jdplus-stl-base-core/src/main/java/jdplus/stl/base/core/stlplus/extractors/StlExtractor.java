/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl.base.core.stlplus.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.stl.base.api.StlDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.stl.base.core.StlResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class StlExtractor extends InformationMapping<StlResults> {

    private static final TsData lin(StlResults rslt, TsData s) {
        if (rslt.isMultiplicative()) {
            return s.log();
        } else {
            return s;
        }
    }

    public StlExtractor() {
        set(SaDictionaries.Y_LIN, TsData.class, source -> lin(source, source.getSeries()));
        set(SaDictionaries.T_LIN, TsData.class, source -> lin(source, source.getTrend()));
        set(SaDictionaries.SA_LIN, TsData.class, source -> lin(source, source.getSa()));
        set(SaDictionaries.S_LIN, TsData.class, source -> lin(source, source.seasonal()));
        set(SaDictionaries.I_LIN, TsData.class, source -> lin(source, source.getIrregular()));
        set(SaDictionaries.Y_CMP, TsData.class, source -> source.getSeries());
        set(SaDictionaries.T_CMP, TsData.class, source -> source.getTrend());
        set(SaDictionaries.SA_CMP, TsData.class, source -> source.getSa());
        set(SaDictionaries.S_CMP, TsData.class, source -> source.seasonal());
        set(SaDictionaries.I_CMP, TsData.class, source -> source.getIrregular());
        set(StlDictionaries.WEIGHTS, TsData.class, source -> source.getWeights());
        set(StlDictionaries.FIT, TsData.class, source -> source.getFit());
    }

    @Override
    public Class<StlResults> getSourceClass() {
        return StlResults.class;
    }

}
