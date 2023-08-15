<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/1.0"/>
  <sch:ns prefix="mri" uri="http://standards.iso.org/iso/19115/-3/mri/1.0"/>
  <sch:ns prefix="srv" uri="http://standards.iso.org/iso/19115/-3/srv/2.0"/>
  <sch:ns prefix="mdb" uri="http://standards.iso.org/iso/19115/-3/mdb/1.0"/>
  <sch:ns prefix="gex" uri="http://standards.iso.org/iso/19115/-3/gex/1.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="mrc" uri="http://standards.iso.org/iso/19115/-3/mrc/1.0"/>
  <sch:ns prefix="lan" uri="http://standards.iso.org/iso/19115/-3/lan/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 11, Figure 6
  -->
  
  <!-- 
    Rule: MD_Identification
    Ref: {(MD_Metadata.metadataScope.MD_MetadataScope.resourceScope)=’dataset’
    implies count(
    extent.geographicElement.EX_GeographicBoundingBox +
    extent.geographicElement.EX_GeographicDescription) >= 1}
    -->
  
  <sch:diagnostics>
    <sch:diagnostic id="rule.mri.datasetextent-failure-en"
      xml:lang="en">The dataset MUST provide a 
      geographic description or a bounding box.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.datasetextent-failure-fr"
      xml:lang="fr">Le jeu de données DOIT être décrit par
      une description géographique ou une emprise.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mri.datasetextentdesc-success-en"
      xml:lang="en">The dataset geographic description is:
      "<sch:value-of select="normalize-space($geodescription)"/>".</sch:diagnostic>
    <sch:diagnostic id="rule.mri.datasetextentdesc-success-fr"
      xml:lang="fr">La description géographique du jeu de données est
      "<sch:value-of select="normalize-space($geodescription)"/>".</sch:diagnostic>
    
    
    <sch:diagnostic id="rule.mri.datasetextentbox-success-en"
      xml:lang="en">The dataset geographic bounding box is:
      [W:<sch:value-of select="$geobox/gex:westBoundLongitude/*/text()"/>,
      S:<sch:value-of select="$geobox/gex:southBoundLatitude/*/text()"/>],
      [E:<sch:value-of select="$geobox/gex:eastBoundLongitude/*/text()"/>,
      N:<sch:value-of select="$geobox/gex:northBoundLatitude/*/text()"/>],
      .</sch:diagnostic>
    <sch:diagnostic id="rule.mri.datasetextentbox-success-fr"
      xml:lang="fr">L'emprise géographique du jeu de données est
      [W:<sch:value-of select="$geobox/gex:westBoundLongitude/*/text()"/>,
      S:<sch:value-of select="$geobox/gex:southBoundLatitude/*/text()"/>],
      [E:<sch:value-of select="$geobox/gex:eastBoundLongitude/*/text()"/>,
      N:<sch:value-of select="$geobox/gex:northBoundLatitude/*/text()"/>]
      .</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mri.datasetextent">
    <sch:title xml:lang="en">Dataset extent</sch:title>
    <sch:title xml:lang="fr">Emprise du jeu de données</sch:title>
    
    <sch:rule context="/mdb:MD_Metadata[mdb:metadataScope/
                          mdb:MD_MetadataScope/mdb:resourceScope/
                          mcc:MD_ScopeCode/@codeListValue = 'dataset']/
                          mdb:identificationInfo/mri:MD_DataIdentification">
      
      
      <sch:let name="geodescription" 
        value="mri:extent/gex:EX_Extent/gex:geographicElement/
                  gex:EX_GeographicDescription/gex:geographicIdentifier[
                  normalize-space(mcc:MD_Identifier/mcc:code/*/text()) != ''
                  ]"/>
      <sch:let name="geobox" 
        value="mri:extent/gex:EX_Extent/gex:geographicElement/
                  gex:EX_GeographicBoundingBox[
                  normalize-space(gex:westBoundLongitude/gco:Decimal) != '' and
                  normalize-space(gex:eastBoundLongitude/gco:Decimal) != '' and
                  normalize-space(gex:southBoundLatitude/gco:Decimal) != '' and
                  normalize-space(gex:northBoundLatitude/gco:Decimal) != ''
                  ]"/>

      <sch:let name="hasGeoextent" 
               value="count($geodescription) + count($geobox) > 0"/>
      
      
      <sch:assert test="$hasGeoextent"
        diagnostics="rule.mri.datasetextent-failure-en 
                     rule.mri.datasetextent-failure-fr"/>
      
      <!-- TODO: Improve reporting when having multiple elements -->
      <sch:report test="count($geodescription) > 0"
        diagnostics="rule.mri.datasetextentdesc-success-en 
                     rule.mri.datasetextentdesc-success-fr"/>
      <sch:report test="count($geobox) > 0"
        diagnostics="rule.mri.datasetextentbox-success-en 
                     rule.mri.datasetextentbox-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  <!--
    Ref: {(MD_Metadata.metadataScope.MD_Scope.resourceScope) = 
            (’dataset’ or ‘series’)
          implies topicCategory is mandatory}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mri.topicategoryfordsandseries-failure-en"
      xml:lang="en">A topic category MUST be specified for 
      dataset or series.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.topicategoryfordsandseries-failure-fr"
      xml:lang="fr">Un thème principal (ISO) DOIT être défini quand
      la ressource est un jeu de donnée ou une série.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mri.topicategoryfordsandseries-success-en"
      xml:lang="en">Number of topic category identified: 
      <sch:value-of select="count($topics)"/>.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.topicategoryfordsandseries-success-fr"
      xml:lang="fr">Nombre de thèmes : 
      <sch:value-of select="count($topics)"/>.</sch:diagnostic>
  </sch:diagnostics>
  
  
  <sch:pattern id="rule.mri.topicategoryfordsandseries">
    <sch:title xml:lang="en">Topic category for dataset and series</sch:title>
    <sch:title xml:lang="fr">Thème principal d'un jeu de données ou d'une série</sch:title>
    
    <sch:rule context="/mdb:MD_Metadata[mdb:metadataScope/
                         mdb:MD_MetadataScope/mdb:resourceScope/
                         mcc:MD_ScopeCode/@codeListValue = 'dataset' or 
                         mdb:metadataScope/
                         mdb:MD_MetadataScope/mdb:resourceScope/
                         mcc:MD_ScopeCode/@codeListValue = 'series']/
                         mdb:identificationInfo/mri:MD_DataIdentification">
      
      <!-- The topic category is the enumeration value and
      not the human readable one. -->
      <sch:let name="topics" 
               value="mri:topicCategory/mri:MD_TopicCategoryCode"/>
      <sch:let name="hasTopics"
               value="count($topics) > 0"/>
      
      <sch:assert test="$hasTopics"
        diagnostics="rule.mri.topicategoryfordsandseries-failure-en 
                     rule.mri.topicategoryfordsandseries-failure-fr"/>
      
      <sch:report test="$hasTopics"
        diagnostics="rule.mri.topicategoryfordsandseries-success-en 
                     rule.mri.topicategoryfordsandseries-success-fr"/>
      
    </sch:rule>
  </sch:pattern>


  <!--
    Rule: MD_AssociatedResource
    Ref: {count(name + metadataReference) > 0}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mri.associatedresource-failure-en"
      xml:lang="en">When a resource is associated, a name or a metadata
      reference MUST be specified.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.associatedresource-failure-fr"
      xml:lang="fr">Lorsqu'une resource est associée, un nom ou une 
      référence à une fiche DOIT être défini.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mri.associatedresource-success-en"
      xml:lang="en">The resource "<sch:value-of select="$resourceRef"/>"
      is associated.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.associatedresource-success-fr"
      xml:lang="fr">La ressource "<sch:value-of select="$resourceRef"/>" 
      est associée.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mri.associatedresource">
    <sch:title xml:lang="en">Associated resource name</sch:title>
    <sch:title xml:lang="fr">Nom ou référence à une ressource associée</sch:title>
    
    <sch:rule context="//mri:MD_DataIdentification/mri:associatedResource/*|
                       //srv:SV_ServiceIdentification/mri:associatedResource/*">
      
      <!-- May be a CharacterString or LocalisedCharacterString -->
      <sch:let name="nameTitle" 
               value="normalize-space(mri:name/*/cit:title)"/>
      <sch:let name="nameRef" 
               value="mri:name/@uuidref"/>
      <sch:let name="mdRefTitle" 
               value="normalize-space(mri:metadataReference/*/cit:title)"/>
      <sch:let name="mdRefRef" 
               value="mri:metadataReference/@uuidref"/>
      
      <sch:let name="hasName" value="$nameTitle != '' or $nameRef != ''"/>
      <sch:let name="hasMdRef" value="$mdRefTitle != '' or $mdRefRef != ''"/>
      
      <!-- Concat ref assuming there is not both name and metadataReference -->
      <sch:let name="resourceRef" 
               value="concat($nameTitle, $nameRef, 
                             $mdRefRef, $mdRefTitle)"/>
    
      <sch:assert test="$hasName or $hasMdRef"
         diagnostics="rule.mri.associatedresource-failure-en 
                      rule.mri.associatedresource-failure-fr"/>
      
      <sch:report test="$hasName or $hasMdRef"
        diagnostics="rule.mri.associatedresource-success-en 
                     rule.mri.associatedresource-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!--    
    Rule: MD_DataIdentification
    Ref: {defaultLocale documented if resource includes textual information}
    -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mri.defaultlocalewhenhastext-failure-en"
      xml:lang="en">Resource language MUST be defined when the resource
      includes textual information.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.defaultlocalewhenhastext-failure-fr"
      xml:lang="fr">La langue de la resource DOIT être renseignée
      lorsque la ressource contient des informations textuelles.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mri.defaultlocalewhenhastext-success-en"
      xml:lang="en">Number of resource language: 
      <sch:value-of select="count($resourceLanguages)"/>.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.defaultlocalewhenhastext-success-fr"
      xml:lang="fr">Nombre de langues de la ressource :
      <sch:value-of select="count($resourceLanguages)"/>.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mri.defaultlocalewhenhastext">
    <sch:title xml:lang="en">Resource language</sch:title>
    <sch:title xml:lang="fr">Langue de la ressource</sch:title>
    
    <!-- 
    QUESTION-TODO: "includes textual information" may not be easy to define.
    Imagery will not. Could we consider that this rule applies to 
    a resource having a feature catalog ? For services ?
    
    Here the context define that the rule applies to DataIdentification
    having FeatureCatalog siblings.
    -->
    <sch:rule context="//mri:MD_DataIdentification[
      ../../mdb:contentInfo/mrc:MD_FeatureCatalogue or
      ../../mdb:contentInfo/mrc:MD_FeatureCatalogueDescription]">
      
      <sch:let name="resourceLanguages" 
        value="mri:defaultLocale/lan:PT_Locale/
                lan:language/lan:LanguageCode/@codeListValue[. != '']"/>
      <sch:let name="hasAtLeastOneLanguage" 
        value="count($resourceLanguages) > 0"/>
      
      <sch:assert test="$hasAtLeastOneLanguage"
        diagnostics="rule.mri.defaultlocalewhenhastext-failure-en 
        rule.mri.defaultlocalewhenhastext-failure-fr"/>
      
      <sch:report test="$hasAtLeastOneLanguage"
        diagnostics="rule.mri.defaultlocalewhenhastext-success-en 
        rule.mri.defaultlocalewhenhastext-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  <!--
    Ref: {defaultLocale.PT_Locale.characterEncoding default value is UTF-8}
    
    See Implemented in rule.mdb.defaultlocale.
    
    TODO: A better implementation would have been to make an Abstract
    test and use it in both places to not mix mdb and mri testsuites.
    -->
  
  
  
  <!--
    Rule: MD_Keywords
    Ref: {When the resource described is a service, 
    one instance of MD_Keyword shall refer to the service taxonomy 
    defined in ISO19119}
    
    QUESTION-TODO: This rules defined should move to srv.sch ?
  -->
  <sch:diagnostics>
    <sch:diagnostic id="rule.mri.servicetaxonomy-failure-en"
      xml:lang="en">A service metadata SHALL refer to the service
      taxonomy defined in ISO19119 defining one or more value in the
      keyword section.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.servicetaxonomy-failure-fr"
      xml:lang="fr">Une métadonnée de service DEVRAIT référencer
      un type de service tel que défini dans l'ISO19119 dans la
      section mot clé.</sch:diagnostic>
    
    <sch:diagnostic id="rule.mri.servicetaxonomy-success-en"
      xml:lang="en">Number of service taxonomy specified: 
      <sch:value-of select="count($serviceTaxonomies)"/>.</sch:diagnostic>
    <sch:diagnostic id="rule.mri.servicetaxonomy-success-fr"
      xml:lang="fr">Nombre de types de service :
      <sch:value-of select="count($serviceTaxonomies)"/>.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.srv.servicetaxonomy">
    <sch:title xml:lang="en">Service taxonomy</sch:title>
    <sch:title xml:lang="fr">Taxonomie des services</sch:title>
    
    <!-- 
    QUESTION-TODO: Is this the list to check against ?
      The list is not multilingual ?
    -->
    <sch:rule context="//srv:SV_ServiceIdentification">
      <sch:let name="listOfTaxonomy" 
               value="'Geographic human interaction services, 
                       Geographic model/information management services, 
                       Geographic workflow/task management services, 
                       Geographic processing services, 
                       Geographic processing services — spatial,
                       Geographic processing services — thematic,
                       Geographic processing services — temporal, 
                       Geographic processing services — metadata, 
                       Geographic communication services'"/>
      <sch:let name="serviceTaxonomies" 
        value="mri:descriptiveKeywords/mri:MD_Keywords/mri:keyword[
        contains($listOfTaxonomy, */text())]"/>
      <sch:let name="hasAtLeastOneTaxonomy" 
        value="count($serviceTaxonomies) > 0"/>
      
      <!-- SHALL <sch:assert test="$hasAtLeastOneTaxonomy"
        diagnostics="rule.mri.servicetaxonomy-failure-en 
                     rule.mri.servicetaxonomy-failure-fr"/> -->
      
      <sch:report test="$hasAtLeastOneTaxonomy"
        diagnostics="rule.mri.servicetaxonomy-success-en 
                     rule.mri.servicetaxonomy-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
</sch:schema>
