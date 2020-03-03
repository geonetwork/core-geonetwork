/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.domain;

import com.vividsolutions.jts.util.Assert;

import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Evaluate the value of a {@link org.fao.geonet.domain.SchematronCriteria} as an xpath against the
 * document to see if the xpath either: <ul> <li>Returns true</li> <li>Returns a non-empty
 * String</li> <li>Returns an Element</li> </ul>
 *
 *
 * Created by Jesse on 2/6/14.
 */
public class XPathCriteriaEvaluator implements SchematronCriteriaEvaluator {
    static final XPathCriteriaEvaluator INSTANCE = new XPathCriteriaEvaluator();
    private static final String OR = "__OR__";
    private static final String AND = "__AND__";

    /**
     * Construct {@link org.fao.geonet.domain.SchematronCriteria} with a value and {@link
     * org.fao.geonet.domain.SchematronCriteriaType#XPATH}.
     * <p/>
     * All of the text values needs to result in successes for the criteria to pass.
     *
     * @param textToMatch one or more text objects.
     * @return a {@link org.fao.geonet.domain.SchematronCriteria} object
     */
    public static SchematronCriteria createAndCriteria(Text... textToMatch) {
        return createAndCriteria(createXpathFrom(textToMatch));
    }

    /**
     * Construct {@link org.fao.geonet.domain.SchematronCriteria} with a value and {@link
     * org.fao.geonet.domain.SchematronCriteriaType#XPATH}.
     * <p/>
     * Only one of the text values needs to result in a success for the criteria to pass.
     *
     * @param textToMatch one or more text objects.
     * @return a {@link org.fao.geonet.domain.SchematronCriteria} object
     */
    public static SchematronCriteria createOrCriteria(Text... textToMatch) {
        return createOrCriteria(createXpathFrom(textToMatch));
    }

    /**
     * Construct {@link org.fao.geonet.domain.SchematronCriteria} with a value and {@link
     * org.fao.geonet.domain.SchematronCriteriaType#XPATH}.
     * <p/>
     * When evaluating each XPATH will be tried until one passes.  If one passes then the evaluator
     * will return true.
     *
     * @param xpaths the xpaths to OR together
     * @return a {@link org.fao.geonet.domain.SchematronCriteria}
     */
    public static SchematronCriteria createOrCriteria(String... xpaths) {
        Assert.isTrue(xpaths != null && xpaths.length > 0, "There needs to be at least one xpath for an OR expression");
        StringBuilder builder = new StringBuilder();
        for (String xpath : xpaths) {
            if (builder.length() > 0) {
                builder.append(OR);
            }
            builder.append(xpath);
        }

        return createSchematronCriteria(builder.toString());
    }

    /**
     * Construct {@link org.fao.geonet.domain.SchematronCriteria} with a value and {@link
     * org.fao.geonet.domain.SchematronCriteriaType#XPATH}.
     * <p/>
     * When evaluating each XPATH will be tried until one fails.  If one fails then the evaluator
     * will return false.
     *
     * @param xpaths the xpaths to OR together
     * @return a {@link org.fao.geonet.domain.SchematronCriteria}
     */
    public static SchematronCriteria createAndCriteria(String... xpaths) {
        Assert.isTrue(xpaths != null && xpaths.length > 0, "There needs to be at least one xpath for an AND expression");
        StringBuilder builder = new StringBuilder();
        for (String xpath : xpaths) {
            if (builder.length() > 0) {
                builder.append(AND);
            }
            builder.append(xpath);
        }

        return createSchematronCriteria(builder.toString());
    }

    private static SchematronCriteria createSchematronCriteria(String value) {
        final SchematronCriteria schematronCriteria = new SchematronCriteria();
        schematronCriteria.setType(SchematronCriteriaType.XPATH);
        schematronCriteria.setValue(value);

        return schematronCriteria;
    }

    private static String[] createXpathFrom(Text[] textToMatch) {
        String[] xpaths = new String[textToMatch.length];
        for (int i = 0; i < textToMatch.length; i++) {
            Text text = textToMatch[i];
            xpaths[i] = Xml.getXPathExpr(text);
        }
        return xpaths;
    }

    @Override
    public boolean accepts(ApplicationContext applicationContext, String value, int metadataId, Element metadata, List<Namespace> metadataNamespaces) {
        String[] ors = value.split(OR);
        boolean orAccepts = false;
        for (String or : ors) {
            String[] ands = or.split(AND);

            boolean andAccepts = true;
            for (String and : ands) {
                andAccepts = doAccept(and, metadata, metadataNamespaces);
                if (!andAccepts) {
                    break;
                }
            }

            orAccepts = andAccepts;

            if (orAccepts) {
                break;
            }
        }

        return orAccepts;

    }

    @Override
    public boolean accepts(ApplicationContext applicationContext, String value, Element metadata, List<Namespace> metadataNamespaces, Integer groupOwnerId) {
        return  accepts(applicationContext, value, -1, metadata, metadataNamespaces);

    }

    private boolean doAccept(String rawValue, Element metadata, List<Namespace> metadataNamespaces) {
        try {
            String value = rawValue;

            if (value.startsWith("/") && !value.startsWith("//")) {
                value = value.substring(1);
            }
            if (value.startsWith(metadata.getQualifiedName() + "/")) {
                value = value.substring(metadata.getQualifiedName().length() + 1);
            }
            if (value.startsWith("//")) {
                value = "*" + value;
            }
            List<?> nodes = Xml.selectNodes(metadata, value, metadataNamespaces);

            boolean accept = evaluateResult(nodes);


            if (!accept && value.startsWith("*//" + metadata.getQualifiedName())) {
                nodes = Xml.selectNodes(metadata, value.substring(metadata.getQualifiedName().length() + 3), metadataNamespaces);
                accept = evaluateResult(nodes);
            }

            if (!accept && rawValue.startsWith("//")) {
                nodes = Xml.selectNodes(metadata, rawValue.substring(2), metadataNamespaces);
                accept = evaluateResult(nodes);
            }

            return accept;
        } catch (Throwable e) {
            warn(rawValue, e);
            return false;
        }
    }

    private boolean evaluateResult(List<?> nodes) {
        boolean accept = false;
        if (nodes.isEmpty()) {
            accept = false;
        } else if (nodes.size() == 1 && nodes.get(0) instanceof Boolean) {
            accept = (Boolean) nodes.get(0);
        } else {
            for (Object node : nodes) {
                if (node instanceof Boolean) {
                    accept = (Boolean) nodes.get(0);
                    if (accept) {
                        break;
                    }
                } else if (node instanceof Text) {
                    Text text = (Text) node;
                    if (!text.getTextTrim().isEmpty()) {
                        accept = true;
                        break;
                    }
                } else if (node instanceof Element || node instanceof Attribute) {
                    accept = true;
                    break;
                }
            }
        }
        return accept;
    }

    protected void warn(String value, Throwable e) {
        Log.warning(Constants.DOMAIN_LOG_MODULE,
            "Error occurred while evaluating XPATH during schematron evaluation: \n\t" + value, e);
    }
}
