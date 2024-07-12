package org.fao.geonet.api.sld;

import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_TAG;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
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
import org.geotools.api.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequestMapping(value = {
    "/{portal}/api/tools/ogc"
})
@Tag(name = API_CLASS_TOOLS_TAG,
    description = API_CLASS_TOOLS_OPS)
public class SldApi {

    public static final String LOGGER = Geonet.GEONETWORK + ".api.sld";

    @Autowired
    TextFileRepository fileRepository;
    @Autowired
    SettingManager settingManager;

    @io.swagger.v3.oas.annotations.Operation(summary = "Test form", hidden = true)
    @GetMapping(value = "/sldform")
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
            "<textarea name=\"filters\">{" +
            "  \"baseStyle\" : \"Test:MuiltiRoad\"," +
            "  \"filters\": [" +
            "{\"field_name\": \"longueur\"," +
            "\"filter\": [" +
            "{\"filter_type\": \"PropertyIsBetween\"," +
            "\"params\": [0, 500]" +
            "}," +
            "{\"filter_type\": \"PropertyIsBetween\"," +
            "  \"params\": [500, 5000]" +
            "}]}," +
            "{" +
            "  \"field_name\": \"departement\"," +
            "  \"filter\": [" +
            "{" +
            "  \"filter_type\": \"PropertyIsEqualTo\"," +
            "  \"params\": [\"Ain\"]" +
            "}]}," +
            "{" +
            "  \"field_name\": \"date_renovation\"," +
            "  \"filter\": [{" +
            "  \"filter_type\": \"PropertyIsBetween\"," +
            "  \"params\": [1379874654, 138564965]}," +
            "{" +
            "  \"filter_type\": \"PropertyIsBetween\"," +
            "  \"params\": [1379874654, 138564965]" +
            "}]}]}</textarea><br>" +
            "<input type=\"submit\" value=\"Go!\"/>" +
            "</form>" +
            "</body></html>";
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get the list of SLD available")
    @GetMapping(value = "/sld",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<String> getSLD() {
        List<TextFile> files = fileRepository.findAll();
        List<String> response = new ArrayList<>(files.size());
        for (TextFile file : files) {
            response.add(
                settingManager.getNodeURL() + "api/tools/ogc/sld/" + file.getId() + ".xml");
        }
        return response;

    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Remove all SLD files",
        description = "Clean all SLD generated previously")
    @DeleteMapping(value = "/sld",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public void deteleSLD() {
        fileRepository.deleteAll();
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Generate a SLD with a new filter",
        description = "Get the current SLD for the requested layers, add new filters in, save the SLD and return the new SLD URL.")
    @PostMapping(value = "/sld",
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
        @RequestParam("filters") String filters) throws ServiceException, JSONException, ParseException, IOException, JDOMException, URISyntaxException {

        Map<String, String> hash = SLDUtil.parseSLD(new URI(serverURL), layers);

        Element root = Xml.loadString(hash.get("content"), false);

        if (root.getName().equals("ServiceExceptionReport")) {
            throw new ServiceException("The WMS GetStyle request failed.");
        }
        Filter customFilter = SLDUtil.generateCustomFilter(new JSONObject(filters));
        SLDUtil.insertFilter(root, customFilter);

        String charset = hash.get("charset");
        Format format = Format.getPrettyFormat();
        if (StringUtils.isNoneEmpty(charset)) {
            format.setEncoding(charset);
        }
        XMLOutputter outputter = new XMLOutputter(format);
        Document doc = new Document(root);
        String sldDoc = outputter.outputString(doc);

        TextFile sld = new TextFile();
        sld.setContent(sldDoc);
        sld.setMimeType("application/xml");

        TransactionManager.runInTransaction("sldApi", ApplicationContextHolder.get(),
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

        return settingManager.getNodeURL() + "api/tools/ogc/sld/" + sld.getId() + ".xml";
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Generate an OGC filter",
        description = "From a JSON filter, return an OGC filter expression.")
    @PostMapping(value = "/filter",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public
    @ResponseBody
    String buildFilter(
        @Parameter(description = "The filters in JSON",
            required = true)
        @RequestParam("filters") String filters) throws JSONException, IOException {

        Filter customFilter = SLDUtil.generateCustomFilter(new JSONObject(filters));
        return SLDUtil.encodeFilter(customFilter);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Download a SLD",
        description = "")
    @GetMapping(value = "/sld/{id:\\d+}.{extension}",
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void downloadSLD(
        @Parameter(description = "The SLD identifier",
            required = true)
        @PathVariable("id") int id,
        @PathVariable("extension") String extension,
        HttpServletResponse response) throws ResourceNotFoundException {
        try {
            Optional<TextFile> file = fileRepository.findById(id);
            // Validate that the file id found matches the extension
            if (file.isPresent() && !StringUtils.isEmpty(extension)) {
                Path path = new File(id + "." + extension).toPath();
                String extensionMimeType = Files.probeContentType(path);
                if (!file.get().getMimeType().equals(extensionMimeType)) {
                    throw new ResourceNotFoundException(String.format(
                        "SLD '%s' with extension '%s' not found. ",
                        id, extension));
                }

                response.setContentType(file.get().getMimeType() + "; charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.write(file.get().getContent());
                writer.flush();
            } else {
                throw new ResourceNotFoundException(String.format(
                    "SLD '%s' not found. ",
                    id));
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException(String.format(
                "SLD '%s' not found. ",
                id));
        }
    }
}
