<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
>

  <xsl:import href="modal.xsl"/>

  <!--
    page content
    -->
  <xsl:template name="content">
    <xsl:call-template name="formLayout">
      <xsl:with-param name="title" select="/root/gui/strings/downloadlist"/>
      <xsl:with-param name="content">

        <div id="downloadlist">
          <table align="center">
            <tr>
              <th class="padded">
                <xsl:value-of select="/root/gui/strings/type"/>
              </th>
              <th class="padded">
                <xsl:value-of select="/root/gui/strings/downloadSelect"/>
              </th>
              <th class="padded">
                <xsl:value-of select="/root/gui/strings/descriptionTab"/>
              </th>
              <th class="padded" align="right">
                <xsl:value-of select="/root/gui/strings/sizeBytes"/>
              </th>
              <th class="padded">
                <xsl:value-of select="/root/gui/strings/dateModified"/>
              </th>
            </tr>

            <tr width="100%">
              <td class="dots" align="center" colspan="6">
              </td>
            </tr>

            <xsl:variable name="lang" select="/root/gui/language"/>

            <!-- loop on all download links first -->

            <xsl:for-each select="/root/response/link[@download]">
              <xsl:sort select="@title"/>
              <tr>
                <td class="padded" align="left">
                  <xsl:choose>
                    <xsl:when test="@local='true'">
                      <xsl:value-of select="/root/gui/strings/localType"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="/root/gui/strings/remoteType"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <!-- download select box -->
                <td class="padded" align="center">
                  <xsl:choose>
                    <xsl:when test="@local='true' and @found='true'">
                      <input name="{@name}" type="checkbox"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:choose>
                        <xsl:when test="@local='true' and @found='false'">
                          <xsl:value-of select="/root/gui/strings/notfound"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="'-'"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <!-- description or file name -->
                <td class="padded" align="left">
                  <xsl:choose>
                    <xsl:when test="@local='true'">
                      <xsl:choose>
                        <xsl:when test="normalize-space(@title)!=''">
                          <xsl:value-of select="@title"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="@name"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                      <a href="{@href}" target="_blank">
                        <xsl:value-of select="@href"/>
                      </a>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <!-- size -->
                <td class="padded" align="right">
                  <xsl:choose>
                    <xsl:when test="@local='true' and @found='true'">
                      <xsl:value-of select="@size"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="'-'"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <!-- date modified -->
                <td class="padded" align="left">
                  <xsl:choose>
                    <xsl:when test="@local='true' and @found='true'">
                      <xsl:value-of select="@datemodified"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="'-'"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
              </tr>
            </xsl:for-each>

            <!-- now process links to other web pages -->

            <xsl:for-each select="/root/response/link[@weblink]">
              <xsl:sort select="@href"/>
              <tr>
                <!-- download select box is - -->
                <td class="padded" align="left">
                  <xsl:value-of select="/root/gui/strings/link"/>
                </td>
                <td class="padded" align="center">
                  <xsl:value-of select="'-'"/>
                </td>
                <!-- description -->
                <td class="padded" align="left" colspan="3">
                  <xsl:choose>
                    <xsl:when test="normalize-space(@title)!=''">
                      <a href="{@href}" target="_blank">
                        <xsl:value-of select="@title"/>
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                      <a href="{@href}" target="_blank">
                        <xsl:value-of select="@href"/>
                      </a>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
              </tr>
            </xsl:for-each>

            <tr width="100%">
              <td align="center" colspan="6">
                &#160;
              </td>
            </tr>

            <tr width="100%">
              <td align="center" colspan="6">
                <button class="content" onclick="doDownload('{/root/response/id}')">
                  <xsl:value-of select="/root/gui/strings/downloadSelected"/>
                </button>
                &#160;
                <button class="content" onclick="doDownload('{/root/response/id}', 'all')">
                  <xsl:value-of select="/root/gui/strings/downloadThemAll"/>
                </button>
              </td>
            </tr>

          </table>
        </div>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
