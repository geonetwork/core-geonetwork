//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.spatial;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.apache.jcs.access.exception.CacheException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

/**
 * This class is responsible for extracting geographic information from metadata and writing that
 * information to a storage mechanism.
 *
 * @author jeichar
 */
@SuppressWarnings("unchecked")
public class SpatialIndexWriter implements FeatureListener {

    public static final String _IDS_ATTRIBUTE_NAME = "metadataid";
    public static final String _SPATIAL_INDEX_TYPENAME = "spatialindex";
    public static final int MAX_WRITES_IN_TRANSACTION = 1000;
    static final String SPATIAL_FILTER_JCS = "SpatialFilterCache";
    private static int _writes;
    private final Parser[] _parsers;
    private final Transaction _transaction;
    private final Lock _lock;
    private int _maxWrites;
    private FeatureStore<SimpleFeatureType, SimpleFeature> _featureStore;
    private STRtree _index;
    private Map<String, String> errorMessage;
    private Name _idColumn;
    private boolean _autocommit;

    /**
     * TODO: javadoc.
     *
     * @param maxWrites Maximum number of writes in a transaction. If set to 1 then AUTO_COMMIT is
     *                  being used.
     */
    public SpatialIndexWriter(DataStore datastore, Parser[] parsers,
                              Transaction transaction, int maxWrites, Lock lock)
        throws Exception {
        // Note: The Configuration takes a long time to create so it is worth
        // re-using the same Configuration
        _lock = lock;
        _parsers = parsers;
        for (Parser _parser : _parsers) {
          _parser.setStrict(false);
          _parser.setValidating(false);
        }
        _transaction = transaction;
        _maxWrites = maxWrites;

        _featureStore = createFeatureStore(datastore);
        _autocommit = maxWrites < 2;
        if (!_autocommit) {
            _featureStore.setTransaction(_transaction);
        }
        _featureStore.addFeatureListener(this);

    }

    /**
     * Extracts a Geometry Collection from metadata default visibility for testing access.
     */
    static MultiPolygon extractGeometriesFrom(Path schemaDir,
                                              Element metadata, Parser[] parsers, Map<String, String> errorMessage) throws Exception {
        return getSpatialExtent(schemaDir, metadata, parsers, new SpatialIndexingErrorHandler(errorMessage));
    }

    public static MultiPolygon getSpatialExtent(Path schemaDir, Element metadata, Parser[] parsers, ErrorHandler errorHandler) throws Exception {
        org.geotools.util.logging.Logging.getLogger("org.geotools.xml")
            .setLevel(Level.SEVERE);
        Path sSheet = schemaDir.resolve("extract-gml.xsl").toAbsolutePath();
        Element transform = Xml.transform(metadata, sSheet);
        if (transform.getChildren().size() == 0) {
            return null;
        }
        List<Polygon> allPolygons = new ArrayList<Polygon>();
        for (Element geom : (List<Element>) transform.getChildren()) {
        	Parser parser = null;
        	if (geom.getNamespace().equals(Geonet.Namespaces.GML32)) {
        	  parser = parsers[1]; // geotools gml3.2 parser
        	} else {
        	  parser = parsers[0];
        	}
            String srs = geom.getAttributeValue("srsName");
            CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
            String gml = Xml.getString(geom);

            try {
                if (srs != null && !(srs.equals(""))) sourceCRS = CRS.decode(srs);
                MultiPolygon jts = parseGml(parser, gml);

                // if we have an srs and its not WGS84 then transform to WGS84
                if (!CRS.equalsIgnoreMetadata(sourceCRS, DefaultGeographicCRS.WGS84)) {
                    MathTransform tform = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84);
                    jts = (MultiPolygon) JTS.transform(jts, tform);
                }

                for (int i = 0; i < jts.getNumGeometries(); i++) {
                    allPolygons.add((Polygon) jts.getGeometryN(i));
                }
            } catch (Exception e) {
                errorHandler.handleParseException(e, gml);
                // continue
            }
        }

