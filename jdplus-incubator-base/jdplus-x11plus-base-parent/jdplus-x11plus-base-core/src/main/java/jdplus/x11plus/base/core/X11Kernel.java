/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.x11plus.base.core;

import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.x11plus.base.api.SeasonalFilterOption;
import jdplus.x11plus.base.api.X11plusSpec;

/**
 *
 * @author palatej
 */
public class X11Kernel {

    private final X11plusSpec spec;

    private X11Kernel(X11plusSpec spec) {
        this.spec = spec;
    }

    public static X11Kernel of(X11plusSpec spec) {
        return new X11Kernel(spec);
    }

    public X11Results process(TsData s) {
        if (spec == null) {
            boolean pos = s.getValues().allMatch(x->x>0);
            X11plusSpec nspec = X11plusSpec.createDefault(pos,s.getAnnualFrequency(), SeasonalFilterOption.S3X5);
            return X11Toolkit.process(s, nspec);
        } else {
            return X11Toolkit.process(s, spec);
        }
    }
}
