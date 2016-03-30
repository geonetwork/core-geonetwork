/*
 * =============================================================================
 * ===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api.records.attachments;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Replace old metadata link to uploaded file by new API
 *
 *  http://localhost:8080/geonetwork/srv/en/resources.get?uuid=da165110-88fd-11da-a88f-000d939bc5d8&fname=basins.zip&access=private
 *
 *  is replaced by
 *
 *  http://localhost:8080/geonetwork/srv/api/metadata/da165110-88fd-11da-a88f-000d939bc5d8/resources/basins.zip
 *
 *
 * Created by francois on 12/01/16.
 */
public class MetadataResourceDatabaseMigration implements DatabaseMigrationTask {

    private static final ArrayList<Namespace> NAMESPACES =
            Lists.newArrayList(
                    ISO19139Namespaces.GMD,
                    ISO19139Namespaces.GCO);
    private static final String XPATH = "*//*[contains(text(), '/resources.get?')]";
    private static final Pattern pattern = Pattern.compile(
            "(.*)\\/([a-zA-Z0-9_\\-]+)\\/([a-z]{2,3})\\/resources.get?.*fname=([\\p{L}\\w\\s\\.\\-]+)(&.*|$)");

    @Override
    public void update(Connection connection) throws SQLException {
        Log.debug(Geonet.DB, "MetadataResourceDatabaseMigration");

        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE metadata SET data=? WHERE id=?")
        ) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT data,id,uuid FROM metadata WHERE isharvested = 'n'")
            ) {
                int numInBatch = 0;
                while (resultSet.next()) {
                    final Element xml = Xml.loadString(resultSet.getString(1), false);
                    final int id = resultSet.getInt(2);
                    final String uuid = resultSet.getString(3);
                    boolean changed = updateMetadataResourcesLink(xml, uuid);
                    if (changed) {
                        String updatedData = Xml.getString(xml);
                        update.setString(1, updatedData);
                        update.setInt(2, id);
                        update.addBatch();
                        numInBatch++;
                        if (numInBatch > 200) {
                            update.executeBatch();
                            numInBatch = 0;
                        }
                    }
                }
                update.executeBatch();
            } catch (java.sql.BatchUpdateException e) {
                System.out.println("Error occurred while updating resource links:");
                e.printStackTrace();
                SQLException next = e.getNextException();
                while (next != null) {
                    System.err.println("Next error: ");
                    next.printStackTrace();
                }

                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    public static boolean updateMetadataResourcesLink(@Nonnull Element xml,
                                                      @Nullable String uuid) throws JDOMException {
        boolean changed = false;

        if (uuid == null) {
            final Element uuidElement = Xml.selectElement(
                    xml,
                    "gmd:fileIdentifier/gco:CharacterString", NAMESPACES);
            if (uuidElement != null) {
                uuid = uuidElement.getText();
            }
        }

        if (StringUtils.isNotEmpty(uuid)) {
            @SuppressWarnings("unchecked") final List<Element> links =
                    Lists.newArrayList((Iterable<? extends Element>)
                            Xml.selectNodes(xml, XPATH));

            for (Element element : links) {
                final String url = element.getText();
                Matcher regexMatcher = pattern.matcher(url);
                element.setText(
                        regexMatcher.replaceAll(
                                "$1/$2/api/records/" + uuid + "/attachments/$4"));
                changed = true;
            }
        } else {
            throw new UnsupportedOperationException("Metadata is not supported. UUID is not defined.");
        }
        return changed;
    }
}
