package org.fao.geonet.api.registries;

import com.google.common.collect.Table;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.XLink;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.*;

import java.util.*;

/**
 * Created by francois on 11/03/16.
 */
public class DirectoryUtils {
    public static final char SEPARATOR = '~';
    private static final String LOGGER = DirectoryApi.LOGGER;

    /**
     * Save entries and metadata
     */
    public static Map<String, Exception> saveEntries(ServiceContext context,
                                                     CollectResults collectResults,
                                                     String sourceIdentifier,
                                                     Integer owner,
                                                     Integer groupOwner,
                                                     boolean saveRecord) {
        DataManager dataManager = context.getBean(DataManager.class);
        IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        Table<String, String, Element> entries = collectResults.getEntries();
        Iterator<String> entriesIterator =
            entries.rowKeySet().iterator();
        AbstractMetadata record = collectResults.getRecord();
        boolean validate = false, ufo = false,
            notify = false, publicForGroup = true, refreshReaders = false;
        Map<String, Exception> errors = new HashMap<>();

        while (entriesIterator.hasNext()) {
            String identifier = entriesIterator.next();
            Map<String, Element> uuidAndEntry = entries.row(identifier);
            // Only one entry in the map expected
            String uuid = uuidAndEntry.keySet().iterator().next();
            Element entry = uuidAndEntry.values().iterator().next();

            AbstractMetadata dbSubTemplate = metadataRepository.findOneByUuid(uuid);
            if (dbSubTemplate == null) {
                AbstractMetadata subtemplate = new Metadata();
                subtemplate.setUuid(uuid);
                subtemplate.getDataInfo().
                    setSchemaId(record.getDataInfo().getSchemaId()).
                    setRoot(entry.getQualifiedName()).
                    setType(MetadataType.SUB_TEMPLATE);
                subtemplate.getSourceInfo().
                    setSourceId(sourceIdentifier).
                    setOwner(owner).
                    setGroupOwner(groupOwner);
                try {
                    subtemplate = dataManager.insertMetadata(
                        context,
                        subtemplate,
                        (Element) entry.clone(),
                        IndexingMode.none, ufo,
                        UpdateDatestamp.NO,
                        publicForGroup, refreshReaders);

                    collectResults.getEntryIdentifiers().put(
                        uuid, subtemplate.getId());
                    // TODO: Set categories ? privileges
                } catch (Exception e) {
                    Log.error(LOGGER, e.getMessage(), e);
                    errors.put(uuid, e);
                }
            } else {
                try {
                    dataManager.updateMetadata(
                        context, "" + dbSubTemplate.getId(),
                        (Element) entry.clone(),
                        validate, ufo, context.getLanguage(),
                        new ISODate().toString(), false, IndexingMode.none);
                    collectResults.getEntryIdentifiers().put(
                        uuid, dbSubTemplate.getId());
                } catch (Exception e) {
                    Log.error(LOGGER, e.getMessage(), e);
                    errors.put(uuid, e);
                }
            }
        }
        if (saveRecord) {
            try {
                dataManager.updateMetadata(
                    context, "" + record.getId(), record.getXmlData(validate),
                    validate, ufo, context.getLanguage(),
                    new ISODate().toString(), true, IndexingMode.none);
            } catch (Exception e) {
                Log.error(LOGGER, e.getMessage(), e);
            }
        }
        return errors;
    }

    /**
     * Extract all entries matching a specific XPath. If an entry is found multiple times in the
     * record, and the identifier match, only one is reported (the last one).
     */
    public static CollectResults collectEntries(ServiceContext context,
                                                AbstractMetadata record,
                                                String xpath,
                                                String identifierXpath) throws Exception {
        return collectEntries(context, record, xpath, identifierXpath, null, false, false, null);
    }

    /**
     * Synchronize all entries in a record matching a specific XPath. If an entry is found in the
     * subtemplate list, the record one is updated. To preserve properties from the record, use the
     * propertiesToCopy
     */
    public static CollectResults synchronizeEntries(ServiceContext context,
                                                    AbstractMetadata record,
                                                    String xpath,
                                                    String identifierXpath,
                                                    List<String> propertiesToCopy,
                                                    boolean substituteAsXLink,
                                                    String directoryFilterQuery) throws Exception {
        return collectEntries(context, record, xpath, identifierXpath,
            propertiesToCopy, substituteAsXLink, true, directoryFilterQuery);
    }


