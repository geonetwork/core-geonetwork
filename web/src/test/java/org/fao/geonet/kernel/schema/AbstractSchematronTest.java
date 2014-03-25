package org.fao.geonet.kernel.schema;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.constants.Geonet.Namespaces.*;

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

    protected static final String CLASS_NAME = AbstractSchematronTest.class.getSimpleName() + ".class";
    protected final static File CLASS_FILE = new File(AbstractSchematronTest.class.getResource(CLASS_NAME).getFile());

    protected final static File WEBAPP_DIR = findWebappDir(CLASS_FILE);
    protected final static File SCHEMATRON_COMPILATION_FILE = new File(WEBAPP_DIR, "WEB-INF/classes/schematron/iso_svrl_for_xslt2.xsl");
    protected final static File SCHEMA_PLUGINS = new File(WEBAPP_DIR, "WEB-INF/data/config/schema_plugins/");
    protected Map<String, String> params = new HashMap<String, String>();

    protected static File THESAURUS_DIR;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() {
        THESAURUS_DIR = new File(AbstractSchematronTest.class.getResource("/thesaurus").getFile());
    }
    @Before
    public void before (){
        try {
            final File targetUtilsFnFile = new File(temporaryFolder.getRoot(), "xsl/utils-fn.xsl");
            targetUtilsFnFile.getParentFile().mkdirs();
            FileUtils.copyFile(new File(WEBAPP_DIR, "xsl/utils-fn.xsl"), targetUtilsFnFile);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected Pair<Element, File> compileSchematron(File sourceFile) {
        try {
            Element schematron = Xml.loadFile(sourceFile);
            Element xsl = Xml.transform(schematron, SCHEMATRON_COMPILATION_FILE.getAbsolutePath());

            final String ruleName = sourceFile.getName().substring(0, sourceFile.getName().indexOf('.'));

            params.clear();
            params.put("lang", "eng");
            params.put("thesaurusDir", THESAURUS_DIR.getPath());
            params.put("rule", ruleName+".xsl");

            File outputFile = new File(temporaryFolder.getRoot(), "path/requiredtoFind/utilsfile/" + ruleName + ".xsl");
            outputFile.getParentFile().mkdirs();

            final String string = Xml.getString(xsl);
            FileUtils.write(outputFile, string);

            BinaryFile.copyDirectory(new File(sourceFile.getParentFile().getParentFile(), "loc/eng"), new File(outputFile.getParentFile().getParentFile(), "loc/eng"));

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
