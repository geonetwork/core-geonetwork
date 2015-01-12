//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.LabelAndValue;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.Translator;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

public class ItemBuilder {

    private ItemConfig config;
    private Facets facets;
    private Translator translator;
    private Formatter formatter;

    public ItemBuilder(ItemConfig config, String langCode, Facets facets, Format format) {
        this.config = config;
        this.facets = facets;
        this.translator = config.getTranslator(langCode);
        this.formatter = format.getFormatter(config.getDimension());
    }

    public Element build() {
        FacetResult dimensionResult = getTopChildren();
        Element dimensionElement = buildDimensionElement(dimensionResult);

        if (dimensionResult != null) {
            addResultSubCategoriesToElement(dimensionResult, dimensionElement);
        }

        return dimensionElement;
    }

    private Element buildDimensionElement(FacetResult facetResults) {
        if (facetResults == null) {
            Log.debug(
                    Geonet.FACET_ENGINE,
                    "Null facet results for field " + config.getDimension().getIndexKey());
            return formatter.buildDimensionTag(0);
        }

        return formatter.buildDimensionTag(getCount(facetResults));
    }

    private int getCount(FacetResult facetResult) {
        if (facetResult == null) {
            return 0;
        } else {
            return facetResult.value.intValue();
        }
    }

    private void addResultSubCategoriesToElement(FacetResult facetResult, Element dimensionElement, String... path) {
        List<CategorySummary> subCategories = toSubCategories(facetResult.labelValues);
        List<CategorySummary> sortedSubCategories = sort(subCategories);

        for (CategorySummary subCategory: sortedSubCategories) {
            addSubCategoryToElement(subCategory, dimensionElement, path );
        }
    }

    private void addSubCategoryToElement(CategorySummary subCategory, Element parent, String... parentPath) {
        Element categoryTag = buildCategoryTag(subCategory);
        String[] path = addCategoryToPath(subCategory.value, parentPath);

        FacetResult subCategoryResult = getTopChildren(path);

        if (subCategoryResult != null) {
            addResultSubCategoriesToElement(subCategoryResult, categoryTag, path);
        }

        parent.addContent(categoryTag);
    }

    private List<CategorySummary> toSubCategories(LabelAndValue[] labelValues) {
        List<CategorySummary> result = new ArrayList<CategorySummary>();

        for (LabelAndValue labelAndValue: labelValues) {
            result.add(toCategoryResult(labelAndValue));
        }

        return result;
    }

    private CategorySummary toCategoryResult(LabelAndValue labelAndValue) {
        CategorySummary result = new CategorySummary();
        result.value = labelAndValue.label;
        result.label = translator.translate(labelAndValue.label);
        result.count = labelAndValue.value.intValue();
        return result;
    }

    private FacetResult getTopChildren(String... path) {
        try {
            return facets.getTopChildren(config.getMax(), config.getDimension().getName(), path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CategorySummary> sort(List<CategorySummary> categories) {
        if (Log.isDebugEnabled(Geonet.FACET_ENGINE)) {
            Log.debug(Geonet.FACET_ENGINE, config.getDimension().getName()
                    + ":\tSorting facet by " + config.getSortBy().toString()
                    + " (" + config.getSortOrder().toString() + ")");
        }

        // No need for a custom comparator Lucene facet request is
        // made by count descending order
        if (SortBy.COUNT != config.getSortBy()) {
            Collections.sort(categories, getComparator());
        }

        return categories;
    }

    private Comparator<CategorySummary> getComparator() {
        Comparator<CategorySummary> comparator;

        if (SortBy.LABEL == config.getSortBy()) {
            comparator = labelComparator();
        } else if (SortBy.NUMVALUE == config.getSortBy()) { 
            comparator = numericComparator();
        } else {
            comparator = valueComparator();
        }
        
        if (SortOrder.DESCENDING == config.getSortOrder()) {
            comparator = descendingComparator(comparator); 
        }

        return comparator;
    }

    private Element buildCategoryTag(CategorySummary result) {

        if (Log.isDebugEnabled(Geonet.FACET_ENGINE)) {
            Log.debug(Geonet.FACET_ENGINE, " - " + result.value
                    + " (" + result.count + ")");
        }

        return formatter.buildCategoryTag(result);
    }

    private String[] addCategoryToPath(String category, String... parentPath) {
        String[] path = Arrays.copyOf(parentPath, parentPath.length + 1);
        path[parentPath.length] = category;
        return path;
    }

    private Comparator<CategorySummary> valueComparator() {
        return new Comparator<CategorySummary>() {
            @Override
            public int compare(final CategorySummary e1, final CategorySummary e2) {
                return e1.value.compareTo(e2.value);
            }
        };
    }

    private Comparator<CategorySummary> numericComparator() {
        return new Comparator<CategorySummary>() {
            @Override
            public int compare(final CategorySummary e1, final CategorySummary e2) {
                try {
                    Double d1 = Double.valueOf(e1.value);
                    Double d2 = Double.valueOf(e2.value);

                    return d1.compareTo(d2);
                } catch (NumberFormatException e) {
                    // String comparison
                    Log.warning(
                            Geonet.FACET_ENGINE,
                            "Failed to compare numeric values ("
                                    + e1.value
                                    + " / "
                                    + e2.value
                                    + ") for facet. Check sortBy option in summary configuration.");
                    return e1.value.compareTo(
                            e2.value);
                }
            }
        };
    }

    private Comparator<CategorySummary> labelComparator() {
        return new Comparator<CategorySummary>() {
            @Override
            public int compare(final CategorySummary e1, final CategorySummary e2) {
                return e1.label.compareTo(e2.label);
            }
        };
    }

    private Comparator<CategorySummary> descendingComparator(final Comparator<CategorySummary> wrappedComparator) {
        return new Comparator<CategorySummary>() {
            @Override
            public int compare(final CategorySummary e1, final CategorySummary e2) {
                return wrappedComparator.compare(e2, e1);
            }
        };
    }

}
