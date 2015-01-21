//=============================================================================
//===    Copyright (C) 2010 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.facet;

import org.fao.geonet.kernel.search.classifier.Classifier;
import org.fao.geonet.kernel.search.classifier.Value;

public class Dimension {

    private static final String TEMPLATE = "  * %s: {indexKey=%s, label=%s, classifier=%s}%n";

    public static final String FACET_FIELD_SUFFIX = "_facet";

    private String name;

    private String indexKey;

    private String label;

    private Classifier classifier;

    public Dimension(String name, String indexKey, String label) {
        this.name = name;
        this.indexKey = indexKey;
        this.label = label;
        this.classifier = new Value();
    }

    public String getName() {
        return name;
    }

    public String getIndexKey() {
        return indexKey;
    }

    public String getLabel() {
        return label;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public String toString() {
        return String.format(TEMPLATE, name, indexKey, label, classifier.getClass().getName());
    }

    public String getFacetFieldName() {
        return name + FACET_FIELD_SUFFIX;
    }

}
