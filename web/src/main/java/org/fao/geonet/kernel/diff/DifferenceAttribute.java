package org.fao.geonet.kernel.diff;

/**
 *
 * @author heikki doeleman
 */
public enum DifferenceAttribute {

    pos,
    oldPos,
    updatedElement,
    inserted,
    deleted,
    insertedAttribute,
    deletedAttribute,
    updatedAttribute,
    updatedText,
    insertedText,
    deletedText;


    public static boolean exists(String name) {
        return name.equals(pos.name()) ||
                name.equals(oldPos.name()) ||
                name.equals(updatedElement.name()) ||
                name.equals(inserted.name()) ||
                name.equals(deleted.name()) ||
                name.equals(insertedAttribute.name()) ||
                name.equals(deletedAttribute.name()) ||
                name.equals(updatedAttribute.name()) ||
                name.equals(updatedText.name()) ||
                name.equals(insertedText.name()) ||
                name.equals(deletedText.name());
    }

}
