//=============================================================================
//===	Copyright (C) 2001-2020 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.exceptions.UnknownHostEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.HarvestHistorySpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.services.harvesting.notifier.SendNotification;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.QuartzSchedulerUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.fao.geonet.repository.HarvesterSettingRepository.ID_PREFIX;
import static org.quartz.JobKey.jobKey;

/**
 * Represents a harvester job. Used to launch harvester workers.
 * <p>
 * If you want to synchronize something here, use protected variable lock.
 * If not, we may not be able to even stop a frozen harvester.
 */
public abstract class AbstractHarvester<T extends HarvestResult, P extends AbstractParams> {
    public static final String HARVESTER_GROUP_NAME = "HARVESTER_GROUP_NAME";
    private static final String SCHEDULER_ID = "abstractHarvester";
    /**
     * Time to wait for not critical operations in seconds. Should be
     * short. If we cannot do it, just show an error and warn.
     */
    private static final Integer SHORT_WAIT = 2;
    /**
     * Time to wait for important operations in seconds. Patience, but don't block forever.
     */
    private static final Integer LONG_WAIT = 30;

    private final ReentrantLock lock = new ReentrantLock(false);

    /**
     * Should we cancel the harvester?
     */
    protected volatile AtomicBoolean cancelMonitor = new AtomicBoolean(false);

    protected ServiceContext context;

    protected HarvesterSettingsManager harvesterSettingsManager;
    protected SettingManager settingManager;

    protected DataManager dataMan;
    protected IMetadataManager metadataManager;
    protected IMetadataUtils metadataUtils;

    protected P params;
    protected T result;

    protected Logger log = Log.createLogger(Geonet.HARVESTER);

    private Element loadedInfo;
    private String id;
    private volatile Status status;
    /**
     * Exception that aborted the harvesting
     */
    private Throwable error;
    /**
     * Contains all the warnings and errors that didn't abort the execution, but were thrown during harvesting
     */
    private List<HarvestError> errors = Collections.synchronizedList(new LinkedList<>());
    private volatile boolean running = false;

    public static AbstractHarvester<?, ?> create(String type, ServiceContext context) throws BadParameterEx, OperationAbortedEx {
        if (type == null) {
            throw new BadParameterEx("type", null);
        }

        try {
            AbstractHarvester<?, ?> ah = context.getBean(type, AbstractHarvester.class);
            ah.setContext(context);
            return ah;
        } catch (Exception e) {
            throw new OperationAbortedEx("Cannot instantiate harvester of type " + type, e);
        }
    }

