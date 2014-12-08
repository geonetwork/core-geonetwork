package iso19139;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.fao.geonet.services.metadata.format.FormatterConstants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Jesse on 10/17/2014.
 */
public class PackageViewFormatterTest extends AbstractFormatterTest {

    @Autowired
    private IsoLanguagesMapper mapper;
    @Autowired
    private IsoLanguageRepository langRepo;
    @Autowired
    private SchemaManager manager;

    @Test
    @SuppressWarnings("unchecked")
    public void testBasicFormat() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final Path iso19139FormatterDir = manager.getSchemaDir("iso19139").resolve("formatter/package");
        java.nio.file.Files.walkFileTree(iso19139FormatterDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Files.exists(dir.resolve(FormatterConstants.VIEW_GROOVY_FILENAME))) {
                    String formatterId = iso19139FormatterDir.getFileName().toString().replace('\\', '/') + "/" + dir.getFileName();
                    final MockHttpServletResponse response = new MockHttpServletResponse();
                    try {
                        formatService.exec("eng", "html", "" + id, null, formatterId, "true", false, request, response);
                        final String view = response.getContentAsString();
                        // for now the fact that there was no error is good enough

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }
        });
    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = PackageViewFormatterTest.class.getResource("/iso19139/example.xml").getFile();
        return new File(mdFile);
    }
}
