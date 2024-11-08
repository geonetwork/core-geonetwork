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
import java.util.List;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

/**
 * This class is responsible of reading and writing xml on the database. It works on tables like
 * (id, data, lastChangeDate).
 */
public abstract class XmlSerializer {
    private static InheritableThreadLocal<ThreadLocalConfiguration> configThreadLocal = new InheritableThreadLocal<XmlSerializer.ThreadLocalConfiguration>();

    public static ThreadLocalConfiguration getThreadLocal(boolean setIfNotPresent) {
        ThreadLocalConfiguration config = configThreadLocal.get();
        if (config == null && setIfNotPresent) {
            config = new ThreadLocalConfiguration();
            configThreadLocal.set(config);
        }

        return config;
    }

    public static void clearThreadLocal() {
        configThreadLocal.set(null);
    }

    public static void removeFilteredElement(Element metadata,
                                             final MetadataSchemaOperationFilter filter,
                                             List<Namespace> namespaces) throws JDOMException {
        // xPathAndMarkedElement seem can be null in some schemas like dublin core
        if (filter == null) return;

        String xpath = filter.getXpath();
        Element mark = filter.getMarkedElement();

        List<?> nodes = Xml.selectNodes(metadata,
            xpath,
            namespaces);
        for (Object object : nodes) {
            if (object instanceof Element) {
                Element element = (Element) object;
                if (mark != null) {
                    element.removeContent();

                    // Remove attributes
                    @SuppressWarnings("unchecked")
                    List<Attribute> atts = new ArrayList<>(element.getAttributes());
                    for (Attribute attribute : atts) {
                        attribute.detach();
                    }

                    // Insert attributes or children element of the mark
                    List<Attribute> markAtts = new ArrayList<>(mark.getAttributes());
                    for (Attribute attribute : markAtts) {
                        element.setAttribute((Attribute) attribute.clone());
                    }
                    for (Object o : mark.getChildren()) {
                        if (o instanceof Element) {
                            Element e = (Element) o;
                            element.setContent((Element) e.clone());
                        }
                    }
                } else {
                    element.detach();
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean resolveXLinks() {
        SettingManager _settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        if (_settingManager == null) { // no initialization, no XLinks
            Log.error(Geonet.DATA_MANAGER, "No settingManager in XmlSerializer, XLink Resolver disabled.");
            return false;
        }

    String xlR = _settingManager.getValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE);
        if (xlR != null) {
            boolean isEnabled = xlR.equals("true");
            if (isEnabled) Log.debug(Geonet.DATA_MANAGER, "XLink Resolver enabled.");
            else Log.debug(Geonet.DATA_MANAGER, "XLink Resolver disabled.");
            return isEnabled;
        } else {
            Log.error(Geonet.DATA_MANAGER, "XLink resolver setting does not exist! XLink Resolver disabled.");
            return false;
        }
    }

    public boolean isLoggingEmptyWithHeld() {
        SettingManager _settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        if (_settingManager == null) {
            return false;
        }

        String enableLogging = _settingManager.getValue(Settings.SYSTEM_HIDEWITHHELDELEMENTS_ENABLE_LOGGING);
        if (enableLogging != null) {
            return enableLogging.equals("true");
        } else {
            return false;
        }
    }

    /**
     * Retrieves the xml element which id matches the given one. The element is read from 'table'
     * and the string read is converted into xml.
     *
     * @param isIndexingTask If true, then withheld elements are not removed.
     * @param applyOperationsFilters If true, then withheld elements are filtered according to user privileges.
     */
    protected Element internalSelect(String id, boolean isIndexingTask, boolean applyOperationsFilters) throws Exception {
        IMetadataUtils _metadataUtils = ApplicationContextHolder.get().getBean(IMetadataUtils.class);

        AbstractMetadata metadata = _metadataUtils.findOne(Integer.parseInt(id));

        if (metadata == null)
            return null;

        return removeHiddenElements(isIndexingTask, metadata, applyOperationsFilters);
    }

    public Element removeHiddenElements(boolean isIndexingTask, AbstractMetadata metadata, boolean applyOperationsFilters) throws Exception {
        AccessManager accessManager = ApplicationContextHolder.get().getBean(AccessManager.class);
        DataManager _dataManager = ApplicationContextHolder.get().getBean(DataManager.class);

        String id = String.valueOf(metadata.getId());
        Element metadataXml = metadata.getXmlData(false);
        if (!isIndexingTask && applyOperationsFilters) {
            ServiceContext context = ServiceContext.get();
            MetadataSchema mds = _dataManager.getSchema(metadata.getDataInfo().getSchemaId());

            // Check if a filter is defined for this schema
            // for the editing operation ie. user who can not edit
            // will not see those elements.
            MetadataSchemaOperationFilter editFilter = mds.getOperationFilter(ReservedOperation.editing);
            boolean filterEditOperationElements = editFilter != null;
            List<Namespace> namespaces = mds.getNamespaces();
            if (context != null) {
                if (editFilter != null) {
                    boolean canEdit = accessManager.canEdit(context, id);
                    if (canEdit) {
                        filterEditOperationElements = false;
                    }
                }

                MetadataSchemaOperationFilter authenticatedFilter = mds.getOperationFilter("authenticated");
                if (authenticatedFilter != null) {
                    boolean isAuthenticated = context.getUserSession().isAuthenticated();
                    if (!isAuthenticated) {
                        removeFilteredElement(metadataXml, authenticatedFilter, namespaces);
                    }
                }

                MetadataSchemaOperationFilter downloadFilter = mds.getOperationFilter(ReservedOperation.download);
                if (downloadFilter != null) {
                    boolean canDownload = accessManager.canDownload(context, id);
                    if (!canDownload) {
                        removeFilteredElement(metadataXml, downloadFilter, namespaces);
                    }
                }
                MetadataSchemaOperationFilter dynamicFilter = mds.getOperationFilter(ReservedOperation.dynamic);
                if (dynamicFilter != null) {
                    boolean canDynamic = accessManager.canDynamic(context, id);
                    if (!canDynamic) {
                        removeFilteredElement(metadataXml, dynamicFilter, namespaces);
                    }
                }
            }
            if (filterEditOperationElements || (getThreadLocal(false) != null && getThreadLocal(false).forceFilterEditOperation)) {
                removeFilteredElement(metadataXml, editFilter, namespaces);
            }
        }
        return metadataXml;
    }

    /**
     * TODO javadoc.
     *
     * @param newMetadata the metadata to insert
     * @param dataXml     the data to set on the metadata before saving
     * @param context     a service context
     * @return the saved metadata
     */
    protected AbstractMetadata insertDb(final AbstractMetadata newMetadata, final Element dataXml, ServiceContext context) throws SQLException {
        if (resolveXLinks()) Processor.removeXLink(dataXml);

        newMetadata.setData(Xml.getString(dataXml));
        return context.getBean(IMetadataManager.class).save(newMetadata);
    }

    /**
     * Updates an xml element into the database. The new data replaces the old one.
     *
     * @param uuid null to not update metadata uuid column or the uuid value to be used for the
     *             update.
     */
    protected void updateDb(final String id, final Element xml, final String changeDate, final String root,
                            final boolean updateDateStamp,
                            final String uuid) throws SQLException {
        if (resolveXLinks()) Processor.removeXLink(xml);

        IMetadataManager _metadataManager = ApplicationContextHolder.get().getBean(IMetadataManager.class);
        IMetadataUtils metadataUtils = ApplicationContextHolder.get().getBean(IMetadataUtils.class);

        int metadataId = Integer.valueOf(id);
        AbstractMetadata md = metadataUtils.findOne(metadataId);

        md.setDataAndFixCR(xml);

        if (updateDateStamp) {
            if (changeDate == null) {
                md.getDataInfo().setChangeDate(new ISODate());
            } else {
                md.getDataInfo().setChangeDate(new ISODate(changeDate));
            }
        }

        if (uuid != null) {
            md.setUuid(uuid);
        }

        _metadataManager.save(md);
    }

    /**
     * Deletes an xml element given its id.
     */

    protected void deleteDb(String id) throws Exception {
        IMetadataManager _metadataManager = ApplicationContextHolder.get().getBean(IMetadataManager.class);

        // TODO: Ultimately we want to remove any xlinks in this document
        // that aren't already in use from the xlink cache. For now we
        // rely on the admin clearing cache and reindexing regularly
        _metadataManager.delete(Integer.valueOf(id));

//        Assert.isTrue(!_metadataRepository.exists(Integer.valueOf(id)), "Metadata should have been deleted");

    }

    public abstract void delete(String id, ServiceContext context)
        throws Exception;

	/* API to be overridden by extensions */

    public abstract void update(String id, Element xml,
                                String changeDate, boolean updateDateStamp, String uuid, ServiceContext context)
        throws Exception;

    public abstract AbstractMetadata insert(AbstractMetadata metadata, Element dataXml, ServiceContext context)
        throws Exception;

    /**
     * Return metadata xml.
     *
     * @param id the metadata id.
     */
    public abstract Element select(ServiceContext context, String id) throws Exception;

    public abstract Element selectNoXLinkResolver(String id, boolean isIndexingTask, boolean applyOperationsFilters)
        throws Exception;

    public static class ThreadLocalConfiguration {
        private boolean forceFilterEditOperation = false;

        public boolean isForceFilterEditOperation() {
            return forceFilterEditOperation;
        }

        public void setForceFilterEditOperation(boolean forceFilterEditOperation) {
            this.forceFilterEditOperation = forceFilterEditOperation;
        }
    }
}
