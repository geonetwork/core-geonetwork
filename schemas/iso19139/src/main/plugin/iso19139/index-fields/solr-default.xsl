<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:gmd="http://www.isotc211.org/2005/gmd"
        xmlns:gmi="http://www.isotc211.org/2005/gmi"
        xmlns:gco="http://www.isotc211.org/2005/gco"
        xmlns:srv="http://www.isotc211.org/2005/srv"
        xmlns:gmx="http://www.isotc211.org/2005/gmx"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:solr="java:org.fao.geonet.solr.SolrUtils"
        xmlns:saxon="http://saxon.sf.net/"
        extension-element-prefixes="saxon"
        exclude-result-prefixes="#all"
        version="2.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <xsl:variable name="dateFormat" as="xs:string"
    select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z'"/>

  <xsl:variable name="separator" as="xs:string"
    select="'|'"/>

  <!--<xsl:import href="metadata-inspire-constant.xsl"/>-->


  <xsl:template match="/">
    <xsl:apply-templates mode="index"/>
  </xsl:template>


  <xsl:template match="gmi:MI_Metadata|gmd:MD_Metadata" mode="index">
    <!-- Main variables for the document -->
    <xsl:variable name="identifier" as="xs:string?"
            select="gmd:fileIdentifier/gco:CharacterString[. != '']"/>


    <xsl:variable name="mainLanguage" as="xs:string?"
            select="gmd:language/gco:CharacterString[normalize-space(.) != '']|
            gmd:language/gmd:LanguageCode/
            @codeListValue[normalize-space(.) != '']"/>

    <xsl:variable name="otherLanguages" as="attribute()*"
            select="gmd:locale/gmd:PT_Locale/
            gmd:languageCode/gmd:LanguageCode/
              @codeListValue[normalize-space(.) != '']"/>

    <!-- Record is dataset if no hierarchyLevel -->
    <xsl:variable name="isDataset" as="xs:boolean"
            select="
            count(gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='dataset']) > 0 or
            count(gmd:hierarchyLevel) = 0"/>
    <xsl:variable name="isService" as="xs:boolean"
            select="
            count(gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='service']) > 0"/>


    <doc>
      <field name="metadataIdentifier">
        <xsl:value-of select="$identifier"/>
      </field>

      <xsl:for-each select="gmd:metadataStandardName/gco:CharacterString">
        <field name="standardName">
          <xsl:value-of select="normalize-space(.)"/>
        </field>
      </xsl:for-each>


      <!-- Indexing record information -->
      <!-- # Date -->
      <!-- TODO improve date formatting maybe using Joda parser
      Select first one because some records have 2 dates !
      eg. fr-784237539-bdref20100101-0105
      -->
      <xsl:for-each select="gmd:dateStamp/*[text() != '' and position() = 1]">
        <field name="dateStamp">
          <xsl:value-of select="if (name() = 'gco:Date' and string-length(.) = 4)
                then concat(., '-01-01T00:00:00Z')
                else if (name() = 'gco:Date' and string-length(.) = 7)
                then concat(., '-01T00:00:00Z')
                else if (name() = 'gco:Date' or string-length(.) = 10)
                then concat(., 'T00:00:00Z')
                else (
                  if (ends-with(., 'Z'))
                  then .
                  else concat(., 'Z')
                )"/>
        </field>
      </xsl:for-each>


      <!-- # Languages -->
      <field name="mainLanguage">
        <xsl:value-of select="$mainLanguage"/>
      </field>

      <xsl:for-each select="$otherLanguages">
        <field name="otherLanguage">
          <xsl:value-of select="."/>
        </field>
      </xsl:for-each>


      <!-- # Resource type -->
      <xsl:choose>
        <xsl:when test="$isDataset">
          <field name="resourceType">dataset</field>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/
                @codeListValue[normalize-space(.) != '']">
            <field name="resourceType">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>


      <!-- Indexing metadata contact -->
      <xsl:apply-templates mode="index-contact" select="gmd:contact">
        <xsl:with-param name="fieldSuffix" select="''"/>
      </xsl:apply-templates>

      <!-- Indexing all codelist

      Indexing method is:
      <gmd:accessConstraints>
        <gmd:MD_RestrictionCode codeListValue="otherRestrictions"
        is indexed as
        codelist_accessConstraints:otherRestrictions

        Exclude some useless codelist like
        Contact role, Date type.
      -->
      <xsl:for-each select=".//*[@codeListValue != '' and
              name() != 'gmd:CI_RoleCode' and
              name() != 'gmd:CI_DateTypeCode' and
              name() != 'gmd:LanguageCode'
              ]">
        <field name="codelist_{local-name(..)}">
          <xsl:value-of select="@codeListValue"/>
        </field>
      </xsl:for-each>


      <!-- Indexing resource information
      TODO: Should we support multiple identification in the same record
      eg. nl db60a314-5583-437d-a2ff-1e59cc57704e
      Also avoid error when records contains multiple MD_IdentificationInfo
      or SRV_ServiceIdentification or a mix
      eg. de 8bb5334f-558b-982b-7b12-86ea486540d7
      -->
      <xsl:for-each select="gmd:identificationInfo[1]/*[1]">
        <xsl:for-each select="gmd:citation/gmd:CI_Citation">
          <field name="resourceTitle">
            <xsl:value-of select="normalize-space(gmd:title/gco:CharacterString/text())"/>
          </field>
          <field name="resourceAltTitle">
            <xsl:value-of select="normalize-space(gmd:alternateTitle/gco:CharacterString/text())"/>
          </field>

          <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:date/*/text() != '']">
            <xsl:variable name="dateType"
                    select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"
                    as="xs:string?"/>
            <xsl:variable name="date"
                    select="string(gmd:date/gco:Date|gmd:date/gco:DateTime)"/>
            <field name="{$dateType}DateForResource">
              <xsl:value-of select="$date"/>
            </field>
            <field name="{$dateType}YearForResource">
              <xsl:value-of select="substring($date, 0, 5)"/>
            </field>
            <field name="{$dateType}MonthForResource">
              <xsl:value-of select="substring($date, 0, 8)"/>
            </field>
          </xsl:for-each>

          <xsl:for-each
              select="gmd:presentationForm/gmd:CI_PresentationFormCode/@codeListValue[. != '']">
            <field name="presentationForm">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>
        </xsl:for-each>

        <field name="resourceAbstract">
          <xsl:value-of select="substring(
            normalize-space(gmd:abstract/gco:CharacterString/text()),
        0, $maxFieldLength)"/>
        </field>


        <!-- Indexing resource contact -->
        <xsl:apply-templates mode="index-contact"
                   select="gmd:pointOfContact">
          <xsl:with-param name="fieldSuffix" select="'ForResource'"/>
        </xsl:apply-templates>

        <xsl:for-each select="gmd:credit/*[. != '']">
          <field name="resourceCredit">
            <xsl:value-of select="."/>
          </field>
        </xsl:for-each>


        <xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic/
                gmd:fileName/gco:CharacterString[. != '']">
          <field name="overviewUrl">
            <xsl:value-of select="."/>
          </field>
        </xsl:for-each>

        <xsl:for-each
            select="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue">
          <field name="resourceLanguage">
            <xsl:value-of select="."/>
          </field>
        </xsl:for-each>


        <!-- TODO: create specific INSPIRE template or mode -->
        <!-- INSPIRE themes

        Select the first thesaurus title because some records
        may contains many even if invalid.

        Also get the first title at it may happen that a record
        have more than one.

        Select any thesaurus having the title containing "INSPIRE themes".
        Some records have "GEMET-INSPIRE themes" eg. sk:ee041534-b8f3-4683-b9dd-9544111a0712
        Some other "GEMET - INSPIRE themes"

        Take in account gmd:descriptiveKeywords or srv:keywords
        -->
        <!-- TODO: Some MS may be using a translated version of the thesaurus title -->
        <xsl:variable name="inspireThemeThesaurusTitle" select="'INSPIRE'"/>
        <!--<xsl:for-each
            select="*/gmd:MD_Keywords[contains(
           gmd:thesaurusName[1]/gmd:CI_Citation/
             gmd:title[1]/gco:CharacterString/text(),
             $inspireThemeThesaurusTitle)]
          /gmd:keyword/gco:CharacterString">

          <xsl:variable name="inspireTheme" as="xs:string"
                  select="solr:analyzeField('inspireTheme_syn', text())"/>

          <field name="inspireTheme_syn">
            <xsl:value-of select="text()"/>
          </field>
          <field name="inspireTheme">
            <xsl:value-of select="$inspireTheme"/>
          </field>
          <field name="inspireAnnex">
            <xsl:value-of
                select="solr:analyzeField('inspireAnnex_syn', $inspireTheme)"/>
          </field>
        </xsl:for-each>-->


        <!-- Index all keywords -->
        <xsl:for-each
            select="*/gmd:MD_Keywords/
              gmd:keyword/gco:CharacterString|
            */gmd:MD_Keywords/
              gmd:keyword/gmd:PT_FreeText/gmd:textGroup/
              gmd:LocalisedCharacterString">
          <field name="tag">
            <xsl:value-of select="text()"/>
          </field>
        </xsl:for-each>

        <!-- Index keywords which are of type place -->
        <xsl:for-each
            select="*/gmd:MD_Keywords/
              gmd:keyword[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'place']/
              gco:CharacterString|
            */gmd:MD_Keywords/
              gmd:keyword[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'place']/
              gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
          <field name="geotag">
            <xsl:value-of select="text()"/>
          </field>
        </xsl:for-each>


        <!-- Index all keywords having a specific thesaurus -->
        <xsl:for-each
            select="*/gmd:MD_Keywords[gmd:thesaurusName]/
              gmd:keyword">

          <xsl:variable name="thesaurusName"
                  select="../gmd:thesaurusName[1]/gmd:CI_Citation/
                  gmd:title[1]/gco:CharacterString"/>

          <xsl:variable name="thesaurusId"
                  select="normalize-space(../gmd:thesaurusName/gmd:CI_Citation/
                  gmd:identifier[position() = 1]/gmd:MD_Identifier/
                  gmd:code/*)"/>

          <xsl:variable name="key">
            <xsl:choose>
              <xsl:when test="$thesaurusId != ''">
                <xsl:value-of select="$thesaurusId"/>
              </xsl:when>
              <!-- Try to build a thesaurus key based on the name
              by removing space - to be improved. -->
              <xsl:when test="normalize-space($thesaurusName) != ''">
                <xsl:value-of select="replace($thesaurusName, ' ', '-')"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>

          <xsl:if test="normalize-space($key) != ''">
            <!-- Index keyword characterString including multilingual ones
             and element like gmx:Anchor including the href attribute
             which may contains keyword identifier. -->
            <xsl:for-each select="*[normalize-space() != '']|
                  */@xlink:href[normalize-space() != '']|
                  gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[normalize-space() != '']">
              <field name="thesaurus_{$key}">
                <xsl:value-of select="normalize-space(.)"/>
              </field>
            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>


        <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
          <field name="topic">
            <xsl:value-of select="."/>
          </field>
          <!-- TODO: Get translation ? -->
        </xsl:for-each>


        <xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
          <xsl:for-each
              select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer[. != '']">
            <field name="resolutionScaleDenominator">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>

          <xsl:for-each select="gmd:distance/gco:Distance[. != '']">
            <field name="resolutionDistance">
              <xsl:value-of select="concat(., ' ', @uom)"/>
            </field>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each
            select="gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue[. != '']">
          <field name="spatialRepresentationType">
            <xsl:value-of select="."/>
          </field>
        </xsl:for-each>


        <xsl:for-each select="gmd:resourceConstraints">
          <xsl:for-each
              select="*/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue[. != '']">
            <field name="accessConstraints">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>
          <xsl:for-each select="*/gmd:otherConstraints/gco:CharacterString[. != '']">
            <field name="otherConstraints">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>
          <xsl:for-each
              select="*/gmd:classification/gmd:MD_ClassificationCode/@codeListValue[. != '']">
            <field name="constraintClassification">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>
          <xsl:for-each select="*/gmd:useLimitation/gco:CharacterString[. != '']">
            <field name="useLimitation">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>
        </xsl:for-each>


        <xsl:for-each select="*/gmd:EX_Extent">

          <xsl:for-each select="gmd:geographicElement/gmd:EX_GeographicDescription/
      gmd:geographicIdentifier/gmd:MD_Identifier/
      gmd:code/gco:CharacterString[normalize-space(.) != '']">
            <field name="geoTag">
              <xsl:value-of select="."/>
            </field>
          </xsl:for-each>

          <!-- TODO: index bounding polygon -->
          <xsl:for-each select=".//gmd:EX_GeographicBoundingBox[
                ./gmd:westBoundLongitude/gco:Decimal castable as xs:decimal and
                ./gmd:eastBoundLongitude/gco:Decimal castable as xs:decimal and
                ./gmd:northBoundLatitude/gco:Decimal castable as xs:decimal and
                ./gmd:southBoundLatitude/gco:Decimal castable as xs:decimal
                ]">
            <xsl:variable name="format" select="'#0.000000'"></xsl:variable>

            <xsl:variable name="w"
                    select="format-number(./gmd:westBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="e"
                    select="format-number(./gmd:eastBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="n"
                    select="format-number(./gmd:northBoundLatitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="s"
                    select="format-number(./gmd:southBoundLatitude/gco:Decimal/text(), $format)"/>

            <!-- Example: ENVELOPE(-10, 20, 15, 10) which is minX, maxX, maxY, minY order
            http://wiki.apache.org/solr/SolrAdaptersForLuceneSpatial4
            https://cwiki.apache.org/confluence/display/solr/Spatial+Search

            bbox field type limited to one. TODO
            <xsl:if test="position() = 1">
              <field name="bbox">
              <xsl:text>ENVELOPE(</xsl:text>
              <xsl:value-of select="$w"/>
              <xsl:text>,</xsl:text>
              <xsl:value-of select="$e"/>
              <xsl:text>,</xsl:text>
              <xsl:value-of select="$n"/>
              <xsl:text>,</xsl:text>
              <xsl:value-of select="$s"/>
              <xsl:text>)</xsl:text>
              </field>
            </xsl:if>
            -->
            <xsl:choose>
              <xsl:when test="-180 &lt;= number($e) and number($e) &lt;= 180 and
                -180 &lt;= number($w) and number($w) &lt;= 180 and
                -90 &lt;= number($s) and number($s) &lt;= 90 and
                -90 &lt;= number($n) and number($n) &lt;= 90">
                <xsl:choose>
                  <xsl:when test="$e = $w and $s = $n">
                    <field name="geom">
                      <xsl:text>POINT(</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>)</xsl:text>
                    </field>
                  </xsl:when>
                  <xsl:when
                      test="($e = $w and $s != $n) or ($e != $w and $s = $n)">
                    <!-- Probably an invalid bbox indexing a point only -->
                    <field name="geom">
                      <xsl:text>POINT(</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>)</xsl:text>
                    </field>
                  </xsl:when>
                  <xsl:otherwise>
                    <field name="geom">
                      <xsl:text>POLYGON((</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($e, ' ', $s)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($e, ' ', $n)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $n)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>))</xsl:text>
                    </field>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>

            <!--<xsl:value-of select="($e + $w) div 2"/>,<xsl:value-of select="($n + $s) div 2"/></field>-->
          </xsl:for-each>
        </xsl:for-each>


        <!-- Service information -->
        <xsl:for-each select="srv:serviceType/gco:LocalName">
          <field name="serviceType">
            <xsl:value-of select="text()"/>
          </field>
          <!--<xsl:variable name="inspireServiceType" as="xs:string"
                  select="solr:analyzeField(
            'inspireServiceType', text(),
            'query',
            'org.apache.lucene.analysis.miscellaneous.KeepWordFilter',
            0)"/>
          <xsl:if test="$inspireServiceType != ''">
            <field name="inspireServiceType">
              <xsl:value-of select="lower-case($inspireServiceType)"/>
            </field>
          </xsl:if>
          <xsl:if test="following-sibling::srv:serviceTypeVersion">
            <field name="serviceTypeAndVersion">
              <xsl:value-of select="concat(
            text(),
            $separator,
            following-sibling::srv:serviceTypeVersion/gco:CharacterString/text())"/>
            </field>
          </xsl:if>-->
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
        <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
          <xsl:variable name="crs" select="gmd:code/gco:CharacterString"/>

          <xsl:if test="$crs != ''">
            <field name="coordinateSystem">
              <xsl:value-of select="$crs"/>
            </field>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>


      <!-- INSPIRE Conformity -->
