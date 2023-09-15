package jdplus.sts.base.api;

/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
import jdplus.toolkit.base.api.data.DoubleSeq;
import nbbrd.design.Development;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public final class RawBsmDecomposition {

    @lombok.Singular
    Map<Component, DoubleSeq> cmps, ecmps;

    /**
     *
     * @param cmp
     * @param stde
     * @return
     */
    public DoubleSeq getSeries(Component cmp, boolean stde) {
        return stde ? ecmps.get(cmp) : cmps.get(cmp);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        // components
        if (!cmps.isEmpty()) {
            builder.append("components").append("\r\n");
            write(cmps, builder);
        }
        if (!ecmps.isEmpty()) {
            builder.append("components errors").append("\r\n");
            write(ecmps, builder);
        }
        return builder.toString();
    }

    private void write(Map<Component, DoubleSeq> cmps, StringBuilder builder) {
        List<DoubleSeq> all = new ArrayList<>();
        DoubleSeq s = cmps.get(Component.Series);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Level);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Slope);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Seasonal);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Cycle);
        if (s != null) {
            all.add(s);
        }
        s = cmps.get(Component.Noise);
        if (s != null) {
            all.add(s);
        }
        
        if (!all.isEmpty()){
            int n=all.get(0).length();
            int m=all.size();
            double[] data=new double[n*m];
            for (int i=0, j=0; i<m; ++i, j+=n){
                all.get(i).copyTo(data, j);
            }
            builder.append(Matrix.toString(Matrix.of(data, n, m), null));
        }
        builder.append("\r\n");
    }

}
