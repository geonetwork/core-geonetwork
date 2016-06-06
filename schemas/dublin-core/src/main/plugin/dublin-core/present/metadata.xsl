<?xml version="1.0" encoding="UTF-8"?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="1.0">

  <xsl:include href="metadata-fop.xsl"/>

  <!-- main template - the way into processing dublin-core -->
  <xsl:template name="metadata-dublin-core">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <xsl:apply-templates mode="dublin-core" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- CompleteTab template - dc just calls completeTab from
         metadata-utils.xsl -->
  <xsl:template name="dublin-coreCompleteTab">
    <xsl:param name="tabLink"/>

    <xsl:call-template name="completeTab">
      <xsl:with-param name="tabLink" select="$tabLink"/>
    </xsl:call-template>
  </xsl:template>

  <!--
    default: in simple mode just a flat list
    -->
  <xsl:template mode="dublin-core" match="*|@*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="element" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="flat" select="$currTab='simple'"/>
    </xsl:apply-templates>
  </xsl:template>

  <!--
    these elements should be boxed
    -->
  <xsl:template mode="dublin-core" match="simpledc|csw:Record">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="elementEP" select="*">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="dublin-core" match="dc:anyCHOICE_ELEMENT0">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="elementEP" select="dc:*|geonet:child[string(@prefix)='dc']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP"
                         select="dct:modified|geonet:child[string(@name)='modified']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP"
                         select="dct:*[name(.)!='dct:modified']|geonet:child[string(@prefix)='dct' and name(.)!='modified']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:apply-templates>

  </xsl:template>

  <!--
    identifier
    -->
  <xsl:template mode="dublin-core" match="dc:identifier">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="text">
        <xsl:value-of select="."/>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <!--
    references
    Add file upload support
    -->
  <xsl:template mode="dublin-core" match="dct:references">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="content">
        <xsl:variable name="value" select="."/>
        <xsl:choose>
          <xsl:when test="$edit">
            <xsl:variable name="id" select="generate-id(.)"/>
            <div id="{$id}"/>
            <xsl:variable name="ref" select="geonet:element/@ref"/>
            <!--<xsl:variable name="button" select="normalize-space(.)!=''"/>-->
            <xsl:variable name="button" select="contains($value, 'fname=')"/>
            <xsl:call-template name="simpleElementGui">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
              <xsl:with-param name="title" select="/root/gui/strings/file"/>
              <xsl:with-param name="text">
                <button class="content" onclick="startFileUpload({//geonet:info/id}, '{$ref}');"
                        type="button">
                  <xsl:value-of select="/root/gui/strings/insertFileMode"/>
                </button>
              </xsl:with-param>
              <xsl:with-param name="id" select="concat('db_',$ref)"/>
              <xsl:with-param name="visible" select="not($button)"/>
            </xsl:call-template>
            <!-- Remove button -->
            <xsl:if test="$button">
              <xsl:apply-templates mode="dublinCoreFileRemove" select=".">
                <xsl:with-param name="access" select="'private'"/>
                <xsl:with-param name="id" select="$id"/>
              </xsl:apply-templates>
            </xsl:if>

            <!-- Displays the Upload button, if no uploaded resource related (id prefix =  'db_').
                            Hidden if an uploaded resource is related -->
            <xsl:call-template name="simpleElementGui">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
              <xsl:with-param name="title">
                <xsl:call-template name="getTitle">
                  <xsl:with-param name="name" select="name(.)"/>
                  <xsl:with-param name="schema" select="$schema"/>
                </xsl:call-template>
              </xsl:with-param>
              <xsl:with-param name="text">
                <xsl:choose>
                  <!-- Check if uploaded resource to display read-only value and Remove button to remove the uploaded resource -->
                  <xsl:when test="contains($value, 'fname=')">
                    <xsl:variable name="fileName"
                                  select="substring-before(substring-after($value, 'fname='), '&amp;')"/>
                    <input id="_{$ref}" class="md" type="text" name="_{$ref}" value="{$fileName}"
                           size="40">
                      <xsl:if test="$button">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                      </xsl:if>
                    </input>
                  </xsl:when>
                  <!-- If NOT uploaded resource display normal edit field -->
                  <xsl:otherwise>
                    <input id="_{$ref}" class="md" type="text" name="_{$ref}" value="{$value}"
                           size="40"/>
                  </xsl:otherwise>
                </xsl:choose>

                <!-- Field checked in metadata-editor.js (doFileUploadSubmit), to execute custom code for Dublin core metadata editor -->
                <input id="dc_{$ref}" class="md" type="hidden" name="dc_{$ref}"/>
              </xsl:with-param>
              <xsl:with-param name="id" select="concat('di_',$ref)"/>
            </xsl:call-template>
          </xsl:when>

          <xsl:otherwise>
            <xsl:if test="string($value)">
              <!-- Add an hyperlink in view mode -->
              <xsl:call-template name="simpleElementGui">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="title">
                  <xsl:call-template name="getTitle">
                    <xsl:with-param name="name" select="name(.)"/>
                    <xsl:with-param name="schema" select="$schema"/>
                  </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="text">
                  <xsl:choose>
                    <!-- Internal url to uploaded resource -->
                    <xsl:when test="starts-with($value, 'http') and contains($value, 'fname=')">
                      <xsl:variable name="fileName"
                                    select="substring-before(substring-after($value, 'fname='), '&amp;')"/>
                      <a href="{$value}" title="{$fileName}"
                         onclick="runFileDownload(this.href, 'Order Confirmation: ' + this.title); return false;">
                        <xsl:value-of select="$fileName"/>
                      </a>
                    </xsl:when>

                    <!-- External url: keep link -->
                    <xsl:when test="starts-with($value, 'http')">
                      bbb
                      <a href="{$value}">
                        <xsl:value-of select="."/>
                      </a>
                    </xsl:when>

                    <!-- Other values -->
                    <xsl:otherwise>
                      <p>
                        <xsl:value-of select="."/>
                      </p>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- dublin-core brief formatting -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  <xsl:template name="dublin-coreBrief">
    <metadata>
      <xsl:if test="dc:title">
        <title>
          <xsl:value-of select="dc:title"/>
        </title>
      </xsl:if>
      <xsl:if test="dc:description">
        <abstract>
          <xsl:value-of select="dc:description"/>
        </abstract>
      </xsl:if>

      <xsl:for-each select="dc:subject[text()]">
        <keyword>
          <xsl:value-of select="."/>
        </keyword>
      </xsl:for-each>
      <xsl:for-each select="dc:identifier[text()]">
        <link type="url">
          <xsl:value-of select="."/>
        </link>
      </xsl:for-each>
      <!-- FIXME
            <image>IMAGE</image>
            -->
      <!-- TODO : ows:BoundingBox -->
      <xsl:variable name="coverage" select="dc:coverage"/>
      <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
      <xsl:variable name="north" select="substring-before($n,',')"/>
      <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
      <xsl:variable name="south" select="substring-before($s,',')"/>
      <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
      <xsl:variable name="east" select="substring-before($e,',')"/>
      <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
      <xsl:variable name="west" select="substring-before($w,'. ')"/>
      <xsl:variable name="p" select="substring-after($coverage,'(')"/>
      <xsl:variable name="place" select="substring-before($p,')')"/>
      <xsl:if test="$n!=''">
        <geoBox>
          <westBL>
            <xsl:value-of select="$west"/>
          </westBL>
          <eastBL>
            <xsl:value-of select="$east"/>
          </eastBL>
          <southBL>
            <xsl:value-of select="$south"/>
          </southBL>
          <northBL>
            <xsl:value-of select="$north"/>
          </northBL>
        </geoBox>
      </xsl:if>

      <xsl:copy-of select="geonet:*"/>
    </metadata>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template mode="dublinCoreFileRemove" match="*">
    <xsl:param name="access" select="'public'"/>
    <xsl:param name="id"/>
    <xsl:call-template name="simpleElementGui">
      <xsl:with-param name="title" select="/root/gui/strings/file"/>
      <xsl:with-param name="text">
        <table width="100%">
          <tr>
            <xsl:variable name="ref" select="geonet:element/@ref"/>
            <td width="70%">
              <xsl:choose>
                <xsl:when test="contains(., 'fname=')">
                  <xsl:variable name="fileName"
                                select="substring-before(substring-after(., 'fname='), '&amp;')"/>
                  <a href="{.}">
                    <xsl:value-of select="$fileName"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <a href="{.}">
                    <xsl:value-of select="."/>
                  </a>
                </xsl:otherwise>
              </xsl:choose>
            </td>
            <td align="right">
              <button class="content"
                      onclick="javascript:doFileRemoveAction('{/root/gui/locService}/resources.del','{$ref}','{$access}','{$id}')">
                <xsl:value-of select="/root/gui/strings/remove"/>
              </button>
            </td>
          </tr>
        </table>
      </xsl:with-param>
      <xsl:with-param name="schema"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="dublin-core-javascript"/>
</xsl:stylesheet>
