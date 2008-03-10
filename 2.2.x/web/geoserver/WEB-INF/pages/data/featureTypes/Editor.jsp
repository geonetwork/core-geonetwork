<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<%
//This file contains some ugly JSP code.
//Be wary, ye who brave the dragon's lair.
%>

<script type="text/javascript">
<!--
function addStyle(){
	for(i=0;i<document.typesEditorForm.panelStyleIds.length;i++){
		if(document.typesEditorForm.panelStyleIds.options[i].selected == true){
 			new_element = new Option(document.typesEditorForm.panelStyleIds.options[i].value,document.typesEditorForm.panelStyleIds.options[i].value,false,false);
 			document.typesEditorForm.otherSelectedStyles.options[document.typesEditorForm.otherSelectedStyles.length] = new_element;
 		}
 	}
}
function removeStyle(){
	var selected=0;
	for(i=0;i<document.typesEditorForm.otherSelectedStyles.length;i++){
		if(document.typesEditorForm.otherSelectedStyles.options[i].selected == true){
			selected++;
		}
		if(selected>0){
			for(i=0;i<document.typesEditorForm.otherSelectedStyles.length;i++){
				if(document.typesEditorForm.otherSelectedStyles.options[i].selected == true){
					document.typesEditorForm.otherSelectedStyles.options[i] = null;
				}
			}
		removeStyle();
		}
	}
}

function prepareFormData(){
	for(i=0;i<document.typesEditorForm.otherSelectedStyles.length;i++){
		document.typesEditorForm.otherSelectedStyles.options[i].selected = true;
	}
}
//-->
</script>

