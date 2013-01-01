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

package org.fao.geonet.kernel;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This class is responsible of reading and writing xml on the database. 
 * It works on tables like (id, data, lastChangeDate).
 */
public abstract class XmlSerializer {

	private static final List<Namespace> XML_SELECT_NAMESPACE = Arrays.asList(Geonet.Namespaces.GCO);
	protected SettingManager sm;

	public static class ThreadLocalConfiguration {
	    private boolean forceHideWithheld = false;

        public boolean isForceHideWithheld() {
            return forceHideWithheld;
        }
        public void setForceHideWithheld(boolean forceHideWithheld) {
            this.forceHideWithheld = forceHideWithheld;
        }
	}

	private static InheritableThreadLocal<ThreadLocalConfiguration> configThreadLocal = new InheritableThreadLocal<XmlSerializer.ThreadLocalConfiguration>();
	public static ThreadLocalConfiguration getThreadLocal(boolean setIfNotPresent) {
	    ThreadLocalConfiguration config = configThreadLocal.get();
	    if(config == null && setIfNotPresent) {
	        config = new ThreadLocalConfiguration();
	        configThreadLocal.set(config);
	    }
	    
	    return config;
	}
	
    /**
     *
     * @param sMan
     */
	public XmlSerializer(SettingManager sMan) {
		sm = sMan;
	}

