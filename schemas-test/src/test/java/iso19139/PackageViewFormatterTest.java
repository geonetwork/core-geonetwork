package iso19139;

import com.google.common.io.Files;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;

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
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final File iso19139Dir = new File(manager.getSchemaDir("iso19139"), "formatter/package");
        final Iterable<File> packages = Files.fileTreeTraverser().children(iso19139Dir);
        for (File aPackage : packages) {
            // just check that the formatter works

            String formatterId = iso19139Dir.getName() + "/" + aPackage.getName();
            String view = formatService.exec("eng", "html", "" + id, null, formatterId, "true", false, request);

            // for now the fact that there was no error is good enough
        }


    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = PackageViewFormatterTest.class.getResource("/iso19139/example.xml").getFile();
        return new File(mdFile);
    }
}
