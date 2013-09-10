package org.fao.geonet.domain;

import org.jdom.Element;
import org.springframework.beans.BeanWrapperImpl;

import javax.persistence.Transient;
import java.beans.PropertyDescriptor;
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
                if (desc.getReadMethod().getDeclaringClass() == getClass() && desc.getReadMethod().getAnnotation(Transient.class)
                                                                              == null) {
                    final String descName = desc.getName();
                    if (descName.equalsIgnoreCase("labelTranslations")) {
                        Element labelEl = new Element(LABEL_EL_NAME);

                        Map<String, String> labels = (Map<String, String>) desc.getReadMethod().invoke(this);
                        for (Map.Entry<String, String> entry : labels.entrySet()) {
                            labelEl.addContent(new Element(entry.getKey().toLowerCase()).setText(entry.getValue()));
                        }

                        record.addContent(labelEl);
                    } else {
                        final String value;
                        value = desc.getReadMethod().invoke(this).toString();
                        record.addContent(
                                new Element(descName.toLowerCase()).setText(value)
                        );
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return record;
    }

}
