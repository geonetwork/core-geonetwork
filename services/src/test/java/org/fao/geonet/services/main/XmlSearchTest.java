package org.fao.geonet.services.main;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class XmlSearchTest {
    XmlSearch xs = new XmlSearch();
    Method setSafeBoundaries = null;

    @Before
    public void setUp() {
        this.setSafeBoundaries = ReflectionUtils.findMethod(XmlSearch.class, "setSafeBoundaries", Element.class);
        this.setSafeBoundaries.setAccessible(true);
    }

    @Test
    public void testFromDefinedToDefined() {
        Element params = new Element("request");
        params.addContent(new Element("from").setText("1"))
            .addContent(new Element("to").setText("99"));

        boolean ret = (boolean) ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);

        // 1 to 99 should be acceptable, no modification of passed parameters
        assertFalse(ret);
    }

    @Test(expected = RuntimeException.class)
    public void testFromDefinedToDefinedFromBiggerThanTo() {
        Element params = new Element("request");
        params.addContent(new Element("from").setText("99"))
            .addContent(new Element("to").setText("1"));

        ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);
    }

    @Test
    public void testFromDefinedToDefinedOutOfRange() {
        Element params = new Element("request");
        params.addContent(new Element("from").setText("324"))
            .addContent(new Element("to").setText(Integer.toString(324 + xs.getMaxRecordValue() + 55)));

        boolean ret = (boolean) ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);

        assertTrue(ret);
        assertTrue(params.getChild("from").getText().equals("324"));
        assertTrue(params.getChild("to").getText().equals(Integer.toString(324 + xs.getMaxRecordValue() - 1)));
    }

    @Test
    public void testFromUndefinedToUndefined() {
        Element params = new Element("request");
        boolean ret = (boolean) ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);

        assertTrue("expected modified passed params", ret);
        assertTrue(params.getChild("from").getText().equals("1"));
        assertTrue(params.getChild("to").getText().equals(Integer.toString(xs.getMaxRecordValue())));
    }

    @Test
    public void testFromUndefinedToDefined() {
        Element params = new Element("request");
        params.addContent(new Element("to").setText("150"));

        boolean ret = (boolean) ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);

        assertTrue("expected modified passed params", ret);
        assertTrue(params.getChild("from").getText().equals(Integer.toString(150 - xs.getMaxRecordValue())));
        assertTrue(params.getChild("to").getText().equals("150"));
    }

    @Test
    public void testFromUndefinedToDefinedButTooSmall() {
        Element params = new Element("request");
        int boundaryTo = xs.getMaxRecordValue() / 2;
        params.addContent(new Element("to").setText(Integer.toString(boundaryTo)));

        boolean ret = (boolean) ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);

        assertTrue("expected modified passed params", ret);
        assertTrue(params.getChild("from").getText().equals("1"));
        assertTrue(params.getChild("to").getText().equals(Integer.toString(boundaryTo)));
    }

    @Test
    public void testFromDefinedToUndefined() {
        Element params = new Element("request");
        params.addContent(new Element("from").setText("42"));

        boolean ret = (boolean) ReflectionUtils.invokeMethod(setSafeBoundaries, xs, params);

        assertTrue("expected modified passed params", ret);
        assertTrue(params.getChild("from").getText().equals("42"));
        assertTrue(params.getChild("to").getText().equals(Integer.toString(42 + xs.getMaxRecordValue() - 1)));
    }
}
