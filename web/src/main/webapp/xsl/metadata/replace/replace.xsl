<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="../../modal.xsl"/>

  <xsl:variable name="profile" select="/root/gui/session/profile"/>

  <xsl:template name="replace-script">

    <script>

      var sectionMetadataFields = {"FIELDS":[
      <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'metadata']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};

      var sectionIdentificationFields = {"FIELDS":[
      <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'identification']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};


      var sectionDataIdentificationFields = {"FIELDS":[
      <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'dataIdentification']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};


      var sectionServiceIdentificationFields = {"FIELDS":[
      <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'serviceIdentification']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};

      var sectionMaintenanceInformationFields =
      {"FIELDS":[
      <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'maintenanceInformation']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};

      var sectionContentInformationFields =
      {"FIELDS":[
      <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'contentInformation']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};

      var sectionDistributionInformationFields =
      {"FIELDS":[
      <xsl:for-each
          select="/root/gui/massive-replace/massiveReplaceForm/section[@id = 'distributionInformation']/field">
        <xsl:if test="position() != 1">,</xsl:if>{"NAME":"<xsl:value-of select="."/>","KEY":"<xsl:value-of
          select="@key"/>"}
      </xsl:for-each>
      ]};
    </script>
  </xsl:template>


  <xsl:template name="jsHeader"/>

  <!-- ================================================================================= -->
  <!-- page content -->
  <!-- ================================================================================= -->

  <xsl:template name="content">

    <xsl:call-template name="formLayout">
      <xsl:with-param name="title" select="/root/gui/massive-replace/massiveUpdateForm/title"/>
      <xsl:with-param name="content">

        <xsl:call-template name="replace-script"/>
        <form id="massiveupdateform" name="massiveupdateform" accept-charset="UTF-8" method="POST"
              action="{/root/gui/locService}/metadata.massive.update.content">

          <h1 style="padding: 5px">
            <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/subtitle"/>
          </h1>

          <!-- Instructions -->
          <div id="massivereplace-instructions-panel">
            <xsl:copy-of select="/root/gui/massive-replace/massiveReplaceForm/description"/>
          </div>

          <!-- Panel to configure/add a replacement -->
          <div id="massivereplace-add-replacement-panel">
            <div style="margin-left: 10px">
              <!-- Section -->
              <label for="section">
                <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/package"/>
              </label>
              <br/>

              <select name="mdsection" id="mdsection" onchange="massiveMetadataReplace_updateFields(this.value)">
                <xsl:for-each select="/root/gui/massive-replace/massiveReplaceForm/section">
                  <option value="{@id}">
                    <xsl:value-of select="@label"/>
                  </option>
                </xsl:for-each>
              </select>
              <br/>
              <br/>

              <!-- Element to replace -->
              <label for="mdfield">
                <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/element"/>
              </label>
              <br/>
              <select id="mdfield" name="mdfield" style="width: 460px;">

              </select>
              <br/>
              <br/>

              <!-- Search value -->
              <label for="searchValue">
                <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/searchText"/>
              </label>
              <br/>
              <input type="text" name="searchValue" id="searchValue" value="" size="70"/>
              <br/>
              <br/>

              <!-- Replace value -->
              <label for="replaceText">
                <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/replaceText"/>
              </label>
              <br/>
              <input type="text" name="replaceValue" id="replaceValue" value="" size="70"/>
              <br/>
              <br/>
              <button type="button" class="content" onclick="massiveMetadataReplace_addRow();">Add replacement</button>
              <br/>
              <br/>
            </div>
          </div>

          <!-- List of replacements -->
          <div id="massivereplace-container">

            <hr/>
            <input type="hidden" name="test" id="test" value="false"/>

            <h2>
              <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/replacements-defined"/>
            </h2>
            <p id="noReplacements">
              <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/no-replacements-defined"/>
            </p>

            <table id="massivereplace-updates" style="display: none">
              <colgroup>
                <col span="1" style="width: 15%;"/>
                <col span="1" style="width: 40%;"/>
                <col span="1" style="width: 20%;"/>
                <col span="1" style="width: 20%;"/>
                <col span="1" style="width: 5%;"/>
              </colgroup>
              <tbody>
                <tr>
                  <th>
                    <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/package"/>
                  </th>
                  <th>
                    <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/element"/>
                  </th>
                  <th>
                    <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/searchText"/>
                  </th>
                  <th>
                    <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/replaceText"/>
                  </th>
                  <th>
                    &#160;
                  </th>

                </tr>
              </tbody>
            </table>


            <input type="checkbox" name="caseinsensitive" value="i"/>
            <label for="caseinsensitive" style="font-weight:bold;">
              <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/caseInsensitive"/>
            </label>
          </div>
        </form>



        <script>
          massiveMetadataReplace_updateFields($("mdsection").value);
        </script>

      </xsl:with-param>

      <xsl:with-param name="buttons">
        <div id="massivereplace-buttons">
          <center>
            <button class="content"
                    onclick="massiveMetadataReplace_execute('metadata.batch.replace')"
                    type="button">
              <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/update"/>
            </button>
            &#160;
            <button class="content"
                    onclick="massiveMetadataReplace_test('metadata.batch.replace')"
                    type="button">
              <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/test"/>
            </button>
            <span id="MB_loading" style="display:none;">&#160;</span>
          </center>
        </div>
      </xsl:with-param>
    </xsl:call-template>

    <!-- Results panel -->
    <div id="massivereplace-results-container">
    </div>
  </xsl:template>


  <!-- ================================================================================= -->

</xsl:stylesheet>
