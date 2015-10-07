package org.fao.geonet.services.openwis;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to return lists using the data-tables format.
 *
 * @author Jose García
 *
 * @param <T>
 */
public class DataListResponse<T> {
    private List<T> data = new ArrayList<T>();
    private Long recordsTotal;
    private Long recordsFiltered;

    private Integer draw;

    public Long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public Long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }

    public List<T> getData() {
        return data;
    }

    /**
     * Adds a new element to the list.
     *
     * @param element
     */
    public void addData(T element) {
        data.add(element);
    }

    /**
     * Adds a new element to the list.
     *
     * @param elements
     */
    public void addAllData(List<T> elements) {
        data.addAll(elements);
    }
}
