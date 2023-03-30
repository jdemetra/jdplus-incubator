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
package jdplus.stl.base.core.mstlplus;

import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.processing.HasLog;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import jdplus.stl.base.core.MStlResults;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class MStlPlusResults implements GenericExplorable, HasLog {

    private HighFreqRegArimaModel preprocessing;
    private MStlResults decomposition;
    private SeriesDecomposition finals;
    private MStlPlusDiagnostics diagnostics;
    private ProcessingLog log;
    
}
