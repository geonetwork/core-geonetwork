<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="mrc" uri="http://standards.iso.org/iso/19115/-3/mrc/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 18, Figure 13 Content information classes
  -->
  
  
  
  <!-- 
    Rule: MD_FeatureCatalogueDescription
    Ref: {if FeatureCatalogue not included with resource and
          MD_FeatureCatalogue not provided then
            featureCatalogueCitation > 0}
    Comment: No test because feature catalogue with resource can't be asserted (TODO-QUESTION)
    -->
  
  
  
  <!-- 
    Rule: MD_SampleDimension
    Ref: {if count(maxValue + minValue + meanValue) > 0 then units is
          mandatory}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mrc.sampledimension-failure-en"
      xml:lang="en">The sample dimension does not provide max, min or mean value.</sch:diagnostic>
    <sch:diagnostic id="rule.mrc.sampledimension-failure-fr"
      xml:lang="fr">La dimension ne précise pas de valeur maximum ou minimum ni de moyenne.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mrc.sampledimension-max-success-en"
      xml:lang="en">
      The sample dimension max value is 
      "<sch:value-of select="normalize-space($max)"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mrc.sampledimension-max-success-fr"
      xml:lang="fr">
      La valeur maximum de la dimension de l'échantillon est
      "<sch:value-of select="normalize-space($max)"/>".
    </sch:diagnostic>
    
    <sch:diagnostic id="rule.mrc.sampledimension-min-success-en"
      xml:lang="en">
      The sample dimension min value is 
      "<sch:value-of select="normalize-space($min)"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mrc.sampledimension-min-success-fr"
      xml:lang="fr">
      La valeur minimum de la dimension de l'échantillon est
      "<sch:value-of select="normalize-space($min)"/>".
    </sch:diagnostic>
    
    <sch:diagnostic id="rule.mrc.sampledimension-mean-success-en"
      xml:lang="en">
      The sample dimension mean value is 
      "<sch:value-of select="normalize-space($mean)"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mrc.sampledimension-mean-success-fr"
      xml:lang="fr">
      La valeur moyenne de la dimension de l'échantillon est
      "<sch:value-of select="normalize-space($mean)"/>".
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mrc.sampledimension">
    <sch:title xml:lang="en">Sample dimension MUST provide a max, 
      a min or a mean value</sch:title>
    <sch:title xml:lang="fr">La dimension de l'échantillon DOIT préciser
      une valeur maximum, une valeur minimum ou une moyenne</sch:title>
    
    <sch:rule context="//mrc:MD_SampleDimension">
      
      <sch:let name="max" 
        value="mrc:maxValue[normalize-space(*) != '']"/>
      <sch:let name="min" 
        value="mrc:minValue[normalize-space(*) != '']"/>
      <sch:let name="mean" 
        value="mrc:meanValue[normalize-space(*) != '']"/>
      
      <sch:let name="hasMaxOrMinOrMean" 
        value="count($max) + count($min) + count($mean) > 0"/>
      
      <sch:assert test="$hasMaxOrMinOrMean"
        diagnostics="rule.mrc.sampledimension-failure-en 
                     rule.mrc.sampledimension-failure-fr"/>
      
      <sch:report test="count($max)"
        diagnostics="rule.mrc.sampledimension-max-success-en 
                     rule.mrc.sampledimension-max-success-fr"/>
      <sch:report test="count($min)"
        diagnostics="rule.mrc.sampledimension-min-success-en 
                     rule.mrc.sampledimension-min-success-fr"/>
      <sch:report test="count($mean)"
        diagnostics="rule.mrc.sampledimension-mean-success-en 
                     rule.mrc.sampledimension-mean-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!-- 
    Rule: MD_Band
    Ref: {if count(boundMax + boundMin) > 0 then boundUnits is mandatory}
    -->
  
  <sch:diagnostics>
    <sch:diagnostic id="rule.mrc.bandunit-failure-en"
      xml:lang="en">The band defined a bound without unit.</sch:diagnostic>
    <sch:diagnostic id="rule.mrc.bandunit-failure-fr"
      xml:lang="fr">La bande définit une borne minimum et/ou maximum
      sans préciser d'unité.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mrc.bandunit-success-en"
      xml:lang="en">
      The band bound [<sch:value-of select="$min"/>-<sch:value-of select="$max"/>] unit is 
      "<sch:value-of select="$units"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mrc.bandunit-success-fr"
      xml:lang="fr">
      L'unité de la borne [<sch:value-of select="$min"/>-<sch:value-of select="$max"/>] est 
      "<sch:value-of select="$units"/>".
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mrc.bandunit">
    <sch:title xml:lang="en">Band MUST specified bounds units 
      when a bound max or bound min is defined</sch:title>
    <sch:title xml:lang="fr">Une bande DOIT préciser l'unité 
      lorsqu'une borne maximum ou minimum est définie</sch:title>
    
    <sch:rule context="//mrc:MD_Band[
      normalize-space(mrc:boundMax/*) != '' or 
      normalize-space(mrc:boundMin/*) != ''
      ]">
      
      <sch:let name="max" 
        value="normalize-space(mrc:boundMax/*)"/>
      <sch:let name="min" 
        value="normalize-space(mrc:boundMin/*)"/>
      <sch:let name="units" 
        value="normalize-space(mrc:boundUnits[normalize-space(*) != ''])"/>
      
      <sch:let name="hasUnits" 
        value="$units != ''"/>
      
      <sch:assert test="$hasUnits"
        diagnostics="rule.mrc.bandunit-failure-en 
        rule.mrc.bandunit-failure-fr"/>
      
      <sch:report test="$hasUnits"
        diagnostics="rule.mrc.bandunit-success-en 
                     rule.mrc.bandunit-success-fr"/>
    </sch:rule>
  </sch:pattern>
</sch:schema>
