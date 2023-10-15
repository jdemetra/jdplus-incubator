/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x12plus.base.core;

import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
@lombok.Builder
public class RawX11Results implements GenericExplorable{

    int nbackcasts, nforecasts;

    DoubleSeq b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b13, b17, b20;
    DoubleSeq c1, c2, c4, c5, c6, c7, c9, c10, c11, c13, c17, c20;
    DoubleSeq d1, d2, d4, d5, d6, d7, d8, d10, d11, d12, d13;

    DecompositionMode mode;

}
