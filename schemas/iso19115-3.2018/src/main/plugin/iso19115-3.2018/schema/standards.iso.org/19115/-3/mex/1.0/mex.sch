<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/1.0"/>
  <sch:ns prefix="mri" uri="http://standards.iso.org/iso/19115/-3/mri/1.0"/>
  <sch:ns prefix="mex" uri="http://standards.iso.org/iso/19115/-3/mex/1.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="lan" uri="http://standards.iso.org/iso/19115/-3/lan/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>

  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 21, Figure 16 Metadata extension information classes
  -->
  
  <!-- 
    Rule: MD_ExtendedElementInformation
    Ref: {if dataType notEqual codelist, enumeration, or codelistElement, 
          then
          obligation, maximumOccurence and domainValue are mandatory}
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mex.datatypedetails-maxocc-failure-en"
      xml:lang="en">
      Extended element information "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      does not specified max occurence.</sch:diagnostic>
    <sch:diagnostic id="rule.mex.datatypedetails-maxocc-failure-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      ne précise pas le nombre d'occurences maximum.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mex.datatypedetails-maxocc-success-en"
      xml:lang="en">
      Extended element information "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      has max occurence: "<sch:value-of select="$maximumOccurrence"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mex.datatypedetails-maxocc-success-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      a pour nombre d'occurences maximum : "<sch:value-of select="$maximumOccurrence"/>".
    </sch:diagnostic>
    
    
    
    <sch:diagnostic id="rule.mex.datatypedetails-domain-failure-en"
      xml:lang="en">
      Extended element information "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      does not specified domain value.</sch:diagnostic>
    <sch:diagnostic id="rule.mex.datatypedetails-domain-failure-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      ne précise pas la valeur du domaine.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mex.datatypedetails-domain-success-en"
      xml:lang="en">
      Extended element information "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      has domain value: "<sch:value-of select="$domainValue"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mex.datatypedetails-domain-success-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      a pour valeur du domaine : "<sch:value-of select="$domainValue"/>".
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mex.datatypedetails">
    <sch:title xml:lang="en">Extended element information 
      which are not codelist, enumeration or codelistElement 
      MUST specified max occurence and domain value</sch:title>
    <sch:title xml:lang="fr">Un élément d'extension qui n'est
      ni une codelist, ni une énumération, ni un élément de codelist
      DOIT préciser le nombre maximum d'occurences 
      ainsi que la valeur du domaine</sch:title>
    
    <sch:rule context="//mex:MD_ExtendedElementInformation[
      mex:dataType/mex:MD_DatatypeCode/@codeListValue != 'codelist' and
      mex:dataType/mex:MD_DatatypeCode/@codeListValue != 'enumeration' and
      mex:dataType/mex:MD_DatatypeCode/@codeListValue != 'codelistElement'
      ]">
      
      <sch:let name="name" 
        value="normalize-space(mex:name/*)"/>
      
      <sch:let name="dataType" 
        value="normalize-space(mex:dataType/mex:MD_DatatypeCode/@codeListValue)"/>
      
      
      <sch:let name="maximumOccurrence" 
        value="normalize-space(mex:maximumOccurrence/*)"/>
      
      <sch:let name="hasMaximumOccurrence" 
        value="$maximumOccurrence != ''"/>
      
      <sch:assert test="$hasMaximumOccurrence"
        diagnostics="rule.mex.datatypedetails-maxocc-failure-en 
                     rule.mex.datatypedetails-maxocc-failure-fr"/>
      
      <sch:report test="$hasMaximumOccurrence"
        diagnostics="rule.mex.datatypedetails-maxocc-success-en 
                     rule.mex.datatypedetails-maxocc-success-fr"/>
      
      
      <sch:let name="domainValue" 
        value="normalize-space(mex:domainValue/*)"/>
      
      <sch:let name="hasDomainValue" 
        value="$domainValue != ''"/>
      
      <sch:assert test="$hasDomainValue"
        diagnostics="rule.mex.datatypedetails-domain-failure-en 
                     rule.mex.datatypedetails-domain-failure-fr"/>
      
      <sch:report test="$hasDomainValue"
        diagnostics="rule.mex.datatypedetails-domain-success-en 
                     rule.mex.datatypedetails-domain-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  <!-- 
    Rule: MD_ExtendedElementInformation
    Ref:  {if obligation = conditional then condition is mandatory}
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mex.conditional-failure-en"
      xml:lang="en">
      The conditional extended element "<sch:value-of select="$name"/>"
      does not specified the condition.</sch:diagnostic>
    <sch:diagnostic id="rule.mex.conditional-failure-fr"
      xml:lang="fr">
      L'élément d'extension conditionnel "<sch:value-of select="$name"/>"
      ne précise pas les termes de la condition.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mex.conditional-success-en"
      xml:lang="en">
      The conditional extended element "<sch:value-of select="$name"/>"
      has for condition: "<sch:value-of select="$condition"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mex.conditional-success-fr"
      xml:lang="fr">
      L'élément d'extension conditionnel "<sch:value-of select="$name"/>"
      a pour condition : "<sch:value-of select="$condition"/>".
    </sch:diagnostic>
    
  </sch:diagnostics>
  
  <sch:pattern id="rule.mex.conditional">
    <sch:title xml:lang="en">Extended element information 
      which are conditional MUST explained the condition</sch:title>
    <sch:title xml:lang="fr">Un élément d'extension conditionnel
      DOIT préciser les termes de la condition</sch:title>
    
    <sch:rule context="//mex:MD_ExtendedElementInformation[
      mex:obligation/mex:MD_ObligationCode = 'conditional'
      ]">
      
      <sch:let name="name" 
        value="normalize-space(mex:name/*)"/>
      
      <sch:let name="condition" 
        value="normalize-space(mex:condition/*)"/>
      
      <sch:let name="hasCondition" 
        value="$condition != ''"/>
      
      <sch:assert test="$hasCondition"
        diagnostics="rule.mex.conditional-failure-en 
                     rule.mex.conditional-failure-fr"/>
      
      <sch:report test="$hasCondition"
        diagnostics="rule.mex.conditional-success-en 
                     rule.mex.conditional-success-fr"/>
      
    </sch:rule>
  </sch:pattern>
  
  
  
  
  
  <!-- 
    Rule: MD_ExtendedElementInformation
    Ref: {if dataType = codelistElement, enumeration, or codelist then code is
        mandatory}
        
    Ref: {if dataType = codelistElement, enumeration, or codelist then
        conceptName is mandatory}
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mex.mandatorycode-failure-en"
      xml:lang="en">
      The extended element "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      does not specified a code.</sch:diagnostic>
    <sch:diagnostic id="rule.mex.mandatorycode-failure-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      ne précise pas de code.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mex.mandatorycode-success-en"
      xml:lang="en">
      The extended element "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      has for code: "<sch:value-of select="$code"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mex.mandatorycode-success-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      a pour code : "<sch:value-of select="$code"/>".
    </sch:diagnostic>
    
    
    
    
    
    <sch:diagnostic id="rule.mex.mex.mandatoryconceptname-failure-en"
      xml:lang="en">
      The extended element "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      does not specified a concept name.</sch:diagnostic>
    <sch:diagnostic id="rule.mex.mex.mandatoryconceptname-failure-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      ne précise pas de nom de concept.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mex.mex.mandatoryconceptname-success-en"
      xml:lang="en">
      The extended element "<sch:value-of select="$name"/>"
      of type "<sch:value-of select="$dataType"/>"
      has for concept name: "<sch:value-of select="$conceptName"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mex.mex.mandatoryconceptname-success-fr"
      xml:lang="fr">
      L'élément d'extension "<sch:value-of select="$name"/>"
      de type "<sch:value-of select="$dataType"/>"
      a pour nom de concept : "<sch:value-of select="$conceptName"/>".
    </sch:diagnostic>
    
  </sch:diagnostics>
  
  <sch:pattern id="rule.mex.mandatorycode">
    <sch:title xml:lang="en">Extended element information 
      which are codelist, enumeration or codelistElement 
      MUST specified a code and a concept name</sch:title>
    <sch:title xml:lang="fr">Un élément d'extension qui est
      une codelist, une énumération, un élément de codelist
      DOIT préciser un code et un nom de concept</sch:title>
    
    <sch:rule context="//mex:MD_ExtendedElementInformation[
      mex:dataType/mex:MD_DatatypeCode/@codeListValue = 'codelist' or
      mex:dataType/mex:MD_DatatypeCode/@codeListValue = 'enumeration' or
      mex:dataType/mex:MD_DatatypeCode/@codeListValue = 'codelistElement'
      ]">
      
      <sch:let name="name" 
        value="normalize-space(mex:name/*)"/>
      
      <sch:let name="dataType" 
        value="normalize-space(mex:dataType/mex:MD_DatatypeCode/@codeListValue)"/>
      
      <sch:let name="code" 
        value="normalize-space(mex:code/*)"/>
      
      <sch:let name="hasCode" 
        value="$code != ''"/>
      
      <sch:assert test="$hasCode"
        diagnostics="rule.mex.mandatorycode-failure-en 
                     rule.mex.mandatorycode-failure-fr"/>
      
      <sch:report test="$hasCode"
        diagnostics="rule.mex.mandatorycode-success-en 
                     rule.mex.mandatorycode-success-fr"/>
      
      
      
      <sch:let name="conceptName" 
        value="normalize-space(mex:conceptName/*)"/>
      
      <sch:let name="hasConceptName" 
        value="$conceptName != ''"/>
      
      <sch:assert test="$hasConceptName"
        diagnostics="rule.mex.mex.mandatoryconceptname-failure-en 
                     rule.mex.mex.mandatoryconceptname-failure-fr"/>
      
      <sch:report test="$hasConceptName"
        diagnostics="rule.mex.mex.mandatoryconceptname-success-en 
                     rule.mex.mex.mandatoryconceptname-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!-- 
    Rule: MD_ExtendedElementInformation
    Ref: {if dataType = codelist, enumeration, or codelistElement then name is
        not used}
    Comment: No test. Should we set the element invalid if name is set ? TODO-QUESTION
  -->
  
  
  
</sch:schema>
