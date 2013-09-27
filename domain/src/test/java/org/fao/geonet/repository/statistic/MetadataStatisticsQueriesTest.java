package org.fao.geonet.repository.statistic;

import com.google.common.base.Optional;
import junit.framework.Assert;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Test MetadataStatisticsQueries
 * User: Jesse
 * Date: 9/23/13
 * Time: 12:20 PM
 */
@Transactional
public class MetadataStatisticsQueriesTest extends AbstractSpringDataTest {

    @Autowired
    private UserRepository _userRepository;
    @Autowired
    private GroupRepository _groupRepository;
    @Autowired
    private MetadataCategoryRepository _categoryRepository;
    @Autowired
    private MetadataRepository _metadataRepository;
    @Autowired
    private MetadataStatusRepository _metadataStatusRepository;
    @Autowired
    private StatusValueRepository _statusValueRepository;
    @Autowired
    private MetadataValidationRepository _metadataValidationRepository;
    @Autowired
    private SourceRepository _sourceRepository;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testGetMetadataCategoryToPopularityMap() throws Exception {
        MetadataCategory category1 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        MetadataCategory category2 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        MetadataCategory category3 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        Metadata metadata1c1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1c1.getCategories().add(_categoryRepository.findOne(category1.getId()));
        metadata1c1.getDataInfo().setPopularity(1);
        _metadataRepository.save(metadata1c1);

        Metadata metadata2c1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2c1.getCategories().add(category1);
        metadata2c1.getDataInfo().setPopularity(1);
        _metadataRepository.save(metadata2c1);

        Metadata metadata3c2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3c2.getCategories().add(category2);
        metadata3c2.getDataInfo().setPopularity(1);
        _metadataRepository.save(metadata3c2);

        final Map<MetadataCategory, Integer> popularityMap = _metadataRepository.getMetadataStatistics()
                .getMetadataCategoryToPopularityMap();

        assertEquals(2, popularityMap.size());
        assertEquals(2, popularityMap.get(category1).intValue());
        assertEquals(1, popularityMap.get(category2).intValue());
        assertNull(popularityMap.get(category3));
    }

    @Test
    public void testGetMetadataCategoryToMetadataCountMap() throws Exception {
        MetadataCategory category1 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        MetadataCategory category2 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        MetadataCategory category3 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        Metadata metadata1c1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1c1.getCategories().add(category1);
        _metadataRepository.save(metadata1c1);

        Metadata metadata2c1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2c1.getCategories().add(category1);
        _metadataRepository.save(metadata2c1);

        Metadata metadata3c2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3c2.getCategories().add(category2);
        _metadataRepository.save(metadata3c2);

        final Map<MetadataCategory, Integer> popularityMap = _metadataRepository.getMetadataStatistics()
                .getMetadataCategoryToMetadataCountMap();

        assertEquals(2, popularityMap.size());
        assertEquals(2, popularityMap.get(category1).intValue());
        assertEquals(1, popularityMap.get(category2).intValue());
        assertNull(popularityMap.get(category3));
    }

    @Test
    public void testGetGroupOwnerToMetadataCountMap() throws Exception {
        Group group1 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group3 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setGroupOwner(group1.getId());
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setGroupOwner(group1.getId());
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setGroupOwner(group2.getId());
        _metadataRepository.save(metadata3g2);

        final Map<Group, Integer> groupOwnerToMetadataCountMap = _metadataRepository.getMetadataStatistics()
                .getGroupOwnerToMetadataCountMap();

        assertEquals(2, groupOwnerToMetadataCountMap.size());
        assertEquals(2, groupOwnerToMetadataCountMap.get(group1).intValue());
        assertEquals(1, groupOwnerToMetadataCountMap.get(group2).intValue());
        assertNull(groupOwnerToMetadataCountMap.get(group3));
    }

    @Test
    public void testGetOwnerToMetadataCountMap() throws Exception {
        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = _userRepository.save(UserRepositoryTest.newUser(_inc));

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setOwner(user1.getId());
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setOwner(user1.getId());
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setOwner(user2.getId());
        _metadataRepository.save(metadata3g2);

        final Map<User, Integer> ownerToMetadataCountMap = _metadataRepository.getMetadataStatistics().getOwnerToMetadataCountMap();

        assertEquals(2, ownerToMetadataCountMap.size());
        assertEquals(2, ownerToMetadataCountMap.get(user1).intValue());
        assertEquals(1, ownerToMetadataCountMap.get(user2).intValue());
        assertNull(ownerToMetadataCountMap.get(user3));
    }

