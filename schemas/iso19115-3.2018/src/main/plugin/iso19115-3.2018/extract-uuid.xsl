<?xml version="1.0" encoding="UTF-8"?>
<!-- FIXME: Which MD_Identifier do we select under mds:metadataIdentifier
  probably should use some form of match on a particular codeSpace?
-->
<xsl:stylesheet version="2.0" xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="mdb:MD_Metadata">
    <uuid>
      <xsl:value-of
        select="mdb:metadataIdentifier[position() = 1]/mcc:MD_Identifier
              /mcc:code/gco:CharacterString"
      />
    </uuid>
  </xsl:template>
</xsl:stylesheet>
