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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.util.Assert;
import jeeves.ThreadLocalCleaner;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneIndexField;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;

/**
 * This class is responsible of reading and writing xml on the database. 
 * It works on tables like (id, data, lastChangeDate).
 */
public abstract class XmlSerializer {
    private static final Set<ReservedOperation> WITHHELD_OPS = Sets.newHashSet(
            ReservedOperation.editing,
            ReservedOperation.dynamic,
            ReservedOperation.download);
    private static final Set<String> WITHHELD_FIELDS_IN_INDEX = Sets.newHashSet();
    static {
        for (ReservedOperation withheldOp : WITHHELD_OPS) {
            WITHHELD_FIELDS_IN_INDEX.add(LuceneIndexField.WITHHELD_OP_PREFIX + withheldOp.name());
        }
    }
    @VisibleForTesting
    static final String WITH_HELD_COMMENT = "Data_Withheld_";

    public static class ThreadLocalConfiguration {
	    private boolean forceFilterEditOperation = false;

        public boolean isForceFilterEditOperation() {
            return forceFilterEditOperation;
        }
        public void setForceFilterEditOperation(boolean forceFilterEditOperation) {
            this.forceFilterEditOperation = forceFilterEditOperation;
        }
	}

    @Autowired
    protected SchemaManager schemaManager;
    @Autowired
    protected SettingManager _settingManager;
    @Autowired
    protected SearchManager searchManager;
    @Autowired
    protected DataManager _dataManager;
    @Autowired
    private MetadataRepository _metadataRepository;
    @Autowired
    private ThreadLocalCleaner threadLocalCleaner;

    private static ThreadLocal<ThreadLocalConfiguration> configThreadLocal = new InheritableThreadLocal<>();

    public static ThreadLocalConfiguration getThreadLocal(boolean setIfNotPresent) {
        ThreadLocalConfiguration config = configThreadLocal.get();
        if (config == null && setIfNotPresent) {
            config = new ThreadLocalConfiguration();
            configThreadLocal.set(config);
        }

        return config;
    }

    @PostConstruct
    void init() {
        configThreadLocal = threadLocalCleaner.createInheritableThreadLocal(ThreadLocalConfiguration.class);
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

        if (!isIndexingTask) {
            ServiceContext context = ServiceContext.get();
            MetadataSchema mds = _dataManager.getSchema(metadata.getDataInfo().getSchemaId());

            // Check if a filter is defined for this schema
            // for the editing operation ie. user who can not edit
            // will not see those elements.
            Pair<String, Element> editXpathFilter = mds.getOperationFilter(ReservedOperation.editing);
            boolean filterEditOperationElements = editXpathFilter != null;
            List<Namespace> namespaces = mds.getNamespaces();
            if (context != null) {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                AccessManager am = gc.getBean(AccessManager.class);
                if (editXpathFilter != null) {
                    boolean canEdit = am.canEdit(context, id);
                    if (canEdit) {
                        filterEditOperationElements = false;
                    }
                }
                Pair<String, Element> downloadXpathFilter = mds.getOperationFilter(ReservedOperation.download);
                if (downloadXpathFilter != null) {
                    boolean canDownload = am.canDownload(context, id);
                    if (!canDownload) {
                        removeFilteredElement(_settingManager, metadataXml, ReservedOperation.download, downloadXpathFilter, namespaces);
                    }
                }
                Pair<String, Element> dynamicXpathFilter = mds.getOperationFilter(ReservedOperation.dynamic);
                if (dynamicXpathFilter != null) {
                    boolean canDynamic = am.canDynamic(context, id);
                    if (!canDynamic) {
                        removeFilteredElement(_settingManager, metadataXml, ReservedOperation.dynamic, dynamicXpathFilter, namespaces);
                    }
                }
            }
            if (filterEditOperationElements || (getThreadLocal(false) != null && getThreadLocal(false).forceFilterEditOperation)) {
                removeFilteredElement(_settingManager, metadataXml, ReservedOperation.editing, editXpathFilter, namespaces);
            }
        }
        return (Element) metadataXml.detach();
    }

