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
import jdplus.toolkit.base.core.math.functions.IParametersDomain;

/**
 *
 * @author palatej
 */
public class GenericParameters implements ParameterInterpreter {

    private boolean fixed;
    private final double[] parameters;
    private final IParametersDomain domain;
    private final String name;

    public GenericParameters(String name, IParametersDomain domain, double[] parameters, boolean fixed) {
        this.name = name;
        this.domain = domain;
        this.fixed = fixed;
        this.parameters = parameters;
    }

    @Override
    public GenericParameters duplicate(){
        return new GenericParameters(name, domain, parameters.clone(), fixed);
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
        int n = parameters.length;
        if (!fixed) {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = parameters[i];
            }
        }
        return pos;
    }

    @Override
    public int encode(DoubleSeqCursor reader, double[] buffer, int pos) {
        int n = parameters.length;
        if (!fixed) {
            for (int i = 0; i < n; ++i) {
                buffer[pos++] = reader.getAndNext();
            }
        } else {
            reader.skip(n);
        }

        return pos;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        for (int i = 0; i < parameters.length; ++i) {
            parameters[i] = reader.getAndNext();
        }
        fixed = true;
    }

    @Override
    public void free(){
        fixed=false;
    }

    @Override
    public IParametersDomain getDomain() {
        return domain;
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            System.arraycopy(parameters, 0, buffer, pos, parameters.length);
            return pos + parameters.length;
        } else {
            return pos;
        }
    }
    
    @Override
    public int rescaleVariances(double factor, double[] buffer, int pos) {
        return pos+parameters.length;
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
