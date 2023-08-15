<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="mrd" uri="http://standards.iso.org/iso/19115/-3/mrd/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 20, Figure 15 Distribution information classes
  -->
  
  <!-- 
    Rule: MD_Medium
    Ref: {if density used then count (densityUnits) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mrd.mediumunit-failure-en"
      xml:lang="en">The medium define a density without unit.</sch:diagnostic>
    <sch:diagnostic id="rule.mrd.mediumunit-failure-fr"
      xml:lang="fr">La densité du média est définie sans unité.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mrd.mediumunit-success-en"
      xml:lang="en">
      Medium density is "<sch:value-of select="$density"/>" (unit:  
      "<sch:value-of select="$units"/>").
    </sch:diagnostic>
    <sch:diagnostic id="rule.mrd.mediumunit-success-fr"
      xml:lang="fr">
      La densité du média est "<sch:value-of select="$density"/>" (unité :  
      "<sch:value-of select="$units"/>").
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mrd.mediumunit">
    <sch:title xml:lang="en">Medium having density MUST specified density units</sch:title>
    <sch:title xml:lang="fr">Un média précisant une densité DOIT préciser l'unité</sch:title>
    
    <sch:rule context="//mrd:MD_Medium[mrd:density]">
      
      <sch:let name="density" 
        value="normalize-space(mrd:density/*)"/>
      <sch:let name="units" 
        value="normalize-space(mrd:densityUnits[normalize-space(*) != ''])"/>
      
      <sch:let name="hasUnits" 
        value="$units != ''"/>
      
      <sch:assert test="$hasUnits"
        diagnostics="rule.mrd.mediumunit-failure-en 
                     rule.mrd.mediumunit-failure-fr"/>
      
      <sch:report test="$hasUnits"
        diagnostics="rule.mrd.mediumunit-success-en 
                     rule.mrd.mediumunit-success-fr"/>
    </sch:rule>
  </sch:pattern>
</sch:schema>
