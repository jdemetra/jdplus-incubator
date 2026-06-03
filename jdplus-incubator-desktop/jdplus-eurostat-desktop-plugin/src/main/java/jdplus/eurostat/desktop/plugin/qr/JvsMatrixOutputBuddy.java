/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.eurostat.desktop.plugin.qr;

import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.desktop.plugin.output.AbstractOutputNode;
import jdplus.sa.desktop.plugin.output.OutputFactoryBuddy;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.ConfigEditor;
import jdplus.toolkit.desktop.plugin.Converter;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.actions.Resetable;
import jdplus.toolkit.desktop.plugin.beans.BeanConfigurator;
import jdplus.toolkit.desktop.plugin.beans.BeanEditor;
import jdplus.toolkit.desktop.plugin.beans.BeanHandler;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.properties.PropertySheetDialogBuilder;
import lombok.NonNull;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.ServiceProvider;

import java.beans.IntrospectionException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import jdplus.eurostat.base.core.JvsMatrixOutputConfiguration;

import static jdplus.eurostat.base.core.JvsMatrixOutputConfiguration.*;
import jdplus.eurostat.base.core.JvsMatrixOutputFactory;
import nbbrd.io.text.IntProperty;

/**
 * @author Mats Maggi
 */
@ServiceProvider(service = OutputFactoryBuddy.class, position = 1100)
public class JvsMatrixOutputBuddy implements OutputFactoryBuddy, Configurable, ConfigEditor, Resetable {

    private static final BeanConfigurator<JvsMatrixOutputConfiguration, JvsMatrixOutputBuddy> configurator = createConfigurator();
    private JvsMatrixOutputConfiguration config = new JvsMatrixOutputConfiguration();

    public JvsMatrixOutputBuddy() {
    }

    @Override
    public @NonNull String getName() {
        return JvsMatrixOutputFactory.NAME;
    }

    @Override
    public AbstractOutputNode createNode() {
        return new JvsMatrixNode(config);
    }

    @Override
    public AbstractOutputNode createNodeFor(Object properties) {
        return properties instanceof JvsMatrixOutputConfiguration cmoc ? new JvsMatrixNode(cmoc) : null;
    }

    @Override
    public @NonNull
    Config getConfig() {
        return configurator.getConfig(this);
    }

    @Override
    public void setConfig(@NonNull Config config) throws IllegalArgumentException {
        configurator.setConfig(this, config);
    }

    @Override
    public @NonNull
    Config editConfig(@NonNull Config config) throws IllegalArgumentException {
        return configurator.editConfig(config);
    }

    @Override
    public void configure() {
        Configurable.configure(this, this);
    }

    @Override
    public void reset() {
        config = new JvsMatrixOutputConfiguration();
    }

    private static BeanConfigurator<JvsMatrixOutputConfiguration, JvsMatrixOutputBuddy> createConfigurator() {
        return new BeanConfigurator<>(new JvsMatrixOutputBeanHandler(), new JvsMatrixOutputConverter(), new JvsMatrixOutputBeanEditor());
    }

    private static final class JvsMatrixOutputBeanHandler implements BeanHandler<JvsMatrixOutputConfiguration, JvsMatrixOutputBuddy> {

        @Override
        public JvsMatrixOutputConfiguration load(JvsMatrixOutputBuddy resource) {
            return resource.config.clone();
        }

        @Override
        public void store(JvsMatrixOutputBuddy resource, JvsMatrixOutputConfiguration bean) {
            resource.config = bean;
        }
    }

    private static final class JvsMatrixOutputBeanEditor implements BeanEditor {

        @Override
        public boolean editBean(@NonNull Object bean) throws IntrospectionException {
            return new PropertySheetDialogBuilder()
                    .title("Edit Jvs Matrix output config")
                    .editNode(new JvsMatrixNode((JvsMatrixOutputConfiguration) bean));
        }
    }

    private static final class JvsMatrixOutputConverter implements Converter<JvsMatrixOutputConfiguration, Config> {

        private final Property<File> folderParam = Property.of("folder", Path.of("").toFile(), Parser.onFile(), Formatter.onFile());
        private final Property<String> fileNameParam = Property.of("fileName", DEFAULT_FILE_NAME, Parser.onString(), Formatter.onString());
        private final BooleanProperty fullNameParam = BooleanProperty.of("fullName", DEFAULT_FULL_NAME);
        private final IntProperty detailParam = IntProperty.of("detail", DEFAULT_DETAIL);
        private final Property<Charset> charsetParam = Property.of("charset", DEFAULT_CHARSET, Parser.onCharset(), Formatter.onCharset());

        @Override
        public Config doForward(JvsMatrixOutputConfiguration a) {
            Config.Builder result = Config.builder("eurostat_qr", "jvs_report", "3.0");
            folderParam.set(result::parameter, a.getFolder());
            fileNameParam.set(result::parameter, a.getFileName());
            fullNameParam.set(result::parameter, a.isFullName());
            detailParam.set(result::parameter, a.getDetail());
            charsetParam.set(result::parameter, a.getCharset());
            return result.build();
        }

        @Override
        public JvsMatrixOutputConfiguration doBackward(Config b) {
            JvsMatrixOutputConfiguration result = new JvsMatrixOutputConfiguration();
            result.setFolder(folderParam.get(b::getParameter));
            result.setFileName(fileNameParam.get(b::getParameter));
            result.setFullName(fullNameParam.get(b::getParameter));
            result.setDetail(detailParam.get(b::getParameter));
            result.setCharset(charsetParam.get(b::getParameter));
            return result;
        }
    }

    public final static class JvsMatrixNode extends AbstractOutputNode<JvsMatrixOutputConfiguration> {

        private static JvsMatrixOutputConfiguration newConfiguration() {
            JvsMatrixOutputConfiguration config = new JvsMatrixOutputConfiguration();
            return config;
        }

        public JvsMatrixNode() {
            super(newConfiguration());
            setDisplayName(JvsMatrixOutputFactory.NAME);
        }

        public JvsMatrixNode(JvsMatrixOutputConfiguration config) {
            super(config);
            setDisplayName(JvsMatrixOutputFactory.NAME);
        }

        @Override
        protected Sheet createSheet() {
            JvsMatrixOutputConfiguration config = getLookup().lookup(JvsMatrixOutputConfiguration.class);
            Sheet sheet = super.createSheet();

            NodePropertySetBuilder builder = new NodePropertySetBuilder();
            builder.reset("Location");
            builder.withFile().select(config, "Folder").directories(true).description("Base output folder. Will be extended by the workspace and processing names").add();
            builder.with(String.class).select(config, "fileName").display("File Name").add();
            builder.with(Charset.class).select(config, "charset").display("Charset").add();
            sheet.put(builder.build());

            builder.reset("Content");
            builder.withBoolean().select(config, "FullName").display("Full series name")
                    .description("If true, the fully qualified name of the series will be used. "
                            + "If false, only the name of the series will be displayed.").add();
            builder.withInt().select(config, "Detail").display("Detail")
                    .description("Level of details [0,3].").add();
            sheet.put(builder.build());

            return sheet;
        }

        @Override
        public SaOutputFactory getFactory() {
            return new JvsMatrixOutputFactory(getLookup().lookup(JvsMatrixOutputConfiguration.class));
        }
    }
}
