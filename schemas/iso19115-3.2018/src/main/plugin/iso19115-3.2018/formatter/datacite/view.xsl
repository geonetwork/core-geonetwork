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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:datacite="http://datacite.org/schema/kernel-4"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdUtil="java:org.fao.geonet.api.records.MetadataUtils"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output method="xml"
              indent="yes"/>

  <!-- Before attribution of a DOI the ISO19139 record does not contain yet
  the DOI value. It is built from the DOI id provided as parameter.
  If the DOI already exist in the record, this parameter is not set and the DOI
  is returned as datacite:identifier -->
  <xsl:param name="doiId"
             select="''"/>
  <xsl:param name="defaultDoiPrefix"
             select="'https://doi.org/'"/>
  <xsl:param name="doiProtocolRegex"
             select="'(DOI|WWW:LINK-1.0-http--metadata-URL)'"/>

  <xsl:variable name="metadata"
                select="//mdb:MD_Metadata"/>
  <xsl:variable name="metadataUuid"
                select="$metadata/mdb:metadataIdentifier/*/mcc:code/*/text()"/>

  <xsl:variable name="standardName"
                select="$metadata/mdb:metadataStandard/cit:CI_Citation/cit:title"/>


  <!-- TODO: Convert language code eng > en_US ? -->
  <xsl:variable name="metadataLanguage"
                select="$metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue"/>


  <xsl:template match="/">
    <datacite:resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:schemaLocation="http://datacite.org/schema/kernel-4 https://schema.datacite.org/meta/kernel-4.6/metadata.xsd">
      <xsl:apply-templates select="$metadata"
                           mode="toDatacite"/>

      <xsl:call-template name="transfer-size"/>

      <xsl:call-template name="related-records"/>
    </datacite:resource>
  </xsl:template>

  <!--
  The Identifier is a unique string that identifies a resource. For software,
  determine whether the identifier is for a specific version of a piece of software,
  (per the Force11 Software Citation Principles13), or for all versions.

  metadataUuid is not provided but is usually part of the DOI.
  -->
  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:metadataIdentifier/*/mcc:code/*/text()">
    <datacite:identifier identifierType="DOI">
      <!-- Return existing one -->
      <xsl:choose>
        <xsl:when test="$doiId = ''">
          <!-- DOI can be located in different places depending on user practice.
          At least we know three:
          * metadata linkage (only in ISO19115-3)
          * citation identifier
          * onlineSrc
          -->
          <xsl:variable name="doiFromMetadataLinkage"
                        select="normalize-space(ancestor::mdb:MD_Metadata/mdb:metadataLinkage/*/cit:linkage/gco:CharacterString[
                                        starts-with(., $defaultDoiPrefix)])"/>
          <xsl:if test="$doiFromMetadataLinkage != ''">
            <xsl:value-of select="$doiFromMetadataLinkage"/>
          </xsl:if>

          <xsl:variable name="doiFromIdentifier"
                        select="normalize-space(ancestor::mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/
                                        cit:identifier/*/mcc:code[
                                          starts-with(*/text(), $defaultDoiPrefix)
                                          or starts-with(*/@xlink:href, $defaultDoiPrefix)])"/>
          <xsl:if test="$doiFromMetadataLinkage = '' and $doiFromIdentifier != ''">
            <xsl:value-of select="$doiFromIdentifier[1]"/>
          </xsl:if>

          <xsl:variable name="doiFromOnlineSrc"
                        select="normalize-space(ancestor::mdb:MD_Metadata/mdb:distributionInfo//mrd:onLine/*[
                                        matches(cit:protocol/gco:CharacterString, $doiProtocolRegex)]/cit:linkage/gco:CharacterString)"/>
          <xsl:if test="$doiFromMetadataLinkage = '' and $doiFromIdentifier = '' and $doiFromOnlineSrc != ''">
            <xsl:value-of select="$doiFromOnlineSrc"/>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <!-- Build a new one from the one provided in the XSL parameter -->
          <xsl:value-of select="$doiId"/>
        </xsl:otherwise>
      </xsl:choose>
    </datacite:identifier>
  </xsl:template>


  <!--
  The primary language of the resource.
  -->
  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:defaultLocale[1]/*/lan:language/*/@codeListValue">
    <!-- TODO: Allowed values are taken
    from IETF BCP 47, ISO 639-1
    language codes.
    Examples: en, de, fr
    -->
    <datacite:language>
      <xsl:value-of select="."/>
    </datacite:language>
  </xsl:template>


  <!--
  A name or title by which a resource is known. May be the title of a dataset or the name of a piece of software.
  -->
  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:title">
    <datacite:titles>
      <xsl:call-template name="toDataciteLocalized">
        <xsl:with-param name="template">
          <datacite:title/>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:for-each select="../cit:alternateTitle[*/text() != '']">
        <xsl:call-template name="toDataciteLocalized">
          <xsl:with-param name="template">
            <datacite:title titleType="AlternativeTitle"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </datacite:titles>
  </xsl:template>



  <!--
  AlternateIdentifier

  An identifier other than the primary Identifier applied to the resource being registered.
  This may be any alphanumeric string which is unique within its domain of issue. May be used for
  local identifiers, a serial number of an instrument or an inventory number. The AlternateIdentifier
  should be an additional identifier for the same instance of the resource (i.e., same location, same
  file).
  -->
  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:identifier[1]">
    <xsl:variable name="identifiers"
                  select="../cit:identifier/*[mcc:codeSpace/gco:CharacterString != '']"/>

    <xsl:if test="$identifiers">
      <datacite:alternateIdentifiers>
        <xsl:for-each select="$identifiers">
          <datacite:alternateIdentifier alternateIdentifierType="{mcc:codeSpace/gco:CharacterString}">
            <xsl:value-of select="mcc:code/(gco:CharacterString|gcx:Anchor)"/>
          </datacite:alternateIdentifier>
        </xsl:for-each>
      </datacite:alternateIdentifiers>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:edition/gco:CharacterString[. != '']">
    <datacite:version>
      <xsl:value-of select="."/>
    </datacite:version>
  </xsl:template>


  <!--
  All additional information that does not fit in any of the other categories.
  May be used for technical information.
  -->
  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:abstract">

    <datacite:descriptions>
      <xsl:call-template name="toDataciteLocalized">
        <xsl:with-param name="template">
          <datacite:description descriptionType="Abstract"/>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:variable name="purpose" select="../(mri:purpose|mri:supplementalInformation)[gco:CharacterString != '']"/>
        <xsl:for-each select="$purpose">
        <xsl:call-template name="toDataciteLocalized">
          <xsl:with-param name="template">
            <datacite:description descriptionType="Other"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>

      <xsl:variable name="lineage" select="ancestor::mdb:MD_Metadata/mdb:resourceLineage/*/mrl:statement[gco:CharacterString != '']"/>
      <xsl:for-each select="$lineage">
        <xsl:call-template name="toDataciteLocalized">
          <xsl:with-param name="template">
            <datacite:description descriptionType="Methods"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </datacite:descriptions>
  </xsl:template>


  <xsl:template mode="toDatacite"
                match="mdb:identificationInfo/*/mri:extent[1]">
    <datacite:geoLocations>
      <xsl:for-each select="$metadata/mdb:identificationInfo//gex:EX_GeographicBoundingBox">
        <datacite:geoLocation>
          <xsl:for-each select="ancestor::gex:EX_Extent/gex:description/gco:CharacterString[. != '']">
            <datacite:geoLocationPlace><xsl:value-of select="."/></datacite:geoLocationPlace>
          </xsl:for-each>
          <datacite:geoLocationBox>
            <datacite:westBoundLongitude>
              <xsl:value-of select="gex:westBoundLongitude/*/text()"/>
            </datacite:westBoundLongitude>
            <datacite:eastBoundLongitude>
              <xsl:value-of select="gex:eastBoundLongitude/*/text()"/>
            </datacite:eastBoundLongitude>
            <datacite:southBoundLatitude>
              <xsl:value-of select="gex:southBoundLatitude/*/text()"/>
            </datacite:southBoundLatitude>
            <datacite:northBoundLatitude>
              <xsl:value-of select="gex:northBoundLatitude/*/text()"/>
            </datacite:northBoundLatitude>
          </datacite:geoLocationBox>
        </datacite:geoLocation>
      </xsl:for-each>
    </datacite:geoLocations>
  </xsl:template>


  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:descriptiveKeywords[1]">
    <datacite:subjects>
      <xsl:for-each select="$metadata//mri:keyword">
        <xsl:call-template name="toDataciteLocalized">
          <xsl:with-param name="template">
            <datacite:subject>
              <xsl:variable name="thesaurusTitle"
                            select="../mri:thesaurusName/*/cit:title"/>
              <xsl:if test="$thesaurusTitle/gcx:Anchor/@xlink:href">
                <xsl:attribute name="schemeURI"
                               select="$thesaurusTitle/gcx:Anchor/@xlink:href"/>
              </xsl:if>
              <xsl:if test="$thesaurusTitle/*/text()">
                <xsl:attribute name="subjectScheme"
                               select="normalize-space($thesaurusTitle/(gco:CharacterString|gcx:Anchor)/text()[. != ''])"/>
              </xsl:if>
              <xsl:if test="gcx:Anchor/@xlink:href">
                <xsl:attribute name="valueURI"
                               select="gcx:Anchor/@xlink:href"/>
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
  <xsl:variable name="scopeMapping" as="node()*">
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
    <entry key="application">Software</entry>
  </xsl:variable>

  <xsl:template mode="toDatacite"
                match="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue">
    <xsl:variable name="key"
                  select="."/>
    <xsl:variable name="type"
                  select="concat(upper-case(substring(.,1,1)), substring(., 2))"/>
    <datacite:resourceType resourceTypeGeneral="{($scopeMapping[@key = $key]/text(), 'Other')[1]}">
      <xsl:value-of select="concat($key, '/', $type)"/>
    </datacite:resourceType>
  </xsl:template>


  <!--
   The main researchers involved in producing the data, or the authors of the publication, in
    priority order. To supply multiple creators, repeat this property.
   -->
  <xsl:variable name="creatorRoles"
                select="('author', 'coAuthor')"/>


  <!-- Contributors
  The institution or person responsible for collecting, managing, distributing, or
  otherwise contributing to the development of the resource.
  To supply multiple contributors, repeat this property.
  For software, if there is an alternate entity that "holds, archives, publishes, prints,
  distributes, releases, issues, or produces" the code, use the contributorType
  "hostingInstitution" for the code repository.

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
  -->
  <xsl:variable name="contributorTypes" as="node()*">
    <entry key="pointOfContact">ContactPerson</entry>
    <entry key="editor">DataCollector</entry>
    <entry key="">DataCurator</entry>
    <entry key="author">DataManager</entry>
    <entry key="coAuthor">DataManager</entry>
    <entry key="resourceProvider">Distributor</entry>
    <entry key="distributor">Distributor</entry>
    <entry key="publisher">Distributor</entry>
    <entry key="processor">Editor</entry>
    <entry key="">HostingInstitution</entry>
    <entry key="originator">Producer</entry>
    <entry key="principalInvestigator">ProjectLeader</entry>
    <entry key="">ProjectManager</entry>
    <entry key="">ProjectMember</entry>
    <entry key="">RegistrationAgency</entry>
    <entry key="">RegistrationAuthority</entry>
    <entry key="collaborator">RelatedPerson</entry>
    <entry key="mediator">RelatedPerson</entry>
    <entry key="contributor">RelatedPerson</entry>
    <entry key="stakeholder">RelatedPerson</entry>
    <entry key="">Researcher</entry>
    <entry key="">ResearchGroup</entry>
    <entry key="owner">RightsHolder</entry>
    <entry key="rightsHolder">RightsHolder</entry>
    <entry key="funder">Sponsor</entry>
    <entry key="custodian">Supervisor</entry>
