/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.eurostat.base.core;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 */
@lombok.Data
public final class JvsMatrixOutputConfiguration implements Cloneable {

    public static final String DEFAULT_FILE_NAME = "JobVacancySurveyQR";
    public static final boolean DEFAULT_FULL_NAME = true;
    public static final int DEFAULT_DETAIL = 3;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    private File folder;
    private String fileName = DEFAULT_FILE_NAME;
    private boolean fullName = DEFAULT_FULL_NAME;
    private int detail =DEFAULT_DETAIL;
    private Charset charset = DEFAULT_CHARSET;

    @Override
    public JvsMatrixOutputConfiguration clone() {
        try {
            return (JvsMatrixOutputConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
