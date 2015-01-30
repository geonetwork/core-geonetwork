package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;

import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.junit.Test;

public class ValueTest {

    @Test
    public void testClassify() {
        Value valueClassifier = new Value();

        List<CategoryPath> result = valueClassifier.classify("ant-bat-car");

        assertCategoryListEquals(result, "ant-bat-car");
    }

}
