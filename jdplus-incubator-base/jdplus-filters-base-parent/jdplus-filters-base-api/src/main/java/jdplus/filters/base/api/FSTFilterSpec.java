/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters.base.api;

import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class FSTFilterSpec implements FilterSpec {
    
    public static FilterSpec DEF_SPEC=builder().build();

    private double w0, w1;
    private int polynomialPreservationDegree;
    private int smoothnessDegree;
    private int lags, leads;
    private boolean antiphase;

    private double smoothnessWeight, timelinessWeight;
    
    public static Builder builder(){
        return new Builder()
                .w0(0)
                .w1( Math.PI / 8)
                .polynomialPreservationDegree(2)
                .smoothnessDegree(3)
                .lags(6)
                .leads(6)
                .smoothnessWeight(1.0/3.0)
                .timelinessWeight(1.0/3.0)
                .antiphase(true);
    }
}
