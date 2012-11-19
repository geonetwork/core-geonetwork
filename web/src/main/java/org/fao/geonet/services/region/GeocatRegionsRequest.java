package org.fao.geonet.services.region;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.MultiValuedFilter.MatchAction;

public class GeocatRegionsRequest extends Request {

	private static final String SEARCH = "SEARCH";
	Set<String> labels = new HashSet<String>();
	Set<String> categoryIds = new HashSet<String>();
	LinkedList<Filter> kantonIds = new LinkedList<Filter>();
	LinkedList<Filter> countryIds = new LinkedList<Filter>();
	LinkedList<Filter> gemeindenIds = new LinkedList<Filter>();
	private int maxRecords = -1;
	private FilterFactory2 filterFactory;
	private boolean all = true;
	private DatastoreMapper countryMapper;
	private DatastoreMapper kantoneMapper;
	private DatastoreMapper gemeindenMapper;

	public GeocatRegionsRequest(ServiceContext context,
			DatastoreCache datastoreCache,
			WeakHashMap<String, Map<String, String>> categoryIdMap, FilterFactory2 filterFactory) {
		this.filterFactory = filterFactory;
		this.countryMapper = new CountryMapper(context, datastoreCache, filterFactory, categoryIdMap);
		this.kantoneMapper = new KantoneMapper(context, datastoreCache, filterFactory, categoryIdMap);
		this.gemeindenMapper = new GemeindenMapper(context, datastoreCache, filterFactory, categoryIdMap);

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
		if (kantoneMapper.accepts(regionId)) {
			this.kantonIds.add(kantoneMapper.idFilter(regionId));
		} else if (gemeindenMapper.accepts(regionId)) {
			this.gemeindenIds.add(gemeindenMapper.idFilter(regionId));
		} else if (countryMapper.accepts(regionId)) {
			this.countryIds.add(countryMapper.idFilter(regionId));
		} else {
			throw new IllegalArgumentException(regionId+" does not have a acceptable id.");
		}
		return this;
	}

	@Override
	public Collection<Region> execute() throws Exception {
		Collection<Region> results = new LinkedList<Region>();
		if(all) {
			categoryIds.add(gemeindenMapper.categoryId());
			categoryIds.add(kantoneMapper.categoryId());
			categoryIds.add(countryMapper.categoryId());
		}
		if(!categoryIds.isEmpty()) {
			if(categoryIds.contains(kantoneMapper.categoryId())) {
				kantoneMapper.loadRegions(results, maxRecords, Filter.INCLUDE);
			}
			if(categoryIds.contains(gemeindenMapper.categoryId())) {
				gemeindenMapper.loadRegions(results, maxRecords, Filter.INCLUDE);
			}
			if(categoryIds.contains(countryMapper.categoryId())) {
				countryMapper.loadRegions(results, maxRecords, Filter.INCLUDE);
			}
		} else if(labels.isEmpty() && !(kantonIds.isEmpty() || gemeindenIds.isEmpty() || countryIds.isEmpty())) {
			// only ids so we can target the correct feature source directly and be more performant
			if (!kantonIds.isEmpty()) {
				kantoneMapper.loadRegions(results, maxRecords, filterFactory.or(kantonIds));
			}
			if (!countryIds.isEmpty()) {
				countryMapper.loadRegions(results, maxRecords, filterFactory.or(countryIds));
			}
			if (!gemeindenIds.isEmpty()) {
				gemeindenMapper.loadRegions(results, maxRecords, filterFactory.or(gemeindenIds));
			}
		} else {
			java.util.List<Filter> filters = new LinkedList<Filter>();
			filters.addAll(countryIds);
			filters.addAll(kantonIds);
			filters.addAll(gemeindenIds);
			for (String label : labels) {
				filters.add(filterFactory.like(filterFactory.property(SEARCH), "*"+label+"*", "*", "?", "\\", false, MatchAction.ANY));
			}
			Filter filter = filterFactory.or(filters);

			countryMapper.loadRegions(results, maxRecords, filter);
			kantoneMapper.loadRegions(results, maxRecords, filter);
			gemeindenMapper.loadRegions(results, maxRecords, filter);
		}
		return results;
	}

}
