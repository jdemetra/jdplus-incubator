/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.core.ssf.composite.MultivariateCompositeSsf;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public interface IMstsBuilder {
    int decode(DoubleSeq parameters, MultivariateCompositeSsf.Builder builder);
}
