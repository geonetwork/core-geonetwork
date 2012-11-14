package org.fao.geonet.services.region;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.util.LangUtils;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.MultiValuedFilter.MatchAction;
import org.opengis.filter.identity.Identifier;

public class GeocatRegionsRequest extends Request {
	static final String GEMEINDEN_DESC = "DESC";
	static final String GEMEINDEN_NAME = "GEMNAME";
	static final String KANTONE_NAME = "NAME";
	static final String COUNTRY_DESC = GEMEINDEN_DESC;
	static final String COUNTRY_NAME = "LAND";
	public static final String KANTON = "kanton";
	public static final String GEMEINDEN = "gemeinden";
	public static final String COUNTRY = "country";
	private static final String KANTON_PREFIX = KANTON+":";
	private static final String GEMEINDEN_PREFIX = GEMEINDEN+":";
	private static final String COUNTRY_PREFIX = COUNTRY+":";
	private static final String SEARCH = "SEARCH";

	Set<String> labels = new HashSet<String>();
	Set<String> categoryIds = new HashSet<String>();
	Set<Identifier> kantonIds = new HashSet<Identifier>();
	Set<Identifier> countryIds = new HashSet<Identifier>();
	Set<Identifier> gemeindenIds = new HashSet<Identifier>();
	private int maxRecords = -1;
	private final DatastoreCache datastoreCache;
	private final ServiceContext context;
	private final WeakHashMap<String, Map<String, String>> categoryIdMap;
	private FilterFactory2 filterFactory;
	private boolean all = true; 

	public GeocatRegionsRequest(ServiceContext context,
			DatastoreCache datastoreCache,
			WeakHashMap<String, Map<String, String>> categoryIdMap, FilterFactory2 filterFactory) {
		this.datastoreCache = datastoreCache;
		this.context = context;
		this.categoryIdMap = categoryIdMap;
		this.filterFactory = filterFactory;
	}

	@Override
	public Request label(String labelParam) {
		all = false;
		labels.add(labelParam);
		return this;
	}

	@Override
	public Request categoryId(String categoryIdParam) {
		all = false;
		categoryIds.add(categoryIdParam);
		return this;
	}

	@Override
	public Request maxRecords(int maxRecordsParam) {
		this.maxRecords = maxRecordsParam;
		return this;
	}

	@Override
	public Request id(String regionId) {
		all = false;
		if (regionId.startsWith(KANTON_PREFIX)) {
			this.kantonIds.add(filterFactory.featureId(regionId.substring(KANTON_PREFIX.length())));
		} else if (regionId.startsWith(GEMEINDEN_PREFIX)) {
			this.gemeindenIds.add(filterFactory.featureId(regionId.substring(GEMEINDEN_PREFIX.length())));
		} else if (regionId.startsWith(COUNTRY_PREFIX)) {
			this.countryIds.add(filterFactory.featureId(regionId.substring(COUNTRY_PREFIX.length())));
		} else {
			throw new IllegalArgumentException(regionId+" does not have a acceptable id:"+KANTON_PREFIX+", "+GEMEINDEN_PREFIX+", "+COUNTRY_PREFIX);
		}
		return this;
	}

	@Override
	public Collection<Region> execute() throws Exception {
		Collection<Region> results = new LinkedList<Region>();
		if(all) {
			categoryIds.add(GEMEINDEN);
			categoryIds.add(KANTON);
			categoryIds.add(COUNTRY);
		}
		if(!categoryIds.isEmpty()) {
			if(categoryIds.contains(KANTON)) {
				loadRegions(results, datastoreCache.getKantons(context), Filter.INCLUDE);
			}
			if(categoryIds.contains(GEMEINDEN)) {
				loadRegions(results, datastoreCache.getGemeindens(context), Filter.INCLUDE);
			}
			if(categoryIds.contains(COUNTRY)) {
				loadRegions(results, datastoreCache.getCountries(context), Filter.INCLUDE);
			}
		} else if(labels.isEmpty() && !(kantonIds.isEmpty() || gemeindenIds.isEmpty() || countryIds.isEmpty())) {
			// only ids so we can target the correct feature source directly and be more performant
			if (!kantonIds.isEmpty()) {
				executeIdQuery(results, kantonIds, datastoreCache.getKantons(context));
			}
			if (!countryIds.isEmpty()) {
				executeIdQuery(results, countryIds, datastoreCache.getCountries(context));
			}
			if (!gemeindenIds.isEmpty()) {
				executeIdQuery(results, gemeindenIds, datastoreCache.getGemeindens(context));
			}
		} else {
			Set<Identifier> allIds = new HashSet<Identifier>();
			allIds.addAll(countryIds);
			allIds.addAll(kantonIds);
			allIds.addAll(gemeindenIds);
			java.util.List<Filter> filters = new LinkedList<Filter>();
			filters.add(filterFactory.id(allIds));
			for (String label : labels) {
				filters.add(filterFactory.like(filterFactory.property(SEARCH), "*"+label+"*", "*", "?", "\\", false, MatchAction.ANY));
			}
			Filter filter = filterFactory.or(filters);

			loadRegions(results, datastoreCache.getCountries(context), filter);
			loadRegions(results, datastoreCache.getKantons(context), filter);
			loadRegions(results, datastoreCache.getGemeindens(context), filter);
		}
		return results;
	}

