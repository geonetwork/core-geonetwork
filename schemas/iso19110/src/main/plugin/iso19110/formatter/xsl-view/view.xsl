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
                xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">
 <!-- tr is defined at  core-geonetwork/services/src/main/java/org/fao/geonet/api/records/formatters/SchemaLocalizations.java -->
  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="if (/root/gfc:FC_FeatureCatalogue)
                        then /root/gfc:FC_FeatureCatalogue
                        else /root/gfc:FC_FeatureType"/>


  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="gfc:FC_FeatureType|gfc:FC_FeatureCatalogue">
    <xsl:variable name="value"
                  select="gmx:name"/>
    <xsl:value-of select="$value/gco:CharacterString"/>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="gfc:FC_FeatureType|gfc:FC_FeatureCatalogue">
    <xsl:variable name="value"
                  select="gmx:scope"/>
    <xsl:value-of select="$value/gco:CharacterString"/>
  </xsl:template>

  <xsl:template mode="getMetadataHeader" match="gfc:FC_FeatureType|gfc:FC_FeatureCatalogue">
  </xsl:template>


  <!-- Ignore some fields displayed in header or in right column -->
  <xsl:template mode="render-field"
                match="gmx:name|gmx:scope|gfc:featureType[count(*) = 0]"
                priority="2000"/>


  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field" match="*[(gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gmd:PT_FreeText|gml:beginPosition|gml:endPosition|gco:Date|gco:DateTime|*/@codeListValue) != '']">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:nodeLabel(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="*|*/@codeListValue"/>
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="gfc:listedValue"
                priority="2000">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <xsl:if test="preceding-sibling::*[1][local-name() != 'listedValue']">
      <strong>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:nodeLabel(tr:create($schema), name(), null)"/>
      </strong>
      <table class="table table-bordered">
        <tbody>
          <tr>
            <th>
              <xsl:value-of select="tr:nodeLabel(tr:create($schema), 'gfc:code', null)"/>
            </th>
            <th>
              <xsl:value-of select="tr:nodeLabel(tr:create($schema), 'gfc:label', null)"/>
            </th>
            <th>
              <xsl:value-of select="tr:nodeLabel(tr:create($schema), 'gfc:definition', null)"/>
            </th>
          </tr>
          <xsl:for-each select="../gfc:listedValue/*">
            <tr>
              <td>
                <xsl:apply-templates mode="render-value" select="gfc:code"/>
              </td>
              <td>
                <xsl:apply-templates mode="render-value" select="gfc:label"/>
              </td>
              <td>
                <xsl:apply-templates mode="render-value" select="gfc:definition"/>
              </td>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
    </xsl:if>
  </xsl:template>

  <!-- Some major sections are boxed -->
  <xsl:template mode="render-field"
                match="*[name() = $configuration/editor/fieldsWithFieldset/name
                         or @gco:isoType = $configuration/editor/fieldsWithFieldset/name]|
                       *[$isFlatMode = false()
                         and gmd:*
                         and not(gco:CharacterString)
                         and not(gmd:URL)]">
    <div class="entry name">
      <h3>
        <xsl:value-of select="tr:nodeLabel(tr:create($schema), name(), null)"/>
      </h3>
      <div class="target">
        <xsl:choose>
          <xsl:when test="count(*) > 0">
            <xsl:apply-templates mode="render-field" select="*"/>
          </xsl:when>
          <xsl:otherwise><xsl:comment select="'No information.'"/></xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

  <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_ResponsibleParty]"
                priority="100">
    <xsl:param name="layout"
               required="no"/>


    <xsl:variable name="email">
      <xsl:for-each select="*/gmd:contactInfo/
                                      */gmd:address/*/gmd:electronicMailAddress">
        <xsl:apply-templates mode="render-value"
                             select="."/><xsl:if test="position() != last()">, </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="role" select="*/gmd:role/gmd:CI_RoleCode/@codeListValue" />

    <!-- Display name is <org name> - <individual name> (<position name>) -->
    <!-- with separator/parentheses as required -->
    <xsl:variable name="displayName">
      <xsl:if test="*/gmd:organisationName">
        <xsl:apply-templates mode="render-value" select="*/gmd:organisationName"/>
      </xsl:if>
      <xsl:if test="*/gmd:organisationName and */gmd:individualName|*/gmd:positionName"> - </xsl:if>
      <xsl:if test="*/gmd:individualName">
        <xsl:apply-templates mode="render-value" select="*/gmd:individualName"/>
      </xsl:if>
      <xsl:if test="*/gmd:positionName">
        <xsl:choose>
          <xsl:when test="*/gmd:individualName">
            (<xsl:apply-templates mode="render-value" select="*/gmd:positionName"/>)
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="render-value" select="*/gmd:positionName"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$layout = 'short'">
        <xsl:copy-of select="$displayName"/>
      </xsl:when>
      <xsl:otherwise>
        <div class="gn-contact">
          <strong>
            <xsl:comment select="'email'"/>
            <xsl:apply-templates mode="render-value"
                                 select="*/gmd:role/*/@codeListValue"/>
          </strong>
          <address>
            <xsl:choose>
              <xsl:when test="$email">
                <i class="fa fa-fw fa-envelope">&#160;</i>
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
                <div>
                  <i class="fa fa-fw fa-map-marker"><xsl:comment select="'address'"/></i>
                  <xsl:for-each select="gmd:deliveryPoint[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:city[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:administrativeArea[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:postalCode[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value" select="."/>,
                  </xsl:for-each>
                  <xsl:for-each select="gmd:country[normalize-space(.) != '']">
                    <xsl:apply-templates mode="render-value" select="."/>
                  </xsl:for-each>
                </div>
              </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="*/gmd:contactInfo/*">
              <xsl:for-each select="gmd:phone/*/gmd:voice[normalize-space(.) != '']">
                <xsl:variable name="phoneNumber">
                  <xsl:apply-templates mode="render-value" select="."/>
                </xsl:variable>
                <i class="fa fa-fw fa-phone"><xsl:comment select="'phone'"/></i>
                <a href="tel:{translate($phoneNumber,' ','')}">
                  <xsl:value-of select="$phoneNumber"/>
                </a>
                <br/>
              </xsl:for-each>
              <xsl:for-each select="gmd:phone/*/gmd:facsimile[normalize-space(.) != '']">
                <xsl:variable name="phoneNumber">
                  <xsl:apply-templates mode="render-value" select="."/>
                </xsl:variable>
                <i class="fa fa-fw fa-fax"><xsl:comment select="'fax'"/></i>
                <a href="tel:{translate($phoneNumber,' ','')}">
                  <xsl:value-of select="normalize-space($phoneNumber)"/>
                </a>
                <br/>
              </xsl:for-each>
              <xsl:for-each select="gmd:onlineResource/*/gmd:linkage/gmd:URL[normalize-space(.) != '']">
                <xsl:variable name="web">
                  <xsl:apply-templates mode="render-value" select="."/></xsl:variable>
                <i class="fa fa-fw fa-link"><xsl:comment select="'link'"/></i>
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
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Traverse the tree -->
  <xsl:template mode="render-field" match="*">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>


  <!-- ########################## -->
  <!-- Render values for text ... -->
  <xsl:template mode="render-value" match="gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gml:beginPosition|gml:endPosition">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template mode="render-value" match="gmd:PT_FreeText">
    <xsl:apply-templates mode="localised" select="../node()">
      <xsl:with-param name="langId" select="$language"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value" match="gmd:URL">
    <a href="{.}">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <!-- ... Dates -->
  <xsl:template mode="render-value" match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <!-- if (tns:Employee/tns:EmpId = 4) then 'new' else 'old'-->
  <xsl:template mode="render-value"
                match="gco:DateTime[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')]">
    <span data-gn-humanize-time="{.}">
      <xsl:value-of select="."/>
    </span>
  </xsl:template>

  <xsl:template mode="render-value" match="gco:Date|gco:DateTime">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- ... Codelists -->
  <xsl:template mode="render-value"
                match="@codeListValue|@indeterminatePosition">
    <xsl:variable name="id" select="."/>
    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            tr:create($schema),
                            parent::node()/local-name(),
                            $id)"/>
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

</xsl:stylesheet>
