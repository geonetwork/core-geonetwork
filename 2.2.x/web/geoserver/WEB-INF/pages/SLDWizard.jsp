<!--
Copyright (c) 2006 TOPP - www.openplans.org.  All rights reserved.
This code is licensed under the GPL 2.0 license, availible at the root
application directory.


@author Brent Owens
-->

<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html>
<head></head>

<!-- color chooser -->
<link rel="stylesheet" href="js_color_picker_v2.css" media="screen">
<script type="text/javascript" src="color_functions.js"></script>
<script type="text/javascript" src="js_color_picker_v2.js"></script>

<!-- ================================ -->
<!-- ---------- JAVASCRIPT ---------- -->
<!-- ================================ -->
<script language="JavaScript" type="text/JavaScript">
<!--


/** global variables **/
var SERVERHOSTNAME = "sigma.openplans.org";//window.location.host;

var SERVERURL = "http://"+SERVERHOSTNAME+"/geoserver/";

var geo_xmlhttp = null;	// AJAX-ness


var featureTypeName = "";	// our feature type that the style will go to

var geomType = "";	// the type of geometry we are making a style for

var columnNames;	// the names of the columns of the feature type


/**
 * Setup
 *
 * This will search through the feature type info and determine 
 * what kind of geometry it uses. This is based on geoserver 
 * output and not tested with anything else.
 */
function setup()
{
	log("setup");
	
	// we need an interval to read the jsp results.
	// javascript will load before the jsp code so we need to wait to use it.
	var myInterval = window.setInterval(function cheese(a,b) {
		ft = document.getElementById('span_ftName').innerHTML;
		// if doesn't equal 'undefined'
		log("ft = "+ft);
		if (ft != "undefined")
		{
			featureTypeName = document.getElementById('span_ftName').innerHTML;
			log("interval; featureTypeName = "+featureTypeName);
			ftInfo = document.getElementById('hidden_ft_attrs').innerHTML;
			log("ft info: "+ftInfo);
			columnNames = document.getElementById('hidden_ft_attrNames').innerHTML;

			ftInfoSplit = ftInfo.split(",");
			for(i=0;i<ftInfoSplit.length;i++)
			{
				var g = ftInfoSplit[i].split(":")[1];
				if (g == "multiPointProperty" || g == "pointProperty" ) {
					geomType = "point";
					break;
				}
				else if (g == "multiLineStringProperty" || g == "lineStringProperty" ) {
					geomType = "line";
					break;
				}
				else if (g == "multiPolygonProperty" || g == "polygonProperty" ) {
					geomType = "polygon";
					break;
				}
			}
			log("geomType = "+geomType);


			setScreenEditMenu(geomType); // set the appropriate html for editing

			clearInterval(myInterval); // cancel the interval timer
		}
		else
			log("undefined");
		
	},300);
	
}


function setScreenEditMenu(geomType)
{
	editForm = "";
	if (geomType == "point")
		editForm = generatePointForm();
	else if (geomType == "line")
		editForm = generateLineForm();
	else if (geomType == "polygon")
		editForm = generatePolygonForm();
	
	document.getElementById('mainEditDiv').innerHTML = editForm;
}

function generatePointForm()
{
	result = "";
	result += '<tr><td colspan=4><b><u><font size="+1">Label names for the points:</font></u></b><br></td></tr>';
	result += '<tr><td width=200 colspan=4><b>Name Field:</b> '+generateNameSelect()+' <i><font color="#92AEFF" size="-1">(This field is the label that will appear on the geometry.)</font></i></td></tr>';
	result += '<tr><td width=200 colspan=4><b>Text Color:</b> '+generateColorPicker("labelColor")+' <i><font color="#92AEFF" size="-1">(This is the color of the label.)</font></i></td></tr>';

	result += '<tr><td colspan=4><br><hr><b><u><font size="+1">Color of the points:</font></u></b></td></tr>';

	result += '<tr><td width=120 align="right"><b>Color:</b></td><td width=100>'+generateColorPicker("fillColor")+'</td>';
	result += '<td width=120 align="right"><b>Opacity:</b></td><td width=140><input type="text" id="fillOpacity" size=3 maxlength=3 value="1"></input> <i><font color="#92AEFF" size="-1">(0.0 - 1.0)</font></i></td></tr>';

	result += '<tr><td colspan=4><br><hr><b><u><font size="+1">Point size and shape:</font></u></b><br></td></tr>';

	result += '<tr><td width=200 colspan=4><b>Point Size:</b> <input type="text" id="pointSize" size=6 maxlength=6></input> <i><font color="#92AEFF" size="-1">(The diameter of the point in pixels.)</font></i><br>&nbsp;</td></tr>';
	result += '<tr><td width=200 colspan=4><b>Point Shape:</b> <select name="pointShape" id="pointShape"> ';
		result += '<option>circle</option>';
		result += '<option>square</option>';
		result += '</select>';
	result += '</td></tr>';

	return result;
}

