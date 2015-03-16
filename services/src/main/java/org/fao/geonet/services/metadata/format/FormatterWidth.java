package org.fao.geonet.services.metadata.format;

/**
 * An indicator of how wide the html container which will contain the formatted metadata with respect to the full width of the
 * screen.  This is required because when embedded, the media queries aren't useful for determining the which css to apply.
 * For example bootstrap grid is broken when embedded.
 * @author Jesse on 3/12/2015.
 */
public enum FormatterWidth {
    /**
     * Indicates the element that the formatter will be embedded in is approximately 25% of the width of the screen.
     */
    _25,
    /**
     * Indicates the element that the formatter will be embedded in is approximately 50% of the width of the screen.
     */
    _50,
    /**
     * Indicates the element that the formatter will be embedded in is approximately 75% of the width of the screen.
     */
    _75,
    /**
     * Indicates the element that the formatter will be embedded in is approximately 100% of the width of the screen.
     */
    _100
}
