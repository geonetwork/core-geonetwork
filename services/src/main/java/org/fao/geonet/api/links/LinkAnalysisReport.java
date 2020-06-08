//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.links;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.fao.geonet.domain.Link;
import org.springframework.data.domain.Page;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a link analysis report
 */
public class LinkAnalysisReport {

    public static void create(final Page<Link> links,
                              final PrintWriter writer) throws Exception {
        CSVPrinter csvFilePrinter = null;

        try {
            CSVFormat csvFileFormat =
                CSVFormat.DEFAULT.withRecordSeparator("\n");
            csvFilePrinter = new CSVPrinter(writer, csvFileFormat);

            String[] entries = {"URL", "LastState", "LastCheck", "Protocol", "Records"};
            csvFilePrinter.printRecord(Arrays.asList(entries));

            for (Link link : links) {
                List<String> record = new ArrayList<>();
                record.add(link.getUrl());
                record.add(link.getLastState() + "");
                record.add(link.getLastCheck().getDateAndTime());
                record.add(link.getProtocol());
                record.add(link.getRecords().stream().map(r -> r.getMetadataUuid()).collect(Collectors.joining(" ")));
                csvFilePrinter.printRecord(record);
            }
        } finally {
            writer.flush();
            if (csvFilePrinter != null) {
                csvFilePrinter.flush();
            }
        }
    }
}
