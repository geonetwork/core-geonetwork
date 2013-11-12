package org.fao.geonet.services.statistics;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Test RequestsByDate.
 *
 * User: Jesse
 * Date: 9/30/13
 * Time: 8:46 PM
 */
public class RequestsByDateTest {

    @Test
    @Ignore
    public void testByDay() throws Exception {
        RequestsByDate rdb = new RequestsByDate();
        RequestsByDateParams params = new RequestsByDateParams();
        params.setDateFrom("2009-04-03T12:00:00");
        params.setDateTo("2009-04-04T12:00:00");
        params.setGraphicType(RequestsByDateParams.BY_DAY);
        //rdb.graphicType = RequestsByDate.BY_MONTH;
        //rdb.graphicType = RequestsByDate.BY_YEAR;
        //rdb.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        params.setChartClass(Day.class);

        rdb.createTooltips = false;
        rdb.createLegend = false;
        rdb.chartWidth = 600;
        rdb.chartHeight = 500;

        TimeSeries ts = new TimeSeries("By " + params.getGraphicType().getClass().getSimpleName(), params.getChartClass());
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        String curDate = "2009-04-04T12:00:00";

        RegularTimePeriod tp = rdb.getTimePeriod(params.getGraphicType(),curDate);

        ts.add(tp, new Double(2.0));

        curDate = "2009-04-04T14:00:00";
        ts.add(tp, new Double(2.0));

        dataset.addSeries(ts);

        JFreeChart chart = ChartFactory.getTimeSeriesChart(dataset, "toto", "titi", "MM/yy", true, true);
        ChartFactory.writeChartImage(chart, new File("/tmp/toto.png"), 600, 400, true, "imageMapName");

    }
}