	private void executeIdQuery(Collection<Region> results, Set<Identifier> ids, SimpleFeatureSource source) throws IOException, NoSuchElementException, JDOMException {
		Id filter = filterFactory.id(ids);
		loadRegions(results, source, filter);
	}

	private void loadRegions(Collection<Region> results,
			SimpleFeatureSource source, Filter filter) throws IOException,
			JDOMException {
		Query query = createQuery(source, filter);
		SimpleFeatureIterator features = source.getFeatures(query).features();
		try {
			while (features.hasNext()) {
				SimpleFeature next = features.next();
				Region region;
				if (next.getFeatureType().getTypeName().equals(COUNTRY)) {
					region = constructRegionFromCountry(next);
				} else if (next.getFeatureType().getTypeName().equals(KANTON)) {
					region = constructRegionFromKanton(next);
				} else {
					region = constructRegionFromGemeinden(next);
				}
				results.add(region);
			}
		} finally {
			features.close();
		}
	}

	private Query createQuery(SimpleFeatureSource source, Filter filter) {
		Query query = new Query(source.getSchema().getTypeName(), filter);
		query.setMaxFeatures(maxRecords);
		query.setPropertyNames(propNames(source.getSchema().getTypeName()));
		return query;
	}

	private String[] propNames(String typeName) {
		if (typeName.equals(COUNTRY)) {
			return new String[]{COUNTRY_NAME, COUNTRY_DESC};
		} else if (typeName.equals(KANTON)) {
			return new String[]{KANTONE_NAME};
		} else {
			return new String[]{GEMEINDEN_NAME, GEMEINDEN_DESC};
		}
	}

	private Region constructRegionFromGemeinden(SimpleFeature next) throws JDOMException, IOException {
		String id = GEMEINDEN_PREFIX+next.getID();
		Map<String, String> labels = new HashMap<String, String>();
		String label = next.getAttribute(GEMEINDEN_NAME).toString();
		labels.put("eng", label);
		labels.put("ger", label);
		labels.put("fre", label);
		labels.put("ita", label);
		boolean hasGeom = true;
		ReferencedEnvelope bbox = new ReferencedEnvelope(next.getBounds());
		Map<String, String> gemeindenLabels = categoryIdMap.get(GEMEINDEN);
		if(gemeindenLabels == null) {
			gemeindenLabels = LangUtils.translate(context, GEMEINDEN);
			categoryIdMap.put(GEMEINDEN, gemeindenLabels);
		}
		return new Region(id, labels, GEMEINDEN, gemeindenLabels, hasGeom, bbox);
	}

	private Region constructRegionFromKanton(SimpleFeature next) throws JDOMException, IOException {
		String id = KANTON_PREFIX+next.getID();
		Map<String, String> labels = new HashMap<String, String>();
		String label = next.getAttribute(KANTONE_NAME).toString();
		labels.put("eng", label);
		labels.put("ger", label);
		labels.put("fre", label);
		labels.put("ita", label);
		boolean hasGeom = true;
		ReferencedEnvelope bbox = new ReferencedEnvelope(next.getBounds());
		Map<String, String> kantonLabels = categoryIdMap.get(KANTON);
		if(kantonLabels == null) {
			kantonLabels = LangUtils.translate(context, KANTON);
			categoryIdMap.put(KANTON, kantonLabels);
		}
		return new Region(id, labels, KANTON, kantonLabels, hasGeom, bbox);
	}

	private Region constructRegionFromCountry(SimpleFeature next) throws JDOMException, IOException {
		String id = COUNTRY_PREFIX+next.getID();
		Map<String, String> labels = new HashMap<String, String>();
		String label = next.getAttribute(COUNTRY_NAME).toString();
		labels.put("eng", label);
		labels.put("ger", label);
		labels.put("fre", label);
		labels.put("ita", label);
		boolean hasGeom = true;
		ReferencedEnvelope bbox = new ReferencedEnvelope(next.getBounds());
		Map<String, String> countryLabels = categoryIdMap.get(COUNTRY);
		if(countryLabels == null) {
			countryLabels = LangUtils.translate(context, COUNTRY);
			categoryIdMap.put(COUNTRY, countryLabels);
		}
		return new Region(id, labels, COUNTRY, countryLabels , hasGeom, bbox);
	}
}