    private static CollectResults collectEntries(ServiceContext context,
                                                 AbstractMetadata record,
                                                 String xpath,
                                                 String identifierXpath,
                                                 List<String> propertiesToCopy,
                                                 boolean substituteAsXLink,
                                                 boolean updateFromDirectory,
                                                 String directoryFilterQuery) throws Exception {
        CollectResults collectResults = new CollectResults(record);
        Map<String, List<Namespace>> namespaceList = new HashMap<String, List<Namespace>>();

        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

        if (Log.isDebugEnabled(LOGGER)) {
            Log.debug(LOGGER, String.format(
                "Collecting directory entries for record '%s' " +
                    "using XPath '%s' and identifier XPath '%s'.",
                record.getUuid(), xpath, identifierXpath
            ));
        }

        Element md = record.getXmlData(false);
        MetadataDataInfo mdInfo = record.getDataInfo();
        String localXlinkUrlPrefix = "local://" + context.getLanguage() + "/subtemplate?uuid=";

        // Build a list of all Namespaces in the metadata document
        List<Namespace> metadataNamespaces = namespaceList.get(mdInfo.getSchemaId());
        if (metadataNamespaces == null) {
            metadataNamespaces = new ArrayList<Namespace>();
            Namespace ns = md.getNamespace();
            if (ns != null) {
                metadataNamespaces.add(ns);
                metadataNamespaces.addAll(md.getAdditionalNamespaces());
                namespaceList.put(mdInfo.getSchemaId(), metadataNamespaces);
            }
        }

        // select all nodes that match the XPath
        List<?> nodes = Xml.selectNodes(md, xpath, metadataNamespaces);

        if (Log.isDebugEnabled(LOGGER)) {
            Log.debug(LOGGER, String.format(
                "%d nodes matching XPath '%s' in record '%s'.",
                nodes.size(), xpath, record.getUuid()
            ));
        }

        int numberOfEntries = 0;
        boolean updated = false;
        for (Iterator<?> iter = nodes.iterator(); iter.hasNext(); ) {
            Object o = iter.next();

            if (o instanceof Element) {
                numberOfEntries++;
                Element elem = (Element) o;
                if (Log.isDebugEnabled(LOGGER)) {
                    Log.debug(LOGGER, String.format(
                        "#%d. XML entry: %s",
                        numberOfEntries, Xml.getString(elem)
                    ));
                }

                // Extract identifier from the XPath provided
                String identifier = null;
                if (StringUtils.isNotEmpty(identifierXpath)) {
                    Object obj = Xml.selectSingle(
                        (Element) elem.clone(),
                        identifierXpath,
                        metadataNamespaces);
                    if (obj instanceof Text) {
                        identifier = ((Text) obj).getTextNormalize();
                    } else if (obj instanceof Attribute) {
                        identifier = ((Attribute) obj).getValue();
                    } else if (obj instanceof String) {
                        identifier = (String) obj;
                    }
                }

                // Extract the identifier from the @uuid attribute
                // This is a common location to store uuid in ISO19139 records
                if (StringUtils.isEmpty(identifier)) {
                    String uuidAttribute = elem.getAttributeValue("uuid");
                    if (StringUtils.isNotEmpty(uuidAttribute)) {
                        identifier = uuidAttribute;
                    }
                }

                // Still null, create a default one
                String uuid;
                if (StringUtils.isEmpty(identifier)) {
                    // Compute uuid based on sha1sum of the element content
                    // this is the way to generate this safely
                    // TODO: Maybe add an option to create randomUUID or SHA ?
                    // TODO: This does not work for a contact which has a role.
                    // In such case, it's better to define a specific UUID.
                    identifier = Sha1Encoder.encodeString(elem.getValue());
                    uuid = identifier;
                    //identifier = UUID.randomUUID().toString();
                } else {
                    uuid = Sha1Encoder.encodeString(identifier);
                }
                if (Log.isDebugEnabled(LOGGER)) {
                    Log.debug(LOGGER, String.format(
                        "#%d. Entry identifier is '%s', uuid is '%s'.",
                        numberOfEntries, identifier, uuid
                    ));
                }

                if (updateFromDirectory) {
                    if (Log.isDebugEnabled(LOGGER)) {
                        Log.debug(LOGGER, String.format(
                            "#%d. Searching '%s' in directory ...",
                            numberOfEntries, uuid
                        ));
                    }
                    // TODO: Add support for other type of subtemplate search
                    // Currently search uuid matching hash of the identifier
                    String searchIndexField = "uuid";
                    Element subTemplateElement = null;
                    // Search in DB by UUID matching entry UUID
                    if (StringUtils.isEmpty(searchIndexField)) {
                        AbstractMetadata subTemplate = metadataRepository.findOneByUuid(uuid);
                        if (subTemplate != null) {
                            subTemplateElement = subTemplate.getXmlData(false);
                        }
                    } else {
                        // or search in Lucene
                        Map<String, String> parameters = new HashMap();
                        if (directoryFilterQuery != null) {
                            String[] tokens = directoryFilterQuery.split(":");
                            if (tokens.length == 2) {
                                parameters.put(tokens[0], tokens[1]);
                            } else {
                                Log.warning(LOGGER, String.format(
                                    "Filter query for directory must be field:value format. '%s' is not.",
                                    directoryFilterQuery
                                ));
                            }
                        }
                        parameters.put(searchIndexField, uuid);
                        String entryUuid = search(context, parameters);
                        if (entryUuid != null) {
                            AbstractMetadata subTemplate = metadataRepository.findOneByUuid(entryUuid);
                            if (subTemplate != null) {
                                uuid = subTemplate.getUuid();
                                subTemplateElement = subTemplate.getXmlData(false);
                            }
                        }
                    }
                    // Check subtemplate is similar to
                    if (subTemplateElement == null) {
                        Log.debug(LOGGER, String.format(
                            "#%d. '%s' Not found in directory. Next.",
                            numberOfEntries, uuid
                        ));
                    } else if (subTemplateElement.getName().equals(
                        elem.getName()
                    )) {
                        // Replace the matching element
                        Element parent = elem.getParentElement();
                        int iIndex = parent.indexOf(elem);
                        parent.removeContent(iIndex);

                        if (!substituteAsXLink) {
                            copyProperties(elem, subTemplateElement, propertiesToCopy, metadataNamespaces);
                            parent.addContent(iIndex, subTemplateElement);
                        } else {
//                            <gmd:pointOfContact
//                                xmlns:xlink="http://www.w3.org/1999/xlink"
//                                xlink:href="local://eng/subtemplate?uuid=0c18725d-7884-4a09-8492-8b0626d958f2&amp;process=gmd:role/gmd:CI_RoleCode/@codeListValue~resourceProvider&amp;">
                            // TODO: Should depend on the setting for XLink
                            // TODO: Add languages
                            StringBuffer params = new StringBuffer(localXlinkUrlPrefix);
                            params.append(uuid);
                            params.append(getPropertiesAsParameters(
                                elem, propertiesToCopy, metadataNamespaces));
                            parent.setAttribute("uuidref", uuid);
                            parent.setAttribute("href",
                                params.toString(),
                                XLink.NAMESPACE_XLINK);
                        }
                        updated = true;
                    } else {
                        Log.debug(LOGGER, String.format(
                            "#%d. One found in directory but with different root element name '%s'.",
                            numberOfEntries, subTemplateElement.getName()
                        ));
                    }
                } else {
                    collectResults.getEntries().put(identifier, uuid, (Element) elem.clone());
                }
            } else {
                Log.debug(LOGGER, String.format(
                    "#%d. Only element can be directory entries. Current element is '%s'.",
                    numberOfEntries, o.getClass()
                ));
            }
        }
        if (updated) {
            collectResults.setUpdatedRecord(md);
        }
        return collectResults;
    }

