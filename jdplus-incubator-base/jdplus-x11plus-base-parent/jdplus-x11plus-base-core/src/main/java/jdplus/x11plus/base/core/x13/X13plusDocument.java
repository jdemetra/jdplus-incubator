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
package jdplus.x11plus.base.core.x13;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.x11plus.base.api.X13plusSpec;

/**
 *
 * @author palatej
 */
public class X13plusDocument extends AbstractTsDocument<X13plusSpec, X13plusResults> {

    private final ModellingContext context;

    public X13plusDocument() {
        super(X13plusSpec.FULL);
        context = ModellingContext.getActiveContext();
    }

    public X13plusDocument(ModellingContext context) {
        super(X13plusSpec.FULL);
        this.context = context;
    }

    @Override
    protected X13plusResults internalProcess(X13plusSpec spec, TsData data) {
        return X13plusKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

}
