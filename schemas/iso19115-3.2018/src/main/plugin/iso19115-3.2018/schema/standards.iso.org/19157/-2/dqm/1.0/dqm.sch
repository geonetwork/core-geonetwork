<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
  <sch:ns prefix="dqm" uri="http://standards.iso.org/iso/19157/-2/dqm/1.0"/>
  <sch:ns prefix="cat" uri="http://standards.iso.org/iso/19115/-3/cat/1.0"/>
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/1.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <!--
    ISO 19157-2 base requirements for data quality metadata instance documents
    
    See ISO19157:2013 page 18, Figure 11 Data quality measures
  -->
  <!-- 
    Rule: DQM_Measure
    Ref: {elementName shall be a TypeName of a data quality element}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.dqm.measurename-failure-en" xml:lang="en">The DQM_Measure.elementName
      shall be a TypeName of a data quality element.</sch:diagnostic>
    
    <sch:diagnostic id="rule.dqm.measurename-success-en" xml:lang="en">elementName is
        "<sch:value-of select="normalize-space($elementName)"/>". </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.dqm.measurename">
    <sch:title xml:lang="en">DQM_Measure.elementName shall be a TypeName of a data quality element</sch:title>
    
    <sch:rule context="//dqm:DQM_Measure">
      
      <sch:let name="elementName" value="dqm:elementName/gco:TypeName/gco:aName/gco:CharacterString"/>

      <sch:assert test="(index-of(
        ('DQ_Completeness','DQ_CompletenessCommission','DQ_CompletenessOmmission',
        'DQ_LogicalConsistency','DQ_DomainConsistency','DQ_FormatConsistency','DQ_TopologicalConsistency',
        'DQ_UsabilityElement',
        'DQ_PositionalAccuracy','DQ_AbsoluteExternalPositionalAccuracy','DQ_RelativeInternalPositionalAccuracy','DQ_GriddedDataPositionalAccuracy',
        'DQ_ThematicAccuracy','DQ_ThematicClassificationCorrectness','DQ_NonQuantitativeAttributeCorrectness','DQ_QuantitativeAttributeAccuracy',
        'DQ_TemporalQuality','DQ_AccuracyOfATimeMeasurement','DQ_TemporalConsistency','DQ_TemporalValidity'),$elementName) > 0)"
        diagnostics="rule.dqm.measurename-failure-en"/>
      
      <sch:report test="(index-of(
        ('DQ_Completeness','DQ_CompletenessCommission','DQ_CompletenessOmmission',
        'DQ_LogicalConsistency','DQ_DomainConsistency','DQ_FormatConsistency','DQ_TopologicalConsistency',
        'DQ_UsabilityElement',
        'DQ_PositionalAccuracy','DQ_AbsoluteExternalPositionalAccuracy','DQ_RelativeInternalPositionalAccuracy','DQ_GriddedDataPositionalAccuracy',
        'DQ_ThematicAccuracy','DQ_ThematicClassificationCorrectness','DQ_NonQuantitativeAttributeCorrectness','DQ_QuantitativeAttributeAccuracy',
        'DQ_TemporalQuality','DQ_AccuracyOfATimeMeasurement','DQ_TemporalConsistency','DQ_TemporalValidity'),$elementName) > 0)"
        diagnostics="rule.dqm.measurename-success-en"/>
    </sch:rule>
  </sch:pattern>
  
  <!-- 
    Rule: DQM_Measure | DQM_BasicMeasure | DQM_Parameter
    Ref: {valueType shall be one of the data types defined in ISO/TS 19103}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.dqm.valuetype-failure-en" xml:lang="en">The valueType
      shall be one of the data types defined in ISO/TS 19103.</sch:diagnostic>
    
    <sch:diagnostic id="rule.dqm.valuetype-success-en" xml:lang="en">valueType is
      "<sch:value-of select="normalize-space($valueType)"/>". </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.dqm.valutype">
    <sch:title xml:lang="en">DQM_Measure.valueType shall be one of the data types defined in ISO/TS 19103</sch:title>
    
    <sch:rule context="//dqm:DQM_Measure | //dqm:DQM_BasicMeasure | dqm:DQM_Parameter">
      
      <sch:let name="valueType" value="dqm:valueType/gco:TypeName/gco:aName/gco:CharacterString"/>
      
      <sch:assert test="(index-of(
        ('Date','Time','DateTime','Number','Decimal','Integer','Real','Vector','CharacterString','Boolean','Set','Bag'),$valueType) > 0)"
        diagnostics="rule.dqm.valuetype-failure-en"/>
      
      <sch:report test="(index-of(
        ('Date','Time','DateTime','Number','Decimal','Integer','Real','Vector','CharacterString','Boolean','Set','Bag'),$valueType) > 0)"
        diagnostics="rule.dqm.valuetype-success-en"/>
    </sch:rule>
  </sch:pattern>  
</sch:schema>
