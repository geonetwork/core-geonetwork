package org.fao.geonet.api.reports;

import jeeves.server.context.ServiceContext;

import java.io.PrintWriter;

/**
 * Interface to implement by reports to build a report
 * and stream to a PrintWriter.
 *
 * @author Jose García
 */
public interface IReport {
    /**
     * Creates a report and streams to a PrintWriter.
     *
     * @param context Service context.
     * @param writer Writer.
     * @throws Exception Exception creating a report.
     */
    void create(ServiceContext context, PrintWriter writer) throws Exception;
}
