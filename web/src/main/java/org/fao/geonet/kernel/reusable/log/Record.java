package org.fao.geonet.kernel.reusable.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.kernel.reusable.ReusableObjManager;

public class Record
{
    public enum Type
    {
        FORMATS(ReusableObjManager.FORMATS), CONTACTS(ReusableObjManager.CONTACTS), KEYWORDS(
                ReusableObjManager.KEYWORDS), EXTENTS(ReusableObjManager.EXTENTS);

        public final String desc;

        private Type(String desc)
        {
            this.desc = desc;
        }

        public static Type lookup(String originalElementName)
        {
            for (Type type : values()) {
                if (type.desc.equalsIgnoreCase(originalElementName)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(originalElementName + " is not a known type");
        }

        @Override
        public String toString()
        {
            return desc;
        }

        public Logger logger()
        {
            return Logger.getLogger(Geocat.Module.REUSABLE + "." + desc);
        }

        public static Logger parentLogger()
        {
            return Logger.getLogger(Geocat.Module.REUSABLE);
        }

    }

    public final Level   level;
    private final String msg;
    private final Type   type;

    protected Record(Level level, Type type, String msg)
    {
        this.level = level;
        this.msg = msg;
        this.type = type;
    }

    public String message()
    {
        return msg;
    }

    public Type type()
    {
        return type;
    }
}
