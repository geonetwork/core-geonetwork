<?xml version="1.0" encoding="UTF-8" ?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:template match="/root">

    <xsl:variable name="op" select="/root/response/myop"/>
    <xsl:choose>

      <xsl:when test="$op='scan'">

        <zs:scanResponse xmlns:zs="http://www.loc.gov/zing/srw/">
          <zs:version>1.1</zs:version>

          <xsl:if test="response/diagnostics">
            <zs:diagnostics xmlns="http://www.loc.gov/zing/srw/diagnostic/">
              <xsl:for-each select="response/diagnostics/diagnostic">
                <diagnostic>
                  <uri>
                    <xsl:value-of select="uri"/>
                  </uri>
                  <message>
                    <xsl:value-of select="message"/>
                  </message>
                  <details>
                    <xsl:value-of select="details"/>
                  </details>
                </diagnostic>

              </xsl:for-each>
            </zs:diagnostics>
          </xsl:if>
        </zs:scanResponse>

      </xsl:when>


      <xsl:when test="$op='searchretrieve'">

        <xsl:variable name="numrec" select="response/numrec"/>
        <xsl:variable name="idle" select="response/idle"/>
        <xsl:variable name="id" select="response/id"/>

        <zs:searchRetrieveResponse xmlns:zs="http://www.loc.gov/zing/srw/">
          <zs:version>1.1</zs:version>
          <zs:numberOfRecords>
            <xsl:value-of select="$numrec"/>
          </zs:numberOfRecords>

          <xsl:if test="response/sruresponse/record/*">
            <zs:resultSetId>
              <xsl:value-of select="$id"/>
            </zs:resultSetId>
            <zs:resultSetIdleTime>
              <xsl:value-of select="$idle"/>
            </zs:resultSetIdleTime>

            <zs:records>
              <xsl:for-each select="response/sruresponse/record/*">
                <zs:record>

                  <zs:recordSchema>
                    <xsl:value-of select="./*[local-name()='info']/schema"/>
                  </zs:recordSchema>
                  <zs:recordPacking>XML</zs:recordPacking>
                  <zs:recordData>
                    <xsl:call-template name="copyrecord"></xsl:call-template>
                  </zs:recordData>
                  <zs:recordPosition>
                    <xsl:value-of select="../@recordPosition"/>
                  </zs:recordPosition>
                </zs:record>
              </xsl:for-each>
            </zs:records>
          </xsl:if>
          <xsl:if test="response/diagnostics">
            <zs:diagnostics xmlns="http://www.loc.gov/zing/srw/diagnostic/">
              <xsl:for-each select="response/diagnostics/diagnostic">
                <diagnostic>
                  <uri>
                    <xsl:value-of select="uri"/>
                  </uri>
                  <message>
                    <xsl:value-of select="message"/>
                  </message>
                  <details>
                    <xsl:value-of select="details"/>
                  </details>
                </diagnostic>

              </xsl:for-each>
            </zs:diagnostics>
          </xsl:if>
        </zs:searchRetrieveResponse>

      </xsl:when>


      <xsl:when test="$op='explain2'">


        <xsl:for-each select="gui">
          <xsl:call-template name="copyrecord"/>
        </xsl:for-each>


      </xsl:when>

      <xsl:when test="$op='explain'">

        <xsl:comment>
          <xsl:variable name="port" select="gui/env/server/port"/>
        </xsl:comment>
        <xsl:variable name="port" select="80"/>
        <xsl:variable name="server" select="'85.10.219.212'"/>
        <xsl:comment>
          <xsl:variable name="server" select="gui/env/server/host"/>
          <xsl:variable name="server" select="response/@servername"/>
        </xsl:comment>

        <zs:explainResponse xmlns:zs="http://www.loc.gov/zing/srw/">
          <zs:version>1.1</zs:version>
          <zs:record>
            <zs:recordSchema>http://explain.z3950.org/dtd/2.0/</zs:recordSchema>
            <zs:recordPacking>XML</zs:recordPacking>
            <zs:recordData>
              <explain xmlns="http://explain.z3950.org/dtd/2.0/">
                <serverInfo protocol="SRU" version="1.2" transport="http" method="GET POST">
                  <host>
                    <xsl:value-of select="$server"/>
                  </host>
                  <port>
                    <xsl:value-of select="$port"/>
                  </port>
                  <database>
                    <xsl:value-of select="response/@sruuri"/>
                  </database>
                </serverInfo>

                <databaseInfo>
                  <title lang="en" primary="true">Geonetwork SRU Interface</title>
                </databaseInfo>

                <indexInfo>

                  <xsl:for-each select="response/sets/set">
                    <set>
                      <xsl:attribute name="name">
                        <xsl:value-of select="@namespace"/>
                      </xsl:attribute>
                      <xsl:attribute name="identifier">
                        <xsl:value-of select="@url"/>
                      </xsl:attribute>
                    </set>
                  </xsl:for-each>

                  <xsl:for-each select="response/indices/index">

                    <index>
                      <xsl:attribute name="id">
                        <xsl:value-of select="@id"/>
                      </xsl:attribute>

                      <title>
                        <xsl:value-of select="map/@text"/>
                      </title>
                      <xsl:for-each select="map">
                        <map>
                          <name>
                            <xsl:attribute name="set">
                              <xsl:value-of select="@set"/>
                            </xsl:attribute>
                            <xsl:value-of select="@text"/>
                          </name>
                        </map>
                      </xsl:for-each>
                    </index>

                  </xsl:for-each>


                </indexInfo>
                <schemaInfo>
                  <schema name="xml:meta"
                          identifier="http://www.iso.org/iso/catalogue_detail.htm?csnumber=32557">
                    <title>ISO 19139</title>
                  </schema>
                </schemaInfo>
                <configInfo>
                  <default type="numberOfRecords">1</default>
                  <setting type="maximumRecords">
                    <xsl:value-of select="response/@records_per_page"/>
                  </setting>

                </configInfo>
              </explain>

            </zs:recordData>
          </zs:record>

          <xsl:if test="response/diagnostics">
            <zs:diagnostics xmlns="http://www.loc.gov/zing/srw/diagnostic/">
              <xsl:for-each select="response/diagnostics/diagnostic">
                <diagnostic>
                  <uri>
                    <xsl:value-of select="uri"/>
                  </uri>
                  <message>
                    <xsl:value-of select="message"/>
                  </message>
                  <details>
                    <xsl:value-of select="details"/>
                  </details>
                </diagnostic>
              </xsl:for-each>
            </zs:diagnostics>
          </xsl:if>

        </zs:explainResponse>

      </xsl:when>


      <xsl:when test="$op='testremotesearch'">
        <xsl:for-each select="response">
          <xsl:call-template name="copyrecord"/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="copyrecord"/>
      </xsl:otherwise>
    </xsl:choose>


  </xsl:template>

  <xsl:template match="@*|node()" name="copyrecord">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
