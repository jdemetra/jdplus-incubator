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
package jdplus.x12plus.base.core.x12;

import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.processing.HasLog;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.modelling.HasRegSarimaPreprocessing;
import jdplus.x12plus.base.core.X11plusResults;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class X12plusResults implements GenericExplorable, HasLog, HasRegSarimaPreprocessing {

    private RegSarimaModel preprocessing;
    private X12plusPreadjustment preadjustment;
    private X11plusResults decomposition;
    private X12plusFinals finals;
    private SaBenchmarkingResults benchmarking;
    private X12plusDiagnostics diagnostics;
    private ProcessingLog log;
    
}