    /**
     * @param propertiesXpath The text value of the matching element will be copied. It will not
     *                        copy a snippet. It will not create non existing element.
     */
    private static Element copyProperties(Element source, Element target,
                                          List<String> propertiesXpath,
                                          List<Namespace> ns) throws JDOMException {
        if (propertiesXpath != null) {
            for (String xpath : propertiesXpath) {
                String value = Xml.selectString(source, xpath, ns);
                if (StringUtils.isNotEmpty(value)) {
                    // Only the first one matching is updated
                    Object targetElement = Xml.selectSingle(target, xpath, ns);
                    if (targetElement instanceof Attribute) {
                        ((Attribute) targetElement).setValue(value);
                    } else if (targetElement instanceof Text) {
                        ((Text) targetElement).setText(value);
                    } else if (targetElement instanceof Element) {
                        ((Element) targetElement).setText(value);
                    } else {
                        // Unsupported
                    }
                }
            }
        }
        return target;
    }

    /**
     * Build process URL parameters containing XPath of values to be replaced in the target
     * subtemplate.
     * <p>
     * xlink:href="local://eng/subtemplate?uuid=0c18725d-7884-4a09-8492-8b0626d958f2&amp;process=gmd:role/gmd:CI_RoleCode/@codeListValue~resourceProvider&amp;
     */
    private static String getPropertiesAsParameters(Element source,
                                                    List<String> propertiesXpath,
                                                    List<Namespace> ns) throws JDOMException {
        StringBuffer properties = new StringBuffer();
        for (String xpath : propertiesXpath) {
            String value = Xml.selectString(source, xpath, ns);
            if (StringUtils.isNotEmpty(value)) {
                properties.append("&amp;").append("process=")
                    .append(xpath)
                    .append(SEPARATOR)
                    .append(value);
            }
        }
        return properties.toString();
    }

    /**
     * Search using Lucene a matching document
     *
     * @return The record identifier
     */
    private static String search(ServiceContext context, Map<String, String> searchParameters) {
        ServiceConfig _config = new ServiceConfig();

        EsSearchManager searchMan = context.getBean(EsSearchManager.class);

        StringBuilder query = new StringBuilder("+isTemplate:s");
        for (Map.Entry<String, String> e : searchParameters.entrySet()) {
            query.append(String.format(" +%s:\"%s\"", e.getKey(), e.getValue()));
        }

        try {
            SearchResponse results = searchMan.query(query.toString(), null, 0, 1);
            if (results.getHits().getTotalHits().value > 0) {
                return results.getHits().getHits()[0].getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
