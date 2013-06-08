package org.fao.geonet.services.statistics;

import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;


/**
 * A basic factory to generate JFreeChart graphics and save them to disk
 * A caching mechanism will be used to return an already generated graphic file,
 * based on the file name
 * todo: use exception for errors ?
 * @author nicolas Ribot
 *
 */
public class ChartFactory {
	public final static byte TIMESERIES = 0;
	public final static byte PIECHART = 1;
	
//	private final String logDateFormat = "";
//	private boolean verboseMessage;
	
	public ChartFactory(boolean verbose) {
//		this.verboseMessage = verbose;
	}
	
	/**
	 * returns the RegularDatePeriod corresponding to the given dateType (day,
	 * month, year, see constants)
	 * 
	 * @param dateType
	 *            the type of date
	 * @param date
	 *            the date, expressed as yyyy-MM-ddThh:mm:ss java pattern
	 * @return a timePeriod with the right type
	 */
//	private RegularTimePeriod getTimePeriod(
//			String dateType, 
//			String date,
//			DateFormat dateFormat) throws Exception {
//		if ("MONTH".equals(dateType)) {
//			return new Month(Integer.parseInt(date.substring(5, 7)), Integer
//					.parseInt(date.substring(0, 4)));
//		} else if ("YEAR".equals(dateType)) {
//			return new Year(Integer.parseInt(date.substring(0, 4)));
//		} else if ("DAY".equals(dateType)) {
//			return new Day(dateFormat.parse(date));
//		}
//		return null;
//	}
	
	/**
	 * returns the name of the PNG image generated to represent the given record
	 * object values into the given graphic type (see public class constant for list
	 * of supported types
	 * @param graphType the graphic type
	 * @param fileName the absolute filename to generates
	 * @param record the Element object containing values to represent into a graphic
	 * @return the absolute path to the generated file
	 */
	public boolean getChartFromRecord(String[] columns, byte graphType, String fileName) {
		switch (graphType) {
		case ChartFactory.TIMESERIES :
			break;
		case ChartFactory.PIECHART :
			break;
		default:
			return false;
		}
		
		return true;
	}
	
	/**
	 * Writes the given chart to the given file
	 * @param chart
	 * @param outFile
	 * @param width
	 * @param height
	 * @param createTooltips true to return the HTML tooltip code
	 * @return empty string if createToolTips is false, the HTML tooltip code otherwise
	 * @throws java.io.IOException if an error occured during writing.
	 */
	public static String writeChartImage(
            JFreeChart chart,
            File outFile,
            int width,
            int height,
            boolean createTooltips,
            String imageMapName) throws IOException {

        FileOutputStream fout = null;
		String res = "";
		try {
			fout = new FileOutputStream(outFile);
			ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();
			ChartUtilities.writeChartAsPNG(
					fout, 
					chart, 
					width, 
					height,
					chartRenderingInfo);
			if (createTooltips) {
				// gets some tooltips:
				res = ImageMapUtilities.getImageMap(imageMapName, chartRenderingInfo);
			}
			fout.flush();
			fout.close();
		} finally {
		    IOUtils.closeQuietly(fout);
		}
		return res;
	}

    /**
     *
     * @param dataset the TimeSeriesCollection containing data to plot
     * @param xAxisLabel the label for X axis
     * @param yAxisLabel the label for Y axis
     * @param axisDateFormat the overload for date format on X axis
     * @param createLegend true to create a legend for the graphic
     * @param createTooltips true to create a tooltip for the graphic (imageMap)
     * @return
     */
    public static JFreeChart getTimeSeriesChart(
            TimeSeriesCollection dataset,
            String xAxisLabel,
            String yAxisLabel,
            String axisDateFormat,
            boolean createLegend,
            boolean createTooltips) {

        JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
				null,
				xAxisLabel, // x-axis label
				yAxisLabel, // y-axis label
				dataset, // data
				createLegend, // create legend?
				createTooltips, // generate tooltips?
				false // generate URLs?
				);

		//hard coded values for the moment. should come from a configuration file.
		chart.setBackgroundPaint(Color.decode("#E7EDF5"));
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
		}
		DateAxis axis = (DateAxis) plot.getDomainAxis();

		axis.setDateFormatOverride(new SimpleDateFormat(axisDateFormat));
		axis.setAutoRange(true);
		//axis.setTickUnit(new DateTickUnit(DateTickUnit.MONTH, this.tickUnit));
		NumberAxis Yaxis = (NumberAxis) plot.getRangeAxis();
		TickUnitSource units = NumberAxis.createIntegerTickUnits();
		Yaxis.setStandardTickUnits(units);

        return chart;
    }
}
