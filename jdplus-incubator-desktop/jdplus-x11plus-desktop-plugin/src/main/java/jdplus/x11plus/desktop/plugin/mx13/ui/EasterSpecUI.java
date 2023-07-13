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
package jdplus.x11plus.desktop.plugin.mx13.ui;

import jdplus.sa.desktop.plugin.descriptors.highfreq.AbstractEasterSpecUI;
import jdplus.sa.desktop.plugin.descriptors.highfreq.HighFreqSpecUI;
import jdplus.toolkit.base.api.modelling.highfreq.EasterSpec;

/**
 *
 * @author PALATEJ
 */
public class EasterSpecUI extends AbstractEasterSpecUI {

   private final MX13plusSpecRoot root;
   
   public EasterSpecUI(MX13plusSpecRoot root){
       this.root=root;
   }

    @Override
    protected EasterSpec spec() {
        return root.getPreprocessing().getRegression().getEaster();
    }

    @Override
    protected HighFreqSpecUI root() {
        return root;
    }


}
