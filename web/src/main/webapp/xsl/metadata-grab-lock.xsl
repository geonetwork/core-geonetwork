<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="modal.xsl"/>
    <xsl:variable name="profile"  select="/root/gui/session/profile"/>

    <xsl:template name="content">
        <xsl:call-template name="formLayout">
            <xsl:with-param name="title" select="/root/gui/strings/grab.lock"/>
            <xsl:with-param name="content">

                <xsl:variable name="lang" select="/root/gui/language"/>
                <xsl:variable name="disabled" select="(/root/response/owner='false')"/>

                <div id="users" style="padding:20px;">
                    <input name="id" id="id" type="hidden" value="{/root/response/id}"/>

                    <table>
                        <tr>
                            <th class="padded"><b><xsl:value-of select="/root/gui/strings/username"/></b></th>
                            <th class="padded"><b><xsl:value-of select="/root/gui/strings/surName"/></b></th>
                            <th class="padded"><b><xsl:value-of select="/root/gui/strings/firstName"/></b></th>
                            <th class="padded"><b><xsl:value-of select="/root/gui/strings/profile"/></b></th>
                        </tr>

                        <xsl:for-each select="/root/response/record">
                            <xsl:variable name="profileId"><xsl:value-of select="profile"/></xsl:variable>
                            <tr>
                                <td class="padded"><xsl:value-of select="username"/></td>
                                <td class="padded"><xsl:value-of select="surname"/></td>
                                <td class="padded"><xsl:value-of select="name"/></td>
                                <td class="padded"><xsl:value-of select="/root/gui/strings/profileChoice[@value=$profileId]"/></td>
                                <td class="padded">
                                    <xsl:choose>
                                        <xsl:when test="id = /root/request/currentLockOwner">
                                            <input type="radio" name="userId" value="{id}" id="u{id}" disabled="disabled"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <input type="radio" name="userId" value="{id}" id="u{id}"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                        </xsl:for-each>

                        <xsl:if test="not($disabled)">
                            <tr width="100%">
                                <td align="left" colspan="8" style="padding-top:20px;">
                                    <button class="content" onclick="radioModalUpdate('users','metadata.grab.lock');"><xsl:value-of select="/root/gui/strings/submit"/></button>
                                </td>
                            </tr>
                        </xsl:if>
                    </table>
                </div>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>