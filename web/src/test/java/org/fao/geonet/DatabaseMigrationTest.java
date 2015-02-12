package org.fao.geonet;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.Service;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.AddressRepository;
import org.fao.geonet.repository.AddressRepositoryTest;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepositoryTest;
import org.fao.geonet.repository.CustomElementSetRepository;
import org.fao.geonet.repository.CustomElementSetRepositoryTest;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.GroupRepositoryTest;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.HarvestHistoryRepositoryTest;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.fao.geonet.repository.HarvesterSettingRepositoryTest;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.LanguageRepositoryTest;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataCategoryRepositoryTest;
import org.fao.geonet.repository.MetadataNotificationRepository;
import org.fao.geonet.repository.MetadataNotificationRepositoryTest;
import org.fao.geonet.repository.MetadataNotifierRepository;
import org.fao.geonet.repository.MetadataNotifierRepositoryTest;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataRatingByIpRepositoryTest;
import org.fao.geonet.repository.MetadataRelationRepository;
import org.fao.geonet.repository.MetadataRelationRepositoryTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataRepositoryTest;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataStatusRepositoryTest;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.MetadataValidationRepositoryTest;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.repository.OperationRepositoryTest;
import org.fao.geonet.repository.ServiceRepository;
import org.fao.geonet.repository.ServiceRepositoryTest;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.SourceRepositoryTest;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.StatusValueRepositoryTest;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.fao.geonet.repository.ThesaurusActivationRepositoryTest;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserGroupRepositoryTest;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.repository.statistic.SearchRequestParamRepository;
import org.fao.geonet.repository.statistic.SearchRequestParamRepositoryTest;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.fao.geonet.repository.statistic.SearchRequestRepositoryTest;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test migration from a 2.8.0 database to current.  The DatabaseMigration postprocessor is configured in the
 * {@link ContextConfiguration} annotation on this class.  Therefore the migration is performed on startup and the test
 * merely checks that it completed correctly.
 */
@ContextConfiguration(inheritLocations = true,
        locations = {
                "classpath:migration-repository-test-context.xml",
                "classpath:services-repository-test-context.xml"})
public class DatabaseMigrationTest extends AbstractSpringDataTest {

    public static final String DATABASE_MIGRATION_XML = "WEB-INF/config-db/database_migration.xml";

    @Autowired
    private MetadataRepository _mdRepo;
    @Autowired
    private AddressRepository _addressRepo;
    @Autowired
    private CswCapabilitiesInfoFieldRepository _capInfoFieldRepo;
    @Autowired
    private CustomElementSetRepository _customElementRepo;
    @Autowired
    private GroupRepository _groupRepo;
    @Autowired
    private HarvesterSettingRepository _harvesterSettingRepo;
    @Autowired
    private HarvestHistoryRepository _harvestHistoryRepo;
    @Autowired
    private IsoLanguageRepository _isoLangRepo;
    @Autowired
    private LanguageRepository _LanguageRepo;
    @Autowired
    private MetadataCategoryRepository _metadataCategoryRepo;
    @Autowired
    private MetadataNotificationRepository _metadataNotificationRepo;
    @Autowired
    private MetadataNotifierRepository _metadataNotificatierRepo;
    @Autowired
    private MetadataRatingByIpRepository _ratingRepo;
    @Autowired
    private MetadataRelationRepository _relationRepo;
    @Autowired
    private MetadataStatusRepository _metadataStatusRepo;
    @Autowired
    private MetadataValidationRepository _metadataValidationRepo;
    @Autowired
    private OperationAllowedRepository _opAllowedRepo;
    @Autowired
    private OperationRepository _operationRepo;
    @Autowired
    private ServiceRepository _serviceRepo;
    @Autowired
    private SettingRepository _settingRepo;
    @Autowired
    private SourceRepository _sourceRepo;
    @Autowired
    private StatusValueRepository _statusValueRepo;
    @Autowired
    private ThesaurusActivationRepository _thesaurusActivationRepo;
    @Autowired
    private UserGroupRepository _userGroupRepo;
    @Autowired
    private UserRepository _userRepo;
    @Autowired
    private SearchRequestRepository _searchRequestRepo;
    @Autowired
    private SearchRequestParamRepository _searchRequestParamRepo;

