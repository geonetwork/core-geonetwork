package de.fzi.dbs.xml.transform;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
            assertEquals(1, firstFactory.compilations.get());
            assertEquals(0, secondFactory.compilations.get());

            secondFactory.clearCache();

            assertTransformation(firstFactory, "after clearing");
            assertEquals(2, firstFactory.compilations.get());
        } finally {
            firstFactory.clearCache();
        }
    }

    @Test
    public void recompilesWhenStylesheetTimestampChangesDuringCompilation() throws Exception {
        stylesheet = folder.newFile("modified-during-compilation.xsl");
        Files.write(stylesheet.toPath(), ("<xsl:stylesheet version=\"1.0\" "
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:output method=\"text\"/>"
            + "<xsl:template match=\"/\"><xsl:value-of select=\"/value\"/></xsl:template>"
            + "</xsl:stylesheet>").getBytes(StandardCharsets.UTF_8));
        long originalTimestamp = stylesheet.lastModified();
        CountingTransformerFactory factory = new CountingTransformerFactory() {
            private boolean timestampChanged;

            @Override
            public Templates newTemplates(Source source) throws TransformerConfigurationException {
                Templates templates = super.newTemplates(source);
                if (!timestampChanged) {
                    try {
                        Files.setLastModifiedTime(stylesheet.toPath(),
                            FileTime.fromMillis(originalTimestamp + TimeUnit.SECONDS.toMillis(1)));
                    } catch (java.io.IOException exception) {
                        throw new TransformerConfigurationException(exception);
                    }
                    timestampChanged = true;
                }
                return templates;
            }
        };
        factory.clearCache();
        try {
            assertTransformation(factory, "first");
            assertEquals(1, factory.compilations.get());
            assertTrue(stylesheet.lastModified() > originalTimestamp);

            assertTransformation(factory, "after modification");
            assertEquals(2, factory.compilations.get());

            assertTransformation(factory, "cached");
            assertEquals(2, factory.compilations.get());
        } finally {
            factory.clearCache();
        }
    }

    @Test(timeout = 30000)
    public void compilesStylesheetOnceUnderConcurrentAccess() throws Exception {
        stylesheet = folder.newFile("concurrent.xsl");
        Files.write(stylesheet.toPath(), ("<xsl:stylesheet version=\"1.0\" "
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:output method=\"text\"/>"
            + "<xsl:template match=\"/\"><xsl:value-of select=\"/value\"/></xsl:template>"
            + "</xsl:stylesheet>").getBytes(StandardCharsets.UTF_8));
        CountingTransformerFactory factory = new CountingTransformerFactory();
        factory.clearCache();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> results = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                String value = "v" + i;
                results.add(pool.submit(() -> {
                    assertTransformation(factory, value);
                    return null;
                }));
            }
            for (Future<?> result : results) {
                result.get();
            }
            assertEquals(1, factory.compilations.get());
        } finally {
            pool.shutdownNow();
            factory.clearCache();
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
        private final AtomicInteger compilations = new AtomicInteger();

        @Override
        public Templates newTemplates(Source source) throws TransformerConfigurationException {
            compilations.incrementAndGet();
            return super.newTemplates(source);
        }
    }
}