<!--    <entry key="">Translator</entry>
    <entry key="">WorkPackageLeader</entry>
    <entry key="">Other</entry>-->
  </xsl:variable>

  <xsl:variable name="notContributorRoles"
                select="$creatorRoles, $publisherRoles"/>


  <xsl:template mode="toDatacite"
                match="mdb:MD_Metadata/mdb:identificationInfo/*/mri:pointOfContact[1]">

    <datacite:creators>
      <xsl:for-each select="../mri:pointOfContact/*[cit:role/*/@codeListValue = $creatorRoles]">
        <datacite:creator>
          <!--
          Expect the entry point to be CI_Organisation
          The full name of the creator. -->
          <xsl:choose>
            <xsl:when test="cit:party/*/cit:individual/*/cit:name/*/text() != ''">
              <datacite:creatorName nameType="Personal">
                <xsl:value-of select="cit:party/*/cit:individual/*/cit:name/*/text()"/>
              </datacite:creatorName>
            </xsl:when>
            <xsl:otherwise>
              <datacite:creatorName nameType="Organizational">
                <xsl:value-of select="cit:party/*/cit:name/*/text()"/>
              </datacite:creatorName>
            </xsl:otherwise>
          </xsl:choose>

          <!--
          Not supported
          <datacite:givenName>Elizabeth</datacite:givenName> See datacite:creatorName
          <datacite:familyName>Miller</datacite:familyName> See datacite:creatorName
          -->

          <!-- TODO: Add support for individual identifier in creator and org one in affiliation -->
          <xsl:for-each select="cit:party//cit:partyIdentifier/*[mcc:code/(gco:CharacterString|gcx:Anchor) != '']">
            <datacite:nameIdentifier nameIdentifierScheme="{mcc:codeSpace/gco:CharacterString}">
              <xsl:value-of select="mcc:code/(gco:CharacterString|gcx:Anchor)"/>
            </datacite:nameIdentifier>
          </xsl:for-each>

          <xsl:apply-templates mode="toDataciteLocalized" select="cit:party/*/cit:name">
            <xsl:with-param name="template">
              <datacite:affiliation/>
            </xsl:with-param>
          </xsl:apply-templates>
        </datacite:creator>
      </xsl:for-each>
    </datacite:creators>



    <datacite:contributors>
      <xsl:for-each select="../mri:pointOfContact/*[not(cit:role/*/@codeListValue = $notContributorRoles)]">
        <xsl:variable name="contributorType"
                      select="$contributorTypes[@key = current()/cit:role/*/@codeListValue]"/>
        <datacite:contributor>
          <xsl:attribute name="contributorType" select="$contributorType"/>
          <xsl:choose>
            <xsl:when test="cit:party/*/cit:individual/*/cit:name/*/text() != ''">
              <datacite:contributorName nameType="Personal">
                <xsl:value-of select="cit:party/*/cit:individual/*/cit:name/*/text()"/>
              </datacite:contributorName>
            </xsl:when>
            <xsl:otherwise>
              <datacite:contributorName nameType="Organizational">
                <xsl:value-of select="cit:party/*/cit:name/*/text()"/>
              </datacite:contributorName>
            </xsl:otherwise>
          </xsl:choose>

          <xsl:for-each select="cit:party//cit:partyIdentifier/*[mcc:code/(gco:CharacterString|gcx:Anchor) != '']">
            <datacite:nameIdentifier nameIdentifierScheme="{mcc:codeSpace/gco:CharacterString}">
              <xsl:value-of select="mcc:code/(gco:CharacterString|gcx:Anchor)"/>
            </datacite:nameIdentifier>
          </xsl:for-each>

          <xsl:apply-templates mode="toDataciteLocalized" select="cit:party/*/cit:name">
            <xsl:with-param name="template">
              <datacite:affiliation/>
            </xsl:with-param>
          </xsl:apply-templates>
        </datacite:contributor>
      </xsl:for-each>
    </datacite:contributors>


    <datacite:fundingReferences>
      <xsl:for-each select="../mri:pointOfContact/*[cit:role/*/@codeListValue = 'funder']">
        <datacite:fundingReference>
          <xsl:choose>
            <xsl:when test="cit:party/*/cit:individual/*/cit:name/*/text() != ''">
              <datacite:funderName>
                <xsl:value-of select="cit:party/*/cit:individual/*/cit:name/*/text()"/>
              </datacite:funderName>
            </xsl:when>
            <xsl:otherwise>
              <datacite:funderName>
                <xsl:value-of select="cit:party/*/cit:name/*/text()"/>
              </datacite:funderName>
            </xsl:otherwise>
          </xsl:choose>

          <xsl:for-each select="cit:party//cit:partyIdentifier/*[mcc:code/(gco:CharacterString|gcx:Anchor) != '']">
            <datacite:funderIdentifier funderIdentifierType="{mcc:codeSpace/gco:CharacterString}">
              <xsl:value-of select="mcc:code/(gco:CharacterString|gcx:Anchor)"/>
            </datacite:funderIdentifier>
          </xsl:for-each>
        </datacite:fundingReference>
      </xsl:for-each>
    </datacite:fundingReferences>
  </xsl:template>

  <!--
  The name of the entity that holds, archives, publishes prints, distributes, releases, issues, or produces the resource.
  This property will be used to formulate the citation, so consider the prominence of the role. For software,
  use Publisher for the code repository. If there is an entity other than a code repository,
  that "holds, archives, publishes, prints, distributes, releases, issues, or produces" the code,
  use the property Contributor/contributorType/ hostingInstitution for the code repository.
  eg.
      <datacite:publisher>DataCite</datacite:publisher>
      <datacite:publicationYear>2014</datacite:publicationYear>

  publisher is the first distributor contact
  or the first point of contact having the role "distributor"
  -->

  <xsl:variable name="publisherRoles"
                select="('publisher')"/>

  <xsl:template mode="toDatacite"
                match="mdb:distributionInfo[1]">
    <xsl:variable name="publishers"
                  select="($metadata//(mdb:identificationInfo|mdb:distributionInfo)
                                              //cit:CI_Responsibility[cit:role/*/@codeListValue = $publisherRoles]
                                                //cit:CI_Organisation)"/>
    <xsl:for-each select="$publishers[1]">
      <xsl:variable name="identifier"
                    select="current()/cit:partyIdentifier/*"/>
      <datacite:publisher>
        <xsl:if test="$identifier">
          <xsl:attribute name="publisherIdentifier" select="$identifier/mcc:code/(gco:CharacterString|gcx:Anchor)"/>
          <xsl:attribute name="publisherIdentifierScheme" select="$identifier/mcc:codeSpace/gco:CharacterString"/>
        </xsl:if>
        <xsl:value-of select="current()/cit:name/(gco:CharacterString|gcx:Anchor)/text()"/>
      </datacite:publisher>
    </xsl:for-each>


    <!--
    The year when the data was or will be made publicly available. In the case of
    resources such as software or dynamic data where there may be multiple releases in
    one year, include the Date/dateType/dateInformation property and
    sub-properties to provide more information about the publication or release date details

    Only one is allowed.
    -->
    <xsl:variable name="publicationDates"
                  select="$metadata/mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = 'publication']/cit:date[* != '']/substring(*, 1, 4)"/>
    <xsl:for-each select="$publicationDates">
      <xsl:sort select="." order="descending"/>
      <xsl:if test="position() = 1">
        <datacite:publicationYear>
          <xsl:value-of select="current()"/>
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
      <xsl:for-each select="$metadata//mrd:formatSpecificationCitation/*/cit:title[*/text() != '']">
        <datacite:format>
          <xsl:value-of select="gco:CharacterString|gcx:Anchor"/>
        </datacite:format>
      </xsl:for-each>
    </datacite:formats>
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
                match="mdb:identificationInfo/*/mri:resourceConstraints[1]">
    <datacite:rightsList>
      <xsl:for-each select="$metadata/mdb:identificationInfo//(mco:useLimitation|mco:otherConstraints)[*/text() != '']">
        <xsl:apply-templates mode="toDataciteLocalized" select=".">
          <xsl:with-param name="template">
            <datacite:rights>
              <xsl:if test="gcx:Anchor/@xlink:href">
                <xsl:attribute name="rightsURI"
                               select="gcx:Anchor/@xlink:href"/>
              </xsl:if>
            </datacite:rights>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
    </datacite:rightsList>
  </xsl:template>


  <xsl:template name="related-records">

    <xsl:variable name="associations"
                  select="mdUtil:getAssociatedAsXml($metadataUuid)"
                  as="node()?"/>

    <xsl:variable name="isoAssociatedTypesToDcatCommonNames"
                  as="node()*">
      <entry associationType="crossReference" initiativeType="collection">isPartOf</entry>
      <entry associationType="partOfSeamlessDatabase">isPartOf</entry>
      <entry associationType="series">isPartOf</entry>
      <entry associationType="crossReference">references</entry>
      <entry associationType="isComposedOf">hasPart</entry>
      <entry associationType="revisionOf">isVersionOf</entry>
    </xsl:variable>

    <xsl:if test="$associations/relations/*">
      <datacite:relatedIdentifiers>
        <xsl:for-each select="$associations/relations/*">
          <xsl:sort select="@url"/>

          <xsl:variable name="permalink"
                        select="java:getPermalink(@uuid, 'all')"/>
          <xsl:variable name="resourceIdentifierWithHttpCodeSpace"
                        select="(root/resourceIdentifier[starts-with(codeSpace, 'http')])[1]"/>
          <xsl:variable name="recordUri"
                        select="if ($permalink)
                                      then $permalink
                                      else if ($resourceIdentifierWithHttpCodeSpace)
                                      then concat($resourceIdentifierWithHttpCodeSpace/codeSpace, $resourceIdentifierWithHttpCodeSpace/code)
                                      else @url" />

          <xsl:variable name="relationType" as="xs:string">
            <xsl:choose>
              <xsl:when test="local-name() = 'parent'">
                isPartOf
              </xsl:when>
              <xsl:when test="local-name() = 'children'">
                hasPart
              </xsl:when>
              <xsl:when test="local-name() = 'brothersAndSisters'">
                references
              </xsl:when>
              <xsl:when test="local-name() = 'sources'">
                isDerivedFrom
              </xsl:when>
              <xsl:when test="local-name() = 'datasets'">
                hasPart
              </xsl:when>
              <xsl:when test="local-name() = 'services'">
                isPublishedIn
              </xsl:when>
              <xsl:when test="local-name() = 'siblings' and not(@uuid = (../children/@uuid))">
                <xsl:variable name="associationType"
                              select="@associationType"/>
                <xsl:variable name="initiativeType"
                              select="@initiativeType"/>
                <xsl:variable name="dcTypeForAssociationAndInitiative"
                              as="xs:string?"
                              select="$isoAssociatedTypesToDcatCommonNames[@associationType = $associationType and @initiativeType = $initiativeType]/text()"/>
                <xsl:variable name="dcTypeForAssociation"
                              as="xs:string?"
                              select="$isoAssociatedTypesToDcatCommonNames[@associationType = $associationType and not(@initiativeType)]/text()"/>
                <xsl:variable name="elementType"
                              as="xs:string"
                              select="if ($dcTypeForAssociationAndInitiative)
                          then $dcTypeForAssociationAndInitiative
                          else if ($dcTypeForAssociation)
                          then $dcTypeForAssociation
                          else 'references'"/>

                <xsl:value-of select="$elementType"/>
              </xsl:when>
              <xsl:otherwise>references</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:variable name="resourceTypeGeneral" as="xs:string?">
            <xsl:choose>
              <xsl:when test="local-name() = 'services'">
                Service
              </xsl:when>
              <xsl:otherwise>Dataset</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <datacite:relatedIdentifier relatedIdentifierType="URL"
                             relationType="{normalize-space($relationType)}">
            <xsl:if test="$resourceTypeGeneral">
              <xsl:attribute name="resourceTypeGeneral" select="normalize-space($resourceTypeGeneral)"/>
            </xsl:if>
            <xsl:value-of select="$recordUri"/>
          </datacite:relatedIdentifier>
        </xsl:for-each>
      </datacite:relatedIdentifiers>
    </xsl:if>
  </xsl:template>


  <xsl:template name="transfer-size">
    <xsl:variable name="transferSizes"
                  select=".//mrd:MD_DigitalTransferOptions/mrd:transferSize/*[. castable as xs:double]"/>
    <xsl:if test="$transferSizes">
      <datacite:sizes>
        <xsl:for-each select="$transferSizes">
          <datacite:size>
            <xsl:value-of select="concat(., ' MB')"/>
          </datacite:size>
        </xsl:for-each>
      </datacite:sizes>
    </xsl:if>
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
  <xsl:variable name="dateMapping" as="node()*">
    <entry key="creation">Created</entry>
    <entry key="revision">Updated</entry>
    <entry key="publication">Issued</entry>
    <entry key="adopted">Accepted</entry>
    <entry key="released">Submitted</entry>
    <entry key="validityBegins">Valid</entry>
    <entry key="expiry">Withdrawn</entry>
    <entry key="superseded">Withdrawn</entry>
    <entry key="deprecated">Withdrawn</entry>
    <entry key="distribution">Available</entry>
