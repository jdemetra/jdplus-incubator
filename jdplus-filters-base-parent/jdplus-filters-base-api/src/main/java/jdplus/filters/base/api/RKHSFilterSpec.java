/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters.base.api;

import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.KernelOption;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class RKHSFilterSpec implements FilterSpec {

    public static RKHSFilterSpec DEF_SPEC = builder().build();

    private int filterLength;
    private KernelOption kernel;
    private int polynomialDegree;
    private boolean optimalBandWidth;
    private AsymmetricCriterion asymmetricBandWith;
    private SpectralDensity density;
    private double passBand;
    private double bandWidth;
    private double minBandWidth;
    private double maxBandWidth;

    public static Builder builder() {
        return new Builder()
                .filterLength(6)
                .kernel(KernelOption.BiWeight)
                .polynomialDegree(2)
                .optimalBandWidth(true)
                .asymmetricBandWith(AsymmetricCriterion.FrequencyResponse)
                .density(SpectralDensity.Undefined)
                .passBand(Math.PI / 8)
                .bandWidth(7)
                .minBandWidth(6)
                .maxBandWidth(18);
    }
}
