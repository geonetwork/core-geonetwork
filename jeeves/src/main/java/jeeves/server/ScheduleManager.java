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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import jeeves.constants.ConfigFile;
import jeeves.exceptions.JeevesException;
import jeeves.interfaces.Schedule;
import jeeves.server.context.ScheduleContext;
import jeeves.server.resources.ProviderManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Util;

import org.jdom.Element;

//=============================================================================

public class ScheduleManager extends Thread
{
	private boolean exit = false;

	private String appPath;
	private String baseUrl;

	private ProviderManager providMan;
	private SerialFactory   serialFact;

	private Vector<ScheduleInfo> vSchedules = new Vector<ScheduleInfo>();
	private Hashtable<String, Object> htContexts = new Hashtable<String, Object>();

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

	public void setProviderMan  (ProviderManager p) { providMan  = p; }
	public void setSerialFactory(SerialFactory   s) { serialFact = s; }

	public void setAppPath(String  path)  { appPath = path;  }

	//--------------------------------------------------------------------------

	public void addSchedule(String pack, Element sched) throws Exception
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

		si.name     = name;
		si.schedule = schedule;
		si.period   = getPeriod(when);
		si.counter  = si.period;

		vSchedules.add(si);
	}

	//--------------------------------------------------------------------------

	public void exit()
	{
		exit = true;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Main loop
	//---
	//--------------------------------------------------------------------------

	public void run()
	{
		while(!exit)
		{
			doJob();

			try
			{
				sleep(1000);
			}
			catch (InterruptedException e) {}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** This method is called every second */

	private void doJob()
	{
		for (ScheduleInfo si : vSchedules) {
			if (--si.counter <= 0) {
				si.counter = si.period;
				executeSchedule(si);
			}
		}
	}

	//--------------------------------------------------------------------------

	private void executeSchedule(ScheduleInfo si)
	{
		//--- create the corresponding schedule context

		ScheduleContext context = new ScheduleContext(si.name, providMan, serialFact, htContexts);

		context.setBaseUrl(baseUrl);
		context.setAppPath(appPath);

		try
		{
			si.schedule.exec(context);
			context.getResourceManager().close();
			return;
		}

		catch(JeevesException e)
		{
			error("Communication exception while executing schedule : "+ si.name);
			error(" (C) Status  : "+e.getId());
			error(" (C) Message : "+e.getMessage());

			if (e.getObject() != null)
				error(" (C) Object  : "+e.getObject());
		}

		catch (Exception e)
		{
			error("Raised exception when executing schedule : "+ si.name);
			error(" (C) Stack trace : "+ Util.getStackTrace(e));
		}

		//--- in case of exception we have to abort all resources

		abort(context);
	}

	//--------------------------------------------------------------------------

	private void abort(ScheduleContext context)
	{
		try
		{
			context.getResourceManager().abort();
		}
		catch (Exception ex)
		{
			error("CANNOT ABORT PREVIOUS EXCEPTION");
			error(" (C) Exc : " + ex);
		}
	}

	//--------------------------------------------------------------------------

	private int getPeriod(String when)
	{
		int period = 0;
		int mult   = 0;

		StringTokenizer st = new StringTokenizer(when, ",");

		while (st.hasMoreTokens())
		{
			String token = st.nextToken().trim().toLowerCase();

			if (token.endsWith(" hour"))
			{
				token = token.substring(0, token.length() -5);
				mult  = 3600;
			}

			else if (token.endsWith(" hours"))
			{
				token = token.substring(0, token.length() -6);
				mult  = 3600;
			}

			else if (token.endsWith(" min"))
			{
				token = token.substring(0, token.length() -4);
				mult  = 60;
			}

			else if (token.endsWith(" sec"))
			{
				token = token.substring(0, token.length() -4);
				mult  = 1;
			}

			else
				throw new IllegalArgumentException("Bad period format :" +when);

			period += mult * Integer.parseInt(token);
		}

		return period;
	}

	//---------------------------------------------------------------------------

	private void error  (String message) { Log.error  (Log.SCHEDULER, message); }
}

//=============================================================================

class ScheduleInfo
{
	public String   name;
	public Schedule schedule;

	public int period;
	public int counter;
}

//=============================================================================

