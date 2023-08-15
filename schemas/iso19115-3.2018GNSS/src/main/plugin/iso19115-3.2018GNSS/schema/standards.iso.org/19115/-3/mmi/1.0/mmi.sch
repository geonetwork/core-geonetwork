<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="mmi" uri="http://standards.iso.org/iso/19115/-3/mmi/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 15, Figure 10 Maintenance information classes
  -->
  
  <!-- 
    Rule: MD_MaintenanceInformation
    Ref: {count(maintenanceAndUpdateFrequency + 
                userDefinedMaintenanceFrequency) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mmi-updatefrequency-failure-en"
      xml:lang="en">
      The maintenance information does not define update frequency.</sch:diagnostic>
    <sch:diagnostic id="rule.mmi-updatefrequency-failure-fr"
      xml:lang="fr">
      L'information sur la maintenance ne définit pas de fréquence de mise à jour.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mmi-updatefrequency-success-en"
      xml:lang="en">
      The update frequency is "<sch:value-of select="$maintenanceAndUpdateFrequency"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mmi-updatefrequency-success-fr"
      xml:lang="fr">
      La fréquence de mise à jour est "<sch:value-of select="$maintenanceAndUpdateFrequency"/>".
    </sch:diagnostic>
    
    <sch:diagnostic id="rule.mmi-updatefrequency-user-success-en"
      xml:lang="en">
      The user defined update frequency is 
      "<sch:value-of select="normalize-space($userDefinedMaintenanceFrequency)"/>".
    </sch:diagnostic>
    <sch:diagnostic id="rule.mmi-updatefrequency-user-success-fr"
      xml:lang="fr">
      La fréquence de mise à jour définie par l'utilisateur est 
      "<sch:value-of select="normalize-space($userDefinedMaintenanceFrequency)"/>".
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mmi-updatefrequency">
    <sch:title xml:lang="en">Maintenance information MUST
    specified an update frequency</sch:title>
    <sch:title xml:lang="fr">L'information sur la maintenance
      DOIT définir une fréquence de mise à jour</sch:title>
    
    <sch:rule context="//mmi:MD_MaintenanceInformation">
      
      <sch:let name="userDefinedMaintenanceFrequency" 
        value="mmi:userDefinedMaintenanceFrequency/
                gco:TM_PeriodDuration[normalize-space(.) != '']"/>
      
      <sch:let name="maintenanceAndUpdateFrequency" 
        value="mmi:maintenanceAndUpdateFrequency/
                mmi:MD_MaintenanceFrequencyCode/@codeListValue[normalize-space(.) != '']"/>
      
      <sch:let name="hasCodeOrUserFreq" 
        value="count($maintenanceAndUpdateFrequency) + 
               count($userDefinedMaintenanceFrequency) > 0"/>
      
      <sch:assert test="$hasCodeOrUserFreq"
        diagnostics="rule.mmi-updatefrequency-failure-en 
                     rule.mmi-updatefrequency-failure-fr"/>
      
      <sch:report test="count($userDefinedMaintenanceFrequency)"
        diagnostics="rule.mmi-updatefrequency-user-success-en 
                     rule.mmi-updatefrequency-user-success-fr"/>
      
      <sch:report test="count($maintenanceAndUpdateFrequency)"
        diagnostics="rule.mmi-updatefrequency-success-en 
                     rule.mmi-updatefrequency-success-fr"/>
      
    </sch:rule>
  </sch:pattern>
  
</sch:schema>
