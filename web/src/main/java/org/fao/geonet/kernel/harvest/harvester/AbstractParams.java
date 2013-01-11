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

import static org.quartz.JobBuilder.newJob;

import java.util.ArrayList;
import java.util.UUID;

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.utils.Log;
import jeeves.utils.QuartzSchedulerUtils;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.quartz.JobDetail;
import org.quartz.Trigger;

//=============================================================================

public abstract class AbstractParams
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public AbstractParams(DataManager dm)
	{
		this.dm = dm;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void create(Element node) throws BadInputEx
	{
		Element site    = node.getChild("site");
		Element opt     = node.getChild("options");
		Element content = node.getChild("content");
		
		
		Element account = (site == null) ? null : site.getChild("account");

		name       = Util.getParam(site, "name", "");
		uuid       = Util.getParam(site, "uuid", UUID.randomUUID().toString());
		
		owner = node.getAttributeValue("owner");
		if (owner == null) {
			Log.warning(Geonet.HARVEST_MAN, "No owner defined for harvester: " + name + " (" + uuid + ")");
		}

		useAccount = Util.getParam(account, "use",      false);
		username   = Util.getParam(account, "username", "");
		password   = Util.getParam(account, "password", "");

		every      = Util.getParam(opt, "every",      "0 0 0 * * ?" );
		
		oneRunOnly = Util.getParam(opt, "oneRunOnly", false);
		
		getTrigger();

		importXslt = Util.getParam(content, "importxslt", "none");
		validate = Util.getParam(content, "validate", false);

		addPrivileges(node.getChild("privileges"));
		addCategories(node.getChild("categories"));

		this.node = node;
	}

	//---------------------------------------------------------------------------

	public void update(Element node) throws BadInputEx
	{
		Element site    = node.getChild("site");
		Element opt     = node.getChild("options");
		Element content = node.getChild("content");

		Element account = (site == null) ? null : site.getChild("account");
		Element privil  = node.getChild("privileges");
		Element categ   = node.getChild("categories");

		name       = Util.getParam(site, "name", name);

		owner = node.getAttributeValue("owner");
		if (owner == null) {
			Log.warning(Geonet.HARVEST_MAN, "No owner defined for harvester: " + name + " (" + uuid + ")");
		}
		
		useAccount = Util.getParam(account, "use",      useAccount);
		username   = Util.getParam(account, "username", username);
		password   = Util.getParam(account, "password", password);

		every      = Util.getParam(opt, "every",      every);
		oneRunOnly = Util.getParam(opt, "oneRunOnly", oneRunOnly);

		getTrigger();
		
		importXslt = Util.getParam(content, "importxslt", importXslt);
		validate = Util.getParam(content, "validate", validate);

		if (privil != null)
			addPrivileges(privil);

		if (categ != null)
			addCategories(categ);

		this.node = node;
	}

	//---------------------------------------------------------------------------

	public Iterable<Privileges> getPrivileges() { return alPrivileges; }
	public Iterable<String>     getCategories() { return alCategories; }

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected void copyTo(AbstractParams copy)
	{
		copy.name       = name;
		copy.uuid       = uuid;
		copy.owner      = owner;
		copy.useAccount = useAccount;
		copy.username   = username;
		copy.password   = password;

		copy.every      = every;
		copy.oneRunOnly = oneRunOnly;

		copy.importXslt = importXslt;
		copy.validate   = validate;

		for (Privileges p : alPrivileges)
			copy.alPrivileges.add(p.copy());

		for (String s : alCategories)
			copy.alCategories.add(s);

		copy.node = node;
	}

	public JobDetail getJob() {
    	return newJob(HarvesterJob.class).withIdentity(uuid, AbstractHarvester.HARVESTER_GROUP_NAME).usingJobData(HarvesterJob.ID_FIELD, uuid).build();
    }
    
    public Trigger getTrigger() {
    	return QuartzSchedulerUtils.getTrigger(uuid, AbstractHarvester.HARVESTER_GROUP_NAME, every, MAX_EVERY);
    }

	//---------------------------------------------------------------------------

	protected void checkPort(int port) throws BadParameterEx
	{
		if (port <1 || port > 65535)
			throw new BadParameterEx("port", port);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Privileges and categories API methods
	//---
	//---------------------------------------------------------------------------

	/** Fills a list with Privileges that reflect the input 'privileges' element.
	  * The 'privileges' element has this format:
	  *
	  *   <privileges>
	  *      <group id="...">
	  *         <operation name="...">
	  *         ...
	  *      </group>
	  *      ...
	  *   </privileges>
	  *
	  * Operation names are: view, download, edit, etc... User defined operations are
	  * taken into account.
	  */

	private void addPrivileges(Element privil) throws BadInputEx
	{
		alPrivileges.clear();

		if (privil == null)
			return;

        for (Object o : privil.getChildren("group")) {
            Element group = (Element) o;
            String groupID = group.getAttributeValue("id");

            if (groupID == null) {
                throw new MissingParameterEx("attribute:id", group);
            }

            Privileges p = new Privileges(groupID);

            for (Object o1 : group.getChildren("operation")) {
                Element oper = (Element) o1;
                int op = getOperationId(oper);

                p.add(op);
            }

            alPrivileges.add(p);
        }
	}

	//---------------------------------------------------------------------------

	private int getOperationId(Element oper) throws BadInputEx
	{
		String operName = oper.getAttributeValue("name");

		if (operName == null)
			throw new MissingParameterEx("attribute:name", oper);

		int operID = dm.getAccessManager().getPrivilegeId(operName);

		if (operID == -1)
			throw new BadParameterEx("attribute:name", operName);

		if (operID == 2 || operID == 4)
			throw new BadParameterEx("attribute:name", operName);

		return operID;
	}

	//---------------------------------------------------------------------------
	/** Fills a list with category identifiers that reflect the input 'categories' element.
	  * The 'categories' element has this format:
	  *
	  *   <categories>
	  *      <category id="..."/>
	  *      ...
	  *   </categories>
	  */

	private void addCategories(Element categ) throws BadInputEx
	{
		alCategories.clear();

		if (categ == null)
			return;

        for (Object o : categ.getChildren("category")) {
            Element categElem = (Element) o;
            String categId = categElem.getAttributeValue("id");

            if (categId == null) {
                throw new MissingParameterEx("attribute:id", categElem);
            }

            if (!Lib.type.isInteger(categId)) {
                throw new BadParameterEx("attribute:id", categElem);
            }

            alCategories.add(categId);
        }
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String  name;
	public String  uuid;
	public String owner;
	public boolean useAccount;
	public String  username;
	public String  password;

	String  every;
	public boolean oneRunOnly;

	public boolean validate;
	public String importXslt;

	public Element node;

	//---------------------------------------------------------------------------

	protected DataManager dm;

	private ArrayList<Privileges> alPrivileges = new ArrayList<Privileges>();
	private ArrayList<String>     alCategories = new ArrayList<String>();

	//---------------------------------------------------------------------------

	private static final long MAX_EVERY = Integer.MAX_VALUE;
}

//=============================================================================