    public static void removeFilteredElement(SettingManager manager,
                                             Element metadata,
                                             ReservedOperation operation,
                                             final Pair<String, Element> xPathAndMarkedElement,
                                             List<Namespace> namespaces) throws JDOMException {
        // xPathAndMarkedElement seem can be null in some schemas like dublin core
        if (xPathAndMarkedElement == null) return;

        String xpath = xPathAndMarkedElement.one();
        Element mark = xPathAndMarkedElement.two();

        List<?> nodes = Xml.selectNodes(metadata,
                xpath,
                namespaces);
        for (Object object : nodes) {
            if (object instanceof Element) {
                Element element = (Element) object;
                if(mark != null) {
                    element.removeContent();
                    element.addContent(new Comment(createWithheldCommentText(manager, operation)));
                    // Remove attributes
                    @SuppressWarnings("unchecked")
                    List<Attribute> atts = new ArrayList<>(element.getAttributes());
                    for (Attribute attribute : atts) {
                        attribute.detach();
                    }

                    // Insert attributes or children element of the mark
                    @SuppressWarnings("unchecked")
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

    private static String createWithheldCommentText(SettingManager settingManager, ReservedOperation operation) {
        return WITH_HELD_COMMENT + operation.name() + "_" + settingManager.getSiteId();
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
        removeWithheldComments(dataXml);
        newMetadata.setDataAndFixCR(dataXml);
        return _metadataRepository.save(newMetadata);
	}

    private void removeWithheldComments(Element dataXml) {
        List<Comment> toDetach = new ArrayList<>();
        final Iterator descendants = dataXml.getDescendants();
        while (descendants.hasNext()) {
            Object next = descendants.next();

            if (next instanceof Comment) {
                Comment comment = (Comment) next;
                if (comment.getText().startsWith(WITH_HELD_COMMENT)) {
                    toDetach.add(comment);
                }
            }
        }

        for (Comment comment : toDetach) {
            comment.detach();
        }
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
    protected void updateDb(final String id, final Element xml, final String changeDate,
                            final boolean updateDateStamp,
                            final String uuid) throws SQLException {
        if (resolveXLinks()) Processor.removeXLink(xml);

        int metadataId = Integer.valueOf(id);
        Metadata md = _metadataRepository.findOne(metadataId);

        assertWithheldElementsAreFull(md, xml, schemaManager.getSchema(md.getDataInfo().getSchemaId()));

        md.setDataAndFixCR(xml);
        md.getDataInfo().setRoot(xml.getQualifiedName());
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

        _metadataRepository.save(md);
    }

    private void assertWithheldElementsAreFull(Metadata unmodifiedMetadata, Element updatedXml, MetadataSchema schema) {
        if (!unmodifiedMetadata.getHarvestInfo().isHarvested()) {
            try (IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1)) {
                final IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
                final Term idTerm = new Term(LuceneIndexField.ID, String.valueOf(unmodifiedMetadata.getId()));
                final TopDocs doc = searcher.search(new TermQuery(idTerm), 1);
                Assert.equals(1, doc.totalHits, "No document with id '" + unmodifiedMetadata.getId() + "' was found in the index");

                final Document document = indexAndTaxonomy.indexReader.document(doc.scoreDocs[0].doc, WITHHELD_FIELDS_IN_INDEX);
                Set<ReservedOperation> checkRequired = Sets.newHashSet();
                for (String field : WITHHELD_FIELDS_IN_INDEX) {
                    String value = document.get(field);
                    if ("y".equals(value)) {
                        String opName = field.substring(LuceneIndexField.WITHHELD_OP_PREFIX.length());
                        final ReservedOperation op = ReservedOperation.valueOf(opName);
                        checkRequired.add(op);
                    }
                }

                if (!checkRequired.isEmpty()) {
                    final Iterator descendants = updatedXml.getDescendants();
                    while (descendants.hasNext()) {
                        Object next = descendants.next();
                        if (next instanceof Comment) {
                            Comment comment = (Comment) next;
                            if (isWithheldComment(comment)) {
                                String msg = "ERROR trying to update a metadata where withheld data has been stripped in metadata: "
                                                 + unmodifiedMetadata.getId();
                                final IllegalStateException exception = new IllegalStateException(msg);
                                Log.error(Geonet.DATA_MANAGER, msg,
                                        exception);
                                throw exception;

                            }
                        }
                    }

                    final Element unmodifiedXml = unmodifiedMetadata.getXmlData(false);
                    for (ReservedOperation operation : checkRequired) {
                        final Pair<String, Element> operationFilter = schema.getOperationFilter(operation);
                        if (operationFilter != null) {
                            String xpath = operationFilter.one();
                            if (Xml.selectNodes(updatedXml, xpath, schema.getNamespaces()).isEmpty()) {
                                int numWithheld = Xml.selectNodes(unmodifiedXml, xpath, schema.getNamespaces()).size();
                                Throwable e = new RuntimeException();
                                Log.warning(Geonet.DATA_MANAGER, "Possible error with withheld elements.  Before update there was: " + numWithheld + " after update there were none.", e);

                            }
                        }

                    }
                }
            } catch (IOException | JDOMException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private boolean isWithheldComment(Comment comment) {
        String text = comment.getText();
        for (ReservedOperation op : WITHHELD_OPS) {
            if (createWithheldCommentText(_settingManager, op).equals(text)) {
                return true;
            }
        }
        return false;
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
