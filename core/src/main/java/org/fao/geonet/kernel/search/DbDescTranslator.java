//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Localized;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Translates keys using a Repository class and property from the retrieved entity.
 *
 * @author jesse, francois
 */
public class DbDescTranslator extends Translator {

    private static final long serialVersionUID = 1L;
    private final ApplicationContext _applicationContext;
    private final String _langCode;
    private final Class<? extends JpaRepository> _repositoryClass;
    private final String _propertyName;


    public DbDescTranslator(ApplicationContext applicationContext, String langCode, String param)
            throws IOException, JDOMException, ClassNotFoundException {
        String[] parts = param.split(":", 2);
        this._repositoryClass = (Class<? extends JpaRepository>) Class.forName(parts[0]);
        this._propertyName = parts[1];
        _applicationContext = applicationContext;
        _langCode = langCode;
    }

    public String translate(String key) {
        try {
            JpaRepository repository = _applicationContext.getBean(_repositoryClass);
            Localized entity = null;
            Method[] methods = repository.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals("findOne") && method.getParameterTypes().length == 1) {
                    Object convertedKey = convertKeyToType(key, method.getParameterTypes()[0]);
                    entity = (Localized) method.invoke(repository, convertedKey);
                }
            }


            if (entity == null) {
                return key;
            }


            String label = entity.getLabel(_langCode);

            if (label == null) {
                return key;
            } else {
                return label;
            }

        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Error translating a string", e);
            return key;
        }
    }

    private Object convertKeyToType(String key, Class<?> aClass) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        if (aClass == String.class) {
            return key;
        } else if (aClass.getMethod("valueOf", String.class) != null) {
            Method converterMethod = aClass.getMethod("valueOf", String.class);
            return converterMethod.invoke(null, key);
        }

        return key;
    }

}
