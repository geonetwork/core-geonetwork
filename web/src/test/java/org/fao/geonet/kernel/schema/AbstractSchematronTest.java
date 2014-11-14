package org.fao.geonet.kernel.schema;

import com.google.common.collect.Lists;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GEONET;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.constants.Geonet.Namespaces.GML;
import static org.fao.geonet.constants.Geonet.Namespaces.GMX;
import static org.fao.geonet.constants.Geonet.Namespaces.SRV;

/**
 * Test.
 * <p/>
 * Created by Jesse on 1/31/14.
 */
public class AbstractSchematronTest {

    protected static final Namespace SVRL_NAMESPACE = Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl");
    protected static final Namespace SCH_NAMESPACE = Namespace.getNamespace("sch", "http://purl.oclc.org/dsdl/schematron");
    protected static final ElementFilter FAILURE_FILTER = new ElementFilter("failed-assert", SVRL_NAMESPACE);

    protected static final List<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO, SVRL_NAMESPACE, SCH_NAMESPACE,GEONET, GML,
            GMX, SRV);

    protected final static Path WEBAPP_DIR = AbstractCoreIntegrationTest.getWebappDir(AbstractSchematronTest.class);
    protected final static Path SCHEMATRON_COMPILATION_FILE = WEBAPP_DIR.resolve("WEB-INF/classes/schematron/iso_svrl_for_xslt2.xsl");
    protected Map<String, Object> params = new HashMap<String, Object>();

    protected static Path THESAURUS_DIR;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        THESAURUS_DIR = IO.toPath(AbstractSchematronTest.class.getResource("/thesaurus").toURI());
    }
    @Before
    public void before (){
        try {
            final Path targetUtilsFnFile = temporaryFolder.getRoot().toPath().resolve("xsl/utils-fn.xsl");
            Files.createDirectories(targetUtilsFnFile.getParent());
            IO.copyDirectoryOrFile(WEBAPP_DIR.resolve("xsl/utils-fn.xsl"), targetUtilsFnFile, false);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected Path getSchematronFile(String schema, String schematronFile) {
        return getSchematronDir(schema).resolve(schematronFile);
    }

    protected Path getSchematronDir(String schema) {
        return WEBAPP_DIR.resolve("../../../../schemas").normalize().resolve(schema).resolve("src/main/plugin").resolve(schema).
                resolve("schematron");
    }

    protected Pair<Element, Path> compileSchematron(Path sourceFile) {
        try {
            Element schematron = Xml.loadFile(sourceFile);
            Element xsl = Xml.transform(schematron, SCHEMATRON_COMPILATION_FILE);

            final String sourceFileName = sourceFile.getFileName().toString();
            final String ruleName = sourceFileName.substring(0, sourceFileName.indexOf('.'));

            params.clear();
            params.put("lang", "eng");
            params.put("thesaurusDir", THESAURUS_DIR.toAbsolutePath().toString().replace("\\", "/"));
            params.put("rule", ruleName+".xsl");

            Path outputFile = temporaryFolder.getRoot().toPath().resolve("path/requiredtoFind/utilsfile/" + ruleName + ".xsl");
            Files.createDirectories(outputFile.getParent());

            final String string = Xml.getString(xsl);
            Files.write(outputFile, string.getBytes(Constants.CHARSET));

            final Path testLoc = outputFile.getParent().getParent().resolve("loc/eng");
            if (!Files.exists(testLoc)) {
                IO.copyDirectoryOrFile(sourceFile.getParent().getParent().resolve("loc/eng"), testLoc, false);
            }

            return Pair.read(schematron, outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static File findWebappDir(File dir) {
        File webappDir = new File(dir, "src/main/webapp");
        if (webappDir.exists()) {
            return webappDir;
        }
        return findWebappDir(dir.getParentFile());
    }

    protected int countFailures(Element results) {
        Iterator failures = results.getDescendants(FAILURE_FILTER);
        int count = 0;
        while (failures.hasNext()) {
            failures.next();
            count++;
        }
        return count;
    }
}
