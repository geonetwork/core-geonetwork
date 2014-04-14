package org.fao.geonet.repository.statistic;

import com.google.common.base.Optional;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.fao.geonet.repository.statistic.MetadataStatisticSpec.StandardSpecs.*;
import static org.junit.Assert.assertEquals;

/**
 * Test MetadataStatisticsQueries
 * User: Jesse
 * Date: 9/23/13
 * Time: 12:20 PM
 */
public class MetadataStatisticsQueriesTest extends AbstractSpringDataTest {

    private static final int POPULARITY = 2;
    public static final int RATING = 3;
    @Autowired
    UserRepository _userRepository;
    @Autowired
    GroupRepository _groupRepository;
    @Autowired
    MetadataCategoryRepository _categoryRepository;
    @Autowired
    MetadataRepository _metadataRepository;
    @Autowired
    MetadataStatusRepository _metadataStatusRepository;
    @Autowired
    StatusValueRepository _statusValueRepository;
    @Autowired
    MetadataValidationRepository _metadataValidationRepository;
    @Autowired
    SourceRepository _sourceRepository;
    @Autowired
    OperationAllowedRepository _operationAllowedRepository;

    @Test
    public void testGetMetadataCategoryToPopularityMap() throws Exception {
        MetadataCategory category1 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        MetadataCategory category2 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        MetadataCategory category3 = _categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        Metadata metadata1c1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1c1.getCategories().add(_categoryRepository.findOne(category1.getId()));
        setPopularityAndRating(metadata1c1);
        _metadataRepository.save(metadata1c1);

        Metadata metadata2c1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2c1.getCategories().add(category1);
        setPopularityAndRating(metadata2c1);
        _metadataRepository.save(metadata2c1);

        Metadata metadata3c2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3c2.getCategories().add(category2);
        setPopularityAndRating(metadata3c2);
        _metadataRepository.save(metadata3c2);

        Map<MetadataCategory, Integer> popularityMap = _metadataRepository.getMetadataStatistics()
                .getMetadataCategoryToStatMap(popularitySum());

        assertEquals(2, popularityMap.size());
        assertEquals(2 * POPULARITY, popularityMap.get(category1).intValue());
        assertEquals(POPULARITY, popularityMap.get(category2).intValue());
        assertNull(popularityMap.get(category3));

        popularityMap = _metadataRepository.getMetadataStatistics()
                .getMetadataCategoryToStatMap(metadataCount());

        assertEquals(2, popularityMap.size());
        assertEquals(2, popularityMap.get(category1).intValue());
        assertEquals(1, popularityMap.get(category2).intValue());
        assertNull(popularityMap.get(category3));

        popularityMap = _metadataRepository.getMetadataStatistics()
                .getMetadataCategoryToStatMap(ratingSum());

        assertEquals(2, popularityMap.size());
        assertEquals(2 * RATING, popularityMap.get(category1).intValue());
        assertEquals(RATING, popularityMap.get(category2).intValue());
        assertNull(popularityMap.get(category3));
    }