    protected void setContext(ServiceContext context) {
        this.context = context;
        this.dataMan = context.getBean(DataManager.class);
        this.metadataUtils = context.getBean(IMetadataUtils.class);
        this.harvesterSettingsManager = context.getBean(HarvesterSettingsManager.class);
        this.settingManager = context.getBean(SettingManager.class);
        this.metadataManager = context.getBean(IMetadataManager.class);
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    private String initializeLog() {

        // configure personalized logger
        String packagename = getClass().getPackage().getName();
        String[] packages = packagename.split("\\.");
        String packageType = packages[packages.length - 1];
        final String harvesterName = this.getParams().getName().replaceAll("\\W+", "_");
        log = Log.createLogger(harvesterName, "geonetwork.harvester");

        String directory = log.getFileAppender();
        if (directory == null || directory.isEmpty()) {
            directory = context.getBean(GeonetworkDataDirectory.class).getSystemDataDir() + "/harvester_logs/";
        }
        File d = new File(directory);
        if (!d.isDirectory()) {
            directory = d.getParent() + File.separator;
        }

        FileAppender fa = new FileAppender();
        fa.setName(harvesterName);
        String logfile = directory + "harvester_" + packageType + "_"
            + harvesterName + "_"
            + dateFormat.format(new Date(System.currentTimeMillis()))
            + ".log";
        fa.setFile(logfile);

        String timeZoneSetting = settingManager.getValue(Settings.SYSTEM_SERVER_TIMEZONE);
        if (StringUtils.isBlank(timeZoneSetting)) {
            timeZoneSetting = TimeZone.getDefault().getID();
        }
        fa.setLayout(new EnhancedPatternLayout("%d{yyyy-MM-dd'T'HH:mm:ss,SSSZ}{" + timeZoneSetting +"} %-5p [%c] - %m%n"));

        fa.setThreshold(log.getThreshold());
        fa.setAppend(true);
        fa.activateOptions();

        log.setAppender(fa);

        return logfile;
    }
    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public void add(Element node) throws BadInputEx, SQLException {
        status = Status.INACTIVE;
        error = null;
        id = doAdd(node);
    }

    public void init(Element node, ServiceContext context) throws BadInputEx, SchedulerException {
        id = node.getAttributeValue("id");
        status = Status.parse(node.getChild("options").getChildText("status"));
        error = null;
        this.context = context;

        doInit(node);

        initInfo(context);

        initializeLog();
        if (status == Status.ACTIVE) {
            doSchedule();
        }
    }

    private void initInfo(ServiceContext context) {
        final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);
        Specification<HarvestHistory> spec = HarvestHistorySpecs.hasHarvesterUuid(getParams().getUuid());
        Pageable pageRequest = PageRequest.of(0, 1,
            Sort.by(Sort.Direction.DESC, SortUtils.createPath(HarvestHistory_.harvestDate)));
        final Page<HarvestHistory> page = historyRepository.findAll(spec, pageRequest);
        if (page.hasContent()) {
            final HarvestHistory history = page.getContent().get(0);
            try {
                this.loadedInfo = history.getInfoAsXml();
            } catch (IOException | JDOMException e) {
                // oh well.  we did our best to get data from the harvester.
            }
        }
    }

