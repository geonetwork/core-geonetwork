<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:datacite="http://datacite.org/schema/kernel-4"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:template name="writeLocalizedText">
    <xsl:param name="values"/>

    <gco:CharacterString>
      <xsl:value-of select="$values[1]"/>
    </gco:CharacterString>
    <xsl:if test="count($values[@xml:lang]) > 1">
      <lan:PT_FreeText>
        <xsl:for-each select="$values[@xml:lang]">
          <xsl:variable name="languageId"
                        select="upper-case(java:twoCharLangCode(@xml:lang))"/>
          <lan:textGroup>
            <lan:LocalisedCharacterString locale="{concat('#', $languageId)}">
              <xsl:value-of select="current()"/>
            </lan:LocalisedCharacterString>
          </lan:textGroup>
        </xsl:for-each>
      </lan:PT_FreeText>
    </xsl:if>
  </xsl:template>

  <xsl:template match="/">
    <xsl:apply-templates select=".//datacite:resource"/>
  </xsl:template>

  <xsl:template match="datacite:resource">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="xs:string(datacite:identifier)"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>


      <xsl:variable name="languages"
                    select="distinct-values(.//@xml:lang)"/>

      <xsl:for-each select="$languages[1]">
        <xsl:variable name="languageId"
                      select="upper-case(java:twoCharLangCode(current()))"/>

        <mdb:defaultLocale>
          <lan:PT_Locale>
            <xsl:attribute name="id" select="$languageId"/>
            <lan:language>
              <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{current()}"/>
            </lan:language>
            <lan:characterEncoding>
              <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_CharacterSetCode"
                                       codeListValue="utf8"/>
            </lan:characterEncoding>
          </lan:PT_Locale>
        </mdb:defaultLocale>
      </xsl:for-each>


      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <!--eg.
            <resourceType resourceTypeGeneral="Dataset" >dataset/Dataset</resourceType>
            <resourceType resourceTypeGeneral="Dataset">series/Series</resourceType>
            <resourceType resourceTypeGeneral="ComputationalNotebook">Jupyter</resourceType>
            <resourceType resourceTypeGeneral="Other" ></resourceType>
            -->
            <xsl:variable name="resourceTypeGeneral" select="string(datacite:resourceType/@resourceTypeGeneral)"/>
            <xsl:variable name="code">
              <xsl:choose>
                <xsl:when test="contains(datacite:resourceType, '/')">
                  <xsl:value-of select="substring-before(datacite:resourceType, '/')"/>
                </xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Software'">software</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'ComputationalNotebook'">software</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Service'">service</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Model'">model</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Image'">tile</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Collection'">series</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Event'">fieldSession</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'PhysicalObject'">feature</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Text'">document</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Book'">document</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'ConferencePaper'">document</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'DataPaper'">document</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Dissertation'">document</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Journal'">document</xsl:when>
                <xsl:when test="$resourceTypeGeneral = 'Report'">document</xsl:when>
                <xsl:otherwise>dataset</xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_ScopeCode"
                              codeListValue="{$code}"/>
          </mdb:resourceScope>
          <mdb:name>
            <gco:CharacterString>
              <xsl:value-of select="datacite:resourceType"/>
            </gco:CharacterString>
          </mdb:name>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>


      <xsl:for-each select="datacite:creators/datacite:creator">
        <mdb:contact>
          <cit:CI_Responsibility>
            <cit:role>
              <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode"
                               codeListValue="pointOfContact"/>
            </cit:role>
            <cit:party>
              <xsl:call-template name="createParty"/>
            </cit:party>
          </cit:CI_Responsibility>
        </mdb:contact>
      </xsl:for-each>


      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>

            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_DateTypeCode"
                                 codeListValue="creation"/>
          </cit:dateType>
        </cit:CI_Date>
      </mdb:dateInfo>

      <mdb:metadataStandard>
        <cit:CI_Citation>
          <cit:title>
            <gco:CharacterString>ISO 19115-3</gco:CharacterString>
          </cit:title>
        </cit:CI_Citation>
      </mdb:metadataStandard>

      <xsl:for-each select="$languages[position() > 1]">
        <xsl:variable name="languageId"
                      select="upper-case(java:twoCharLangCode(current()))"/>

        <mdb:otherLocale>
          <lan:PT_Locale>
            <xsl:attribute name="id" select="$languageId"/>
            <lan:language>
              <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{current()}"/>
            </lan:language>
            <lan:characterEncoding>
              <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_CharacterSetCode"
                                       codeListValue="utf8"/>
            </lan:characterEncoding>
          </lan:PT_Locale>
        </mdb:otherLocale>
      </xsl:for-each>


      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <xsl:call-template name="writeLocalizedText">
                  <xsl:with-param name="values" select="datacite:titles/datacite:title[not(@titleType)]"/>
                </xsl:call-template>
              </cit:title>
              <xsl:for-each select="datacite:titles/datacite:title[@titleType='AlternativeTitle']">
                <cit:alternateTitle>
                  <xsl:call-template name="writeLocalizedText">
                    <xsl:with-param name="values" select="."/>
                  </xsl:call-template>
                </cit:alternateTitle>
              </xsl:for-each>

              <xsl:if test="datacite:publicationYear">
                <cit:date>
                  <cit:CI_Date>
                    <cit:date>
                      <gco:DateTime>
                        <xsl:value-of select="datacite:publicationYear"/>
                      </gco:DateTime>
                    </cit:date>
                    <cit:dateType>
                      <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_DateTypeCode"
                                           codeListValue="publication"/>
                    </cit:dateType>
                  </cit:CI_Date>
                </cit:date>
              </xsl:if>

              <xsl:for-each select="datacite:dates/datacite:date">
                <xsl:variable name="dateType" select="@dateType"/>
                <xsl:variable name="isoDateType">
                  <xsl:choose>
                    <xsl:when test="$dateType = 'Created'">creation</xsl:when>
                    <xsl:when test="$dateType = 'Updated'">revision</xsl:when>
                    <xsl:when test="$dateType = 'Issued'">publication</xsl:when>
                    <xsl:when test="$dateType = 'Accepted'">adopted</xsl:when>
                    <xsl:when test="$dateType = 'Submitted'">released</xsl:when>
                    <xsl:when test="$dateType = 'Valid'">validityBegins</xsl:when>
                    <xsl:when test="$dateType = 'Withdrawn'">expiry</xsl:when>
                    <xsl:when test="$dateType = 'Available'">distribution</xsl:when>
                    <xsl:otherwise>creation</xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>

                <cit:date>
                  <cit:CI_Date>
                    <cit:date>
                      <gco:DateTime>
                        <xsl:value-of select="."/>
                      </gco:DateTime>
                    </cit:date>
                    <cit:dateType>
                      <cit:CI_DateTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_DateTypeCode"
                                           codeListValue="{$isoDateType}"/>
                    </cit:dateType>
                  </cit:CI_Date>
                </cit:date>
              </xsl:for-each>

              <xsl:if test="datacite:version">
                <cit:edition>
                  <gco:CharacterString>
                    <xsl:value-of select="datacite:version"/>
                  </gco:CharacterString>
                </cit:edition>
              </xsl:if>

              <cit:identifier>
                <mcc:MD_Identifier>
                  <mcc:code>
                    <gco:CharacterString>
                      <xsl:value-of select="datacite:identifier"/>
                    </gco:CharacterString>
                  </mcc:code>
                </mcc:MD_Identifier>
              </cit:identifier>

              <xsl:for-each select="datacite:alternateIdentifiers/datacite:alternateIdentifier">
                <cit:identifier>
                  <mcc:MD_Identifier>
                    <mcc:code>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </mcc:code>
                    <xsl:if test="@nameIdentifierScheme">
                      <mcc:codeSpace>
                        <gco:CharacterString>
                          <xsl:value-of select="@nameIdentifierScheme"/>
                        </gco:CharacterString>
                      </mcc:codeSpace>
                    </xsl:if>
                  </mcc:MD_Identifier>
                </cit:identifier>
              </xsl:for-each>
            </cit:CI_Citation>
          </mri:citation>

          <xsl:if test="datacite:descriptions/datacite:description[not(@descriptionType = 'Methods' or @descriptionType = 'Other')]">
            <mri:abstract>
              <xsl:call-template name="writeLocalizedText">
                <xsl:with-param name="values" select="datacite:descriptions/datacite:description[not(@descriptionType = 'Methods' or @descriptionType = 'Other')]"/>
              </xsl:call-template>
            </mri:abstract>
          </xsl:if>

          <xsl:for-each select="datacite:creators/datacite:creator">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode"
                                   codeListValue="author"/>
                </cit:role>
                <cit:party>
                  <xsl:call-template name="createParty"/>
                </cit:party>
              </cit:CI_Responsibility>
            </mri:pointOfContact>
          </xsl:for-each>

          <xsl:if test="datacite:publisher">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode"
                                   codeListValue="publisher"/>
                </cit:role>
                <cit:party>
                  <cit:CI_Organisation>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="datacite:publisher"/>
                      </gco:CharacterString>
                    </cit:name>
                    <xsl:if test="datacite:publisher/@publisherIdentifier">
                      <cit:partyIdentifier>
                        <mcc:MD_Identifier>
                          <mcc:code>
                            <gco:CharacterString>
                              <xsl:value-of select="datacite:publisher/@publisherIdentifier"/>
                            </gco:CharacterString>
                          </mcc:code>
                          <xsl:if test="datacite:publisher/@publisherIdentifierScheme">
                            <mcc:codeSpace>
                              <gco:CharacterString>
                                <xsl:value-of select="datacite:publisher/@publisherIdentifierScheme"/>
                              </gco:CharacterString>
                            </mcc:codeSpace>
                          </xsl:if>
                        </mcc:MD_Identifier>
                      </cit:partyIdentifier>
                    </xsl:if>
                  </cit:CI_Organisation>
                </cit:party>
              </cit:CI_Responsibility>
            </mri:pointOfContact>
          </xsl:if>

          <xsl:for-each select="datacite:contributors/datacite:contributor">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <xsl:variable name="role">
                    <xsl:choose>
                      <xsl:when test="@contributorType='ContactPerson'">pointOfContact</xsl:when>
                      <xsl:when test="@contributorType='DataCollector'">editor</xsl:when>
                      <xsl:when test="@contributorType='DataManager'">author</xsl:when>
                      <xsl:when test="@contributorType='Distributor'">distributor</xsl:when>
                      <xsl:when test="@contributorType='Editor'">processor</xsl:when>
                      <xsl:when test="@contributorType='HostingInstitution'">pointOfContact</xsl:when>
                      <xsl:when test="@contributorType='Producer'">originator</xsl:when>
                      <xsl:when test="@contributorType='ProjectLeader'">principalInvestigator</xsl:when>
                      <xsl:when test="@contributorType='ProjectManager'">pointOfContact</xsl:when>
                      <xsl:when test="@contributorType='ProjectMember'">pointOfContact</xsl:when>
                      <xsl:when test="@contributorType='RelatedPerson'">collaborator</xsl:when>
                      <xsl:when test="@contributorType='RightsHolder'">rightsHolder</xsl:when>
                      <xsl:when test="@contributorType='Sponsor'">funder</xsl:when>
                      <xsl:when test="@contributorType='Supervisor'">custodian</xsl:when>
                      <xsl:otherwise>pointOfContact</xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>

                  <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode"
                                   codeListValue="{$role}"/>
                </cit:role>
                <cit:party>
                  <xsl:call-template name="createParty">
                    <xsl:with-param name="name" select="datacite:contributorName"/>
                    <xsl:with-param name="nameType" select="datacite:contributorName/@nameType"/>
                  </xsl:call-template>
                </cit:party>
              </cit:CI_Responsibility>
            </mri:pointOfContact>
          </xsl:for-each>

          <xsl:for-each select="datacite:fundingReferences/datacite:fundingReference">
            <mri:pointOfContact>
              <cit:CI_Responsibility>
                <cit:role>
                  <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode"
                                   codeListValue="funder"/>
                </cit:role>
                <cit:party>
                  <xsl:call-template name="createParty">
                    <xsl:with-param name="name" select="datacite:funderName"/>
                    <xsl:with-param name="nameType" select="datacite:funderName/@nameType"/>
                    <xsl:with-param name="identifier" select="datacite:funderIdentifier"/>
                  </xsl:call-template>
                </cit:party>
              </cit:CI_Responsibility>
            </mri:pointOfContact>
          </xsl:for-each>

          <xsl:if test="datacite:geoLocations/datacite:geoLocation/datacite:geoLocationBox">
            <xsl:for-each select="datacite:geoLocations/datacite:geoLocation/datacite:geoLocationBox">
              <mri:extent>
                <gex:EX_Extent>
                  <gex:geographicElement>
                    <gex:EX_GeographicBoundingBox>
                      <gex:westBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="datacite:westBoundLongitude"/>
                        </gco:Decimal>
                      </gex:westBoundLongitude>
                      <gex:eastBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="datacite:eastBoundLongitude"/>
                        </gco:Decimal>
                      </gex:eastBoundLongitude>
                      <gex:southBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="datacite:southBoundLatitude"/>
                        </gco:Decimal>
                      </gex:southBoundLatitude>
                      <gex:northBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="datacite:northBoundLatitude"/>
                        </gco:Decimal>
                      </gex:northBoundLatitude>
                    </gex:EX_GeographicBoundingBox>
                  </gex:geographicElement>
                </gex:EX_Extent>
              </mri:extent>
            </xsl:for-each>
          </xsl:if>

          <xsl:for-each-group select="datacite:subjects/datacite:subject" group-by="concat(@schemeURI, '|', @subjectScheme)">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="current-group()">
                  <mri:keyword>
                    <xsl:choose>
                      <xsl:when test="@valueURI">
                        <gcx:Anchor xlink:href="{@valueURI}">
                          <xsl:value-of select="."/>
                        </gcx:Anchor>
                      </xsl:when>
                      <xsl:otherwise>
                        <gco:CharacterString>
                          <xsl:value-of select="."/>
                        </gco:CharacterString>
                      </xsl:otherwise>
                    </xsl:choose>
                  </mri:keyword>
                </xsl:for-each>

                <xsl:if test="current-group()[1]/@subjectScheme or current-group()[1]/@schemeURI">
                  <mri:thesaurusName>
                    <cit:CI_Citation>
                      <cit:title>
                        <xsl:choose>
                          <xsl:when test="current-group()[1]/@schemeURI">
                            <gcx:Anchor xlink:href="{current-group()[1]/@schemeURI}">
                              <xsl:value-of select="current-group()[1]/@subjectScheme"/>
                            </gcx:Anchor>
                          </xsl:when>
                          <xsl:otherwise>
                            <gco:CharacterString>
                              <xsl:value-of select="current-group()[1]/@subjectScheme"/>
                            </gco:CharacterString>
                          </xsl:otherwise>
                        </xsl:choose>
                      </cit:title>
                    </cit:CI_Citation>
                  </mri:thesaurusName>
                </xsl:if>

                <mri:type>
                  <mri:MD_KeywordTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_KeywordTypeCode" codeListValue="theme"/>
                </mri:type>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>
          </xsl:for-each-group>


          <xsl:if test="datacite:rightsList/datacite:rights">
            <mri:resourceConstraints>
              <mco:MD_LegalConstraints>
                <xsl:for-each select="datacite:rightsList/datacite:rights">
                  <mco:useLimitation>
                    <xsl:call-template name="writeLocalizedText">
                      <xsl:with-param name="values" select="."/>
                    </xsl:call-template>
                  </mco:useLimitation>
                </xsl:for-each>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>
          </xsl:if>

          <xsl:for-each select="datacite:relatedIdentifiers/datacite:relatedIdentifier">
            <mri:associatedResource>
              <mri:MD_AssociatedResource>
                <mri:associationType>
                  <xsl:variable name="type">
                    <xsl:choose>
                      <xsl:when test="@relationType='IsPartOf'">isPartOf</xsl:when>
                      <xsl:when test="@relationType='HasPart'">hasPart</xsl:when>
                      <xsl:when test="@relationType='References'">crossReference</xsl:when>
                      <xsl:when test="@relationType='IsreferencedBy'">crossReference</xsl:when>
                      <xsl:when test="@relationType='IsDerivedFrom'">source</xsl:when>
                      <xsl:when test="@relationType='IsSourceOf'">source</xsl:when>
                      <xsl:when test="@relationType='IsVersionOf'">revisionOf</xsl:when>
                      <xsl:otherwise>crossReference</xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <mri:DS_AssociationTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#DS_AssociationTypeCode"
                                              codeListValue="{$type}"/>
                </mri:associationType>
                <mri:metadataReference xlink:href="{.}"/>
              </mri:MD_AssociatedResource>
            </mri:associatedResource>
          </xsl:for-each>

          <xsl:for-each select="datacite:language">
            <mri:defaultLocale>
              <lan:PT_Locale>
                <lan:language>
                  <lan:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="{current()}"/>
                </lan:language>
                <lan:characterEncoding>
                  <lan:MD_CharacterSetCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode"
                                           codeListValue=""/>
                </lan:characterEncoding>
              </lan:PT_Locale>
            </mri:defaultLocale>
          </xsl:for-each>


          <xsl:if test="datacite:descriptions/datacite:description[@descriptionType='Other']">
            <mri:supplementalInformation>
              <xsl:call-template name="writeLocalizedText">
                <xsl:with-param name="values" select="datacite:descriptions/datacite:description[@descriptionType='Other']"/>
              </xsl:call-template>
            </mri:supplementalInformation>
          </xsl:if>
        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <xsl:if test="datacite:formats/datacite:format | datacite:sizes/datacite:size">
        <mdb:distributionInfo>
          <mrd:MD_Distribution>
            <xsl:for-each select="datacite:formats/datacite:format">
              <mrd:distributionFormat>
                <mrd:MD_Format>
                  <mrd:formatSpecificationCitation>
                    <cit:CI_Citation>
                      <cit:title>
                        <gco:CharacterString>
                          <xsl:value-of select="."/>
                        </gco:CharacterString>
                      </cit:title>
                    </cit:CI_Citation>
                  </mrd:formatSpecificationCitation>
                </mrd:MD_Format>
              </mrd:distributionFormat>
            </xsl:for-each>

            <xsl:if test="datacite:sizes/datacite:size">
              <mrd:transferOptions>
                <mrd:MD_DigitalTransferOptions>
                  <xsl:for-each select="datacite:sizes/datacite:size">
                    <mrd:transferSize>
                      <gco:Real>
                        <xsl:value-of select="if (ends-with(., ' MB')) then replace(., ' MB', '') else ."/>
                      </gco:Real>
                    </mrd:transferSize>
                  </xsl:for-each>
                </mrd:MD_DigitalTransferOptions>
              </mrd:transferOptions>
            </xsl:if>

            <!-- TODO: Some providers eg. Nakala use relatedIdentifier for distribution. Can be improved -->
            <xsl:variable name="relatedIdentifierUrl"
                          select="datacite:relatedIdentifiers/datacite:relatedIdentifier"/>
            <xsl:if test="$relatedIdentifierUrl">
              <mrd:transferOptions>
                <mrd:MD_DigitalTransferOptions>
                  <xsl:for-each select="$relatedIdentifierUrl">
                    <mrd:onLine>
                      <cit:CI_OnlineResource>
                        <cit:linkage>
                          <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                        </cit:linkage>
                        <cit:protocol>
                          <gco:CharacterString>WWW:LINK</gco:CharacterString>
                        </cit:protocol>
                        <cit:name>
                          <gco:CharacterString><xsl:value-of select="@relationType"/></gco:CharacterString>
                        </cit:name>
                      </cit:CI_OnlineResource>
                    </mrd:onLine>
                  </xsl:for-each>
                </mrd:MD_DigitalTransferOptions>
              </mrd:transferOptions>
            </xsl:if>
          </mrd:MD_Distribution>
        </mdb:distributionInfo>
      </xsl:if>

      <xsl:if test="datacite:descriptions/datacite:description[@descriptionType='Methods']">
        <mdb:resourceLineage>
          <mrl:LI_Lineage>
            <mrl:statement>
              <xsl:call-template name="writeLocalizedText">
                <xsl:with-param name="values" select="datacite:descriptions/datacite:description[@descriptionType='Methods']"/>
              </xsl:call-template>
            </mrl:statement>
          </mrl:LI_Lineage>
        </mdb:resourceLineage>
      </xsl:if>

    </mdb:MD_Metadata>
  </xsl:template>

  <xsl:template name="createParty">
    <xsl:param name="name" select="datacite:creatorName"/>
    <xsl:param name="nameType" select="datacite:creatorName/@nameType"/>
    <xsl:param name="identifier" select="datacite:nameIdentifier"/>

    <xsl:choose>
      <xsl:when test="$nameType = 'Organizational'">
        <cit:CI_Organisation>
          <cit:name>
            <gco:CharacterString>
              <xsl:value-of select="$name"/>
            </gco:CharacterString>
          </cit:name>
          <xsl:if test="$identifier">
            <cit:partyIdentifier>
              <mcc:MD_Identifier>
                <mcc:code>
                  <gco:CharacterString>
                    <xsl:value-of select="$identifier"/>
                  </gco:CharacterString>
                </mcc:code>
                <xsl:if test="$identifier/@nameIdentifierScheme">
                  <mcc:codeSpace>
                    <gco:CharacterString>
                      <xsl:value-of select="$identifier/@nameIdentifierScheme"/>
                    </gco:CharacterString>
                  </mcc:codeSpace>
                </xsl:if>
              </mcc:MD_Identifier>
            </cit:partyIdentifier>
          </xsl:if>
        </cit:CI_Organisation>
      </xsl:when>
      <xsl:otherwise>
          <cit:CI_Organisation>
            <cit:name>
              <gco:CharacterString>
                <xsl:value-of select="datacite:affiliation"/>
              </gco:CharacterString>
            </cit:name>
            <cit:individual>
              <cit:CI_Individual>
                <cit:name>
                  <gco:CharacterString>
                    <xsl:value-of select="$name"/>
                  </gco:CharacterString>
                </cit:name>

                <xsl:if test="$identifier">
                  <cit:partyIdentifier>
                    <mcc:MD_Identifier>
                      <mcc:code>
                        <gco:CharacterString>
                          <xsl:value-of select="$identifier"/>
                        </gco:CharacterString>
                      </mcc:code>
                      <xsl:if test="$identifier/@nameIdentifierScheme">
                        <mcc:codeSpace>
                          <gco:CharacterString>
                            <xsl:value-of select="$identifier/@nameIdentifierScheme"/>
                          </gco:CharacterString>
                        </mcc:codeSpace>
                      </xsl:if>
                    </mcc:MD_Identifier>
                  </cit:partyIdentifier>
                </xsl:if>
              </cit:CI_Individual>
            </cit:individual>
          </cit:CI_Organisation>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
