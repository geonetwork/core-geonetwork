/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.jdom.Element;
import org.quartz.SchedulerException;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * API for adding/removing/running harvesters.
 *
 * User: Jesse Date: 10/11/13 Time: 9:49 AM
 */
public interface HarvestManager {
    /**
     * Set required information at startup of system.
     *
     * @param context    a context object for accessing required objects and information
     * @param isReadOnly indicator if system is readonly
     */
    void init(@Nonnull ServiceContext context, boolean isReadOnly) throws Exception;

    /**
     * Refresh the harvesters.
     * - Stops the existing scheduled jobs.
     * - Reloads the harvester definitions from the database.
     * - Schedule the jobs again.
     * <p>
     * Useful when running multiple instances as one instance is not aware of new harvesters added on the other one.
     * <p>
     * TODO when a durable solution for running highly available is implemented this scheduled task could be deleted, e.g., when created harvesters are notified through a shared message queue.
     */
    void refreshHarvesters();

    /**
     * Shutdown all harvesters and thread pools and jobs.
     */
    void shutdown();

    /**
     * Get the configuration element for the identified harvester.
     *
     * @param id      the id of the harvester in question or null for all harvesters
     * @param context context object
     * @param sort    how to sort the harvesters (or null.
     * @return the configuration element for the identified harvester.
     */
    @Nullable
    Element get(@Nullable String id, @Nonnull ServiceContext context, @Nullable String sort) throws Exception;

    /**
     * Add a new harvester.
     *
     * @param node    harvester config
     * @param ownerId the id of the user doing this
     * @return id of new harvester
     */
    String addHarvesterReturnId(Element node, String ownerId) throws JeevesException, SQLException;


    /**
     * Add a new harvester.
     *
     * @param node harvester config
     * @return id of new harvester
     */
    String addHarvesterReturnUUID(Element node) throws JeevesException, SQLException;

    /**
     * Create a new harvester by cloning the identified harvester.
     *
     * @param id      id of harvester to clone
     * @param ownerId the owner of the new harvester
     * @param context a context object
     * @return a new harvester by cloning the identified harvester.
     */
    String createClone(String id, String ownerId, ServiceContext context) throws Exception;

    /**
     * Update a harvester's configuration
     *
     * @param node    the new configuration
     * @param ownerId the id of the user that is making the change.
     * @return true if a harvester was found and updated, false if not.
     */
    boolean update(Element node, String ownerId) throws BadInputEx, SQLException, SchedulerException;

    /**
     * Reindexes all metadata associated to one harvester.
     *
     * @param id of the harvester
     */
    Common.OperResult reindexBatch(String id) throws Exception;

    /**
     * Remove all metadata associated to one harvester
     *
     * @param id of the harvester
     */
    Common.OperResult clearBatch(String id) throws Exception;

    Common.OperResult remove(String id) throws Exception;

    /**
     * Set harvester status to {@link org.fao.geonet.kernel.harvest.Common.Status#ACTIVE} and
     * schedule the harvester to be ran at the next time according to the harvesters schedule.
     *
     * @return return {@link org.fao.geonet.kernel.harvest.Common.OperResult#ALREADY_ACTIVE} if the
     * harvester is already active or {@link org.fao.geonet.kernel.harvest.Common.OperResult#OK}
     */
    Common.OperResult start(String id) throws SQLException, SchedulerException;

    /**
     * Set the harvester status to the provided status and unschedule any scheduled jobs.
     *
     * @return {@link org.fao.geonet.kernel.harvest.Common.OperResult#ALREADY_INACTIVE} if the not
     * currently enabled or {@link org.fao.geonet.kernel.harvest.Common.OperResult#OK}
     */
    Common.OperResult stop(String id, Common.Status status) throws SQLException, SchedulerException;

    /**
     * Call {@link AbstractHarvester#start()} if status is currently {@link
     * org.fao.geonet.kernel.harvest.Common.Status#INACTIVE}.  Trigger a harvester job to run
     * immediately.
     *
     * @return {@link org.fao.geonet.kernel.harvest.Common.OperResult#OK} or {@link
     * org.fao.geonet.kernel.harvest.Common.OperResult#ALREADY_RUNNING} if harvester is currently
     * running.
     */
    Common.OperResult run(String id) throws SQLException, SchedulerException;

    /**
     * Run the harvester in the synchronously (in the current thread) and return whether the harvest
     * correctly completed.
     *
     * @return {@link org.fao.geonet.kernel.harvest.Common.OperResult#OK} or {@link
     * org.fao.geonet.kernel.harvest.Common.OperResult#ERROR}
     */
    Common.OperResult invoke(String id);

    /**
     * Get the harvester instance for the given harvester uuid.
     *
     * @param harvestUuid uuid of the harvester to look up.
     * @return the harvester instance for the given harvester uuid.
     */
    AbstractHarvester getHarvester(String harvestUuid);

    boolean isReadOnly();

    void setReadOnly(boolean readOnly);

    ConfigurableApplicationContext getApplicationContext();

    /**
     * Reschedule the active harvesters if their timezone is different than the one in
     * {@link org.fao.geonet.kernel.setting.Settings#SYSTEM_SERVER_TIMEZONE}.
     *
     */
    void rescheduleActiveHarvesters();
}
