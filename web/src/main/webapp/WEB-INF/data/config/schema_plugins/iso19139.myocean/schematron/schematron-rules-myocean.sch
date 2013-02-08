<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">Schematron validation / MyOcean recommendations</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>    

    <!-- =============================================================
    MyOcean schematron rules:
    ============================================================= -->
    <sch:pattern>
        <sch:title>$loc/strings/R1</sch:title>
        <sch:rule
            context="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|
            //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification">
            
            <!--  All of the product form fields, except for "product version", "Spatial resolution", 
                "Online resources" and "other resources" are mandatory. It does not seems as of now 
                that the metadata validation in geonetwork checks these cardinalities. -->
            
            <!-- Check alternate title -->
            <sch:let name="altTitle" value="gmd:citation/gmd:CI_Citation/gmd:alternateTitle"/>
            <sch:assert test="normalize-space($altTitle) != ''"
                >$loc/strings/alert.R1</sch:assert>
            <sch:report test="normalize-space($altTitle) != ''"
                ><sch:value-of select="$loc/strings/report.R1"/> "<sch:value-of select="normalize-space($altTitle)"/>"</sch:report>
            
            
            <!-- To be continued ... -->
            
        </sch:rule>
    </sch:pattern>
</sch:schema>
