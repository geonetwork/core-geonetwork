//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.JeevesException;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet20.Geonet20Harvester;
import org.fao.geonet.kernel.harvest.harvester.webdav.WebDavHarvester;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

public abstract class AbstractHarvester
{
	//---------------------------------------------------------------------------
	//---
	//--- Static API methods
	//---
	//---------------------------------------------------------------------------

	public static void staticInit(ServiceContext context) throws Exception
	{
		register(context, GeonetHarvester  .class);
		register(context, WebDavHarvester  .class);
		register(context, Geonet20Harvester.class);
	}

	//---------------------------------------------------------------------------

	private static void register(ServiceContext context, Class harvester) throws Exception
	{
		try
		{
			Method initMethod = harvester.getMethod("init", context.getClass());
			initMethod.invoke(null, context);

			AbstractHarvester ah = (AbstractHarvester) harvester.newInstance();

			hsHarvesters.put(ah.getType(), harvester);
		}
		catch(Exception e)
		{
			throw new Exception("Cannot register harvester : "+harvester, e);
		}
	}

	//---------------------------------------------------------------------------

	public static AbstractHarvester create(String type, ServiceContext context,
														SettingManager sm, DataManager dm)
														throws BadParameterEx, OperationAbortedEx
	{
		//--- raises an exception if type is null

		if (type == null)
			throw new BadParameterEx("type", type);

		Class c = hsHarvesters.get(type);

		if (c == null)
			throw new BadParameterEx("type", type);

		try
		{
			AbstractHarvester ah = (AbstractHarvester) c.newInstance();

			ah.context    = context;
			ah.settingMan = sm;
			ah.dataMan    = dm;

			return ah;
		}
		catch(Exception e)
		{
			throw new OperationAbortedEx("Cannot instantiate harvester", e);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public void add(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		name = node.getAttributeValue("name");

		if (name == null)
		{
			name = "";
			node.setAttribute("name", name);
		}

		id       = doAdd(dbms, node);
		name     = node.getAttributeValue("name");
		status   = Status.INACTIVE;
		executor = null;
		error    = null;
	}

	//--------------------------------------------------------------------------

	public void init(Element node) throws BadInputEx
	{
		id       = node.getAttributeValue("id");
		name     = node.getAttributeValue("name");
		status   = Status.parse(node.getChild("options").getChildText("status"));
		executor = null;
		error    = null;

		//--- init harvester

		doInit(node);

		if (status == Status.ACTIVE)
		{
			executor = new Executor(this);
			executor.setTimeout(doGetEvery());
			executor.start();
		}
	}

	//--------------------------------------------------------------------------

	public synchronized void destroy(Dbms dbms) throws Exception
	{
		if (executor != null)
			executor.terminate();

		executor = null;
		doDestroy(dbms);
	}

	//--------------------------------------------------------------------------

	public synchronized OperResult start(Dbms dbms) throws SQLException
	{
		if (status != Status.INACTIVE)
			return OperResult.ALREADY_ACTIVE;

		settingMan.setValue(dbms, "harvesting/id:"+id+"/options/status", Status.ACTIVE);

		status     = Status.ACTIVE;
		error      = null;
		executor   = new Executor(this);
		executor.setTimeout(doGetEvery());
		executor.start();

		return OperResult.OK;
	}

	//--------------------------------------------------------------------------

	public synchronized OperResult stop(Dbms dbms) throws SQLException
	{
		if (status != Status.ACTIVE)
			return OperResult.ALREADY_INACTIVE;

		settingMan.setValue(dbms, "harvesting/id:"+id+"/options/status", Status.INACTIVE);

		executor.terminate();
		status   = Status.INACTIVE;
		executor = null;

		return OperResult.OK;
	}

	//--------------------------------------------------------------------------

	public synchronized OperResult run()
	{
		if (status == Status.INACTIVE)
			return OperResult.INACTIVE;

		if (executor.isRunning())
			return OperResult.ALREADY_RUNNING;

		executor.interrupt();

		return OperResult.OK;
	}

	//--------------------------------------------------------------------------

	public synchronized void update(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		//--- update name

		if (node.getAttributeValue("name") != null)
			name = node.getAttributeValue("name");

		//--- update other fields

		doUpdate(dbms, id, node);

		if (status == Status.ACTIVE)
		{
			//--- stop executor
			executor.terminate();

			//--- restart executor
			error      = null;
			executor   = new Executor(this);
			executor.setTimeout(doGetEvery());
			executor.start();
		}
	}

	//--------------------------------------------------------------------------

	public String getID()   { return id;   }
	public String getName() { return name; }

	//--------------------------------------------------------------------------

	public void addInfo(Element node)
	{
		Element info = node.getChild("info");

		//--- 'running'

		if (status == Status.ACTIVE && executor.isRunning())
			info.addContent(new Element("running").setText("true"));

		//--- harvester specific info

		doAddInfo(node);

		//--- add error information

		if (error != null)
			node.addContent(JeevesException.toElement(error));
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package methods (called by Executor)
	//---
	//---------------------------------------------------------------------------

	void harvest()
	{
		ResourceManager rm = new ResourceManager(context.getProviderManager());

		Logger logger = Log.createLogger(Geonet.HARVESTER);

		String nodeName = name + " ("+ getClass().getSimpleName() +")";

		error = null;

		try
		{
			Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

			//--- update lastRun

			String lastRun = new ISODate(System.currentTimeMillis()).toString();
			settingMan.setValue(dbms, "harvesting/id:"+ id +"/info/lastRun", lastRun);

			//--- proper harvesting

			logger.info("Started harvesting from node : "+ nodeName);
			doHarvest(logger, rm);
			logger.info("Ended harvesting from node : "+ nodeName);

			if (doIsOneRunOnly())
				stop(dbms);

			rm.close();
		}
		catch(Throwable t)
		{
			logger.warning("Raised exception while harvesting from : "+ nodeName);
			logger.warning(" (C) Class   : "+ t.getClass().getSimpleName());
			logger.warning(" (C) Message : "+ t.getMessage());

			error = t;
			t.printStackTrace();

			try
			{
				rm.abort();
			}
			catch (Exception ex)
			{
				logger.warning("CANNOT ABORT EXCEPTION");
				logger.warning(" (C) Exc : "+ ex);
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Abstract methods that must be overridden
	//---
	//---------------------------------------------------------------------------

	public abstract String getType();

	protected abstract void doInit(Element entry) throws BadInputEx;

	/** Called when the harvesting entry is removed from the system.
	  * It is used to remove harvested metadata.
	  */

	protected abstract void doDestroy(Dbms dbms) throws Exception;

	protected abstract String doAdd(Dbms dbms, Element node)
											throws BadInputEx, SQLException;

	protected abstract void doUpdate(Dbms dbms, String id, Element node)
											throws BadInputEx, SQLException;

	protected abstract int doGetEvery();

	protected abstract boolean doIsOneRunOnly();

	protected abstract void doAddInfo(Element node);
	protected abstract void doHarvest(Logger l, ResourceManager rm) throws Exception;

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected void setValue(Map<String, Object> values, String path, Element el, String name)
	{
		if (el == null)
			return ;

		String value = el.getChildText(name);

		if (value != null)
			values.put(path, value);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String id;
	private String name;
	private Status status;

	private Executor  executor;
	private Throwable error;

	protected ServiceContext context;
	protected SettingManager settingMan;
	protected DataManager    dataMan;

	private static Map<String, Class> hsHarvesters = new HashMap<String, Class>();
}

//=============================================================================


