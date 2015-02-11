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

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.kernel.search.classifier.Classifier;
import org.fao.geonet.kernel.search.classifier.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Set;

public class Dimension {
    @Autowired
    private ApplicationContext context;

    private static final String TEMPLATE = "  * %s: {indexKey=%s, label=%s, classifier=%s, localized=%b}%n";

    public static final String FACET_FIELD_SUFFIX = "_facet";

    private String name;

    private String indexKey;

    private String label;

    private boolean localized;

    private Classifier classifier;

    public Dimension(String name, String indexKey, String label) {
        this.name = name;
        this.indexKey = indexKey;
        this.label = label;
        localized = false;
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
        return String.format(TEMPLATE, name, indexKey, label, classifier.getClass().getName(), localized);
    }

    public String getFacetFieldName(String langCode) {
        return getName(langCode) + FACET_FIELD_SUFFIX;
    }

    public boolean isLocalized() {
        return this.localized;
    }

    public void setLocalized(Boolean localized) {
        this.localized = localized;
    }

    public String getName(String langCode) {
        if (this.localized && getLocales().contains(langCode)) {
            return this.name + "_" + langCode.toLowerCase();
        } else {
            return this.name;
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getLocales() {
        return this.context.getBean("languages", Set.class);
    }

    @VisibleForTesting
    void setApplicationContext(GenericApplicationContext applicationContext) {
        this.context = applicationContext;
    }
}
