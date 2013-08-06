<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:import href="../base-layout.xsl"/>
  
  <xsl:template mode="content" match="/">
    <div class="container-fluid">
      <div class="row-fluid">
        <div class="span12">
          <form action="catalog.search.nojs">
            <fieldset>
              <div class="input-append">
                <input type="hidden" name="fast" value="index"/>
                <input type="text" name="any"
                  class="span12 input-large gn-search-text"/>
                <button class="btn btn-primary" type="submit"><xsl:value-of select="$i18n/search"/></button>
              </div>
            </fieldset>
          </form>
          
          <xsl:if test="/root/request/*">
            <xsl:for-each select="/root/response/metadata">
              <li><h2><xsl:value-of select="title"/></h2>
                <xsl:value-of select="abstract"/>
              </li>
            </xsl:for-each>
          </xsl:if>
        </div>
      </div>
    </div>
  </xsl:template>
  
</xsl:stylesheet>