    @Test
    public void testGetSourceToMetadataCountMap() throws Exception {

        Source source1 = _sourceRepository.save(SourceRepositoryTest.newSource(_inc));
        Source source2 = _sourceRepository.save(SourceRepositoryTest.newSource(_inc));
        Source source3 = _sourceRepository.save(SourceRepositoryTest.newSource(_inc));

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setSourceId(source1.getUuid());
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setSourceId(source1.getUuid());
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setSourceId(source2.getUuid());
        _metadataRepository.save(metadata3g2);

        final Map<Source, Integer> sourceToMetadataCountMap = _metadataRepository.getMetadataStatistics()
                .getSourceToMetadataCountMap();

        assertEquals(2, sourceToMetadataCountMap.size());
        assertEquals(2, sourceToMetadataCountMap.get(source1).intValue());
        assertEquals(1, sourceToMetadataCountMap.get(source2).intValue());
        assertNull(sourceToMetadataCountMap.get(source3));
    }

    @Test
    public void testGetSchemaToMetadataCountMap() throws Exception {

        String schema1 = "schema1";
        String schema2 = "schema2";
        String schema3 = "schema3";

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getDataInfo().setSchemaId(schema1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getDataInfo().setSchemaId(schema1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getDataInfo().setSchemaId(schema2);
        _metadataRepository.save(metadata3g2);

        final Map<String, Integer> sourceToMetadataCountMap = _metadataRepository.getMetadataStatistics()
                .getSchemaToMetadataCountMap();

        assertEquals(2, sourceToMetadataCountMap.size());
        assertEquals(2, sourceToMetadataCountMap.get(schema1).intValue());
        assertEquals(1, sourceToMetadataCountMap.get(schema2).intValue());
        assertNull(sourceToMetadataCountMap.get(schema3));
    }

    @Test
    public void testGetMetadataTypeToMetadataCountMap() throws Exception {
        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getDataInfo().setType(MetadataType.METADATA);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getDataInfo().setType(MetadataType.METADATA);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        _metadataRepository.save(metadata3g2);

        final Map<MetadataType, Integer> sourceToMetadataCountMap = _metadataRepository.getMetadataStatistics()
                .getMetadataTypeToMetadataCountMap();

        assertEquals(3, sourceToMetadataCountMap.size());
        assertEquals(2, sourceToMetadataCountMap.get(MetadataType.METADATA).intValue());
        assertEquals(1, sourceToMetadataCountMap.get(MetadataType.SUB_TEMPLATE).intValue());
        assertEquals(0, sourceToMetadataCountMap.get(MetadataType.TEMPLATE).intValue());
    }

    @Test
    public void testGetIsHarvestedToMetadataCountMap() throws Exception {
        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getHarvestInfo().setHarvested(false);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getHarvestInfo().setHarvested(false);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getHarvestInfo().setHarvested(true);
        _metadataRepository.save(metadata3g2);

        final Map<Boolean, Integer> sourceToMetadataCountMap = _metadataRepository.getMetadataStatistics()
                .getIsHarvestedToMetadataCountMap();

        assertEquals(2, sourceToMetadataCountMap.size());
        assertEquals(2, sourceToMetadataCountMap.get(false).intValue());
        assertEquals(1, sourceToMetadataCountMap.get(true).intValue());
    }

    @Test
    public void testGetStatusValueToMetadataCountMap() throws Exception {
        Metadata md1 = _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md2 = _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md3 = _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));

        MetadataStatus metadataStatus1 = MetadataStatusRepositoryTest.newMetadataStatus(_inc, _statusValueRepository);
        metadataStatus1.getId().setMetadataId(md1.getId());
        metadataStatus1 = _metadataStatusRepository.save(metadataStatus1);

        final MetadataStatus metadataStatus2 = MetadataStatusRepositoryTest.newMetadataStatus(_inc, _statusValueRepository);
        metadataStatus2.setStatusValue(metadataStatus1.getStatusValue());
        metadataStatus2.getId().setMetadataId(md1.getId());
        _metadataStatusRepository.save(metadataStatus2);

        final MetadataStatus metadataStatus3 = MetadataStatusRepositoryTest.newMetadataStatus(_inc, _statusValueRepository);
        metadataStatus3.getId().setMetadataId(md2.getId());
        _metadataStatusRepository.save(metadataStatus3);


        final Map<StatusValue, Integer> sourceToMetadataCountMap = _metadataRepository.getMetadataStatistics()
                .getStatusValueToMetadataCountMap();

        assertEquals(2, sourceToMetadataCountMap.size());
        assertEquals(2, sourceToMetadataCountMap.get(metadataStatus1.getStatusValue()).intValue());
        assertEquals(1, sourceToMetadataCountMap.get(metadataStatus3.getStatusValue()).intValue());
    }

