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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;

import org.apache.log4j.Priority;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

/**
 * This class is responsible of reading and writing xml on the database. 
 * It works on tables like (id, data, lastChangeDate).
 */
public abstract class XmlSerializer {

	private static final List<Namespace> XML_SELECT_NAMESPACE = Arrays.asList(Geonet.Namespaces.GCO);
	private static final String WITHHELD = "withheld";	
	@SuppressWarnings("serial")
	private static final Filter EMPTY_WITHHELD = new Filter() {
		
		@Override
		public boolean matches(Object obj) {
			if (obj instanceof Element) {
				Element elem = (Element) obj;
				String withheld = elem.getAttributeValue("nilReason", Geonet.Namespaces.GCO);
				if(WITHHELD.equalsIgnoreCase(withheld) && elem.getChildren().size() == 0 && elem.getTextTrim().isEmpty()) {
					return true;
				}
			}
			return false;
		}
	};
	
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
	public static void clearThreadLocal() {
		configThreadLocal.set(null);
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
     * @param isIndexingTask If true, then withheld elements are not removed.
     * @return
     * @throws Exception
     */
	protected Element internalSelect(Dbms dbms, String table, String id, boolean isIndexingTask) throws Exception {
		String query = "SELECT * FROM " + table + " WHERE id = ?";
		Element select = dbms.select(query, Integer.valueOf(id));
		Element record = select.getChild(Jeeves.Elem.RECORD);

		if (record == null)
			return null;

		String xmlData = record.getChildText("data");
		Element metadata = Xml.loadString(xmlData, false);

		logEmptyWithheld(id, metadata, "XmlSerializer.internalSelect");
		
		if (!isIndexingTask) { 
			boolean hideWithheldElements = sm.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/enable", false);
    		if(ServiceContext.get() != null) {
    			ServiceContext context = ServiceContext.get();
    			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
    			boolean canEdit = gc.getAccessManager().canEdit(context, id);
    			if(canEdit) {
    				hideWithheldElements = false;
    			}
    		}
    		if (hideWithheldElements || (getThreadLocal(false) != null && getThreadLocal(false).forceHideWithheld)) {
    		    removeWithheldElements(metadata, sm);
    		}
		}
		return (Element) metadata.detach();
	}

    private boolean logEmptyWithheld(String id, Element metadata, String methodName) {
        boolean hideWithheldElements = sm.getValueAsBool("system/" + Geonet.Config.HIDE_WITHHELD_ELEMENTS + "/enable", false);
        if (hideWithheldElements && Log.isEnabledFor(Geonet.DATA_MANAGER, Priority.WARN_INT)) {
            Iterator<?> emptyWithheld = metadata.getDescendants(EMPTY_WITHHELD);
            if (emptyWithheld.hasNext()) {
                StringBuilder withheld = new StringBuilder();
                while (emptyWithheld.hasNext()) {
                    Element next = (Element) emptyWithheld.next();
                    withheld.append("\n    ");
                    xpath(withheld, next);
                }
                Log.warning(Geonet.DATA_MANAGER, "[" + WITHHELD + "] " + "In method [" + methodName + "] Metadata id=" + id
                        + " has withheld elements that don't contain any data: " + withheld);
                return true;
            }
        }
        return false;
    }
    private void xpath(StringBuilder buffer, Element next) {
		if(next.getParentElement() != null) {
			xpath(buffer, next.getParentElement());
			buffer.append("/");
		}
		
		String name = next.getName();
		Namespace namespace = next.getNamespace();
		buffer.append(namespace.getPrefix()).append(":").append(name);
		if(next.getParentElement() != null) {
			List<?> children = next.getParentElement().getChildren(name, namespace);
			if(children.size() > 1) {
				buffer.append('[').append(children.indexOf(next)+1).append(']');
			}
		}
	}

	public static void removeWithheldElements(Element metadata, SettingManager sm) throws JDOMException {
        boolean keepMarkedElement = sm.getValueAsBool("system/"+Geonet.Config.HIDE_WITHHELD_ELEMENTS+"/keepMarkedElement", false);
        List<?> nodes = Xml.selectNodes(metadata, "*[@gco:nilReason = '"+WITHHELD+"'] | *//*[@gco:nilReason = '"+WITHHELD+"']", XML_SELECT_NAMESPACE);
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

		xml = removeGeonetElements(xml);

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
			args.add(Integer.valueOf(groupOwner));
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
     * @param uuid null to not update metadata uuid column or the uuid value to be used for the update.
     * @throws SQLException
     */
	protected void updateDb(Dbms dbms, String id, Element xml, String changeDate, String root, boolean updateDateStamp, String uuid) throws SQLException {
		if (resolveXLinks()) Processor.removeXLink(xml);
		xml = removeGeonetElements(xml);
        if (logEmptyWithheld(id, xml, "XmlSerializer.updateDb")) {
            StackTraceElement[] stacktrace = new Exception("").getStackTrace();
            StringBuffer info = new StringBuffer();
            info.append('[').append(WITHHELD).append(']');
            info.append("Extra information related to updating the metadata with an empty withheld element:");
            final String indent = "\n    ";
            ServiceContext serviceContext = ServiceContext.get();
            if (serviceContext != null) {
                UserSession userSession = serviceContext.getUserSession();
                if (userSession != null) {
                    UserSession session = userSession;
                    info.append(indent).append("User: ").append(session.getUsername());
                    info.append(indent).append("Userid: ").append(session.getUserId());
                }
                info.append(indent).append("IP: ").append(serviceContext.getIpAddress());
            }

            info.append(indent).append("StackTrace: ");
            final String doubleIndent = "\n        ";
            for (int i = 0; i < stacktrace.length; i++) {
                StackTraceElement traceElement = stacktrace[i];
                if (traceElement.getClassName().startsWith("org.fao.geonet")) {
                    info.append(doubleIndent).append(traceElement.getClassName()).append('.').append(traceElement.getMethodName())
                            .append('(').append(traceElement.getLineNumber()).append(')');
                }
            }
            Log.warning(Geonet.DATA_MANAGER, info.toString());
        }

		fixCR(xml);
		String metadata = Xml.getString(xml);
		int metadataId = Integer.valueOf(id);
		
        if (updateDateStamp)  {
            if (changeDate == null)	{
                changeDate = new ISODate().toString();
            }
            if (uuid != null)  {
                String queryWithUUIDUpdate = "UPDATE Metadata SET data=?, changeDate=?, root=?, uuid=? WHERE id=?";
                dbms.execute(queryWithUUIDUpdate, metadata, changeDate, root, uuid, metadataId);
            } else {
                String query = "UPDATE Metadata SET data=?, changeDate=?, root=? WHERE id=?";
                dbms.execute(query, metadata, changeDate, root, metadataId);
            }
        } else {
            if (uuid != null)  {
                String queryMinorWithUUIDUpdate = "UPDATE Metadata SET data=?, root=?, uuid=? WHERE id=?";
                dbms.execute(queryMinorWithUUIDUpdate, metadata, root, uuid, metadataId);
            } else {
                String queryMinor = "UPDATE Metadata SET data=?, root=? WHERE id=?";
                dbms.execute(queryMinor, metadata, root, metadataId);
            }
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
		dbms.execute(query, Integer.valueOf(id));
	}

    /**
     *
     * @param xml
     */
	private void fixCR(Element xml) {
		List<?> list = xml.getChildren();
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

	private Element removeGeonetElements(Element md) {
    //--- purge geonet: attributes

    List listAtts = md.getAttributes();
    for (int i=0; i<listAtts.size(); i++) {
      Attribute attr = (Attribute) listAtts.get(i);
      if (Edit.NAMESPACE.getPrefix().equals(attr.getNamespacePrefix())) {
        attr.detach();
        i--;
      }
    }

    //--- purge geonet: children
    List list = md.getChildren();
    for (int i=0; i<list.size(); i++) {
      Element child = (Element) list.get(i);
      if (!Edit.NAMESPACE.getPrefix().equals(child.getNamespacePrefix()))
        removeGeonetElements(child);
      else {
        child.detach();
        i--;
      }
    }

		return md;
  }

	/* API to be overridden by extensions */

	public abstract void delete(Dbms dbms, String table, String id, ServiceContext context) 
	   throws Exception;

	public abstract void update(Dbms dbms, String id, Element xml, 
		 String changeDate, boolean updateDateStamp, String uuid, ServiceContext context) 
		 throws Exception;

	public abstract String insert(Dbms dbms, String schema, Element xml, 
					 int serial, String source, String uuid, String createDate,
					 String changeDate, String isTemplate, String title,
			 int owner, String groupOwner, String docType, ServiceContext context) 
			 throws Exception;

	
	public abstract Element select(Dbms dbms, String table, String id, ServiceContext context) 
			 throws Exception;
	public abstract Element selectNoXLinkResolver(Dbms dbms, String table, String id, boolean isIndexingTask) 
			 throws Exception;
} 