function generateLineForm()
{
	result = "";
	result += '<tr><td colspan=4><b><u><font size="+1">Label names for the lines:</font></u></b><br></td></tr>';
	result += '<tr><td width=200 colspan=4><b>Name Field:</b> '+generateNameSelect()+' <i><font color="#92AEFF" size="-1">(This field is the label that will appear on the geometry.)</font></i></td></tr>';
	result += '<tr><td width=200 colspan=4><b>Text Color:</b> '+generateColorPicker("labelColor")+' <i><font color="#92AEFF" size="-1">(This is the color of the label.)</font></i></td></tr>';

	result += '<tr><td colspan=4><br><hr><b><u><font size="+1">Color of the lines:</font></u></b><br></td></tr>';

	result += '<tr><td width=150 align="right"><b>Color:</b></td><td width=100>'+generateColorPicker("lineColor")+'</td>';
	result += '<td width=150 align="right"><b>Opacity:</b></td><td width=140><input type="text" id="lineOpacity" size=3 maxlength=3 value="1"></input> <i><font color="#92AEFF" size="-1">(0.0 - 1.0)</font></i></td></tr>';
	
	result += '<tr><td colspan=4><br><hr><b><u><font size="+1">Line dimentions:</font></u></b><br></td></tr>';

	result += '<tr><td width=200 colspan=4><b>Line width:</b> <input type="text" id="lineWidth" size=6 maxlength=6></input> <i><font color="#92AEFF" size="-1">(The width, or thickness, of the line in pixles.)</font></i><br>&nbsp;</td></tr>';

	return result;
}

function generatePolygonForm()
{
	result = "";
	result += '<tr><td colspan=4><b><u><font size="+1">Label names for the polygons:</font></u></b><br></td></tr>';
	result += '<tr><td width=200 colspan=4><b>Name Field:</b> '+generateNameSelect()+' <i><font color="#92AEFF" size="-1">(This field is the label that will appear on the geometry.)</font></i><br>&nbsp;</td></tr>';
	result += '<tr><td width=200 colspan=4><b>Text Color:</b> '+generateColorPicker("labelColor")+' <i><font color="#92AEFF" size="-1">(This is the color of the label.)</font></i></td></tr>';

	result += '<tr><td colspan=4><br><hr><b><u><font size="+1">Color inside the polygons:</font></u></b><br></td></tr>';

	result += '<tr><td width=120 align="right"><b>Fill Color:</b></td><td width=150>'+generateColorPicker("fillColor")+'&nbsp;&nbsp;&nbsp;&nbsp;</td>';
	result += '<td width=120 align="right"><b>Fill Opacity:</b></td><td><input type="text" id="fillOpacity" size=3 maxlength=3 value="1"></input> <i><font color="#92AEFF" size="-1">(0.0 - 1.0)</font></i></td></tr>';
	
	result += '<tr><td colspan=4><br><hr><b><u><font size="+1">Outline color of the polygons:</font></u></b><br></td></tr>';

	result += '<tr><td width=120 align="right"><b>Outline Color:</b></td><td width=150>'+generateColorPicker("lineColor")+'&nbsp;&nbsp;&nbsp;&nbsp;</td>';
	result += '<td width=120 align="right"><b>Outline Opacity:</b></td><td><input type="text" id="lineOpacity" size=3 maxlength=3 value="1"></input> <i><font color="#92AEFF" size="-1">(0.0 - 1.0)</font></i></td></tr>';

	return result;
}


function generateColorPicker(colorFieldName)
{
	
	result = '<input name="'+colorFieldName+'" id="'+colorFieldName+'" size="7" ';
	result += 'onChange="relateColor(\''+colorFieldName+'\', this.value);"> ';
	result += '<script language="javascript">relateColor(\'pick'+colorFieldName+'\', getObj(\''+colorFieldName+'\').value);</scr'+'ipt> ';
	result += '<a href="javascript:pickColor(\''+colorFieldName+'\');" id="pick'+colorFieldName+'" name="pick'+colorFieldName+'" style="border: 1px solid #000000; font-family:Verdana; font-size:10px; background=#FFFF33; ';
	result += 'text-decoration: none; " ><img src="../../data/images/colorpicker.jpg" width=12 height=12 border="none"></a>';

	return result;
}

/**
 * Generates the <select> form field for column names based on feature type column names.
 */
