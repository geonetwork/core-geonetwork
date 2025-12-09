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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class XmlFileCacher {
    private ServletContext servletContext;
    private Path appPath;
    private Path file;
    private int interval; //--- in secs
    private long lastTime;
    private FileTime lastModified;

    private Element elem;

    public XmlFileCacher(Path file, Path appPath) {
        this(file, null, appPath);
    }

    /**
     * @param servletContext if non-null the config-overrides can be applied to the xml file when it
     *                       is loaded
     */
    public XmlFileCacher(Path file, ServletContext servletContext, Path appPath) {
        //--- 10 seconds as default interval
        this(file, 10, servletContext, appPath);
    }

    /**
     * @param servletContext if non-null the config-overrides can be applied to the xml file when it
     *                       is loaded
     */
    public XmlFileCacher(Path file, int interval, ServletContext servletContext, Path appPath) {
        this.file = file;
        this.interval = interval;
        this.servletContext = servletContext;
        this.appPath = appPath;
    }

    public Element get() throws JDOMException, IOException {
        if (elem == null) {
            elem = load();
            lastTime = System.currentTimeMillis();
            lastModified = Files.getLastModifiedTime(file);
        } else {
            long now = System.currentTimeMillis();
            int delta = (int) (now - lastTime) / 1000;

            if ((delta >= interval)) {
                FileTime fileModified = Files.getLastModifiedTime(file);

                if (!lastModified.equals(fileModified)) {
                    elem = load();
                    lastModified = fileModified;
                }

                lastTime = now;
            }
        }

        return elem;
    }

    protected Element load() throws JDOMException, IOException {
        Element xml = Xml.loadFile(file);
        return xml;
    }
}
