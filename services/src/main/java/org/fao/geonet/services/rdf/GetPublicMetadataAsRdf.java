package org.fao.geonet.services.rdf;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.services.thesaurus.GetList;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;

public class GetPublicMetadataAsRdf implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {}

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        Element thesaurusEl = new GetList().exec(params, context);

        RdfOutputManager manager = new RdfOutputManager((Element) thesaurusEl.getChild("thesauri").detach());

        RdfSearcher rdfHarvestSearcher = new RdfSearcher(params, context);
        File rdfFile = manager.createRdfFile(context, rdfHarvestSearcher);

        return BinaryFile.encode(200, rdfFile.toPath().toAbsolutePath().normalize(), true).getElement();
    }

}