/**
 *
 * SchematronCriteriaType.java
 *
 * Copyright (C) 2013
 *
 * This file is part of project geonetwork
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 *
 * @author delawen
 */
package org.fao.geonet.kernel.schema;

import jeeves.resources.dbms.Dbms;
import org.jdom.Element;
import org.jdom.Namespace;

import java.sql.SQLException;
import java.util.List;

/**
 * Used on {@link SchematronCriteria}
 *
 * @author delawen
 */
public enum SchematronCriteriaType {
    /**
     * A criteria where the value must match a group name.
     */
    GROUP(new SchematronCriteriaEvaluator() {
        @Override
        public boolean accepts(Dbms dbms, String value, int metadataId, Element metadata, List<Namespace> metadataNamespaces) {
            boolean tmpApply;
            try {
                tmpApply = dbms.select("SELECT * FROM metadata WHERE id = ? AND groupowner IN ( "+value+")", metadataId).getChildren().size() > 0;

                return tmpApply;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }),
    /**
     * An always true criteria.
     */
    ALWAYS_ACCEPT(new SchematronCriteriaEvaluator() {
        @Override
        public boolean accepts(Dbms dbms, String value, int metadataId, Element metadata, List<Namespace> metadataNamespaces) {
            return true;
        }
    }),
    /**
     * A criteria where the value must match a one of the ISO19139 keywords in the metadata.
     */
    XPATH(XPathCriteriaEvaluator.INSTANCE);

    private final SchematronCriteriaEvaluator evaluator;

    SchematronCriteriaType(SchematronCriteriaEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public boolean accepts(Dbms dbms, String value, int metadataId, Element metadata, List<Namespace> metadataNamespaces) {
        return evaluator.accepts(dbms, value, metadataId, metadata, metadataNamespaces);
    }
}
