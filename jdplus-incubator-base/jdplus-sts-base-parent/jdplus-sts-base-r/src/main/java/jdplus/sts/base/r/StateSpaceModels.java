/*
 * Copyright 2026 JDemetra+.
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
package jdplus.sts.base.r;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class StateSpaceModels {
    
    public ISsf ssf(StateComponent cmp, ISsfLoading loading, ISsfError error){
        return Ssf.of(cmp.initialization(), cmp.dynamics(), loading, error);
    }
    
    public ISsf ssf(StateComponent cmp, ISsfLoading loading, double evar){
        return Ssf.of(cmp.initialization(), cmp.dynamics(), loading, evar);
    }
    
    public StateComponent componentOf(ISsf ssf){
        return new StateComponent(ssf.initialization(), ssf.dynamics());
    }
}
