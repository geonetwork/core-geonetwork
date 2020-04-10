package org.fao.geonet.api.sld;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequestMapping(value = {
    "/{portal}/api/tools/ogc",
    "/{portal}/api/" + API.VERSION_0_1 + "/tools/ogc"
})
@Api(value = "tools",
    tags = "tools",
    description = "Utility operations")
public class SldApi {

    public static final String LOGGER = Geonet.GEONETWORK + ".api.sld";

    // SPECIFIC SEXTANT: actually create a logger
    private static final Logger SLF4J_LOGGER = LoggerFactory.getLogger(LOGGER);

    @ApiOperation(value = "Test form", hidden = true)
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


    @Autowired
    TextFileRepository fileRepository;

    @Autowired
    SettingManager settingManager;

    @ApiOperation(value = "Get the list of SLD available")
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

    @ApiOperation(value = "Remove all SLD files",
        notes = "Clean all SLD generated previously")
    @RequestMapping(value = "/sld",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public void deteleSLD() {
        fileRepository.deleteAll();
    }


    @ApiOperation(value = "Generate a SLD with a new filter",
        nickname = "buildSLD",
        notes = "Get the currend SLD for the requested layers, add new filters in, save the SLD and return the new SLD URL.")
    @RequestMapping(value = "/sld",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public
    @ResponseBody
    String buildSLD(
        @ApiParam(value = "The WMS server URL",
            required = true)
        @RequestParam("url") String serverURL,
        @ApiParam(value = "The layers",
            required = true)
        @RequestParam("layers") String layers,
        @ApiParam(value = "The filters in JSON",
            required = true)
        @RequestParam("filters") String filters,
        HttpServletRequest request) throws ServiceException, TransformerException, JSONException, ParseException, IOException, JDOMException, URISyntaxException {

        try {
            HashMap<String, String> hash = SLDUtil.parseSLD(new URI(serverURL), layers);

            Element root = Xml.loadString(hash.get("content"), false);

            if(root.getName().equals("ServiceExceptionReport")) {
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

            TransactionManager.runInTransaction("sldApi",  ApplicationContextHolder.get(),
                    TransactionManager.TransactionRequirement.CREATE_NEW,
                    TransactionManager.CommitBehavior.ALWAYS_COMMIT,
                    false, new TransactionTask<Void>() {
                        @Override
                        public Void doInTransaction(TransactionStatus transaction) throws Throwable {
                            fileRepository.saveAndFlush(sld);
                            return null;
                        }
                    }
            );

            String pathPrefix = request.getContextPath() + request.getServletPath();
            String url = settingManager.getNodeURL() + "api/tools/ogc/sld/" + sld.getId() + ".xml";

            return url;
        } catch (Exception e) {
            throw e;
        }
    }

    @ApiOperation(value = "Download a SLD",
        nickname = "downloadSLD",
        notes = "")
    @RequestMapping(value = "/sld/{id:\\d+}.{extension}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void downloadSLD(
        @ApiParam(value = "The SLD identifier",
            required = true)
        @PathVariable("id") int id,
        HttpServletResponse response) throws ResourceNotFoundException {
        try {
            TextFile file = fileRepository.findOne(id);
            response.setContentType(file.getMimeType() + "; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(file.getContent());
            writer.flush();
        } catch (Exception e) {
            SLF4J_LOGGER.error(String.format("Error fetching SLD '%s'",id), e);
            throw new ResourceNotFoundException(String.format(
                "SLD '%s' not found. ",
                id));
        }
    }
}
