/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.desktop.plugin.ui;

import jdplus.sa.desktop.plugin.descriptors.highfreq.AbstractOutlierSpecUI;
import jdplus.sa.desktop.plugin.descriptors.highfreq.HighFreqSpecUI;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;

/**
 *
 * @author PALATEJ
 */
public class OutlierSpecUI extends AbstractOutlierSpecUI {

    private final ExtendedAirlineSpecRoot root;

    public OutlierSpecUI(ExtendedAirlineSpecRoot root) {
        this.root = root;
    }

    @Override
    protected OutlierSpec spec() {
        return root.getCore().getOutlier();
    }

    @Override
    protected HighFreqSpecUI root() {
        return root;
    }
}
