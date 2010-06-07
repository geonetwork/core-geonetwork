<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================== -->
	<!-- Rating popup -->
	<!-- ============================================================================== -->
	
	<xsl:template match="/">				
		<div style="padding: 0px; margin: 0px">
			<div style="background-color: #064377; color: #ffffff;" align="left">
				<h1 style="padding-bottom: 4px; margin: 0px 0px 4px 0px;">
					<xsl:value-of select="/root/gui/strings/mdRating"/>
					<img class="ratingCloser" src="{/root/gui/url}/images/fileclose.png" onClick="hideRatingPopup()"/>
				</h1>
			</div>
			
			<div align="center">
				<button class="rating" onclick="rateMetadata(5)">
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
				</button>
				<br/>
				
				<button class="rating" onclick="rateMetadata(4)">
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
				</button>
				<br/>
				
				<button class="rating" onclick="rateMetadata(3)">
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
				</button>
				<br/>
				
				<button class="rating" onclick="rateMetadata(2)">
					<img src="{/root/gui/url}/images/score.png" />
					<img src="{/root/gui/url}/images/score.png" />
				</button>
				<br/>
				
				<button class="rating" onclick="rateMetadata(1)">
					<img src="{/root/gui/url}/images/score.png" />
				</button>
			</div>
			
			<div>
				<xsl:value-of select="/root/gui/strings/ratingMsg"/>
			</div>
			
			<div id="rating.image" align="center" style="display: none;">
				<img src="{/root/gui/url}/images/loading.gif" />
			</div>
			
		</div>
		
	</xsl:template>
	
	<!-- ============================================================================== -->

</xsl:stylesheet>
