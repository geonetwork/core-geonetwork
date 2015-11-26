package org.fao.geonet.services.sld;

import org.fao.geonet.domain.TextFile;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.repository.TextFileRepository;
import org.geonetwork.map.wms.SLDUtil;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;


@Controller("sld.generate")
public class Generate {

    @Autowired
    private TextFileRepository fileRepository;


    @RequestMapping(value = "/{lang}/generateSLD", method= RequestMethod.GET)
    @ResponseBody
    public String handleFileUpload(HttpServletResponse response){

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


    @RequestMapping(value = "/{lang}/generateSLD", method= RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@PathVariable("lang") String lang,
                                                 @RequestParam("filters") String filters,
                                                 @RequestParam("serverURL") String serverURL,
                                                 @RequestParam("layers") String layers,
                                                 HttpServletRequest request){

        try {
            Style[] originalStyles = SLDUtil.parseSLD(new URL(serverURL), layers);

            Filter customFilter  = SLDUtil.generateCustomFilter(new JSONObject(filters));
            Style[] newFilters = SLDUtil.addAndFilter(originalStyles, customFilter);
            StyledLayerDescriptor newSLD = SLDUtil.buildSLD(newFilters, layers);

            SLDTransformer styleTransform = new SLDTransformer();
            String xml = styleTransform.transform(newSLD);


            TextFile sld = new TextFile();
            sld.setContent(xml);
            sld.setMimeType("application/xml");
            fileRepository.save(sld);

            String pathPrefix = request.getContextPath() + request.getServletPath();
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + pathPrefix + "/" + lang + "/" + "getSLD/" + sld.getId() + ".xml";

            return url;
        } catch (Exception e) {
           return e.getMessage();
        }
    }

    @RequestMapping("/{lang}/getSLD/{id:\\d+}.{extension}")
    public void handleFileDownload(@PathVariable("id") int id, HttpServletResponse response) throws IOException {

        TextFile file = fileRepository.findOne(id);
        response.setContentType(file.getMimeType()+ "; charset=utf-8");

        PrintWriter writer = response.getWriter();
        writer.write(file.getContent());
        writer.flush();

    }

}
