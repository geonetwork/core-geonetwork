package org.fao.xsl;

import static org.junit.Assert.*;

import net.sf.saxon.Configuration;

import org.fao.geonet.util.XslUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class XslUtilTest
{
    private static String transformer;

    @BeforeClass
    public static void setup() {
        transformer = System.getProperty("javax.xml.transform.TransformerFactory");
        System.setProperty("javax.xml.transform.TransformerFactory", "de.fzi.dbs.xml.transform.CachingTransformerFactory");
    }
    @AfterClass
    public static void teardown() {
        if(transformer == null) {
            System.getProperties().remove("javax.xml.transform.TransformerFactory");
        } else {
            System.setProperty("javax.xml.transform.TransformerFactory", transformer);
        }
    }

    @Test
    public void testExpandScientific()
    {
        assertEquals("25.0", XslUtil.expandScientific("2.5E1"));
        assertEquals("25", XslUtil.expandScientific("25"));
        assertEquals("0.25", XslUtil.expandScientific("2.5E-1"));
        assertEquals("2.5", XslUtil.expandScientific("2.5E0"));
    }

    @Test
	public void testTrim() throws Exception {
		final String data = "9.554388227214988 47.227429422799325 9.55177303747616 47.223761312928346 9.558524927009797 47.224159032974136 9.568714326423056 47.2196699532557 9.584855495669517 47.20492815402061 9.580329376185912 47.19577137436106 9.572605606822618 47.19078600449766 9.573028357142288 47.17558945513593 9.564569767835167 47.17028420527933 9.579475126808317 47.171150405380835 9.596798715826692 47.16293511588421 9.605481185553527 47.1492923865341 9.614143305008204 47.14758103668585 9.621203014440818 47.15154142658616 9.625878864255304 47.145891556865365 9.622593874581305 47.14134687702446 9.62467794464066 47.132641827407035 9.635041264045803 47.12812493769189 9.620839875411871 47.1103087883032 9.634394794709424 47.10112395881238 9.633471865176878 47.08343265954178 9.623398325874577 47.082571919484174 9.61870029629155 47.0781620096246 9.61154502674488 47.07939549950689 9.61340848684568 47.06948889997308 9.60906485730699 47.06207541024225 9.582169819328978 47.05269625038509 9.55692975112104 47.04850933032742 9.551628591243894 47.0588355598477 9.539763211899613 47.06514158947552 9.51208393395024 47.05681155956954 9.50000618475596 47.05712105944623 9.499541614854131 47.0541482295661 9.49131816536049 47.05624554940342 9.484385025984087 47.04925277963224 9.48228353597144 47.056112589326666 9.475796346506117 47.051746019449894 9.472387436437717 47.06502194886433 9.513539513202083 47.085629738344096 9.519591062507237 47.09833084786932 9.508595032308762 47.13984863603497 9.49033171295671 47.16569425478833 9.48594882290298 47.181131744103446 9.489723392304073 47.19627083350568 9.504032770698023 47.22426799246752 9.52084265909696 47.24449824177717 9.530718877828498 47.27061970077713 9.566799276019418 47.24287507226918 9.554388227214988 47.227429422799325";
		String trimmed = XslUtil.trimPosList(data);
		String[] coords = trimmed.split(" ");
		assertEquals("9.554388", coords[0]);
	}

    @Test
	public void toHyperlinks() throws Exception {
		String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>hello I am some http://testdata.net. aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaélaksdjflékajsflékajsdfélkajsélfkjaslékfjaslékfjasasldjflsakjflasjfasFFFF<br/>JJJJksajfaSSSSS<br/>LLLLkjsaflkjasFFFFF<br/><p>NNNNnndjhasdfJJJ</p>. ftp://ftpsite.com";
        String result = XslUtil.toHyperlinksSplitNodes(data, Configuration.makeConfiguration(null, null)).toString();
        assertEquals(3, result.split("<a").length);
        assertEquals(2, result.split("<p>").length);
        assertEquals(2, result.split("</p>").length);
        assertEquals(4, result.split("<br/>").length);
        assertNotNull(XslUtil.parse(Configuration.makeConfiguration(null, null), result, true));
	}

    @Test
	public void toHyperlinks2() throws Exception {
		String data = "<a target=\"_new\" href=\"http://www.swisstopo.ch\">http://www.swisstopo.ch</a>";
        String result = XslUtil.toHyperlinksSplitNodes(data, Configuration.makeConfiguration(null, null)).toString();
        assertEquals(data, result);
	}

}
