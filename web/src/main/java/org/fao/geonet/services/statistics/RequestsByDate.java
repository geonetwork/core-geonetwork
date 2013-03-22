package org.fao.geonet.services.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;

/**
 * Service to get the db-stored requests information group by a date (year, month, day)
 * @author nicolas Ribot
 *
 */
public class RequestsByDate extends NotInReadOnlyModeService{
	public static final String BY_YEAR = "YEAR";
	public static final String BY_MONTH = "MONTH";
	public static final String BY_DAY = "DAY";

	/** the date to search for from (format MUST be: ) */
	private String dateFrom;
	/** the date to search for too (format MUST be: yyy-MM-ddThh:mm) */
	private String dateTo;
	/** the type of graphic (by year, month or day to display */
	private String graphicType;

	/** the graph factory used to write images */
	//private ChartFactory chartFact;

	/** the class of the time period to get from JFreeeChart, to allow timeSeries to be
	 * correctly formatted */
	private Class chartClass;

	/** the custom part of the date query; according to user choice for graphic */
	private Hashtable<String, String> queryFragments;

	/** should we generate and send tooltips to client (caution, can slow down the process if
	 * dataset is big)
	 */
	private boolean createTooltips;

	/** the imagemap for this chart, allowing to display tooltips */
	private String imageMap;
	/** should we generate and send legend to client (caution, can slow down the process if
	 * dataset is big)
	 */
	private boolean createLegend;

	/** jFreeChart parameter: number by which the ticks will be divided for units
	 *  ie: 2 means that only one unit on two will be rendered
	 * */
	private int tickUnit;

	/** chart width, service parameter, can be overloaded by request */
	private int chartWidth;

	/** chart width, can be overloaded by request */
	private int chartHeight;
    
    /** the Element doc containing I18N strings, got from the current app language */
    private Element i18nStrings;
    
    /** the full path to the application directory */
    private  String appDir;
    /** the current language */
    private String lang;



	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception	{
        super.init(appPath, params);
		this.createLegend = Boolean.parseBoolean(params.getValue("createLegend"));
		this.createTooltips = Boolean.parseBoolean(params.getValue("createTooltips"));
		this.tickUnit = Integer.parseInt(params.getValue("tickUnit"));
		this.chartWidth = Integer.parseInt(params.getValue("chartWidth"));
		this.chartHeight = Integer.parseInt(params.getValue("chartHeight"));

		//this.chartFact = new ChartFactory(verbose);

		queryFragments = new Hashtable<String, String>(3);

		queryFragments.put(RequestsByDate.BY_DAY,   "substring(requestDate, 1, 10)");
		queryFragments.put(RequestsByDate.BY_MONTH, "substring(requestDate, 1, 7)");
		queryFragments.put(RequestsByDate.BY_YEAR,  "substring(requestDate, 1, 4)");

        this.appDir = appPath;
        this.lang = "eng";
        this.i18nStrings = loadStrings(appPath + "loc" + File.separator + this.lang + File.separator  + "xml" + File.separator + "strings.xml");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
    @Override
	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        boolean readOnlyMode = super.exec(params, context) == null;
        if(readOnlyMode) {
            return null;
        }
        if (! this.lang.equalsIgnoreCase(context.getLanguage()) ) {
            // user changed the language, must reload strings file to get translated values
            this.lang = context.getLanguage();
            this.i18nStrings = loadStrings(appDir + "loc" +
                    File.separator +
                    this.lang +
                    File.separator  + 
                    "xml" +
                    File.separator +
                    "strings.xml");
        }
		this.dateFrom = Util.getParam(params, "dateFrom");
		this.dateTo = Util.getParam(params, "dateTo");
		this.graphicType  = Util.getParam(params, "graphicType");
		String message = "";

		// initialize some variables needed by JFreeChart
		if (this.graphicType.equals(RequestsByDate.BY_MONTH))
			this.chartClass = Month.class;
		else if (this.graphicType.equals(RequestsByDate.BY_YEAR))
			this.chartClass = Year.class;
		else if (this.graphicType.equals(RequestsByDate.BY_DAY))
			this.chartClass = Day.class;
		// query to values according to type,
		String query = buildQuery();
        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) Log.debug(Geonet.SEARCH_LOGGER,"query to get count by date:\n" + query);
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		
		TimeSeries ts = new TimeSeries("By " + this.graphicType.toLowerCase(), this.chartClass);
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		List resultSet = dbms.select(query, this.dateFrom, this.dateTo).getChildren();

