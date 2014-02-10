package org.fao.geonet.services.metadata.schema;

import com.google.common.collect.Lists;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A service for the metadata validation administration UI.
 *
 * Created by Jesse on 2/9/14.
 */
public class SchematronAdmin implements Service {

    private SchematronService schematronService = new SchematronService();

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        Element schematrons = schematronService.exec(params, context);
        Element schemas = new Element("schemas");
        @SuppressWarnings("unchecked")
        List<Element> schematronElements = Lists.newArrayList(schematrons.getChildren());

        for (Element element : schematronElements) {
            String schemaname = element.getChildText("schemaname");
            Element schemaEl = schemas.getChild(schemaname);
            if (schemaEl == null) {
                schemaEl = new Element(schemaname);
                schemaEl.addContent(new Element("name").setText(schemaname));
                schemas.addContent(schemaEl);
            }

            element.setName("schematron");
            schemaEl.addContent(element.detach());
        }

        @SuppressWarnings("unchecked")
        List<Element> schemasChildren = schemas.getChildren();
        SchemaManager schemaManager = context.getApplicationContext().getBean(SchemaManager.class);

        for (Element schemaEl : schemasChildren) {
            String schemaName = schemaEl.getName();
            File file = new File(schemaManager.getSchemaDir(schemaName), "schematron" + File.separator + "criteria-type.xml");
            if (file.exists()) {
                Element criteriaType = Xml.loadFile(file);
                criteriaType.setName("criteriaTypes");
                schemaEl.addContent(criteriaType);
            }
        }
        return schemas;
    }
}
