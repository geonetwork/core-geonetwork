<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork">

    <!-- Parent metadata record UUID -->
    <xsl:param name="parentUuid"/>

    <xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates
                    select="gmd:fileIdentifier|
                            gmd:language|
                            gmd:characterSet|
                            gmd:parentIdentifier|
                            gmd:hierarchyLevel|
                            gmd:hierarchyLevelName|
                            gmd:contact|
                            gmd:dateStamp|
                            gmd:metadataStandardName|
                            gmd:metadataStandardVersion|
                            gmd:dataSetURI|
                            gmd:locale"/>

            <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
                                    gmd:spatialResolution">
                <xsl:variable name="distance"
                              select="*/gmd:distance/*/text()"/>
                <xsl:variable name="unit"
                              select="*/gmd:distance/*/@uom"/>
                <gmd:spatialRepresentationInfo>
                    <gmd:MD_GridSpatialRepresentation>
                        <gmd:numberOfDimensions>
                            <gco:Integer>2</gco:Integer>
                        </gmd:numberOfDimensions>
                        <gmd:axisDimensionProperties>
                            <gmd:MD_Dimension>
                                <gmd:dimensionName>
                                    <gmd:MD_DimensionNameTypeCode
                                            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_DimensionNameTypeCode"
                                            codeListValue="row"/>
                                </gmd:dimensionName>
                                <gmd:resolution>
                                    <gco:Measure uom="{$unit}">
                                        <xsl:value-of select="$distance"/>
                                    </gco:Measure>
                                </gmd:resolution>
                            </gmd:MD_Dimension>
                        </gmd:axisDimensionProperties>
                        <gmd:axisDimensionProperties>
                            <gmd:MD_Dimension>
                                <gmd:dimensionName>
                                    <gmd:MD_DimensionNameTypeCode
                                            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_DimensionNameTypeCode"
                                            codeListValue="column"/>
                                </gmd:dimensionName>
                                <gmd:resolution>
                                    <gco:Measure uom="{$unit}">
                                        <xsl:value-of select="$distance"/>
                                    </gco:Measure>
                                </gmd:resolution>
                            </gmd:MD_Dimension>
                        </gmd:axisDimensionProperties>
                        <gmd:cellGeometry>
                            <gmd:MD_CellGeometryCode
                                    codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CellGeometryCode"
                                    codeListValue="area"/>
                        </gmd:cellGeometry>
                        <gmd:transformationParameterAvailability>
                            <gco:Boolean>false</gco:Boolean>
                        </gmd:transformationParameterAvailability>
                    </gmd:MD_GridSpatialRepresentation>
                </gmd:spatialRepresentationInfo>
            </xsl:for-each>
            <xsl:apply-templates
                    select="
                            gmd:spatialRepresentationInfo|
                            gmd:referenceSystemInfo|
                            gmd:metadataExtensionInfo|
                            gmd:identificationInfo|
                            gmd:contentInfo|
                            gmd:distributionInfo|
                            gmd:dataQualityInfo|
                            gmd:portrayalCatalogueInfo|
                            gmd:metadataConstraints|
                            gmd:applicationSchemaInfo|
                            gmd:metadataMaintenance|
                            gmd:series|
                            gmd:describes|
                            gmd:propertyType|
                            gmd:featureType|
                            gmd:featureAttribute"/>

        </xsl:copy>
    </xsl:template>

    <!-- Do a copy of every nodes and attributes recursively -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*|gmd:spatialResolution" priority="2"/>
</xsl:stylesheet>
