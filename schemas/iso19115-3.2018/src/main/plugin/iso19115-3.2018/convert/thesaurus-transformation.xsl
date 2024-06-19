<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">


  <!-- A set of templates use to convert thesaurus concept to
       iso19115-3.2018 fragments. -->

  <!-- Convert a concept to an iso19115-3.2018 fragment with an Anchor
        for each keywords pointing to the concept URI-->
  <xsl:template name="to-iso19115-3.2018-keyword-with-anchor">
    <xsl:call-template name="to-iso19115-3.2018-keyword">
      <xsl:with-param name="withAnchor" select="true()"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Convert a concept to an iso19115-3.2018 mri:MD_Keywords with an
       XLink which will be resolved by XLink resolver. -->
  <xsl:template name="to-iso19115-3.2018-keyword-as-xlink">
    <xsl:call-template name="to-iso19115-3.2018-keyword">
      <xsl:with-param name="withXlink" select="true()"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Convert a concept to an iso19115-3.2018 keywords.
    If no keyword is provided, only thesaurus section is adaded.
    -->
  <xsl:template name="to-iso19115-3.2018-keyword">
    <xsl:param name="withAnchor" select="false()"/>
    <xsl:param name="withXlink" select="false()"/>
    <!-- Add thesaurus identifier using an Anchor which points to the download link.
        It's recommended to use it in order to have the thesaurus widget inline editor
        which use the thesaurus identifier for initialization.
        Also add the thesaurus title using an Anchor. -->
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

    <xsl:apply-templates mode="to-iso19115-3.2018-keyword" select="." >
      <xsl:with-param name="withAnchor" select="$withAnchor"/>
      <xsl:with-param name="withXlink" select="$withXlink"/>
      <xsl:with-param name="withThesaurusAnchor" select="$withThesaurusAnchor"/>
      <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
      <xsl:with-param name="textgroupOnly" select="$textgroupOnly"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template mode="to-iso19115-3.2018-keyword"
                match="*[not(/root/request/skipdescriptivekeywords)]">
    <xsl:param name="textgroupOnly"/>
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="withAnchor"/>
    <xsl:param name="withXlink"/>
    <xsl:param name="withThesaurusAnchor"/>

    <mri:descriptiveKeywords>
      <xsl:choose>
        <xsl:when test="$withXlink">
          <xsl:variable name="multiple"
                        select="if (contains(/root/request/id, ','))
                                then 'true' else 'false'"/>

          <xsl:variable name="isLocalXlink"
                        select="util:getSettingValue('system/xlinkResolver/localXlinkEnable')"/>
          <xsl:variable name="prefixUrl"
                        select="if ($isLocalXlink = 'true')
                              then  concat('local://', /root/gui/language)
                              else $serviceUrl"/>

          <xsl:attribute name="xlink:href"
                         select="concat($prefixUrl,
                                  'api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=',
                                   if (thesaurus/key) then thesaurus/key else /root/request/thesaurus,
                                  '&amp;amp;id=', encode-for-uri(/root/request/id),
                                  '&amp;amp;multiple=', $multiple,
                                  if (/root/request/lang) then concat('&amp;amp;lang=', /root/request/lang) else '',
                                  if ($textgroupOnly) then '&amp;amp;textgroupOnly' else '')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="to-iso19115-3.2018-md-keywords">
            <xsl:with-param name="withAnchor" select="$withAnchor"/>
            <xsl:with-param name="withThesaurusAnchor" select="$withThesaurusAnchor"/>
            <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
            <xsl:with-param name="textgroupOnly" select="$textgroupOnly"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </mri:descriptiveKeywords>
  </xsl:template>


  <xsl:template mode="to-iso19115-3.2018-keyword" match="*[/root/request/skipdescriptivekeywords]">
    <xsl:param name="textgroupOnly"/>
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="withAnchor"/>
    <xsl:param name="withThesaurusAnchor"/>
    <xsl:call-template name="to-iso19115-3.2018-md-keywords">
      <xsl:with-param name="withAnchor" select="$withAnchor"/>
      <xsl:with-param name="withThesaurusAnchor" select="$withThesaurusAnchor"/>
      <xsl:with-param name="listOfLanguage" select="$listOfLanguage"/>
      <xsl:with-param name="textgroupOnly" select="$textgroupOnly"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="to-iso19115-3.2018-md-keywords">
    <xsl:param name="textgroupOnly"/>
    <xsl:param name="listOfLanguage"/>
    <xsl:param name="withAnchor"/>
    <xsl:param name="withThesaurusAnchor"/>
    <mri:MD_Keywords>

      <!-- Get thesaurus ID from keyword or from request parameter if no keyword found. -->
      <xsl:variable name="currentThesaurus" select="if (thesaurus/key) then thesaurus/key else /root/request/thesaurus"/>

      <!-- Loop on all keyword from the same thesaurus -->
      <xsl:for-each select="//keyword[thesaurus/key = $currentThesaurus]">
        <mri:keyword>
          <xsl:if test="$currentThesaurus = 'external.none.allThesaurus'">
            <!--
                if 'all' thesaurus we need to encode the thesaurus name so that update-fixed-info can re-organize the
                keywords into the correct thesaurus sections.
            -->
            <xsl:variable name="keywordThesaurus"
                          select="replace(./uri, 'http://org.fao.geonet.thesaurus.all/([^@]+)@@@.+', '$1')" />
            <xsl:attribute name="gco:nilReason"
                           select="concat('thesaurus::', $keywordThesaurus)" />
          </xsl:if>

          <xsl:choose>
            <xsl:when test="count($listOfLanguage) > 1">
              <xsl:attribute name="xsi:type" select="'lan:PT_FreeText_PropertyType'"/>
              <xsl:variable name="keyword" select="." />

              <xsl:if test="not($textgroupOnly)">
                <xsl:choose>
                  <xsl:when test="$withAnchor">
                    <gcx:Anchor>
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
                    </gcx:Anchor>
                  </xsl:when>
                  <xsl:otherwise>
                    <gco:CharacterString>
                      <xsl:value-of
                        select="$keyword/values/value[@language = $listOfLanguage[1]]/text()"></xsl:value-of>
                    </gco:CharacterString>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
              <lan:PT_FreeText>
                <xsl:for-each select="$listOfLanguage">
                  <xsl:variable name="lang" select="." />
                  <xsl:if test="$textgroupOnly or $lang != $listOfLanguage[1]">
                    <lan:textGroup>
                      <lan:LocalisedCharacterString locale="#{upper-case(util:twoCharLangCode($lang))}">
                        <xsl:value-of select="$keyword/values/value[@language = $lang]/text()"/>
                      </lan:LocalisedCharacterString>
                    </lan:textGroup>
                  </xsl:if>
                </xsl:for-each>
              </lan:PT_FreeText>
            </xsl:when>
            <xsl:otherwise>
              <!-- ... default mode -->
              <xsl:choose>
                <xsl:when test="$withAnchor">
                  <gcx:Anchor>
                    <xsl:attribute name="xlink:href">
                      <xsl:choose>
                        <xsl:when test="matches(uri, '^http.*')">
                          <xsl:value-of select="uri"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="concat($serviceUrl, 'api/registries/vocabularies/keyword?thesaurus=',
                                                thesaurus/key, '&amp;id=', uri)"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="value"/>
                  </gcx:Anchor>
                </xsl:when>
                <xsl:otherwise>
                  <gco:CharacterString>
                    <xsl:value-of select="value" />
                  </gco:CharacterString>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </mri:keyword>
      </xsl:for-each>

      <!-- If no keyword, add one to avoid invalid metadata -->
      <xsl:if test="count(//keyword[thesaurus/key = $currentThesaurus]) = 0">
        <mri:keyword gco:nilReason="missing">
          <gco:CharacterString></gco:CharacterString>
        </mri:keyword>
      </xsl:if>

      <xsl:copy-of select="geonet:add-iso19115-3.2018-thesaurus-info(
                                    $currentThesaurus, $listOfLanguage[1],
                                    $withThesaurusAnchor,
                                    /root/gui/thesaurus/thesauri, not(/root/request/keywordOnly))" />

    </mri:MD_Keywords>
  </xsl:template>



  <xsl:function name="geonet:add-iso19115-3.2018-thesaurus-info">
    <xsl:param name="currentThesaurus" as="xs:string"/>
    <xsl:param name="mainLanguage" as="xs:string?"/>
    <xsl:param name="withThesaurusAnchor" as="xs:boolean"/>
    <xsl:param name="thesauri" as="node()"/>
    <xsl:param name="thesaurusInfo" as="xs:boolean"/>

    <!-- Add thesaurus theme -->
    <mri:type>
      <mri:MD_KeywordTypeCode codeList="http://standards.iso.org/iso/19115/-3/resources/codeList.xml#MD_KeywordTypeCode"
                              codeListValue="{$thesauri/thesaurus[key = $currentThesaurus]/dname}" />
    </mri:type>
    <xsl:if test="$thesaurusInfo">
      <xsl:variable name="thesaurusInMainLanguage"
                    select="$thesauri/thesaurus[key = $currentThesaurus]
                              /multilingualTitles/multilingualTitle[
                                lang = util:twoCharLangCode($mainLanguage, '')]/title"/>
      <xsl:variable name="thesaurusTitle"
                    select="if ($thesaurusInMainLanguage != '')
                            then $thesaurusInMainLanguage
                            else $thesauri/thesaurus[key = $currentThesaurus]/title"/>
      <mri:thesaurusName>
        <cit:CI_Citation>
          <cit:title>
            <xsl:choose>
              <xsl:when test="$withThesaurusAnchor = true()">
                <gcx:Anchor xlink:href="{$thesauri/thesaurus[key = $currentThesaurus]/defaultNamespace}">
                  <xsl:value-of select="$thesaurusTitle"/>
                </gcx:Anchor>
              </xsl:when>
              <xsl:otherwise>
                <gco:CharacterString>
                  <xsl:value-of select="$thesaurusTitle"/>
                </gco:CharacterString>
              </xsl:otherwise>
            </xsl:choose>
          </cit:title>

          <xsl:variable name="thesaurusDate"
                        select="normalize-space($thesauri/thesaurus[key = $currentThesaurus]/date)" />
          <xsl:variable name="thesaurusCreatedDate"
                        select="normalize-space($thesauri/thesaurus[key = $currentThesaurus]/createdDate)"/>
          <xsl:variable name="thesaurusIssuedDate"
                        select="normalize-space($thesauri/thesaurus[key = $currentThesaurus]/issuedDate)"/>
          <xsl:variable name="thesaurusModifiedDate"
                        select="normalize-space($thesauri/thesaurus[key = $currentThesaurus]/modifiedDate)"/>

          <xsl:if test="$thesaurusDate != ''">
            <cit:date>
              <cit:CI_Date>
                <cit:date>
                  <xsl:choose>
                    <xsl:when test="contains($thesaurusDate, 'T')">
                      <gco:DateTime>
                        <xsl:value-of select="$thesaurusDate" />
                      </gco:DateTime>
                    </xsl:when>
                    <xsl:otherwise>
                      <gco:Date>
                        <xsl:value-of select="$thesaurusDate" />
                      </gco:Date>
                    </xsl:otherwise>
                  </xsl:choose>
                </cit:date>
                <cit:dateType>
                  <cit:CI_DateTypeCode
                          codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                          codeListValue="publication" />
                </cit:dateType>
              </cit:CI_Date>
            </cit:date>
          </xsl:if>

          <!-- Publication Date-->
          <xsl:choose>
            <xsl:when test="$thesaurusIssuedDate != ''">
              <cit:date>
                <cit:CI_Date>
                  <cit:date>
                    <xsl:choose>
                      <xsl:when test="contains($thesaurusIssuedDate, 'T')">
                        <gco:DateTime>
                          <xsl:value-of select="$thesaurusIssuedDate" />
                        </gco:DateTime>
                      </xsl:when>
                      <xsl:otherwise>
                        <gco:Date>
                          <xsl:value-of select="$thesaurusIssuedDate" />
                        </gco:Date>
                      </xsl:otherwise>
                    </xsl:choose>
                  </cit:date>
                  <cit:dateType>
                    <cit:CI_DateTypeCode
                      codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                      codeListValue="publication" />
                  </cit:dateType>
                </cit:CI_Date>
              </cit:date>
            </xsl:when>
            <xsl:otherwise>
              <cit:date>
                <cit:CI_Date>
                  <cit:date>
                    <xsl:choose>
                      <xsl:when test="contains($thesaurusDate, 'T')">
                        <gco:DateTime>
                          <xsl:value-of select="$thesaurusDate" />
                        </gco:DateTime>
                      </xsl:when>
                      <xsl:otherwise>
                        <gco:Date>
                          <xsl:value-of select="$thesaurusDate" />
                        </gco:Date>
                      </xsl:otherwise>
                    </xsl:choose>
                  </cit:date>
                  <cit:dateType>
                    <cit:CI_DateTypeCode
                      codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                      codeListValue="publication" />
                  </cit:dateType>
                </cit:CI_Date>
              </cit:date>
            </xsl:otherwise>
          </xsl:choose>

          <!--Creation Date-->
          <xsl:if test="$thesaurusCreatedDate != ''">
            <cit:date>
              <cit:CI_Date>
                <cit:date>
                  <xsl:choose>
                    <xsl:when test="contains($thesaurusCreatedDate, 'T')">
                      <gco:DateTime>
                        <xsl:value-of select="$thesaurusCreatedDate" />
                      </gco:DateTime>
                    </xsl:when>
                    <xsl:otherwise>
                      <gco:Date>
                        <xsl:value-of select="$thesaurusCreatedDate" />
                      </gco:Date>
                    </xsl:otherwise>
                  </xsl:choose>
                </cit:date>
                <cit:dateType>
                  <cit:CI_DateTypeCode
                    codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="creation" />
                </cit:dateType>
              </cit:CI_Date>
            </cit:date>
          </xsl:if>

          <xsl:if test="$withThesaurusAnchor">
            <cit:identifier>
              <mcc:MD_Identifier>
                <mcc:code>
                  <gcx:Anchor xlink:href="{$thesauri/thesaurus[key = $currentThesaurus]/url}">geonetwork.thesaurus.<xsl:value-of
                          select="$currentThesaurus" />
                  </gcx:Anchor>
                </mcc:code>
              </mcc:MD_Identifier>
            </cit:identifier>
          </xsl:if>
        </cit:CI_Citation>
      </mri:thesaurusName>
    </xsl:if>
  </xsl:function>


  <!-- Convert a concept to an iso19115-3.2018 extent -->
  <xsl:template name="to-iso19115-3.2018-extent">
    <xsl:param name="isService" select="false()"/>

    <xsl:variable name="currentThesaurus" select="thesaurus/key"/>
    <!-- Loop on all keyword from the same thesaurus -->
    <xsl:for-each select="//keyword[thesaurus/key = $currentThesaurus]">
      <xsl:choose>
        <xsl:when test="$isService">
          <srv:extent>
            <xsl:copy-of select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, value)"/>
          </srv:extent>
        </xsl:when>
        <xsl:otherwise>
          <mri:extent>
            <xsl:copy-of select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, value)"/>
          </mri:extent>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
