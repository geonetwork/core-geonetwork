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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:xslUtils="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
  <!-- This formatter render an ISO19139 record based on the
  editor configuration file.


  The layout is made in 2 modes:
  * render-field taking care of elements (eg. sections, label)
  * render-value taking care of element values (eg. characterString, URL)

  3 levels of priority are defined: 100, 50, none

  -->
 <!-- tr is defined at  core-geonetwork/services/src/main/java/org/fao/geonet/api/records/formatters/SchemaLocalizations.java -->

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
  <xsl:include href="../../formatter/jsonld/iso19139-to-jsonld.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/gmd:MD_Metadata"/>

  <xsl:variable name="langId" select="gn-fn-iso19139:getLangId($metadata, $language)"/>


  <!-- Ignore some fields displayed in header or in right column -->
  <xsl:template mode="render-field"
                match="gmd:graphicOverview|gmd:abstract|gmd:title"
                priority="2000"/>

  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="gmd:MD_Metadata">
    <xsl:for-each select="gmd:identificationInfo/*/gmd:citation/*/gmd:title">
      <xsl:call-template name="localised">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="gmd:MD_Metadata">
    <xsl:for-each select="gmd:identificationInfo/*/gmd:abstract">

      <xsl:variable name="txt">
        <xsl:call-template name="localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="addLineBreaksAndHyperlinks">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="getMetadataHierarchyLevel" match="gmd:MD_Metadata">
    <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
  </xsl:template>

  <xsl:template mode="getMetadataThumbnail" match="gmd:MD_Metadata">
    <xsl:value-of select="gmd:identificationInfo/*/gmd:graphicOverview[1]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
  </xsl:template>

  <xsl:template mode="getOverviews" match="gmd:MD_Metadata">
    <section class="gn-md-side-overview">
      <h2>
        <i class="fa fa-fw fa-image"><xsl:comment select="'image'"/></i>
        <span><xsl:comment select="name()"/>
          <xsl:value-of select="$schemaStrings/overviews"/>
        </span>
      </h2>

      <xsl:for-each select="gmd:identificationInfo/*/gmd:graphicOverview/*">
        <img data-gn-img-modal="md"
             class="gn-img-thumbnail center-block"
             alt="{$schemaStrings/overview}"
             src="{gmd:fileName/*}"/>

        <xsl:for-each select="gmd:fileDescription">
          <div class="gn-img-thumbnail-caption">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </div>
        </xsl:for-each>

      </xsl:for-each>
    </section>
  </xsl:template>

  <xsl:template mode="getMetadataHeader" match="gmd:MD_Metadata">
    <div class="gn-abstract">
      <xsl:for-each select="gmd:identificationInfo/*/gmd:abstract">
        <xsl:variable name="txt">
          <xsl:call-template name="localised">
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
                             select="$metadata"/>
      </script>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="getMetadataCitation" match="gmd:MD_Metadata">
    <!-- Citation -->
    <blockquote>
      <div class="row">
        <div class="col-md-3">
          <i class="fa fa-quote-left pull-right"><xsl:comment select="'icon'"/></i>
        </div>
        <div class="col-md-9">
          <h2 title="{$schemaStrings/citationProposal-help}"><xsl:comment select="name()"/>
            <xsl:value-of select="$schemaStrings/citationProposal"/>
            <i class="fa fa-info-circle"><xsl:comment select="'icon'"/></i>
          </h2>
          <br/>
          <p>
            <!-- Custodians -->
            <xsl:for-each select="gmd:identificationInfo/*/gmd:pointOfContact/
                              *[gmd:role/*/@codeListValue = ('custodian', 'author')]">
              <xsl:variable name="name">
                <xsl:for-each select="gmd:individualName">
                  <xsl:call-template name="localised">
                    <xsl:with-param name="langId" select="$langId"/>
                  </xsl:call-template>
                </xsl:for-each>
              </xsl:variable>

              <xsl:value-of select="$name"/>
              <xsl:if test="$name != ''">&#160;(</xsl:if>
              <xsl:for-each select="gmd:organisationName">
                <xsl:call-template name="localised">
                  <xsl:with-param name="langId" select="$langId"/>
                </xsl:call-template>
              </xsl:for-each>
              <xsl:if test="$name">)</xsl:if>
              <xsl:if test="position() != last()">&#160;-&#160;</xsl:if>
            </xsl:for-each>

            <!-- Publication year: use last publication date -->
            <xsl:variable  name="publicationDate">
              <xsl:for-each select="gmd:identificationInfo/*/gmd:citation/*/gmd:date/*[
                                gmd:dateType/*/@codeListValue = 'publication']/
                                  gmd:date/gco:*">
                <xsl:sort select="." order="descending" />

                <xsl:if test="position() = 1">
                  <xsl:value-of select="." />
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>

            <xsl:if test="normalize-space($publicationDate) != ''">
              (<xsl:value-of select="substring(normalize-space($publicationDate), 1, 4)"/>)
            </xsl:if>

            <xsl:text>. </xsl:text>

            <!-- Title -->
            <xsl:for-each select="gmd:identificationInfo/*/gmd:citation/*/gmd:title">
              <xsl:call-template name="localised">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:call-template>
            </xsl:for-each>
            <xsl:text>. </xsl:text>

            <!-- Publishers -->
            <xsl:for-each select="gmd:identificationInfo/*/gmd:pointOfContact/
                              *[gmd:role/*/@codeListValue = 'publisher']/gmd:organisationName">
              <xsl:call-template name="localised">
                <xsl:with-param name="langId" select="$langId"/>
              </xsl:call-template>
              <xsl:if test="position() != last()">&#160;-&#160;</xsl:if>
            </xsl:for-each>

            <br/>
            <!-- Link -->
            <xsl:variable name="url"
                          select="concat($nodeUrl, 'api/records/', $metadataUuid)"/>
            <a href="{url}">
              <xsl:value-of select="$url"/>
            </a>
            <br/>
          </p>
        </div>
      </div>
    </blockquote>
  </xsl:template>

  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field"
                match="*[gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|
       gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gmd:PT_FreeText|
       gco:Date|gco:DateTime|*/@codeListValue]"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <xsl:if test="normalize-space(string-join(*|*/@codeListValue, '')) != ''">
      <dl>
        <dt>
          <xsl:value-of select="if ($fieldName)
                                  then $fieldName
                                  else tr:nodeLabel(tr:create($schema), name(), null)"/>
        </dt>
        <dd><xsl:comment select="name()"/>
          <xsl:apply-templates mode="render-value" select="*|*/@codeListValue"/>
          <xsl:apply-templates mode="render-value" select="@*"/>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Template for boolean fields that can be empty: no gco:Boolean subelement and @gco:nilReason attribute -->
  <!-- Uncomment and add required fields to be handled in the match clause -->
  <!--<xsl:template mode="render-field" match="gmd:pass[@gco:nilReason and not(gco:Boolean)]"
                priority="100">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                  then $fieldName
                                  else tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:value-of select="$schemaStrings/nilValue"/>
      </dd>
    </dl>
  </xsl:template>-->

  <xsl:template mode="render-field"
                match="*[gmx:Anchor]|*[normalize-space(gco:CharacterString) != '']|gml:beginPosition[. != '']|gml:endPosition[. != '']|gml320:beginPosition[. != '']|gml320:endPosition[. != '']"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:comment select="name()"/>
        <xsl:apply-templates mode="render-value" select="."/>
        <xsl:apply-templates mode="render-value" select="@*"/>
      </dd>
    </dl>
  </xsl:template>

  <!-- Some elements are only containers so bypass them -->
  <xsl:template mode="render-field"
                match="*[
                          count(gmd:*[name() != 'gmd:PT_FreeText']) = 1 and
                          count(*/@codeListValue) = 0
                        ]"
                priority="50">

    <xsl:apply-templates mode="render-value" select="@*"/>
    <xsl:apply-templates mode="render-field" select="*"/>
  </xsl:template>


  <!-- Some major sections are boxed -->
  <xsl:template mode="render-field"
                match="*[name() = $configuration/editor/fieldsWithFieldset/name
    or @gco:isoType = $configuration/editor/fieldsWithFieldset/name]|
      gmd:report/*|
      gmd:result/*|
      gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
      *[$isFlatMode = false() and gmd:* and
        not(gco:CharacterString) and not(gmd:URL)]">

    <div class="entry name">
      <h2>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
        <xsl:apply-templates mode="render-value"
                             select="@*"/>
      </h2>
      <div class="target"><xsl:comment select="name()"/>
        <xsl:choose>
          <xsl:when test="count(*) > 0">
            <xsl:apply-templates mode="render-field" select="*"/>
          </xsl:when>
          <xsl:otherwise>
            No information provided.
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>

  </xsl:template>


  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="gmd:EX_GeographicBoundingBox[
          gmd:westBoundLongitude/gco:Decimal != '']">
    <xsl:copy-of select="gn-fn-render:bbox(
                            xs:double(gmd:westBoundLongitude/gco:Decimal),
                            xs:double(gmd:southBoundLatitude/gco:Decimal),
                            xs:double(gmd:eastBoundLongitude/gco:Decimal),
                            xs:double(gmd:northBoundLatitude/gco:Decimal))"/>

    <br/>
    <br/>
  </xsl:template>


  <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_ResponsibleParty]"
                priority="100">
    <xsl:variable name="email">
      <xsl:for-each select="*/gmd:contactInfo/
                                      */gmd:address/*/gmd:electronicMailAddress">
        <xsl:apply-templates mode="render-value"
                             select="."/><xsl:if test="position() != last()">, </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="role" select="*/gmd:role/gmd:CI_RoleCode/@codeListValue" />

    <!-- Display name is <org name> - <individual name> (<position name> -->
    <xsl:variable name="displayName">
      <xsl:choose>
        <xsl:when
          test="*/gmd:organisationName and */gmd:individualName">
          <!-- Org name may be multilingual -->
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:organisationName"/>
          - <xsl:apply-templates mode="render-value"
                  select="*/gmd:individualName"/>
            <xsl:if test="*/gmd:positionName">
              (<xsl:apply-templates mode="render-value" select="*/gmd:positionName"/>)
            </xsl:if>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="*/gmd:organisationName|*/gmd:individualName"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <div class="gn-contact">
      <strong>
        <i class="fa fa-envelope"><xsl:comment select="'email'"/></i>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:role/*/@codeListValue"/>
      </strong>
      <div class="row">
        <div class="col-md-6">
          <address>
              <xsl:choose>
                <xsl:when test="$email">
                  <a href="mailto:{normalize-space($email)}">
                    <xsl:copy-of select="$displayName"/><xsl:comment select="'email'"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:copy-of select="$displayName"/><xsl:comment select="'name'"/>
                </xsl:otherwise>
              </xsl:choose>
            <br/>
            <xsl:for-each select="*/gmd:contactInfo/*">
              <xsl:for-each select="gmd:address/*">
                <div><xsl:comment select="'address'"/>
                  <xsl:for-each select="gmd:deliveryPoint[normalize-space(.) != '']">
                      <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:for-each>
                  <xsl:for-each select="gmd:city[normalize-space(.) != '']">
                      <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:for-each>
                  <xsl:for-each select="gmd:administrativeArea[normalize-space(.) != '']">
                      <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:for-each>
                  <xsl:for-each select="gmd:postalCode[normalize-space(.) != '']">
                      <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:for-each>
                  <xsl:for-each select="gmd:country[normalize-space(.) != '']">
                      <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:for-each>
                </div>
                <br/>
              </xsl:for-each>
            </xsl:for-each>
          </address>
        </div>
        <div class="col-md-6">
          <address>
            <xsl:for-each select="*/gmd:contactInfo/*">
              <xsl:for-each select="gmd:phone/*/gmd:voice[normalize-space(.) != '']">
                  <xsl:variable name="phoneNumber">
                    <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:variable>
                  <i class="fa fa-phone"><xsl:comment select="'phone'"/></i>
                  <a href="{translate($phoneNumber,' ','')}">
                    <xsl:value-of select="$phoneNumber"/>
                  </a>
              </xsl:for-each>
              <xsl:for-each select="gmd:phone/*/gmd:facsimile[normalize-space(.) != '']">
                <xsl:variable name="phoneNumber">
                  <xsl:apply-templates mode="render-value" select="."/>
                </xsl:variable>
                <i class="fa fa-fax"><xsl:comment select="'fax'"/></i>
                <a href="{translate($phoneNumber,' ','')}">
                  <xsl:value-of select="normalize-space($phoneNumber)"/>
                </a>
              </xsl:for-each>
              <xsl:for-each select="gmd:onlineResource/*/gmd:linkage/gmd:URL[normalize-space(.) != '']">
                <xsl:variable name="web">
                  <xsl:apply-templates mode="render-value" select="."/></xsl:variable>
                <i class="fa fa-link"><xsl:comment select="'link'"/></i>
                <a href="{normalize-space($web)}">
                  <xsl:value-of select="normalize-space($web)"/>
                </a>
              </xsl:for-each>
              <xsl:for-each select="gmd:hoursOfService[normalize-space(.) != '']">
                  <xsl:apply-templates mode="render-field"
                                       select="."/>
              </xsl:for-each>
            </xsl:for-each>
          </address>
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- Metadata linkage -->
  <xsl:template mode="render-field"
                match="gmd:fileIdentifier"
                priority="100">
    <dl>
      <dt>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="*"/>
        <xsl:apply-templates mode="render-value" select="@*"/>
        <a class="btn btn-default" href="{$nodeUrl}api/records/{$metadataId}/formatters/xml">
          <i class="fa fa-file-code-o"><xsl:comment select="'file'"/></i>
          <span><xsl:value-of select="$schemaStrings/metadataInXML"/></span>
        </a>
      </dd>
    </dl>
  </xsl:template>

  <!-- Linkage -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_OnlineResource and */gmd:linkage/gmd:URL != '']"
                priority="100">
    <dl class="gn-link">
      <dt>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:variable name="linkUrl"
                      select="*/gmd:linkage/gmd:URL"/>
        <xsl:variable name="linkName">
          <xsl:choose>
            <xsl:when test="*/gmd:name[* != '']">
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:name"/>
            </xsl:when>
            <xsl:when test="*/gmd:description[* != '']">
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:description"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$linkUrl"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <a href="{$linkUrl}" title="{$linkName}">
          <span><xsl:comment select="name()"/>
            <xsl:value-of select="$linkName"/>
          </span>
        </a>
        <xsl:if test="*/gmd:protocol[normalize-space(gco:CharacterString|gmx:Anchor) != '']">
        (<span><xsl:comment select="name()"/>
          <xsl:apply-templates mode="render-value"
                   select="*/gmd:protocol"/>
        </span>)</xsl:if>
        <xsl:if test="*/gmd:description[normalize-space(gco:CharacterString|gmx:Anchor) != '' and * != $linkName]">
          <p><xsl:comment select="name()"/>
            <xsl:apply-templates mode="render-value"
                                 select="*/gmd:description"/>
          </p>
        </xsl:if>
      </dd>
    </dl>
  </xsl:template>

  <!-- Identifier -->
  <xsl:template mode="render-field"
                match="*[(gmd:RS_Identifier or gmd:MD_Identifier) and
                  */gmd:code/gco:CharacterString != '']"
                priority="100">
    <dl class="gn-code">
      <dt>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>

        <xsl:if test="*/gmd:codeSpace">
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:codeSpace"/>
          /
        </xsl:if>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:code"/>
        <xsl:if test="*/gmd:version">
          /
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:version"/>
        </xsl:if>
        <xsl:if test="*/gmd:authority">
          <p><xsl:comment select="name()"/>
            <xsl:apply-templates mode="render-field"
                                 select="*/gmd:authority"/>
          </p>
        </xsl:if>
      </dd>
    </dl>
  </xsl:template>

 <!-- Display thesaurus name and the list of keywords if at least one keyword is set -->
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[*/gmd:thesaurusName/gmd:CI_Citation/gmd:title and
                count(*/gmd:keyword/*[. != '']) = 0]"
                priority="100"/>
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[*/gmd:thesaurusName/gmd:CI_Citation/gmd:title and
                count(*/gmd:keyword/*[. != '']) > 0]"
                priority="100">
    <dl class="gn-keyword">
      <dt>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:thesaurusName/gmd:CI_Citation/gmd:title/*"/>

        <xsl:if test="*/gmd:type/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:type/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
        <div>
          <ul>
            <xsl:for-each select="*/gmd:keyword">
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
                match="gmd:descriptiveKeywords[not(*/gmd:thesaurusName/gmd:CI_Citation/gmd:title)]"
                priority="100">
    <dl class="gn-keyword">
      <dt>
        <xsl:value-of select="$schemaStrings/noThesaurusName"/>
        <xsl:if test="*/gmd:type/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:type/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
        <div>
          <ul>
            <xsl:for-each select="*/gmd:keyword">
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
                match="gmd:distributionFormat[1]"
                priority="100">
    <dl class="gn-format">
      <dt>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/gmd:distributionFormat">
            <xsl:if test="*/gmd:name[. != '']">
              <li>
                <xsl:apply-templates mode="render-value"
                                    select="*/gmd:name"/>
                (<xsl:apply-templates mode="render-value"
                                      select="*/gmd:version"/>)
                <p><xsl:comment select="name()"/>
                  <xsl:apply-templates mode="render-field"
                                      select="*/(gmd:amendmentNumber|gmd:specification|
                                gmd:fileDecompressionTechnique|gmd:formatDistributor)"/>
                </p>
              </li>
            </xsl:if>
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="gmd:distributionFormat[position() > 1]"
                priority="100"/>

  <!-- Date -->
  <xsl:template mode="render-field"
                match="gmd:date"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
        <xsl:if test="*/gmd:dateType/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:dateType/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:date/*"/>
      </dd>
    </dl>
  </xsl:template>


  <!-- Enumeration -->
  <xsl:template mode="render-field"
                match="gmd:topicCategory[1]|gmd:obligation[1]|gmd:pointInPixel[1]"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/(gmd:topicCategory|gmd:obligation|gmd:pointInPixel)">
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
                match="gmd:topicCategory[position() > 1]|
                        gmd:obligation[position() > 1]|
                        gmd:pointInPixel[position() > 1]"
                priority="100"/>


  <!-- Link to other metadata records -->
  <xsl:template mode="render-field"
                match="*[@uuidref]"
                priority="100">
    <xsl:variable name="nodeName" select="name()"/>

    <!-- Only render the first element of this kind and render a list of
    following siblings. -->
    <xsl:variable name="isFirstOfItsKind"
                  select="count(preceding-sibling::node()[name() = $nodeName]) = 0"/>
    <xsl:if test="$isFirstOfItsKind">
      <dl class="gn-md-associated-resources">
        <dt>
          <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
        </dt>
        <dd>
          <ul>
            <xsl:for-each select="parent::node()/*[name() = $nodeName]">
              <li>
                <a href="{$nodeUrl}api/records/{@uuidref}">
                  <i class="fa fa-link"><xsl:comment select="'link'"/></i>
                  <span><xsl:comment select="'dataset'"/>
                    <xsl:value-of select="gn-fn-render:getMetadataTitle(@uuidref, $langId)"/>
                  </span>
                </a>
              </li>
            </xsl:for-each>
          </ul>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

 <!-- Elements to avoid render -->
  <xsl:template mode="render-field" match="gmd:PT_Locale" priority="100"/>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field"
                match="*">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>


  <!-- ########################## -->
  <!-- Render values for text ... -->
   <xsl:template mode="render-value"
                match="*[gco:CharacterString]">

     <xsl:variable name="txt">
       <xsl:apply-templates mode="localised" select=".">
         <xsl:with-param name="langId" select="$langId"/>
       </xsl:apply-templates>
     </xsl:variable>
     <span>
      <xsl:choose>
        <xsl:when test="name()='gmd:parentIdentifier'">
          <a href="{$nodeUrl}api/records/{./gco:CharacterString}">
            <i class="fa fa-fw fa-link"><xsl:comment select="'link'"/></i>
            <xsl:value-of select="gn-fn-render:getMetadataTitle(./gco:CharacterString, $langId)"/>
          </a>
        </xsl:when>

      </xsl:choose><xsl:comment select="name()"/>
      <xsl:call-template name="addLineBreaksAndHyperlinks">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
     </span>
  </xsl:template>


  <xsl:template mode="render-value"
                match="*[gmx:Anchor]">
    <xsl:variable name="txt">
      <xsl:apply-templates mode="localised" select=".">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </xsl:variable>

    <a href="{gmx:Anchor/@xlink:href}">
      <xsl:value-of select="$txt"/>
    </a>
  </xsl:template>


  <xsl:template mode="render-value"
                match="gco:Integer|gco:Decimal|
       gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gml:beginPosition|gml:endPosition|gml320:beginPosition|gml320:endPosition">

    <xsl:choose>
      <xsl:when test="contains(., 'http')">
        <!-- Replace hyperlink in text by an hyperlink -->
        <xsl:variable name="textWithLinks"
                      select="replace(., '([a-z][\w-]+:/{1,3}[^\s()&gt;&lt;]+[^\s`!()\[\]{};:'&apos;&quot;.,&gt;&lt;?«»“”‘’])',
                                    '&lt;a href=''$1''&gt;$1&lt;/a&gt;')"/>

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

    <xsl:if test="@uom"><xsl:comment select="name()"/>
      <xsl:value-of select="@uom"/>
    </xsl:if>
  </xsl:template>

  <!-- Translate boolean values -->
  <xsl:template mode="render-value"
                match="gco:Boolean">

    <xsl:choose>
      <xsl:when test=". = 'true'">
        <xsl:value-of select="$schemaStrings/trueValue"/>
      </xsl:when>
      <xsl:when test=". = 'false'">
        <xsl:value-of select="$schemaStrings/falseValue"/>
      </xsl:when>

    </xsl:choose>
  </xsl:template>

  <!-- filename -->
  <xsl:template mode="render-value"
                match="gmx:FileName[@src != '']">
    <xsl:variable name="href" select="@src"/>
    <xsl:variable name="label" select="."/>

    <xsl:choose>
      <xsl:when test="matches($href, $imageExtensionsRegex, 'i')">
        <img src="{$href}" title="{$label}" alt="{$label}"/>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$href}"><xsl:comment select="name()"/>
          <xsl:value-of select="$label"/>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value"
                match="gmd:URL">
    <a href="{.}"><xsl:comment select="name()"/>
      <xsl:value-of select="."/>
    </a>
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
    <span data-gn-humanize-time="{.}" data-format="MMM YYYY"><xsl:comment select="name()"/>
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}" data-format="DD MMM YYYY"><xsl:comment select="name()"/>
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:DateTime[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')]">
    <span data-gn-humanize-time="{.}"><xsl:comment select="name()"/>
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date|gco:DateTime">
    <span data-gn-humanize-time="{.}"><xsl:comment select="name()"/>
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gmd:language/gmd:LanguageCode/@codeListValue">
      <xsl:value-of select="xslUtils:twoCharLangCode(.)"/>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gmd:language/gco:CharacterString">
    <span data-translate=""><xsl:comment select="name()"/>
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <!-- ... Codelists -->
  <xsl:template mode="render-value"
                match="@codeListValue">
    <xsl:variable name="id" select="."/>
    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            tr:create($schema),
                            parent::node()/local-name(), $id)"/>
    <xsl:choose>
      <xsl:when test="$codelistTranslation != ''">

        <xsl:variable name="codelistDesc"
                      select="tr:codelist-value-desc(
                            tr:create($schema),
                            parent::node()/local-name(), $id)"/>
        <span title="{$codelistDesc}">
          <xsl:value-of select="$codelistTranslation"/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Enumeration -->
  <xsl:template mode="render-value"
                match="gmd:MD_TopicCategoryCode|
                        gmd:MD_ObligationCode|
                        gmd:MD_PixelOrientationCode">
    <xsl:variable name="id" select="."/>
    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            tr:create($schema),
                            local-name(), $id)"/>
    <xsl:choose>
      <xsl:when test="$codelistTranslation != ''">

        <xsl:variable name="codelistDesc"
                      select="tr:codelist-value-desc(
                            tr:create($schema),
                            local-name(), $id)"/>
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
    <i class="fa fa-lock text-warning" title="{{{{'withheld' | translate}}}}"><xsl:comment select="'warning'"/></i>
  </xsl:template>
  <xsl:template mode="render-value"
                match="@*"/>

</xsl:stylesheet>
