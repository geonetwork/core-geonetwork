<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.isotc211.org/2005/gmd">

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="SpatRepTypes">

    <xsl:for-each select="GridSpatRep">
      <MD_GridSpatialRepresentation>
        <xsl:apply-templates select="." mode="GridSpatRep"/>
      </MD_GridSpatialRepresentation>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="Georect">
      <MD_Georectified>
        <xsl:apply-templates select="." mode="Georect"/>
      </MD_Georectified>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="Georef">
      <MD_Georeferenceable>
        <xsl:apply-templates select="." mode="Georef"/>
      </MD_Georeferenceable>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="VectSpatRep">
      <MD_VectorSpatialRepresentation>
        <xsl:apply-templates select="." mode="VectSpatRep"/>
      </MD_VectorSpatialRepresentation>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === GridSpatRep === -->
  <!-- ============================================================================= -->

  <xsl:template match="*" mode="GridSpatRep">

    <numberOfDimensions>
      <gco:Integer>
        <xsl:value-of select="numDims"/>
      </gco:Integer>
    </numberOfDimensions>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="axDimProps/Dimen">
      <axisDimensionProperties>
        <MD_Dimension>
          <xsl:apply-templates select="." mode="DimensionProps"/>
        </MD_Dimension>
      </axisDimensionProperties>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <cellGeometry>
      <MD_CellGeometryCode codeList="./resources/codeList.xml#MD_CellGeometryCode"
                           codeListValue="{cellGeo/CellGeoCd/@value}"/>
    </cellGeometry>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <transformationParameterAvailability>
      <gco:Boolean>
        <xsl:value-of select="tranParaAv"/>
      </gco:Boolean>
    </transformationParameterAvailability>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="DimensionProps">

    <dimensionName>
      <MD_DimensionNameTypeCode codeList="./resources/codeList.xml#MD_DimensionNameTypeCode"
                                codeListValue="{dimName/DimNameTypCd/@value}"/>
    </dimensionName>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <dimensionSize>
      <gco:Integer>
        <xsl:value-of select="dimSize"/>
      </gco:Integer>
    </dimensionSize>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="dimResol">
      <resolution>
        <gco:Measure>
          <xsl:apply-templates select="." mode="Measure"/>
        </gco:Measure>
      </resolution>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Measure">

    <xsl:attribute name="uom">
      <xsl:value-of select="uom/*/uomName"/>,
      <xsl:value-of select="uom/*/conversionToISOstandardUnit"/>
    </xsl:attribute>

    <xsl:value-of select="value/Decimal"/>
    <xsl:value-of select="value/Integer"/>
    <xsl:value-of select="value/Real"/>

  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Georect === -->
  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Georect">

    <xsl:apply-templates select="*" mode="GridSpatRep"/>

    <checkPointAvailability>
      <gco:Boolean>
        <xsl:value-of select="chkPtAv"/>
      </gco:Boolean>
    </checkPointAvailability>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="chkPtDesc">
      <checkPointDescription>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </checkPointDescription>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="cornerPts">
      <cornerPoints>
        <gml:Point>
          <xsl:apply-templates select="." mode="PointType"/>
        </gml:Point>
      </cornerPoints>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="centerPt">
      <centerPoint>
        <gml:Point>
          <xsl:apply-templates select="." mode="PointType"/>
        </gml:Point>
      </centerPoint>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <pointInPixel>
      <MD_PixelOrientationCode>
        <xsl:value-of select="ptInPixel/PixOrientCd/@value"/>
      </MD_PixelOrientationCode>
    </pointInPixel>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="transDimDesc">
      <transformationDimensionDescription>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </transformationDimensionDescription>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="transDimMap">
      <transformationDimensionMapping>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </transformationDimensionMapping>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="PointType">
    <gml:coordinates>

      <xsl:if test="coordinates/@tupleSep">
        <xsl:attribute name="ts">
          <xsl:choose>
            <xsl:when test="coordinates/@tupleSep = 'space'"></xsl:when>
            <xsl:when test="coordinates/@tupleSep = 'comma'">,</xsl:when>
            <xsl:when test="coordinates/@tupleSep = 'period'">.</xsl:when>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="coordinates/@coordSep">
        <xsl:attribute name="cs">
          <xsl:choose>
            <xsl:when test="coordinates/@coordSep = 'space'"></xsl:when>
            <xsl:when test="coordinates/@coordSep = 'comma'">,</xsl:when>
            <xsl:when test="coordinates/@coordSep = 'period'">.</xsl:when>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="coordinates/@decimalChar">
        <xsl:attribute name="decimal">
          <xsl:choose>
            <xsl:when test="coordinates/@decimalChar = 'space'"></xsl:when>
            <xsl:when test="coordinates/@decimalChar = 'comma'">,</xsl:when>
            <xsl:when test="coordinates/@decimalChar = 'period'">.</xsl:when>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>

      <xsl:value-of select="."/>
    </gml:coordinates>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Georef === -->
  <!-- ============================================================================= -->

  <xsl:template match="*" mode="Georef">

    <xsl:apply-templates select="*" mode="GridSpatRep"/>

    <controlPointAvailability>
      <gco:Boolean>
        <xsl:value-of select="ctrlPtAv"/>
      </gco:Boolean>
    </controlPointAvailability>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <orientationParameterAvailability>
      <gco:Boolean>
        <xsl:value-of select="orieParaAv"/>
      </gco:Boolean>
    </orientationParameterAvailability>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="orieParaDesc">
      <orientationParameterDescription>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </orientationParameterDescription>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <georeferencedParameters>
      <gco:Record>
        <xsl:value-of select="georefPars"/>
      </gco:Record>
    </georeferencedParameters>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="paraCit">
      <parameterCitation>
        <CI_Citation>
          <xsl:apply-templates select="." mode="Citation"/>
        </CI_Citation>
      </parameterCitation>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === VectSpatRep === -->
  <!-- ============================================================================= -->

  <xsl:template match="*" mode="VectSpatRep">

    <xsl:for-each select="topLvl">
      <topologyLevel>
        <MD_TopologyLevelCode codeList="./resources/codeList.xml#MD_TopologyLevelCode"
                              codeListValue="{TopoLevCd/@value}"/>
      </topologyLevel>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="geometObjs">
      <geometricObjects>
        <MD_GeometricObjects>
          <xsl:apply-templates select="." mode="GeometObjs"/>
        </MD_GeometricObjects>
      </geometricObjects>
    </xsl:for-each>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="GeometObjs">

    <geometricObjectType>
      <MD_GeometricObjectTypeCode codeList="./resources/codeList.xml#MD_GeometricObjectTypeCode">
        <xsl:attribute name="codeListValue">
          <xsl:choose>
            <xsl:when test="geoObjTyp/GeoObjTypCd/@value = 'complexes'">complex</xsl:when>

            <xsl:when test="geoObjTyp/GeoObjTypCd/@value = 'composites'">composite</xsl:when>

            <xsl:otherwise>
              <xsl:value-of select="geoObjTyp/GeoObjTypCd/@value"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </MD_GeometricObjectTypeCode>
    </geometricObjectType>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="geoObjCnt">
      <geometricObjectCount>
        <gco:Integer>
          <xsl:value-of select="."/>
        </gco:Integer>
      </geometricObjectCount>
    </xsl:for-each>

  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
