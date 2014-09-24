package org.fao.geonet.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;

public class CategoryTestHelper {

    public static List<CategoryPath> createCategoryPathList(String... categoryPaths) {
        List<CategoryPath> categories = new ArrayList<CategoryPath>();

        for (String categoryPath: categoryPaths) {
            categories.add(new CategoryPath(categoryPath, '/'));
        }

        return categories;
    }

    public static void assertCategoryListEquals(List<CategoryPath> result, String... categories) {
        assertEquals(categories.length, result.size());

        for (int i=0; i < categories.length; i++) {
            assertEquals(categories[i], result.get(i).toString('>'));
        }
    }

}
