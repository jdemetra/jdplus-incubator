/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tdarima.desktop.plugin.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.desktop.plugin.html.HtmlFragment;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;

/**
 *
 * @author Jean Palate
 */
public class KfWeightsUI implements ItemUI<KfWeightsUI.Information> {

    @lombok.Value
    public static class Information {

        MatrixStorage weights;
        ComponentDescriptor[] descriptors;
        int[] items;
        String[] itemsName;
    }

    @Override
    public JComponent getView(KfWeightsUI.Information information) {
        try {
            int ncmps = information.descriptors.length;
            List<FiniteFiltersView.FilterProvider> providers = new ArrayList<>();
            for (int i = 0; i < information.getItems().length; ++i) {
                int item = information.getItems()[i];
                FiniteFilter[] filters = new FiniteFilter[ncmps];
                for (int j = 0; j < ncmps; ++j) {
                    int component = information.getDescriptors()[j].getComponent();
                    boolean signal = information.getDescriptors()[j].isSignal();
                    FastMatrix matrix = information.getWeights().matrix(component);
                    double[] w = matrix.column(item).toArray();
                    if (!signal) {
                        for (int k = 0; k < w.length; ++k) {
                            w[k] = -w[k];
                        }
                        w[item] += 1;
                    }
                    filters[j] = FiniteFilter.ofInternal(w, -item);
                }
                FiniteFiltersView.FilterProvider provider = new FiniteFiltersView.FilterProvider(
                        filters, information.itemsName[i], information.descriptors);
                providers.add(provider);
            }
            return new FiniteFiltersView(providers);
        } catch (Exception err) {
            return TsViewToolkit.getHtmlViewer(new HtmlFragment("Unable to compute the final estimators"));
        }
    }
}