    /**
     *
     * @return
     */
	public boolean resolveXLinks() {
		if (sm == null) { // no initialization, no XLinks
			Log.error(Geonet.DATA_MANAGER,"No settingManager in XmlSerializer, XLink Resolver disabled.");
			return false; 
		}

		String xlR = sm.getValue("system/xlinkResolver/enable");
		if (xlR != null) {
			boolean isEnabled = xlR.equals("true");
			if (isEnabled) Log.info(Geonet.DATA_MANAGER,"XLink Resolver enabled.");
			else Log.info(Geonet.DATA_MANAGER,"XLink Resolver disabled.");
			return isEnabled; 
		} else {
			Log.error(Geonet.DATA_MANAGER,"XLink resolver setting does not exist! XLink Resolver disabled.");
			return false;
		}
	}

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table' and the string read is converted into xml.
     *
     * @param dbms
     * @param table
     * @param id
     * @return
     * @throws Exception
     */
	protected Element internalSelect(Dbms dbms, String table, String id) throws Exception {
		String query = "SELECT * FROM " + table + " WHERE id = ?";
		Element select = dbms.select(query, new Integer(id));
		Element record = select.getChild(Jeeves.Elem.RECORD);

		if (record == null)
			return null;

		String xmlData = record.getChildText("data");
		Element metadata = Xml.loadString(xmlData, false);

		boolean hideWithheldElements = sm.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/enable", false);
		if(ServiceContext.get() != null) {
			ServiceContext context = ServiceContext.get();
			String ownerId = record.getChildText("owner");
			UserSession userSession = context.getUserSession();
			boolean isOwner = userSession != null && ownerId != null && ownerId.equals(userSession.getUserId());
			boolean isAdmin = userSession != null && userSession.getProfile() != null && context.getProfileManager().getProfilesSet(userSession.getProfile()).contains(ProfileManager.ADMIN);
			if(isAdmin || isOwner) {
				hideWithheldElements = false;
			}
		}
		boolean keepMarkedElement = sm.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/keepMarkedElement", false);
		if (hideWithheldElements || (getThreadLocal(false) != null && getThreadLocal(false).forceHideWithheld)) {
			List<?> nodes = Xml.selectNodes(metadata, "*[@gco:nilReason = 'withheld'] | *//*[@gco:nilReason = 'withheld']", XML_SELECT_NAMESPACE);
			for (Object object : nodes) {
				if (object instanceof Element) {
					Element element = (Element) object;
					
					if(keepMarkedElement) {
						element.removeContent();
						Attribute nilReason = element.getAttribute("nilReason", Geonet.Namespaces.GCO);
						@SuppressWarnings("unchecked")
						List<Attribute> atts = new ArrayList<Attribute>(element.getAttributes());
						for (Attribute attribute : atts) {
							if(attribute != nilReason) {
								attribute.detach();
							}
						}
					} else {
						element.detach();
					}
				}
			}
		}

		return (Element) metadata.detach();
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param schema
     * @param xml
     * @param serial
     * @param source
     * @param uuid
     * @param createDate
     * @param changeDate
     * @param isTemplate
     * @param root
     * @param title
     * @param owner
     * @param groupOwner
     * @param docType
     * @return
     * @throws SQLException
     */
	protected String insertDb(Dbms dbms, String schema, Element xml, int serial,
					 String source, String uuid, String createDate,
					 String changeDate, String isTemplate, String root, String title,
					 int owner, String groupOwner, String docType) throws SQLException {
	
		if (resolveXLinks()) Processor.removeXLink(xml);

		String date = new ISODate().toString();

		if (createDate == null)
			createDate = date;

		if (changeDate == null)
			changeDate = date;

		fixCR(xml);

		StringBuffer fields = new StringBuffer("id, schemaId, data, createDate, changeDate, source, "+
															"uuid, isTemplate, isHarvested, root, owner, doctype");
		StringBuffer values = new StringBuffer("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");

		Vector<Serializable> args = new Vector<Serializable>();
		args.add(serial);
		args.add(schema);
		args.add(Xml.getString(xml));
		args.add(createDate);
		args.add(changeDate);
		args.add(source);
		args.add(uuid);
		args.add(isTemplate);
		args.add("n");
		args.add(root);
		args.add(owner);
		args.add(docType);

		if (groupOwner != null) {
			fields.append(", groupOwner");
			values.append(", ?");
			args.add(new Integer(groupOwner));
		}

		if (title != null)
		{
			fields.append(", title");
			values.append(", ?");
			args.add(title);
		}

		String query = "INSERT INTO Metadata (" + fields + ") VALUES(" + values + ")";
		dbms.execute(query, args.toArray());

		return Integer.toString(serial);
	}

    /**
     *  Updates an xml element into the database. The new data replaces the old one.
     *
     * @param dbms
     * @param id
     * @param xml
     * @param changeDate
     * @param updateDateStamp
     *
     * @throws SQLException
     */
	protected void updateDb(Dbms dbms, String id, Element xml, String changeDate, String root, boolean updateDateStamp) throws SQLException {
		if (resolveXLinks()) Processor.removeXLink(xml);

		String query = "UPDATE Metadata SET data=?, changeDate=?, root=? WHERE id=?";
        String queryMinor = "UPDATE Metadata SET data=?, root=? WHERE id=?";

		Vector<Serializable> args = new Vector<Serializable>();

		fixCR(xml);
		args.add(Xml.getString(xml));

        if (updateDateStamp) {
            if (changeDate == null)	{
                args.add(new ISODate().toString());
            } else {
                args.add(changeDate);
            }
        }

 		args.add(root);
		args.add(new Integer(id));

        if (updateDateStamp)  {
            dbms.execute(query, args.toArray());
        } else {
            dbms.execute(queryMinor, args.toArray());
        }
	}

    /**
     * Deletes an xml element given its id.
     *
     * @param dbms
     * @param table
     * @param id
     * @throws SQLException
     */
	protected void deleteDb(Dbms dbms, String table, String id) throws Exception {
		// TODO: Ultimately we want to remove any xlinks in this document
		// that aren't already in use from the xlink cache. For now we
		// rely on the admin clearing cache and reindexing regularly
		String query = "DELETE FROM " + table + " WHERE id=?";
		dbms.execute(query, new Integer(id));
	}

    /**
     *
     * @param xml
     */
	private void fixCR(Element xml) {
		List list = xml.getChildren();
		if (list.size() == 0) {
			String text = xml.getText();
			xml.setText(Util.replaceString(text, "\r\n", "\n"));
		}
		else {
            for (Object o : list) {
                fixCR((Element) o);
            }
        }
	}

	/* API to be overridden by extensions */

	public abstract void delete(Dbms dbms, String table, String id, ServiceContext context) 
	   throws Exception;

	public abstract void update(Dbms dbms, String id, Element xml, 
		 String changeDate, boolean updateDateStamp, ServiceContext context) 
		 throws Exception;

	public abstract String insert(Dbms dbms, String schema, Element xml, 
					 int serial, String source, String uuid, String createDate,
					 String changeDate, String isTemplate, String title,
			 int owner, String groupOwner, String docType, ServiceContext context) 
			 throws Exception;

	
	public abstract Element select(Dbms dbms, String table, String id) 
			 throws Exception;
	public abstract Element selectNoXLinkResolver(Dbms dbms, String table, String id) 
			 throws Exception;
} 
