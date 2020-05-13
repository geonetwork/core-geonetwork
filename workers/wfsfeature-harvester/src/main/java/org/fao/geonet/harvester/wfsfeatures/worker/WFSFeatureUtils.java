/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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
package org.fao.geonet.harvester.wfsfeatures.worker;

import org.apache.commons.lang.StringUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by francois on 02/09/16.
 */
public class WFSFeatureUtils {
    private static Pattern pt = Pattern.compile("\\{\\{([^}]*)\\}\\}");

    private static Pattern titleColumnShouldMatchPattern =
        Pattern.compile(
            ".*(TITLE|LABEL|NAME|TITRE|NOM|LIBELLE).*",
            Pattern.CASE_INSENSITIVE);

    /**
     * Build a title for the feature. The title expression could be
     * an attribute name or could contain expression were attributes
     * will be substituted. eg. "{{TITLE_FR}} ({{ID}})"
     *
     * @param feature   A simple feature
     * @param fields    List of columns
     * @param titleExpression A title expression based on one or more attributes
     * @return
     */
    static String buildFeatureTitle(SimpleFeature feature,
                                    Map<String, String> fields,
                                    String titleExpression) {
        if (StringUtils.isNotEmpty(titleExpression)) {
            if (titleExpression.contains("{{")) {
                Matcher m = pt.matcher(titleExpression);
                while (m.find()) {
                    String attributeName = m.group(1);
                    String attributeValue = (String) feature.getAttribute(attributeName);
                    titleExpression = titleExpression.replaceAll(
                        "\\{\\{" + attributeName + "\\}\\}",
                        attributeValue);

                }
                return titleExpression;
            } else {
                String attributeValue = (String) feature.getAttribute(titleExpression);
                if (attributeValue != null) {
                    return attributeValue;
                } else {
                    return null;
                }
            }
        } else {
            for (String attributeName : fields.keySet()) {
                String attributeType = fields.get(attributeName);
                String attributeValue = (String) feature.getAttribute(attributeName);
                if (attributeValue != null && !attributeType.equals("geometry")) {
                    return attributeValue;
                }
            }
        }
        return null;
    }

    /**
     * From the list of attributes try to find the best one
     * for a title. If not found, return the first attribute
     * which is not a geometry. If none, return null.
     *
     * @param fields List of attributes
     * @return
     */
    static String guessFeatureTitleAttribute(Map<String, String> fields) {
        Set<String> keySet = fields.keySet();
        String defaultTitle = null;
        for (String attributeName : keySet) {
            if (!"geometry".equals(fields.get(attributeName))) {
                // Default title is the first column which is not a geom
                if (defaultTitle == null) {
                    defaultTitle = attributeName;
                }
                Matcher m = titleColumnShouldMatchPattern.matcher(attributeName);
                if (m.find()) {
                    return attributeName;
                }
            }
        }
        return defaultTitle;
    }

}
