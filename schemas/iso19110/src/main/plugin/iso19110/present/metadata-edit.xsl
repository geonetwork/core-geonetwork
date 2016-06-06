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

<!--
    ISO19110 support

    Feature catalogue support:
     * feature catalogue description
     * class/attribute/property/list of values viewing/editing support

    Known limitation:
     * iso19110 links between elements (eg. inheritance)
     * partial support of association and feature operation description

     @author francois
     @author mathieu
     @author sppigot
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0"
                exclude-result-prefixes="gfc gmx gmd gco geonet">

  <xsl:include href="metadata-view.xsl"/>

  <!-- main template - the way into processing iso19110 -->
  <xsl:template name="metadata-iso19110">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <xsl:apply-templates mode="iso19110" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="iso19110CompleteTab">
    <xsl:param name="tabLink"/>
    <xsl:param name="schema"/>

    <xsl:if test="/root/gui/config/metadata-tab/advanced">
      <xsl:call-template name="mainTab">
        <xsl:with-param name="title" select="/root/gui/strings/byPackage"/>
        <xsl:with-param name="default">advanced</xsl:with-param>
        <xsl:with-param name="menu">
          <item label="byPackage">advanced</item>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <!-- =================================================================== -->
  <!-- default: in simple mode just a flat list -->
  <!-- =================================================================== -->

  <xsl:template mode="iso19110" match="*|@*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <!-- do not show empty elements in view mode -->
    <xsl:variable name="adjustedSchema">
      <xsl:choose>
        <xsl:when test="namespace-uri(.) != 'http://www.isotc211.org/2005/gfc'">
          <xsl:text>iso19139</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$schema"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="element" select=".">
          <xsl:with-param name="schema" select="$adjustedSchema"/>
          <xsl:with-param name="edit" select="true()"/>
          <xsl:with-param name="flat" select="$currTab='simple'"/>
        </xsl:apply-templates>
      </xsl:when>

      <xsl:otherwise>
        <xsl:variable name="empty">
          <xsl:apply-templates mode="iso19110IsEmpty" select="."/>
        </xsl:variable>
        <xsl:if test="$empty!=''">
          <xsl:apply-templates mode="element" select=".">
            <xsl:with-param name="schema" select="$adjustedSchema"/>
            <xsl:with-param name="edit" select="false()"/>
            <xsl:with-param name="flat" select="$currTab='simple'"/>
          </xsl:apply-templates>
        </xsl:if>

      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- ===================================================================== -->
  <!-- these elements should be boxed -->
  <!-- ===================================================================== -->

  <xsl:template mode="iso19110" match="gfc:*[gfc:FC_FeatureType]|
        gfc:*[gfc:FC_AssociationRole]|
        gfc:*[gfc:FC_AssociationOperation]|
        gfc:listedValue|gfc:constrainedBy|gfc:inheritsFrom|gfc:inheritsTo">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- some gco: elements -->
  <!-- ===================================================================== -->

  <xsl:template mode="iso19110"
                match="gfc:*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure]|
        gmd:*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType]|
        gmx:*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure]">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <!-- Generate a textarea when relevant -->
    <xsl:variable name="rows">
      <xsl:choose>
        <xsl:when test="name(.)='gfc:description' or
                    (name(.)='gfc:definition' and name(parent::*)!='gfc:FC_ListedValue')
                    ">3
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:call-template name="iso19139String">
      <xsl:with-param name="schema">
        <xsl:choose>
          <xsl:when test="namespace-uri(.) != 'http://www.isotc211.org/2005/gfc'">
            <xsl:text>iso19139</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$schema"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="rows" select="$rows">
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="iso19110"
                match="gfc:*[gmx:FileName]" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:call-template name="file-upload">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================================= -->
  <!-- codelists -->
  <!-- ================================================================= -->

  <xsl:template mode="iso19110" match="gfc:*[*/@codeList]|gmd:*[*/@codeList]">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:call-template name="iso19139Codelist">
      <xsl:with-param name="schema">
        <xsl:choose>
          <xsl:when test="namespace-uri(.) != 'http://www.isotc211.org/2005/gfc'">
            <xsl:text>iso19139</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$schema"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Element set on save by update-fixed-info. -->
  <xsl:template mode="iso19110" match="gmx:versionDate|gfc:versionDate" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="false()"/>
      <xsl:with-param name="text">
        <xsl:choose>
          <xsl:when test="normalize-space(gco:*)=''">
            <span class="info">
              -
              <xsl:value-of select="/root/gui/strings/setOnSave"/> -
            </span>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="gco:*"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ============================================================================= -->
  <!--
      date (format = %Y-%m-%d)
      editionDate
      dateOfNextUpdate
      mdDateSt is not editable (!we use DateTime instead of only Date!)
  -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19110"
                match="gmd:editionDate|gmd:dateOfNextUpdate"
                priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="text">
            <xsl:variable name="ref"
                          select="gco:Date/geonet:element/@ref|gco:DateTime/geonet:element/@ref"/>
            <xsl:variable name="format">
              <xsl:choose>
                <xsl:when test="gco:Date">
                  <xsl:text>%Y-%m-%d</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>%Y-%m-%dT%H:%M:00</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:call-template name="calendar">
              <xsl:with-param name="ref" select="$ref"/>
              <xsl:with-param name="date" select="gco:DateTime/text()|gco:Date/text()"/>
              <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="iso19139String">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Do not display those elements:
   * hide nested featureType elements
   * hide definition reference elements
   * inheritance : does not support linking feature catalogue objects (eg. to indicate subtype or supertype)
  -->
  <xsl:template mode="iso19110" match="gfc:featureType[ancestor::gfc:featureType]|
        gfc:featureCatalogue|
        gfc:FC_InheritanceRelation/gfc:featureCatalogue|
        @gco:isoType" priority="100"/>

  <xsl:template mode="elementEP" match="
        geonet:child[@name='featureCatalogue']|
        gfc:FC_InheritanceRelation/geonet:child[@name='subtype']|
        gfc:FC_InheritanceRelation/geonet:child[@name='supertype']
        " priority="100"/>

  <!-- ==================================================================== -->
  <!-- Metadata -->
  <!-- ==================================================================== -->

  <xsl:template mode="iso19110"
                match="gfc:FC_FeatureCatalogue|*[@gco:isoType='gfc:FC_FeatureCatalogue']">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="embedded"/>

    <xsl:call-template name="iso19110Simple">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="flat" select="$currTab='simple'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ============================================================================= -->
  <!--
      simple mode; ISO order is:
  -->
  <!-- ============================================================================= -->

  <xsl:template name="iso19110Simple">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="flat"/>

    <xsl:call-template name="iso19110Metadata">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:call-template>

    <xsl:apply-templates mode="elementEP" select="gfc:featureType">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="flat" select="$flat"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP"
                         select="geonet:child[@name='featureType' and @prefix='gfc']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

  </xsl:template>

  <!-- ===================================================================== -->
  <!-- === iso19110 brief formatting === -->
  <!-- ===================================================================== -->

  <xsl:template mode="superBrief" match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType">
    <xsl:variable name="uuid" select="geonet:info/uuid"/>
    <id>
      <xsl:value-of select="geonet:info/id"/>
    </id>
    <uuid>
      <xsl:value-of select="$uuid"/>
    </uuid>
    <xsl:if test="gmx:name|gfc:name|gfc:typeName">
      <title>
        <xsl:value-of
          select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString|gfc:typeName/gco:LocalName"/>
      </title>
    </xsl:if>
  </xsl:template>

  <xsl:template name="iso19110Brief">
    <metadata>
      <xsl:variable name="id" select="geonet:info/id"/>
      <xsl:variable name="uuid" select="geonet:info/uuid"/>

      <xsl:if test="gmx:name or gfc:name">
        <title>
          <xsl:value-of select="gmx:name/gco:CharacterString|gfc:name/gco:CharacterString"/>
        </title>
      </xsl:if>

      <xsl:if test="gmx:scope or gfc:scope">
        <abstract>
          <xsl:value-of select="gmx:scope/gco:CharacterString|gfc:scope/gco:CharacterString"/>
        </abstract>
      </xsl:if>

      <geonet:info>
        <xsl:copy-of select="geonet:info/*"/>
        <category internal="true">featureCatalogue</category>
      </geonet:info>
    </metadata>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="iso19110Metadata">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <!-- if the parent is root then display fields not in tabs -->
    <xsl:choose>
      <xsl:when test="name(..)='root'">
        <xsl:apply-templates mode="elementEP"
                             select="@uuid">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP"
                             select="gmx:name|gfc:name|geonet:child[@name='name']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP"
                             select="gmx:scope|gfc:scope|geonet:child[@name='scope']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP"
                             select="gmx:fieldOfApplication|gfc:fieldOfApplication|geonet:child[@name='fieldOfApplication']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP"
                             select="gmx:versionNumber|gfc:versionNumber|geonet:child[@name='versionNumber']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP"
                             select="gmx:versionDate|gfc:versionDate|geonet:child[@name='versionDate']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP" select="gfc:producer|geonet:child[@name='producer']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="elementEP"
                             select="gfc:functionalLanguage|geonet:child[@name='functionalLanguage']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>

      </xsl:when>

      <!-- otherwise, display everything because we have embedded gfc:FC_FeatureCatalogue -->

      <xsl:otherwise>
        <xsl:apply-templates mode="elementEP" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- Display producer as contact in ISO 19139 -->
  <xsl:template mode="iso19110" match="gfc:producer">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:call-template name="contactTemplate">
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="iso19110" match="gfc:carrierOfCharacteristics/gfc:FC_FeatureAttribute">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:variable name="content">
      <td class="padded-content" width="100%" colspan="2">
        <table width="100%">
          <tr>
            <td width="50%" valign="top">
              <table width="100%">

                <xsl:apply-templates mode="elementEP"
                                     select="gfc:memberName|geonet:child[string(@name)='memberName']">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>

                <xsl:apply-templates mode="elementEP"
                                     select="gfc:definition|geonet:child[string(@name)='definition']">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>

                <xsl:apply-templates mode="elementEP"
                                     select="gfc:cardinality|geonet:child[string(@name)='cardinality']">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>

                <xsl:apply-templates mode="elementEP"
                                     select="gfc:featureType|geonet:child[string(@name)='featureType']">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>

                <xsl:apply-templates mode="elementEP"
                                     select="gfc:valueType|geonet:child[string(@name)='valueType']">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>

              </table>
            </td>
            <td valign="top">
              <table width="100%">
                <xsl:choose>
                  <xsl:when test="$edit=true() or $currTab!='simple'">
                    <xsl:apply-templates mode="elementEP" select="gfc:listedValue|gfc:definitionReference|gfc:valueMeasurementUnit|
                                            geonet:child[string(@name)='listedValue']|geonet:child[string(@name)='definitionReference']|geonet:child[string(@name)='valueMeasurementUnit']">
                      <xsl:with-param name="schema" select="$schema"/>
                      <xsl:with-param name="edit" select="$edit"/>
                    </xsl:apply-templates>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:if test="gfc:listedValue">
                      <xsl:call-template name="complexElementGui">
                        <xsl:with-param name="title">
                          <xsl:value-of
                            select="/root/gui/schemas/iso19110/labels/element[@name='gfc:listedValue']/label"/>
                          <xsl:text> </xsl:text>
                          (<xsl:value-of
                          select="/root/gui/schemas/iso19110/labels/element[@name='gfc:label']/label"/>
                          [<xsl:value-of
                          select="/root/gui/schemas/iso19110/labels/element[@name='gfc:code']/label"/>]
                          :
                          <xsl:value-of
                            select="/root/gui/schemas/iso19110/labels/element[@name='gfc:definition']/label"/>)
                        </xsl:with-param>
                        <xsl:with-param name="content">

                          <ul class="md">
                            <xsl:for-each select="gfc:listedValue/gfc:FC_ListedValue">
                              <li>
                                <b>
                                  <xsl:value-of select="gfc:label/gco:CharacterString"/>
                                </b>
                                [<xsl:value-of select="gfc:code/gco:CharacterString"/>] :
                                <xsl:value-of select="gfc:definition/gco:CharacterString"/>
                              </li>
                            </xsl:for-each>
                          </ul>
                        </xsl:with-param>
                      </xsl:call-template>
                    </xsl:if>
                  </xsl:otherwise>
                </xsl:choose>

              </table>
            </td>
          </tr>
        </table>
      </td>
    </xsl:variable>

    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="content" select="$content"/>
    </xsl:apply-templates>

  </xsl:template>

  <!-- handle cardinality edition
      Update fixed info take care of setting UnlimitedInteger attribute.
  -->
  <xsl:template mode="iso19110" match="gfc:cardinality">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <!-- Variables -->
    <xsl:variable name="minValue"
                  select="gco:Multiplicity/gco:range/gco:MultiplicityRange/gco:lower/gco:Integer"/>
    <xsl:variable name="maxValue"
                  select="gco:Multiplicity/gco:range/gco:MultiplicityRange/gco:upper/gco:UnlimitedInteger"/>
    <xsl:variable name="isInfinite"
                  select="gco:Multiplicity/gco:range/gco:MultiplicityRange/gco:upper/gco:UnlimitedInteger/@isInfinite"/>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:variable name="cardinality">
          <tr>
            <td colspan="2">
              <table width="100%">
                <tr>
                  <th class="md" width="20%" valign="top">
                    <span id="stip.iso19110|gco:lower" onclick="toolTip(this.id);"
                          style="cursor: help;">
                      <xsl:value-of
                        select="string(/root/gui/iso19110/element[@name='gco:lower']/label)"/>
                    </span>
                  </th>
                  <td class="padded" valign="top">
                    <!-- Min cardinality list -->
                    <select name="_{$minValue/geonet:element/@ref}" class="md" size="1">
                      <option value=""/>
                      <option value="0">
                        <xsl:if test="$minValue = '0'">
                          <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:text>0</xsl:text>
                      </option>
                      <option value="1">
                        <xsl:if test="$minValue = '1'">
                          <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:text>1</xsl:text>
                      </option>
                    </select>
                  </td>
                  <th class="md" width="20%" valign="top">
                    <span id="stip.iso19110|gco:upper" onclick="toolTip(this.id);"
                          style="cursor: help;">
                      <xsl:value-of
                        select="string(/root/gui/iso19110/element[@name='gco:upper']/label)"/>
                    </span>
                  </th>
                  <td class="padded" valign="top">
                    <!-- Max cardinality list -->
                    <select name="minCard" class="md" size="1"
                            onchange="updateUpperCardinality('_{$maxValue/geonet:element/@ref}', this.value)">
                      <option value=""/>
                      <option value="0">
                        <xsl:if test="$maxValue = '0'">
                          <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:text>0</xsl:text>
                      </option>
                      <option value="1">
                        <xsl:if test="$maxValue = '1'">
                          <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:text>1</xsl:text>
                      </option>
                      <option value="n">
                        <xsl:if test="$isInfinite = 'true'">
                          <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:text>n</xsl:text>
                      </option>
                    </select>

                    <!-- Hidden value to post -->
                    <input type="hidden" name="_{$maxValue/geonet:element/@ref}"
                           id="_{$maxValue/geonet:element/@ref}" value="{$maxValue}"/>
                    <input type="hidden" name="_{$maxValue/geonet:element/@ref}_isInfinite"
                           id="_{$maxValue/geonet:element/@ref}_isInfinite" value="{$isInfinite}"/>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </xsl:variable>
        <xsl:apply-templates mode="complexElement" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="content">
            <xsl:copy-of select="$cardinality"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td colspan="2">
            <table width="100%">
              <tr>
                <xsl:if test="$minValue != ''">
                  <th class="md" width="20%" valign="top">
                    <span id="stip.iso19110|gco:lower" onclick="toolTip(this.id);"
                          style="cursor: help;">
                      <xsl:value-of
                        select="string(/root/gui/iso19110/element[@name='gco:lower']/label)"/>
                    </span>
                  </th>
                  <td class="padded" valign="top">
                    <xsl:value-of select="$minValue"/>
                  </td>
                </xsl:if>
                <xsl:if test="$maxValue !='' or $isInfinite = 'true'">
                  <th class="md" width="20%" valign="top">
                    <span id="stip.iso19110|gco:upper" onclick="toolTip(this.id);"
                          style="cursor: help;">
                      <xsl:value-of
                        select="string(/root/gui/iso19110/element[@name='gco:upper']/label)"/>
                    </span>
                  </th>
                  <td class="padded" valign="top">
                    <xsl:choose>
                      <xsl:when test="$isInfinite = 'true'">
                        <xsl:text>n</xsl:text>
                      </xsl:when>
                      <xsl:when test="$maxValue != ''">
                        <xsl:value-of select="$maxValue"/>
                      </xsl:when>
                    </xsl:choose>
                  </td>
                </xsl:if>
              </tr>
            </table>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