function generateNameSelect()
{
	formCode = '<select name="propertyName" id="propertyName">';
	formCode += '<option>none</option>';

	colNamesSplit = columnNames.split(",");
	for(i=0;i<colNamesSplit.length-1;i++)
	{
		if (colNamesSplit[i] != null && colNamesSplit[i] != "")
		{
			formCode += '<option>'+colNamesSplit[i]+'</option>';
		}
	}
	
	formCode += "</select>";

	return formCode;
}


/**
 *
 * This is the main function that reads in the form fields and generates 
 * the SLD.
 *
 */
function generateSLD()
{
	featureTypeName = document.getElementById('span_ftName').innerHTML;
	//alert("featureTypeName: "+featureTypeName);
	
	hideSuccessMessage(); // hide the SLD success message

	SLD="";
	SLD += createSLDHeader(featureTypeName);

	SLDcore = ""
	if (geomType == "point")
		SLDcore += generatePointSLD();
	else if (geomType == "line")
		SLDcore += generateLineSLD();
	else if (geomType == "polygon")
		SLDcore += generatePolygonSLD();
	
	matches = SLDcore.match("ERROR:");
	if (matches != null && matches.length > 0)
	{
		alert(SLDcore);	// alert the user with the error and return
		return;
	}

	
	SLD += SLDcore; // append the code if there were no errors

	SLD += createSLDFooter();

	log("SLD:\n"+SLD);
	saveStyle(SLD); // send it using PutStyles request
}

/**
 * Point SLD generation
 */
function generatePointSLD()
{
	propertyName = document.getElementById('propertyName').value;
	labelColor = document.getElementById('labelColor').value;
	fillColor = document.getElementById('fillColor').value;
	fillOpacity = document.getElementById('fillOpacity').value;
	pointSize = document.getElementById('pointSize').value;
	pointShape = document.getElementById('pointShape').value;

	if (labelColor.length == 6)
		labelColor = '#'+labelColor;
	if (fillColor.length == 6)
		fillColor = '#'+fillColor;

	var font;
	var halo;

	// check values to make sure they are in range and valid
	if (propertyName != "none" && (labelColor == null || labelColor == "") )
		return "ERROR: label name specified, but no text color specified.";
	if (labelColor == null || labelColor == "")
		return "ERROR: Label color cannot be empty";
	if (labelColor.length != 7)
		return "ERROR: Label color must be 7 characters long in hexadecimal (#00ff23).";
	if (fillColor == null || fillColor == "")
		return "ERROR: Point color cannot be empty";
	if (fillColor.length != 7)
		return "ERROR: Point color must be 7 characters long in hexadecimal (#00ff23).";
	if (fillOpacity == null || fillOpacity == "")
		return "ERROR: Point opacity cannot be empty";
	if (fillOpacity < 0.0 || fillOpacity > 1.0)
		return "ERROR: Point opacity must be between 0.0 and 1.0";
	if (pointSize == null || pointSize == "")
		return "ERROR: Point size cannot be empty";


	// create stroke
	var stroke;
	//stroke = createStroke(lineColor, lineOpacity);

	pointFill = createFill(fillColor, fillOpacity);
	graphicMark = createGraphic(pointShape, pointFill, stroke, pointSize, 1.0);

	SLDr = createSLDPointSymbolizer(graphicMark);

	textFont = createFont("Times New Roman", "Normal", 12);

	textFill = createFill(labelColor, 1.0);

	if (propertyName != null && propertyName != "none")
		SLDr += createTextSymbolizer(propertyName, textFont, halo, textFill);

	return SLDr;
}

/**
 * Line SLD generation
 */
function generateLineSLD()
{
	propertyName = document.getElementById('propertyName').value;
	labelColor = document.getElementById('labelColor').value;
	lineColor = document.getElementById('lineColor').value;
	lineOpacity = document.getElementById('lineOpacity').value;
	lineWidth = document.getElementById('lineWidth').value;

	if (labelColor.length == 6)
		labelColor = '#'+labelColor;
	if (lineColor.length == 6)
		lineColor = '#'+lineColor;

	var halo;

	// check values to make sure they are in range and valid
	if (propertyName != "none" && (labelColor == null || labelColor == "") )
		return "ERROR: label name specified, but no text color specified.";
	if (labelColor == null || labelColor == "")
		return "ERROR: Label color cannot be empty";
	if (labelColor.length != 7)
		return "ERROR: Label color must be 7 characters long in hexadecimal (#00ff23).";
	if (lineColor == null || lineColor == "")
		return "ERROR: Line color cannot be empty";
	if (lineColor.length != 7)
		return "ERROR: Line color must be 7 characters long in hexadecimal (#00ff23).";
	if (lineOpacity == null || lineOpacity == "")
		return "ERROR: Line opacity cannot be empty";
	if (lineOpacity < 0.0 || lineOpacity > 1.0)
		return "ERROR: Line opacity must be between 0.0 and 1.0";
	if (lineWidth == null || lineWidth == "")
		return "ERROR: Line width cannot be empty";
	if (lineWidth < 0)
		return "ERROR: Line width must be a positive number";


	// create stroke
	stroke = createStroke(lineColor, lineOpacity, lineWidth);
	
	SLDr = createSLDLineSymbolizer(stroke);

	textFont = createFont("Times New Roman", "Normal", 12);

	textFill = createFill(labelColor, 1.0);

	if (propertyName != null && propertyName != "none")
		SLDr += createTextSymbolizer(propertyName, textFont, halo, textFill);

	return SLDr;
}

