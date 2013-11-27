//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.constants.ConfigFile;
import jeeves.interfaces.Schedule;
import jeeves.monitor.MonitorManager;
import org.fao.geonet.utils.QuartzSchedulerUtils;
import org.jdom.Element;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

//=============================================================================
public class ScheduleManager
{
    /**
     * Id for the Quartz scheduler for the Schedule jobs.
     */
    public static final String SCHEDULER_ID = "scheduleManager";

	private String appPath;
	private String baseUrl;
	private String instanceId = SCHEDULER_ID+"-"+UUID.randomUUID().toString();
	private Hashtable<String, Object> htContexts = new Hashtable<String, Object>();
    private Scheduler scheduler;
    private Map<String, ScheduleInfo> vSchedules = new HashMap<String, ScheduleInfo>();

    @Autowired
    private ConfigurableApplicationContext jeevesApplicationContext;
    @PersistenceContext
    private EntityManager entityManager;

    //--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public ScheduleManager() {}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void registerContext(String name, Object context)
	{
		htContexts.put(name, context);
	}

	//---------------------------------------------------------------------------

	public void setBaseUrl(String name)
	{
		baseUrl = name;

		if (!baseUrl.startsWith("/"))
			baseUrl = "/"+ baseUrl;
	}

	//--------------------------------------------------------------------------

	public void setAppPath(String  path)  { appPath = path;  }

    //--------------------------------------------------------------------------

    public String getAppPath() { return appPath;}
    public String getBaseUrl() {return baseUrl;}
    public ConfigurableApplicationContext getApplicationContext() { return jeevesApplicationContext; }
    public Hashtable<String, Object> getHtContexts() {return new Hashtable<String, Object>(htContexts);}

    //--------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
   void addSchedule(String pack, Element sched) throws Exception
	{
		String name = sched.getAttributeValue(ConfigFile.Schedule.Attr.NAME);
		String clas = sched.getAttributeValue(ConfigFile.Schedule.Attr.CLASS);
		String when = sched.getAttributeValue(ConfigFile.Schedule.Attr.WHEN);

		//--- get class name

		if (clas == null)
			throw new IllegalArgumentException("Missing 'class' attrib in 'schedule' element");

		if (clas.startsWith("."))
			clas = pack + clas;

		//--- create instance

		Schedule schedule = (Schedule) Class.forName(clas).newInstance();

		schedule.init(appPath, new ServiceConfig(sched.getChildren(ConfigFile.Schedule.Child.PARAM)));

		//--- store schedule

		ScheduleInfo si = new ScheduleInfo();

        si.name = name;
        si.schedule = schedule;
        si.job = newJob(ScheduleJob.class).withIdentity(name, instanceId).usingJobData(ScheduleJob.NAME_FIELD_NAME, name).build();
        si.trigger = QuartzSchedulerUtils.getTrigger(name, instanceId, when, Integer.MAX_VALUE);

		vSchedules.put(name, si);
	}

	//--------------------------------------------------------------------------

    public void start() throws SchedulerException {
        scheduler = QuartzSchedulerUtils.getScheduler(SCHEDULER_ID, true);
        scheduler.getListenerManager().addJobListener(new ScheduleListener(this), jobGroupEquals(instanceId));

        for (ScheduleInfo info : vSchedules.values()) {
            scheduler.scheduleJob(info.job, info.trigger);
        }
    }

	public void exit() throws SchedulerException
	{
        if (scheduler != null) {
		    scheduler.shutdown();
        }
	}

	//--------------------------------------------------------------------------

    public ScheduleInfo getScheduleInfo(String scheduleName) {
        return vSchedules.get(scheduleName);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    //---------------------------------------------------------------------------


}

//=============================================================================

class ScheduleInfo
{
	public Schedule schedule;
    public JobDetail job;
    public Trigger trigger;
    public String   name;
}

//=============================================================================

