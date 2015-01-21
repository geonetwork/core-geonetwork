package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.junit.Test;

public class CategoryHelperTest {

    @Test
    public void testAddSubCategory() {
        CategoryPath result = CategoryHelper.addSubCategory(new CategoryPath("a/b/c", '/'), "d");
        assertEquals("a/b/c/d", result.toString());
    }

    @Test
    public void testAddParentCategory() {
        CategoryPath result = CategoryHelper.addParentCategory("a", new CategoryPath("b/c/d", '/'));
        assertEquals("a/b/c/d", result.toString());
    }
}