<!--

      &lt;!&ndash; Conformity for data sets &ndash;&gt;
      <xsl:choose>
        <xsl:when test="$isService">
          <xsl:for-each-group select="gmd:dataQualityInfo/*/gmd:report"
                    group-by="*/gmd:result/*/gmd:specification/gmd:CI_Citation/
    gmd:title/gco:CharacterString">

            <xsl:variable name="title" select="current-grouping-key()"/>
            <xsl:if test="count($eu9762009/*[lower-case(normalize-space(.)) =
        lower-case(normalize-space($title))]) = 1">

              <xsl:variable name="pass" select="*/gmd:result/*/gmd:pass/gco:Boolean"/>
              <field name="inspireConformResource">
                <xsl:value-of select="$pass"/>
              </field>
            </xsl:if>
          </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
          &lt;!&ndash; Conformity for services &ndash;&gt;
          <xsl:for-each-group select="gmd:dataQualityInfo/*/gmd:report"
                    group-by="*/gmd:result/*/gmd:specification/gmd:CI_Citation/
    gmd:title/gco:CharacterString">

            <xsl:variable name="title" select="current-grouping-key()"/>
            <xsl:if test="count($eu10892010/*[lower-case(normalize-space(.)) =
        lower-case(normalize-space($title))]) = 1">

              <xsl:variable name="pass" select="*/gmd:result/*/gmd:pass/gco:Boolean"/>
              <field name="inspireConformResource">
                <xsl:value-of select="$pass"/>
              </field>
            </xsl:if>
          </xsl:for-each-group>
        </xsl:otherwise>
      </xsl:choose>
