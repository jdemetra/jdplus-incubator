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
package jdplus.x12plus.desktop.plugin.x12.ui;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.HendersonSpec;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.x12plus.base.api.GenericSeasonalFilterSpec;
import jdplus.x12plus.base.api.SeasonalFilterOption;
import jdplus.x12plus.base.api.SeasonalFilterSpec;
import jdplus.x12plus.base.api.X11SeasonalFilterSpec;
import jdplus.x12plus.base.api.X11plusSpec;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class X11plusSpecUI implements IPropertyDescriptors {

    public enum TrendType {
        Henderson,
        LocalPolynomial
    }

    public enum SeasonalType {
        X11,
        LocalPolynomial
    }

    private final X12plusSpecRoot root;

    private void update(X11plusSpec nx11) {
        root.update(nx11);
    }

    private boolean isRo() {
        return root.isRo();
    }

    public X11plusSpecUI(X12plusSpecRoot root) {
        this.root = root;
    }

    public X11plusSpec spec() {
        return root.getX11();
    }

    public boolean isDefault() {
        return spec() == null;
    }

    public void setDefault(boolean def) {
        if (isDefault() == def) {
            return;
        }
        X11plusSpec spec = null;
        if (!def) {
            TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
            if (domain != null) {
                spec = X11plusSpec.createDefault(true, domain.getAnnualFrequency(), SeasonalFilterOption.S3X5);
            }
        }
        root.update(spec);
    }

    public DecompositionMode getMode() {
        return spec().getMode();
    }

    public void setMode(DecompositionMode value) {
        update(spec().toBuilder().mode(value).build());
    }

//    public boolean isUseForecast() {
//        return spec().getForecastHorizon() != 0;
//    }
    public int getForecastHorizon() {
        return spec().getForecastHorizon();
    }

    public int getBackcastHorizon() {
        return spec().getBackcastHorizon();
    }

    public boolean isSeasonal() {
        return spec().isSeasonal();
    }

    public void setSeasonal(boolean value) {
        update(spec().toBuilder().seasonal(value).build());
    }

    public void setForecastHorizon(int value) {
        update(spec().toBuilder().forecastHorizon(value).build());
    }

    public void setBackcastHorizon(int value) {
        update(spec().toBuilder().backcastHorizon(value).build());
    }

    public double getLSigma() {
        return spec().getLowerSigma();
    }

    public void setLSigma(double value) {
        update(spec().toBuilder().lowerSigma(value).build());
    }

    public double getUSigma() {
        return spec().getUpperSigma();
    }

    public void setUSigma(double value) {
        update(spec().toBuilder().upperSigma(value).build());
    }

    @Messages({
        "x11plusSpecUI.modeDesc.name=Mode",
        "x11plusSpecUI.modeDesc.desc=[mode] Decomposition mode. Could be changed by the program, if needed."
    })
    private EnhancedPropertyDescriptor modeDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Mode", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, MODE_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.x11plusSpecUI_modeDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_modeDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "x11plusSpecUI.forecastDesc.name=Forecasts horizon",
        "x11plusSpecUI.forecastDesc.desc=[forecast(maxlead)] Length of the forecasts generated by ARIMA. Negative figures are translated in years of forecasts"
    })
    private EnhancedPropertyDescriptor forecastDesc() {
        if (!root.isPreprocessing()) {
            return null;
        }
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("ForecastHorizon", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FORECAST_ID);
            desc.setDisplayName(Bundle.x11plusSpecUI_forecastDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_forecastDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "x11plusSpecUI.backcastDesc.name=Backcasts horizon",
        "x11plusSpecUI.backcastDesc.desc=[backcast(maxback)] Length of the backcasts used in X11. Negative figures are translated in years of backcasts"
    })
    private EnhancedPropertyDescriptor backcastDesc() {
        if (!root.isPreprocessing()) {
            return null;
        }
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("BackcastHorizon", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BACKCAST_ID);
            desc.setDisplayName(Bundle.x11plusSpecUI_backcastDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_backcastDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "x11plusSpecUI.lsigmaDesc.name=LSigma",
        "x11plusSpecUI.lsigmaDesc.desc=[sigmalim] Lower sigma boundary for the detection of extreme values."
    })
    private EnhancedPropertyDescriptor lsigmaDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("LSigma", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LSIGMA_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.x11plusSpecUI_lsigmaDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_lsigmaDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "x11plusSpecUI.usigmaDesc.name=USigma",
        "x11plusSpecUI.usigmaDesc.desc=[sigmalim] Upper sigma boundary for the detection of extreme values."
    })
    private EnhancedPropertyDescriptor usigmaDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("USigma", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, USIGMA_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.x11plusSpecUI_usigmaDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_usigmaDesc_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public TrendType getTrendType() {
        FilterSpec tspec = spec().getTrendFilter();
        if (tspec instanceof LocalPolynomialFilterSpec) {
            return TrendType.LocalPolynomial;
        } else if (tspec instanceof HendersonSpec) {
            return TrendType.Henderson;
        } else {
            return null;
        }
    }

    public IPropertyDescriptors getTrendFilter() {
        FilterSpec tspec = spec().getTrendFilter();
        if (tspec instanceof LocalPolynomialFilterSpec lpspec) {
            return new LocalPolynomialSpecUI(lpspec, isRo(), false, nspec
                    -> {
                update(spec().toBuilder()
                        .trendFilter(nspec)
                        .build());
            }
            );
        } else if (tspec instanceof HendersonSpec) {
            return new HendersonSpecUI(root);
        } else {
            return null;
        }
    }

    public void setTrendType(TrendType type) {
        FilterSpec nspec = null;
        TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
        if (domain == null) {
            return;
        }
        switch (type) {
            case Henderson ->
                nspec = new HendersonSpec(domain.getAnnualFrequency() / 2, 3.5);
            case LocalPolynomial ->
                nspec = LocalPolynomialFilterSpec.DEF_TREND_SPEC
                        .toBuilder()
                        .filterHorizon(domain.getAnnualFrequency() / 2)
                        .build();
        }
        if (nspec != null) {
            update(spec().toBuilder()
                    .trendFilter(nspec)
                    .build());
        }
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = defDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = modeDesc();
        if (desc != null) {
            descs.add(desc);
        }
//        desc = seasDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
        desc = forecastDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = backcastDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = lsigmaDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = usigmaDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = trendTypeDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = trendDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = seasTypeDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = initialSeasDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = finalSeasDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    public SeasonalType getSeasonalType() {
        SeasonalFilterSpec sspec = spec().getFinalSeasonalFilter();
        if (sspec instanceof X11SeasonalFilterSpec) {
            return SeasonalType.X11;
        } else if (sspec instanceof GenericSeasonalFilterSpec gspec) {
            if (gspec.getFilter() instanceof LocalPolynomialFilterSpec) {
                return SeasonalType.LocalPolynomial;
            }
        }
        return null;

    }

    public IPropertyDescriptors getInitialSeasonalFilter() {
        SeasonalFilterSpec sspec = spec().getInitialSeasonalFilter();
        if (sspec instanceof GenericSeasonalFilterSpec gspec) {
            if (gspec.getFilter() instanceof LocalPolynomialFilterSpec lpspec) {
                TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();

                return new LocalPolynomialSpecUI(lpspec, isRo(), true, nspec
                        -> {
                    GenericSeasonalFilterSpec ngspec = new GenericSeasonalFilterSpec(domain.getAnnualFrequency(), nspec);
                    update(spec().toBuilder()
                            .initialSeasonalFilter(ngspec)
                            .build());
                });
            }
        }
        return null;
    }

    public IPropertyDescriptors getFinalSeasonalFilter() {
        SeasonalFilterSpec sspec = spec().getFinalSeasonalFilter();
        if (sspec instanceof GenericSeasonalFilterSpec gspec) {
            if (gspec.getFilter() instanceof LocalPolynomialFilterSpec lpspec) {
                TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();

                return new LocalPolynomialSpecUI(lpspec, isRo(), true, nspec
                        -> {
                    GenericSeasonalFilterSpec ngspec = new GenericSeasonalFilterSpec(domain.getAnnualFrequency(), nspec);
                    update(spec().toBuilder()
                            .finalSeasonalFilter(ngspec)
                            .build());
                });
            }
        }
        return null;
    }

    public void setSeasonalType(SeasonalType type) {
        SeasonalFilterSpec nspec = null;
        TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
        if (domain == null) {
            return;
        }
        switch (type) {
            case X11 ->
                nspec = new X11SeasonalFilterSpec(domain.getAnnualFrequency(), SeasonalFilterOption.S3X5);
            case LocalPolynomial ->
                nspec = new GenericSeasonalFilterSpec(domain.getAnnualFrequency(), LocalPolynomialFilterSpec.DEF_SEAS_SPEC);
        }

        if (nspec != null) {
            update(spec().toBuilder()
                    .initialSeasonalFilter(nspec)
                    .finalSeasonalFilter(nspec)
                    .build());
        }
    }

    public SeasonalFilterOption getX11InitialSeasonalFilter() {
        SeasonalFilterSpec sspec = spec().getInitialSeasonalFilter();
        if (sspec instanceof X11SeasonalFilterSpec xspec) {
            return xspec.getFilter();
        } else {
            return null;
        }
    }

    public SeasonalFilterOption getX11FinalSeasonalFilter() {
        SeasonalFilterSpec sspec = spec().getFinalSeasonalFilter();
        if (sspec instanceof X11SeasonalFilterSpec xspec) {
            return xspec.getFilter();
        } else {
            return null;
        }
    }

    public void setX11InitialSeasonalFilter(SeasonalFilterOption option) {
        TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
        SeasonalFilterSpec nspec = new X11SeasonalFilterSpec(domain.getAnnualFrequency(), option);
        update(spec().toBuilder()
                .initialSeasonalFilter(nspec)
                .build());
    }

    public void setX11FinalSeasonalFilter(SeasonalFilterOption option) {
        TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
        SeasonalFilterSpec nspec = new X11SeasonalFilterSpec(domain.getAnnualFrequency(), option);
        update(spec().toBuilder()
                .finalSeasonalFilter(nspec)
                .build());
    }

//    ///////////////////////////////////////////////////////////////////////////
    private static final int DEF_ID = -1, MODE_ID = 0, SEAS_ID = 1, FORECAST_ID = 2, BACKCAST_ID = 12, LSIGMA_ID = 3, USIGMA_ID = 4, AUTOTREND_ID = 5,
            TREND_ID = 6, ISEAS_ID = 7, FSEAS_ID = 8, TRENDTYPE_ID = 10, SEASTYPE_ID = 11;

    @NbBundle.Messages({
        "x11plusSpecUI.defDesc.name=Default",
        "x11plusSpecUI.defDesc.desc=Is Default?"
    })
    private EnhancedPropertyDescriptor defDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Default", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            desc.setDisplayName(Bundle.x11plusSpecUI_defDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_defDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "x11plusSpecUI.trendDesc.name=TREND",
        "x11plusSpecUI.trendDesc.desc=Trend specification."
    })
    private EnhancedPropertyDescriptor trendDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TrendFilter", this.getClass(), "getTrendFilter", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            desc.setDisplayName(Bundle.x11plusSpecUI_trendDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_trendDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "x11plusSpecUI.trendTypeDesc.name=Trend type",
        "x11plusSpecUI.trendTypeDesc.desc=Trend specification."
    })
    private EnhancedPropertyDescriptor trendTypeDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TrendType", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRENDTYPE_ID);
            desc.setDisplayName(Bundle.x11plusSpecUI_trendTypeDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_trendTypeDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "x11plusSpecUI.seasTypeDesc.name=Seasonal type",
        "x11plusSpecUI.seasTypeDesc.desc=Seasonal specification."
    })
    private EnhancedPropertyDescriptor seasTypeDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SeasonalType", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEASTYPE_ID);
            desc.setDisplayName(Bundle.x11plusSpecUI_seasTypeDesc_name());
            desc.setShortDescription(Bundle.x11plusSpecUI_seasTypeDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "x11plusSpecUI.iseasDesc.name=Initial seasonal filter",
        "x11plusSpecUI.iseasDesc.desc=Initial seasonal filter"
    })
    private EnhancedPropertyDescriptor initialSeasDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            SeasonalFilterSpec sspec = spec().getFinalSeasonalFilter();
            if (sspec instanceof X11SeasonalFilterSpec) {
                PropertyDescriptor desc = new PropertyDescriptor("X11InitialSeasonalFilter", this.getClass());
                EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ISEAS_ID);
                desc.setDisplayName(Bundle.x11plusSpecUI_iseasDesc_name());
                desc.setShortDescription(Bundle.x11plusSpecUI_iseasDesc_desc());
                return edesc;
            } else {
                PropertyDescriptor desc = new PropertyDescriptor("InitialSeasonalFilter", this.getClass(), "getInitialSeasonalFilter", null);
                EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ISEAS_ID);
                desc.setDisplayName(Bundle.x11plusSpecUI_iseasDesc_name());
                desc.setShortDescription(Bundle.x11plusSpecUI_iseasDesc_desc());
                return edesc;
            }
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "x11plusSpecUI.fseasDesc.name=Final seasonal filter",
        "x11plusSpecUI.fseasDesc.desc=Final seasonal filter"
    })
    private EnhancedPropertyDescriptor finalSeasDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            SeasonalFilterSpec sspec = spec().getFinalSeasonalFilter();
            if (sspec instanceof X11SeasonalFilterSpec) {
                PropertyDescriptor desc = new PropertyDescriptor("X11FinalSeasonalFilter", this.getClass());
                EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FSEAS_ID);
                desc.setDisplayName(Bundle.x11plusSpecUI_fseasDesc_name());
                desc.setShortDescription(Bundle.x11plusSpecUI_fseasDesc_desc());
                return edesc;
            } else {
                PropertyDescriptor desc = new PropertyDescriptor("FinalSeasonalFilter", this.getClass(), "getFinalSeasonalFilter", null);
                EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FSEAS_ID);
                desc.setDisplayName(Bundle.x11plusSpecUI_fseasDesc_name());
                desc.setShortDescription(Bundle.x11plusSpecUI_fseasDesc_desc());
                return edesc;
            }
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    @NbBundle.Messages({
//        "x11plusSpecUI.seasDesc.name=Initial seasonal filter",
//        "x11plusSpecUI.seasDesc.desc=Initial seasonal filter"
//    })
//    private EnhancedPropertyDescriptor seasDesc() {
//        if (isDefault()) {
//            return null;
//        }
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("SeasonalFilter", this.getClass(), "getInitialSeasonalFilter", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ISEAS_ID);
//            desc.setDisplayName(Bundle.x11plusSpecUI_seasDesc_name());
//            desc.setShortDescription(Bundle.x11plusSpecUI_seasDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }

    @Messages("x11plusSpecUI.getDisplayName=X11+")
    @Override
    public String getDisplayName() {
        return Bundle.x11plusSpecUI_getDisplayName();
    }

}
