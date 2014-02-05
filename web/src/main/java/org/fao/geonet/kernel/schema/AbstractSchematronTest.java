package org.fao.geonet.kernel.schema;

import com.google.common.collect.Lists;
import jeeves.utils.Xml;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
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
    protected final static File THESAURUS_DIR = new File(WEBAPP_DIR, "WEB-INF/data/config/codelist");
    protected final static File SCHEMA_PLUGINS = new File(WEBAPP_DIR, "WEB-INF/data/config/schema_plugins/");
    protected final static File TMP_DIR;
    protected final static Map<String, String> PARAMS = new HashMap<String, String>();

    static {
        try {
            TMP_DIR = File.createTempFile("xyz", "tt");
            TMP_DIR.delete();
            TMP_DIR.mkdirs();

            final File targetUtilsFnFile = new File(TMP_DIR, "xsl/utils-fn.xsl");
            targetUtilsFnFile.getParentFile().mkdirs();
            FileUtils.copyFile(new File(WEBAPP_DIR, "xsl/utils-fn.xsl"), targetUtilsFnFile);

            PARAMS.put("lang", "eng");
            PARAMS.put("thesaurusDir", THESAURUS_DIR.getPath());
            PARAMS.put("rule", "inspire");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected static Pair<Element, File> compileSchematron(File sourceFile) {
        try {
            Element schematron = Xml.loadFile(sourceFile);
            Element xsl = Xml.transform(schematron, SCHEMATRON_COMPILATION_FILE.getAbsolutePath());

            File outputFile = new File(TMP_DIR, "path/requiredtoFind/utilsfile/" + sourceFile.getName() + ".xsl");
            outputFile.getParentFile().mkdirs();

            final String string = Xml.getString(xsl);
            FileUtils.write(outputFile, string);

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

    @AfterClass
    public static void deleteXsl() throws IOException {
        FileUtils.deleteDirectory(TMP_DIR);
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
