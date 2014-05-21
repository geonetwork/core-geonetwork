package org.fao.geonet.services.metadata.schema;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.newGroup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jesse on 2/13/14.
 */
public abstract class AbstractSchematronServiceIntegrationTest extends AbstractServiceIntegrationTest {
    protected SchematronCriteriaGroup _group1_Name1_SchematronId1;
    protected SchematronCriteriaGroup _group2_Name2_SchematronId2;
    protected SchematronCriteriaGroup _group3_Name3_SchemtronId1;
    protected SchematronCriteriaGroup _group4_Name2_SchematronId4;
    @Autowired
    SchematronRepository _schematronRepository;
    @Autowired
    private SchematronCriteriaGroupRepository _schematronCriteriaGroupRepository;
    @Autowired
    SchematronCriteriaRepository _schematronCriteriaRepository;

    @Before
    public void addTestData() {
        _schematronCriteriaGroupRepository.deleteAll();
        _schematronRepository.deleteAll();
        assertEquals(0, _schematronCriteriaRepository.count());
        this._group1_Name1_SchematronId1 = _schematronCriteriaGroupRepository.save(newGroup(_inc, _schematronRepository));
        this._group2_Name2_SchematronId2 = _schematronCriteriaGroupRepository.save(newGroup(_inc, _schematronRepository));
        final SchematronCriteriaGroup entity = newGroup(_inc, _schematronRepository);
        entity.setSchematron(_group1_Name1_SchematronId1.getSchematron());
        this._group3_Name3_SchemtronId1 = _schematronCriteriaGroupRepository.save(entity);
        final SchematronCriteriaGroup entity2 = newGroup(_inc, _schematronRepository);
        entity2.getId().setName(_group2_Name2_SchematronId2.getId().getName());
        this._group4_Name2_SchematronId4 = _schematronCriteriaGroupRepository.save(entity2);
    }

    protected void assertGroupNames(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> groupNames = selectGroupNames(result);
        assertEquals(groups.length, groupNames.size());
        for (SchematronCriteriaGroup group : groups) {
            assertTrue(groupNames.contains(group.getId().getName()));
        }
    }

    protected void assertSchematronIds(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> schematronIds = selectSchematronIds(result);
        assertEquals(groups.length, schematronIds.size());
        for (SchematronCriteriaGroup group : groups) {
            assertTrue(schematronIds.contains(""+group.getId().getSchematronId()));
        }
    }

    private List<?> selectSchematronIds(Element result) throws JDOMException {
        return Lists.transform(Xml.selectNodes(result, "record/id/schematronid/text()"), new Function<Object,
                Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Object input) {
                return ((Text) input).getText();
            }
        });
    }

    protected void assertNotGroupNames(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> groupNames = selectGroupNames(result);
        for (SchematronCriteriaGroup group : groups) {
            assertFalse(groupNames.contains(group.getId().getName()));
        }
    }

    private List<?> selectGroupNames(Element result) throws JDOMException {
        return Lists.transform(Xml.selectNodes(result, "record/id/name/text()"), new Function<Object, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Object input) {
                return ((Text) input).getText();
            }
        });
    }

    protected void assertNotSchematronIds(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> schematronIds = selectSchematronIds(result);
        for (SchematronCriteriaGroup group : groups) {
            assertFalse(schematronIds.contains(""+group.getId().getSchematronId()));
        }
    }

    protected void assertSuccessfulAdd(Element result) {
        assertEquals("success", result.getChildText("status"));
    }

    public void init(AbstractSchematronService service, SchematronServiceAction action) throws Exception {
        ServiceConfig params = new ServiceConfig(Arrays.asList(createServiceConfigParam(Params.ACTION, action.toString())));
        service.init(getWebappDir(AbstractSchematronServiceIntegrationTest.class), params);
    }
}
