package org.fao.geonet.services.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import jeeves.utils.BinaryFile;

import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class PDF implements Service {

    private static final String TMP_PDF_FILE = "Metadata";

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);
        
        if (id == null) {
            throw new MetadataNotFoundEx("Metadata not found.");
        }

        Lib.resource.checkPrivilege(context, id, AccessManager.OPER_VIEW);
        
        LocalServiceRequest request = LocalServiceRequest.create("metadata.show.xml", params);
        request.setLanguage(context.getLanguage());
        request.setDebug(false);
        
        context.executeOnly(request);
        String htmlContent = request.getResultString();
        
        File tempDir = (File) context.getServlet().getServletContext().
                       getAttribute( "javax.servlet.context.tempdir" );

        File tempFile = File.createTempFile(TMP_PDF_FILE, ".pdf", tempDir);
        OutputStream os = new FileOutputStream(tempFile);
        
        try {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlContent, context.getBaseUrl());
                renderer.layout();
                renderer.createPDF(os);
        }
        finally {
                os.close();
        }
        
        Element res = BinaryFile.encode(200, tempFile.getAbsolutePath(), true);
        return res;
    }

}
