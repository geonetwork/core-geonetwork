package org.fao.geonet.services.metadata.format.groovy.util;

import org.fao.geonet.services.metadata.format.groovy.Functions;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the group name and icon of a type of links.  IE Dataset, parent, children, etc...
 *
 * @author Jesse on 11/20/2014.
 */
public class LinkType {
    /**
     * The untranslated name of the link.
     */
    public String name;
    /**
     * The direction of the relationship.  Indicates if the current metadata is the parent, child,sibling of the related item.
     *
     * In some cases this is not important (link linking to webpages), in those cases it can be null.
     */
    public Direction relationDirection;
    /**
     * The url of the icon for this link group.  This may be null.
     */
    public String icon;
    /**
     * The classes to put on the &lt;i> element
     */
    public String iconClasses;
    /**
     * The title attribute to put on the icon elements (&lt;i> if iconClasses != null and &lt;img> if icon != null)
     */
    public String iconTitle;
    /**
     * Raw HTML for the icon.
     */
    public String iconHtml;
    public LinkType(){}
    public LinkType(@Nonnull String name, @Nullable Direction relationDirection,
                    @Nullable String icon, @Nullable String iconClasses) {
        this(name, relationDirection, icon, iconClasses, null);
    }

    public LinkType(@Nonnull String name, @Nullable Direction relationDirection,
                    @Nullable String icon, @Nullable String iconClasses, @Nullable String iconTitle) {
        Objects.requireNonNull(name);
        this.name = name;
        this.relationDirection = relationDirection;
        this.icon = icon;
        this.iconClasses = iconClasses;
        this.iconTitle = iconTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkType linkType = (LinkType) o;
        return com.google.common.base.Objects.equal(name, linkType.name) &&
               com.google.common.base.Objects.equal(relationDirection, linkType.relationDirection);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(name, relationDirection);
    }

    public String getName(Functions functions) throws Exception {
        return functions.translate(name);
    }

    @Override
    public String toString() {
        return "LinkType{" + "dir='" + relationDirection + '\'' +  "name='" + name + '\'' + '}';
    }
}
