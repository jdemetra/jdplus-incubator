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

import jdplus.sa.desktop.plugin.descriptors.regular.AbstractRegressionSpecUI;
import jdplus.sa.desktop.plugin.descriptors.regular.RegularSpecUI;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;

/**
 *
 * @author palatej
 */
public class RegressionSpecUI extends AbstractRegressionSpecUI {

    private final TdModelSpecRoot root;

    public RegressionSpecUI(TdModelSpecRoot root) {
        this.root = root;
    }

    @Override
    protected RegressionSpec spec() {
        return root.getPreprocessing().getRegression();
    }

    @Override
    protected RegularSpecUI root() {
        return root;
    }

    @Override
    public IPropertyDescriptors getCalendar() {
        return new CalendarSpecUI(root);
    }

    @Override
    public IPropertyDescriptors getMean() {
        return new MeanSpecUI(root);
    }
}
