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
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
  xmlns:te="java:com.occamlab.te.TECore"
  version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:output indent="yes"/>
  <xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

  <!-- Combines two compiled scripts and adds the root template -->

  <xsl:param name="prev">
    <none/>
  </xsl:param>

  <xsl:template match="/">
    <txsl:transform version="1.0">
      <xsl:for-each select="$prev/xsl:transform/xsl:include">
        <txsl:import href="{@href}"/>
      </xsl:for-each>

      <txsl:param name="te:core"/>
      <txsl:param name="te:logdir" select="''"/>
      <txsl:param name="te:starting-context-label" select="''"/>
      <txsl:param name="te:mode"/>
      <txsl:param name="te:starting-test-path"/>
      <txsl:param name="te:starting-log">
        <txsl:copy-of select="te:read_log($te:logdir, $te:starting-test-path)"/>
      </txsl:param>

      <xsl:variable name="variables" select="/xsl:transform/xsl:variable"/>
      <xsl:for-each select="$prev/xsl:transform/xsl:variable">
        <xsl:variable name="name" select="@name"/>
        <xsl:if test="not($variables/xsl:variable[@name=$name])">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each>

      <xsl:variable name="functions" select="/xsl:transform/xsl:function"/>
      <xsl:for-each select="$prev/xsl:transform/xsl:function">
        <xsl:variable name="name" select="@name"/>
        <xsl:if test="not($functions/xsl:function[@name=$name])">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each>

      <xsl:variable name="templates" select="/xsl:transform/xsl:template"/>
      <xsl:for-each select="$prev/xsl:transform/xsl:template[not(@match='/' or @match='*')]">
        <xsl:variable name="name" select="string(@name)"/>
        <xsl:variable name="match" select="string(@match)"/>
        <xsl:if test="not($templates/xsl:template[string(@name)=$name and string(@match)=$match])">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each>

      <xsl:copy-of select="/xsl:transform/*|/xsl:transform/comment()"/>

      <txsl:template match="/">
        <xsl:for-each
          select="$prev/xsl:transform/xsl:template[@match='/']/xsl:call-template[contains(@name, ':te-initialize')]">
          <xsl:copy-of select="."/>
        </xsl:for-each>
        <xsl:for-each select="/xsl:transform/xsl:include">
          <txsl:call-template name="file:te-initialize">
            <xsl:variable name="element">
              <xsl:element name="file:te-initialize" namespace="{@href}"/>
            </xsl:variable>
            <xsl:copy-of select="$element/*/namespace::*[name()='file']"/>
          </txsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="/xsl:transform/xsl:template[contains(@name, ':te-initialize')]">
          <txsl:call-template name="{@name}">
            <xsl:copy-of select="namespace::*[name()='file']"/>
          </txsl:call-template>
        </xsl:for-each>

        <txsl:variable name="te:results">
          <txsl:apply-templates/>
        </txsl:variable>
        <txsl:value-of
          select="$te:results"/>  <!-- Make sure the variable is calculated rather than optimized out -->
      </txsl:template>
    </txsl:transform>
  </xsl:template>
</xsl:transform>
