<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:joda="java:org.fao.geonet.domain.ISODate"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:gn-fn-index="http://geonetwork-opensource.org/xsl/functions/index"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                exclude-result-prefixes="#all">


  <xsl:include href="common/index-utils.xsl"/>
  <xsl:include href="common/functions-core.xsl"/>
  <xsl:include href="../layout/utility-tpl-multilingual.xsl"/>


  <!-- Thesaurus folder -->
  <xsl:param name="thesauriDir"/>

  <!-- Enable INSPIRE or not -->
  <xsl:param name="inspire">false</xsl:param>

  <!-- Parent may be encoded using an associatedResource.
  Define which association type should be considered as parent. -->
  <xsl:variable name="parentAssociatedResourceType" select="'partOfSeamlessDatabase'"/>

  <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>

  <!-- If identification citation dates
    should be indexed as a temporal extent information (eg. in INSPIRE
    metadata implementing rules, those elements are defined as part
    of the description of the temporal extent). -->
  <xsl:variable name="useDateAsTemporalExtent" select="false()"/>

  <!-- For record not having status obsolete, flag them as non
  obsolete records. Some catalog like to restrict to non obsolete
  records only the default search. -->
  <xsl:variable name="flagNonObseleteRecords" select="true()"/>

  <!-- Load INSPIRE theme thesaurus if available -->
  <xsl:variable name="inspire-thesaurus"
                select="document(concat('file:///', replace($thesauriDir, '\\', '/'), '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))"/>

  <xsl:variable name="inspire-theme"
                select="if ($inspire-thesaurus//skos:Concept)
                        then $inspire-thesaurus//skos:Concept
                        else ''"/>

  <xsl:variable name="metadata"
                select="/mdb:MD_Metadata"/>


  <!-- Metadata UUID. -->
  <xsl:variable name="fileIdentifier"
                select="$metadata/
                            mdb:metadataIdentifier[1]/
                            mcc:MD_Identifier/mcc:code/*"/>

  <!-- Get the language
      If not set, the default will be english.
  -->
  <xsl:variable name="defaultLang">eng</xsl:variable>

  <xsl:variable name="documentMainLanguage"
                select="if ($metadata/mdb:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue != '')
                        then $metadata/mdb:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue
                        else $defaultLang"/>




  <xsl:template name="indexMetadata">
    <xsl:param name="lang" select="$documentMainLanguage"/>
    <xsl:param name="langId" select="''"/>

    <Document locale="{$lang}">
      <Field name="_locale" string="{$lang}" store="true" index="true"/>
      <Field name="_docLocale" string="{$lang}" store="true" index="true"/>

      <!-- Extension point using index mode -->
      <xsl:apply-templates mode="index" select="*"/>

      <xsl:call-template name="CommonFieldsFactory">
        <xsl:with-param name="lang" select="$lang"/>
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:call-template>



      <!-- === Free text search === -->
      <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:choose>
            <xsl:when test="$langId != ''">
              <xsl:value-of select="normalize-space(//node()[@locale=$langId])"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- <xsl:value-of select="normalize-space(string(.))"/> -->
              <!-- Index all text nodes except those that are in a bounding
                   polygon -->
              <xsl:for-each select="//text()[not(ancestor::gex:EX_BoundingPolygon) and normalize-space()!='']">
                 <xsl:value-of select="concat(.,' ')"/>
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text> </xsl:text>
          <xsl:for-each select="//@codeListValue">
            <xsl:value-of select="concat(., ' ')"/>
          </xsl:for-each>
        </xsl:attribute>
      </Field>
    </Document>
  </xsl:template>



  <!-- Index a field based on the language -->
  <xsl:function name="gn-fn-iso19115-3.2018:index-field" as="node()?">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="element" as="node()"/>

    <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field($fieldName,
                  $element, $documentMainLanguage, true(), true())"/>
  </xsl:function>
  <xsl:function name="gn-fn-iso19115-3.2018:index-field" as="node()?">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="element" as="node()"/>
    <xsl:param name="langId" as="xs:string?"/>

    <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field($fieldName,
                  $element, $langId, true(), true())"/>
  </xsl:function>
  <xsl:function name="gn-fn-iso19115-3.2018:index-field" as="node()?">
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:param name="element" as="node()"/>
    <xsl:param name="langId" as="xs:string?"/>
    <xsl:param name="store" as="xs:boolean"/>
    <xsl:param name="index" as="xs:boolean"/>

    <xsl:variable name="value">
      <xsl:for-each select="$element">
        <xsl:apply-templates mode="localised" select=".">
          <xsl:with-param name="langId" select="concat('#', $langId)"/>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:variable>
    <!--<xsl:message><xsl:value-of select="$fieldName"/>:<xsl:value-of select="normalize-space($value)"/> (<xsl:value-of select="$langId"/>) </xsl:message>-->
    <xsl:if test="normalize-space($value) != ''">
      <Field name="{$fieldName}"
             string="{$value}"
             store="{$store}"
             index="{$index}"/>
    </xsl:if>
  </xsl:function>


  <!-- Grab the default title which will
  be added to all document in the index
  whatever the langugae. -->
  <xsl:template name="defaultTitle">
    <xsl:param name="isoDocLangId"/>

    <xsl:variable name="poundLangId"
                  select="concat('#',upper-case(util:twoCharLangCode($isoDocLangId)))" />

    <xsl:variable name="identification"
                  select="$metadata/mdb:identificationInfo/*"/>
    <xsl:variable name="docLangTitle"
                  select="$identification/mri:citation/*/cit:title//lan:LocalisedCharacterString[@locale = $poundLangId]"/>
    <xsl:variable name="charStringTitle"
                  select="$identification/mri:citation/*/cit:title/gco:CharacterString"/>
    <xsl:variable name="locStringTitles"
                  select="$identification/mri:citation/*/cit:title//lan:LocalisedCharacterString"/>
    <xsl:choose>
      <xsl:when test="string-length(string($docLangTitle)) != 0">
        <xsl:value-of select="$docLangTitle[1]"/>
      </xsl:when>
      <xsl:when test="string-length(string($charStringTitle[1])) != 0">
        <xsl:value-of select="string($charStringTitle[1])"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string($locStringTitles[1])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <!-- Format a date.
  If null, unknown return empty,
  if current, now return the current date time.
  -->
  <xsl:function name="gn-fn-iso19115-3.2018:formatDateTime" as="xs:string">
    <xsl:param name="value" as="xs:string?"/>

    <xsl:choose>
      <xsl:when test="$value='' or lower-case($value)='unknown'">
        <xsl:value-of select="''"/>
      </xsl:when>
      <xsl:when test="lower-case($value)='current' or lower-case($value)='now'">
        <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="joda:parseISODateTime($value)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <xsl:template name="CommonFieldsFactory">
    <xsl:param name="lang" select="$documentMainLanguage"/>
    <xsl:param name="langId" select="''"/>

    <!-- The default title in the main language -->
    <xsl:variable name="_defaultTitle">
      <xsl:call-template name="defaultTitle">
        <xsl:with-param name="isoDocLangId" select="$documentMainLanguage"/>
      </xsl:call-template>
    </xsl:variable>

    <Field name="_defaultTitle"
           string="{string($_defaultTitle)}"
           store="true"
           index="true"/>
    <!-- not tokenized title for sorting, needed for multilingual sorting -->
    <Field name="_title"
           string="{string($_defaultTitle)}"
           store="true"
           index="true" />


    <xsl:for-each select="$metadata/mdb:identificationInfo/*">
      <Field name="anylight" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:for-each
            select="mri:citation/*/cit:title/gco:CharacterString|
                    mri:citation/*/cit:alternateTitle/gco:CharacterString|
                    mri:abstract/gco:CharacterString|
                    mri:credit/gco:CharacterString|
                    .//cit:CI_Organisation/cit:name/gco:CharacterString|
                    mri:descriptiveKeywords/*/mri:keyword/gco:CharacterString|
                    mri:descriptiveKeywords/*/mri:keyword/gcx:Anchor">
            <xsl:value-of select="concat(., ' ')"/>
          </xsl:for-each>
        </xsl:attribute>
      </Field>

      <xsl:for-each select="mri:citation/*">
        <xsl:for-each select="cit:identifier/mcc:MD_Identifier/mcc:code">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('identifier', ., $langId)"/>
        </xsl:for-each>

        <xsl:for-each select="cit:title">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('title', ., $langId)"/>
        </xsl:for-each>

        <xsl:for-each select="cit:alternateTitle">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('altTitle', ., $langId)"/>
        </xsl:for-each>

        <xsl:for-each select="cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']/cit:date">
          <Field name="revisionDate"
                 string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                 store="true" index="true"/>
          <Field name="createDateMonth"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 8)}"
                 store="true" index="true"/>
          <Field name="createDateYear"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 5)}"
                 store="true" index="true"/>
          <xsl:if test="$useDateAsTemporalExtent">
            <Field name="tempExtentBegin"
                   string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                   store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>


        <xsl:for-each select="cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='creation']/cit:date">
          <Field name="createDate"
                 string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                 store="true" index="true"/>
          <Field name="createDateMonth"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 8)}"
                 store="true" index="true"/>
          <Field name="createDateYear"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 5)}"
                 store="true" index="true"/>
          <xsl:if test="$useDateAsTemporalExtent">
            <Field name="tempExtentBegin"
                   string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                   store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>


        <xsl:for-each select="cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='publication']/cit:date">
          <Field name="publicationDate"
                 string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                 store="true" index="true"/>
          <xsl:if test="$useDateAsTemporalExtent">
            <Field name="tempExtentBegin"
                   string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                   store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="mri:abstract">
        <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('abstract', ., $langId)"/>
      </xsl:for-each>

      <xsl:for-each select="mri:credit">
        <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('credit', ., $langId)"/>
      </xsl:for-each>


      <xsl:for-each select="*/gex:EX_Extent">
        <xsl:apply-templates select="gex:geographicElement/gex:EX_GeographicBoundingBox" mode="latLon19115-3"/>

        <xsl:for-each select="gex:geographicElement/gex:EX_GeographicDescription/gex:geographicIdentifier/
                                  mcc:MD_Identifier/mcc:code">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('geoDescCode', ., $langId)"/>
        </xsl:for-each>

        <xsl:for-each select="gex:temporalElement/gex:EX_TemporalExtent/gex:extent">
          <xsl:for-each select="gml:TimePeriod">
            <xsl:for-each select="gml:beginPosition[. != '']|gml:begin/gml:TimeInstant/gml:timePosition[. != '']">
              <Field name="tempExtentBegin"
                     string="{lower-case(gn-fn-iso19115-3.2018:formatDateTime(.))}"
                     store="true" index="true"/>
            </xsl:for-each>
            <xsl:for-each select="gml:endPosition[. != '']|gml:end/gml:TimeInstant/gml:timePosition[. != '']">
              <Field name="tempExtentEnd"
                     string="{lower-case(gn-fn-iso19115-3.2018:formatDateTime(.))}"
                     store="true" index="true"/>
            </xsl:for-each>

            <Field name="tempExtentPeriod"
                   string="{concat(lower-case(gn-fn-iso19115-3.2018:formatDateTime(gml:beginPosition|gml:begin/gml:TimeInstant/gml:timePosition)), '|',
                                   lower-case(gn-fn-iso19115-3.2018:formatDateTime(gml:endPosition|gml:end/gml:TimeInstant/gml:timePosition)))}"
                   store="true" index="true"/>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:if test="*/gex:EX_Extent/*/gex:EX_BoundingPolygon">
        <Field name="boundingPolygon" string="y" store="true" index="false"/>
      </xsl:if>


      <xsl:for-each select="//mri:MD_Keywords">
        <xsl:variable name="thesaurusTitle"
                      select="replace(mri:thesaurusName/*/cit:title/gco:CharacterString/text(), ' ', '')"/>
        <xsl:variable name="thesaurusIdentifier"
                      select="mri:thesaurusName/*/cit:identifier/*/mcc:code/*/text()"/>
        <xsl:if test="$thesaurusIdentifier != ''">
          <Field name="thesaurusIdentifier"
                 string="{substring-after(
                              $thesaurusIdentifier,
                              'geonetwork.thesaurus.')}"
                 store="true" index="true"/>
        </xsl:if>
        <xsl:if test="mri:thesaurusName/*/cit:title/gco:CharacterString/text() != ''">
          <Field name="thesaurusName"
                 string="{mri:thesaurusName/*/cit:title/gco:CharacterString/text()}"
                 store="true" index="true"/>
        </xsl:if>

        <xsl:variable name="fieldName"
                      select="if ($thesaurusIdentifier != '')
                              then $thesaurusIdentifier
                              else $thesaurusTitle"/>
        <xsl:variable name="fieldNameTemp"
                      select="if (starts-with($fieldName, 'geonetwork.thesaurus'))
                                then substring-after($fieldName, 'geonetwork.thesaurus.')
                                else $fieldName"/>

        <xsl:for-each select="mri:keyword">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('keyword', ., $langId)"/>

          <xsl:if test="$fieldNameTemp != ''">
            <!-- field thesaurus-{{thesaurusIdentifier}}={{keyword}} allows
            to group all keywords of same thesaurus in a field -->

            <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field(
                                  concat('thesaurus-', $fieldNameTemp), ., $langId)"/>
          </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="mri:keyword/gco:CharacterString|
                                mri:keyword/gcx:Anchor|
                                mri:keyword/lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString">
          <xsl:if test="$inspire = 'true'">
            <xsl:if test="string-length(.) &gt; 0">
              <xsl:variable name="inspireannex">
                <xsl:call-template name="findInspireAnnex">
                  <xsl:with-param name="keyword" select="string(.)"/>
                  <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                </xsl:call-template>
              </xsl:variable>

              <!-- Add the inspire field if it's one of the 34 themes -->
              <xsl:if test="normalize-space($inspireannex)!=''">
                <xsl:variable name="keyword" select="."/>
                <xsl:variable name="inspireThemeAcronym">
                  <xsl:call-template name="findInspireThemeAcronym">
                    <xsl:with-param name="keyword" select="$keyword"/>
                  </xsl:call-template>
                </xsl:variable>

                <Field name="inspiretheme" string="{string(.)}" store="false" index="true"/>
                <Field name="inspirethemewithac"
                       string="{concat($inspireThemeAcronym, '|', $keyword)}"
                       store="true" index="true"/>
                <Field name="inspirethemeuri" string="{$inspire-theme[skos:prefLabel = $keyword]/@rdf:about}" store="true" index="true"/>
                <Field name="inspireannex" string="{$inspireannex}" store="false" index="true"/>
                <Field name="inspirecat" string="true" store="false" index="true"/>
              </xsl:if>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:variable name="listOfKeywords">{
        <xsl:variable name="keywordWithNoThesaurus"
                      select="//mri:MD_Keywords[
                                not(mri:thesaurusName) or mri:thesaurusName/*/cit:title/*/text() = '']/
                                  mri:keyword[*/text() != '']"/>
        <xsl:for-each-group select="//mri:MD_Keywords[mri:thesaurusName/*/cit:title/*/text() != '']"
                            group-by="mri:thesaurusName/*/cit:title/*/text()">
          '<xsl:value-of select="replace(current-grouping-key(), '''', '\\''')"/>' :[
          <xsl:for-each select="current-group()/mri:keyword/(gco:CharacterString|gcx:Anchor)">
            {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>,
            'link': '<xsl:value-of select="@xlink:href"/>'}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each-group>
        <xsl:if test="count(//mri:MD_Keywords[mri:thesaurusName/*/cit:title/*/text() != '']) > 0">
          <xsl:if test="//mri:MD_Keywords[mri:thesaurusName]">,</xsl:if>
          'otherKeywords': [
          <xsl:for-each select="$keywordWithNoThesaurus/(gco:CharacterString|gcx:Anchor)">
            {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>,
            'link': '<xsl:value-of select="@xlink:href"/>'}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:if>
        }
      </xsl:variable>

      <Field name="keywordGroup"
             string="{normalize-space($listOfKeywords)}"
             store="true"
             index="false"/>

      <xsl:for-each select="mri:topicCategory/mri:MD_TopicCategoryCode[text() != '']">
        <Field name="topicCat" string="{string(.)}" store="true" index="true"/>

        <!--FIXME <Field name="keyword"
               string="{util:getCodelistTranslation('gmd:MD_TopicCategoryCode', string(.), $lang}"
               store="true" index="true"/>-->
      </xsl:for-each>

      <xsl:for-each select="mri:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue">
        <Field name="datasetLang" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>


      <!-- TODO: Index new type of resolution -->
      <xsl:for-each select="mri:spatialResolution/mri:MD_Resolution">
        <xsl:for-each select="mri:equivalentScale/mri:MD_RepresentativeFraction/mri:denominator/gco:Integer">
          <Field name="denominator" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="mri:distance/gco:Distance">
          <Field name="distanceVal" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="mri:distance/gco:Distance/@uom">
          <Field name="distanceUom" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>

      <!-- Add an extra value to the status codelist to indicate all
      non obsolete records -->
      <xsl:if test="$flagNonObseleteRecords">
        <xsl:variable name="isNotObsolete"
                      select="count(mri:status[mcc:MD_ProgressCode/@codeListValue = 'obsolete']) = 0"/>
        <xsl:if test="$isNotObsolete">
          <Field name="cl_status" string="notobsolete" store="true" index="true"/>
        </xsl:if>
      </xsl:if>


      <!-- Index associated resources and provides option to query by type of
           association and type of initiative

      Association info is indexed by adding the following fields to the index:
       * agg_use: boolean
       * agg_with_association: {$associationType}
       * agg_{$associationType}: {$code}
       * agg_{$associationType}_with_initiative: {$initiativeType}
       * agg_{$associationType}_{$initiativeType}: {$code}

      Sample queries:
       * Search for records with siblings: http://localhost:8080/geonetwork/srv/fre/q?agg_use=true
       * Search for records having a crossReference with another record:
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
       * Search for records having a crossReference with another record:
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
       * Search for records having a crossReference of type "study" with another record:
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_study=23f0478a-14ba-4a24-b365-8be88d5e9e8c
       * Search for records having a crossReference of type "study":
       http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_with_initiative=study
       * Search for records having a "crossReference" :
       http://localhost:8080/geonetwork/srv/fre/q?agg_with_association=crossReference
      -->
      <xsl:for-each select="mri:associatedResource/mri:MD_AssociatedResource">
        <xsl:variable name="code"
                      select="if (mri:metadataReference/@uuidref != '')
                              then mri:metadataReference/@uuidref
                              else mri:metadataReference/cit:CI_Citation/cit:identifier/mcc:MD_Identifier/mcc:code/gco:CharacterString"/>
        <xsl:if test="$code != ''">
          <xsl:variable name="associationType" select="mri:associationType/mri:DS_AssociationTypeCode/@codeListValue"/>

          <xsl:if test="$associationType = $parentAssociatedResourceType">
            <Field name="parentUuid" string="{string($code)}" store="true" index="true"/>
          </xsl:if>

          <xsl:variable name="initiativeType" select="mri:initiativeType/mri:DS_InitiativeTypeCode/@codeListValue"/>
          <Field name="agg_{$associationType}_{$initiativeType}" string="{$code}" store="false" index="true"/>
          <Field name="agg_{$associationType}_with_initiative" string="{$initiativeType}" store="false" index="true"/>
          <Field name="agg_{$associationType}" string="{$code}" store="false" index="true"/>
          <Field name="agg_associated" string="{$code}" store="false" index="true"/>
          <Field name="agg_with_association" string="{$associationType}" store="false" index="true"/>
          <Field name="agg_use" string="true" store="false" index="true"/>
        </xsl:if>
      </xsl:for-each>

      <xsl:for-each select="srv:serviceType/gco:ScopedName">
        <Field name="serviceType" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
        <Field  name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
        <Field  name="operation" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:operatesOn/@uuidref">
        <Field  name="operatesOn" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:coupledResource">
        <xsl:for-each select="srv:SV_CoupledResource/srv:identifier/gco:CharacterString">
          <Field  name="operatesOnIdentifier" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="srv:SV_CoupledResource/srv:operationName/gco:CharacterString">
          <Field  name="operatesOnName" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mri:pointOfContact/cit:CI_Responsibility/cit:party/cit:CI_Organisation">
        <xsl:variable name="orgName" select="string(cit:name/*)"/>
        <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('orgName', cit:name, $langId)"/>

        <xsl:call-template name="ContactIndexing">
          <xsl:with-param name="lang" select="$lang"/>
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="mri:pointOfContact/cit:CI_Responsibility/cit:party/cit:CI_Individual[not(cit:CI_Organisation)]">
        <xsl:call-template name="ContactIndexing">
          <xsl:with-param name="lang" select="$lang"/>
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>

      <xsl:for-each select="mri:resourceConstraints/*">
        <xsl:variable name="fieldPrefix" select="local-name()"/>

        <xsl:for-each
          select="mco:accessConstraints/*/@codeListValue[string(.) != 'otherRestrictions']">
          <Field name="{$fieldPrefix}AccessConstraints"
                 string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="mco:otherConstraints[gco:CharacterString]">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field(
                                  concat($fieldPrefix, 'OtherConstraints'), ., $langId)"/>
        </xsl:for-each>

        <xsl:for-each select="mco:otherConstraints/gcx:Anchor">
          <Field name="{$fieldPrefix}OtherConstraints"
                 string="{concat('link|',string(@xlink:href), '|', string(.))}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="mco:useLimitation[gco:CharacterString]">
          <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field(
                                  concat($fieldPrefix, 'UseLimitation'), ., $langId)"/>
        </xsl:for-each>

        <xsl:for-each select="mco:useLimitation/gcx:Anchor[not(string(@xlink:href))]">
          <Field name="{$fieldPrefix}UseLimitation"
                 string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="mco:useLimitation/gcx:Anchor[string(@xlink:href)]">
          <Field name="{$fieldPrefix}UseLimitation"
                 string="{concat('link|',string(@xlink:href), '|', string(.))}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>




      <!-- FIXME: BrowseGraphic needs improvement - there are changes
           to get URL from mcc:linkage rather than mcc:fileName and
           mcc:fileDescription - see extract-thumnails.xsl -->
      <xsl:for-each select="mri:graphicOverview/mcc:MD_BrowseGraphic">
        <xsl:variable name="fileName"  select="mcc:fileName/gco:CharacterString"/>
        <xsl:if test="$fileName != ''">
          <xsl:variable name="fileDescr" select="mcc:fileDescription/gco:CharacterString"/>
          <xsl:choose>
            <xsl:when test="contains($fileName ,'://')">
              <xsl:choose>
                <xsl:when test="string($fileDescr)='thumbnail'">
                  <Field  name="image" string="{concat('thumbnail|', $fileName)}" store="true" index="false"/>
                </xsl:when>
                <xsl:when test="string($fileDescr)='large_thumbnail'">
                  <Field  name="image" string="{concat('overview|', $fileName)}" store="true" index="false"/>
                </xsl:when>
                <xsl:otherwise>
                  <Field  name="image" string="{concat('unknown|', $fileName)}" store="true" index="false"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
      <xsl:if test="count(mri:graphicOverview/mcc:MD_BrowseGraphic) = 0">
        <Field  name="nooverview" string="true" store="false" index="true"/>
      </xsl:if>

    </xsl:for-each>



    <xsl:for-each select="$metadata/mdb:distributionInfo/mrd:MD_Distribution">
      <xsl:for-each select="mrd:distributionFormat/mrd:MD_Format/
                                mrd:formatSpecificationCitation/cit:CI_Citation/cit:title">
        <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('format', ., $langId)"/>
      </xsl:for-each>


      <!-- TODO: Need a rework -->
      <xsl:for-each select="mrd:transferOptions/mrd:MD_DigitalTransferOptions">
        <xsl:variable name="tPosition"
                      select="position()"/>
        <xsl:for-each select="mrd:onLine/cit:CI_OnlineResource[cit:linkage/*!='']">
          <xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
          <xsl:variable name="linkage"
                        select="cit:linkage/*" />
          <xsl:variable name="title"
                        select="normalize-space(cit:name/gco:CharacterString|cit:name/gcx:MimeFileType)"/>
          <xsl:variable name="desc"
                        select="normalize-space(cit:description/gco:CharacterString)"/>
          <xsl:variable name="protocol"
                        select="normalize-space(cit:protocol/gco:CharacterString)"/>
          <xsl:variable name="mimetype" select="gn-fn-core:protocolMimeType($linkage, $protocol, cit:name/gcx:MimeFileType/@type)"/>


          <!-- If the linkage points to WMS service and no protocol specified, manage as protocol OGC:WMS -->
          <xsl:variable name="wmsLinkNoProtocol"
                        select="contains(lower-case($linkage), 'service=wms') and not(string($protocol))" />

          <!-- ignore empty downloads -->
          <xsl:if test="string($linkage)!='' and not(contains($linkage,$download_check))">
            <Field name="protocol"
                   string="{string($protocol)}"
                   store="true" index="true"/>
          </xsl:if>

          <xsl:if test="normalize-space($mimetype)!=''">
            <Field name="mimetype"
                   string="{$mimetype}"
                   store="true" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'WWW:DOWNLOAD')">
            <Field name="download" string="true"
                   store="false" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'OGC:WMS') or $wmsLinkNoProtocol">
            <Field name="dynamic" string="true"
                   store="false" index="true"/>
          </xsl:if>

          <!-- ignore WMS links without protocol (are indexed below with mimetype application/vnd.ogc.wms_xml) -->
          <xsl:if test="not($wmsLinkNoProtocol)">
            <Field name="link" string="{concat($title, '|', $desc, '|', $linkage, '|', $protocol, '|', $mimetype, '|', $tPosition)}" store="true" index="false"/>
          </xsl:if>

          <!-- Add KML link if WMS -->
          <xsl:if test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($title)!=''">
            <!-- FIXME : relative path -->
            <Field name="link" string="{concat($title, '|', $desc, '|',
                '../../srv/en/google.kml?uuid=', $fileIdentifier, '&amp;layers=', $title,
                '|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml', '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>

          <!-- Try to detect Web Map Context by checking protocol or file extension -->
          <xsl:if test="starts-with($protocol,'OGC:WMC') or contains($linkage,'.wmc')">
            <Field name="link" string="{concat($title, '|', $desc, '|',
                $linkage, '|application/vnd.ogc.wmc|application/vnd.ogc.wmc', '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>

          <xsl:if test="$wmsLinkNoProtocol">
            <Field name="link" string="{concat($title, '|', $desc, '|',
        $linkage, '|OGC:WMS|application/vnd.ogc.wms_xml', '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>





    <xsl:for-each select="$metadata/mdb:dataQualityInfo/*/dqm:report/*/dqm:result">
      <xsl:if test="$inspire='true'">
        <!--
        INSPIRE related dataset could contains a conformity section with:
        * COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services
        * INSPIRE Data Specification on <Theme Name> – <version>
        * INSPIRE Specification on <Theme Name> – <version> for CRS and GRID

        Index those types of citation title to found dataset related to INSPIRE (which may be better than keyword
        which are often used for other types of datasets).

        "1089/2010" is maybe too fuzzy but could work for translated citation like "Règlement n°1089/2010, Annexe II-6" TODO improved
        -->
        <xsl:if test="(
            contains(dqm:DQ_ConformanceResult/dqm:specification/cit:CI_Citation/cit:title/gco:CharacterString, '1089/2010') or
            contains(dqm:DQ_ConformanceResult/dqm:specification/cit:CI_Citation/cit:title/gco:CharacterString, 'INSPIRE Data Specification') or
            contains(dqm:DQ_ConformanceResult/dqm:specification/cit:CI_Citation/cit:title/gco:CharacterString, 'INSPIRE Specification'))">
          <Field name="inspirerelated" string="on" store="false" index="true"/>
        </xsl:if>
      </xsl:if>

      <xsl:for-each select="//dqm:pass/gco:Boolean">
        <Field name="degree" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//dqm:specification/*/cit:title">
        <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('specificationTitle', ., $langId)"/>
      </xsl:for-each>

      <xsl:for-each select="//dqm:specification/*/cit:date/*/cit:date">
        <Field name="specificationDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//dqm:specification/*/cit:date/*/cit:dateType/cit:CI_DateTypeCode/@codeListValue">
        <Field name="specificationDateType" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>
    </xsl:for-each>

    <xsl:for-each select="$metadata/mdb:resourceLineage/*/mrl:statement">
      <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('lineage', ., $langId)"/>
    </xsl:for-each>




    <xsl:for-each select="$metadata/mdb:contentInfo/mrc:MD_FeatureCatalogueDescription/mrc:featureCatalogueCitation[@uuidref]">
      <Field  name="hasfeaturecat" string="{string(@uuidref)}" store="false" index="true"/>
    </xsl:for-each>

    <!-- Index feature catalog as complex object in attributeTable field.
    TODO multilingual -->
    <xsl:variable name="jsonFeatureTypes">[

      <xsl:for-each select="$metadata/mdb:contentInfo/mrc:MD_FeatureCatalogue//gfc:featureType">{

        "typeName" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:typeName/text()"/>",
        "definition" :"<xsl:value-of select="gn-fn-index:json-escape(gfc:FC_FeatureType/gfc:definition/*/text())"/>",
        "code" :"<xsl:value-of select="gfc:FC_FeatureType/gfc:code/*/text()"/>",
        "isAbstract" :"<xsl:value-of select="gfc:FC_FeatureType/gfc:isAbstract/*/text()"/>",
        "aliases" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:aliases/*/text()"/>",
        <!--"inheritsFrom" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsFrom/*/text()"/>",
        "inheritsTo" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:inheritsTo/*/text()"/>",
        "constrainedBy" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:constrainedBy/*/text()"/>",
        "definitionReference" : "<xsl:value-of select="gfc:FC_FeatureType/gfc:definitionReference/*/text()"/>",-->
        <!-- Index attribute table as JSON object -->
        <xsl:variable name="attributes"
                      select="*/gfc:carrierOfCharacteristics"/>
        <xsl:if test="count($attributes) > 0">
          "attributeTable" : [
          <xsl:for-each select="$attributes">
            {"name": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:memberName/text())"/>",
            "definition": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:definition/*/text())"/>",
            "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
            "link": "<xsl:value-of select="*/gfc:code/*/@xlink:href"/>",
            "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
            <xsl:if test="*/gfc:listedValue">
              ,"values": [
              <xsl:for-each select="*/gfc:listedValue">{
                "label": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:label/*/text())"/>",
                "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
                "definition": "<xsl:value-of select="gn-fn-index:json-escape(*/gfc:definition/*/text())"/>"}
                <xsl:if test="position() != last()">,</xsl:if>
              </xsl:for-each>
              ]
            </xsl:if>
            }
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:if>
        }
        <xsl:if test="position() != last()">,</xsl:if>
      </xsl:for-each>
      ]

    </xsl:variable>

    <Field name="featureTypes" index="true" store="true"
           string="{$jsonFeatureTypes}"/>


    <Field name="hasDqMeasures" index="true" store="true"
           string="{count($metadata/mdb:dataQualityInfo/*/mdq:report/*[
                            mdq:measure/*/mdq:measureIdentification/*/mcc:code/*/text() != ''
                          ]/mdq:result/mdq:DQ_QuantitativeResult[mdq:value/gco:Record/text() != '']) > 0}"/>

   <!-- TODO: Multilingual support -->
    <xsl:for-each select="$metadata/mdb:dataQualityInfo">
      <!-- Checpoint / Index component id.
        If not set, then index by dq section position. -->
      <xsl:variable name="cptId" select="*/@uuid"/>
      <xsl:variable name="cptName" select="*/mdq:scope/*/mcc:levelDescription[1]/*/mcc:other/*/text()"/>
      <xsl:variable name="dqId" select="if ($cptId != '') then $cptId else position()"/>

      <Field name="dqCpt" index="true" store="true"
             string="{$dqId}"/>


      <xsl:for-each select="*/mdq:standaloneQualityReport/*[
                              mdq:reportReference/*/cit:title/*/text() != ''
                            ]">
        <Field name="dqSReport" index="false" store="true"
               string="{normalize-space(concat(
                          mdq:reportReference/*/cit:title/*/text(), '|', mdq:abstract/*/text()))}"/>
      </xsl:for-each>

      <xsl:for-each select="*/mdq:report/*[
                            mdq:measure/*/mdq:measureIdentification/*/mcc:code/*/text() != ''
                          ]">

        <xsl:variable name="qmId" select="mdq:measure/*/mdq:measureIdentification/*/mcc:code/*/text()"/>
        <xsl:variable name="qmName" select="mdq:measure/*/mdq:nameOfMeasure/*/text()"/>

        <!-- Search record by measure id or measure name. -->
        <Field name="dqMeasure" index="true" store="false"
               string="{$qmId}"/>
        <Field name="dqMeasureName" index="true" store="false"
               string="{$qmName}"/>

        <xsl:for-each select="mdq:result/mdq:DQ_QuantitativeResult">
          <xsl:variable name="qmDate" select="mdq:dateTime/gco:Date/text()"/>
          <!-- TODO: We assume one value per measure which may not be the case. -->
          <xsl:variable name="qmValue" select="mdq:value/gco:Record/text()"/>
          <xsl:variable name="qmUnit" select="mdq:valueUnit/*/gml:identifier/text()"/>
          <Field name="dqValues" index="true" store="true"
                 string="{concat($dqId, '|', $cptName, '|', $qmId, '|', $qmName, '|', $qmDate, '|', string-join($qmValue, ', '), '|', $qmUnit)}"/>

        </xsl:for-each>
      </xsl:for-each>
    </xsl:for-each>



    <xsl:for-each select="$metadata/mdb:resourceLineage/*/mrl:source[@uuidref]">
      <Field  name="hassource" string="{string(@uuidref)}" store="false" index="true"/>
    </xsl:for-each>




    <!-- Metadata scope -->
    <xsl:variable name="isDataset"
                  select="count($metadata/mdb:metadataScope[mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='dataset']) > 0"/>


    <!-- A map is identified when presentation form is mapDigital
    in that case, the record is not flagged as a dataset.
    -->
    <xsl:variable name="isMapDigital"
                  select="count($metadata/mdb:identificationInfo/*/mri:citation/*/
                                cit:presentationForm[cit:CI_PresentationFormCode/@codeListValue = 'mapDigital']) > 0"/>
    <xsl:variable name="isStatic"
                  select="count($metadata/mdb:distributionInfo/mrd:MD_Distribution/
                                mrd:distributionFormat/mrd:MD_Format/mrd:formatSpecificationCitation/*/
                                  cit:name/gco:CharacterString[
                                    contains(., 'PDF') or
                                    contains(., 'PNG') or
                                    contains(., 'JPEG')]) > 0"/>
    <xsl:variable name="isInteractive"
                  select="count($metadata/mdb:distributionInfo/*/
                                mrd:distributionFormat/mrd:MD_Format/mrd:formatSpecificationCitation/*/
                                  cit:name/gco:CharacterString[
                                    contains(., 'OGC:WMC') or
                                    contains(., 'OGC:OWS')]) > 0"/>
    <xsl:variable name="isPublishedWithWMCorOWSProtocol"
                  select="count($metadata/mdb:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/*/cit:protocol[
                                  starts-with(gco:CharacterString, 'OGC:WMC') or
                                  starts-with(gco:CharacterString, 'OGC:OWS')]) > 0"/>

    <xsl:if test="$isDataset and $isMapDigital and ($isStatic or $isInteractive or $isPublishedWithWMCorOWSProtocol)">
      <Field name="type" string="map" store="true" index="true"/>
      <xsl:choose>
        <xsl:when test="$isStatic">
          <Field name="maptype" string="staticMap" store="true" index="true"/>
        </xsl:when>
        <xsl:when test="$isInteractive or $isPublishedWithWMCorOWSProtocol">
          <Field name="maptype" string="interactiveMap" store="true" index="true"/>
        </xsl:when>
      </xsl:choose>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="$metadata/mdb:metadataScope">
        <xsl:if test="not($isMapDigital)">
          <xsl:for-each select="$metadata/mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue">
            <Field name="type" string="{string(.)}" store="true" index="true"/>
          </xsl:for-each>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- If not defined, record is a dataset -->
        <Field name="type" string="dataset" store="true" index="true"/>
      </xsl:otherwise>
    </xsl:choose>

    <!-- Set type as service if scope code not defined. -->
    <xsl:choose>
      <xsl:when test="$metadata[not(mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue)]/
          mdb:identificationInfo/srv:SV_ServiceIdentification">
        <Field name="type" string="service" store="false" index="true"/>
      </xsl:when>
    </xsl:choose>

    <xsl:for-each select="$metadata/mdb:metadataScope/mdb:MD_MetadataScope/mdb:name">
      <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('levelName', .)"/>
    </xsl:for-each>




    <xsl:for-each select="$metadata/mdb:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue|
                          $metadata/mdb:otherLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue">
      <Field name="language" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <xsl:variable name="standardName"
                  select="$metadata/mdb:metadataStandard/cit:CI_Citation/cit:title/gco:CharacterString"/>

    <xsl:for-each select="$standardName">
      <Field name="standardName" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>


    <xsl:for-each select="$metadata/mdb:metadataIdentifier/mcc:MD_Identifier">
      <Field name="fileId" string="{string(mcc:code/gco:CharacterString)}" store="false" index="true"/>
    </xsl:for-each>



    <xsl:for-each select="
      $metadata/mdb:parentMetadata/cit:CI_Citation/cit:identifier/mcc:MD_Identifier/mcc:code/gco:CharacterString|
      $metadata/mdb:parentMetadata/@uuidref">
      <Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>
    <Field name="isChild" string="{exists($metadata/mdb:parentMetadata)}" store="true" index="true"/>



    <xsl:for-each select="$metadata/mdb:contact/cit:CI_Responsibility/cit:party/cit:CI_Organisation">
      <xsl:variable name="orgName" select="string(cit:name/*)"/>
      <!--<xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('orgName', cit:name, $langId)"/>-->

      <xsl:call-template name="ContactIndexing">
        <xsl:with-param name="type" select="'metadata'"/>
        <xsl:with-param name="lang" select="$lang"/>
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:call-template>
    </xsl:for-each>


    <xsl:for-each select="$metadata/mdb:dateInfo/cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']/cit:date/*">
      <Field name="changeDate" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>


    <xsl:for-each select="$metadata/mdb:referenceSystemInfo/mrs:MD_ReferenceSystem">
      <xsl:for-each select="mrs:referenceSystemIdentifier/mcc:MD_Identifier">
        <xsl:variable name="crs"
                      select="if (normalize-space(mcc:description/gco:CharacterString) != '')
                              then mcc:description/gco:CharacterString
                              else mcc:code/gco:CharacterString"/>

        <xsl:if test="$crs != ''">
          <Field name="crs" string="{$crs}" store="true" index="true"/>
          <Field name="crsCode" string="{mcc:code/gco:CharacterString}" store="true" index="true"/>
        </xsl:if>

        <xsl:variable name="crsDetails">
          {
          "code": "<xsl:value-of select="mcc:code/*/text()"/>",
          "codeSpace": "<xsl:value-of select="mcc:codeSpace/*/text()"/>",
          "name": "<xsl:value-of select="mcc:description/*/text()"/>",
          "url": "<xsl:value-of select="mcc:code/*/@xlink:href"/>"
          }
        </xsl:variable>

        <Field name="crsDetails"
               string="{normalize-space($crsDetails)}"
               store="true"
               index="false"/>
      </xsl:for-each>
    </xsl:for-each>


    <!-- Index all codelist -->
    <xsl:for-each select="$metadata//*[*/@codeListValue != '']">
      <Field name="cl_{local-name()}"
             string="{*/@codeListValue}"
             store="true" index="true"/>
      <!--<xsl:message><xsl:value-of select="name(*)"/>:<xsl:value-of select="*/@codeListValue"/> (<xsl:value-of select="$lang"/>) = <xsl:value-of select="util:getCodelistTranslation(name(*), string(*/@codeListValue), $lang)"/></xsl:message>-->
      <Field name="cl_{concat(local-name(), '_text')}"
             string="{util:getCodelistTranslation(name(*), string(*/@codeListValue), string($lang))}"
             store="true" index="true"/>
    </xsl:for-each>

  </xsl:template>



  <xsl:template name="ContactIndexing">
    <xsl:param name="type" select="'resource'" required="no" as="xs:string"/>
    <xsl:param name="fieldPrefix" select="'responsibleParty'" required="no" as="xs:string"/>
    <xsl:param name="lang"/>
    <xsl:param name="langId"/>

    <!-- Only used in ISO19139 -->
    <xsl:variable name="position" select="'0'"/>

    <!-- Name is optional if logo or identifier is provided -->
    <xsl:if test="cit:name">
      <xsl:copy-of select="gn-fn-iso19115-3.2018:index-field('orgName', cit:name, $langId)"/>
    </xsl:if>

    <xsl:variable name="uuid" select="@uuid"/>
    <xsl:variable name="role" select="../../cit:role/*/@codeListValue"/>
    <xsl:variable name="email" select="cit:contactInfo[1]/cit:CI_Contact/
                             cit:address/cit:CI_Address/
                             cit:electronicMailAddress/gco:CharacterString[normalize-space()!='']|
                 cit:individual//cit:contactInfo[1]/cit:CI_Contact/
                                cit:address/cit:CI_Address/
                                cit:electronicMailAddress/gco:CharacterString[normalize-space()!='']"/>
    <xsl:variable name="roleTranslation" select="util:getCodelistTranslation('cit:CI_RoleCode', string($role), string($lang))"/>
    <xsl:variable name="logo" select="cit:logo/mcc:MD_BrowseGraphic/mcc:fileName/gco:CharacterString"/>
    <xsl:variable name="website" select="cit:contactInfo[1]//cit:onlineResource[1]/*/cit:linkage/gco:CharacterString"/>
    <xsl:variable name="phones"
                  select="cit:contactInfo[1]/cit:CI_Contact/cit:phone/*/cit:number/gco:CharacterString"/>
    <!--<xsl:variable name="phones"
                  select="cit:contactInfo/cit:CI_Contact/cit:phone/concat(*/cit:numberType/*/@codeListValue, ':', */cit:number/gco:CharacterString)"/>-->
    <xsl:variable name="address" select="string-join(cit:contactInfo[1]/*/cit:address/*/(
                                          cit:deliveryPoint|cit:postalCode|cit:city|
                                          cit:administrativeArea|cit:country)/gco:CharacterString/text(), ', ')"/>
    <xsl:variable name="individualNames" select="cit:individual//cit:name/gco:CharacterString"/>
    <xsl:variable name="positionName" select="cit:individual//cit:positionName/gco:CharacterString"/>

    <xsl:variable name="orgName">
      <xsl:apply-templates mode="localised" select="cit:name">
        <xsl:with-param name="langId" select="concat('#', $langId)"/>
      </xsl:apply-templates>
    </xsl:variable>

    <Field name="{$type}_{$fieldPrefix}_{$role}"
           string="{$orgName}"
           store="false"
           index="true"/>

    <Field name="{$fieldPrefix}"
           string="{concat($roleTranslation, '|',
                           $type, '|',
                           $orgName, '|',
                           $logo, '|',
                           string-join($email, ','), '|',
                           string-join($individualNames, ','), '|',
                           string-join($positionName, ','), '|',
                           $address, '|',
                           string-join($phones, ','), '|',
                           $uuid, '|',
                           $position, '|',
                           $website)}"
           store="true" index="false"/>

    <xsl:for-each select="$email">
      <Field name="{$fieldPrefix}Email" string="{string(.)}" store="true" index="true"/>
      <Field name="{$fieldPrefix}RoleAndEmail" string="{$role}|{string(.)}" store="true" index="true"/>
    </xsl:for-each>
    <xsl:for-each select="@uuid">
      <Field name="{$fieldPrefix}Uuid" string="{string(.)}" store="true" index="true"/>
      <Field name="{$fieldPrefix}RoleAndUuid" string="{$role}|{string(.)}" store="true" index="true"/>
    </xsl:for-each>

  </xsl:template>


  <!-- Traverse the tree in index mode -->
  <xsl:template mode="index" match="*|@*">
    <xsl:apply-templates mode="index" select="*|@*"/>
  </xsl:template>



  <!-- inspireThemes is a nodeset consisting of skos:Concept elements -->
  <!-- each containing a skos:definition and skos:prefLabel for each language -->
  <!-- This template finds the provided keyword in the skos:prefLabel elements and returns the English one from the same skos:Concept -->
  <xsl:template name="translateInspireThemeToEnglish">
    <xsl:param name="keyword"/>
    <xsl:param name="inspireThemes"/>
    <xsl:if test="$inspireThemes">
      <xsl:for-each select="$inspireThemes/skos:prefLabel">
        <!-- if this skos:Concept contains a kos:prefLabel with text value equal to keyword -->
        <xsl:if test="text() = $keyword">
          <xsl:value-of select="../skos:prefLabel[@xml:lang='en']/text()"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="findInspireThemeAcronym">
    <xsl:param name="keyword"/>
    <xsl:value-of select="$inspire-theme/skos:altLabel[../skos:prefLabel = $keyword]/text()"/>
  </xsl:template>

  <xsl:template name="findInspireAnnex">
    <xsl:param name="keyword"/>
    <xsl:param name="inspireThemes"/>
    <xsl:variable name="englishKeywordMixedCase">
      <xsl:call-template name="translateInspireThemeToEnglish">
        <xsl:with-param name="keyword" select="$keyword"/>
        <xsl:with-param name="inspireThemes" select="$inspireThemes"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="englishKeyword" select="lower-case($englishKeywordMixedCase)"/>
    <!-- Another option could be to add the annex info in the SKOS thesaurus using something
    like a related concept. -->
    <xsl:choose>
      <!-- annex i -->
      <xsl:when test="$englishKeyword='coordinate reference systems' or $englishKeyword='geographical grid systems'
                  or $englishKeyword='geographical names' or $englishKeyword='administrative units'
                  or $englishKeyword='addresses' or $englishKeyword='cadastral parcels'
                  or $englishKeyword='transport networks' or $englishKeyword='hydrography'
                  or $englishKeyword='protected sites'">
        <xsl:text>i</xsl:text>
      </xsl:when>
      <!-- annex ii -->
      <xsl:when test="$englishKeyword='elevation' or $englishKeyword='land cover'
                  or $englishKeyword='orthoimagery' or $englishKeyword='geology'">
        <xsl:text>ii</xsl:text>
      </xsl:when>
      <!-- annex iii -->
      <xsl:when test="$englishKeyword='statistical units' or $englishKeyword='buildings'
                  or $englishKeyword='soil' or $englishKeyword='land use'
                  or $englishKeyword='human health and safety' or $englishKeyword='utility and governmental services'
                  or $englishKeyword='environmental monitoring facilities' or $englishKeyword='production and industrial facilities'
                  or $englishKeyword='agricultural and aquaculture facilities' or $englishKeyword='population distribution — demography'
                  or $englishKeyword='area management/restriction/regulation zones and reporting units'
                  or $englishKeyword='natural risk zones' or $englishKeyword='atmospheric conditions'
                  or $englishKeyword='meteorological geographical features' or $englishKeyword='oceanographic geographical features'
                  or $englishKeyword='sea regions' or $englishKeyword='bio-geographical regions'
                  or $englishKeyword='habitats and biotopes' or $englishKeyword='species distribution'
                  or $englishKeyword='energy resources' or $englishKeyword='mineral resources'">
        <xsl:text>iii</xsl:text>
      </xsl:when>
      <!-- inspire annex cannot be established: leave empty -->
    </xsl:choose>
  </xsl:template>


  <xsl:template match="*" mode="latLon19115-3">
    <xsl:variable name="format" select="'##.00'"></xsl:variable>

    <xsl:if test="number(gex:westBoundLongitude/gco:Decimal)
            and number(gex:southBoundLatitude/gco:Decimal)
            and number(gex:eastBoundLongitude/gco:Decimal)
            and number(gex:northBoundLatitude/gco:Decimal)
            ">
      <Field name="westBL" string="{format-number(gex:westBoundLongitude/gco:Decimal, $format)}"
             store="false" index="true"/>
      <Field name="southBL" string="{format-number(gex:southBoundLatitude/gco:Decimal, $format)}"
             store="false" index="true"/>

      <Field name="eastBL" string="{format-number(gex:eastBoundLongitude/gco:Decimal, $format)}"
             store="false" index="true"/>
      <Field name="northBL" string="{format-number(gex:northBoundLatitude/gco:Decimal, $format)}"
             store="false" index="true"/>

      <Field name="geoBox" string="{concat(gex:westBoundLongitude/gco:Decimal, '|',
                gex:southBoundLatitude/gco:Decimal, '|',
                gex:eastBoundLongitude/gco:Decimal, '|',
                gex:northBoundLatitude/gco:Decimal
                )}" store="true" index="false"/>
    </xsl:if>

  </xsl:template>
</xsl:stylesheet>
