package org.fao.geonet.domain;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.filter.ContentFilter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MetadataTest {

    @Test
    public void testSetDataAndFixCR() throws Exception {
        final Metadata metadata = new Metadata();
        Element md = new Element("root");
        md.addContent(new Element("child").setText("hi"));
        md.addContent(new Comment("Comment"));

        metadata.setDataAndFixCR(md);

        md = metadata.getXmlData(false);
        final List<Content> content = md.getContent(new ContentFilter(ContentFilter.COMMENT | ContentFilter.ELEMENT));
        assertEquals(2, content.size());

        assertEquals("hi", ((Element)content.get(0)).getText());
        assertEquals("Comment", content.get(1).getValue());

    }
}