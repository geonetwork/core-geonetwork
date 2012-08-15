package org.fao.geonet.kernel.search.keyword;

public enum SortDirection {
    ASC(-1), DESC(1);
    
    public final int multiplier;

    private SortDirection(int multiplier) {
        this.multiplier = multiplier;
    }
}
