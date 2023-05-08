<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
  xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:dqc="http://standards.iso.org/iso/19157/-2/dqc/1.0"
  xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
  xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:java="java:org.fao.geonet.util.XslUtil"
  xmlns:mime="java:org.fao.geonet.util.MimeTypeFinder"
  xmlns:gn="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">

  <xsl:import href="convert/ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:include href="convert/functions.xsl"/>
  <xsl:include href="layout/utility-fn.xsl"/>

  <xsl:variable name="editorConfig"
                select="document('layout/config-editor.xml')"/>

  <!-- The default language is also added as gmd:locale
  for multilingual metadata records. -->
  <xsl:variable name="mainLanguage"
                select="/root/*/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>

  <xsl:variable name="isMultilingual"
                select="count(/root/*/mdb:otherLocale[*/lan:language/*/@codeListValue != $mainLanguage]) > 0"/>

  <xsl:variable name="mainLanguageId"
                select="upper-case(java:twoCharLangCode($mainLanguage))"/>

  <xsl:variable name="locales"
                select="/root/*/*/lan:PT_Locale"/>


  <xsl:variable name="nonMultilingualFields"
                select="$editorConfig/editor/multilingualFields/exclude"/>

  <!-- If no metadata linkage exist, build one based on
  the metadata UUID. -->
  <xsl:variable name="createMetadataLinkage"
                select="count(/root/*/mdb:metadataLinkage/cit:CI_OnlineResource/cit:linkage/*[normalize-space(.) != '']) = 0"/>


  <xsl:variable name="url" select="/root/env/siteURL"/>
  <xsl:variable name="uuid" select="/root/env/uuid"/>

  <xsl:template match="/root">
    <xsl:apply-templates select="mdb:MD_Metadata"/>
  </xsl:template>

  <xsl:template match="@xsi:schemaLocation">
    <xsl:if test="java:getSettingValue('system/metadata/validation/removeSchemaLocation') = 'false'">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mdb:MD_Metadata">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*"/>

      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>

      <xsl:for-each select="mdb:metadataIdentifier">
        <xsl:copy>
          <mcc:MD_Identifier>
            <xsl:apply-templates select="*/mcc:authority"/>

            <xsl:choose>
              <xsl:when test="/root/env/uuid != ''">
                <mcc:code>
                  <gco:CharacterString><xsl:value-of select="/root/env/uuid"/></gco:CharacterString>
                </mcc:code>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="*/mcc:code"/>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:apply-templates select="*/mcc:codeSpace"/>
            <xsl:apply-templates select="*/mcc:version"/>
            <xsl:apply-templates select="*/mcc:description"/>
          </mcc:MD_Identifier>
        </xsl:copy>
      </xsl:for-each>

      <xsl:apply-templates select="mdb:defaultLocale"/>
      
      <xsl:choose>
        <xsl:when test="/root/env/parentUuid != ''">
          <mdb:parentMetadata uuidref="{/root/env/parentUuid}"/>
        </xsl:when>
        <xsl:when test="mdb:parentMetadata">
          <xsl:apply-templates select="mdb:parentMetadata"/>
        </xsl:when>
      </xsl:choose>
      
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>


      <xsl:variable name="isCreationDateAvailable"
                    select="count(mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'creation'
                              and cit:date/*/text() != '']) > 0"
                    as="xs:boolean"/>
      <xsl:variable name="isRevisionDateAvailable"
                    select="count(mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'revision'
                              and cit:date/*/text() != '']) > 0"
                    as="xs:boolean"/>

      <!-- Add creation date if it does not exist-->
      <xsl:if test="not($isCreationDateAvailable)
                    or (/root/env/createDate != ''
                        and /root/env/newRecord = 'true')">
        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime><xsl:value-of select="/root/env/createDate"/></gco:DateTime>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="creation"/>
            </cit:dateType>
          </cit:CI_Date>
        </mdb:dateInfo>
      </xsl:if>
      <xsl:if test="not($isRevisionDateAvailable)">
        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime><xsl:value-of select="/root/env/changeDate"/></gco:DateTime>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="revision"/>
            </cit:dateType>
          </cit:CI_Date>
        </mdb:dateInfo>
      </xsl:if>


      <!-- Preserve date order -->
      <xsl:for-each select="mdb:dateInfo">
        <xsl:variable name="currentDateType" select="*/cit:dateType/*/@codeListValue"/>
        <!-- Update revision date-->
        <xsl:choose>
          <xsl:when test="$currentDateType = 'revision' and /root/env/changeDate">
            <mdb:dateInfo>
              <cit:CI_Date>
                <cit:date>
                  <gco:DateTime><xsl:value-of select="/root/env/changeDate"/></gco:DateTime>
                </cit:date>
                <cit:dateType>
                  <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                </cit:dateType>
              </cit:CI_Date>
            </mdb:dateInfo>
          </xsl:when>
          <xsl:when test="$currentDateType = 'creation'
                          and */cit:date/* = ''">
            <!-- remove empty creation date, added before if emtpy. -->
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>



      <!-- Add metadataStandard if it doesn't exist -->
      <xsl:choose>
        <xsl:when test="not(mdb:metadataStandard)">
          <mdb:metadataStandard>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>ISO 19115-3</gco:CharacterString>
              </cit:title>
            </cit:CI_Citation>
          </mdb:metadataStandard>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="mdb:metadataStandard"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>
      <xsl:apply-templates select="mdb:metadataLinkage"/>

      <xsl:variable name="pointOfTruthUrl" select="concat(/root/env/nodeURL, 'api/records/', $uuid)"/>

      <!-- Create metadata linkage only if it does not exist already. -->
      <xsl:if test="$createMetadataLinkage">
        <!-- TODO: This should only be updated for not harvested records ? -->
        <mdb:metadataLinkage>
          <cit:CI_OnlineResource>
            <cit:linkage>
              <!-- TODO: define a URL pattern and use it here -->
              <!-- TODO: URL could be multilingual ? -->
              <gco:CharacterString><xsl:value-of select="$pointOfTruthUrl"/></gco:CharacterString>
            </cit:linkage>
            <!-- TODO: Could be relevant to add description of the
            point of truth for the metadata linkage but this
            needs to be language dependant. -->
            <cit:function>
              <cit:CI_OnLineFunctionCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                                         codeListValue="completeMetadata"/>
            </cit:function>
          </cit:CI_OnlineResource>
        </mdb:metadataLinkage>
      </xsl:if>

      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>
      <xsl:apply-templates select="mdb:contentInfo"/>
      <xsl:apply-templates select="mdb:distributionInfo"/>
      <xsl:apply-templates select="mdb:dataQualityInfo"/>
      <xsl:apply-templates select="mdb:resourceLineage"/>
      <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
      <xsl:apply-templates select="mdb:metadataConstraints"/>
      <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
      <xsl:apply-templates select="mdb:metadataMaintenance"/>
      <xsl:apply-templates select="mdb:acquisitionInformation"/>
    </xsl:copy>
  </xsl:template>

  <!-- Update revision date -->
  <xsl:template match="mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue='lastUpdate']">
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="/root/env/changeDate">
          <cit:CI_Date>
            <cit:date>
              <gco:DateTime><xsl:value-of select="/root/env/changeDate"/></gco:DateTime>
            </cit:date>
            <cit:dateType>
              <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_DateTypeCode" codeListValue="lastUpdate"/>
            </cit:dateType>
          </cit:CI_Date>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="node()|@*"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- Force element with DateTime_PropertyType to have gco:DateTime -->
  <xsl:template match="mac:time|mac:expiryDate|mac:requestedDateOfCollection
                      |mac:latestAcceptableDate|cit:editionDate
                      |mrd:plannedAvailableDateTime|mdq:dateTime"
                priority="200">
    <xsl:variable name="value" select="gco:Date|gco:DateTime" />
    <xsl:copy>
      <gco:DateTime>
        <xsl:value-of select="$value" /><xsl:if test="string-length($value) = 10">T00:00:00</xsl:if>
      </gco:DateTime>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@gml:id">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="gml:id">
          <xsl:value-of select="generate-id(.)"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Fix srsName attribute generate CRS:84 (EPSG:4326 with long/lat
    ordering) by default -->
  <xsl:template match="@srsName">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="srsName">
          <xsl:text>CRS:84</xsl:text>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Add required gml attributes if missing -->
  <xsl:template match="gml:TimePeriod[not(@gml:id)]|
                       gml:Polygon[not(@gml:id) and not(@srsName)]|
                       gml:MultiSurface[not(@gml:id) and not(@srsName)]|
                       gml:LineString[not(@gml:id) and not(@srsName)]">
    <xsl:copy>
      <xsl:attribute name="gml:id">
        <xsl:value-of select="generate-id(.)"/>
      </xsl:attribute>
      <xsl:if test="local-name(.) != 'TimePeriod'">
        <xsl:attribute name="srsName">
          <xsl:text>urn:ogc:def:crs:EPSG:6.6:4326</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[gco:CharacterString|gcx:Anchor|lan:PT_FreeText]">
    <xsl:copy>
      <xsl:apply-templates select="@*[not(name() = 'gco:nilReason') and not(name() = 'xsi:type')]"/>

      <xsl:variable name="excluded"
                    select="gn-fn-iso19115-3.2018:isNotMultilingualField(., $editorConfig)"/>

      <xsl:variable name="valueInPtFreeTextForMainLanguage"
                    select="normalize-space(lan:PT_FreeText/*/lan:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)])"/>
      <!-- Add nileason if text is empty -->
      <xsl:variable name="isEmpty"
                    select="if ($isMultilingual and not($excluded))
                            then $valueInPtFreeTextForMainLanguage = ''
                            else if ($valueInPtFreeTextForMainLanguage != '')
                            then $valueInPtFreeTextForMainLanguage = ''
                            else normalize-space(gco:CharacterString|gcx:Anchor) = ''"/>

      <xsl:choose>
        <xsl:when test="$isEmpty">
          <xsl:attribute name="gco:nilReason">
            <xsl:choose>
              <xsl:when test="@gco:nilReason">
                <xsl:value-of select="@gco:nilReason"/>
              </xsl:when>
              <xsl:otherwise>missing</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@gco:nilReason != 'missing' and not($isEmpty)">
          <xsl:copy-of select="@gco:nilReason"/>
        </xsl:when>
      </xsl:choose>


      <!-- For multilingual records, for multilingual fields,
       create a gco:CharacterString or gcx:Anchor containing
       the same value as the default language PT_FreeText.
      -->
      <xsl:variable name="element" select="name()"/>

      <xsl:choose>
        <!-- Check record does not contains multilingual elements
         matching the main language. This may happen if the main
         language is declared in locales and only PT_FreeText are set.
         It should not be possible in GeoNetwork, but record user can
         import may use this encoding. -->
        <xsl:when test="not($isMultilingual) and
                        $valueInPtFreeTextForMainLanguage != '' and
                        normalize-space(gco:CharacterString|gcx:Anchor) = ''">
          <xsl:element name="{if (gcx:Anchor) then 'gcx:Anchor' else 'gco:CharacterString'}">
            <xsl:copy-of select="gcx:Anchor/@*"/>
            <xsl:value-of select="$valueInPtFreeTextForMainLanguage"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="not($isMultilingual) or
                        $excluded">
          <!-- Copy gco:CharacterString only. PT_FreeText are removed if not multilingual. -->
          <xsl:apply-templates select="gco:CharacterString|gcx:Anchor"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Add xsi:type for multilingual element. -->
          <xsl:attribute name="xsi:type" select="'lan:PT_FreeText_PropertyType'"/>

          <!-- Is the default language value set in a PT_FreeText ? -->
          <xsl:variable name="isInPTFreeText"
                        select="count(lan:PT_FreeText/*/lan:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)]) = 1"/>
          <xsl:choose>
            <xsl:when test="$isInPTFreeText">
              <!-- Update gco:CharacterString to contains
                   the default language value from the PT_FreeText.
                   PT_FreeText takes priority. -->
              <xsl:element name="{if (gcx:Anchor) then 'gcx:Anchor' else 'gco:CharacterString'}">
                <xsl:copy-of select="gcx:Anchor/@*"/>
                <xsl:value-of select="lan:PT_FreeText/*/lan:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)]/text()"/>
              </xsl:element>

              <xsl:if test="lan:PT_FreeText[normalize-space(.) != '']">
                <lan:PT_FreeText>
                  <xsl:call-template name="populate-free-text"/>
                </lan:PT_FreeText>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <!-- Populate PT_FreeText for default language if not existing and it is not null. -->
              <xsl:apply-templates select="gco:CharacterString|gcx:Anchor"/>
              <xsl:if test="normalize-space(gco:CharacterString|gcx:Anchor) != '' or lan:PT_FreeText">
                <lan:PT_FreeText>
                  <lan:textGroup>
                    <lan:LocalisedCharacterString locale="#{$mainLanguageId}">
                      <xsl:value-of select="gco:CharacterString|gcx:Anchor"/>
                    </lan:LocalisedCharacterString>
                  </lan:textGroup>
                  <xsl:call-template name="populate-free-text"/>
                </lan:PT_FreeText>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="populate-free-text">
    <xsl:variable name="freeText"
                  select="lan:PT_FreeText/lan:textGroup"/>

    <!-- Loop on locales in order to preserve order.
        Keep main language on top.
        Translations having no locale are ignored. eg. when removing a lang. -->
    <xsl:apply-templates select="$freeText[*/@locale = concat('#', $mainLanguageId)]"/>

    <xsl:for-each select="$locales[@id != $mainLanguageId]">
      <xsl:variable name="localId"
                    select="@id"/>

      <xsl:variable name="element"
                    select="$freeText[*/@locale = concat('#', $localId)]"/>

      <xsl:apply-templates select="$element"/>
    </xsl:for-each>
  </xsl:template>

  <!-- codelists: set @codeList path -->
  <xsl:template match="lan:LanguageCode[@codeListValue]" priority="10">
    <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/">
      <xsl:apply-templates select="@*[name(.)!='codeList']"/>
    </lan:LanguageCode>
  </xsl:template>

  <xsl:template match="dqm:*[@codeListValue]" priority="10">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of select="concat('http://standards.iso.org/iso/19157/resources/Codelists/cat/codelists.xml#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of select="concat('http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mdb:dataQualityInfo/mdq:DQ_DataQuality/mdq:scope/*/mcc:level/*/@codeListValue[. = '']">
    <xsl:attribute name="codeListValue">
      <xsl:value-of select="/root/*/mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue"/>
    </xsl:attribute>
  </xsl:template>


  <!-- For XLinked subtemplates, the lang parameter MUST be in the same order as in the record.
  Main language first, then other locales. If not, then the default CharacterString does not contain
  the main language. It user change the language order in the record, the lang parameter needs to
  be reordered too.

  Example of URL:
  <gmd:pointOfContact xmlns:xlink="http://www.w3.org/1999/xlink"
                             xlink:href="local://srv/api/registries/entries/af9e5d4e-2c1a-48c0-853f-3a771fcf9ee3?
                               process=gmd:role/gmd:CI_RoleCode/@codeListValue~distributor&amp;
                               lang=eng,ara,spa,rus,fre,ger,chi&amp;
                               schema=iso19139"
  Can also be using lang=eng&amp;lang=ara.
  -->
  <xsl:template match="@xlink:href[starts-with(., 'local://srv/api/registries/entries') and contains(., '?')]">
    <xsl:variable name="urlBase"
                  select="substring-before(., '?')"/>
    <xsl:variable name="urlParameters"
                  select="substring-after(., '?')"/>

    <!-- Collect all parameters excluding language -->
    <xsl:variable name="listOfAllParameters">
      <xsl:for-each select="tokenize($urlParameters, '&amp;')">
        <xsl:variable name="parameterName"
                      select="tokenize(., '=')[1]"/>

        <xsl:if test="$parameterName != 'lang'">
          <param name="{$parameterName}"
                 value="{.}"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:attribute name="xlink:href"
                   select="concat(
                    $urlBase,
                    '?lang=', string-join(($mainLanguage, $locales//lan:LanguageCode/@codeListValue[. != $mainLanguage]), ','),
                    '&amp;',
                    string-join($listOfAllParameters/param/@value, '&amp;'))"/>
  </xsl:template>



  <!-- Set local identifier to the first 3 letters of iso code. Locale ids
    are used for multilingual charcterString using #iso2code for referencing.
  -->
  <xsl:template match="mdb:MD_Metadata/*/lan:PT_Locale">
    <xsl:element name="lan:{local-name()}">
      <xsl:variable name="id"
                    select="upper-case(java:twoCharLangCode(lan:language/lan:LanguageCode/@codeListValue))"/>

      <xsl:apply-templates select="@*"/>
      <xsl:if test="normalize-space(@id)='' or normalize-space(@id)!=$id">
        <xsl:attribute name="id">
          <xsl:value-of select="$id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <!-- Remove attribute indeterminatePosition having empty
       value which is not a valid facet for it. -->
  <xsl:template match="@indeterminatePosition[. = '']" priority="2"/>

  <!-- ================================================================= -->
  <!-- Adjust the namespace declaration - In some cases name() is used to get the
    element. The assumption is that the name is in the format of  <ns:element>
    however in some cases it is in the format of <element xmlns=""> so the
    following will convert them back to the expected value. This also corrects the issue
    where the <element xmlns=""> loose the xmlns="" due to the exclude-result-prefixes="#all" -->
  <!-- Note: Only included prefix gml, mds and gco for now. -->
  <!-- TODO: Figure out how to get the namespace prefix via a function so that we don't need to hard code them -->
  <!-- ================================================================= -->

  <xsl:template name="correct_ns_prefix">
    <xsl:param name="element" />
    <xsl:param name="prefix" />
    <xsl:choose>
      <xsl:when test="local-name($element)=name($element) and $prefix != '' ">
        <xsl:element name="{$prefix}:{local-name($element)}">
          <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mdb:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'mdb'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gco:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gco'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gml:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gml'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Sextant / Template adding nilReason attribut with withheld value
  for some protocols. -->
  <xsl:template match="cit:linkage" priority="10">
    <xsl:choose>
      <xsl:when test="
				contains(lower-case(string(../cit:protocol/gco:CharacterString)), 'db') or
				contains(lower-case(string(../cit:protocol/gco:CharacterString)), 'copyfile') or
				contains(lower-case(string(../cit:protocol/gco:CharacterString)), 'file')">
        <cit:linkage gco:nilReason="withheld">
          <xsl:apply-templates select="@*"/>
          <xsl:copy-of select="./*" />
        </cit:linkage>
      </xsl:when>
      <xsl:otherwise>
        <cit:linkage >
          <xsl:apply-templates select="@*"/>
          <xsl:copy-of select="./*" />
        </cit:linkage>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="mri:descriptiveKeywords[not(*/mri:thesaurusName)]" priority="10">
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="freeTextKeywordBlockType"
                  select="*/mri:type/*/@codeListValue"/>
    <xsl:variable name="isFirstFreeTextKeywordBlockOfThisType"
                  select="count(preceding-sibling::*[
                          name() = $name
                          and not(*/mri:thesaurusName)
                          and */mri:type/*/@codeListValue = $freeTextKeywordBlockType]) = 0"/>

    <xsl:if test="$isFirstFreeTextKeywordBlockOfThisType">
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
        <mri:MD_Keywords>
          <xsl:apply-templates select="*/mri:keyword"/>

          <!-- Combine all free text keyword of same type -->
          <xsl:apply-templates select="following-sibling::*[
                      name() = $name
                      and not(*/mri:thesaurusName)
                      and */mri:type/*/@codeListValue = $freeTextKeywordBlockType]/*/mri:keyword"/>

          <xsl:apply-templates select="*/mri:type"/>
        </mri:MD_Keywords>
      </xsl:copy>
    </xsl:if>
  </xsl:template>



  <!-- Remove empty DQ elements, empty transfer options. -->
  <xsl:template match="mdb:dataQualityInfo[count(*) = 0]"/>
  <xsl:template match="mrd:transferOptions[mrd:MD_DigitalTransferOptions/count(*) = 0]"/>

  <!-- copy everything else as is -->
  <xsl:template match="@*|node()">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