function generatePolygonSLD()
{
	propertyName = document.getElementById('propertyName').value;
	labelColor = document.getElementById('labelColor').value;
	fillColor = document.getElementById('fillColor').value;
	fillOpacity = document.getElementById('fillOpacity').value;
	lineColor = document.getElementById('lineColor').value;
	lineOpacity = document.getElementById('lineOpacity').value;
	
	if (labelColor.length == 6)
		labelColor = '#'+labelColor;
	if (fillColor.length == 6)
		fillColor = '#'+fillColor;
	if (lineColor.length == 6)
		lineColor = '#'+lineColor;
	
	var halo;

	// check values to make sure they are in range and valid
	if (propertyName != "none" && (labelColor == null || labelColor == "") )
		return "ERROR: label name specified, but no text color specified.";
	if (labelColor == null || labelColor == "")
		return "ERROR: Label color cannot be empty";
	if (labelColor.length != 7)
		return "ERROR: Label color must be 7 characters long in hexadecimal (#00ff23).";
	if (fillColor == null || fillColor == "")
		return "ERROR: Polygon fill color cannot be empty";
	if (fillColor.length != 7)
		return "ERROR: Polygon fill color must be 7 characters long in hexadecimal (#00ff23).";
	if (fillOpacity == null || fillOpacity == "")
		return "ERROR: Polygon color opacity cannot be empty";
	if (fillOpacity < 0.0 || fillOpacity > 1.0)
		return "ERROR: Polygon fill opacity must be between 0.0 and 1.0";
	if (lineColor == null || lineColor == "")
		return "ERROR: Polygon outline color cannot be empty";
	if (lineColor.length != 7)
		return "ERROR: Polygon outline color must be 7 characters long in hexadecimal (#00ff23).";
	if (lineOpacity == null || lineOpacity == "")
		return "ERROR: Polygon outline opacity cannot be empty";
	if (lineOpacity < 0.0 || lineOpacity > 1.0)
		return "ERROR: Polygon outline opacity must be between 0.0 and 1.0";

	// create fill
	polygonFill = createFill(fillColor, fillOpacity);

	// create stroke
	stroke = createStroke(lineColor, lineOpacity);

	XMLr = createSLDPolygonSymbolizer(polygonFill, stroke);

	textFill = createFill(labelColor, 1.0);

	textFont = createFont("Times New Roman", "Normal", 12);

	if (propertyName != null && propertyName != "none")
		XMLr += createTextSymbolizer(propertyName, textFont, halo, textFill);

	return XMLr;
}

/**
 * Perform the putStyles request
 */
function saveStyle(SLD)
{
	log("Saving style");
	//kill any current requests!
	if (geo_xmlhttp !=null)
	{
		geo_xmlhttp.abort();
		geo_xmlhttp = null;
	}

	// build XML POST query
	URL  = "/geoserver/wms?request=putstyles";//"http://"+SERVERHOSTNAME+"/

	getXML(URL,SLD,XMLProgressFunction);
	
}

function createSLDHeader(featureType)
{
	log("Making sld for: "+featureType);
	XML  = '<?xml version="1.0" encoding="UTF-8"?>'+"\n";
	XML += '<StyledLayerDescriptor version="1.0.0"'+"\n";
	XML += '	xmlns:gml="http://www.opengis.net/gml"'+"\n";
	XML += '	xmlns:ogc="http://www.opengis.net/ogc"'+"\n";
	XML += '	xmlns="http://www.opengis.net/sld">'+"\n";
	XML += '	<NamedLayer>'+"\n";
	XML += '		<Name>'+featureType+'</Name>'+"\n";
	XML += '		<UserStyle>'+"\n";
	XML += '			<Name>'+featureType+'_style</Name>'+"\n";
	XML += '			<Title>geoserver style</Title>'+"\n";
	XML += '			<Abstract>Generated by GeoServer</Abstract>'+"\n";
	XML += '			<FeatureTypeStyle>'+"\n";
	XML += '			<Rule>'+"\n";
	//log(XML);
	return XML;
}


