package org.fao.geonet.domain;

import org.jdom.Element;
import org.springframework.beans.BeanWrapperImpl;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import java.beans.PropertyDescriptor;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains common methods of all entities in Geonetwork.
 * <p/>
 * User: Jesse
 * Date: 9/10/13
 * Time: 4:33 PM
 */
public class GeonetEntity {

    public static final String LABEL_EL_NAME = "label";
    public static final String RECORD_EL_NAME = "record";

    /**
     * Convert the entity to Xml.  The process is to find all getters and invoke the getters to get the value.  The xml
     * tag is the name of the getter as per the Java bean conventions, and the data is the text.
     * <p/>
     * If the returned value of the getter is another GeonetEntity then the the entity is recursively encoded, if the returned
     * value is a collection then the collection is encoded and many children of the tag.  Each child tag will have the singular
     * form of the getter or if that cannot be determined they will have the same tagname.
     *
     * @return XML representing the entity.
     */
    @Nonnull
    public final Element asXml() {
        IdentityHashMap<Object, Void> alreadyEncoded = new IdentityHashMap<Object, Void>();

        Element record = asXml(alreadyEncoded);

        return record;
    }

    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        return asXml(this, alreadyEncoded);
    }

    private static Element asXml(Object obj, IdentityHashMap<Object, Void> alreadyEncoded) {
        alreadyEncoded.put(obj, null);
        Element record = new Element(RECORD_EL_NAME);
        BeanWrapperImpl wrapper = new BeanWrapperImpl(obj);

        for (PropertyDescriptor desc : wrapper.getPropertyDescriptors()) {
            try {
                if (desc.getReadMethod() != null && desc.getReadMethod().getDeclaringClass() == obj.getClass()) {
                    final String descName = desc.getName();
                    if (descName.equalsIgnoreCase("labelTranslations")) {
                        Element labelEl = new Element(LABEL_EL_NAME);

                        @SuppressWarnings("unchecked")
                        Map<String, String> labels = (Map<String, String>) desc.getReadMethod().invoke(obj);

                        if (labels != null) {
                            for (Map.Entry<String, String> entry : labels.entrySet()) {
                                labelEl.addContent(new Element(entry.getKey().toLowerCase()).setText(entry.getValue()));
                            }
                        }

                        record.addContent(labelEl);
                    } else {
                        final Object rawData = desc.getReadMethod().invoke(obj);
                        if (rawData != null) {
                            final Element element = propertyToElement(alreadyEncoded, descName, rawData);
                            record.addContent(element);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return record;
    }

    private static Element propertyToElement(IdentityHashMap<Object, Void> alreadyEncoded, String descName, Object rawData) {
        final Element element = new Element(descName.toLowerCase());
        if (rawData instanceof GeonetEntity) {
            if (!alreadyEncoded.containsKey(rawData)) {
                final Element element1 = ((GeonetEntity)rawData).asXml(alreadyEncoded);
                final List list = element1.removeContent();
                element.addContent(list);
            }
        } else if (rawData instanceof XmlEmbeddable) {
            ((XmlEmbeddable) rawData).addToXml(element);
        } else if (hasEmbeddableAnnotation(rawData)) {
            final Element element1 = asXml(rawData, alreadyEncoded);
            final List list = element1.removeContent();
            element.addContent(list);
        } else if (rawData instanceof Iterable) {
            String childName = pluralToSingular(descName);
            for (Object o : (Iterable<?>) rawData) {
                element.addContent(propertyToElement(alreadyEncoded, childName, o));
            }
        } else {
            element.addContent(rawData.toString());
        }
        return element;
    }

    private static boolean hasEmbeddableAnnotation(Object obj) {
        return obj.getClass().getAnnotation(Embeddable.class) != null;
    }

    private static String pluralToSingular(String descName) {
        if (descName.endsWith("es")) {
            return descName.substring(0, descName.length() - 2);
        } else if (descName.endsWith("s")) {
            return descName.substring(0, descName.length() - 1);
        }
        return descName;
    }

}
