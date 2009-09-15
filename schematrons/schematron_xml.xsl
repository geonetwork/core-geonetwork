<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="1.0"
                gml:dummy-for-xmlns=""
                gmd:dummy-for-xmlns=""
                srv:dummy-for-xmlns=""
                gco:dummy-for-xmlns=""
                geonet:dummy-for-xmlns=""
                xlink:dummy-for-xmlns="">
   <xsl:output method="xml"/>
   <xsl:param name="lang"/>
   <xsl:variable name="loc" select="document(concat('loc/', $lang, '/schematron.xml'))"/>
   <xsl:template match="*|@*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:if test="count(. | ../@*) = count(../@*)">@</xsl:if>
      <xsl:value-of select="name()"/>
      <xsl:text>[</xsl:text>
      <xsl:value-of select="1+count(preceding-sibling::*[name()=name(current())])"/>
      <xsl:text>]</xsl:text>
   </xsl:template>
   <xsl:template match="/">
      <geonet:schematronerrors>
         <xsl:apply-templates select="/" mode="M7"/>
         <xsl:apply-templates select="/" mode="M8"/>
         <xsl:apply-templates select="/" mode="M9"/>
         <xsl:apply-templates select="/" mode="M10"/>
         <xsl:apply-templates select="/" mode="M11"/>
         <xsl:apply-templates select="/" mode="M12"/>
         <xsl:apply-templates select="/" mode="M13"/>
         <xsl:apply-templates select="/" mode="M14"/>
         <xsl:apply-templates select="/" mode="M15"/>
         <xsl:apply-templates select="/" mode="M16"/>
         <xsl:apply-templates select="/" mode="M17"/>
         <xsl:apply-templates select="/" mode="M18"/>
         <xsl:apply-templates select="/" mode="M19"/>
         <xsl:apply-templates select="/" mode="M20"/>
         <xsl:apply-templates select="/" mode="M21"/>
         <xsl:apply-templates select="/" mode="M22"/>
         <xsl:apply-templates select="/" mode="M23"/>
         <xsl:apply-templates select="/" mode="M24"/>
         <xsl:apply-templates select="/" mode="M25"/>
         <xsl:apply-templates select="/" mode="M26"/>
         <xsl:apply-templates select="/" mode="M27"/>
         <xsl:apply-templates select="/" mode="M28"/>
         <xsl:apply-templates select="/" mode="M29"/>
         <xsl:apply-templates select="/" mode="M30"/>
         <xsl:apply-templates select="/" mode="M31"/>
         <xsl:apply-templates select="/" mode="M32"/>
         <xsl:apply-templates select="/" mode="M33"/>
      </geonet:schematronerrors>
   </xsl:template>
   <xsl:template match="*[gco:CharacterString]" priority="4000" mode="M7">
      <xsl:if test="(normalize-space(gco:CharacterString) = '') and (not(@gco:nilReason) or not(contains('inapplicable missing template unknown withheld',@gco:nilReason)))">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M6.characterString"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M7"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M7"/>
   <xsl:template match="//gml:DirectPositionType" priority="4000" mode="M8">
      <xsl:if test="not(@srsDimension) or @srsName">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M6.directPosition"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:if test="not(@axisLabels) or @srsName">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M7.axisAndSrs"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:if test="not(@uomLabels) or @srsName">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M7.uomAndSrs"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:if test="(not(@uomLabels) and not(@axisLabels)) or (@uomLabels and @axisLabels)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M7.uomAndAxis"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M8"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M8"/>
   <xsl:template match="//gmd:CI_ResponsibleParty" priority="4000" mode="M9">
      <xsl:choose>
         <xsl:when test="(count(gmd:individualName) + count(gmd:organisationName) + count(gmd:positionName)) &gt; 0"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M8"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M9"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M9"/>
   <xsl:template match="//gmd:MD_LegalConstraints" priority="4000" mode="M10">
      <xsl:if test="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions' and not(gmd:otherConstraints)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M8.access"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:if test="gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions' and not(gmd:otherConstraints)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M8.use"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M10"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M10"/>
   <xsl:template match="//gmd:MD_Band" priority="4000" mode="M11">
      <xsl:if test="(gmd:maxValue or gmd:minValue) and not(gmd:units)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M9"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M11"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M11"/>
   <xsl:template match="//gmd:LI_Source" priority="4000" mode="M12">
      <xsl:choose>
         <xsl:when test="gmd:description or gmd:sourceExtent"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M11"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M12"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M12"/>
   <xsl:template match="//gmd:LI_Source" priority="4000" mode="M13">
      <xsl:choose>
         <xsl:when test="gmd:description or gmd:sourceExtent"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M12"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M13"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M13"/>
   <xsl:template match="//gmd:DQ_DataQuality" priority="4000" mode="M14">
      <xsl:if test="(((count(*/gmd:LI_Lineage/gmd:source) + count(*/gmd:LI_Lineage/gmd:processStep)) = 0) and (gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' or gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='series')) and not(gmd:lineage/gmd:LI_Lineage/gmd:statement) and (gmd:lineage)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M13"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M14"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M14"/>
   <xsl:template match="//gmd:LI_Lineage" priority="4000" mode="M15">
      <xsl:if test="not(gmd:source) and not(gmd:statement) and not(gmd:processStep)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M14"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M15"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M15"/>
   <xsl:template match="//gmd:LI_Lineage" priority="4000" mode="M16">
      <xsl:if test="not(gmd:processStep) and not(gmd:statement) and not(gmd:source)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M15"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M16"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M16"/>
   <xsl:template match="//gmd:DQ_DataQuality" priority="4000" mode="M17">
      <xsl:if test="gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' and not(gmd:report) and not(gmd:lineage)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M16"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M17"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M17"/>
   <xsl:template match="//gmd:DQ_Scope" priority="4000" mode="M18">
      <xsl:choose>
         <xsl:when test="gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' or gmd:level/gmd:MD_ScopeCode/@codeListValue='series' or (gmd:levelDescription and ((normalize-space(gmd:levelDescription) != '') or (gmd:levelDescription/gmd:MD_ScopeDescription) or (gmd:levelDescription/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:levelDescription/@gco:nilReason))))"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M17"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M18"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M18"/>
   <xsl:template match="//gmd:MD_Medium" priority="4000" mode="M19">
      <xsl:if test="gmd:density and not(gmd:densityUnits)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M18"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M19"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M19"/>
   <xsl:template match="//gmd:MD_Distribution" priority="4000" mode="M20">
      <xsl:choose>
         <xsl:when test="count(gmd:distributionFormat)&gt;0 or count(gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat)&gt;0"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M19"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M20"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M20"/>
   <xsl:template match="//gmd:EX_Extent" priority="4000" mode="M21">
      <xsl:choose>
         <xsl:when test="count(gmd:description)&gt;0 or count(gmd:geographicElement)&gt;0 or count(gmd:temporalElement)&gt;0 or count(gmd:verticalElement)&gt;0"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M20"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M21"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M21"/>
   <xsl:template match="//*[gmd:identificationInfo/gmd:MD_DataIdentification]" priority="4000"
                 mode="M22">
      <xsl:if test="(not(gmd:hierarchyLevel) or gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset') and (count(//gmd:MD_DataIdentification/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox) + count (//gmd:MD_DataIdentification/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicDescription)) =0 ">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M21"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M22"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M22"/>
   <xsl:template match="//gmd:MD_DataIdentification" priority="4000" mode="M23">
      <xsl:if test="(not(../../gmd:hierarchyLevel) or (../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset') or (../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='series')) and (not(gmd:topicCategory))">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M6"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M23"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M23"/>
   <xsl:template match="//gmd:MD_AggregateInformation" priority="4000" mode="M24">
      <xsl:choose>
         <xsl:when test="gmd:aggregateDataSetName or gmd:aggregateDataSetIdentifier"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M22"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M24"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M24"/>
   <xsl:template match="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" priority="4000"
                 mode="M25">
      <xsl:choose>
         <xsl:when test="gmd:language and ((normalize-space(gmd:language) != '')  or (normalize-space(gmd:language/gco:CharacterString) != '') or (gmd:language/gmd:LanguageCode) or (gmd:language/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:language/@gco:nilReason)))"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M23"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M25"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M25"/>
   <xsl:template match="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" priority="4000"
                 mode="M26">
      <xsl:apply-templates mode="M26"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M26"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M27">
      <xsl:choose>
         <xsl:when test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:obligation and ((normalize-space(gmd:obligation) != '')  or (gmd:obligation/gmd:MD_ObligationCode) or (gmd:obligation/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:obligation/@gco:nilReason))))"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M26.obligation"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
         <xsl:when test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:maximumOccurrence and ((normalize-space(gmd:maximumOccurrence) != '')  or (normalize-space(gmd:maximumOccurrence/gco:CharacterString) != '') or (gmd:maximumOccurrence/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:maximumOccurrence/@gco:nilReason))))"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M26.minimumOccurence"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
         <xsl:when test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:domainValue and ((normalize-space(gmd:domainValue) != '')  or (normalize-space(gmd:domainValue/gco:CharacterString) != '') or (gmd:domainValue/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:domainValue/@gco:nilReason))))"/>
         <xsl:otherwise>
            <geonet:errorFound ref="#_{geonet:element/@ref}">
               <geonet:pattern name="{name(.)}"/>
               <geonet:diagnostics>
                  <xsl:value-of select="$loc/strings/alert.M26.domainValue"/>
               </geonet:diagnostics>
            </geonet:errorFound>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M27"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M27"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M28">
      <xsl:if test="gmd:obligation/gmd:MD_ObligationCode='conditional' and not(gmd:condition)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M27"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M28"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M28"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M29">
      <xsl:if test="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement' and not(gmd:domainCode)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M28"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M29"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M29"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M30">
      <xsl:if test="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue!='codelistElement' and not(gmd:shortName)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M29"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M30"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M30"/>
   <xsl:template match="//gmd:MD_Georectified" priority="4000" mode="M31">
      <xsl:if test="(gmd:checkPointAvailability/gco:Boolean='1' or gmd:checkPointAvailability/gco:Boolean='true') and not(gmd:checkPointDescription)">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M30"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M31"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M31"/>
   <xsl:template match="//gmd:MD_Metadata/gmd:language|//*[@gco:isoType='gmd:MD_Metadata']/gmd:language"
                 priority="4000"
                 mode="M32">
      <xsl:if test="../gmd:locale and @gco:nilReason='missing'">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M500"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M32"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M32"/>
   <xsl:template match="//gmd:MD_Metadata/gmd:locale|//*[@gco:isoType='gmd:MD_Metadata']/gmd:locale"
                 priority="4000"
                 mode="M33">
      <xsl:if test="gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue=../gmd:language/gco:CharacterString">
         <geonet:errorFound ref="#_{geonet:element/@ref}">
            <geonet:pattern name="{name(.)}"/>
            <geonet:diagnostics>
               <xsl:value-of select="$loc/strings/alert.M501"/>
            </geonet:diagnostics>
         </geonet:errorFound>
      </xsl:if>
      <xsl:apply-templates mode="M33"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M33"/>
   <xsl:template match="text()" priority="-1"/>
</xsl:stylesheet>