		for (int i=0; i < resultSet.size(); i++) {
			Element record = (Element) resultSet.get(i);
			String curDate = record.getChildText("reqdate");
			ts.add(getTimePeriod(
					graphicType,
					curDate),
					Double.parseDouble(record.getChildText("number")));
		}
		dataset.addSeries(ts);

   		String axisFormat = "MM/yy";
		if ("MONTH".equals(this.graphicType)) {
			axisFormat = "MM/yy";
		} else if ("YEAR".equals(this.graphicType)) {
			axisFormat = "yyyy";
		} else if ("DAY".equals(this.graphicType)) {
			axisFormat = "dd/MM/yy";
		}

        String xAxisLabel = this.getI18NValue("stat." + this.graphicType.toLowerCase());
        String yAxisLabel = this.getI18NValue("stat.numberOfSearch");

        JFreeChart chart = org.fao.geonet.services.statistics.ChartFactory.getTimeSeriesChart(
                dataset,
                xAxisLabel,
                yAxisLabel,
                axisFormat,
                this.createLegend,
                this.createTooltips);

		// chart filename is built here with pattern:
		// type_datefrom_dateto.png, after having removed time from date
		// build tmp path from Jeeves context
		String chartFilename = getFileName();

		File statFolder = new File(gc.getHandlerConfig().getMandatoryValue(
				Geonet.Config.RESOURCES_DIR) + File.separator + "images" + File.separator + "statTmp");
		if (!statFolder.exists()) {
			statFolder.mkdirs();
		}

		File f = new File(statFolder, chartFilename);
		//if (!f.exists()) {
			// generate the graph
			this.imageMap = org.fao.geonet.services.statistics.ChartFactory.writeChartImage(
					chart,
					f,
					this.chartWidth,
					this.chartHeight,
					this.createTooltips,
					"graphByDateImageMap");
			message = "Graphic generated from request";
		//} else {
		//	message = "cached graphic image used";
		//}

		// will return some info to the XSLT:
		// dateFrom, dateTo, graphicType, chartUrl, tooltipImageMap,
		// message, chartWidth, chartHeight

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		Element elDateFrom = new Element("dateFrom").setText(this.dateFrom);
		Element elDateTo = new Element("dateTo").setText(this.dateTo);
		Element elGraph = new Element("graphicType").setText(this.graphicType);
		Element elchartUrl = new Element("graphByDateUrl").setText(context.getBaseUrl() +
				"/images/statTmp/" + chartFilename);
		Element elTooltipImageMap = new Element("tooltipImageMap").addContent(
				this.createTooltips ? this.imageMap : "");

		Element elMessage = new Element("message").setText(message);
		Element elChartWidth= new Element("chartWidth").setText("" + this.chartWidth);
		Element elChartHeight= new Element("chartHeight").setText("" + this.chartHeight);

		elResp.addContent(elDateFrom);
		elResp.addContent(elDateTo);
		elResp.addContent(elGraph);
		elResp.addContent(elchartUrl);
		elResp.addContent(elTooltipImageMap);
		elResp.addContent(elMessage);
		elResp.addContent(elChartWidth);
		elResp.addContent(elChartHeight);

