<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">

  <!-- EMODNET / Use thesaurus local.theme.emodnet.catalogue-type
  to populate the field gmd:hierarchyLevelName. -->
  <xsl:template mode="mode-iso19139" priority="2000"
                match="gmd:hierarchyLevelName[contains(
                          $metadata/gmd:metadataStandardName/gco:CharacterString,
                          'EMODNET -')]">

    <div class="form-group gn-field"
         id="gn-el-hierarchyLevelName">
      <label for="gn-field-hierarchyLevelName" class="col-sm-2 control-label">
        <xsl:value-of select="$iso19139strings/emodnetCatalogueType"/>
      </label>
      <div class="col-sm-9 gn-value">
        <input class="form-control"
               value="{gco:CharacterString}"
               name="_{gco:CharacterString/gn:element/@ref}"
               data-gn-keyword-picker=""
               data-thesaurus-key="external.theme.NVS.L23"
               data-gn-field-tooltip="iso19139|gmd:hierarchyLevelName||/gmd:MD_Metadata/gmd:hierarchyLevelName"
               type="text"/>
      </div>
    </div>
  </xsl:template>



  <xsl:template mode="mode-iso19139" priority="2000"
                match="gmd:dataQualityInfo/*/gmd:report[
                          contains(
                            */gmd:nameOfMeasure/*/text(),
                            'emodnet-bathymetry.QI.') and contains(
                          $metadata/gmd:metadataStandardName/gco:CharacterString,
                          'EMODNET -')]">

    <xsl:variable name="id" select="generate-id()"/>
    <xsl:variable name="name" select="*/gmd:nameOfMeasure/*/text()"/>

    <div class="form-group gn-field"
         id="gn-el-{$id}">
      <label for="gn-field-{$id}" class="col-sm-2 control-label">
        <xsl:value-of select="$iso19139strings/*[name() = $name]"/>
      </label>
      <div class="col-sm-9 gn-value">
        <input class="form-control"
               value="{*/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record}"
               name="_{*/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record/gn:element/@ref}"
               data-gn-keyword-picker=""
               data-thesaurus-key="local.theme.{$name}"
               type="text"/>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
