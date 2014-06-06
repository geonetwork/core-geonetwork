package org.fao.geonet.services.category;

import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fao.geonet.services.category.CategoryUpdateResponse.Operation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for Category Update service
 * User: pmauduit
 * Date: 2014-06-03
 */

public class UpdateTest extends AbstractServiceIntegrationTest {


    @Autowired
    private MetadataCategoryRepository categoryRepository;
    @Autowired
    private Update updateCategoryController;

    @Test
    public void updateCategoryTest() throws Exception {

        final String categoryName = "updatecategorytest_renamed";
        CategoryUpdateResponse ret = updateCategoryController.exec(1, categoryName);
    	assertEquals(1, ret.getOperations().size());
    	assertTrue(ret.getOperations().contains(Operation.updated));
    	// Checks that the category has correctly been saved using JPA
        MetadataCategory category = categoryRepository.findOne(1);
        assertTrue(category.getName().equals(categoryName));
    }

	@Test
    public void createCategoryTest() throws Exception {

        final String name = "createdcategory_updatetest";
    	CategoryUpdateResponse ret = updateCategoryController.exec(null, name);
    	assertEquals(1, ret.getOperations().size());
    	
    	assertTrue(ret.getOperations().contains(Operation.added));
    	// Checks that the category has correctly been saved using JPA
        MetadataCategory category = categoryRepository.findOneByName(name);
        assertTrue(category != null);
    }

	@Test
    public void badParameterCategoryTest() throws Exception {
    	try {
    		updateCategoryController.exec(42, null);
    	} catch (Throwable e) {
    		// Should complain of a missing parameter "name"
    		assertTrue (e.getClass().toString(), e instanceof MissingParameterEx);
    	}
    	
    }

}
