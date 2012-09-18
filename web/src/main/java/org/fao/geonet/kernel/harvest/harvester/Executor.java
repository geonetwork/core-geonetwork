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

import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.Producer;
import org.fao.geonet.jms.message.harvest.HarvestMessage;

/**
 *  TODO javadoc.
 */
class Executor extends Thread
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Executor(AbstractHarvester ah) {
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

	public void setTimeout(int minutes) {
		timeout = minutes;
	}


	public void terminate() {
		terminate = true;
		interrupt();
		harvester = null;
	}

	public boolean isRunning() { return status == RUNNING; }

	//---------------------------------------------------------------------------
	//---
	//--- Executor's main loop
	//---
	//---------------------------------------------------------------------------

	public void run() {
		while (!terminate) {
			if (timeout == -1) {
				await(1);
            }
			else {
				await(timeout);

                //
                // notify peers if clustered : harvest job message is placed on point-to-point queue, the first
                // GN node in the cluster to pick it up will run it (and cause it to be removed from the queue)
                //
                if(ClusterConfig.isEnabled()) {
                    if (harvester.getNodeId().equals(ClusterConfig.getClientID())) {
                    try {
                        Log.info(Geonet.HARVESTER, "clustering enabled, creating harvest message");
                        HarvestMessage message = new HarvestMessage();
                        message.setId(harvester.getID());
                        Producer harvestProducer = ClusterConfig.get(Geonet.ClusterMessageQueue.HARVEST);
                        harvestProducer.produce(message);
                    }
                    catch (ClusterException x) {
                        Log.error(Geonet.HARVESTER, x.getMessage());
                        x.printStackTrace();
                    }
                }

                }
                //
                // if not clustered, just run the harvest job in this GN node
                //
                else {
                    if (!terminate && harvester != null) {
					status = RUNNING;
					harvester.harvest();
					status = WAITING;
				}
			}
		}
	}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private boolean await(int minutes) {
		try {
			sleep(minutes * 60 * 1000);
			return false;
		}
		catch (InterruptedException e) {
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


	private boolean terminate;
	private int     status;
	private int     timeout;

	private AbstractHarvester harvester;
}
