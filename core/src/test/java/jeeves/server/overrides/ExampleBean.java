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

package jeeves.server.overrides;

import java.util.ArrayList;
import java.util.List;

public class ExampleBean {
    private String basicProp;
    private String basicProp2;
    private List<String> collectionProp = new ArrayList<String>();
    private List<String> collectionProp2 = new ArrayList<String>();
    private ExampleBean simpleRefOtherNameForTesting;
    private List<ExampleBean> collectionRef = new ArrayList<ExampleBean>();

    public ExampleBean() {
        collectionProp.add("initial");
        collectionProp2.add("initial");
    }

    public String getBasicProp() {
        return basicProp;
    }

    public void setBasicProp(String basicProp) {
        this.basicProp = basicProp;
    }

    public String getBasicProp2() {
        return basicProp2;
    }

    public void setBasicProp2(String basicProp2) {
        this.basicProp2 = basicProp2;
    }

    public List<String> getCollectionProp() {
        return collectionProp;
    }

    public void setCollectionProp(List<String> collectionProp) {
        this.collectionProp = collectionProp;
    }

    public ExampleBean getSimpleRef() {
        return simpleRefOtherNameForTesting;
    }

    public void setSimpleRef(ExampleBean simpleRef) {
        this.simpleRefOtherNameForTesting = simpleRef;
    }

    public List<ExampleBean> getCollectionRef() {
        return collectionRef;
    }

    public void setCollectionRef(List<ExampleBean> collectionRef) {
        this.collectionRef = collectionRef;
    }


}
