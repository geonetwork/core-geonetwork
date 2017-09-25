package org.fao.geonet.api.records.backup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.util.Assert;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ArchiveAllMetadataJob extends QuartzJobBean {

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private SettingManager settingManager;

    static final String BACKUP_FILENAME = "gn_backup";
    static final String BACKUP_DIR = "backup_archive";
    public static final String BACKUP_LOG = Geonet.GEONETWORK + ".backup";
    private AtomicBoolean backupIsRunning = new AtomicBoolean(false);


    @Override
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        ServiceContext serviceContext = serviceManager.createServiceContext("unpublishMetadata", context);
        serviceContext.setLanguage("eng");
        serviceContext.setAsThreadLocal();

        ApplicationContextHolder.set(this.context);

        if(!settingManager.getValueAsBool(Settings.METADATA_BACKUPARCHIVE_ENABLE)) {
            Log.info(BACKUP_LOG, "Backup archive not enabled");
            return;
        }

        try {
            createBackup(serviceContext);
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Error running " + ArchiveAllMetadataJob.class.getSimpleName(), e);
        }
    }

    public void createBackup(ServiceContext serviceContext) throws Exception {

        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);

        Path stylePath = dataDirectory.getWebappDir().resolve(Geonet.Path.SCHEMAS);

        if (!backupIsRunning.compareAndSet(false, true)) {
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            Log.info(BACKUP_LOG, "Starting backup of all metadata");
            System.out.println("Starting backup of all metadata");

            final MetadataRepository metadataRepository = serviceContext.getBean(MetadataRepository.class);

            loginAsAdmin(serviceContext);
            final Specification<Metadata> harvested = Specifications.where(MetadataSpecs.isHarvested(false)).
                    and(Specifications.not(MetadataSpecs.hasType(MetadataType.SUB_TEMPLATE)));
            List<String> uuids = Lists.transform(metadataRepository.findAll(harvested), new Function<Metadata,
                    String>() {
                @Nullable
                @Override
                public String apply(@Nullable Metadata input) {
                    return input.getUuid();
                }
            });

            Log.info(BACKUP_LOG, "Backing up " + uuids.size() + " metadata");

            String format = "full";
            boolean resolveXlink = true;
            boolean removeXlinkAttribute = false;
            boolean skipOnError = true;
            Path srcFile = MEFLib.doMEF2Export(serviceContext, new HashSet<>(uuids), format, false, stylePath,
                    resolveXlink, removeXlinkAttribute, skipOnError);

            Path backupDir = dataDirectory.getBackupDir().resolve(BACKUP_DIR);
            String today = new SimpleDateFormat("-yyyy-MM-dd").format(new Date());
            Path destFile = backupDir.resolve(BACKUP_FILENAME + today + ".zip");
            IO.deleteFileOrDirectory(backupDir);
            Files.createDirectories(destFile.getParent());
            Files.move(srcFile, destFile);
            if (!Files.exists(destFile)) {
                throw new Exception("Moving backup file failed!");
            }
            long timeMinutes = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
            Log.info(BACKUP_LOG, "Backup finished. Backup time: " + timeMinutes + "  Backup file: " + destFile);
        } catch (Throwable t) {
            Log.error(BACKUP_LOG, "Failed to create a back up of metadata", t);
        } finally {
            backupIsRunning.set(false);
        }
    }

    private void loginAsAdmin(ServiceContext serviceContext) {
        final User adminUser = serviceContext.getBean(UserRepository.class).findAll(UserSpecs.hasProfile(Profile.Administrator), new
                PageRequest(0, 1)).getContent().get(0);
        Assert.isTrue(adminUser != null, "The system does not have an admin user");
        UserSession session = new UserSession();
        session.loginAs(adminUser);
        serviceContext.setUserSession(session);
    }

}
