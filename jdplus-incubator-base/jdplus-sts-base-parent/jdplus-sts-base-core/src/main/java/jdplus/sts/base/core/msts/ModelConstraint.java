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
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.composite.MultivariateCompositeSsf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author palatej
 */
public class ModelConstraint implements ModelItem {

    private final String name;
    private final List<Item> items = new ArrayList<>();
    private final double value;

    @lombok.Value
    public static class Item {

        String cmp;
        double c;
        ISsfLoading loading;
    }

    public ModelConstraint(String name, double value) {
        this.name = name;
        this.value = value;
    }
    
    private ModelConstraint(ModelConstraint cnt){
        this.name=cnt.name;
        this.items.addAll(cnt.items);
        this.value=cnt.value;
    }
    
    @Override
    public ModelConstraint duplicate(){
        return new ModelConstraint(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public void add(String item) {
        items.add(new Item(item, 1.0, null));
    }

    public void add(String item, double coeff, ISsfLoading loading) {
        items.add(new Item(item, coeff, loading));
    }

    public double getValue() {
        return value;
    }

    public int getItemsCount() {
        return items.size();
    }

    public Item getItem(int pos) {
        return items.get(pos);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add((p, builder) -> {
            int pos = 0;
            double v = p.get(pos++);
            MultivariateCompositeSsf.Equation eq = new MultivariateCompositeSsf.Equation(v);
            for (Item item : items) {
                eq.add(new MultivariateCompositeSsf.Item(item.cmp, item.c, item.loading));
            }
            builder.add(eq);
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.emptyList();
    }

}
