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

package org.fao.geonet.kernel.harvest.harvester.webfolder;

import java.util.ArrayList;
import java.util.Iterator;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

//=============================================================================

public class WAFParams extends AbstractParams
{
	//--------------------------------------------------------------------------
	//---
	//--- Init : called when an entry is read from database. Vars are initialized
	//---        from the given entry
	//---
	//--------------------------------------------------------------------------

	public void init(Element node) throws BadInputEx
	{
		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= site.getChild("account");

		url = site.getChildText("url");

		useAccount = account.getChildText("use").equals("true");
		username   = account.getChildText("username");
		password   = account.getChildText("password");

		every      = Integer.parseInt(opt.getChildText("every"));
		oneRunOnly = opt.getChildText("oneRunOnly").equals("true");
		validate   = opt.getChildText("validate")  .equals("true");
		structure  = opt.getChildText("structure") .equals("true");

		//--- add privileges

		alPrivileges.clear();

		Iterator i = privil.getChildren("group").iterator();

		while (i.hasNext())
		{
			Element group   = (Element) i.next();
			String  groupID = group.getAttributeValue("id");

			Privilege p = new Privilege();
			p.groupID = groupID;

			Iterator operList = group.getChildren("operation").iterator();

			while (operList.hasNext())
			{
				Element oper = (Element) operList.next();

				p.add(getOperationID(oper));
			}

			alPrivileges.add(p);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Create : called when a new entry must be added. Reads values from the
	//---          provided entry, providing default values
	//---
	//---------------------------------------------------------------------------

	public void create(Element node) throws BadInputEx
	{
		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= (site == null) ? null : site.getChild("account");

		url        = getValue(site,    "url",      "");
		useAccount = getValue(account, "use",      false);
		username   = getValue(account, "username", "");
		password   = getValue(account, "password", "");

		every      = getValue(opt, "every",      90);
		oneRunOnly = getValue(opt, "oneRunOnly", false);
		validate   = getValue(opt, "validate",   false);
		structure  = getValue(opt, "structure",  false);

		checkEvery(every);
		addPrivileges(privil);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update : called when an entry has changed and variables must be updated
	//---
	//---------------------------------------------------------------------------

	public void update(Element node) throws BadInputEx
	{
		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= (site == null) ? null : site.getChild("account");

		url        = getValue(site,    "url",      url);
		useAccount = getValue(account, "use",      useAccount);
		username   = getValue(account, "username", username);
		password   = getValue(account, "password", password);

		every      = getValue(opt, "every",      every);
		oneRunOnly = getValue(opt, "oneRunOnly", oneRunOnly);
		validate   = getValue(opt, "validate",   validate);
		structure  = getValue(opt, "structure",  structure);

		checkEvery(every);

		//--- if some privileges are given, we drop the previous ones and
		//--- set these new ones

		if (privil != null)
			addPrivileges(privil);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Other API methods
	//---
	//---------------------------------------------------------------------------

	public Iterable<Privilege> getPrivileges() { return alPrivileges; }

	//---------------------------------------------------------------------------

	public WAFParams copy()
	{
		WAFParams copy = new WAFParams();

		copy.url = url;

		copy.useAccount = useAccount;
		copy.username   = username;
		copy.password   = password;

		copy.every      = every;
		copy.oneRunOnly = oneRunOnly;
		copy.validate   = validate;
		copy.structure  = structure;

		for (Privilege p : alPrivileges)
			copy.alPrivileges.add(p.copy());

		return copy;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void addPrivileges(Element privil) throws BadInputEx
	{
		alPrivileges.clear();

		if (privil == null)
			return;

		Iterator groupList = privil.getChildren("group").iterator();

		while (groupList.hasNext())
		{
			Element group   = (Element) groupList.next();
			String  groupID = group.getAttributeValue("id");

			if (groupID == null)
				throw new MissingParameterEx("attribute:id", group);

			Privilege p = new Privilege();
			p.groupID = groupID;

			Iterator operList = group.getChildren("operation").iterator();

			while (operList.hasNext())
			{
				Element oper = (Element) operList.next();
				int     op   = getOperationID(oper);

				p.add(op);
			}

			alPrivileges.add(p);
		}
	}

	//---------------------------------------------------------------------------

	private int getOperationID(Element oper) throws BadInputEx
	{
		String operName = oper.getAttributeValue("name");

		if (operName == null)
			throw new MissingParameterEx("attribute:name", oper);

		int operID = AccessManager.getPrivilegeId(operName);

		if (operID == -1)
			throw new BadParameterEx("attribute:name", operName);

		if (operID == 2 || operID == 4)
			throw new BadParameterEx("attribute:name", operName);

		return operID;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String  url;

	public boolean useAccount;
	public String  username;
	public String  password;

	public int     every;
	public boolean oneRunOnly;
	public boolean validate;
	public boolean structure;

	private ArrayList<Privilege> alPrivileges = new ArrayList<Privilege>();
}

//=============================================================================

class Privilege
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void add(int operation)
	{
		alOperations.add(operation);
	}

	//---------------------------------------------------------------------------

	public Iterable<Integer> getOperations() { return alOperations; }

	//---------------------------------------------------------------------------

	public Privilege copy()
	{
		Privilege copy = new Privilege();

		copy.groupID = groupID;

		for (int oper : alOperations)
			copy.alOperations.add(oper);

		return copy;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String groupID;

	private ArrayList<Integer> alOperations = new ArrayList<Integer>();
}

//=============================================================================

