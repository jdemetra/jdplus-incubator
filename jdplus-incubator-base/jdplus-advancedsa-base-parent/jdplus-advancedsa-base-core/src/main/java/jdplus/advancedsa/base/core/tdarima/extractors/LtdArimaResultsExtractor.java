/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.core.tdarima.extractors;

import jdplus.advancedsa.base.api.tdarima.LtdDictionaries;
import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class LtdArimaResultsExtractor extends InformationMapping<LtdArimaResults> {

    @Override
    public Class<LtdArimaResults> getSourceClass() {
        return LtdArimaResults.class;
    }

    public LtdArimaResultsExtractor() {
        set(modelItem(LtdDictionaries.PARAMETERS_FIXED), double[].class, s -> s.getStartMax().getParameters().toArray());
        set(modelItem(LtdDictionaries.PARAMETERS_ALL), double[].class, s -> s.getMax().getParameters().toArray());
        set(modelItem(LtdDictionaries.PARAMETERS_V0), Double.class, s -> s.v0());
        set(modelItem(LtdDictionaries.PARAMETERS_V1), Double.class, s -> s.v1());
        set(modelItem(LtdDictionaries.PARAMETERS_P0), double[].class, s -> s.getModel().getP0().toArray());
        set(modelItem(LtdDictionaries.PARAMETERS_P1), double[].class, s -> s.getModel().getP1().toArray());
        set(modelItem(LtdDictionaries.PARAMETERS_MEAN), double[].class, s -> {
            DoubleSeq p0 = s.getModel().getP0();
            DoubleSeq p1 = s.getModel().getP1();
            int n = p0.length();
            return DoubleSeq.onMapping(n, i -> (p0.get(i) + p1.get(i)) / 2).toArray();
        });
        set(modelItem(LtdDictionaries.PARAMETERS_DELTA), double[].class, s -> {
            DoubleSeq p0 = s.getModel().getP0();
            DoubleSeq p1 = s.getModel().getP1();
            int n = s.getModel().getN() - 1;
            return DoubleSeq.onMapping(p0.length(), i -> (p1.get(i) - p0.get(i)) / n).toArray();
        });
        set(modelItem(LtdDictionaries.PARAMETERS_COV), Matrix.class, source -> {
            try {
                return source.getMax().asymptoticCovariance();
            } catch (MatrixException err) {
                return null;
            }
        });
        set(mlItem("information1"), Matrix.class, source -> source.getMax().getInformation());
        set(mlItem("score1"), double[].class, source -> source.getMax().getScore().toArray());
        set(modelItem(LtdDictionaries.PARAMETERS_FIXED_COV), Matrix.class, source -> {
            try {
                return source.getStartMax().asymptoticCovariance();
            } catch (MatrixException err) {
                return null;
            }
        });

        delegate("ll0", LikelihoodStatistics.class, source -> source.getLl0());
        delegate("ll1", LikelihoodStatistics.class, source -> source.getLl1());

        set(regItem(LtdDictionaries.REGS_C0), double[].class, s -> s.getCoefficients0().isEmpty() ? null : s.getCoefficients0().toArray());
        set(regItem(LtdDictionaries.REGS_COV0), Matrix.class, s -> s.getCovariance0().isEmpty() ? null : s.getCovariance0());
        set(regItem(LtdDictionaries.REGS_EFFECT0), double[].class, s -> s.getRegsEffect0().isEmpty() ? null : s.getRegsEffect0().toArray());
        set(regItem(LtdDictionaries.Y_LIN0), double[].class, s -> s.getLinearizedSeries0().toArray());
        set(regItem(LtdDictionaries.REGS_C1), double[].class, s -> s.getCoefficients1().isEmpty() ? null : s.getCoefficients1().toArray());
        set(regItem(LtdDictionaries.REGS_COV1), Matrix.class, s -> s.getCovariance1().isEmpty() ? null : s.getCovariance1());
        set(regItem(LtdDictionaries.REGS_EFFECT1), double[].class, s -> s.getRegsEffect1().isEmpty() ? null : s.getRegsEffect1().toArray());
        set(regItem(LtdDictionaries.Y_LIN1), double[].class, s -> s.getLinearizedSeries1().toArray());
        
        delegate("res0", TsResiduals.class, source -> source.getResiduals0());
        delegate("res1", TsResiduals.class, source -> source.getResiduals1());
        
    }

    private String mlItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
    }

    private String regItem(String key) {
        return Dictionary.concatenate(LtdDictionaries.REGRESSION, key);
    }
    
    private String modelItem(String key) {
        return Dictionary.concatenate(LtdDictionaries.MODEL, key);
    }

}
