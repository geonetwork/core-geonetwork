//=============================================================================
//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.exceptions;

import java.util.Locale;

import org.fao.geonet.languages.LocaleMessages;

public abstract class LocalizedRuntimeException extends RuntimeException implements ILocalizedException {

    private Locale locale;

    private String messageKey;
    private Object[] messageKeyArgs;

    private String descriptionKey;
    private Object[] descriptionKeyArgs;

    public LocalizedRuntimeException() {
        super();
    }

    public LocalizedRuntimeException(String message) {
        super(message);
    }

    public LocalizedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalizedRuntimeException(Throwable cause) {
        super(cause);
    }

    public LocalizedRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    // Note that the following could not be a generic abstract class using generic type -i.e<T extends LocalizedException>
    // As generic class is not allowed to extend the Throwable class directly or indirectly.
    // https://docs.oracle.com/javase/specs/jls/se6/html/classes.html#303584
    // So each child class needs to have it's own version.
    public LocalizedRuntimeException withMessageKey(String messageKey) {
        return withMessageKey(messageKey, null);
    }

    public LocalizedRuntimeException withMessageKey(String messageKey, Object[] messageKeyArgs) {
        this.messageKey = messageKey;
        this.messageKeyArgs = messageKeyArgs;
        return this;
    }

    public LocalizedRuntimeException withDescriptionKey(String descriptionKey) {
        return withDescriptionKey(descriptionKey, null);
    }

    public LocalizedRuntimeException withDescriptionKey(String descriptionKey, Object[] descriptionKeyArgs) {
        this.descriptionKey = descriptionKey;
        this.descriptionKeyArgs = descriptionKeyArgs;
        return this;
    }

    public LocalizedRuntimeException withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Class that extends this class will need to override this function so that it returns the bean qualifier for where it wants to get the messages from.
     *
     * @return bean qualifier to use for the message translations.
     */
    protected abstract String getResourceBundleBeanQualifier();

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getLocalizedMessage() {
        return LocaleMessages.getMessageForLocale(messageKey, messageKeyArgs, locale, getResourceBundleBeanQualifier());
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public Object[] getMessageKeyArgs() {
        return messageKeyArgs;
    }

    @Override
    public void setMessageKeyArgs(Object[] messageKeyArgs) {
        this.messageKeyArgs = messageKeyArgs;
    }

    @Override
    public String getLocalizedDescription() {
        return LocaleMessages.getMessageForLocale(descriptionKey, descriptionKeyArgs, locale, getResourceBundleBeanQualifier());
    }

    @Override
    public String getDescriptionKey() {
        return descriptionKey;
    }

    @Override
    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    @Override
    public Object[] getDescriptionKeyArgs() {
        return descriptionKeyArgs;
    }

    @Override
    public void setDescriptionKeyArgs(Object[] descriptionKeyArgs) {
        this.descriptionKeyArgs = descriptionKeyArgs;
    }
}
