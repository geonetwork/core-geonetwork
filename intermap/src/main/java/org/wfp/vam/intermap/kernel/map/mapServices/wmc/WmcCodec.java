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

package org.wfp.vam.intermap.kernel.map.mapServices.wmc;

import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.Layer;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.ArcIMSService;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl.WMCFactory;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.CapabilitiesStore;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSCapabilities;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLayer;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;

/**
 * @author Emanuele Tajariol
 */
public class WmcCodec
{
	/**
	 * Builds a 110 View Context from the WMS layers held in the MapMerger
	 *
	 * @param    mm                  the MapMerger holding the layers
	 * @param    title               the context title
	 * @param    width               the view width in pixels
	 * @param    height              the view height in pixels
	 *
	 * @return   the WMCViewContext
	 */
	public static WMCViewContext createViewContext(MapMerger mm, MarkerSet ms, String title, int width, int height)
		throws Exception
	{
		UUID uuid = UUID.randomUUID();

		WMCViewContext vc = WMCFactory.createWMCViewContext();
		//ViewContextType vc = ViewContextType.Factory.newInstance();
		vc.setVersion("1.1.0"); // mandatory
		vc.setId(uuid.toString()); // mandatory

		WMCGeneral vcg = vc.addNewGeneral();

		vcg.setTitle(title!=null ? title: ("Intermap context " + uuid)); // mandatory

		WMCBoundingBox bbt = vcg.addNewBoundingBox(); // mandatory
		bbt.setSRS("EPSG:4326");
		bbt.setMaxx(mm.getBoundingBox().getEast());
		bbt.setMaxy(mm.getBoundingBox().getNorth());
		bbt.setMinx(mm.getBoundingBox().getWest());
		bbt.setMiny(mm.getBoundingBox().getSouth());

		WMCWindow win = vcg.addNewWindow(); // not mandatory
		win.setWidth(width);
		win.setHeight(height);

		List<WMCLayer> invertedList = new ArrayList<WMCLayer>();

		for(Layer mmLayer: mm.getLayers())
		{
			MapService mmService = mmLayer.getService();

			// Find out if/how this mmService can be added to the context
			switch(mmService.getType())
			{
				case WmsService.TYPE:
					WMCLayer wmcLayer = buildLayer(mmLayer);
					System.out.println("ADDING WMS SERVICE: " + mmService.getTitle());
					invertedList.add(wmcLayer);
					break;

				case ArcIMSService.TYPE:
				default:
					System.out.println("Service '"+mmLayer.getService().getName()+"' is not OGC compliant and will not be addeded in the WMC");
					continue;
			}
		}

		WMCLayerList llist = vc.addNewLayerList();

		Collections.reverse(invertedList); // intermap order is reversed with respect to WMC
		for (WMCLayer layer : invertedList)
		{
			llist.addLayer(layer);
		}

		if(ms != null && ! ms.isEmpty())
		{
			WMCExtension ext = vcg.addNewExtension();
			ext.add(new Element("georss").addContent(GeoRSSCodec.getGeoRSS(ms)));
		}

		return vc;
	}

	/**
	 * Builds a WMCLayer from an IM Layer.
	 * <br/>Transparency is stored as an Extended element.
	 */
	private static WMCLayer buildLayer(Layer mmLayer) throws Exception
	{
		WmsService wmsService = (WmsService)mmLayer.getService();

		WMSLayer wmsLayer = wmsService.getWmsLayer();
		WMSCapabilities wmsCapa =
			CapabilitiesStore.getCapabilityDocument(wmsService.getServerURL());

		WMCLayer wmcLayer = WMCFactory.createWMCLayer();

		// Mandatory attributes
		wmcLayer.setQueryable(wmsLayer.isQueryable()); // mandatory
		wmcLayer.setHidden( ! mmLayer.isVisible()); // mandatory
		// Mandatory info
		wmcLayer.setName(wmsLayer.getName()); // mandatory
		wmcLayer.setTitle(wmsLayer.getTitle()); // mandatory
		wmcLayer.setAbstract(wmsLayer.getAbstract());
		// Extra info

		float transparency = mmLayer.getTransparency();
		// add transparency only if it holds a non-default value
		if(transparency < 1.0f)
		{
			WMCExtension layerExt = wmcLayer.addNewExtension();
			Element etransp = new Element("Transparency").setText(""+transparency);
			layerExt.add(etransp);
		}

		// Server
		WMCServer server = wmcLayer.addNewServer();
		server.setService(WMCService.OGC_WMS); // mandatory
		server.setVersion(wmsCapa.getVersion().toString());// mandatory
		server.setTitle(wmsCapa.getService().getTitle()); // optional

		// ????? Specs are quite ambiguous on which onlineResource should be set.
		WMCOnlineResource ort = server.addNewOnlineResource();
//		ort.setHref(wmsCapa.getService().getOnlineResource().getHref()); // this is unreliable
		ort.setHref(wmsCapa.getCapability().getRequest().getGetCapabilities().getDCPType(0).getHttpGetHref());

		return wmcLayer;
	}

}

//=============================================================================

