package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;

import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;

public class SplitTest {
    public void testClassify() {
        Split splitClassifier = new Split("-");

        List<CategoryPath> result = splitClassifier.classify("ant-bat-car");

        assertCategoryListEquals(result, "ant>bar>car");
    }

}
