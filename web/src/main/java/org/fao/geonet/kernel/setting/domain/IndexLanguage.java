package org.fao.geonet.kernel.setting.domain;

/**
 * Represents a language to be used for stopword filtering in the Lucene index.
 */
public class IndexLanguage {
    private String name;
    private String nameId;
    private boolean selected;
    private String selectedId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    public boolean isSelected() {

        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
