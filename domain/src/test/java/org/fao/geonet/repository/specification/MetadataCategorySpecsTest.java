package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataCategoryRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetadataCategorySpecsTest extends AbstractSpringDataTest {


    @Autowired
    private MetadataCategoryRepository categoryRepository;

    @Test
    public void testHasCategoryNameIn() throws Exception {
        final MetadataCategory category = categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory category2 = categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory category3 = categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        final Specification<MetadataCategory> specification = MetadataCategorySpecs.hasCategoryNameIn(Arrays.asList
                (category.getName(), category3.getName()));

        final List<String> all = Lists.transform(categoryRepository.findAll(specification), new Function<MetadataCategory, String>() {
            @Nullable
            @Override
            public String apply(@Nullable MetadataCategory input) {
                return input.getName();
            }
        });

        assertEquals(2, all.size());
        assertTrue(all.contains(category.getName()));
        assertFalse(all.contains(category2.getName()));
        assertTrue(all.contains(category3.getName()));
    }
}