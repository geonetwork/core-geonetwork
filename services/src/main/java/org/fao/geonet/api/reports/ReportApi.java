package org.fao.geonet.api.reports;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * API to produce reports in CSV format.
 *
 * @author Jose García
 */
@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/reports"
})
@Tag(name = ApiParams.API_CLASS_RECORD_TAG,
    description = ApiParams.API_CLASS_RECORD_OPS)
public class ReportApi {

    /**
     * Get list of metadata file downloads.
     *
     * @param dateFrom From date of the metadata downloads.
     * @param dateTo   To date of the metadata downloads.
     * @param groups   Metadata group(s).
     * @param response HttpServletResponse.
     * @param request  HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @Operation(summary = "Get list of metadata file downloads")
    @RequestMapping(
        value = "/datadownloads",
        method = RequestMethod.GET,
        produces = {
            "text/x-csv; charset=UTF-8"
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "",
            description = "List of metadata file downloads.")
    })
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportDataDownloads(
        @Parameter(
            description = "From date of the metadata downloads",
            required = true)
        @RequestParam final String dateFrom,
        @Parameter(
            description = "To date of the metadata downloads",
            required = true)
        @RequestParam final String dateTo,
        @Parameter(
            description = "Metadata group(s)")
        @RequestParam(required = false) final List<Integer> groups,
        final HttpServletResponse response,
        final HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        ReportFilter filter = new ReportFilter(dateFrom, dateTo,
            ReportUtils.groupsForFilter(context, groups));

        ReportDownloads report = new ReportDownloads(filter);
        response.setHeader("Content-Disposition",
            "attachment; filename=\"downloads.csv\"");
        report.create(context, response.getWriter());
    }


    /**
     * Get the uploaded files to metadata records during a period.
     *
     * @param dateFrom From date of the metadata uploads.
     * @param dateTo   To date of the metadata uploads.
     * @param groups   Metadata group(s).
     * @param response HttpServletResponse.
     * @param request  HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get uploaded files to metadata records during a period.")
    @RequestMapping(
        value = "/datauploads",
        method = RequestMethod.GET,
        produces = {
            "text/x-csv; charset=UTF-8"
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
    })
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportDataUploads(
        @Parameter(
            description = "From date of the metadata uploads",
            required = true)
        @RequestParam final String dateFrom,
        @Parameter(
            description = "To date of the metadata uploads",
            required = true)
        @RequestParam final String dateTo,
        @Parameter(
            description = "Metadata group(s)")
        @RequestParam(required = false) final List<Integer> groups,
        final HttpServletResponse response,
        final HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        ReportFilter filter = new ReportFilter(dateFrom, dateTo,
            ReportUtils.groupsForFilter(context, groups));

        ReportUploads report = new ReportUploads(filter);
        response.setHeader("Content-Disposition",
            "attachment; filename=\"uploads.csv\"");
        report.create(context, response.getWriter());
    }


    /**
     * Get the list of users "active" during a time period.
     *
     * @param dateFrom From date of users login date.
     * @param dateTo   To date of users login date.
     * @param groups   Group(s) for the users.
     * @param response HttpServletResponse.
     * @param request  HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get the list of users \"active\" during a time period.")
    @RequestMapping(
        value = "/users",
        method = RequestMethod.GET,
        produces = {
            "text/x-csv; charset=UTF-8"
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "",
            description = "List of users \"active\" during a time period.")
    })
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getActiveUsers(
        @Parameter(
            description = "From date of users login date",
            required = true)
        @RequestParam final String dateFrom,
        @Parameter(
            description = "To date of users login date",
            required = true)
        @RequestParam final String dateTo,
        @Parameter(
            description = "Group(s) for the users")
        @RequestParam(required = false) final List<Integer> groups,
        final HttpServletResponse response,
        final HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        ReportFilter filter = new ReportFilter(dateFrom, dateTo,
            ReportUtils.groupsForFilter(context, groups));

        ReportUsers report = new ReportUsers(filter);
        response.setHeader("Content-Disposition",
            "attachment; filename=\"users.csv\"");
        report.create(context, response.getWriter());
    }


    /**
     * Get the metadata not published during a period.
     *
     * @param dateFrom From date of metadata change date.
     * @param dateTo   To date of metadata change date.
     * @param groups   Metadata group(s).
     * @param response HttpServletResponse.
     * @param request  HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Get the metadata not published during a period.")
    @RequestMapping(
        value = "/metadatainternal",
        method = RequestMethod.GET,
        produces = {
            "text/x-csv; charset=UTF-8"
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "",
            description = "Metadata not published during a period.")
    })
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportInternalMetadata(
        @Parameter(
            description = "From date of metadata change date",
            required = true)
        @RequestParam final String dateFrom,
        @Parameter(
            description = "To date of metadata change date",
            required = true)
        @RequestParam final String dateTo,
        @Parameter(
            description = "Metadata group(s)")
        @RequestParam(required = false) final List<Integer> groups,
        final HttpServletResponse response,
        final HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        ReportFilter filter = new ReportFilter(dateFrom, dateTo,
            ReportUtils.groupsForFilter(context, groups));

        ReportInternalMetadata report = new ReportInternalMetadata(filter);
        response.setHeader("Content-Disposition",
            "attachment; filename=\"internalmetadata.csv\"");
        report.create(context, response.getWriter());
    }


    /**
     * Get the updated metadata during a period.
     *
     * @param dateFrom From date of metadata change date.
     * @param dateTo   To date of metadata change date.
     * @param groups   Metadata group(s).
     * @param response HttpServletResponse.
     * @param request  HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "Get the updated metadata during a period.")
    @RequestMapping(
        value = "/metadataupdated",
        method = RequestMethod.GET,
        produces = {
            "text/x-csv; charset=UTF-8"
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "",
            description = "Updated metadata during a period.")
    })
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportUpdatedMetadata(
        @Parameter(
            description = "From date of metadata change date",
            required = true)
        @RequestParam final String dateFrom,
        @Parameter(
            description = "To date of metadata change date",
            required = true)
        @RequestParam final String dateTo,
        @Parameter(
            description = "Metadata group(s)")
        @RequestParam(required = false) final List<Integer> groups,
        final HttpServletResponse response,
        final HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        ReportFilter filter = new ReportFilter(dateFrom, dateTo,
            ReportUtils.groupsForFilter(context, groups));

        ReportUpdatedMetadata report = new ReportUpdatedMetadata(filter);
        response.setHeader("Content-Disposition",
            "attachment; filename=\"updatedmetadata.csv\"");
        report.create(context, response.getWriter());
    }
}
