<?xml version="1.0" encoding="UTF-8"?>
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
  xmlns:te="java:com.occamlab.te.TECore"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0">
  <xsl:output method="text"/>
  <xsl:output name="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:param name="logdir"/>
  <xsl:param name="index"/>

  <xsl:template name="build-param-string">
    <xsl:param name="param-string" select="''"/>
    <xsl:param name="params"/>
    <xsl:variable name="new-param-string">
      <xsl:value-of select="$param-string"/>
      <xsl:if test="count($params) &gt; 0">
        <xsl:if test="not($param-string = '')">&amp;</xsl:if>
        <xsl:value-of select="concat($params[1]/@name, '=', $params[1])"/>
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($params) &gt; 1">
        <xsl:call-template name="build-param-string">
          <xsl:with-param name="param-string" select="$new-param-string"/>
          <xsl:with-param name="params" select="$params[position() &gt; 1]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$new-param-string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="build-url">
    <xsl:param name="url"/>
    <xsl:param name="params"/>
    <xsl:value-of select="$url"/>
    <xsl:if test="count($params) &gt; 0">
      <xsl:variable name="last-char" select="substring($url, string-length($url)-1)"/>
      <xsl:if test="not(contains('?&amp;', $last-char))">
        <xsl:choose>
          <xsl:when test="contains($url, '?')">&amp;</xsl:when>
          <xsl:otherwise>?</xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:call-template name="build-param-string">
        <xsl:with-param name="params" select="$params"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="literal">
    <xsl:value-of xmlns:saxon="http://saxon.sf.net/" select="saxon:serialize(., 'xml')"/>
  </xsl:template>

  <!--
      <xsl:template name="literal">
          <xsl:value-of select="concat('&lt;', name())"/>
          <xsl:for-each select="@*">
              <xsl:value-of select="concat(' ', name(), '=', .)"/>
          </xsl:for-each>
          <xsl:value-of select="'&gt;'"/>
          <xsl:for-each select="text()|*">
              <xsl:choose>
                  <xsl:when test="name() = ''">
                      <xsl:value-of select="."/>
                  </xsl:when>
                  <xsl:otherwise>
                      <xsl:call-template name="literal"/>
                  </xsl:otherwise>
              </xsl:choose>
          </xsl:for-each>
          <xsl:value-of select="concat('&lt;', name(), '&gt;')"/>
      </xsl:template>
  -->

  <xsl:template match="sessions">
    <xsl:text>Sessions:&#xa;</xsl:text>
    <xsl:for-each select="session">
      <xsl:value-of select="concat('   ', @id, '&#xa;')"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="test" name="viewtest">
    <xsl:param name="indent" select="''"/>
    <!--    <xsl:value-of select="saxon:serialize(/, 'xml')" xmlns:saxon="http://saxon.sf.net/"/> -->
    <xsl:variable name="result">
      <xsl:choose>
        <xsl:when test="@failed='yes' and @complete='no'">Failed and did not complete</xsl:when>
        <xsl:when test="@failed='yes'">Failed</xsl:when>
        <xsl:when test="@complete='no'">Did not complete</xsl:when>
        <xsl:when test="descendant::test[@failed='yes']">Failed (Inherited Failure)</xsl:when>
        <xsl:when test="@warning='yes'">Warning</xsl:when>
        <xsl:otherwise>Passed</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of
      select="concat($indent, 'Test ', @prefix, ':', @local-name, ' (', @path, ') ', $result, '&#xa;')"/>
    <xsl:for-each select="test">
      <xsl:call-template name="viewtest">
        <xsl:with-param name="indent" select="concat($indent, '   ')"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="log">
    <xsl:apply-templates select="*"/>
    <xsl:variable name="result">
      <xsl:choose>
        <xsl:when test="endtest/@result = 3">Failed</xsl:when>
        <xsl:when test="$index//test[@failed='yes']">Failed (Inherited Failure)</xsl:when>
        <xsl:when test="endtest/@result = 1">Warning</xsl:when>
        <xsl:when test="endtest">Passed</xsl:when>
        <xsl:otherwise>Test execution did not complete</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="concat('Result: ', $result, '&#xa;')"/>
  </xsl:template>

  <xsl:template match="starttest">
    <xsl:value-of select="concat('Test ', @prefix, ':', @local-name, ' (', @path, ')&#xa;&#xa;')"/>

    <xsl:value-of select="concat('Assertion: ', assertion, '&#xa;&#xa;')"/>

    <xsl:for-each select="param">
      <xsl:value-of select="concat('Parameter ', @name, ':&#xa;')"/>
      <xsl:value-of select="concat('   Label: ', @label, '&#xa;')"/>
      <xsl:text>   Value: </xsl:text>
      <xsl:for-each select="value">
        <xsl:value-of select="text()|@*"/>
        <xsl:for-each select="*">
          <xsl:call-template name="literal"/>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
    <xsl:if test="param">
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>

    <xsl:for-each select="context[not(value/@te:used='false')]">
      <xsl:text>Context:&#xa;</xsl:text>
      <xsl:value-of select="concat('   Label: ', @label, '&#xa;')"/>
      <xsl:text>   Value: </xsl:text>
      <xsl:for-each select="value">
        <xsl:value-of select="text()|@*"/>
        <xsl:for-each select="*">
          <xsl:call-template name="literal"/>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
    <xsl:if test="context[not(value/@te:used='false')]">
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="exception">
    <xsl:value-of select="."/>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="request">
    <xsl:text>Request </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:text>:&#xa;</xsl:text>
    <xsl:text>   Method: </xsl:text>
    <xsl:value-of select="method"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:text>   URL: </xsl:text>
    <xsl:choose>
      <xsl:when test="translate(method, 'GET', 'get') = 'get'">
        <xsl:call-template name="build-url">
          <xsl:with-param name="url" select="url"/>
          <xsl:with-param name="params" select="param"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="url"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xa;</xsl:text>
    <xsl:if test="translate(method, 'POST', 'post') = 'post'">
      <xsl:text>   Body: </xsl:text>
      <xsl:choose>
        <xsl:when test="body">
          <xsl:text>&#xa;</xsl:text>
          <xsl:for-each select="body/*">
            <xsl:call-template name="literal"/>
          </xsl:for-each>
          <xsl:if test="not(body/*)">
            <xsl:value-of select="body"/>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="build-param-string">
            <xsl:with-param name="params" select="param"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="response">
    <xsl:text>   Response</xsl:text>
    <xsl:if test="parser">
      <xsl:value-of select="concat(' from parser ', parser/@prefix, ':', parser/@local-name)"/>
    </xsl:if>
    <xsl:text>:&#xa;      </xsl:text>
    <xsl:for-each select="content/*">
      <xsl:call-template name="literal"/>
    </xsl:for-each>
    <xsl:text>&#xa;</xsl:text>
    <xsl:if test="parser[not(.='')]">
      <xsl:value-of
        select="concat('   Messages from parser ', parser/@prefix, ':', parser/@local-name, ':&#xa;')"/>
      <xsl:text>        </xsl:text>
      <xsl:value-of select="translate(parser, '&#xa;', '&#xa;      ')"/>
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="parse">
    <xsl:text>Parse </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:text>:&#xa;</xsl:text>
    <xsl:apply-templates select="response"/>
  </xsl:template>

  <xsl:template match="testcall">
    <xsl:variable name="path" select="@path"/>
    <xsl:variable name="result">
      <xsl:choose>
        <xsl:when test="$index//test[@path=$path and @failed='yes' and @complete='no']">Failed and
          did not complete
        </xsl:when>
        <xsl:when test="$index//test[@path=$path and @failed='yes']">Failed</xsl:when>
        <xsl:when test="$index//test[@path=$path and @complete='no']">Did not complete</xsl:when>
        <xsl:when test="$index//test[@path=$path]//test[@failed='yes']">Failed (Inherited Failure)
        </xsl:when>
        <xsl:when test="$index//test[@path=$path and @warning='yes']">Warning</xsl:when>
        <xsl:otherwise>Passed</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="concat('Subtest ', @path, ' ', $result, '&#xa;&#xa;')"/>
  </xsl:template>

  <xsl:template match="formresults">
    <xsl:text>Form </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:text>:&#xa;</xsl:text>
    <xsl:for-each select="values/value">
      <xsl:value-of select="concat('   ', @key, '=', ., '&#xa;')"/>
    </xsl:for-each>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="message">
    <xsl:value-of select="concat('Message ', @id, ':&#xa;   ', ., '&#xa;&#xa;')"/>
  </xsl:template>

  <xsl:template match="*|@*"/>
</xsl:transform>
