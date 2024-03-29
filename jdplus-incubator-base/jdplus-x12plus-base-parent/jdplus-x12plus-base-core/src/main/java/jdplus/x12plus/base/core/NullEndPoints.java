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
package jdplus.x12plus.base.core;

import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * A "do nothing" processor. The module pre-suppose that
 * the missing end points in the output buffer have been set to 0.
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public class NullEndPoints implements IEndPointsProcessor {

    @Override
    public void process(DoubleSeq in, DataBlock out) {
    }

}