		return elResp;
	}

	public String buildQuery() {
        String requestDateSubstring = this.queryFragments.get(this.graphicType);

		StringBuilder query = new StringBuilder("SELECT ");
        query.append(requestDateSubstring);
        query.append(" as reqdate, count(*) as number FROM Requests ");
		query.append(" where requestdate >= ?");
		query.append(" and requestdate <= ?");
        query.append(" GROUP BY ");
        query.append(requestDateSubstring);
        query.append(" ORDER BY ");
        query.append(requestDateSubstring);

		return query.toString();
	}

	/**
	 * type_datefrom_dateto.png, after having replaced semi column from date:
	 * example: YEAR_20090213T120300_20100101T120300.png
	 * @return
	 */
	public String getFileName() {
		return this.graphicType + "_" + this.dateFrom.replaceAll(":","") + "_" + this.dateTo.replaceAll(":", "") + ".png";
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
	private RegularTimePeriod getTimePeriod(
			String dateType,
			String date) throws Exception {

		if ("MONTH".equals(dateType)) {
			return new Month(Integer.parseInt(date.substring(5, 7)), Integer
					.parseInt(date.substring(0, 4)));
		} else if ("YEAR".equals(dateType)) {
			return new Year(Integer.parseInt(date.substring(0, 4)));
		} else if ("DAY".equals(dateType)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return new Day(dateFormat.parse(date));
		}
		return null;
	}

	private void writeImage(JFreeChart chart, File outFile) throws IOException {
		// File f = new File(System.getProperty("java.io.tmpdir"), "toto.png");
		FileOutputStream fout = null;
		//System.out.println("will generate image to : " + outFile.getAbsolutePath());
		try {
			fout = new FileOutputStream(outFile);
			ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();
			ChartUtilities.writeChartAsPNG(
					fout,
					chart,
					this.chartWidth,
					this.chartHeight,
					chartRenderingInfo);
			if (this.createTooltips) {
				// gets some tooltips:
				this.imageMap = ImageMapUtilities.getImageMap("graphByDateImageMap", chartRenderingInfo);
			}
			fout.flush();
			fout.close();
		} finally {
			try {
				fout.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * To test graphic generation locally
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		RequestsByDate rdb = new RequestsByDate();
		rdb.dateFrom = "2009-04-03T12:00:00";
		rdb.dateTo = "2009-04-04T12:00:00";
		rdb.graphicType = RequestsByDate.BY_DAY;
		//rdb.graphicType = RequestsByDate.BY_MONTH;
		//rdb.graphicType = RequestsByDate.BY_YEAR;
		rdb.createTooltips = false;
		rdb.createLegend = false;
		rdb.tickUnit = 2;
		rdb.chartWidth = 600;
		rdb.chartHeight = 500;
        //rdb.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        rdb.chartClass = Day.class;

		TimeSeries ts = new TimeSeries("By " + rdb.graphicType.toLowerCase(), rdb.chartClass);
		TimeSeriesCollection dataset = new TimeSeriesCollection();
        String curDate = "2009-04-04T12:00:00";

        RegularTimePeriod tp = rdb.getTimePeriod(rdb.graphicType,curDate);

        ts.add(tp, new Double(2.0));

        curDate = "2009-04-04T14:00:00";
        ts.add(tp, new Double(2.0));

        dataset.addSeries(ts);

        JFreeChart chart = org.fao.geonet.services.statistics.ChartFactory.getTimeSeriesChart(dataset, "toto", "titi", "MM/yy", true, true);
        org.fao.geonet.services.statistics.ChartFactory.writeChartImage(chart, new File("/tmp/toto.png"), 600, 400, true, "imageMapName");
        System.out.println("done.");
    }

    /**
     * Returns the value corresponding to the given key by looking at Strings.xml I18N files,
     * for the given 3-letters country code
     * @param key the key whose value is needed
     * @return the value for the given key, or the key itself if value not found
     */
    private String getI18NValue(String key) {
        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) Log.debug(Geonet.SEARCH_LOGGER,"searching for key: " + key);
        if (this.i18nStrings == null) {
            if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER,"I18N file is null, returning key as value: " + key);
            return key;
        }
        return this.i18nStrings.getChildText(key) == null ? key : this.i18nStrings.getChildText(key);
    }

    private Element loadStrings(String filePath) {
        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) Log.debug(Geonet.SEARCH_LOGGER,"loading file: " + filePath);
        File f = new File(filePath);
        Element xmlDoc = null;
        Element ret = null;

        if ( f.exists() ) {
            try {
                xmlDoc = Xml.loadFile(f);
            } catch (Exception ex) {
                if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                    Log.debug(Geonet.SEARCH_LOGGER,"Cannot load file: " + filePath + ": " + ex.getMessage());
                return ret;
            }
            ret = xmlDoc;
        }
        return ret;
    }
}
