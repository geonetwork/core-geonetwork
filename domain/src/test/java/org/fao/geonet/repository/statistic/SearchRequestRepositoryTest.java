package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SpringDataTestSupport;
import org.fao.geonet.repository.specification.SearchRequestParamSpecs;
import org.fao.geonet.repository.specification.SearchRequestSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.specification.SearchRequestSpecs.isMoreRecentThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the SearchRequestRepository.
 * <p/>
 * User: Jesse
 * Date: 9/29/13
 * Time: 11:03 PM
 */
public class SearchRequestRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    SearchRequestRepository _requestRepo;
    @Autowired
    SearchRequestParamRepository _paramRepo;

    @PersistenceContext
    EntityManager _entityManager;

    @Test
    public void testFindOne() throws Exception {
        SearchRequest searchRequest1 = newSearchRequest();
        assertTrue(searchRequest1.getParams().size() > 0);
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        assertTrue(searchRequest2.getParams().size() > 0);
        searchRequest2 = _requestRepo.save(searchRequest2);

        _entityManager.flush();
        _entityManager.clear();

        SearchRequest found = _requestRepo.findOne(searchRequest2.getId());
        assertSameContents(searchRequest2, found);
        assertEquals(searchRequest2.getParams().size(), found.getParams().size());
        List<SearchRequestParam> params = _paramRepo.findAll(SearchRequestParamSpecs.hasService(searchRequest2.getService()));
        assertEquals(searchRequest2.getParams().size(), params.size());
    }

    @Test
    public void testSelectAllDistinctAttributes() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10T00:00:00"));
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1980-10-13T01:11:00"));
        _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest4);

        final List<ISODate> isoDates = _requestRepo.selectAllDistinctAttributes(SearchRequest_.requestDate);

        assertEquals(3, isoDates.size());
        assertTrue(isoDates.contains(searchRequest1.getRequestDate()));
        assertTrue(isoDates.contains(searchRequest2.getRequestDate()));
        assertTrue(isoDates.contains(searchRequest3.getRequestDate()));

    }

    @Test
    public void testGetSummary() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10T00:00:00"));
        searchRequest1.setSimple(true);
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        searchRequest2.setSimple(false);
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1980-10-13T01:11:00"));
        searchRequest3.setSimple(true);
        _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1980-11-16T01:11:00"));
        searchRequest4.setSimple(true);
        _requestRepo.save(searchRequest4);

        PathSpec<SearchRequest, Boolean> path = new PathSpec<SearchRequest, Boolean>() {
            @Override
            public Path<Boolean> getPath(Root<SearchRequest> root) {
                return root.get(SearchRequest_.simple);
            }
        };

        assertSummary(isMoreRecentThanOrEqualTo(searchRequest1.getRequestDate()), path, 3, 1);
        assertSummary(isMoreRecentThanOrEqualTo(searchRequest3.getRequestDate()), path, 2, 0);
    }

    private void assertSummary(Specification<SearchRequest> spec, PathSpec<SearchRequest, Boolean> path, int expectedTrue,
                               int expectedFalse) {
        final List<Pair<Boolean, Integer>> hitSummary = _requestRepo.getHitSummary(spec, path, org.springframework.data.domain.Sort
                .Direction.DESC);
        for (Pair<Boolean, Integer> summary : hitSummary) {
            if (summary.one()) {
                assertEquals(expectedTrue, summary.two().intValue());
            } else {
                assertEquals(expectedFalse, summary.two().intValue());
            }
        }
    }

    @Test
    public void testGetRequestDateToRequestCountBetweenByDayWithSpec() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10"));
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1980-11-13T01:11:00"));
        searchRequest3 = _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1980-11-16T01:11:00"));
        searchRequest4 = _requestRepo.save(searchRequest4);

        final List<Pair<DateInterval.Day, Integer>> fullInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Day(), searchRequest1.getRequestDate(), searchRequest4.getRequestDate(),
                        SearchRequestSpecs.hasService(searchRequest1.getService()));
        assertEquals(1, fullInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Day(searchRequest1.getRequestDate()), 1)));
    }

    @Test
    public void testGetRequestDateToRequestCountBetweenByDay() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10"));
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1980-11-13T01:11:00"));
        searchRequest3 = _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1980-11-16T01:11:00"));
        searchRequest4 = _requestRepo.save(searchRequest4);

        final List<Pair<DateInterval.Day, Integer>> fullInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Day(), searchRequest1.getRequestDate(), searchRequest4.getRequestDate());
        assertEquals(3, fullInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Day(searchRequest1.getRequestDate()), 2)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Day(searchRequest3.getRequestDate()), 1)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Day(searchRequest4.getRequestDate()), 1)));


        final List<Pair<DateInterval.Day, Integer>> shortInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Day(), searchRequest3.getRequestDate(), searchRequest4.getRequestDate());
        assertEquals(2, shortInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Day(searchRequest3.getRequestDate()), 1)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Day(searchRequest4.getRequestDate()), 1)));

        final List<Pair<DateInterval.Day, Integer>> outOfRangeInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Day(), new ISODate("1990-01-01"), new ISODate("1990-01-02"));
        assertEquals(0, outOfRangeInterval.size());
    }

    @Test
    public void testGetRequestDateToRequestCountBetweenByMonth() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10"));
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1980-11-13T01:11:00"));
        searchRequest3 = _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1980-12-16T01:11:00"));
        searchRequest4 = _requestRepo.save(searchRequest4);

        final List<Pair<DateInterval.Month, Integer>> fullInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Month(), searchRequest1.getRequestDate(), searchRequest4.getRequestDate());
        assertEquals(3, fullInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Month(searchRequest1.getRequestDate()), 2)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Month(searchRequest3.getRequestDate()), 1)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Month(searchRequest4.getRequestDate()), 1)));


        final List<Pair<DateInterval.Month, Integer>> shortInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Month(), searchRequest3.getRequestDate(), searchRequest4.getRequestDate());
        assertEquals(2, shortInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Month(searchRequest3.getRequestDate()), 1)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Month(searchRequest4.getRequestDate()), 1)));

        final List<Pair<DateInterval.Month, Integer>> outOfRangeInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Month(), new ISODate("1990-01-01"), new ISODate("1990-01-02"));
        assertEquals(0, outOfRangeInterval.size());
    }

    @Test
    public void testGetRequestDateToRequestCountBetweenByYear() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10"));
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1981-11-13T01:11:00"));
        searchRequest3 = _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1982-12-16T01:11:00"));
        searchRequest4 = _requestRepo.save(searchRequest4);

        final List<Pair<DateInterval.Year, Integer>> fullInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Year(), searchRequest1.getRequestDate(), searchRequest4.getRequestDate());
        assertEquals(3, fullInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Year(searchRequest1.getRequestDate()), 2)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Year(searchRequest3.getRequestDate()), 1)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Year(searchRequest4.getRequestDate()), 1)));


        final List<Pair<DateInterval.Year, Integer>> shortInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Year(), searchRequest3.getRequestDate(), searchRequest4.getRequestDate());
        assertEquals(2, shortInterval.size());
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Year(searchRequest3.getRequestDate()), 1)));
        assertTrue(fullInterval.contains(Pair.read(new DateInterval.Year(searchRequest4.getRequestDate()), 1)));

        final List<Pair<DateInterval.Year, Integer>> outOfRangeInterval = _requestRepo.getRequestDateToRequestCountBetween
                (new DateInterval.Year(), new ISODate("1990-01-01"), new ISODate("1990-01-02"));
        assertEquals(0, outOfRangeInterval.size());
    }

    @Test
    public void testGetMostRecentRequestDate() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10T01:01:01"));
        _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1981-11-13T01:11:00"));
        _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1982-12-16T01:11:00"));
        searchRequest4 = _requestRepo.save(searchRequest4);

        assertEquals(searchRequest4.getRequestDate(), _requestRepo.getMostRecentRequestDate());
    }

    @Test
    public void testGetOldestRequestDate() {
        SearchRequest searchRequest1 = newSearchRequest();
        searchRequest1.setRequestDate(new ISODate("1980-10-10T01:01:01"));
        searchRequest1 = _requestRepo.save(searchRequest1);

        SearchRequest searchRequest2 = newSearchRequest();
        searchRequest2.setRequestDate(new ISODate("1980-10-10T01:11:00"));
        _requestRepo.save(searchRequest2);

        SearchRequest searchRequest3 = newSearchRequest();
        searchRequest3.setRequestDate(new ISODate("1981-11-13T01:11:00"));
        _requestRepo.save(searchRequest3);

        SearchRequest searchRequest4 = newSearchRequest();
        searchRequest4.setRequestDate(new ISODate("1982-12-16T01:11:00"));
        _requestRepo.save(searchRequest4);

        assertEquals(searchRequest1.getRequestDate(), _requestRepo.getOldestRequestDate());
    }

    private SearchRequest newSearchRequest() {
        final AtomicInteger inc = _inc;
        return newSearchRequest(inc);
    }

    public static SearchRequest newSearchRequest(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setSpatialFilter("spatialRequest" + val);
        searchRequest.setSortBy("sortby" + val);
        searchRequest.setSimple(val % 2 == 0);
        searchRequest.setService("service" + val);
        searchRequest.setAutogenerated(val % 2 == 1);
        searchRequest.setHits(val);
        searchRequest.setIpAddress("ip" + val);
        searchRequest.setLang("l" + val);
        searchRequest.setLuceneQuery("query" + val);
        searchRequest.setMetadataType("mdtype" + val);
        searchRequest.setRequestDate(new ISODate());
        searchRequest.addParam(SearchRequestParamRepositoryTest.newRequestParam(inc));
        searchRequest.addParam(SearchRequestParamRepositoryTest.newRequestParam(inc));

        return searchRequest;
    }


}
