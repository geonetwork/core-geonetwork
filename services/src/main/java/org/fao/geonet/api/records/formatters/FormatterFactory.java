/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.api.records.formatters;

import jeeves.server.context.ServiceContext;

import java.nio.file.Files;
import java.nio.file.Path;

public class FormatterFactory {
    private FormatterFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a FormatterImpl instance based on the provided parameters.
     *
     * @param context       ServiceContext for accessing beans.
     * @param viewXslFile   Formatter view.xsl file. Used to determine if the formatter exists.
     * @param formatterId   Formatter identifier.
     * @return A FormatterImpl instance.
     * @throws IllegalArgumentException Exception thrown if the viewXslFile does not exist for the formatter.
     */
    public static FormatterImpl getFormatter(ServiceContext context, Path viewXslFile, String formatterId) throws IllegalArgumentException {
        FormatterImpl formatter;

        if (Files.exists(viewXslFile)) {
            if (formatterId.contains("dcat")) {
                formatter = context.getBean(DcatFormatter.class);
            } else {
                formatter = context.getBean(XsltFormatter.class);
            }

            return formatter;
        } else {
            throw new IllegalArgumentException(String.format("Formatter id : %s is not valid. Can't find file: %s", formatterId, viewXslFile));
        }
    }
}
