<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                exclude-result-prefixes="#all">


  <!-- A set of templates use to convert thesaurus concept to ISO19139 fragments. -->


  <xsl:include href="../process/process-utility.xsl"/>


  <!-- Convert a concept to an ISO19139 fragment with an Anchor
        for each keywords pointing to the concept URI-->
  <xsl:template name="to-iso19139-keyword-with-anchor">
    <xsl:call-template name="to-iso19139-keyword">
      <xsl:with-param name="withAnchor" select="true()"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Convert a concept to an ISO19139 gmd:MD_Keywords with an XLink which
    will be resolved by XLink resolver. -->
  <xsl:template name="to-iso19139-keyword-as-xlink">
    <xsl:call-template name="to-iso19139-keyword">
      <xsl:with-param name="withXlink" select="true()"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Convert a concept to an ISO19139 gmd:MD_Keywords with an XLink which
    will be resolved by XLink resolver with Anchor encoding. -->
  <xsl:template name="to-iso19139-keyword-as-xlink-with-anchor">
    <xsl:call-template name="to-iso19139-keyword">
      <xsl:with-param name="withXlink" select="true()"/>
      <xsl:with-param name="withAnchor" select="true()"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Convert a concept to an ISO19139 keywords.
    If no keyword is provided, only thesaurus section is adaded.
    -->
  <xsl:template name="to-iso19139-keyword">
    <xsl:param name="withAnchor" select="false()"/>
    <xsl:param name="withXlink" select="false()"/>
    <!-- Add thesaurus identifier using an Anchor which points to the download link.
        It's recommended to use it in order to have the thesaurus widget inline editor
        which use the thesaurus identifier for initialization. -->
    <xsl:param name="withThesaurusAnchor" select="true()"/>


    <!-- The lang parameter contains a list of languages
    with the main one as the first element. If only one element
    is provided, then CharacterString or Anchor are created.
    If more than one language is provided, then PT_FreeText
    with or without CharacterString can be created. -->
    <xsl:variable name="listOfLanguage" select="tokenize(/root/request/lang, ',')"/>
    <xsl:variable name="textgroupOnly"
                  as="xs:boolean"
                  select="if (/root/request/textgroupOnly and normalize-space(/root/request/textgroupOnly) != '')
                          then /root/request/textgroupOnly
                          else false()"/>


    <xsl:apply-templates mode="to-iso19139-keyword" select=".">
      <xsl:with-param name="withAnchor" select="$withAnchor"/>
      <xsl:with-param name="withXlink" select="$withXlink"/>
      <xsl:with-param name="withThesaurusAnchor" select="$withThesaurusAnchor"/>
      <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
      <xsl:with-param name="textgroupOnly" select="$textgroupOnly"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- given a thesarus and language, find the most appropriate responsible party name.
       This will be in the dublin core section as "publisher"-->
  <xsl:function name="geonet:getThesaurusResponsiblePartyIso19139">
    <xsl:param name="thesarus" />
    <xsl:param name="lang1" />
    <xsl:variable name="lang" select="lower-case($lang1)" />
    <xsl:variable name="lang_2letter" select="lower-case(util:twoCharLangCode($lang))" />

    <xsl:variable name="thesaurusPublisherMultilingualNode" select="$thesarus/dublinCoreMultilinguals/dublinCoreMultilingual[lower-case(./lang) = $lang and ./tag='publisher']/value" />
    <xsl:variable name="thesaurusPublisherMultilingualNode_2letter" select="$thesarus/dublinCoreMultilinguals/dublinCoreMultilingual[lower-case(./lang) = $lang_2letter and ./tag='publisher']/value" />

    <xsl:choose>
      <xsl:when test="$thesaurusPublisherMultilingualNode">
        <xsl:value-of select="$thesaurusPublisherMultilingualNode"/>
      </xsl:when>
      <xsl:when test="$thesaurusPublisherMultilingualNode_2letter">
        <xsl:value-of select="$thesaurusPublisherMultilingualNode_2letter"/>
      </xsl:when>
      <xsl:otherwise>
        Unknown
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <xsl:template mode="to-iso19139-keyword" match="*[not(/root/request/skipdescriptivekeywords)]">
    <xsl:param name="textgroupOnly"/>
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="withAnchor"/>
    <xsl:param name="withXlink"/>
    <xsl:param name="withThesaurusAnchor"/>

    <gmd:descriptiveKeywords>
      <xsl:choose>
        <xsl:when test="$withXlink">
          <xsl:variable name="isLocalXlink"
                        select="util:getSettingValue('system/xlinkResolver/localXlinkEnable')"/>
          <xsl:variable name="prefixUrl"
                        select="if ($isLocalXlink = 'true')
                                then concat('local://', $node, '/')
                                else $serviceUrl"/>

          <xsl:attribute name="xlink:href"
                         select="concat(
                                  $prefixUrl,
                                  'api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=',
                                   if (thesaurus/key) then thesaurus/key else /root/request/thesaurus,
                                  '&amp;id=', encode-for-uri(/root/request/id),
                                  if (/root/request/lang) then concat('&amp;lang=', /root/request/lang) else '',
                                  if ($textgroupOnly) then '&amp;textgroupOnly' else '',
                                  if ($withAnchor) then '&amp;transformation=to-iso19139-keyword-with-anchor' else '')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="to-md-keywords">
            <xsl:with-param name="withAnchor" select="$withAnchor"/>
            <xsl:with-param name="withThesaurusAnchor" select="$withThesaurusAnchor"/>
            <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
            <xsl:with-param name="textgroupOnly" select="$textgroupOnly"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </gmd:descriptiveKeywords>
  </xsl:template>

  <xsl:template mode="to-iso19139-keyword" match="*[/root/request/skipdescriptivekeywords]">
    <xsl:param name="textgroupOnly"/>
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="withAnchor"/>
    <xsl:param name="withThesaurusAnchor"/>

    <xsl:call-template name="to-md-keywords">
      <xsl:with-param name="withAnchor" select="$withAnchor"/>
      <xsl:with-param name="withThesaurusAnchor" select="$withThesaurusAnchor"/>
      <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
      <xsl:with-param name="textgroupOnly" select="$textgroupOnly"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="to-md-keywords">
    <xsl:param name="textgroupOnly"/>
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="withAnchor"/>
    <xsl:param name="withThesaurusAnchor"/>

    <gmd:MD_Keywords>
      <!-- Get thesaurus ID from keyword or from request parameter if no keyword found. -->
      <xsl:variable name="currentThesaurus"
                    select="if (thesaurus/key) then thesaurus/key else /root/request/thesaurus"/>
      <!-- Loop on all keyword from the same thesaurus -->
      <xsl:for-each select="//keyword[thesaurus/key = $currentThesaurus]">
        <gmd:keyword>
          <xsl:if test="$currentThesaurus = 'external.none.allThesaurus'">
            <!--
                if 'all' thesaurus we need to encode the thesaurus name so that update-fixed-info can re-organize the
                keywords into the correct thesaurus sections.
            -->
            <xsl:variable name="keywordThesaurus"
                          select="replace(./uri, 'http://org.fao.geonet.thesaurus.all/([^@]+)@@@.+', '$1')"/>
            <xsl:attribute name="gco:nilReason" select="concat('thesaurus::', $keywordThesaurus)"/>
          </xsl:if>

          <!-- Multilingual output if more than one requested language -->
          <xsl:choose>
            <xsl:when test="count($listOfLanguage) > 1">
              <xsl:attribute name="xsi:type" select="'gmd:PT_FreeText_PropertyType'"/>
              <xsl:variable name="keyword" select="."/>

              <xsl:if test="not($textgroupOnly)">
                <xsl:choose>
                  <xsl:when test="$withAnchor">
                    <gmx:Anchor>
                      <xsl:attribute name="xlink:href">
                        <xsl:choose>
                          <xsl:when test="matches(uri, '^http.*')">
                            <xsl:value-of select="uri"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="concat($serviceUrl, 'api/registries/vocabularies/keyword?thesaurus=', thesaurus/key, '&amp;id=', uri)"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:attribute>
                      <xsl:value-of select="value"/>
                    </gmx:Anchor>
                  </xsl:when>
                  <xsl:otherwise>
                    <gco:CharacterString>
                      <xsl:value-of
                        select="$keyword/values/value[@language = $listOfLanguage[1]]/text()"></xsl:value-of>
                    </gco:CharacterString>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>

              <gmd:PT_FreeText>
                <xsl:for-each select="$listOfLanguage">
                  <xsl:variable name="lang" select="."/>
                  <gmd:textGroup>
                    <gmd:LocalisedCharacterString
                      locale="#{upper-case(util:twoCharLangCode($lang))}">
                      <xsl:value-of
                        select="$keyword/values/value[@language = $lang]/text()"></xsl:value-of>
                    </gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                </xsl:for-each>
              </gmd:PT_FreeText>
            </xsl:when>
            <xsl:otherwise>
              <!-- ... default mode -->
              <xsl:choose>
                <xsl:when test="$withAnchor">
                  <gmx:Anchor>
                    <xsl:attribute name="xlink:href">
                      <xsl:choose>
                        <xsl:when test="matches(uri, '^http.*')">
                          <xsl:value-of select="uri"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="concat($serviceUrl, 'api/registries/vocabularies/keyword?thesaurus=', thesaurus/key, '&amp;id=', uri)"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="value"/>
                  </gmx:Anchor>
                </xsl:when>
                <xsl:otherwise>
                  <gco:CharacterString>
                    <xsl:value-of select="value"/>
                  </gco:CharacterString>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </gmd:keyword>
      </xsl:for-each>

      <!-- If no keyword, add one to avoid invalid metadata -->
      <xsl:if test="count(//keyword[thesaurus/key = $currentThesaurus]) = 0">
        <gmd:keyword gco:nilReason="missing">
          <xsl:choose>
            <xsl:when test="$withAnchor">
              <gmx:Anchor xlink:href="" />
            </xsl:when>
            <xsl:otherwise>
              <gco:CharacterString />
            </xsl:otherwise>
          </xsl:choose>
        </gmd:keyword>
      </xsl:if>

      <xsl:copy-of
        select="geonet:add-thesaurus-info($currentThesaurus, $withAnchor, $withThesaurusAnchor, /root/gui/thesaurus/thesauri, not(/root/request/keywordOnly))"/>
    </gmd:MD_Keywords>
  </xsl:template>

  <xsl:function name="geonet:add-thesaurus-info">
    <xsl:param name="currentThesaurus" as="xs:string"/>
    <xsl:param name="withTitleAnchor" as="xs:boolean"/>
    <xsl:param name="withThesaurusAnchor" as="xs:boolean"/>
    <xsl:param name="thesauri" as="node()"/>
    <xsl:param name="thesaurusInfo" as="xs:boolean"/>

    <!-- Add thesaurus theme -->
    <gmd:type>
      <gmd:MD_KeywordTypeCode
        codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode"
        codeListValue="{$thesauri/thesaurus[key = $currentThesaurus]/dname}"/>
    </gmd:type>
    <xsl:if test="$thesaurusInfo">
      <gmd:thesaurusName>
        <gmd:CI_Citation>
          <gmd:title>
            <xsl:choose>
              <xsl:when test="$withTitleAnchor = true()">
                <gmx:Anchor xlink:href="{$thesauri/thesaurus[key = $currentThesaurus]/defaultNamespace}">
                  <xsl:value-of select="$thesauri/thesaurus[key = $currentThesaurus]/title"/>
                </gmx:Anchor>
              </xsl:when>
              <xsl:otherwise>
                <gco:CharacterString>
                  <xsl:value-of select="$thesauri/thesaurus[key = $currentThesaurus]/title"/>
                </gco:CharacterString>
              </xsl:otherwise>
            </xsl:choose>
          </gmd:title>

          <xsl:variable name="thesaurusDate"
                        select="normalize-space($thesauri/thesaurus[key = $currentThesaurus]/date)"/>

          <xsl:if test="$thesaurusDate != ''">
            <gmd:date>
              <gmd:CI_Date>
                <gmd:date>
                  <xsl:choose>
                    <xsl:when test="contains($thesaurusDate, 'T')">
                      <gco:DateTime>
                        <xsl:value-of select="$thesaurusDate"/>
                      </gco:DateTime>
                    </xsl:when>
                    <xsl:otherwise>
                      <gco:Date>
                        <xsl:value-of select="$thesaurusDate"/>
                      </gco:Date>
                    </xsl:otherwise>
                  </xsl:choose>
                </gmd:date>
                <gmd:dateType>
                  <gmd:CI_DateTypeCode
                    codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="publication"/>
                </gmd:dateType>
              </gmd:CI_Date>
            </gmd:date>
          </xsl:if>

          <!--
              You can pull in the publisher from the Thesaurus XML.  See Metadata101/iso19139.ca.HNAP
              NOTE: HNAP only has 2 languages, so this should be modified so it loops through the non-default lanaguages
                    instead of just using ${altLang}
              NOTE: This also hard-codes the <gmd:role>, but you can also add this to the RDF and put it in here.
              NOTE:      <xsl:variable name="currentThesaurusFull" select="$thesauri/thesaurus[key = $currentThesaurus]" />

           <gmd:citedResponsibleParty>
            <gmd:CI_ResponsibleParty>
              <gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">
                <gco:CharacterString><xsl:value-of select="geonet:getThesaurusResponsibleParty($currentThesaurusFull,$mdlang)"/></gco:CharacterString>
                <gmd:PT_FreeText>
                  <gmd:textGroup>
                    <gmd:LocalisedCharacterString locale="#{$altLang}"><xsl:value-of select="geonet:getThesaurusResponsiblePartyIso19139($currentThesaurusFull,$altLang)"/></gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                </gmd:PT_FreeText>
              </gmd:organisationName>
              <gmd:role>
                <gmd:CI_RoleCode codeListValue="RI_409" codeList="http://nap.geogratis.gc.ca/metadata/register/napMetadataRegister.xml#IC_90">custodian; conservateur</gmd:CI_RoleCode>
              </gmd:role>
            </gmd:CI_ResponsibleParty>
          </gmd:citedResponsibleParty>
          -->

          <xsl:if test="$withThesaurusAnchor">
            <gmd:identifier>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gmx:Anchor xlink:href="{$thesauri/thesaurus[key = $currentThesaurus]/url}">
                    geonetwork.thesaurus.<xsl:value-of
                    select="$currentThesaurus"/>
                  </gmx:Anchor>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:identifier>
          </xsl:if>
        </gmd:CI_Citation>
      </gmd:thesaurusName>
    </xsl:if>
  </xsl:function>

  <!-- Convert a concept to an ISO19139 extent -->
  <xsl:template name="to-iso19139-extent">
    <xsl:param name="isService" select="false()"/>

    <xsl:variable name="currentThesaurus" select="thesaurus/key"/>
    <!-- Loop on all keyword from the same thesaurus -->
    <xsl:for-each select="//keyword[thesaurus/key = $currentThesaurus]">
      <xsl:choose>
        <xsl:when test="$isService">
          <srv:extent>
            <xsl:copy-of
              select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, value)"/>
          </srv:extent>
        </xsl:when>
        <xsl:otherwise>
          <gmd:extent>
            <xsl:copy-of
              select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, value)"/>
          </gmd:extent>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
