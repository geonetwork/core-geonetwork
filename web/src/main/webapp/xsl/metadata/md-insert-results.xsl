<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>
  
  <xsl:template match="/">
    {
     "success":true, 
     "msg": "<xsl:value-of select="/root/gui/strings/fileUploadSuccessful"/>",
     "id" : "<xsl:value-of select="/root/response/id"/>",
     "records" : "<xsl:value-of select="/root/response/records"/>"
    }
  </xsl:template>
</xsl:stylesheet>
