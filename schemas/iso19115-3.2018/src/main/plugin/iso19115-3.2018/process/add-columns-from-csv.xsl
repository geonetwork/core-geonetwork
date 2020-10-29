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
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="csv-info-loc">
    <msg id="a" xml:lang="eng">Add attribute table description from CSV</msg>
    <msg id="a" xml:lang="dut">Add attribute table description from CSV</msg>
    <msg id="a" xml:lang="fre">Ajouter le modèle de données (colonnes) à partir d'un fichier CSV</msg>
    <msg id="b" xml:lang="eng">Add columns from CSV for table </msg>
    <msg id="b" xml:lang="dut">Add columns from CSV for table </msg>
    <msg id="b" xml:lang="fre">Ajouter les colonnes à partir d'un fichier CSV pour la table </msg>
  </xsl:variable>

  <!-- Process parameters and variables-->
  <xsl:param name="table" select="''"/>
  <xsl:param name="replaceColumns" select="'1'"/>
  <xsl:param name="columnListAsCsv" select="''"/>
  <xsl:param name="columnListSeparator" select="';'"/>


  <xsl:template name="list-add-columns-from-csv">
    <suggestion process="add-columns-from-csv"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-add-columns-from-csv">
    <xsl:param name="root"/>

    <xsl:if test="count($root//srv:SV_ServiceIdentification) = 0">
      <xsl:choose>
        <!-- Add column to existing feature type declared -->
        <xsl:when test="$root//mdb:contentInfo//gfc:featureType/*">
          <xsl:for-each select="$root//mdb:contentInfo//gfc:featureType/*">
            <suggestion process="add-columns-from-csv" id="{generate-id()}"
                        category="fcat" target="gfc:FC_FeatureType">
              <name>
                <xsl:value-of select="concat(geonet:i18n($csv-info-loc, 'b', $guiLang), gfc:typeName)"/>
              </name>
              <operational>true</operational>
              <params>{
                "table":{"type":"string", "defaultValue":"<xsl:value-of select="gfc:typeName"/>"},
                "replaceColumns":{"type":"boolean", "defaultValue":"1"},
                "columnListSeparator":{"type":"string", "defaultValue":"<xsl:value-of select="$columnListSeparator"/>"},
                "columnListAsCsv":{"type":"textarea", "defaultValue":"SHORT_NAME;DESCRIPTION;TYPE;CARDINALITY_MIN..CARDINALITY_MAX"}
                }</params>
            </suggestion>
          </xsl:for-each>
        </xsl:when>
        <!-- or create a new feature catalogue with a new feature type -->
        <xsl:otherwise>
          <suggestion process="add-columns-from-csv" id="{generate-id()}"
                      category="fcat" target="gfc:FC_FeatureType">
            <name>
              <xsl:value-of select="geonet:i18n($csv-info-loc, 'a', $guiLang)"/>
            </name>
            <operational>true</operational>
            <params>{
              "table":{"type":"string", "defaultValue":""},
              "replaceColumns":{"type":"boolean", "defaultValue":"1"},
              "columnListSeparator":{"type":"string", "defaultValue":"<xsl:value-of select="$columnListSeparator"/>"},
              "columnListAsCsv":{"type":"textarea", "defaultValue":"SHORT_NAME;DESCRIPTION;TYPE;CARDINALITY_MIN..CARDINALITY_MAX"}
              }</params>
          </suggestion>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>



  <!-- When the feature type exist, insert or replace columns list -->
  <xsl:template match="gfc:FC_FeatureType[gfc:typeName = $table]" priority="99">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*[name() != 'gfc:carrierOfCharacteristics']"/>

      <xsl:if test="$replaceColumns != '1'">
        <xsl:apply-templates select="gfc:carrierOfCharacteristics"/>
      </xsl:if>

      <xsl:call-template name="build-attribute-table"/>
    </xsl:copy>
  </xsl:template>


  <!-- When a feature catalogue exists and the feature type does not,
   insert the table description in the first feature catalogue. -->
  <xsl:variable name="tableExist" select="count(//gfc:FC_FeatureType[gfc:typeName = $table]) > 0"/>

  <xsl:template match="mdb:contentInfo[not($tableExist) and position() = 1]/*/mrc:featureCatalogue[1]/*">
    <xsl:copy>
      <xsl:apply-templates select="@*|*[local-name() = 'name' or local-name() = 'scope' or local-name() = 'versionNumber' or local-name() = 'fieldOfApplication' or local-name() = 'characterSet' or local-name() = 'producer' or local-name() = 'locale' or local-name() = 'versionDate' or local-name() = 'functionalLanguage' or local-name() = 'identifier' or local-name() = 'featureType']"/>

      <gfc:featureType>
        <gfc:FC_FeatureType>
          <gfc:typeName><xsl:value-of select="$table"/></gfc:typeName>
          <gfc:isAbstract>
            <gco:Boolean>false</gco:Boolean>
          </gfc:isAbstract>
          <xsl:call-template name="build-attribute-table"/>
        </gfc:FC_FeatureType>
      </gfc:featureType>

      <xsl:apply-templates select="*[local-name() != 'name' and local-name() != 'scope' and local-name() != 'versionNumber' and local-name() != 'fieldOfApplication' and local-name() != 'characterSet' and local-name() != 'producer' and local-name() != 'locale' and local-name() != 'versionDate' and local-name() != 'functionalLanguage' and local-name() != 'identifier' and local-name() != 'featureType']"/>
    </xsl:copy>
  </xsl:template>


  <!-- When the feature catalogue does not exist -->
  <xsl:template match="mdb:MD_Metadata[not(mdb:contentInfo)]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="mdb:metadataIdentifier"/>
      <xsl:apply-templates select="mdb:defaultLocale"/>
      <xsl:apply-templates select="mdb:parentMetadata"/>
      <xsl:apply-templates select="mdb:metadataScope"/>
      <xsl:apply-templates select="mdb:contact"/>
      <xsl:apply-templates select="mdb:dateInfo"/>
      <xsl:apply-templates select="mdb:metadataStandard"/>
      <xsl:apply-templates select="mdb:metadataProfile"/>
      <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
      <xsl:apply-templates select="mdb:otherLocale"/>
      <xsl:apply-templates select="mdb:metadataLinkage"/>
      <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
      <xsl:apply-templates select="mdb:referenceSystemInfo"/>
      <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
      <xsl:apply-templates select="mdb:identificationInfo"/>

      <xsl:apply-templates select="mdb:contentInfo"/>

      <mdb:contentInfo>
        <mrc:MD_FeatureCatalogue>
          <mrc:featureCatalogue>
            <gfc:FC_FeatureCatalogue>
              <cat:name gco:nilReason="missing">
                <gco:CharacterString/>
              </cat:name>
              <cat:scope gco:nilReason="missing">
                <gco:CharacterString/>
              </cat:scope>
              <cat:versionNumber gco:nilReason="missing">
                <gco:CharacterString/>
              </cat:versionNumber>
              <gfc:featureType>
                <gfc:FC_FeatureType>
                  <gfc:typeName><xsl:value-of select="$table"/></gfc:typeName>
                  <gfc:isAbstract>
                    <gco:Boolean>false</gco:Boolean>
                  </gfc:isAbstract>
                  <xsl:call-template name="build-attribute-table"/>
                </gfc:FC_FeatureType>
              </gfc:featureType>
            </gfc:FC_FeatureCatalogue>
          </mrc:featureCatalogue>
        </mrc:MD_FeatureCatalogue>
      </mdb:contentInfo>
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



  <xsl:template name="build-attribute-table">
    <xsl:variable name="lines"
                  select="tokenize($columnListAsCsv, '\n')"/>

    <xsl:for-each select="$lines[normalize-space(.) != '']">
      <xsl:variable name="line"
                    select="."/>
      <xsl:variable name="columns"
                    select="tokenize(string($line), $columnListSeparator)"/>

      <gfc:carrierOfCharacteristics>
        <gfc:FC_FeatureAttribute>
          <xsl:if test="$columns[1]">
            <gfc:memberName><xsl:value-of select="$columns[1]"/></gfc:memberName>
          </xsl:if>
          <xsl:if test="$columns[2]">
            <gfc:definition>
              <gco:CharacterString><xsl:value-of select="$columns[2]"/></gco:CharacterString>
            </gfc:definition>
          </xsl:if>
          <xsl:if test="$columns[4]">
          <gfc:cardinality>
            <gco:CharacterString><xsl:value-of select="$columns[4]"/></gco:CharacterString>
          </gfc:cardinality>
          </xsl:if>
          <xsl:if test="$columns[3]">
          <gfc:valueType>
            <gco:TypeName>
              <gco:aName>
                <gco:CharacterString><xsl:value-of select="$columns[3]"/></gco:CharacterString>
              </gco:aName>
            </gco:TypeName>
          </gfc:valueType>
          </xsl:if>
        </gfc:FC_FeatureAttribute>
      </gfc:carrierOfCharacteristics>
    </xsl:for-each>
    <gfc:featureCatalogue/>
  </xsl:template>

  <!-- Processing templates -->
  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
