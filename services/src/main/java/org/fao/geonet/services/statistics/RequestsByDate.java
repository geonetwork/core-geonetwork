package org.fao.geonet.services.statistics;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.statistic.DateInterval;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.*;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;

/**
 * Service to get the db-stored requests information group by a date (year, month, day)
 * @author nicolas Ribot
 *
 */
public class RequestsByDate extends NotInReadOnlyModeService {

    /** the custom part of the date query; according to user choice for graphic */
	private Hashtable<String, String> queryFragments;

	/** should we generate and send tooltips to client (caution, can slow down the process if
	 * dataset is big)
	 */
    boolean createTooltips;
	/** should we generate and send legend to client (caution, can slow down the process if
	 * dataset is big)
	 */
    boolean createLegend;

	/** chart width, service parameter, can be overloaded by request */
    int chartWidth;

	/** chart width, can be overloaded by request */
    int chartHeight;

    /** the Element doc containing I18N strings, got from the current app language */
    private Cache<String, Element> i18nStringsCache;

    /** the current language */
    private String defaultLang;

    /** the full path to the application directory */
    private  String appDir;



	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception	{
        super.init(appPath, params);
		this.createLegend = Boolean.parseBoolean(params.getValue("createLegend"));
		this.createTooltips = Boolean.parseBoolean(params.getValue("createTooltips"));
		this.chartWidth = Integer.parseInt(params.getValue("chartWidth"));
		this.chartHeight = Integer.parseInt(params.getValue("chartHeight"));

        this.appDir = appPath;
        this.defaultLang = "eng";
        final String SEP = File.separator;
        this.i18nStringsCache = CacheBuilder.<String, Element>newBuilder().softValues().maximumSize(3).build();
        this.i18nStringsCache.put(defaultLang, loadStrings(appPath + "loc" + SEP + this.defaultLang + SEP + "xml" + SEP + "strings.xml"));

        queryFragments = new Hashtable<String, String>(3);

        queryFragments.put(RequestsByDateParams.BY_DAY,   "substring(requestDate, 1, 10)");
        queryFragments.put(RequestsByDateParams.BY_MONTH, "substring(requestDate, 1, 7)");
        queryFragments.put(RequestsByDateParams.BY_YEAR,  "substring(requestDate, 1, 4)");

	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
    @Override
	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        final String SEP = File.separator;
        final RequestsByDateParams dateParams = new RequestsByDateParams();

        synchronized (this) {
            final String language = context.getLanguage();
            dateParams.setLang(language);
            Element strings = i18nStringsCache.getIfPresent(language);

            if (strings == null) {
                // user changed the language, must reload strings file to get translated values
                this.i18nStringsCache.put(language, loadStrings(appDir + "loc" + SEP
                                                                  + language + SEP + "xml" + SEP + "strings.xml"));
            }

            dateParams.setStringElementHashMap18nStrings(strings);
        }
		dateParams.setDateFrom(Util.getParam(params, "dateFrom"));
        dateParams.setDateTo(Util.getParam(params, "dateTo"));
        dateParams.setGraphicType(Util.getParam(params, "graphicType"));
		String message = "";

		// initialize some variables needed by JFreeChart
		if (dateParams.getGraphicType() instanceof DateInterval.Month) {
            dateParams.setChartClass(Month.class);
        } else if (dateParams.getGraphicType() instanceof DateInterval.Year) {
            dateParams.setChartClass(Year.class);
        } else if (dateParams.getGraphicType()  instanceof DateInterval.Day) {
            dateParams.setChartClass(Day.class);
        }

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		
		TimeSeries ts = new TimeSeries("By " + dateParams.getGraphicType().getClass().getSimpleName(), dateParams.getChartClass());
		TimeSeriesCollection dataset = new TimeSeriesCollection();

        SearchRequestRepository repository = context.getBean(SearchRequestRepository.class);

        List<Pair<DateInterval, Integer>> countMap = repository.getRequestDateToRequestCountBetween(dateParams.getGraphicType(),
                new ISODate(dateParams.getDateFrom()),
                new ISODate(dateParams.getDateTo()));
		for (Pair<DateInterval, Integer> record : countMap) {
			String curDate = record.one().getDateString();

			ts.add(getTimePeriod(dateParams.getGraphicType(), curDate), record.two());
		}
		dataset.addSeries(ts);

   		String axisFormat = "MM/yy";
		if (dateParams.getGraphicType()  instanceof DateInterval.Month) {
			axisFormat = "MM/yy";
		} else if (dateParams.getGraphicType()  instanceof DateInterval.Year) {
			axisFormat = "yyyy";
		} else if (dateParams.getGraphicType()  instanceof DateInterval.Day) {
			axisFormat = "dd/MM/yy";
		}

        String xAxisLabel = this.getI18NValue(dateParams, "stat." + dateParams.getGraphicType().getClass().getSimpleName().toLowerCase());
        String yAxisLabel = this.getI18NValue(dateParams, "stat.numberOfSearch");

        JFreeChart chart = ChartFactory.getTimeSeriesChart(
                dataset,
                xAxisLabel,
                yAxisLabel,
                axisFormat,
                this.createLegend,
                this.createTooltips);

		// chart filename is built here with pattern:
		// type_datefrom_dateto.png, after having removed time from date
		// build tmp path from Jeeves context
		String chartFilename = getFileName(dateParams);

		File statFolder = new File(gc.getBean(ServiceConfig.class).getMandatoryValue(
				Geonet.Config.RESOURCES_DIR) + SEP + "images" + SEP + "statTmp");
        IO.mkdirs(statFolder, "Statistics temp directory");

		File imageFile = new File(statFolder, chartFilename);
		//if (!f.exists()) {
			// generate the graph
        dateParams.setImageMap(ChartFactory.writeChartImage(
                chart, imageFile,
                this.chartWidth,
                this.chartHeight,
                this.createTooltips,
                "graphByDateImageMap"));

        message = "Graphic generated from request";
		//} else {
		//	message = "cached graphic image used";
		//}

		// will return some info to the XSLT:
		// dateFrom, dateTo, graphicType, chartUrl, tooltipImageMap,
		// message, chartWidth, chartHeight

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		Element elDateFrom = new Element("dateFrom").setText(dateParams.getDateFrom());
		Element elDateTo = new Element("dateTo").setText(dateParams.getDateTo());
		Element elGraph = new Element("graphicType").setText(dateParams.getGraphicType().getClass().toString().toUpperCase());
		Element elchartUrl = new Element("graphByDateUrl").setText(context.getBaseUrl() + "/images/statTmp/" + chartFilename);
		Element elTooltipImageMap = new Element("tooltipImageMap").addContent(this.createTooltips ? dateParams.getImageMap() : "");

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

	/**
	 * type_datefrom_dateto.png, after having replaced semi column from date:
	 * example: YEAR_20090213T120300_20100101T120300.png
	 * @return
	 */
	public String getFileName(RequestsByDateParams dateParams) {
		return dateParams.getGraphicType() + "_" + dateParams.getDateFrom().replaceAll(":", "") + "_" + dateParams.getDateTo().replaceAll(":", "") + ".png";
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
    RegularTimePeriod getTimePeriod(
            DateInterval dateType,
            String date) throws Exception {

		if (dateType instanceof DateInterval.Month) {
			return new Month(Integer.parseInt(date.substring(5, 7)), Integer
					.parseInt(date.substring(0, 4)));
		} else if (dateType instanceof DateInterval.Year) {
			return new Year(Integer.parseInt(date.substring(0, 4)));
		} else if (dateType instanceof DateInterval.Day) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return new Day(dateFormat.parse(date));
		}
		return null;
	}


    /**
     * Returns the value corresponding to the given key by looking at Strings.xml I18N files,
     * for the given 3-letters country code
     * @param key the key whose value is needed
     * @return the value for the given key, or the key itself if value not found
     */
    private String getI18NValue(RequestsByDateParams params, String key) {
        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) Log.debug(Geonet.SEARCH_LOGGER,"searching for key: " + key);
        if (params.getI18nStrings() == null) {
            if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                Log.debug(Geonet.SEARCH_LOGGER,"I18N file is null, returning key as value: " + key);
            }
            return key;
        }
        return params.getI18nStrings().getChildText(key) == null ? key : params.getI18nStrings().getChildText(key);
    }

    private Element loadStrings(String filePath) {
        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
            Log.debug(Geonet.SEARCH_LOGGER,"loading file: " + filePath);
        }
        File f = new File(filePath);
        Element xmlDoc = null;
        Element ret = null;

        if ( f.exists() ) {
            try {
                xmlDoc = Xml.loadFile(f);
            } catch (Exception ex) {
                if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                    Log.debug(Geonet.SEARCH_LOGGER,"Cannot load file: " + filePath + ": " + ex.getMessage());
                }
                return ret;
            }
            ret = xmlDoc;
        }
        return ret;
    }
}
