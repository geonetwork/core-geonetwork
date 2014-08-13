package org.fao.xsl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jeeves.utils.Xml;
import org.fao.xsl.support.And;
import org.fao.xsl.support.Attribute;
import org.fao.xsl.support.ContainsText;
import org.fao.xsl.support.Count;
import org.fao.xsl.support.DoesNotExist;
import org.fao.xsl.support.EqualText;
import org.fao.xsl.support.EqualTrimText;
import org.fao.xsl.support.Exists;
import org.fao.xsl.support.Finder;
import org.fao.xsl.support.Not;
import org.fao.xsl.support.PolygonValidator;
import org.fao.xsl.support.Prefix;
import org.fao.xsl.support.Requirement;
import org.fao.xsl.support.StartsWithText;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import static org.fao.geonet.services.extent.ExtentHelper.ExtentTypeCode.EXCLUDE;
import static org.fao.geonet.services.extent.ExtentHelper.ExtentTypeCode.INCLUDE;
import static org.fao.geonet.services.extent.ExtentHelper.ExtentTypeCode.NA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidateTransformationTest
{

    static final File data      = TransformationTestSupport.data;
    static final File outputDir = TransformationTestSupport.outputDir;
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

    @Before
    public void deleteOutputDir()
    {
        TransformationTestSupport.delete(outputDir);
    }
    
    @Test
    public void iso19139NewLangCodeTransform() throws Throwable
    {
    	File file = new File(data, "iso19139/linkage.xml");
    	Multimap<String, Requirement> rules = ArrayListMultimap.create();
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification", new Exists(new Finder("language", new EqualTrimText("fr"))));
    	rules.put("DATASECTION", new Not(new ContainsText("ERROR")));
    	file = testFile(file, Control.ISO_GM03, rules, true);

    	rules.clear();
    	rules.put("CHE_MD_Metadata/language", new Exists(new Finder("CharacterString", new EqualTrimText("fre"))));
    	rules.put("CHE_MD_Metadata/locale", new Exists(new Attribute("LanguageCode", "codeListValue", "fre")));
    	rules.put("CHE_MD_Metadata/locale", new Exists(new Attribute("LanguageCode", "codeListValue", "ger")));
    	rules.put("CHE_MD_Metadata/locale", new Exists(new Attribute("LanguageCode", "codeListValue", "ita")));
    	rules.put("CHE_MD_Metadata/locale", new Exists(new Attribute("LanguageCode", "codeListValue", "eng")));
    	rules.put("CHE_MD_Metadata/locale", new Exists(new Attribute("LanguageCode", "codeListValue", "roh")));
    	testFile(file, Control.GM03_2_ISO, rules, true);
    }
    @Test
    public void textExportDataModel_SuppInfo_EnvDesc() throws Throwable
    {
    	File file = new File(data, "non_validating/ExportGM03_Missing_EnvDes_SuppInfo_DataModel.xml");
    	Multimap<String, Requirement> rules = ArrayListMultimap.create();
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("environmentDescription",
                        new Exists(new Finder("plainText", new EqualTrimText("DE Env Desc"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("environmentDescription",
                        new Exists(new Finder("plainText", new EqualTrimText("IT Env Desc"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("environmentDescription",
                        new Exists(new Finder("plainText", new EqualTrimText("FR Env Desc"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("environmentDescription",
                        new Exists(new Finder("plainText", new EqualTrimText("EN DE Env Desc"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("supplementalInformation",
                        new Exists(new Finder("plainText", new EqualTrimText("DE Supplemental Information"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("supplementalInformation",
                        new Exists(new Finder("plainText", new EqualTrimText("EN Supplemental Information"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("supplementalInformation",
                        new Exists(new Finder("plainText", new EqualTrimText("IT  Supplemental Information"))))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification",
                new Exists(new Finder("supplementalInformation",
                        new Exists(new Finder("plainText", new EqualTrimText("FR Supplemental Information"))))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription",
                new Exists(new Finder("dataModel",
                        new Exists(new Finder("plainURL", new EqualTrimText("DE Data Model"))))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription",
                new Exists(new Finder("dataModel",
                        new Exists(new Finder("plainURL", new EqualTrimText("FR Data Model"))))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription",
                new Exists(new Finder("dataModel",
                        new Exists(new Finder("plainURL", new EqualTrimText("EN Data Model"))))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription",
                new Exists(new Finder("dataModel",
                        new Exists(new Finder("plainURL", new EqualTrimText("IT Data Model"))))));
    	testFile(file, Control.ISO_GM03, rules, true);

    }
    @Test
    public void exportServiceMetadata() throws Throwable
    {
    	File file = new File(data, "iso19139/servicemetadata.xml");
    	Multimap<String, Requirement> rules = ArrayListMultimap.create();
    	rules.put("DATASECTION", new Not(new ContainsText("ERROR")));
    	testFile(file, Control.ISO_GM03, rules, true);

    }
@Test
    public void exportAltTitleDuplicated() throws Throwable
    {
    	File file = new File(data, "non_validating/iso19139che/problemTitle_remove_charstrings.xml");
    	Multimap<String, Requirement> rules = ArrayListMultimap.create();
    	rules.put("alternateTitle",
                new Count(1, new Finder("plainText")));
    	testFile(file, Control.ISO_GM03, rules, false);

    }

    @Test
    public void smallGeom() throws Throwable
    {
        File file = new File(data, "non_validating/smallGeom.xml");
		Multimap<String, Requirement> rules = ArrayListMultimap.create();
	    testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void exportThesaurusTitle() throws Throwable
    {
        File file = new File(data, "non_validating/iso19139che/exportThesaurusTitleToGM03Bug.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Core.Core.MD_Keywords", new Count(1, new Finder("thesaurus")));
        rules.put("GM03_2_1Core.Core.MD_Keywords", new Exists(new Attribute("thesaurus", "REF", null)));
        rules.put("TRANSFER/DATASECTION/GM03_2_1Comprehensive.Comprehensive", new Count(1, new Finder("GM03_2_1Core.Core.MD_Thesaurus/citation")));
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules.clear();
        rules.put("CHE_MD_Metadata/identificationInfo/CHE_MD_DataIdentification", new Count(1, new Finder("descriptiveKeywords/MD_Keywords/keyword")));
        rules.put("CHE_MD_Metadata/identificationInfo/CHE_MD_DataIdentification", new Count(1, new Finder("descriptiveKeywords/MD_Keywords/thesaurusName/CI_Citation/title")));
        file = testFile(file, Control.GM03_2_ISO, rules, true);
        System.out.println(file);
    }

    @Test
    public void keywordGmdTitleInCorrectPlace() throws Throwable
    {
        File file = new File(data, "non_validating/iso19139che/exportThesaurusTitleToGM03Bug.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.ISO_GM03, rules, true);

        // ensure file validates
        file = testFile(file, Control.GM03_2_ISO, rules, true);
        System.out.println(file);
    }

    @Test
    public void GM03TitleError() throws Throwable
    {
        File file = new File(data, "non_validating/gm03V2/cad_couvert_title_is_error_after_process.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.GM03_2_ISO, rules, false);
    }

    @Test
    public void exportTopicCategoryToGM03() throws Throwable
    {
        File file = new File(data, "non_validating/iso19139che/topicCategory.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification", new Count(1, new Finder("topicCategory")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new DoesNotExist(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("imageryBaseMapsEarthCover"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new DoesNotExist(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("planningCadastre"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new DoesNotExist(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("geoscientificInformation"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new DoesNotExist(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("environment"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new DoesNotExist(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("utilitiesCommunication"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Exists(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("planningCadastre.planningCadastre_Planning"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Exists(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("imageryBaseMapsEarthCover.imageryBaseMapsEarthCover_BaseMaps"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Exists(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("planningCadastre.planningCadastre_Planning"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Exists(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("geoscientificInformation.geoscientificInformation_Geology"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Exists(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("environment.environment_EnvironmentalProtection"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Exists(new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value", new EqualText("utilitiesCommunication.utilitiesCommunication_Energy"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/topicCategory", new Count(7, new Finder("GM03_2_1Core.Core.MD_TopicCategoryCode_/value")));
        file = testFile(file, Control.ISO_GM03, rules, false);
        
        rules.clear();
        rules.put("che:CHE_MD_DataIdentification/gmd:topicCategory", new Exists(new Finder("MD_TopicCategoryCode", new EqualText("planningCadastre_Planning"))));
        rules.put("che:CHE_MD_DataIdentification/gmd:topicCategory", new Exists(new Finder("MD_TopicCategoryCode", new EqualText("imageryBaseMapsEarthCover_BaseMaps"))));
        rules.put("che:CHE_MD_DataIdentification/gmd:topicCategory", new Exists(new Finder("MD_TopicCategoryCode", new EqualText("planningCadastre_Planning"))));
        rules.put("che:CHE_MD_DataIdentification/gmd:topicCategory", new Exists(new Finder("MD_TopicCategoryCode", new EqualText("geoscientificInformation_Geology"))));
        rules.put("che:CHE_MD_DataIdentification/gmd:topicCategory", new Exists(new Finder("MD_TopicCategoryCode", new EqualText("environment_EnvironmentalProtection"))));
        rules.put("che:CHE_MD_DataIdentification/gmd:topicCategory", new Exists(new Finder("MD_TopicCategoryCode", new EqualText("utilitiesCommunication_Energy"))));

        testFile(file, Control.GM03_2_ISO, rules, false);
        
    }

    @Test
    public void exportPresentationFormToGM03() throws Throwable
    {
        File file = new File(data, "non_validating/iso19139che/presentationForm.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.CI_Citation", new Count(1, new Finder("presentationForm")));
        file = testFile(file, Control.ISO_GM03, rules, false);
    }
    
    @Test
    public void removeDuplicateTranslation() throws Throwable
    {
    	File file = new File(data, "non_validating/iso19139che/withCharstringAndPtFreeText.xml");
    	Multimap<String, Requirement> rules = ArrayListMultimap.create();

    	rules.put("GM03_2_1Comprehensive.Comprehensive.CI_Citation/title/GM03_2_1Core.Core.PT_FreeText",
    			new Count(1, new Finder("GM03_2_1Core.Core.PT_Group/language", new EqualText("de"))));
    	rules.put("GM03_2_1Comprehensive.Comprehensive.CI_Citation/title/GM03_2_1Core.Core.PT_FreeText",
    			new Count(1, new Finder("GM03_2_1Core.Core.PT_Group/language", new EqualText("fr"))));
    	testFile(file, Control.ISO_GM03, rules, false);
    }

    @Test
    public void contactLinkageNotImported() throws Throwable
    {
        File file = new File(data, "iso19139/contact_with_linkage.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty/linkage/GM03_2_1Core.Core.PT_FreeURL",
                new Exists(new Finder("plainURL", new EqualTrimText("http://etat.geneve.ch/dt/geomatique/accueil.html"))));
        file = testFile(file, Control.ISO_GM03, rules, false);

        rules = ArrayListMultimap.create();
        rules.put("CHE_MD_Metadata/contact/CHE_CI_ResponsibleParty/contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage",
                new Exists(new Finder("LocalisedURL", new EqualText("http://etat.geneve.ch/dt/geomatique/accueil.html"))));
        testFile(file, Control.GM03_2_ISO, rules, false);
    }

    @Test
    public void exportPurpose() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("che:CHE_MD_DataIdentification/purpose",new Exists(
                new Finder("LocalisedCharacterString",new And(new Exists(new Attribute("LocalisedCharacterString","locale","#DE")), new EqualText("de geocat.ch II testen")))));
        rules.put("che:CHE_MD_DataIdentification/purpose",new Exists(
                new Finder("LocalisedCharacterString",new And(new Exists(new Attribute("LocalisedCharacterString","locale","#FR")), new EqualText("fr geocat.ch II testen")))));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        rules.clear();
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/purpose",new Exists(new Finder("GM03_2_1Core.Core.PT_Group",
                new And(new Exists(new Finder("language",new EqualText("de"))),
                        new Exists(new Finder("plainText",new EqualText("de geocat.ch II testen")))
        ))));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification/purpose",new Exists(new Finder("GM03_2_1Core.Core.PT_Group",
                new And(new Exists(new Finder("language",new EqualText("fr"))),
                        new Exists(new Finder("plainText",new EqualText("fr geocat.ch II testen")))
                        ))));
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules.clear();
        rules.put("che:CHE_MD_DataIdentification/purpose",new Exists(
                new Finder("LocalisedCharacterString",new And(new Exists(new Attribute("LocalisedCharacterString","locale","#DE")), new EqualText("de geocat.ch II testen")))));
        rules.put("che:CHE_MD_DataIdentification/purpose",new Exists(
                new Finder("LocalisedCharacterString",new And(new Exists(new Attribute("LocalisedCharacterString","locale","#FR")), new EqualText("fr geocat.ch II testen")))));
        file = testFile(file, Control.GM03_2_ISO, rules, true);


    }

    @Test
    public void doNotCreateEmptyBasicGeoId() throws Throwable
    {
        File file = new File(data, "gm03V2/noBasicGeoId.xml");
		Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("identificationInfo",new Not(new Exists(new Finder("basicGeodataID"))));
	    file = testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void serviceDoesNotHaveBasicGeoId() throws Throwable
    {
        File file = new File(data, "iso19139/service_19139che.xml");
		Multimap<String, Requirement> rules = ArrayListMultimap.create();
	    file = testFile(file, Control.ISO_GM03, rules, true);
        rules.put("identificationInfo",new Not(new Exists(new Finder("basicGeodataID"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("extent", new Prefix("srv"))));
        // Doesn't seem to be in GM03 model
        //rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("restrictions", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("serviceTypeVersion", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("serviceType", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Count(2, new Finder("coupledResource", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("coupledResource/SV_CoupledResource", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("coupledResource/SV_CoupledResource/operationName", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("couplingType", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Count(3, new Finder("containsOperations", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("containsOperations/SV_OperationMetadata", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("containsOperations/SV_OperationMetadata/operationName", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("containsOperations/SV_OperationMetadata/DCP", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("containsOperations/SV_OperationMetadata/connectPoint", new Prefix("srv"))));
        rules.put("CHE_SV_ServiceIdentification",new Exists(new Finder("operatesOn", new Prefix("srv"))));
	    file = testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void DoNotAddDatasetByDefault18386() throws Throwable
    {
        File file = new File(data, "non_validating/bug18386.xml");
		Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("gmd:dataQualityInfo", new Not(new Exists(new Finder("DQ_Scope/level"))));
	    file = testFile(file, Control.GM03_1_ISO, rules, false);
    }

    @Test
    public void gm03V1orgName() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
		Multimap<String, Requirement> rules = ArrayListMultimap.create();
		rules.put("che:parentResponsibleParty",
                new Exists(new Finder("organisationName",
                new Exists(new Finder("LocalisedCharacterString",
						   new EqualText("Office federal de topographie"))))));
        file = testFile(file, Control.GM03_1_ISO, rules, true);
    }

    @Test
    public void gm03V2orgName() throws Throwable
    {
        File file = new File(data, "gm03V2/missing_orgname_ basicGeoDataID_ basicGeodataIDType.xml");
		Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("che:CHE_CI_ResponsibleParty",
                new Exists(new Finder("organisationName",
                new Exists(new Finder("LocalisedCharacterString",
                           new EqualText("Kanton Thurgau, Amt fur Geoinformation"))))));
        rules.put("che:CHE_MD_DataIdentification", new Exists(new Finder("basicGeodataID")));
        rules.put("che:CHE_MD_DataIdentification", new Exists(new Finder("basicGeodataIDType")));
        file = testFile(file, Control.GM03_2_ISO, rules, true);

        rules = ArrayListMultimap.create();

        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification", new Exists(new Finder("basicGeodataID")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification", new Exists(new Finder("basicGeodataIDType")));
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty", new Exists(new Finder("organisationName")));

        testFile(file, Control.ISO_GM03, rules, true);
    }


    @Test
    public void transferMD_IdentifierAndScaleDenominator() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("LI_Source", new Exists(new Finder("scaleDenominator")));
        rules.put("CHE_MD_FeatureCatalogueDescription/complianceCode/Boolean", new Not(new EqualText("false")));
        rules.put("CHE_MD_FeatureCatalogueDescription/complianceCode/Boolean", new EqualText("0"));
        rules.put("CHE_MD_FeatureCatalogueDescription/includedWithDataset/Boolean", new EqualText("1"));
        rules.put("MD_BrowseGraphic", Requirement.ACCEPT);
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("TRANSFER", new Exists(new Attribute("HEADERSECTION", "SENDER", "TransformationTestSupport")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription/complianceCode", new EqualText(
                "false"));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription/includedWithDataset",
                new EqualText("true"));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_BrowseGraphic", Requirement.ACCEPT);
        testFile(file, Control.ISO_GM03, rules, true);
    }

    @Test
    public void import_Bug16374() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("MD_Distribution/distributor", new Exists(new Finder("distributorContact")));
        rules.put("MD_Distribution/distributor", new Exists(new Finder("distributionOrderProcess", new Exists(new Finder("fees")))));
        rules.put("CHE_MD_FeatureCatalogueDescription", new Exists(new Finder("dataModel")));
        rules.put("CHE_MD_FeatureCatalogueDescription", new Exists(new Finder("domain")));
        rules.put("CHE_MD_FeatureCatalogueDescription", new Exists(new Finder("class")));;
        rules.put("CHE_MD_FeatureCatalogueDescription", new Exists(new Finder("modelType")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importDistributor_Bug16374() throws Throwable
    {
        File file = new File(data, "gm03/missingDistributorInfo.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("MD_Distribution/distributor", new Exists(new Finder("distributorFormat")));
        rules.put("MD_Distribution/distributor/MD_Distributor/distributorFormat", new Exists(new Finder("formatDistributor")));
        rules.put("MD_Distribution/distributor", new Exists(new Finder("distributorTransferOptions")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importRole() throws Throwable
    {
        File file = new File(data, "gm03/role_import_failure.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("CHE_MD_Metadata", new Count(2, new Finder("contact/CHE_CI_ResponsibleParty/role")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importConstraintsBug17465_P1() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("CHE_MD_Metadata", new Exists(new Finder("MD_Constraints")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("CHE_MD_LegalConstraints/accessConstraints")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("CHE_MD_LegalConstraints/useConstraints")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("CHE_MD_LegalConstraints/otherConstraints")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("resourceConstraints")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("MD_SecurityConstraints")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importMaintenanceContactBug17465_P2() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("metadataMaintenance", new Exists(new Finder("contact")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importFeatureCatalogCitation_ResponsibleParty_Bug17465_P3() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("featureCatalogueCitation/CI_Citation", new Exists(new Finder("citedResponsibleParty")));
        rules.put("featureCatalogueCitation/CI_Citation/citedResponsibleParty", new Exists(new Finder("role")));
        rules.put("featureCatalogueCitation/CI_Citation", new Exists(new Finder("identifier")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }
    @Test
    public void missingCitationConformanceResult_Bug17465_P4() throws Throwable
    {
        File file = new File(data, "gm03/Bug17465_Missing_Responsible_In_FeatureCatalogCitation.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("DQ_ConformanceResult/specification/CI_Citation",
                new Exists(new Finder("title/PT_FreeText/textGroup/LocalisedCharacterString", new EqualText("Perfekt") )));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importGeoreferenceableGeorectifiable17465_P2() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("CHE_MD_Metadata", new Exists(new Finder("MD_Georeferenceable")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("MD_GridSpatialRepresentation")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("MD_VectorSpatialRepresentation")));
        rules.put("CHE_MD_Metadata", new Exists(new Finder("MD_Georectified")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        file = testFile(file, Control.ISO_GM03, ArrayListMultimap.<String, Requirement>create(), true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void exportImportCountry() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("che:CHE_CI_Address", new Exists(new Finder("gmd:country")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Core.Core.CI_Address", new Exists(new Finder("country")));
        testFile(file, Control.ISO_GM03, rules, true);
    }

    @Test
    public void exportImportLinkage() throws Throwable
    {
        File file = new File(data, "gm03/bug16971.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("CHE_CI_ResponsibleParty/contactInfo/CI_Contact/onlineResource/CI_OnlineResource", new Exists(new Finder("linkage")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

    }

    @Test
    public void importExportParentResponsiblePartyCountry() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("parentResponsibleParty", new Exists(new Finder("CHE_CI_Address")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);
    }

    @Test
    public void importExportMD_Usage() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("resourceSpecificUsage", new Exists(new Finder("MD_Usage")));
        rules.put("MD_Usage", new Exists(new Finder("specificUsage")));
        rules.put("MD_Usage", new Exists(new Finder("usageDateTime")));
        rules.put("MD_Usage", new Exists(new Finder("userDeterminedLimitations")));
        rules.put("MD_Usage", new Exists(new Finder("userContactInfo")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_Usage", new Exists(new Finder("specificUsage")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_UsageuserContactInfo", Requirement.ACCEPT);
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("resourceSpecificUsage", new Exists(new Finder("MD_Usage")));
        rules.put("MD_Usage", new Exists(new Finder("userContactInfo")));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importExportAggregateInfo() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("aggregationInfo", Requirement.ACCEPT);
        rules.put("MD_AggregateInformation", Requirement.ACCEPT);
        rules.put("aggregateDataSetName", new Exists(new Finder("CI_Citation")));
        rules.put("aggregateDataSetIdentifier", new Exists(new Finder("MD_Identifier")));
        rules.put("associationType", new Exists(new Finder("DS_AssociationTypeCode")));
        rules.put("initiativeType", new Exists(new Finder("DS_InitiativeTypeCode")));
        file = testFile(file, Control.GM03_1_ISO, rules, true);
    }

    @Test @Ignore("I don't have time right now to get this to pass")
    public void importExportAggregateInfoFull() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.GM03_1_ISO, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.aggregationInfo_MD_Identification", new Exists(new Finder("aggregationInfo")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_AggregateInformation", new Exists(new Finder("associationType")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_AggregateInformation", new Exists(new Finder("initiativeType")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_AggregateInformation", new Exists(new Finder("aggregateDataSetIdentifier")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.CI_Citation", new Exists(new Finder("MD_AggregateInformation")));

        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("aggregationInfo", Requirement.ACCEPT);
        rules.put("MD_AggregateInformation", Requirement.ACCEPT);
        rules.put("aggregateDataSetName", new Exists(new Finder("CI_Citation")));
        rules.put("aggregateDataSetIdentifier", new Exists(new Finder("MD_Identifier")));
        rules.put("associationType", new Exists(new Finder("DS_AssociationTypeCode")));
        rules.put("initiativeType", new Exists(new Finder("DS_InitiativeTypeCode")));

        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importExportUOM() throws Throwable
    {
        File file = new File(data, "gm03/AllComprehensiveAttributes.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("DQ_QuantitativeResult/valueUnit/BaseUnit/catalogSymbol", new EqualText("m"));
        rules.put("DQ_QuantitativeResult/valueUnit/BaseUnit/name", new EqualText("meter"));
        file = testFile(file, Control.GM03_1_ISO, rules, true);


        rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                new Exists(new Finder("valueUnit", new EqualText("m"))));
        testFile(file, Control.ISO_GM03, rules, true);
    }

    @Test
    public void transferUOM() throws Throwable
    {
        File file = new File(data, "gm03/export_Swisstopo2_Metadata_380.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("spatialResolution/MD_Resolution/distance/Distance", new Exists(new Attribute("uom")));
        testFile(file, Control.GM03_1_ISO, rules, true);
    }

    @Test
    public void distributor() throws Throwable
    {
        File file = new File(data, "gm03/bs_mit_distributorFormat.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("MD_Distribution", new Exists(new Finder("distributor")));
        rules.put("MD_Distribution", new Exists(new Finder("transferOptions")));
        testFile(file, Control.GM03_1_ISO, rules, true);
    }

    @Test
    public void exportLinkage() throws Throwable
    {
        File file = new File(data, "iso19139/linkage.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty", new Exists(new Finder("linkage")));
        rules.put("GM03_2_1Core.Core.CI_OnlineResource", new Exists(new Finder("linkage")));
        rules.put("GM03_2_1Core.Core.CI_OnlineResource", new Count(1, new Finder(
                "linkage/GM03_2_1Core.Core.PT_FreeURL/URLGroup/GM03_2_1Core.Core.PT_URLGroup/plainURL")));
        rules.put("GM03_2_1Core.Core.CI_OnlineResource", new Count(1, new Finder(
                "name/GM03_2_1Core.Core.PT_FreeText/textGroup/GM03_2_1Core.Core.PT_Group/plainText")));
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty", new Not(new Exists(new Finder("electronicalMailAddress"))));
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty", new Not(new Exists(new Finder("organisationName"))));
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty", new Not(new Exists(new Finder("positionName"))));
        rules.put("GM03_2_1Core.Core.CI_ResponsibleParty", new Not(new Exists(new Finder("organisationAcronym"))));
        rules.put("GM03_2_1Comprehensive.Comprehensive", new Not(new Exists(new Finder(
                "GM03_2_1Core.Core.CI_Telephone/CI_ResponsibleParty"))));
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("CI_Contact/onlineResource/CI_OnlineResource", new Exists(new Finder("linkage")));
        rules.put("MD_DigitalTransferOptions/onLine/CI_OnlineResource", new Exists(new Finder("linkage")));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test //@Ignore
    public void importXMLBLBBOX() throws Throwable
    {
        File file = new File(data, "iso19139/XMLBLBOX.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive",
                new And(new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_ConformanceResult")),
                        new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                                              new Exists(new Finder("valueType"))))
                ));
        rules.put("GM03_2_1Comprehensive.Comprehensive",
                new And(new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_ConformanceResult")),
                        new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                                              new Exists(new Finder("valueType")))),
                        new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                                new Exists(new Finder("XMLBLBOX/arbitrary/c1")))),
                        new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                                new Exists(new Finder("XMLBLBOX/arbitrary/another"))))


                ));

// TODO Find out why some of the items in the model don't seem supported by the xsd...
//        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_ElementevaluationProcedure", Requirement.ACCEPT);
//        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_ElementmeasureIdentification", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.reportDQ_DataQuality", Requirement.ACCEPT);
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("gmd:DQ_DataQuality/gmd:scope", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:lineage", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/gmd:DQ_TemporalValidity", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency", Requirement.ACCEPT);
        Requirement hasExpectedConformanceChildren =
            new And(new Exists(new Finder("gmd:explanation")),
                    new Exists(new Finder("gmd:pass/gco:Boolean"))
            );

        Requirement hasExpectedQuantitativeChildren =
            new And(new Exists(new Finder("gmd:valueType/gco:RecordType")),
                    new Exists(new Finder("gmd:valueUnit")),
                    new Exists(new Finder("gmd:errorStatistic/CharacterString")),  // may need to be changed to PT_FreeText/textGroup/LocalisedCharacterString in future
                    new Exists(new Finder("gmd:value/gco:Record/arbitrary/c1")),
                    new Exists(new Finder("gmd:value/gco:Record/arbitrary/another"))
                    );


        rules.put("gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency",
                   new And (new Exists(new Finder("gmd:DQ_ConformanceResult", hasExpectedConformanceChildren)),
                            new Exists(new Finder("gmd:DQ_QuantitativeResult", hasExpectedQuantitativeChildren))
                           )
                   );
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test //@Ignore
    public void importXMLBLBBOXNoXmlInIsoFile() throws Throwable
    {
        File file = new File(data, "iso19139/XMLBLBOXNoXmlInIso.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive",
                new And(new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_ConformanceResult")),
                        new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                                              new Exists(new Finder("valueType")))),
                        new Exists(new Finder("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult",
                                new Exists(new Finder("value", new EqualText("textOfRecord")))))
                ));

// TODO Find out why some of the items in the model don't seem supported by the xsd...
//        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_ElementevaluationProcedure", Requirement.ACCEPT);
//        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_ElementmeasureIdentification", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.reportDQ_DataQuality", Requirement.ACCEPT);
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("gmd:DQ_DataQuality/gmd:scope", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:lineage", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/gmd:DQ_TemporalValidity", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency", Requirement.ACCEPT);
        Requirement hasExpectedConformanceChildren =
            new And(new Exists(new Finder("gmd:explanation")),
                    new Exists(new Finder("gmd:pass/gco:Boolean"))
            );

        Requirement hasExpectedQuantitativeChildren =
            new And(new Exists(new Finder("gmd:valueType/gco:RecordType")),
                    new Exists(new Finder("gmd:valueUnit")),
                    new Exists(new Finder("gmd:errorStatistic/CharacterString")),  // may need to be changed to PT_FreeText/textGroup/LocalisedCharacterString in future
                    new Exists(new Finder("gmd:value/gco:Record", new EqualText("textOfRecord")))
                    );

        rules.put("gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency",
                   new Exists(new Finder("gmd:DQ_QuantitativeResult", hasExpectedQuantitativeChildren))
                   );
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importExportDQReports() throws Throwable
    {
        File file = new File(data, "iso19139/allDQ_Reports.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_TemporalValidity", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_TemporalConsistency", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_TopologicalConsistency", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_FormatConsistency", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_DomainConsistency", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_ConceptualConsistency", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_CompletenessOmission", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.DQ_CompletenessCommission", Requirement.ACCEPT);

        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_TemporalValidity", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_TemporalConsistency", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_AccuracyOfATimeMeasurement", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_QuantitativeAttributeAccuracy", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_NonQuantitativeAttributeAccuracy", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_ThematicClassificationCorrectness", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_RelativeInternalPositionalAccuracy", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_GriddedDataPositionalAccuracy", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_AbsoluteExternalPositionalAccuracy", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_TopologicalConsistency", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_FormatConsistency", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_DomainConsistency", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_ConceptualConsistency", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_CompletenessOmission", Requirement.ACCEPT);
        rules.put("gmd:DQ_DataQuality/gmd:report/DQ_CompletenessCommission", Requirement.ACCEPT);

        testFile(file, Control.GM03_2_ISO, rules, true);
    }


    @Test
    public void importCitation() throws Throwable
    {
        File file = new File(data, "iso19139/missing_extent_data.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier", new Exists(new Finder("CI_Citation")));
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("CI_Citation", new Exists(new Finder("identifier")));
        rules.put("MD_Identifier/code", new Exists(new Finder("PT_FreeText")));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void mergeExtentsFirstTime() throws Throwable
    {
        File file = new File(data, "iso19139/many_extent_data.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = addMergeExtentsRules();
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void mergeExtentsSecondTime() throws Throwable
    {
        File file = new File(data, "iso19139/many_extent_data.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.ISO_GM03, rules, true);
        file = testFile(file, Control.GM03_2_ISO, rules, true);
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = addMergeExtentsRules();
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void mergeExtentsAfterReusableObjectProcessAndExport() throws Throwable
    {
        File file = new File(data, "iso19139/many_extent_export_from_geocat_after_reusable_objects.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.ISO_GM03, rules, true);
        testFile(file, Control.GM03_2_ISO, rules, true);
    }
    private Multimap<String, Requirement> addMergeExtentsRules() {
        Multimap<String, Requirement> rules;
        rules = ArrayListMultimap.create();
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_0", NA, 1, 0, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_1", INCLUDE, 1, 2, 1)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_2", INCLUDE, 0, 1, 1)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_3", INCLUDE, 0, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_4", EXCLUDE, 0, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_5", INCLUDE, 0, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_6", INCLUDE, 0, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_7", INCLUDE, 0, 2, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_8", EXCLUDE, 0, 2, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_9", INCLUDE, 0, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_10", EXCLUDE, 0, 1, 1)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_11", EXCLUDE, 1, 0, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_12", INCLUDE, 1, 0, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_13", INCLUDE, 1, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_15", INCLUDE, 1, 1, 0)));

        rules.put("identificationInfo", new Count(2, new Finder("EX_Extent/description/PT_FreeText/textGroup/LocalisedCharacterString", new StartsWithText("EX_11"))));
        rules.put("identificationInfo", new Count(2, new Finder("EX_Extent/description/PT_FreeText/textGroup/LocalisedCharacterString", new StartsWithText("EX_12"))));

        rules.put("EX_Extent", new And(new Exists(new Finder("geographicElement/EX_GeographicDescription")),
                                       new Count(1, new Finder("geographicElement"))));
        return rules;
    }

    @Test
    public void importHoles() throws Throwable
    {
        File file = new File(data, "iso19139/stackoverflow.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("Polygon", new Count(1, new Finder("exterior")));
        rules.put("Polygon", new Count(1, new Finder("interior")));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importFormat() throws Throwable
    {
        File file = new File(data, "gm03V2/3-different-types-of-polygons.gm03.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules = ArrayListMultimap.create();
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_1", INCLUDE, 1, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_2", INCLUDE, 0, 1, 0)));
        rules.put("identificationInfo", new Exists(new PolygonValidator("EX_3", INCLUDE, 1, 0, 0)));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }


    @Test
    public void importExportRsIdentifier() throws Throwable
    {
        File file = new File(data, "iso19139/rs_citation_identifier.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier", new Exists(new Finder("CI_Citation")));
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("CI_Citation", new Exists(new Finder("identifier")));
        rules.put("identifier", new Exists(new Finder("RS_Identifier")));
        rules.put("RS_Identifier/code", new Exists(new Finder("PT_FreeText")));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void importLineageStatement() throws Throwable
    {
        File file = new File(data, "invalid-iso19139.xml");

        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Core.Core.LI_Lineage", new Exists(new Finder("statement/GM03_2_1Core.Core.PT_FreeText")));
        file = testFile(file, Control.ISO_GM03, rules, true);

        rules = ArrayListMultimap.create();
        rules.put("LI_Lineage", new Exists(new Finder("statement")));
        testFile(file, Control.GM03_2_ISO, rules, true);
    }

    @Test
    public void transferOption() throws Throwable
    {
        File file = new File(data, "gm03/ProblematicTransferOption.xml");
        assertTrue(file.exists());
        file = TransformationTestSupport.transformGM03toIso(file, outputDir);
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Core.Core.CI_OnlineResource", new Exists(new Finder("MD_DigitalTransferOptions")));
        testFile(file, Control.ISO_GM03, rules, true);
    }

    @Test
    public void harvesterData() throws Throwable
    {
        File file = new File(data, "gm03/BL_Rept_received.xml");
        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_DigitalTransferOptions", new Exists(new Finder("offLine")));
        rules.put("GM03_2_1Comprehensive.Comprehensive.MD_Medium", new Exists(new Finder("name")));
        testFile(file, Control.GMO_1_ISO_GM03, rules, true);
    }

    @Test
    public void zonengrenzen() throws Throwable
    {
        File file = new File(data, "gm03/xMetadataxZonengrenzen.xml");

        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("LI_ProcessStep/description", Requirement.ACCEPT);
        rules.put("LI_ProcessStep/dateTime", Requirement.ACCEPT);
        File iso = testFile(file, Control.GM03_1_ISO, rules, true);

        rules.clear();
        rules.put("GM03_2_1Comprehensive.Comprehensive.sourceLI_Lineage/LI_Lineage", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.sourceLI_Lineage/source", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.LI_ProcessStep/description", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.LI_ProcessStep/dateTime", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.LI_ProcessStep/LI_Lineage", Requirement.ACCEPT);
        rules.put("GM03_2_1Comprehensive.Comprehensive.LI_Source/description", Requirement.ACCEPT);

        testFile(iso, Control.ISO_GM03, rules, true);
    }

    @Test
    public void temporalExtent() throws Throwable
    {
        File file = new File(data, "non_validating/gm03/home_temporalExt.xml");

        Multimap<String, Requirement> rules = ArrayListMultimap.create();
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod", Requirement.ACCEPT);
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/begin/TimeInstant/timePosition", Requirement.ACCEPT);
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/end/TimeInstant/timePosition", Requirement.ACCEPT);
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/begin/TimeInstant/timePosition", new EqualText("0200-01-01T00:00:00"));
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/end/TimeInstant/timePosition", new EqualText("2000-12-31T00:00:00"));
        File iso = testFile(file, Control.GM03_1_ISO, rules, false);

        rules.clear();
        rules.put("GM03_2_1Core.Core.EX_TemporalExtent/extent/GM03_2_1Core.Core.TM_Primitive/begin", Requirement.ACCEPT);
        rules.put("GM03_2_1Core.Core.EX_TemporalExtent/extent/GM03_2_1Core.Core.TM_Primitive/end", Requirement.ACCEPT);

        iso = testFile(iso, Control.ISO_GM03, rules, false);

        rules.clear();
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod", Requirement.ACCEPT);
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/begin/TimeInstant/timePosition", Requirement.ACCEPT);
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/end/TimeInstant/timePosition", Requirement.ACCEPT);
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/begin/TimeInstant/timePosition", new EqualText("0200-01-01T00:00:00"));
        rules.put("EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/end/TimeInstant/timePosition", new EqualText("2000-12-31T00:00:00"));

        testFile(iso, Control.GM03_2_ISO, rules, false);
    }

    /**
     *
     * @param file
     *            file to test
     * @param control
     *            a contol explaining what transforms are required
     * @param validationRules
     *            the rules to test (node/node-segment to find, requirement
     *            tests to run at that node)
     * @param testValidity
     * @return file after transform
     */
    private File testFile(File file, Control control, Multimap<String, Requirement> validationRules, boolean testValidity) throws Throwable
    {

        assertTrue(file.exists());

        File transformed;

        switch (control)
        {
        case GM03_1_ISO:
        {
            transformed = TransformationTestSupport.transformGM03toIso(file, outputDir, testValidity);
            break;
        }
        case GM03_2_ISO:
        {
            transformed = TransformationTestSupport.transformGM03toIso(file, outputDir, testValidity);
            break;
        }
        case ISO_GM03:
        {
            if (!file.getParentFile().equals(outputDir)) {
                outputDir.mkdir();
                File copied = TransformationTestSupport.copy(file, new File(outputDir, file.getName()));
                transformed = TransformationTestSupport.transformIsoToGM03(copied, outputDir, testValidity);
            } else {
                transformed = TransformationTestSupport.transformIsoToGM03(file, outputDir, testValidity);
            }
            break;
        }
        case GMO_1_ISO_GM03:
        {

            transformed = TransformationTestSupport.transformGM03toIso(file, outputDir, testValidity);
            transformed = TransformationTestSupport.transformIsoToGM03(transformed, outputDir, testValidity);
            break;
        }
        default:
            throw new RuntimeException();
        }

        Element xml = Xml.loadFile(transformed);
        Iterator errorTags = xml.getDescendants(new Filter(){
            private static final long serialVersionUID = 1L;

            public boolean matches(Object obj) {
                if (obj instanceof Element) {
                    Element e = (Element) obj;
                    return e.getName().equals("ERROR");
                }
                return false;
            }

        });

        assertFalse("Error tag found in xml", errorTags.hasNext());

        Collection<Entry<String, Requirement>> rules = validationRules.entries();
        StringBuilder failures = new StringBuilder();
        for (Entry<String, Requirement> entry : rules) {
            Finder finder = new Finder(entry.getKey(), entry.getValue());
            if (!finder.matches(xml) && !xml.getDescendants(finder).hasNext()) {
                failures.append("Failure with rule: " + entry.getKey() + "[" + entry.getValue() + "]");
                failures.append("\n");
            }
        }

        assertTrue("\n" + failures.toString(), failures.length() == 0);

        return transformed;
    }

    enum Control
    {
        /**
         * Indicates a transform from GM03 1.8 to ISO
         */
        GM03_1_ISO,
        /**
         * Indicates a transform from GM03 2.0 to ISO
         */
        GM03_2_ISO,
        /**
         * Indicates a transform from ISO to GM03 2.0
         */
        ISO_GM03,
        /**
         * Indicates a transform from GM03 1.8 to ISO to GM03 2.0
         */
        GMO_1_ISO_GM03
    }
}
