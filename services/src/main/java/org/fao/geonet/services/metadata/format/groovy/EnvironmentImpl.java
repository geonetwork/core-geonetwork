package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Map;

/**
 * The actual Environment implementation.
 *
 * @author Jesse on 10/20/2014.
 */
public class EnvironmentImpl implements Environment {
    private final String lang3;
    private final String lang2;
    private final String resourceUrl;
    private final Multimap<String, ParamValue> params = ArrayListMultimap.create();
    private final FormatType formatType;
    private final Metadata metadataInfo;
    private final String locUrl;
    private final Element jdomMetadata;
    private final ApplicationContext applicationContext;

    public EnvironmentImpl(FormatterParams fparams, IsoLanguagesMapper mapper) {
        jdomMetadata = fparams.metadata;
        this.lang3 = fparams.context.getLanguage();
        this.lang2 = mapper.iso639_2_to_iso639_1(lang3, "en");

        this.formatType = fparams.formatType;
        this.resourceUrl = fparams.getResourceUrl();
        this.locUrl = fparams.getLocUrl();
        this.metadataInfo = fparams.metadataInfo;

        for (Map.Entry<String, String[]> entry : fparams.params.entrySet()) {
            for (String value : entry.getValue()) {
                this.params.put(entry.getKey(), new ParamValue(value));
            }
        }

        this.applicationContext = fparams.context.getApplicationContext();
    }

    /**
     * Return the map of all parameters passed to the Format service.
     */
    public Multimap<String, ParamValue> params() {
        return this.params;
    }

    /**
     * Return the value of the first parameter with the provided name.  Null is returned if there is no parameter with the given name.
     */
    public ParamValue param(String paramName) {
        final Collection<ParamValue> paramValues = this.params.get(paramName);
        if (paramValues.isEmpty()) {
            return new ParamValue("") {
                @Override
                public String toString() {
                    return "Null Value";
                }

                @Override
                public boolean toBool() {
                    return false;
                }

                @Override
                public int toInt() {
                    return -1;
                }

                @Override
                public Double toDouble() {
                    return -1.0;
                }
            };
        }
        return paramValues.iterator().next();
    }
    /**
     * Return ALL values of parameter with the provided name.
     */
    public Collection<ParamValue> paramValues(String paramName) {
        return this.params.get(paramName);
    }

    public String getLang3() {
        return this.lang3;
    }

    public String getLang2() {
        return this.lang2;
    }

    @Override
    public int getMetadataId() {
        return this.metadataInfo.getId();
    }

    @Override
    public String getMetadataUUID() {
        return this.metadataInfo.getUuid();

    }

    @Override
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    @Override
    public String getLocalizedUrl() {
        return this.locUrl;
    }

    @Override
    public Authentication getAuth() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            return context.getAuthentication();
        }
        return null;
    }

    @Override
    public FormatType getFormatType() {
        return this.formatType;
    }

    @Override
    public Element getMetadataElement() {
        return this.jdomMetadata;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return this.applicationContext.getBean(clazz);
    }
}
