<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gsr="http://www.isotc211.org/2005/gsr"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:srvold="http://www.isotc211.org/2005/srv"
                xmlns:gml30="http://www.opengis.net/gml"
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
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                exclude-result-prefixes="#all">

  <xsl:import href="../utility/multiLingualCharacterStrings.xsl"/>

  <xsl:template match="srvold:optionality" mode="from19139to19115-3.2018">
    <srv:optionality>
      <gco:Boolean>
        <xsl:value-of select="gcoold:CharacterString"/>
      </gco:Boolean>
    </srv:optionality>
  </xsl:template>


  <xsl:template match="srvold:containsOperations" mode="from19139to19115-3.2018">
    <srv:containsOperations>
      <srv:SV_OperationMetadata>
        <xsl:call-template name="writeCharacterStringElement">
          <xsl:with-param name="elementName" select="'srv:operationName'"/>
          <xsl:with-param name="nodeWithStringToWrite" select="srvold:SV_OperationMetadata/srvold:operationName"/>
        </xsl:call-template>
        <xsl:choose>
          <xsl:when test="srvold:SV_OperationMetadata/srvold:DCP/srvold:DCPList">
            <xsl:call-template name="writeCodelistElement">
              <xsl:with-param name="elementName" select="'srv:distributedComputingPlatform'"/>
              <xsl:with-param name="codeListName" select="'srv:DCPList'"/>
              <xsl:with-param name="codeListValue" select="srvold:SV_OperationMetadata/srvold:DCP/
                                        srvold:DCPList/@codeListValue"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <srv:distributedComputingPlatform gco:nilReason="missing"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="writeCharacterStringElement">
          <xsl:with-param name="elementName" select="'srv:invocationName'"/>
          <xsl:with-param name="nodeWithStringToWrite" select="srvold:SV_OperationMetadata/srvold:invocationName"/>
        </xsl:call-template>
        <xsl:apply-templates select="srvold:SV_OperationMetadata/srvold:connectPoint" mode="from19139to19115-3.2018"/>
        <xsl:apply-templates select="srvold:SV_OperationMetadata/srvold:parameters" mode="from19139to19115-3.2018"/>
      </srv:SV_OperationMetadata>
    </srv:containsOperations>
  </xsl:template>

  <xsl:template match="srvold:parameters" mode="from19139to19115-3.2018">
    <srv:parameter>
      <xsl:apply-templates select="srvold:SV_Parameter" mode="from19139to19115-3.2018"/>
    </srv:parameter>
  </xsl:template>

  <xsl:template match="srvold:SV_Parameter/srvold:name" mode="from19139to19115-3.2018">
    <xsl:element name="srv:name">
      <gco:MemberName>
        <xsl:apply-templates mode="from19139to19115-3.2018"/>
      </gco:MemberName>
    </xsl:element>
  </xsl:template>

  <xsl:template match="srvold:containsOperations/srvold:SV_OperationMetadata/srvold:parameters/srvold:SV_Parameter/srvold:valueType" priority="5" mode="from19139to19115-3.2018"/>

</xsl:stylesheet>
