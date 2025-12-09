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

package org.fao.geonet;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Replace old metadata link to uploaded file by new API.
 * Only applies to ISO19139 graphic overview and online resources.
 *
 * http://localhost:8080/geonetwork/srv/en/resources.get?uuid=da165110-88fd-11da-a88f-000d939bc5d8&fname=basins.zip&access=private
 *
 * is replaced by
 *
 * http://localhost:8080/geonetwork/srv/api/metadata/da165110-88fd-11da-a88f-000d939bc5d8/resources/basins.zip
 *
 *
 * Created by francois on 12/01/16.
 */
public class MetadataResourceDatabaseMigration extends DatabaseMigrationTask {

    private static final ArrayList<Namespace> NAMESPACES =
        Lists.newArrayList(
            ISO19139Namespaces.GMD,
            ISO19139Namespaces.GCO);

    private static final String XPATH_RESOURCES =
            "*//*[contains(text(), '/resources.get?')]";
    private static final String XPATH_THUMBNAIL_WITH_NO_URL =
            "*//gmd:MD_BrowseGraphic" +
                    "[gmd:fileDescription/gco:CharacterString = 'thumbnail' or " +
                    "gmd:fileDescription/gco:CharacterString = 'large_thumbnail']/gmd:fileName/" +
                    "gco:CharacterString[not(starts-with(normalize-space(text()), 'http'))]";
    private static final String XPATH_THUMBNAIL_WITH_URL =
            "*//gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString[starts-with(normalize-space(text()), 'http')]";

    private static final String XPATH_ATTACHMENTS_WITH_URL =
            "*//gmd:CI_OnlineResource/gmd:linkage/gmd:URL";
    private static final String URL_ATTACHED_RESOURCES = "api/records/%s/attachments/";

    private static final Pattern pattern = Pattern.compile(
            "(.*)\\/([a-zA-Z0-9_\\-]+)\\/([a-z]{2,3})\\/{1,2}resources.get\\?.*fname=([\\p{L}\\w\\s_=\\(\\)\\.\\-%:]+)(&.*|$)");

    public static boolean updateMetadataResourcesLink(@Nonnull Element xml,
                                                      @Nullable String uuid,
                                                      SettingManager settingManager) throws JDOMException {
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
                    Xml.selectNodes(xml, XPATH_RESOURCES));

            for (Element element : links) {
                final String url = element.getText();
                Matcher regexMatcher = pattern.matcher(url);
                element.setText(
                    regexMatcher.replaceAll(
                        settingManager.getNodeURL() +
                                    "api/records/" + uuid + "/attachments/$4"));
                changed = true;
            }

            // ATTACHMENTS
            // This fix the imports of metadata with attachments
            @SuppressWarnings("unchecked") final List<Element> linksAttachmentsUrl =
                    Lists.newArrayList((Iterable<? extends Element>)
                            Xml.selectNodes(xml, XPATH_ATTACHMENTS_WITH_URL, NAMESPACES));

            for (Element element : linksAttachmentsUrl) {
                final String url = element.getText();
                // Extra check if the URL contains the current UUID and rest API url pattern
                if(url.indexOf(String.format(URL_ATTACHED_RESOURCES, uuid)) > 0) {
                    element.setText(url.replace(url.substring(0, url.indexOf("api/records/")), settingManager.getNodeURL()));
                    changed = true;
                }
            }

            // THUMBNAILS
            // This fix the imports from older versions of GN
            // where the thumbnails contains just the filename
            @SuppressWarnings("unchecked") final List<Element> linksThumbnailsNoUrl =
                    Lists.newArrayList((Iterable<? extends Element>)
                            Xml.selectNodes(xml, XPATH_THUMBNAIL_WITH_NO_URL, NAMESPACES));

            for (Element element : linksThumbnailsNoUrl) {
                final String filename = element.getText();
                element.setText(
                    String.format(
                        "%sapi/records/" + uuid + "/attachments/%s",
                        settingManager.getNodeURL(), filename));
                changed = true;
            }

            // This fix the imports from current versions of GN
            // where the thumbnails contains the full URL of the resource
            @SuppressWarnings("unchecked") final List<Element> linksThumbnailsWithUrl =
                    Lists.newArrayList((Iterable<? extends Element>)
                            Xml.selectNodes(xml, XPATH_THUMBNAIL_WITH_URL, NAMESPACES));

            for (Element element : linksThumbnailsWithUrl) {
                final String url = element.getText();
                if(url.indexOf("api/records/") > 0) {
                    element.setText(url.replace(url.substring(0, url.indexOf("api/records/")), settingManager.getNodeURL()));
                    changed = true;
                }
            }
        } else {
            throw new UnsupportedOperationException("Metadata is not supported. UUID is not defined.");
        }
        return changed;
    }

    @Override
    public void update(Connection connection) throws SQLException {
        Log.debug(Geonet.DB, "MetadataResourceDatabaseMigration");

        final SettingManager settingManager = applicationContext.getBean(SettingManager.class);

        try (PreparedStatement update = connection.prepareStatement(
            "UPDATE metadata SET data=? WHERE id=?")
        ) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(String.format(
                     "SELECT data, id, uuid FROM metadata WHERE data LIKE '%%%s%%'",
                     settingManager.getServerURL()))
            ) {
                int numInBatch = 0;

                while (resultSet.next()) {
                    final Element xml = Xml.loadString(resultSet.getString(1), false);
                    final int id = resultSet.getInt(2);
                    final String uuid = resultSet.getString(3);
                    boolean changed = updateMetadataResourcesLink(xml, uuid, settingManager);
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
                Log.error(Geonet.GEONETWORK, "Error occurred while updating resource links:" + e.getMessage(), e);
                SQLException next = e.getNextException();
                while (next != null) {
                    Log.error(Geonet.GEONETWORK, "Next error: " + next.getMessage(), next);
                    next = e.getNextException();
                }

                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
}
