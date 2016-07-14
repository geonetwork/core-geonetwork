<?xml version="1.0" encoding="UTF-8"?>
<!--
  Main XSL for creating an editor form.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0"
                exclude-result-prefixes="geonet saxon" extension-element-prefixes="saxon">

  <xsl:include href="../common.xsl"/>


  <xsl:template match="/">
    <html>
      <body>
        <xsl:call-template name="content"/>
      </body>
    </html>
  </xsl:template>

  <!--
  Form content
  -->
  <xsl:template name="content">
    <xsl:for-each select="$metadata">


      <div class="metadata {$currTab}">
        <form id="editForm" name="mainForm" accept-charset="UTF-8" method="POST"
              action="{/root/gui/locService}/metadata.update">
          <input type="hidden" id="schema" value="{geonet:info/schema}"/>
          <input type="hidden" id="template" name="template" value="{geonet:info/isTemplate}"/>
          <input type="hidden" id="uuid" value="{geonet:info/uuid}"/>
          <input type="hidden" name="id" value="{geonet:info/id}"/>
          <!-- TODO : add groupOwner -->
          <input type="hidden" name="type" value="{geonet:info/id}"/>
          <!-- FIXME -->
          <input type="hidden" id="version" name="version" value="{geonet:info/version}"/>
          <input type="hidden" id="currTab" name="currTab" value="{/root/gui/currTab}"/>
          <input type="hidden" name="editTab" value="true"/>
          <input type="hidden" id="minor" name="minor">
            <xsl:attribute name="value">
              <xsl:value-of select="java:encodeForJavaScript(/root/request/minor)"/>
            </xsl:attribute>
          </input>
          <input type="hidden" name="ref"/>
          <input type="hidden" name="name"/>
          <input type="hidden" name="licenseurl"/>
          <input type="hidden" name="type"/>
          <input type="hidden" name="editing" value="{geonet:info/id}"/>
          <input type="hidden" name="child"/>
          <input type="hidden" name="fname"/>
          <input type="hidden" name="access"/>
          <xsl:if test="//JUSTCREATED">
            <input id="just-created" type="hidden" name="just-created" value="true"/>
          </xsl:if>
          <input type="hidden" name="position" value="-1"/>
          <!-- showvalidationerrors is only set to true when 'Check' is
                   pressed - default is false -->
          <input type="hidden" name="showvalidationerrors">
            <xsl:attribute name="value">
              <xsl:value-of select="java:encodeForJavaScript(/root/request/showvalidationerrors)"/>
            </xsl:attribute>
          </input>

          <xsl:apply-templates mode="schema-hidden-fields" select="."/>

          <!-- Hidden div to contains extra elements like when posting
          multiple keywords, CRS, .... -->
          <div id="hiddenFormElements" style="display:none;"/>

          <!-- Tabs -->
          <xsl:call-template name="tab">
            <xsl:with-param name="tabLink"
                            select="concat(/root/gui/locService,'/metadata.update.new')"/>
            <xsl:with-param name="schema" select="geonet:info/schema"/>
          </xsl:call-template>

          <xsl:choose>
            <xsl:when test="$currTab='xml'">
              <xsl:apply-templates mode="xmlDocument" select=".">
                <xsl:with-param name="edit" select="true()"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
              <table class="gn">
                <tbody>
                  <xsl:apply-templates mode="elementEP" select=".">
                    <xsl:with-param name="edit" select="true()"/>
                  </xsl:apply-templates>
                </tbody>
              </table>
            </xsl:otherwise>
          </xsl:choose>
        </form>
      </div>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="schema-hidden-fields" match="*"/>

</xsl:stylesheet>
