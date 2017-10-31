package org.fao.geonet.kernel.xlink;

import jeeves.xlink.XLink;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fgravin on 10/26/17.
 */
public final class Utils {


    static boolean isXLink(Element elem)
    {
        if( elem==null ) return false;
        return elem.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK) != null;
    }

    static <T> List<T> convertToList(Iterator<?> iter, Class<T> class1 ) {
        List<T> placeholders = new ArrayList<T>();
        while( iter.hasNext() ) {
            placeholders.add(class1.cast(iter.next()));
        }
        return placeholders;
    }

}
