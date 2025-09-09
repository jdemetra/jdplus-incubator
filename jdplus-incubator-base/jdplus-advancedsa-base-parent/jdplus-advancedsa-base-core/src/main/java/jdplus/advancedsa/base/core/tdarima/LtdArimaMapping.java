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
package jdplus.advancedsa.base.core.tdarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;

/**
 * The parameters are organized as follows: p_mean for all sarima parameters,
 * p_step for all variable sarima parameters (phi, bphi, theta, btheta), true
 * signs + var_end (var_start = 1)
 *
 * @author Jean Palate
 */
public interface LtdArimaMapping extends IParametricMapping<LtdArimaModel> {

    public int getN();
    public SarimaOrders getOrders();

    static final double MAX = 0.99999;
    public static final double STEP = Math.pow(2.220446e-16, 0.5), EVAR = 1e-6;
    
    DoubleSeq parametersOf(LtdArimaModel model);

}
