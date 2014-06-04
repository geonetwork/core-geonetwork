package org.fao.geonet.services.category;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;

/**
 * Test for Category Update service
 * User: pmauduit
 * Date: 2014-06-03
 */

public class UpdateTest extends AbstractServiceIntegrationTest {

	@Test
    public void updateCategoryTest() throws Exception {
    	Update updateCategoryController = new Update();

    	@SuppressWarnings("unchecked")
		Element params = createParams(Pair.write("id", "1"), Pair.write("name", "updatecategorytest_renamed"));
    	ServiceContext srvctx = createServiceContext();
    	Element ret = updateCategoryController.exec(params, srvctx);
    	assertNotNull(ret.getChild("operation"));
    	assertTrue(ret.getChildText("operation").equals("updated"));
    	// Checks that the category has correctly been saved using JPA
        final MetadataCategoryRepository categoryRepository = srvctx.getBean(MetadataCategoryRepository.class);
        MetadataCategory category = categoryRepository.findOne(1);
        assertTrue(category.getName().equals("updatecategorytest_renamed"));
    }

	@Test
    public void createCategoryTest() throws Exception {
    	Update updateCategoryController = new Update();

    	@SuppressWarnings("unchecked")
		Element params = createParams(Pair.write("name", "createdcategory_updatetest"));
    	ServiceContext srvctx = createServiceContext();
    	Element ret = updateCategoryController.exec(params, srvctx);
    	assertNotNull(ret.getChild("operation"));
    	
    	assertTrue(ret.getChildText("operation").equals("added"));
    	// Checks that the category has correctly been saved using JPA
        final MetadataCategoryRepository categoryRepository = srvctx.getBean(MetadataCategoryRepository.class);
        MetadataCategory category = categoryRepository.findOneByName("createdcategory_updatetest");
        assertTrue(category != null);
    }

	@Test
    public void badParameterCategoryTest() throws Exception {
    	Update updateCategoryController = new Update();

    	@SuppressWarnings("unchecked")
		Element params = createParams(Pair.write("not_expected_parameter", "not_expected_string"), Pair.write("id", "42"));
    	ServiceContext srvctx = createServiceContext();
    	try {
    		updateCategoryController.exec(params, srvctx);
    	} catch (Throwable e) {
    		// Should complain of a missing parameter "name"
    		assertTrue (e instanceof MissingParameterEx);
    	}
    	
    }

}
