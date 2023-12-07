/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core;

import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.RawBsmDecomposition;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;

/**
 *
 * @author PALATEJ
 */
public interface BasicStructuralModel extends GeneralLinearModel<BsmSpec>{
    
    RawBsmDecomposition getBsmDecomposition();
}
