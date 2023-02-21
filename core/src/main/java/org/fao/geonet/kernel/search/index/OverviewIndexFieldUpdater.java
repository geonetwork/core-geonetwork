package org.fao.geonet.kernel.search.index;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.util.XslUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An operation which takes some time to add information
 * into the index. This action can be run in the background (TODO)
 */
public class OverviewIndexFieldUpdater {
    private Integer imageSize = 140;

    @Autowired
    EsSearchManager searchManager;

    public void process(String uuid) {
        Set<String> source = new HashSet<>();
        source.add("overview");
        SearchResponse response = null;
        try {
            response = searchManager.query(String.format(
                "+uuid:\"%s\" _exists_:overview.url -_exists_:overview.data",
                uuid), null, source, 0, 1);
            response.getHits().forEach(hit -> {
                AtomicBoolean updates = new AtomicBoolean(false);
                Map<String, Object> fields = hit.getSourceAsMap();
                getHitOverviews(fields)
                    .stream()
                    .forEach(overview -> {
                        String url = (String) ((Map) overview).get("url");
                        if (StringUtils.isNotEmpty(url)) {
                            String data = XslUtil.buildDataUrl(url, imageSize);
                            ((Map) overview).put("data", data);
                            updates.set(true);
                        }
                    });
                if (updates.get()) {
                    try {
                        searchManager.updateFields(uuid, fields, source);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<HashMap<String, String>> getHitOverviews(Map<String, Object> fields) {
        Object overviews = fields.get("overview");
        if (overviews != null) {
            return ((ArrayList) overviews);
        }
        return new ArrayList<>();
    }
}
