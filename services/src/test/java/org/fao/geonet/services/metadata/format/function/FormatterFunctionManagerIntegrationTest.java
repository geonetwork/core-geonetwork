package org.fao.geonet.services.metadata.format.function;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Namespace;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class FormatterFunctionManagerIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private FormatterFunctionManager functionManager;
    @Autowired
    private SchemaManager schemaManager;

    @Test
    public void testGetFunctions() throws Exception {
        final Set<String> schemas = schemaManager.getSchemas();
        final FormatterFunctionMap functions = functionManager.getFunctions(null);
        assertNotSame(functions, functionManager.getFunctions(null));

        assertFalse(functions.functions().isEmpty());
        for (String schemaName : schemas) {
            final FormatterFunctionMap schemaSpecificFunctions = functionManager.getFunctions(schemaName);
            final MetadataSchema schemaObj = this.schemaManager.getSchema(schemaName);
            final List<Namespace> namespaces = schemaObj.getNamespaces();
            for (Namespace namespace : namespaces) {
                assertTrue("Missing namespace: " + namespace, schemaSpecificFunctions.getNamespaces().contains(namespace));
            }
            functions.merge(schemaSpecificFunctions);
        }

        for (FormatterFunction formatterFunction : functions.functions()) {
            formatterFunction.assertValidDependencies("'GetFunctions'", functions);
        }
    }
}