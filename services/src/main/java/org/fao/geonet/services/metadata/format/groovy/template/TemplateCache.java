package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.metadata.format.ConfigFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.PostConstruct;

import static org.fao.geonet.services.metadata.format.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;

/**
 * A Cache for the template files that are loaded by FileResult objects.  This is intended to reduce the number of files that
 * need to be loaded.
 *
 * @author Jesse on 10/20/2014.
 */
@Component
public class TemplateCache {
    private static final Weigher<Path, TNode> STRING_LENGTH_WEIGHER = new Weigher<Path, TNode>()  {
        @Override
        public int weigh(Path key, TNode value) {
            return key.toString().length() + (int) value.getUnparsedSize();
        }
    };

    @VisibleForTesting
    @Autowired
    SystemInfo systemInfo;
    @VisibleForTesting
    @Autowired
    SchemaManager schemaManager;
    @VisibleForTesting
    @Autowired
    TemplateParser xmlTemplateParser;



    private int maxSizeKB = 100000;
    Cache<Path, TNode> canonicalFileNameToText;
    private int concurrencyLevel = 4;

    @PostConstruct
    void init() {
        long maxSize = ((long) maxSizeKB * 1024) / 8;
        if (maxSize > Integer.MAX_VALUE) {
            final long maxAllowed = ((long) Integer.MAX_VALUE * 8) / 1024;
            throw new AssertionError("maxSizeKB is too large: " + maxSizeKB + " max allowed value is: " + maxAllowed);
        }
        if (maxSize < 1) {
            throw new AssertionError("maxSizeKB is too small: " + maxSizeKB);
        }
        canonicalFileNameToText = CacheBuilder.newBuilder().
                concurrencyLevel(this.concurrencyLevel).
                initialCapacity(100).
                maximumWeight((int) maxSize). // allow caching roughly 100MB maximum
                weigher(STRING_LENGTH_WEIGHER).
                build();
    }

    public void setMaxSizeKB(int maxSizeKB) {
        this.maxSizeKB = maxSizeKB;
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public synchronized FileResult createFileResult(Path formatterDir, Path schemaDir, Path rootFormatterDir, String path,
                                             Map<String, Object> substitutions) throws IOException {
        Path file = formatterDir.resolve(path);
        TNode template = this.canonicalFileNameToText.getIfPresent(toRealPath(file));
        Path fromParentSchema = null;

        if (!this.systemInfo.isDevMode()) {
            if (template != null) {
                return new FileResult(template, substitutions);
            }

            file = schemaDir.resolve(path);
            template = this.canonicalFileNameToText.getIfPresent(toRealPath(file));
            if (template != null) {
                return new FileResult(template, substitutions);
            }
            fromParentSchema = fromParentSchema(formatterDir, schemaDir, path);
            if (fromParentSchema != null) {
                template = this.canonicalFileNameToText.getIfPresent(toRealPath(fromParentSchema));
                if (template != null) {
                    return new FileResult(template, substitutions);
                }
            }

            file = rootFormatterDir.resolve(path);
            template = this.canonicalFileNameToText.getIfPresent(toRealPath(file));
            if (template != null) {
                return new FileResult(template, substitutions);
            }
        }

        file = formatterDir.resolve(path);
        if (!Files.exists(file) && schemaDir != null) {
            file = schemaDir.resolve(path);
        }

        if (!Files.exists(file)) {
            if (fromParentSchema == null) {
                fromParentSchema = fromParentSchema(formatterDir, schemaDir, path);
            }
            if (fromParentSchema != null) {
                file = fromParentSchema;
            }
        }
        if (!Files.exists(file)) {
            file = rootFormatterDir.resolve(path);
        }
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("There is no file: " + path + " in any of: \n" +
                                               "\t * " + formatterDir + "\n" +
                                               "\t * " + schemaDir + "\n" +
                                               "\t * if parent exists: " + fromParentSchema + "\n" +
                                               "\t * " + rootFormatterDir);
        }

        template = xmlTemplateParser.parse(file);
        this.canonicalFileNameToText.put(toRealPath(file), template);

        return new FileResult(template, substitutions);
    }

    private Path toRealPath(Path file) throws IOException {
        if (Files.exists(file)) {
            return file.toRealPath();
        } else {
            return file.toAbsolutePath().normalize();
        }
    }

    private Path fromParentSchema(Path formatterDir, Path schemaDir, String path) throws IOException {
        final ConfigFile configFile;
        if (formatterDir != null) {
            configFile = new ConfigFile(formatterDir, true, schemaDir);
        } else {
            configFile = new ConfigFile(schemaDir, false, null);
        }

        final String schemaName = configFile.dependOn();
        if (schemaName != null) {
            Path parentSchema = this.schemaManager.getSchemaDir(schemaName).resolve(SCHEMA_PLUGIN_FORMATTER_DIR);

            Path file = parentSchema.resolve(path);
            if (Files.exists(file)) {
                return file;
            }

            return fromParentSchema(null, parentSchema, path);
        }


        return null;
    }
}
