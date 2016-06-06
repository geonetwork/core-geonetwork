<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License.

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:param name="title"/>
  <xsl:param name="web"/>
  <xsl:param name="files"/>
  <xsl:param name="thread"/>
  <xsl:param name="method"/>
  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:value-of select="$title"/>
        </title>
      </head>
      <body>
        <xsl:choose>
          <xsl:when test="$method = 'post'">
            <xsl:choose>
              <xsl:when test="$files = 'yes'">
                <form method="post" enctype="multipart/form-data">
                  <xsl:if test="$web = 'yes'">
                    <xsl:attribute name="action">test</xsl:attribute>
                    <input type="hidden" name="te-operation" value="SubmitPostForm"/>
                    <input type="hidden" name="te-thread" value="{$thread}"/>
                  </xsl:if>
                  <xsl:for-each select="form">
                    <xsl:copy-of select="*|text()"/>
                  </xsl:for-each>
                </form>
              </xsl:when>
              <xsl:otherwise>
                <form method="post">
                  <xsl:if test="$web = 'yes'">
                    <xsl:attribute name="action">test</xsl:attribute>
                    <input type="hidden" name="te-operation" value="SubmitPostForm"/>
                    <input type="hidden" name="te-thread" value="{$thread}"/>
                  </xsl:if>
                  <xsl:for-each select="form">
                    <xsl:copy-of select="*|text()"/>
                  </xsl:for-each>
                </form>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <form method="get">
              <xsl:if test="$web = 'yes'">
                <xsl:attribute name="action">test</xsl:attribute>
                <input type="hidden" name="te-operation" value="SubmitForm"/>
                <input type="hidden" name="te-thread" value="{$thread}"/>
              </xsl:if>
              <xsl:for-each select="form">
                <xsl:copy-of select="*|text()"/>
              </xsl:for-each>
            </form>
          </xsl:otherwise>
        </xsl:choose>
      </body>
    </html>
  </xsl:template>
</xsl:transform>
