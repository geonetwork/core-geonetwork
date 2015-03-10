package org.fao.geonet.services.metadata.format;

/**
 * The interface for formatter implementations.
 *
 * @author Jesse on 10/23/2014.
 */
public interface FormatterImpl {
    public String format(FormatterParams fparams) throws Exception;
}
