/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core;

import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sts.base.api.BsmDecomposition;
import jdplus.sts.base.api.BsmDescription;

/**
 *
 * @author PALATEJ
 */
public interface BasicStructuralModel {
    
    BsmDescription getDescription();
    BsmEstimation getEstimation();
    BsmDecomposition getBsmDecomposition();
    SeriesDecomposition getFinalDecomposition();
 }
