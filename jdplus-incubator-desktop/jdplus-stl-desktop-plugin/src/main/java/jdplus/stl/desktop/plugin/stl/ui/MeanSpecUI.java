/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl.desktop.plugin.stl.ui;

import jdplus.sa.desktop.plugin.descriptors.regular.AbstractMeanSpecUI;
import jdplus.sa.desktop.plugin.descriptors.regular.RegularSpecUI;
import jdplus.toolkit.base.api.modelling.regular.MeanSpec;

/**
 *
 * @author Jean Palate
 */
public class MeanSpecUI extends AbstractMeanSpecUI {

    private final StlPlusSpecRoot root;

    public MeanSpecUI(StlPlusSpecRoot root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return isUsed() ? "in use" : "";
    }

    @Override
    protected MeanSpec spec() {
        return root.getPreprocessing().getRegression().getMean();
    }

    @Override
    protected RegularSpecUI root() {
        return root;
    }

}
