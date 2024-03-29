/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl.base.core.stlplus.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.InformationExtractor;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.stl.base.api.StlDictionaries;
import jdplus.stl.base.core.MStlResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class MStlExtractor extends InformationMapping<MStlResults> {

    private static DoubleSeq lin(MStlResults rslt, DoubleSeq s) {
        if (rslt.isMultiplicative()) {
            return s.log();
        } else {
            return s;
        }
    }

    public MStlExtractor() {
        set(SaDictionaries.Y_LIN, double[].class, source -> lin(source, source.getSeries()).toArray());
        set(SaDictionaries.T_LIN, double[].class, source -> lin(source, source.getTrend()).toArray());
        set(SaDictionaries.SA_LIN, double[].class, source -> lin(source, source.getSa()).toArray());
        set(SaDictionaries.S_LIN, double[].class, source -> lin(source, source.seasonal()).toArray());
        set(SaDictionaries.I_LIN, double[].class, source -> lin(source, source.getIrregular()).toArray());
        set(SaDictionaries.Y_CMP, double[].class, source -> source.getSeries().toArray());
        set(SaDictionaries.T_CMP, double[].class, source -> source.getTrend().toArray());
        set(SaDictionaries.SA_CMP, double[].class, source -> source.getSa().toArray());
        set(SaDictionaries.S_CMP, double[].class, source -> source.seasonal().toArray());
        set(SaDictionaries.I_CMP, double[].class, source -> source.getIrregular().toArray());
        set(StlDictionaries.WEIGHTS, double[].class, source -> source.getWeights().toArray());
        set(StlDictionaries.FIT, double[].class, source -> source.getFit().toArray());
    }

    @Override
    public Class<MStlResults> getSourceClass() {
        return MStlResults.class;
    }

}