        if (allPolygons.isEmpty()) {
            return null;
        } else {
            try {
                Polygon[] array = new Polygon[allPolygons.size()];
                GeometryFactory geometryFactory = allPolygons.get(0).getFactory();
                return geometryFactory.createMultiPolygon(allPolygons.toArray(array));


            } catch (Exception e) {
                errorHandler.handleBuildException(e, allPolygons);
                // continue
                return null;
            }
        }
    }

    static private class SpatialIndexingErrorHandler implements ErrorHandler {
        private final Map<String, String> errorMessage;

        public SpatialIndexingErrorHandler(Map<String, String> errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public void handleParseException(Exception e, String gml) {
            errorMessage.put("PARSE", gml + ". Error is:" + e.getMessage());
            Log.error(Geonet.INDEX_ENGINE, "Failed to convert gml to jts object: " + gml + "\n\t" + e.getMessage(), e);
        }

        @Override
        public void handleBuildException(Exception e, List<Polygon> allPolygons) {
            errorMessage.put("BUILD", allPolygons + ". Error is:" + e.getMessage());
            Log.error(Geonet.INDEX_ENGINE, "Failed to create a MultiPolygon from: " + allPolygons, e);
        }
    }

    public static MultiPolygon parseGml(Parser parser, String gml) throws IOException, SAXException,
        ParserConfigurationException {
        Object value = parser.parse(new StringReader(gml));
        if (value instanceof HashMap) {
            @SuppressWarnings("rawtypes")
            HashMap map = (HashMap) value;
            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                addToList(geoms, entry);
            }
            if (geoms.isEmpty()) {
                return null;
            } else if (geoms.size() > 1) {
                GeometryFactory factory = geoms.get(0).getFactory();
                return factory.createMultiPolygon(geoms.toArray(new Polygon[0]));
            } else {
                return toMultiPolygon(geoms.get(0));
            }

        } else if (value == null) {
            return null;
        } else {
            return toMultiPolygon((Geometry) value);
        }
    }

    public static void addToList(List<Polygon> geoms, Object entry) {
        if (entry instanceof Polygon) {
            geoms.add((Polygon) entry);
        } else if (entry instanceof Collection) {
            @SuppressWarnings("rawtypes")
            Collection collection = (Collection) entry;
            for (Object object : collection) {
                geoms.add((Polygon) object);
            }
        }
    }


    public Map<String, String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Add a metadata record to the index
     *
     * @param schemaDir the base directory that contains the different metadata schemas
     */
    public void index(Path schemaDir, String id,
                      Element metadata) throws Exception {
        _lock.lock();
        try {
            _index = null;
            errorMessage = new HashMap<>();
            Geometry geometry = extractGeometriesFrom(
                schemaDir, metadata, _parsers, errorMessage);

            if (geometry != null && !geometry.getEnvelopeInternal().isNull()) {
                MemoryFeatureCollection features = new MemoryFeatureCollection(_featureStore.getSchema());
                SimpleFeatureType schema = _featureStore.getSchema();

                SimpleFeature template = SimpleFeatureBuilder.template(schema,
                    SimpleFeatureBuilder.createDefaultFeatureId());
                template.setAttribute(schema.getGeometryDescriptor().getName(), geometry);
                template.setAttribute(getIdColumn(), id);
                features.add(template);

                _featureStore.addFeatures(features);

                _writes++;

                if (!_autocommit && _writes > _maxWrites) {
                    _transaction.commit();
                    _writes = 0;
                }
            }
        } finally {
            _lock.unlock();
        }
    }

    private String getIdColumn() {
        _lock.lock();
        try {
            if (_idColumn == null) {
                _idColumn = findIdColumn(_featureStore);
            }
            return _idColumn == null ? _IDS_ATTRIBUTE_NAME : _idColumn.toString();
        } finally {
            _lock.unlock();
        }
    }

    public void close() throws IOException {
        _lock.lock();
        try {
            if (_writes > 0) {
                _transaction.commit();
                _writes = 0;
            }
            _transaction.close();
            _index = null;
            _featureStore.setTransaction(Transaction.AUTO_COMMIT);
            // Done by JCSServletContextListener: SpatialFilter.getJCSCache().clear();
        } catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE,"SpatialIndexWriter close error: " + e.getMessage(), e);
        } finally {
            _lock.unlock();
        }
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource() {
        return _featureStore;
    }

    public void delete(String id) throws IOException {
        _lock.lock();
        try {
            FilterFactory2 factory = CommonFactoryFinder
                .getFilterFactory2(GeoTools.getDefaultHints());
            Filter filter = factory.equals(
                factory.property(getIdColumn()), factory.literal(id));

            _index = null;

            _featureStore.removeFeatures(filter);
            try {
                SpatialFilter.getJCSCache().clear();
            } catch (Throwable e) {
                Log.error(Geonet.INDEX_ENGINE,"SpatialIndexWriter JCSCache clear error: " + e.getMessage(), e);
            }
            _writes++;
        } finally {
            _lock.unlock();
        }
    }

    public void delete(List<String> ids) throws IOException {
        _lock.lock();
        try {
            FilterFactory2 factory = CommonFactoryFinder
                .getFilterFactory2(GeoTools.getDefaultHints());

            List<Filter> filters = new LinkedList<Filter>();
            String idColumn = getIdColumn();
            for (String id : ids) {
                filters.add(factory.equals(
                    factory.property(idColumn), factory.literal(id)));
            }

            _index = null;

            _featureStore.removeFeatures(factory.or(filters));
            try {
                SpatialFilter.getJCSCache().clear();
            } catch (Throwable e) {
                Log.error(Geonet.INDEX_ENGINE,"SpatialIndexWriter JCSCache clear error: " + e.getMessage(), e);
            }
            _writes++;
        } finally {
            _lock.unlock();
        }
    }

    public void commit() throws IOException {
        _lock.lock();
        try {

            if (!_autocommit && _writes > 0) {
                _writes = 0;
                _transaction.commit();
                _index = null;
                SpatialFilter.getJCSCache().clear();
            }
        } catch (Throwable e) {
            Log.error(Geonet.INDEX_ENGINE,"SpatialIndexWriter JCSCache commit error: " + e.getMessage(), e);
        } finally {
            _lock.unlock();
        }

    }

    public SpatialIndex getIndex() throws IOException {
        _lock.lock();
        try {

            if (_index == null) {
                populateIndex();
            }
            return _index;
        } finally {
            _lock.unlock();
        }
    }

    /**
     * Deletes the old index and sets up an empty index file
     */
    public void reset() throws Exception {
        _lock.lock();
        try {
            _featureStore.setTransaction(Transaction.AUTO_COMMIT);
            _index = null;
            _featureStore.removeFeatures(Filter.INCLUDE);
            _featureStore.setTransaction(_transaction);
        } finally {
            _lock.unlock();
        }
    }

    private void populateIndex() throws IOException {
        try {
            SpatialFilter.getJCSCache().clear();
        } catch (CacheException e) {
            Log.error(Geonet.INDEX_ENGINE,"SpatialIndexWriter JCSCache clear error: " + e.getMessage(), e);
        }
        _index = new STRtree();

        FeatureIterator<SimpleFeature> features = null;
        try {
            features = _featureStore.getFeatures().features();
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                if (_idColumn == null) {
                    _idColumn = findIdColumn(_featureStore);
                }

                if (feature.getDefaultGeometry() != null) {
                    insertFeatureInIndex(feature);
                }
            }

        } finally {
            if (features != null) {
                features.close();
            }
        }
    }

    private FeatureStore<SimpleFeatureType, SimpleFeature> createFeatureStore(DataStore datastore) throws Exception {
        Log.debug(Geonet.SPATIAL, "Configuring SpatialIndexWriter.");
        FeatureStore<SimpleFeatureType, SimpleFeature> featureSource;

        featureSource = findSpatialIndexStore(datastore);
        if (featureSource != null) {
            _idColumn = findIdColumn(featureSource);
            if (_idColumn == null) {
                throw new IllegalArgumentException(
                    "ERROR, unable to find _idColumn!!! in \n    DataStore: " + featureSource.getDataStore() +
                        "\n    FeatureType: " + featureSource.getSchema());
            }
            return featureSource;

        }
        return null;
    }

    /**
     * Find the spatialindex featureStore or return null
     */
    public static FeatureStore<SimpleFeatureType, SimpleFeature> findSpatialIndexStore(DataStore datastore) throws IOException {
        Log.debug(Geonet.SPATIAL, "Attempting to find FeatureType");
        for (String name : datastore.getTypeNames()) {
            Log.debug(Geonet.SPATIAL, "Found FeatureType: " + name);

            if (_SPATIAL_INDEX_TYPENAME.equalsIgnoreCase(name)) {
                Log.debug(Geonet.SPATIAL, "Found the spatial index FeatureType: " + name);
                return (FeatureStore<SimpleFeatureType, SimpleFeature>) datastore.getFeatureSource(name);
            }
        }
        return attemptToCreateSpatialIndexFeatureStore(datastore);
    }

    private static FeatureStore<SimpleFeatureType, SimpleFeature> attemptToCreateSpatialIndexFeatureStore(DataStore datastore) throws
        IOException {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(_SPATIAL_INDEX_TYPENAME);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        typeBuilder.add("geom", MultiPolygon.class);
        typeBuilder.add(_IDS_ATTRIBUTE_NAME, String.class);

        SimpleFeatureType type = typeBuilder.buildFeatureType();
        datastore.createSchema(type);
        return (FeatureStore<SimpleFeatureType, SimpleFeature>) datastore.getFeatureSource(type.getTypeName());
    }

    public static Name findIdColumn(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) {

        Log.debug(Geonet.SPATIAL, "Trying to find " + _IDS_ATTRIBUTE_NAME + " attribute in " + featureSource.getSchema());
        for (AttributeDescriptor descriptor : featureSource.getSchema().getAttributeDescriptors()) {
            Log.debug(Geonet.SPATIAL, "Found attribute " + descriptor.getLocalName());

            if (_IDS_ATTRIBUTE_NAME.equalsIgnoreCase(descriptor.getLocalName())) {
                Log.debug(Geonet.SPATIAL, "Found the id attribute of the spatial index: " + descriptor.getLocalName());
                return descriptor.getName();
            }
        }

        if (featureSource.getSchema().getDescriptor("fid") != null) {
            return featureSource.getSchema().getDescriptor("fid").getName();
        }
        return null;
    }

    public static MultiPolygon toMultiPolygon(Geometry geometry) {
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;

            return geometry.getFactory().createMultiPolygon(
                new Polygon[]{polygon});
        } else if (geometry instanceof MultiPolygon) {
            return (MultiPolygon) geometry;
        }
        String message = geometry.getClass() + " cannot be converted to a polygon. Check metadata";
        Log.error(Geonet.INDEX_ENGINE, message);
        throw new IllegalArgumentException(message);
    }

    public void changed(FeatureEvent featureEvent) {
        try {
            switch (featureEvent.getType()) {
                case ADDED:
                    break;
                case CHANGED:
                    SpatialFilter.getJCSCache().clear();
                    break;
                case REMOVED:
                    SpatialFilter.getJCSCache().clear();
                    break;
                case COMMIT:
                    SpatialFilter.getJCSCache().clear();
                    break;
                case ROLLBACK:
                    SpatialFilter.getJCSCache().clear();
                    break;
                default:
                    SpatialFilter.getJCSCache().clear();
                    break;
            }
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertFeatureInIndex(SimpleFeature feature) {
        Geometry defaultGeometry = (Geometry) feature.getDefaultGeometry();
        if (defaultGeometry instanceof MultiPolygon && defaultGeometry.getNumGeometries() > 1) {
            for (int i = 0; i < defaultGeometry.getNumGeometries(); i++) {
                Data data = buildData(feature, defaultGeometry.getNumGeometries());
                Envelope envelope = defaultGeometry.getGeometryN(i).getEnvelopeInternal();
                data.setEnv(envelope);
                _index.insert(envelope, data);
            }

        } else {
            Data data = buildData(feature, 1);
            Envelope envelope = defaultGeometry.getEnvelopeInternal();
            data.setEnv(envelope);
            _index.insert(envelope, data);
        }
    }

    private Data buildData(SimpleFeature feature, int numBrotherGeometries) {
        Data data = new Data();
        data.setMetadataId(feature.getAttribute(_idColumn == null ? _IDS_ATTRIBUTE_NAME : _idColumn.toString()).toString());
        data.setFeatureId(feature.getIdentifier());
        data.setNumBrotherGeometries(numBrotherGeometries);
        return data;
    }

    public class Data {
        private FeatureId featureId;
        private String metadataId;
        private Envelope env;
        private int numBrotherGeometries;

        public Envelope getEnv() {
            return env;
        }

        public void setEnv(Envelope env) {
            this.env = env;
        }

        public FeatureId getFeatureId() {
            return featureId;
        }

        public void setFeatureId(FeatureId featureId) {
            this.featureId = featureId;
        }

        public String getMetadataId() { return metadataId; }

        public void setMetadataId(String metadataId) {
            this.metadataId = metadataId;
        }

        public int getNumBrotherGeometries() {return numBrotherGeometries;}

        public void setNumBrotherGeometries(int numBrotherGeometries) {
            this.numBrotherGeometries = numBrotherGeometries;
        }
    }
}
