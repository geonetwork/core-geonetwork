package org.fao.geonet.kernel.schema;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
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
public abstract class AbstractSchematronTest extends AbstractCoreIntegrationTest {

    protected static final Namespace SVRL_NAMESPACE = Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl");
    protected static final Namespace SCH_NAMESPACE = Namespace.getNamespace("sch", "http://purl.oclc.org/dsdl/schematron");
    protected static final ElementFilter FAILURE_FILTER = new ElementFilter("failed-assert", SVRL_NAMESPACE);

    protected static final List<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO, SVRL_NAMESPACE, SCH_NAMESPACE,GEONET, GML,
            GMX, SRV);

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Before
    public void before() throws IOException, JDOMException {
        File from = new File(AbstractInspireTest.class.getResource("/thesaurus/external/thesauri/theme/inspire-theme.rdf").getFile());
        File to = new File(dataDirectory.getThesauriDir(), "/external/thesauri/theme/inspire-theme.rdf");
        Files.copy(from, to);

        from = new File(AbstractInspireTest.class.getResource("/thesaurus/external/thesauri/theme/gemet-theme.rdf").getFile());
        to = new File(dataDirectory.getThesauriDir(), "/external/thesauri/theme/gemet-theme.rdf");
        Files.copy(from, to);
    }

    protected Map<String, Object> getParams(String schematronName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("lang", "eng");
        params.put("thesaurusDir", dataDirectory.getThesauriDir().getPath().replace("\\", "/"));
        params.put("rule", schematronName+".xsl");

        return params;
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
