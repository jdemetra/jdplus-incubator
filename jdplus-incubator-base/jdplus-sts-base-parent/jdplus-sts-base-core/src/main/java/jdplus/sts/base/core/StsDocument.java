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
package jdplus.sts.base.core;

import jdplus.sts.base.api.StsSpec;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;

/**
 *
 * @author palatej
 */
public class StsDocument extends AbstractTsDocument<StsSpec, StsResults> {

    private final ModellingContext context;

    public StsDocument() {
        super(StsSpec.FULL);
        context = ModellingContext.getActiveContext();
    }

    public StsDocument(ModellingContext context) {
        super(StsSpec.FULL);
        this.context = context;
    }

    @Override
    protected StsResults internalProcess(StsSpec spec, TsData data) {
//        return StsKernel.of(spec, context).process(data, ProcessingLog.dummy());
        return null;
    }

}
