<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">Schematron validation / GeoNetwork recommendations</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>    

    <!-- =============================================================
    GeoNetwork schematron rules:
    ============================================================= -->
    <sch:pattern>
        <sch:title>$loc/strings/M500</sch:title>
        <sch:rule
            context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
        	<sch:let name="language" value="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue"/>
            <sch:let name="localeAndNoLanguage" value="not(gmd:locale and gmd:language/@gco:nilReason='missing')
                and not(gmd:locale and not(gmd:language))"/>
            <sch:let name="duplicateLanguage" value="not(gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue=$language)"/>
   
   
            <!--  Check that main language is not defined and gmd:locale element exist. -->
            <sch:assert test="$localeAndNoLanguage"
                >$loc/strings/alert.M500</sch:assert>
            <sch:report test="$localeAndNoLanguage"
                ><sch:value-of select="$loc/strings/report.M500"/> "<sch:value-of select="normalize-space($language)"/>"</sch:report>
            
    
            <!-- 
                * Check that main language is defined and does not exist in gmd:locale.
                * Do not declare a language twice in gmd:locale section.	
                This should not happen due to XSD error
                which is usually made before schematron validation:
                "The value 'XX' of attribute 'id' on element 'gmd:PT_Locale' is not valid with respect to its type, 'ID'. 
                (Element: gmd:PT_Locale with parent element: gmd:locale)"
            -->
            <sch:assert test="$duplicateLanguage"
                >$loc/strings/alert.M501</sch:assert>
            <sch:report test="$duplicateLanguage"
                >$loc/strings/report.M501</sch:report>
            
        </sch:rule>
    </sch:pattern>
</sch:schema>
