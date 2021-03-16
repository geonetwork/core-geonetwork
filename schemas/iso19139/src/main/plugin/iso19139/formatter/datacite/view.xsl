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
<!-- Conversion from ISO19139 to Datacite


    See http://schema.datacite.org/meta/kernel-4.1/doc/DataCite-MetadataKernel_v4.1.pdf

    Mandatory elements / There is no check that those elements are present in the record
    ID Property Obligation
1 Identifier (with mandatory type sub-property) M
2 Creator (with optional given name, family name, name identifier and affiliation sub-properties) M
3 Title (with optional type sub-properties) M
4 Publisher M
5 PublicationYear M
10 ResourceType (with mandatory general type description subproperty) M



    The following elements are not mapped
    * alternateIdentifiers: Not sure if there is requirements for this
      <datacite:alternateIdentifiers>
        <datacite:alternateIdentifier alternateIdentifierType="URL">https://schema.datacite.org/meta/kernel-4.1/example/datacite-example-full-v4.1.xml</datacite:alternateIdentifier>
      </datacite:alternateIdentifiers>

    * relatedIdentifiers: Would make sense if target relation is also a DOI ?
      <datacite:relatedIdentifiers>
        <datacite:relatedIdentifier relatedIdentifierType="URL" relationType="HasMetadata" relatedMetadataScheme="citeproc+json" schemeURI="https://github.com/citation-style-language/schema/raw/master/csl-data.json">https://data.datacite.org/application/citeproc+json/10.5072/example-full</datacite:relatedIdentifier>
        <datacite:relatedIdentifier relatedIdentifierType="arXiv" relationType="IsReviewedBy" resourceTypeGeneral="Text">arXiv:0706.0001</datacite:relatedIdentifier>
      </datacite:relatedIdentifiers>

    * sizes
      <datacite:sizes>
        <datacite:size>4 kB</datacite:size>
      </datacite:sizes>

    * fundingReferences
      <datacite:fundingReferences>
        <datacite:fundingReference>
          <datacite:funderName>National Science Foundation</datacite:funderName>
          <datacite:funderIdentifier funderIdentifierType="Crossref Funder ID">https://doi.org/10.13039/100000001</datacite:funderIdentifier>
          <datacite:awardNumber>CBET-106</datacite:awardNumber>
          <datacite:awardTitle>Full DataCite XML Example</datacite:awardTitle>
        </datacite:fundingReference>
      </datacite:fundingReferences>

     To retrieve a record:
     http://localhost:8080/geonetwork/srv/api/records/ff8d8cd6-c753-4581-99a3-af23fe4c996b/formatters/datacite?output=xml
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:datacite="http://datacite.org/schema/kernel-4"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:gn="http://www.fao.org/geonetwork"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output method="xml"
              indent="yes"/>

  <!-- Before attribution of a DOI the ISO19139 record does not contain yet
  the DOI value. It is built from the DOI prefix provided as parameter
  and the UUID of the record.
  If the DOI already exist in the record, this parameter is not set and the DOI
  is returned as datacite:identifier -->
  <xsl:param name="doiPrefix"
             select="''"/>
  <xsl:param name="defaultDoiPrefix"
             select="'https://doi.org/'"/>
  <xsl:param name="doiProtocolRegex"
             select="'(DOI|WWW:LINK-1.0-http--metadata-URL)'"/>

  <xsl:variable name="metadata"
                select="//gmd:MD_Metadata"/>

  <!-- TODO: Convert language code eng > en_US ? -->
  <xsl:variable name="metadataLanguage"
                select="//gmd:MD_Metadata/gmd:language/*/@codeListValue"/>


  <xsl:template match="/">
    <datacite:resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd">
      <xsl:apply-templates select="$metadata"
                           mode="toDatacite"/>
    </datacite:resource>
  </xsl:template>



  <!--
  The Identifier is a unique
  string that identifies a
  resource. For software,
  determine whether the
  identifier is for a specific
  version of a piece of software,
  (per the Force11 Software
  Citation Principles13), or for all
  versions.
  -->
  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:fileIdentifier/*/text()">
    <datacite:identifier identifierType="DOI">
      <!-- Return existing one -->
      <xsl:choose>
        <xsl:when test="$doiPrefix = ''">
          <!-- DOI can be located in different places depending on user practice.
          At least we know two:
          * citation identifier
          * onlineSrc
          -->
          <xsl:variable name="doiFromIdentifier"
                        select="normalize-space(../../../gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString[starts-with(., $defaultDoiPrefix)])"/>
          <xsl:if test="$doiFromIdentifier != ''">
            <xsl:value-of select="$doiFromIdentifier[1]"/>
          </xsl:if>

          <xsl:variable name="doiFromOnlineSrc"
                        select="normalize-space(../../../gmd:distributionInfo//gmd:onLine/*[matches(gmd:protocol/gco:CharacterString, $doiProtocolRegex)]/gmd:linkage/gmd:URL)"/>
          <xsl:if test="$doiFromIdentifier = '' and $doiFromOnlineSrc != ''">
            <xsl:value-of select="$doiFromOnlineSrc"/>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <!-- Build a new one -->
          <xsl:value-of select="concat($doiPrefix, '/', .)"/>
        </xsl:otherwise>
      </xsl:choose>
    </datacite:identifier>
  </xsl:template>


  <!--
  The primary language of the resource.
  -->
  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:language[1]/*/@codeListValue">
    <!-- TODO: Allowed values are taken
    from IETF BCP 47, ISO 639-1
    language codes.
    Examples: en, de, fr
    -->
    <datacite:language><xsl:value-of select="."/></datacite:language>
  </xsl:template>


  <!--
  A name or title by which a
  resource is known. May be
  the title of a dataset or the
  name of a piece of software.
  -->
  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:title">
    <datacite:titles>
      <xsl:call-template name="toDataciteLocalized">
        <xsl:with-param name="template">
          <datacite:title/>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:for-each select="../gmd:alternateTitle[*/text() != '']">
        <xsl:call-template name="toDataciteLocalized">
          <xsl:with-param name="template">
            <datacite:title titleType="AlternativeTitle"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </datacite:titles>
  </xsl:template>


  <!--
  All additional information that
  does not fit in any of the other
  categories. May be used for
  technical information.
  -->
  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract">

    <datacite:descriptions>
      <xsl:call-template name="toDataciteLocalized">
        <xsl:with-param name="template">
          <datacite:description descriptionType="Abstract"/>
        </xsl:with-param>
      </xsl:call-template>
    </datacite:descriptions>
  </xsl:template>


  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:extent[1]">
    <datacite:geoLocations>
      <xsl:for-each select="$metadata//gmd:EX_GeographicBoundingBox">
        <datacite:geoLocation>
          <!--TODO: <datacite:geoLocationPlace>Atlantic Ocean</datacite:geoLocationPlace>-->
          <datacite:geoLocationBox>
            <datacite:westBoundLongitude>
              <xsl:value-of select="gmd:westBoundLongitude/*/text()"/>
            </datacite:westBoundLongitude>
            <datacite:eastBoundLongitude>
              <xsl:value-of select="gmd:eastBoundLongitude/*/text()"/>
            </datacite:eastBoundLongitude>
            <datacite:southBoundLatitude>
              <xsl:value-of select="gmd:southBoundLatitude/*/text()"/>
            </datacite:southBoundLatitude>
            <datacite:northBoundLatitude>
              <xsl:value-of select="gmd:northBoundLatitude/*/text()"/>
            </datacite:northBoundLatitude>
          </datacite:geoLocationBox>
        </datacite:geoLocation>
      </xsl:for-each>
    </datacite:geoLocations>
  </xsl:template>


  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords[1]">
    <datacite:subjects>
      <xsl:for-each select="$metadata//gmd:keyword">
        <xsl:call-template name="toDataciteLocalized">
          <xsl:with-param name="template">
            <datacite:subject>
              <xsl:variable name="thesaurusTitle"
                            select="../gmd:thesaurusName/*/gmd:title"/>
              <xsl:if test="$thesaurusTitle/gmx:Anchor/@xlink:href">
                <xsl:attribute name="schemeURI"
                               select="$thesaurusTitle/gmx:Anchor/@xlink:href"/>
              </xsl:if>
              <xsl:if test="$thesaurusTitle/*/text()">
                <xsl:attribute name="subjectScheme"
                               select="normalize-space($thesaurusTitle/(gco:CharacterString|gmx:Anchor)/text()[. != ''])"/>
              </xsl:if>
              <xsl:if test="gmx:Anchor/@xlink:href">
                <xsl:attribute name="valueURI"
                               select="gmx:Anchor/@xlink:href"/>
              </xsl:if>
            </datacite:subject>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </datacite:subjects>
  </xsl:template>


  <!--
  The general type of a resource

  Controlled List Values:
  Audiovisual
  Collection
  DataPaper
  Dataset
  Event
  Image
  InteractiveResource
  Model
  PhysicalObject
  Service
  Software
  Sound
  Text18
  Workflow
  Other
  See Appendix for definitions and
  examples.
  -->
  <xsl:variable name="scopeMapping">
    <entry key="attribute">Model</entry>
    <entry key="attributeType">Model</entry>
    <entry key="featureType">Model</entry>
    <entry key="propertyType">Model</entry>
    <entry key="model">Model</entry>
    <entry key="collectionHardware">Other</entry>
    <entry key="collectionSession">Dataset</entry>
    <entry key="dataset">Dataset</entry>
    <entry key="tile">Image</entry>
    <entry key="nonGeographicDataset">Dataset</entry>
    <entry key="dimensionGroup">Other</entry>
    <entry key="fieldSession">Event</entry>
    <entry key="feature">PhysicalObject</entry>
    <entry key="series">Dataset</entry>
    <entry key="service">Service</entry>
    <entry key="software">Software</entry>
  </xsl:variable>

  <xsl:template mode="toDatacite"
                match="gmd:hierarchyLevel/*/@codeListValue">
    <xsl:variable name="key"
                  select="."/>
    <xsl:variable name="type"
                  select="concat(upper-case(substring(.,1,1)), substring(., 2))"/>
    <datacite:resourceType resourceTypeGeneral="{$scopeMapping//*[@key = $key]/text()}">
      <xsl:value-of select="concat($key, '/', $type)"/>
    </datacite:resourceType>
  </xsl:template>


  <!--
   The main researchers involved
    in producing the data, or the
    authors of the publication, in
    priority order. To supply
    multiple creators, repeat this
    property.
   -->
  <xsl:variable name="creatorRoles"
                select="'pointOfContact', 'custodian'"/>
  <xsl:template mode="toDatacite"
                match="gmd:MD_Metadata/gmd:identificationInfo/*/
                          gmd:pointOfContact[1]">
    <datacite:creators>
      <!-- [gmd:role/*/@codeListValue = $roles] TODO: Restrict on roles ?-->
      <xsl:for-each select="../gmd:pointOfContact/*">
        <datacite:creator>
          <!-- The full name of the creator. -->
          <datacite:creatorName nameType="Personal">
            <xsl:value-of select="gmd:individualName/*/text()"/>
          </datacite:creatorName>
          <!--<xsl:apply-templates mode="toDataciteLocalized" select="gmd:individualName">
            <xsl:with-param name="template">
              <datacite:creatorName nameType="Personal"/>
            </xsl:with-param>
          </xsl:apply-templates>-->

          <!--
          <datacite:givenName>Elizabeth</datacite:givenName>
          <datacite:familyName>Miller</datacite:familyName>
          <datacite:nameIdentifier schemeURI="http://orcid.org/" nameIdentifierScheme="ORCID">0000-0001-5000-0007</datacite:nameIdentifier>
          -->
          <xsl:apply-templates mode="toDataciteLocalized" select="gmd:organisationName">
            <xsl:with-param name="template">
              <datacite:affiliation/>
            </xsl:with-param>
          </xsl:apply-templates>
        </datacite:creator>
      </xsl:for-each>
    </datacite:creators>
  </xsl:template>


  <!-- TODO: contributors
  The institution or person
  responsible for collecting,
  managing, distributing, or
  otherwise contributing to the
  development of the resource.
  To supply multiple contributors,
  repeat this property.
  For software, if there is an
  alternate entity that "holds,
  archives, publishes, prints,
  distributes, releases, issues, or
  produces" the code, use the
  contributorType
  "hostingInstitution" for the code
  repository.

eg.
      <datacite:contributors>
        <datacite:contributor contributorType="ProjectLeader">
          <datacite:contributorName>Starr, Joan</datacite:contributorName>
          <datacite:givenName>Joan</datacite:givenName>
          <datacite:familyName>Starr</datacite:familyName>
          <datacite:nameIdentifier schemeURI="http://orcid.org/" nameIdentifierScheme="ORCID">0000-0002-7285-027X</datacite:nameIdentifier>
          <datacite:affiliation>California Digital Library</datacite:affiliation>
        </datacite:contributor>
      </datacite:contributors>

   If Contributor is used, then contributorType is mandatory.
    Controlled List Values:
    ContactPerson
    DataCollector
    DataCurator
    DataManager
    Distributor
    Editor
    HostingInstitution
    Producer
    ProjectLeader
    ProjectManager
    ProjectMember
    RegistrationAgency
    RegistrationAuthority
    RelatedPerson
    Researcher
    ResearchGroup
    RightsHolder
    Sponsor
    Supervisor
    WorkPackageLeader
    Other
  -->



  <!--
  The name of the entity that
  holds, archives, publishes
  prints, distributes, releases,
  issues, or produces the
  resource. This property will be
  used to formulate the citation,
  so consider the prominence of
  the role. For software, use
  Publisher for the code
  repository. If there is an entity
  other than a code repository,
  that "holds, archives,
  publishes, prints, distributes,
  releases, issues, or produces"
  the code, use the property
  Contributor/contributorType/
  hostingInstitution for the code
  repository.
  eg.
      <datacite:publisher>DataCite</datacite:publisher>
      <datacite:publicationYear>2014</datacite:publicationYear>

      TODO: Define who is the publisher ? Only one allowed.
  -->
  <xsl:template mode="toDatacite"
                match="gmd:distributionInfo[1]">
    <datacite:publisher>
      <xsl:value-of select="($metadata//gmd:distributorContact)[1]/*/gmd:organisationName/gco:CharacterString"/>
    </datacite:publisher>

    <!--
    The year when the data was
    or will be made publicly
    available. In the case of
    resources such as software or
    dynamic data where there
    may be multiple releases in
    one year, include the
    Date/dateType/
    dateInformation property and
    sub-properties to provide
    more information about the
    publication or release date
    details
    -->
    <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*[gmd:dateType/*/@codeListValue = 'publication']/gmd:date[* != '']/substring(*, 1, 4)">
      <xsl:sort select="." order="descending" />
      <xsl:if test="position() = 1">
        <datacite:publicationYear>
          <xsl:value-of select="."/>
        </datacite:publicationYear>
      </xsl:if>
    </xsl:for-each>


    <!--
    Technical format of the resource.
        <datacite:formats>
          <datacite:format>application/xml</datacite:format>
        </datacite:formats>
        -->
    <datacite:formats>
      <xsl:for-each select="$metadata//gmd:distributionFormat/*/gmd:name[*/text() != '']">
        <datacite:format><xsl:value-of select="gco:CharacterString"/></datacite:format>
      </xsl:for-each>
    </datacite:formats>
  </xsl:template>



  <!--
  The version number of the resource.
      <datacite:version>4.1</datacite:version>
      -->
  <xsl:template mode="toDatacite"
                match="gmd:edition[*/text() != '']">
    <datacite:version><xsl:value-of select="gco:CharacterString"/></datacite:version>
  </xsl:template>


  <!--
  Any rights information for this
  resource.
  The property may be repeated to
  record complex rights
  characteristics.

      <datacite:rightsList>
        <datacite:rights xml:lang="en-US" rightsURI="http://creativecommons.org/publicdomain/zero/1.0/">CC0 1.0 Universal</datacite:rights>
      </datacite:rightsList>
      -->
  <xsl:template mode="toDatacite"
                match="gmd:resourceConstraints[1]">
    <datacite:rightsList>
      <xsl:for-each select="$metadata//gmd:useLimitation[*/text() != '']">
        <xsl:apply-templates mode="toDataciteLocalized" select=".">
          <xsl:with-param name="template">
            <datacite:rights>
              <xsl:if test="gmx:Anchor/@xlink:href">
                <xsl:attribute name="rightsURI"
                               select="gmx:Anchor/@xlink:href"/>
              </xsl:if>
            </datacite:rights>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
    </datacite:rightsList>
  </xsl:template>


  <!--
  Controlled List Values:
  Accepted
  Available
  Copyrighted
  Collected
  Created
  Issued
  Submitted
  Updated
  Valid
  Other
-->
  <xsl:variable name="dateMapping">
    <entry key="creation">Created</entry>
    <entry key="revision">Updated</entry>
    <!--<entry key="publication"></entry> is in publicationYear -->
  </xsl:variable>

  <xsl:template mode="toDatacite"
                match="gmd:identificationInfo/*/gmd:citation/*/gmd:date[1]">
    <datacite:dates>
      <xsl:for-each select="../gmd:date/*[gmd:dateType/*/@codeListValue = $dateMapping/entry/@key]">
        <xsl:variable name="key"
                      select="gmd:dateType/*/@codeListValue"/>
        <datacite:date dateType="{$dateMapping//*[@key = $key]/text()}">
          <xsl:value-of select="gmd:date/*/text()"/>
        </datacite:date>
      </xsl:for-each>
    </datacite:dates>
  </xsl:template>


  <!-- Convert a multi or monolingual element to a localized datacite element. -->
  <xsl:template name="toDataciteLocalized"
                mode="toDataciteLocalized" match="*">
    <xsl:param name="template" as="node()"/>
    <xsl:choose>
      <xsl:when test="gmd:PT_FreeText">
        <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/*">
          <xsl:variable name="languageId"
                        select="@locale"/>
          <xsl:variable name="languageCode"
                        select="$metadata/gmd:locale/*[concat('#', @id) = $languageId]/gmd:languageCode/*/@codeListValue"/>
          <xsl:element name="{name($template/*)}">
            <xsl:attribute name="xml:lang" select="$languageCode"/>
            <xsl:copy-of select="$template/*/@*"/>
            <xsl:value-of select="."/>
          </xsl:element>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="{name($template/*)}">
          <xsl:attribute name="xml:lang" select="$metadataLanguage"/>
          <xsl:copy-of select="$template/*/@*"/>
          <xsl:value-of select="gco:CharacterString|gmx:Anchor"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--Traverse tree -->
  <xsl:template mode="toDatacite"
                match="@*|node()">
    <xsl:apply-templates mode="toDatacite"
                         select="@*|node()"/>
  </xsl:template>
</xsl:stylesheet>
