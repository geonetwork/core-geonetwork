<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/1.0"/>
  <sch:ns prefix="mri" uri="http://standards.iso.org/iso/19115/-3/mri/1.0"/>
  <sch:ns prefix="mdb" uri="http://standards.iso.org/iso/19115/-3/mdb/1.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="lan" uri="http://standards.iso.org/iso/19115/-3/lan/1.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>
  <!--
    ISO 19115-3 base requirements for metadata instance documents
    
    See ISO19115-1:2014(E) page 10, Figure 5
  -->
  
  <!-- 
    Rule: Check root element. 
    Ref: N/A
  -->
  <sch:diagnostics>
    <sch:diagnostic 
      id="rule.mdb.root-element-failure-en"
      xml:lang="en">The root element must be MD_Metadata.</sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.root-element-failure-fr"
      xml:lang="fr">Modifier l'élément racine du document pour que ce 
      soit un élément MD_Metadata.</sch:diagnostic>

    <sch:diagnostic 
      id="rule.mdb.root-element-success-en"
      xml:lang="en">Root element MD_Metadata found.</sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.root-element-success-fr"
      xml:lang="fr">Élément racine MD_Metadata défini.</sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mdb.root-element">
    <sch:title xml:lang="en">Metadata document root element</sch:title>
    <sch:title xml:lang="fr">Élément racine du document</sch:title>
    
    <sch:p xml:lang="en">A metadata instance document conforming to 
      this specification SHALL have a root MD_Metadata element 
      defined in the http://standards.iso.org/iso/19115/-3/mdb/1.0 namespace.</sch:p>
    <sch:p xml:lang="fr">Une fiche de métadonnées conforme au standard
      ISO19115-1 DOIT avoir un élément racine MD_Metadata (défini dans l'espace
      de nommage http://standards.iso.org/iso/19115/-3/mdb/1.0).</sch:p>
    <sch:rule context="/">
      <sch:let name="hasOneMD_MetadataElement" 
               value="count(/mdb:MD_Metadata) = 1"/>
      
      <sch:assert test="$hasOneMD_MetadataElement"
      diagnostics="rule.mdb.root-element-failure-en 
                   rule.mdb.root-element-failure-fr"/>
      
      <sch:report test="$hasOneMD_MetadataElement"
        diagnostics="rule.mdb.root-element-success-en 
                     rule.mdb.root-element-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  
  
  <!-- 
    Rule:  
    Ref: {defaultLocale documented if not defined by the encoding}
    This can't be validated because the encoding is part of the default locale ? TODO-QUESTION
    
    Ref: {defaultLocale.PT_Locale.characterEncoding default value is UTF-8}
    Check that encoding is not empty.
  -->
  
  <sch:diagnostics>
    <sch:diagnostic 
      id="rule.mdb.defaultlocale-failure-en" 
      xml:lang="en">The default locale character encoding is "UTF-8". Current value is
      "<sch:value-of select="$encoding"/>".</sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.defaultlocale-failure-fr" 
      xml:lang="fr">L'encodage ne doit pas être vide. La valeur par défaut est 
      "UTF-8". La valeur actuelle est "<sch:value-of select="$encoding"/>".</sch:diagnostic>
    
    
    <sch:diagnostic 
      id="rule.mdb.defaultlocale-success-en" 
      xml:lang="en">The characeter encoding is "<sch:value-of select="$encoding"/>.
    </sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.defaultlocale-success-fr" 
      xml:lang="fr">L'encodage est "<sch:value-of select="$encoding"/>.
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mdb.defaultlocale">
    <sch:title xml:lang="en">Default locale</sch:title>
    <sch:title xml:lang="fr">Langue du document</sch:title>
    
    <sch:p xml:lang="en">The default locale MUST be documented if
      not defined by the encoding. The default value for the character
      encoding is "UTF-8".</sch:p>
    <sch:p xml:lang="fr">La langue doit être documentée
      si non définie par l'encodage. L'encodage par défaut doit être "UTF-8".</sch:p>
    
    <sch:rule context="/mdb:MD_Metadata/mdb:defaultLocale|
                       /mdb:MD_Metadata/mdb:identificationInfo/*/mri:defaultLocale">
      
      <sch:let name="encoding" 
        value="string(lan:PT_Locale/lan:characterEncoding/
                  lan:MD_CharacterSetCode/@codeListValue)"/>
      
      <sch:let name="hasEncoding" 
        value="normalize-space($encoding) != ''"/>
      
      
      <sch:assert test="$hasEncoding"
        diagnostics="rule.mdb.defaultlocale-failure-en
                     rule.mdb.defaultlocale-failure-fr"/>
      
      <sch:report test="$hasEncoding"
        diagnostics="rule.mdb.defaultlocale-success-en
                     rule.mdb.defaultlocale-success-fr"/>
    </sch:rule>
  </sch:pattern>
 
 
  <!-- 
    Rule: 
    Ref: {count(MD_Metadata.parentMetadata) > 0 when there is an higher 
    level object}
    Comment: Can't be validated using schematron AFA the existence
    of an higher level object can't be checked. TODO-QUESTION
  -->
  
  
  <!--
    Rule:  
    Ref: {count(MD_Metadata.metadataScope) > 0 if 
    MD_Metadata.metadataScope.MD_MetadataScope.resourceScope
    not equal to "dataset"}
    
    Ref: {name is mandatory if resourceScope not equal to "dataset"}
  -->
  <sch:diagnostics>
    <sch:diagnostic 
      id="rule.mdb.scope-name-failure-en" 
      xml:lang="en">Specify a name for the metadata scope 
      (required if the scope code is not "dataset", in that case
      "<sch:value-of select="$scopeCode"/>").</sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.scope-name-failure-fr" 
      xml:lang="fr">Préciser la description du domaine d'application 
      (car le document décrit une ressource qui n'est pas un "jeu de données",
      la ressource est de type "<sch:value-of select="$scopeCode"/>").</sch:diagnostic>
    
    
    <sch:diagnostic 
      id="rule.mdb.scope-name-success-en" 
      xml:lang="en">Scope name 
      "<sch:value-of select="$scopeCodeName"/><sch:value-of select="$nilReason"/>"
      is defined for resource with type "<sch:value-of select="$scopeCode"/>".
    </sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.scope-name-success-fr" 
      xml:lang="fr">La description du domaine d'application 
      "<sch:value-of select="$scopeCodeName"/><sch:value-of select="$nilReason"/>"
      est renseignée pour la ressource de type "<sch:value-of select="$scopeCode"/>".
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mdb.scope-name">
    <sch:title xml:lang="en">Metadata scope Name</sch:title>
    <sch:title xml:lang="fr">Description du domaine d'application</sch:title>
    
    <sch:p xml:lang="en">If a MD_MetadataScope element is present, 
      the name property MUST have a value if resourceScope is not equal to "dataset"</sch:p>
    <sch:p xml:lang="fr">Si un élément domaine d'application (MD_MetadataScope)
      est défini, sa description (name) DOIT avoir une valeur
      si ce domaine n'est pas "jeu de données" (ie. "dataset").</sch:p>
    
    <sch:rule context="/mdb:MD_Metadata/mdb:metadataScope/
                          mdb:MD_MetadataScope[not(mdb:resourceScope/
                            mcc:MD_ScopeCode/@codeListValue = 'dataset')]">
      
      <sch:let name="scopeCode" 
        value="mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue"/>
      
      <sch:let name="scopeCodeName" 
        value="normalize-space(mdb:name)"/>
      <sch:let name="hasScopeCodeName" 
        value="normalize-space($scopeCodeName) != ''"/>
      
      <sch:let name="nilReason" 
        value="mdb:name/@gco:nilReason"/>
      <sch:let name="hasNilReason" 
        value="$nilReason != ''"/>
      
      <sch:assert test="$hasScopeCodeName or $hasNilReason"
        diagnostics="rule.mdb.scope-name-failure-en
                     rule.mdb.scope-name-failure-fr"/>
      
      <sch:report test="$hasScopeCodeName or $hasNilReason"
        diagnostics="rule.mdb.scope-name-success-en
                     rule.mdb.scope-name-success-fr"/>
    </sch:rule>
  </sch:pattern>
  
  
  <!-- 
    Rule: At least one creation date
    Ref: {count(MD _Metadata.dateInfo.CI_Date.dateType.CI_DateTypeCode= "creation") > 0}
  -->
  <sch:diagnostics>
    <sch:diagnostic 
      id="rule.mdb.create-date-failure-en"
      xml:lang="en">Specify a creation date for the metadata record 
      in the metadata section.</sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.create-date-failure-fr"
      xml:lang="fr">Définir une date de création pour le document
      dans la section sur les métadonnées.</sch:diagnostic>
    
    <sch:diagnostic 
      id="rule.mdb.create-date-success-en" 
      xml:lang="en">
      Metadata creation date: <sch:value-of select="$creationDates"/>.
    </sch:diagnostic>
    <sch:diagnostic 
      id="rule.mdb.create-date-success-fr" 
      xml:lang="fr">
      Date de création du document : <sch:value-of select="$creationDates"/>.
    </sch:diagnostic>
  </sch:diagnostics>
  
  <sch:pattern id="rule.mdb.create-date">
    <sch:title xml:lang="en">Metadata create date</sch:title>
    <sch:title xml:lang="fr">Date de création du document</sch:title>
    
    <sch:p xml:lang="en">A dateInfo property value with data type = "creation" 
      MUST be present in every MD_Metadata instance.</sch:p>
    <sch:p xml:lang="fr">Tout document DOIT avoir une date de création 
      définie (en utilisant un élément dateInfo avec un type de date "creation").</sch:p>
    
    <sch:rule context="mdb:MD_Metadata">
      <sch:let name="creationDates"
        value="./mdb:dateInfo/cit:CI_Date[
                    normalize-space(cit:date/gco:DateTime) != '' and 
                    cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'creation']/
                  cit:date/gco:DateTime"/>
      
      <!-- Check at least one non empty creation date element is defined. -->
      <sch:let name="hasAtLeastOneCreationDate"
        value="count(./mdb:dateInfo/cit:CI_Date[
                    normalize-space(cit:date/gco:DateTime) != '' and 
                    cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'creation']
                    ) &gt; 0"/>
      
      <sch:assert test="$hasAtLeastOneCreationDate"
        diagnostics="rule.mdb.create-date-failure-en
                     rule.mdb.create-date-failure-fr"/>
      <sch:report test="$hasAtLeastOneCreationDate"
        diagnostics="rule.mdb.create-date-success-en
                     rule.mdb.create-date-success-fr"/>
    </sch:rule>
  </sch:pattern>
</sch:schema>
