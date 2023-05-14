package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import org.fao.geonet.utils.Log;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HarvesterTest {

    @Test
    public void test_buildPagesUrl() {
        final SimpleUrlParams params = new SimpleUrlParams(null);
        params.url = "http://dados.gov.br/api/3/action/package_search?q=&rows=10&start=1";
        params.pageFromParam = "start";
        params.pageSizeParam = "rows";

        int numberOfResult = 21;

        final Harvester harvester = new Harvester(null, Log.createLogger("TEST"), null, params);
        List<String> list = harvester.buildListOfUrl(params, numberOfResult);
        assertEquals(3, list.size());
        assertEquals("http://dados.gov.br/api/3/action/package_search?q=&rows=10&start=1", list.get(0));
        assertEquals("http://dados.gov.br/api/3/action/package_search?q=&rows=10&start=11", list.get(1));
        assertEquals("http://dados.gov.br/api/3/action/package_search?q=&rows=1&start=21", list.get(2));



        params.url = "http://dados.gov.br/api/3/action/package_search?q=&rows=10&start=0";
        list = harvester.buildListOfUrl(params, numberOfResult);
        assertEquals(3, list.size());
        assertEquals("http://dados.gov.br/api/3/action/package_search?q=&rows=10&start=0", list.get(0));
        assertEquals("http://dados.gov.br/api/3/action/package_search?q=&rows=10&start=10", list.get(1));
        assertEquals("http://dados.gov.br/api/3/action/package_search?q=&rows=1&start=20", list.get(2));

        params.url = "http://dados.gov.br/api/3/action/package_search?q=&rows=DADA&start=1";
        list = harvester.buildListOfUrl(params, numberOfResult);
        assertEquals(1, list.size());

        params.url = "http://dados.gov.br/api/3/action/package_search?q=&rows=11&start=DADA";
        list = harvester.buildListOfUrl(params, numberOfResult);
        assertEquals(1, list.size());

        params.url = "http://dados.gov.br/api/3/action/package_search?q=&&start=1";
        list = harvester.buildListOfUrl(params, numberOfResult);
        assertEquals(1, list.size());

        params.url = "http://dados.gov.br/api/3/action/package_search?q=&rows=11&";
        list = harvester.buildListOfUrl(params, numberOfResult);
        assertEquals(1, list.size());


        params.url = "http://dados.gov.br/api/3/action/package_search?q=&rows=2&start=0";
        list = harvester.buildListOfUrl(params, 8);
        assertEquals(4, list.size());
    }
}
