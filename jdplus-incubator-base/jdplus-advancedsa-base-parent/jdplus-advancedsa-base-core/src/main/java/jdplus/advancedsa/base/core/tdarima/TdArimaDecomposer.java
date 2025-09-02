/*
 * Copyright 2024 JDemetra+.
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
package jdplus.advancedsa.base.core.tdarima;

import java.util.function.IntFunction;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaException;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
public class TdArimaDecomposer {

    private final int period;
    private final ArimaModel[] arima;
    private final UcarimaModel[] ucms;

    private boolean modified;

    public TdArimaDecomposer(int period, int n, IntFunction<IArimaModel> fn) {
        this.period=period;
        arima = new ArimaModel[n];
        ucms = new UcarimaModel[n];
        int first = -1, last = -1;
        for (int i = 0; i < ucms.length; ++i) {
            ArimaModel m= ArimaModel.of(fn.apply(i));
            m=m.simplifyAr();
            arima[i]=m;
            UcarimaModel ucm = ucm(m);
            ucms[i] = ucm;
            if (ucm != null) {
                if (first == -1) {
                    first = i;
                }
                last = i;
            }
        }
        if (first == -1){
            throw new UcarimaException();
           
        }
        if (first != 0){
            modified=true;
            for (int i=0; i<first; ++i){
                ucms[i]=ucms[first];
            }
        }
        if (last+1 != ucms.length){
            modified=true;
            for (int i=last+1; i<ucms.length; ++i){
                ucms[i]=ucms[last];
            }
        }
    }
    
    public UcarimaModel[] ucarimaModels(){
        return ucms;
    }
    
    

    private UcarimaModel ucm(IArimaModel model) {
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(period);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(model);
        ucm = ucm.setVarianceMax(2, false);
//        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }
    
    public boolean isModified(){
        return modified;
    }
}
