import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.JDOMException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Test each xml file in each schema plugin and verify that they are all well-formed.
 *
 * @author Jesse on 2/5/2015.
 */
public class AllXmlFilesAreSyntacticallyCorrect {
    @Test
    public void testWellformedXmlFiles() throws Exception {
        Path classFile = AbstractCoreIntegrationTest.getClassFile(getClass()).toPath();
        Path tmp = classFile;
        while(tmp != null && !Files.exists(tmp.resolve("schemas/dublin-core"))) {
            tmp = tmp.getParent();
        }
        final List<String> badXmlFiles = Lists.newArrayList();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(tmp.resolve("schemas"))) {
            for (Path path : paths) {
                Path pluginDir = path.resolve("src/main/plugin");
                if (Files.exists(pluginDir)) {
                    Files.walkFileTree(pluginDir, new SimpleFileVisitor<Path>(){
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.toString().endsWith(".xml") || file.toString().endsWith(".html") ||
                                file.toString().endsWith(".xsd") || file.toString().endsWith(".xsl")) {
                                try {
                                    Xml.loadFile(file);
                                } catch (JDOMException e) {
                                    badXmlFiles.add(file + " -- " + e.getMessage());
                                }
                            }
                            return super.visitFile(file, attrs);
                        }
                    });
                }
            }
        }

        assertTrue("\n  *" + Joiner.on("\n  * ").join(badXmlFiles), badXmlFiles.isEmpty());
    }
}
