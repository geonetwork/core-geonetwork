/**
 * 
 */
package org.fao.geonet.services.openwis.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class Request {

    // Ajax
    private int draw;
    private int start;
    private int length;
    private Map<SearchCriterias, String> search;
    private List<Map<ColumnCriterias, String>> columns = null;
    private List<Map<OrderCriterias, String>> order = null;

    public enum SearchCriterias {
        value, regex
    }

    public enum OrderCriterias {
        column, dir
    }

    public enum ColumnCriterias {
        data, name, searchable, orderable, searchValue, searchRegex
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Map<SearchCriterias, String> getSearch() {
        return search;
    }

    public void setSearch(Map<SearchCriterias, String> search) {
        this.search = search;
    }

    public List<Map<ColumnCriterias, String>> getColumns() {
        if (columns == null) {
            columns = new LinkedList<Map<ColumnCriterias, String>>();
        }
        return columns;
    }

    public void setColumns(List<Map<ColumnCriterias, String>> columns) {
        this.columns = columns;
    }

    public List<Map<OrderCriterias, String>> getOrder() {
        if (order == null) {
            order = new LinkedList<Map<OrderCriterias, String>>();
        }
        return order;
    }

    public void setOrder(List<Map<OrderCriterias, String>> order) {
        this.order = order;
    }

    // sAjaxSource for filter by column
    private String sSearch;
    private String iSortCol_0 = null;
    private String sSortDir_0 = null;

    public void setiDisplayStart(Integer iDisplayStart) {
        this.start = iDisplayStart;
    }

    public void setiDisplayLength(Integer iDisplayLength) {
        this.length = iDisplayLength;
    }

    public String getsSearch() {
        return sSearch;
    }

    public void setsEcho(Integer sEcho) {
        this.draw = sEcho;
    }

    public void setsColumns(String sColumns) {
        String[] columns = sColumns.split(",");
        List<Map<ColumnCriterias, String>> list = getColumns();
        for (String column : columns) {
            Map<ColumnCriterias, String> map = new HashMap<ColumnCriterias, String>();
            map.put(ColumnCriterias.name, column);
            list.add(map);
        }
    }

    public void setsSearch(String sSearch) {
        this.sSearch = sSearch;
    }

    public void setiSortCol_0(String iSortCol_0) {
        this.iSortCol_0 = iSortCol_0;
        if (this.sSortDir_0 != null) {
            this.getOrder().add(setUpSortCriteria());
        }
    }

    public void setsSortDir_0(String sSortDir_0) {
        this.sSortDir_0 = sSortDir_0;
        if (this.iSortCol_0 != null) {
            this.getOrder().add(setUpSortCriteria());
        }
    }

    private Map<OrderCriterias, String> setUpSortCriteria() {
        Map<OrderCriterias, String> map = new HashMap<OrderCriterias, String>();

        map.put(OrderCriterias.column, iSortCol_0);
        map.put(OrderCriterias.dir, sSortDir_0);

        return map;
    }

    public void populate(HttpServletRequest httpRequest) {
        Integer i = 0;
        Map<String, String[]> params = httpRequest.getParameterMap();
        for (Map<ColumnCriterias, String> column : getColumns()) {
            if (params.containsKey("mDataProp_" + 1)) {
                column.put(ColumnCriterias.data,
                        params.get("mDataProp_" + i)[0]);
            }
            if (params.containsKey("bSortable_" + 1)) {
                column.put(ColumnCriterias.orderable,
                        params.get("bSortable_" + i)[0]);
            }
            if (params.containsKey("bSearchable_" + 1)) {
                column.put(ColumnCriterias.searchable,
                        params.get("bSearchable_" + i)[0]);
            }
            if (params.containsKey("bRegex_" + 1)) {
                column.put(ColumnCriterias.searchRegex,
                        params.get("bRegex_" + i)[0]);
            }
            if (params.containsKey("sSearch_" + 1)) {
                column.put(ColumnCriterias.searchValue,
                        params.get("sSearch_" + i)[0]);
            }
            i++;
        }
    }
}
