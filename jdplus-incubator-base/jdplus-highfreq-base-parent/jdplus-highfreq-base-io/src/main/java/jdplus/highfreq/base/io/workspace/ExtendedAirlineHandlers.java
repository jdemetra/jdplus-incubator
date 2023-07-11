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
package jdplus.highfreq.base.io.workspace;

import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineDocument;
import jdplus.highfreq.base.core.extendedairline.decomposition.ExtendedAirlineDecompositionDocument;
import jdplus.highfreq.base.io.information.ExtendedAirlineDecompositionSpecMapping;
import jdplus.highfreq.base.io.information.ExtendedAirlineModellingSpecMapping;
import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializer;
import jdplus.toolkit.base.information.TsDocumentMapping;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.informationSet;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.parse;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ExtendedAirlineHandlers {

    public final WorkspaceFamily SA_DOC_HF_MODEL = parse("Modelling@documents@extendedairline");
    public final WorkspaceFamily SA_DOC_HF_DECOMPOSITION = parse("Seasonal adjustment@documents@extendedairline");

    @ServiceProvider(FamilyHandler.class)
    public final class DocModel implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_HF_MODEL,
                new InformationSetSerializer<ExtendedAirlineDocument>() {
            @Override
            public InformationSet write(ExtendedAirlineDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, ExtendedAirlineModellingSpecMapping.SERIALIZER, verbose, true);
            }

            @Override
            public ExtendedAirlineDocument read(InformationSet info) {

                ExtendedAirlineDocument doc = new ExtendedAirlineDocument();
                TsDocumentMapping.read(info, ExtendedAirlineModellingSpecMapping.SERIALIZER, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, "ExtendedAirlineModellingDoc");

    }

    @ServiceProvider(FamilyHandler.class)
    public final class DecompositionModel implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_HF_DECOMPOSITION,
                new InformationSetSerializer<ExtendedAirlineDecompositionDocument>() {
            @Override
            public InformationSet write(ExtendedAirlineDecompositionDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, ExtendedAirlineDecompositionSpecMapping.SERIALIZER, verbose, true);
            }

            @Override
            public ExtendedAirlineDecompositionDocument read(InformationSet info) {

                ExtendedAirlineDecompositionDocument doc = new ExtendedAirlineDecompositionDocument();
                TsDocumentMapping.read(info, ExtendedAirlineDecompositionSpecMapping.SERIALIZER, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, "ExtendedAirlineDecomposiitonDoc");

    }
}
