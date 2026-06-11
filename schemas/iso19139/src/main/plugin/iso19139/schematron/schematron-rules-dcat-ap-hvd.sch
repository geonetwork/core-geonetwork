<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">

  <sch:title xmlns="http://www.w3.org/2001/XMLSchema">DCAT-AP High Value Dataset (HVD)</sch:title>
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


  <sch:diagnostic id="rule.hvd.legislation.mandatory-failure-en" xml:lang="en">
    Applicable legislation is mandatory. Use a keyword with an Anchor pointing to
    http://data.europa.eu/eli/reg_impl/2023/138/oj.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.legislation.mandatory-failure-fr" xml:lang="fr">
    La législation applicable est obligatoire. Utilisez un mot-clé avec une ancre pointant vers
    http://data.europa.eu/eli/reg_impl/2023/138/oj.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.legislation.mandatory-success-en"
                  xml:lang="en">Applicable legislation keyword found.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.legislation.mandatory-success-fr"
                  xml:lang="fr">La législation applicable HVD est encodée.
  </sch:diagnostic>

  <sch:diagnostic id="rule.hvd.conformity.mandatory-failure-en" xml:lang="en">
    No implementing rule or other specification found. Check the data quality
    report specification to add one. For INSPIRE datasets, this is a data specification conformity.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.conformity.mandatory-failure-fr" xml:lang="fr">
    Aucune règle d'implémentation ou autre spécification n'a été trouvée. Vérifiez la spécification du rapport de
    qualité des données
    pour en ajouter une. Pour les ensembles de données INSPIRE, il s'agit d'une conformité aux spécifications des
    données.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.conformity.mandatory-success-en"
                  xml:lang="en">
    Implementing rules or specifications found:<sch:value-of
    select="concat(' ', string-join($implementingRules, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.conformity.mandatory-success-fr"
                  xml:lang="fr">
    Règles ou spécifications encodées :<sch:value-of select="concat(' ', string-join($implementingRules, ', '))"/>.
  </sch:diagnostic>


  <sch:diagnostic id="rule.hvd.contactPoint.mandatory-failure-en" xml:lang="en">
    Contact information that can be used for sending comments about the Dataset is missing.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.contactPoint.mandatory-failure-fr" xml:lang="fr">
    Les informations de contact pouvant être utilisées pour envoyer des commentaires sur l'ensemble de données sont
    manquantes.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.contactPoint.mandatory-success-en"
                  xml:lang="en">
    Contact information that can be used for sending comments about the Dataset defined:<sch:value-of
    select="concat(' ', string-join($resourcePointOfContact, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.contactPoint.mandatory-success-fr"
                  xml:lang="fr">
    Contact pouvant être utilisées pour envoyer des commentaires sur l'ensemble de données encodé :<sch:value-of
    select="concat(' ', string-join($resourcePointOfContact, ', '))"/>.
  </sch:diagnostic>


  <sch:diagnostic id="rule.hvd.category.mandatory-failure-en" xml:lang="en">
    The HVD category to which this Dataset belongs is missing.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.category.mandatory-failure-fr" xml:lang="fr">
    La catégorie HVD à laquelle appartient cet ensemble de données est manquante.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.category.mandatory-success-en"
                  xml:lang="en">
    HVD categories found:<sch:value-of select="concat(' ', string-join($hvdCategories, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.category.mandatory-success-fr"
                  xml:lang="fr">
    Catégories HVD encodées :<sch:value-of select="concat(' ', string-join($hvdCategories, ', '))"/>.
  </sch:diagnostic>


  <sch:diagnostic id="rule.hvd.distribution.mandatory-failure-en" xml:lang="en">
    The HVD IR is a quality improvement of existing datasets. The intention is that HVD datasets are publicly and open
    accessible. Therefore a Distribution is expected to be present. Add an online resource with a download protocol or
    function.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.distribution.mandatory-failure-fr" xml:lang="fr">
    Les règles d'implémentation HVD ont pour objectif une amélioration de la qualité des ensembles de données existants.
    L'objectif est que les ensembles de données HVD soient accessibles au public et en libre accès. Par conséquent, une
    distribution est attendue. Ajoutez une ressource en ligne avec un protocole ou une fonction de téléchargement.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.distribution.mandatory-success-en"
                  xml:lang="en">
    Distribution URLs found:<sch:value-of select="concat(' ', string-join($distributions, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.distribution.mandatory-success-fr"
                  xml:lang="fr">
    URL(s) de distribution encodées :<sch:value-of select="concat(' ', string-join($distributions, ', '))"/>.
  </sch:diagnostic>

  <sch:diagnostic id="rule.hvd.endpointurl.mandatory-failure-en" xml:lang="en">
    The root location or primary endpoint of the service (an IRI) is missing. Add an operation with a protocol which is
    not considered as an endpoint description (ie.<sch:value-of
    select="concat(' ', $endpointDescriptionProtocolsExpression)"/>) or a URL containing <sch:value-of select="$endpointDescriptionUrllExpression"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.endpointurl.mandatory-failure-fr" xml:lang="fr">
    L'URL principale du service (un IRI) est manquant. Ajoutez une opération avec un protocole qui n'est pas une
    description de service
    (ie.<sch:value-of
    select="concat(' ', $endpointDescriptionProtocolsExpression)"/>) ou une URL contenant <sch:value-of select="$endpointDescriptionUrllExpression"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.endpointurl.mandatory-success-en"
                  xml:lang="en">
    End point URL found:<sch:value-of select="concat(' ', string-join($endpointUrls, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.endpointurl.mandatory-success-fr"
                  xml:lang="fr">
    URL(s) du service encodées :<sch:value-of select="concat(' ', string-join($endpointUrls, ', '))"/>.
  </sch:diagnostic>

  <sch:diagnostic id="rule.hvd.operateson.mandatory-failure-en" xml:lang="en">
    An API in the context of HVD is not a standalone resource. It is used to open up HVD datasets. Therefore each Data
    Service is at least tightly connected with a Dataset.
    Add at least one operatesOn element with a xlink:href or uuidref.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.operateson.mandatory-failure-fr" xml:lang="fr">
    Une API dans le contexte de HVD n'est pas une ressource autonome. Elle est utilisée pour ouvrir des ensembles de
    données HVD. Par conséquent, chaque service de données est au moins étroitement lié à un ensemble de données.
    Ajoutez au moins un élément operateOn avec un xlink:href ou un uuidref.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.operateson.mandatory-success-en"
                  xml:lang="en">
    Operates on dataset found:<sch:value-of select="concat(' ', string-join($operatesOnDatasets, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.operateson.mandatory-success-fr"
                  xml:lang="fr">
    Données associées encodées :<sch:value-of select="concat(' ', string-join($operatesOnDatasets, ', '))"/>.
  </sch:diagnostic>


  <sch:diagnostic id="rule.hvd.servicedocumentation.mandatory-failure-en" xml:lang="en">
    A page that provides additional information about the Data Service is missing.
    Add at least one online resource with a function documentation, an additional documentation or a URL pointing to https://directory.spatineo.com.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.servicedocumentation.mandatory-failure-fr" xml:lang="fr">
    Il manque une page qui fournit des informations supplémentaires sur le service de données.
    Ajoutez au moins une ressource en ligne avec une function documentation, une documentation supplémentaire ou une URL pointant vers https://directory.spatineo.com.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.servicedocumentation.mandatory-success-en"
                  xml:lang="en">
    Documentation pages found:<sch:value-of select="concat(' ', string-join($documentationUrls, ', '))"/>.
  </sch:diagnostic>
  <sch:diagnostic id="rule.hvd.servicedocumentation.mandatory-success-fr"
                  xml:lang="fr">
    Documentations encodées :<sch:value-of select="concat(' ', string-join($documentationUrls, ', '))"/>.
  </sch:diagnostic>



  <sch:pattern>
    <sch:title>HVD</sch:title>
    <sch:rule
      context="//*:MD_Metadata">

      <sch:let name="hasOneKeywordEncodingApplicableLegislationAsAnchor"
               value="count(*:identificationInfo/*/*:descriptiveKeywords/*/
                              *:keyword[*:Anchor/@xlink:href
                                  = 'http://data.europa.eu/eli/reg_impl/2023/138/oj']) = 1"/>
      <!-- TODO: Relax with CharacterString? -->

      <sch:assert test="$hasOneKeywordEncodingApplicableLegislationAsAnchor"
                  diagnostics="rule.hvd.legislation.mandatory-failure-en rule.hvd.legislation.mandatory-failure-fr"/>
      <sch:report test="$hasOneKeywordEncodingApplicableLegislationAsAnchor"
                  diagnostics="rule.hvd.legislation.mandatory-success-en rule.hvd.legislation.mandatory-success-fr"/>


      <!--
      HVD Category
      Concept
      1..*
      The HVD category to which this Dataset belongs.
      P

         <mri:descriptiveKeywords>
            <mri:MD_Keywords>
               <mri:keyword>
                  <gcx:Anchor xlink:href="http://data.europa.eu/bna/c_ac64a52d">Géospatiales</gcx:Anchor>
               </mri:keyword>
               <mri:type>
                  <mri:MD_KeywordTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_KeywordTypeCode"
                                          codeListValue="theme"/>
               </mri:type>
               <mri:thesaurusName>
                  <cit:CI_Citation>
                     <cit:title>
                        <gcx:Anchor xlink:href="http://data.europa.eu/bna/asd487ae75">High-value dataset categories</gcx:Anchor>
                     </cit:title>
      -->
      <sch:let name="hvdCategories"
               value="*:identificationInfo/*/*:descriptiveKeywords/*[
               *:thesaurusName/*/*:title/*:CharacterString = 'High-value dataset categories'
               or *:thesaurusName/*/*:title/*:Anchor/@xlink:href = 'http://data.europa.eu/bna/asd487ae75']/*:keyword/*[text() != '']"/>
      <sch:let name="hasOneOrMoreKeywordEncodingHvdCategory"
               value="count($hvdCategories) > 0"/>

      <sch:assert test="$hasOneOrMoreKeywordEncodingHvdCategory"
                  diagnostics="rule.hvd.category.mandatory-failure-en rule.hvd.category.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMoreKeywordEncodingHvdCategory"
                  diagnostics="rule.hvd.category.mandatory-success-en rule.hvd.category.mandatory-success-fr"/>


      <!--
      contact point
      Kind
      0..* (dataset) 1..* (service)
      Contact information that can be used for sending comments about the Dataset.
      A

      <mri:pointOfContact>
        <cit:CI_Responsibility>
          <cit:role>
          <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode" codeListValue="pointOfContact"/>
          </cit:role>

       Rule is more strict than HVD because contact point is mandatory in ISO and INSPIRE.
      -->
      <sch:let name="resourcePointOfContact"
               value="*:identificationInfo/*/*:pointOfContact[*/*:role/*/@codeListValue = 'pointOfContact']"/>
      <sch:let name="hasOneOrMorePointOfContact"
               value="count($resourcePointOfContact) > 0"/>

      <sch:assert test="$hasOneOrMorePointOfContact"
                  diagnostics="rule.hvd.contactPoint.mandatory-failure-en rule.hvd.contactPoint.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMorePointOfContact"
                  diagnostics="rule.hvd.contactPoint.mandatory-success-en rule.hvd.contactPoint.mandatory-success-fr"/>


    </sch:rule>
  </sch:pattern>
  <sch:pattern id="HVD (dataset)">
    <sch:rule
      context="//*:MD_Metadata[(*:metadataScope/*/*:resourceScope|*:hierarchyLevel)/*/@codeListValue = 'dataset']">

      <!--
      conforms to
      Standard
      0..*
      An implementing rule or other specification.
      The provided information should enable to the verification whether the detailed information
      requirements by the HVD is satisfied. For more usage suggestions see section on specific data requirements.
      A

      ISO encoding
      <mdq:report>
          <mdq:DQ_DomainConsistency>
             <mdq:result>
                <mdq:DQ_ConformanceResult>
                   <mdq:specification>
                      <cit:CI_Citation>
                         <cit:title>
                            <gcx:Anchor xlink:href="https://inspire.ec.europa.eu/id/document/tg/ad">
                            INSPIRE Data Specification on Addresses – Technical Guidelines, version 3.1</gcx:Anchor>

          Rule:
          * More strict, at least one?
          * TODO: non INSPIRE datasets?
      -->
      <sch:let name="implementingRules"
               value="*:dataQualityInfo/*/*:report/*/*:result/*/*:specification/*/
                                    *:title[starts-with(*:Anchor/@xlink:href, 'https://inspire.ec.europa.eu/id/document')]"/>
      <!-- TODO: Relax with has a specification ? or CharacterString starting with INSPIRE Data Specification... ? -->
      <sch:let name="hasOneOrMoreDataSpecConformityForINSPIRE"
               value="count($implementingRules) > 0"/>

      <sch:assert test="$hasOneOrMoreDataSpecConformityForINSPIRE"
                  diagnostics="rule.hvd.conformity.mandatory-failure-en rule.hvd.conformity.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMoreDataSpecConformityForINSPIRE"
                  diagnostics="rule.hvd.conformity.mandatory-success-en rule.hvd.conformity.mandatory-success-fr"/>


      <!--
        dataset distribution
        Distribution
        1..*
        An available Distribution for the Dataset.	The HVD IR is a quality improvement of existing datasets.
        The intention is that HVD datasets are publicly and open accessible.
        Therefore a Distribution is expected to be present. (Article 3.1)	Link	A

        See mapping dcat-core-distribution.xsl, foaf:page are created for
              $function = ('information', 'search', 'completeMetadata', 'browseGraphic', 'upload', 'emailService')
              or (not($function) and matches($protocol, 'WWW:LINK.*'))
        else it is a distribution
        -->
      <sch:let name="distributions"
               value="*:distributionInfo//*:onLine/*[*:linkage/(*:CharacterString|*:URL)/text() != ''][not(
                                        cit:function/*/@codeListValue = ('information', 'search', 'completeMetadata', 'browseGraphic', 'upload', 'emailService')
                                        or (not(cit:function/*/@codeListValue) and matches(*:protocol/*/text(), 'WWW:LINK.*')))]/*:linkage/(*:CharacterString|*:URL)"/>

      <sch:let name="hasOneOrMoreDistributions"
               value="count($distributions) > 0"/>

      <sch:assert test="$hasOneOrMoreDistributions"
                  diagnostics="rule.hvd.distribution.mandatory-failure-en rule.hvd.distribution.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMoreDistributions"
                  diagnostics="rule.hvd.distribution.mandatory-success-en rule.hvd.distribution.mandatory-success-fr"/>

    </sch:rule>
  </sch:pattern>


  <sch:pattern id="HVD (service)">
    <sch:rule
      context="//*:MD_Metadata[(*:metadataScope/*/*:resourceScope|*:hierarchyLevel)/*/@codeListValue = 'service']">
      <!--
      documentation (service)
      Document
      1..*
      A page that provides additional information about the Data Service.	Quality of service covers a broad spectrum of aspects.
      The HVD regulation does not list any mandatory topic. Therefore quality of service information is considered
      part of the generic documentation of a Data Service.
      P
      -->
      <sch:let name="onlineResource"
               value=".//(*:distributionInfo//mrd:onLine
                            |*:portrayalCatalogueCitation/*/*:onlineResource
                            |*:additionalDocumentation/*/*:onlineResource
                            |*:reportReference/*/*:onlineResource
                            |*:specification/*/*:onlineResource
                            |*:featureCatalogueCitation/*/*:onlineResource)"/>
      <sch:let name="documentationUrls"
               value="$onlineResource[*/*:function/*/@codeListValue = ('documentation')
                                  or count(ancestor::*:additionalDocumentation) = 1
                                  or starts-with(*/*:linkage/(*:CharacterString|*:URL), 'https://directory.spatineo.com')]/*/*:linkage/(*:CharacterString|*:URL)"/>

      <sch:let name="hasOneOrMoreDocumentation"
               value="count($documentationUrls) > 0"/>

      <sch:assert test="$hasOneOrMoreDocumentation"
                  diagnostics="rule.hvd.servicedocumentation.mandatory-failure-en rule.hvd.servicedocumentation.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMoreDocumentation"
                  diagnostics="rule.hvd.servicedocumentation.mandatory-success-en rule.hvd.servicedocumentation.mandatory-success-fr"/>

      <!--
      endpoint URL	Resource
      1..*
      The root location or primary endpoint of the service (an IRI).
      The endpoint URL SHOULD be persistent. This means that publishers should do everything in their power
      to maintain the value stable and existing.
      E
      -->

      <sch:let name="endpointDescriptionUrllExpression"
                    value="'GetCapabilities|WSDL'"/>
      <sch:let name="endpointDescriptionProtocolsExpression"
                    value="'OpenAPI|Swagger|GetCapabilities|WSDL|Description'"/>
      <sch:let name="endpointUrls"
                    value=".//*:containsOperations/*/*:connectPoint/*[not(
                                matches(*:protocol/(*:CharacterString|*:Anchor)/text(), $endpointDescriptionProtocolsExpression, 'i')
                                or matches(*:linkage/(*:CharacterString|*:Anchor)/text(), $endpointDescriptionUrllExpression, 'i'))]/(*:linkage|*:URL)/*/text()"/>

      <sch:let name="hasOneOrMoreEndPointUrls"
               value="count($endpointUrls) > 0"/>

      <sch:assert test="$hasOneOrMoreEndPointUrls"
                  diagnostics="rule.hvd.endpointurl.mandatory-failure-en rule.hvd.endpointurl.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMoreEndPointUrls"
                  diagnostics="rule.hvd.endpointurl.mandatory-success-en rule.hvd.endpointurl.mandatory-success-fr"/>

      <!--
      serves dataset
      Dataset
      1..*
      This property refers to a collection of data that this data service can distribute.
      An API in the context of HVD is not a standalone resource. It is used to open up HVD datasets.
      Therefore each Data Service is at least tightly connected with a Dataset.
      -->
      <sch:let name="operatesOnDatasets"
               value=".//*:operatesOn/(@xlink:href[. != ''], @uuidref[. != ''])[1]"/>

      <sch:let name="hasOneOrMoreOperatesOn"
               value="count($operatesOnDatasets) > 0"/>

      <sch:assert test="$hasOneOrMoreOperatesOn"
                  diagnostics="rule.hvd.operateson.mandatory-failure-en rule.hvd.operateson.mandatory-failure-fr"/>
      <sch:report test="$hasOneOrMoreOperatesOn"
                  diagnostics="rule.hvd.operateson.mandatory-success-en rule.hvd.operateson.mandatory-success-fr"/>


    </sch:rule>
  </sch:pattern>
</sch:schema>
