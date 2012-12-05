<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gfc="http://www.isotc211.org/2005/gfc"
    xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="xsl geonet" version="2.0">
    
    <!-- 
      Usage: 
        Search for iso19110 metadata:
            http://localhost:8080/geonetwork/srv/fr/xml.search?_schema=iso19110
        
        Select them:
            http://localhost:8080/geonetwork/srv/fr/metadata.select?id=0&selected=add-all

        Migrate:
            http://localhost:8080/geonetwork/srv/en/metadata.batch.processing?process=iso19110_2011_schema_update
    -->
    
    <!-- Replace gfc namespace prefix with gmx for the following elements -->
    <xsl:template match="gfc:name|gfc:scope|gfc:fieldOfApplication|gfc:versionNumber|gfc:versionDate">
      <xsl:element name="gmx:{local-name()}">
         <xsl:apply-templates select="@* | node()"/>
      </xsl:element>
    </xsl:template>

    <!-- Fix order of elements -->
    <xsl:template match="gfc:FC_FeatureType" priority="2">
      <xsl:copy>
        <xsl:apply-templates select="gfc:typeName"/>
        <xsl:apply-templates select="gfc:definition"/>
        <xsl:apply-templates select="gfc:code"/>
        <xsl:apply-templates select="gfc:isAbstract"/>
        <xsl:apply-templates select="gfc:aliases"/>
        <xsl:apply-templates select="gfc:inheritsFrom"/>
        <xsl:apply-templates select="gfc:featureCatalogue"/>
        <xsl:apply-templates select="gfc:constrainedBy"/>
        <xsl:apply-templates select="gfc:definitionReference"/>
        <xsl:apply-templates select="gfc:carrierOfCharacteristics"/>
      </xsl:copy>
    </xsl:template>

    <!-- Fix order of elements -->
    <xsl:template match="gfc:FC_FeatureAttribute" priority="2">
      <xsl:copy>
        <xsl:apply-templates select="gfc:featureType"/>
        <xsl:apply-templates select="gfc:constrainedBy"/>
        <xsl:apply-templates select="gfc:memberName"/>
        <xsl:apply-templates select="gfc:definition"/>
        <xsl:apply-templates select="gfc:cardinality"/>
        <xsl:apply-templates select="gfc:definitionReference"/>
        <xsl:apply-templates select="gfc:featureCatalogue"/>
        <xsl:apply-templates select="gfc:code"/>
        <xsl:apply-templates select="gfc:valueMeasurementUnit"/>
        <xsl:apply-templates select="gfc:valueType"/>
        <xsl:apply-templates select="gfc:listedValue"/>
      </xsl:copy>
    </xsl:template>


    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Always remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
