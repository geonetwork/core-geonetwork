package org.fao.geonet.wro4j;

import org.fao.geonet.utils.Log;
import ro.isdc.wro.http.WroFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketException;

/**
 * @author Jesse on 2/1/2015.
 */
public class GeonetWro4jFilter extends WroFilter {

    public static final String GEONET_WRO4J_FILTER_KEY = "GEONET_WRO4J_FILTER_KEY";

    @Override
    protected void doInit(FilterConfig config) throws ServletException {
        super.doInit(config);
        config.getServletContext().setAttribute(GEONET_WRO4J_FILTER_KEY, this);
    }

    @Override
    protected void onException(Exception e, HttpServletResponse response, FilterChain chain) {
        if (e.getCause() instanceof SocketException) {
            // ignore this because it means that a client closed the socket while data was being written to it.
        } else {
            Log.error(GeonetworkWrojManagerFactory.WRO4J_LOG, "Error occurred during a wro4j request handling", e);
            // Do not call super to avoid having WRO4J chaining
            // to other filters.
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
}