function createSLDFooter()
{
	XML   = '			</Rule>'+"\n";
	XML  += '			</FeatureTypeStyle>'+"\n";
	XML  += '		</UserStyle>'+"\n";
	XML  += '	</NamedLayer>'+"\n";
	XML  += '</StyledLayerDescriptor>';

	return XML;
}

function createSLDPointSymbolizer(graphic)
{
	XML  = '				<PointSymbolizer>'+"\n";
	XML += '					'+graphic+"\n";
	XML += '				</PointSymbolizer>'+"\n";

	return XML;

}

function createSLDLineSymbolizer(stroke)
{
	XML  = '				<LineSymbolizer>'+"\n";
	XML += stroke;
	XML += '				</LineSymbolizer>'+"\n";

	return XML;
}

function createSLDPolygonSymbolizer(fill, stroke)
{
	XML  = '				<PolygonSymbolizer>'+"\n";
	XML += fill;
	XML += stroke;
	XML += '				</PolygonSymbolizer>'+"\n";

	return XML;
}

function createFill(color, opacity)
{
	// add # to front of 'color'
	if(color.charAt(0) != "#")
		color = "#"+color;

	XML  = '					<Fill>'+"\n";
	XML += '						<CssParameter name="fill">'+color+'</CssParameter>'+"\n";
	XML += '						<CssParameter name="fill-opacity">'+opacity+'</CssParameter>'+"\n";
	XML += '					</Fill>'+"\n";

	return XML;
}

function createStroke(color, opacity, width, linecap, linejoin, dasharray)
{
	// add # to front of 'color'
	if(color.charAt(0) != "#")
		color = "#"+color;

	XML  = '					<Stroke>'+"\n";
	if(color)	XML += '						<CssParameter name="stroke">'+color+'</CssParameter>'+"\n";
	if(opacity)	XML += '						<CssParameter name="stroke-opacity">'+opacity+'</CssParameter>'+"\n";
	if(width)	XML += '						<CssParameter name="stroke-width">'+width+'</CssParameter>'+"\n";
	if(linecap)	XML += '						<CssParameter name="stroke-linecap">'+linecap+'</CssParameter>'+"\n";
	if(linejoin)XML += '						<CssParameter name="stroke-linejoin">'+linejoin+'</CssParameter>'+"\n";
	if(dasharray)XML += '						<CssParameter name="stroke-dasharray">'+dasharray+'</CssParameter>'+"\n";
	XML += '					</Stroke>'+"\n";

	return XML;
}

// a note to figure out all possible line caps
function getLineCaps()
{

}

// a note to figure out all possible line joins
function getLineJoins()
{

}

function createGraphic(shape, fill, stroke, size, opacity)
{
	XML = '<Graphic>'+"\n";
	XML += '	<Mark>'+"\n";
	XML += '		<WellKnownName>'+shape+'</WellKnownName>'+"\n";
	XML += fill;
	if(stroke) XML += stroke;
	XML += '	</Mark>'+"\n";
	XML += '	<Opacity>'+opacity+'</Opacity>'+"\n";
	XML += '	<Size>'+size+'</Size>'+"\n";
	XML += '</Graphic>'+"\n";

	return XML;
}


function createTextSymbolizer(columnName, font, halo, fill)
{
	XML  = '				<TextSymbolizer>'+"\n";
	XML += '					<Label>'+"\n";
	XML += '						<ogc:PropertyName>'+columnName+'</ogc:PropertyName>'+"\n";
	XML += '					</Label>'+"\n";
	if(font) XML += font;
	if(halo) XML += halo;
	if(fill) XML += fill;
	XML += '				</TextSymbolizer>'+"\n";


	return XML;
}

function createFont(name, style, size, weight)
{
	XML  = '						<Font>'+"\n";
	XML += '							<CssParameter name="font-family">'+name+'</CssParameter>'+"\n";
	XML += '							<CssParameter name="font-style">'+style+'</CssParameter>'+"\n";
	XML += '							<CssParameter name="font-size">'+size+'</CssParameter>'+"\n";
	if (weight)XML += '							<CssParameter name="font-weight">'+weight+'</CssParameter>'+"\n";
	XML += '						</Font>'+"\n";

	return XML;
}

function createHalo(radius, fill)
{
	XML  = '						<Halo>'+"\n";
	XML += '							<Radius>'+"\n";
	XML += '								<ogc:Literal>'+radius+'</ogc:Literal>'+"\n";
	XML += '							</Radius>'+"\n";
	XML += fill;
	XML += '						</Halo>'+"\n";

	return XML;
}


