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
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.api.arima.SarimaOrders;

/**
 *
 * @author palatej
 */
public final class SarimaInterpreter implements ParameterInterpreter {

    private final String name;
    private double[] values;
    private final SarimaMapping mapping;
    private final int np;
    private boolean fixed;

    public SarimaInterpreter(final String name, final SarimaOrders spec, final double[] p, final boolean fixed) {
        this.name = name;
        this.mapping = SarimaMapping.of(spec);
        this.values = p == null ? mapping.getDefaultParameters().toArray() : p;
        this.np = spec.getParametersCount();
        this.fixed = fixed;
    }

    @Override
    public SarimaInterpreter duplicate(){
        return new SarimaInterpreter(name, mapping.getSpec(), values.clone(), fixed);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public int decode(DoubleSeqCursor reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = values[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleSeqCursor reader, double[] buffer, int pos) {
        if (!fixed) {
            for (int i = 0; i < np; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            reader.skip(np);
        }
        return pos;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        for (int i = 0; i < values.length; ++i) {
            values[i] = reader.getAndNext();
        }
        fixed = true;
    }

    @Override
    public void free(){
        fixed=false;
    }

    @Override
    public SarimaMapping getDomain() {
        return mapping;
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (! fixed) {
           for (int i = 0; i < values.length; ++i) {
                buffer[pos + i] = values[i];
            }
            return pos + values.length;
        } else {
            return pos;
        }
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return false;
    }

    @Override
    public int rescaleVariances(double factor, double[] buffer, int pos) {
        return pos+values.length;
    }

}
