package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.util.Assert;

import java.util.List;

/**
 * Represents an element in a list that can be clicked on for an action.
 *
 * @author Jesse on 1/6/2015.
 */
public class MenuAction {
    private String iconClasses = "";
    private String label = "";
    private String liClasses = "";
    private String javascript = "";
    private List<MenuAction> submenu = Lists.newArrayList();


    /**
     * Get the string to add to the &lt;i> tag of the action.  For example it might be: fa fa-eye to have the eye icon
     * @return
     */
    public String getIconClasses() {
        return iconClasses;
    }

    public void setIconClasses(String iconClasses) {
        this.iconClasses = iconClasses;
    }

    /**
     * Get the label key.  This is the key to use for looking up the translation for the action label.
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the javascript to execute when this list item is clicked on.
     */
    public String getJavascript() {
        return javascript;
    }

    public void setJavascript(String javascript) {
        this.javascript = javascript;
    }

    /**
     * Get the submenu menu actions.
     */
    public List<MenuAction> getSubmenu() {
        for (MenuAction menuAction : submenu) {
            Assert.isTrue(menuAction.submenu.isEmpty(), "A MenuAction that is part of a submenu may no have a submenu because nested submenus is not permitted");
        }

        return submenu;
    }

    public void setSubmenu(List<MenuAction> submenu) {
        this.submenu = submenu;
    }

    /**
     * Set the classes that will go on the li element for the menu item
     */
    public String getLiClasses() {
        return liClasses;
    }

    public void setLiClasses(String liClasses) {
        this.liClasses = liClasses;
    }
}
