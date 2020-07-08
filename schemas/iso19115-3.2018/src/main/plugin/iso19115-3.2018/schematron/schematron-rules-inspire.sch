<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	queryBinding="xslt2">

	<sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE rules</sch:title>
	<sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
	<sch:ns prefix="gmd" uri="http://standards.iso.org/iso/19115/-3/gmd"/>
    <sch:ns prefix="gmx" uri="http://standards.iso.org/iso/19115/-3/gmx"/>
	<sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
	<sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
	<sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>
    <sch:ns prefix="srv" uri="http://standards.iso.org/iso/19115/-3/srv/2.0"/>
    <sch:ns prefix="mdb" uri="http://standards.iso.org/iso/19115/-3/mdb/2.0"/>
    <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
    <sch:ns prefix="mri" uri="http://standards.iso.org/iso/19115/-3/mri/1.0"/>
    <sch:ns prefix="mrs" uri="http://standards.iso.org/iso/19115/-3/mrs/1.0"/>
    <sch:ns prefix="mrd" uri="http://standards.iso.org/iso/19115/-3/mrd/1.0"/>
    <sch:ns prefix="mco" uri="http://standards.iso.org/iso/19115/-3/mco/1.0"/>
    <sch:ns prefix="msr" uri="http://standards.iso.org/iso/19115/-3/msr/2.0"/>
    <sch:ns prefix="lan" uri="http://standards.iso.org/iso/19115/-3/lan/1.0"/>
    <sch:ns prefix="gcx" uri="http://standards.iso.org/iso/19115/-3/gcx/1.0"/>
    <sch:ns prefix="gex" uri="http://standards.iso.org/iso/19115/-3/gex/1.0"/>
    <sch:ns prefix="dqm" uri="http://standards.iso.org/iso/19157/-2/dqm/1.0"/>
    <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/2.0"/>
    <sch:ns prefix="mdq" uri="http://standards.iso.org/iso/19157/-2/mdq/1.0"/>
    <sch:ns prefix="mrl" uri="http://standards.iso.org/iso/19115/-3/mrl/2.0"/>
    <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>

	<!-- INSPIRE metadata rules / START -->

	<!-- Resource Identification -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.identification-cit-title-failure-en" xml:lang="en">Resource title is missing.</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-title-failure-fr" xml:lang="fr">L'intitulé de la ressource est manquant.</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-title-success-en" xml:lang="en">Resource title found:"<sch:value-of select="$resourceTitle"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-title-success-fr" xml:lang="fr">L'intitulé de la ressource est :"<sch:value-of select="$resourceTitle"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mri-abstract-failure-en" xml:lang="en">Resource abstract is missing.</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mri-abstract-failure-fr" xml:lang="fr">Le résumé de la ressource est manquant.</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mri-abstract-success-en" xml:lang="en">Resource abstract is :"<sch:value-of select="$resourceAbstract"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mri-abstract-success-fr" xml:lang="fr">Le résumé de la ressource est :"<sch:value-of select="$resourceAbstract"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-CI_OnlineResource-failure-en" xml:lang="en">Resource locator is missing (INSPIRE - Resource locator is mandatory if linkage is available). Implementing instructions: >Specify a valid URL to the resource. If no direct link to a resource is available, provide link to a contact point where more information about the resource is available. For a service, the Resource Locator might be one of the following: a link to the service capabilities document; a link to the service WSDL document (SOAP Binding); a link to a web page with further instructions; a link to a client application that directly accesses the service
		</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-CI_OnlineResource-failure-fr" xml:lang="fr">Le localisateur de la ressource est manquant</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-CI_OnlineResource-success-en" xml:lang="en">Resource locator found:"<sch:value-of select="$resourceLocator"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-cit-CI_OnlineResource-success-fr" xml:lang="fr">Le localisateur de la ressource est :"<sch:value-of select="$resourceLocator"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mcc-MD_ScopeCode-failure-en" xml:lang="en">Resource type is missing or has a wrong value. Implementing instructions: The values of MD_ScopeCode in the scope of the directive (See SC4 in 1.2) are: dataset for spatial datasets;series for spatial dataset series;services for spatial data services. The hierarchyLevel property is not mandated by ISO 19115, but is mandated for conformance to the INSPIRE Metadata Implementing rules (See SC2 in 1.2). 
		</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mcc-MD_ScopeCode-failure-fr" xml:lang="fr">Le type de la ressource est manquant ou sa valeur est incorrecte (valeurs autorisées : 'Série de données', 'Ensemble de séries de données' ou 'Service').</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mcc-MD_ScopeCode-success-en" xml:lang="en">Resource type is: "<sch:value-of select="$resourceType"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.identification-mcc-MD_ScopeCode-success-fr" xml:lang="fr">Le type de la ressource est :"<sch:value-of select="$resourceType"/>"</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.identification">
		<sch:title xml:lang="en">Resource identification</sch:title>
      	<sch:title xml:lang="fr">Identification de la ressource</sch:title>
		
		<!-- Title -->
		<sch:rule context="//mdb:identificationInfo/*/mri:citation/cit:CI_Citation">
			<sch:let name="resourceTitle" value="cit:title/*/text()"/>
			<sch:let name="noResourceTitle" value="not(cit:title) or cit:title/@gco:nilReason='missing'"/>
			<sch:assert test="not($noResourceTitle)" diagnostics="rule.identification-cit-title-failure-en
			rule.identification-cit-title-failure-fr"/>
			<sch:report test="not($noResourceTitle)" diagnostics="rule.identification-cit-title-success-en
			rule.identification-cit-title-success-fr"/>
		</sch:rule>

		<!-- Abstract -->
		<sch:rule context="//mri:MD_DataIdentification|
			//*[@gco:isoType='mri:MD_DataIdentification']|
			//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:let name="resourceAbstract" value="mri:abstract/*/text()"/>
			<sch:let name="noResourceAbstract" value="not(mri:abstract) or mri:abstract/@gco:nilReason='missing'"/>
			<sch:assert test="not($noResourceAbstract)" diagnostics="rule.identification-mri-abstract-failure-en
			rule.identification-mri-abstract-failure-fr"/>
			<sch:report test="not($noResourceAbstract)" diagnostics="rule.identification-mri-abstract-success-en
			rule.identification-mri-abstract-success-fr"/>
		</sch:rule>
		
		<!-- Online resource 
			Conditional for spatial dataset and spatial dataset
			series: Mandatory if a URL is available to obtain
			IR more information on the resources and/or access Obligation / condition related services.
			• Conditional for services: Mandatory if linkage to the
			service is available
		-->
		<sch:rule context="//mdb:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/cit:CI_OnlineResource">
			<sch:let name="resourceLocator" value="cit:linkage/*/text()"/>
			<sch:let name="noResourceLocator" value="normalize-space(cit:linkage/gco:CharacterString)='' 
					or not(cit:linkage)"/>
			
			<sch:assert test="not($noResourceLocator)" diagnostics="rule.identification-cit-CI_OnlineResource-failure-en
			rule.identification-cit-CI_OnlineResource-failure-fr"/>
			<sch:report test="not($noResourceLocator)" diagnostics="rule.identification-cit-CI_OnlineResource-success-en
			rule.identification-cit-CI_OnlineResource-success-fr"/>
		</sch:rule>

		<!-- Resource type -->
		<sch:rule context="//mdb:MD_Metadata">
			<sch:let name="resourceType_present" value="mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='dataset'
				or mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='series'
				or mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='service'"/>
			<sch:let name="resourceType" value="string-join(mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue, ',')"/>
			
			<sch:assert test="$resourceType_present" diagnostics="rule.identification-mcc-MD_ScopeCode-failure-en
			rule.identification-mcc-MD_ScopeCode-failure-fr"/>
			<sch:report test="$resourceType_present" diagnostics="rule.identification-mcc-MD_ScopeCode-success-en
			rule.identification-mcc-MD_ScopeCode-success-fr"/>
		</sch:rule>
		
	</sch:pattern>	
	
	<!-- Data Identification -->
	<!-- Dataset and series only -->
	<sch:diagnostics >
		<sch:diagnostic id="rule.dataIdentification-lan-language-failure-en" xml:lang="en">INSPIRE (datasets and series) - Resource language is mandatory if the resource includes textual information. An instance of the language property is mandated by ISO19115 ; it can be defaulted to the value of the Metadata Implementing instructions Language when the dataset or the dataset series does not contain textual information. Implementing instructions: Codelist (See ISO/TS 19139) based on alpha-3 codes of ISO 639-2.
		</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-lan-language-failure-fr" xml:lang="fr">La langue de la ressource est manquante ou a une valeur incorrecte</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-lan-language-success-en" xml:lang="en">Resource language is:"<sch:value-of select="$resourceLanguage"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-lan-language-success-fr" xml:lang="fr">La langue de la ressource est :"<sch:value-of select="$resourceLanguage"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-mri-topicCategory-failure-en" xml:lang="en">ISO topic category is missing (INSPIRE - ISO topic category is mandatory). The topic categories defined in Part D 2 of the INSPIRE Implementing rules for metadata are derived directly from the topic categories defined in B.5.27 of ISO 19115. INSPIRE Implementing rules for metadata define the INSPIRE data themes to which each topic category is Implementing instructions applicable, i.e., Administrative units (I.4) and Statistical units (III.1) are INSPIRE themes for which the boundaries topic category is applicable. The value of the ISO 19115/ISO 19119 metadata element is the value appearing in the “name” column of the table in B.5.27 of ISO 19115.
		</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-mri-topicCategory-failure-fr" xml:lang="fr">Une catégorie thématique est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-mri-topicCategory-success-en" xml:lang="en">ISO topic category is:"<sch:value-of select="$topic"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-mri-topicCategory-success-fr" xml:lang="fr">La catégorie thématique est :"<sch:value-of select="$topic"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-cit-identifier-failure-en" xml:lang="en">Unique resource identifier is missing. Mandatory for dataset and dataset series. Example: 527c4cac-070c-4bca-9aaf-92bece7be902
		</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-cit-identifier-failure-fr" xml:lang="fr">L'identificateur de ressource unique est manquant.</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-cit-identifier-success-en" xml:lang="en">Unique resource identifier is:"<sch:value-of select="$resourceIdentifier_code"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-cit-identifier-success-fr" xml:lang="fr">L'identificateur de ressource unique est :"<sch:value-of select="$resourceIdentifier_code"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-cit-identifier-codespace-success-en" xml:lang="en">Unique resource identifier codespace is:"<sch:value-of select="$resourceIdentifier_codeSpace"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.dataIdentification-cit-identifier-codespace-success-fr" xml:lang="fr">L'espace de nom de l'identificateur de ressource unique est :"<sch:value-of select="$resourceIdentifier_codeSpace"/>"</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.dataIdentification">
		<sch:title xml:lang="en">Data Identification</sch:title>
      	<sch:title xml:lang="fr">Identification des données</sch:title>
			
		<sch:rule context="//mri:MD_DataIdentification[
			../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = '']|
			//*[@gco:isoType='mri:MD_DataIdentification' and (
			../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'
			or ../../mdb:metadataScope/mdb:MD_Metadatacope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = '')]">
			<!-- resource language is only conditional for 'dataset' and 'series'. 
			-->			
			<sch:let name="resourceLanguage" value="string-join(mri:defaultLocale/*/lan:language/gco:CharacterString|mri:defaultLocale/*/lan:language/lan:LanguageCode/@codeListValue, ', ')"/>
			<sch:let name="euLanguage" value="not(mri:defaultLocale/*/lan:language/@gco:nilReason='missing') and contains(string-join(('eng', 'fre', 'ger', 'spa', 'dut', 'ita', 'cze', 'lav', 'dan', 'lit', 'mlt', 'pol', 'est', 'por', 'fin', 'rum', 'slo', 'slv', 'gre', 'bul', 'hun', 'swe', 'gle'), ', '), $resourceLanguage)"/>

			<sch:assert test="$euLanguage" diagnostics="rule.dataIdentification-lan-language-failure-en
			rule.dataIdentification-lan-language-failure-fr"/>
			<sch:report test="$euLanguage" diagnostics="rule.dataIdentification-lan-language-success-en
			rule.dataIdentification-lan-language-success-fr"/>
			
			<!-- Topic category -->
			<sch:let name="topic" value="mri:topicCategory/mri:MD_TopicCategoryCode"/>
			<sch:let name="noTopic" value="not(mri:topicCategory)  or normalize-space(mri:topicCategory/mri:MD_TopicCategoryCode/text()) = ''"/>
			<sch:assert test="not($noTopic)" diagnostics="rule.dataIdentification-mri-topicCategory-failure-en
			rule.dataIdentification-mri-topicCategory-failure-fr"/>
			<sch:report test="not($noTopic)" diagnostics="rule.dataIdentification-mri-topicCategory-success-en
			rule.dataIdentification-mri-topicCategory-success-fr"/>
			
			<!-- Unique identifier -->
			<sch:let name="resourceIdentifier" value="mri:citation/cit:CI_Citation/cit:identifier 
				and not(mri:citation/cit:CI_Citation/cit:identifier[*/mcc:code/@gco:nilReason='missing'])"/>
			<sch:let name="resourceIdentifier_code" value="mri:citation/cit:CI_Citation/cit:identifier/*/mcc:code/*/text()"/>
			<sch:let name="resourceIdentifier_codeSpace" value="mri:citation/cit:CI_Citation/cit:identifier/*/mcc:codeSpace/*/text()"/>
			
			<sch:assert test="$resourceIdentifier" diagnostics="rule.dataIdentification-cit-identifier-failure-en
			rule.dataIdentification-cit-identifier-failure-fr"/>
			<sch:report test="$resourceIdentifier_code" diagnostics="rule.dataIdentification-cit-identifier-success-en
			rule.dataIdentification-cit-identifier-success-fr"/>
			<sch:report test="$resourceIdentifier_codeSpace" diagnostics="rule.dataIdentification-cit-identifier-codespace-success-en
			rule.dataIdentification-cit-identifier-codespace-success-fr"/>
		</sch:rule>
	</sch:pattern>
		
	<!-- Service Identification -->
	<!-- Service only -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.serviceIdentification-srv-operatesOn-failure-en" xml:lang="en">Coupled resource is missing.
			<div><h2>INSPIRE (service) - Coupled resource is mandatory if linkage to data sets on which the service operates are available</h2>
		        <br/>
		        <b>Implementing instructions</b><br/>
		        Not applicable to dataset and dataset series<br/>
		        Conditional to services: Mandatory if linkage to datasets on which the service operates are available.<br/>
		        
		        <ul>
		            <li>The property shall be implemented by reference (See
		        SC11 in 1.2) and the MD_DataIdentification object
		        reference value is the code of the Coupled resource
		        metadata element.</li>
		            <li>For consistency, the code of the Couple resource
		        metadata element should also be the code of one of
		        the Unique resource identifiers of the corresponding
		        coupled resource.</li>
		        </ul>
		    </div>
		</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-operatesOn-failure-fr" xml:lang="fr">La ressource couplée est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-operatesOn-coupledResourceHref-success-en" xml:lang="en">Coupled resources found : "<sch:value-of select="$coupledResourceHref"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-operatesOn-coupledResourceHref-success-fr" xml:lang="fr">Les ressources couplées sont : "<sch:value-of select="$coupledResourceHref"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-operatesOn-coupledResourceUUID-success-en" xml:lang="en">ReCoupled resources found : "<sch:value-of select="$coupledResourceUUID"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-operatesOn-coupledResourceUUID-success-fr" xml:lang="fr">Les ressources couplées sont : "<sch:value-of select="$coupledResourceUUID"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-serviceType-failure-en" xml:lang="en">ServiceType is missing. Mandatory for services. Not applicable to dataset and dataset series. Example: 'view', 'discovery', 'download', 'transformation', 'invoke', 'other'.
		</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-serviceType-failure-fr" xml:lang="fr">Le type de service est manquant ou sa valeur est incorrecte (valeurs autorisées : 'view', 'discovery', 'download', 'transformation', 'invoke', 'other').</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-serviceType-success-en" xml:lang="en">Service type is : "<sch:value-of select="$serviceType"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.serviceIdentification-srv-serviceType-success-fr" xml:lang="fr">Le type de service est :"<sch:value-of select="$serviceType"/>"</sch:diagnostic>
	</sch:diagnostics>

	<sch:pattern id="rule.serviceIdentification">
		<sch:title xml:lang="en">Service Identification</sch:title>
      	<sch:title xml:lang="fr">Identification des services</sch:title>
		
		<!-- No operatesOn for services -->
		<sch:rule context="//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">
		  <sch:let name="coupledResourceHref" value="string-join(srv:operatesOn/@xlink:href, ', ')"/>
		  <sch:let name="coupledResourceUUID" value="string-join(srv:operatesOn/@uuidref, ', ')"/>
			<sch:let name="coupledResource" value="../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='service'
				and //srv:operatesOn"/>
			
			<!--
			  "Conditional to services: Mandatory if linkage to
			  datasets on which the service operates are available."
			  TODO : maybe check if service couplingType=tight or serviceType=view ?
			<sch:assert test="$coupledResource">
				<sch:value-of select="$loc/strings/alert.M51/div"/>
			</sch:assert>-->
			<sch:report test="$coupledResource and $coupledResourceHref!=''" diagnostics="rule.serviceIdentification-srv-operatesOn-coupledResourceHref-success-en
			rule.serviceIdentification-srv-operatesOn-coupledResourceHref-success-fr"/>
			<sch:report test="$coupledResource and $coupledResourceUUID!=''" diagnostics="rule.serviceIdentification-srv-operatesOn-coupledResourceUUID-success-en
			rule.serviceIdentification-srv-operatesOn-coupledResourceUUID-success-fr"/>
			
			<sch:let name="serviceType" value="srv:serviceType/gco:LocalName"/>
			<sch:let name="noServiceType" value="contains(string-join(('view', 'discovery', 'download', 'transformation', 'invoke', 'other'), ','), srv:serviceType/gco:LocalName)"/>
			<sch:assert test="$noServiceType" diagnostics="rule.serviceIdentification-srv-serviceType-failure-en
			rule.serviceIdentification-srv-serviceType-failure-fr"/>
			<sch:report test="$noServiceType" diagnostics="rule.serviceIdentification-srv-serviceType-success-en
			rule.serviceIdentification-srv-serviceType-success-fr"/>
		</sch:rule>
	</sch:pattern>
	
	
	<!--  Keyword and INSPIRE themes -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.keyword-thesaurus-file-failure-en" xml:lang="en">INSPIRE Theme thesaurus not found. Download thesaurus from the INSPIRE Registry.</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-thesaurus-file-failure-fr" xml:lang="fr">Le thesaurus du thème ISNPIRE n'a pas été trouvé. Télécharger le Registre INSPIRE.</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-inspire-theme-failure-en" xml:lang="en">INSPIRE theme is mandatory.</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-inspire-theme-failure-fr" xml:lang="fr">Le thème INSPIRE est manquant (mot clé issu du thésaurus "GEMET INSPIRE themes")</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-inspire-theme-success-en" xml:lang="en">"<sch:value-of select="$inspire-theme-found"/>" INSPIRE theme(s) found.</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-inspire-theme-success-fr" xml:lang="fr">"<sch:value-of select="$inspire-theme-found"/>" Thème INSPIRE trouvé.</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-thesaurus-name-success-en" xml:lang="en">Thesaurus: "<sch:value-of select="$thesaurus_name"/>, <sch:value-of select="$thesaurus_date"/> (<sch:value-of select="$thesaurus_dateType"/>)"</sch:diagnostic>
		<sch:diagnostic id="rule.keyword-thesaurus-name-success-fr" xml:lang="fr">Thesaurus: "<sch:value-of select="$thesaurus_name"/>, <sch:value-of select="$thesaurus_date"/> (<sch:value-of select="$thesaurus_dateType"/>)"</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.keyword">
		<sch:title xml:lang="en">Keyword and INSPIRE themes</sch:title>
      	<sch:title xml:lang="fr">Mots clés et thèmes INSPIRE</sch:title>
	
		<sch:rule context="//mri:MD_DataIdentification|
			//*[@gco:isoType='mri:MD_DataIdentification']">
			<!-- Check that INSPIRE theme are available.
				Use INSPIRE thesaurus available on SVN to check keywords in all EU languages.
			-->
			<!-- TEST only			
			<sch:let name="inspire-thesaurus" value="document('file:///..../core-geonetwork/web/src/main/webapp/WEB-INF/data/config/codelist/external/thesauri/theme/inspire-theme.rdf')"/>
			-->

			<sch:let name="inspire-thesaurus" value="document(concat($thesaurusDir, '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))"/>
			<sch:let name="inspire-theme" value="$inspire-thesaurus//skos:Concept"/>
			
			<!-- Display error if INSPIRE Theme thesaurus is not available. -->
			<sch:assert test="count($inspire-theme) > 0" diagnostics="rule.keyword-thesaurus-file-failure-en
			rule.keyword-thesaurus-file-failure-fr"/>
			
			<sch:let name="thesaurus_name" value="mri:descriptiveKeywords/*/mri:thesaurusName/*/cit:title/*/text()"/>
			<sch:let name="thesaurus_date" value="mri:descriptiveKeywords/*/mri:thesaurusName/*/cit:date/*/cit:date/*/text()"/>
			<sch:let name="thesaurus_dateType" value="mri:descriptiveKeywords/*/mri:thesaurusName/*/cit:date/*/cit:dateType/*/@codeListValue/text()"/>
			<sch:let name="keyword" 
				value="mri:descriptiveKeywords/*/mri:keyword/gco:CharacterString|
				mri:descriptiveKeywords/*/mri:keyword/gcx:Anchor"/>
			<sch:let name="inspire-theme-found" 
				value="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $keyword])"/>
			<sch:assert test="$inspire-theme-found > 0" diagnostics="rule.keyword-inspire-theme-failure-en
			rule.keyword-inspire-theme-failure-fr"/>
			<sch:report test="$inspire-theme-found > 0" diagnostics="rule.keyword-inspire-theme-success-en
			rule.keyword-inspire-theme-success-fr"/>
			<sch:report test="$thesaurus_name" diagnostics="rule.keyword-thesaurus-name-success-en
			rule.keyword-thesaurus-name-success-fr"/>
			<!-- TODO : We should check GEMET Thesaurus reference and date is set. -->
		</sch:rule>

	</sch:pattern>
	
	<!--  INSPIRE Service taxonomy -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.serviceTaxonomy-taxonomy-file-failure-en" xml:lang="en">INSPIRE service taxonomy thesaurus not found. Check installation in codelist/external/thesauri/theme. Download thesaurus from https://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/gemet/thesauri/.</sch:diagnostic>
		<sch:diagnostic id="rule.serviceTaxonomy-taxonomy-file-failure-fr" xml:lang="fr">Le thesaurus de taxonomie des services ISNPIRE n'a pas été trouvé. Vérifier si ce dernier est bien installer (codelist/external/thesauri/theme). Télécharger le thesaurus à partir de l'url suivant https://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/gemet/thesauri/.</sch:diagnostic>
		<sch:diagnostic id="rule.serviceTaxonomy-inspire-theme-failure-en" xml:lang="en">Missing service taxonomy information (select on or more keyword from "inspire-service-taxonomy.rdf" thesaurus)</sch:diagnostic>
		<sch:diagnostic id="rule.serviceTaxonomy-inspire-theme-failure-fr" xml:lang="fr">Une catégorie de service est manquante (mot clé issu du thesaurus "inspire-service-taxonomy")</sch:diagnostic>
		<sch:diagnostic id="rule.serviceTaxonomy-inspire-theme-success-en" xml:lang="en">A service taxonomy classification defined : "<sch:value-of select="$inspire-theme-found"/>"  </sch:diagnostic>
		<sch:diagnostic id="rule.serviceTaxonomy-inspire-theme-success-fr" xml:lang="fr">Une catégorie de service est définie : "<sch:value-of select="$inspire-theme-found"/>" </sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.serviceTaxonomy">
		<sch:title xml:lang="en">INSPIRE Service taxonomy</sch:title>
      	<sch:title xml:lang="fr">Catégorie de services</sch:title>
			
		<sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<!-- Check that INSPIRE service taxonomy is available.
				Use INSPIRE thesaurus available on SVN to check keywords in all EU languages.
			-->
			<!-- TEST only			
			<sch:let name="inspire-thesaurus" value="document('file:///..../core-geonetwork/web/src/main/webapp/WEB-INF/data/config/codelist/external/thesauri/theme/inspire-theme.rdf')"/>
			-->

			<sch:let name="inspire-thesaurus" value="document(concat($thesaurusDir, '/external/thesauri/theme/inspire-service-taxonomy.rdf'))"/>
			<sch:let name="inspire-st" value="$inspire-thesaurus//skos:Concept"/>
			
			<!-- Display error if INSPIRE thesaurus is not available. -->
			<sch:assert test="count($inspire-st) > 0" diagnostics="rule.serviceTaxonomy-taxonomy-file-failure-en
			rule.serviceTaxonomy-taxonomy-file-failure-fr"/>
			
			
			<sch:let name="keyword" 
				value="mri:descriptiveKeywords/*/gmd:keyword/gco:CharacterString|
                mri:descriptiveKeywords/*/gmd:keyword/gmx:Anchor"/>
			<sch:let name="inspire-theme-found" 
				value="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $keyword])"/>
			<sch:assert test="$inspire-theme-found > 0" diagnostics="rule.serviceTaxonomy-inspire-theme-failure-en
			rule.serviceTaxonomy-inspire-theme-failure-fr"/>
			<sch:report test="$inspire-theme-found > 0" diagnostics="rule.serviceTaxonomy-inspire-theme-success-en
			rule.serviceTaxonomy-inspire-theme-success-fr"/>
		</sch:rule>
		
		
	</sch:pattern>
	
	
	<!--  Geographic location -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-W-failure-en" xml:lang="en">WestBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-W-failure-fr" xml:lang="fr">Borne ouest manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-W-success-en" xml:lang="en">WestBoundLongitude found:"<sch:value-of select="$west"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-W-success-fr" xml:lang="fr">Borne ouest :"<sch:value-of select="$west"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-E-failure-en" xml:lang="en">EastBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-E-failure-fr" xml:lang="fr">Borne est manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-E-success-en" xml:lang="en">EastBoundLongitude found:"<sch:value-of select="$east"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-E-success-fr" xml:lang="fr">Borne est :"<sch:value-of select="$east"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-S-failure-en" xml:lang="en">SouthBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-S-failure-fr" xml:lang="fr">Borne sud manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-S-success-en" xml:lang="en">SouthBoundLongitude found:"<sch:value-of select="$south"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-S-success-fr" xml:lang="fr">Borne sud :"<sch:value-of select="$south"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-N-failure-en" xml:lang="en">NorthBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-N-failure-fr" xml:lang="fr">Borne nord manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-N-success-en" xml:lang="en">NorthBoundLongitude found:"<sch:value-of select="$north"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-mri-MD_DataIdentification-N-success-fr" xml:lang="fr">Borne nord :"<sch:value-of select="$north"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-W-failure-en" xml:lang="en">WestBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-W-failure-fr" xml:lang="fr">Borne ouest manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-W-success-en" xml:lang="en">WestBoundLongitude found:"<sch:value-of select="$west"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-W-success-fr" xml:lang="fr">Borne ouest :"<sch:value-of select="$west"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-E-failure-en" xml:lang="en">EastBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-E-failure-fr" xml:lang="fr">Borne est manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-E-success-en" xml:lang="en">EastBoundLongitude found:"<sch:value-of select="$east"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-E-success-fr" xml:lang="fr">Borne est :"<sch:value-of select="$east"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-S-failure-en" xml:lang="en">SouthBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-S-failure-fr" xml:lang="fr">Borne sud manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-S-success-en" xml:lang="en">SouthBoundLongitude found:"<sch:value-of select="$south"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-S-success-fr" xml:lang="fr">Borne sud :"<sch:value-of select="$south"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-N-failure-en" xml:lang="en">NorthBoundLongitude is missing or has wrong value.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-N-failure-fr" xml:lang="fr">Borne nord manquante ou valeur invalide.</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-N-success-en" xml:lang="en">NorthBoundLongitude found:"<sch:value-of select="$north"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.geographic.location-SV-ServiceIdentification-N-success-fr" xml:lang="fr">Borne nord :"<sch:value-of select="$north"/>"</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.geographic.location">
		<sch:title xml:lang="en">Geographic location</sch:title>
      	<sch:title xml:lang="fr">Situation géographique</sch:title>
		
		<sch:rule context="//mri:MD_DataIdentification[
			../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = '']
			/mri:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox
			|
			//*[@gco:isoType='mri:MD_DataIdentification' and (
			../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'
			or ../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = '')]
			/mri:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox
			">
		
			<sch:let name="west" value="number(gex:westBoundLongitude/gco:Decimal/text())"/>
			<sch:let name="east" value="number(gex:eastBoundLongitude/gco:Decimal/text())"/>
			<sch:let name="north" value="number(gex:northBoundLatitude/gco:Decimal/text())"/>
			<sch:let name="south" value="number(gex:southBoundLatitude/gco:Decimal/text())"/>
			
			<!-- assertions and report -->
			<sch:assert test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-W-failure-en rule.geographic.location-mri-MD_DataIdentification-W-failure-fr"/>
			<sch:report test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-W-success-en rule.geographic.location-mri-MD_DataIdentification-W-success-fr"/>
			<sch:assert test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-E-failure-en rule.geographic.location-mri-MD_DataIdentification-E-failure-fr"/>
			<sch:report test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-E-success-en rule.geographic.location-mri-MD_DataIdentification-E-success-fr"/>
			<sch:assert test="(-90.00 &lt;= $south) and ($south &lt;= $north)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-S-failure-en rule.geographic.location-mri-MD_DataIdentification-S-failure-fr"/>
			<sch:report test="(-90.00 &lt;= $south) and ($south &lt;= $north)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-S-success-en rule.geographic.location-mri-MD_DataIdentification-S-success-fr"/>
			<sch:assert test="($south &lt;= $north) and ($north &lt;= 90.00)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-N-failure-en rule.geographic.location-mri-MD_DataIdentification-N-failure-fr"/>
			<sch:report test="($south &lt;= $north) and ($north &lt;= 90.00)" diagnostics="rule.geographic.location-mri-MD_DataIdentification-N-success-en rule.geographic.location-mri-MD_DataIdentification-N-success-fr"/>
		</sch:rule>
		<sch:rule context="//srv:SV_ServiceIdentification[
			../../mdb:metadataScope/mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue/normalize-space(.) = 'service']
			/mri:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox">
			<sch:let name="west" value="number(gex:westBoundLongitude/gco:Decimal/text())"/>
			<sch:let name="east" value="number(gex:eastBoundLongitude/gco:Decimal/text())"/>
			<sch:let name="north" value="number(gex:northBoundLatitude/gco:Decimal/text())"/>
			<sch:let name="south" value="number(gex:southBoundLatitude/gco:Decimal/text())"/>
			<!-- assertions and report -->
			<sch:assert test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)" diagnostics="rule.geographic.location-SV-ServiceIdentification-W-failure-en rule.geographic.location-SV-ServiceIdentification-W-failure-fr"/>
			<sch:report test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)" diagnostics="rule.geographic.location-SV-ServiceIdentification-W-success-en rule.geographic.location-SV-ServiceIdentification-W-success-fr"/>
			<sch:assert test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)" diagnostics="rule.geographic.location-SV-ServiceIdentification-E-failure-en rule.geographic.location-SV-ServiceIdentification-E-failure-fr"/>
			<sch:report test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)" diagnostics="rule.geographic.location-SV-ServiceIdentification-E-success-en rule.geographic.location-SV-ServiceIdentification-E-success-fr"/>
			<sch:assert test="(-90.00 &lt;= $south) and ($south &lt;= $north)" diagnostics="rule.geographic.location-SV-ServiceIdentification-S-failure-en rule.geographic.location-SV-ServiceIdentification-S-failure-fr"/>
			<sch:report test="(-90.00 &lt;= $south) and ($south &lt;= $north)" diagnostics="rule.geographic.location-SV-ServiceIdentification-S-success-en rule.geographic.location-SV-ServiceIdentification-S-success-fr"/>
			<sch:assert test="($south &lt;= $north) and ($north &lt;= 90.00)" diagnostics="rule.geographic.location-SV-ServiceIdentification-N-failure-en rule.geographic.location-SV-ServiceIdentification-N-failure-fr"/>
			<sch:report test="($south &lt;= $north) and ($north &lt;= 90.00)" diagnostics="rule.geographic.location-SV-ServiceIdentification-N-success-en rule.geographic.location-SV-ServiceIdentification-N-success-fr"/>
		</sch:rule>
	</sch:pattern>		
	
	
	<!--  Temporal reference -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-failure-en" xml:lang="en">Temporal reference is missing (INSPIRE - Temporal reference is mandatory). No instance of Temporal reference has been found. Implementing instructions: Each instance of the temporal extent may be an interval  of dates or an individual date. The overall time period covered by the content of the resource may be composed of one or many instances. Or a reference date of the cited resource (publication, last revision or creation). Example: From 1977-03-10T11:45:30 to 2005-01-15T09:10:00
		</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-failure-fr" xml:lang="fr">Une référence temporelle est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-creation-failure-en" xml:lang="en">There shall not be more than one instance of MD_Metadata.identificationInfo[1].MD_Identification.citation.CI_Citation.date declared as a creation date (i.e. CI_Date.dateType having the creation value).</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-creation-failure-fr" xml:lang="fr">Il ne peut y avoir plus d'une date de création
        (i.e. CI_Date.dateType ayant la valeur creation).</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-creation-success-en" xml:lang="en">Date of creation of the resource found :"<sch:value-of select="$creationDate"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-creation-success-fr" xml:lang="fr">Date de création de la ressource :"<sch:value-of select="$creationDate"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-begin-success-en" xml:lang="en">Temporal extent (begin) found:"<sch:value-of select="$temporalExtentBegin"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-begin-success-fr" xml:lang="fr">Etendue temporelle (début) trouvée :"<sch:value-of select="$temporalExtentBegin"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-end-success-en" xml:lang="en">Temporal extent (end) found : "<sch:value-of select="$temporalExtentEnd"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-end-success-fr" xml:lang="fr">Etendue temporelle (fin) trouvée :"<sch:value-of select="$temporalExtentEnd"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-publication-success-en" xml:lang="en">Date of publication of the resource found :"<sch:value-of select="$publicationDate"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-publication-success-fr" xml:lang="fr">Date de publication de la ressource :"<sch:value-of select="$publicationDate"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-revision-success-en" xml:lang="en">Date of revision of the resource found :"<sch:value-of select="$revisionDate"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.temporal.reference-temporaldate-revision-success-fr" xml:lang="fr">Date de révision de la ressource :"<sch:value-of select="$revisionDate"/>"</sch:diagnostic>
		
	</sch:diagnostics>		
	<sch:pattern id="rule.temporal.reference">
		<sch:title xml:lang="en">Temporal reference</sch:title>
      	<sch:title xml:lang="fr">Référence temporelle</sch:title>

		<sch:rule context="//mri:MD_DataIdentification|
			//*[@gco:isoType='mri:MD_DataIdentification']|
			//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:let name="temporalExtentBegin" 
				value="mri:extent/*/gex:temporalElement/*/gex:extent/*/gml:beginPosition/text()"/>
			<sch:let name="temporalExtentEnd" 
				value="mri:extent/*/gex:temporalElement/*/gex:extent/*/gml:endPosition/text()"/>
			<sch:let name="publicationDate" 
				value="mri:citation/*/cit:date[./*/cit:dateType/*/@codeListValue='publication']/*/cit:date/*"/>
			<sch:let name="creationDate" 
				value="mri:citation/*/cit:date[./*/cit:dateType/*/@codeListValue='creation']/*/cit:date/*"/>
			<sch:let name="no_creationDate" 
				value="count(mri:citation/*/cit:date[./*/cit:dateType/*/@codeListValue='creation'])"/>
			<sch:let name="revisionDate" 
				value="mri:citation/*/cit:date[./*/cit:dateType/*/@codeListValue='revision']/*/cit:date/*"/>
			
			<sch:assert test="$no_creationDate &lt;= 1" diagnostics="rule.temporal.reference-temporaldate-creation-failure-en rule.temporal.reference-temporaldate-creation-failure-fr"/>
			<sch:assert test="$publicationDate or $creationDate or $revisionDate or $temporalExtentBegin or $temporalExtentEnd" diagnostics="rule.temporal.reference-temporaldate-failure-en rule.temporal.reference-temporaldate-failure-fr"/>
			<sch:report test="$temporalExtentBegin" diagnostics="rule.temporal.reference-temporaldate-begin-success-en rule.temporal.reference-temporaldate-begin-success-fr"/>
			<sch:report test="$temporalExtentEnd" diagnostics="rule.temporal.reference-temporaldate-end-success-en rule.temporal.reference-temporaldate-end-success-fr"/>
			<sch:report test="$publicationDate" diagnostics="rule.temporal.reference-temporaldate-publication-success-en rule.temporal.reference-temporaldate-publication-success-fr"/>
			<sch:report test="$revisionDate" diagnostics="rule.temporal.reference-temporaldate-revision-success-en rule.temporal.reference-temporaldate-revision-success-fr"/>
			<sch:report test="$creationDate" diagnostics="rule.temporal.reference-temporaldate-creation-success-en rule.temporal.reference-temporaldate-creation-success-fr"/>
		</sch:rule>
	</sch:pattern>
	
	
	<!--  Quality and validity -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.quality-lineage-failure-en" xml:lang="en">Lineage is missing (INSPIRE - Lineage is mandatory). Mandatory for spatial dataset and spatial dataset series; not applicable to services. In addition to general explanation of the data producer’s knowledge about the lineage of a dataset it is possible to put data quality statements here. A single ISO 19115 metadata set may comprise more than one set of Implementing instructions   quality information, each of them having one or zero lineage statement. There shall be one and only one set of quality information scoped to the full resource and having a lineage statement (See SC6 in 1.2). Example: Dataset has been digitised from the standard 1:5.000 map
		</sch:diagnostic>
		<sch:diagnostic id="rule.quality-lineage-failure-fr" xml:lang="fr">La généalogie de la ressource est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.quality-lineage-success-en" xml:lang="en">Lineage is set.</sch:diagnostic>
		<sch:diagnostic id="rule.quality-lineage-success-fr" xml:lang="fr">La généalogie de la ressource est définie.</sch:diagnostic>
		<sch:diagnostic id="rule.quality-mri-spatialResolution-failure-en" xml:lang="en"> Spatial resolution is missing (INSPIRE - Spatial resolution is mandatory if an equivalent scale or a resolution distance can be specified). Implementing instructions: Each spatial resolution is either an equivalent scale OR a ground sample distance. When two equivalent scales or two ground sample distances are expressed, the spatial resolution is an interval bounded by these two values. Example: 5000 (e.g. 1:5000 scale map)
		</sch:diagnostic>
		<sch:diagnostic id="rule.quality-mri-spatialResolution-failure-fr" xml:lang="fr">La résolution spatiale est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.quality-mri-spatialResolution-success-en" xml:lang="en">Spatial resolution is set.</sch:diagnostic>
		<sch:diagnostic id="rule.quality-mri-spatialResolution-success-fr" xml:lang="fr">La résolution spatiale est définie.</sch:diagnostic>
	</sch:diagnostics>	
	<sch:pattern id="rule.quality">
		<sch:title xml:lang="en">Quality and validity</sch:title>
      	<sch:title xml:lang="fr">Qualité et validité</sch:title>

		<sch:rule context="//mdb:resourceLineage">
			<sch:let name="lineage" value="not(mrl:LI_Lineage/mrl:statement) or (mrl:LI_lineage/mrl:statement/@gco:nilReason)"/>
			<sch:assert test="not($lineage)" diagnostics="rule.quality-lineage-failure-en rule.quality-lineage-failure-fr"/>
			<sch:report test="not($lineage)" diagnostics="rule.quality-lineage-success-en rule.quality-lineage-success-fr"/>
		</sch:rule>

		<sch:rule context="//mri:MD_DataIdentification/mri:spatialResolution|//*[@gco:isoType='mri:MD_DataIdentification']/mri:spatialResolution">
			<sch:assert test="*/mri:equivalentScale or */mri:distance or */mri:vertical or */mri:angularDistance or */mri:levelOfDetail" diagnostics="rule.quality-mri-spatialResolution-failure-en rule.quality-mri-spatialResolution-failure-fr"/>
			<sch:report test="*/mri:equivalentScale or */mri:distance or */mri:vertical or */mri:angularDistance or */mri:levelOfDetail" diagnostics="rule.quality-mri-spatialResolution-success-en rule.quality-mri-spatialResolution-success-fr"/>			
		</sch:rule>
	</sch:pattern>
	
	
	<!--  Conformity -->
	<!-- Make a non blocker conformity check operation - no assertion here -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.conformity-degree-nonev-success-en" xml:lang="en">The degree of conformity of the resource has not yet been evaluated.</sch:diagnostic>
		<sch:diagnostic id="rule.conformity-degree-nonev-success-fr" xml:lang="fr">Le degré de conformité de la ressource n'a pas encore été évalué</sch:diagnostic>
		<sch:diagnostic id="rule.conformity-specification-success-en" xml:lang="en">Spécification :"<sch:value-of select="$specification_title"/>" , "<sch:value-of select="$specification_date"/>", "<sch:value-of select="$specification_dateType"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.conformity-specification-success-fr" xml:lang="fr">Spécification :"<sch:value-of select="$specification_title"/>" , "<sch:value-of select="$specification_date"/>", "<sch:value-of select="$specification_dateType"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.conformity-degree-success-en" xml:lang="en">Degree of conformity found:"<sch:value-of select="$degree"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.conformity-degree-success-fr" xml:lang="fr">Degré de conformité :"<sch:value-of select="$degree"/>"</sch:diagnostic>
	</sch:diagnostics>	
	<sch:pattern id="rule.conformity">
		<sch:title xml:lang="en">Conformity</sch:title>
      	<sch:title xml:lang="fr">Conformité</sch:title>

		<!-- Search for on quality report result with status ... We don't really know if it's an INSPIRE conformity report or not. -->
		<sch:rule context="/mdb:MD_Metadata">
			<sch:let name="degree" value="count(mdb:dataQualityInfo/*/mdq:report/*/mdq:result/*/mdq:pass)"/>
			<sch:report test="$degree = 0" diagnostics="rule.conformity-degree-nonev-success-en rule.conformity-degree-nonev-success-fr"/>
		</sch:rule>
		
		<!-- Check specification names and status -->
		<sch:rule context="//mdb:dataQualityInfo/*/mdq:report/*/mdq:result/*">
			<sch:let name="degree" value="mdq:pass/*/text()"/>
			<sch:let name="specification_title" value="mdq:specification/*/cit:title/*/text()"/>
			<sch:let name="specification_date"  value="mdq:specification/*/cit:date/*/cit:date/*/text()"/>
			<sch:let name="specification_dateType" value="normalize-space(mdq:specification/*/cit:date/*/cit:dateType/*/@codeListValue)"/>
			
			<sch:report test="$specification_title" diagnostics="rule.conformity-specification-success-en rule.conformity-specification-success-fr"/>
			<sch:report test="$degree" diagnostics="rule.conformity-degree-success-en rule.conformity-degree-success-fr"/>
		</sch:rule>
	</sch:pattern>
	
	<!--  Constraints related to access and use -->		
	<sch:diagnostics>
		<sch:diagnostic id="rule.constraints-mri-resourceConstraints-failure-en" xml:lang="en">There shall be at least one instance of
        MD_Metadata.identificationInfo[1].MD_Identification.resourceConstraints</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-mri-resourceConstraints-failure-fr" xml:lang="fr">Une contrainte sur la ressource est requise (voir
        MD_Metadata.identificationInfo[1].MD_Identification.resourceConstraints)</sch:diagnostic>
        <sch:diagnostic id="rule.constraints-mco-accessConstraints-found-failure-en" xml:lang="en">There shall be at least one instance of 'accessConstraints'</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-mco-accessConstraints-found-failure-fr" xml:lang="fr">Il doit y avoir au moins une valeur de contrainte d'accès.</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-mco-accessConstraints-found-success-en" xml:lang="en"><sch:value-of select="$accessConstraints_count"/> instance(s) of 'accessConstraints' found.</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-mco-accessConstraints-found-success-fr" xml:lang="fr"><sch:value-of select="$accessConstraints_count"/> contrainte d'accès définie (accessConstraints).</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-allConstraints-failure-en" xml:lang="en">The value of 'accessConstraints' must be 'otherRestrictions', 
    	if there are instances of 'otherConstraints' expressing limitations on public access. Check access constraints list and other constraints text field.</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-allConstraints-failure-fr" xml:lang="fr">Les contraintes d'accès prennent la valeur 'autres restrictions' si 
        et seulement si une valeur de restrictions d'accès public au sens INSPIRE est renseignée.</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-allConstraints-success-en" xml:lang="en">Limitation on public access (otherConstraints) found:"<sch:value-of select="$otherConstraintInfo"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-allConstraints-success-fr" xml:lang="fr">Restrictions d'accès public au sens INSPIRE définie :"<sch:value-of select="$otherConstraintInfo"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-mco-accessConstraints-success-en" xml:lang="en">Limitation on public access (accessConstraints) found:"<sch:value-of select="$accessConstraints"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-mco-accessConstraints-success-fr" xml:lang="fr">Contrainte d'accès définie (accessConstraints) :"<sch:value-of select="$accessConstraints"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-classification-success-en" xml:lang="en">Limitation on public access (classification) found:"<sch:value-of select="$classification"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-classification-success-fr" xml:lang="fr">Contrainte de sécurité définie (classification) :"<sch:value-of select="$classification"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-useLimitation-failure-en" xml:lang="en">Conditions applying to access and use is missing.</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-useLimitation-failure-fr" xml:lang="fr">Une condition applicable à l'accès et à l'utilisation de la ressource est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-useLimitation-success-en" xml:lang="en">Conditions applying to access and use found :"<sch:value-of select="$useLimitation"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.constraints-useLimitation-success-fr" xml:lang="fr">Condition applicable à l'accès et à l'utilisation de la ressource :"<sch:value-of select="$useLimitation"/>"</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.constraints">
		<sch:title xml:lang="en">Constraints related to access and use</sch:title>
      	<sch:title xml:lang="fr">Contrainte d'accès et d'utilisation</sch:title>

		<sch:rule context="//mri:MD_DataIdentification|
			//*[@gco:isoType='mri:MD_DataIdentification']|
			//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:assert test="count(mri:resourceConstraints/*) > 0" diagnostics="rule.constraints-mri-resourceConstraints-failure-en rule.constraints-mri-resourceConstraints-failure-fr"/>

			<!-- cardinality of accessconstraints is [1..n] -->
			<sch:let name="accessConstraints_count" value="count(mri:resourceConstraints/*/mco:accessConstraints[*/@codeListValue != ''])"/>
			<sch:let name="accessConstraints_found" value="$accessConstraints_count > 0"/>
			
			<!-- If the value of accessConstraints is otherRestrictions
				there shall be instances of otherConstraints expressing
				limitations on public access. This is because the
				limitations on public access required by the INSPIRE
				Directive may need the use of free text, and
				otherConstraints is the only element allowing this data
				type
			-->
			<sch:let name="accessConstraints" 
				value="
				count(mri:resourceConstraints/*/mco:accessConstraints/mco:MD_RestrictionCode[@codeListValue='otherRestrictions'])&gt;0 
				and (
				not(mri:resourceConstraints/*/mco:otherConstraints)     
				or mri:resourceConstraints/*/mco:otherConstraints[@gco:nilReason='missing']
				)"/>
			<sch:let name="otherConstraints" 
				value="
				mri:resourceConstraints/*/mco:otherConstraints and
				mri:resourceConstraints/*/mco:otherConstraints/gco:CharacterString!='' and 
				count(mri:resourceConstraints/*/mco:accessConstraints/mco:MD_RestrictionCode[@codeListValue='otherRestrictions'])=0
				"/>
			<sch:let name="otherConstraintInfo" 
				value="mri:resourceConstraints/*/mco:otherConstraints/gco:CharacterString"/>

			<sch:assert test="$accessConstraints_found" diagnostics="rule.constraints-mco-accessConstraints-found-failure-en rule.constraints-mco-accessConstraints-found-failure-fr"/>
			<sch:report test="$accessConstraints_found" diagnostics="rule.constraints-mco-accessConstraints-found-success-en rule.constraints-mco-accessConstraints-found-success-fr"/>
			<sch:assert test="not($accessConstraints)" diagnostics="rule.constraints-allConstraints-failure-en rule.constraints-allConstraints-failure-fr"/>
			<sch:assert test="not($otherConstraints)" diagnostics="rule.constraints-allConstraints-failure-en rule.constraints-allConstraints-failure-fr"/>
			<sch:report test="$otherConstraintInfo!='' and not($accessConstraints) and not($otherConstraints)" diagnostics="rule.constraints-allConstraints-success-en rule.constraints-allConstraints-success-fr"/>
		</sch:rule>
		
		<sch:rule context="//mri:MD_DataIdentification/mri:resourceConstraints/*|
			//*[@gco:isoType='mri:MD_DataIdentification']/mri:resourceConstraints/*|
			//srv:SV_ServiceIdentification/mri:resourceConstraints/*|
			//*[@gco:isoType='srv:SV_ServiceIdentification']/mri:resourceConstraints/*">
			<sch:let name="accessConstraints" value="string-join(mco:accessConstraints/*/@codeListValue, ', ')"/>
			<sch:let name="classification" value="string-join(mco:classification/*/@codeListValue, ', ')"/>
			<sch:let name="otherConstraints" value="mco:otherConstraints/gco:CharacterString/text()"/>
			<sch:let name="useLimitation" value="mco:useLimitation/*/text()"/>
			<sch:let name="useLimitation_count" value="count(mco:useLimitation/*/text())"/>
			<sch:report test="$accessConstraints!=''" diagnostics="rule.constraints-mco-accessConstraints-success-en rule.constraints-mco-accessConstraints-success-fr"/>
			<sch:report test="$classification!=''" diagnostics="rule.constraints-classification-success-en rule.constraints-classification-success-fr"/>
			<sch:assert test="$useLimitation_count" diagnostics="rule.constraints-useLimitation-failure-en rule.constraints-useLimitation-failure-fr"/>
			<sch:report test="$useLimitation_count" diagnostics="rule.constraints-useLimitation-success-en rule.constraints-useLimitation-success-fr"/>
		</sch:rule>

	</sch:pattern>
	
	
	<!--  Responsible organisation -->	
	<sch:diagnostics>
		<sch:diagnostic id="rule.organisation-mri-pointOfContact-failure-en" xml:lang="en">Responsible organisation for the resource is missing (INSPIRE - Responsible organisation for the resource is mandatory). Relative to a responsible organisation, but there may be many responsible organisations for a single resource. Organisation name and email are required. See identification section / point of contact.
		</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-mri-pointOfContact-failure-fr" xml:lang="fr">Une organisation responsable de la ressource est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-mri-pointOfContact-success-en" xml:lang="en">Responsible organisation for the resource found.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-mri-pointOfContact-success-fr" xml:lang="fr">Organisation responsable de la ressource trouvée.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-info-failure-en" xml:lang="en">Organisation name and email not found for responsible organisation.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-info-failure-fr" xml:lang="fr">Le nom et l'email de l'organisation responsable de la ressource sont manquants.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-info-success-en" xml:lang="en">Organisation name and email found for :"<sch:value-of select="$organisationName"/>" ("<sch:value-of select="$role"/>")</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-info-success-fr" xml:lang="fr">Le nom et l'email de l'organisation responsable de la ressource sont définis pour :"<sch:value-of select="$organisationName"/>" ("<sch:value-of select="$role"/>")</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-cit-role-failure-en" xml:lang="en">Contact role is empty.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-cit-role-failure-fr" xml:lang="fr">Le rôle de la partie responsable est vide.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-cit-name-failure-en" xml:lang="en">Contact name (responsible person) is empty.</sch:diagnostic>
		<sch:diagnostic id="rule.organisation-cit-name-failure-fr" xml:lang="fr">Le nom de la personne responsable est vide.</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.organisation">
		<sch:title xml:lang="en">Responsible organisation</sch:title>
      	<sch:title xml:lang="fr">Organisation responsable</sch:title>
		
		<sch:rule context="//mdb:identificationInfo">
			<sch:let name="missing" value="not(*/mri:pointOfContact)"/>
			<sch:assert test="not($missing)" diagnostics="rule.organisation-mri-pointOfContact-failure-en rule.organisation-mri-pointOfContact-failure-fr"/>
			<sch:report test="not($missing)" diagnostics="rule.organisation-mri-pointOfContact-success-en rule.organisation-mri-pointOfContact-success-fr"/>
		</sch:rule>
		
		<sch:rule context="//mdb:identificationInfo/*/mri:pointOfContact
			|//*[@gco:isoType='mri:MD_DataIdentification']/mri:pointOfContact
			|//*[@gco:isoType='srv:SV_ServiceIdentification']/mri:pointOfContact">
			<sch:let name="missing" value="not(*/cit:party/*/cit:name) 
				or (*/cit:party/*/cit:name/@gco:nilReason) 
				or not(*/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress) 
				or (*/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/@gco:nilReason)"/>
			<sch:let name="organisationName" value="*/cit:party/*/cit:name/*/text()"/>
			<sch:let name="role" value="*/cit:party/*/cit:individual/*/cit:positionName/*/text()"/>
		    <sch:let name="emptyRole" value="$role=''"/>
		    <sch:let name="name" value="*/cit:party/*/cit:individual/*/cit:name/*/text()"/>
		    <sch:let name="emptyName" value="$name=''"/>
		    <sch:let name="emailAddress" value="*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/*/text()"/>			
			
			<sch:assert test="not($missing)" diagnostics="rule.organisation-info-failure-en rule.organisation-info-failure-fr"/>
		    <sch:assert test="not($emptyRole)" diagnostics="rule.organisation-cit-role-failure-en rule.organisation-cit-role-failure-fr"/> 
		    <sch:assert test="not($emptyName)" diagnostics="rule.organisation-cit-name-failure-en rule.organisation-cit-name-failure-fr"/>
		    <sch:report test="not($missing)" diagnostics="rule.organisation-info-success-en rule.organisation-info-success-fr"/>
		</sch:rule>
	</sch:pattern>
	
	<!--  Metadata on metadata -->
	<sch:diagnostics>
		<sch:diagnostic id="rule.metadata-mdb-dateInfo-failure-en" xml:lang="en">Metadata date stamp is missing.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-dateInfo-failure-fr" xml:lang="fr">La date des métadonnées est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-dateInfo-success-en" xml:lang="en">Metadata date stamp is :"<sch:value-of select="$dateInfo"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-dateInfo-success-fr" xml:lang="fr">La date des métadonnées est :"<sch:value-of select="$dateInfo"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-cit-LanguageCode-failure-en" xml:lang="en">Metadata language is missing (INSPIRE - Metadata language is mandatory). The language property is not mandated by ISO 19115, but is mandated for conformance to the INSPIRE Metadata Implementing rules.
		</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-cit-LanguageCode-failure-fr" xml:lang="fr">La langue des métadonnées est manquante.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-cit-LanguageCode-success-en" xml:lang="en">Metadata language is :"<sch:value-of select="$language"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-cit-LanguageCode-success-fr" xml:lang="fr">La langue des métadonnées est :"<sch:value-of select="$language"/>"</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-contact-failure-en" xml:lang="en">Metadata point of contact is missing (INSPIRE - Metadata point of contact is mandatory). Implementing instructions: The role of the responsible party serving as a metadata point of contact is out of scope of the INSPIRE Implementing Rules, but this property is mandated by ISO 19115. Its value can be defaulted to pointOfContact.
		</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-contact-failure-fr" xml:lang="fr">Le point de contact des métadonnées est manquant.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-contact-success-en" xml:lang="en">Metadata point of contact found.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-mdb-contact-success-fr" xml:lang="fr">Point de contact des métadonnées trouvé</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-cit-role-failure-en" xml:lang="en">Contact role is empty.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-cit-role-failure-fr" xml:lang="fr">Le rôle de la partie responsable est vide</sch:diagnostic>		
		<sch:diagnostic id="rule.metadata-info-failure-en" xml:lang="en">Organisation name and email not found metadata point of contact.</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-info-failure-fr" xml:lang="fr">Le nom et l'email du point de contact des métadonnées sont manquants</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-info-success-en" xml:lang="en">Organisation name and email found for :"<sch:value-of select="$organisationName"/>" ("<sch:value-of select="$role"/>")</sch:diagnostic>
		<sch:diagnostic id="rule.metadata-info-success-fr" xml:lang="fr">Le nom et l'email du point de contact des métadonnées sont définis pour :"<sch:value-of select="$organisationName"/>" ("<sch:value-of select="$role"/>")</sch:diagnostic>
	</sch:diagnostics>
	<sch:pattern id="rule.metadata">
		<sch:title xml:lang="en">Metadata on metadata</sch:title>
      	<sch:title xml:lang="fr">Métadonnées concernant les métadonnées</sch:title>

		<sch:rule context="//mdb:MD_Metadata">
			<!--  Date Info -->
			<sch:let name="dateInfo" value="mdb:dateInfo/*/cit:date/*/text()"/>
			<sch:assert test="$dateInfo" diagnostics="rule.metadata-mdb-dateInfo-failure-en rule.metadata-mdb-dateInfo-failure-fr"/> 
			<sch:report test="$dateInfo" diagnostics="rule.metadata-mdb-dateInfo-success-en rule.metadata-mdb-dateInfo-success-fr"/> 

			<!--  Language -->
			<sch:let name="language" value="normalize-space(mdb:defaultLocale/*/lan:language/gco:CharacterString|mdb:defaultLocale/*/lan:language/lan:LanguageCode/@codeListValue)"/>
			<sch:let name="language_present" value="contains(string-join(('eng', 'fre', 'ger', 'spa', 'dut', 'ita', 'cze', 'lav', 'dan', 'lit', 'mlt', 'pol', 'est', 'por', 'fin', 'rum', 'slo', 'slv', 'gre', 'bul', 'hun', 'swe', 'gle'), ','), $language)"/>					
			<sch:assert test="$language_present" diagnostics="rule.metadata-cit-LanguageCode-failure-en rule.metadata-cit-LanguageCode-failure-fr"/> 
			<sch:report test="$language_present" diagnostics="rule.metadata-cit-LanguageCode-success-en rule.metadata-cit-LanguageCode-success-fr"/> 

			<!--  Contact -->
			<sch:let name="missing" value="not(mdb:contact)"/>
			<sch:assert test="not($missing)" diagnostics="rule.metadata-mdb-contact-failure-en rule.metadata-mdb-contact-failure-fr"/>
			<sch:report test="not($missing)" diagnostics="rule.metadata-mdb-contact-success-en rule.metadata-mdb-contact-success-fr"/>
		</sch:rule>
		
		<sch:rule context="//mdb:MD_Metadata/mdb:contact">
			<sch:let name="missing" value="not(cit:CI_Responsibility/cit:party/*/cit:name) 
				or (cit:CI_Responsibility/cit:party/*/cit:name/@gco:nilReason) 
				or not(cit:CI_Responsibility/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress) 
				or (cit:CI_Responsibility/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/@gco:nilReason)"/>
			<sch:let name="organisationName" value="cit:CI_Responsibility/cit:party/*/cit:name/*/text()"/>
			<!-- 
				2.11.1 "The role of the responsible party serving as a metadata
				point of contact is out of scope of the INSPIRE
				Implementing Rules, but this property is mandated by ISO
				19115. The default value is pointOfContact."
				JRC schematron 1.0 validate only if role=pointOfContact
			-->
			<sch:let name="role" value="normalize-space(cit:CI_Responsibility/cit:party/*/cit:individual/*/cit:positionName/*/text())"/>
		    <sch:let name="emptyRole" value="$role=''"/>
			<sch:let name="emailAddress" value="cit:CI_Responsibility/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/*/text()"/>			
			
		    <sch:assert test="not($emptyRole)" diagnostics="rule.metadata-cit-role-failure-en rule.metadata-cit-role-failure-fr"/>
		    <sch:assert test="not($missing)" diagnostics="rule.metadata-info-failure-en rule.metadata-info-failure-fr"/>
			<sch:report test="not($missing)" diagnostics="rule.metadata-info-success-en rule.metadata-info-success-fr"/>
		</sch:rule>
		
	</sch:pattern>
	
	<!-- INSPIRE metadata rules / END -->
	
</sch:schema>
