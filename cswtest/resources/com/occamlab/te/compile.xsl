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
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:math="java:java.lang.Math"
  exclude-result-prefixes="math"
  version="2.0">

  <xsl:strip-space elements="*"/>
  <xsl:output indent="yes"/>
  <xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

  <xsl:param name="filename"/>
  <xsl:param name="txsl_filename" select="'memory:txsl'"/>

  <xsl:variable name="apos">'</xsl:variable>

  <xsl:include href="ext/session_info.xsl"/>
  <xsl:include href="ext/config.xsl"/>

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

  <xsl:template name="loc">
    <xsl:attribute name="loc" namespace="java:com.occamlab.te.TECore">
      <xsl:value-of select="concat(saxon:line-number(.), ',', $filename)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="loc-element">
    <txsl:if test="false()">
      <xsl:call-template name="loc"/>
    </txsl:if>
  </xsl:template>

  <xsl:template match="ctl:package">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="ctl:test">
    <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
    <txsl:template match="{@name}">
      <xsl:call-template name="loc"/>
      <xsl:copy-of select="namespace::*[name()=$prefix]"/>
      <txsl:for-each select="$te:starting-log/log/starttest/context/value">
        <txsl:for-each select="*|@*|text()[not(../*|../@*)]|.[not(*|@*|text())]">
          <txsl:call-template name="{@name}">
            <xsl:copy-of select="namespace::*[name()=$prefix]"/>
            <xsl:for-each select="ctl:param">
              <txsl:with-param name="{@name}">
                <xsl:call-template name="loc"/>
                <txsl:for-each select="$te:starting-log/log/starttest/param[@name='{@name}']/value">
                  <txsl:copy-of select="node()|@*"/>
                </txsl:for-each>
              </txsl:with-param>
            </xsl:for-each>
          </txsl:call-template>
        </txsl:for-each>
      </txsl:for-each>
    </txsl:template>

    <txsl:template name="{@name}">
      <xsl:call-template name="loc"/>
      <xsl:copy-of select="namespace::*"/>
      <!--      <xsl:copy-of select="namespace::*[name()=$prefix]"/> -->
      <xsl:for-each select="ctl:param">
        <txsl:param name="{@name}">
          <xsl:call-template name="loc"/>
        </txsl:param>
        <txsl:param name="{@name}-label">
          <txsl:choose>
            <txsl:when test="count(${@name}) != 1">
              <xsl:value-of select="@name"/>
            </txsl:when>
            <txsl:when test="${@name}/../*">
              <xsl:text>&lt;</xsl:text>
              <txsl:value-of select="name(${@name})"/>
              <xsl:text>&gt;</xsl:text>
            </txsl:when>
            <txsl:otherwise>
              <xsl:text>&lt;</xsl:text>
              <txsl:value-of select="name(${@name}/*)"/>
              <xsl:text>&gt;</xsl:text>
            </txsl:otherwise>
          </txsl:choose>
        </txsl:param>
      </xsl:for-each>
      <txsl:param name="te:context-label" select="$te:starting-context-label"/>
      <txsl:param name="te:call-path" select="$te:starting-test-path"/>
      <txsl:param name="te:call-depth" select="0"/>
      <txsl:param name="te:indent" select="''"/>
      <txsl:param name="te:log" select="$te:starting-log"/>

      <txsl:value-of select="te:create_log($te:core, $te:logdir, $te:call-path)"/>

      <txsl:variable name="te:start-test">
        <starttest local-name="{$local-name}" prefix="{$prefix}" namespace-uri="{$namespace-uri}"
                   file="{$filename}">
          <txsl:attribute name="path">
            <txsl:value-of select="$te:call-path"/>
          </txsl:attribute>
          <assertion>
            <txsl:variable name="te:assertion">
              <xsl:for-each
                select="ctl:param[contains(../ctl:assertion, concat('{$', @name, '}'))]">
                <txsl:variable name="{@name}" select="${@name}-label"/>
              </xsl:for-each>
              <xsl:if test="contains(ctl:assertion, '{$context}')">
                <txsl:variable name="context" select="$te:context-label"/>
              </xsl:if>
              <assertion value="{ctl:assertion}"/>
            </txsl:variable>
            <txsl:value-of select="$te:assertion/assertion/@value"/>
          </assertion>
          <xsl:for-each select="ctl:param">
            <param name="{@name}">
              <txsl:attribute name="label">
                <txsl:value-of select="${@name}-label"/>
              </txsl:attribute>
              <value>
                <txsl:copy-of select="${@name}"/>
              </value>
            </param>
          </xsl:for-each>
          <context>
            <txsl:attribute name="label">
              <txsl:value-of select="$te:context-label"/>
            </txsl:attribute>
            <value>
              <xsl:choose>
                <xsl:when test="boolean(ctl:context)">
                  <txsl:copy-of select="."/>
                </xsl:when>
                <xsl:otherwise>
                  <txsl:attribute name="te:used">false</txsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
            </value>
          </context>
        </starttest>
      </txsl:variable>
      <txsl:value-of select="te:log_xml($te:core, $te:start-test)"/>
      <txsl:if test="$te:mode &lt; 3 or not($te:log/log/starttest)">
        <txsl:value-of
          select="te:message($te:core, $te:call-depth, concat('Testing {@name} (', $te:call-path, ')...'))"/>
        <txsl:value-of
          select="te:message($te:core, $te:call-depth + 1, concat('Assertion: ', $te:start-test/starttest/assertion))"/>
      </txsl:if>

      <txsl:variable name="te:test-results">
        <txsl:value-of select="''"/>  <!-- This prevents error in case the code element is empty -->
        <txsl:for-each select="$te:start-test/starttest/context/value">
          <txsl:for-each select="*|@*|text()[not(../*|../@*)]|.[not(*|@*|text())]">
            <xsl:apply-templates select="ctl:code/*"/>
          </txsl:for-each>
        </txsl:for-each>
      </txsl:variable>

      <txsl:variable name="te:end-test">
        <endtest>
          <txsl:attribute name="result">
            <txsl:choose>
              <txsl:when test="boolean($te:test-results/te:fail[@code=2])">
                <txsl:value-of
                  select="{concat('te:message($te:core, $te:call-depth, ', $apos, 'Test ', @name, ' Failed', $apos, ')')}"/>
                <txsl:text>3</txsl:text>
              </txsl:when>
              <txsl:when test="boolean($te:test-results/te:fail[@code=1])">
                <txsl:value-of
                  select="{concat('te:message($te:core, $te:call-depth, ', $apos, 'Test ', @name, ' Failed (Inherited Failure)', $apos, ')')}"/>
                <txsl:text>2</txsl:text>
              </txsl:when>
              <txsl:when test="boolean($te:test-results/te:warning)">
                <txsl:value-of
                  select="{concat('te:message($te:core, $te:call-depth, ', $apos, 'Test ', @name, ' Warning', $apos, ')')}"/>
                <txsl:text>1</txsl:text>
              </txsl:when>
              <txsl:otherwise>
                <txsl:value-of
                  select="{concat('te:message($te:core, $te:call-depth, ', $apos, 'Test ', @name, ' Passed', $apos, ')')}"/>
                <txsl:text>0</txsl:text>
              </txsl:otherwise>
            </txsl:choose>
          </txsl:attribute>
        </endtest>
      </txsl:variable>
      <txsl:value-of select="te:log_xml($te:core, $te:end-test)"/>
      <txsl:value-of select="te:close_log($te:core)"/>

      <txsl:if test="boolean($te:test-results/te:fail)">
        <te:fail code="1"/>
      </txsl:if>
    </txsl:template>
  </xsl:template>

  <xsl:template match="ctl:form">
    <xsl:variable name="form-id" select="generate-id()"/>
    <txsl:variable name="te:form-call-id">
      <xsl:call-template name="loc"/>
      <xsl:attribute name="select">
        <xsl:value-of select="concat('concat(', $apos, $form-id, '_', $apos, ', position())')"/>
      </xsl:attribute>
    </txsl:variable>
    <txsl:variable name="te:form-xhtml">
      <form>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
      </form>
    </txsl:variable>
    <txsl:variable name="te:formresults">
      <txsl:choose>
        <txsl:when
          test="$te:mode&gt;=2 and boolean($te:log/log/formresults[@id = $te:form-call-id])">
          <txsl:copy-of select="$te:log/log/formresults[@id = $te:form-call-id]"/>
        </txsl:when>
        <txsl:otherwise>
          <formresults>
            <txsl:attribute name="id">
              <txsl:value-of select="$te:form-call-id"/>
            </txsl:attribute>
            <txsl:copy-of select="te:form($te:core, $te:form-xhtml)"/>
          </formresults>
        </txsl:otherwise>
      </txsl:choose>
    </txsl:variable>
    <txsl:copy-of select="$te:formresults/formresults/*"/>
    <txsl:value-of select="te:log_xml($te:core, $te:formresults)"/>
  </xsl:template>

  <xsl:template name="request" match="ctl:request">
    <xsl:param name="mode"/>
    <xsl:if test="$mode = 'fn-code'">
      <txsl:variable name="te:call-depth" select="0"/>
      <txsl:variable name="te:log">
        <log/>
      </txsl:variable>
    </xsl:if>
    <xsl:variable name="request-id" select="generate-id()"/>
    <txsl:variable name="te:web-call-id">
      <xsl:call-template name="loc"/>
      <!--       <xsl:attribute name="select"><xsl:value-of select="concat('concat(', $apos, $request-id, '_', $apos, ', position())')"/></xsl:attribute> -->
      <xsl:attribute name="select">
        <xsl:text>concat('</xsl:text>
        <xsl:value-of select="$request-id"/>
        <xsl:text>_', </xsl:text>
        <xsl:choose>
          <xsl:when test="$mode = 'fn-code'">
            <xsl:value-of select="math:random()"/>
          </xsl:when>
          <xsl:otherwise>position()</xsl:otherwise>
        </xsl:choose>
        <xsl:text>)</xsl:text>
      </xsl:attribute>
    </txsl:variable>
    <txsl:variable name="te:processed-request">
      <xsl:apply-templates select="*"/>
    </txsl:variable>
    <txsl:variable name="te:request">
      <txsl:choose>
        <txsl:when test="$te:mode&gt;=2 and boolean($te:log/log/request[@id = $te:web-call-id])">
          <txsl:copy-of select="$te:log/log/request[@id = $te:web-call-id]"/>
        </txsl:when>
        <txsl:otherwise>
          <request>
            <txsl:attribute name="id">
              <txsl:value-of select="$te:web-call-id"/>
            </txsl:attribute>
            <!-- Just the elements in the CTL namespace are part of the request.  Anything else is a parser -->
            <txsl:for-each select="$te:processed-request/ctl:*">
              <txsl:element>
                <xsl:attribute name="name">{local-name()}</xsl:attribute>
                <txsl:copy-of select="@*"/>
                <txsl:copy-of select="node()"/>
              </txsl:element>
            </txsl:for-each>
          </request>
        </txsl:otherwise>
      </txsl:choose>
    </txsl:variable>
    <txsl:value-of select="te:log_xml($te:core, $te:request)"/>
    <txsl:variable name="te:request-response" select="te:build_request($te:request/request)">
      <xsl:call-template name="loc"/>
    </txsl:variable>
    <txsl:variable name="te:parser">
      <txsl:copy-of
        select="$te:processed-request/*[not(namespace-uri() = 'http://www.occamlab.com/ctl')]"/>
    </txsl:variable>
    <xsl:call-template name="loc-element"/>
    <txsl:variable name="te:response">
      <txsl:choose>
        <txsl:when test="$te:mode&gt;=2 and boolean($te:log/log/response[@id = $te:web-call-id])">
          <txsl:copy-of select="$te:log/log/response[@id = $te:web-call-id]"/>
        </txsl:when>
        <txsl:when test="boolean($te:parser/*)">
          <txsl:copy-of
            select="te:parse($te:core, $te:request-response, $te:web-call-id, $te:parser)"/>
        </txsl:when>
        <txsl:otherwise>
          <txsl:copy-of select="te:parse($te:core, $te:request-response, $te:web-call-id)"/>
        </txsl:otherwise>
      </txsl:choose>
    </txsl:variable>
    <txsl:if test="string-length($te:response/response/parser) &gt; 0">
      <txsl:value-of
        select="te:message($te:core, $te:call-depth + 1, $te:response/response/parser)"/>
    </txsl:if>
    <txsl:value-of select="te:log_xml($te:core, $te:response)"/>
    <txsl:for-each select="$te:response/response/content">
      <txsl:copy-of select="*|text()"/>
    </txsl:for-each>
  </xsl:template>

  <!-- Warning: If ctl:request is used inside a function, it will be resubmitted even in resume mode.  Not recommended. -->
  <xsl:template match="ctl:request" mode="fn-code">
    <xsl:call-template name="request">
      <xsl:with-param name="mode" select="'fn-code'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="ctl:parse">
    <xsl:variable name="id" select="generate-id()"/>
    <txsl:variable name="te:parse-id">
      <xsl:call-template name="loc"/>
      <xsl:attribute name="select">
        <xsl:value-of select="concat('concat(', $apos, $id, '_', $apos, ', position())')"/>
      </xsl:attribute>
    </txsl:variable>
    <txsl:variable name="te:parse-instruction">
      <xsl:apply-templates select="*"/>
    </txsl:variable>
    <xsl:variable name="parser">
      <xsl:for-each select="*">
        <xsl:choose>
          <xsl:when test="self::xsl:output"/>
          <xsl:when test="self::ctl:content"/>
          <xsl:otherwise>
            <xsl:apply-templates select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <txsl:variable name="te:response">
      <parse>
        <txsl:attribute name="id">
          <txsl:value-of select="$te:parse-id"/>
        </txsl:attribute>
        <xsl:choose>
          <xsl:when test="boolean($parser/*)">
            <xsl:variable name="parser-prefix" select="substring-before(name($parser/*), ':')"/>
            <txsl:copy-of select="te:serialize_and_parse($te:core, $te:parse-instruction)">
              <xsl:copy-of select="namespace::*[name()=$parser-prefix]"/>
            </txsl:copy-of>
          </xsl:when>
          <xsl:otherwise>
            <txsl:copy-of select="te:serialize_and_parse($te:core, $te:parse-instruction)"/>
          </xsl:otherwise>
        </xsl:choose>
      </parse>
    </txsl:variable>
    <txsl:if test="string-length($te:response/parse/response/parser) &gt; 0">
      <txsl:value-of
        select="te:message($te:core, $te:call-depth + 1, $te:response/parse/response/parser)"/>
    </txsl:if>
    <txsl:value-of select="te:log_xml($te:core, $te:response/parse)"/>
    <txsl:for-each select="$te:response/parse/response/content">
      <txsl:copy-of select="*|text()"/>
    </txsl:for-each>
  </xsl:template>

  <xsl:template match="ctl:call-test">
    <xsl:variable name="test-title" select="@name"/>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
    <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
    <txsl:variable name="te:new-call-path"
                   select="concat($te:call-path, '/{generate-id()}_', position())"/>
    <txsl:variable name="te:test-call">
      <testcall>
        <txsl:attribute name="path">
          <txsl:value-of select="$te:new-call-path"/>
        </txsl:attribute>
      </testcall>
    </txsl:variable>
    <txsl:value-of select="te:log_xml($te:core, $te:test-call)"/>
    <txsl:variable name="te:new-log">
      <txsl:copy-of select="te:read_log($te:logdir, $te:new-call-path)"/>
    </txsl:variable>
    <txsl:choose>
      <txsl:when test="boolean($te:new-log/log/endtest)">
        <txsl:choose>
          <txsl:when test="$te:new-log/log/endtest/@result = 3">
            <te:fail code="2"/>
            <txsl:if test="$te:mode &lt; 3">
              <txsl:value-of
                select="te:message($te:core, $te:call-depth + 1, 'Test {$test-title} Failed')"/>
            </txsl:if>
          </txsl:when>
          <txsl:when test="$te:new-log/log/endtest/@result = 1">
            <te:warning/>
            <txsl:if test="$te:mode &lt; 3">
              <txsl:value-of
                select="te:message($te:core, $te:call-depth + 1, 'Test {$test-title} generated a Warning')"/>
            </txsl:if>
          </txsl:when>
          <txsl:otherwise>
            <txsl:if test="$te:mode &lt; 3">
              <txsl:value-of
                select="te:message($te:core, $te:call-depth + 1, 'Test {$test-title} Passed')"/>
            </txsl:if>
          </txsl:otherwise>
        </txsl:choose>
      </txsl:when>
      <txsl:otherwise>
        <xsl:for-each select="ctl:with-param">
          <txsl:variable name="te:with-param-{position()}">
            <value>
              <xsl:if test="@select">
                <txsl:copy-of select="{@select}"/>
              </xsl:if>
              <xsl:apply-templates/>
            </value>
          </txsl:variable>
        </xsl:for-each>
        <txsl:call-template name="{$prefix}:{$local-name}">
          <xsl:call-template name="namespace-attribute">
            <xsl:with-param name="prefix" select="$prefix"/>
            <xsl:with-param name="uri" select="$namespace-uri"/>
          </xsl:call-template>
          <xsl:for-each select="*[local-name()='with-param']">
            <txsl:with-param name="{@name}"
                             select="$te:with-param-{position()}/value/node()|$te:with-param-{position()}/value/@*"/>
            <xsl:if test="@label-expr">
              <txsl:with-param name="{@name}-label" select="{@label-expr}"/>
            </xsl:if>
          </xsl:for-each>
          <txsl:with-param name="te:context-label" select="$te:context-label"/>
          <txsl:with-param name="te:call-path" select="$te:new-call-path"/>
          <txsl:with-param name="te:call-depth" select="$te:call-depth + 1"/>
          <txsl:with-param name="te:indent" select="concat($te:indent, '  ')"/>
          <txsl:with-param name="te:log" select="$te:new-log"/>
        </txsl:call-template>
      </txsl:otherwise>
    </txsl:choose>
    <!--
            <txsl:variable name="te:test-call">
                <testcall>
                    <txsl:attribute name="path"><txsl:value-of select="$te:new-call-path"/></txsl:attribute>
                </testcall>
            </txsl:variable>
            <txsl:value-of select="te:log_xml($te:core, $te:test-call)"/>
    -->
  </xsl:template>

  <xsl:template match="xsl:variable">
    <xsl:copy>
      <xsl:call-template name="loc"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
    <txsl:variable name="te:variable">
      <content>
        <txsl:copy-of select="${@name}"/>
      </content>
    </txsl:variable>
    <txsl:for-each select="$te:variable//te:fail">
      <txsl:if test="@message">
        <txsl:value-of select="te:message($te:core, $te:call-depth + 1, @message)"/>
      </txsl:if>
      <txsl:copy-of select="."/>
    </txsl:for-each>
    <txsl:for-each select="$te:variable//te:warning">
      <txsl:if test="@message">
        <txsl:value-of select="te:message($te:core, $te:call-depth + 1, @message)"/>
      </txsl:if>
      <txsl:copy-of select="."/>
    </txsl:for-each>
  </xsl:template>

  <xsl:template match="ctl:fail">
    <te:fail code="2">
      <xsl:call-template name="loc"/>
      <xsl:copy-of select="@message"/>
    </te:fail>
  </xsl:template>

  <xsl:template match="ctl:warning">
    <te:warning>
      <xsl:call-template name="loc"/>
      <xsl:copy-of select="@message"/>
    </te:warning>
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
      <xsl:call-template name="loc"/>
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
    <txsl:for-each>
      <xsl:call-template name="loc"/>
      <xsl:apply-templates select="@select"/>
      <xsl:if test="@label-expr">
        <txsl:variable name="te:context-label" select="{@label-expr}"/>
      </xsl:if>
      <xsl:apply-templates/>
    </txsl:for-each>
  </xsl:template>

  <xsl:template match="ctl:message">
    <txsl:variable name="te:message">
      <message id="{generate-id()}">
        <xsl:choose>
          <xsl:when test="@select">
            <txsl:value-of select="{@select}">
              <xsl:call-template name="loc"/>
            </txsl:value-of>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="loc"/>
            <xsl:apply-templates/>
          </xsl:otherwise>
        </xsl:choose>
      </message>
    </txsl:variable>
    <txsl:value-of select="te:log_xml($te:core, $te:message)"/>
    <txsl:value-of select="te:message($te:core, $te:call-depth + 1, string($te:message/message))"/>
  </xsl:template>

  <xsl:template match="ctl:out">
    <xsl:choose>
      <xsl:when test="@select">
        <txsl:value-of select="te:copy($te:core, {@select})">
          <xsl:call-template name="loc"/>
        </txsl:value-of>
      </xsl:when>
      <xsl:otherwise>
        <txsl:variable name="te:output">
          <xsl:call-template name="loc"/>
          <xsl:apply-templates/>
        </txsl:variable>
        <txsl:value-of select="te:copy($te:core, $te:output)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="ctl:call-function">
    <xsl:for-each select="*[local-name()='with-param']">
      <xsl:if test="not(@select)">
        <txsl:variable name="te:param-{position()}">
          <xsl:call-template name="loc"/>
          <xsl:apply-templates/>
        </txsl:variable>
      </xsl:if>
    </xsl:for-each>
    <xsl:variable name="te:param-list">
      <xsl:for-each select="*[local-name()='with-param']">
        <xsl:choose>
          <xsl:when test="@select">
            <xsl:value-of select="concat(',', @select)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat(',$te:param-', position())"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <!--
            <txsl:value-of select="{@name}({substring($te:param-list,2)})">
                <xsl:call-template name="loc"/>
                <xsl:copy-of select="namespace::*[name()=$prefix]"/>
            </txsl:value-of>
    -->
    <txsl:copy-of select="{@name}({substring($te:param-list,2)})">
      <xsl:call-template name="loc"/>
      <xsl:copy-of select="namespace::*[name()=$prefix]"/>
    </txsl:copy-of>
  </xsl:template>

  <xsl:template name="make-var-params">
    <xsl:param name="count"/>
    <xsl:param name="current" select="0"/>
    <xsl:if test="$current &lt; $count">
      <txsl:param name="te:var-param-{$current}">
        <xsl:if test="$current=0">
          <xsl:call-template name="loc"/>
        </xsl:if>
      </txsl:param>
      <xsl:call-template name="make-var-params">
        <xsl:with-param name="count" select="$count"/>
        <xsl:with-param name="current" select="$current + 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ctl:parser"/>

  <xsl:template name="function" match="ctl:function">
    <xsl:param name="var-param-num" select="0"/>
    <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
    <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
    <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
    <txsl:function name="{@name}">
      <xsl:call-template name="loc"/>
      <xsl:copy-of select="namespace::*"/>
      <!--      <xsl:copy-of select="namespace::*[name()=$prefix]"/> -->
      <xsl:variable name="parameters">
        <xsl:for-each select="ctl:param">
          <xsl:variable name="param-name" select="@name"/>
          <xsl:copy-of select="../ctl:code/xsl:param[@name=$param-name]"/>
          <xsl:if test="not(../ctl:code/xsl:param[@name=$param-name])">
            <txsl:param name="{$param-name}">
              <xsl:call-template name="loc"/>
            </txsl:param>
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="ctl:var-params">
          <xsl:call-template name="make-var-params">
            <xsl:with-param name="count" select="@min + $var-param-num"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:variable>
      <xsl:copy-of select="$parameters"/>
      <xsl:choose>
        <xsl:when test="ctl:code">
          <xsl:for-each select="ctl:code">
            <xsl:call-template name="loc-element"/>
          </xsl:for-each>
          <xsl:apply-templates
            select="ctl:code/*[not(namespace-uri() = 'http://www.w3.org/1999/XSL/Transform' and local-name() = 'param')]"
            mode="fn-code"/>
        </xsl:when>
        <xsl:when test="ctl:java">
          <xsl:variable name="param-list">
            <xsl:if test="ctl:java/@initialized = 'true'">
              <xsl:value-of select="concat(',$object:', $local-name)"/>
            </xsl:if>
            <xsl:if test="ctl:context">
              <xsl:value-of select="',current()'"/>
            </xsl:if>
            <xsl:for-each select="$parameters/xsl:param">
              <xsl:value-of select="concat(',$', @name)"/>
            </xsl:for-each>
          </xsl:variable>
          <xsl:for-each select="ctl:java">
            <txsl:copy-of select="function:{@method}({substring($param-list,2)})">
              <xsl:call-template name="loc"/>
              <xsl:call-template name="namespace-attribute">
                <xsl:with-param name="prefix" select="'function'"/>
                <xsl:with-param name="uri" select="concat('java:',@class)"/>
              </xsl:call-template>
              <xsl:if test="@initialized = 'true'">
                <xsl:call-template name="namespace-attribute">
                  <xsl:with-param name="prefix" select="'object'"/>
                  <xsl:with-param name="uri" select="$namespace-uri"/>
                </xsl:call-template>
              </xsl:if>
            </txsl:copy-of>
          </xsl:for-each>
        </xsl:when>
      </xsl:choose>
    </txsl:function>
    <xsl:if test="ctl:var-params">
      <xsl:if test="ctl:var-params/@min + $var-param-num &lt; ctl:var-params/@max">
        <xsl:call-template name="function">
          <xsl:with-param name="var-param-num" select="$var-param-num + 1"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xsl:template">
    <xsl:copy>
      <xsl:call-template name="loc"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="fn-code"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="xsl:*">
    <xsl:copy>
      <xsl:call-template name="loc"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="xsl:*" mode="fn-code">
    <xsl:copy>
      <xsl:call-template name="loc"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="fn-code"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="fn-code">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="fn-code"/>
    </xsl:copy>
  </xsl:template>
  <!--
       <xsl:template match="node()" mode="drop-namespace">
          <xsl:element name="{local-name()}">
              <xsl:apply-templates select="@*"/>
              <xsl:apply-templates/>
          </xsl:element>
      </xsl:template>
  -->
  <xsl:template match="/">
    <txsl:transform version="1.0" exclude-result-prefixes="ctl saxon">
      <xsl:for-each select="//ctl:function|//ctl:parser">
        <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
        <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
        <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
        <xsl:for-each select="ctl:java[@initialized='true']">
          <xsl:for-each select="ctl:with-param">
            <xsl:if test="not(@select)">
              <txsl:variable name="{$prefix}:{$local-name}-{position()}">
                <xsl:call-template name="loc"/>
                <xsl:call-template name="namespace-attribute">
                  <xsl:with-param name="prefix" select="$prefix"/>
                  <xsl:with-param name="uri" select="$namespace-uri"/>
                </xsl:call-template>
                <xsl:apply-templates/>
              </txsl:variable>
            </xsl:if>
          </xsl:for-each>
          <xsl:variable name="param-list">
            <xsl:for-each select="ctl:with-param">
              <xsl:choose>
                <xsl:when test="@select">
                  <xsl:value-of select="concat(',', @select)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat(',$' ,$prefix, ':', $local-name, '-', position())"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:variable>
          <txsl:variable name="{$prefix}:{$local-name}"
                         select="initializer:new({substring($param-list,2)})">
            <xsl:call-template name="loc"/>
            <xsl:call-template name="namespace-attribute">
              <xsl:with-param name="prefix" select="$prefix"/>
              <xsl:with-param name="uri" select="$namespace-uri"/>
            </xsl:call-template>
            <xsl:call-template name="namespace-attribute">
              <xsl:with-param name="prefix" select="'initializer'"/>
              <xsl:with-param name="uri" select="concat('java:',@class)"/>
            </xsl:call-template>
          </txsl:variable>
        </xsl:for-each>
      </xsl:for-each>
      <txsl:template name="file:te-initialize">
        <xsl:call-template name="namespace-attribute">
          <xsl:with-param name="prefix" select="'file'"/>
          <xsl:with-param name="uri" select="$txsl_filename"/>
        </xsl:call-template>
        <xsl:for-each select="//ctl:parser">
          <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
          <xsl:variable name="local-name" select="substring-after(@name, ':')"/>
          <xsl:variable name="namespace-uri" select="namespace::*[name()=$prefix]"/>
          <xsl:choose>
            <xsl:when test="ctl:java/@initialized = 'true'">
              <txsl:value-of
                select="te:register_parser($te:core, '{$namespace-uri}', '{$local-name}', '{ctl:java/@method}', ${@name})">
                <xsl:call-template name="loc"/>
                <xsl:copy-of select="namespace::*[name()=$prefix]"/>
              </txsl:value-of>
            </xsl:when>
            <xsl:otherwise>
              <txsl:value-of
                select="te:register_parser($te:core, '{$namespace-uri}', '{$local-name}', '{ctl:java/@method}', '{ctl:java/@class}')">
                <xsl:call-template name="loc"/>
                <xsl:copy-of select="namespace::*[name()=$prefix]"/>
              </txsl:value-of>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </txsl:template>
      <!--
                  <xsl:for-each select="//ctl:parser">
                      <xsl:variable name="prefix" select="substring-before(@name, ':')"/>
                      <xsl:choose>
                          <xsl:when test="ctl:java/@initialized = 'true'">
                              <txsl:variable name="{@name}-method" select="te:get_parser_method('{ctl:java/@method}', ${@name})">
                                  <xsl:call-template name="loc"/>
                                  <xsl:copy-of select="namespace::*[name()=$prefix]"/>
                              </txsl:variable>
                          </xsl:when>
                          <xsl:otherwise>
                              <txsl:variable name="{@name}">
                                  <xsl:call-template name="loc"/>
                                  <xsl:copy-of select="namespace::*[name()=$prefix]"/>
                              </txsl:variable>
                              <txsl:variable name="{@name}-method" select="te:get_parser_method('{ctl:java/@method}', '{ctl:java/@class}')">
                                  <xsl:call-template name="loc"/>
                                  <xsl:copy-of select="namespace::*[name()=$prefix]"/>
                              </txsl:variable>
                          </xsl:otherwise>
                      </xsl:choose>
                  </xsl:for-each>
      -->
      <xsl:apply-templates/>
    </txsl:transform>
  </xsl:template>
</xsl:transform>

