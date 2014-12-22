package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Maps;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.TransformationContext;
import org.fao.geonet.utils.IO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URL;
import java.util.Map;

/**
 * @author Jesse on 12/3/2014.
 */
public class TNodeTranscludeTest extends AbstractTemplateParserTest {
    Handlers handlers;
    Functions functions;
    Environment env;

    @Before
    public void setUp() throws Exception {
        handlers = Mockito.mock(Handlers.class);
        functions = Mockito.mock(Functions.class);
        env = Mockito.mock(Environment.class);

        Mockito.when(handlers.fileResult(Mockito.anyString(), Mockito.anyMap())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final SystemInfo info = SystemInfo.createForTesting(SystemInfo.STAGE_TESTING);
                SimpleTNode node = new SimpleTNode(info, TextContentParserTest.createTestTextContentParser(), "div", TNode.EMPTY_ATTRIBUTES);
                node.setTextContent("{{include}}{{other1}}{{other2}}{{other3}}");
                final Map<String, Object> substitutions = (Map<String, Object>) invocation.getArguments()[1];
                return new FileResult(node, substitutions);
            }
        });

        new TransformationContext(handlers, functions, env).setThreadLocal();

    }

    @Test
    public void testTransclude() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("transclude-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        model.put("other2", "Other&Two");
        model.put("other3", "Other Three");
        String expected = "<div><div>"
                          + "    within"
                          + "    </div>Other OneOther&TwoOther Three</div>";

        assertCorrectRender(parseTree, model, expected);
    }

    @Test
    public void testTranscludeReplace() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("transclude-template-replace.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        model.put("other2", "Other&Two");
        model.put("other3", "Other Three");

        String expected = "<div>withinOther OneOther&TwoOther Three</div>";
        assertCorrectRender(parseTree, model, expected);
    }
}
