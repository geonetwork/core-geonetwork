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

package org.fao.geonet.api.records.formatters;

import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.api.records.extent.MapRenderer;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Allows a user to display a metadata in PDF with a particular formatters
 *
 * @author fgravin
 */
public class PDF implements Service {

    private static final String TMP_PDF_FILE = "Document";

    public Element exec(Element metadata, ServiceContext context) throws Exception {

        Element htmlDoc = metadata.getChild("html");
        XMLOutputter printer = new XMLOutputter();
        String htmlContent = printer.outputString(htmlDoc);
        XslUtil.setNoScript();
        File tempDir = (File) context.getServlet().getServletContext().
            getAttribute("javax.servlet.context.tempdir");

        Path tempFile = Files.createTempFile(tempDir.toPath(), TMP_PDF_FILE, ".pdf");

        try (OutputStream os = Files.newOutputStream(tempFile)) {
            ITextRenderer renderer = new ITextRenderer();
            String siteUrl = context.getBean(SettingManager.class).getSiteURL(context);
            MapRenderer mapRenderer = new MapRenderer(context);
            renderer.getSharedContext().setReplacedElementFactory(new ImageReplacedElementFactory(siteUrl,
                renderer.getSharedContext().getReplacedElementFactory(), mapRenderer));
            renderer.setDocumentFromString(htmlContent, siteUrl);
            renderer.layout();
            renderer.createPDF(os);
        }

        return BinaryFile.encode(200, tempFile.toAbsolutePath().normalize(), true).getElement();
    }

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }
}
