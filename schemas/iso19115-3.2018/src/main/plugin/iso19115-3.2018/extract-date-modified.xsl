<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="2.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema">

  <xsl:variable name="dateFormat"
                as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][ZN]'"/>

  <xsl:template
    match="mdb:MD_Metadata">
    <dateStamp>
      <xsl:value-of select="(
        mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'revision']/cit:date/*[. != ''],
        mdb:dateInfo/*[cit:dateType/*/@codeListValue = 'creation']/cit:date/*[. != ''],
        format-dateTime(current-dateTime(), $dateFormat)
      )[1]"/>
    </dateStamp>
  </xsl:template>
</xsl:stylesheet>
