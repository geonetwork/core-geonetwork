//===	Copyright (C) 2012 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.languages;

import java.util.Locale;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class LocaleMessages {
    /**
     * Get localized message based on message key
     *
     * @param messageKey message key to use when retrieving the value from the properties file.
     * @param locale locale to use when getting the message key
     * @param resourceBundleBeanQualifier resource bundle qualifier to use when getting ResourceBundleMessageSource bean
     * @return message
     */

    public static String getMessageForLocale(String messageKey, Locale locale, String resourceBundleBeanQualifier) {
        return getMessageForLocale(messageKey, null, locale, resourceBundleBeanQualifier);
    }

    /**
     * Get localized message based on message key
     *
     * @param messageKey message key to use when retrieving the value from the properties file.
     * @param args Argument that may be supplied to the messagekey string
     * @param locale locale to use when getting the message key. If null then it will default to locale context holder.
     * @param resourceBundleBeanQualifier resource bundle qualifier to use when getting ResourceBundleMessageSource bean
     * @return message
     */

    public static String getMessageForLocale(String messageKey, Object[] args, Locale locale, String resourceBundleBeanQualifier) {
        if (!StringUtils.isEmpty(messageKey)) {
            ResourceBundleMessageSource resourceBundleMessageSource = getResourceBundleMessageSource(resourceBundleBeanQualifier);
            if (resourceBundleMessageSource != null) {
                return resourceBundleMessageSource.getMessage(messageKey, args, locale == null ? LocaleContextHolder.getLocale() : locale);
            }
        }
        // If we could not find the ResourceBundleMessageSource or the messageKey was in an invalid format then lets return the original key as the message.
        return messageKey;
    }

    /**
     * Locate the ResourceBundleMessageSource bean.
     *
     * @param resourceBundleBeanQualifier to locate the bean. Without the qualifier, it will fails when there are multiple beans for ResourceBundleMessageSource
     *
     * @return bean for ResourceBundleMessageSource
     */

    private static ResourceBundleMessageSource getResourceBundleMessageSource(String resourceBundleBeanQualifier) {
        ResourceBundleMessageSource resourceBundleMessageSource = null;
        try {
            resourceBundleMessageSource = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                ApplicationContextHolder.get().getBeanFactory(),
                ResourceBundleMessageSource.class,
                resourceBundleBeanQualifier);
        } catch (Exception e) {
            //If there are any errors in getting the bean then lets just ensure resourceBundleMessageSource is null;
            resourceBundleMessageSource = null;
        }
        return resourceBundleMessageSource;
    }
}
