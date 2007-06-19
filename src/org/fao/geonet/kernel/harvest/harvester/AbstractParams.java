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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

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
		Element account = (site == null) ? null : site.getChild("account");

		name       = getValue(site, "name", "");
		uuid       = getValue(site, "uuid", UUID.randomUUID().toString());

		useAccount = getValue(account, "use",      false);
		username   = getValue(account, "username", "");
		password   = getValue(account, "password", "");

		every      = getValue(opt, "every",      90   );
		oneRunOnly = getValue(opt, "oneRunOnly", false);

		checkEvery(every);

		addPrivileges(node.getChild("privileges"));
		addCategories(node.getChild("categories"));
	}

	//---------------------------------------------------------------------------

	public void update(Element node) throws BadInputEx
	{
		Element site    = node.getChild("site");
		Element opt     = node.getChild("options");
		Element account = (site == null) ? null : site.getChild("account");
		Element privil  = node.getChild("privileges");
		Element categ   = node.getChild("categories");

		name       = getValue(site, "name", name);

		useAccount = getValue(account, "use",      useAccount);
		username   = getValue(account, "username", username);
		password   = getValue(account, "password", password);

		every      = getValue(opt, "every",      every);
		oneRunOnly = getValue(opt, "oneRunOnly", oneRunOnly);

		checkEvery(every);

		if (privil != null)
			addPrivileges(privil);

		if (categ != null)
			addCategories(categ);
	}

	//---------------------------------------------------------------------------

	public Iterable<Privileges> getPrivileges() { return alPrivileges; }
	public Iterable<Integer>    getCategories() { return alCategories; }

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected void copyTo(AbstractParams copy)
	{
		copy.name       = name;
		copy.uuid       = uuid;

		copy.useAccount = useAccount;
		copy.username   = username;
		copy.password   = password;

		copy.every      = every;
		copy.oneRunOnly = oneRunOnly;

		for (Privileges p : alPrivileges)
			copy.alPrivileges.add(p.copy());

		for (Integer i : alCategories)
			copy.alCategories.add(i);
	}

	//---------------------------------------------------------------------------

	protected String getValue(Element el, String name) throws MissingParameterEx, BadParameterEx
	{
		if (el == null)
			throw new MissingParameterEx(name);

		String value = el.getChildText(name);

		if (value == null)
			throw new MissingParameterEx(name);

		if (value.trim().length() == 0)
			throw new BadParameterEx(name, value);

		return value;
	}

	//---------------------------------------------------------------------------

	protected String getValue(Element el, String name, String defValue)
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		return (value != null) ? value : defValue;
	}

	//---------------------------------------------------------------------------

	protected boolean getValue(Element el, String name, boolean defValue) throws BadParameterEx
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		if (value == null)
			return defValue;

		if (!value.equals("true") && !value.equals("false"))
			throw new BadParameterEx(name, value);

		return Boolean.parseBoolean(value);
	}

	//---------------------------------------------------------------------------

	protected int getValue(Element el, String name, int defValue) throws BadParameterEx
	{
		if (el == null)
			return defValue;

		String value = el.getChildText(name);

		if (value == null || value.length() == 0)
			return defValue;

		try
		{
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e)
		{
			throw new BadParameterEx(name, value);
		}
	}

	//---------------------------------------------------------------------------

	protected void checkEvery(int every) throws BadParameterEx
	{
		if (every <1 || every > MAX_EVERY)
			throw new BadParameterEx("every", every);
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

		Iterator groupList = privil.getChildren("group").iterator();

		while (groupList.hasNext())
		{
			Element group   = (Element) groupList.next();
			String  groupID = group.getAttributeValue("id");

			if (groupID == null)
				throw new MissingParameterEx("attribute:id", group);

			Privileges p = new Privileges(groupID);

			Iterator operList = group.getChildren("operation").iterator();

			while (operList.hasNext())
			{
				Element oper = (Element) operList.next();
				int     op   = getOperationId(oper);

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

		Iterator categList = categ.getChildren("category").iterator();

		while (categList.hasNext())
		{
			Element categElem = (Element) categList.next();
			String  categId   = categElem.getAttributeValue("id");

			if (categId == null)
				throw new MissingParameterEx("attribute:id", categElem);

			if (!Lib.type.isInteger(categId))
				throw new BadParameterEx("attribute:id", categElem);

			alCategories.add(new Integer(categId));
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String  name;
	public String  uuid;

	public boolean useAccount;
	public String  username;
	public String  password;

	public int     every;
	public boolean oneRunOnly;

	//---------------------------------------------------------------------------

	protected DataManager dm;

	private ArrayList<Privileges> alPrivileges = new ArrayList<Privileges>();
	private ArrayList<Integer>    alCategories = new ArrayList<Integer>();

	//---------------------------------------------------------------------------

	private static final int MAX_EVERY = 1000000;
}

//=============================================================================

