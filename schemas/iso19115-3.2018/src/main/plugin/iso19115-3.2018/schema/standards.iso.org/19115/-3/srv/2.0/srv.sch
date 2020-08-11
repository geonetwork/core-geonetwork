<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="srv" uri="http://standards.iso.org/iso/19115/-3/srv/2.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 23, Figure 18 Service metadata information classes
  -->
  
  <!-- 
    Rule: SV_ServiceIdentification
    Ref: {count(containsChain + containsOperations) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.chainoroperations-failure-en"
      xml:lang="en">The service identification does not contain chain or operation.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.chainoroperations-failure-fr"
      xml:lang="fr">L'identification du service ne contient ni chaîne d'opérations, ni opération.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.chainoroperations-success-en"
      xml:lang="en">
      The service identification contains the following 
      number of chains: <sch:value-of select="count($chains)"/>.
      and number of operations: <sch:value-of select="count($operations)"/>.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.chainoroperations-success-fr"
      xml:lang="fr">
      L'identification du service contient 
      le nombre de chaînes d'opérations suivant : <sch:value-of select="count($chains)"/>.
      le nombre d'opérations : <sch:value-of select="count($operations)"/>.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.chainoroperations">
    <sch:title xml:lang="en">Service identification MUST contains chain or operations</sch:title>
    <sch:title xml:lang="fr">L'identification du service DOIT contenir des chaînes d'opérations
      ou des opérations</sch:title>
    
    <sch:rule context="//srv:SV_ServiceIdentification">
      
      <!-- Consider only containsChain or operationsName
      having a name. -->
      <sch:let name="chains" value="srv:containsChain[
        normalize-space(srv:SV_OperationChainMetadata/srv:name) != '']"/>
      <sch:let name="operations" value="srv:containsOperations[
        normalize-space(srv:SV_OperationMetadata/srv:operationName) != '']"/>
      <sch:let name="hasChainOrOperation" 
        value="count($operations) + count($chains) > 0"/>
      
      <sch:assert test="$hasChainOrOperation"
        diagnostics="rule.srv.chainoroperations-failure-en 
                     rule.srv.chainoroperations-failure-fr"/>
      
      <sch:report test="$hasChainOrOperation"
        diagnostics="rule.srv.chainoroperations-success-en 
                     rule.srv.chainoroperations-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  <!-- 
    Rule: SV_ServiceIdentification
    Ref:  {If coupledResource exists then count(coupledResource) > 0}
    Comment: Can't be validated using schematron AFA the existence
    of the related object can't be defined. TODO-QUESTION
    -->
  
  
  <!-- 
    Rule: SV_ServiceIdentification
    Ref: {If coupledResource exists then count(couplingType) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.coupledresource-failure-en"
      xml:lang="en">The service identification MUST specify coupling type 
      when coupled resource exist</sch:diagnostic>
    <sch:diagnostic id="rule.srv.coupledresource-failure-fr"
      xml:lang="fr">L'identification du service DOIT 
      définir un type de couplage lorsqu'une ressource est couplée.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.coupledresource-success-en"
      xml:lang="en">
      Number of coupled resources: <sch:value-of select="count($coupledResource)"/>.
      Coupling type: "<sch:value-of select="$couplingType"/>".</sch:diagnostic>
    <sch:diagnostic id="rule.srv.coupledresource-success-fr"
      xml:lang="fr">
      Nombre de ressources couplées : <sch:value-of select="count($coupledResource)"/>.
      Type de couplage : "<sch:value-of select="$couplingType"/>".</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.coupledresource">
    <sch:title xml:lang="en">Service identification MUST specify coupling type 
      when coupled resource exist</sch:title>
    <sch:title xml:lang="fr">L'identification du service DOIT 
      définir un type de couplage lorsqu'une ressource est couplée</sch:title>
    
    <sch:rule context="//srv:SV_ServiceIdentification[srv:coupledResource]">
      
      <sch:let name="couplingType" 
               value="srv:couplingType/
                        srv:SV_CouplingType/@codeListValue[. != '']"/>
      <sch:let name="coupledResource" value="srv:coupledResource"/>
      <sch:let name="hasCouplingType" 
        value="count($couplingType) > 0"/>
      
      <sch:assert test="$hasCouplingType"
        diagnostics="rule.srv.coupledresource-failure-en 
                     rule.srv.coupledresource-failure-fr"/>
      
      <sch:report test="$hasCouplingType"
        diagnostics="rule.srv.coupledresource-success-en 
                     rule.srv.coupledresource-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!-- 
    Rule: SV_ServiceIdentification
    Ref: {If operatedDataset used then count (operatesOn) = 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.operateddataset-failure-en"
      xml:lang="en">The service identification define operatedDataset.
      No operatesOn can be specified.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.operateddataset-failure-fr"
      xml:lang="fr">L'identification du service utilise operatedDataset.
      OperatesOn ne peut être utilisé dans ce cas.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.operateddataset-success-en"
      xml:lang="en">Service identification only use operated dataset.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.operateddataset-success-fr"
      xml:lang="fr">L'identification du service n'utilise que operatedDataset.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.operateddataset">
    <sch:title xml:lang="en">Service identification MUST not use 
      both operatedDataset and operatesOn</sch:title>
    <sch:title xml:lang="fr">L'identification du service NE DOIT PAS 
      utiliser en même temps operatedDataset et operatesOn</sch:title>
    
    <sch:rule context="//srv:SV_ServiceIdentification[srv:operatedDataset]">
      
      <sch:let name="operatesOn" value="srv:operatesOn"/>
      <sch:let name="hasOperatesOn" 
        value="count($operatesOn) > 0"/>
      
      <sch:assert test="not($hasOperatesOn)"
        diagnostics="rule.srv.operateddataset-failure-en 
                     rule.srv.operateddataset-failure-fr"/>
      
      <sch:report test="not($hasOperatesOn)"
        diagnostics="rule.srv.operateddataset-success-en 
                     rule.srv.operateddataset-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!-- 
    Rule: SV_ServiceIdentification
    Ref: {If operatesOn used count(operatedDataset) = 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.operatesononly-failure-en"
      xml:lang="en">The service identification define operatesOn.
      No operatedDataset can be specified.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.operatesononly-failure-fr"
      xml:lang="fr">L'identification du service utilise operatesOn.
      OperatedDataset ne peut être utilisé dans ce cas.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.operatesononly-success-en"
      xml:lang="en">The service identification only use operates on.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.operatesononly-success-fr"
      xml:lang="fr">L'identification du service n'utilise que 
      des éléments de type operatesOn.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.operatesononly">
    <sch:title xml:lang="en">Service identification MUST not use 
      both operatesOn and operatedDataset</sch:title>
    <sch:title xml:lang="fr">L'identification du service NE DOIT PAS 
      utiliser en même temps operatesOn et operatedDataset</sch:title>
    
    <sch:rule context="//srv:SV_ServiceIdentification[srv:operatesOn]">
      
      <sch:let name="operatedDataset" value="srv:operatedDataset"/>
      
      <sch:let name="hasOperatedDataset" 
        value="count($operatedDataset) > 0"/>
      
      <sch:assert test="not($hasOperatedDataset)"
        diagnostics="rule.srv.operatesononly-failure-en 
                     rule.srv.operatesononly-failure-fr"/>
      
      <sch:report test="not($hasOperatedDataset)"
        diagnostics="rule.srv.operatesononly-success-en 
                     rule.srv.operatesononly-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!-- 
    Rule: SV_CoupledResource
    Ref: {count(resourceReference + resource) > 0}
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.harresourcereforresource-failure-en"
      xml:lang="en">The coupled resource does not contains a resource 
    nor a resource reference.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.harresourcereforresource-failure-fr"
      xml:lang="fr">La ressource couplée ne contient ni une ressource
    ni une référence à une ressource.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.harresourcereforresource-success-en"
      xml:lang="en">The coupled resource contains a resource or a resource reference.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.harresourcereforresource-success-fr"
      xml:lang="fr">La ressource couplée contient une ressource ou une référence 
      à une ressource.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.harresourcereforresource">
    <sch:title xml:lang="en">Coupled resource MUST contains 
      a resource or a resource reference</sch:title>
    <sch:title xml:lang="fr">Une ressource couplée DOIT
      définir une ressource ou une référence à une ressource</sch:title>
    
    <sch:rule context="//srv:SV_CoupledResource">
      
      <sch:let name="resourceReference" value="srv:resourceReference"/>
      <sch:let name="resource" value="srv:resource"/>
      
      <sch:let name="hasResourceReferenceOrResource" 
        value="count($resourceReference) + count($resource) > 0"/>
      
      <sch:assert test="$hasResourceReferenceOrResource"
        diagnostics="rule.srv.harresourcereforresource-failure-en 
                     rule.srv.harresourcereforresource-failure-fr"/>
      
      <sch:report test="$hasResourceReferenceOrResource"
        diagnostics="rule.srv.harresourcereforresource-success-en 
                     rule.srv.harresourcereforresource-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  
  
  
  <!-- 
    Rule: SV_CoupledResource
    Ref: {If resource used then count(resourceReference) = 0}
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.coupledresourceonlyresource-failure-en"
      xml:lang="en">The coupled resource contains both a resource 
      and a resource reference.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.coupledresourceonlyresource-failure-fr"
      xml:lang="fr">La ressource couplée utilise à la fois une ressource
      et une référence à une ressource.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.coupledresourceonlyresource-success-en"
      xml:lang="en">The coupled resource contains only resources.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.coupledresourceonlyresource-success-fr"
      xml:lang="fr">La ressource couplée contient uniquement des ressources.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.coupledresourceonlyresource">
    <sch:title xml:lang="en">Coupled resource MUST not use
      both resource and resource reference</sch:title>
    <sch:title xml:lang="fr">Une ressource couplée NE DOIT PAS
      utiliser en même temps une ressource et une référence
      à une ressource</sch:title>
    
    <sch:rule context="//srv:SV_CoupledResource[srv:resource]">
      
      <sch:let name="resourceReference" value="srv:resourceReference"/>
      <sch:let name="hasResourceReference" 
        value="count($resourceReference) > 0"/>
      
      <sch:assert test="not($hasResourceReference)"
        diagnostics="rule.srv.coupledresourceonlyresource-failure-en 
                     rule.srv.coupledresourceonlyresource-failure-fr"/>
      
      <sch:report test="not($hasResourceReference)"
        diagnostics="rule.srv.coupledresourceonlyresource-success-en 
                     rule.srv.coupledresourceonlyresource-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  <!-- 
    Rule: SV_CoupledResource
    Ref: {If resourceReference used then count(resource) = 0}
  -->
  
  <sch:diagnostics>
    <sch:diagnostic id="rule.srv.coupledresourceonlyresourceref-failure-en"
      xml:lang="en">The coupled resource contains both a resource 
      and a resource reference.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.coupledresourceonlyresourceref-failure-fr"
      xml:lang="fr">La ressource couplée utilise à la fois une ressource
      et une référence à une ressource.</sch:diagnostic>
    
    <sch:diagnostic id="rule.srv.coupledresourceonlyresourceref-success-en"
      xml:lang="en">The coupled resource contains only resource references.</sch:diagnostic>
    <sch:diagnostic id="rule.srv.coupledresourceonlyresourceref-success-fr"
      xml:lang="fr">La ressource couplée contient uniquement des références à des ressources.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.coupledresourceonlyresourceref">
    <sch:title xml:lang="en">Coupled resource MUST not use
      both resource and resource reference</sch:title>
    <sch:title xml:lang="fr">Une ressource couplée NE DOIT PAS
      utiliser en même temps une ressource et une référence
      à une ressource</sch:title>
    
    <sch:rule context="//srv:SV_CoupledResource[srv:resourceReference]">
      
      <sch:let name="resource" value="srv:resource"/>
      <sch:let name="hasResource" 
        value="count($resource) > 0"/>
      
      <sch:assert test="not($hasResource)"
        diagnostics="rule.srv.coupledresourceonlyresourceref-failure-en 
                     rule.srv.coupledresourceonlyresourceref-failure-fr"/>
      
      <sch:report test="not($hasResource)"
        diagnostics="rule.srv.coupledresourceonlyresourceref-success-en 
                     rule.srv.coupledresourceonlyresourceref-success-fr"/>
    </sch:rule>
  </sch:pattern>
</sch:schema>
