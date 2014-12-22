package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import org.fao.geonet.SystemInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public abstract class AbstractTemplateParserTest {

    public static void assertCorrectRender(TNode parseTree, Map<String, Object> model, String expected) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);

        parseTree.render(context);

        assertEquals(expected + "\n" + result, expected.replaceAll("\\n|\\r|\\s+", ""), result.toString().replaceAll("\\n|\\r|\\s+", ""));
    }

    public static TemplateParser createTestParser(String systemInfoStage) throws IllegalAccessException, InstantiationException {
        SystemInfo info = SystemInfo.createForTesting(systemInfoStage);
        final TemplateParser parser = new TemplateParser();
        final TextContentParser contentParser = TextContentParserTest.createTestTextContentParser();
        parser.textContentParser = contentParser;
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(
                new TNodeFactoryIf(info, contentParser), new TNodeFactoryRepeat(info, contentParser), new TNodeFactoryTranslate(info, contentParser),
                new TNodeFactoryInclude(info, contentParser), new TNodeFactoryTransclude(info, contentParser));
        return parser;
    }
}