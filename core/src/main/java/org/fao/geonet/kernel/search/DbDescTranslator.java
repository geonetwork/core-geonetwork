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

import com.google.common.base.Optional;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Localized;
import org.fao.geonet.utils.Log;
import org.jdom.JDOMException;
import org.springframework.context.ConfigurableApplicationContext;
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

    private final transient ConfigurableApplicationContext _applicationContext;
    private final String _langCode;
    private Class<? extends JpaRepository> _repositoryClass;
    private final String _methodName;
    private final String _parameterType;
    private String _beanName;

    public DbDescTranslator(ConfigurableApplicationContext applicationContext, String langCode, String param)
            throws IOException, JDOMException, ClassNotFoundException {
        String[] parts = param.split(":", 3);
        try {
            this._repositoryClass = (Class<? extends JpaRepository>) Class.forName(parts[0]);
        } catch (Exception e) {
            this._beanName = parts[0];
        }

        this._methodName = parts.length == 2 ? parts[1] : "findOne";
        this._parameterType = parts.length == 3 ? parts[2] : "String";

        _applicationContext = applicationContext;
        _langCode = langCode;
    }

    public String translate(final String key) {
        try {
            final TranslatorCache cache = this._applicationContext.getBean(TranslatorCache.class);
            Optional<Localized> entityOptional = cache.get(key);
            Localized entity;
            if (entityOptional != null) {
                if (entityOptional.isPresent()) {
                    entity = entityOptional.get();
                } else {
                    entity = null;
                }
            } else {
                JpaRepository repository;
                if (_repositoryClass != null) {
                    repository = _applicationContext.getBean(_repositoryClass);
                } else {
                    repository = _applicationContext.getBean(_beanName, JpaRepository.class);
                }
                final Class<?> repositoryClass = repository.getClass();

                entity = findEntity(key, repository, repositoryClass);

                cache.put(this._applicationContext, key, entity);
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

    private Localized findEntity(final String key, final JpaRepository repository, final Class<?> repositoryClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method[] methods = repositoryClass.getMethods();

        Localized entity = null;
        for (Method method : methods) {
            if (method.getName().equals(this._methodName) && method.getParameterTypes().length == 1) {
                try {
                    if (_parameterType.equals("int")) {
                        entity = (Localized) method.invoke(repository, Integer.valueOf(key));
                    } else if (_parameterType.equals("long")) {
                        entity = (Localized) method.invoke(repository, Long.valueOf(key));
                    } else if (_parameterType.equals("double")) {
                        entity = (Localized) method.invoke(repository, Double.valueOf(key));
                    } else if (_parameterType.equals("float")) {
                        entity = (Localized) method.invoke(repository, Float.valueOf(key));
                    } else if (_parameterType.equals("boolean")) {
                        entity = (Localized) method.invoke(repository, Boolean.valueOf(key));
                    } else if (_parameterType.equals("short")) {
                        entity = (Localized) method.invoke(repository, Short.valueOf(key));
                    } else if (_parameterType.equals("char")) {
                        entity = (Localized) method.invoke(repository, key.charAt(0));
                    } else {
                        entity = (Localized) method.invoke(repository, key);
                    }
                } catch (java.lang.IllegalArgumentException e) {
                    // Call to the method with wrong argument type.
                }
                if (entity == null) {
                    try {
                        entity = (Localized) method.invoke(repository, key);
                   } catch (java.lang.IllegalArgumentException e) {
                        // Call to the method with wrong argument type.
                    }
                }
                if (entity != null) {
                    break;
                }
            }
        }

        if (entity != null) {
            return entity;
        } else {
            if (repositoryClass.getSuperclass() != null) {
                entity = findEntity(key, repository, repositoryClass.getSuperclass());
            }
            if (entity == null) {
                final Class<?>[] interfaces = repositoryClass.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    entity = findEntity(key, repository, anInterface);
                    if (entity != null) {
                        return entity;
                    }
                }
            }
            return null;
        }
    }
}