function createMinScaleDenominator(scale)
{
	XML = '<MinScaleDenominator>'+scale+'</MinScaleDenominator>'+"\n";

	return XML;
}

function createMaxScaleDenominator(scale)
{
	XML = '<MaxScaleDenominator>'+scale+'</MaxScaleDenominator>'+"\n";

	return XML;
}


/**
 * Send the XML request to GeoServer. Hands off control to procfunction method passed in.
 */
function getXML(url,post,procfunction)
{
	try {

		if (window.ActiveXObject)
		{
			// IE
			log("getXML through IE");
			geo_xmlhttp =  new ActiveXObject("Microsoft.XMLHTTP");
			geo_xmlhttp.onreadystatechange = procfunction;
			geo_xmlhttp.open("POST", url, true);
			geo_xmlhttp.setRequestHeader('Content-Type', 'text/xml');  //correct request type
			geo_xmlhttp.setRequestHeader('Cache-Control', 'no-cache');	// don't cache the requests!!!
			//geo_xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			geo_xmlhttp.send(post);
		}
		else if (window.XMLHttpRequest)
		{
			
			// Mozilla and others
			//log("getXML through Mozilla etc.");
			geo_xmlhttp =  new XMLHttpRequest();
			//geo_xmlhttp.overrideMimeType('text/xml');	// <- bad? caused some serious problems at one point
			geo_xmlhttp.onreadystatechange = procfunction;
			geo_xmlhttp.open("POST", url, true);
			geo_xmlhttp.setRequestHeader('Content-Type', 'text/xml');	//correct request type
			geo_xmlhttp.setRequestHeader('Cache-Control', 'no-cache');	// don't cache the requests!!!
			geo_xmlhttp.send(post);
		}
		else
			log("Invalid browser format: not expecting this kind of browser.");
	 }
	 catch(e)
	 {
		alert(e);
		alert("If you just got a security exception, its because you need to serve the .html file from the same server as where you're sending the XML requests to!");
	 }
}



/**
 * Waits for requests and handles them.
 */
function XMLProgressFunction()
{
	if (geo_xmlhttp != null)
	{	// add 'working' animation
		hideSuccessMessage();
	}

	if ( (geo_xmlhttp.readyState == 4) && (geo_xmlhttp.status == 200) )
	{
		log("XML reponse received");
		//we got a good response.  We need to process it!
		if (geo_xmlhttp.responseXML == null)
		{
			log("XMLProgressFunction(): abort 1");
			//document.getElementById('working_anim_gnis_span').innerHTML = '';	// remove 'working' animation
			return;
		}

		log("response:\n"+geo_xmlhttp.responseText);

		// ug, temporary hack just so I can get it working
		if (window.ActiveXObject)
		{
			val = geo_xmlhttp.responseText.indexOf("success");

			if (val > -1)
			{
				enableSuccessMessage();
				log("PutStyles successful");
			}
		}
		else
		{
			success_node = getElements(geo_xmlhttp.responseXML,"sld","success")[0];
			if (success_node != null)
			{
				enableSuccessMessage();
				log("PutStyles successful");
			}
		}
	}
	else
		log("waiting for response...");
}

/**
 * Browser capabilities: prefixes for IE
 * Inspect for IE first. If you do mozilla first, it will just be mozilla and IE will die
 */
function getElements(node,tag_prefix,tag_name)
{
	if (window.ActiveXObject)
	{
		//IE has no idea of namespaces/prefixes
		log("parsing IE");
		return node.getElementsByTagName(tag_prefix+":"+tag_name);
    }
    else if (window.XMLHttpRequest)
    {
       //mozilla
       return node.getElementsByTagName(tag_name);
    }
    else
		log("Unsupported browser format: not expecting this kind of browser.");
    
}

function enableSuccessMessage()
{
	if (document.all) // if IE
	{
		log("ie put");
		doc = document.getElementById('sld_success').getElementsByTagName('span')[0];
		doc.innerHTML = '<b><font color="#22ff33" size="+1">Success</font></b>';
		
	}
	else
		document.getElementById('inner_sld_success').innerHTML = "<b><font color=\"#22ff33\" size=\"+1\">Success</font></b>";
}

function hideSuccessMessage()
{
	if (document.all) // if IE
	{
		doc = document.getElementById('sld_success').getElementsByTagName('span')[0];
		doc.innerHTML = '';
	}
	else
		document.getElementById('inner_sld_success').innerHTML = "";
}

/**
 * Log to screen
 */
