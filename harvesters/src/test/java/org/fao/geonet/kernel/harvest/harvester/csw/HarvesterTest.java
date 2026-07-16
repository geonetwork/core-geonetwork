/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.harvest.harvester.csw;

import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.jdom.Element;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the page recovery logic of the CSW {@link Harvester}.
 *
 * <p>They exercise {@link Harvester#recoverRange} with a fake CSW server that
 * aborts the whole GetRecords response (as GeoNetwork does) when a record of the
 * requested page can not be returned, for instance an ISO 19110 feature
 * catalogue requested with outputSchema=gmd.</p>
 */
public class HarvesterTest {

    /**
     * Fake CSW server: holds a fixed number of records (1-based positions) and a
     * set of "bad" positions that can not be returned. A GetRecords request for a
     * page that contains a bad position fails as a whole, mimicking a real CSW
     * server aborting the response. Positions beyond the end return an empty
     * page.
     */
    private static class FakeCswServer implements Harvester.SearchResultsFetcher {
        final int total;
        final Set<Integer> badPositions;
        int requestCount = 0;

        FakeCswServer(int total, Integer... bad) {
            this.total = total;
            this.badPositions = new TreeSet<>();
            for (Integer b : bad) {
                this.badPositions.add(b);
            }
        }

        @Override
        public Element fetch(int start, int length) throws Exception {
            requestCount++;
            int last = Math.min(start + length - 1, total);
            for (int pos = start; pos <= last; pos++) {
                if (badPositions.contains(pos)) {
                    throw new NoApplicableCodeEx(
                        "OutputSchema 'gmd' not supported for record at position " + pos);
                }
            }
            Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);
            results.setAttribute("numberOfRecordsMatched", Integer.toString(total));
            for (int pos = start; pos <= last; pos++) {
                results.addContent(new Element("Record", Csw.NAMESPACE_CSW)
                    .setAttribute("pos", Integer.toString(pos)));
            }
            return results;
        }
    }

    private static List<Integer> positionsOf(List<Element> records) {
        return records.stream()
            .map(r -> Integer.parseInt(r.getAttributeValue("pos")))
            .sorted()
            .collect(Collectors.toList());
    }

    private static List<Integer> range(int from, int to) {
        return IntStream.rangeClosed(from, to).boxed().collect(Collectors.toList());
    }

    @Test
    public void cleanPageReturnsEveryRecordInOneRequest() throws Exception {
        FakeCswServer server = new FakeCswServer(20);
        List<Element> records = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        int[] matched = {-1};

        int consumed = Harvester.recoverRange(server, 1, 20, records, matched,
            (pos, cause) -> skipped.add(pos));

        assertEquals(range(1, 20), positionsOf(records));
        assertTrue(skipped.isEmpty());
        assertEquals(20, consumed);
        assertEquals(20, matched[0]);
        assertEquals(1, server.requestCount);
    }

    @Test
    public void skipsSingleUnreturnableRecordAndKeepsTheRest() throws Exception {
        FakeCswServer server = new FakeCswServer(20, 10);
        List<Element> records = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        int[] matched = {-1};

        int consumed = Harvester.recoverRange(server, 1, 20, records, matched,
            (pos, cause) -> skipped.add(pos));

        List<Integer> expected = range(1, 20);
        expected.remove(Integer.valueOf(10));
        assertEquals(expected, positionsOf(records));
        assertEquals(List.of(10), skipped);
        assertEquals("the whole page width must be consumed", 20, consumed);
        assertEquals(20, matched[0]);
        // The offending record is isolated by splitting, not by fetching every
        // record one by one.
        assertTrue("recovery should not fetch records one by one (was "
            + server.requestCount + ")", server.requestCount < 20);
    }

    @Test
    public void skipsSeveralUnreturnableRecords() throws Exception {
        FakeCswServer server = new FakeCswServer(20, 5, 15);
        List<Element> records = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        int[] matched = {-1};

        int consumed = Harvester.recoverRange(server, 1, 20, records, matched,
            (pos, cause) -> skipped.add(pos));

        List<Integer> expected = range(1, 20);
        expected.remove(Integer.valueOf(5));
        expected.remove(Integer.valueOf(15));
        assertEquals(expected, positionsOf(records));
        skipped.sort(Integer::compareTo);
        assertEquals(List.of(5, 15), skipped);
        assertEquals(20, consumed);
    }

    @Test
    public void recoversFromBadRecordOnAPartialLastPage() throws Exception {
        // Only 15 records exist but a full page of 20 is requested.
        FakeCswServer server = new FakeCswServer(15, 10);
        List<Element> records = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        int[] matched = {-1};

        int consumed = Harvester.recoverRange(server, 1, 20, records, matched,
            (pos, cause) -> skipped.add(pos));

        List<Integer> expected = range(1, 15);
        expected.remove(Integer.valueOf(10));
        assertEquals(expected, positionsOf(records));
        assertEquals(List.of(10), skipped);
        assertEquals(15, matched[0]);
        assertEquals("only the existing positions are consumed", 15, consumed);
    }

    @Test
    public void skipsAFullPageOfUnreturnableRecordsSoHarvestCanContinue() throws Exception {
        // Records 41..60 (a full page) can not be returned, the matched count is
        // already known from a previous page.
        FakeCswServer server = new FakeCswServer(100,
            range(41, 60).toArray(new Integer[0]));
        List<Element> records = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        int[] matched = {100};

        int consumed = Harvester.recoverRange(server, 41, 20, records, matched,
            (pos, cause) -> skipped.add(pos));

        assertTrue(records.isEmpty());
        skipped.sort(Integer::compareTo);
        assertEquals(range(41, 60), skipped);
        // The full page width is consumed so the caller advances past the block
        // instead of stalling or re-requesting it.
        assertEquals(20, consumed);
    }

    @Test
    public void doesNotRequestPositionsBeyondTheMatchedCount() throws Exception {
        FakeCswServer server = new FakeCswServer(5);
        List<Element> records = new ArrayList<>();
        int[] matched = {5};

        int consumed = Harvester.recoverRange(server, 6, 20, records, matched,
            (pos, cause) -> fail("nothing should be skipped"));

        assertTrue(records.isEmpty());
        assertEquals(0, consumed);
        assertEquals("no request should be sent past the end", 0, server.requestCount);
    }

    @Test
    public void connectionErrorsAbortInsteadOfBeingSkipped() {
        Harvester.SearchResultsFetcher brokenServer = (start, length) -> {
            throw new IOException("connection reset");
        };
        try {
            Harvester.recoverRange(brokenServer, 1, 20, new ArrayList<>(), new int[]{-1},
                (pos, cause) -> fail("a connection error must not be skipped"));
            fail("expected the connection error to propagate");
        } catch (Exception e) {
            assertTrue("expected the original IOException, got " + e,
                e instanceof IOException);
        }
    }

    @Test
    public void treatsEmptySuccessResponseForSingleRecordAsConsumed() throws Exception {
        // A server that returns a valid but empty SearchResults for a single-record
        // window (no exception, just 0 children) must still advance the position by
        // 1 so the caller does not stall or stop the harvest prematurely.
        Harvester.SearchResultsFetcher server = (start, length) -> {
            Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);
            results.setAttribute("numberOfRecordsMatched", "5");
            // deliberately return no Record children
            return results;
        };
        List<Element> records = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        int[] matched = {-1};

        int consumed = Harvester.recoverRange(server, 3, 1, records, matched,
            (pos, cause) -> skipped.add(pos));

        assertTrue(records.isEmpty());
        assertTrue(skipped.isEmpty());
        assertEquals("empty single-record response must consume the position", 1, consumed);
    }

    @Test
    public void requestRejectedErrorsAbortInsteadOfBeingSkipped() {
        Harvester.SearchResultsFetcher rejectedServer = (start, length) -> {
            throw new InvalidParameterValueEx("outputSchema", "gmd");
        };
        try {
            Harvester.recoverRange(rejectedServer, 1, 20, new ArrayList<>(), new int[]{-1},
                (pos, cause) -> fail("a request-rejected error must not be skipped"));
            fail("expected the request-rejected error to propagate");
        } catch (Exception e) {
            assertTrue("expected the original InvalidParameterValueEx, got " + e,
                e instanceof InvalidParameterValueEx);
        }
    }

    @Test
    public void classifiesAnUnreturnableRecordAsRecoverable() {
        // A CSW server that can not present a single record in the requested
        // outputSchema surfaces a generic NoApplicableCode OWS exception (the
        // outputSchema / record id only survive in the message text). This is
        // the case the page recovery is meant to skip.
        assertEquals(Harvester.CswRequestError.RECORD_NOT_RETURNABLE,
            Harvester.classifyRequestError(new NoApplicableCodeEx(
                "OutputSchema 'gmd' not supported for metadata with '2368' (iso19110)")));
        // wrapped in another exception
        assertEquals(Harvester.CswRequestError.RECORD_NOT_RETURNABLE,
            Harvester.classifyRequestError(new RuntimeException(new NoApplicableCodeEx("nope"))));
    }

    @Test
    public void classifiesARejectedRequestAsSystematic() {
        // A wrong outputSchema / typeNames / operation for the endpoint comes
        // back as a specifically typed OWS exception. It fails the same way for
        // every record, so it must abort the harvest, not be skipped.
        assertEquals(Harvester.CswRequestError.REQUEST_REJECTED,
            Harvester.classifyRequestError(new InvalidParameterValueEx("outputSchema", "gmd")));
        assertEquals(Harvester.CswRequestError.REQUEST_REJECTED,
            Harvester.classifyRequestError(new OperationNotSupportedEx("GetRecords")));
        // wrapped in another exception
        assertEquals(Harvester.CswRequestError.REQUEST_REJECTED,
            Harvester.classifyRequestError(new RuntimeException(
                new InvalidParameterValueEx("typeNames", "gmd:MD_Metadata"))));
    }

    @Test
    public void classifiesConnectionErrorsAsTransport() {
        assertEquals(Harvester.CswRequestError.TRANSPORT,
            Harvester.classifyRequestError(new IOException("connection reset")));
        assertEquals(Harvester.CswRequestError.TRANSPORT,
            Harvester.classifyRequestError(new RuntimeException("boom")));
    }
}
