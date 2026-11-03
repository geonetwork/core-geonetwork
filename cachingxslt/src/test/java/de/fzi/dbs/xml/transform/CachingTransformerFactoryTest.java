package de.fzi.dbs.xml.transform;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;

public class CachingTransformerFactoryTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File stylesheet;

    @Test
    public void cachesTemplatesAcrossFactoriesAndClearsSharedCache() throws Exception {
        stylesheet = folder.newFile("stylesheet.xsl");
        String content = "<xsl:stylesheet version=\"1.0\" "
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:output method=\"text\"/>"
            + "<xsl:template match=\"/\"><xsl:value-of select=\"/value\"/></xsl:template>"
            + "</xsl:stylesheet>";
        Files.write(stylesheet.toPath(), content.getBytes(StandardCharsets.UTF_8));
        CountingTransformerFactory firstFactory = new CountingTransformerFactory();
        CountingTransformerFactory secondFactory = new CountingTransformerFactory();

        firstFactory.clearCache();
        try {
            assertTransformation(firstFactory, "first");
            assertTransformation(firstFactory, "second");
            assertTransformation(secondFactory, "third");
            assertEquals(1, firstFactory.compilations);
            assertEquals(0, secondFactory.compilations);

            secondFactory.clearCache();

            assertTransformation(firstFactory, "after clearing");
            assertEquals(2, firstFactory.compilations);
        } finally {
            firstFactory.clearCache();
        }
    }

    private void assertTransformation(CachingTransformerFactory factory, String value)
        throws Exception {
        Transformer transformer = factory.newTransformer(new StreamSource(stylesheet));
        StringWriter output = new StringWriter();
        transformer.transform(new StreamSource(new StringReader("<value>" + value + "</value>")),
            new StreamResult(output));
        assertEquals(value, output.toString());
    }

    private static class CountingTransformerFactory extends CachingTransformerFactory {
        private int compilations;

        @Override
        public Templates newTemplates(Source source) throws TransformerConfigurationException {
            compilations++;
            return super.newTemplates(source);
        }
    }
}