package org.fao.geonet.services.api.directory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.fao.geonet.domain.Metadata;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;


/**
 * List of entries collected in a metadata record
 */
public class CollectResults {
    private Metadata record;
    private Element updatedRecord;
    public boolean isRecordUpdated() {
        return recordUpdated;
    }

    boolean recordUpdated = false;
    /**
     * Table composed of the entry identifier (extracted from the Element),
     * the UUID and the XML Element.
     */
    private Table<String, String, Element> entries =
            HashBasedTable.create();

    /**
     * Map of UUID and db identifiers if the entry is saved in the database
     */
    private Map<String, Integer> entryIdentifiers = new HashMap<>();

    public CollectResults(Metadata record) {
        this.record = record;
    }

    public Metadata getRecord() {
        return record;
    }

    public CollectResults setRecord(Metadata record) {
        this.record = record;
        return this;
    }

    public Table<String, String, Element> getEntries() {
        return entries;
    }

    public Map<String, Integer> getEntryIdentifiers() {
        return entryIdentifiers;
    }

    public CollectResults setEntryIdentifiers(Map<String, Integer> entryIdentifiers) {
        this.entryIdentifiers = entryIdentifiers;
        return this;
    }

    public Element getUpdatedRecord() {
        return updatedRecord;
    }

    public CollectResults setUpdatedRecord(Element updatedRecord) {
        this.updatedRecord = updatedRecord;
        this.recordUpdated = updatedRecord != null;
        return this;
    }
}
