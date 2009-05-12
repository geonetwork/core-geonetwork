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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.indexed.IndexType;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This class is responsible for extracting geographic information from metadata
 * and writing that information to a storage mechanism.
 * 
 * @author jeichar
 */
public class SpatialIndexWriter
{

    static final String                                          IDS_ATTRIBUTE_NAME        = "id";
    static final String                                          GEOM_ATTRIBUTE_NAME       = "the_geom";
    static final String                                          SPATIAL_INDEX_FILENAME    = "spatialIndex";
    private static final int                                     MAX_WRITES_IN_TRANSACTION = 5000;

    private final Parser                                         _parser;
    private final Transaction                                    _transaction;
    private final SimpleFeatureType                              _schema;
    private final File                                           _file;
    private final Lock                                           _lock;
    private FeatureStore<SimpleFeatureType, SimpleFeature> _featureStore;
    private STRtree                                              _index;
    private static int                                           _writes;

    public SpatialIndexWriter(String indexBasedir, Configuration config)
            throws Exception
    {
        this(indexBasedir, new Parser(config),
                new DefaultTransaction("SpatialIndexWriter"),
                new ReentrantLock());
    }

    public SpatialIndexWriter(String indexBasedir, Configuration config,
            Transaction transaction) throws Exception
    {
        this(indexBasedir, new Parser(config), transaction, new ReentrantLock());
    }

    public SpatialIndexWriter(String indexBasedir, Parser parser,
            Transaction transaction, Lock lock) throws Exception
    {
        // Note: The Configuration takes a long time to create so it is worth
        // re-using the same Configuration
        _lock = lock;
        _parser = parser;
        _parser.setStrict(false);
        _parser.setValidating(false);
        _file = createDataStoreFile(indexBasedir);
        _transaction = transaction;

        _schema = createSchema();
        _featureStore = createFeatureStore();
        _featureStore.setTransaction(_transaction);

    }

    /**
     * Add a metadata record to the index
     * 
     * @param schemasDir
     *            the base directory that contains the different metadata
     *            schemas
     * @param type
     *            the type of the metadata that is being passed in
     * @param metadata
     *            the metadata
     */
    public void index(String schemasDir, String type, String id,
            Element metadata) throws Exception
    {
        _lock.lock();
        try {
            _index = null;
            Geometry[] extractGeometriesFrom = extractGeometriesFrom(
                    schemasDir, type, metadata, _parser);

            if (extractGeometriesFrom.length > 0) {
                FeatureCollection features = FeatureCollections.newCollection();
                for (Geometry geometry : extractGeometriesFrom) {
                    Object[] data = { geometry, id };
                    features.add(SimpleFeatureBuilder.build(_schema, data,
                            SimpleFeatureBuilder.createDefaultFeatureId()));
                }

                _featureStore.addFeatures(features);

                _writes++;

                if (_writes > MAX_WRITES_IN_TRANSACTION) {
                    _transaction.commit();
                    _writes = 0;
                }
            }
        } finally {
            _lock.unlock();
        }
    }

    public void close() throws IOException
    {
        _lock.lock();
        try {
            if (_writes > 0) {
                _transaction.commit();
                _writes = 0;
            }
            _transaction.close();
            _index = null;
            _featureStore.setTransaction(Transaction.AUTO_COMMIT);
        } finally {
            _lock.unlock();
        }
    }

    public void delete() throws IOException
    {
        _lock.lock();
        try {
            if (_featureStore.getTransaction() != Transaction.AUTO_COMMIT) {
                close();
            }
            _index = null;
            delete(_file.getParentFile());
        } finally {
            _lock.unlock();
        }
    }

    public FeatureSource getFeatureSource()
    {
        return _featureStore;
    }

    public void delete(String id) throws IOException
    {
        _lock.lock();
        try {
            FilterFactory2 factory = CommonFactoryFinder
                    .getFilterFactory2(GeoTools.getDefaultHints());
            Filter filter = factory.equals(
                    factory.property(IDS_ATTRIBUTE_NAME), factory.literal(id));

            _index = null;

            _featureStore.removeFeatures(filter);
            _writes++;
        } finally {
            _lock.unlock();
        }
    }

    public void commit() throws IOException
    {
        _lock.lock();
        try {

            if (_writes > 0) {
                _writes = 0;
                _transaction.commit();
                _index = null;
                populateIndex();
            }
        } finally {
            _lock.unlock();
        }

    }

    public SpatialIndex getIndex() throws IOException
    {
        _lock.lock();
        try {

            if (_index==null) {
                populateIndex();
            }
            return _index; 
        } finally {
            _lock.unlock();
        }
    }

