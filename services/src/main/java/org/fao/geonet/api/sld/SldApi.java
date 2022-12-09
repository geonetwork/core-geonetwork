package org.fao.geonet.api.sld;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.TextFile;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.TextFileRepository;
import org.fao.geonet.utils.Xml;
import org.geonetwork.map.wms.SLDUtil;
import org.geotools.ows.ServiceException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@Service
@RequestMapping(value = {
    "/{portal}/api/tools/ogc"
})
@Tag(name = "tools",
    description = "Utility operations")
public class SldApi {

    public static final String LOGGER = Geonet.GEONETWORK + ".api.sld";
    @Autowired
    TextFileRepository fileRepository;
    @Autowired
    SettingManager settingManager;

    @io.swagger.v3.oas.annotations.Operation(summary = "Test form", hidden = true)
    @RequestMapping(value = "/sldform",
        method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public String getSLDTestingForm() {

        return "<html><body>" +
            "<form method=\"POST\">" +
            "Server URL : " +
            "<input type=\"text\" name=\"serverURL\" value=\"http://www.ifremer.fr/services/wms/biologie\"/><br>" +
            "Layers :" +
            "<input type=\"text\" name=\"layers\"/ value=\"IFR_RBT_PEUPL_GGASC_P\"><br>" +
            "JSON Custom filters : " +
            "<textarea name=\"filters\">{\n" +
            "  \"baseStyle\" : \"Test:MuiltiRoad\",\n" +
            "  \"filters\": [\n" +
            "\t{\n" +
            "\t  \"field_name\": \"longueur\",\n" +
            "\t  \"filter\": [\n" +
            "\t\t{\n" +
            "\t\t  \"filter_type\": \"PropertyIsBetween\",\n" +
            "\t\t  \"params\": [\n" +
            "\t\t\t0,\n" +
            "\t\t\t500\n" +
            "\t\t  ]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t  \"filter_type\": \"PropertyIsBetween\",\n" +
            "\t\t  \"params\": [\n" +
            "\t\t\t500,\n" +
            "\t\t\t5000\n" +
            "\t\t  ]\n" +
            "\t\t}\n" +
            "\t  ]\n" +
            "\t},\n" +
            "\t{\n" +
            "\t  \"field_name\": \"departement\",\n" +
            "\t  \"filter\": [\n" +
            "\t\t{\n" +
            "\t\t  \"filter_type\": \"PropertyIsEqualTo\",\n" +
            "\t\t  \"params\": [\n" +
            "\t\t\t\"Ain\"\n" +
            "\t\t  ]\n" +
            "\t\t}\n" +
            "\t  ]\n" +
            "\t},\n" +
            "\t{\n" +
            "\t  \"field_name\": \"date_renovation\",\n" +
            "\t  \"filter\": [\n" +
            "\t\t{\n" +
            "\t\t  \"filter_type\": \"PropertyIsBetween\",\n" +
            "\t\t  \"params\": [\n" +
            "\t\t\t1379874654,\n" +
            "\t\t\t138564965\n" +
            "\t\t  ]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t  \"filter_type\": \"PropertyIsBetween\",\n" +
            "\t\t  \"params\": [\n" +
            "\t\t\t1379874654,\n" +
            "\t\t\t138564965\n" +
            "\t\t  ]\n" +
            "\t\t}\n" +
            "\t  ]\n" +
            "\t}\n" +
            "  ]\n" +
            "}</textarea><br>" +
            "<input type=\"submit\" value=\"Go!\"/>" +
            "</form>" +
            "</body></html>";
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get the list of SLD available")
    @RequestMapping(value = "/sld",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<String> getSLD(HttpServletRequest request) {
        List<TextFile> files = fileRepository.findAll();
        List<String> response = new ArrayList<>(files.size());
        String pathPrefix = request.getContextPath() + request.getServletPath();
        for (TextFile file : files) {
            response.add(
                settingManager.getNodeURL() + "api/tools/ogc/sld/" + file.getId() + ".xml");
        }
        return response;

    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Remove all SLD files",
        description = "Clean all SLD generated previously")
    @RequestMapping(value = "/sld",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public void deteleSLD() {
        fileRepository.deleteAll();
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Generate a SLD with a new filter",
        description = "Get the currend SLD for the requested layers, add new filters in, save the SLD and return the new SLD URL.")
    @RequestMapping(value = "/sld",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public
    @ResponseBody
    String buildSLD(
        @Parameter(description = "The WMS server URL",
            required = true)
        @RequestParam("url") String serverURL,
        @Parameter(description = "The layers",
            required = true)
        @RequestParam("layers") String layers,
        @Parameter(description = "The filters in JSON",
            required = true)
        @RequestParam("filters") String filters,
        HttpServletRequest request) throws ServiceException, TransformerException, JSONException, ParseException, IOException, JDOMException, URISyntaxException {

        try {
            Map<String, String> hash = SLDUtil.parseSLD(new URI(serverURL), layers);

            Element root = Xml.loadString(hash.get("content"), false);

            if (root.getName().equals("ServiceExceptionReport")) {
                throw new ServiceException("The WMS GetStyle request failed.");
            }
            Filter customFilter = SLDUtil.generateCustomFilter(new JSONObject(filters));
            SLDUtil.insertFilter(root, customFilter);

            String charset = hash.get("charset");
            Format format = Format.getPrettyFormat();
            if (charset != null && charset != "") {
                format.setEncoding(charset);
            }
            XMLOutputter outputter = new XMLOutputter(format);
            Document doc = new Document(root);
            String sldDoc = outputter.outputString(doc);

            TextFile sld = new TextFile();
            sld.setContent(sldDoc);
            sld.setMimeType("application/xml");
            fileRepository.save(sld);

            String pathPrefix = request.getContextPath() + request.getServletPath();
            String url = settingManager.getNodeURL() + "api/tools/ogc/sld/" + sld.getId() + ".xml";

            return url;
        } catch (Exception e) {
            throw e;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Download a SLD",
        description = "")
    @RequestMapping(value = "/sld/{id:\\d+}.{extension}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void downloadSLD(
        @Parameter(description = "The SLD identifier",
            required = true)
        @PathVariable("id") int id,
        @PathVariable("extension") String extension,
        HttpServletResponse response) throws ResourceNotFoundException {
        try {
            TextFile file = fileRepository.findById(id).get();
            // Validate that the file id found matches the extension
            if (!StringUtils.isEmpty(extension)) {
                Path path = new File(id + "." + extension).toPath();
                String extensionMimeType = Files.probeContentType(path);
                if (!file.getMimeType().equals(extensionMimeType)) {
                    throw new ResourceNotFoundException(String.format(
                        "SLD '%s' with extension '%s' not found. ",
                        id, extension));
                }
            }
            response.setContentType(file.getMimeType() + "; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(file.getContent());
            writer.flush();
        } catch (Exception e) {
            throw new ResourceNotFoundException(String.format(
                "SLD '%s' not found. ",
                id));
        }
    }
}
