<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="main.xsl" />
    <xsl:include href="mapfish_includes.xsl" />
    <xsl:include href="extent-util.xsl" />

    <xsl:template mode="css" match="/">
        <script type="text/javascript">
            window.gMfLocation = '<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/';
        </script>
        <xsl:call-template name="geoCssHeader"/>
        <xsl:call-template name="mapfish_css_includes"/>
    </xsl:template>
            
    <xsl:template mode="script" match="/">
    
        <!-- To avoid an interaction with prototype and ExtJs.Tooltip, should be loaded before ExtJs -->
        <xsl:choose>
            <xsl:when test="/root/request/debug">
                <script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"></script>
            </xsl:when>
            <xsl:otherwise>
              <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.js"></script>      
            </xsl:otherwise>
        </xsl:choose>
    
        <xsl:call-template name="geoHeader"/>
        <xsl:call-template name="mapfish_script_includes"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/translation_edit.js"/>
        <script language="JavaScript1.2" type="text/javascript">
            var currentCrs = 0;

            var latlong= new OpenLayers.Projection("EPSG:4326");
            var ch03= new OpenLayers.Projection("EPSG:21781");

            function init() {
                editI18n.init('desc', '<xsl:value-of select="/root/gui/language"/>', updateSubmitButton);
                editI18n.init('geoId', '<xsl:value-of select="/root/gui/language"/>', updateSubmitButton);
                
                currentCrs = $('crsChooser').selectedIndex;

                // Creates map component
                mapCmp = new MapComponent('olMap', {displayLayertree: false});
                drawCmp = new MapDrawComponent(mapCmp.map, {
                    toolbar: mapCmp.toolbar,
                    controlOptions: {
                        title: 'Draw shape',
                        featureAdded: writeGeometry
                    },
                    activate: true,
                    onClearFeatures: function() {
                        $('geom').value = '';
                    }
                });

                // Form behaviour
                Ext.get('geom').on('keyup', function() {
                    readGeometry();
                });


                Ext.each(Ext.get('editform').dom.format, function(i) {
                    Ext.get(i).on('click', function() {
                        writeGeometry();
                    });
                });
                
                Ext.get('crsChooser').on('change',  function() {
                    transformGeometry();
                    updateSubmitButton();
                });

                Ext.get('editform').on('reset', function() {
                    updateSubmitButton();
                    setTimeout(readGeometry, 250);
                });
                
                readGeometry();
                drawCmp.zoomToFeatures();

                updateSubmitButton();
            }


            function submitButton() {return Ext.get('submitButton');}
            function updateSubmitButton(){
                var desc = Ext.get('descDE').dom.value + Ext.get('descIT').dom.value + Ext.get('descFR').dom.value + Ext.get('descEN').dom.value + '';
                var geom = Ext.get('geom').dom.value || '';

                var enabled = desc.length&gt;0 &amp;&amp; geom.length&gt;0;

                submitButton().dom.disabled = !enabled;

            }

            function writeGeometry() {
                var format = getSelectedFormat();

                var options = {};
                options.format = getSelectedFormat();
                if(  $('crsChooser').selectedIndex == 1 ){
                    options.to = latlong;
                    options.from = ch03;
                }
                Ext.get('geom').dom.value = drawCmp.writeFeature(options);
                Ext.get('geom').highlight("#CDDCF3");
                updateSubmitButton();
            }

            function readGeometry() {
                drawCmp.destroyFeatures();
                var options = {};
                options.format=getSelectedFormat();
                if(  $('crsChooser').selectedIndex == 1 ){
                    options.to = ch03;
                    options.from = latlong;
                }
                var wkt = $('geom').value;
                drawCmp.readFeature(wkt, options);
                updateSubmitButton();
            }
            function getSelectedFormat() {
                return Ext.get('WKT').dom.checked ? 'WKT' : 'GML';
            }

            function transformGeometry() {
                var crsChooser = $('crsChooser');
                if (crsChooser.selectedIndex == currentCrs )
                    return;

                currentCrs = crsChooser.selectedIndex;

                var formatString = getSelectedFormat() || 'WKT';
                var format = new OpenLayers.Format[formatString];

                var from,to;
                if( currentCrs == 0 ) {
                    from = latlong;
                    to = ch03;
                } else {
                    from = ch03;
                    to = latlong;
                }

                var feature = format.read(Ext.get('geom').dom.value);
                if(feature !=null ){
                 format.internalProjection = from;
                 format.externalProjection = to;


                    Ext.get('geom').highlight("#CDDCF3");
                 Ext.get('geom').dom.value = format.write(feature);
                }
            }
            
            function submitExtent(){
               var ok = confirm("This request will return quickly but all the metadata that reference this extent must be indexed, this means this might take
                        a significant amount of time before the index is consistent with this change.  Please be patient as it could take
                        10-15 minutes if there are many metadata that reference this extent.  Once the indexing is complete, the spatial
                        searches will use the new extent in their searches.\n\nPress Ok to continue.");

               if (!ok) {
                   return
               }
               submitButton().addClass('loading');
               Ext.get('resetButton').setVisible(false);
			   new Ajax.Request($('editform').action,
				{
		            method: 'post',
		            parameters: $('editform').serialize(true),
		            onSuccess: function(req) {
		                window.close();
		            },
		            onFailure: function(req) {
                        submitButton().removeClass('loading');
                        Ext.get('resetButton').setVisible(true);
                        alert(translate("errorSaveFailed") + "/ status " + req.status + " text: " + req.statusText + " - " + translate("tryAgain"));
		            }
			    });
            }
        </script>
    </xsl:template>
	<xsl:template name="content">

	    <xsl:variable name="wfs" select="/root/request/wfs"/>
	    <xsl:variable name="typename" select="/root/request/typename"/>
	    <xsl:variable name="id" select="/root/request/id"/>
	     <xsl:variable name="action">
	        <xsl:choose>
		        <xsl:when test="string-length($id)>0">
		            <xsl:value-of select="concat(/root/gui/locService,'/xml.extent.update?wfs=',$wfs,'&amp;typename=',$typename,'&amp;id=',$id)" />
			    </xsl:when>
		        <xsl:otherwise>
		            <xsl:value-of select="concat(/root/gui/locService,'/xml.extent.add?wfs=',$wfs,'&amp;typename=',$typename)" />
		        </xsl:otherwise>
		    </xsl:choose>
	    </xsl:variable>
    
	   <FORM id="editform" action="{$action}" method="post" style="padding: 20px">
   	    <P>
   	       <table>
                  <tr><td>Geo Id:</td><td>
                  <xsl:call-template name="translationWidgetInputs">
                       <xsl:with-param name="key" select="'geoId'"/>
                       <xsl:with-param name="size" select="50"/>
                       <xsl:with-param name="root" select="/root/response/wfs/featureType/feature/geoId"/>
                  </xsl:call-template>
                   </td><td>
                  <xsl:call-template name="translationWidgetSelect">
                       <xsl:with-param name="key" select="'geoId'"/>
                  </xsl:call-template>     
                  </td></tr>
   	           <tr><td>Description:</td><td>
   	           
                  <xsl:call-template name="translationWidgetInputs">
                       <xsl:with-param name="key" select="'desc'"/>
                       <xsl:with-param name="size" select="50"/>
                       <xsl:with-param name="root" select="/root/response/wfs/featureType/feature/desc"/>
                  </xsl:call-template>
                   </td><td>
   	           <xsl:call-template name="translationWidgetSelect">
                       <xsl:with-param name="key" select="'desc'"/>
                  </xsl:call-template>				
   	           </td></tr>
	           <tr><td>Geometry:</td><td><textarea name="geom" id="geom" class="content" cols="50" rows="4"><xsl:value-of select="/root/response/wfs/featureType/feature/geom"/></textarea></td> 
	           <td>
	              <select class="content" name="crs" id="crsChooser">
	                  <option value="EPSG:21781" selected="true">CHO3 (EPSG:21781)</option>
                         <option value="EPSG:4326">WGS 84 (EPSG:4326)</option>
                     </select>
	           </td></tr>
		       <tr><td><br></br><INPUT class="button" type="button" onclick="submitExtent()" id="submitButton" value="Send"/>&#160;<INPUT class="button" id="resetButton" type="reset"/>&#160;</td>
		           <td style="text-align: center"><br/><INPUT class="content" type="radio" name="format" id="WKT" value="WKT" checked="checked"/> WKT
	                   &#160;<INPUT class="content" type="radio" name="format" value="GML2"/> GML</td></tr>
                  <tr><td colspan="3"><div id="olMap"></div></td></tr>
           </table>
   	    </P>
       </FORM>
    </xsl:template>
</xsl:stylesheet>
