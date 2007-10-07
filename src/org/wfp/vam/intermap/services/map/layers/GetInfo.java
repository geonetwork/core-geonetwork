/**
 * GetInfo.java
 *
 */


package org.wfp.vam.intermap.services.map.layers;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSFormat;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSMetadataURL;
import org.wfp.vam.intermap.services.map.MapUtil;

public class GetInfo implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	static class PreferredInfo
	{
		WMSMetadataURL.MDTYPE _type;
		WMSFormat _format;

		public PreferredInfo(WMSMetadataURL.MDTYPE type, WMSFormat format)
		{
			_type = type;
			_format = format;
		}

		public boolean match(WMSMetadataURL mdurl)
		{
			return _type==mdurl.getType() && _format==mdurl.getFormat();
		}
	}

	private static List<PreferredInfo> _preferred = new ArrayList<PreferredInfo>();

	static
	{
		_preferred.add(new PreferredInfo(WMSMetadataURL.MDTYPE.ISO19115, WMSFormat.TEXT_HTML));
		_preferred.add(new PreferredInfo(WMSMetadataURL.MDTYPE.ISO19115, WMSFormat.TEXT_XHTML));
		_preferred.add(new PreferredInfo(WMSMetadataURL.MDTYPE.ISO19115, WMSFormat.TEXT_PLAIN));
		_preferred.add(new PreferredInfo(WMSMetadataURL.MDTYPE.FGDC, WMSFormat.TEXT_HTML));
		_preferred.add(new PreferredInfo(WMSMetadataURL.MDTYPE.FGDC, WMSFormat.TEXT_XHTML));
		_preferred.add(new PreferredInfo(WMSMetadataURL.MDTYPE.FGDC, WMSFormat.TEXT_PLAIN));
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int id = Integer.parseInt(params.getChildText(Constants.MAP_SERVICE_ID));
		MapMerger mm = MapUtil.getMapMerger(context);

		Element ret = new Element("response");

		MapService ms = mm.getService(id);

		if(ms instanceof WmsService)
		{
			WmsService ws = (WmsService)ms;
			WMSLayer wlayer = ws.getWmsLayer();

			ret.addContent(new Element("type").setText("WMS"));
			ret.addContent(new Element("title").setText(wlayer.getTitle()));
			ret.addContent(new Element("abstract").setText(wlayer.getAbstract()));

			String legendURL = ws.getLegendUrl();
			if(legendURL != null)
				ret.addContent(new Element("legendURL").setText(legendURL));

			WMSMetadataURL md=null;
			for(PreferredInfo pref : _preferred)
			{
				md = getMetadata(wlayer, pref);
				if(md != null)
					break;
			}

			if(md != null)
			{
				String href = md.getOnlineResource().getHref();
				URL url = new URL(href);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				String info = (String)conn.getContent(); // preferred info are all text
				ret.addContent(new Element("info")
								   .setText(info)
								   .setAttribute("format",md.getFormat().toString())
								   .setAttribute("type",md.getType().toString()));
			}
		}
		else
		{
			ret.addContent(new Element("type").setText("ARCIMS"));
			ret.addContent(new Element("title").setText(ms.getTitle()));
		}

		return ret;
	}

	private WMSMetadataURL getMetadata(WMSLayer wlayer, PreferredInfo pref)
	{
		for(WMSMetadataURL wmdurl : wlayer.getMetadataURLIterator())
		{
			if(pref.match(wmdurl))
				return wmdurl;
		}
		return null;
	}

}
