<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
	exclude-result-prefixes="fn">
<!--							-->
<!-- Collection of utilities handy in text processing.	-->
<!--							-->


	<!--									-->
	<!-- Just as substring-before, but matching the delimiter only if	-->
	<!-- it occurs after position. 						-->
	<!--									-->
	<xsl:template name="substring-before-from">
		<xsl:param name="start-position"/>		
		<xsl:param name="delimiter"/>
		<xsl:param name="string"/>
		
		<xsl:variable name="string-before-position" select="substring($string, 1, $start-position - 1)"/>
		<xsl:variable name="string-after-position" select="substring($string, $start-position)"/>
		<xsl:variable name="first-word-after-position" select="substring-before($string-after-position, $delimiter)"/>
		
		<xsl:choose>
			<xsl:when test="$first-word-after-position">
				<xsl:value-of select="concat($string-before-position, $first-word-after-position)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$string"/>	
			</xsl:otherwise>
		</xsl:choose>			
	</xsl:template>
		
	<!--										-->
	<!-- Template to add HTML hyperlinks if your text contains them; also breaks 	-->
	<!-- long words that might otherwise run outside your containing <div>.		-->
	<!--										-->
	<!-- Divide-and-conquer (DVC) version to avoid stack overflow for long texts 	-->
	<!--										-->
	<xsl:template name="addHyperlinksAndLineBreaks">
		<xsl:param name="txt"/>
		
		<xsl:choose>
			<xsl:when test="/root/gui/env/clickablehyperlinks/enable = 'true'">

				<xsl:variable name="nTxt" select="normalize-space($txt)"/>		
					
				<xsl:variable name="first-word" select="substring-before($nTxt,' ')"/>
				<xsl:variable name="rest" select="substring-after($nTxt,' ')"/>
				
				<xsl:choose>
					<!-- there is more than 1 word -->
					<xsl:when test="$first-word">
						<!-- handle first word -->
						<xsl:variable name="first-word-with-space-appended" select="concat($first-word,' ')"/>
						
						<xsl:call-template name="addHyperlinksAndLineBreaksToSingleWord">
							<xsl:with-param name="word" select="$first-word-with-space-appended"/>
						</xsl:call-template>	
						
						<!-- halve the rest, breaking at space -->
						<xsl:variable name="half-length" select="floor(string-length($rest) div 2)"/>
						
						<xsl:variable name="first-half">
							<xsl:call-template name="substring-before-from">
								<xsl:with-param name="start-position" select="$half-length"/>
								<xsl:with-param name="delimiter" select="' '"/>
								<xsl:with-param name="string" select="$rest"/>
							</xsl:call-template>
						</xsl:variable>
						
						<xsl:variable name="second-half" select="substring($rest, string-length($first-half) + 1)"/>
						
						<!-- recursively handle the first half of the rest of the words -->
						<xsl:call-template name="addHyperlinksAndLineBreaks">
							<xsl:with-param name="txt" select="$first-half"/>
						</xsl:call-template>
						
						<!-- recursively handle the second half of the rest of the words -->
						<xsl:call-template name="addHyperlinksAndLineBreaks">
							<xsl:with-param name="txt" select="$second-half"/>
						</xsl:call-template>
						
					</xsl:when>
					<!-- there is exactly 1 word -->
					<xsl:when test="$txt">
						<!-- handle the word -->
						<xsl:variable name="word-with-space-appended" select="concat($txt,' ')"/>
						<xsl:call-template name="addHyperlinksAndLineBreaksToSingleWord">
							<xsl:with-param name="word" select="$word-with-space-appended"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
				
				
				
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="$txt"/>
			</xsl:otherwise>
		</xsl:choose>			
	</xsl:template>

	<!--							-->
	<!-- Adds a <br/> if word is longer than a max length.	-->
	<!--							-->
	<xsl:template name="addLineBreaksToSingleWord">
		<xsl:param name="word"/>
		<xsl:param name="maxWordLength"/>
		<xsl:choose>
			<!-- line break if word is longer than 56 characters -->
			<xsl:when test="string-length($word) &gt; $maxWordLength">
				<xsl:value-of select="substring($word, 0, $maxWordLength)"/>
				<br/>
				<xsl:call-template name="addLineBreaksToSingleWord">
					<xsl:with-param name="word" select="substring($word, $maxWordLength)"/>
					<xsl:with-param name="maxWordLength" select="$maxWordLength"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$word"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--											-->
	<!-- Adds hyperlinks to a word and adds <br/> if word is longer than max length.	-->
	<!--											-->
	<xsl:template name="addHyperlinksAndLineBreaksToSingleWord">
		<xsl:param name="word"/>
		<xsl:variable name="maxWordLength" select="56"/>		

		<!-- if word contains ), remove remainder from processing here  -->
		<!-- this is to cope with texts containing "(http://blah.org)," -->
		<!-- (the part from the ')' is not part of the hyperlink)       -->
		<xsl:variable name="word-to-use">
			<xsl:choose>
				<xsl:when test="contains($word, ')')">
					<xsl:value-of select="substring-before($word, ')')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of  select="$word"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:choose>
			<!-- http links -->
			<xsl:when test="substring($word-to-use, 0, 8) = 'http://'">
				<a>
					<xsl:attribute name="href">
						<xsl:value-of select="$word-to-use"/>
					</xsl:attribute>
					<xsl:call-template name="addLineBreaksToSingleWord">
						<xsl:with-param name="word" select="$word-to-use"/>
						<xsl:with-param name="maxWordLength" select="$maxWordLength"/>
					</xsl:call-template>
				</a>
			</xsl:when>			
			<!-- https links -->
			<xsl:when test="substring($word-to-use, 0, 9) = 'https://'">
				<a>
					<xsl:attribute name="href">
						<xsl:value-of select="$word-to-use"/>
					</xsl:attribute>
					<xsl:call-template name="addLineBreaksToSingleWord">
						<xsl:with-param name="word" select="$word-to-use"/>
						<xsl:with-param name="maxWordLength" select="$maxWordLength"/>
					</xsl:call-template>
				</a>
			</xsl:when>
			<!-- ftp links -->
			<xsl:when test="substring($word-to-use, 0, 7) = 'ftp://'">
				<a>
					<xsl:attribute name="href">
						<xsl:value-of select="$word-to-use"/>
					</xsl:attribute>
					<xsl:call-template name="addLineBreaksToSingleWord">
						<xsl:with-param name="word" select="$word-to-use"/>
						<xsl:with-param name="maxWordLength" select="$maxWordLength"/>
					</xsl:call-template>
				</a>
			</xsl:when>
			<!-- mailto links -->
			<xsl:when test="contains($word-to-use, '@')">
				<a>
					<xsl:attribute name="href">
						<xsl:text>mailto:</xsl:text><xsl:value-of select="$word-to-use"/>
					</xsl:attribute>
					<xsl:call-template name="addLineBreaksToSingleWord">
						<xsl:with-param name="word" select="$word-to-use"/>
						<xsl:with-param name="maxWordLength" select="$maxWordLength"/>
					</xsl:call-template>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="addLineBreaksToSingleWord">
					<xsl:with-param name="word" select="$word-to-use"/>
					<xsl:with-param name="maxWordLength" select="$maxWordLength"/>
				</xsl:call-template>
			</xsl:otherwise>			
		</xsl:choose>

		<xsl:if test="contains($word, ')')">
			<xsl:text>)</xsl:text><xsl:value-of select="substring-after($word, ')')"/>
		</xsl:if>

	</xsl:template>



	<!--
		Translates CR-LF sequences into HTML newlines <p/>
		and process current line and next line to add hyperlinks.
		
		Add new line before hyperlinks because normalize-space
		remove new line information.
		
	-->
	<xsl:template name="addLineBreaksAndHyperlinks">
		<xsl:param name="txt"/>
		
		<xsl:choose>
			<xsl:when test="/root/gui/env/clickablehyperlinks/enable = 'true'">
				<xsl:choose>
					<xsl:when test="contains($txt,'&#13;&#10;')">
						<p>
							<xsl:choose>
								<xsl:when test="contains($txt,'&#13;&#10;')">
									<xsl:call-template name="addLineBreaksAndHyperlinks">
										<xsl:with-param name="txt" select="substring-before($txt,'&#13;&#10;')"/>							
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="addHyperlinksAndLineBreaks">
										<xsl:with-param name="txt" select="substring-before($txt,'&#13;&#10;')"/>							
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</p>
						<p>
							<xsl:choose>
								<xsl:when test="contains($txt,'&#13;&#10;')">
									<xsl:call-template name="addLineBreaksAndHyperlinks">
										<xsl:with-param name="txt" select="substring-after($txt,'&#13;&#10;')"/>							
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="addHyperlinksAndLineBreaks">
										<xsl:with-param name="txt" select="substring-after($txt,'&#13;&#10;')"/>							
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</p>
					</xsl:when>
					<xsl:when test="contains($txt,'&#13;')">
						<p>
							<xsl:choose>
								<xsl:when test="contains($txt,'&#13;')">
									<xsl:call-template name="addLineBreaksAndHyperlinks">
										<xsl:with-param name="txt" select="substring-before($txt,'&#13;')"/>							
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="addHyperlinksAndLineBreaks">
										<xsl:with-param name="txt" select="substring-before($txt,'&#13;')"/>							
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</p>
						<p>
							<xsl:choose>
								<xsl:when test="contains($txt,'&#13;')">
									<xsl:call-template name="addLineBreaksAndHyperlinks">
										<xsl:with-param name="txt" select="substring-after($txt,'&#13;')"/>							
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="addHyperlinksAndLineBreaks">
										<xsl:with-param name="txt" select="substring-after($txt,'&#13;')"/>							
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</p>
					</xsl:when>
					<xsl:when test="contains($txt,'&#10;')">
						<p>
							<xsl:choose>
								<xsl:when test="contains($txt,'&#10;')">
									<xsl:call-template name="addLineBreaksAndHyperlinks">
										<xsl:with-param name="txt" select="substring-before($txt,'&#10;')"/>							
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="addHyperlinksAndLineBreaks">
										<xsl:with-param name="txt" select="substring-before($txt,'&#10;')"/>							
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</p>
						<p>
							<xsl:choose>
								<xsl:when test="contains($txt,'&#10;')">
									<xsl:call-template name="addLineBreaksAndHyperlinks">
										<xsl:with-param name="txt" select="substring-after($txt,'&#10;')"/>							
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="addHyperlinksAndLineBreaks">
										<xsl:with-param name="txt" select="substring-after($txt,'&#10;')"/>							
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</p>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="addHyperlinksAndLineBreaks">
							<xsl:with-param name="txt"  select="$txt"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="contains($txt,'&#13;&#10;')">
						<p>
							<xsl:value-of select="substring-before($txt,'&#13;&#10;')"/>
						</p><p>
						<xsl:call-template name="addLineBreaksAndHyperlinks">
							<xsl:with-param name="txt"  select="substring-after($txt,'&#13;&#10;')"/>
						</xsl:call-template>
						</p>
					</xsl:when>
					<xsl:when test="contains($txt,'&#13;')">
						<p><xsl:value-of select="substring-before($txt,'&#13;')"/>
						</p><p>
						<xsl:call-template name="addLineBreaksAndHyperlinks">
							<xsl:with-param name="txt"  select="substring-after($txt,'&#13;')"/>
						</xsl:call-template>
						</p>
					</xsl:when>
					<xsl:when test="contains($txt,'&#10;')">
						<p><xsl:value-of select="substring-before($txt,'&#10;')"/>
						</p><p>
						<xsl:call-template name="addLineBreaksAndHyperlinks">
							<xsl:with-param name="txt"  select="substring-after($txt,'&#10;')"/>
						</xsl:call-template>
						</p>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$txt"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	

</xsl:stylesheet> 