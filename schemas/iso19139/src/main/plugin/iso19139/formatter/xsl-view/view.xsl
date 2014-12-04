<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="2.0">

  <xsl:include href="sharedFormatterDir/functions.xsl"/>

  <xsl:variable name="configuration" as="element()">
    <configuration>
      <view>
        <field xpath="/root/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:title"/>
      </view>
    </configuration>
  </xsl:variable>


  <xsl:template match="/">

    <xsl:variable name="metadata" select="/root/gmd:MD_Metadata"/>

    <!-- Match a service or data set identification section -->
    <xsl:variable name="identification" select="/root/gmd:MD_Metadata/gmd:identificationInfo/*"/>

    <div>
      <link href="http://getbootstrap.com/dist/css/bootstrap.min.css" rel="stylesheet"/>
      <xsl:call-template name="header"/>
      <div class="row">
        <div class="col-xs-4">
          <img src=""/>
        </div>
        <div class="col-xs-8">
          <h1><xsl:value-of select="$identification/gmd:citation/*/gmd:title"/></h1>
          <p>
            <xsl:value-of select="$identification/gmd:abstract"/>
          </p>
        </div>
      </div>


      <table class="table">
        <xsl:for-each select="$identification/gmd:descriptiveKeywords">
          <xsl:variable name="thesaurusName" select="gmd:thesaurusName/*/gmd:title"/>
          <tr>
            <th><xsl:value-of select="if ($thesaurusName != '') then $thesaurusName else 'MOTS-CLEFS'"/></th>
            <td><xsl:value-of select="string-join(*/gmd:keyword, ', ')"/></td>
          </tr>
        </xsl:for-each>
        <tr>
          <th>LICENCE</th>
          <td><xsl:value-of select="$identification/gmd:resourceConstraints/*/gmd:accessConstraints/*/@codeListValue"/></td>
        </tr>
        <xsl:for-each select="$identification/gmd:pointOfContact/*">
          <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
          <tr>
            <th><xsl:value-of select="if ($role != '') then $role else 'GESTIONNAIRE'"/></th>
            <td>
              <xsl:value-of select="gmd:organisationName"/>
            </td>
          </tr>
        </xsl:for-each>
        <tr>
          <th>FORMAT</th>
          <td>

          </td>
        </tr>
        <xsl:for-each select="$identification/gmd:citation/*/gmd:date/*">
          <tr>
            <th><xsl:value-of select="gmd:dateType/*/@codeListValue"/></th>
            <td><span data-ng-humanize-time=""><xsl:value-of select="gmd:date"/></span></td>
          </tr>
        </xsl:for-each>
        <tr>
          <th>FRÉQUENCE DE MISE À JOUR</th>
          <td></td>
        </tr>
      </table>


      <div>
        <h2>LISTE DES FICHES DISPONIBLES</h2>
        <!-- TODO: Get relation -->
      </div>

      <ul class="nav nav-pills">
        <li class="active"><a href="#">La carte</a></li>
        <li><a href="#">La donnée</a></li>
        <li><a href="#">Les informations techniques</a></li>
        <li><a href="#">Les contacts</a></li>
      </ul>

      <div>
        <h2>La carte</h2>
      </div>

      <div>
        <h2>La données</h2>
      </div>

      <div>
        <h2>Les informations techniques</h2>
      </div>

      <div>
        <h2>Les contacts</h2>

        <table class="table">
          <xsl:for-each select="$metadata/gmd:contact/*">
            <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
            <tr>
              <th><xsl:value-of select="if ($role != '') then $role else 'GESTIONNAIRE'"/></th>
              <td>
                <xsl:value-of select="gmd:organisationName"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>