/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.stl.base.core;

/**
 *
 * @author palatej
 */
public class PartialSort {

    private final int BUFFER_LENGTH = 16, BLOCK = 10;
    private double[] a;
    private int[] ind;
    private int n, ni, jl, ju, i, j, k, l, m;

    private final int[] indl = new int[BUFFER_LENGTH], indu = new int[BUFFER_LENGTH], il = new int[BUFFER_LENGTH], iu = new int[BUFFER_LENGTH];

    private boolean initialize(double[] a, int[] ind) {
        this.a = a;
        this.ind = ind;
        n = a.length;
        ni = ind.length;
        if (n < 2 || ni == 0) {
            return false;
        }
        // lower/upper bounds of the indices (included)
        jl = 0;
        ju = ni - 1;

        indl[0] = 0;
        indu[0] = ni - 1;
        i = 0;
        j = n - 1;
        m = 0;
        return true;
    }

    public void mainLoop() {
        boolean skip = true;
        do {
            if (i < j) {
                skip = true;
            }
            do {
                if (!skip && !nextLimits()) {
                    return;
                }
                if (innerLoop(skip)) {
                    if (finalTreatment()) {
                        break;
                    }
                }
                skip = false;
            } while (true);

        } while (true);
    }

    private boolean nextLimits() {
        do {
            if (--m < 0) {
                return false;
            }
            i = il[m];
            j = iu[m];
            jl = indl[m];
            ju = indu[m];
        } while (jl > ju);
        return true;
    }

    private boolean finalTreatment() {
        if (i != 0) {
            --i;
            do {
                ++i;
                if (i == j) {
                    return false;
                }
                double t = a[i + 1];
                if (a[i] > t) {
                    k = i;
                    do {
                        a[k + 1] = a[k];
                    } while (t < a[--k]);
                    a[k + 1] = t;
                }
            } while (true);
        }
        return true;

    }

    /**
     *
     * @return true = 173, false = 166
     */
    private boolean innerLoop(boolean skip) {
        while (skip || j - i > BLOCK) {
            skip = false;
            // first, if need be, we swap the item in the middle with the extremities of the current interval
            double t = startInnerLoop();
            k = i;
            l = j;
            swapItems(t);
            indl[m] = jl;
            indu[m] = ju;
            int p = m++;
            if (!updateIndices(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return The central value
     */
    private double startInnerLoop() {
        // first, if need be, we swap the item in the middle with the extremities of the current interval
        int ij = (i + j) / 2;
        double t = a[ij];
        if (a[i] > t) {
            // swap ij, i
            a[ij] = a[i];
            a[i] = t;
            t = a[ij];
        }
        // t contains the max of a[ij], a[i]
        // a[i] < a[ij]
        if (a[j] < t) {
            // swap ij, j
            a[ij] = a[j];
            a[j] = t;
            t = a[ij];
            // a[i] < a[ij] < a[j]
            // t contains a[ij]
            if (a[i] > t) {
                // swap ij, i
                a[ij] = a[i];
                a[i] = t;
                t = a[ij];
            }
        }
        // a[i] < a[ij] < a[j]
        // t = a[ij]
        return t;
    }

    /**
     * Swap items, from k to l, based on t as pivot value
     *
     * @param t
     */
    private void swapItems(double t) {
        do {
            if (a[--l] <= t) {
                double tt = a[l];
                do {
                    ++k;
                } while (a[k] < t);
                if (k > l) {
                    return;
                }
                a[l] = a[k];
                a[k] = tt;
            }
        } while (true);
    }

    /**
     *
     * @param p
     * @return If false, stop the current iteration of the main loop
     */
    private boolean updateIndices(int p) {
        if (l - i <= j - k) {
            il[p] = k;
            iu[p] = j;
            j = l;
            while (jl <= ju && ind[ju] > j) {
                ju--;
            }
            if (jl > ju) {
                return false;
            }
            indl[p] = ju + 1;
        } else {
            il[p] = i;
            iu[p] = l;
            i = k;
            while (jl <= ju && ind[jl] < i) {
                jl++;
            }
            if (jl > ju) {
                return false;
            }
            indu[p] = jl - 1;
        }
        return true;
    }

    public void psort(double[] a, int[] ind) {
        if (!initialize(a, ind)) {
            throw new IllegalArgumentException("psort");
        }
        mainLoop();
    }
//        int p;
//        double tt;
//        boolean skip = true;
//        do // main loop
//        {
//            if (i < j) {
//                skip = true;
//            }
//            do {
//                if (!skip) {
//                    if (!nextLimits()) // 166
//                    {
//                        return;
//                    }
//                }
//                boolean next = false;
//                while (skip || j - i > BLOCK) { // Treatment of large blocks
//                    skip = false;
//                    // first, if need be, we swap the item in the middle with the extremities of the current interval
//                    int ij = (i + j) / 2;
//                    t = a[ij];
//                    if (a[i] > t) {
//                        // swap ij, i
//                        a[ij] = a[i];
//                        a[i] = t;
//                        t = a[ij];
//                    }
//                    // t contains the max of a[ij], a[i]
//                    // a[i] < a[ij]
//                    if (a[j] < t) {
//                        // swap ij, j
//                        a[ij] = a[j];
//                        a[j] = t;
//                        t = a[ij];
//                        // a[i] < a[ij] < a[j]
//                        // t contains a[ij]
//                        if (a[i] > t) {
//                            // swap ij, i
//                            a[ij] = a[i];
//                            a[i] = t;
//                            t = a[ij];
//                        }
//                    }
//                    // a[i] < a[ij] < a[j]
//                    // t = a[ij]
//                    k = i;
//                    l = j;
//
//                    do {
//                        if (a[--l] <= t) {
//                            tt = a[l];
//                            do {
//                                ++k;
//                            } while (a[k] < t);
//                            if (k > l) {
//                                break;
//                            }
//                            a[l] = a[k];
//                            a[k] = tt;
//                        }
//                    } while (true);
//                    indl[m] = jl;
//                    indu[m] = ju;
//                    p = m++;
//                    if (l - i <= j - k) {
//                        il[p] = k;
//                        iu[p] = j;
//                        j = l;
//                        if (jl > ju) {
//                            next = true;
//                            break;
//                        }
//                        while (jl <= ju && ind[ju] > j) {
//                            ju--;
//                        }
//                        if (jl > ju) {
//                            next = true;
//                            break;//+break
//                        }
//                        indl[p] = ju + 1;
//                    } else {
//                        il[p] = i;
//                        iu[p] = l;
//                        i = k;
//                        while (jl <= ju && ind[jl] < i) {
//                            jl++;
//                        }
//                        if (jl > ju) {
//                            next = true;
//                            break;//+break
//                        }
//                        indu[p] = jl - 1;
//                    }
//                }
//                if (!next) {
//                    if (i > 0) {
//                        --i;
//                        while (++i != j) {
//                            t = a[i + 1];
//                            if (a[i] > t) {
//                                int k = i;
//                                do {
//                                    a[k + 1] = a[k];
//                                } while (t < a[--k]);
//                                a[k + 1] = t;
//                            }
//                        }
//                    } else {
//                        break;
//                    }
//                }
//            } while (true);
//        } while (true);
//    }
}
