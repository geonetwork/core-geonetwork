package org.fao.geonet.services.extent;

import jeeves.utils.Log;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geocat;
import org.geotools.data.DataStore;
import org.geotools.util.logging.Logging;
import org.jdom.Element;

import java.io.IOException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static org.fao.geonet.services.extent.ExtentHelper.*;

/**
 * The configuration object for Extents. It allows access to the Datastore(s)
 * for obtaining the extents
 *
 * @author jeichar
 */
public class ExtentManager {

    private static ExtentManager instance;
    public static final String GEOTOOLS_LOG_NAME = "geotools";

    public static ExtentManager getInstance() {
        return instance;
    }

    private final class SourcesLogHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            Log.debug(GEOTOOLS_LOG_NAME, record.getMessage());
        }

        @Override
        public void flush() {
            // nothing
        }

        @Override
        public void close() throws SecurityException {
            // nothing

        }
    }

    private final Map<String, Source> sources = new HashMap<String, Source>();

    public ExtentManager(DataStore dataStore, java.util.List<Element> extentConfig) throws Exception {
        instance = this;
        if (Logger.getLogger(GEOTOOLS_LOG_NAME).isDebugEnabled()) {
            Logging.getLogger("org.geotools.data").setLevel(java.util.logging.Level.FINE);
            Logging.getLogger("org.geotools.data").addHandler(new SourcesLogHandler());
        }
        if (extentConfig == null) {
            Log.error(Geocat.Module.EXTENT, "No Extent configuration found.");
        } else {
            Element sourceElem = extentConfig.get(0);

                String id = sourceElem.getAttributeValue(ID);
                if (id == null) {
                    id = DEFAULT_SOURCE_ID;
                }

                final Source source = new Source(id);

                sources.put(id, source);

                source.datastore = dataStore;

                for (final Object obj : sourceElem.getChildren(TYPENAME)) {
                    final Element elem = (Element) obj;
                    final String typename = elem.getAttributeValue(TYPENAME);
                    final String idColumn = elem.getAttributeValue(ID_COLUMN);
                    if (idColumn == null) {
                        throw new Exception("the idColumn attribute for extent source configuration " + typename +"is missing");
                    }

                    final String projection = elem.getAttributeValue("CRS");
                    final String descColumn = elem.getAttributeValue(DESC_COLUMN);
                    final String geoIdColumn = elem.getAttributeValue(GEO_ID_COLUMN);
                    final String searchColumn = elem.getAttributeValue("searchColumn");
                    final String modifiable = elem.getAttributeValue(MODIFIABLE_FEATURE_TYPE);

                    source.addFeatureType(typename, idColumn, geoIdColumn, descColumn, searchColumn, projection, "true"
                            .equalsIgnoreCase(modifiable));
                }

        }

    }

    public DataStore getDataStore() throws IOException {
        return sources.get(DEFAULT_SOURCE_ID).getDataStore();
    }

    public DataStore getDataStore(String id) throws IOException {
        final String concId = id == null ? DEFAULT_SOURCE_ID : id;
        return sources.get(concId).getDataStore();
    }

    public Map<String, Source> getSources() {
        return sources;
    }

    public Source getSource(String source) {
        if (source == null) {
            return sources.get(DEFAULT_SOURCE_ID);
        }
        return sources.get(source);
    }

    public Source getSource() {
        return sources.get(DEFAULT_SOURCE_ID);
    }

}
