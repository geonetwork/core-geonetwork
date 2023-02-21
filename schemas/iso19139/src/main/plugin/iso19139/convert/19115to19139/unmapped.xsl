<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="/">
    <result>
      <xsl:apply-templates select="*"/>
    </result>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Metadata/appSchInfo === -->
  <!-- ============================================================================= -->

  <xsl:template match="*/appSchInfo/fetCatSup">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Metadata/dqInfo === -->
  <!-- ============================================================================= -->

  <xsl:template match="*/srcRefSys/MdCoRefSys">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/measResult/Result">
    <xsl:if test="string(.) != ''">
      <element class="not mapped">
        <xsl:copy-of select="."/>
      </element>
    </xsl:if>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/QuanResult/quanValUnit">
    <element class="rough mapping, no data loss">
      <xsl:copy-of select="."/>
    </element>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Metadata/contInfo === -->
  <!-- ============================================================================= -->

  <xsl:template match="*/contInfo/ContInfo">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/seqID/scope">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/attributeType/scope">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/conversionToISOstandarUnit">
    <xsl:if test="string(.) != ''">
      <element class="not mapped">
        <xsl:copy-of select="."/>
      </element>
    </xsl:if>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/catFetTypes/MemberName | */catFetTypes/TypeName">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Metadata/spatRepInfo === -->
  <!-- ============================================================================= -->

  <xsl:template match="*/Dimen/dimResol">
    <element class="rough mapping, no data loss">
      <xsl:copy-of select="."/>
    </element>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/cornerPts/MdCoRefSys">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/coordinates/@dimension | */coordinates/@precision">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Metadata/dataIdInfo === -->
  <!-- ============================================================================= -->

  <xsl:template match="*/citIdType">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/designator | */timeIndicator">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/dataScale/scaleDist">
    <element class="rough mapping, no data loss">
      <xsl:copy-of select="."/>
    </element>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === Extent === -->
  <!-- ============================================================================= -->

  <xsl:template match="*/vertEle/vertUoM | */vertEle/vertDatum">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/TM_Instant/tmPosition">
    <element class="rough mapping, no data loss">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="*/GM_Polygon/MdCoRefSys">
    <element class="not mapped">
      <xsl:copy-of select="."/>
    </element>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- === (default) === -->
  <!-- ============================================================================= -->

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- ============================================================================= -->

</xsl:stylesheet>
