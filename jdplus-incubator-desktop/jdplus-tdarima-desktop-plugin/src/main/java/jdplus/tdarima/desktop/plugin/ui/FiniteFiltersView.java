/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tdarima.desktop.plugin.ui;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.desktop.plugin.components.tools.FilterView;
import jdplus.toolkit.desktop.plugin.ui.DiscreteDisplayDomain;
import jdplus.toolkit.desktop.plugin.ui.DiscreteInformationProvider;

public class FiniteFiltersView extends JComponent {

    private final JTabbedPane tabbedpanel_;

    public static class FilterProvider implements DiscreteInformationProvider {

        private final FiniteFilter filter[];
        private final String title;
        private final ComponentDescriptor descriptor[];
        private final int n0, n1, n;

        public FilterProvider(FiniteFilter filter[], String title, ComponentDescriptor descriptor[]) {
            this.filter = filter;
            this.title = title;
            this.descriptor = descriptor;
            this.n0 = filter[0].getLowerBound();
            this.n1 = filter[0].getUpperBound();
            this.n = n1 - n0 + 1;
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
        public DiscreteDisplayDomain getDiscreteDisplayDomain(int npoints) {
            if (npoints >= n) {
                return new DiscreteDisplayDomain(n0, n1);
            } else {
                int n2 = npoints / 2;
                if (-n2 <= n0) {
                    return new DiscreteDisplayDomain(n0, n0 + npoints - 1);
                } else if (-n2 + npoints - 1 > n1) {
                    return new DiscreteDisplayDomain(n1 - npoints + 1, n1);
                } else {
                    return new DiscreteDisplayDomain(-n2, -n2 + npoints - 1);
                }
            }
        }

        @Override
        public DiscreteDisplayDomain getDiscreteDisplayDomain(int lower, int upper) {
            return new DiscreteDisplayDomain(Math.max(lower, n0), Math.min(upper, n1));
        }

        @Override
        public boolean isDefined(int cmp) {
            return cmp < descriptor.length;
        }

        @Override
        public double[] getDataArray(int cmp, DiscreteDisplayDomain domain) {
            double[] v = new double[domain.getLength()];
            IntToDoubleFunction ff = filter[cmp].weights();
            for (int i = 0; i < v.length; ++i) {
                v[i] = ff.applyAsDouble(domain.x(i));
            }
            return v;
        }

        @Override
        public double getData(int cmp, int x) {
            return filter[cmp].weights().applyAsDouble(x);
        }
    }

    public FiniteFiltersView(List<FilterProvider> providers) {
        tabbedpanel_ = new JTabbedPane();

        for (FilterProvider provider : providers) {
            FilterView filterView = new FilterView(provider, provider.n, provider.n0, provider.n1, -0.3, 1);
            tabbedpanel_.addTab(provider.getInformation(), filterView);
        }
        setLayout(new BorderLayout());
        add(tabbedpanel_, BorderLayout.CENTER);
    }
}
