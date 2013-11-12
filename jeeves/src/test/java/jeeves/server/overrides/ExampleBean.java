package jeeves.server.overrides;

import java.util.ArrayList;
import java.util.List;

public class ExampleBean {
    private String basicProp;
    private String basicProp2;
    private List<String> collectionProp = new ArrayList<String>();
    private List<String> collectionProp2 = new ArrayList<String>();
    private ExampleBean simpleRefOtherNameForTesting;
    private List<ExampleBean> collectionRef = new ArrayList<ExampleBean>();

    public ExampleBean() {
        collectionProp.add("initial");
        collectionProp2.add("initial");
    }
    public String getBasicProp() {
        return basicProp;
    }
    public void setBasicProp(String basicProp) {
        this.basicProp = basicProp;
    }
    public String getBasicProp2() {
        return basicProp2;
    }
    public void setBasicProp2(String basicProp2) {
        this.basicProp2 = basicProp2;
    }
    public List<String> getCollectionProp() {
        return collectionProp;
    }
    public void setCollectionProp(List<String> collectionProp) {
        this.collectionProp = collectionProp;
    }
    public ExampleBean getSimpleRef() {
        return simpleRefOtherNameForTesting;
    }
    public void setSimpleRef(ExampleBean simpleRef) {
        this.simpleRefOtherNameForTesting = simpleRef;
    }
    public List<ExampleBean> getCollectionRef() {
        return collectionRef;
    }
    public void setCollectionRef(List<ExampleBean> collectionRef) {
        this.collectionRef = collectionRef;
    }

    
}