<!--    <entry key="">Copyrighted</entry>
    <entry key="">Collected</entry>
    <entry key="">Coverage</entry>
    <entry key="">Other</entry>-->
  </xsl:variable>

  <xsl:template mode="toDatacite"
                match="mdb:identificationInfo/*/mri:citation/*/cit:date[1]">
    <datacite:dates>
      <xsl:for-each select="../cit:date/*[cit:dateType/*/@codeListValue = $dateMapping/@key]">
        <xsl:variable name="key"
                      select="cit:dateType/*/@codeListValue"/>
        <datacite:date dateType="{$dateMapping[@key = $key]/text()}" dateInformation="{$key}">
          <xsl:value-of select="cit:date/*/text()"/>
        </datacite:date>
      </xsl:for-each>
    </datacite:dates>
  </xsl:template>


  <!-- Convert a multi or monolingual element to a localized datacite element. -->
  <xsl:template name="toDataciteLocalized"
                mode="toDataciteLocalized" match="*">
    <xsl:param name="template" as="node()"/>

    <xsl:choose>
      <xsl:when test="lan:PT_FreeText">
        <xsl:for-each select="lan:PT_FreeText/lan:textGroup/*">
          <xsl:variable name="languageId"
                        select="@locale"/>
          <xsl:variable name="languageCode"
                        select="$metadata/(mdb:otherLocale|mdb:defaultLocale)/*[concat('#', @id) = $languageId]/lan:language/*/@codeListValue"/>
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
          <xsl:value-of select="gco:CharacterString|gcx:Anchor"/>
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
