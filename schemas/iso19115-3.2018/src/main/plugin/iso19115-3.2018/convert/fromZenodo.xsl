<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <!--
  Conversion from Zenodo JSON export to ISO19115-3.

  Example input:
  https://zenodo.org/records/6343858/export/json
  -->

  <xsl:import href="ISO19139/utility/create19115-3Namespaces.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/record">
    <mdb:MD_Metadata>
      <xsl:call-template name="add-iso19115-3.2018-namespaces"/>
      <mdb:metadataIdentifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="concat('zenodo.', id)"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </mdb:metadataIdentifier>
      <mdb:defaultLocale>
        <lan:PT_Locale>
          <!--
        <languages>
          <id>eng</id>
          <title>
            <en>English</en>
          </title>
        </languages>
          -->
          <lan:language>
            <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                              codeListValue="{metadata/languages/id}"/>
          </lan:language>
          <lan:characterEncoding>
            <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                     codeListValue="utf8"/>
          </lan:characterEncoding>
        </lan:PT_Locale>
      </mdb:defaultLocale>
      <mdb:metadataScope>
        <mdb:MD_MetadataScope>
          <mdb:resourceScope>
            <!--
            <resource_type>
              <id>software</id>
              <title>
                <de>Software</de>
                <en>Software</en>
              </title>
            </resource_type>
            -->
            <mcc:MD_ScopeCode
              codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ScopeCode"
              codeListValue="{(metadata/resource_type/id, 'dataset')[1]}"/>
          </mdb:resourceScope>
        </mdb:MD_MetadataScope>
      </mdb:metadataScope>
      <xsl:for-each select="metadata/creators">
        <mdb:contact>
          <xsl:call-template name="build-contact"/>
        </mdb:contact>
      </xsl:for-each>

      <mdb:dateInfo>
        <cit:CI_Date>
          <cit:date>
            <gco:DateTime>
              <xsl:value-of select="updated"/>
            </gco:DateTime>
          </cit:date>
          <cit:dateType>
            <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="revision"/>
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
      <!--
      <xs:element name="URI" type="xs:anyURI" minOccurs="0"/>
      -->
      <xsl:for-each select="apiUrl">
        <mdb:metadataLinkage>
          <cit:CI_OnlineResource>
            <cit:linkage>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </cit:linkage>
            <cit:function>
              <cit:CI_OnLineFunctionCode
                codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_OnLineFunctionCode"
                codeListValue="completeMetadata"/>
            </cit:function>
          </cit:CI_OnlineResource>
        </mdb:metadataLinkage>
      </xsl:for-each>

      <mdb:identificationInfo>
        <mri:MD_DataIdentification>
          <mri:citation>
            <cit:CI_Citation>
              <cit:title>
                <gco:CharacterString>
                  <!--
                    <title>argopy: A Python library for Argo ocean data analysis</title>
                  -->
                  <xsl:value-of select="metadata/title"/>
                </gco:CharacterString>
              </cit:title>
              <cit:date>
                <cit:CI_Date>
                  <cit:date>
                    <gco:Date>
                      <!--
                      <publication_date>2020-08-31</publication_date>
                      <publisher>Zenodo</publisher>
                      -->
                      <xsl:value-of select="metadata/publication_date"/>
                    </gco:Date>
                  </cit:date>
                  <cit:dateType>
                    <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="publication"/>
                  </cit:dateType>
                </cit:CI_Date>
              </cit:date>

              <xsl:for-each select="metadata/version">
                <cit:edition>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </cit:edition>
              </xsl:for-each>

              <xsl:for-each select="pids/*">
                <cit:identifier>
                  <mcc:MD_Identifier>
                    <mcc:code>
                      <gco:CharacterString>
                        <xsl:value-of select="identifier"/>
                      </gco:CharacterString>
                    </mcc:code>
                  </mcc:MD_Identifier>
                </cit:identifier>
              </xsl:for-each>
            </cit:CI_Citation>
          </mri:citation>

          <mri:abstract>
            <gco:CharacterString>
              <xsl:value-of select="util:html2text(metadata/description, true())"/>
            </gco:CharacterString>
          </mri:abstract>

          <!--
          TODO
          <funding>
            <funder>
              <name>European Commission</name>
              <id>00k4n6c32</id>
            </funder>
            <award>
              <number>824131</number>
              <acronym>Euro-Argo RISE</acronym>
              <identifiers>
                <identifier>https://cordis.europa.eu/projects/824131</identifier>
                <scheme>url</scheme>
              </identifiers>
              <id>00k4n6c32::824131</id>
              <program>H2020-EU.1.4.</program>
              <title>
                <en>Euro-Argo Research Infrastructure Sustainability and Enhancement</en>
              </title>
            </award>
          </funding>

          <publisher>Zenodo</publisher>?
          -->
          <xsl:for-each select="metadata/creators">
            <mri:pointOfContact>
              <xsl:call-template name="build-contact">
                <xsl:with-param name="role" select="'author'"/>
              </xsl:call-template>
            </mri:pointOfContact>
          </xsl:for-each>
          <xsl:for-each select="metadata/contributors">
            <mri:pointOfContact>
              <xsl:call-template name="build-contact">
                <xsl:with-param name="role" select="role/id"/>
              </xsl:call-template>
            </mri:pointOfContact>
          </xsl:for-each>

          <!--
              <subjects>
                <subject>oceanography</subject>
              </subjects>
          -->
          <xsl:variable name="keywords"
                        select="metadata/subjects/subject"
                        as="node()*"/>

          <xsl:if test="$keywords">
            <mri:descriptiveKeywords>
              <mri:MD_Keywords>
                <xsl:for-each select="$keywords">
                  <mri:keyword>
                    <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                  </mri:keyword>
                </xsl:for-each>
                <mri:type>
                  <mri:MD_KeywordTypeCode codeListValue="theme"
                                          codeList="./resources/codeList.xml#MD_KeywordTypeCode"/>
                </mri:type>
              </mri:MD_Keywords>
            </mri:descriptiveKeywords>
          </xsl:if>


          <!--
          <rights>
            <description>
              <en>A permissive license whose main conditions require preservation of copyright and license notices.
                Contributors provide an express grant of patent rights. Licensed works, modifications, and larger works may be
                distributed under different terms and without source code.
              </en>
            </description>
            <id>apache-2.0</id>
            <title>
              <en>Apache License 2.0</en>
            </title>
            <props>
              <scheme>spdx</scheme>
              <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            </props>
          </rights>
          -->
          <xsl:for-each select="metadata/rights">
            <mri:resourceConstraints>
              <mco:MD_LegalConstraints>
                <mco:accessConstraints>
                  <mco:MD_RestrictionCode
                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode"
                    codeListValue="license"/>
                </mco:accessConstraints>
                <mco:useConstraints>
                  <mco:MD_RestrictionCode
                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_RestrictionCode"
                    codeListValue="license"/>
                </mco:useConstraints>
                <mco:otherConstraints>
                  <xsl:element name="{if (props/url) then 'gcx:Anchor' else 'gco:CharacterString'}">
                    <xsl:if test="props/url">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of select="props/url"/>
                      </xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="title"/>
                  </xsl:element>
                </mco:otherConstraints>
              </mco:MD_LegalConstraints>
            </mri:resourceConstraints>
          </xsl:for-each>

          <xsl:for-each select="links/parent_doi">
            <mri:associatedResource>
              <mri:MD_AssociatedResource>
                <mri:associationType>
                  <mri:DS_AssociationTypeCode
                    codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#DS_AssociationTypeCode"
                    codeListValue="partOfSeamlessDatabase"/>
                </mri:associationType>
                <mri:metadataReference xlink:href="{.}" xlink:title="{.}"/>
              </mri:MD_AssociatedResource>
            </mri:associatedResource>
          </xsl:for-each>

          <!-- TODO Source to check-->
          <mri:defaultLocale>
            <lan:PT_Locale>
              <lan:language>
                <lan:LanguageCode codeList="codeListLocation#LanguageCode"
                                  codeListValue="{'eng'}"/>
              </lan:language>
              <lan:characterEncoding>
                <lan:MD_CharacterSetCode codeList="codeListLocation#MD_CharacterSetCode"
                                         codeListValue="utf8"/>
              </lan:characterEncoding>
            </lan:PT_Locale>
          </mri:defaultLocale>

        </mri:MD_DataIdentification>
      </mdb:identificationInfo>

      <mdb:distributionInfo>
        <mrd:MD_Distribution>

          <!-- <xsl:for-each select="md:technicalInfo/md:entry[@key = 'mimeType']/@value">
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
 -->

          <!--
          TODO
           <files>
    <entries>
      <argopy-0.1.12.tar.gz>
        <ext>gz</ext>
        <metadata>null</metadata>
        <access>
          <hidden>false</hidden>
        </access>
        <storage_class>L</storage_class>
        <size>5918014</size>
        <checksum>md5:bf9c8011113bdec1d0b02e93a3eb4218</checksum>
        <links><self>https://zenodo.org/api/records/6343858/files/argopy-0.1.12.tar.gz</self>
          https://zenodo.org/api/records/6343858/files/argopy-0.1.12.tar.gz/content
        </links>
        <mimetype>application/gzip</mimetype>
        <id>e2f49b5f-0f18-42f5-a508-7ca6221e3c7b</id>
        <key>argopy-0.1.12.tar.gz</key>
      </argopy-0.1.12.tar.gz>
      <argopy-0.1.12.zip>
        <ext>zip</ext>
        <metadata>null</metadata>
        <access>
          <hidden>false</hidden>
        </access>
        <storage_class>L</storage_class>
        <size>6015494</size>
        <checksum>md5:68cab1686b96e9f62063279c5639a8b8</checksum>
        <links><self>https://zenodo.org/api/records/6343858/files/argopy-0.1.12.zip</self>
          https://zenodo.org/api/records/6343858/files/argopy-0.1.12.zip/content
        </links>
        <mimetype>application/zip</mimetype>
        <id>1298f601-088b-4706-ab91-3d02e0afca8c</id>
        <key>argopy-0.1.12.zip</key>
      </argopy-0.1.12.zip>
          -->
          <mrd:transferOptions>
            <mrd:MD_DigitalTransferOptions>
              <xsl:for-each select="files/entries/*">
                <mrd:onLine>
                  <cit:CI_OnlineResource id="{replace(checksum, ':', '_')}">
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="links/self"/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        WWW:DOWNLOAD:<xsl:value-of select="mimetype"/>
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString>
                        <xsl:value-of select="name(.)"/>
                      </gco:CharacterString>
                    </cit:name>
                    <cit:function>
                      <cit:CI_OnLineFunctionCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode"
                        codeListValue="download"/>
                    </cit:function>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>

              <xsl:for-each select="links/doi">
                <mrd:onLine>
                  <cit:CI_OnlineResource>
                    <cit:linkage>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </cit:linkage>
                    <cit:protocol>
                      <gco:CharacterString>
                        DOI
                      </gco:CharacterString>
                    </cit:protocol>
                    <cit:name>
                      <gco:CharacterString><xsl:value-of select="name(.)"/></gco:CharacterString>
                    </cit:name>
                    <cit:function>
                      <cit:CI_OnLineFunctionCode
                        codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode"
                        codeListValue="information"/>
                    </cit:function>
                  </cit:CI_OnlineResource>
                </mrd:onLine>
              </xsl:for-each>

            </mrd:MD_DigitalTransferOptions>
          </mrd:transferOptions>
        </mrd:MD_Distribution>
      </mdb:distributionInfo>

      <mdb:resourceLineage>
        <mrl:LI_Lineage>
          <mrl:statement>
            <gco:CharacterString>
              <!-- TODO -->
            </gco:CharacterString>
          </mrl:statement>
          <mrl:scope>
            <mcc:MD_Scope>
              <mcc:level>
                <mcc:MD_ScopeCode codeList="codeListLocation#MD_ScopeCode" codeListValue="dataset"/>
              </mcc:level>
            </mcc:MD_Scope>
          </mrl:scope>
        </mrl:LI_Lineage>
      </mdb:resourceLineage>
    </mdb:MD_Metadata>
  </xsl:template>


  <xsl:template name="build-contact">
    <xsl:param name="role" select="'author'"/>

    <cit:CI_Responsibility>
      <cit:role>
        <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#CI_RoleCode"
                         codeListValue="{$role}"/>
      </cit:role>
      <cit:party>
        <cit:CI_Organisation>
          <!--
             <creators>
                  <affiliations>
                    <name>Ifremer</name>
                  </affiliations>
                  <person_or_org>
                    <identifiers>
                      <identifier>0000-0001-7231-2095</identifier>
                      <scheme>orcid</scheme>
                    </identifiers>
                    <name>M</name>
                    <given_name>G</given_name>
                    <type>personal</type>
                    <family_name>M</family_name>
                  </person_or_org>
                </creators>
          -->
          <cit:name>
            <gco:CharacterString>
              <xsl:value-of select="affiliations/name"/>
            </gco:CharacterString>
          </cit:name>
          <xsl:for-each select="person_or_org">
            <cit:individual>
              <cit:CI_Individual>
                <cit:name>
                  <gco:CharacterString>
                    <xsl:value-of select="concat(given_name, ' ', family_name)"/>
                  </gco:CharacterString>
                </cit:name>
                <!--<xsl:for-each select="md:eMail">
                  <cit:contactInfo>
                    <cit:CI_Contact>
                      <cit:address>
                        <cit:CI_Address>
                          <cit:electronicMailAddress>
                            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                          </cit:electronicMailAddress>
                        </cit:CI_Address>
                      </cit:address>
                    </cit:CI_Contact>
                  </cit:contactInfo>
                </xsl:for-each>-->
                <xsl:for-each select="identifiers">
                  <cit:partyIdentifier>
                    <mcc:MD_Identifier>
                      <mcc:code>
                        <gco:CharacterString>
                          <xsl:value-of select="if (scheme = 'orcid') then concat('https://orcid.org/', identifier) else identifier"/>
                        </gco:CharacterString>
                      </mcc:code>
                      <mcc:codeSpace>
                        <gco:CharacterString>
                          <xsl:value-of select="scheme"/>
                        </gco:CharacterString>
                      </mcc:codeSpace>
                    </mcc:MD_Identifier>
                  </cit:partyIdentifier>
                </xsl:for-each>
              </cit:CI_Individual>
            </cit:individual>
          </xsl:for-each>
        </cit:CI_Organisation>
      </cit:party>
    </cit:CI_Responsibility>
  </xsl:template>
</xsl:stylesheet>