    @Test
    public void testGetGroupOwnerToStatMap() throws Exception {
        Group group1 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group3 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setGroupOwner(group1.getId());
        setPopularityAndRating(metadata1g1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setGroupOwner(group1.getId());
        setPopularityAndRating(metadata2g1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setGroupOwner(group2.getId());
        setPopularityAndRating(metadata3g2);
        _metadataRepository.save(metadata3g2);

        Map<Group, Integer> groupOwnerToStatMap = _metadataRepository.getMetadataStatistics()
                .getGroupOwnerToStatMap(metadataCount());

        assertEquals(2, groupOwnerToStatMap.size());
        assertEquals(2, groupOwnerToStatMap.get(group1).intValue());
        assertEquals(1, groupOwnerToStatMap.get(group2).intValue());
        assertNull(groupOwnerToStatMap.get(group3));

        groupOwnerToStatMap = _metadataRepository.getMetadataStatistics()
                .getGroupOwnerToStatMap(popularitySum());

        assertEquals(2, groupOwnerToStatMap.size());
        assertEquals(2 * POPULARITY, groupOwnerToStatMap.get(group1).intValue());
        assertEquals(POPULARITY, groupOwnerToStatMap.get(group2).intValue());
        assertNull(groupOwnerToStatMap.get(group3));

        groupOwnerToStatMap = _metadataRepository.getMetadataStatistics()
                .getGroupOwnerToStatMap(ratingSum());

        assertEquals(2, groupOwnerToStatMap.size());
        assertEquals(2 * RATING, groupOwnerToStatMap.get(group1).intValue());
        assertEquals(RATING, groupOwnerToStatMap.get(group2).intValue());
        assertNull(groupOwnerToStatMap.get(group3));
    }

    @Test
    public void testGetOwnerToStatMap() throws Exception {
        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = _userRepository.save(UserRepositoryTest.newUser(_inc));

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setOwner(user1.getId());
        setPopularityAndRating(metadata1g1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setOwner(user1.getId());
        setPopularityAndRating(metadata2g1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setOwner(user2.getId());
        setPopularityAndRating(metadata3g2);
        _metadataRepository.save(metadata3g2);

        Map<User, Integer> ownerToStatMap = _metadataRepository.getMetadataStatistics().getOwnerToStatMap(metadataCount());

        assertEquals(2, ownerToStatMap.size());
        assertEquals(2, ownerToStatMap.get(user1).intValue());
        assertEquals(1, ownerToStatMap.get(user2).intValue());
        assertNull(ownerToStatMap.get(user3));

        ownerToStatMap = _metadataRepository.getMetadataStatistics().getOwnerToStatMap(popularitySum());

        assertEquals(2, ownerToStatMap.size());
        assertEquals(2 * POPULARITY, ownerToStatMap.get(user1).intValue());
        assertEquals(POPULARITY, ownerToStatMap.get(user2).intValue());
        assertNull(ownerToStatMap.get(user3));

        ownerToStatMap = _metadataRepository.getMetadataStatistics().getOwnerToStatMap(ratingSum());

        assertEquals(2, ownerToStatMap.size());
        assertEquals(2 * RATING, ownerToStatMap.get(user1).intValue());
        assertEquals(RATING, ownerToStatMap.get(user2).intValue());
        assertNull(ownerToStatMap.get(user3));
    }

    @Test
    public void testGetSourceToStatMap() throws Exception {

        Source source1 = _sourceRepository.save(SourceRepositoryTest.newSource(_inc));
        Source source2 = _sourceRepository.save(SourceRepositoryTest.newSource(_inc));
        Source source3 = _sourceRepository.save(SourceRepositoryTest.newSource(_inc));

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setSourceId(source1.getUuid());
        setPopularityAndRating(metadata1g1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setSourceId(source1.getUuid());
        setPopularityAndRating(metadata2g1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setSourceId(source2.getUuid());
        setPopularityAndRating(metadata3g2);
        _metadataRepository.save(metadata3g2);

        Map<Source, Integer> sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getSourceToStatMap(metadataCount());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2, sourceToStatMap.get(source1).intValue());
        assertEquals(1, sourceToStatMap.get(source2).intValue());
        assertNull(sourceToStatMap.get(source3));

        sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getSourceToStatMap(popularitySum());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2 * POPULARITY, sourceToStatMap.get(source1).intValue());
        assertEquals(POPULARITY, sourceToStatMap.get(source2).intValue());
        assertNull(sourceToStatMap.get(source3));

        sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getSourceToStatMap(ratingSum());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2 * RATING, sourceToStatMap.get(source1).intValue());
        assertEquals(RATING, sourceToStatMap.get(source2).intValue());
        assertNull(sourceToStatMap.get(source3));
    }

    @Test
    public void testGetSchemaToStatMap() throws Exception {

        String schema1 = "schema1";
        String schema2 = "schema2";
        String schema3 = "schema3";

        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getDataInfo().setSchemaId(schema1);
        setPopularityAndRating(metadata1g1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getDataInfo().setSchemaId(schema1);
        setPopularityAndRating(metadata2g1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getDataInfo().setSchemaId(schema2);
        setPopularityAndRating(metadata3g2);
        _metadataRepository.save(metadata3g2);

        Map<String, Integer> sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getSchemaToStatMap(metadataCount());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2, sourceToStatMap.get(schema1).intValue());
        assertEquals(1, sourceToStatMap.get(schema2).intValue());
        assertNull(sourceToStatMap.get(schema3));

        sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getSchemaToStatMap(popularitySum());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2 * POPULARITY, sourceToStatMap.get(schema1).intValue());
        assertEquals(POPULARITY, sourceToStatMap.get(schema2).intValue());
        assertNull(sourceToStatMap.get(schema3));

        sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getSchemaToStatMap(ratingSum());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2 * RATING, sourceToStatMap.get(schema1).intValue());
        assertEquals(RATING, sourceToStatMap.get(schema2).intValue());
        assertNull(sourceToStatMap.get(schema3));
    }

    @Test
    public void testGetMetadataTypeToStatMap() throws Exception {
        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getDataInfo().setType(MetadataType.METADATA);
        setPopularityAndRating(metadata1g1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getDataInfo().setType(MetadataType.METADATA);
        setPopularityAndRating(metadata2g1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        setPopularityAndRating(metadata3g2);
        _metadataRepository.save(metadata3g2);


        Map<MetadataType, Integer> typeToStatMap = _metadataRepository.getMetadataStatistics()
                .getMetadataTypeToStatMap(metadataCount());
        assertEquals(3, typeToStatMap.size());
        assertEquals(2, typeToStatMap.get(MetadataType.METADATA).intValue());
        assertEquals(1, typeToStatMap.get(MetadataType.SUB_TEMPLATE).intValue());
        assertEquals(0, typeToStatMap.get(MetadataType.TEMPLATE).intValue());

        typeToStatMap = _metadataRepository.getMetadataStatistics()
                .getMetadataTypeToStatMap(popularitySum());
        assertEquals(3, typeToStatMap.size());
        assertEquals(2 * POPULARITY, typeToStatMap.get(MetadataType.METADATA).intValue());
        assertEquals(POPULARITY, typeToStatMap.get(MetadataType.SUB_TEMPLATE).intValue());
        assertEquals(0, typeToStatMap.get(MetadataType.TEMPLATE).intValue());

        typeToStatMap = _metadataRepository.getMetadataStatistics()
                .getMetadataTypeToStatMap(ratingSum());
        assertEquals(3, typeToStatMap.size());
        assertEquals(2 * RATING, typeToStatMap.get(MetadataType.METADATA).intValue());
        assertEquals(RATING, typeToStatMap.get(MetadataType.SUB_TEMPLATE).intValue());
        assertEquals(0, typeToStatMap.get(MetadataType.TEMPLATE).intValue());
    }

    @Test
    public void testGetIsHarvestedToStatMap() throws Exception {
        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getHarvestInfo().setHarvested(false);
        setPopularityAndRating(metadata1g1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getHarvestInfo().setHarvested(false);
        setPopularityAndRating(metadata2g1);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getHarvestInfo().setHarvested(true);
        setPopularityAndRating(metadata3g2);
        _metadataRepository.save(metadata3g2);

        Map<Boolean, Integer> sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getIsHarvestedToStatMap(metadataCount());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2, sourceToStatMap.get(false).intValue());
        assertEquals(1, sourceToStatMap.get(true).intValue());

        sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getIsHarvestedToStatMap(popularitySum());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2 * POPULARITY, sourceToStatMap.get(false).intValue());
        assertEquals(POPULARITY, sourceToStatMap.get(true).intValue());

        sourceToStatMap = _metadataRepository.getMetadataStatistics()
                .getIsHarvestedToStatMap(ratingSum());

        assertEquals(2, sourceToStatMap.size());
        assertEquals(2 * RATING, sourceToStatMap.get(false).intValue());
        assertEquals(RATING, sourceToStatMap.get(true).intValue());
    }

    @Test
    public void testGetStatusValueToStatMap() throws Exception {
        Metadata md1 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(md1);
        md1 = _metadataRepository.save(md1);

        Metadata md2 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(md2);
        md2 = _metadataRepository.save(md2);

        Metadata md3 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(md3);
        _metadataRepository.save(md3);

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


        final MetadataStatisticsQueries statistics = _metadataRepository.getMetadataStatistics();
        Map<StatusValue, Integer> statusValueToStatMap = statistics.getStatusValueToStatMap(metadataCount());

        assertEquals(2, statusValueToStatMap.size());
        assertEquals(2, statusValueToStatMap.get(metadataStatus1.getStatusValue()).intValue());
        assertEquals(1, statusValueToStatMap.get(metadataStatus3.getStatusValue()).intValue());

        statusValueToStatMap = statistics.getStatusValueToStatMap(popularitySum());

        assertEquals(2, statusValueToStatMap.size());
        assertEquals(2 * POPULARITY, statusValueToStatMap.get(metadataStatus1.getStatusValue()).intValue());
        assertEquals(POPULARITY, statusValueToStatMap.get(metadataStatus3.getStatusValue()).intValue());

        statusValueToStatMap = statistics.getStatusValueToStatMap(ratingSum());

        assertEquals(2, statusValueToStatMap.size());
        assertEquals(2 * RATING, statusValueToStatMap.get(metadataStatus1.getStatusValue()).intValue());
        assertEquals(RATING, statusValueToStatMap.get(metadataStatus3.getStatusValue()).intValue());
    }

    private Metadata setPopularityAndRating(Metadata md3) {
        md3.getDataInfo().setPopularity(POPULARITY);
        md3.getDataInfo().setRating(RATING);
        return md3;
    }

    @Test
    public void testGetMetadataValidationStatusToStatMap() throws Exception {

        _metadataRepository.save(setPopularityAndRating(MetadataRepositoryTest.newMetadata(_inc)));
        _metadataRepository.save(setPopularityAndRating(MetadataRepositoryTest.newMetadata(_inc)));
        _metadataRepository.save(setPopularityAndRating(MetadataRepositoryTest.newMetadata(_inc)));
        _metadataRepository.save(setPopularityAndRating(MetadataRepositoryTest.newMetadata(_inc)));

        MetadataValidation validation = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation.getId().getMetadataId());
        validation.setValid(true);
        _metadataValidationRepository.save(validation);

        MetadataValidation validation2 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation2.getId().getMetadataId());
        validation2.setValid(true);
        _metadataValidationRepository.save(validation2);

        MetadataValidation validation3 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation3.getId().getMetadataId());
        validation3.setValid(false);
        _metadataValidationRepository.save(validation3);

        Map<MetadataValidationStatus, Integer> statusToStatMap = _metadataRepository
                .getMetadataStatistics().getMetadataValidationStatusToStatMap(metadataCount());

        assertEquals(3, statusToStatMap.size());
        assertEquals(2, statusToStatMap.get(MetadataValidationStatus.VALID).intValue());
        assertEquals(1, statusToStatMap.get(MetadataValidationStatus.INVALID).intValue());
        assertEquals(4, statusToStatMap.get(MetadataValidationStatus.NEVER_CALCULATED).intValue());

        statusToStatMap = _metadataRepository
                .getMetadataStatistics().getMetadataValidationStatusToStatMap(popularitySum());

        assertEquals(3, statusToStatMap.size());
        assertEquals(2 * POPULARITY, statusToStatMap.get(MetadataValidationStatus.VALID).intValue());
        assertEquals(POPULARITY, statusToStatMap.get(MetadataValidationStatus.INVALID).intValue());
        assertEquals(4 * POPULARITY, statusToStatMap.get(MetadataValidationStatus.NEVER_CALCULATED).intValue());

        statusToStatMap = _metadataRepository
                .getMetadataStatistics().getMetadataValidationStatusToStatMap(ratingSum());

        assertEquals(3, statusToStatMap.size());
        assertEquals(2 * RATING, statusToStatMap.get(MetadataValidationStatus.VALID).intValue());
        assertEquals(RATING, statusToStatMap.get(MetadataValidationStatus.INVALID).intValue());
        assertEquals(4 * RATING, statusToStatMap.get(MetadataValidationStatus.NEVER_CALCULATED).intValue());
    }

