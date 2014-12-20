package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.metadata.format.ConfigFile;
import org.fao.geonet.utils.IO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.services.metadata.format.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;

/**
 * A Cache for the template files that are loaded by FileResult objects.  This is intended to reduce the number of files that
 * need to be loaded.
 *
 * @author Jesse on 10/20/2014.
 */
@Component
public class TemplateCache {


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @VisibleForTesting
    @Autowired
    SystemInfo systemInfo;
    @VisibleForTesting
    @Autowired
    SchemaManager schemaManager;
    @VisibleForTesting
    @Autowired
    TemplateParser xmlTemplateParser;

    @VisibleForTesting
    final Map<Path, TNode> canonicalFileNameToText = Maps.newHashMap();
    private final Set<Path> filesKnownToNotExist = Sets.newHashSet();


    public synchronized FileResult createFileResult(Path formatterDir, Path schemaDir, Path rootFormatterDir, String path,
                                                    Map<String, Object> model) throws IOException {
        final Path originalPath = IO.toPath(path);
        Path file = formatterDir.resolve(path);
        TNode template = fetchFromCache(originalPath, file);
        Path fromParentSchema;

        if (template != null) {
            return new FileResult(template, model);
        }

        if (schemaDir != null) {
            file = schemaDir.resolve(path);
        }

        template = fetchFromCache(originalPath, file);
        if (template != null) {
            return new FileResult(template, model);
        }
        fromParentSchema = fromParentSchema(formatterDir, schemaDir, path);
        if (fromParentSchema != null) {
            template = fetchFromCache(originalPath, fromParentSchema);
            if (template != null) {
                return new FileResult(template, model);
            }
        }

        file = rootFormatterDir.resolve(path);
        template = fetchFromCache(originalPath, file);
        if (template != null) {
            return new FileResult(template, model);
        }

        file = formatterDir.resolve(path);
        if (!exists(file) && schemaDir != null) {
            file = schemaDir.resolve(path);
        }

        if (!exists(file)) {
            if (fromParentSchema == null) {
                fromParentSchema = fromParentSchema(formatterDir, schemaDir, path);
            }
            if (fromParentSchema != null) {
                file = fromParentSchema;
            }
        }
        if (!exists(file)) {
            file = rootFormatterDir.resolve(path);
        }
        if (!exists(file)) {
            throw new IllegalArgumentException("There is no file: " + path + " in any of: \n" +
                                               "\t * " + formatterDir + "\n" +
                                               "\t * " + schemaDir + "\n" +
                                               "\t * if parent exists: " + fromParentSchema + "\n" +
                                               "\t * " + rootFormatterDir);
        }

        template = xmlTemplateParser.parse(file);
        cacheTemplate(originalPath, file, template);

        return new FileResult(template, model);
    }

    public boolean exists(Path file) throws IOException {
        if (!this.systemInfo.isDevMode() &&
            (this.filesKnownToNotExist.contains(file) || this.filesKnownToNotExist.contains(file.toAbsolutePath()))) {
            return false;
        }
        final boolean exists = Files.exists(file);
        if (!exists) {
            this.filesKnownToNotExist.add(file);
            this.filesKnownToNotExist.add(file.toAbsolutePath());
        }
        return exists;
    }

    public void cacheTemplate(Path originalPath, Path file, TNode template) throws IOException {
        doCache(originalPath, template);
        doCache(toRealPath(file), template);
        doCache(file, template);
        doCache(file.toAbsolutePath(), template);
        doCache(file.toAbsolutePath().normalize(), template);
    }

    public void doCache(Path originalPath, TNode template) {
        filesKnownToNotExist.remove(originalPath);
        this.canonicalFileNameToText.put(originalPath, template);
    }

    public TNode fetchFromCache(Path originalPath, Path file) throws IOException {
        if (this.systemInfo.isDevMode()) {
            return null;
        }
        TNode template = this.canonicalFileNameToText.get(originalPath);
        boolean recache = false;
        if (template == null) {
            template = this.canonicalFileNameToText.get(file);
            recache = true;
        }
        if (template == null) {
            template = this.canonicalFileNameToText.get(file.toAbsolutePath());
            recache = true;
        }
        if (template == null) {
            template = this.canonicalFileNameToText.get(file.toAbsolutePath().normalize());
            recache = true;
        }
        if (template == null) {
            template = this.canonicalFileNameToText.get(toRealPath(file));
            recache = true;
        }
        if (recache && template != null) {
            cacheTemplate(originalPath, file, template);
        }
        return template;
    }

    private Path toRealPath(Path file) throws IOException {
        if (exists(file)) {
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
            if (exists(file)) {
                return file;
            }

            return fromParentSchema(null, parentSchema, path);
        }


        return null;
    }
}
