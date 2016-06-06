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

package org.fao.geonet.kernel.rdf;

import java.util.Collection;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * An interpreter that assumes a query with id, label, note and bound selector to be added to the
 * query.  The {@link QueryBuilder#keywordQueryBuilder(IsoLanguagesMapper, String...)} creates a
 * mapper with this interpreter
 *
 * @author jeichar
 */
class KeywordResultInterpreter extends ResultInterpreter<KeywordBean> {

    private Collection<String> languages;

    public KeywordResultInterpreter(Collection<String> languages) {
        this.languages = languages;
    }

    @Override
    public KeywordBean createFromRow(Thesaurus thesaurus, QueryResultsTable resultsTable, int row) {

        BiMap<String, Integer> columnNameMap = createColumnNameMap(resultsTable);

        Pair<String, String> lowerCorner = parseCorner(resultsTable, columnNameMap, row, Selectors.LOWER_CORNER.id);
        String coordWest = lowerCorner.one();
        String coordSouth = lowerCorner.two();

        Pair<String, String> upperCorner = parseCorner(resultsTable, columnNameMap, row, Selectors.UPPER_CORNER.id);
        String coordEast = upperCorner.one();
        String coordNorth = upperCorner.two();

        KeywordBean keywordBean = new KeywordBean(thesaurus.getIsoLanguageMapper())
            .setThesaurusInfo(thesaurus)
            .setId(row)
            .setUriCode(columnValue(resultsTable, columnNameMap, row, Selectors.ID.id))
            .setCoordEast(coordEast)
            .setCoordNorth(coordNorth)
            .setCoordSouth(coordSouth)
            .setCoordWest(coordWest)
            .setDownloadUrl(thesaurus.getDownloadUrl())
            .setKeywordUrl(thesaurus.getKeywordUrl());

        for (String lang : this.languages) {
            String value = columnValue(resultsTable, columnNameMap, row, lang + Selectors.LABEL_POSTFIX);
            keywordBean.setValue(value, lang);
            String definition = columnValue(resultsTable, columnNameMap, row, lang + Selectors.NOTE_POSTFIX);
            keywordBean.setDefinition(definition, lang);
        }

        return keywordBean;
    }

    private Pair<String, String> parseCorner(QueryResultsTable resultsTable, BiMap<String, Integer> columnNameMap, int row, String columnName) {
        String corner = columnValue(resultsTable, columnNameMap, row, columnName);
        String[] parts = corner.split(" ");
        if (parts.length == 2) {
            return Pair.read(parts[0], parts[1]);
        } else {
            return Pair.read("", "");
        }
    }

    private String columnValue(QueryResultsTable resultsTable, BiMap<String, Integer> columnNameMap, int row, String columnName) {
        Integer columnIdx = columnNameMap.get(columnName);
        String prefLabel = "";
        if (columnIdx != null) {
            Value value = resultsTable.getValue(row, columnIdx);
            if (value != null) {
                prefLabel = value.toString();
            }
        }
        return prefLabel;
    }

    private BiMap<String, Integer> createColumnNameMap(QueryResultsTable resultsTable) {
        String[] columnNames = resultsTable.getColumnNames();
        BiMap<String, Integer> map = HashBiMap.create();
        for (int i = 0; i < columnNames.length; i++) {
            map.put(columnNames[i], i);
        }
        return map;
    }
}