    @Test
    public void testMigrate() throws Exception {
        final AtomicInteger _inc = new AtomicInteger(10000);
        final AtomicInteger smallNumInt = new AtomicInteger(7);

        assertEquals(2, _addressRepo.count());
        _addressRepo.saveAndFlush(AddressRepositoryTest.newAddress(_inc));
        assertEquals(64, _capInfoFieldRepo.count());
        _capInfoFieldRepo.saveAndFlush(CswCapabilitiesInfoFieldRepositoryTest.newCswServerCapabilitiesInfo(smallNumInt).setLangId("eng"));
        assertEquals(0, _customElementRepo.count());
        _customElementRepo.saveAndFlush(CustomElementSetRepositoryTest.newCustomElementSet(smallNumInt));
        assertEquals(4, _groupRepo.count());
        assertEquals(16, _groupRepo.findOne(1).getLabelTranslations().size());
        final Group group = _groupRepo.saveAndFlush(GroupRepositoryTest.newGroup(_inc));
        assertEquals(22, _harvesterSettingRepo.count());
        assertEquals(1, _harvesterSettingRepo.findAllByName("name").size());
        _harvesterSettingRepo.saveAndFlush(HarvesterSettingRepositoryTest.newSetting(_inc));
        assertEquals(1, _harvestHistoryRepo.count());
        _harvestHistoryRepo.saveAndFlush(HarvestHistoryRepositoryTest.createHarvestHistory(_inc));
        assertEquals(16, _LanguageRepo.count());
        Language newLang = LanguageRepositoryTest.newLanguage(smallNumInt);
        _LanguageRepo.saveAndFlush(newLang);
        assertEquals(484, _isoLangRepo.count());
        IsoLanguage isoLang = new IsoLanguage();
        isoLang.setCode("zzz");
        isoLang.setShortCode("zz");
        _isoLangRepo.saveAndFlush(isoLang);
        assertEquals(8, _mdRepo.count());
        Metadata metadata = _mdRepo.saveAndFlush(MetadataRepositoryTest.newMetadata(_inc));
        assertEquals(13, _metadataCategoryRepo.count());
        _metadataCategoryRepo.saveAndFlush(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        assertEquals(1, _metadataNotificatierRepo.count());
        _metadataNotificatierRepo.saveAndFlush(MetadataNotifierRepositoryTest.newMetadataNotifier(_inc));
        assertEquals(0, _metadataNotificationRepo.count());
        _metadataNotificationRepo.saveAndFlush(MetadataNotificationRepositoryTest.newMetadataNotification(_inc, _metadataNotificatierRepo));
        assertEquals(6, _statusValueRepo.count());
        _statusValueRepo.saveAndFlush(StatusValueRepositoryTest.newStatusValue(_inc));
        assertEquals(1, _metadataStatusRepo.count());
        final MetadataStatus metadataStatus = MetadataStatusRepositoryTest.newMetadataStatus(_inc, _statusValueRepo);
        metadataStatus.getId().setMetadataId(metadata.getId());
        _metadataStatusRepo.saveAndFlush(metadataStatus);
        assertEquals(3, _metadataValidationRepo.count());
        _metadataValidationRepo.saveAndFlush(MetadataValidationRepositoryTest.newValidation(_inc, _mdRepo));
        assertEquals(6, _operationRepo.count());
        Operation operation = _operationRepo.saveAndFlush(OperationRepositoryTest.newOperation(_inc));
        assertEquals(72, _opAllowedRepo.count());
        _opAllowedRepo.saveAndFlush(new OperationAllowed(new OperationAllowedId(metadata.getId(), group.getId(), operation.getId())));
        assertEquals(2, _ratingRepo.count());
        final MetadataRatingByIp ratingByIp = MetadataRatingByIpRepositoryTest.newMetadataRatingByIp(_inc);
        ratingByIp.getId().setMetadataId(metadata.getId());
        _ratingRepo.saveAndFlush(ratingByIp);
        assertEquals(0, _relationRepo.count());
        _relationRepo.saveAndFlush(MetadataRelationRepositoryTest.newMetadataRelation(_inc, _mdRepo));
        assertEquals(4, _searchRequestParamRepo.count());
        _searchRequestParamRepo.saveAndFlush(SearchRequestParamRepositoryTest.newRequestParam(_inc));
        assertEquals(39, _searchRequestRepo.count());
        _searchRequestRepo.saveAndFlush(SearchRequestRepositoryTest.newSearchRequest(_inc));
        assertEquals(0, _serviceRepo.count());
        Service service = ServiceRepositoryTest.newService(_inc);
        service.getParameters().clear();
        service = _serviceRepo.saveAndFlush(service);
        service.getParameters().put("p1", "p2");
        _serviceRepo.saveAndFlush(service);
        assertTrue(_settingRepo.count() > 0);
        _settingRepo.update("system/csw/metadataPublic", new Updater<Setting>() {
            @Override
            public void apply(@Nonnull Setting entity) {
                entity.setValue("true");
            }
        });
        assertNotNull(_settingRepo.findOne(SettingManager.CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS));
        final Setting ignoreChars = _settingRepo.findOne(SettingManager.SYSTEM_LUCENE_IGNORECHARS);
        assertNotNull(ignoreChars);
        assertEquals(SettingDataType.STRING, ignoreChars.getDataType());
        assertEquals("", ignoreChars.getValue());
        assertEquals(1, _sourceRepo.count());
        _sourceRepo.saveAndFlush(SourceRepositoryTest.newSource(_inc));
        assertEquals(0, _thesaurusActivationRepo.count());
        _thesaurusActivationRepo.saveAndFlush(ThesaurusActivationRepositoryTest.newThesaurusActivation(_inc));
        assertEquals(2, _userRepo.count());
        _userRepo.saveAndFlush(UserRepositoryTest.newUser(_inc));
        assertEquals(1, _userGroupRepo.count());
        _userGroupRepo.saveAndFlush(UserGroupRepositoryTest.getUserGroup(_inc, _userRepo, _groupRepo));
    }


    static File findwebappDir() {
        File current = new File(".").getAbsoluteFile();
        final String pathToFile = "src/main/webapp/" + DATABASE_MIGRATION_XML;
        final String pathToFile2 = "web/src/main/webapp/" + DATABASE_MIGRATION_XML;
        while (!new File(current, pathToFile).exists() && !new File(current, pathToFile2).exists()) {
            current = current.getParentFile();
        }

        if (new File(current, pathToFile).exists()) {
            return new File(current, pathToFile).getParentFile().getParentFile().getParentFile();
        } else {
            return new File(current, pathToFile2).getParentFile().getParentFile().getParentFile();
        }
    }

    public static class DatabasePathLocator implements Callable<String> {
        public TemporaryFolder dbFileContainer = new TemporaryFolder();

        @Override
        public String call() throws Exception {
            final String pathToDbFile = "2_8/geonetwork.h2.db";
            final String resource = DatabaseMigrationTest.class.getClassLoader().getResource(pathToDbFile).getFile();
            dbFileContainer.create();
            String parentFile = new File(resource).getParent();
            FileUtils.copyDirectory(new File(parentFile), dbFileContainer.getRoot());
            String path = dbFileContainer.getRoot() + "/geonetwork";
            return path;
        }

        @PreDestroy
        public void deleteTmpFiles() {
            dbFileContainer.delete();
        }
    }

    public static class MigrationConfigLoader implements Callable<LinkedHashMap<String, List<String>>> {

        @Override
        public LinkedHashMap<String, List<String>> call() throws Exception {
            final String configLocation = new File(findwebappDir(), DATABASE_MIGRATION_XML).toURI().toString();
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
            return context.getBean(DatabaseMigration.class).getMigrationConfig();
        }
    }
}