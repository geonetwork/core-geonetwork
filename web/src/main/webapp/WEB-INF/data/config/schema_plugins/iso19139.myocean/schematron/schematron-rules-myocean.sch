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
            
            <!-- Check title -->
            <sch:let name="title" value="gmd:citation/gmd:CI_Citation/gmd:title"/>
            <sch:assert test="normalize-space($title) != ''"
                >$loc/strings/alert.R1</sch:assert>
            <sch:report test="normalize-space($title) != ''"
                ><sch:value-of select="$loc/strings/report.R1"/> "<sch:value-of select="normalize-space($title)"/>"</sch:report>
            
            <!-- Check internal permanent shortname -->            
            <sch:let name="interShortname" value="gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code"/>
            <sch:assert test="normalize-space($interShortname) != ''"
                >$loc/strings/alert.R2</sch:assert>
            <sch:report test="normalize-space($interShortname) != ''"
                ><sch:value-of select="$loc/strings/report.R2"/> "<sch:value-of select="normalize-space($interShortname)"/>"</sch:report>
            
            <!-- Check alternate title (external shortname) -->
            <sch:let name="altTitle" value="gmd:citation/gmd:CI_Citation/gmd:alternateTitle"/>
            <sch:assert test="normalize-space($altTitle) != ''"
                >$loc/strings/alert.R3</sch:assert>
            <sch:report test="normalize-space($altTitle) != ''"
                ><sch:value-of select="$loc/strings/report.R3"/> "<sch:value-of select="normalize-space($altTitle)"/>"</sch:report>
            
            <!-- Check creation date -->           
            <sch:let name="creationDate" value="gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date"/>
            <sch:assert test="normalize-space($creationDate) != ''"
                >$loc/strings/alert.R4</sch:assert>
            <sch:report test="normalize-space($creationDate) != ''"
                ><sch:value-of select="$loc/strings/report.R4"/> "<sch:value-of select="normalize-space($creationDate)"/>"</sch:report>
            
            <!-- Check edition date -->           
            <sch:let name="updateDate" value="gmd:citation/gmd:CI_Citation/gmd:editionDate"/>
            <sch:assert test="normalize-space($updateDate) != ''"
                >$loc/strings/alert.R5</sch:assert>
            <sch:report test="normalize-space($updateDate) != ''"
                ><sch:value-of select="$loc/strings/report.R5"/> "<sch:value-of select="normalize-space($updateDate)"/>"</sch:report>
            
             <!-- Check abstract -->
            <sch:let name="abstract" value="gmd:abstract"/>
            <sch:assert test="normalize-space($abstract) != ''"
                >$loc/strings/alert.R6</sch:assert>
            <sch:report test="normalize-space($abstract) != ''"
                ><sch:value-of select="$loc/strings/report.R6"/> "<sch:value-of select="normalize-space($abstract)"/>"</sch:report>
            
             <!-- Check overview -->
            <sch:let name="overview" value="gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName"/>
            <sch:assert test="normalize-space($overview) != ''"
                >$loc/strings/alert.R7</sch:assert>
            <sch:report test="normalize-space($overview) != ''"
                ><sch:value-of select="$loc/strings/report.R7"/> "<sch:value-of select="normalize-space($overview)"/>"</sch:report>
            
            <!-- Check Mission type -->
            <sch:let name="mission" value="gmd:descriptiveKeywords[2]/gmd:MD_Keywords[1]/gmd:keyword[1]"/>
            <sch:assert test="normalize-space($mission) != ''"
                >$loc/strings/alert.R8</sch:assert>
            <sch:report test="normalize-space($mission) != ''"
                ><sch:value-of select="$loc/strings/report.R8"/> "<sch:value-of select="normalize-space($mission)"/>"</sch:report>
            
            <!-- Check area of benefit -->
            <sch:let name="benefit" value="count(gmd:descriptiveKeywords[6]/gmd:MD_Keywords[1]/gmd:keyword)"/>
            <sch:assert test="$benefit > 0">$loc/strings/alert.R9</sch:assert>
            <sch:report test="$benefit > 0">
            	<sch:value-of select="$loc/strings/report.R9"/> "<sch:value-of select="gmd:descriptiveKeywords[6]/gmd:MD_Keywords[1]/gmd:keyword"/>"
            </sch:report>
            
            <!-- Check ocean variables -->
            <sch:let name="ocean" value="count(gmd:descriptiveKeywords[4]/gmd:MD_Keywords[1]/gmd:keyword)"/>
            <sch:assert test="$ocean > 0">$loc/strings/alert.R10</sch:assert>
            <sch:report test="$ocean > 0">
            	<sch:value-of select="$loc/strings/report.R10"/> "<sch:value-of select="gmd:descriptiveKeywords[4]/gmd:MD_Keywords[1]/gmd:keyword"/>"
            </sch:report>
            
            <!-- Check display priority -->
            <sch:let name="credit" value="gmd:credit"/>
            <sch:assert test="normalize-space($credit) != ''">$loc/strings/alert.R11</sch:assert>
            <sch:report test="normalize-space($credit) != ''">
            	<sch:value-of select="$loc/strings/report.R11"/> "<sch:value-of select="normalize-space($credit)"/>"
            </sch:report>
            
            <!-- Check BBox -->
            <sch:let name="west" value="gmd:extent[1]//gmd:westBoundLongitude"/>
            <sch:let name="east" value="gmd:extent[1]//gmd:eastBoundLongitude"/>
            <sch:let name="north" value="gmd:extent[1]//gmd:northBoundLatitude"/>
            <sch:let name="south" value="gmd:extent[1]//gmd:southBoundLatitude"/>
            <sch:assert test="normalize-space($west) != '' and normalize-space($east) != '' and
            	normalize-space($north) != '' and normalize-space($south) != ''">$loc/strings/alert.R12
            </sch:assert>
            <sch:report test="normalize-space($west) != '' and normalize-space($east) != '' and
            	normalize-space($north) != '' and normalize-space($south) != ''">
            	<sch:value-of select="$loc/strings/report.R12"/> "
            		<sch:value-of select="normalize-space($west)"/>
	            	<sch:value-of select="normalize-space($east)"/>
	            	<sch:value-of select="normalize-space($north)"/>
	            	<sch:value-of select="normalize-space($south)"/>"
            </sch:report>
            
            <!-- Check reference area -->
            <sch:let name="area" value="gmd:descriptiveKeywords[7]/gmd:MD_Keywords/gmd:keyword"/>
            <sch:assert test="normalize-space($area) != ''">$loc/strings/alert.R13</sch:assert>
            <sch:report test="normalize-space($area) != ''">
            	<sch:value-of select="$loc/strings/report.R13"/> "<sch:value-of select="normalize-space($area)"/>"
            </sch:report>
            
            <!-- Check min/max level -->
            <sch:let name="minValue" value="gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue"/>
            <sch:let name="maxValue" value="gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue"/>
            <sch:assert test="normalize-space($minValue) != '' and normalize-space($maxValue) != ''">$loc/strings/alert.R14</sch:assert>
            <sch:report test="normalize-space($minValue) != '' and normalize-space($maxValue) != ''">
            	<sch:value-of select="$loc/strings/report.R14"/> "
            	<sch:value-of select="normalize-space(concat(concat($minValue,' - '), $maxValue))"/>"
            </sch:report>
            
            <!-- Check start date -->
            <sch:let name="startDate" value="gmd:extent/gmd:EX_Extent/gmd:temporalElement[1]/gmd:EX_TemporalExtent[1]/gmd:extent[1]/gml:TimePeriod/gml:beginPosition"/>
            <sch:assert test="normalize-space($startDate) != ''">$loc/strings/alert.R15</sch:assert>
            <sch:report test="normalize-space($startDate) != ''">
            	<sch:value-of select="$loc/strings/report.R15"/> "<sch:value-of select="normalize-space($startDate)"/>"
            </sch:report>
            
            <!-- Check temporal scale -->
            <sch:let name="scale" value="gmd:descriptiveKeywords[5]/gmd:MD_Keywords[1]/gmd:keyword[1]"/>
            <sch:assert test="normalize-space($scale) != ''">$loc/strings/alert.R16</sch:assert>
            <sch:report test="normalize-space($scale) != ''">
            	<sch:value-of select="$loc/strings/report.R16"/> "<sch:value-of select="normalize-space($scale)"/>"
            </sch:report>
            
            <!-- Check processing level -->
            <sch:let name="level" value="gmd:descriptiveKeywords[8]/gmd:MD_Keywords[1]/gmd:keyword[1]"/>
            <sch:assert test="normalize-space($level) != ''">$loc/strings/alert.R17</sch:assert>
            <sch:report test="normalize-space($level) != ''">
            	<sch:value-of select="$loc/strings/report.R17"/> "<sch:value-of select="normalize-space($level)"/>"
            </sch:report>
            
            <!-- Check update frequency -->
            <sch:let name="freq" value="gmd:resourceMaintenance[1]/gmd:MD_MaintenanceInformation[1]/gmd:maintenanceAndUpdateFrequency[1]/gmd:MD_MaintenanceFrequencyCode[1]/@codeListValue"/>
            <sch:assert test="normalize-space($freq) != ''">$loc/strings/alert.R18</sch:assert>
            <sch:report test="normalize-space($freq) != ''">
            	<sch:value-of select="$loc/strings/report.R18"/> "<sch:value-of select="normalize-space($freq)"/>"
            </sch:report>
            
            <!-- Check start period -->
            <sch:let name="period" value="gmd:resourceMaintenance[1]/gmd:MD_MaintenanceInformation[1]/gmd:updateScopeDescription[1]/gmd:MD_ScopeDescription[1]/gmd:other"/>
            <sch:assert test="normalize-space($period) != ''">$loc/strings/alert.R19</sch:assert>
            <sch:report test="normalize-space($period) != ''">
            	<sch:value-of select="$loc/strings/report.R19"/> "<sch:value-of select="normalize-space($period)"/>"
            </sch:report>
            
            <!-- Check production center -->
            <sch:let name="center" value="gmd:pointOfContact[3]/gmd:CI_ResponsibleParty[1]/gmd:organisationName[1]"/>
            <sch:assert test="normalize-space($center) != ''">$loc/strings/alert.R20</sch:assert>
            <sch:report test="normalize-space($center) != ''">
            	<sch:value-of select="$loc/strings/report.R20"/> "<sch:value-of select="normalize-space($center)"/>"
            </sch:report>

            <!-- Check local service desk -->
            <sch:let name="desk" value="gmd:pointOfContact[1]/gmd:CI_ResponsibleParty[1]/gmd:contactInfo[1]/gmd:CI_Contact[1]/gmd:address[1]/gmd:CI_Address[1]/gmd:electronicMailAddress[1]"/>
            <sch:assert test="normalize-space($desk) != ''">$loc/strings/alert.R22</sch:assert>
            <sch:report test="normalize-space($desk) != ''">
            	<sch:value-of select="$loc/strings/report.R22"/> "<sch:value-of select="normalize-space($desk)"/>"
            </sch:report>
         </sch:rule>
         
        <sch:rule
            context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
            
            <!-- Check features type -->
            <sch:let name="feature" value="count(gmd:contentInfo[2]/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes)"/>
            <sch:assert test="$feature > 0">$loc/strings/alert.R23</sch:assert>
            <sch:report test="$feature > 0">
            	<sch:value-of select="$loc/strings/report.R23"/> "<sch:value-of select="gmd:contentInfo[2]/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes/gco:LocalName"/>"
            </sch:report>
            
            <!-- Check Coordinate reference system -->
            <sch:let name="coord" value="gmd:referenceSystemInfo[1]/gmd:MD_ReferenceSystem[1]/gmd:referenceSystemIdentifier[1]/gmd:RS_Identifier[1]/gmd:code"/>
            <sch:assert test="normalize-space($coord) != ''">$loc/strings/alert.R24</sch:assert>
            <sch:report test="normalize-space($coord) != ''">
            	<sch:value-of select="$loc/strings/report.R24"/> "<sch:value-of select="normalize-space($coord)"/>"
            </sch:report>
            
            <!-- Check Number of vertical levels -->
            <sch:let name="vert" value="gmd:contentInfo[1]/gmd:MD_CoverageDescription[1]/gmd:dimension[2]/gmd:MD_RangeDimension[1]/gmd:descriptor"/>
            <sch:assert test="normalize-space($vert) != ''">$loc/strings/alert.R25</sch:assert>
            <sch:report test="normalize-space($vert) != ''">
            	<sch:value-of select="$loc/strings/report.R25"/> "<sch:value-of select="normalize-space($vert)"/>"
            </sch:report>
            
            <!-- Check Temporal resolution -->
            <sch:let name="resolu" value="gmd:contentInfo[1]/gmd:MD_CoverageDescription[1]/gmd:dimension[1]/gmd:MD_RangeDimension[1]/gmd:descriptor"/>
            <sch:assert test="normalize-space($resolu) != ''">$loc/strings/alert.R26</sch:assert>
            <sch:report test="normalize-space($resolu) != ''">
            	<sch:value-of select="$loc/strings/report.R26"/> "<sch:value-of select="normalize-space($resolu)"/>"
            </sch:report>
            
            <!-- Check product manager -->
            <sch:let name="manager" value="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor[1]/gmd:MD_Distributor[1]/gmd:distributorContact[1]/gmd:CI_ResponsibleParty[1]/gmd:organisationName"/>
            <sch:assert test="normalize-space($manager) != ''">$loc/strings/alert.R21</sch:assert>
            <sch:report test="normalize-space($manager) != ''">
            	<sch:value-of select="$loc/strings/report.R21"/> "<sch:value-of select="$manager"/>"
            </sch:report>
            
        </sch:rule>
    </sch:pattern>
</sch:schema>
