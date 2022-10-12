<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:reg="http://standards.iso.org/iso/19115/-3/reg/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
  <!-- This formatter render an ISO19115-3 record based on the
  editor configuration file.

  The layout is made in 2 modes:
  * render-field taking care of elements (eg. sections, label)
  * render-value taking care of element values (eg. characterString, URL)

  3 levels of priority are defined: 100, 50, none
  -->



  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

 <!-- Required for utility-fn.xsl -->
  <xsl:variable name="editorConfig"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>
  <xsl:include href="../../layout/utility-fn.xsl"/>
  <xsl:include href="../../formatter/jsonld/iso19115-3.2018-to-jsonld.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/mdb:MD_Metadata"/>

  <xsl:variable name="langId" select="gn-fn-iso19115-3.2018:getLangId($metadata, $language)"/>

  <xsl:variable name="allLanguages">
    <xsl:call-template name="get-iso19115-3.2018-other-languages"/>
  </xsl:variable>

  <xsl:variable name="isOnlyFeatureCatalog"
                select="not($metadata/mdb:identificationInfo)
                            and exists($metadata/mdb:contentInfo/*/mrc:featureCatalogue)"
                as="xs:boolean"/>

  <!-- Ignore some fields displayed in header or in right column -->
  <xsl:template mode="render-field"
                match="mri:graphicOverview|mri:abstract|mdb:identificationInfo/*/mri:citation/*/cit:title"
                priority="2000"/>


  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="mdb:MD_Metadata">
    <xsl:for-each select="if ($isOnlyFeatureCatalog)
                          then mdb:contentInfo/*/mrc:featureCatalogue/*/cat:name
                          else mdb:identificationInfo/*/mri:citation/*/cit:title">
      <xsl:call-template name="get-iso19115-3.2018-localised">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="mdb:MD_Metadata">
    <xsl:for-each select="if ($isOnlyFeatureCatalog)
                          then mdb:contentInfo/*/mrc:featureCatalogue/*/cat:scope
                          else mdb:identificationInfo/*/mri:abstract">
      <xsl:variable name="txt">
        <xsl:call-template name="get-iso19115-3.2018-localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="addLineBreaksAndHyperlinks">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="getTags" match="mdb:MD_Metadata">
    <xsl:param name="byThesaurus" select="false()"/>

    <xsl:variable name="tags">
      <xsl:for-each select="$metadata/mdb:identificationInfo/*/mri:descriptiveKeywords/
                                          *[
                                          mri:type/*/@codeListValue != 'place'
                                            and normalize-space(string-join(mri:keyword//text(), '')) != ''
                                            and (not(mri:thesaurusName/*/cit:identifier/*/mcc:code)
                                            or mri:thesaurusName/*/cit:identifier/*/mcc:code/*/
                                                text() != '')]">
        <xsl:variable name="thesaurusTitle">
          <xsl:for-each select="mri:thesaurusName/*/cit:title">
            <xsl:call-template name="get-iso19115-3.2018-localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:variable>
        <xsl:for-each select="mri:keyword">
          <xsl:variable name="keyword">
            <xsl:call-template name="get-iso19115-3.2018-localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:if test="$keyword != ''">
            <tag thesaurus="{$thesaurusTitle}">
              <xsl:value-of select="$keyword"/>
            </tag>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>

    <xsl:if test="count($tags/*) > 0">
      <section class="gn-md-side-social">
        <h2>
          <i class="fa fa-fw fa-tag"><xsl:comment select="'image'"/></i>
          <span><xsl:comment select="name()"/>
            <xsl:value-of select="$schemaStrings/noThesaurusName"/>
          </span>
        </h2>

        <xsl:choose>
          <xsl:when test="$byThesaurus">
            <xsl:for-each-group select="$tags/tag" group-by="@thesaurus">
              <xsl:sort select="@thesaurus"/>
              <xsl:if test="current-grouping-key() != ''">
                <xsl:value-of select="current-grouping-key()"/><br/>
              </xsl:if>

              <xsl:for-each select="current-group()">
                <xsl:sort select="."/>
                <xsl:choose>
                  <xsl:when test="$portalLink != ''">
                    <span class="badge"><xsl:copy-of select="."/></span>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href='#/search?query_string=%7B"tag.\\*":%7B"{.}":true%7D%7D'>
                      <span class="badge"><xsl:copy-of select="."/></span>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
              <xsl:if test="position() != last()">
                <hr/>
              </xsl:if>
            </xsl:for-each-group>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="$tags/tag">
              <xsl:sort select="."/>

              <xsl:choose>
                <xsl:when test="$portalLink != ''">
                  <span class="badge"><xsl:copy-of select="."/></span>
                </xsl:when>
                <xsl:otherwise>
                  <a href='#/search?query_string=%7B"tag.\\*":%7B"{.}":true%7D%7D'>
                    <span class="badge"><xsl:copy-of select="."/></span>
                  </a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </section>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="getMetadataHierarchyLevel" match="mdb:MD_Metadata">
    <xsl:value-of select="mdb:metadataScope/*/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue"/>
  </xsl:template>

  <xsl:template mode="getMetadataThumbnail" match="mdb:MD_Metadata">
    <xsl:value-of select="mdb:identificationInfo/*/mri:graphicOverview[1]/*/mcc:fileName/gco:CharacterString"/>
  </xsl:template>

  <xsl:template mode="getExtent" match="mdb:MD_Metadata">
    <xsl:if test=".//mdb:identificationInfo/*/mri:extent">
      <section class="gn-md-side-extent">
        <h2>
          <i class="fa fa-fw fa-map-marker"><xsl:comment select="'image'"/></i>
          <span><xsl:comment select="name()"/>
            <xsl:value-of select="$schemaStrings/spatialExtent"/>
          </span>
        </h2>

        <xsl:choose>
          <xsl:when test=".//mdb:identificationInfo/*/mri:extent//gex:EX_BoundingPolygon">
            <xsl:copy-of select="gn-fn-render:extent($metadataUuid)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="render-field"
                                 select=".//mdb:identificationInfo/*/mri:extent//gex:EX_GeographicBoundingBox">
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </section>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="getOverviews" match="mdb:MD_Metadata">
    <xsl:if test="mdb:identificationInfo/*/mri:graphicOverview">
      <section class="gn-md-side-overview">
        <h2>
          <i class="fa fa-fw fa-image"><xsl:comment select="'.'"/></i><xsl:comment select="'.'"/>
          <span>
            <xsl:value-of select="$schemaStrings/overviews"/>
          </span>
        </h2>

        <xsl:for-each select="mdb:identificationInfo/*/mri:graphicOverview/*">
          <img data-gn-img-modal="md"
               class="gn-img-thumbnail center-block"
               alt="{$schemaStrings/overview}"
               src="{mcc:fileName/*}"/>

          <xsl:for-each select="mcc:fileDescription">
            <div class="gn-img-thumbnail-caption">
              <xsl:call-template name="get-iso19115-3.2018-localised">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:call-template>
            </div>
          </xsl:for-each>
        </xsl:for-each>
      </section>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="getMetadataHeader" match="mdb:MD_Metadata">
    <div class="gn-abstract">
      <xsl:for-each select="if ($isOnlyFeatureCatalog)
                            then mdb:contentInfo/*/mrc:featureCatalogue/*/cat:scope
                            else mdb:identificationInfo/*/mri:abstract">
        <xsl:variable name="txt">
          <xsl:call-template name="get-iso19115-3.2018-localised">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:call-template name="addLineBreaksAndHyperlinks">
          <xsl:with-param name="txt" select="$txt"/>
        </xsl:call-template>
      </xsl:for-each>
    </div>

    <xsl:if test="$withJsonLd = 'true'">
      <script type="application/ld+json">
        <xsl:apply-templates mode="getJsonLD"
                             select="$metadata"/><xsl:comment select="'.'"/>
      </script>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="getMetadataCitation" match="mdb:MD_Metadata">
    <xsl:variable name="displayCitation"
                  select="true()"/>

    <xsl:variable name="doiUrl"
                  select=".//cit:onlineResource/*[cit:protocol/* = ('WWW:LINK-1.0-http--metadata-URL', 'WWW:LINK-1.0-http--publication-URL', 'DOI')]/cit:linkage/*"/>
    <xsl:variable name="landingPageUrl"
                  select="if ($doiUrl != '') then $doiUrl else concat($nodeUrl, 'api/records/', $metadataUuid)"/>

    <xsl:if test="$displayCitation">
      <blockquote>
        <div class="row">
          <div class="col-md-3">
            <i class="fa fa-quote-left pull-right"><xsl:comment select="'icon'"/></i>
          </div>
          <div class="col-md-9">
            <h2 title="{$schemaStrings/citationProposal-help}"><xsl:comment select="name()"/>
              <xsl:value-of select="$schemaStrings/citationProposal"/>
            </h2>
            <br/>
            <p>
              <!-- Custodians -->
              <xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact/
                                  *[cit:role/*/@codeListValue = ('custodian', 'author')]">

                <xsl:variable name="name"
                              select="normalize-space(.//cit:individual/*/cit:name[1])"/>

                <xsl:value-of select="$name"/>
                <xsl:if test="$name != ''"><xsl:comment select="'.'"/>(</xsl:if>
                <xsl:for-each select="cit:party/*/cit:name">
                  <xsl:call-template name="get-iso19115-3.2018-localised">
                    <xsl:with-param name="langId" select="$langId"/>
                  </xsl:call-template>
                </xsl:for-each>
                <xsl:if test="$name">)</xsl:if>
                <xsl:if test="position() != last()"><xsl:comment select="'.'"/>-<xsl:comment select="'.'"/></xsl:if>
              </xsl:for-each>

              <!-- Publication year: use last publication or revision date -->
              <xsl:variable name="publicationDate">
                <xsl:perform-sort select="mdb:identificationInfo/*/mri:citation/*/cit:date/*[
                                    cit:dateType/*/@codeListValue = ('publication', 'revision')]/
                                      cit:date/gco:*[. != '']">
                  <xsl:sort select="." order="descending"/>
                </xsl:perform-sort>
              </xsl:variable>
              <xsl:choose>
                <xsl:when test="$publicationDate/*[1]">
                  <xsl:for-each select="$publicationDate/*[1]">
                    (<xsl:value-of select="substring($publicationDate, 1, 4)"/>).
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>.<xsl:comment select="'.'"/></xsl:otherwise>
              </xsl:choose>

              <!-- Title -->
              <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:title">
                <xsl:call-template name="get-iso19115-3.2018-localised">
                  <xsl:with-param name="langId" select="$langId"/>
                </xsl:call-template>
              </xsl:for-each>

              <xsl:text>. </xsl:text>

              <!-- Publishers -->
              <xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact/
                                  *[cit:role/*/@codeListValue = 'publisher']">
                <xsl:for-each select="cit:party/*/cit:name">
                  <xsl:call-template name="get-iso19115-3.2018-localised">
                    <xsl:with-param name="langId" select="$langId"/>
                  </xsl:call-template>
                </xsl:for-each>
                <xsl:if test="position() != last()"><xsl:comment select="'.'"/>-<xsl:comment select="'.'"/></xsl:if>
              </xsl:for-each>

              <br/>
              <a href="{$landingPageUrl}">
                <xsl:value-of select="$landingPageUrl"/>
              </a>
              <br/>
            </p>
          </div>
        </div>
      </blockquote>
    </xsl:if>
  </xsl:template>

  <!-- Data quality section is rendered in a table / Disabled
  <xsl:template mode="render-field"
                match="mdb:dataQualityInfo[1]"
                priority="9999">
    <div data-gn-data-quality-measure-renderer="{$metadataId}"/>
  </xsl:template>

  <xsl:template mode="render-field"
                match="mdb:dataQualityInfo[position() > 1]"
                priority="9999"/>-->


  <!-- Most of the elements are ... -->
  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field"
                match="*[gco:CharacterString = '']|*[gco:Integer = '']|
                       *[gco:Decimal = '']|*[gco:Boolean = '']|
                       *[gco:Real = '']|*[gco:Measure = '']|*[gco:Length = '']|
                       *[gco:Distance = '']|*[gco:Angle = '']|*[gco:Scale = '']|
                       *[gco:Record = '']|*[gco:RecordType = '']|
                       *[gco:LocalName = '']|*[lan:PT_FreeText = '']|
                       *[gml:beginPosition = '']|*[gml:endPosition = '']|
                       gml:description[. != '']|gml:timePosition[. != '']|
                       *[gco:Date = '']|*[gco:DateTime = '']|*[gco:TM_PeriodDuration = '']"
                priority="500"/>
  <xsl:template mode="render-field"
                match="*[gco:CharacterString != '']|*[gcx:Anchor != '']|
                       *[gco:Integer != '']|
                       *[gco:Decimal != '']|*[gco:Boolean != '']|
                       *[gco:Real != '']|*[gco:Measure != '']|*[gco:Length != '']|
                       *[gco:Distance != '']|*[gco:Angle != '']|*[gco:Scale != '']|
                       *[gco:Record != '']|*[gco:RecordType != '']|
                       *[gco:LocalName != '']|*[lan:PT_FreeText != '']|
                       *[gml:beginPosition != '']|*[gml:endPosition != '']|
                       *[gco:Date != '']|*[gco:DateTime != '']|*[gco:TM_PeriodDuration != '']|
                       *[*/@codeListValue]|*[@codeListValue]|
                       gml:identifier[. != '']|gml:name[. != '']|
                       gml:description[. != '']|gml:timePosition[. != '']|
                       gml:beginPosition[. != '']|gml:endPosition[. != '']"
                priority="500">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <xsl:variable name="elementName" select="if (@codeListValue) then name(..) else name(.)"/>
    <dl>
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="fieldName" select="$fieldName"/>
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:choose>
          <xsl:when test="@codeListValue|*/@codeListValue">
            <!-- Do not render codeList text element. -->
            <xsl:apply-templates mode="render-value" select="@codeListValue|*/@codeListValue"/>
          </xsl:when>
          <!-- Display the value for simple field eg. gml:beginPosition. -->
          <xsl:when test="count(*) = 0 and not(*/@codeListValue)">
            <xsl:apply-templates mode="render-value" select="text()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="render-value" select="."/>
          </xsl:otherwise>
        </xsl:choose><xsl:comment select="'.'"/>
        <xsl:apply-templates mode="render-value" select="@*"/>
      </dd>
    </dl>
  </xsl:template>



  <xsl:template name="render-field-label">
    <xsl:param name="fieldName" select="''" as="xs:string" required="no"/>
    <xsl:param name="languages" as="node()*" required="no"/>
    <xsl:param name="contextLabel" as="attribute()?" required="no"/>

    <xsl:variable name="name"
                  select="name()"/>

    <xsl:variable name="context"
                  select="name(..)"/>

    <xsl:choose>
      <!-- eg. for codelist, display label in all record languages -->
      <xsl:when test="$fieldName = '' and $language = 'all' and count($languages/lang) > 0">
        <xsl:for-each select="$languages/lang">
          <div xml:lang="{@code}">
            <xsl:value-of select="tr:nodeLabel(tr:create($schema, @code), $name, $context)"/>
            <xsl:if test="$contextLabel">
              <xsl:variable name="extraLabel">
                <xsl:apply-templates mode="render-value"
                                     select="$contextLabel">
                  <xsl:with-param name="forcedLanguage" select="@code"/>
                </xsl:apply-templates>
              </xsl:variable>
              <xsl:value-of select="concat(' (', $extraLabel, ')')"/>
            </xsl:if>
          </div>
        </xsl:for-each>
      </xsl:when>
      <!-- eg. for multilingual element, display label in all translations -->
      <xsl:when test="$fieldName = '' and $language = 'all' and lan:PT_FreeText">
        <xsl:for-each select="lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[. != '']">
          <xsl:variable name="id"
                        select="replace(@locale, '#', '')"/>
          <xsl:variable name="lang3"
                        select="$metadata//mdb:otherLocale/*[@id = $id]/lan:languageCode/*/@codeListValue"/>
          <div xml:lang="{$lang3}">
            <xsl:value-of select="tr:nodeLabel(tr:create($schema, $lang3), $name, $context)"/>
          </div>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <!-- Overriden label or element name in current UI language. -->
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:nodeLabel(tr:create($schema), $name, $context)"/>
        <xsl:if test="$contextLabel">
          <xsl:variable name="extraLabel">
            <xsl:apply-templates mode="render-value"
                                 select="$contextLabel"/>
          </xsl:variable>
          <xsl:value-of select="concat(' (', $extraLabel, ')')"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Some elements are only containers so bypass them
  unless they are flat mode exceptions -->
  <xsl:template mode="render-field"
                match="*[
                          count(*[name() != 'lan:PT_FreeText']) = 1 and
                          count(*/@codeListValue) = 0
                          ]"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <xsl:apply-templates mode="render-value" select="@*"/>
    <xsl:apply-templates mode="render-field" select="*">
      <xsl:with-param name="fieldName" select="$fieldName"/>
    </xsl:apply-templates>
  </xsl:template>



  <!-- Some major sections are boxed but
  * if part of fieldsWithFieldset exception
  * has content
  * only if more than one child to be displayed (non flat mode only) bypass container elements
  . -->
  <xsl:template mode="render-field"
                match="*[$isFlatMode = true() and not(gco:CharacterString) and (
                            name() = $configuration/editor/fieldsWithFieldset/name
                            or @gco:isoType = $configuration/editor/fieldsWithFieldset/name)]|
                       *[$isFlatMode = false() and not(gco:CharacterString) and (
                            name() = $configuration/editor/fieldsWithFieldset/name
                            or @gco:isoType = $configuration/editor/fieldsWithFieldset/name
                            or count(*) > 1)]"
                priority="100">

    <xsl:variable name="content">
      <xsl:apply-templates mode="render-field" select="*"/>
    </xsl:variable>

    <xsl:if test="count($content/*) > 0">
      <div class="entry name">
        <h2>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
          <xsl:apply-templates mode="render-value"
                               select="@*"/><xsl:comment select="'.'"/>
        </h2>
        <div class="target"><xsl:comment select="'.'"/>
          <xsl:apply-templates mode="render-field" select="*"/>
        </div>
      </div>
    </xsl:if>
  </xsl:template>



  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="gex:EX_GeographicBoundingBox[
                            gex:westBoundLongitude/gco:Decimal != '']" priority="100">
    <xsl:copy-of select="gn-fn-render:bbox(
                            xs:double(gex:westBoundLongitude/gco:Decimal),
                            xs:double(gex:southBoundLatitude/gco:Decimal),
                            xs:double(gex:eastBoundLongitude/gco:Decimal),
                            xs:double(gex:northBoundLatitude/gco:Decimal))"/>
    <br/>
    <br/>
  </xsl:template>


  <xsl:template mode="render-field"
                match="gex:EX_BoundingPolygon/gex:polygon"
                priority="100">
    <xsl:copy-of select="gn-fn-render:extent($metadataUuid,
        count(ancestor::mri:extent/preceding-sibling::mri:extent/*/*[local-name() = 'geographicElement']/*) +
        count(../../preceding-sibling::gex:geographicElement) + 1)"/>
    <br/>
    <br/>
  </xsl:template>

  <!-- Display spatial extents containing bounding polygons on a map -->

  <xsl:template mode="render-field"
                match="gex:EX_Extent"
                priority="100">
    <div class="entry name">
    <h4>
      <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      <xsl:apply-templates mode="render-value"
                           select="@*"/>
    </h4>
    <div class="target">
      <xsl:apply-templates mode="render-field"
                           select="gex:*"/>
      </div>
    </div>
  </xsl:template>


  <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*[cit:CI_Responsibility]"
                priority="100">
    <div class="gn-contact">
      <h4>
        <i class="fa fa-envelope"><xsl:comment select="'.'"/></i>
        <xsl:apply-templates mode="render-value"
                             select="*/cit:role/*/@codeListValue"/>
      </h4>



      <xsl:for-each select="*/cit:party/(cit:CI_Organisation|cit:CI_Individual)">
        <!-- Display name is <org name> - <individual name> (<position name>) -->
        <xsl:variable name="displayName">
          <xsl:choose>
            <xsl:when
              test="name(.) = 'cit:CI_Organisation'">
              <!-- Org name may be multilingual -->
              <xsl:apply-templates mode="render-value"
                                   select="cit:name"/>
              <xsl:if test="cit:individual">
                <xsl:text> - </xsl:text>
              </xsl:if>
              <xsl:for-each select="cit:individual">
                <xsl:apply-templates mode="get-display-name" select="cit:CI_Individual"/>
                <xsl:if test="position() != last()">
                  <xsl:text>, </xsl:text>
                </xsl:if>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates mode="get-display-name" select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:variable name="email">
          <xsl:for-each select=".//cit:electronicMailAddress[not(gco:nilReason)]">
            <xsl:apply-templates mode="render-value" select="."/>
            <xsl:if test="position() != last()">,<xsl:comment select="'.'"/></xsl:if>
          </xsl:for-each>
        </xsl:variable>


        <div class="row">
          <div class="col-md-6">
            <!-- Needs improvements as contact/org are more flexible in iso19115-3.2018 -->
            <address>
              <strong>
                <xsl:choose>
                  <xsl:when test="normalize-space($email) != ''">
                    <a href="mailto:{normalize-space($email)}">
                      <xsl:value-of select="$displayName"/><xsl:comment select="'.'"/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$displayName"/><xsl:comment select="'.'"/>
                  </xsl:otherwise>
                </xsl:choose>
              </strong><br/>
              <xsl:for-each select=".//cit:contactInfo/*">
                <xsl:for-each select="cit:address/*/(
                                            cit:deliveryPoint|cit:city|
                                            cit:administrativeArea|cit:postalCode|cit:country)">
                  <div>
                    <xsl:if test="normalize-space(.) != ''">
                      <xsl:apply-templates mode="render-value" select="."/><br/>
                    </xsl:if>
                  </div>
                </xsl:for-each>
              </xsl:for-each>
            </address>
          </div>
          <div class="col-md-6">
            <xsl:for-each select=".//cit:contactInfo/*">
              <address>
                <xsl:for-each select="cit:phone/*[cit:numberType/*/@codeListValue = 'voice']/cit:number[normalize-space(.) != '']">
                  <div>
                    <xsl:variable name="phoneNumber">
                      <xsl:apply-templates mode="render-value" select="."/>
                    </xsl:variable>
                    <i class="fa fa-phone"><xsl:comment select="'.'"/></i>
                    <a href="tel:{$phoneNumber}">
                      <xsl:value-of select="$phoneNumber"/>
                    </a>
                  </div>
                </xsl:for-each>
                <xsl:for-each select="cit:phone/*[cit:numberType/*/@codeListValue != 'voice']/cit:number[normalize-space(.) != '']">
                  <div>
                    <xsl:variable name="phoneNumber">
                      <xsl:apply-templates mode="render-value" select="."/>
                    </xsl:variable>
                    <i class="fa fa-fax"><xsl:comment select="'.'"/></i>
                    <a href="tel:{normalize-space($phoneNumber)}">
                      <xsl:value-of select="normalize-space($phoneNumber)"/><xsl:comment select="'.'"/>(<xsl:value-of select="../cit:numberType/*/@codeListValue"/>)
                    </a>
                  </div>
                </xsl:for-each>
                <xsl:for-each select="cit:onlineResource/*/cit:linkage[normalize-space(.) != '']">
                  <xsl:variable name="linkDescription">
                    <xsl:apply-templates mode="render-value"
                                         select="../cit:description"/>
                  </xsl:variable>
                  <xsl:variable name="linkage">
                    <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:variable>
                  <i class="fa fa-link"><xsl:comment select="'.'"/></i>
                  <a href="{normalize-space($linkage)}" target="_blank">
                    <xsl:value-of select="if (../cit:name)
                                          then ../cit:name/* else
                                          normalize-space(linkage)"/><xsl:comment select="'.'"/>
>                 </a>
                  <p>
                    <xsl:value-of select="normalize-space($linkDescription)"/>
                  </p>
                </xsl:for-each>
                <xsl:for-each select="cit:hoursOfService">
                  <span>
                    <xsl:apply-templates mode="render-field"
                                         select="."/>
                  </span>
                </xsl:for-each>
                <xsl:apply-templates mode="render-field"
                                     select="cit:contactInstructions"/>
              </address>
            </xsl:for-each>
          </div>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>

  <!-- Format CT_Individual element for display -->
  <!-- Display name is <individual name> (<position name>) -->
  <xsl:template mode="get-display-name" match="cit:CI_Individual">
    <xsl:if test="cit:name">
      <xsl:apply-templates mode="render-value"
                           select="cit:name"/>
    </xsl:if>
    <xsl:if test="cit:positionName">
      <xsl:if test="cit:name">
        <xsl:text> (</xsl:text>
      </xsl:if>
      <xsl:apply-templates mode="render-value"
                            select="cit:positionName"/>
      <xsl:if test="cit:name">
        <xsl:text>)</xsl:text>
      </xsl:if>
    </xsl:if>

  </xsl:template>

  <!-- Metadata linkage -->
  <xsl:template mode="render-field"
                match="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code"
                priority="100">
    <dl class="gn-link">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="*"/>
        <xsl:apply-templates mode="render-value" select="@*"/>

        <a class="btn btn-link" href="{$nodeUrl}api/records/{$metadataId}/formatters/xml" target="_blank">
          <i class="fa fa-file-code-o fa-2x"><xsl:comment select="'.'"/></i>
          <span><xsl:value-of select="$schemaStrings/metadataInXML"/></span>
        </a>
      </dd>
    </dl>
  </xsl:template>

  <!-- Linkage -->
  <xsl:template mode="render-field"
                match="*[cit:CI_OnlineResource and */cit:linkage/* != '']"
                priority="100">
    <dl class="gn-link">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:variable name="linkDescription">
          <xsl:apply-templates mode="render-value"
                               select="*/cit:description"/>
        </xsl:variable>
        <a href="{*/cit:linkage/*}" target="_blank">
          <xsl:apply-templates mode="render-value"
                               select="if (*/cit:name != '') then */cit:name else */cit:linkage"/><xsl:comment select="'.'"/>
        </a>
        <p>
          <xsl:value-of select="normalize-space($linkDescription)"/>
        </p>
      </dd>
    </dl>
  </xsl:template>

  <!-- Identifier -->
  <xsl:template mode="render-field"
                match="*[(mcc:RS_Identifier or mcc:MD_Identifier) and
                       */mcc:code/gco:CharacterString != '']"
                priority="100">
    <dl class="gn-code">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <xsl:if test="*/mcc:codeSpace">
          <xsl:variable name="prefix">
            <xsl:apply-templates mode="render-value"
                                 select="*/mcc:codeSpace"/>
          </xsl:variable>
          <xsl:value-of select="if(ends-with($prefix, '/')) then $prefix else concat($prefix, '/')"/>
        </xsl:if>
        <xsl:apply-templates mode="render-value"
                               select="*/mcc:code"/>
        <p>
          <xsl:apply-templates mode="render-field"
                               select="*/mcc:authority"/>
        </p>
      </dd>
    </dl>
  </xsl:template>



  <!-- Display thesaurus name and the list of keywords -->
  <xsl:template mode="render-field"
                match="mri:descriptiveKeywords[
                  normalize-space(string-join(*/mri:keyword//text(), '')) = ''
                  or count(*/mri:keyword) = 0]" priority="200"/>
  <xsl:template mode="render-field"
                match="mri:descriptiveKeywords[
                        */mri:thesaurusName/cit:CI_Citation/cit:title]"
                priority="100">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl class="gn-keyword">
      <dt>
        <xsl:choose>
          <xsl:when test="$fieldName != ''"><xsl:value-of select="$fieldName"/></xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="render-value"
                                 select="*/mri:thesaurusName/cit:CI_Citation/cit:title/*"/>
          </xsl:otherwise>
        </xsl:choose>

        <!--<xsl:if test="*/mri:type/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/mri:type/*/@codeListValue"/>)
        </xsl:if>-->
      </dt>
      <dd>
        <div>
          <ul>
            <xsl:for-each select="*/mri:keyword">
              <li>
                <xsl:apply-templates mode="render-value"
                                     select="."/>
              </li>
            </xsl:for-each>
          </ul>
        </div>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="mri:descriptiveKeywords[not(*/mri:thesaurusName/cit:CI_Citation/cit:title)]"
                priority="100">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl class="gn-keyword">
      <dt>
        <xsl:variable name="thesaurusType">
          <xsl:apply-templates mode="render-value"
                               select="*/mri:type/*/@codeListValue[. != '']"/>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="$fieldName != ''">
            <xsl:value-of select="$fieldName"/>
          </xsl:when>
          <xsl:when test="$thesaurusType != ''">
            <xsl:copy-of select="$thesaurusType"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$schemaStrings/noThesaurusName"/>
          </xsl:otherwise>
        </xsl:choose>
      </dt>
      <dd>
        <div>
          <ul>
            <xsl:for-each select="*/mri:keyword">
              <li>
                <xsl:apply-templates mode="render-value"
                                     select="."/>
              </li>
            </xsl:for-each>
          </ul>
        </div>
      </dd>
    </dl>
  </xsl:template>

  <xsl:template mode="render-field"
                match="mrd:distributionFormat[1]"
                priority="100">
    <dl class="gn-format">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/mrd:distributionFormat">
            <li>
              <xsl:apply-templates mode="render-value"
                                   select="*/mrd:formatSpecificationCitation/*/
                                    cit:title"/>
              <p>
              <xsl:apply-templates mode="render-field"
                      select="*/(mrd:amendmentNumber|
                              mrd:fileDecompressionTechnique|
                              mrd:medium|
                              mrd:formatDistributor)"/>
              </p>
            </li>
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="mrd:distributionFormat[position() > 1]"
                priority="100"/>

  <!-- Date -->
  <xsl:template mode="render-field"
                match="cit:date|mdb:dateInfo"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
          <xsl:with-param name="contextLabel" select="*/cit:dateType/*/@codeListValue[. != '']"/>
        </xsl:call-template>
      </dt>
      <dd>
          <xsl:apply-templates mode="render-value"
                               select="*/cit:date/*"/>
      </dd>
    </dl>
  </xsl:template>


  <!-- Enumeration -->
  <xsl:template mode="render-field"
                match="mri:topicCategory[1]|
                       mex:MD_ObligationCode[1]|
                       msr:MD_PixelOrientationCode[1]|
                       srv:SV_ParameterDirection[1]|
                       reg:RE_AmendmentType[1]"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:call-template name="render-field-label">
          <xsl:with-param name="languages" select="$allLanguages"/>
        </xsl:call-template>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/(mri:topicCategory|mex:MD_ObligationCode|
                msr:MD_PixelOrientationCode|srv:SV_ParameterDirection|
                reg:RE_AmendmentType)">
            <li>
            <xsl:apply-templates mode="render-value"
                                 select="*"/>
            </li>
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="mri:topicCategory[position() > 1]|
                       mex:MD_ObligationCode[position() > 1]|
                       msr:MD_PixelOrientationCode[position() > 1]|
                       srv:SV_ParameterDirection[position() > 1]|
                       reg:RE_AmendmentType[position() > 1]"
                priority="100"/>


  <!-- Link to other metadata records -->
  <xsl:template mode="render-field"
                match="srv:operatesOn[@uuidref]|mrc:featureCatalogueCitation[@uuidref]|mrl:source[@uuidref]|mri:metadataReference[@uuidref]"
                priority="100">
    <xsl:variable name="nodeName" select="name()"/>

    <!-- Only render the first element of this kind and render a list of
    following siblings. -->
    <xsl:variable name="isFirstOfItsKind"
                  select="count(preceding-sibling::node()[name() = $nodeName]) = 0"/>
    <xsl:if test="$isFirstOfItsKind">
      <dl class="gn-md-associated-resources">
        <dt>
          <xsl:call-template name="render-field-label">
            <xsl:with-param name="languages" select="$allLanguages"/>
          </xsl:call-template>
        </dt>
        <dd>
          <ul>
            <xsl:for-each select="parent::node()/*[name() = $nodeName]">
              <li>
                <a href="{$nodeUrl}api/records/{@uuidref}">
                  <i class="fa fa-link"><xsl:comment select="'.'"/></i>
                  <xsl:value-of select="gn-fn-render:getMetadataTitle(@uuidref, $language)"/>
                </a>
              </li>
            </xsl:for-each>
          </ul>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field"
                match="*">
    <xsl:param name="fieldName" select="''" as="xs:string"/>
    <xsl:apply-templates mode="render-field">
      <xsl:with-param name="fieldName" select="$fieldName"/>
    </xsl:apply-templates>
  </xsl:template>







  <!-- ########################## -->
  <!-- Render values for text ... -->

  <xsl:template mode="render-value"
                match="*[gco:CharacterString]">

    <xsl:apply-templates mode="localised" select=".">
      <xsl:with-param name="langId" select="$langId"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Integer|gco:Decimal|
                       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Angle|
                       gco:Scale|gco:Record|gco:RecordType|
                       gco:LocalName|gml:beginPosition|gml:endPosition">
    <xsl:choose>
      <xsl:when test="contains(., 'http')">
        <!-- Replace hyperlink in text by an hyperlink -->
        <xsl:variable name="textWithLinks"
                      select="replace(., '([a-z][\w-]+:/{1,3}[^\s()&gt;&lt;]+[^\s`!()\[\]{};:'&apos;&quot;.,&gt;&lt;?«»“”‘’])',
                                    '&lt;a target=''_blank'' href=''$1''&gt;$1&lt;/a&gt;')"/>

        <xsl:if test="$textWithLinks != ''">
          <xsl:copy-of select="saxon:parse(
                          concat('&lt;p&gt;',
                          replace($textWithLinks, '&amp;', '&amp;amp;'),
                          '&lt;/p&gt;'))"/>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space(.)"/>
      </xsl:otherwise>
    </xsl:choose>


    <xsl:if test="@uom">
      <xsl:comment select="'.'"/>&#160;<xsl:value-of select="@uom"/>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="render-value"
                match="*[gcx:Anchor]">
    <xsl:apply-templates mode="render-value"
                         select="gcx:Anchor"/>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gcx:Anchor">
    <xsl:variable name="link"
                  select="@xlink:href"/>
    <xsl:variable name="txt">
      <xsl:apply-templates mode="localised" select="..">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$link != ''">
        <a href="{$link}">
          <xsl:value-of select="$txt"/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$txt"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="render-value"
                match="lan:PT_FreeText">
    <xsl:apply-templates mode="localised" select="../node()">
      <xsl:with-param name="langId" select="$language"/>
    </xsl:apply-templates>
  </xsl:template>




  <xsl:template mode="render-value"
                match="*[gco:Distance|gco:Measure]">
    <xsl:apply-templates mode="render-value"
                         select="gco:Distance|gco:Measure"/>
  </xsl:template>
  <xsl:template mode="render-value"
                match="gco:Distance|gco:Measure">
    <span><xsl:value-of select="."/>&#160;<xsl:value-of select="@uom"/></span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="*[gco:TM_PeriodDuration != '']"
                priority="99">
    <div data-gn-field-duration-div="{gco:TM_PeriodDuration}"><xsl:value-of select="gco:TM_PeriodDuration"/></div>
  </xsl:template>


  <!-- ... Dates - formatting is made on the client side by the directive  -->
  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}')]">
    <span data-gn-humanize-time="{.}" data-format="YYYY">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}" data-format="MMM YYYY">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]
                      |gml:beginPosition[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]
                      |gml:endPosition[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:DateTime[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date|gco:DateTime">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>


  <xsl:template mode="render-value"
                match="lan:language/*/@codeListValue">
    <xsl:variable name="label"
                  select="util:getIsoLanguageLabel(., .)"/>
    <xsl:value-of select="if ($label != '') then $label else ."/>
  </xsl:template>

  <!-- ... Codelists -->
  <xsl:template mode="render-value"
                match="@codeListValue|
                       @indeterminatePosition|
                       mri:MD_TopicCategoryCode|
                       mex:MD_ObligationCode|
                       msr:MD_PixelOrientationCode|
                       srv:SV_ParameterDirection|
                       reg:RE_AmendmentType">
    <xsl:param name="forcedLanguage" select="''" required="no"/>

    <xsl:variable name="id" select="."/>
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="parentName"
                  select="if (. instance of attribute())
                          then parent::node()/local-name()
                          else local-name()"/>

    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            if ($forcedLanguage = '') then tr:create($schema) else tr:create($schema, $forcedLanguage),
                            if ($name = 'indeterminatePosition') then 'indeterminatePosition' else $parentName,
                            $id)"/>
    <xsl:choose>
      <!-- eg. for codelist, display label in all record languages -->
      <xsl:when test="$forcedLanguage = '' and $language = 'all' and count($allLanguages/lang) > 0">
        <xsl:for-each select="$allLanguages/lang">
          <xsl:variable name="codelistTranslation"
                        select="tr:codelist-value-label(
                            tr:create($schema, @code),
                            if ($name = 'indeterminatePosition') then 'indeterminatePosition' else $parentName,
                            $id)"/>

          <div xml:lang="{@code}"
               title="{if ($codelistTranslation != '') then tr:codelist-value-desc(
                            tr:create($schema),
                            $parentName, $id) else ''}">
            <xsl:value-of select="if ($codelistTranslation != '') then $codelistTranslation else $id"/>
          </div>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$codelistTranslation != ''">
        <xsl:variable name="codelistDesc"
                      select="tr:codelist-value-desc(
                            if ($forcedLanguage = '') then tr:create($schema) else tr:create($schema, $forcedLanguage),
                            $parentName, $id)"/>
        <span title="{$codelistDesc}">
          <xsl:value-of select="$codelistTranslation"/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template mode="render-value"
                match="@gco:nilReason[. = 'withheld']"
                priority="100">
    <i class="fa fa-lock text-warning" title="{{{{'withheld' | translate}}}}"><xsl:comment select="'.'"/></i>
  </xsl:template>
  <xsl:template mode="render-value"
                match="@*"/>

</xsl:stylesheet>
