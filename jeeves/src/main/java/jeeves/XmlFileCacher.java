//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves;

import jeeves.server.overrides.ConfigurationOverrides;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

//=============================================================================

public class XmlFileCacher
{
	private ServletContext servletContext;
    private String appPath;

    //--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------
    public XmlFileCacher(File file, String appPath)
    {
        this(file, null, appPath);
    }
    /**
     * @param servletContext if non-null the config-overrides can be applied to the xml file when it is loaded
     */
	public XmlFileCacher(File file, ServletContext servletContext, String appPath)
	{
		//--- 10 seconds as default interval
		this(file, 10, servletContext, appPath);
	}

	//--------------------------------------------------------------------------
	/**
	 * @param servletContext if non-null the config-overrides can be applied to the xml file when it is loaded
	 */
	public XmlFileCacher(File file, int interval, ServletContext servletContext, String appPath)
	{
		this.file     = file;
		this.interval = interval;
		this.servletContext = servletContext;
		this.appPath = appPath;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public Element get() throws JDOMException, IOException
	{
		if (elem == null)
		{
			elem         = load();
			lastTime     = System.currentTimeMillis();
			lastModified = file.lastModified();
		}

		else
		{
			long now   = System.currentTimeMillis();
			int  delta = (int) (now - lastTime) / 1000;

			if((delta >= interval))
			{
				long fileModified = file.lastModified();

				if (lastModified != fileModified)
				{
					elem         = load();
					lastModified = fileModified;
				}

				lastTime = now;
			}
		}

		return elem;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//--------------------------------------------------------------------------

	/** Overriding this method makes it possible a conversion to XML on the fly
	  * of files in other formats */

	protected Element load() throws JDOMException, IOException
	{
		Element xml = Xml.loadFile(file);
	    ConfigurationOverrides.DEFAULT.updateWithOverrides(file.getPath(), servletContext, appPath, xml);
        return xml;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private File file;
	private int  interval; //--- in secs
	private long lastTime;
	private long lastModified;

	private Element elem;
}

//=============================================================================