function log(text)
{
	IFrameElement = document.getElementsByName("logFrame")[0];

	//get doc
	if (IFrameElement.contentDocument)
	{
		IFrameDoc = IFrameElement.contentDocument;
	}
	else if (IFrameElement.contentWindow)
	{
		IFrameDoc = IFrameElement.contentWindow.document;
	}
	else if (IFrameElement.document)
	{
		IFrameDoc = IFrameElement.document;
	}
	else
	{
		return true;
	}


     //put a <div><pre></pre></div>
	if (IFrameDoc.body.getElementsByTagName("div")[0] ==null)
	{

 	   divNode = IFrameDoc.createElement("div");
 	   preNode = IFrameDoc.createElement("pre");
 	   preNode.appendChild(IFrameDoc.createTextNode(""));
 	   divNode.appendChild(preNode);

       IFrameDoc.body.appendChild(divNode);


       //alert(IFrameDoc.body.getElementsByTagName("div")[0] );
	}


	// add the text
	//<div><pre>  text<br>  </pre></div>
	preNode = IFrameDoc.body.getElementsByTagName("pre")[0];

	textNode = IFrameDoc.createTextNode(text) ;

	preNode.appendChild(  textNode  );
	preNode.appendChild(  IFrameDoc.createElement("br")  );
	//  textNode.scrollIntoView(true);
}

/**
 * For killing form 'action' functionality. Cheap hack, I'm sure there are better ways to do it.
 * Also for killing link tags.
 */
function nothing()
{
}



// Flooble Color Picker
// Color Picker Script from Flooble.com
// For more information, visit 
//	http://www.flooble.com/scripts/colorpicker.php
// Copyright 2003 Animus Pactum Consulting inc.
// You may use and distribute this code freely, as long as
// you keep this copyright notice and the link to flooble.com
// if you chose to remove them, you must link to the page
// listed above from every web page where you use the color
// picker code.
//---------------------------------------------------------
     var perline = 15;
     var divSet = false;
     var curId;
     var colorLevels = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');
     var colorArray = Array();
     var ie = false;
     var nocolor = 'none';
	 if (document.all) { ie = true; nocolor = ''; }
	 function getObj(id) {
		if (ie) { return document.all[id]; } 
		else {	return document.getElementById(id);	}
	 }

     function addColor(r, g, b) {
     	var red = colorLevels[r];
     	var green = colorLevels[g];
     	var blue = colorLevels[b];
     	addColorValue(red, green, blue);
     }

     function addColorValue(r, g, b) {
     	colorArray[colorArray.length] = '#' + r + r + g + g + b + b;
     }
     
     function setColor(color) {
     	var link = getObj('pick'+curId);
     	var field = getObj(curId);// + 'field'
     	var picker = getObj('colorpicker');
     	field.value = color;
     	if (color == '') {
			link.innerHTML="&nbsp;&nbsp;&nbsp;";
	     	link.style.background = nocolor;
	     	link.style.color = nocolor;
	     	color = nocolor;
     	} else {
			link.innerHTML="&nbsp;&nbsp;&nbsp;";
	     	link.style.background = color;
	     	link.style.color = color;
	    }
     	picker.style.display = 'none';
	    eval(getObj(curId).title);// + 'field'
     }
        
     function setDiv() {     
     	if (!document.createElement) { return; }
        var elemDiv = document.createElement('div');
        if (typeof(elemDiv.innerHTML) != 'string') { return; }
        genColors();
        elemDiv.id = 'colorpicker';
	    elemDiv.style.position = 'absolute';
        elemDiv.style.display = 'none';
        elemDiv.style.border = '#000000 1px solid';
        elemDiv.style.background = '#FFFFFF';
        elemDiv.innerHTML = '<span style="font-family:Verdana; font-size:11px;">Pick a color: ' 
          	+ '(<a href="javascript:setColor(\'\');">No color</a>)<br>' 
        	+ getColorTable() 
        	+ '<center><a href="http://www.flooble.com/scripts/colorpicker.php"'
        	+ ' target="_blank">color picker</a> by <a href="http://www.flooble.com" target="_blank"><b>flooble</b></a></center></span>';

        document.body.appendChild(elemDiv);
        divSet = true;
     }
     
     function pickColor(id) {
     	if (!divSet) { setDiv(); }
     	var picker = getObj('colorpicker');     	
		if (id == curId && picker.style.display == 'block') {
			picker.style.display = 'none';
			return;
		}
     	curId = id;
     	var thelink = getObj(id);
     	picker.style.top = getAbsoluteOffsetTop(thelink) + 20;
     	picker.style.left = getAbsoluteOffsetLeft(thelink);     
	picker.style.display = 'block';
     }
     
     function genColors() {
        addColorValue('0','0','0');
		//addColorValue('1','1','1');
		addColorValue('2','2','2');
        addColorValue('3','3','3');
		addColorValue('4','4','4');
		addColorValue('5','5','5');
        addColorValue('6','6','6');
		addColorValue('7','7','7');
        addColorValue('8','8','8');
        addColorValue('9','9','9');
        addColorValue('A','A','A');
		addColorValue('B','B','B');
        addColorValue('C','C','C');
		addColorValue('D','D','D');
        addColorValue('E','E','E');
        addColorValue('F','F','F');
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(0,0,a);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(a,a,5);

        for (a = 1; a < colorLevels.length; a++)
			addColor(0,a,0);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(a,5,a);
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(a,0,0);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(5,a,a);
			
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(a,a,0);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(5,5,a);
			
        for (a = 1; a < colorLevels.length; a++)
			addColor(0,a,a);
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(a,5,5);

        for (a = 1; a < colorLevels.length; a++)
			addColor(a,0,a);			
        for (a = 1; a < colorLevels.length - 1; a++)
			addColor(5,a,5);
			
       	return colorArray;
     }
     function getColorTable() {
         var colors = colorArray;
      	 var tableCode = '';
         tableCode += '<table border="0" cellspacing="1" cellpadding="1">';
         for (i = 0; i < colors.length; i++) {
              if (i % perline == 0) { tableCode += '<tr>'; }
              tableCode += '<td bgcolor="#000000"><a style="outline: 1px solid #000000; color: ' 
              	  + colors[i] + '; background: ' + colors[i] + ';font-size: 10px;" title="' 
              	  + colors[i] + '" href="javascript:setColor(\'' + colors[i] + '\');">&nbsp;&nbsp;&nbsp;</a></td>';
              if (i % perline == perline - 1) { tableCode += '</tr>'; }
         }
         if (i % perline != 0) { tableCode += '</tr>'; }
         tableCode += '</table>';
      	 return tableCode;
     }
     function relateColor(id, color) {
     	var link = getObj(id);
     	if (color == '') {
	     	link.style.background = nocolor;
	     	link.style.color = nocolor;
	     	color = nocolor;
     	} else {
	     	link.style.background = color;
	     	link.style.color = color;
	    }
	    eval(getObj(id + 'field').title);
     }
     function getAbsoluteOffsetTop(obj) {
     	var top = obj.offsetTop;
     	var parent = obj.offsetParent;
     	while (parent != document.body) {
     		top += parent.offsetTop;
     		parent = parent.offsetParent;
     	}
     	return top;
     }
     
     function getAbsoluteOffsetLeft(obj) {
     	var left = obj.offsetLeft;
     	var parent = obj.offsetParent;
     	while (parent != document.body) {
     		left += parent.offsetLeft;
     		parent = parent.offsetParent;
     	}
     	return left;
     }



