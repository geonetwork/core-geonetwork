package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;

import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.newGroup;
import static org.junit.Assert.*;

/**
 * Test the specifications for selecting {@link org.fao.geonet.domain.SchematronCriteria}.
 *
 * Created by Jesse on 2/12/14.
 */
public class SchematronCriteriaGroupSpecsTest extends AbstractSpringDataTest {


    @Autowired
    private SchematronCriteriaGroupRepository _criteriaGroupRepository;
    @Autowired
    private SchematronRepository _schematronRepository;

    @Test
    public void testHasSchematronId() throws Exception {
        SchematronCriteriaGroup group1 = newGroup(_inc, _schematronRepository);
        group1 = _criteriaGroupRepository.saveAndFlush(group1);

        SchematronCriteriaGroup group2 = newGroup(_inc, _schematronRepository);
        group2 = _criteriaGroupRepository.saveAndFlush(group2);

        SchematronCriteriaGroup group3 = newGroup(_inc, _schematronRepository);
        group3.setSchematron(group1.getSchematron());
        group3 = _criteriaGroupRepository.saveAndFlush(group3);

        final List<SchematronCriteriaGroup> found = _criteriaGroupRepository.findAll(SchematronCriteriaGroupSpecs.hasSchematronId(group1.getSchematron()
                .getId()));

        assertCorrectCriteriaFound(group1, group2, group3, found);
    }

    @Test
    public void testHasGroupName() throws Exception {
        SchematronCriteriaGroup group1 = newGroup(_inc, _schematronRepository);
        group1 = _criteriaGroupRepository.saveAndFlush(group1);

        SchematronCriteriaGroup group2 = newGroup(_inc, _schematronRepository);
        group2 = _criteriaGroupRepository.saveAndFlush(group2);

        SchematronCriteriaGroup group3 = newGroup(_inc, _schematronRepository);
        group3.getId().setName(group1.getId().getName());
        group3 = _criteriaGroupRepository.saveAndFlush(group3);

        final List<SchematronCriteriaGroup> found = _criteriaGroupRepository.findAll(SchematronCriteriaGroupSpecs.hasGroupName(group1.getId().getName()));

        assertCorrectCriteriaFound(group1, group2, group3, found);
    }

    private void assertCorrectCriteriaFound(SchematronCriteriaGroup group1, SchematronCriteriaGroup group2, SchematronCriteriaGroup group3, List<SchematronCriteriaGroup> found) {
        assertEquals(2, found.size());
        List<Object> ids = Lists.transform(found, new Function<SchematronCriteriaGroup, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable SchematronCriteriaGroup input) {
                assert input != null;
                return input.getId();
            }
        });

        assertTrue(ids.contains(group1.getId()));
        assertFalse(ids.contains(group2.getId()));
        assertTrue(ids.contains(group3.getId()));
    }
}
