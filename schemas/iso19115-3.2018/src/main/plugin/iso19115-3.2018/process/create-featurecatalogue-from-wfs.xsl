<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <xsl:param name="featureCatWfsUrl" select="''"/>
  <xsl:param name="featureCatWfsFeatureType" select="''"/>
  <xsl:param name="featureCatReplace" select="'0'"/>

  <!-- i18n information -->
  <xsl:variable name="add-featureCat-loc">
    <msg id="a" xml:lang="eng">Create feature catalogue from download service (WFS or ESRI:REST): </msg>
    <msg id="a" xml:lang="fre">Ajouter le catalogue d'attribut à partir du service de téléchargement (WFS ou ESRI:REST) : </msg>
  </xsl:variable>


  <xsl:variable name="featureCatIsReplacedBy"
                select="geonet:parseBoolean($featureCatReplace)"/>


  <xsl:template name="list-create-featurecatalogue-from-wfs">
    <suggestion process="create-featurecatalogue-from-wfs"/>
  </xsl:template>



  <!-- Analyze the metadata record and return available suggestion
      for that process -->
  <xsl:template name="analyze-create-featurecatalogue-from-wfs">
    <xsl:param name="root"/>


    <xsl:variable name="wfsServices"
                  select="$root[count(.//srv:SV_ServiceIdentification) = 0]//mdb:distributionInfo//mrd:onLine/*[contains(cit:protocol/*, 'WFS') or contains(cit:protocol/*, 'ESRI:REST')]"/>

    <xsl:variable name="id"
                  select="generate-id(.)"/>

    <xsl:for-each select="$wfsServices">
      <xsl:variable name="url" select="cit:linkage/*/text()"/>
      <xsl:variable name="featureType" select="cit:name/*/text()"/>
      <xsl:variable name="protocol" select="cit:protocol/*/text()"/>
      <suggestion process="create-featurecatalogue-from-wfs"
                  id="{concat($id, '-', position())}"
                  category="contentinfo"
                  target="link#{$protocol}#{$url[1]}#{$featureType[1]}">
        <name><xsl:value-of select="geonet:i18n($add-featureCat-loc, 'a', $guiLang)"/><xsl:value-of select="concat($url[1], '#', $featureType[1])"/></name>
        <operational>true</operational>
        <params>{"featureCatWfsUrl":{"type":"text", "defaultValue":"<xsl:value-of select="$url[1]"/>"},
          "featureCatWfsFeatureType":{"type":"text", "defaultValue":"<xsl:value-of select="$featureType[1]"/>"},
          "featureCatReplace":{"type":"boolean", "defaultValue":"<xsl:value-of select="$featureCatReplace"/>"}}</params>
      </suggestion>
    </xsl:for-each>
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
                mdb:metadataScope|
                mdb:dateInfo|
                mdb:metadataStandard|
                mdb:metadataProfile|
                mdb:alternativeMetadataReference|
                mdb:otherLocale|
                mdb:metadataLinkage|
                mdb:spatialRepresentationInfo|
                mdb:referenceSystemInfo|
                mdb:metadataExtensionInfo|
                mdb:identificationInfo"/>

      <xsl:copy-of select="geonet:make-iso19115-3.2018-featurecatalogue-from-wfs($featureCatWfsUrl)"/>

      <xsl:if test="not($featureCatIsReplacedBy)">
        <xsl:apply-templates
                select="mdb:contentInfo"/>
      </xsl:if>

      <xsl:apply-templates
              select="
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



  <!-- eg.
   * Get WFS DescribeFeatureType for WFS
   http://visi-sextant.ifremer.fr/cgi-bin/sextant/wfs/bgmb?SERVICE=WFS&VERSION=1.0.0&REQUEST=DescribeFeatureType&TYPENAME=SISMER_mesures
   * Feature type description for ESRI:REST
   https://geoservices.wallonie.be/arcgis/rest/services/INDUSTRIES_SERVICES/ESRS/MapServer/0?f=json
   -->
  <xsl:function name="geonet:make-iso19115-3.2018-featurecatalogue-from-wfs" as="node()?">
    <xsl:param name="featureCatWfsUrl" as="xs:string"/>

    <xsl:variable name="isEsri"
                  select="contains($featureCatWfsUrl, '/rest/services/')"/>
    <xsl:variable name="featureTypeNameNoNamespace"
                  select="if (contains($featureCatWfsFeatureType, ':')) then tokenize($featureCatWfsFeatureType, ':')[2] else $featureCatWfsFeatureType"/>

    <xsl:variable name="sep"
                  select="if (contains($featureCatWfsUrl, '?')) then '&amp;' else '?'"/>
    <!-- TODO: Improve which layer to analyze
    By default collect the first layer if not set-->
    <xsl:variable name="url"
                  select="if ($isEsri)
                          then concat($featureCatWfsUrl, if (ends-with($featureCatWfsUrl, '/MapServer')) then '/0' else '', '?f=json')
                          else concat($featureCatWfsUrl, $sep, 'SERVICE=WFS&amp;VERSION=1.0.0&amp;REQUEST=DescribeFeatureType&amp;TYPENAME=', $featureCatWfsFeatureType)"/>
    <xsl:variable name="describeFeatureType"
                  select="if ($isEsri)
                          then util:downloadJsonAsXML($url)
                          else document($url)"/>

    <xsl:choose>
      <xsl:when test="$describeFeatureType">
        <mdb:contentInfo>
          <mrc:MD_FeatureCatalogue>
            <mrc:featureCatalogue>
              <gfc:FC_FeatureCatalogue>
                <gfc:producer/>
                <xsl:choose>
                  <xsl:when test="$isEsri">
                    <gfc:featureType>
                      <gfc:FC_FeatureType>
                        <gfc:typeName><xsl:value-of select="$describeFeatureType/root/name"/> </gfc:typeName>
                        <gfc:isAbstract>
                          <gco:Boolean>false</gco:Boolean>
                        </gfc:isAbstract>
                        <xsl:for-each select="$describeFeatureType/root/fields">
                          <gfc:carrierOfCharacteristics>
                            <gfc:FC_FeatureAttribute>
                              <gfc:memberName><xsl:value-of select="alias"/></gfc:memberName>
                              <gfc:definition>
                                <gco:CharacterString></gco:CharacterString>
                              </gfc:definition>
                              <gfc:cardinality>
                                <gco:CharacterString>
                                  <xsl:value-of select="if (type = 'esriFieldTypeOID') then '1..1' else '0..1'"/>
                                </gco:CharacterString>
                              </gfc:cardinality>
                              <gfc:code>
                                <gco:CharacterString><xsl:value-of select="name"/></gco:CharacterString>
                              </gfc:code>
                              <gfc:valueType>
                                <gco:TypeName>
                                  <gco:aName>
                                    <gco:CharacterString>
                                      <xsl:value-of select="replace(type, 'esriFieldType', '')"/>
                                      <xsl:if test="length">
                                        <xsl:value-of select="concat(' (', length, ')')"/>
                                      </xsl:if>
                                    </gco:CharacterString>
                                  </gco:aName>
                                </gco:TypeName>
                              </gfc:valueType>
                            </gfc:FC_FeatureAttribute>
                          </gfc:carrierOfCharacteristics>

                          <!-- TODO: may contains domain information with list of value. -->
                        </xsl:for-each>
                        <gfc:featureCatalogue/>
                      </gfc:FC_FeatureType>
                    </gfc:featureType>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:for-each select="$describeFeatureType//xs:complexType[ends-with(@name, $featureTypeNameNoNamespace) or @name = concat($featureTypeNameNoNamespace, 'Type')]">
                      <gfc:featureType>
                        <gfc:FC_FeatureType>
                          <gfc:typeName><xsl:value-of select="$featureCatWfsFeatureType"/> </gfc:typeName>
                          <gfc:isAbstract>
                            <gco:Boolean>false</gco:Boolean>
                          </gfc:isAbstract>
                          <xsl:for-each select=".//xs:element">
                            <gfc:carrierOfCharacteristics>
                              <gfc:FC_FeatureAttribute>
                                <gfc:memberName></gfc:memberName>
                                <gfc:definition>
                                  <gco:CharacterString></gco:CharacterString>
                                </gfc:definition>
                                <gfc:cardinality>
                                  <gco:CharacterString>
                                    <xsl:value-of select="if (@minOccurs) then @minOccurs else '1'"/>..<xsl:value-of select="if (@maxOccurs) then @maxOccurs else '1'"/>
                                  </gco:CharacterString>
                                </gfc:cardinality>
                                <gfc:code>
                                  <gco:CharacterString><xsl:value-of select="@name"/></gco:CharacterString>
                                </gfc:code>
                                <gfc:valueType>
                                  <gco:TypeName>
                                    <gco:aName>
                                      <gco:CharacterString>
                                        <xsl:value-of select="@type"/>
                                      </gco:CharacterString>
                                    </gco:aName>
                                  </gco:TypeName>
                                </gfc:valueType>
                              </gfc:FC_FeatureAttribute>
                            </gfc:carrierOfCharacteristics>
                          </xsl:for-each>
                          <gfc:featureCatalogue/>
                        </gfc:FC_FeatureType>
                      </gfc:featureType>
                    </xsl:for-each>
                  </xsl:otherwise>
                </xsl:choose>

              </gfc:FC_FeatureCatalogue>
            </mrc:featureCatalogue>
          </mrc:MD_FeatureCatalogue>
        </mdb:contentInfo>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>Failed to retrieve DescribeFeatureType document using: <xsl:value-of select="$url"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>

</xsl:stylesheet>
