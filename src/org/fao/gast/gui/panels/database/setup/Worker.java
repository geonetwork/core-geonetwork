//==============================================================================
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

package org.fao.gast.gui.panels.database.setup;

import java.text.MessageFormat;
import java.util.List;
import org.dlib.gui.ProgressDialog;
import org.fao.gast.lib.DatabaseLib;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;
import org.fao.gast.localization.Messages;

//==============================================================================

public class Worker implements Runnable
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Worker(ProgressDialog dlg)
	{
		this.dlg = dlg;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Runnable interface
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		try
		{
			dlg.reset(0);
			Resource res = Lib.config.createResource();
			Lib.database.setup(res, callBack);
		}
		catch(Throwable e)
		{
			Lib.gui.showError(dlg, e);
		}
		finally
		{
			dlg.stop();
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private DatabaseLib.CallBack callBack = new DatabaseLib.CallBack()
	{
		public void schemaObjects(int count)
		{
			dlg.reset(count*3);
		}

		//------------------------------------------------------------------------

		public void removed(String object, String type)
		{
			dlg.advance(MessageFormat.format(Messages.getString("Worker.removing"), object));
		}

		//------------------------------------------------------------------------

		public void cyclicRefs(List<String> objects)
		{
			Lib.gui.showError(dlg, MessageFormat.format(Messages.getString("Worker.cyclicReference"),objects));
		}

		//------------------------------------------------------------------------

		public void creating(String object, String type)
		{
			dlg.advance(MessageFormat.format(Messages.getString("Worker.creating"), object));
		}

		//------------------------------------------------------------------------
		public void skipping(String table) {}
		//------------------------------------------------------------------------

		public void loadingData()
        {
            dlg.advance(Messages.getString("Worker.loadingData"));
        }
		
		public void filling(String table, String file)
		{
			dlg.advance(MessageFormat.format(Messages.getString("Worker.fillingTable"), table));
		}
	};

	//---------------------------------------------------------------------------

	private ProgressDialog dlg;
}

//==============================================================================

