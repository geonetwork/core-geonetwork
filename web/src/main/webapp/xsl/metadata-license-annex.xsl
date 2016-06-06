<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0"
                exclude-result-prefixes="#all">

  <!-- runs on brief metadata and produces a license description - annex -->
  <!-- not localized - license is in english -->

  <xsl:output method='html' omit-xml-declaration="yes"
              doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
              doctype-system="http://www.w3.org/TR/html4/loose.dtd" indent="yes"/>

  <!-- TODO: Move to schema plugin -->
  <!--<xsl:include href="edit.xsl"/>-->

  <xsl:variable name="baseurl" select="//geonet:info/baseurl"/>
  <xsl:variable name="locserv" select="//geonet:info/locserv"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="doit" select="*"/>
  </xsl:template>

  <xsl:template mode="doit" match="*">
    <html>
      <head>
        <xsl:call-template name="myheader"/>
      </head>
      <body>
        <table width="100%">
          <!-- content -->
          <tr>
            <td>
              <xsl:call-template name="content"/>
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="myheader">
    <link href="{$baseurl}/images/logos/favicon.png" rel="shortcut icon" type="image/x-icon"/>
    <link href="{$baseurl}/images/logos/favicon.png" rel="icon" type="image/x-icon"/>

    <!-- stylesheet -->
    <link rel="stylesheet" type="text/css" href="{$baseurl}/geonetwork.css"/>
    <link rel="stylesheet" type="text/css" href="{$baseurl}/modalbox.css"/>
  </xsl:template>


  <!-- page content -->
  <xsl:template name="content">
    <xsl:apply-templates mode="metadata" select="/root/metadata"/>
  </xsl:template>

  <xsl:template mode="metadata" match="*">
    <div align="center">
      <xsl:choose>
        <xsl:when test="datacommons">
          <h1>Data Commons Data License for:
            <xsl:value-of select="title"/>
          </h1>
          <h2 align="center">Download date:
            <xsl:value-of select="@currdate"/>
          </h2>
          <br/>
          <xsl:apply-templates mode="doCommons" select="datacommons"/>
        </xsl:when>
        <xsl:when test="creativecommons">
          <h1>Creative Commons Data License for:
            <xsl:value-of select="title"/>
          </h1>
          <h2 align="center">Download date:
            <xsl:value-of select="@currdate"/>
          </h2>
          <br/>
          <xsl:apply-templates mode="doCommons" select="creativecommons"/>
        </xsl:when>
        <xsl:otherwise>
          <h1 align="center">"LICENSED CONDITIONS OF USE agreement"</h1>
          <b align="center">Agreement to conditions of use at <xsl:value-of select="@currdate"/>,
            applies to the files listed below.
          </b>
          <br/>
          <br/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="fileInfo" select="/root/downloaded"/>
      <br/>
      <xsl:apply-templates mode="metadataInfo" select="."/>
      <br/>
      <xsl:apply-templates mode="doLicensee" select="."/>
      <br/>
      <!--
            <xsl:apply-templates mode="doLicensor" select="licensor|ipOwner|owner|principalInvestigator"/>
            <br/>
             -->
      <xsl:if test="SecurityConstraints or Constraints or LegalConstraints">
        <fieldset>
          <legend>
            <b>
              <xsl:value-of select="'Additional Constraints on use of the files'"/>
            </b>
          </legend>
          <xsl:if test="SecurityConstraints">
            <fieldset>
              <legend>
                <b>
                  <xsl:value-of select="'Security Constraints'"/>
                </b>
              </legend>
              <xsl:for-each select="SecurityConstraints">
                <xsl:apply-templates mode="doOtherConstraints" select="*"/>
              </xsl:for-each>
            </fieldset>
          </xsl:if>
          <br/>
          <xsl:if test="LegalConstraints">
            <fieldset>
              <legend>
                <b>
                  <xsl:value-of select="'Legal Constraints'"/>
                </b>
              </legend>
              <xsl:for-each select="LegalConstraints">
                <xsl:apply-templates mode="doOtherConstraints" select="*"/>
              </xsl:for-each>
            </fieldset>
          </xsl:if>
          <br/>
          <xsl:if test="Constraints">
            <fieldset>
              <legend>
                <b>
                  <xsl:value-of select="'General Constraints'"/>
                </b>
              </legend>
              <xsl:for-each select="Constraints">
                <xsl:apply-templates mode="doOtherConstraints" select="*"/>
              </xsl:for-each>
            </fieldset>
          </xsl:if>
        </fieldset>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template mode="doLicensor" match="*">
    <div align="center">
      <fieldset>
        <legend>
          <b>Licensor Information</b>
        </legend>
        <table width="100%">
          <tr>
            <td>
              <b>Name:</b>
              <i>
                <xsl:value-of
                  select="concat(individualName,' ',positionName,' ',organisationName)"/>
              </i>
              <br/>
              <b>Address:</b>
              <br/>
              <i>
                <xsl:value-of select="deliveryPoint"/>
                <br/>
                <xsl:value-of select="concat(city,' ',administrativeArea,' ',postalCode)"/>
                <br/>
                <xsl:value-of select="country"/>
                <br/>
              </i>
            </td>
            <td>
              <b>Organisation:</b>
              <i>
                <xsl:value-of select="organisationName"/>
              </i>
            </td>
            <td>
              <b>Email:</b>
              <i>
                <xsl:value-of select="electronicMailAddress"/>
              </i>
            </td>
            <td>
              <b>Role:</b>
              <i>
                <xsl:value-of select="name(.)"/>
              </i>
            </td>
          </tr>
        </table>
      </fieldset>
    </div>
    <br/>
  </xsl:template>

  <xsl:template mode="doLicensee" match="*">
    <xsl:if test="/root/entered">
      <div align="center">
        <fieldset>
          <legend>
            <b>Licensee Information</b>
          </legend>
          <table width="100%">
            <tr>
              <td colspan="3">
                <b>Information Provided:</b>
              </td>
            </tr>
            <tr>
              <td>
                <b>Name:</b>
                <i>
                  <xsl:value-of select="/root/entered/name"/>
                </i>
              </td>
              <td>
                <b>Organisation:</b>
                <i>
                  <xsl:value-of select="/root/entered/org"/>
                </i>
              </td>
              <td>
                <b>Email:</b>
                <i>
                  <xsl:value-of select="/root/entered/email"/>
                </i>
              </td>
            </tr>
            <xsl:if test="/root/userdetails/username">
              <tr>
                <td colspan="3">
                  <b>Logged in as</b>
                  <i>
                    <xsl:value-of select="/root/userdetails/username"/>
                  </i>
                </td>
              </tr>
              <tr>
                <td>
                  <b>Name:</b>
                  <i>
                    <xsl:value-of
                      select="concat(/root/userdetails/name,' ',/root/userdetails/surname)"/>
                  </i>
                  <br/>
                  <b>Address:</b>
                  <br/>
                  <i>
                    <xsl:value-of select="/root/userdetails/address"/>
                    <br/>
                    <xsl:value-of
                      select="concat(/root/userdetails/state,' ',/root/userdetails/zip)"/>
                    <br/>
                    <xsl:value-of select="/root/userdetails/country"/>
                    <br/>
                  </i>
                </td>
                <td>
                  <b>Organisation:</b>
                  <i>
                    <xsl:value-of select="/root/userdetails/organisation"/>
                  </i>
                </td>
                <td>
                  <b>Email:</b>
                  <i>
                    <xsl:value-of select="/root/userdetails/email"/>
                  </i>
                </td>
              </tr>
            </xsl:if>
          </table>
        </fieldset>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="metadataInfo" match="*">
    <div align="center">
      <fieldset>
        <legend>
          <b>Metadata Information</b>
        </legend>
        <table width="100%">
          <tr>
            <td>
              <b>Title in Metadata Dataset Identification is</b>
              <i>
                <xsl:value-of select="title"/>
              </i>
            </td>
            <td>
              <b>Last revision date is</b>
              <i>
                <xsl:value-of select="@changedate"/>
              </i>
            </td>
            <xsl:variable name="mdurl" select="link[@type='metadataurl']"/>
            <xsl:if test="normalize-space($mdurl)!=''">
              <td>
                <a href="{$mdurl}" target="_blank">Metadata URL in MEST</a>
              </td>
            </xsl:if>
          </tr>
        </table>
      </fieldset>
    </div>
  </xsl:template>


  <xsl:template mode="fileInfo" match="*">
    <div align="center">
      <fieldset>
        <legend>
          <b>Applies to these files</b>
        </legend>
        <table width="100%">
          <tr>
            <th width="33%" align="left">
              File name
            </th>
            <th width="33%" align="left">
              Size
            </th>
            <th width="33%" align="left">
              Date Modified
            </th>
          </tr>
          <tr>
            <td colspan="3" class="dots"/>
          </tr>
          <xsl:for-each select="file">
            <tr>
              <td width="33%">
                <xsl:value-of select="@name"/>
              </td>
              <td width="33%">
                <xsl:value-of select="@size"/>
              </td>
              <td width="33%">
                <xsl:value-of select="@datemodified"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </fieldset>
    </div>
  </xsl:template>


  <xsl:template mode="doCommons" match="*">
    <div align="center">
      <fieldset>
        <legend>
          <b>License:</b>
          <xsl:value-of select="licenseName"/>
        </legend>
        <table width="75%">
          <tr>
            <td width="33%">
              <a href="{jurisdictionLink}" target="_blank">Jurisdiction</a>
            </td>
            <td width="33%">
              <a href="{licenseLink}" target="_blank">
                <IMG align="middle" src="{imageLink}" longdesc="{licenseLink}"
                     alt="{licenseName}"></IMG>
              </a>
            </td>
            <td width="33%">
              <a href="{licenseLink}" target="_blank">Show license (opens a new window)</a>
            </td>
          </tr>
        </table>
      </fieldset>
      <br/>
      <xsl:if test="*[contains(name(.),'Constraints')]">
        <fieldset>
          <legend>
            <b>Additional constraints</b>
          </legend>
          <table width="100%">
            <xsl:for-each select="*[contains(name(.),'Constraints')]">
              <tr>
                <td width="15%" align="left">
                  <b>
                    <xsl:value-of select="name(.)"/>
                  </b>
                </td>
                <td align="left">
                  <xsl:call-template name="preformatted">
                    <xsl:with-param name="text" select="."/>
                  </xsl:call-template>
                </td>
              </tr>
            </xsl:for-each>
          </table>
        </fieldset>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template mode="doOtherConstraints" match="*">
    <div align="center">
      <table width="100%">
        <xsl:apply-templates mode="constraints" select="."/>
      </table>
    </div>
  </xsl:template>

  <!-- HACK - translate field names here until we can redesign process to use labels.xml -->

  <xsl:template mode="constraints" match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates mode="constraints" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:useLimitation']">
    <xsl:text>Use Limitation</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:accessConstraints']">
    <xsl:text>Access Constraints</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:useConstraints']">
    <xsl:text>Use Constraints</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:otherConstraints']">
    <xsl:text>Other Constraints</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:classification']">
    <xsl:text>Classification</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:userNote']">
    <xsl:text>User Note</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:classificationSystem']">
    <xsl:text>Classification System</xsl:text>
  </xsl:template>

  <xsl:template mode="constraints" match="text()[.='gmd:handlingDescription']">
    <xsl:text>Handling Description</xsl:text>
  </xsl:template>

  <!--
    translates CR-LF sequences into HTML newlines <p/>
    -->
  <xsl:template name="preformatted">
    <xsl:param name="text"/>

    <xsl:choose>
      <xsl:when test="contains($text,'&#13;&#10;')">
        <xsl:value-of select="substring-before($text,'&#13;&#10;')"/>
        <br/>
        <xsl:call-template name="preformatted">
          <xsl:with-param name="text" select="substring-after($text,'&#13;&#10;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($text,'&#13;')">
        <xsl:value-of select="substring-before($text,'&#13;')"/>
        <br/>
        <xsl:call-template name="preformatted">
          <xsl:with-param name="text" select="substring-after($text,'&#13;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($text,'&#10;')">
        <xsl:value-of select="substring-before($text,'&#10;')"/>
        <br/>
        <xsl:call-template name="preformatted">
          <xsl:with-param name="text" select="substring-after($text,'&#10;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
