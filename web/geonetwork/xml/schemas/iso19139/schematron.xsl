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
   <xsl:output method="html"/>
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
      <html>
         <head>
            <title/>
            <link rel="stylesheet" type="text/css" href="../../geonetwork.css"/>
         </head>
         <body>
            <table width="100%" height="100%">
               <tr class="banner">
                  <td class="banner">
                     <img alt="GeoNetwork opensource" align="top" src="../../images/header-left.jpg"/>
                  </td>
                  <td align="right" class="banner">
                     <img alt="World picture" align="top" src="../../images/header-right.gif"/>
                  </td>
               </tr>
               <tr height="100%">
                  <td class="content" colspan="3">
                     <h1>
                        <xsl:value-of select="$loc/strings/title"/>
                     </h1>
                     <h2>
                        <xsl:attribute name="title">
                           <xsl:value-of select="$loc/strings/report.alt"/>
                        </xsl:attribute>
                        <xsl:value-of select="$loc/strings/report"/>
                     </h2>
                     <h3>
                        <xsl:value-of select="$loc/strings/M6"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M7"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M7"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M8"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M8"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M9"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M9"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M10"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M10"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M11"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M11"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M12"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M12"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M13"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M13"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M14"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M14"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M15"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M15"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M16"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M16"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M17"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M17"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M18"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M18"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M19"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M19"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M20"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M20"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M21"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M21"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M22"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M22"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M23"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M23"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M24"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M24"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M25"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M25"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M26"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M26"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M27"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M27"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M28"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M28"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M29"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M29"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M30"/>
                     <h3>
                        <xsl:value-of select="$loc/strings/M30"/>
                     </h3>
                     <xsl:apply-templates select="/" mode="M31"/>
                  </td>
               </tr>
            </table>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="*[gco:CharacterString]" priority="4000" mode="M7">
      <xsl:if test="(normalize-space(gco:CharacterString) = '') and (not(@gco:nilReason) or not(contains('inapplicable missing template unknown withheld',@gco:nilReason)))">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M6.characterString"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M7"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M7"/>
   <xsl:template match="//gml:DirectPositionType" priority="4000" mode="M8">
      <xsl:if test="not(@srsDimension) or @srsName">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M6.directPosition"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:if test="not(@axisLabels) or @srsName">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M7.axisAndSrs"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:if test="not(@uomLabels) or @srsName">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M7.uomAndSrs"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:if test="(not(@uomLabels) and not(@axisLabels)) or (@uomLabels and @axisLabels)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M7.uomAndAxis"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M8"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M8"/>
   <xsl:template match="//gmd:CI_ResponsibleParty" priority="4000" mode="M9">
      <xsl:choose>
         <xsl:when test="(count(gmd:individualName) + count(gmd:organisationName) + count(gmd:positionName)) &gt; 0"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M8"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M9"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M9"/>
   <xsl:template match="//gmd:MD_LegalConstraints" priority="4000" mode="M10">
      <xsl:if test="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions' and not(gmd:otherConstraints)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M8.access"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:if test="gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions' and not(gmd:otherConstraints)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M8.use"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M10"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M10"/>
   <xsl:template match="//gmd:MD_Band" priority="4000" mode="M11">
      <xsl:if test="(gmd:maxValue or gmd:minValue) and not(gmd:units)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M9"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M11"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M11"/>
   <xsl:template match="//gmd:LI_Source" priority="4000" mode="M12">
      <xsl:choose>
         <xsl:when test="gmd:description or gmd:sourceExtent"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M11"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M12"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M12"/>
   <xsl:template match="//gmd:LI_Source" priority="4000" mode="M13">
      <xsl:choose>
         <xsl:when test="gmd:description or gmd:sourceExtent"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M12"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M13"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M13"/>
   <xsl:template match="//gmd:DQ_DataQuality" priority="4000" mode="M14">
      <xsl:if test="(((count(*/gmd:LI_Lineage/gmd:source) + count(*/gmd:LI_Lineage/gmd:processStep)) = 0) and (gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' or gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='series')) and not(gmd:lineage/gmd:LI_Lineage/gmd:statement) and (gmd:lineage)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M13"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M14"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M14"/>
   <xsl:template match="//gmd:LI_Lineage" priority="4000" mode="M15">
      <xsl:if test="not(gmd:source) and not(gmd:statement) and not(gmd:processStep)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M14"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M15"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M15"/>
   <xsl:template match="//gmd:LI_Lineage" priority="4000" mode="M16">
      <xsl:if test="not(gmd:processStep) and not(gmd:statement) and not(gmd:source)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M15"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M16"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M16"/>
   <xsl:template match="//gmd:DQ_DataQuality" priority="4000" mode="M17">
      <xsl:if test="gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' and not(gmd:report) and not(gmd:lineage)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M16"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M17"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M17"/>
   <xsl:template match="//gmd:DQ_Scope" priority="4000" mode="M18">
      <xsl:choose>
         <xsl:when test="gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' or gmd:level/gmd:MD_ScopeCode/@codeListValue='series' or (gmd:levelDescription and ((normalize-space(gmd:levelDescription) != '') or (gmd:levelDescription/gmd:MD_ScopeDescription) or (gmd:levelDescription/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:levelDescription/@gco:nilReason))))"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M17"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M18"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M18"/>
   <xsl:template match="//gmd:MD_Medium" priority="4000" mode="M19">
      <xsl:if test="gmd:density and not(gmd:densityUnits)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M18"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M19"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M19"/>
   <xsl:template match="//gmd:MD_Distribution" priority="4000" mode="M20">
      <xsl:choose>
         <xsl:when test="count(gmd:distributionFormat)&gt;0 or count(gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat)&gt;0"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M19"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M20"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M20"/>
   <xsl:template match="//gmd:EX_Extent" priority="4000" mode="M21">
      <xsl:choose>
         <xsl:when test="count(gmd:description)&gt;0 or count(gmd:geographicElement)&gt;0 or count(gmd:temporalElement)&gt;0 or count(gmd:verticalElement)&gt;0"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M20"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M21"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M21"/>
   <xsl:template match="//*[gmd:identificationInfo/gmd:MD_DataIdentification]" priority="4000"
                 mode="M22">
      <xsl:if test="(not(gmd:hierarchyLevel) or gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset') and (count(//gmd:MD_DataIdentification/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox) + count (//gmd:MD_DataIdentification/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicDescription)) =0 ">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M21"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M22"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M22"/>
   <xsl:template match="//gmd:MD_DataIdentification" priority="4000" mode="M23">
      <xsl:if test="(not(../../gmd:hierarchyLevel) or (../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset') or (../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='series')) and (not(gmd:topicCategory))">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M6"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M23"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M23"/>
   <xsl:template match="//gmd:MD_AggregateInformation" priority="4000" mode="M24">
      <xsl:choose>
         <xsl:when test="gmd:aggregateDataSetName or gmd:aggregateDataSetIdentifier"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M22"/>
                  <b/>
               </a>
            </li>
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
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M23"/>
                  <b/>
               </a>
            </li>
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
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M26.obligation"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
         <xsl:when test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:maximumOccurrence and ((normalize-space(gmd:maximumOccurrence) != '')  or (normalize-space(gmd:maximumOccurrence/gco:CharacterString) != '') or (gmd:maximumOccurrence/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:maximumOccurrence/@gco:nilReason))))"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M26.minimumOccurence"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
         <xsl:when test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:domainValue and ((normalize-space(gmd:domainValue) != '')  or (normalize-space(gmd:domainValue/gco:CharacterString) != '') or (gmd:domainValue/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:domainValue/@gco:nilReason))))"/>
         <xsl:otherwise>
            <li>
               <a href="schematron-out.html#{generate-id(.)}" target="out"
                  title="Link to where this pattern was expected">
                  <xsl:value-of select="$loc/strings/alert.M26.domainValue"/>
                  <b/>
               </a>
            </li>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="M27"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M27"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M28">
      <xsl:if test="gmd:obligation/gmd:MD_ObligationCode='conditional' and not(gmd:condition)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M27"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M28"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M28"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M29">
      <xsl:if test="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement' and not(gmd:domainCode)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M28"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M29"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M29"/>
   <xsl:template match="//gmd:MD_ExtendedElementInformation" priority="4000" mode="M30">
      <xsl:if test="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue!='codelistElement' and not(gmd:shortName)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M29"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M30"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M30"/>
   <xsl:template match="//gmd:MD_Georectified" priority="4000" mode="M31">
      <xsl:if test="(gmd:checkPointAvailability/gco:Boolean='1' or gmd:checkPointAvailability/gco:Boolean='true') and not(gmd:checkPointDescription)">
         <li>
            <a href="schematron-out.html#{generate-id(.)}" target="out"
               title="Link to where this pattern was found">
               <xsl:value-of select="$loc/strings/alert.M30"/>
               <b/>
            </a>
         </li>
      </xsl:if>
      <xsl:apply-templates mode="M31"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M31"/>
   <xsl:template match="text()" priority="-1"/>
</xsl:stylesheet>
