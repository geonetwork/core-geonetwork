package org.fao.geonet.api.sld;

import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_TOOLS_TAG;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.constants.Geonet;
import org.geonetwork.map.wms.SLDUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.geotools.api.filter.Filter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Service
@RequestMapping(value = {
    "/{portal}/api/tools/ogc"
})
@Tag(name = API_CLASS_TOOLS_TAG,
    description = API_CLASS_TOOLS_OPS)
public class SldApi {

    public static final String LOGGER = Geonet.GEONETWORK + ".api.sld";

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
}
