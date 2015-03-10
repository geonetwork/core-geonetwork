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

//=============================================================================

class Executor extends Thread
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Executor(AbstractHarvester<?> ah)
	{
		terminate  = false;
		status     = WAITING;
		harvester  = ah;
		timeout    = -1;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setTimeout(int minutes)
	{
		timeout = minutes;
	}

	//---------------------------------------------------------------------------

	public void terminate()
	{
		terminate = true;
		interrupt();
		harvester = null;
	}

	//---------------------------------------------------------------------------

	public boolean isRunning() { return status == RUNNING; }

	//---------------------------------------------------------------------------
	//---
	//--- Executor's main loop
	//---
	//---------------------------------------------------------------------------

	public void run()
	{
		while (!terminate)
		{
			if (timeout == -1)
				await(1);
			else
			{
				await(timeout);

				if (!terminate && harvester != null)
				{
					status = RUNNING;
					harvester.harvest();
					status = WAITING;
				}
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private boolean await(int minutes)
	{
		try
		{
			sleep((long)minutes * 60 * 1000);
			return false;
		}
		catch (InterruptedException e)
		{
			return true;
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static final int WAITING = 0;
	private static final int RUNNING = 1;

	//---------------------------------------------------------------------------

	private boolean terminate;
	private int     status;
	private int     timeout;

	private AbstractHarvester<?> harvester;
}

//=============================================================================

