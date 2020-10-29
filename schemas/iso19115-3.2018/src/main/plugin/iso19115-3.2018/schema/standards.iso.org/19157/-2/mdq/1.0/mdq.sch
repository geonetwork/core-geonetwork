<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="mdq" uri="http://standards.iso.org/iso/19157/-2/mdq/1.0"/>
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/1.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <!--
    ISO 19157-2 base requirements for data quality metadata instance documents
    
    See ISO19157:2013 page 11, Figure 6 Data quality measure reference
  -->
  
  <!-- 
    Rule: DQ_MeasureReference
    Ref: {count(measureIdentification + nameOfMeasure) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mdq.measureidentifierorname-failure-en"
      xml:lang="en">The DQ_MeasureReference must include a measureIdentification or a nameOfmeasure.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mdq.measureidentifierorname-success-en"
      xml:lang="en">measureIdentifier is  
      "<sch:value-of select="normalize-space($identifierCode)"/>"
      and nameOfmeasure
      "<sch:value-of select="normalize-space($name)"/>"
      .</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mdq.measureidentifierorname">
    <sch:title xml:lang="en">DQ_MeasureReference MUST include a measureIdentification or a nameOfmeasure</sch:title>
    
    <sch:rule context="//mdq:DQ_MeasureReference">
      
      <sch:let name="identifierCode" value="mdq:measureIdentification/mcc:MD_Identifier/mcc:code/gco:CharacterString"/>
      <sch:let name="name" value="mdq:nameOfMeasure/gco:CharacterString"/>
      <sch:let name="hasIdentifier" 
        value="normalize-space($identifierCode) != ''"/>
      <sch:let name="hasName" 
               value="normalize-space($name) != ''"/>
      
      <sch:assert test="$hasIdentifier or $hasName"
        diagnostics="rule.mdq.measureidentifierorname-failure-en"/>
      
      <sch:report test="$hasIdentifier or $hasName"
        diagnostics="rule.mdq.measureidentifierorname-success-en"/>
    </sch:rule>
  </sch:pattern>
  
</sch:schema>
