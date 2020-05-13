package org.fao.geonet.api.reports;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        "/{portal}/api/reports",
        "/{portal}/api/" + API.VERSION_0_1
                + "/reports"
})
@Api(value = ApiParams.API_CLASS_RECORD_TAG,
        tags = ApiParams.API_CLASS_RECORD_TAG,
        description = ApiParams.API_CLASS_RECORD_OPS)
public class ReportApi {

    /**
     * Get list of metadata file downloads.
     *
     * @param dateFrom From date of the metadata downloads.
     * @param dateTo To date of the metadata downloads.
     * @param groups Metadata group(s).
     * @param response HttpServletResponse.
     * @param request HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @ApiOperation(value = "Get list of metadata file downloads",
            nickname = "getReportDataDownloads")
    @RequestMapping(
            value = "/datadownloads",
            method = RequestMethod.GET,
            produces = {
                    "text/x-csv; charset=UTF-8"
            })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK,
                    message = "List of metadata file downloads.")
    })
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportDataDownloads(
        @ApiParam(
                value = "From date of the metadata downloads",
                required = true)
        @RequestParam
        final String dateFrom,
        @ApiParam(
                value = "To date of the metadata downloads",
                required = true)
        @RequestParam
        final String dateTo,
        @ApiParam(
                value = "Metadata group(s)")
        @RequestParam(required = false)
        final List<Integer> groups,
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
     * @param dateTo To date of the metadata uploads.
     * @param groups Metadata group(s).
     * @param response HttpServletResponse.
     * @param request HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @ApiOperation(
            value = "Get uploaded files to metadata records during a period.",
            nickname = "getReportDataUploads")
    @RequestMapping(
            value = "/datauploads",
            method = RequestMethod.GET,
            produces = {
                    "text/x-csv; charset=UTF-8"
            })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
    })
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportDataUploads(
            @ApiParam(
                    value = "From date of the metadata uploads",
                    required = true)
            @RequestParam
            final String dateFrom,
            @ApiParam(
                    value = "To date of the metadata uploads",
                    required = true)
            @RequestParam
            final String dateTo,
            @ApiParam(
                    value = "Metadata group(s)")
            @RequestParam(required = false)
            final List<Integer> groups,
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
     * @param dateTo To date of users login date.
     * @param groups Group(s) for the users.
     * @param response HttpServletResponse.
     * @param request HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @ApiOperation(
            value = "Get the list of users \"active\" during a time period.",
            nickname = "getActiveUsers")
    @RequestMapping(
            value = "/users",
            method = RequestMethod.GET,
            produces = {
                    "text/x-csv; charset=UTF-8"
            })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK,
                    message = "List of users \"active\" during a time period.")
    })
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getActiveUsers(
            @ApiParam(
                    value = "From date of users login date",
                    required = true)
            @RequestParam
            final String dateFrom,
            @ApiParam(
                    value = "To date of users login date",
                    required = true)
            @RequestParam
            final String dateTo,
            @ApiParam(
                    value = "Group(s) for the users")
            @RequestParam(required = false)
            final List<Integer> groups,
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
     * @param dateTo To date of metadata change date.
     * @param groups Metadata group(s).
     * @param response HttpServletResponse.
     * @param request HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @ApiOperation(value = "Get the metadata not published during a period.",
            nickname = "getReportDataUploads")
    @RequestMapping(
            value = "/metadatainternal",
            method = RequestMethod.GET,
            produces = {
                    "text/x-csv; charset=UTF-8"
            })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK,
                    message = "Metadata not published during a period.")
    })
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportInternalMetadata(
            @ApiParam(
                    value = "From date of metadata change date",
                    required = true)
            @RequestParam
            final String dateFrom,
            @ApiParam(
                    value = "To date of metadata change date",
                    required = true)
            @RequestParam
            final String dateTo,
            @ApiParam(
                    value = "Metadata group(s)")
            @RequestParam(required = false)
            final List<Integer> groups,
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
     * @param dateTo To date of metadata change date.
     * @param groups Metadata group(s).
     * @param response HttpServletResponse.
     * @param request HttpServletRequest.
     * @throws Exception Exception creating the report.
     */
    @ApiOperation(value = "Get the updated metadata during a period.",
            nickname = "getReportDataUploads")
    @RequestMapping(
            value = "/metadataupdated",
            method = RequestMethod.GET,
            produces = {
                    "text/x-csv; charset=UTF-8"
            })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK,
                    message = "Updated metadata during a period.")
    })
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void getReportUpdatedMetadata(
            @ApiParam(
                    value = "From date of metadata change date",
                    required = true)
            @RequestParam
            final String dateFrom,
            @ApiParam(
                    value = "To date of metadata change date",
                    required = true)
            @RequestParam
            final String dateTo,
            @ApiParam(
                    value = "Metadata group(s)")
            @RequestParam(required = false)
            final List<Integer> groups,
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
