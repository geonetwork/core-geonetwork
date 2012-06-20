<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:util="xalan://org.fao.geonet.util.XslUtil"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="util"
                >

    <xsl:template match="GM03Comprehensive.Comprehensive.MD_VectorSpatialRepresentation" mode="SpatialRepr">
        <gmd:MD_VectorSpatialRepresentation>
            <xsl:apply-templates mode="SpatialRepr"/>
        </gmd:MD_VectorSpatialRepresentation>
    </xsl:template>

    <xsl:template match="topologyLevel" mode="SpatialRepr">
        <gmd:topologyLevel>
            <gmd:MD_TopologyLevelCode codeList="./resources/codeList.xml#MD_TopologyLevelCode" codeListValue="{.}"/>
        </gmd:topologyLevel>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template match="GM03Comprehensive.Comprehensive.MD_GridSpatialRepresentation" mode="SpatialRepr">
        <gmd:MD_GridSpatialRepresentation>
            <xsl:apply-templates mode="SpatialRepr" select="numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="GM03Comprehensive.Comprehensive.MD_Dimension"/>
            <xsl:apply-templates mode="SpatialRepr" select="cellGeometry"/>
            <xsl:apply-templates mode="SpatialRepr" select="transformationParameterAvailability"/>
        </gmd:MD_GridSpatialRepresentation>
    </xsl:template>

    <xsl:template match="numberOfDimensions" mode="SpatialRepr">
        <gmd:numberOfDimensions>
            <gco:Integer>
                <xsl:value-of select="."/>
            </gco:Integer>
        </gmd:numberOfDimensions>
    </xsl:template>
    <xsl:template match="GM03Comprehensive.Comprehensive.MD_Dimension" mode="SpatialRepr">
        <gmd:axisDimensionProperties>
            <xsl:apply-templates select="." mode="Dimension"/>
        </gmd:axisDimensionProperties>
    </xsl:template>
    <xsl:template match="cellGeometry" mode="SpatialRepr">
        <gmd:cellGeometry>
            <gmd:MD_CellGeometryCode codeList="./resources/codeList.xml#MD_CellGeometryCode"
                                 codeListValue="{.}"/>
        </gmd:cellGeometry>
    </xsl:template>
    <xsl:template match="transformationParameterAvailability" mode="SpatialRepr">
        <gmd:transformationParameterAvailability>
            <xsl:apply-templates mode="boolean" select="text()"/>
        </gmd:transformationParameterAvailability>
    </xsl:template>

    <!-- ================================================================================= -->
    <xsl:template match="GM03Comprehensive.Comprehensive.MD_Georeferenceable" mode="SpatialRepr">
        <gmd:MD_Georeferenceable>
    <!-- gridSpatial properties -->
            <xsl:apply-templates mode="SpatialRepr" select="numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="GM03Comprehensive.Comprehensive.MD_Dimension"/>
            <xsl:apply-templates mode="SpatialRepr" select="cellGeometry"/>
            <xsl:apply-templates mode="SpatialRepr" select="transformationParameterAvailability"/>

    <!-- specific to MD_Georeferenceable -->
            <xsl:apply-templates mode="boolean" select="controlPointAvailability"/>
            <xsl:apply-templates mode="boolean" select="orientationParameterAvailability"/>
            <xsl:apply-templates mode="text" select="orientationParameterDescription"/>
            <xsl:apply-templates mode="SpatialRepr" select="georeferencedParameters"/>
            <xsl:apply-templates mode="Citation" select="parameterCitation"/>
        </gmd:MD_Georeferenceable>
    </xsl:template>

    <xsl:template match="georeferencedParameters" mode="SpatialRepr">
        <gmd:georeferencedParameters>
            <gco:Record><xsl:value-of select="."/></gco:Record>
        </gmd:georeferencedParameters>
    </xsl:template>
    <!-- ================================================================================= -->
    <xsl:template match="GM03Comprehensive.Comprehensive.MD_Georectified" mode="SpatialRepr">
        <gmd:MD_Georectified>
    <!-- gridSpatial properties -->
            <xsl:apply-templates mode="SpatialRepr" select="numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="GM03Comprehensive.Comprehensive.MD_Dimension"/>
            <xsl:apply-templates mode="SpatialRepr" select="cellGeometry"/>
            <xsl:apply-templates mode="SpatialRepr" select="transformationParameterAvailability"/>

    <!-- specific to MD_Georectified -->
            <xsl:apply-templates mode="boolean" select="checkPointAvailability"/>
            <xsl:apply-templates mode="text" select="checkPointDescription"/>
            <xsl:apply-templates mode="SpatialRepr" select="cornerPoints"/>
            <xsl:apply-templates mode="SpatialRepr" select="centerPoint"/>
            <xsl:apply-templates mode="SpatialRepr" select="pointInPixel"/>
            <xsl:apply-templates mode="text" select="transformationDimensionDescription"/>
            <xsl:apply-templates mode="text" select="transformationDimensionMapping"/>
        </gmd:MD_Georectified>
    </xsl:template>

    <xsl:template match="cornerPoints" mode="SpatialRepr">
        <xsl:for-each select="GM03Core.Core.GM_Point_">
            <gmd:cornerPoints>
                <xsl:apply-templates mode="GM_Object" select="."/>
            </gmd:cornerPoints>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="centerPoint" mode="SpatialRepr">
        <gmd:centerPoint>
            <gml:Point gml:id="{util:randomId()}">
                <gml:coordinates><xsl:apply-templates mode="GM_Object" select="."/></gml:coordinates>
            </gml:Point>
        </gmd:centerPoint>
    </xsl:template>

    <xsl:template match="pointInPixel" mode="SpatialRepr">
        <gmd:pointInPixel>
            <gmd:MD_PixelOrientationCode><xsl:value-of select="."/></gmd:MD_PixelOrientationCode>
        </gmd:pointInPixel>
    </xsl:template>
    <!-- ================================================================================= -->

    <xsl:template match="GM03Comprehensive.Comprehensive.MD_Dimension" mode="Dimension">
        <gmd:MD_Dimension>
            <xsl:for-each select="dimensionName">
                <gmd:dimensionName>
                    <gmd:MD_DimensionNameTypeCode codeList="./resources/codeList.xml#MD_DimensionNameTypeCode" codeListValue="{.}"/>
                </gmd:dimensionName>
            </xsl:for-each>

            <xsl:for-each select="dimensionSize">
                <gmd:dimensionSize>
                    <gco:Integer>
                        <xsl:value-of select="."/>
                    </gco:Integer>
                </gmd:dimensionSize>
            </xsl:for-each>

            <xsl:for-each select="resolution">
                <gmd:resolution>
                    <gco:Measure uom="m"><xsl:value-of select="."/></gco:Measure>
                </gmd:resolution>
            </xsl:for-each>
        </gmd:MD_Dimension>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template match="GM03Comprehensive.Comprehensive.MD_GeometricObjects" mode="SpatialRepr">
        <gmd:geometricObjects>
            <gmd:MD_GeometricObjects>
                <xsl:apply-templates mode="SpatialRepr"/>
            </gmd:MD_GeometricObjects>
        </gmd:geometricObjects>
    </xsl:template>

    <xsl:template match="geometricObjectType" mode="SpatialRepr">
        <gmd:geometricObjectType>
            <gmd:MD_GeometricObjectTypeCode codeList="./resources/codeList.xml#MD_GeometricObjectTypeCode"
                                        codeListValue="{.}"/>
        </gmd:geometricObjectType>
    </xsl:template>

    <xsl:template match="geometricObjectCount" mode="SpatialRepr">
        <gmd:geometricObjectCount>
            <gco:Integer>
                <xsl:value-of select="."/>
            </gco:Integer>
        </gmd:geometricObjectCount>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="SpatialRepr" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">SpatialRepr</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
