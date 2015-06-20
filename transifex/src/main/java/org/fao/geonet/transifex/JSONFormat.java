package org.fao.geonet.transifex;

import com.google.common.collect.Lists;
import org.fao.geonet.Constants;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A file in the json key-value properties format.
 *
 * @author Jesse on 6/18/2015.
 */
public class JsonFormat implements TranslationFormat {
    private Set<String> categories;

    @Override
    public TranslationFormat configure(TranslationFileConfig stdConfig, Map<String, String> properties) {
        this.categories = stdConfig.categories;
        return this;
    }

    @Override
    public List<TransifexReadyFile> toTransifex(String translationFile) throws IOException {
        Path path = Paths.get(translationFile);
        String data = new String(Files.readAllBytes(path), Constants.CHARSET);
        TransifexReadyFile readyFile = new TransifexReadyFile(path.getFileName().toString(), path.getFileName().toString(),
                data, categories);
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
