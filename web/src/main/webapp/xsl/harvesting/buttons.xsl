<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- List panel - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<xsl:template name="listButtons">
		<button class="content" onclick="harvesting.start()">
			<xsl:value-of select="/root/gui/harvesting/activate"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.stop()">
			<xsl:value-of select="/root/gui/harvesting/deactivate"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.run()">
			<xsl:value-of select="/root/gui/harvesting/run"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.remove()">
			<xsl:value-of select="/root/gui/harvesting/remove"/>
		</button>

		<p/>

		<button class="content" onclick="load('{/root/gui/locService}/admin')">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.show(SHOW.ADD)">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.refresh()">
			<xsl:value-of select="/root/gui/harvesting/refresh"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- Add panel - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<xsl:template name="addButtons">
		<button class="content" onclick="harvesting.show(SHOW.LIST)">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.newNode()">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->
	<!-- Edit panel - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<xsl:template name="editButtons">
		<button class="content" onclick="harvesting.show(SHOW.LIST)">
			<xsl:value-of select="/root/gui/strings/back"/>
		</button>
		&#160;
		<button class="content" onclick="harvesting.update()">
			<xsl:value-of select="/root/gui/harvesting/save"/>
		</button>
	</xsl:template>
	
	<!-- ============================================================================================= -->

</xsl:stylesheet>
