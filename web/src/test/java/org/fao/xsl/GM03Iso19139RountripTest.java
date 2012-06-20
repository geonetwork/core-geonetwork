package org.fao.xsl;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.validation.Schema;


import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.services.gm03.TranslateAndValidate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class GM03Iso19139RountripTest
{

    static String base;

    static File     src;
    static File     isoResult;
    static File     cheResult;
    static File     isoRoundRoundResult;
    static File     cheRoundRoundResult;

    private static String transformer;

    File[][] file;

    public void configure(File baseFile)
    {
        base = baseFile.getName();

        src = baseFile.getAbsoluteFile();
        isoResult = new File(TransformationTestSupport.outputDir, "result_" + base).getAbsoluteFile();
        cheResult = new File(TransformationTestSupport.outputDir, "result_result_" + base).getAbsoluteFile();
        isoRoundRoundResult = new File(TransformationTestSupport.outputDir, "result_result_result_" + base).getAbsoluteFile();
        cheRoundRoundResult = new File(TransformationTestSupport.outputDir, "result_result_result_result_" + base).getAbsoluteFile();

        file = new File[][]{ new File[] { isoResult, TransformationTestSupport.isoXsd }, new File[] { cheResult, TransformationTestSupport.gm03Xsd },
               new File[] { isoRoundRoundResult, TransformationTestSupport.isoXsd }, new File[] { cheRoundRoundResult, TransformationTestSupport.gm03Xsd } };
    }
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
    public void gm03RoundTrip() throws Throwable
    {
        File[] xml = new File(TransformationTestSupport.data,"gm03").listFiles();
//        testFile(new File(TransformationTestSupport.data,"gm03/AllComprehensiveAttributes.xml"), Control.FIRST_PASS, false);
//        testFile(new File(TransformationTestSupport.data,"gm03/wanderkartenGC1.xml"), Control.FIRST_PASS, false);
        for (File file : xml) {
           testFile(file, Control.FIRST_PASS, false);
        }
    }

    @Test
    public void iso19139RoundTrip() throws Throwable
    {
        File[] xml = new File(TransformationTestSupport.data,"iso19139").listFiles();

        testFile(new File(TransformationTestSupport.data, "iso19139/service_19139che.xml"), Control.START_WITH_ISO, false);
        for (File file : xml) {
        //    testFile(file, Control.START_WITH_ISO, false);
        }
    }

    @Test
    public void iso19139SchematronValidation() throws Throwable
    {
        File[] xml = new File(TransformationTestSupport.data,"schematrontests").listFiles();

//      testFile(new File(TransformationTestSupport.data, "schematrontests/Forestier.xml"), Control.START_WITH_ISO, true);
        for (File file : xml) {
            if(file.getName().endsWith("-iso19139.xml"))
                testFile(file, Control.START_WITH_ISO, true);
            else
                testFile(file, Control.FIRST_PASS, true);
        }
    }

    @Test(expected=AssertionError.class) @Ignore
    public void iso19139SchematronVerifyInvalid() throws Throwable
    {
        // This verifies that the schematron test works correctly
        testFile(new File(TransformationTestSupport.data, "invalid-iso19139.xml"), Control.START_WITH_ISO, true);
    }


    private void testFile(File file, Control control, boolean schematronValidate) throws Throwable
    {
        System.out.println("Testing RoundTrip on file: "+file);
        configure(file);
        TransformationTestSupport.delete(TransformationTestSupport.outputDir);
        TransformationTestSupport.outputDir.mkdirs();

        try {
            roundTripTransform(src, TransformationTestSupport.outputDir, isoResult,control, schematronValidate);
            System.out.println("Finished first round trip");

            assertTrue(isoResult.exists());
            assertTrue(cheResult.exists());

            roundTripTransform(cheResult, TransformationTestSupport.outputDir, isoRoundRoundResult,Control.SECOND_PASS, schematronValidate);

            assertTrue(isoRoundRoundResult.exists());
            assertTrue(cheRoundRoundResult.exists());

        } catch (AssertionError e) {
            validation(e, control);
        } catch (RuntimeException e) {
            validation(e, control);
        } finally {
            // delete(outputDir);
        }

    }

    private void validation(Throwable e, Control control) throws Throwable
    {
        TranslateAndValidate transformer = new TranslateAndValidate();
        transformer.outputDir = TransformationTestSupport.outputDir;
        for (File[] info : file) {
            if(info[0]==isoResult) continue;

            if (info[0].exists()) {
                Schema schema = TranslateAndValidate.SCHEMA_FACTORY.newSchema(info[1]);
                try{
                    transformer.validate(schema, info[0].getAbsolutePath());
                }catch(Throwable e2){
                    System.out.println(e2.getMessage());
                    throw e;
                }
            }
        }
        throw e;
    }

    private void roundTripTransform(File src, File outputDir, File isoResult, Control control, boolean schematronValidate) throws Throwable
    {
        assertTrue(TransformationTestSupport.toGm03StyleSheet.getCanonicalPath(), TransformationTestSupport.toGm03StyleSheet.exists());
        assertTrue(TransformationTestSupport.gm03Xsd.getCanonicalPath(), TransformationTestSupport.gm03Xsd.exists());
        assertTrue(TransformationTestSupport.toIsoStyleSheet.getCanonicalPath(), TransformationTestSupport.toIsoStyleSheet.exists());
        assertTrue(TransformationTestSupport.isoXsd.getCanonicalPath(), TransformationTestSupport.isoXsd.exists());


        switch (control)
        {
        case FIRST_PASS:
            TransformationTestSupport.transformGM03_1ToIso(src, outputDir);
            if(schematronValidate){
                TransformationTestSupport.schematronValidation(isoResult);
            }
            System.out.println("Finished transforming to iso");
            break;
        case START_WITH_ISO:
            TransformationTestSupport.copy(src,isoResult);
            if(schematronValidate){
                TransformationTestSupport.schematronValidation(isoResult);
            }
            Schema schema = TranslateAndValidate.SCHEMA_FACTORY.newSchema(TransformationTestSupport.isoXsd);
            try{
                TranslateAndValidate transformer = new TranslateAndValidate();
                transformer.outputDir = outputDir;
                transformer.validate(schema, isoResult.getAbsolutePath());
            }catch(Throwable e){
                System.out.println(e.getMessage());
                throw e;
            }

            System.out.println("Finished validating input iso file");
            break;
        case SECOND_PASS:
            TransformationTestSupport.transformGM03_2toIso(src, outputDir);
            if(schematronValidate){
                TransformationTestSupport.schematronValidation(isoResult);
            }
            System.out.println("Finished transforming to iso");
            break;
        default:
            throw new AssertionError("didnt expect "+control);
        }

        TransformationTestSupport.transformIsoToGM03(isoResult, outputDir);
        System.out.println("Finished transforming to gm03");
    }

    public static void main(String[] args) throws Exception
    {
        TranslateAndValidate transformer = new TranslateAndValidate();
        transformer.outputDir = TransformationTestSupport.outputDir;
        transformer.run(new File(TransformationTestSupport.geonetworkWebapp, "xsl/conversion/import/GM03-to-ISO19139CHE.xsl"), TransformationTestSupport.isoXsd, new String[] { src.getAbsolutePath() });

    }

    enum Control {
        FIRST_PASS, SECOND_PASS, START_WITH_ISO
    }
}
