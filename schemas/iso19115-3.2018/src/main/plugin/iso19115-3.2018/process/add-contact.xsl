<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <xsl:param name="contactRole" select="'author'"/>
  <xsl:param name="contactId" select="''"/>
  <xsl:param name="contactReplace" select="'0'"/>
  <xsl:param name="contactSetForMetadata" select="'1'"/>
  <xsl:param name="contactSetForResource" select="'0'"/>

  <!-- i18n information -->
  <xsl:variable name="add-contact-loc">
    <msg id="a" xml:lang="eng">Add '{{user.name}} {{user.surname}}' as contact for the metadata or the resource.</msg>
    <msg id="a" xml:lang="fre">Ajouter '{{user.name}} {{user.surname}}' en tant que contact pour la fiche ou la resource.</msg>
  </xsl:variable>


  <xsl:variable name="contactIsReplacedBy"
                select="geonet:parseBoolean($contactReplace)"/>
  <xsl:variable name="isContactSetForMetadata"
                select="geonet:parseBoolean($contactSetForMetadata)"/>
  <xsl:variable name="isContactSetForResource"
                select="geonet:parseBoolean($contactSetForResource)"/>



  <xsl:template name="list-add-contact">
    <suggestion process="add-contact"/>
  </xsl:template>



  <!-- Analyze the metadata record and return available suggestion
      for that process -->
  <xsl:template name="analyze-add-contact">
    <xsl:param name="root"/>

    <xsl:variable name="isMdContactAlreadySet"
                  select="count($root//mdb:contact) > 0"/>

    <!--<xsl:if test="$isMdContactAlreadySet">-->
    <xsl:if test="true()">
      <suggestion process="add-contact"
                  id="{generate-id()}"
                  category="contact"
                  target="metadata">
        <name><xsl:value-of select="geonet:i18n($add-contact-loc, 'a', $guiLang)"/></name>
        <operational>true</operational>
        <params>{"contactRole":{"type":"codelist", "codelist": "roleCode", "defaultValue": "<xsl:value-of select="$contactRole"/>"},
                 "contactId":{"type": "expression", "defaultValue":"{{user.id}}"},
                 "contactReplace":{"type": "boolean", "defaultValue": "<xsl:value-of select="$contactReplace"/>"},
                  "contactSetForMetadata": {"type": "boolean", "defaultValue": "<xsl:value-of select="$contactSetForMetadata"/>"},
                  "contactSetForResource": {"type": "boolean", "defaultValue": "<xsl:value-of select="$contactSetForResource"/>"}}</params>
      </suggestion>
    </xsl:if>

  </xsl:template>




  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


  <!-- Insert contact for the metadata -->
  <xsl:template
          match="mdb:MD_Metadata"
          priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates
              select="mdb:metadataIdentifier|
                mdb:defaultLocale|
                mdb:parentMetadata|
                mdb:metadataScope"/>

      <xsl:if test="$isContactSetForMetadata and $contactId != ''">
        <mdb:contact>
          <xsl:copy-of select="geonet:make-iso19115-3-contact($contactId, $contactRole)"/>
        </mdb:contact>
      </xsl:if>

      <xsl:if test="$isContactSetForMetadata and not($contactIsReplacedBy)">
        <xsl:apply-templates
              select="mdb:contact"/>
      </xsl:if>

      <xsl:apply-templates
              select="mdb:dateInfo|
                mdb:metadataStandard|
                mdb:metadataProfile|
                mdb:alternativeMetadataReference|
                mdb:otherLocale|
                mdb:metadataLinkage|
                mdb:spatialRepresentationInfo|
                mdb:referenceSystemInfo|
                mdb:metadataExtensionInfo|
                mdb:identificationInfo|
                mdb:contentInfo|
                mdb:distributionInfo|
                mdb:dataQualityInfo|
                mdb:resourceLineage|
                mdb:portrayalCatalogueInfo|
                mdb:metadataConstraints|
                mdb:applicationSchemaInfo|
                mdb:metadataMaintenance|
                mdb:acquisitionInformation"/>

    </xsl:copy>
  </xsl:template>


  <!-- Insert contact for the resource -->

  <xsl:template
          match="mdb:identificationInfo/*"
          priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates
              select="mri:citation|
                mri:abstract|
                mri:purpose|
                mri:credit|
                mri:status
                "/>

      <xsl:if test="$isContactSetForResource and $contactId != ''">
        <mri:pointOfContact>
          <xsl:copy-of select="geonet:make-iso19115-3-contact($contactId, $contactRole)"/>
        </mri:pointOfContact>
      </xsl:if>

      <xsl:if test="$isContactSetForResource and not($contactIsReplacedBy)">
        <xsl:apply-templates
                select="mri:pointOfContact"/>
      </xsl:if>

      <xsl:apply-templates
              select="mri:spatialRepresentationType|
                mri:spatialResolution|
                mri:temporalResolution|
                mri:topicCategory|
                mri:additionalDocumentation|
                mri:processingLevel|
                mri:resourceMaintenance|
                mri:graphicOverview|
                mri:resourceFormat|
                mri:descriptiveKeywords|
                mri:resourceSpecificUsage|
                mri:resourceConstraints|
                mri:associatedResource|
                mri:defaultLocale|
                mri:otherLocale|
                mri:environmentDescription|
                mri:supplementalInformation
                "/>

      <!-- Service -->
      <xsl:apply-templates select="srv:*"/>
    </xsl:copy>
  </xsl:template>




  <!-- Get database user info and create a contact -->
  <xsl:function name="geonet:make-iso19115-3-contact" as="node()?">
    <xsl:param name="contactId" as="xs:string"/>
    <xsl:param name="contactRole" as="xs:string"/>

    <xsl:variable name="contactDetails" select="util:getUserDetails($contactId)"/>

    <xsl:if test="$contactDetails != ''">
      <xsl:variable name="user" select="saxon:parse($contactDetails)" />
      <cit:CI_Responsibility>
        <cit:role>
          <cit:CI_RoleCode codeList="codeListLocation#CI_RoleCode" codeListValue="{$contactRole}"></cit:CI_RoleCode>
        </cit:role>
        <cit:party>
          <cit:CI_Organisation>
            <cit:name>
              <gco:CharacterString>
                <xsl:value-of select="$user/record/organisation"/>
              </gco:CharacterString>
            </cit:name>
            <cit:contactInfo>
              <cit:CI_Contact>
                <cit:address>
                  <cit:CI_Address>
                    <xsl:if test="$user/record/primaryaddress/address != ''">
                      <cit:deliveryPoint>
                        <gco:CharacterString>
                          <xsl:value-of select="$user/record/primaryaddress/address"/>
                        </gco:CharacterString>
                      </cit:deliveryPoint>
                    </xsl:if>
                    <xsl:if test="$user/record/primaryaddress/city != ''">
                      <cit:city>
                        <gco:CharacterString>
                          <xsl:value-of select="$user/record/primaryaddress/city"/>
                        </gco:CharacterString>
                      </cit:city>
                    </xsl:if>
                    <xsl:if test="$user/record/primaryaddress/state != ''">
                      <cit:administrativeArea>
                        <gco:CharacterString>
                          <xsl:value-of select="$user/record/primaryaddress/state"/>
                        </gco:CharacterString>
                      </cit:administrativeArea>
                    </xsl:if>
                    <xsl:if test="$user/record/primaryaddress/zip != ''">
                      <cit:postalCode>
                        <gco:CharacterString>
                          <xsl:value-of select="$user/record/primaryaddress/zip"/>
                        </gco:CharacterString>
                      </cit:postalCode>
                    </xsl:if>
                    <xsl:if test="$user/record/primaryaddress/country != ''">
                      <cit:country>
                        <gco:CharacterString>
                          <xsl:value-of select="$user/record/primaryaddress/country"/>
                        </gco:CharacterString>
                      </cit:country>
                    </xsl:if>
                    <cit:electronicMailAddress>
                      <gco:CharacterString>
                        <xsl:value-of select="$user/record/email"/>
                      </gco:CharacterString>
                    </cit:electronicMailAddress>
                  </cit:CI_Address>
                </cit:address>
              </cit:CI_Contact>
            </cit:contactInfo>
            <cit:individual>
              <cit:CI_Individual>
                <cit:name>
                  <gco:CharacterString>
                    <xsl:value-of select="concat($user/record/name, ' ', $user/record/surname)"/>
                  </gco:CharacterString>
                </cit:name>
              </cit:CI_Individual>
            </cit:individual>
          </cit:CI_Organisation>
        </cit:party>
      </cit:CI_Responsibility>
    </xsl:if>
  </xsl:function>

</xsl:stylesheet>
