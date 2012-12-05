//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Allows a user to display a metadata in PDF with a particular formatters
 * 
 * @author fgravin
 */
public class PDF implements Service {
	
	private final String TMP_PDF_FILE = "Document";
	
    public Element exec(Element metadata, ServiceContext context) throws Exception {
        
    	Element htmlDoc = metadata.getChild("html");
        XMLOutputter printer = new XMLOutputter();
        String htmlContent = printer.outputString(htmlDoc);
        
        File tempDir = (File) context.getServlet().getServletContext().
        	       getAttribute( "javax.servlet.context.tempdir" );

        File tempFile = File.createTempFile(this.TMP_PDF_FILE, ".pdf", tempDir);
        OutputStream os = new FileOutputStream(tempFile);
        
        try {
	        ITextRenderer renderer = new ITextRenderer();
	        renderer.setDocumentFromString(htmlContent);
	        renderer.layout();
	        renderer.createPDF(os);
        }
        finally {
        	os.close();
        }
        
        Element res = BinaryFile.encode(200, tempFile.getAbsolutePath(), true);
        return res;
    }

	@Override
	public void init(String appPath, ServiceConfig params) throws Exception {
	}
}
