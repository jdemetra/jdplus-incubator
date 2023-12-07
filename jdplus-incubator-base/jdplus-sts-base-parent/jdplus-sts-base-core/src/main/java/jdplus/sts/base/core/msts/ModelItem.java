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

import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author palatej
 */
public interface ModelItem {
    String getName();
    
    void addTo(MstsMapping model);
    /**
     * Gets the parameters involved in the component
     * @return 
     */
    List<ParameterInterpreter> parameters();
    
    default boolean isScalable(){
        return true;
    }
    
    /**
     * 
     * @param variance
     * @return 
     */
    default boolean isScaleSensitive(boolean variance ){
        List<ParameterInterpreter> parameters = parameters();
        for (ParameterInterpreter p: parameters){
            if (p.isScaleSensitive(variance))
                return true;
        }
        return false;
    }
  
    default int rescale(double factor, double[] buffer, int pos, Predicate<ParameterInterpreter> check){
        List<ParameterInterpreter> parameters = parameters();
        int npos=pos;   
        for (ParameterInterpreter p: parameters){
            p.rescale(factor, buffer, npos, check);
            npos+=p.getDomain().getDim();
        }
        return npos;
    }
    
    ModelItem duplicate();
    
}