-->
</script>
<!-- ================================ -->
<!-- --- END -- JAVASCRIPT ---------- -->
<!-- ================================ -->




<body onload="setup()" text="#AAC0FF">

<font color="#08809F" size="+2"><b>Create new SLD for FeatureType: &nbsp;<i><span id="span_ftName" name="span_ftName"><bean:write property='<%= "typeName" %>' name="typesEditorForm"/></span></font></i></b>
<br>&nbsp;<br>

<span id="hidden_ft_attrs" name="hidden_ft_attrs" style="display:none"><bean:write property='<%= "attributes" %>' name="typesEditorForm" /></span>

<span id="hidden_ft_attrNames" name="hidden_ft_attrNames" style="display:none">
<logic:iterate id="attribute" indexId="index" name="typesEditorForm" property="attributes">
	<bean:write name="attribute" property="name"/>,
</logic:iterate>
</span>



<!--bean:write property="<%= "attributes" %>" name="typesEditorForm" /-->
<form action="javascript:nothing()">
	<table width=650 bgcolor="#7C8DBF">
		<tr><td>
			<div id="mainEditDiv" name="mainEditDiv">
			</div>
		</td></tr>
	</table>
	<i><font color="#770000">* All fields are required.</font></i><br>&nbsp;<br>
	
	<input type="submit" value="Apply Style" onclick="generateSLD()">
	<div id="sld_success"><span id="inner_sld_success" name="inner_sld_success"></span></div>
</form>


<font color="#222222"><i>You must apply the style before it will be saved.<br>
Hit the 'Apply Style' button above'.</i></font><be>

<!-- finished button "back to FeatureType editor" -->
<form action="<%=request.getContextPath()%>/config/data/typeEditor.do">
	<input type="submit" value="Finished">
</form>

<p>&nbsp;<br>
<span><iframe width=90% height=150px name=logFrame></iframe></span><!-- style="display:none"-->
<br>




</body>

</html>
