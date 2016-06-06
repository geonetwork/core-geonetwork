<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                version="1.0" exclude-result-prefixes="exslt">

  <!--
  parses an expression into a parse tree
  -->
  <xsl:template name="parse">
    <xsl:param name="expr"/>
    <xsl:param name="currOper" select="'and'"/>

    <xsl:variable name="tokens">
      <xsl:call-template name="tokenize">
        <xsl:with-param name="expr" select="$expr"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="doParse">
      <xsl:with-param name="expr" select="exslt:node-set($tokens)"/>
    </xsl:call-template>
  </xsl:template>

  <!--
  recursive parser
  -->
  <xsl:template name="doParse">
    <xsl:param name="expr"/>
    <xsl:param name="currOper"/>
    <xsl:variable name="first" select="$expr/*[1]"/>
    <xsl:variable name="second" select="$expr/*[2]"/>
    <xsl:variable name="oper">
      <xsl:choose>
        <xsl:when test="$second/@type='oper'">
          <xsl:value-of select="$second/@text"/>
        </xsl:when>
        <xsl:otherwise>and</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="rest">
      <xsl:choose>
        <xsl:when test="$second/@type='oper'">
          <xsl:copy-of select="$expr/*[position()>2]"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$expr/*[position()>1]"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>

      <!-- empty token list: do nothing -->
      <xsl:when test="not(boolean($first))"/>

      <!-- last token in list: just return first token -->
      <xsl:when test="not(boolean($second))">
        <xsl:copy-of select="$first"/>
      </xsl:when>

      <!-- two tokens, and operator is the current one: just add first token to the list -->
      <xsl:when test="$oper=$currOper">
        <xsl:copy-of select="$first"/>
        <xsl:call-template name="doParse">
          <xsl:with-param name="expr" select="exslt:node-set($rest)"/>
          <xsl:with-param name="currOper" select="$oper"/>
        </xsl:call-template>
      </xsl:when>

      <!-- two tokens, and operator not the current one: just add new list and recurse on it -->
      <xsl:otherwise>
        <tree type="{$oper}">
          <xsl:copy-of select="$first"/>
          <xsl:call-template name="doParse">
            <xsl:with-param name="expr" select="exslt:node-set($rest)"/>
            <xsl:with-param name="currOper" select="$oper"/>
          </xsl:call-template>
        </tree>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!--
  makes a parse tree binary
  -->
  <xsl:template name="binarize">
    <xsl:param name="expr"/>

    <xsl:call-template name="binarizeExpr">
      <xsl:with-param name="expr" select="exslt:node-set($expr)/*"/>
    </xsl:call-template>
  </xsl:template>

  <!--
  base step
  -->
  <xsl:template name="binarizeExpr">
    <xsl:param name="expr"/>

    <xsl:choose>
      <xsl:when test="name($expr)='tree'">
        <xsl:call-template name="doBinarize">
          <xsl:with-param name="type" select="string($expr/@type)"/>
          <xsl:with-param name="children" select="$expr/*"/>
        </xsl:call-template>
      </xsl:when>

      <xsl:otherwise>
        <xsl:copy-of select="$expr"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  recursive binary tree maker
  -->
  <xsl:template name="doBinarize">
    <xsl:param name="type"/>
    <xsl:param name="children"/>

    <xsl:choose>

      <xsl:when test="count($children)=0"/>

      <xsl:when test="count($children)=1">
        <xsl:call-template name="binarizeExpr">
          <xsl:with-param name="expr" select="$children[1]"/>
        </xsl:call-template>
      </xsl:when>

      <xsl:when test="count($children)=2">
        <tree type="{$type}">
          <xsl:call-template name="binarizeExpr">
            <xsl:with-param name="expr" select="$children[1]"/>
          </xsl:call-template>
          <xsl:call-template name="binarizeExpr">
            <xsl:with-param name="expr" select="$children[2]"/>
          </xsl:call-template>
        </tree>
      </xsl:when>

      <xsl:otherwise>
        <tree type="{$type}">
          <xsl:call-template name="binarizeExpr">
            <xsl:with-param name="expr" select="$children[1]"/>
          </xsl:call-template>
          <xsl:call-template name="doBinarize">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="children" select="$children[position()>1]"/>
          </xsl:call-template>
        </tree>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  tokenizes an expression into a sequence of tokens <token text="$text" type="$type"/>
  where $text is the token text
  and $type is string | qstring | oper
  -->
  <xsl:template name="tokenize">
    <xsl:param name="expr"/>

    <xsl:variable name="nExpr" select="normalize-space($expr)"/>
    <xsl:choose>
      <xsl:when test="starts-with($nExpr,'&quot;')">
        <xsl:variable name="expr2" select="substring-after($nExpr,'&quot;')"/>
        <xsl:call-template name="newToken">
          <xsl:with-param name="text" select="substring-before($expr2,'&quot;')"/>
          <xsl:with-param name="type" select="'qstring'"/>
        </xsl:call-template>
        <xsl:call-template name="tokenize">
          <xsl:with-param name="expr" select="substring-after($expr2,'&quot;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="first" select="substring-before($nExpr,' ')"/>
        <xsl:choose>
          <xsl:when test="$first">
            <xsl:call-template name="newToken">
              <xsl:with-param name="text" select="$first"/>
            </xsl:call-template>
            <xsl:call-template name="tokenize">
              <xsl:with-param name="expr" select="substring-after($nExpr,' ')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$expr">
            <xsl:call-template name="newToken">
              <xsl:with-param name="text" select="$nExpr"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- creates a new token -->
  <xsl:template name="newToken">
    <xsl:param name="text"/>
    <xsl:param name="type" select="'string'"/>

    <xsl:variable name="nText" select="normalize-space($text)"/>
    <xsl:variable name="opText" select="translate($nText,'ADNORT','adnort')"/>
    <xsl:choose>

      <!-- quoted string -->
      <xsl:when test="$type='qstring'">
        <token text="{$nText}" type="qstring"/>
      </xsl:when>

      <!-- operator -->
      <xsl:when test="$opText='and' or $opText='or' or $opText='not'">
        <token text="{$opText}" type="oper"/>
      </xsl:when>

      <!-- prefix string -->
      <xsl:when test="substring($nText,string-length($nText),1)='*'">
        <token text="{substring($nText,1,string-length($nText)-1)}" type="pstring"/>
      </xsl:when>

      <!-- simple string -->
      <xsl:otherwise>
        <token text="{$nText}" type="string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
