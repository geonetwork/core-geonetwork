<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:import href="modal.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/status"/>
			<xsl:with-param name="content">

				<div id="status" align="center">
					<xsl:if test="/root/response/statusvalue/*">

							<input name="id" type="hidden" value="{/root/response/id}"/>
							<table>
								<tr>
									<th class="padded" align="center" colspan="2"><xsl:value-of select="/root/gui/strings/status"/></th>
								</tr>

								<xsl:variable name="lang" select="/root/gui/language"/>

								<!-- loop on all status -->
								<xsl:variable name="isSubmitted"
									select="count(/root/response/statusvalue/status[on and name='submitted']) = 1"/>
								<xsl:variable name="profile" select="/root/gui/session/profile"/>
								<xsl:variable name="userId" select="/root/gui/session/userId"/>
								<xsl:variable name="isReviewerOrAdmin" select="
									count(/root/response/contentReviewers/record[userid=$userId]) > 0
									or $profile = 'Administrator'"/>

								<!--<ul>
									<li>Profile: <xsl:value-of select="$profile"/></li>
									<li>User: <xsl:value-of select="$userId"/></li>
									<li>Reviewer: <xsl:value-of select="$isReviewerOrAdmin"/></li>
									<li>Has edit perm: <xsl:value-of select="/root/response/hasEditPermission"/></li>
									<li>Owner: <xsl:value-of select="/root/response/isOwner"/></li>
								</ul>-->

								<xsl:for-each select="/root/response/statusvalue/status">
								  <xsl:sort select="displayorder"/>
								  <xsl:sort select="label/child::*[name() = $lang]"/>
									<tr>
										<td class="padded" align="left" colspan="2">


											<input type="radio" name="status" value="{id}" id="st{id}">
												<xsl:if test="on">
													<xsl:attribute name="checked"/>
												</xsl:if>
												<!-- status value submitted is disabled for reviewers
												<xsl:if test="$isReviewer or contains($profile,'Admin')">-->

												<!-- status value draft and unkown disabled once submitted -->
												<xsl:if test="$isSubmitted and not($isReviewerOrAdmin)">
													<xsl:if test="name='unknown' or name='draft'">
														<xsl:attribute name="disabled"/>
													</xsl:if>
												</xsl:if>

												<!-- some status values are not available to Editors -->
												<xsl:if test="(/root/response/hasEditPermission = 'true' or
													/root/response/isOwner = 'true')
													and not($isReviewerOrAdmin)">
													<xsl:if test="name='approved' or name='retired' or name='rejected'">
														<xsl:attribute name="disabled"/>
													</xsl:if>
												</xsl:if>
												<label for="st{id}">
													<xsl:value-of select="label/child::*[name() = $lang]"/>
												</label>
											</input>
										</td>
									</tr>
								</xsl:for-each>
								<tr width="100%">
									<td align="left">
										<xsl:value-of select="/root/gui/strings/changeLogMessage"/>
									</td>
									<td align="left">
										<textarea rows="8" cols="25" id="changeMessage" name="changeMessage"><xsl:value-of select="/root/gui/strings/defaultStatusChangeMessage"/></textarea>
									</td>
								</tr>
								<tr width="100%">
									<td align="center" colspan="2">
										<xsl:choose>
											<xsl:when test="contains(/root/gui/reqService,'metadata.batch')">
												<button class="content" onclick="radioModalUpdate('status','metadata.batch.update.status','true','{concat(/root/gui/strings/results,' ',/root/gui/strings/batchUpdateStatusTitle)}')"><xsl:value-of select="/root/gui/strings/submit"/></button>
											</xsl:when>
											<xsl:otherwise>
												<button class="content" onclick="radioModalUpdate('status','metadata.status');"><xsl:value-of select="/root/gui/strings/submit"/></button>
											</xsl:otherwise>
										</xsl:choose>
									</td>
								</tr>
							</table>
					</xsl:if>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
