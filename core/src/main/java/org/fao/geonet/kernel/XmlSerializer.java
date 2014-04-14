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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.util.Assert;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import jeeves.xlink.Processor;

import org.apache.log4j.Priority;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;

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

	public static class ThreadLocalConfiguration {
	    private boolean forceHideWithheld = false;

        public boolean isForceHideWithheld() {
            return forceHideWithheld;
        }
        public void setForceHideWithheld(boolean forceHideWithheld) {
            this.forceHideWithheld = forceHideWithheld;
        }
	}

    @Autowired
    protected SettingManager _settingManager;
    @Autowired
    private MetadataRepository _metadataRepository;

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
     * @return
     */
	public boolean resolveXLinks() {
		if (_settingManager == null) { // no initialization, no XLinks
			Log.error(Geonet.DATA_MANAGER,"No settingManager in XmlSerializer, XLink Resolver disabled.");
			return false; 
		}

		String xlR = _settingManager.getValue("system/xlinkResolver/enable");
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
     *
     * @param id
     * @param isIndexingTask If true, then withheld elements are not removed.
     * @return
     * @throws Exception
     */
	protected Element internalSelect(String id, boolean isIndexingTask) throws Exception {
        Metadata metadata = _metadataRepository.findOne(id);

		if (metadata == null)
			return null;

		String xmlData = metadata.getData();
		Element metadataXml = Xml.loadString(xmlData, false);

		logEmptyWithheld(id, metadataXml, "XmlSerializer.internalSelect");
		
		if (!isIndexingTask) { 
			boolean hideWithheldElements = _settingManager.getHideWitheldElements();
			ServiceContext context = ServiceContext.get();
    		if(context != null) {
    			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
    			boolean canEdit = gc.getBean(AccessManager.class).canEdit(context, id);
    			if(canEdit) {
    				hideWithheldElements = false;
    			}
    		}
    		if (hideWithheldElements || (getThreadLocal(false) != null && getThreadLocal(false).forceHideWithheld)) {
    		    removeWithheldElements(metadataXml, _settingManager);
    		}
		}
		return (Element) metadataXml.detach();
	}

    private boolean logEmptyWithheld(String id, Element metadata, String methodName) {
        boolean hideWithheldElements = _settingManager.getValueAsBool("system/" + Geonet.Config.HIDE_WITHHELD_ELEMENTS + "/enable", false);
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
     *
     * @param newMetadata the metadata to insert
     * @param dataXml the data to set on the metadata before saving
     * @param context a service context
     * @return the saved metadata
     * @throws SQLException
     */
	protected Metadata insertDb(final Metadata newMetadata, final Element dataXml,ServiceContext context) throws SQLException {
		if (resolveXLinks()) Processor.removeXLink(dataXml);

        newMetadata.setData(Xml.getString(dataXml));
        Metadata savedMetadata = _metadataRepository.save(newMetadata);
		return savedMetadata;
	}

    /**
     *  Updates an xml element into the database. The new data replaces the old one.
     *
     * @param id
     * @param xml
     * @param changeDate
     * @param updateDateStamp
     * @param uuid null to not update metadata uuid column or the uuid value to be used for the update.
     * @throws SQLException
     */
	protected void updateDb(final String id, final Element xml, final String changeDate, final String root,
                            final boolean updateDateStamp,
                            final String uuid) throws SQLException {

		if (resolveXLinks()) Processor.removeXLink(xml);
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
        int metadataId = Integer.valueOf(id);
        Metadata md = _metadataRepository.findOne(metadataId);

        md.setDataAndFixCR(xml);

        if (updateDateStamp)  {
            if (changeDate == null)	{
                md.getDataInfo().setChangeDate( new ISODate());
            } else {
                md.getDataInfo().setChangeDate( new ISODate(changeDate));
            }
        }

        if (uuid != null) {
            md.setUuid(uuid);
        }

        _metadataRepository.save(md);
	}

    /**
     * Deletes an xml element given its id.
     *
     *
     * @param id
     * @throws SQLException
     */
	protected void deleteDb(String id) throws Exception {
		// TODO: Ultimately we want to remove any xlinks in this document
		// that aren't already in use from the xlink cache. For now we
		// rely on the admin clearing cache and reindexing regularly
        _metadataRepository.delete(Integer.valueOf(id));

//        Assert.isTrue(!_metadataRepository.exists(Integer.valueOf(id)), "Metadata should have been deleted");

	}

	/* API to be overridden by extensions */

	public abstract void delete(String id, ServiceContext context)
	   throws Exception;

	public abstract void update(String id, Element xml,
                                String changeDate, boolean updateDateStamp, String uuid, ServiceContext context)
		 throws Exception;

	public abstract Metadata insert(Metadata metadata, Element dataXml, ServiceContext context)
			 throws Exception;


    /**
     * Return metadata xml.
     * @param id the metadata id.
     */
	public abstract Element select(ServiceContext context, String id) throws Exception;
	public abstract Element selectNoXLinkResolver(String id, boolean isIndexingTask)
			 throws Exception;
} 
