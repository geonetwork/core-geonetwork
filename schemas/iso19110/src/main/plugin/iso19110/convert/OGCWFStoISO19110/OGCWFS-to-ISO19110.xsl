<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:gfc="http://www.isotc211.org/2005/gfc"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema"
                xmlns:date="http://exslt.org/dates-and-times"
                version="2.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                exclude-result-prefixes="date geonet">

  <!-- ============================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <!-- ============================================================================= -->

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template match="xsi:schema">
    <gfc:FC_FeatureCatalogue>
      <gfc:name>
        <gco:CharacterString>
          <xsl:value-of select="xsi:complexType/@name"/>
        </gco:CharacterString>
      </gfc:name>
      <gfc:scope>
        <gco:CharacterString></gco:CharacterString>
      </gfc:scope>
      <gfc:fieldOfApplication>
        <gco:CharacterString/>
      </gfc:fieldOfApplication>
      <gfc:versionNumber>
        <gco:CharacterString>
          <xsl:value-of select="@version"/>
        </gco:CharacterString>
      </gfc:versionNumber>

      <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
      <gfc:versionDate>
        <gco:DateTime>
          <!--
                    <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
-->
        </gco:DateTime>
      </gfc:versionDate>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!-- Create an empty producer as this is recommended
            but not available in the DescribeFeatureType operation.
            TODO : use the GetCapabilities ?
            -->
      <gfc:producer>
        <gmd:CI_ResponsibleParty>
          <gmd:individualName>
            <gco:CharacterString></gco:CharacterString>
          </gmd:individualName>
          <gmd:organisationName>
            <gco:CharacterString/>
          </gmd:organisationName>
          <gmd:positionName>
            <gco:CharacterString/>
          </gmd:positionName>
          <gmd:role>
            <gmd:CI_RoleCode codeListValue="pointOfContact" codeList="CI_RoleCode"/>
          </gmd:role>
        </gmd:CI_ResponsibleParty>
      </gfc:producer>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:apply-templates select="xsi:element"/>

    </gfc:FC_FeatureCatalogue>
  </xsl:template>

  <xsl:template match="xsi:element">
    <gfc:featureType>
      <gfc:FC_FeatureType>
        <gfc:typeName>
          <gco:LocalName>
            <xsl:value-of select="@type"/>
          </gco:LocalName>
        </gfc:typeName>
        <gfc:definition>
          <gco:CharacterString/>
        </gfc:definition>
        <gfc:isAbstract>
          <gco:Boolean>false</gco:Boolean>
        </gfc:isAbstract>
        <gfc:aliases>
          <gco:LocalName>
            <xsl:value-of select="@name"/>
          </gco:LocalName>
        </gfc:aliases>
        <gfc:featureCatalogue/>

        <xsl:variable name="typeName" select="@name"/>
        <xsl:apply-templates select="//xsi:complexType[@name=concat($typeName, 'Type')]"/>


        <gfc:constrainedBy>
          <gfc:FC_Constraint>
            <gfc:description>
              <gco:CharacterString>
                Namespace: <xsl:value-of select="/xsi:schema/@targetNamespace"/>.
              </gco:CharacterString>
            </gfc:description>
          </gfc:FC_Constraint>
        </gfc:constrainedBy>
      </gfc:FC_FeatureType>
    </gfc:featureType>
  </xsl:template>

  <xsl:template match="xsi:complexType|xsi:complexContent|xsi:extension">
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="xsi:sequence/xsi:element">
    <gfc:carrierOfCharacteristics>
      <gfc:FC_FeatureAttribute>
        <gfc:memberName>
          <gco:LocalName>
            <xsl:value-of select="@name"/>
          </gco:LocalName>
        </gfc:memberName>
        <gfc:definition>
          <gco:CharacterString/>
        </gfc:definition>
        <gfc:cardinality>
          <gco:Multiplicity>
            <gco:range>
              <gco:MultiplicityRange>
                <gco:lower>
                  <gco:Integer>
                    <xsl:choose>
                      <xsl:when test="@minOccurs">
                        <xsl:value-of select="@minOccurs"/>
                      </xsl:when>
                      <xsl:otherwise>0</xsl:otherwise>
                    </xsl:choose>
                  </gco:Integer>
                </gco:lower>
                <gco:upper>
                  <xsl:choose>
                    <xsl:when test="@maxOccurs">
                      <gco:UnlimitedInteger isInfinite="false">
                        <xsl:value-of select="@maxOccurs"/>
                      </gco:UnlimitedInteger>
                    </xsl:when>
                    <xsl:otherwise>
                      <gco:UnlimitedInteger isInfinite="true" xsi:nil="true"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </gco:upper>
              </gco:MultiplicityRange>
            </gco:range>
          </gco:Multiplicity>
        </gfc:cardinality>
        <gfc:featureType/>
        <!-- TODO codelist values using a GetFeature. -->
        <gfc:valueType>
          <gco:TypeName>
            <gco:aName>
              <gco:CharacterString>
                <xsl:value-of select="@type"></xsl:value-of>
              </gco:CharacterString>
            </gco:aName>
          </gco:TypeName>
        </gfc:valueType>
      </gfc:FC_FeatureAttribute>
    </gfc:carrierOfCharacteristics>
  </xsl:template>


  <xsl:template name="codelist">
    <gfc:listedValue>
      <gfc:FC_ListedValue>
        <gfc:label>
          <gco:CharacterString>Codelist value</gco:CharacterString>
        </gfc:label>
        <gfc:code>
          <gco:CharacterString>code</gco:CharacterString>
        </gfc:code>
        <gfc:definition>
          <gco:CharacterString/>
        </gfc:definition>
      </gfc:FC_ListedValue>
    </gfc:listedValue>
  </xsl:template>

</xsl:stylesheet>
