//==============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Formatter for DCAT output, converting RDF/XML to TURTLE or JSON-LD formats.
 * Uses XsltFormatter for initial formatting and then converts the result.
 */
@Component
public class DcatFormatter implements FormatterImpl {
    @Autowired
    private XsltFormatter formatterImpl;

    @Override
    public String format(FormatterParams fparams) throws Exception {
        String rdfXml = formatterImpl.format(fparams);

        if (fparams.formatType.equals(FormatType.turtle)) {
            return converRdfToFormat(rdfXml, "TURTLE");
        } else if (fparams.formatType.equals(FormatType.jsonld)) {
            return converRdfToFormat(rdfXml, "JSONLD");
        } else {
            // Return original RDF/XML
            return rdfXml;
        }
    }

    private String converRdfToFormat(String rdfXml, String format) throws IOException {
        InputStream is = new ByteArrayInputStream(rdfXml.getBytes(StandardCharsets.UTF_8));

        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            final Model model = ModelFactory.createDefaultModel();

            model.read(is, null);
            model.write(os, format);

            return os.toString();
        }

    }
}
