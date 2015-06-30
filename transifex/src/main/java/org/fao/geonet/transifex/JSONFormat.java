package org.fao.geonet.transifex;

import com.google.common.collect.Lists;
import net.sf.json.JSONObject;
import org.fao.geonet.Constants;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * A file in the json key-value properties format.
 *
 * @author Jesse on 6/18/2015.
 */
public class JSONFormat implements TranslationFormat {

    private TranslationFileConfig stdConfig;

    @Override
    public TranslationFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties) {
        this.stdConfig = stdConfig;
        return this;
    }

    @Override
    public List<TransifexReadyFile> toTransifex(String translationFile) throws IOException {
        Path path = stdConfig.layout.getFile(stdConfig.path, stdConfig.fileName, "en");
        String data = new String(Files.readAllBytes(path), Constants.CHARSET);
        TransifexReadyFile readyFile = new TransifexReadyFile(stdConfig.id, stdConfig.name,
                JSONObject.fromObject(data).toString(), stdConfig.categories);
        return Lists.newArrayList(readyFile);
    }

    @Override
    public String toGeonetwork(List<TransifexReadyFile> fromTransifex) {
        Assert.isTrue(fromTransifex.size() == 1);
        return fromTransifex.get(0).data;
    }

    @Override
    public Layout getDefaultLayout() {
        return Layout.PREFIX;
    }

}
