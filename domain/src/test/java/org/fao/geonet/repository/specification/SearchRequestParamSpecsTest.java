package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.statistic.SearchRequestParamRepository;
import org.fao.geonet.repository.statistic.SearchRequestParamRepositoryTest;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.fao.geonet.repository.statistic.SearchRequestRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test SearchRequestParamSpecs
 * User: Jesse
 * Date: 10/8/13
 * Time: 11:26 AM
 */
public class SearchRequestParamSpecsTest extends AbstractSpringDataTest {
    @Autowired
    SearchRequestParamRepository _paramRepo;
    @Autowired
    SearchRequestRepository _requestRepo;

    @Test
    public void testHasTermField() throws Exception {
        final SearchRequestParam param = _paramRepo.save(SearchRequestParamRepositoryTest.newRequestParam(_inc));
        _paramRepo.save(SearchRequestParamRepositoryTest.newRequestParam(_inc));

        final List<SearchRequestParam> all = _paramRepo.findAll(SearchRequestParamSpecs.hasTermField(param.getTermField()));

        assertEquals(1, all.size());

        assertEquals(param.getId(), all.get(0).getId());
    }

    @Test
    public void testHasTermFieldIn() throws Exception {
        final SearchRequestParam param = _paramRepo.save(SearchRequestParamRepositoryTest.newRequestParam(_inc));
        final SearchRequestParam param2 = _paramRepo.save(SearchRequestParamRepositoryTest.newRequestParam(_inc));
        _paramRepo.save(SearchRequestParamRepositoryTest.newRequestParam(_inc));

        final List<String> termFields = Arrays.asList(param.getTermField(), param2.getTermField());
        final List<SearchRequestParam> all = _paramRepo.findAll(SearchRequestParamSpecs.hasTermFieldIn(termFields));

        assertEquals(2, all.size());

        List<Integer> ids = Lists.transform(all, new Function<SearchRequestParam, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable SearchRequestParam input) {
                return input.getId();
            }
        });
        assertTrue(ids.contains(param.getId()));
        assertTrue(ids.contains(param2.getId()));
    }

    @Test
    public void testHasService() throws Exception {
        _requestRepo.save(SearchRequestRepositoryTest.newSearchRequest(_inc));

        SearchRequest request1 = SearchRequestRepositoryTest.newSearchRequest(_inc);
        final String expectedService = "ExpectedService";
        request1.setService(expectedService);
        while (request1.getParams().size() > 1) {
            request1.getParams().remove(0);
        }
        assertEquals(1, request1.getParams().size());
        request1 = _requestRepo.save(request1);

        final List<SearchRequestParam> all = _paramRepo.findAll(SearchRequestParamSpecs.hasService(expectedService));

        assertEquals(1, all.size());

        assertEquals(request1.getParams().get(0).getId(), all.get(0).getId());
    }
}
