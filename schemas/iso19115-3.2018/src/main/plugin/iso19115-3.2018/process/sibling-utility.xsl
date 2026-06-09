<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all" version="2.0">

  <xsl:template name="make-aggregate">
    <xsl:param name="uuid"/>
    <xsl:param name="context"/>
    <xsl:param name="associationType" select="'crossReference'" required="no"/>
    <xsl:param name="initiativeType" select="''" required="no"/>
    <xsl:param name="title" select="''" required="no"/>
    <xsl:param name="remoteUrl" select="''" required="no"/>
    <xsl:param name="nodeUrl" select="util:getSettingValue('nodeUrl')" required="no"/>

    <xsl:variable name="notExist" select="count($context/mri:associatedResource/mri:MD_AssociatedResource[
			mri:metadataReference/@uuidref = $uuid
			and mri:associationType/mri:DS_AssociationTypeCode/@codeListValue = $associationType
			and mri:initiativeType/mri:DS_InitiativeTypeCode/@codeListValue = $initiativeType
			]) = 0"/>
    <xsl:if test="$notExist">

      <mri:associatedResource>
        <mri:MD_AssociatedResource>
          <mri:associationType>
            <mri:DS_AssociationTypeCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_AssociationTypeCode"
                codeListValue="{$associationType}"/>
          </mri:associationType>

          <xsl:if test="$initiativeType != ''">
            <mri:initiativeType>
              <mri:DS_InitiativeTypeCode
                  codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_InitiativeTypeCode"
                  codeListValue="{$initiativeType}"/>
            </mri:initiativeType>
          </xsl:if>
          <mri:metadataReference uuidref="{$uuid}">
            <xsl:choose>
              <xsl:when test="$remoteUrl != ''">
                <xsl:attribute name="xlink:href" select="$remoteUrl"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="xlink:href"
                               select="concat($nodeUrl, 'api/records/', $uuid)"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$title != ''">
              <xsl:attribute name="xlink:title" select="$title"/>
            </xsl:if>
          </mri:metadataReference>
        </mri:MD_AssociatedResource>
      </mri:associatedResource>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
