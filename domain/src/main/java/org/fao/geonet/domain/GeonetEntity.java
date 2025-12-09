/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Embeddable;

import org.jdom.Element;
import org.springframework.beans.InvalidPropertyException;

/**
 * Contains common methods of all entities in Geonetwork.
 * <p/>
 * User: Jesse Date: 9/10/13 Time: 4:33 PM
 */
public class GeonetEntity {

    public static final String LABEL_EL_NAME = "label";
    public static final String RECORD_EL_NAME = "record";

    /**
     * Process the object by reflection to get all the attributes in xml format
     * 
     * @param obj
     * @param alreadyEncoded
     * @param exclude
     * @return
     */
    private static Element asXml(Object obj, IdentityHashMap<Object, Void> alreadyEncoded, Set<String> exclude) {
        alreadyEncoded.put(obj, null);
        Element record = new Element(RECORD_EL_NAME);

        Class<? extends Object> objclass = obj.getClass();
        while (objclass != null) {
            for (Method method : objclass.getDeclaredMethods()) {
                try {
                    if (shouldBeAdded(exclude, objclass, method)) {
                        // Then process all getters
                        if (isGetter(method)) {
                            final String descName = method.getName().substring(3);

                            if (isLabel(descName) && !objclass.equals(Localized.class)) {
                                addLabels(obj, record, method);
                            } else if (!isADuplicatedMethodWithAnotherReturnType(descName) && !isLabel(descName)) {
                                addPropertyToElement(obj, alreadyEncoded, exclude, record, method, descName);
                            }

                        } else if (isBooleanGetter(method)) {
                            final String descName = method.getName().substring(2);

                            if (!isADuplicatedMethodWithAnotherReturnType(descName)) {
                                addPropertyToElement(obj, alreadyEncoded, exclude, record, method, descName);
                            }
                        }
                    }
                } catch (InvalidPropertyException e) {
                    // just ignore it and get to the following property
                } catch (Exception e) {
                    // e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            // Iterate over the parent classes of the object
            objclass = getNextSignificantAncestor(objclass);
        }
        return record;
    }

    protected static void addLabels(Object obj, Element record, Method method) throws IllegalAccessException, InvocationTargetException {
        Element labelEl = new Element(LABEL_EL_NAME);

        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) method.invoke(obj);

        if (labels != null) {
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                labelEl.addContent(new Element(entry.getKey().toLowerCase()).setText(entry.getValue()));
            }
        }

        record.addContent(labelEl);
    }

    /**
     * Get the property of the getter and add it to the element
     * 
     * @param obj
     * @param alreadyEncoded
     * @param exclude
     * @param record
     * @param method
     * @param descName
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected static void addPropertyToElement(Object obj, IdentityHashMap<Object, Void> alreadyEncoded, Set<String> exclude,
            Element record, Method method, final String descName) throws IllegalAccessException, InvocationTargetException {
        final Object rawData = method.invoke(obj);
        if (rawData != null) {
            final Element element = propertyToElement(alreadyEncoded, descName, rawData, exclude);
            record.addContent(element);
        }
    }

    /**
     * Checks if we should add the property or not
     * 
     * @param exclude
     * @param objclass
     * @param method
     * @return
     */
    protected static boolean shouldBeAdded(Set<String> exclude, Class<? extends Object> objclass, Method method) {
        return method.getDeclaringClass() == objclass && !exclude.contains(method.getName());
    }

    protected static boolean isBooleanGetter(Method method) {
        return method.getName().startsWith("is") && method.getGenericParameterTypes().length == 0;
    }

    protected static boolean isGetter(Method method) {
        return method.getName().startsWith("get") && method.getGenericParameterTypes().length == 0;
    }

    /**
     * Gets the parent of a class but stops when reaching GeonetEntity or Object (Object shouldn't be accessed if it is a database object
     * domain because GeonetEntity should come first, but just in case.
     * 
     * @param objclass
     * @return
     */
    protected static Class<? extends Object> getNextSignificantAncestor(Class<? extends Object> objclass) {
        objclass = objclass.getSuperclass();
        if (objclass != null && (objclass.equals(GeonetEntity.class) || objclass.equals(Object.class))) {
            objclass = null;
        }
        return objclass;
    }

    /**
     * Checks if this is a label getter
     * 
     * @param descName
     * @return
     */
    protected static boolean isLabel(final String descName) {
        return descName.equals("LabelTranslations");
    }

    /**
     * Checks if this is a primitive like int or boolean
     * 
     * @param descName
     * @return
     */
    protected static boolean isADuplicatedMethodWithAnotherReturnType(final String descName) {
        return descName.endsWith("AsInt") || descName.endsWith("AsBool");
    }

    /**
     * Given a property (from a getter) on the object, convert it to a simple attribute in xml
     * 
     * @param alreadyEncoded
     * @param descName
     * @param rawData
     * @param exclude
     * @return
     */
    private static Element propertyToElement(IdentityHashMap<Object, Void> alreadyEncoded, String descName, Object rawData,
            Set<String> exclude) {
        final Element element = new Element(descName.toLowerCase());
        if (rawData instanceof GeonetEntity) {
            if (!alreadyEncoded.containsKey(rawData)) {
                final Element element1 = ((GeonetEntity) rawData).asXml(alreadyEncoded);
                final List list = element1.removeContent();
                element.addContent(list);
            }
        } else if (rawData instanceof XmlEmbeddable) {
            ((XmlEmbeddable) rawData).addToXml(element);
        } else if (hasEmbeddableAnnotation(rawData)) {
            final Element element1 = asXml(rawData, alreadyEncoded, exclude);
            final List list = element1.removeContent();
            element.addContent(list);
        } else if (rawData instanceof Iterable) {
            String childName = pluralToSingular(descName);
            for (Object o : (Iterable<?>) rawData) {
                element.addContent(propertyToElement(alreadyEncoded, childName, o, exclude));
            }
        } else {
            element.addContent(rawData.toString());
        }
        return element;
    }

    private static boolean hasEmbeddableAnnotation(Object obj) {
        return obj.getClass().getAnnotation(Embeddable.class) != null;
    }

    /**
     * Harmonize names
     * 
     * @param descName
     * @return
     */
    private static String pluralToSingular(String descName) {
        if (descName.endsWith("es")) {
            return descName.substring(0, descName.length() - 2);
        } else if (descName.endsWith("s")) {
            return descName.substring(0, descName.length() - 1);
        }
        return descName;
    }

    /**
     * Convert the entity to Xml. The process is to find all getters and invoke the getters to get the value. The xml tag is the name of the
     * getter as per the Java bean conventions, and the data is the text.
     * <p/>
     * If the returned value of the getter is another GeonetEntity then the the entity is recursively encoded, if the returned value is a
     * collection then the collection is encoded and many children of the tag. Each child tag will have the singular form of the getter or
     * if that cannot be determined they will have the same tagname.
     *
     * @return XML representing the entity.
     */
    @Nonnull
    public final Element asXml() {
        IdentityHashMap<Object, Void> alreadyEncoded = new IdentityHashMap<Object, Void>();

        Element record = asXml(alreadyEncoded);

        return record;
    }

    /**
     * Subclasses can override this if there are properties that should not be called when constructing the XML representation. of this
     * entity.
     *
     * The property should not have the get prefix.
     */
    protected Set<String> propertiesToExcludeFromXml() {
        return Collections.emptySet();
    }

    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        return asXml(this, alreadyEncoded, propertiesToExcludeFromXml());
    }

}
