package org.fao.geonet.services.category;

import com.google.common.collect.Lists;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response object for Update categories.
 *
 * @author Jesse on 6/4/2014.
 */
@XmlRootElement(name = "response")
public class CategoryUpdateResponse {
    private java.util.List<Operation> operations = Lists.newArrayList();

    public List<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation value) {
        this.operations.add(value);
    }

    enum Operation {
        added, updated, removed
    }
}