<% try { %>
<html:form action="/config/data/typeEditorSubmit" onsubmit="prepareFormData()">
  <table class="info">
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.name"/>">
          <bean:message key="label.name"/>:
        </span>
      </td>
      <td class="datum">
		<bean:write name="typesEditorForm" property="typeName"/>
      </td>
    </tr>
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.style"/>">
          <bean:message key="label.style"/>:
        </span>
      </td>
      <td class="datum">
        <html:select property="styleId">
          <html:options property="styles"/>
        </html:select> 
        <html:submit property="action">
          <bean:message key="config.data.sldWizard.label"/>
        </html:submit><br>
      </td>
    </tr>    
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.type.style.additional"/>">
          <bean:message key="label.style.additional"/>:
        </span>
      </td>
      <td class="datum">
        <table>
        	<tr>
        		<td>
        			<html:select property="panelStyleIds" multiple="multiple">
          				<html:options property="styles"/>
        			</html:select>
        		</td>
        		<td style="font-size:10">
        			<input type="button" value=">>" style="width:30" onClick="addStyle()">
        			<br>
        			<input type="button" value="<<" style="width:30" onClick="removeStyle()">
        		</td>
        		<td>
        			<html:select property="otherSelectedStyles" style="width:130" multiple="multiple">
        				<html:options property="typeStyles"/>
        			</html:select>
        		</td>
        	</tr>
        </table> 
      </td>
    </tr>    
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.srs"/>">
          <bean:message key="label.SRS"/>:
        </span>
      </td>
	  <td class="datum">
	  <table>
	  <tr>
	  <td>
		<html:text property="SRS" size="32"/>
		</td>
		<td>
        <html:submit property="action">
          <bean:message key="config.data.lookupSRS.label"/>
        </html:submit>
        </td>
		<td>
		    <a href="<bean:message key="label.SRSHelp.URL"/>">
              <bean:message key="label.SRSHelp"/>
            </a>
        </td>
        <td>
        &nbsp;-&nbsp;
        <a href="../../../srsHelp.do">
              <bean:message key="label.SRSList"/>
            </a>
        </td>
       
        </tr>
        </table>
	</td></tr>
	<!------------------------->
	
	
	
	<!------ This puts in the SRS WKT definition --->
	
	<tr>
	<td class="label">
		<span class="help" title="<bean:message key="help.type.srswkt"/>">
          <bean:message key="label.type.srswkt"/>:
        </span>
	  </td>
	  <td class="greyedOut2">
              <bean:write name="typesEditorForm" property="SRSWKT"/>
            </td>
	</tr>
	
	<tr>
	<td class="label">
		<span class="help" title="<bean:message key="label.type.crswkt"/>">
          <bean:message key="label.type.crswkt"/>:
        </span>
	  </td>
	  <td class="greyedOut2">
              <bean:write name="typesEditorForm" property="nativeSRSWKT"/>
            </td>
	</tr>
	
	<tr>
	
	<td class="label">
		<span class="help" title="<bean:message key="label.type.srshandling"/>">
          <bean:message key="label.type.srshandling"/>:
        </span>
	  </td>
	  <td class="greyedOut2">
              <html:select property="srsHandling">
	             <html:options property="allSrsHandling"/>
              </html:select>
      </td>
	</tr>
	
	
	<!-------------------------->

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.title"/>">
          <bean:message key="label.title"/>:
        </span>
	  </td>
	  <td class="datum">
		<html:text property="title" size="60"/>
	  </td>
	</tr>

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.bbox"/>">
          <bean:message key="label.bbox"/>:          
        </span>
	  </td>
	  <td class="datum">
        <html:submit property="action">
          <bean:message key="config.data.calculateBoundingBox.label"/>
        </html:submit><br/>
        
      <!-------------------------------------------------------------->
      <!----- This will add the bounding box of the data (in its CRS) to the page --->
      <!----- Its only added if its actually been calculated (i.e. they presed the generate bbox button) --->

<html:hidden property="dataMinX"/>
<html:hidden property="dataMinY"/>
<html:hidden property="dataMaxX"/>
<html:hidden property="dataMaxY"/>

<logic:notEmpty name="typesEditorForm"  property="dataMinX">
        <table border=0 width=90%>
          <tr>
            <td class="greyedOut">
              <span class="help" title="<bean:message key="help.type.dataminx"/>">
                <bean:message key="label.type.dataminx"/>:
              </span>
            </td>
            <td class="greyedOut">
              <bean:write name="typesEditorForm" property="dataMinX"/>
            </td>
            <td class="greyedOut">
              <span class="help" title="<bean:message key="help.type.dataminy"/>">
                <bean:message key="label.type.dataminy"/>:
              </span>
            </td>
            <td class="greyedOut">
              <bean:write name="typesEditorForm" property="dataMinY"/>
            </td>
          </tr>
          <tr>
            <td class="greyedOut">
              <span class="help" title="<bean:message key="help.type.datamaxx"/>">
                <bean:message key="label.type.datamaxx"/>:
              </span>
            </td>
            <td class="greyedOut">
             <bean:write name="typesEditorForm" property="dataMaxX"/>
            </td>
            <td class="greyedOut">
              <span class="help" title="<bean:message key="help.type.datamaxy"/>">
                <bean:message key="label.type.datamaxy"/>:
              </span>
            </td>
            <td class="greyedOut">
             <bean:write name="typesEditorForm" property="dataMaxY"/>
            </td>
          </tr>
        </table>
        
</logic:notEmpty>        
<!-------------------------------------------------------------->
        
        
        <table border=0>
          <tr>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.type.minx"/>">
                <bean:message key="label.type.minx"/>:
              </span>
            </td>
            <td>
              <html:text property="minX" size="15"/>
            </td>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.type.miny"/>">
                <bean:message key="label.type.miny"/>:
              </span>
            </td>
            <td>
              <html:text property="minY" size="15"/>
            </td>
          </tr>
          <tr>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.type.maxx"/>">
                <bean:message key="label.type.maxx"/>:
              </span>
            </td>
            <td>
              <html:text property="maxX" size="15"/>
            </td>
            <td style="white-space: nowrap;">
              <span class="help" title="<bean:message key="help.type.maxy"/>">
                <bean:message key="label.type.maxy"/>:
              </span>
            </td>
            <td>
              <html:text property="maxY" size="15"/>
            </td>
          </tr>
        </table>
	  </td>
    </tr>

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.dataFeatureTypeKeywords"/>">
			<bean:message key="label.keywords"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="keywords" cols="60" rows="2"/>
	  </td>
    </tr>

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.dataFeatureTypeAbstract"/>">
			<bean:message key="label.abstract"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:textarea property="abstract" cols="60" rows="3"/>
      </td>
    </tr>
    
	<tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.wms.path"/>">
          <bean:message key="label.wms.path"/>:
        </span>
	  </td>
	  <td class="datum">
		<html:text property="wmsPath" size="60"/>
	  </td>
	</tr>

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.dataFeatureTypeMetadataURLs"/>">
			<bean:message key="label.metadataURL"/>:
		</span>
	  </td>
	  <td class="datum">
	  	  <table cellspacing="0">
	  	  	<tr>
	  	  	  <td align="left">
				<span class="help" title="<bean:message key="help.dataFeatureTypeMetadataURLType"/>">
					<bean:message key="label.metadataURLType"/>
				</span>
			  </td>
			  <td align="left">
				<span class="help" title="<bean:message key="help.dataFeatureTypeMetadataURLFormat"/>">
					<bean:message key="label.metadataURLFormat"/>
				</span>
			  </td>
			  <td align="left">
				<span class="help" title="<bean:message key="help.dataFeatureTypeMetadataURLLink"/>">
					<bean:message key="label.metadataURLLink" />
				</span>
			  </td>
	  	  	</tr>
	  	  	<tr>
	  	  	  <td class="datum">
	  	  	  	<html:select property="metadataLink[0].metadataType">
	  	  	  	  <html:options property="allMetadataURLTypes"/>
	  	  	  	</html:select>
	  	  	  </td>
	  	  	  <td class="datum">
	  	  	  	<html:text property="metadataLink[0].type"/>
	  	  	  </td>
	  	  	  <td class="datum">
	  	  	  	<html:text property="metadataLink[0].content" size="60"/>
	  	  	  </td>
	  	  	</tr>
	  	  	<tr>
	  	  	  <td class="datum">
	  	  	  	<html:select property="metadataLink[1].metadataType">
	  	  	  	  <html:options property="allMetadataURLTypes"/>
	  	  	  	</html:select>
	  	  	  </td>
	  	  	  <td class="datum">
	  	  	  	<html:text property="metadataLink[1].type"/>
	  	  	  </td>
	  	  	  <td class="datum">
	  	  	  	<html:text property="metadataLink[1].content" size="60"/>
	  	  	  </td>
	  	  	</tr>
	  	  </table>
	  </td>
    </tr>
    
    <tr>
	    <td class="label">
    		<span class="help" title="<bean:message key="help.type.cachingEnabled" />">
    			<bean:message key="label.cachingEnabled" />:
    		</span>
    	</td>
    	<td class="datum">
    		<html:checkbox property="cachingEnabled" />
    	</td>
    </tr>
    
    <tr>
    	<td class="label">
    		<span class="help" title="<bean:message key="help.type.cacheMaxAge" />">
    			<bean:message key="label.cacheMaxAge" />:
    		</span>
    	</td>
    	<td class="datum">
    		<html:text property="cacheMaxAge" size="10" />
    	</td>
    </tr>

    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.base"/>">
			<bean:message key="label.base"/>:
		</span>
	  </td>
	  <td class="datum">
		<html:select property="schemaBase">
			<html:options property="allYourBase"/>
		</html:select>
		<html:submit property="action">
			<bean:message key="label.change"/>
		</html:submit>
      </td>
    </tr>

  <% boolean first = true;
     org.vfny.geoserver.form.data.AttributeDisplay attributeDisplay = null;;
     org.vfny.geoserver.form.data.AttributeForm attributeForm = null; 
     
     org.vfny.geoserver.form.data.TypesEditorForm form = 
             (org.vfny.geoserver.form.data.TypesEditorForm) request.getAttribute("typesEditorForm");
     java.util.List attributes = (java.util.List) form.getAttributes();
     int attributesSize = attributes.size(); %>
             
    <% if (!("--".equals(form.getSchemaBase())) ) { %>
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.type.schemaName"/>">
          <bean:message key="label.schemaName"/>:
        </span>
	  </td>
	  <td class="datum">
		<html:text property="schemaName" size="60"/>
	  </td>
	</tr>
    <% } %>    

<logic:iterate id="attribute" indexId="index" name="typesEditorForm" property="attributes">
	<tr>
      <td class="label">
		<bean:write name="attribute" property="name"/>:
	  </td>
	  <td class="datum">
        <table border=0 width="100%">
          <tr style="white-space: nowrap;">
            
		  <%
		     if (attribute instanceof org.vfny.geoserver.form.data.AttributeDisplay) { 
		  	     attributeDisplay = (org.vfny.geoserver.form.data.AttributeDisplay) attribute; %>
            <td width="70%"><bean:write name="attribute" property="type"/></td>
            <td>nillable:<bean:write name="attribute" property="nillable"/></td>
            <td>min:<bean:write name="attribute" property="minOccurs"/></td>
            <td>max:<bean:write name="attribute" property="maxOccurs"/></td>
          <% } else { 
                 attributeForm = (org.vfny.geoserver.form.data.AttributeForm) attribute; %>
            <td width="70%">
            	<html:select property='<%= "attributes[" + index + "].type"%>'>
          			<html:options property='<%= "attributes[" + index + "].types"%>'/>
        		</html:select>
				<html:submit property="action">
					<bean:message key="label.change"/>
				</html:submit>        		
            </td>
            <td><bean:message key="label.nillable"/>:<html:checkbox property='<%= "attributes[" + index + "].nillable" %>'/></td>
            <td><bean:message key="label.min"/>:<html:text size="2" property='<%= "attributes[" + index + "].minOccurs"%>'/></td>
            <td><bean:message key="label.max"/>:<html:text size="2" property='<%= "attributes[" + index + "].maxOccurs"%>'/></td>
            <td width=16>
              <% if (first == false) { %>
          	  <html:image src="../../../data/images/up.png" 
          	  	          titleKey="type.title.up" 
          	  	          property="action" 
          	  	          value='<%= "up_"+ index%>'/>
          	  <% } 
          	     first = false; %>
          	</td>
          	<td width=16>
          	  <% if (attributesSize-1 != index.intValue()) { %>
          	  <html:image src="../../../data/images/down.png" 
          	              titleKey="type.title.down" 
          	              property="action" 
          	              value='<%= "down_"+ index%>'/>
          	  <% } %>
          	</td> 
          	<td width=16>
          	  <html:image src="../../../data/images/delete.png" 
          	  	          titleKey="type.title.delete" 
          	  	          property="action" 
          	  	          value='<%= "delete_"+ index%>'/>
		    </td>
          <% } %>
          </tr>
          <%
            if (attributeForm != null && attributeForm.getType().equals(org.vfny.geoserver.config.AttributeTypeInfoConfig.TYPE_FRAGMENT)) {
  		  %>
          <tr>
            <td align="left" valign="top" colspan="7">
              		<html:textarea cols="80" rows="3" 
              		               property='<%= "attributes[" + index + "].fragment" %>'/>
            </td>
          </tr>
          <% } %>
        </table>		
      </td>
    </tr>
</logic:iterate>
    
    
    <% if (form.getCreateableAttributes().size() != 0) { %>
    <tr>
    	<td>
  			<html:select property="newAttribute">
				<html:options property="createableAttributes"/>
			</html:select>
		</td>
		<td>
			<html:submit property="action">
				<bean:message key="label.add"/>
			</html:submit>    	
		</td>
    </tr>
    <% } %>
    
    <tr>
      <td class="label">
        &nbsp;
      </td>
	  <td class="datum">

		<html:submit property="action">
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>

	  </td>
    </tr>
  </table>
<html:hidden property="autoGenerateExtent" value="false"/>
</html:form>

<% } catch (Throwable hate ){
   System.err.println( "FeatureType Editor problem:"+ hate );
   hate.printStackTrace();
   throw hate;
} %>
