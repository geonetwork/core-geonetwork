<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
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
    =============================================================
    * Language:
    * Do not declare a language twice in gmd:locale section.	
    This should not happen due to XSD error
    which is usually made before schematron validation:
    "The value 'XX' of attribute 'id' on element 'gmd:PT_Locale' is not valid with respect to its type, 'ID'. 
    (Element: gmd:PT_Locale with parent element: gmd:locale)"
    * gmd:LocalisedCharacterString locale="#FR" id exist in gmd:locale.
    * Check that main language is not defined and gmd:locale element exist.		 
-->
    <sch:pattern name="$loc/strings/M500">
        <sch:rule
            context="//gmd:MD_Metadata/gmd:language|//*[@gco:isoType='gmd:MD_Metadata']/gmd:language">
            <sch:report test="../gmd:locale and @gco:nilReason='missing'"
                >$loc/strings/alert.M500</sch:report>
        </sch:rule>
    </sch:pattern>
    <!-- 
    * Check that main language is defined and does not exist in gmd:locale.
-->
    <sch:pattern name="$loc/strings/M501">
        <sch:rule
            context="//gmd:MD_Metadata/gmd:locale|//*[@gco:isoType='gmd:MD_Metadata']/gmd:locale">
            <sch:report
                test="gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue=../gmd:language/gco:CharacterString"
                >$loc/strings/alert.M501</sch:report>
        </sch:rule>
    </sch:pattern>
</sch:schema>
