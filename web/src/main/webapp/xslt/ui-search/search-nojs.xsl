<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- 
  Basic search interface which does not require JS.
  -->
  <xsl:import href="../base-layout.xsl"/>

  <xsl:template mode="content" match="/">
    <div class="row">
      <div class="col-lg-9">
        <form action="catalog.search.nojs">
          <fieldset>
            <input type="hidden" name="fast" value="index"/>
            <div class="form-group">
              <input type="text" name="any" class="form-control input-large gn-search-text" autofocus=""/>
            </div>
          </fieldset>
        </form>

        <xsl:if test="/root/request/*">
          <xsl:for-each select="/root/response/metadata">
            <li>
              <h2>
                <xsl:value-of select="title"/>
              </h2>
              <xsl:value-of select="abstract"/>
            </li>
          </xsl:for-each>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
