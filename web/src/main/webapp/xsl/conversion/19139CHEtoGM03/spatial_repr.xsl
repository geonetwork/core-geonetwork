<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="che gco gmd gml">

    <!-- ================================================================================== -->
    
    <xsl:template mode="SpatialRepr" match="gmd:MD_VectorSpatialRepresentation">
        <GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="text" select="gmd:topologyLevel"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:geometricObjects"/>
        </GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:geometricObjects">
        <xsl:apply-templates mode="SpatialRepr"/>
    </xsl:template>
    
    <xsl:template mode="SpatialRepr" match="gmd:MD_GeometricObjects">
        <GM03_2Comprehensive.Comprehensive.MD_GeometricObjects TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:geometricObjectType"/>
            <xsl:apply-templates mode="text" select="gmd:geometricObjectCount"/>
            <BACK_REF name="MD_VectorSpatialRepresentation"/>
        </GM03_2Comprehensive.Comprehensive.MD_GeometricObjects>
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template mode="SpatialRepr" match="gmd:MD_GridSpatialRepresentation">
        <GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="text" select="gmd:numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:axisDimensionProperties"/>
            <xsl:apply-templates mode="text" select="gmd:cellGeometry"/>
            <xsl:apply-templates mode="text" select="gmd:transformationParameterAvailability"/>
        </GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:axisDimensionProperties">
        <xsl:apply-templates mode="SpatialRepr"/>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:MD_Dimension">
        <GM03_2Comprehensive.Comprehensive.MD_Dimension TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:dimensionName"/>
            <xsl:apply-templates mode="text" select="gmd:dimensionSize"/>
            <xsl:apply-templates mode="text" select="gmd:resolution"/>
            <BACK_REF name="MD_GridSpatialRepresentation"/>
        </GM03_2Comprehensive.Comprehensive.MD_Dimension>
    </xsl:template>

    <!-- ================================================================================== -->
    
    <xsl:template mode="SpatialRepr" match="gmd:MD_Georectified">
        <GM03_2Comprehensive.Comprehensive.MD_Georectified TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
    <!-- gridSpatial properties -->
            <xsl:apply-templates mode="text" select="gmd:numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:axisDimensionProperties"/>
            <xsl:apply-templates mode="text" select="gmd:cellGeometry"/>
            <xsl:apply-templates mode="text" select="gmd:transformationParameterAvailability"/>

    <!-- specific to MD_Georectified -->
            <xsl:apply-templates mode="text" select="gmd:checkPointAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:checkPointDescription"/>
            <xsl:if test="gmd:cornerPoints">
                <cornerPoints>
                    <xsl:apply-templates mode="SpatialRepr" select="gmd:cornerPoints"/>
                </cornerPoints>
            </xsl:if>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:centerPoint"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:pointInPixel"/>
            <xsl:apply-templates mode="text" select="gmd:transformationDimensionDescription"/>
            <xsl:apply-templates mode="text_" select="gmd:transformationDimensionMapping"/>
        </GM03_2Comprehensive.Comprehensive.MD_Georectified>
    </xsl:template>
    
    <xsl:template match="gmd:cornerPoints" mode="SpatialRepr">
                <GM03_2Core.Core.GM_Point_>
                    <value>
                        <xsl:call-template name="explode" >
                            <xsl:with-param name="string" select="gml:Point/gml:coordinates"/>
                        </xsl:call-template>
                    </value>
                </GM03_2Core.Core.GM_Point_>
    </xsl:template>
    
    <xsl:template match="gmd:centerPoint" mode="SpatialRepr">
        <centerPoint>
                <xsl:call-template name="explode" >
                    <xsl:with-param name="string" select="gml:Point/gml:coordinates"/>
                </xsl:call-template>
        </centerPoint>
    </xsl:template>
    
    <xsl:template match="gmd:pointInPixel" mode="SpatialRepr">
        <pointInPixel><xsl:value-of select="gmd:MD_PixelOrientationCode"/></pointInPixel>
    </xsl:template>
    
    <!-- ================================================================================== -->
        
    <xsl:template mode="SpatialRepr" match="gmd:MD_Georeferenceable">
        <GM03_2Comprehensive.Comprehensive.MD_Georeferenceable TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>

    <!-- gridSpatial properties -->
            <xsl:apply-templates mode="text" select="gmd:numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:axisDimensionProperties"/>
            <xsl:apply-templates mode="text" select="gmd:cellGeometry"/>
            <xsl:apply-templates mode="text" select="gmd:transformationParameterAvailability"/>

    <!-- specific to MD_Georeferenceable -->
            <xsl:apply-templates mode="text" select="gmd:controlPointAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:orientationParameterAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:orientationParameterDescription"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:georeferencedParameters"/>
            <xsl:apply-templates mode="Citation" select="gmd:parameterCitation"/>
        </GM03_2Comprehensive.Comprehensive.MD_Georeferenceable>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:georeferencedParameters">
        <georeferencedParameters><xsl:value-of select="gco:Record"/></georeferencedParameters>
    </xsl:template>

    <!-- ================================================================================== -->
    
    <xsl:template mode="SpatialRepr" match="*" priority="-100">
        <ERROR>Unknown SpatialRepr element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

</xsl:stylesheet>