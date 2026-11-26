/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.tdarima.desktop.plugin.ui;

import jdplus.sa.desktop.plugin.descriptors.regular.AbstractTradingDaysSpecUI;
import jdplus.sa.desktop.plugin.descriptors.regular.RegularSpecUI;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;

/**
 *
 * @author palatej
 */
public class TradingDaysSpecUI extends AbstractTradingDaysSpecUI {

    private final TdModelSpecRoot root;

    public TradingDaysSpecUI(TdModelSpecRoot root) {
        this.root = root;
    }

    @Override
    protected TradingDaysSpec spec() {
        return root.td();
    }

    @Override
    protected RegularSpecUI root() {
        return root;
    }
}
