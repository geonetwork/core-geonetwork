package org.fao.geonet.transifex;

import com.google.common.collect.Maps;
import org.apache.maven.plugins.annotations.Parameter;
import org.fao.geonet.Constants;
import org.fao.geonet.transifex.xml.LeafElementFormat;
import org.fao.geonet.transifex.xml.SchemaPluginCodelistFormat;
import org.fao.geonet.transifex.xml.SchemaPluginLabelsFormat;
import org.fao.geonet.transifex.xml.SimpleElementFormat;
import org.fao.geonet.transifex.xml.XmlFormat;
import org.springframework.util.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The translation file format.
 *
 * @author Jesse on 6/18/2015.
 */
public class TranslationFileConfig {
    private static final Map<String, Class<? extends TranslationFormat>> FORMATS = Maps.newHashMap();
    static {
        FORMATS.put(SchemaPluginLabelsFormat.class.getSimpleName(), SchemaPluginLabelsFormat.class);
        FORMATS.put(SchemaPluginCodelistFormat.class.getSimpleName(), SchemaPluginCodelistFormat.class);
        FORMATS.put(LeafElementFormat.class.getSimpleName(), LeafElementFormat.class);
        FORMATS.put(SimpleElementFormat.class.getSimpleName(), SimpleElementFormat.class);
        FORMATS.put(JsonFormat.class.getSimpleName(), JsonFormat.class);
        FORMATS.put(XmlFormat.class.getSimpleName(), XmlFormat.class);
    }
    /**
     * Id of the format.  Will be used in id uploaded to transifex.
     */
    @Parameter(property = "id", required = true)
    public String id;
    /**
     * A human readable name of resource will be used in name uploaded to Transifex.
     */
    @Parameter(property = "name", required = false)
    public String name;
    /**
     * The format strategy class. Options:
     * <ul>
     *     <li>JsonNFormat</li>
     *     <li>XmlFormat</li>
     *     <li>SchemaPluginLabelsFormat</li>
     *     <li>SchemaPluginCodelistFormat</li>
     *     <li>SimpleElementFormat</li>
     *     <li>LeafElementFormat</li>
     * </ul>
     */
    @Parameter(property = "format-class", defaultValue = "JSONFormat")
    public String formatClass;
    /**
     * The categories of the Translation file
     */
    @Parameter(property = "categories", required = false)
    public Set<String> categories;
    @Parameter(property = "parameters", required = false)
    public Map<String, String> parameters;
    /**
     * The root path of the translation files.  This can depend on layout.  For example if the
     * layout is DIR then it should be to the directory where the language directories are.  If the layout is
     * PREFIX then the path should be the directory containing all the language files.
     */
    @Parameter(property = "path", required = true)
    public String path;
    /**
     * The root path of the translation files.  This can depend on layout.  For example if the
     * layout is DIR then it should be to the directory where the language directories are.  If the layout is
     * PREFIX then the path should be the directory containing all the language files.
     */
    @Parameter(property = "file-name", required = true)
    public String fileName;
    /**
     * How the translation files are organized and named.
     */
    @Parameter(property = "layout", required = false)
    public Layout layout;

    private TranslationFormat format;

    public void init() throws Exception {
        Assert.notNull(id, "id property must not be null");
        id = id.trim();
        Assert.isTrue(id.length() > 0, "id property must be non-empty");
        Assert.notNull(fileName, "file-name property must not be null");
        fileName = fileName.trim();
        Assert.notNull(fileName.length() > 0, "file-name property must be non-empty");

        if (name == null || name.trim().isEmpty()) {
            name = id;
        }

        Class<? extends TranslationFormat> lookedUpClass = FORMATS.get(formatClass);
        Assert.notNull(lookedUpClass, formatClass + " is not one of: " + FORMATS.keySet());
        format = lookedUpClass.newInstance();

        format.configure(this, parameters);

        if (layout == null) {
            layout = format.getDefaultLayout();
        }
    }

    public List<TransifexReadyFile> getTransifexFiles() throws Exception {
        return format.toTransifex(path);
    }

    public void writeTranslations(String lang, List<TransifexReadyFile> fromTransifex) throws Exception {
        String geonetworkFormattedData = format.toGeonetwork(fromTransifex);
        Path file = layout.getFile(path, fileName, lang);
        Files.createDirectories(file.getParent());
        Files.write(file, geonetworkFormattedData.getBytes(Constants.CHARSET));
    }
}
