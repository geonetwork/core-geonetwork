//=============================================================================
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
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geoPREST;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import org.fao.geonet.utils.Log;
import org.junit.Assert;

import org.junit.Test;

public class HarvesterTest
{

    public HarvesterTest() {
    }

    @Test
    public void testParseDate() throws Exception {

        Harvester h = new Harvester(null, Log.createLogger("TEST"), null, null, new ArrayList<>());

        // test EN date
        h.parseDate("Mon, 04 Feb 2013 10:19:00 +1000");

        // test DE date
        h.parseDate("Fr, 24 Mär 2017 10:58:59 +0100");
    }

    /**
     * @see https://bugs.openjdk.java.net/browse/JDK-8136539
     */
    @Test
    public void testJDK8136539Workaround() throws Exception {

        Harvester h = new Harvester(null, Log.createLogger("TEST"), null, null, new ArrayList<>());
        Date p0 = h.parseDate("Fr, 24 Mär 2017 10:58:59 +0100");
        Date p1 = h.parseDate("Fr, 24 Mrz 2017 10:58:59 +0100");

        Assert.assertEquals(p1,p0);
    }

    @Test
    public void testUnparsableDate() throws Exception {

        Harvester h = new Harvester(null, Log.createLogger("TEST"), null, null, new ArrayList<>());

        try {
            h.parseDate("Xyz, 04 Feb 2013 10:19:00 +1000");
            Assert.fail("Undetected bad input");
        } catch (ParseException ex) {
            // ok, the exception is expected
        }
    }


}
