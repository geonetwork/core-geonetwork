//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester;

import java.sql.SQLException;
import java.util.Map;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ProviderManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.Common.Type;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetHarvester;
import org.fao.geonet.kernel.harvest.harvester.webfolder.WAFHarvester;
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

	public static AbstractHarvester create(Type type, SettingManager sm, ProviderManager pm)
	{
		AbstractHarvester ah = null;

		if (type == Type.GEONETWORK)
			ah = new GeonetHarvester();

		else if (type == Type.WEB_FOLDER)
			ah = new WAFHarvester();

		if (ah == null)
			throw new IllegalArgumentException("Unknown type : "+ type);

		ah.settingMan = sm;
		ah.providMan  = pm;

		return ah;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public String add(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		name = node.getAttributeValue("name");

		if (name == null)
		{
			name = "";
			node.setAttribute("name", name);
		}

		String id = doAdd(dbms, node);

		status   = Status.INACTIVE;
		executor = null;
		error    = null;

		return id;
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
			executor.setTimeout(getEvery());
			executor.start();
		}
	}

	//--------------------------------------------------------------------------

	public synchronized void destroy()
	{
		if (executor != null)
			executor.terminate();

		executor = null;
	}

	//--------------------------------------------------------------------------

	public synchronized void start(Dbms dbms) throws SQLException
	{
		if (status != Status.INACTIVE)
			return;

		settingMan.setValue(dbms, "harvesting/id:"+id+"/options/status", Status.ACTIVE);

		status     = Status.ACTIVE;
		error      = null;
		executor   = new Executor(this);
		executor.setTimeout(getEvery());
		executor.start();
	}

	//--------------------------------------------------------------------------

	public synchronized void stop(Dbms dbms) throws SQLException
	{
		if (status != Status.ACTIVE)
			return;

		settingMan.setValue(dbms, "harvesting/id:"+id+"/options/status", Status.INACTIVE);

		executor.terminate();
		status   = Status.INACTIVE;
		executor = null;
	}

	//--------------------------------------------------------------------------

	public synchronized boolean run()
	{
		if (status == Status.INACTIVE)
			return false;

		if (!executor.isRunning())
			executor.interrupt();

		return true;
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
			executor.setTimeout(getEvery());
	}

	//--------------------------------------------------------------------------

	public String getID() { return id; }

	//--------------------------------------------------------------------------

	public void addInfo(Element node)
	{
		Element info = node.getChild("info");

		//--- 'running'

		if (status == Status.ACTIVE && executor.isRunning())
			info.addContent(new Element("running").setText("true"));

		//--- harvester specific info

		doAddInfo(info);

		//--- add error information

		if (error != null)
			node.addContent(error.getErrorElement());
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package methods (called by Executor)
	//---
	//---------------------------------------------------------------------------

	void harvest()
	{
		ResourceManager rm = new ResourceManager(providMan);

		try
		{
			Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

			//--- update lastRun

			String lastRun = new ISODate(System.currentTimeMillis()).toString();
			settingMan.setValue(dbms, "harvesting/id:"+ id +"/info/lastRun", lastRun);

			//--- proper harvesting

			String nodeName = name + " ("+ getClass().getSimpleName() +")";

			Logger logger = Log.createLogger(Geonet.HARVESTER);

			logger.info("Started harvesting from node : "+ nodeName);
			doHarvest(logger, rm);
			logger.info("Ended harvesting from node : "+ nodeName);

			if (doIsOneRunOnly())
				stop(dbms);

			rm.close();
		}
		catch(Throwable t)
		{
			Log.warning(Geonet.HARVESTER, "Raised exception while harvesting");
			Log.warning(Geonet.HARVESTER, " (C) Node ID  : "+ id);
			Log.warning(Geonet.HARVESTER, " (C) Exception: "+ t);

			error = new HarvestError(HarvestError.UNKNOWN, t.toString(), null);
			t.printStackTrace();

			try
			{
				rm.abort();
			}
			catch (Exception ex)
			{
				Log.warning(Geonet.HARVESTER, "CANNOT ABORT EXCEPTION");
				Log.warning(Geonet.HARVESTER, " (C) Exc : "+ ex);
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Abstract methods that must be overridden
	//---
	//---------------------------------------------------------------------------

	protected abstract void doInit(Element entry) throws BadInputEx;

	protected abstract String doAdd(Dbms dbms, Element node)
											throws BadInputEx, SQLException;

	protected abstract void doUpdate(Dbms dbms, String id, Element node)
											throws BadInputEx, SQLException;

	protected abstract String doGetEvery();

	protected abstract boolean doIsOneRunOnly();

	protected abstract void doAddInfo(Element info);
	protected abstract void doHarvest(Logger l, ResourceManager rm);

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected String getValue(Element el, String name, String defValue)
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		return (value != null) ? value : defValue;
	}

	//---------------------------------------------------------------------------

	protected boolean getValue(Element el, String name, boolean defValue)
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		return (value != null) ? Boolean.parseBoolean(value) : defValue;
	}

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
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private int getEvery()
	{
		String every = doGetEvery();

		try
		{
			return Integer.parseInt(every);
		}
		catch(Exception e)
		{
			error = new HarvestError(HarvestError.BAD_EVERY, "'every' is not an int", every);

			return -1;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String id;
	private String name;
	private Status status;

	private Executor        executor;
	private HarvestError    error;
	private ProviderManager providMan;

	protected SettingManager settingMan;
}

//=============================================================================

class HarvestError
{
	//--- error codes

	public static final String BAD_EVERY = "bad-every";
	public static final String UNKNOWN   = "unknown";

	//--------------------------------------------------------------------------

	public String code;
	public String message;
	public String object;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public HarvestError(String code, String message, String object)
	{
		this.code    = code;
		this.message = message;
		this.object  = object;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public Element getErrorElement()
	{
		Element error = new Element("error")
								.addContent(new Element("code")   .setText(code))
								.addContent(new Element("message").setText(message));

		if (object != null)
			error.addContent(new Element("object").setText(object));

		return error;
	}
}

//=============================================================================

