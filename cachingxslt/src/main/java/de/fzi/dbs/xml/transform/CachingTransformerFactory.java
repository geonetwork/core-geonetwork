package de.fzi.dbs.xml.transform;

import net.sf.saxon.TransformerFactoryImpl;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;


/**
 * Caching implementation of JAXP transformer factory.
 * This implementation caches templates that were loaded from local files
 * so that consequent calls to local stylesheets require stylesheet reparsing
 * only if stylesheet was changed.
 */
public class CachingTransformerFactory extends TransformerFactoryImpl {
    /**
     * Map to hold templates cache.
     */
    private static Map<String, TemplatesCacheEntry> templatesCache = new HashMap<String, TemplatesCacheEntry>();

    /**
     * Factory logger.
     */
    protected static final Logger logger =
            Logger.getLogger(CachingTransformerFactory.class);

    /**
     * Active readers count.
     */
    static int activeReaders = 0;
    /**
     * Active writers count.
     */
    static int activeWriters = 0;

    /**
     * Clear the stylesheet cache. This is not part of the
     * JAXP TransformerFactoryImpl so users should test for existence of this
     * method before calling otherwise JAXP compatibility will be broken.
     */
    public void clearCache() {
        beforeWrite();
        templatesCache.clear();
        afterWrite();
    }

    /**
     * Process the source into a Transformer object. If source is a StreamSource
     * with <code>systemID</code> pointing to a file, transformer is produced
     * from a cached templates object. Cache is done in soft references; cached
     * objects are reloaded, when file's date of last modification changes.
     *
     * @param source An object that holds a URI, input stream, etc.
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     * @throws TransformerConfigurationException - May throw this during the
     *                                           parse when it is constructing the Templates object and fails.
     */
    public Transformer newTransformer(final Source source)
            throws TransformerConfigurationException {
        // Check that source in a StreamSource
        if (source instanceof StreamSource) {
            try {
                // Create URI of the source
                String srcId = source.getSystemId();
                final URI uri = new URI(srcId);
                // If URI points to a file, load transformer from the file
                // (or from the cache)
                if ("file".equalsIgnoreCase(uri.getScheme()))
                    return newTransformer(new File(uri.getPath()));
            } catch (URISyntaxException urise) {
                throw new TransformerConfigurationException(urise);
            }
        }
        return super.newTransformer(source);
    }

    /**
     * Creates a transformer from a file (and caches templates) or from
     * cached templates object.
     *
     * @param file file to load transformer from.
     * @return Transformer, built from given file.
     * @throws TransformerConfigurationException if there was a problem loading
     *                                           transformer from the file.
     */
    protected Transformer newTransformer(final File file)
            throws TransformerConfigurationException {
        // Search the cache for the templates entry
        TemplatesCacheEntry templatesCacheEntry = read(file.getAbsolutePath());

        // If entry found
        if (templatesCacheEntry != null) {
            // Check timestamp of modification
            if (templatesCacheEntry.lastModified
                < templatesCacheEntry.templatesFile.lastModified())
                templatesCacheEntry = null;
        }
        // If no templatesEntry is found or this entry was obsolete
        if (templatesCacheEntry == null) {
            //System.out.println("Loading transformation [" + file.getAbsolutePath() + "].");
            // If this file does not exists, throw the exception
            if (!file.exists()) {
                throw new TransformerConfigurationException(
                        "Requested transformation ["
                        + file.getAbsolutePath()
                        + "] does not exist.");
            }

            // Create new cache entry
            templatesCacheEntry =
                    new TemplatesCacheEntry(newTemplates(new StreamSource(file)), file);

            // Save this entry to the cache
            write(file.getAbsolutePath(), templatesCacheEntry);
        } else {
            //System.out.println("Using cached transformation [" + file.getAbsolutePath() + "].");
        }
        return templatesCacheEntry.templates.newTransformer();
    }

    /**
     * Returns a templates cache entry for the specified absolute path.
     *
     * @param absolutePath absolute path of the entry.
     * @return Templates cache entry for the specified path.
     */
    protected TemplatesCacheEntry read(String absolutePath) {
        beforeRead();
        final TemplatesCacheEntry templatesCacheEntry = templatesCache.get(absolutePath);
        afterRead();
        return templatesCacheEntry;
    }

    /**
     * Saves templates cache entry for the specified absolute path.
     *
     * @param absolutePath        absolute path of the entry.
     * @param templatesCacheEntry templates cache entry to save.
     */
    protected void write(String absolutePath, TemplatesCacheEntry templatesCacheEntry) {
        beforeWrite();
        templatesCache.put(absolutePath, templatesCacheEntry);
        afterWrite();
    }

    /**
     * Invoked just before reading, waits until reading is allowed.
     */
    protected synchronized void beforeRead() {
        while (activeWriters > 0)
            try {
                wait();
            } catch (InterruptedException iex) {
            }
        ++activeReaders;
    }

    /**
     * Invoked just after reading.
     */
    protected synchronized void afterRead() {
        --activeReaders;
        notifyAll();
    }

    /**
     * Invoked just before writing, waits until writing is allowed.
     */
    protected synchronized void beforeWrite() {
        while (activeReaders > 0 || activeWriters > 0)
            try {
                wait();
            } catch (InterruptedException iex) {
            }
        ++activeWriters;
    }

    /**
     * Invoked just after writing.
     */
    protected synchronized void afterWrite() {
        --activeWriters;
        notifyAll();
    }

    /**
     * Private class to hold templates cache entry.
     */
    private static final class TemplatesCacheEntry {
        /**
         * When was the cached entry last modified.
         */
        private long lastModified;

        /**
         * Cached templates object.
         */
        private Templates templates;

        /**
         * Templates file object.
         */
        private File templatesFile;

        /**
         * Constructs a new cache entry.
         *
         * @param templates     templates to cache.
         * @param templatesFile file, from which this transformer was loaded.
         */
        private TemplatesCacheEntry(final Templates templates, final File templatesFile) {
            this.templates = templates;
            this.templatesFile = templatesFile;
            this.lastModified = templatesFile.lastModified();
        }
    }
}

/*
 * The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is
 * Aleksei Valikov of Forschungszentrum Informatik (valikov@fzi.de).
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): none.
 */