    private void doSchedule() throws SchedulerException {
        Scheduler scheduler = getScheduler();

        JobDetail jobDetail = getParams().getJob();
        Trigger trigger = getParams().getTrigger();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    private void doUnschedule() throws SchedulerException {
        getScheduler().deleteJob(jobKey(getParams().getUuid(), HARVESTER_GROUP_NAME));
    }

    /**
     * Deletes the harvester job from the scheduler and schedule it again.
     * @throws SchedulerException
     */
    public void doReschedule() throws SchedulerException {
        doUnschedule();
        doSchedule();
    }

    /**
     * Get the timezone of the harvester cron trigger.
     * @return a time zone.
     * @throws SchedulerException
     */
    public TimeZone getTriggerTimezone() throws SchedulerException {
        Scheduler scheduler = getScheduler();
        List<? extends Trigger> jobTriggers = scheduler.getTriggersOfJob(jobKey(getParams().getUuid(), HARVESTER_GROUP_NAME));
        for (Trigger t : jobTriggers) {
            if (t instanceof CronTrigger) {
                CronTrigger ct = (CronTrigger) t;
                return ct.getTimeZone();
            }
        }
        return null;
    }

    public static Scheduler getScheduler() throws SchedulerException {
        return QuartzSchedulerUtils.getScheduler(SCHEDULER_ID, true);
    }

    public void shutdown() throws SchedulerException {
        getScheduler().deleteJob(jobKey(getParams().getUuid(), HARVESTER_GROUP_NAME));
    }

    public static void shutdownScheduler() throws SchedulerException {
        getScheduler().shutdown(false);
    }

    /**
     * Called when the harvesting entry is removed from the system. It is used to remove harvested metadata.
     */
    public void destroy() throws Exception {
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {

                doUnschedule();

                final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
                final SourceRepository sourceRepository = context.getBean(SourceRepository.class);
                final Resources resources = context.getBean(Resources.class);

                final Specification<? extends AbstractMetadata> ownedByHarvester = Specification.where(MetadataSpecs.hasHarvesterUuid(getParams().getUuid()));
                Set<String> sources = new HashSet<>();
                for (Integer id : metadataRepository.findAllIdsBy(ownedByHarvester)) {
                    sources.add(metadataUtils.findOne(id).getSourceInfo().getSourceId());
                    metadataManager.deleteMetadata(context, "" + id);
                }

                // Remove all sources related to the harvestUuid if they are not linked to any record anymore
                for (String sourceUuid : sources) {
                    Long ownedBySource =
                        metadataRepository.count(Specification.where(MetadataSpecs.hasSource(sourceUuid)));
                    if (ownedBySource == 0
                        && !sourceUuid.equals(params.getUuid())
                        && sourceRepository.existsById(sourceUuid)) {
                        removeIcon(resources, sourceUuid);
                        sourceRepository.deleteById(sourceUuid);
                    }
                }

                doDestroy(resources);
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Set harvester status to {@link Status#ACTIVE} and schedule the harvester to be ran
     * at the next time according to the harvesters schedule.
     *
     * @return return {@link OperResult#ALREADY_ACTIVE} if the harvester is already active or {@link OperResult#OK}
     */
    public OperResult start() throws SchedulerException {
        try {
            if (lock.tryLock(SHORT_WAIT, TimeUnit.SECONDS)) {
                if (status != Status.INACTIVE) {
                    return OperResult.ALREADY_ACTIVE;
                }
                harvesterSettingsManager.setValue("harvesting/id:" + id + "/options/status", Status.ACTIVE);

                status = Status.ACTIVE;
                error = null;

                doSchedule();

                return OperResult.OK;
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return OperResult.ERROR;
    }

    /**
     * Set the harvester status to {@link Status#ACTIVE} and unschedule any scheduled jobs.
     */
    public OperResult stop(final Status newStatus) throws SchedulerException {
        this.cancelMonitor.set(true);

        JobKey jobKey = jobKey(getParams().getUuid(), HARVESTER_GROUP_NAME);
        if (getScheduler().checkExists(jobKey)) {
            getScheduler().interrupt(jobKey);
        }

        try {
            if (lock.tryLock(LONG_WAIT, TimeUnit.SECONDS)) {
                this.running = false;

                harvesterSettingsManager.setValue("harvesting/id:" + id + "/options/status", newStatus);
                if (newStatus == Status.INACTIVE) {
                    if (this.status != Status.ACTIVE) {
                        return OperResult.ALREADY_INACTIVE;
                    }
                    doUnschedule();
                }

                this.status = newStatus;
                return OperResult.OK;
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");

                // Sometimes the harvester is frozen
                // give some time, but if it does not finish properly...
                // just kill it!!
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        //Wait again for proper shutdown
                        try {
                            Thread.sleep(LONG_WAIT * 1000);
                        } catch (InterruptedException e) {
                            log.error(e);
                        }

                        //Still running?
                        if (AbstractHarvester.this.running) {
                            //Then kill it!
                            log.error("Forcefully stopping harvester '" +
                                AbstractHarvester.this.getID() + "'.");
                            try {
                                AbstractHarvester.this.running = false;
                                harvesterSettingsManager.setValue("harvesting/id:" + id + "/options/status", newStatus);
                                AbstractHarvester.this.status = newStatus;

                                //Restart scheduling. Something went terribly wrong!
                                doUnschedule();
                                doSchedule();
                            } catch (SchedulerException e) {
                                log.error(e);
                            }
                        }
                    }
                }.start();
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return OperResult.ERROR;
    }

    /**
     * Call {@link #start()} if status is currently {@link Status#INACTIVE}.  Trigger a harvester job to run immediately.
     *
     * @return {@link OperResult#OK} or {@link OperResult#ALREADY_RUNNING} if harvester is currently running.
     */
    public OperResult run() throws SchedulerException {

        try {
            if (lock.tryLock(SHORT_WAIT, TimeUnit.SECONDS)) {
                if (status == Status.INACTIVE) {
                    start();
                }
                if (running) {
                    return OperResult.ALREADY_RUNNING;
                }
                getScheduler().triggerJob(jobKey(getParams().getUuid(), HARVESTER_GROUP_NAME));
                return OperResult.OK;
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return OperResult.ERROR;
    }

    /**
     * Run the harvester in the synchronously (in the current thread) and return whether the harvest correctly completed.
     *
     * @return {@link OperResult#OK} or {@link OperResult#ERROR}
     */
    public OperResult invoke() {
        try {
            if (lock.tryLock(SHORT_WAIT, TimeUnit.SECONDS)) {
                Status oldStatus = status;

                try {
                    status = Status.ACTIVE;
                    return harvest();
                } finally {
                    status = oldStatus;
                }
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return OperResult.ERROR;
    }

    public void update(Element node) throws BadInputEx, SQLException, SchedulerException {

        try {
            if (lock.tryLock(SHORT_WAIT, TimeUnit.SECONDS)) {
                doUpdate(id, node);

                if (status == Status.ACTIVE) {
                    //--- stop executor
                    doUnschedule();
                    //--- restart executor
                    error = null;
                    doSchedule();
                }
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public String getID() {
        return id;
    }

    /**
     * Adds harvesting result information to each harvesting entry.
     */
    public void addInfo(Element node) {
        Element info = node.getChild("info");

        info.addContent(new Element("running").setText(running + ""));

        //--- harvester specific info
        doAddInfo(node);

        //--- add error information
        if (error != null) {
            node.addContent(JeevesException.toElement(error));
        }
    }

    /**
     * Adds harvesting information to each metadata element. Some sites can generate url for thumbnails.
     */
    public void addHarvestInfo(Element info, String id, String uuid) {
        info.addContent(new Element("type").setText(getType()));
    }

    public ServiceContext getServiceContext() {
        return context;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Nested class to handle harvesting with fast indexing.
     */
    public class HarvestWithIndexProcessor extends MetadataIndexerProcessor {
        Logger logger;

        public HarvestWithIndexProcessor(DataManager dm, Logger logger) {
            super(dm);
            this.logger = logger;
        }

        @Override
        public void process() throws Exception {
            doHarvest(logger);
        }
    }

    /**
     * Create a session for the user who created the harvester. The owner identifier is added when the harvester config
     * is created or updated according to user session.
     */
    private void login() throws Exception {

        String ownerId = getParams().getOwnerId();
        if (log.isDebugEnabled()) {
            log.debug("AbstractHarvester login: ownerId = " + ownerId);
        }

        UserRepository repository = this.context.getBean(UserRepository.class);
        User user = null;
        if (StringUtils.isNotEmpty(ownerId)) {
            user = repository.findById(Integer.parseInt(ownerId)).get();
        }

        // for harvesters created before owner was added to the harvester code,
        // or harvesters belonging to a user that no longer exists
        if (user == null || StringUtils.isEmpty(ownerId) || !this.dataMan.existsUser(this.context, Integer.parseInt(ownerId))) {
            // just pick any Administrator (they can all see all harvesters and groups anyway)
            user = repository.findAllByProfile(Profile.Administrator).get(0);
            getParams().setOwnerId(String.valueOf(user.getId()));
            if (log.isDebugEnabled()) {
                log.debug("AbstractHarvester login: picked Administrator  " + ownerId + " to run this job");
            }
        }

        // todo reject if < useradmin ?

        UserSession session = new UserSession();
        session.loginAs(user);
        this.context.setUserSession(session);

        this.context.setIpAddress(null);
    }

    /**
     * Run the harvest process. This has to be protected or better for CGLib to proxy to it.
     */
    protected OperResult harvest() {
        OperResult operResult = OperResult.OK;
        Boolean releaseLock = false;
        try {
            if (lock.isHeldByCurrentThread() || (releaseLock = lock.tryLock(LONG_WAIT, TimeUnit.SECONDS))) {
                long startTime = System.currentTimeMillis();
                running = true;
                cancelMonitor.set(false);
                try {

                    String logfile = initializeLog();
                    this.log.info("Starting harvesting of " + this.getParams().getName());
                    error = null;
                    errors.clear();
                    final Logger logger = this.log;
                    final String nodeName = getParams().getName() + " (" + getClass().getSimpleName() + ")";
                    final String lastRun = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
                    try {
                        login();

                        //--- update lastRun
                        harvesterSettingsManager.setValue("harvesting/id:" + id + "/info/lastRun", lastRun);

                        //--- proper harvesting
                        logger.info("Started harvesting from node : " + nodeName);
                        HarvestWithIndexProcessor h = new HarvestWithIndexProcessor(dataMan, logger);
                        // todo check (was: processwithfastindexing)
                        h.process();
                        logger.info("Ended harvesting from node : " + nodeName);

                        if (getParams().isOneRunOnly()) {
                            stop(Status.INACTIVE);
                        }
                    } catch (InvalidParameterValueEx e) {
                        logger.error("The harvester " + this.getParams().getName() + "["
                            + this.getType()
                            + "] didn't accept some of the parameters sent.");

                        errors.add(new HarvestError(context, e));
                        error = e;
                        operResult = OperResult.ERROR;

                    } catch (UnknownHostException e) {
                        logger.error("The harvester " + this.getParams().getName() + "["
                            + this.getType()
                            + "] host is unknown.");

                        // Using custom UnknownHostEx as UnknownHostException error message differs from IPv4 / IPv6,
                        // in IPv4 only reports the host name in the message, the harvester uses this field
                        // and error reported to the user is unclear
                        error = new UnknownHostEx(e.getMessage());
                        errors.add(new HarvestError(context, error));

                        operResult = OperResult.ERROR;

                    } catch (Throwable t) {
                        operResult = OperResult.ERROR;
                        logger.warning("Raised exception while harvesting from : " + nodeName);
                        logger.warning(" (C) Class   : " + t.getClass().getSimpleName());
                        logger.warning(" (C) Message : " + t.getMessage());
                        logger.error(t);
                        error = t;
                        errors.add(new HarvestError(context, t));
                    } finally {
                        List<HarvestError> harvesterErrors = getErrors();
                        if (harvesterErrors != null) {
                            errors.addAll(harvesterErrors);
                        }
                    }

                    long elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);

                    logHarvest(logfile, logger, nodeName, lastRun, elapsedTime);
                } finally {
                    cancelMonitor.set(false);
                    running = false;
                }
            } else {
                log.error("Harvester '" + this.getID() + "' looks deadlocked.");
                log.error("Harvester '" + this.getID() + "' hasn't initiated.");
                operResult = OperResult.ERROR;
            }
        } catch (InterruptedException e) {
            log.error(e);
        } finally {
            if (lock.isHeldByCurrentThread() && releaseLock) {
                lock.unlock();
            }
        }

        return operResult;


    }

    private void logHarvest(String logfile, Logger logger, String nodeName, String lastRun, long elapsedTime) {
        try {
            // record the results/errors for this harvest in the database
            Element result = getResult();
            if (error != null) {
                result = JeevesException.toElement(error);
            }
            Element priorLogfile_ = result.getChild("logfile");
            if (priorLogfile_ != null) {
                // removing prior logfile
                logger.warning("Detected duplicate logfile: " + priorLogfile_.getText());
                result.getChildren().remove(priorLogfile_);
            }
            Element logfile_ = new Element("logfile");
            logfile_.setText(logfile);

            result.addContent(logfile_);

            result.addContent(toElement(errors));
            final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);
            final HarvestHistory history = new HarvestHistory()
                .setHarvesterType(getType())
                .setHarvesterName(getParams().getName())
                .setHarvesterUuid(getParams().getUuid())
                .setElapsedTime((int) elapsedTime)
                .setHarvestDate(new ISODate(lastRun))
                .setParams(getParams().getNodeElement())
                .setInfo(result);
            historyRepository.save(history);


            //Send notification email, if needed
            try {
                SendNotification.process(context, history.asXml(), this);
            } catch (Exception e2) {
                logger.error("Raised exception while attempting to send email");
                logger.error(" (C) Exc   : " + e2);
                logger.error(e2);
            }

        } catch (Exception e) {
            logger.warning("Raised exception while attempting to store harvest history from : " + nodeName);
            logger.warning(" (C) Exc   : " + e.getMessage());
            logger.error(e);
        }
    }


    /**
     * Convert {@link HarvestError} to an element that can be saved on the database.
     */
    private Element toElement(List<HarvestError> errors) {
        Element res = new Element("errors");
        for (HarvestError error : errors) {
            Element herror = new Element("error");

            Element desc = new Element("description");
            desc.setText(error.getDescription());
            herror.addContent(desc);

            Element hint = new Element("hint");
            hint.setText(error.getHint());
            herror.addContent(hint);

            herror.addContent(JeevesException.toElement(error.getOrigin()));
            res.addContent(herror);
        }
        return res;
    }

    /**
     * Should be overriden to get a better insight on harvesting
     * <p/>
     * Returns the list of exceptions that ocurred during the harvesting but
     * didn't really stop and abort the harvest.
     */
    public List<HarvestError> getErrors() {
        return Collections.synchronizedList(errors);
    }

    public final String getType() {
        // FIXME: context is null when removing record
        // eg. http://localhost:8080/geonetwork/node1/eng/admin.harvester.clear@json?id=585
        final String[] types = context.getApplicationContext().getBeanNamesForType(getClass());
        return types[0];
    }

    public P getParams() {
        return params;
    }

    private void doInit(Element node) throws BadInputEx {
        setParams(createParams());
        params.create(node);
    }

    private void doDestroy(final Resources resources) {
        removeIcon(resources, getParams().getUuid());

        context.getBean(SourceRepository.class).deleteById(getParams().getUuid());
        // FIXME: Should also delete the categories we have created for servers
    }

    private void removeIcon(final Resources resources, String uuid) {

        try {
            resources.deleteImageIfExists(uuid + ".gif", resources.locateLogosDir(context));
        } catch (IOException e) {
            Log.warning(Geonet.HARVESTER + "." + getType(), "Unable to delete icon: " + uuid, e);
        }
    }

    private final String doAdd(Element node) throws BadInputEx, SQLException {
        params = createParams();

        //--- retrieve/initialize information
        params.create(node);

        //--- force the creation of a new uuid
        params.setUuid(UUID.randomUUID().toString());

        String id = harvesterSettingsManager.add("harvesting", "node", getType());
        storeNode(params, "id:" + id);

        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), SourceType.harvester);
        context.getBean(SourceRepository.class).save(source);
        final String icon = params.getIcon();
        if (icon != null) {
            context.getBean(Resources.class)
                .copyLogo(context, "images" + File.separator + "harvesting" + File.separator + icon, params.getUuid());
        }

        return id;
    }

    private void doUpdate(String id, Element node) throws BadInputEx, SQLException {
        @SuppressWarnings("unchecked")
        P copy = (P) params.copy();
        //--- update variables
        copy.update(node);
        String path = "harvesting/id:" + id;
        harvesterSettingsManager.removeChildren(path);
        //--- update database
        storeNode(copy, path);
        //--- we update a copy first because if there is an exception CswParams
        //--- could be half updated and so it could be in an inconsistent state
        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), SourceType.harvester);
        context.getBean(SourceRepository.class).save(source);
        final String icon = copy.getIcon();
        if (icon != null) {
            context.getBean(Resources.class)
                .copyLogo(context, "images" + File.separator + "harvesting" + File.separator + icon, copy.getUuid());
        }

        setParams(copy);
    }

    protected void doAddInfo(Element node) {
        //--- if the harvesting is not started yet, we don't have any info

        if (result == null && this.loadedInfo == null) {
            return;
        }

        //--- ok, add proper info

        Element info = node.getChild("info");
        Element res = getResult();
        info.addContent(res);
    }

    /**
     * Extend to do the actual harvesting.
     */
    protected abstract void doHarvest(Logger l) throws Exception;


    /**
     * Invoked from doAdd and doUpdate.
     */
    private void storeNode(P params, String path) throws SQLException {
        String siteId = harvesterSettingsManager.add(path, "site", "");
        String translations = harvesterSettingsManager.add(ID_PREFIX + siteId, AbstractParams.TRANSLATIONS, "");
        String optionsId = harvesterSettingsManager.add(path, "options", "");
        String infoId = harvesterSettingsManager.add(path, "info", "");
        String contentId = harvesterSettingsManager.add(path, "content", "");

        //--- setup site node ----------------------------------------

        harvesterSettingsManager.add(ID_PREFIX + siteId, "name", params.getName());
        for (Map.Entry<String, String> entry : params.getTranslations().entrySet()) {
            harvesterSettingsManager.add(ID_PREFIX + translations, entry.getKey(), entry.getValue());
        }
        harvesterSettingsManager.add(ID_PREFIX + siteId, "uuid", params.getUuid());

        /* User who created or updated this node. */
        harvesterSettingsManager.add(ID_PREFIX + siteId, "ownerId", params.getOwnerId());
        /* User selected by user who created or updated this node. */
        harvesterSettingsManager.add(ID_PREFIX + siteId, "ownerUser", params.getOwnerIdUser());
        /* Group selected by user who created or updated this node. */
        harvesterSettingsManager.add(ID_PREFIX + siteId, "ownerGroup", params.getOwnerIdGroup());

        String useAccId = harvesterSettingsManager.add(ID_PREFIX + siteId, "useAccount", params.isUseAccount());

        harvesterSettingsManager.add(ID_PREFIX + useAccId, "username", params.getUsername());
        harvesterSettingsManager.add(ID_PREFIX + useAccId, "password", params.getPassword(), true);

        //--- setup options node ---------------------------------------

        harvesterSettingsManager.add(ID_PREFIX + optionsId, "every", params.getEvery());
        harvesterSettingsManager.add(ID_PREFIX + optionsId, "oneRunOnly", params.isOneRunOnly());
        harvesterSettingsManager.add(ID_PREFIX + optionsId, "overrideUUID", params.getOverrideUuid());
        harvesterSettingsManager.add(ID_PREFIX + optionsId, "ifRecordExistAppendPrivileges", params.isIfRecordExistAppendPrivileges());
        harvesterSettingsManager.add(ID_PREFIX + optionsId, "status", status);

        //--- setup content node ---------------------------------------

        harvesterSettingsManager.add(ID_PREFIX + contentId, "importxslt", params.getImportXslt());
        harvesterSettingsManager.add(ID_PREFIX + contentId, "batchEdits", params.getBatchEdits());
        harvesterSettingsManager.add(ID_PREFIX + contentId, "validate", params.getValidate());

        //--- setup stats node ----------------------------------------

        harvesterSettingsManager.add(ID_PREFIX + infoId, "lastRun", "");

        //--- store privileges and categories ------------------------

        storePrivileges(params, path);
        storeCategories(params, path);

        storeNodeExtra(params, path, siteId, optionsId);
    }

    /**
     * Override this method with an empty body to avoid privileges storage.
     */
    private void storePrivileges(P params, String path) {
        String privId = harvesterSettingsManager.add(path, "privileges", "");

        for (Privileges p : params.getPrivileges()) {
            String groupId = harvesterSettingsManager.add(ID_PREFIX + privId, "group", p.getGroupId());
            for (int oper : p.getOperations()) {
                harvesterSettingsManager.add(ID_PREFIX + groupId, "operation", oper);
            }
        }
    }

    /**
     * Override this method with an empty body to avoid categories storage.
     */
    private void storeCategories(P params, String path) {
        String categId = harvesterSettingsManager.add(path, "categories", "");

        for (String id : params.getCategories()) {
            harvesterSettingsManager.add(ID_PREFIX + categId, "category", id);
        }
    }

    /**
     * Override this method to store harvesting node's specific settings.
     */
    protected void storeNodeExtra(P params, String path, String siteId, String optionsId) throws SQLException {
    }

    protected void setValue(Map<String, Object> values, String path, Element el, String name) {
        if (el == null) {
            return;
        }

        String value = el.getChildText(name);

        if (value != null) {
            values.put(path, value);
        }
    }

    protected void add(Element el, String name, int value) {
        el.addContent(new Element(name).setText(Integer.toString(value)));
    }

    public void setParams(P params) {
        this.params = params;
    }

    /**
     * Get the results of the last harvest.
     */
    public Element getResult() {
        Element res = new Element("result");
        if (result != null) {
            this.loadedInfo = null;
            add(res, "added", result.addedMetadata);
            add(res, "atomicDatasetRecords", result.atomicDatasetRecords);
            add(res, "badFormat", result.badFormat);
            add(res, "collectionDatasetRecords", result.collectionDatasetRecords);
            add(res, "datasetUuidExist", result.datasetUuidExist);
            add(res, "privilegesAppendedOnExistingRecord", result.privilegesAppendedOnExistingRecord);
            add(res, "doesNotValidate", result.doesNotValidate);
            add(res, "xpathFilterExcluded", result.xpathFilterExcluded);
            add(res, "duplicatedResource", result.duplicatedResource);
            add(res, "fragmentsMatched", result.fragmentsMatched);
            add(res, "fragmentsReturned", result.fragmentsReturned);
            add(res, "fragmentsUnknownSchema", result.fragmentsUnknownSchema);
            add(res, "incompatible", result.incompatibleMetadata);
            add(res, "recordsBuilt", result.recordsBuilt);
            add(res, "recordsUpdated", result.recordsUpdated);
            add(res, "removed", result.locallyRemoved);
            add(res, "serviceRecords", result.serviceRecords);
            add(res, "subtemplatesAdded", result.subtemplatesAdded);
            add(res, "subtemplatesRemoved", result.subtemplatesRemoved);
            add(res, "subtemplatesUpdated", result.subtemplatesUpdated);
            add(res, "total", result.totalMetadata);
            add(res, "unchanged", result.unchangedMetadata);
            add(res, "unknownSchema", result.unknownSchema);
            add(res, "unretrievable", result.unretrievable);
            add(res, "updated", result.updatedMetadata);
            add(res, "thumbnails", result.thumbnails);
            add(res, "thumbnailsFailed", result.thumbnailsFailed);
        } else if (this.loadedInfo != null) {
            return (Element) this.loadedInfo.clone();
        }
        return res;
    }

    public void emptyResult() {
        result = null;
        this.loadedInfo = null;
    }

    /**
     * Get the list of registered harvester
     */
    public static String[] getHarvesterTypes(ServiceContext context) {
        return context.getApplicationContext().getBeanNamesForType(AbstractHarvester.class);
    }

    /**
     * Get the list of not disabled registered harvesters
     */
    public static String[] getNonDisabledHarvesterTypes(ServiceContext context) {
        String[] availableTypes = context.getApplicationContext().getBeanNamesForType(AbstractHarvester.class);
        SettingManager localSettingManager = context.getBean(SettingManager.class);
        String disabledTypesString = StringUtils.defaultIfBlank(localSettingManager.getValue(Settings.SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES), "");
        String[] disabledTypes = StringUtils.split(disabledTypesString.toLowerCase().replace(',', ' '), " ");

        return Arrays.stream(availableTypes)
            .filter(type -> Arrays.stream(disabledTypes).noneMatch(type::equalsIgnoreCase))
            .collect(Collectors.toList()).toArray(new String[]{});
    }

    /**
     * Check if the harvester's type is in the list of disabled harvesters.
     *
     * @return <code>true</code> if the harvester's type is disabled in the settings, <code>false</code> otherwise.
     */
    public boolean isHarvesterTypeDisabled() {
        String[] disabledTypes = StringUtils.split(
            StringUtils.defaultIfBlank(
                settingManager.getValue(Settings.SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES),
                "").toLowerCase().replace(',', ' '),
            " ");
        String type = getType();
        return Arrays.stream(disabledTypes).anyMatch(disabledType -> disabledType.equalsIgnoreCase(type));
    }

    /**
     * Who should we notify by default?
     */
    public String getOwnerEmail() {
        String ownerId = getParams().getOwnerIdGroup();

        final Group group = context.getBean(GroupRepository.class).findById(Integer.parseInt(ownerId)).get();
        return group.getEmail();
    }

    protected abstract P createParams();
}
