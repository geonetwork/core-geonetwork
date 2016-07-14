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
  xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:te="java:com.occamlab.te.TECore"
  version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:output indent="yes"/>
  <xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

  <xsl:param name="filename"/>
  <xsl:param name="txsl_filename" select="'memory:txsl'"/>

  <xsl:variable name="apos">'</xsl:variable>

  <xsl:template name="namespace-attribute">
    <xsl:param name="prefix"/>
    <xsl:param name="uri"/>
    <xsl:if test="$uri != ''">
      <xsl:variable name="element">
        <xsl:element name="{$prefix}:x" namespace="{$uri}"/>
      </xsl:variable>
      <xsl:copy-of select="$element/*/namespace::*[name()=$prefix]"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ctl:test">
    <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
    <txsl:template name="{@name}">
      <!--      <xsl:copy-of select="namespace::*[name()=$prefix]"/> -->
      <xsl:copy-of select="namespace::*"/>
      <xsl:for-each select="ctl:param">
        <txsl:param name="{@name}" select="string(.)"/>
      </xsl:for-each>
      <txsl:param name="te:context-label" select="$te:starting-context-label"/>
      <txsl:param name="te:call-path" select="$te:starting-test-path"/>
      <txsl:param name="te:call-depth" select="0"/>
      <txsl:param name="te:indent" select="''"/>

      <txsl:variable name="te:start-test">
        <starttest local-name="{$local-name}" prefix="{$prefix}" namespace-uri="{$namespace-uri}">
          <txsl:attribute name="path">
            <txsl:value-of select="$te:call-path"/>
          </txsl:attribute>
          <assertion>
            <txsl:variable name="context" select="$te:context-label"/>
            <!--            <txsl:variable name="context" select="'{ctl:context}'"/> -->
            <txsl:variable name="te:assertion">
              <assertion value="{ctl:assertion}"/>
            </txsl:variable>
            <txsl:value-of select="$te:assertion/assertion/@value"/>
          </assertion>
          <xsl:for-each select="ctl:param">
            <param name="{@name}">
              <txsl:copy-of select="${@name}"/>
            </param>
          </xsl:for-each>
          <xsl:for-each select="ctl:context">
            <context>
              <txsl:copy-of select="$te:context-label"/>
            </context>
          </xsl:for-each>
        </starttest>
      </txsl:variable>
      <!--      <txsl:value-of select="te:message($te:core, $te:call-depth, concat('{$local-name}: ', $te:start-test/starttest/assertion))"/> -->
      <txsl:variable name="te:first-line">
        <xsl:text>&lt;test local-name="</xsl:text>
        <xsl:value-of select="$local-name"/>
        <xsl:text>" prefix="</xsl:text>
        <xsl:value-of select="$prefix"/>
        <xsl:text>" namespace-uri="</xsl:text>
        <xsl:value-of select="$namespace-uri"/>
        <xsl:text>"&gt;</xsl:text>
      </txsl:variable>
      <txsl:variable name="te:body">
        <xsl:text>&lt;assertion&gt;</xsl:text>
        <txsl:copy-of select="$te:start-test/starttest/assertion"/>
        <xsl:text>&lt;/assertion&gt;&#xa;</xsl:text>
        <xsl:for-each select="ctl:comment">
          <xsl:text>&lt;comment&gt;</xsl:text>
          <xsl:value-of select="."/>
          <xsl:text>&lt;/comment&gt;&#xa;</xsl:text>
        </xsl:for-each>
        <xsl:for-each select="ctl:link">
          <xsl:if test="position() != 1">&#xa;</xsl:if>
          <xsl:text>&lt;link</xsl:text>
          <xsl:if test="@title">
            <xsl:text> title="</xsl:text>
            <xsl:value-of select="@title"/>
            <xsl:text>"</xsl:text>
          </xsl:if>
          <xsl:text>&gt;</xsl:text>
          <xsl:value-of select="."/>
          <xsl:text>&lt;/link&gt;</xsl:text>
        </xsl:for-each>
      </txsl:variable>
      <txsl:variable name="te:last-line">&lt;/test&gt;</txsl:variable>
      <!--
                  <txsl:variable name="te:info">
                      <info>
                          <txsl:copy-of select="$te:start-test/starttest/@*"/>
                          <txsl:copy-of select="$te:start-test/starttest/assertion"/>
                          <xsl:for-each select="ctl:comment">
                              <comment><xsl:copy-of select="@*|*|text()"/></comment>
                          </xsl:for-each>
                          <xsl:for-each select="ctl:link">
                              <link><xsl:copy-of select="@*|*|text()"/></link>
                          </xsl:for-each>
                      </info>
                  </txsl:variable>
                  <txsl:value-of select="te:message($te:core, 1, '&lt;test&gt;')"/>
                  <txsl:value-of select="te:message($te:core, 1, saxon:serialize($te:info/info, 'xml'))" xmlns:saxon="http://saxon.sf.net/"/>
      -->
      <txsl:value-of select="te:message($te:core, $te:call-depth, $te:first-line)"/>
      <txsl:value-of select="te:message($te:core, $te:call-depth+1, $te:body)"/>
      <xsl:apply-templates select="ctl:code/*"/>
      <txsl:value-of select="te:message($te:core, $te:call-depth, $te:last-line)"/>
    </txsl:template>
  </xsl:template>

  <xsl:template match="ctl:call-test">
    <xsl:variable name="test-title" select="@name"/>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
    <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
    <txsl:variable name="te:new-call-path"
                   select="concat($te:call-path, '/{generate-id()}_', position())"/>
    <txsl:call-template name="{$prefix}:{$local-name}">
      <xsl:call-template name="namespace-attribute">
        <xsl:with-param name="prefix" select="$prefix"/>
        <xsl:with-param name="uri" select="$namespace-uri"/>
      </xsl:call-template>
      <xsl:for-each select="ctl:with-param">
        <xsl:if test="@label">
          <txsl:with-param name="{@name}" select="'{@label}'"/>
        </xsl:if>
      </xsl:for-each>
      <txsl:with-param name="te:context-label" select="$te:context-label"/>
      <txsl:with-param name="te:call-path" select="$te:new-call-path"/>
      <txsl:with-param name="te:call-depth" select="$te:call-depth + 1"/>
      <txsl:with-param name="te:indent" select="concat($te:indent, '  ')"/>
    </txsl:call-template>
    <!--
            <txsl:variable name="te:test-call">
                <testcall>
                    <txsl:attribute name="path"><txsl:value-of select="$te:new-call-path"/></txsl:attribute>
                </testcall>
            </txsl:variable>
    -->
  </xsl:template>

  <xsl:template match="ctl:suite">
    <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <xsl:variable name="content">
      <xsl:for-each select="ctl:starting-test">
        <xsl:variable name="test-prefix" select="substring-before(., ':')"/>
        <txsl:call-template name="{.}">
          <xsl:copy-of select="namespace::*[name()=$test-prefix]"/>
        </txsl:call-template>
      </xsl:for-each>
    </xsl:variable>
    <txsl:template match="{@name}">
      <xsl:copy-of select="namespace::*[name()=$prefix]"/>
      <xsl:copy-of select="$content"/>
    </txsl:template>
    <txsl:template match="{$prefix}-{$local-name}">
      <xsl:copy-of select="$content"/>
    </txsl:template>
    <xsl:if test="not($local-name = 'suite')">
      <txsl:template match="{$local-name}">
        <xsl:copy-of select="$content"/>
      </txsl:template>
    </xsl:if>
    <txsl:template match="suite">
      <xsl:copy-of select="$content"/>
    </txsl:template>
  </xsl:template>

  <xsl:template match="ctl:for-each">
    <txsl:for-each select="/">
      <xsl:if test="@label">
        <txsl:variable name="te:context-label" select="'{@label}'"/>
      </xsl:if>
      <xsl:apply-templates/>
    </txsl:for-each>
  </xsl:template>

  <xsl:template match="xsl:for-each">
    <txsl:for-each select="/">
      <xsl:apply-templates/>
    </txsl:for-each>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="text()">
  </xsl:template>

  <xsl:template match="*">
  </xsl:template>

  <xsl:template match="xsl:*">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="ctl:*">
    <xsl:apply-templates/>
  </xsl:template>
  <!--
      <xsl:template match="/">
          <txsl:transform version="1.0" exclude-result-prefixes="ctl saxon">
              <xsl:apply-templates/>
          </txsl:transform>
      </xsl:template>
  -->
  <xsl:template match="/">
    <txsl:transform version="2.0" exclude-result-prefixes="ctl saxon">
      <txsl:output name="xml" omit-xml-declaration="yes" indent="yes"/>
      <txsl:template name="file:te-initialize">
        <xsl:call-template name="namespace-attribute">
          <xsl:with-param name="prefix" select="'file'"/>
          <xsl:with-param name="uri" select="$txsl_filename"/>
        </xsl:call-template>
      </txsl:template>
      <xsl:apply-templates/>
    </txsl:transform>
  </xsl:template>
</xsl:transform>