    private void findAndSetPopularityAndRating(int metadataId) {
        final Metadata metadata = _metadataRepository.findOne(metadataId);
        setPopularityAndRating(metadata);
        _metadataRepository.save(metadata);
    }

    @Test
    public void testGetMetadataValidationTypeAndStatusToStatMap() throws Exception {
        MetadataValidation validation = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation.getId().getMetadataId());
        validation.setValid(true);
        _metadataValidationRepository.save(validation);

        MetadataValidation validation2 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation2.getId().getMetadataId());
        validation2.getId().setValidationType(validation.getId().getValidationType());
        validation2.setValid(false);
        _metadataValidationRepository.save(validation2);

        MetadataValidation validation3 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation3.getId().getMetadataId());
        validation3.getId().setValidationType(validation.getId().getValidationType());
        validation3.setValid(false);
        _metadataValidationRepository.save(validation3);

        MetadataValidation validation4 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation4.getId().getMetadataId());
        validation4.setValid(false);
        _metadataValidationRepository.save(validation4);

        MetadataValidation validation5 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation5.getId().getMetadataId());
        validation5.setValid(false);
        _metadataValidationRepository.save(validation5);

        MetadataValidation validation6 = MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepository);
        findAndSetPopularityAndRating(validation6.getId().getMetadataId());
        validation6.setValid(false);
        _metadataValidationRepository.save(validation6);

        Map<Pair<String, MetadataValidationStatus>, Integer> map =
                _metadataRepository.getMetadataStatistics().getMetadataValidationTypeAndStatusToStatMap(metadataCount());

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

        _metadataRepository.save(setPopularityAndRating(MetadataRepositoryTest.newMetadata(_inc)));
        map = _metadataRepository.getMetadataStatistics().getMetadataValidationTypeAndStatusToStatMap(metadataCount());
        assertEquals(1, map.get(Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED)).intValue());


        map = _metadataRepository.getMetadataStatistics().getMetadataValidationTypeAndStatusToStatMap(popularitySum());

        assertEquals(6, map.size());
        assertEquals(POPULARITY, map.get(Pair.read(validation.getId().getValidationType(), MetadataValidationStatus.VALID)).intValue());
        assertEquals(2 * POPULARITY, map.get(Pair.read(validation.getId().getValidationType(),
                MetadataValidationStatus.INVALID)).intValue());
        assertEquals(POPULARITY, map.get(Pair.read(validation4.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue
                ());
        assertEquals(POPULARITY, map.get(Pair.read(validation5.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue
                ());
        assertEquals(POPULARITY, map.get(Pair.read(validation6.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue
                ());
        assertNull(map.get(Pair.read(validation4.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertNull(map.get(Pair.read(validation5.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertNull(map.get(Pair.read(validation6.getId().getValidationType(), MetadataValidationStatus.VALID)));

        assertEquals(POPULARITY, map.get(Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED)).intValue());


        map = _metadataRepository.getMetadataStatistics().getMetadataValidationTypeAndStatusToStatMap(ratingSum());

        assertEquals(6, map.size());
        assertEquals(RATING, map.get(Pair.read(validation.getId().getValidationType(), MetadataValidationStatus.VALID)).intValue());
        assertEquals(2 * RATING, map.get(Pair.read(validation.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertEquals(RATING, map.get(Pair.read(validation4.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertEquals(RATING, map.get(Pair.read(validation5.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertEquals(RATING, map.get(Pair.read(validation6.getId().getValidationType(), MetadataValidationStatus.INVALID)).intValue());
        assertNull(map.get(Pair.read(validation4.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertNull(map.get(Pair.read(validation5.getId().getValidationType(), MetadataValidationStatus.VALID)));
        assertNull(map.get(Pair.read(validation6.getId().getValidationType(), MetadataValidationStatus.VALID)));

        assertEquals(RATING, map.get(Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED)).intValue());
    }

    @Test
    public void testSumPopularity() throws Exception {
        final MetadataStatisticsQueries metadataStatistics = _metadataRepository.getMetadataStatistics();
        final Optional<Specification<Metadata>> absent = Optional.absent();
        final Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(metadata);
        _metadataRepository.save(metadata);

        Metadata metadata1 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(metadata1);
        metadata1 = _metadataRepository.save(metadata1);

        final Metadata metadata2 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(metadata2);
        _metadataRepository.save(metadata2);

        assertEquals(3 * POPULARITY, metadataStatistics.getTotalStat(popularitySum(), absent));
        assertEquals(POPULARITY, metadataStatistics.getTotalStat(popularitySum(), Optional.of(MetadataSpecs.hasMetadataId
                (metadata1.getId()))));

        assertEquals(3, metadataStatistics.getTotalStat(metadataCount(), absent));
        assertEquals(1, metadataStatistics.getTotalStat(metadataCount(), Optional.of(MetadataSpecs.hasMetadataId
                (metadata1.getId()))));

        assertEquals(3 * RATING, metadataStatistics.getTotalStat(ratingSum(), absent));
        assertEquals(RATING, metadataStatistics.getTotalStat(ratingSum(), Optional.of(MetadataSpecs.hasMetadataId
                (metadata1.getId()))));


    }

    @Test
    public void testGetStatBasedOnOperationAllowed() throws Exception {
        final MetadataStatisticsQueries metadataStatistics = _metadataRepository.getMetadataStatistics();
        final Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(metadata);
        final Metadata md1 = _metadataRepository.save(metadata);

        Metadata metadata1 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(metadata1);
        final Metadata md2 = _metadataRepository.save(metadata1);

        final Metadata metadata2 = MetadataRepositoryTest.newMetadata(_inc);
        setPopularityAndRating(metadata2);
        final Metadata md3 = _metadataRepository.save(metadata2);


        _operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId(md1.getId(), ReservedGroup.all.getId(),
                ReservedOperation.download.getId())));
        _operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId(md1.getId(), ReservedGroup.all.getId(),
                ReservedOperation.view.getId())));
        _operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId(md1.getId(), ReservedGroup.intranet.getId(),
                ReservedOperation.view.getId())));

        _operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId(md2.getId(), ReservedGroup.all.getId(),
                ReservedOperation.view.getId())));
        _operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId(md2.getId(), ReservedGroup.intranet.getId(),
                ReservedOperation.view.getId())));

        _operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId(md3.getId(), ReservedGroup.intranet.getId(),
                ReservedOperation.view.getId())));

        Specification<OperationAllowed> operationAllowedSpec = Specifications.where(OperationAllowedSpecs.isPublic(ReservedOperation
                .view)).or(OperationAllowedSpecs.isPublic(ReservedOperation.download));

        assertEquals(2, metadataStatistics.getStatBasedOnOperationAllowed(metadataCount(), operationAllowedSpec));
        assertEquals(2 * POPULARITY, metadataStatistics.getStatBasedOnOperationAllowed(popularitySum(), operationAllowedSpec));
        assertEquals(2 * RATING, metadataStatistics.getStatBasedOnOperationAllowed(ratingSum(), operationAllowedSpec));


    }

    @Test
    public void testNoErrorsWhenNoMetadata() throws Exception {
        final MetadataStatisticsQueries metadataStatistics = _metadataRepository.getMetadataStatistics();
        final Optional<Specification<Metadata>> absent = Optional.absent();
        assertEquals(0, metadataStatistics.getTotalStat(ratingSum(), absent));
        assertEquals(0, metadataStatistics.getTotalStat(metadataCount(), absent));
        assertEquals(0, metadataStatistics.getTotalStat(popularitySum(), absent));
        assertEquals(0, metadataStatistics.getTotalStat(metadataCount(), Optional.of(MetadataSpecs.hasMetadataId(1))));
        assertEquals(0, metadataStatistics.getGroupOwnerToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getGroupOwnerToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getGroupOwnerToStatMap(popularitySum()).size());
        assertEquals(0, metadataStatistics.getStatBasedOnOperationAllowed(popularitySum(), OperationAllowedSpecs.hasOperation
                (ReservedOperation.view)));

        final Map<Boolean, Integer> isHarvestedToStatMap = metadataStatistics.getIsHarvestedToStatMap(metadataCount());
        metadataStatistics.getIsHarvestedToStatMap(ratingSum());
        metadataStatistics.getIsHarvestedToStatMap(popularitySum());
        assertEquals(2, isHarvestedToStatMap.size());
        assertEquals(0, isHarvestedToStatMap.get(false).intValue());
        assertEquals(0, isHarvestedToStatMap.get(true).intValue());
        assertEquals(0, metadataStatistics.getMetadataCategoryToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getMetadataCategoryToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getMetadataCategoryToStatMap(popularitySum()).size());

        final Map<MetadataType, Integer> metadataTypeToStatMap = metadataStatistics.getMetadataTypeToStatMap(metadataCount());
        metadataStatistics.getMetadataTypeToStatMap(ratingSum());
        metadataStatistics.getMetadataTypeToStatMap(popularitySum());
        assertEquals(3, metadataTypeToStatMap.size());
        assertEquals(0, metadataTypeToStatMap.get(MetadataType.SUB_TEMPLATE).intValue());
        assertEquals(0, metadataTypeToStatMap.get(MetadataType.METADATA).intValue());
        assertEquals(0, metadataTypeToStatMap.get(MetadataType.TEMPLATE).intValue());

        final Map<MetadataValidationStatus, Integer> metadataValidationStatusToStatMap = metadataStatistics
                .getMetadataValidationStatusToStatMap(metadataCount());
        metadataStatistics.getMetadataValidationStatusToStatMap(ratingSum());
        metadataStatistics.getMetadataValidationStatusToStatMap(popularitySum());
        assertEquals(1, metadataValidationStatusToStatMap.size());
        assertEquals(0, metadataValidationStatusToStatMap.get(MetadataValidationStatus.NEVER_CALCULATED).intValue());

        final Map<Pair<String, MetadataValidationStatus>, Integer> metadataValidationTypeAndStatusToStatMap =
                metadataStatistics.getMetadataValidationTypeAndStatusToStatMap(metadataCount());
        metadataStatistics.getMetadataValidationTypeAndStatusToStatMap(popularitySum());
        metadataStatistics.getMetadataValidationTypeAndStatusToStatMap(ratingSum());
        assertEquals(1, metadataValidationTypeAndStatusToStatMap.size());
        assertEquals(0, metadataValidationTypeAndStatusToStatMap.get(Pair.read(null, MetadataValidationStatus.NEVER_CALCULATED))
                .intValue());

        assertEquals(0, metadataStatistics.getMetadataCategoryToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getMetadataCategoryToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getMetadataCategoryToStatMap(popularitySum()).size());
        assertEquals(0, metadataStatistics.getOwnerToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getOwnerToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getOwnerToStatMap(popularitySum()).size());
        assertEquals(0, metadataStatistics.getSchemaToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getSchemaToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getSchemaToStatMap(popularitySum()).size());
        assertEquals(0, metadataStatistics.getSourceToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getSourceToStatMap(popularitySum()).size());
        assertEquals(0, metadataStatistics.getSourceToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getStatusValueToStatMap(metadataCount()).size());
        assertEquals(0, metadataStatistics.getStatusValueToStatMap(ratingSum()).size());
        assertEquals(0, metadataStatistics.getStatusValueToStatMap(popularitySum()).size());
    }
}
