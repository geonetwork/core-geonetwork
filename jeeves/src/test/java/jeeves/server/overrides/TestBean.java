package jeeves.server.overrides;

import java.util.ArrayList;
import java.util.List;

public class TestBean {
    private String basicProp;
    private String basicProp2;
    private List<String> collectionProp = new ArrayList<String>();
    private List<String> collectionProp2 = new ArrayList<String>();
    private TestBean simpleRefOtherNameForTesting;
    private List<TestBean> collectionRef = new ArrayList<TestBean>();

    public TestBean() {
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
    public TestBean getSimpleRef() {
        return simpleRefOtherNameForTesting;
    }
    public void setSimpleRef(TestBean simpleRef) {
        this.simpleRefOtherNameForTesting = simpleRef;
    }
    public List<TestBean> getCollectionRef() {
        return collectionRef;
    }
    public void setCollectionRef(List<TestBean> collectionRef) {
        this.collectionRef = collectionRef;
    }

    
}