-->

      <xsl:for-each-group select="gmd:dataQualityInfo/*/gmd:report"
                group-by="*/gmd:result/*/gmd:specification/
                    */gmd:title/gco:CharacterString">
        <xsl:variable name="title" select="current-grouping-key()"/>
        <xsl:variable name="pass" select="*/gmd:result/*/gmd:pass/gco:Boolean"/>
        <xsl:if test="$pass">
          <field name="conformTo_{replace(normalize-space($title), ' ', '-')}">
            <xsl:value-of select="$pass"/>
          </field>
        </xsl:if>
      </xsl:for-each-group>


      <xsl:for-each select="gmd:dataQualityInfo/*">

        <xsl:for-each select="gmd:lineage/gmd:LI_Lineage/
                gmd:statement/gco:CharacterString[. != '']">
          <field name="lineage">
            <xsl:value-of select="normalize-space(.)"/>
          </field>
        </xsl:for-each>


        <!-- Indexing measure value -->
        <xsl:for-each select="gmd:report/*[
        normalize-space(gmd:nameOfMeasure/gco:CharacterString) != '']">
          <xsl:variable name="measureName"
                  select="normalize-space(gmd:nameOfMeasure/gco:CharacterString)"/>
          <xsl:for-each select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value">
            <xsl:if test=". != ''">
              <field name="measure_{$measureName}">
                <xsl:value-of select="."/>
              </field>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="gmd:distributionInfo/*">
        <xsl:for-each select="gmd:distributionFormat/*/gmd:name/gco:CharacterString">
          <field name="format">
            <xsl:value-of select="."/>
          </field>
        </xsl:for-each>

        <xsl:for-each select="gmd:transferOptions/*/
                gmd:onLine/*[gmd:linkage/gmd:URL != '']">
          <field name="linkUrl">
            <xsl:value-of select="gmd:linkage/gmd:URL"/>
          </field>
          <field name="linkProtocol">
            <xsl:value-of select="gmd:protocol/gco:CharacterString/text()"/>
          </field>
          <field name="link">
            <xsl:value-of select="gmd:protocol/*/text()"/>
            <xsl:text>|</xsl:text>
            <xsl:value-of select="gmd:linkage/gmd:URL"/>
            <xsl:text>|</xsl:text>
            <xsl:value-of select="gmd:name/*/text()"/>
            <xsl:text>|</xsl:text>
            <xsl:value-of select="gmd:description/*/text()"/>
          </field>
        </xsl:for-each>
      </xsl:for-each>

      <!-- Service/dataset relation. Create document for the association.
      Note: not used for indicators anymore
       This could be used to retrieve :
      {!child of=documentType:metadata}+documentType:metadata +id:9940c446-6fd4-4ab3-a4de-7d0ee028a8d1
      {!child of=documentType:metadata}+documentType:metadata +resourceType:service +serviceType:view
      {!child of=documentType:metadata}+documentType:metadata +resourceType:service +serviceType:download
       -->
      <xsl:for-each
          select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn">
        <xsl:variable name="associationType" select="'operatesOn'"/>
        <xsl:variable name="serviceType" select="../srv:serviceType/gco:LocalName"/>
        <!--<xsl:variable name="relatedTo" select="@uuidref"/>-->
        <xsl:variable name="getRecordByIdId">
          <xsl:if test="@xlink:href != ''">
            <xsl:analyze-string select="@xlink:href"
                      regex=".*[i|I][d|D]=([\w\-\.\{{\}}]*).*">
              <xsl:matching-substring>
                <xsl:value-of select="regex-group(1)"/>
              </xsl:matching-substring>
            </xsl:analyze-string>
          </xsl:if>
        </xsl:variable>

        <xsl:variable name="datasetId">
          <xsl:choose>
            <xsl:when test="$getRecordByIdId != ''">
              <xsl:value-of select="$getRecordByIdId"/>
            </xsl:when>
            <xsl:when test="@uuidref != ''">
              <xsl:value-of select="@uuidref"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$datasetId != ''">
          <field name="recordOperateOn">
            <xsl:value-of select="$datasetId"/>
          </field>
        </xsl:if>
      </xsl:for-each>
    </doc>
  </xsl:template>


  <xsl:template mode="index-contact" match="*[gmd:CI_ResponsibleParty]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>

    <!-- Select the first child which should be a CI_ResponsibleParty.
    Some records contains more than one CI_ResponsibleParty which is
    not valid and they will be ignored.
     Same for organisationName eg. de:b86a8604-bf78-480f-a5a8-8edff5586679 -->
    <xsl:variable name="organisationName"
            select="*[1]/gmd:organisationName[1]/(gco:CharacterString|gmx:Anchor)"
            as="xs:string*"/>

    <xsl:variable name="role"
            select="*[1]/gmd:role/*/@codeListValue"
            as="xs:string?"/>
    <xsl:if test="normalize-space($organisationName) != ''">
      <field name="Org{$fieldSuffix}">
        <xsl:value-of select="$organisationName"/>
      </field>
      <field name="{$role}Org{$fieldSuffix}">
        <xsl:value-of select="$organisationName"/>
      </field>
    </xsl:if>
    <field name="contact{$fieldSuffix}">{
      org:"<xsl:value-of select="replace($organisationName, '&quot;', '\\&quot;')"/>",
      role:"<xsl:value-of select="$role"/>"
      }
    </field>
  </xsl:template>
</xsl:stylesheet>
