<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/1.0"/>
  <sch:ns prefix="gex" uri="http://standards.iso.org/iso/19115/-3/gex/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 24, Figure 19 Extent information classes
  -->
  
  <!-- 
    Rule: EX_Extent
    Ref: {count(description + 
                geographicElement + 
                temporalElement + 
                verticalElement) >0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.gex.extenthasoneelement-failure-en"
      xml:lang="en">The extent does not contain a description or a geographicElement.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-failure-fr"
      xml:lang="fr">L'étendue ne contient aucun élement.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.extenthasoneelement-desc-success-en"
      xml:lang="en">The extent contains a description.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-desc-success-fr"
      xml:lang="fr">L'étendue contient une description.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.extenthasoneelement-id-success-en"
      xml:lang="en">The extent contains a geographic identifier.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-id-success-fr"
      xml:lang="fr">L'étendue contient un identifiant géographique.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.extenthasoneelement-box-success-en"
      xml:lang="en">The extent contains a bounding box element.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-box-success-fr"
      xml:lang="fr">L'étendue contient une emprise géographique.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.extenthasoneelement-poly-success-en"
      xml:lang="en">The extent contains a bounding polygon.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-poly-success-fr"
      xml:lang="fr">L'étendue contient un polygone englobant.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.extenthasoneelement-vertical-success-en"
      xml:lang="en">The extent contains a vertical element.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-vertical-success-fr"
      xml:lang="fr">L'étendue contient une étendue verticale.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.extenthasoneelement-temporal-success-en"
      xml:lang="en">The extent contains a temporal element.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.extenthasoneelement-temporal-success-fr"
      xml:lang="fr">L'étendue contient une étendue temporelle.</sch:diagnostic>
    
  </sch:diagnostics>
  
  <sch:pattern id="rule.gex.extenthasoneelement">
    <sch:title xml:lang="en">Extent MUST have one description or one geographic, temporal or vertical element</sch:title>
    <sch:title xml:lang="fr">Une étendue DOIT avoir une description ou un élément géographique, temporel ou vertical</sch:title>
    
    <sch:rule context="//gex:EX_Extent">
      
      <!-- Check that element exist and is not empty ones.
      TODO improve nonEmpty checks -->
      <sch:let name="description" 
               value="gex:description[text() != '']"/>
      <sch:let name="geographicId" 
               value="gex:geographicElement/gex:EX_GeographicDescription/
                         gex:geographicIdentifier[normalize-space(*) != '']"/>
      <sch:let name="geographicBox" 
               value="gex:geographicElement/
                         gex:EX_GeographicBoundingBox[
                         normalize-space(gex:westBoundLongitude/gco:Decimal) != '' and
                         normalize-space(gex:eastBoundLongitude/gco:Decimal) != '' and
                         normalize-space(gex:southBoundLatitude/gco:Decimal) != '' and
                         normalize-space(gex:northBoundLatitude/gco:Decimal) != ''
                         ]"/>
      <sch:let name="geographicPoly" 
               value="gex:geographicElement/gex:EX_BoundingPolygon[
                         normalize-space(gex:polygon) != '']"/>
      <sch:let name="temporal" 
               value="gex:temporalElement/gex:EX_TemporalExtent[
                         normalize-space(gex:extent) != '']"/>
      <sch:let name="vertical" 
               value="gex:verticalElement/gex:EX_VerticalExtent[
                         normalize-space(gex:minimumValue) != '' and
                         normalize-space(gex:maximumValue) != '']"/>
      
      
      <sch:let name="hasAtLeastOneElement" 
        value="count($description) +
        count($geographicId) +
        count($geographicBox) +
        count($geographicPoly) +
        count($temporal) +
        count($vertical) > 0
        "/>
      
      <sch:assert test="$hasAtLeastOneElement"
        diagnostics="rule.gex.extenthasoneelement-failure-en 
                     rule.gex.extenthasoneelement-failure-fr"/>
      
      <sch:report test="count($description)"
        diagnostics="rule.gex.extenthasoneelement-desc-success-en 
                     rule.gex.extenthasoneelement-desc-success-fr"/>
      <sch:report test="count($geographicId)"
        diagnostics="rule.gex.extenthasoneelement-id-success-en 
                     rule.gex.extenthasoneelement-id-success-fr"/>
      <sch:report test="count($geographicBox)"
        diagnostics="rule.gex.extenthasoneelement-box-success-en 
                     rule.gex.extenthasoneelement-box-success-fr"/>
      <sch:report test="count($geographicPoly)"
        diagnostics="rule.gex.extenthasoneelement-poly-success-en 
                     rule.gex.extenthasoneelement-poly-success-fr"/>
      <sch:report test="count($temporal)"
        diagnostics="rule.gex.extenthasoneelement-temporal-success-en 
                     rule.gex.extenthasoneelement-temporal-success-fr"/>
      <sch:report test="count($vertical)"
        diagnostics="rule.gex.extenthasoneelement-vertical-success-en 
                     rule.gex.extenthasoneelement-vertical-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  <!-- 
    Rule: EX_VerticalExtent
    Ref: {count(verticalCRS + verticalCRSId) > 0)}
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.gex.verticalhascrsorcrsid-failure-en"
      xml:lang="en">The vertical extent does not contains CRS or CRS identifier.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.verticalhascrsorcrsid-failure-fr"
      xml:lang="fr">L'étendue verticale ne contient pas de CRS ou d'identifiant de CRS.</sch:diagnostic>
    
    <sch:diagnostic id="rule.gex.verticalhascrsorcrsid-success-en"
      xml:lang="en">The vertical extent contains CRS information.</sch:diagnostic>
    <sch:diagnostic id="rule.gex.verticalhascrsorcrsid-success-fr"
      xml:lang="fr">L'étendue verticale contient les informations sur le CRS.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.gex.verticalhascrsorcrsid">
    <sch:title xml:lang="en">Vertical element MUST contains a CRS or CRS identifier</sch:title>
    <sch:title xml:lang="fr">Une étendue verticale DOIT contenir un CRS ou un identifiant de CRS</sch:title>
    
    <sch:rule context="//gex:EX_VerticalExtent">
      
      <sch:let name="crs" value="gex:verticalCRS"/>
      <sch:let name="crsId" value="gex:verticalCRSId"/>
      <sch:let name="hasCrsOrCrsId" 
        value="count($crs) + count($crsId) > 0"/>
      
      <sch:assert test="$hasCrsOrCrsId"
        diagnostics="rule.gex.verticalhascrsorcrsid-failure-en 
                     rule.gex.verticalhascrsorcrsid-failure-fr"/>
      
      <sch:report test="$hasCrsOrCrsId"
        diagnostics="rule.gex.verticalhascrsorcrsid-success-en 
                     rule.gex.verticalhascrsorcrsid-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
</sch:schema>
