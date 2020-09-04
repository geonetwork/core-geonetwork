<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" 
  xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0" 
  xmlns:gml="http://www.opengis.net/gml/3.2" 
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p><xd:b>Created on: </xd:b>April 17, 2015</xd:p>
      <xd:p><xd:b>Author: </xd:b>Ted Habermann</xd:p>
      <xd:p><xd:b>Updated on: </xd:b>September 10, 20175</xd:p>
      <xd:p>Added Catalog information and summary fields (counts of codelists and items), updated Version date</xd:p>     
    </xd:desc>
  </xd:doc>
  <xsl:param name="contents"/>
  <xsl:variable name="stylesheetName" select="'CT_CodelistHTML.xsl'"/>
  <xsl:variable name="stylesheetVersion" select="'2017-09-10'"/>
  <xsl:template match="/">
    <html>
      <a name="top"/>
      <h1>Codelist Catalog</h1>
      <p>This report describes the ISO TC211 codelist catalogs, the codelists they contain, and the values and definitions of the codes.</p>
      <p> 
        Please contact <a href="mailto:rehabermann@me.com">Ted Habermann</a> if you have questions or suggestions.</p>
      <h2>Catalog</h2>
      <b>Name: </b><xsl:value-of select="cat:CT_CodelistCatalogue/cat:name/gco:CharacterString"/><br/>
      <b>Scope: </b><xsl:value-of select="cat:CT_CodelistCatalogue/cat:scope/gco:CharacterString"/><br/>
      <b>Field of application: </b><xsl:value-of select="cat:CT_CodelistCatalogue/cat:fieldOfApplication/gco:CharacterString"/><br/>
      <b>Version: </b><xsl:value-of select="cat:CT_CodelistCatalogue/cat:versionNumber/gco:CharacterString"/><br/>
      <b>Date: </b><xsl:value-of select="cat:CT_CodelistCatalogue/cat:versionDate/gco:Date"/><br/>
      <b>Number of CodeLists: </b><xsl:value-of select="count(//cat:codelistItem)"/><br/>
      <b>Number of items: </b><xsl:value-of select="count(//cat:codeEntry)"/>
      <hr/>
      <h2>Codelists</h2>
      <xsl:if test="$contents">
        <table width="95%" border="1" cellpadding="2" cellspacing="2">
          <tr>
            <td valign="top">
              <b>Citation Information CodeLists</b>
            </td>
          </tr>
          <tr>
            <td><a href="#CI_DateTypeCode">CI_DateTypeCode</a> | <a href="#CI_OnLineFunctionCode">CI_OnLineFunctionCode</a> | <a href="#CI_PresentationFormCode">CI_PresentationFormCode</a> | <a href="#CI_RoleCode">CI_RoleCode</a></td>
          </tr>
          <tr>
            <td valign="top">
              <b>Data Quality CodeLists</b>
            </td>
          </tr>
          <tr>
            <td>
              <a href="#DQ_EvaluationMethodTypeCode">DQ_EvaluationMethodTypeCode</a>
            </td>
          </tr>
          <tr>
            <td valign="top">
              <b>Dataset CodeLists</b>
            </td>
          </tr>
          <tr>
            <td><a href="#DS_AssociationTypeCode">DS_AssociationTypeCode</a> | <a href="#DS_InitiativeTypeCode">DS_InitiativeTypeCode</a></td>
          </tr>
          <tr>
            <td valign="top">
              <b>Metadata CodeLists</b>
            </td>
          </tr>
          <tr>
            <td><a href="#MD_CellGeometryCode">MD_CellGeometryCode</a> | <a href="#MD_CharacterSetCode">MD_CharacterSetCode</a> | <a href="#MD_ClassificationCode">MD_ClassificationCode</a> | <a href="#MD_CoverageContentTypeCode">MD_CoverageContentTypeCode</a> | <a href="#MD_DatatypeCode">MD_DatatypeCode</a> | <a href="#MD_DimensionNameTypeCode">MD_DimensionNameTypeCode</a> | <a href="#MD_GeometricObjectTypeCode">MD_GeometricObjectTypeCode</a> | <a href="#MD_ImagingConditionCode"
                >MD_ImagingConditionCode</a> | <a href="#MD_KeywordTypeCode">MD_KeywordTypeCode</a> | <a href="#MD_MaintenanceFrequencyCode">MD_MaintenanceFrequencyCode</a> | <a href="#MD_MediumFormatCode">MD_MediumFormatCode</a> | <a href="#MD_MediumNameCode">MD_MediumNameCode</a> | <a href="#MD_ObligationCode">MD_ObligationCode</a> | <a href="#MD_PixelOrientationCode">MD_PixelOrientationCode</a> | <a href="#MD_ProgressCode">MD_ProgressCode</a> | <a href="#MD_RestrictionCode"
                >MD_RestrictionCode</a> | <a href="#MD_ScopeCode">MD_ScopeCode</a> | <a href="#MD_SpatialRepresentationTypeCode">MD_SpatialRepresentationTypeCode</a> | <a href="#MD_TopicCategoryCode">MD_TopicCategoryCode</a> | <a href="#MD_TopologyLevelCode">MD_TopologyLevelCode</a></td>
          </tr>
          <tr>
            <td valign="top">
              <b>Metadata for Grids and Images CodeLists</b>
            </td>
          </tr>
          <tr>
            <td><a href="#MI_BandDefinition">MI_BandDefinition</a> | <a href="#MI_ContextCode">MI_ContextCode</a> | <a href="#MI_GeometryTypeCode">MI_GeometryTypeCode</a> | <a href="#MI_ObjectiveTypeCode">MI_ObjectiveTypeCode</a> | <a href="#MI_OperationTypeCode">MI_OperationTypeCode</a> | <a href="#MI_PolarizationOrientationCode">MI_PolarizationOrientationCode</a> | <a href="#MI_PriorityCode">MI_PriorityCode</a> | <a href="#MI_SequenceCode">MI_SequenceCode</a> | <a
                href="#MI_TransferFunctionTypeCode">MI_TransferFunctionTypeCode</a> | <a href="#MI_TriggerCode">MI_TriggerCode</a></td>
          </tr>
          <tr>
            <td valign="top">
              <b>Metadata Transfer CodeLists</b>
            </td>
          </tr>
          <tr>
            <td>
              <a href="#MX_ScopeCode">MX_ScopeCode</a>
            </td>
          </tr>
        </table>
      </xsl:if>
      <xsl:for-each select="//cat:CT_Codelist">
        <xsl:element name="a">
          <xsl:attribute name="href">
            <xsl:value-of select="@id"/>
          </xsl:attribute>
        </xsl:element>
        <h2><xsl:value-of select="cat:identifier"/>: </h2>
        <b>Description: </b><xsl:value-of select="cat:definition"/><br/>
        <b>CodeSpace: </b><xsl:value-of select="cat:identifier/gco:ScopedName/@codeSpace"/><br/>
        <b>Number of items: </b><xsl:value-of select="count(cat:codeEntry)"/>
        <table width="95%" border="1" cellpadding="2" cellspacing="2">
          <tr>
            <th valign="top">Entry</th>
            <th valign="top">Definition</th>
          </tr>
          <xsl:for-each select="cat:codeEntry">
            <xsl:variable name="entry" select="(cat:CT_CodelistValue/cat:identifier)"/>
            <xsl:variable name="definition" select="cat:CT_CodelistValue/cat:definition"/>
            <tr>
              <td valign="top">
                <xsl:value-of select="$entry"/>
              </td>
              
              <td valign="top">
                <xsl:value-of select="$definition"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>
        <a href="#top">top</a>
      </xsl:for-each>
      <hr/>
      <i><xsl:value-of select="concat('Stylesheet: ',$stylesheetName,' Version: ',$stylesheetVersion)"/></i>
    </html>
  </xsl:template>
</xsl:stylesheet>
