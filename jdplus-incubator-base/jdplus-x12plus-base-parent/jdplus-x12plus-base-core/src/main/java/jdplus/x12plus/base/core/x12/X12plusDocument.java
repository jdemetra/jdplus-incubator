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

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.x12plus.base.api.X12plusSpec;

/**
 *
 * @author palatej
 */
public class X12plusDocument extends AbstractTsDocument<X12plusSpec, X12plusResults> {

    private final ModellingContext context;

    public X12plusDocument() {
        super(X12plusSpec.FULL);
        context = ModellingContext.getActiveContext();
    }

    public X12plusDocument(ModellingContext context) {
        super(X12plusSpec.FULL);
        this.context = context;
    }

    @Override
    protected X12plusResults internalProcess(X12plusSpec spec, TsData data) {
        return X12plusKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

}