    @Test
    public void testGetMetadataValidationStatusToMetadataCountMap() throws Exception {

        _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
        _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
        _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
        _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));

        MetadataValidation validation = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation.setValid(true);
        _metadataValidationRepository.save(validation);

        MetadataValidation validation2 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation2.setValid(true);
        _metadataValidationRepository.save(validation2);

        MetadataValidation validation3 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation3.setValid(false);
        _metadataValidationRepository.save(validation3);

        final Map<MetadataValidationStatus, Integer> statusToMetadataCountMap = _metadataRepository
                .getMetadataStatistics().getMetadataValidationStatusToMetadataCountMap();

        assertEquals(3, statusToMetadataCountMap.size());
        Assert.assertEquals(2, statusToMetadataCountMap.get(MetadataValidationStatus.VALID).intValue());
        Assert.assertEquals(1, statusToMetadataCountMap.get(MetadataValidationStatus.INVALID).intValue());
        Assert.assertEquals(4, statusToMetadataCountMap.get(MetadataValidationStatus.NEVER_CALCULATED).intValue());
    }

    @Test
    public void testGetMetadataValidationTypeAndStatusToMetadataCountMap() throws Exception {
        MetadataValidation validation = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation.setValid(true);
        _metadataValidationRepository.save(validation);

        MetadataValidation validation2 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation2.getId().setValidationType(validation.getId().getValidationType());
        validation2.setValid(false);
        _metadataValidationRepository.save(validation2);

        MetadataValidation validation3 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation3.getId().setValidationType(validation.getId().getValidationType());
        validation3.setValid(false);
        _metadataValidationRepository.save(validation3);

        MetadataValidation validation4 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation4.setValid(false);
        _metadataValidationRepository.save(validation4);

        MetadataValidation validation5 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation5.setValid(false);
        _metadataValidationRepository.save(validation5);

        MetadataValidation validation6 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        validation6.setValid(false);
        _metadataValidationRepository.save(validation6);

        Map<Pair<String, MetadataValidationStatus>, Integer> map =
                _metadataRepository.getMetadataStatistics().getMetadataValidationTypeAndStatusToMetadataCountMap();

        assertEquals(6, map.size());
        assertEquals(1, map.get(Pair.read(validation.getId().getValidationType(), MetadataValidationStatus.VALID)).intValue());
        assertEquals(2, map.get(Pair.read(validation.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertEquals(1, map.get(Pair.read(validation4.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertEquals(1, map.get(Pair.read(validation5.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertEquals(1, map.get(Pair.read(validation6.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertNull(map.get(Pair.read(validation4.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertNull(map.get(Pair.read(validation5.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertNull(map.get(Pair.read(validation6.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertEquals(0, map.get(Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED)).intValue());

        _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
        map = _metadataRepository.getMetadataStatistics().getMetadataValidationTypeAndStatusToMetadataCountMap();
        assertEquals(1, map.get(Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED)).intValue());

    }

    @Test
    public void testSumPopularity() throws Exception {
        final Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata.getDataInfo().setPopularity(1);
        _metadataRepository.save(metadata);
        Metadata metadata1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1.getDataInfo().setPopularity(2);
        metadata1 = _metadataRepository.save(metadata1);
        final Metadata metadata2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2.getDataInfo().setPopularity(3);
        _metadataRepository.save(metadata2);
        assertEquals(6, _metadataRepository.getMetadataStatistics().sumOfPopularity(Optional.<Specification<Metadata>>absent()));
        assertEquals(2, _metadataRepository.getMetadataStatistics().sumOfPopularity(Optional.of(MetadataSpecs.hasMetadataId(metadata1.getId()))));

    }}
