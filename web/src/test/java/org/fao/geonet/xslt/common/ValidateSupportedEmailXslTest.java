package org.fao.geonet.xslt.common;

import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.hamcrest.MatcherAssert;
import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xmlunit.matchers.EvaluateXPathMatcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Parameterized.class)
public class ValidateSupportedEmailXslTest {
    private final String expectedResult;
    private final int testId;
    private final String resultString;

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        String xslFilename = "org/fao/geonet/xslt/common/utility-tester.xsl";

        Path xslFile = Paths.get(ValidateSupportedEmailXslTest.class.getClassLoader().getResource(xslFilename).toURI());

        Element inputElement = Xml.loadFile(ValidateSupportedEmailXslTest.class.getClassLoader().getResource("org/fao/geonet/xslt/common/utility-tester.xml"));
        Map<String, Object> params = new HashMap<>();
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        int expectedMailId = 0;
        ArrayList<Object[]> data = new ArrayList<>();
        for (int i = 1; i < inputElement.getContentSize(); i+=2) {
            String expectedMail = inputElement.getContent(i).getValue();
            expectedMailId++;
            Object[] row = new Object[]{expectedMailId, expectedMail, resultString};
            data.add(row);
        }

        return data;
    }


    public ValidateSupportedEmailXslTest(int testId, String expectedResult, String resultString) {
        this.testId = testId;
        this.expectedResult = expectedResult;
        this.resultString = resultString;
    }

    @Test
    public void validateSupportedEmailTest() throws Exception {
        String mailPath = "/results/a[" + testId + "]";
        MatcherAssert.assertThat(resultString, EvaluateXPathMatcher.hasXPath(mailPath, equalTo(expectedResult)));
        MatcherAssert.assertThat(resultString, EvaluateXPathMatcher.hasXPath(mailPath + "/@href", equalTo("mailto:"+expectedResult)));
    }
}
