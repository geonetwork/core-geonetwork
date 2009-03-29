package org.fao.geonet.kernel.search.spatial;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.SpatialOperator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.SpatialIndex;

public abstract class SpatialFilter extends Filter
{
    private static final long     serialVersionUID = -6221744013750827050L;
    protected final Geometry      _geom;
    protected final FeatureSource _featureSource;
    protected final SpatialIndex    _index;

    protected final FilterFactory2  _filterFactory;
    protected final Query                 _query;
    protected final SetBasedFieldSelector _selector;
    private org.opengis.filter.Filter _spatialFilter;
    private Map<String, FeatureId> _unrefinedMatches;

    protected SpatialFilter(Query query, Element request, Geometry geom,
            FeatureSource featureSource, SpatialIndex index) throws IOException
    {
        _query = query;
        _geom = geom;
        _featureSource = featureSource;
        _index = index;
        _filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools
                .getDefaultHints());

        Set fieldsToLoad = new HashSet<String>();
        fieldsToLoad.add("_id");
        Set lazyFieldsToLoad = Collections.emptySet();
        _selector = new SetBasedFieldSelector(fieldsToLoad, lazyFieldsToLoad);
    }

    protected SpatialFilter(Query query, Envelope bounds,
            FeatureSource featureSource, SpatialIndex index) throws IOException
    {
        this(query,null,JTS.toGeometry(bounds),featureSource,index);
    }

    public BitSet bits(final IndexReader reader) throws IOException
    {
        final BitSet bits = new BitSet(reader.maxDoc());

        final Map<String, FeatureId> unrefinedSpatialMatches = unrefinedSpatialMatches();
        final Set<FeatureId> matches = new HashSet<FeatureId>();
        final Map<FeatureId,Integer> docIndexLookup = new HashMap<FeatureId,Integer>();
        
        new IndexSearcher(reader).search(_query, new HitCollector()
        {
            public final void collect(int doc, float score)
            {
                Document document;
                try {
                    document = reader.document(doc);
                    String key = document.get("_id");
                    FeatureId featureId = unrefinedSpatialMatches.get(key); 
                    if (featureId!=null) {
                        matches.add(featureId);
                        docIndexLookup.put(featureId, doc);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        
        return applySpatialFilter(matches,docIndexLookup,bits);
    }

    private BitSet applySpatialFilter(Set<FeatureId> matches, Map<FeatureId, Integer> docIndexLookup, BitSet bits) throws IOException
    {
        Id fidFilter = _filterFactory.id(matches);
        String ftn = _featureSource.getSchema().getName().getLocalPart();
        String[] geomAtt = {_featureSource.getSchema().getGeometryDescriptor().getLocalName()};
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = _featureSource
                .getFeatures(new DefaultQuery(ftn, fidFilter,geomAtt));
        FeatureIterator<SimpleFeature> iterator = features.features();

        
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                if( getFilter().evaluate(feature) ){
                    bits.set(docIndexLookup.get(feature.getIdentifier()));
                }
            }
        } finally {
            iterator.close();
        }
        return bits;
    }

    private synchronized org.opengis.filter.Filter getFilter()
    {
        if (_spatialFilter == null) {
            _spatialFilter = createFilter(_featureSource);
        }

        return _spatialFilter;
    }

    /**
     * Returns all the FeatureId and ID attributes based on the query against the spatial index
     * 
     * @return all the FeatureId and ID attributes based on the query against the spatial index
     */
    protected synchronized Map<String,FeatureId> unrefinedSpatialMatches(){
        if(_unrefinedMatches==null){
            List<Pair<FeatureId,String>> fids = _index.query(_geom.getEnvelopeInternal());
            _unrefinedMatches = new HashMap<String,FeatureId>();
            for (Pair<FeatureId, String> match : fids) {
                _unrefinedMatches.put(match.two(), match.one());
            }
        }
        return _unrefinedMatches;
    }
    
    protected org.opengis.filter.Filter createFilter(FeatureSource source)
    {
        String geomAttName = source.getSchema().getGeometryDescriptor()
                .getLocalName();
        PropertyName geomPropertyName = _filterFactory.property(geomAttName);

        Literal geomExpression = _filterFactory.literal(_geom);
        org.opengis.filter.Filter filter = createGeomFilter(_filterFactory,
                geomPropertyName, geomExpression);
        return filter;
    }

    protected SpatialOperator createGeomFilter(FilterFactory2 filterFactory,
            PropertyName geomPropertyName, Literal geomExpression)
    {
        throw new UnsupportedOperationException(
                "createGeomFilter must be overridden if createFilter is not overridden");
    }

}