/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tdarima.desktop.plugin.ui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.desktop.plugin.components.tools.PiView;
import jdplus.toolkit.desktop.plugin.ui.ContinuousDisplayDomain;
import jdplus.toolkit.desktop.plugin.ui.ContinuousInformationProvider;
import jdplus.toolkit.desktop.plugin.ui.FrequencyInformationProvider;

public class SquaredGainsView extends JComponent {

    private final JTabbedPane tabbedpanel_;

    public static class GainProvider implements ContinuousInformationProvider {

        private final int period;
        private final FiniteFilter filter[];
        private final String title;
        private final ComponentDescriptor descriptor[];

        public GainProvider(int period, FiniteFilter filter[], String title, ComponentDescriptor descriptor[]) {
            this.period = period;
            this.filter = filter;
            this.title = title;
            this.descriptor = descriptor;
        }

        @Override
        public String getInformation() {
            return title;
        }

        @Override
        public String[] getComponents() {
            String[] cmps = new String[descriptor.length];
            for (int i = 0; i < cmps.length; ++i) {
                cmps[i] = descriptor[i].getName();
            }
            return cmps;
        }

        @Override
        public ContinuousDisplayDomain getContinuousDisplayDomain(int npoints) {
            return FrequencyInformationProvider.getDisplayDomain(period, npoints);
        }

        @Override
        public ContinuousDisplayDomain getContinuousDisplayDomain(double lower, double upper, int npoints) {
            return FrequencyInformationProvider.getDisplayDomain(period, lower, upper, npoints);
        }

        @Override
        public boolean isDefined(int cmp) {
            return cmp < descriptor.length;
        }

        @Override
        public double[] getDataArray(int cmp, ContinuousDisplayDomain domain) {
            double[] data = new double[domain.npoints];
            double freq = domain.beg;
            FiniteFilter ff = filter[cmp];
            for (int j = 0; j < data.length; ++j, freq += domain.step) {
                data[j] = ff.frequencyResponse(freq).absSquare();
            }
            return data;
        }

        @Override
        public double getData(int cmp, double x) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from
            // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    public SquaredGainsView(List<GainProvider> providers) {
        tabbedpanel_ = new JTabbedPane();

        for (GainProvider provider : providers) {
            PiView filterView = new PiView(provider, 601, 0, Math.PI, 0, 1.3);
            tabbedpanel_.addTab(provider.getInformation(), filterView);
        }
        setLayout(new BorderLayout());
        add(tabbedpanel_, BorderLayout.CENTER);
    }
}
