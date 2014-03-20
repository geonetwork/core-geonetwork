package org.fao.geonet.repository.specification;

import static org.junit.Assert.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.*;

/**
 * Test the specifications for selecting {@link org.fao.geonet.domain.SchematronCriteria}.
 *
 * Created by Jesse on 2/12/14.
 */
public class SchematronCriteriaSpecsTest extends AbstractSpringDataTest {


    @Autowired
    private SchematronCriteriaGroupRepository _criteriaGroupRepository;
    @Autowired
    private SchematronRepository _schematronRepository;
    @Autowired
    private SchematronCriteriaRepository _criteriaRepository;

    @Test
    public void testHasSchematronId() throws Exception {
        SchematronCriteriaGroup group1 = newGroup(_inc, _schematronRepository);
        group1 = _criteriaGroupRepository.saveAndFlush(group1);

        SchematronCriteriaGroup group2 = newGroup(_inc, _schematronRepository);
        group2 = _criteriaGroupRepository.saveAndFlush(group2);

        SchematronCriteriaGroup group3 = newGroup(_inc, _schematronRepository);
        group3.setSchematron(group1.getSchematron());
        group3 = _criteriaGroupRepository.saveAndFlush(group3);

        final List<SchematronCriteria> found = _criteriaRepository.findAll(SchematronCriteriaSpecs.hasSchematronId(group1.getSchematron()
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

        final List<SchematronCriteria> found = _criteriaRepository.findAll(SchematronCriteriaSpecs.hasGroupName(group1.getId().getName()));

        assertCorrectCriteriaFound(group1, group2, group3, found);
    }

    private void assertCorrectCriteriaFound(SchematronCriteriaGroup group1, SchematronCriteriaGroup group2, SchematronCriteriaGroup group3, List<SchematronCriteria> found) {
        assertEquals(group1.getCriteria().size() + group3.getCriteria().size(), found.size());
        List<Object> ids = Lists.transform(found, new Function<SchematronCriteria, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable SchematronCriteria input) {
                return input.getId();
            }
        });

        for (SchematronCriteria schematronCriteria : group1.getCriteria()) {
            assertTrue(ids.contains(schematronCriteria.getId()));
        }
        for (SchematronCriteria schematronCriteria : group2.getCriteria()) {
            assertFalse(ids.contains(schematronCriteria.getId()));
        }

        for (SchematronCriteria schematronCriteria : group3.getCriteria()) {
            assertTrue(ids.contains(schematronCriteria.getId()));
        }
    }
}
