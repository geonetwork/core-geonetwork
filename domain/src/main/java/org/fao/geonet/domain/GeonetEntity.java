package org.fao.geonet.domain;

import org.jdom.Content;
import org.jdom.Element;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Transient;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Contains common methods of all entities in Geonetwork.
 *
 * User: Jesse
 * Date: 9/10/13
 * Time: 4:33 PM
 */
public class GeonetEntity {

    public static final String LABEL_EL_NAME = "label";
    public static final String RECORD_EL_NAME = "record";

    public Element asXml() {
        Element record = new Element(RECORD_EL_NAME);
        BeanWrapperImpl wrapper = new BeanWrapperImpl(this);

        for (PropertyDescriptor desc : wrapper.getPropertyDescriptors()) {
            try {
                if (desc.getReadMethod() != null && desc.getReadMethod().getDeclaringClass() == getClass()) {
                    final String descName = desc.getName();
                    if (descName.equalsIgnoreCase("labelTranslations")) {
                        Element labelEl = new Element(LABEL_EL_NAME);

                        Map<String, String> labels = (Map<String, String>) desc.getReadMethod().invoke(this);
                        if (labels != null) {
                            for (Map.Entry<String, String> entry : labels.entrySet()) {
                                labelEl.addContent(new Element(entry.getKey().toLowerCase()).setText(entry.getValue()));
                            }
                        }

                        record.addContent(labelEl);
                    } else {
                        final Object rawData = desc.getReadMethod().invoke(this);
                        if (rawData != null) {
                            final Element element = new Element(descName.toLowerCase());
                            if (rawData instanceof GeonetEntity) {
                                final Element element1 = ((GeonetEntity) rawData).asXml();
                                final List list = element1.removeContent();
                                element.addContent(list);
                            } else {
                                element.addContent(rawData.toString());
                            }
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


}