    /**
     * Deletes the old index and sets up an empty index file
     * @throws Exception
     */
    public void reset() throws Exception
    {

        _lock.lock();
        try {
            _featureStore.setTransaction(Transaction.AUTO_COMMIT);
            _index=null;
            delete(_file);
            _featureStore=createFeatureStore();
            _featureStore.setTransaction(_transaction);
        }finally{
            _lock.unlock();
        }
    }

    /**
     * Extracts a Geometry Collection from metadata default visibility for
     * testing access.
     */
    static MultiPolygon[] extractGeometriesFrom(String schemasDir, String type,
            Element metadata, Parser parser) throws Exception
    {

        org.geotools.util.logging.Logging.getLogger("org.geotools.xml")
                .setLevel(Level.SEVERE);
        File schemaDir = new File(schemasDir, type);
        String sSheet = new File(schemaDir, "extract-gml.xsl")
                .getAbsolutePath();
        Element transform = Xml.transform(metadata, sSheet);
        if (transform.getChildren().size() == 0) {
            return new MultiPolygon[0];
        }
        String gml = Xml.getString(transform);

        try {
            Object value = parser.parse(new StringReader(gml));
            if (value instanceof HashMap) {
                HashMap map = (HashMap) value;
                List<Polygon> geoms = new ArrayList<Polygon>();
                for (Object entry : map.values()) {
                    addToList(geoms, entry);
                }
                if( geoms.isEmpty() ){
                    return new MultiPolygon[0];
                } else if( geoms.size()>1 ){
                    GeometryFactory factory = geoms.get(0).getFactory();
                    return new MultiPolygon[]{factory.createMultiPolygon(geoms.toArray(new Polygon[0]))};
                } else {
                    return new MultiPolygon[]{toMultiPolygon(geoms.get(0))};
                }

            } else if (value == null) {
                return new MultiPolygon[0];
            } else {
                return new MultiPolygon[] { toMultiPolygon((Geometry) value) };
            }
        } catch (Exception e) {
            return new MultiPolygon[0];
        }
    }

    /**
     * @see SpatialIndexWriter#extractGeometriesFrom(String, String, Element)
     */
    private static void addToList(List<Polygon> geoms, Object entry)
    {
        if (entry instanceof Polygon) {
            geoms.add((Polygon) entry);
        } else if (entry instanceof Collection) {
            Collection collection = (Collection) entry;
            for (Object object : collection) {
                geoms.add((Polygon) object);
            }
        }
    }

    private static void delete(File parentFile)
    {
        if (parentFile.isDirectory()) {
            for (File file : parentFile.listFiles()) {
                delete(file);
            }
        }
        parentFile.delete();
    }

    private void populateIndex() throws IOException
    {
        _index = new STRtree();
        FeatureIterator<SimpleFeature> features = _featureStore.getFeatures().features();
        try {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                Pair<FeatureId, Object> data = Pair.read(feature.getIdentifier(), feature.getAttribute("id"));
                _index.insert(((Geometry) feature.getDefaultGeometry())
                        .getEnvelopeInternal(), data);
            }

        } finally {
            features.close();
        }
    }

    private SimpleFeatureType createSchema() throws SchemaException
    {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(
                DefaultGeographicCRS.WGS84).binding(MultiPolygon.class)
                .buildDescriptor("the_geom");
        builder.setName(SPATIAL_INDEX_FILENAME);
        builder.add(geomDescriptor);
        builder.add(IDS_ATTRIBUTE_NAME, String.class);
        return builder.buildFeatureType();
    }

    public static File createDataStoreFile(String indexBasedir)
    {
        return new File(indexBasedir + "/" + SPATIAL_INDEX_FILENAME + ".shp");
        // return new File(indexBasedir + "/" + SPATIAL_INDEX_FILENAME + "/"
        // + SPATIAL_INDEX_FILENAME + ".h2");
    }

    private FeatureStore createFeatureStore() throws Exception
    {
        _file.getParentFile().mkdirs();

        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(_file
                .toURI().toURL(), new URI("http://geonetwork.org"), false,
                true, IndexType.NONE, Charset.defaultCharset());

        if (!_file.exists()) {
            ds.createSchema(_schema);
        }
        
        return (FeatureStore) ds.getFeatureSource(_schema.getTypeName());
    }

    private static MultiPolygon toMultiPolygon(Geometry geometry)
    {
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            
            return geometry.getFactory().createMultiPolygon(
                    new Polygon[] { polygon });
        }else if (geometry instanceof MultiPolygon) {
            return  (MultiPolygon) geometry;
        }
        String message = geometry.getClass()+" cannot be converted to a polygon. Check Metadata";
        Log.error("SpatialIndexWriter", message);
        throw new IllegalArgumentException(message);
    }

}
