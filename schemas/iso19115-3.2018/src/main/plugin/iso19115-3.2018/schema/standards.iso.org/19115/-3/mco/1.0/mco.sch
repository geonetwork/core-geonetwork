<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="mco" uri="http://standards.iso.org/iso/19115/-3/mco/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 13, Figure 8 Constraint information classes
  -->
  
  <!-- 
    Rule: MD_Releasability
    Ref: {count(addressee + statement) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mco-releasability-failure-en"
      xml:lang="en">
      The releasabilty does not define addresse or statement.</sch:diagnostic>
    <sch:diagnostic id="rule.mco-releasability-failure-fr"
      xml:lang="fr">
      La possibilité de divulgation ne définit pas de 
      destinataire ou d'indication.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mco-releasability-success-en"
      xml:lang="en">
      The releasability addressee is defined: 
      "<sch:value-of select="normalize-space($addressee)"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mco-releasability-success-fr"
      xml:lang="fr">
      Le destinataire dans le cas de possibilité de divulgation 
      est défini "<sch:value-of select="normalize-space($addressee)"/>".
    </sch:diagnostic>
    
    <sch:diagnostic id="rule.mco-releasability-statement-success-en"
      xml:lang="en">
      The releasability statement is
      "<sch:value-of select="normalize-space($statement)"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mco-releasability-statement-success-fr"
      xml:lang="fr">
      L'indication concernant la possibilité de divulgation est 
      "<sch:value-of select="normalize-space($statement)"/>".
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mco-releasability">
    <sch:title xml:lang="en">Releasability MUST
    specified an addresse or a statement</sch:title>
    <sch:title xml:lang="fr">La possibilité de divulgation 
      DOIT définir un destinataire ou une indication</sch:title>
    
    <sch:rule context="//mco:MD_Releasability">
      
      <sch:let name="addressee" 
        value="mco:addressee[normalize-space(.) != '']"/>
      
      <sch:let name="statement" 
        value="mco:statement/*[normalize-space(.) != '']"/>
      
      <sch:let name="hasAddresseeOrStatement" 
        value="count($addressee) + 
               count($statement) > 0"/>
      
      <sch:assert test="$hasAddresseeOrStatement"
        diagnostics="rule.mco-releasability-failure-en 
                     rule.mco-releasability-failure-fr"/>
      
      <sch:report test="count($addressee)"
        diagnostics="rule.mco-releasability-success-en 
                     rule.mco-releasability-success-fr"/>
      
      <sch:report test="count($statement)"
        diagnostics="rule.mco-releasability-statement-success-en 
                     rule.mco-releasability-statement-success-fr"/>
      
    </sch:rule>
  </sch:pattern>
  
  
  <!--
    Rule: MD_LegalConstraints
    Ref: {If MD_LegalConstraints used then 
          count of (accessConstraints +
                    useConstraints + 
                    otherConstraints + 
                    useLimitation + 
                    releasability) > 0}
         -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mco-legalconstraintdetails-failure-en"
      xml:lang="en">
      The legal constraint is incomplete.</sch:diagnostic>
    <sch:diagnostic id="rule.mco-legalconstraintdetails-failure-fr"
      xml:lang="fr">
      La contrainte légale est incomplète.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mco-legalconstraintdetails-success-en"
      xml:lang="en">
      The legal constraint is complete.
    </sch:diagnostic>
    <sch:diagnostic id="rule.mco-legalconstraintdetails-success-fr"
      xml:lang="fr">
      La contrainte légale est complète.
    </sch:diagnostic>
    
  </sch:diagnostics>
  
  <sch:pattern id="rule.mco-legalconstraintdetails">
    <sch:title xml:lang="en">Legal constraint MUST
      specified an access, use or other constraint or
      use limitation or releasability</sch:title>
    <sch:title xml:lang="fr">Une contrainte légale DOIT
      définir un type de contrainte (d'accès, d'utilisation ou autre)
      ou bien une limite d'utilisation ou une possibilité de divulgation</sch:title>
    
    <sch:rule context="//mco:MD_LegalConstraints">
      
      <sch:let name="accessConstraints" 
        value="mco:accessConstraints[
                normalize-space(.) != '' or
                count(.//@codeListValue[. != '']) > 0]"/>
      
      <sch:let name="useConstraints" 
        value="mco:useConstraints/*[
                 normalize-space(.) != '' or
                 count(.//@codeListValue[. != '']) > 0]"/>
      
      <sch:let name="otherConstraints" 
        value="mco:otherConstraints/*[
                 normalize-space(.) != '']"/>
      
      <sch:let name="useLimitation" 
        value="mco:useLimitation/*[
                 normalize-space(.) != '' or
                 count(.//@codeListValue[. != '']) > 0]"/>
      
      <sch:let name="releasability" 
        value="mco:releasability/*[
                 normalize-space(.) != '' or
                 count(.//@codeListValue[. != '']) > 0]"/>
      
      <sch:let name="hasDetails" 
               value="count($accessConstraints) + 
                      count($useConstraints) + 
                      count($otherConstraints) + 
                      count($useLimitation) + 
                      count($releasability)
                      > 0"/>
      
      <sch:assert test="$hasDetails"
        diagnostics="rule.mco-legalconstraintdetails-failure-en 
                     rule.mco-legalconstraintdetails-failure-fr"/>
      
      <sch:report test="$hasDetails"
        diagnostics="rule.mco-legalconstraintdetails-success-en 
                     rule.mco-legalconstraintdetails-success-fr"/>
      
    </sch:rule>
  </sch:pattern>
  
  <!--
    Rule: MD_LegalConstraints
    Ref: {otherConstraints: only documented if accessConstraints or
      useConstraints = “otherRestrictions”}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mco-legalconstraint-other-failure-en"
      xml:lang="en">
      The legal constraint does not specified other constraints
      while access and use constraint is set to other restriction.</sch:diagnostic>
    <sch:diagnostic id="rule.mco-legalconstraint-other-failure-fr"
      xml:lang="fr">
      La contrainte légale ne précise pas les autres contraintes
      bien que les contraintes d'accès ou d'usage indiquent 
      que d'autres restrictions s'appliquent.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mco-legalconstraint-other-success-en"
      xml:lang="en">
      The legal constraint other constraints is 
      "<sch:value-of select="$otherConstraints"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mco-legalconstraint-other-success-fr"
      xml:lang="fr">
      Les autres contraintes de la contrainte légale sont
      "<sch:value-of select="$otherConstraints"/>".
    </sch:diagnostic>
    
  </sch:diagnostics>
  
  <sch:pattern id="rule.mco-legalconstraint-other">
    <sch:title xml:lang="en">Legal constraint defining
      other restrictions for access or use constraint MUST
      specified other constraint.</sch:title>
    <sch:title xml:lang="fr">Une contrainte légale indiquant
      d'autres restrictions d'utilisation ou d'accès DOIT
      préciser ces autres restrictions</sch:title>
    
    <sch:rule context="//mco:MD_LegalConstraints[
      mco:accessConstraints/mco:MD_RestrictionCode/@codeListValue = 'otherRestrictions' or
      mco:useConstraints/mco:MD_RestrictionCode/@codeListValue = 'otherRestrictions'
      ]">
      
      
      <sch:let name="otherConstraints" 
               value="mco:otherConstraints/*[normalize-space(.) != '']"/>
      
      <sch:let name="hasOtherConstraints" 
               value="count($otherConstraints) > 0"/>
      
      <sch:assert test="$hasOtherConstraints"
        diagnostics="rule.mco-legalconstraint-other-failure-en 
                     rule.mco-legalconstraint-other-failure-fr"/>
      
      <sch:report test="$hasOtherConstraints"
        diagnostics="rule.mco-legalconstraint-other-success-en 
                     rule.mco-legalconstraint-other-success-fr"/>
      
    </sch:rule>
  </sch:pattern>
  
</sch:schema>
