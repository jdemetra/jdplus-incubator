/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.base.core.extendedairline;

import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.processing.HasLog;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.highfreq.base.core.extendedairline.decomposition.ExtendedAirlineDecomposition;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class ExtendedAirlineResults implements GenericExplorable, HasLog{

    private HighFreqRegArimaModel preprocessing;
    private ExtendedAirlineDecomposition decomposition;
    private SeriesDecomposition components, finals;
    private ProcessingLog log;
    
}
