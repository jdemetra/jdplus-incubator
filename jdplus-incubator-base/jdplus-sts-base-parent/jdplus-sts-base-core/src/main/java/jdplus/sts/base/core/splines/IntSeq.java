/*
 * Copyright 2024 JDemetra+.
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
package jdplus.sts.base.core.splines;

/**
 *
 * @author Jean Palate
 */
public interface IntSeq {
    
    int length();
    
    int pos(int idx);
    
    static IntSeq sequential(int from, int to){
        return new SequentialIntSeq(from, to);
    }
    
    static class SequentialIntSeq implements IntSeq{
    
        private final int start, length;
        
        SequentialIntSeq(int from, int to){
            this.start=from;
            this.length=to-from;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public int pos(int idx) {
            return start+idx;
        }
        
    }
    
}
