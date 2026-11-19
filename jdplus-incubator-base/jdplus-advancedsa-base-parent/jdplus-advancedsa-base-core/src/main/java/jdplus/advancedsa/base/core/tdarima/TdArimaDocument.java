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
package jdplus.advancedsa.base.core.tdarima;

import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;

/**
 *
 * @author Jean Palate
 */
public class TdArimaDocument extends AbstractTsDocument<TdModelSpec, TdModellingResults> {

    private final ModellingContext context;

    public TdArimaDocument() {
        super(TdModelSpec.DEFAULT);
        context = ModellingContext.getActiveContext();
    }

    public TdArimaDocument(ModellingContext context) {
        super(TdModelSpec.DEFAULT);
        this.context = context;
    }

    @Override
    protected TdModellingResults internalProcess(TdModelSpec s, TsData tsdata) {
        return new TdModellingKernel().process(tsdata, s, context, ProcessingLog.dummy());
    }
}
