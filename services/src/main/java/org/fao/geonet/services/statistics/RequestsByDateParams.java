package org.fao.geonet.services.statistics;

import org.fao.geonet.repository.statistic.DateInterval;
import org.jdom.Element;
import org.jfree.data.time.RegularTimePeriod;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 9/30/13
 * Time: 8:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestsByDateParams {

    static final String BY_YEAR = "YEAR";
    static final String BY_MONTH = "MONTH";
    static final String BY_DAY = "DAY";
    static final String BY_HOUR = "HOUR";
    /**
     * the date to search for from (format MUST be: )
     */
    private String dateFrom;
    /**
     * the date to search for too (format MUST be: yyy-MM-ddThh:mm)
     */
    private String dateTo;
    /**
     * the type of graphic (by year, month or day to display
     */
    private DateInterval graphicType;

    /** the graph factory used to write images */
    //private ChartFactory chartFact;

    /**
     * the class of the time period to get from JFreeeChart, to allow timeSeries to be
     * correctly formatted
     */
    private Class<? extends RegularTimePeriod> chartClass;

    /**
     * the imagemap for this chart, allowing to display tooltips
     */
    private String imageMap;
    /**
     * the Element doc containing I18N strings, got from the current app language
     */
    private Element stringElementHashMap18nStrings;

    /**
     * the current language
     */
    private String lang;

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public DateInterval getGraphicType() {
        return graphicType;
    }

    public void setGraphicType(String graphicType) {
        if (graphicType.equals(BY_MONTH)) {
            this.graphicType = new DateInterval.Month();
        } else if (graphicType.equals(BY_DAY)) {
            this.graphicType = new DateInterval.Day();
        } else if (graphicType.equals(BY_YEAR)) {
            this.graphicType = new DateInterval.Year();
        } else if (graphicType.equals(BY_HOUR)) {
            this.graphicType = new DateInterval.Hour();
        }
    }

    public Class<? extends RegularTimePeriod> getChartClass() {
        return chartClass;
    }

    public void setChartClass(Class<? extends RegularTimePeriod> chartClass) {
        this.chartClass = chartClass;
    }

    public String getImageMap() {
        return imageMap;
    }

    public void setImageMap(String imageMap) {
        this.imageMap = imageMap;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Element getI18nStrings() {
        return stringElementHashMap18nStrings;
    }

    public void setStringElementHashMap18nStrings(Element stringElementHashMap18nStrings) {
        this.stringElementHashMap18nStrings = stringElementHashMap18nStrings;
    }
}
