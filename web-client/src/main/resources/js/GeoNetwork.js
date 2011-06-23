Ext.namespace("Ext.ux.form");
Ext.ux.form.TwinTriggerComboBox=function(a){Ext.ux.form.TwinTriggerComboBox.superclass.constructor.apply(this,arguments)
};
Ext.extend(Ext.ux.form.TwinTriggerComboBox,Ext.form.ComboBox,{trigger1Class:"x-form-clear-trigger",trigger2Class:"",trigger3Class:"x-form-trigger3-trigger",hideTrigger1:true,tooltipType:"qtip",initComponent:function(){Ext.ux.form.TwinTriggerComboBox.superclass.initComponent.call(this);
this.addEvents({clear:true,trigger3:true});
this.triggerConfig={tag:"span",cls:"x-form-twin-triggers",cn:[{tag:"img",src:Ext.BLANK_IMAGE_URL,cls:"x-form-trigger "+this.trigger1Class},{tag:"img",src:Ext.BLANK_IMAGE_URL,cls:"x-form-trigger "+this.trigger2Class},{tag:"img",src:Ext.BLANK_IMAGE_URL,cls:"x-form-trigger "+this.trigger3Class}]}
},getTrigger:function(a){return this.triggers[a]
},initTrigger:function(){var c=this.trigger.select(".x-form-trigger",true);
var e=this;
c.each(function(g,i,f){g.hide=function(){var j=e.wrap.getWidth();
this.dom.style.display="none";
e.el.setWidth(j-e.trigger.getWidth())
};
g.show=function(){var j=e.wrap.getWidth();
this.dom.style.display="";
e.el.setWidth(j-e.trigger.getWidth())
};
var h="Trigger"+(f+1);
if(this["hide"+h]){g.dom.style.display="none"
}g.on("click",this["on"+h+"Click"],this,{preventDefault:true});
g.addClassOnOver("x-form-trigger-over");
g.addClassOnClick("x-form-trigger-click")
},this);
this.triggers=c.elements;
if(this.trigger3TipConfig){var a={target:this.getTrigger(2)};
for(var b in this.helpTipConfig){a[b]=this.helpTipConfig[b]
}var d=new Ext.ToolTip(a)
}if(this.trigger3TipConfig){if(typeof this.trigger3TipConfig=="object"){Ext.QuickTips.register(Ext.apply({target:this.getTrigger(2)},this.trigger3TipConfig))
}else{this.getTrigger(2).dom[this.tooltipType]=this.trigger3TipConfig
}}},onTrigger1Click:function(){this.clearValue();
this.triggerBlur.defer(50,this)
},onTrigger2Click:function(){this.onTriggerClick()
},onTrigger3Click:function(){this.fireEvent("trigger3Click",this)
},onSelect:function(a,b){Ext.ux.form.TwinTriggerComboBox.superclass.onSelect.apply(this,[a,b]);
this.triggers[0].show()
},clearValue:function(){Ext.ux.form.TwinTriggerComboBox.superclass.clearValue.call(this);
this.triggers[0].hide();
this.fireEvent("clear",this)
},insert:function(a,b){this.reset();
var c=new this.store.recordType(b);
c.id=c.data.id;
this.store.insert(a,c);
this.setValue(c.data.id);
this.fireEvent("select",this,c,a)
}});
Ext.reg("twintriggercombo",Ext.ux.form.TwinTriggerComboBox);/*
 * Ext Core Library $version&#xD;&#xA;http://extjs.com/&#xD;&#xA;Copyright(c) 2006-2009, $author.&#xD;&#xA;&#xD;&#xA;The MIT License&#xD;&#xA;&#xD;&#xA;Permission is hereby granted, free of charge, to any person obtaining a copy&#xD;&#xA;of this software and associated documentation files (the &quot;Software&quot;), to deal&#xD;&#xA;in the Software without restriction, including without limitation the rights&#xD;&#xA;to use, copy, modify, merge, publish, distribute, sublicense, and/or sell&#xD;&#xA;copies of the Software, and to permit persons to whom the Software is&#xD;&#xA;furnished to do so, subject to the following conditions:&#xD;&#xA;&#xD;&#xA;The above copyright notice and this permission notice shall be included in&#xD;&#xA;all copies or substantial portions of the Software.&#xD;&#xA;&#xD;&#xA;THE SOFTWARE IS PROVIDED &quot;AS IS&quot;, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR&#xD;&#xA;IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,&#xD;&#xA;FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE&#xD;&#xA;AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER&#xD;&#xA;LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,&#xD;&#xA;OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN&#xD;&#xA;THE SOFTWARE.&#xD;&#xA;
 */
Ext.ns("Ext.ux");
Ext.ux.RatingItem=Ext.extend(Ext.Component,{starWidth:16,resetValue:"",defaultConfig:{defaultSelected:-1,nbStars:5,split:1,selected:-1,showTitles:true,cls:"",disabled:false},constructor:function(c,a){Ext.apply(this,a);
Ext.applyIf(this,this.defaultConfig);
Ext.ux.RatingItem.superclass.constructor.call(this);
this.addEvents("change","reset");
if(c==undefined){c=document.createElement("div");
c.className=this.itemCls;
this.text="";
for(var b=0;
b<(this.nbStars*this.split);
b++){this.text+='<input type="radio" name="rating'+this.id+'" value="'+(b+1)/this.split+'" '+((b+1)/this.split==this.defaultSelected?' checked="true"':"")+'"/>'
}c.innerHTML=this.text
}this.el=Ext.get(c);
this.init()
},onRender:function(){Ext.ux.RatingItem.superclass.onRender.apply(this,arguments)
},init:function(){var a=this.disabled;
this.values=[];
this.titles=[];
this.stars=[];
this.container=this.el.createChild({cls:"ux-rating-container ux-rating-clearfix"+this.cls});
if(this.canReset){this.resetEl=this.container.createChild({cls:"ux-rating-reset",cn:[{tag:"a",title:this.showTitles?(this.resetTitle||"Reset your vote"):"",html:"Reset"}]});
this.resetEl.visibilityMode=Ext.Element.DISPLAY;
this.resetEl.hover(function(){Ext.fly(this).addClass("ux-rating-reset-hover")
},function(){Ext.fly(this).removeClass("ux-rating-reset-hover")
});
this.resetEl.on("click",this.reset,this)
}this.radioBoxes=this.el.select("input[type=radio]");
this.radioBoxes.each(this.initStar,this);
this.input=this.container.createChild({tag:"input",type:"hidden",name:this.name,value:this.values[this.defaultSelected]||this.resetValue});
this.radioBoxes.remove();
this.select(this.defaultSelected===undefined?false:this.defaultSelected);
if(a){this.disable()
}else{this.enable()
}},initStar:function(d,c,b){var a=Math.floor(this.starWidth/this.split);
if(b==0){this.name=d.dom.name;
this.disabled=d.dom.disabled
}this.values[b]=d.dom.value;
this.titles[b]=d.dom.title;
if(d.dom.checked){this.defaultSelected=b
}var g=this.container.createChild({cls:"ux-rating-star"});
var f=g.createChild({tag:"a",html:this.values[b],title:this.showTitles?this.titles[b]:""});
if(this.split){var e=(b%this.split);
g.setWidth(a);
f.setStyle("margin-left","-"+(e*a)+"px")
}this.stars.push(g.dom)
},onStarClick:function(b,a){if(!this.disabled){this.select(this.stars.indexOf(a))
}},onStarOver:function(b,a){if(!this.disabled){this.fillTo(this.stars.indexOf(a),true)
}},onStarOut:function(b,a){if(!this.disabled){this.fillTo(this.selected,false)
}},reset:function(b,a){this.select(-1)
},select:function(a){if(a===false||a===-1){this.value=this.resetValue;
this.title="";
this.input.dom.value="";
if(this.canReset){this.resetEl.setOpacity(0.5)
}this.fillNone();
if(this.selected!==-1){this.fireEvent("change",this,this.values[a],this.stars[a])
}this.selected=-1
}else{if(a!==this.selected){this.selected=a;
this.value=this.values[a];
this.title=this.titles[a];
this.input.dom.value=this.value;
if(this.canReset){this.resetEl.setOpacity(0.99)
}this.fillTo(a,false);
this.fireEvent("change",this,this.values[a],this.stars[a])
}}},fillTo:function(a,b){if(a!=-1){var c=b?"ux-rating-star-hover":"ux-rating-star-on";
var d=b?"ux-rating-star-on":"ux-rating-star-hover";
Ext.each(this.stars.slice(0,a+1),function(){Ext.fly(this).removeClass(d).addClass(c)
});
Ext.each(this.stars.slice(a+1),function(){Ext.fly(this).removeClass([d,c])
})
}else{this.fillNone()
}},fillNone:function(){this.container.select(".ux-rating-star").removeClass(["ux-rating-star-hover","ux-rating-star-on"])
},enable:function(){if(this.canReset){this.resetEl.show()
}this.input.dom.disabled=null;
this.disabled=false;
this.container.removeClass("ux-rating-disabled");
this.container.on({click:this.onStarClick,mouseover:this.onStarOver,mouseout:this.onStarOut,scope:this,delegate:"div.ux-rating-star"})
},disable:function(){if(this.canReset){this.resetEl.hide()
}this.input.dom.disabled=true;
this.disabled=true;
this.container.addClass("ux-rating-disabled");
this.container.un({click:this.onStarClick,mouseover:this.onStarOver,mouseout:this.onStarOut,scope:this,delegate:"div.ux-rating-star"})
},getValue:function(){return this.values[this.selected]||this.resetValue
},destroy:function(){this.disable();
this.container.remove();
this.radioBoxes.appendTo(this.el);
if(this.selected!==-1){this.radioBoxes.elements[this.selected].checked=true
}}});
Ext.reg("ratingitem",Ext.ux.RatingItem);Ext.form.FileUploadField=Ext.extend(Ext.form.TextField,{buttonText:"Browse...",buttonOnly:false,buttonOffset:3,readOnly:true,autoSize:Ext.emptyFn,initComponent:function(){Ext.form.FileUploadField.superclass.initComponent.call(this);
this.addEvents("fileselected")
},onRender:function(c,a){Ext.form.FileUploadField.superclass.onRender.call(this,c,a);
this.wrap=this.el.wrap({cls:"x-form-field-wrap x-form-file-wrap"});
this.el.addClass("x-form-file-text");
this.el.dom.removeAttribute("name");
this.fileInput=this.wrap.createChild({id:this.getFileInputId(),name:this.name||this.getId(),cls:"x-form-file",tag:"input",type:"file",size:1});
var b=Ext.applyIf(this.buttonCfg||{},{text:this.buttonText});
this.button=new Ext.Button(Ext.apply(b,{renderTo:this.wrap,cls:"x-form-file-btn"+(b.iconCls?" x-btn-icon":"")}));
if(this.buttonOnly){this.el.hide();
this.wrap.setWidth(this.button.getEl().getWidth())
}this.fileInput.on("change",function(){var d=this.fileInput.dom.value;
this.setValue(d);
this.fireEvent("fileselected",this,d)
},this)
},getFileInputId:function(){return this.id+"-file"
},onResize:function(a,b){Ext.form.FileUploadField.superclass.onResize.call(this,a,b);
this.wrap.setWidth(a);
if(!this.buttonOnly){var a=this.wrap.getWidth()-this.button.getEl().getWidth()-this.buttonOffset;
this.el.setWidth(a)
}},preFocus:Ext.emptyFn,getResizeEl:function(){return this.wrap
},getPositionEl:function(){return this.wrap
},alignErrorIcon:function(){this.errorIcon.alignTo(this.wrap,"tl-tr",[2,0])
}});
Ext.reg("fileuploadfield",Ext.form.FileUploadField);var OpenLayers={singleFile:true};(function(){var j=(typeof OpenLayers=="object"&&OpenLayers.singleFile);
var a;
window.OpenLayers={_scriptName:(!j)?"lib/OpenLayers.js":"OpenLayers.js",_getScriptLocation:function(){if(a!=undefined){return a
}a="";
var r=new RegExp("(^|(.*?\\/))("+OpenLayers._scriptName+")(\\?|$)");
var n=document.getElementsByTagName("script");
for(var p=0,h=n.length;
p<h;
p++){var q=n[p].getAttribute("src");
if(q){var o=q.match(r);
if(o){a=o[1];
break
}}}return a
}};
if(!j){var k=new Array("OpenLayers/Util.js","OpenLayers/BaseTypes.js","OpenLayers/BaseTypes/Class.js","OpenLayers/BaseTypes/Bounds.js","OpenLayers/BaseTypes/Element.js","OpenLayers/BaseTypes/LonLat.js","OpenLayers/BaseTypes/Pixel.js","OpenLayers/BaseTypes/Size.js","OpenLayers/Console.js","OpenLayers/Tween.js","Rico/Corner.js","Rico/Color.js","OpenLayers/Ajax.js","OpenLayers/Events.js","OpenLayers/Request.js","OpenLayers/Request/XMLHttpRequest.js","OpenLayers/Projection.js","OpenLayers/Map.js","OpenLayers/Layer.js","OpenLayers/Icon.js","OpenLayers/Marker.js","OpenLayers/Marker/Box.js","OpenLayers/Popup.js","OpenLayers/Tile.js","OpenLayers/Tile/Image.js","OpenLayers/Tile/Image/IFrame.js","OpenLayers/Tile/WFS.js","OpenLayers/Layer/Image.js","OpenLayers/Layer/SphericalMercator.js","OpenLayers/Layer/EventPane.js","OpenLayers/Layer/FixedZoomLevels.js","OpenLayers/Layer/Google.js","OpenLayers/Layer/VirtualEarth.js","OpenLayers/Layer/Yahoo.js","OpenLayers/Layer/HTTPRequest.js","OpenLayers/Layer/Grid.js","OpenLayers/Layer/MapGuide.js","OpenLayers/Layer/MapServer.js","OpenLayers/Layer/MapServer/Untiled.js","OpenLayers/Layer/KaMap.js","OpenLayers/Layer/KaMapCache.js","OpenLayers/Layer/MultiMap.js","OpenLayers/Layer/Markers.js","OpenLayers/Layer/Text.js","OpenLayers/Layer/WorldWind.js","OpenLayers/Layer/ArcGIS93Rest.js","OpenLayers/Layer/WMS.js","OpenLayers/Layer/WMS/Untiled.js","OpenLayers/Layer/WMS/Post.js","OpenLayers/Layer/ArcIMS.js","OpenLayers/Layer/GeoRSS.js","OpenLayers/Layer/Boxes.js","OpenLayers/Layer/XYZ.js","OpenLayers/Layer/TMS.js","OpenLayers/Layer/TileCache.js","OpenLayers/Layer/Zoomify.js","OpenLayers/Popup/Anchored.js","OpenLayers/Popup/AnchoredBubble.js","OpenLayers/Popup/Framed.js","OpenLayers/Popup/FramedCloud.js","OpenLayers/Feature.js","OpenLayers/Feature/Vector.js","OpenLayers/Feature/WFS.js","OpenLayers/Handler.js","OpenLayers/Handler/Click.js","OpenLayers/Handler/Hover.js","OpenLayers/Handler/Point.js","OpenLayers/Handler/Path.js","OpenLayers/Handler/Polygon.js","OpenLayers/Handler/Feature.js","OpenLayers/Handler/Drag.js","OpenLayers/Handler/RegularPolygon.js","OpenLayers/Handler/Box.js","OpenLayers/Handler/MouseWheel.js","OpenLayers/Handler/Keyboard.js","OpenLayers/Control.js","OpenLayers/Control/Attribution.js","OpenLayers/Control/Button.js","OpenLayers/Control/ZoomBox.js","OpenLayers/Control/ZoomToMaxExtent.js","OpenLayers/Control/DragPan.js","OpenLayers/Control/Navigation.js","OpenLayers/Control/MouseDefaults.js","OpenLayers/Control/MousePosition.js","OpenLayers/Control/OverviewMap.js","OpenLayers/Control/KeyboardDefaults.js","OpenLayers/Control/PanZoom.js","OpenLayers/Control/PanZoomBar.js","OpenLayers/Control/ArgParser.js","OpenLayers/Control/Permalink.js","OpenLayers/Control/Scale.js","OpenLayers/Control/ScaleLine.js","OpenLayers/Control/Snapping.js","OpenLayers/Control/Split.js","OpenLayers/Control/LayerSwitcher.js","OpenLayers/Control/DrawFeature.js","OpenLayers/Control/DragFeature.js","OpenLayers/Control/ModifyFeature.js","OpenLayers/Control/Panel.js","OpenLayers/Control/SelectFeature.js","OpenLayers/Control/NavigationHistory.js","OpenLayers/Control/Measure.js","OpenLayers/Control/WMSGetFeatureInfo.js","OpenLayers/Control/Graticule.js","OpenLayers/Control/TransformFeature.js","OpenLayers/Geometry.js","OpenLayers/Geometry/Rectangle.js","OpenLayers/Geometry/Collection.js","OpenLayers/Geometry/Point.js","OpenLayers/Geometry/MultiPoint.js","OpenLayers/Geometry/Curve.js","OpenLayers/Geometry/LineString.js","OpenLayers/Geometry/LinearRing.js","OpenLayers/Geometry/Polygon.js","OpenLayers/Geometry/MultiLineString.js","OpenLayers/Geometry/MultiPolygon.js","OpenLayers/Geometry/Surface.js","OpenLayers/Renderer.js","OpenLayers/Renderer/Elements.js","OpenLayers/Renderer/SVG.js","OpenLayers/Renderer/Canvas.js","OpenLayers/Renderer/VML.js","OpenLayers/Layer/Vector.js","OpenLayers/Layer/Vector/RootContainer.js","OpenLayers/Strategy.js","OpenLayers/Strategy/Fixed.js","OpenLayers/Strategy/Cluster.js","OpenLayers/Strategy/Paging.js","OpenLayers/Strategy/BBOX.js","OpenLayers/Strategy/Save.js","OpenLayers/Strategy/Refresh.js","OpenLayers/Filter.js","OpenLayers/Filter/FeatureId.js","OpenLayers/Filter/Logical.js","OpenLayers/Filter/Comparison.js","OpenLayers/Filter/Spatial.js","OpenLayers/Protocol.js","OpenLayers/Protocol/HTTP.js","OpenLayers/Protocol/SQL.js","OpenLayers/Protocol/SQL/Gears.js","OpenLayers/Protocol/WFS.js","OpenLayers/Protocol/WFS/v1.js","OpenLayers/Protocol/WFS/v1_0_0.js","OpenLayers/Protocol/WFS/v1_1_0.js","OpenLayers/Protocol/SOS.js","OpenLayers/Protocol/SOS/v1_0_0.js","OpenLayers/Layer/PointTrack.js","OpenLayers/Layer/GML.js","OpenLayers/Style.js","OpenLayers/StyleMap.js","OpenLayers/Rule.js","OpenLayers/Format.js","OpenLayers/Format/XML.js","OpenLayers/Format/ArcXML.js","OpenLayers/Format/ArcXML/Features.js","OpenLayers/Format/GML.js","OpenLayers/Format/GML/Base.js","OpenLayers/Format/GML/v2.js","OpenLayers/Format/GML/v3.js","OpenLayers/Format/Atom.js","OpenLayers/Format/KML.js","OpenLayers/Format/GeoRSS.js","OpenLayers/Format/WFS.js","OpenLayers/Format/WFSCapabilities.js","OpenLayers/Format/WFSCapabilities/v1.js","OpenLayers/Format/WFSCapabilities/v1_0_0.js","OpenLayers/Format/WFSCapabilities/v1_1_0.js","OpenLayers/Format/WFSDescribeFeatureType.js","OpenLayers/Format/WMSDescribeLayer.js","OpenLayers/Format/WMSDescribeLayer/v1_1.js","OpenLayers/Format/WKT.js","OpenLayers/Format/OSM.js","OpenLayers/Format/GPX.js","OpenLayers/Format/Filter.js","OpenLayers/Format/Filter/v1.js","OpenLayers/Format/Filter/v1_0_0.js","OpenLayers/Format/Filter/v1_1_0.js","OpenLayers/Format/SLD.js","OpenLayers/Format/SLD/v1.js","OpenLayers/Format/SLD/v1_0_0.js","OpenLayers/Format/CSWGetDomain.js","OpenLayers/Format/CSWGetDomain/v2_0_2.js","OpenLayers/Format/CSWGetRecords.js","OpenLayers/Format/CSWGetRecords/v2_0_2.js","OpenLayers/Format/WFST.js","OpenLayers/Format/WFST/v1.js","OpenLayers/Format/WFST/v1_0_0.js","OpenLayers/Format/WFST/v1_1_0.js","OpenLayers/Format/Text.js","OpenLayers/Format/JSON.js","OpenLayers/Format/GeoJSON.js","OpenLayers/Format/WMC.js","OpenLayers/Format/WMC/v1.js","OpenLayers/Format/WMC/v1_0_0.js","OpenLayers/Format/WMC/v1_1_0.js","OpenLayers/Format/WMSCapabilities.js","OpenLayers/Format/WMSCapabilities/v1.js","OpenLayers/Format/WMSCapabilities/v1_1.js","OpenLayers/Format/WMSCapabilities/v1_1_0.js","OpenLayers/Format/WMSCapabilities/v1_1_1.js","OpenLayers/Format/WMSCapabilities/v1_3.js","OpenLayers/Format/WMSCapabilities/v1_3_0.js","OpenLayers/Format/WMSGetFeatureInfo.js","OpenLayers/Format/OWSCommon/v1_1_0.js","OpenLayers/Format/SOSCapabilities.js","OpenLayers/Format/SOSCapabilities/v1_0_0.js","OpenLayers/Format/SOSGetObservation.js","OpenLayers/Format/SOSGetFeatureOfInterest.js","OpenLayers/Layer/WFS.js","OpenLayers/Control/GetFeature.js","OpenLayers/Control/MouseToolbar.js","OpenLayers/Control/NavToolbar.js","OpenLayers/Control/PanPanel.js","OpenLayers/Control/Pan.js","OpenLayers/Control/ZoomIn.js","OpenLayers/Control/ZoomOut.js","OpenLayers/Control/ZoomPanel.js","OpenLayers/Control/EditingToolbar.js","OpenLayers/Lang.js","OpenLayers/Lang/en.js");
var c=navigator.userAgent;
var e=(c.match("MSIE")||c.match("Safari"));
if(e){var b=new Array(k.length)
}var l=OpenLayers._getScriptLocation()+"lib/";
for(var d=0,g=k.length;
d<g;
d++){if(e){b[d]="<script src='"+l+k[d]+"'><\/script>"
}else{var m=document.createElement("script");
m.src=l+k[d];
var f=document.getElementsByTagName("head").length?document.getElementsByTagName("head")[0]:document.body;
f.appendChild(m)
}}if(e){document.write(b.join(""))
}}})();
OpenLayers.VERSION_NUMBER="OpenLayers 2.9.1 -- $Revision: 10129 $";OpenLayers.Util={};
OpenLayers.Util.getElement=function(){var d=[];
for(var c=0,a=arguments.length;
c<a;
c++){var b=arguments[c];
if(typeof b=="string"){b=document.getElementById(b)
}if(arguments.length==1){return b
}d.push(b)
}return d
};
OpenLayers.Util.isElement=function(a){return !!(a&&a.nodeType===1)
};
if(typeof window.$==="undefined"){window.$=OpenLayers.Util.getElement
}OpenLayers.Util.extend=function(a,e){a=a||{};
if(e){for(var d in e){var c=e[d];
if(c!==undefined){a[d]=c
}}var b=typeof window.Event=="function"&&e instanceof window.Event;
if(!b&&e.hasOwnProperty&&e.hasOwnProperty("toString")){a.toString=e.toString
}}return a
};
OpenLayers.Util.removeItem=function(c,b){for(var a=c.length-1;
a>=0;
a--){if(c[a]==b){c.splice(a,1)
}}return c
};
OpenLayers.Util.clearArray=function(a){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"array = []"}));
a.length=0
};
OpenLayers.Util.indexOf=function(d,c){if(typeof d.indexOf=="function"){return d.indexOf(c)
}else{for(var b=0,a=d.length;
b<a;
b++){if(d[b]==c){return b
}}return -1
}};
OpenLayers.Util.modifyDOMElement=function(e,h,d,f,a,c,g,b){if(h){e.id=h
}if(d){e.style.left=d.x+"px";
e.style.top=d.y+"px"
}if(f){e.style.width=f.w+"px";
e.style.height=f.h+"px"
}if(a){e.style.position=a
}if(c){e.style.border=c
}if(g){e.style.overflow=g
}if(parseFloat(b)>=0&&parseFloat(b)<1){e.style.filter="alpha(opacity="+(b*100)+")";
e.style.opacity=b
}else{if(parseFloat(b)==1){e.style.filter="";
e.style.opacity=""
}}};
OpenLayers.Util.createDiv=function(a,i,h,f,e,c,b,g){var d=document.createElement("div");
if(f){d.style.backgroundImage="url("+f+")"
}if(!a){a=OpenLayers.Util.createUniqueID("OpenLayersDiv")
}if(!e){e="absolute"
}OpenLayers.Util.modifyDOMElement(d,a,i,h,e,c,b,g);
return d
};
OpenLayers.Util.createImage=function(a,h,g,e,d,c,f,i){var b=document.createElement("img");
if(!a){a=OpenLayers.Util.createUniqueID("OpenLayersDiv")
}if(!d){d="relative"
}OpenLayers.Util.modifyDOMElement(b,a,h,g,d,c,null,f);
if(i){b.style.display="none";
OpenLayers.Event.observe(b,"load",OpenLayers.Function.bind(OpenLayers.Util.onImageLoad,b));
OpenLayers.Event.observe(b,"error",OpenLayers.Function.bind(OpenLayers.Util.onImageLoadError,b))
}b.style.alt=a;
b.galleryImg="no";
if(e){b.src=e
}return b
};
OpenLayers.Util.setOpacity=function(b,a){OpenLayers.Util.modifyDOMElement(b,null,null,null,null,null,null,a)
};
OpenLayers.Util.onImageLoad=function(){if(!this.viewRequestID||(this.map&&this.viewRequestID==this.map.viewRequestID)){this.style.display=""
}OpenLayers.Element.removeClass(this,"olImageLoadError")
};
OpenLayers.IMAGE_RELOAD_ATTEMPTS=0;
OpenLayers.Util.onImageLoadError=function(){this._attempts=(this._attempts)?(this._attempts+1):1;
if(this._attempts<=OpenLayers.IMAGE_RELOAD_ATTEMPTS){var d=this.urls;
if(d&&d instanceof Array&&d.length>1){var e=this.src.toString();
var c,a;
for(a=0;
c=d[a];
a++){if(e.indexOf(c)!=-1){break
}}var f=Math.floor(d.length*Math.random());
var b=d[f];
a=0;
while(b==c&&a++<4){f=Math.floor(d.length*Math.random());
b=d[f]
}this.src=e.replace(c,b)
}else{this.src=this.src
}}else{OpenLayers.Element.addClass(this,"olImageLoadError")
}this.style.display=""
};
OpenLayers.Util.alphaHackNeeded=null;
OpenLayers.Util.alphaHack=function(){if(OpenLayers.Util.alphaHackNeeded==null){var d=navigator.appVersion.split("MSIE");
var a=parseFloat(d[1]);
var b=false;
try{b=!!(document.body.filters)
}catch(c){}OpenLayers.Util.alphaHackNeeded=(b&&(a>=5.5)&&(a<7))
}return OpenLayers.Util.alphaHackNeeded
};
OpenLayers.Util.modifyAlphaImageDiv=function(a,b,j,i,g,f,c,d,h){OpenLayers.Util.modifyDOMElement(a,b,j,i,f,null,null,h);
var e=a.childNodes[0];
if(g){e.src=g
}OpenLayers.Util.modifyDOMElement(e,a.id+"_innerImage",null,i,"relative",c);
if(OpenLayers.Util.alphaHack()){if(a.style.display!="none"){a.style.display="inline-block"
}if(d==null){d="scale"
}a.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+e.src+"', sizingMethod='"+d+"')";
if(parseFloat(a.style.opacity)>=0&&parseFloat(a.style.opacity)<1){a.style.filter+=" alpha(opacity="+a.style.opacity*100+")"
}e.style.filter="alpha(opacity=0)"
}};
OpenLayers.Util.createAlphaImageDiv=function(b,j,i,g,f,c,d,h,k){var a=OpenLayers.Util.createDiv();
var e=OpenLayers.Util.createImage(null,null,null,null,null,null,null,false);
a.appendChild(e);
if(k){e.style.display="none";
OpenLayers.Event.observe(e,"load",OpenLayers.Function.bind(OpenLayers.Util.onImageLoad,a));
OpenLayers.Event.observe(e,"error",OpenLayers.Function.bind(OpenLayers.Util.onImageLoadError,a))
}OpenLayers.Util.modifyAlphaImageDiv(a,b,j,i,g,f,c,d,h);
return a
};
OpenLayers.Util.upperCaseObject=function(b){var a={};
for(var c in b){a[c.toUpperCase()]=b[c]
}return a
};
OpenLayers.Util.applyDefaults=function(d,c){d=d||{};
var b=typeof window.Event=="function"&&c instanceof window.Event;
for(var a in c){if(d[a]===undefined||(!b&&c.hasOwnProperty&&c.hasOwnProperty(a)&&!d.hasOwnProperty(a))){d[a]=c[a]
}}if(!b&&c&&c.hasOwnProperty&&c.hasOwnProperty("toString")&&!d.hasOwnProperty("toString")){d.toString=c.toString
}return d
};
OpenLayers.Util.getParameterString=function(c){var b=[];
for(var h in c){var g=c[h];
if((g!=null)&&(typeof g!="function")){var d;
if(typeof g=="object"&&g.constructor==Array){var e=[];
var i;
for(var a=0,f=g.length;
a<f;
a++){i=g[a];
e.push(encodeURIComponent((i===null||i===undefined)?"":i))
}d=e.join(",")
}else{d=encodeURIComponent(g)
}b.push(encodeURIComponent(h)+"="+d)
}}return b.join("&")
};
OpenLayers.Util.urlAppend=function(a,b){var d=a;
if(b){var c=(a+" ").split(/[?&]/);
d+=(c.pop()===" "?b:c.length?"&"+b:"?"+b)
}return d
};
OpenLayers.ImgPath="";
OpenLayers.Util.getImagesLocation=function(){return OpenLayers.ImgPath||(OpenLayers._getScriptLocation()+"img/")
};
OpenLayers.Util.Try=function(){var d=null;
for(var c=0,a=arguments.length;
c<a;
c++){var b=arguments[c];
try{d=b();
break
}catch(f){}}return d
};
OpenLayers.Util.getNodes=function(c,b){var a=OpenLayers.Util.Try(function(){return OpenLayers.Util._getNodes(c.documentElement.childNodes,b)
},function(){return OpenLayers.Util._getNodes(c.childNodes,b)
});
return a
};
OpenLayers.Util._getNodes=function(c,e){var b=[];
for(var d=0,a=c.length;
d<a;
d++){if(c[d].nodeName==e){b.push(c[d])
}}return b
};
OpenLayers.Util.getTagText=function(c,d,b){var a=OpenLayers.Util.getNodes(c,d);
if(a&&(a.length>0)){if(!b){b=0
}if(a[b].childNodes.length>1){return a.childNodes[1].nodeValue
}else{if(a[b].childNodes.length==1){return a[b].firstChild.nodeValue
}}}else{return""
}};
OpenLayers.Util.getXmlNodeValue=function(a){var b=null;
OpenLayers.Util.Try(function(){b=a.text;
if(!b){b=a.textContent
}if(!b){b=a.firstChild.nodeValue
}},function(){b=a.textContent
});
return b
};
OpenLayers.Util.mouseLeft=function(a,c){var b=(a.relatedTarget)?a.relatedTarget:a.toElement;
while(b!=c&&b!=null){b=b.parentNode
}return(b!=c)
};
OpenLayers.Util.DEFAULT_PRECISION=14;
OpenLayers.Util.toFloat=function(b,a){if(a==null){a=OpenLayers.Util.DEFAULT_PRECISION
}var b;
if(a==0){b=parseFloat(b)
}else{b=parseFloat(parseFloat(b).toPrecision(a))
}return b
};
OpenLayers.Util.rad=function(a){return a*Math.PI/180
};
OpenLayers.Util.distVincenty=function(g,c){var K=6378137,J=6356752.3142,F=1/298.257223563;
var m=OpenLayers.Util.rad(c.lon-g.lon);
var I=Math.atan((1-F)*Math.tan(OpenLayers.Util.rad(g.lat)));
var H=Math.atan((1-F)*Math.tan(OpenLayers.Util.rad(c.lat)));
var l=Math.sin(I),i=Math.cos(I);
var k=Math.sin(H),h=Math.cos(H);
var q=m,n=2*Math.PI;
var p=20;
while(Math.abs(q-n)>1e-12&&--p>0){var y=Math.sin(q),e=Math.cos(q);
var M=Math.sqrt((h*y)*(h*y)+(i*k-l*h*e)*(i*k-l*h*e));
if(M==0){return 0
}var D=l*k+i*h*e;
var x=Math.atan2(M,D);
var j=Math.asin(i*h*y/M);
var E=Math.cos(j)*Math.cos(j);
var o=D-2*l*k/E;
var u=F/16*E*(4+F*(4-3*E));
n=q;
q=m+(1-u)*F*Math.sin(j)*(x+u*M*(o+u*D*(-1+2*o*o)))
}if(p==0){return NaN
}var t=E*(K*K-J*J)/(J*J);
var w=1+t/16384*(4096+t*(-768+t*(320-175*t)));
var v=t/1024*(256+t*(-128+t*(74-47*t)));
var z=v*M*(o+v/4*(D*(-1+2*o*o)-v/6*o*(-3+4*M*M)*(-3+4*o*o)));
var r=J*w*(x-z);
var G=r.toFixed(3)/1000;
return G
};
OpenLayers.Util.getParameters=function(b){b=b||window.location.href;
var a="";
if(OpenLayers.String.contains(b,"?")){var c=b.indexOf("?")+1;
var e=OpenLayers.String.contains(b,"#")?b.indexOf("#"):b.length;
a=b.substring(c,e)
}var l={};
var d=a.split(/[&;]/);
for(var g=0,h=d.length;
g<h;
++g){var f=d[g].split("=");
if(f[0]){var k=decodeURIComponent(f[0]);
var j=f[1]||"";
j=decodeURIComponent(j.replace(/\+/g," ")).split(",");
if(j.length==1){j=j[0]
}l[k]=j
}}return l
};
OpenLayers.Util.getArgs=function(a){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.Util.getParameters"}));
return OpenLayers.Util.getParameters(a)
};
OpenLayers.Util.lastSeqID=0;
OpenLayers.Util.createUniqueID=function(a){if(a==null){a="id_"
}OpenLayers.Util.lastSeqID+=1;
return a+OpenLayers.Util.lastSeqID
};
OpenLayers.INCHES_PER_UNIT={inches:1,ft:12,mi:63360,m:39.3701,km:39370.1,dd:4374754,yd:36};
OpenLayers.INCHES_PER_UNIT["in"]=OpenLayers.INCHES_PER_UNIT.inches;
OpenLayers.INCHES_PER_UNIT.degrees=OpenLayers.INCHES_PER_UNIT.dd;
OpenLayers.INCHES_PER_UNIT.nmi=1852*OpenLayers.INCHES_PER_UNIT.m;
OpenLayers.METERS_PER_INCH=0.0254000508001016;
OpenLayers.Util.extend(OpenLayers.INCHES_PER_UNIT,{Inch:OpenLayers.INCHES_PER_UNIT.inches,Meter:1/OpenLayers.METERS_PER_INCH,Foot:0.3048006096012192/OpenLayers.METERS_PER_INCH,IFoot:0.3048/OpenLayers.METERS_PER_INCH,ClarkeFoot:0.3047972651151/OpenLayers.METERS_PER_INCH,SearsFoot:0.30479947153867626/OpenLayers.METERS_PER_INCH,GoldCoastFoot:0.3047997101815088/OpenLayers.METERS_PER_INCH,IInch:0.0254/OpenLayers.METERS_PER_INCH,MicroInch:0.0000254/OpenLayers.METERS_PER_INCH,Mil:2.54e-8/OpenLayers.METERS_PER_INCH,Centimeter:0.01/OpenLayers.METERS_PER_INCH,Kilometer:1000/OpenLayers.METERS_PER_INCH,Yard:0.9144018288036576/OpenLayers.METERS_PER_INCH,SearsYard:0.914398414616029/OpenLayers.METERS_PER_INCH,IndianYard:0.9143985307444408/OpenLayers.METERS_PER_INCH,IndianYd37:0.91439523/OpenLayers.METERS_PER_INCH,IndianYd62:0.9143988/OpenLayers.METERS_PER_INCH,IndianYd75:0.9143985/OpenLayers.METERS_PER_INCH,IndianFoot:0.30479951/OpenLayers.METERS_PER_INCH,IndianFt37:0.30479841/OpenLayers.METERS_PER_INCH,IndianFt62:0.3047996/OpenLayers.METERS_PER_INCH,IndianFt75:0.3047995/OpenLayers.METERS_PER_INCH,Mile:1609.3472186944373/OpenLayers.METERS_PER_INCH,IYard:0.9144/OpenLayers.METERS_PER_INCH,IMile:1609.344/OpenLayers.METERS_PER_INCH,NautM:1852/OpenLayers.METERS_PER_INCH,"Lat-66":110943.31648893273/OpenLayers.METERS_PER_INCH,"Lat-83":110946.25736872235/OpenLayers.METERS_PER_INCH,Decimeter:0.1/OpenLayers.METERS_PER_INCH,Millimeter:0.001/OpenLayers.METERS_PER_INCH,Dekameter:10/OpenLayers.METERS_PER_INCH,Decameter:10/OpenLayers.METERS_PER_INCH,Hectometer:100/OpenLayers.METERS_PER_INCH,GermanMeter:1.0000135965/OpenLayers.METERS_PER_INCH,CaGrid:0.999738/OpenLayers.METERS_PER_INCH,ClarkeChain:20.1166194976/OpenLayers.METERS_PER_INCH,GunterChain:20.11684023368047/OpenLayers.METERS_PER_INCH,BenoitChain:20.116782494375872/OpenLayers.METERS_PER_INCH,SearsChain:20.11676512155/OpenLayers.METERS_PER_INCH,ClarkeLink:0.201166194976/OpenLayers.METERS_PER_INCH,GunterLink:0.2011684023368047/OpenLayers.METERS_PER_INCH,BenoitLink:0.20116782494375873/OpenLayers.METERS_PER_INCH,SearsLink:0.2011676512155/OpenLayers.METERS_PER_INCH,Rod:5.02921005842012/OpenLayers.METERS_PER_INCH,IntnlChain:20.1168/OpenLayers.METERS_PER_INCH,IntnlLink:0.201168/OpenLayers.METERS_PER_INCH,Perch:5.02921005842012/OpenLayers.METERS_PER_INCH,Pole:5.02921005842012/OpenLayers.METERS_PER_INCH,Furlong:201.1684023368046/OpenLayers.METERS_PER_INCH,Rood:3.778266898/OpenLayers.METERS_PER_INCH,CapeFoot:0.3047972615/OpenLayers.METERS_PER_INCH,Brealey:375/OpenLayers.METERS_PER_INCH,ModAmFt:0.304812252984506/OpenLayers.METERS_PER_INCH,Fathom:1.8288/OpenLayers.METERS_PER_INCH,"NautM-UK":1853.184/OpenLayers.METERS_PER_INCH,"50kilometers":50000/OpenLayers.METERS_PER_INCH,"150kilometers":150000/OpenLayers.METERS_PER_INCH});
OpenLayers.Util.extend(OpenLayers.INCHES_PER_UNIT,{mm:OpenLayers.INCHES_PER_UNIT.Meter/1000,cm:OpenLayers.INCHES_PER_UNIT.Meter/100,dm:OpenLayers.INCHES_PER_UNIT.Meter*100,km:OpenLayers.INCHES_PER_UNIT.Meter*1000,kmi:OpenLayers.INCHES_PER_UNIT.nmi,fath:OpenLayers.INCHES_PER_UNIT.Fathom,ch:OpenLayers.INCHES_PER_UNIT.IntnlChain,link:OpenLayers.INCHES_PER_UNIT.IntnlLink,"us-in":OpenLayers.INCHES_PER_UNIT.inches,"us-ft":OpenLayers.INCHES_PER_UNIT.Foot,"us-yd":OpenLayers.INCHES_PER_UNIT.Yard,"us-ch":OpenLayers.INCHES_PER_UNIT.GunterChain,"us-mi":OpenLayers.INCHES_PER_UNIT.Mile,"ind-yd":OpenLayers.INCHES_PER_UNIT.IndianYd37,"ind-ft":OpenLayers.INCHES_PER_UNIT.IndianFt37,"ind-ch":20.11669506/OpenLayers.METERS_PER_INCH});
OpenLayers.DOTS_PER_INCH=72;
OpenLayers.Util.normalizeScale=function(b){var a=(b>1)?(1/b):b;
return a
};
OpenLayers.Util.getResolutionFromScale=function(d,a){var b;
if(d){if(a==null){a="degrees"
}var c=OpenLayers.Util.normalizeScale(d);
b=1/(c*OpenLayers.INCHES_PER_UNIT[a]*OpenLayers.DOTS_PER_INCH)
}return b
};
OpenLayers.Util.getScaleFromResolution=function(b,a){if(a==null){a="degrees"
}var c=b*OpenLayers.INCHES_PER_UNIT[a]*OpenLayers.DOTS_PER_INCH;
return c
};
OpenLayers.Util.safeStopPropagation=function(a){OpenLayers.Event.stop(a,true)
};
OpenLayers.Util.pagePosition=function(f){var a=0,d=0;
var b=f;
var g=f;
while(b){if(b==document.body){if(OpenLayers.Element.getStyle(g,"position")=="absolute"){break
}}a+=b.offsetTop||0;
d+=b.offsetLeft||0;
g=b;
try{b=b.offsetParent
}catch(c){OpenLayers.Console.error(OpenLayers.i18n("pagePositionFailed",{elemId:b.id}));
break
}}b=f;
while(b){a-=b.scrollTop||0;
d-=b.scrollLeft||0;
b=b.parentNode
}return[d,a]
};
OpenLayers.Util.isEquivalentUrl=function(f,e,c){c=c||{};
OpenLayers.Util.applyDefaults(c,{ignoreCase:true,ignorePort80:true,ignoreHash:true});
var b=OpenLayers.Util.createUrlObject(f,c);
var a=OpenLayers.Util.createUrlObject(e,c);
for(var d in b){if(d!=="args"){if(b[d]!=a[d]){return false
}}}for(var d in b.args){if(b.args[d]!=a.args[d]){return false
}delete a.args[d]
}for(var d in a.args){return false
}return true
};
OpenLayers.Util.createUrlObject=function(c,k){k=k||{};
if(!(/^\w+:\/\//).test(c)){var g=window.location;
var e=g.port?":"+g.port:"";
var h=g.protocol+"//"+g.host.split(":").shift()+e;
if(c.indexOf("/")===0){c=h+c
}else{var f=g.pathname.split("/");
f.pop();
c=h+f.join("/")+"/"+c
}}if(k.ignoreCase){c=c.toLowerCase()
}var i=document.createElement("a");
i.href=c;
var d={};
d.host=i.host.split(":").shift();
d.protocol=i.protocol;
if(k.ignorePort80){d.port=(i.port=="80"||i.port=="0")?"":i.port
}else{d.port=(i.port==""||i.port=="0")?"80":i.port
}d.hash=(k.ignoreHash||i.hash==="#")?"":i.hash;
var b=i.search;
if(!b){var j=c.indexOf("?");
b=(j!=-1)?c.substr(j):""
}d.args=OpenLayers.Util.getParameters(b);
d.pathname=(i.pathname.charAt(0)=="/")?i.pathname:"/"+i.pathname;
return d
};
OpenLayers.Util.removeTail=function(b){var c=null;
var a=b.indexOf("?");
var d=b.indexOf("#");
if(a==-1){c=(d!=-1)?b.substr(0,d):b
}else{c=(d!=-1)?b.substr(0,Math.min(a,d)):b.substr(0,a)
}return c
};
OpenLayers.Util.getBrowserName=function(){var b="";
var a=navigator.userAgent.toLowerCase();
if(a.indexOf("opera")!=-1){b="opera"
}else{if(a.indexOf("msie")!=-1){b="msie"
}else{if(a.indexOf("safari")!=-1){b="safari"
}else{if(a.indexOf("mozilla")!=-1){if(a.indexOf("firefox")!=-1){b="firefox"
}else{b="mozilla"
}}}}}return b
};
OpenLayers.Util.getRenderedDimensions=function(b,o,p){var k,e;
var a=document.createElement("div");
a.style.visibility="hidden";
var n=(p&&p.containerElement)?p.containerElement:document.body;
if(o){if(o.w){k=o.w;
a.style.width=k+"px"
}else{if(o.h){e=o.h;
a.style.height=e+"px"
}}}if(p&&p.displayClass){a.className=p.displayClass
}var f=document.createElement("div");
f.innerHTML=b;
f.style.overflow="visible";
if(f.childNodes){for(var d=0,c=f.childNodes.length;
d<c;
d++){if(!f.childNodes[d].style){continue
}f.childNodes[d].style.overflow="visible"
}}a.appendChild(f);
n.appendChild(a);
var m=false;
var j=a.parentNode;
while(j&&j.tagName.toLowerCase()!="body"){var g=OpenLayers.Element.getStyle(j,"position");
if(g=="absolute"){m=true;
break
}else{if(g&&g!="static"){break
}}j=j.parentNode
}if(!m){a.style.position="absolute"
}if(!k){k=parseInt(f.scrollWidth);
a.style.width=k+"px"
}if(!e){e=parseInt(f.scrollHeight)
}a.removeChild(f);
n.removeChild(a);
return new OpenLayers.Size(k,e)
};
OpenLayers.Util.getScrollbarWidth=function(){var c=OpenLayers.Util._scrollbarWidth;
if(c==null){var e=null;
var d=null;
var a=0;
var b=0;
e=document.createElement("div");
e.style.position="absolute";
e.style.top="-1000px";
e.style.left="-1000px";
e.style.width="100px";
e.style.height="50px";
e.style.overflow="hidden";
d=document.createElement("div");
d.style.width="100%";
d.style.height="200px";
e.appendChild(d);
document.body.appendChild(e);
a=d.offsetWidth;
e.style.overflow="scroll";
b=d.offsetWidth;
document.body.removeChild(document.body.lastChild);
OpenLayers.Util._scrollbarWidth=(a-b);
c=OpenLayers.Util._scrollbarWidth
}return c
};
OpenLayers.Util.getFormattedLonLat=function(h,b,e){if(!e){e="dms"
}var d=Math.abs(h);
var i=Math.floor(d);
var a=(d-i)/(1/60);
var c=a;
a=Math.floor(a);
var g=(c-a)/(1/60);
g=Math.round(g*10);
g/=10;
if(i<10){i="0"+i
}var f=i+" ";
if(e.indexOf("dm")>=0){if(a<10){a="0"+a
}f+=a+"'";
if(e.indexOf("dms")>=0){if(g<10){g="0"+g
}f+=g+'"'
}}if(b=="lon"){f+=h<0?OpenLayers.i18n("W"):OpenLayers.i18n("E")
}else{f+=h<0?OpenLayers.i18n("S"):OpenLayers.i18n("N")
}return f
};OpenLayers.String={startsWith:function(b,a){return(b.indexOf(a)==0)
},contains:function(b,a){return(b.indexOf(a)!=-1)
},trim:function(a){return a.replace(/^\s\s*/,"").replace(/\s\s*$/,"")
},camelize:function(f){var d=f.split("-");
var b=d[0];
for(var c=1,a=d.length;
c<a;
c++){var e=d[c];
b+=e.charAt(0).toUpperCase()+e.substring(1)
}return b
},format:function(d,c,a){if(!c){c=window
}var b=function(j,e){var h;
var g=e.split(/\.+/);
for(var f=0;
f<g.length;
f++){if(f==0){h=c
}h=h[g[f]]
}if(typeof h=="function"){h=a?h.apply(null,a):h()
}if(typeof h=="undefined"){return"undefined"
}else{return h
}};
return d.replace(OpenLayers.String.tokenRegEx,b)
},tokenRegEx:/\$\{([\w.]+?)\}/g,numberRegEx:/^([+-]?)(?=\d|\.\d)\d*(\.\d*)?([Ee]([+-]?\d+))?$/,isNumeric:function(a){return OpenLayers.String.numberRegEx.test(a)
},numericIf:function(a){return OpenLayers.String.isNumeric(a)?parseFloat(a):a
}};
if(!String.prototype.startsWith){String.prototype.startsWith=function(a){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.String.startsWith"}));
return OpenLayers.String.startsWith(this,a)
}
}if(!String.prototype.contains){String.prototype.contains=function(a){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.String.contains"}));
return OpenLayers.String.contains(this,a)
}
}if(!String.prototype.trim){String.prototype.trim=function(){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.String.trim"}));
return OpenLayers.String.trim(this)
}
}if(!String.prototype.camelize){String.prototype.camelize=function(){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.String.camelize"}));
return OpenLayers.String.camelize(this)
}
}OpenLayers.Number={decimalSeparator:".",thousandsSeparator:",",limitSigDigs:function(a,c){var b=0;
if(c>0){b=parseFloat(a.toPrecision(c))
}return b
},format:function(c,a,g,i){a=(typeof a!="undefined")?a:0;
g=(typeof g!="undefined")?g:OpenLayers.Number.thousandsSeparator;
i=(typeof i!="undefined")?i:OpenLayers.Number.decimalSeparator;
if(a!=null){c=parseFloat(c.toFixed(a))
}var b=c.toString().split(".");
if(b.length==1&&a==null){a=0
}var d=b[0];
if(g){var e=/(-?[0-9]+)([0-9]{3})/;
while(e.test(d)){d=d.replace(e,"$1"+g+"$2")
}}var f;
if(a==0){f=d
}else{var h=b.length>1?b[1]:"0";
if(a!=null){h=h+new Array(a-h.length+1).join("0")
}f=d+i+h
}return f
}};
if(!Number.prototype.limitSigDigs){Number.prototype.limitSigDigs=function(a){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.Number.limitSigDigs"}));
return OpenLayers.Number.limitSigDigs(this,a)
}
}OpenLayers.Function={bind:function(c,b){var a=Array.prototype.slice.apply(arguments,[2]);
return function(){var d=a.concat(Array.prototype.slice.apply(arguments,[0]));
return c.apply(b,d)
}
},bindAsEventListener:function(b,a){return function(c){return b.call(a,c||window.event)
}
},False:function(){return false
},True:function(){return true
}};
if(!Function.prototype.bind){Function.prototype.bind=function(){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.Function.bind"}));
Array.prototype.unshift.apply(arguments,[this]);
return OpenLayers.Function.bind.apply(null,arguments)
}
}if(!Function.prototype.bindAsEventListener){Function.prototype.bindAsEventListener=function(a){OpenLayers.Console.warn(OpenLayers.i18n("methodDeprecated",{newMethod:"OpenLayers.Function.bindAsEventListener"}));
return OpenLayers.Function.bindAsEventListener(this,a)
}
}OpenLayers.Array={filter:function(g,f,b){var d=[];
if(Array.prototype.filter){d=g.filter(f,b)
}else{var a=g.length;
if(typeof f!="function"){throw new TypeError()
}for(var c=0;
c<a;
c++){if(c in g){var e=g[c];
if(f.call(b,e,c,g)){d.push(e)
}}}}return d
}};OpenLayers.Class=function(){var d=function(){if(arguments&&arguments[0]!=OpenLayers.Class.isPrototype){this.initialize.apply(this,arguments)
}};
var c={};
var f,b;
for(var e=0,a=arguments.length;
e<a;
++e){if(typeof arguments[e]=="function"){if(e==0&&a>1){b=arguments[e].prototype.initialize;
arguments[e].prototype.initialize=function(){};
c=new arguments[e];
if(b===undefined){delete arguments[e].prototype.initialize
}else{arguments[e].prototype.initialize=b
}}f=arguments[e].prototype
}else{f=arguments[e]
}OpenLayers.Util.extend(c,f)
}d.prototype=c;
return d
};
OpenLayers.Class.isPrototype=function(){};
OpenLayers.Class.create=function(){return function(){if(arguments&&arguments[0]!=OpenLayers.Class.isPrototype){this.initialize.apply(this,arguments)
}}
};
OpenLayers.Class.inherit=function(){var d=arguments[0];
var e=new d(OpenLayers.Class.isPrototype);
for(var c=1,a=arguments.length;
c<a;
c++){if(typeof arguments[c]=="function"){var b=arguments[c];
arguments[c]=new b(OpenLayers.Class.isPrototype)
}OpenLayers.Util.extend(e,arguments[c])
}return e
};OpenLayers.Bounds=OpenLayers.Class({left:null,bottom:null,right:null,top:null,centerLonLat:null,initialize:function(d,a,b,c){if(d!=null){this.left=OpenLayers.Util.toFloat(d)
}if(a!=null){this.bottom=OpenLayers.Util.toFloat(a)
}if(b!=null){this.right=OpenLayers.Util.toFloat(b)
}if(c!=null){this.top=OpenLayers.Util.toFloat(c)
}},clone:function(){return new OpenLayers.Bounds(this.left,this.bottom,this.right,this.top)
},equals:function(b){var a=false;
if(b!=null){a=((this.left==b.left)&&(this.right==b.right)&&(this.top==b.top)&&(this.bottom==b.bottom))
}return a
},toString:function(){return("left-bottom=("+this.left+","+this.bottom+") right-top=("+this.right+","+this.top+")")
},toArray:function(a){if(a===true){return[this.bottom,this.left,this.top,this.right]
}else{return[this.left,this.bottom,this.right,this.top]
}},toBBOX:function(b,e){if(b==null){b=6
}var g=Math.pow(10,b);
var f=Math.round(this.left*g)/g;
var d=Math.round(this.bottom*g)/g;
var c=Math.round(this.right*g)/g;
var a=Math.round(this.top*g)/g;
if(e===true){return d+","+f+","+a+","+c
}else{return f+","+d+","+c+","+a
}},toGeometry:function(){return new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing([new OpenLayers.Geometry.Point(this.left,this.bottom),new OpenLayers.Geometry.Point(this.right,this.bottom),new OpenLayers.Geometry.Point(this.right,this.top),new OpenLayers.Geometry.Point(this.left,this.top)])])
},getWidth:function(){return(this.right-this.left)
},getHeight:function(){return(this.top-this.bottom)
},getSize:function(){return new OpenLayers.Size(this.getWidth(),this.getHeight())
},getCenterPixel:function(){return new OpenLayers.Pixel((this.left+this.right)/2,(this.bottom+this.top)/2)
},getCenterLonLat:function(){if(!this.centerLonLat){this.centerLonLat=new OpenLayers.LonLat((this.left+this.right)/2,(this.bottom+this.top)/2)
}return this.centerLonLat
},scale:function(e,c){if(c==null){c=this.getCenterLonLat()
}var a,h;
if(c.CLASS_NAME=="OpenLayers.LonLat"){a=c.lon;
h=c.lat
}else{a=c.x;
h=c.y
}var g=(this.left-a)*e+a;
var b=(this.bottom-h)*e+h;
var d=(this.right-a)*e+a;
var f=(this.top-h)*e+h;
return new OpenLayers.Bounds(g,b,d,f)
},add:function(a,c){if((a==null)||(c==null)){var b=OpenLayers.i18n("boundsAddError");
OpenLayers.Console.error(b);
return null
}return new OpenLayers.Bounds(this.left+a,this.bottom+c,this.right+a,this.top+c)
},extend:function(a){var b=null;
if(a){switch(a.CLASS_NAME){case"OpenLayers.LonLat":b=new OpenLayers.Bounds(a.lon,a.lat,a.lon,a.lat);
break;
case"OpenLayers.Geometry.Point":b=new OpenLayers.Bounds(a.x,a.y,a.x,a.y);
break;
case"OpenLayers.Bounds":b=a;
break
}if(b){this.centerLonLat=null;
if((this.left==null)||(b.left<this.left)){this.left=b.left
}if((this.bottom==null)||(b.bottom<this.bottom)){this.bottom=b.bottom
}if((this.right==null)||(b.right>this.right)){this.right=b.right
}if((this.top==null)||(b.top>this.top)){this.top=b.top
}}}},containsLonLat:function(b,a){return this.contains(b.lon,b.lat,a)
},containsPixel:function(b,a){return this.contains(b.x,b.y,a)
},contains:function(b,d,a){if(a==null){a=true
}if(b==null||d==null){return false
}b=OpenLayers.Util.toFloat(b);
d=OpenLayers.Util.toFloat(d);
var c=false;
if(a){c=((b>=this.left)&&(b<=this.right)&&(d>=this.bottom)&&(d<=this.top))
}else{c=((b>this.left)&&(b<this.right)&&(d>this.bottom)&&(d<this.top))
}return c
},intersectsBounds:function(e,b){if(b==null){b=true
}var d=false;
var h=(this.left==e.right||this.right==e.left||this.top==e.bottom||this.bottom==e.top);
if(b||!h){var g=(((e.bottom>=this.bottom)&&(e.bottom<=this.top))||((this.bottom>=e.bottom)&&(this.bottom<=e.top)));
var f=(((e.top>=this.bottom)&&(e.top<=this.top))||((this.top>e.bottom)&&(this.top<e.top)));
var c=(((e.left>=this.left)&&(e.left<=this.right))||((this.left>=e.left)&&(this.left<=e.right)));
var a=(((e.right>=this.left)&&(e.right<=this.right))||((this.right>=e.left)&&(this.right<=e.right)));
d=((g||f)&&(c||a))
}return d
},containsBounds:function(g,b,a){if(b==null){b=false
}if(a==null){a=true
}var c=this.contains(g.left,g.bottom,a);
var d=this.contains(g.right,g.bottom,a);
var f=this.contains(g.left,g.top,a);
var e=this.contains(g.right,g.top,a);
return(b)?(c||d||f||e):(c&&d&&f&&e)
},determineQuadrant:function(c){var b="";
var a=this.getCenterLonLat();
b+=(c.lat<a.lat)?"b":"t";
b+=(c.lon<a.lon)?"l":"r";
return b
},transform:function(d,b){this.centerLonLat=null;
var e=OpenLayers.Projection.transform({x:this.left,y:this.bottom},d,b);
var a=OpenLayers.Projection.transform({x:this.right,y:this.bottom},d,b);
var c=OpenLayers.Projection.transform({x:this.left,y:this.top},d,b);
var f=OpenLayers.Projection.transform({x:this.right,y:this.top},d,b);
this.left=Math.min(e.x,c.x);
this.bottom=Math.min(e.y,a.y);
this.right=Math.max(a.x,f.x);
this.top=Math.max(c.y,f.y);
return this
},wrapDateLine:function(a,c){c=c||{};
var d=c.leftTolerance||0;
var b=c.rightTolerance||0;
var e=this.clone();
if(a){while(e.left<a.left&&(e.right-b)<=a.left){e=e.add(a.getWidth(),0)
}while((e.left+d)>=a.right&&e.right>a.right){e=e.add(-a.getWidth(),0)
}}return e
},CLASS_NAME:"OpenLayers.Bounds"});
OpenLayers.Bounds.fromString=function(b){var a=b.split(",");
return OpenLayers.Bounds.fromArray(a)
};
OpenLayers.Bounds.fromArray=function(a){return new OpenLayers.Bounds(parseFloat(a[0]),parseFloat(a[1]),parseFloat(a[2]),parseFloat(a[3]))
};
OpenLayers.Bounds.fromSize=function(a){return new OpenLayers.Bounds(0,a.h,a.w,0)
};
OpenLayers.Bounds.oppositeQuadrant=function(a){var b="";
b+=(a.charAt(0)=="t")?"b":"t";
b+=(a.charAt(1)=="l")?"r":"l";
return b
};OpenLayers.Rico=new Object();
OpenLayers.Rico.Corner={round:function(d,b){d=OpenLayers.Util.getElement(d);
this._setOptions(b);
var a=this.options.color;
if(this.options.color=="fromElement"){a=this._background(d)
}var c=this.options.bgColor;
if(this.options.bgColor=="fromParent"){c=this._background(d.offsetParent)
}this._roundCornersImpl(d,a,c)
},changeColor:function(c,b){c.style.backgroundColor=b;
var a=c.parentNode.getElementsByTagName("span");
for(var d=0;
d<a.length;
d++){a[d].style.backgroundColor=b
}},changeOpacity:function(c,f){var d=f;
var a="alpha(opacity="+f*100+")";
c.style.opacity=d;
c.style.filter=a;
var b=c.parentNode.getElementsByTagName("span");
for(var e=0;
e<b.length;
e++){b[e].style.opacity=d;
b[e].style.filter=a
}},reRound:function(d,c){var b=d.parentNode.childNodes[0];
var a=d.parentNode.childNodes[2];
d.parentNode.removeChild(b);
d.parentNode.removeChild(a);
this.round(d.parentNode,c)
},_roundCornersImpl:function(c,a,b){if(this.options.border){this._renderBorder(c,b)
}if(this._isTopRounded()){this._roundTopCorners(c,a,b)
}if(this._isBottomRounded()){this._roundBottomCorners(c,a,b)
}},_renderBorder:function(d,e){var b="1px solid "+this._borderColor(e);
var a="border-left: "+b;
var f="border-right: "+b;
var c="style='"+a+";"+f+"'";
d.innerHTML="<div "+c+">"+d.innerHTML+"</div>"
},_roundTopCorners:function(c,a,e){var d=this._createCorner(e);
for(var b=0;
b<this.options.numSlices;
b++){d.appendChild(this._createCornerSlice(a,e,b,"top"))
}c.style.paddingTop=0;
c.insertBefore(d,c.firstChild)
},_roundBottomCorners:function(c,a,e){var d=this._createCorner(e);
for(var b=(this.options.numSlices-1);
b>=0;
b--){d.appendChild(this._createCornerSlice(a,e,b,"bottom"))
}c.style.paddingBottom=0;
c.appendChild(d)
},_createCorner:function(b){var a=document.createElement("div");
a.style.backgroundColor=(this._isTransparent()?"transparent":b);
return a
},_createCornerSlice:function(c,d,g,a){var e=document.createElement("span");
var b=e.style;
b.backgroundColor=c;
b.display="block";
b.height="1px";
b.overflow="hidden";
b.fontSize="1px";
var f=this._borderColor(c,d);
if(this.options.border&&g==0){b.borderTopStyle="solid";
b.borderTopWidth="1px";
b.borderLeftWidth="0px";
b.borderRightWidth="0px";
b.borderBottomWidth="0px";
b.height="0px";
b.borderColor=f
}else{if(f){b.borderColor=f;
b.borderStyle="solid";
b.borderWidth="0px 1px"
}}if(!this.options.compact&&(g==(this.options.numSlices-1))){b.height="2px"
}this._setMargin(e,g,a);
this._setBorder(e,g,a);
return e
},_setOptions:function(a){this.options={corners:"all",color:"fromElement",bgColor:"fromParent",blend:true,border:false,compact:false};
OpenLayers.Util.extend(this.options,a||{});
this.options.numSlices=this.options.compact?2:4;
if(this._isTransparent()){this.options.blend=false
}},_whichSideTop:function(){if(this._hasString(this.options.corners,"all","top")){return""
}if(this.options.corners.indexOf("tl")>=0&&this.options.corners.indexOf("tr")>=0){return""
}if(this.options.corners.indexOf("tl")>=0){return"left"
}else{if(this.options.corners.indexOf("tr")>=0){return"right"
}}return""
},_whichSideBottom:function(){if(this._hasString(this.options.corners,"all","bottom")){return""
}if(this.options.corners.indexOf("bl")>=0&&this.options.corners.indexOf("br")>=0){return""
}if(this.options.corners.indexOf("bl")>=0){return"left"
}else{if(this.options.corners.indexOf("br")>=0){return"right"
}}return""
},_borderColor:function(a,b){if(a=="transparent"){return b
}else{if(this.options.border){return this.options.border
}else{if(this.options.blend){return this._blend(b,a)
}else{return""
}}}},_setMargin:function(d,e,b){var c=this._marginSize(e);
var a=b=="top"?this._whichSideTop():this._whichSideBottom();
if(a=="left"){d.style.marginLeft=c+"px";
d.style.marginRight="0px"
}else{if(a=="right"){d.style.marginRight=c+"px";
d.style.marginLeft="0px"
}else{d.style.marginLeft=c+"px";
d.style.marginRight=c+"px"
}}},_setBorder:function(d,e,b){var c=this._borderSize(e);
var a=b=="top"?this._whichSideTop():this._whichSideBottom();
if(a=="left"){d.style.borderLeftWidth=c+"px";
d.style.borderRightWidth="0px"
}else{if(a=="right"){d.style.borderRightWidth=c+"px";
d.style.borderLeftWidth="0px"
}else{d.style.borderLeftWidth=c+"px";
d.style.borderRightWidth=c+"px"
}}if(this.options.border!=false){d.style.borderLeftWidth=c+"px";
d.style.borderRightWidth=c+"px"
}},_marginSize:function(e){if(this._isTransparent()){return 0
}var d=[5,3,2,1];
var a=[3,2,1,0];
var c=[2,1];
var b=[1,0];
if(this.options.compact&&this.options.blend){return b[e]
}else{if(this.options.compact){return c[e]
}else{if(this.options.blend){return a[e]
}else{return d[e]
}}}},_borderSize:function(e){var d=[5,3,2,1];
var b=[2,1,1,1];
var a=[1,0];
var c=[0,2,0,0];
if(this.options.compact&&(this.options.blend||this._isTransparent())){return 1
}else{if(this.options.compact){return a[e]
}else{if(this.options.blend){return b[e]
}else{if(this.options.border){return c[e]
}else{if(this._isTransparent()){return d[e]
}}}}}return 0
},_hasString:function(b){for(var a=1;
a<arguments.length;
a++){if(b.indexOf(arguments[a])>=0){return true
}}return false
},_blend:function(c,a){var b=OpenLayers.Rico.Color.createFromHex(c);
b.blend(OpenLayers.Rico.Color.createFromHex(a));
return b
},_background:function(a){try{return OpenLayers.Rico.Color.createColorFromBackground(a).asHex()
}catch(b){return"#ffffff"
}},_isTransparent:function(){return this.options.color=="transparent"
},_isTopRounded:function(){return this._hasString(this.options.corners,"all","top","tl","tr")
},_isBottomRounded:function(){return this._hasString(this.options.corners,"all","bottom","bl","br")
},_hasSingleTextChild:function(a){return a.childNodes.length==1&&a.childNodes[0].nodeType==3
}};OpenLayers.Console={log:function(){},debug:function(){},info:function(){},warn:function(){},error:function(){},userError:function(a){alert(a)
},assert:function(){},dir:function(){},dirxml:function(){},trace:function(){},group:function(){},groupEnd:function(){},time:function(){},timeEnd:function(){},profile:function(){},profileEnd:function(){},count:function(){},CLASS_NAME:"OpenLayers.Console"};
(function(){var b=document.getElementsByTagName("script");
for(var c=0,a=b.length;
c<a;
++c){if(b[c].src.indexOf("firebug.js")!=-1){if(console){OpenLayers.Util.extend(OpenLayers.Console,console);
break
}}}})();OpenLayers.Format=OpenLayers.Class({options:null,externalProjection:null,internalProjection:null,data:null,keepData:false,initialize:function(a){OpenLayers.Util.extend(this,a);
this.options=a
},destroy:function(){},read:function(a){OpenLayers.Console.userError(OpenLayers.i18n("readNotImplemented"))
},write:function(a){OpenLayers.Console.userError(OpenLayers.i18n("writeNotImplemented"))
},CLASS_NAME:"OpenLayers.Format"});OpenLayers.Format.CSWGetRecords=function(b){b=OpenLayers.Util.applyDefaults(b,OpenLayers.Format.CSWGetRecords.DEFAULTS);
var a=OpenLayers.Format.CSWGetRecords["v"+b.version.replace(/\./g,"_")];
if(!a){throw"Unsupported CSWGetRecords version: "+b.version
}return new a(b)
};
OpenLayers.Format.CSWGetRecords.DEFAULTS={version:"2.0.2"};OpenLayers.Event={observers:false,KEY_BACKSPACE:8,KEY_TAB:9,KEY_RETURN:13,KEY_ESC:27,KEY_LEFT:37,KEY_UP:38,KEY_RIGHT:39,KEY_DOWN:40,KEY_DELETE:46,element:function(a){return a.target||a.srcElement
},isLeftClick:function(a){return(((a.which)&&(a.which==1))||((a.button)&&(a.button==1)))
},isRightClick:function(a){return(((a.which)&&(a.which==3))||((a.button)&&(a.button==2)))
},stop:function(b,a){if(!a){if(b.preventDefault){b.preventDefault()
}else{b.returnValue=false
}}if(b.stopPropagation){b.stopPropagation()
}else{b.cancelBubble=true
}},findElement:function(c,b){var a=OpenLayers.Event.element(c);
while(a.parentNode&&(!a.tagName||(a.tagName.toUpperCase()!=b.toUpperCase()))){a=a.parentNode
}return a
},observe:function(b,d,c,a){var e=OpenLayers.Util.getElement(b);
a=a||false;
if(d=="keypress"&&(navigator.appVersion.match(/Konqueror|Safari|KHTML/)||e.attachEvent)){d="keydown"
}if(!this.observers){this.observers={}
}if(!e._eventCacheID){var f="eventCacheID_";
if(e.id){f=e.id+"_"+f
}e._eventCacheID=OpenLayers.Util.createUniqueID(f)
}var g=e._eventCacheID;
if(!this.observers[g]){this.observers[g]=[]
}this.observers[g].push({element:e,name:d,observer:c,useCapture:a});
if(e.addEventListener){e.addEventListener(d,c,a)
}else{if(e.attachEvent){e.attachEvent("on"+d,c)
}}},stopObservingElement:function(a){var b=OpenLayers.Util.getElement(a);
var c=b._eventCacheID;
this._removeElementObservers(OpenLayers.Event.observers[c])
},_removeElementObservers:function(e){if(e){for(var b=e.length-1;
b>=0;
b--){var c=e[b];
var a=new Array(c.element,c.name,c.observer,c.useCapture);
var d=OpenLayers.Event.stopObserving.apply(this,a)
}}},stopObserving:function(h,a,g,b){b=b||false;
var f=OpenLayers.Util.getElement(h);
var d=f._eventCacheID;
if(a=="keypress"){if(navigator.appVersion.match(/Konqueror|Safari|KHTML/)||f.detachEvent){a="keydown"
}}var k=false;
var c=OpenLayers.Event.observers[d];
if(c){var e=0;
while(!k&&e<c.length){var j=c[e];
if((j.name==a)&&(j.observer==g)&&(j.useCapture==b)){c.splice(e,1);
if(c.length==0){delete OpenLayers.Event.observers[d]
}k=true;
break
}e++
}}if(k){if(f.removeEventListener){f.removeEventListener(a,g,b)
}else{if(f&&f.detachEvent){f.detachEvent("on"+a,g)
}}}return k
},unloadCache:function(){if(OpenLayers.Event&&OpenLayers.Event.observers){for(var a in OpenLayers.Event.observers){var b=OpenLayers.Event.observers[a];
OpenLayers.Event._removeElementObservers.apply(this,[b])
}OpenLayers.Event.observers=false
}},CLASS_NAME:"OpenLayers.Event"};
OpenLayers.Event.observe(window,"unload",OpenLayers.Event.unloadCache,false);
if(window.Event){OpenLayers.Util.applyDefaults(window.Event,OpenLayers.Event)
}else{var Event=OpenLayers.Event
}OpenLayers.Events=OpenLayers.Class({BROWSER_EVENTS:["mouseover","mouseout","mousedown","mouseup","mousemove","click","dblclick","rightclick","dblrightclick","resize","focus","blur"],listeners:null,object:null,element:null,eventTypes:null,eventHandler:null,fallThrough:null,includeXY:false,clearMouseListener:null,initialize:function(c,e,g,f,b){OpenLayers.Util.extend(this,b);
this.object=c;
this.fallThrough=f;
this.listeners={};
this.eventHandler=OpenLayers.Function.bindAsEventListener(this.handleBrowserEvent,this);
this.clearMouseListener=OpenLayers.Function.bind(this.clearMouseCache,this);
this.eventTypes=[];
if(g!=null){for(var d=0,a=g.length;
d<a;
d++){this.addEventType(g[d])
}}if(e!=null){this.attachToElement(e)
}},destroy:function(){if(this.element){OpenLayers.Event.stopObservingElement(this.element);
if(this.element.hasScrollEvent){OpenLayers.Event.stopObserving(window,"scroll",this.clearMouseListener)
}}this.element=null;
this.listeners=null;
this.object=null;
this.eventTypes=null;
this.fallThrough=null;
this.eventHandler=null
},addEventType:function(a){if(!this.listeners[a]){this.eventTypes.push(a);
this.listeners[a]=[]
}},attachToElement:function(d){if(this.element){OpenLayers.Event.stopObservingElement(this.element)
}this.element=d;
for(var c=0,a=this.BROWSER_EVENTS.length;
c<a;
c++){var b=this.BROWSER_EVENTS[c];
this.addEventType(b);
OpenLayers.Event.observe(d,b,this.eventHandler)
}OpenLayers.Event.observe(d,"dragstart",OpenLayers.Event.stop)
},on:function(a){for(var b in a){if(b!="scope"){this.register(b,a.scope,a[b])
}}},register:function(b,d,c){if((c!=null)&&(OpenLayers.Util.indexOf(this.eventTypes,b)!=-1)){if(d==null){d=this.object
}var a=this.listeners[b];
a.push({obj:d,func:c})
}},registerPriority:function(b,d,c){if(c!=null){if(d==null){d=this.object
}var a=this.listeners[b];
if(a!=null){a.unshift({obj:d,func:c})
}}},un:function(a){for(var b in a){if(b!="scope"){this.unregister(b,a.scope,a[b])
}}},unregister:function(d,f,e){if(f==null){f=this.object
}var c=this.listeners[d];
if(c!=null){for(var b=0,a=c.length;
b<a;
b++){if(c[b].obj==f&&c[b].func==e){c.splice(b,1);
break
}}}},remove:function(a){if(this.listeners[a]!=null){this.listeners[a]=[]
}},triggerEvent:function(e,b){var d=this.listeners[e];
if(!d||d.length==0){return
}if(b==null){b={}
}b.object=this.object;
b.element=this.element;
if(!b.type){b.type=e
}var d=d.slice(),f;
for(var c=0,a=d.length;
c<a;
c++){var g=d[c];
f=g.func.apply(g.obj,[b]);
if((f!=undefined)&&(f==false)){break
}}if(!this.fallThrough){OpenLayers.Event.stop(b,true)
}return f
},handleBrowserEvent:function(a){if(this.includeXY){a.xy=this.getMousePosition(a)
}this.triggerEvent(a.type,a)
},clearMouseCache:function(){this.element.scrolls=null;
this.element.lefttop=null;
this.element.offsets=null
},getMousePosition:function(a){if(!this.includeXY){this.clearMouseCache()
}else{if(!this.element.hasScrollEvent){OpenLayers.Event.observe(window,"scroll",this.clearMouseListener);
this.element.hasScrollEvent=true
}}if(!this.element.scrolls){this.element.scrolls=[(document.documentElement.scrollLeft||document.body.scrollLeft),(document.documentElement.scrollTop||document.body.scrollTop)]
}if(!this.element.lefttop){this.element.lefttop=[(document.documentElement.clientLeft||0),(document.documentElement.clientTop||0)]
}if(!this.element.offsets){this.element.offsets=OpenLayers.Util.pagePosition(this.element);
this.element.offsets[0]+=this.element.scrolls[0];
this.element.offsets[1]+=this.element.scrolls[1]
}return new OpenLayers.Pixel((a.clientX+this.element.scrolls[0])-this.element.offsets[0]-this.element.lefttop[0],(a.clientY+this.element.scrolls[1])-this.element.offsets[1]-this.element.lefttop[1])
},CLASS_NAME:"OpenLayers.Events"});OpenLayers.Icon=OpenLayers.Class({url:null,size:null,offset:null,calculateOffset:null,imageDiv:null,px:null,initialize:function(a,b,d,c){this.url=a;
this.size=(b)?b:new OpenLayers.Size(20,20);
this.offset=d?d:new OpenLayers.Pixel(-(this.size.w/2),-(this.size.h/2));
this.calculateOffset=c;
var e=OpenLayers.Util.createUniqueID("OL_Icon_");
this.imageDiv=OpenLayers.Util.createAlphaImageDiv(e)
},destroy:function(){this.erase();
OpenLayers.Event.stopObservingElement(this.imageDiv.firstChild);
this.imageDiv.innerHTML="";
this.imageDiv=null
},clone:function(){return new OpenLayers.Icon(this.url,this.size,this.offset,this.calculateOffset)
},setSize:function(a){if(a!=null){this.size=a
}this.draw()
},setUrl:function(a){if(a!=null){this.url=a
}this.draw()
},draw:function(a){OpenLayers.Util.modifyAlphaImageDiv(this.imageDiv,null,null,this.size,this.url,"absolute");
this.moveTo(a);
return this.imageDiv
},erase:function(){if(this.imageDiv!=null&&this.imageDiv.parentNode!=null){OpenLayers.Element.remove(this.imageDiv)
}},setOpacity:function(a){OpenLayers.Util.modifyAlphaImageDiv(this.imageDiv,null,null,null,null,null,null,null,a)
},moveTo:function(a){if(a!=null){this.px=a
}if(this.imageDiv!=null){if(this.px==null){this.display(false)
}else{if(this.calculateOffset){this.offset=this.calculateOffset(this.size)
}var b=this.px.offset(this.offset);
OpenLayers.Util.modifyAlphaImageDiv(this.imageDiv,null,b)
}}},display:function(a){this.imageDiv.style.display=(a)?"":"none"
},isDrawn:function(){var a=(this.imageDiv&&this.imageDiv.parentNode&&(this.imageDiv.parentNode.nodeType!=11));
return a
},CLASS_NAME:"OpenLayers.Icon"});OpenLayers.Marker=OpenLayers.Class({icon:null,lonlat:null,events:null,map:null,initialize:function(c,b){this.lonlat=c;
var a=(b)?b:OpenLayers.Marker.defaultIcon();
if(this.icon==null){this.icon=a
}else{this.icon.url=a.url;
this.icon.size=a.size;
this.icon.offset=a.offset;
this.icon.calculateOffset=a.calculateOffset
}this.events=new OpenLayers.Events(this,this.icon.imageDiv,null)
},destroy:function(){this.erase();
this.map=null;
this.events.destroy();
this.events=null;
if(this.icon!=null){this.icon.destroy();
this.icon=null
}},draw:function(a){return this.icon.draw(a)
},erase:function(){if(this.icon!=null){this.icon.erase()
}},moveTo:function(a){if((a!=null)&&(this.icon!=null)){this.icon.moveTo(a)
}this.lonlat=this.map.getLonLatFromLayerPx(a)
},isDrawn:function(){var a=(this.icon&&this.icon.isDrawn());
return a
},onScreen:function(){var b=false;
if(this.map){var a=this.map.getExtent();
b=a.containsLonLat(this.lonlat)
}return b
},inflate:function(b){if(this.icon){var a=new OpenLayers.Size(this.icon.size.w*b,this.icon.size.h*b);
this.icon.setSize(a)
}},setOpacity:function(a){this.icon.setOpacity(a)
},setUrl:function(a){this.icon.setUrl(a)
},display:function(a){this.icon.display(a)
},CLASS_NAME:"OpenLayers.Marker"});
OpenLayers.Marker.defaultIcon=function(){var a=OpenLayers.Util.getImagesLocation()+"marker.png";
var b=new OpenLayers.Size(21,25);
var c=function(d){return new OpenLayers.Pixel(-(d.w/2),-d.h)
};
return new OpenLayers.Icon(a,b,null,c)
};OpenLayers.Popup=OpenLayers.Class({events:null,id:"",lonlat:null,div:null,contentSize:null,size:null,contentHTML:null,backgroundColor:"",opacity:"",border:"",contentDiv:null,groupDiv:null,closeDiv:null,autoSize:false,minSize:null,maxSize:null,displayClass:"olPopup",contentDisplayClass:"olPopupContent",padding:0,disableFirefoxOverflowHack:false,fixPadding:function(){if(typeof this.padding=="number"){this.padding=new OpenLayers.Bounds(this.padding,this.padding,this.padding,this.padding)
}},panMapIfOutOfView:false,keepInMap:false,closeOnMove:false,map:null,initialize:function(g,c,f,b,e,d){if(g==null){g=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_")
}this.id=g;
this.lonlat=c;
this.contentSize=(f!=null)?f:new OpenLayers.Size(OpenLayers.Popup.WIDTH,OpenLayers.Popup.HEIGHT);
if(b!=null){this.contentHTML=b
}this.backgroundColor=OpenLayers.Popup.COLOR;
this.opacity=OpenLayers.Popup.OPACITY;
this.border=OpenLayers.Popup.BORDER;
this.div=OpenLayers.Util.createDiv(this.id,null,null,null,null,null,"hidden");
this.div.className=this.displayClass;
var a=this.id+"_GroupDiv";
this.groupDiv=OpenLayers.Util.createDiv(a,null,null,null,"relative",null,"hidden");
var g=this.div.id+"_contentDiv";
this.contentDiv=OpenLayers.Util.createDiv(g,null,this.contentSize.clone(),null,"relative");
this.contentDiv.className=this.contentDisplayClass;
this.groupDiv.appendChild(this.contentDiv);
this.div.appendChild(this.groupDiv);
if(e){this.addCloseBox(d)
}this.registerEvents()
},destroy:function(){this.id=null;
this.lonlat=null;
this.size=null;
this.contentHTML=null;
this.backgroundColor=null;
this.opacity=null;
this.border=null;
if(this.closeOnMove&&this.map){this.map.events.unregister("movestart",this,this.hide)
}this.events.destroy();
this.events=null;
if(this.closeDiv){OpenLayers.Event.stopObservingElement(this.closeDiv);
this.groupDiv.removeChild(this.closeDiv)
}this.closeDiv=null;
this.div.removeChild(this.groupDiv);
this.groupDiv=null;
if(this.map!=null){this.map.removePopup(this)
}this.map=null;
this.div=null;
this.autoSize=null;
this.minSize=null;
this.maxSize=null;
this.padding=null;
this.panMapIfOutOfView=null
},draw:function(a){if(a==null){if((this.lonlat!=null)&&(this.map!=null)){a=this.map.getLayerPxFromLonLat(this.lonlat)
}}if(this.closeOnMove){this.map.events.register("movestart",this,this.hide)
}if(!this.disableFirefoxOverflowHack&&OpenLayers.Util.getBrowserName()=="firefox"){this.map.events.register("movestart",this,function(){var b=document.defaultView.getComputedStyle(this.contentDiv,null);
var c=b.getPropertyValue("overflow");
if(c!="hidden"){this.contentDiv._oldOverflow=c;
this.contentDiv.style.overflow="hidden"
}});
this.map.events.register("moveend",this,function(){var b=this.contentDiv._oldOverflow;
if(b){this.contentDiv.style.overflow=b;
this.contentDiv._oldOverflow=null
}})
}this.moveTo(a);
if(!this.autoSize&&!this.size){this.setSize(this.contentSize)
}this.setBackgroundColor();
this.setOpacity();
this.setBorder();
this.setContentHTML();
if(this.panMapIfOutOfView){this.panIntoView()
}return this.div
},updatePosition:function(){if((this.lonlat)&&(this.map)){var a=this.map.getLayerPxFromLonLat(this.lonlat);
if(a){this.moveTo(a)
}}},moveTo:function(a){if((a!=null)&&(this.div!=null)){this.div.style.left=a.x+"px";
this.div.style.top=a.y+"px"
}},visible:function(){return OpenLayers.Element.visible(this.div)
},toggle:function(){if(this.visible()){this.hide()
}else{this.show()
}},show:function(){OpenLayers.Element.show(this.div);
if(this.panMapIfOutOfView){this.panIntoView()
}},hide:function(){OpenLayers.Element.hide(this.div)
},setSize:function(c){this.size=c.clone();
var b=this.getContentDivPadding();
var a=b.left+b.right;
var e=b.top+b.bottom;
this.fixPadding();
a+=this.padding.left+this.padding.right;
e+=this.padding.top+this.padding.bottom;
if(this.closeDiv){var d=parseInt(this.closeDiv.style.width);
a+=d+b.right
}this.size.w+=a;
this.size.h+=e;
if(OpenLayers.Util.getBrowserName()=="msie"){this.contentSize.w+=b.left+b.right;
this.contentSize.h+=b.bottom+b.top
}if(this.div!=null){this.div.style.width=this.size.w+"px";
this.div.style.height=this.size.h+"px"
}if(this.contentDiv!=null){this.contentDiv.style.width=c.w+"px";
this.contentDiv.style.height=c.h+"px"
}},updateSize:function(){var e="<div class='"+this.contentDisplayClass+"'>"+this.contentDiv.innerHTML+"</div>";
var h=(this.map)?this.map.layerContainerDiv:document.body;
var i=OpenLayers.Util.getRenderedDimensions(e,null,{displayClass:this.displayClass,containerElement:h});
var g=this.getSafeContentSize(i);
var f=null;
if(g.equals(i)){f=i
}else{var b=new OpenLayers.Size();
b.w=(g.w<i.w)?g.w:null;
b.h=(g.h<i.h)?g.h:null;
if(b.w&&b.h){f=g
}else{var d=OpenLayers.Util.getRenderedDimensions(e,b,{displayClass:this.contentDisplayClass,containerElement:h});
var c=OpenLayers.Element.getStyle(this.contentDiv,"overflow");
if((c!="hidden")&&(d.equals(g))){var a=OpenLayers.Util.getScrollbarWidth();
if(b.w){d.h+=a
}else{d.w+=a
}}f=this.getSafeContentSize(d)
}}this.setSize(f)
},setBackgroundColor:function(a){if(a!=undefined){this.backgroundColor=a
}if(this.div!=null){this.div.style.backgroundColor=this.backgroundColor
}},setOpacity:function(a){if(a!=undefined){this.opacity=a
}if(this.div!=null){this.div.style.opacity=this.opacity;
this.div.style.filter="alpha(opacity="+this.opacity*100+")"
}},setBorder:function(a){if(a!=undefined){this.border=a
}if(this.div!=null){this.div.style.border=this.border
}},setContentHTML:function(a){if(a!=null){this.contentHTML=a
}if((this.contentDiv!=null)&&(this.contentHTML!=null)&&(this.contentHTML!=this.contentDiv.innerHTML)){this.contentDiv.innerHTML=this.contentHTML;
if(this.autoSize){this.registerImageListeners();
this.updateSize()
}}},registerImageListeners:function(){var f=function(){this.popup.updateSize();
if(this.popup.visible()&&this.popup.panMapIfOutOfView){this.popup.panIntoView()
}OpenLayers.Event.stopObserving(this.img,"load",this.img._onImageLoad)
};
var b=this.contentDiv.getElementsByTagName("img");
for(var e=0,a=b.length;
e<a;
e++){var c=b[e];
if(c.width==0||c.height==0){var d={popup:this,img:c};
c._onImgLoad=OpenLayers.Function.bind(f,d);
OpenLayers.Event.observe(c,"load",c._onImgLoad)
}}},getSafeContentSize:function(k){var d=k.clone();
var i=this.getContentDivPadding();
var j=i.left+i.right;
var g=i.top+i.bottom;
this.fixPadding();
j+=this.padding.left+this.padding.right;
g+=this.padding.top+this.padding.bottom;
if(this.closeDiv){var c=parseInt(this.closeDiv.style.width);
j+=c+i.right
}if(this.minSize){d.w=Math.max(d.w,(this.minSize.w-j));
d.h=Math.max(d.h,(this.minSize.h-g))
}if(this.maxSize){d.w=Math.min(d.w,(this.maxSize.w-j));
d.h=Math.min(d.h,(this.maxSize.h-g))
}if(this.map&&this.map.size){var f=0,e=0;
if(this.keepInMap&&!this.panMapIfOutOfView){var h=this.map.getPixelFromLonLat(this.lonlat);
switch(this.relativePosition){case"tr":f=h.x;
e=this.map.size.h-h.y;
break;
case"tl":f=this.map.size.w-h.x;
e=this.map.size.h-h.y;
break;
case"bl":f=this.map.size.w-h.x;
e=h.y;
break;
case"br":f=h.x;
e=h.y;
break;
default:f=h.x;
e=this.map.size.h-h.y;
break
}}var a=this.map.size.h-this.map.paddingForPopups.top-this.map.paddingForPopups.bottom-g-e;
var b=this.map.size.w-this.map.paddingForPopups.left-this.map.paddingForPopups.right-j-f;
d.w=Math.min(d.w,b);
d.h=Math.min(d.h,a)
}return d
},getContentDivPadding:function(){var a=this._contentDivPadding;
if(!a){if(this.div.parentNode==null){this.div.style.display="none";
document.body.appendChild(this.div)
}a=new OpenLayers.Bounds(OpenLayers.Element.getStyle(this.contentDiv,"padding-left"),OpenLayers.Element.getStyle(this.contentDiv,"padding-bottom"),OpenLayers.Element.getStyle(this.contentDiv,"padding-right"),OpenLayers.Element.getStyle(this.contentDiv,"padding-top"));
this._contentDivPadding=a;
if(this.div.parentNode==document.body){document.body.removeChild(this.div);
this.div.style.display=""
}}return a
},addCloseBox:function(c){this.closeDiv=OpenLayers.Util.createDiv(this.id+"_close",null,new OpenLayers.Size(17,17));
this.closeDiv.className="olPopupCloseBox";
var b=this.getContentDivPadding();
this.closeDiv.style.right=b.right+"px";
this.closeDiv.style.top=b.top+"px";
this.groupDiv.appendChild(this.closeDiv);
var a=c||function(d){this.hide();
OpenLayers.Event.stop(d)
};
OpenLayers.Event.observe(this.closeDiv,"click",OpenLayers.Function.bindAsEventListener(a,this))
},panIntoView:function(){var e=this.map.getSize();
var d=this.map.getViewPortPxFromLayerPx(new OpenLayers.Pixel(parseInt(this.div.style.left),parseInt(this.div.style.top)));
var c=d.clone();
if(d.x<this.map.paddingForPopups.left){c.x=this.map.paddingForPopups.left
}else{if((d.x+this.size.w)>(e.w-this.map.paddingForPopups.right)){c.x=e.w-this.map.paddingForPopups.right-this.size.w
}}if(d.y<this.map.paddingForPopups.top){c.y=this.map.paddingForPopups.top
}else{if((d.y+this.size.h)>(e.h-this.map.paddingForPopups.bottom)){c.y=e.h-this.map.paddingForPopups.bottom-this.size.h
}}var b=d.x-c.x;
var a=d.y-c.y;
this.map.pan(b,a)
},registerEvents:function(){this.events=new OpenLayers.Events(this,this.div,null,true);
this.events.on({mousedown:this.onmousedown,mousemove:this.onmousemove,mouseup:this.onmouseup,click:this.onclick,mouseout:this.onmouseout,dblclick:this.ondblclick,scope:this})
},onmousedown:function(a){this.mousedown=true;
OpenLayers.Event.stop(a,true)
},onmousemove:function(a){if(this.mousedown){OpenLayers.Event.stop(a,true)
}},onmouseup:function(a){if(this.mousedown){this.mousedown=false;
OpenLayers.Event.stop(a,true)
}},onclick:function(a){OpenLayers.Event.stop(a,true)
},onmouseout:function(a){this.mousedown=false
},ondblclick:function(a){OpenLayers.Event.stop(a,true)
},CLASS_NAME:"OpenLayers.Popup"});
OpenLayers.Popup.WIDTH=200;
OpenLayers.Popup.HEIGHT=200;
OpenLayers.Popup.COLOR="white";
OpenLayers.Popup.OPACITY=1;
OpenLayers.Popup.BORDER="0px";OpenLayers.Popup.Anchored=OpenLayers.Class(OpenLayers.Popup,{relativePosition:null,keepInMap:true,anchor:null,initialize:function(h,d,g,c,b,f,e){var a=[h,d,g,c,f,e];
OpenLayers.Popup.prototype.initialize.apply(this,a);
this.anchor=(b!=null)?b:{size:new OpenLayers.Size(0,0),offset:new OpenLayers.Pixel(0,0)}
},destroy:function(){this.anchor=null;
this.relativePosition=null;
OpenLayers.Popup.prototype.destroy.apply(this,arguments)
},show:function(){this.updatePosition();
OpenLayers.Popup.prototype.show.apply(this,arguments)
},moveTo:function(c){var b=this.relativePosition;
this.relativePosition=this.calculateRelativePosition(c);
var d=this.calculateNewPx(c);
var a=new Array(d);
OpenLayers.Popup.prototype.moveTo.apply(this,a);
if(this.relativePosition!=b){this.updateRelativePosition()
}},setSize:function(b){OpenLayers.Popup.prototype.setSize.apply(this,arguments);
if((this.lonlat)&&(this.map)){var a=this.map.getLayerPxFromLonLat(this.lonlat);
this.moveTo(a)
}},calculateRelativePosition:function(b){var d=this.map.getLonLatFromLayerPx(b);
var c=this.map.getExtent();
var a=c.determineQuadrant(d);
return OpenLayers.Bounds.oppositeQuadrant(a)
},updateRelativePosition:function(){},calculateNewPx:function(b){var e=b.offset(this.anchor.offset);
var a=this.size||this.contentSize;
var d=(this.relativePosition.charAt(0)=="t");
e.y+=(d)?-a.h:this.anchor.size.h;
var c=(this.relativePosition.charAt(1)=="l");
e.x+=(c)?-a.w:this.anchor.size.w;
return e
},CLASS_NAME:"OpenLayers.Popup.Anchored"});OpenLayers.Popup.AnchoredBubble=OpenLayers.Class(OpenLayers.Popup.Anchored,{rounded:false,initialize:function(g,c,f,b,a,e,d){this.padding=new OpenLayers.Bounds(0,OpenLayers.Popup.AnchoredBubble.CORNER_SIZE,0,OpenLayers.Popup.AnchoredBubble.CORNER_SIZE);
OpenLayers.Popup.Anchored.prototype.initialize.apply(this,arguments)
},draw:function(a){OpenLayers.Popup.Anchored.prototype.draw.apply(this,arguments);
this.setContentHTML();
this.setBackgroundColor();
this.setOpacity();
return this.div
},updateRelativePosition:function(){this.setRicoCorners()
},setSize:function(a){OpenLayers.Popup.Anchored.prototype.setSize.apply(this,arguments);
this.setRicoCorners()
},setBackgroundColor:function(a){if(a!=undefined){this.backgroundColor=a
}if(this.div!=null){if(this.contentDiv!=null){this.div.style.background="transparent";
OpenLayers.Rico.Corner.changeColor(this.groupDiv,this.backgroundColor)
}}},setOpacity:function(a){OpenLayers.Popup.Anchored.prototype.setOpacity.call(this,a);
if(this.div!=null){if(this.groupDiv!=null){OpenLayers.Rico.Corner.changeOpacity(this.groupDiv,this.opacity)
}}},setBorder:function(a){this.border=0
},setRicoCorners:function(){var a=this.getCornersToRound(this.relativePosition);
var b={corners:a,color:this.backgroundColor,bgColor:"transparent",blend:false};
if(!this.rounded){OpenLayers.Rico.Corner.round(this.div,b);
this.rounded=true
}else{OpenLayers.Rico.Corner.reRound(this.groupDiv,b);
this.setBackgroundColor();
this.setOpacity()
}},getCornersToRound:function(){var a=["tl","tr","bl","br"];
var b=OpenLayers.Bounds.oppositeQuadrant(this.relativePosition);
OpenLayers.Util.removeItem(a,b);
return a.join(" ")
},CLASS_NAME:"OpenLayers.Popup.AnchoredBubble"});
OpenLayers.Popup.AnchoredBubble.CORNER_SIZE=5;OpenLayers.Feature=OpenLayers.Class({layer:null,id:null,lonlat:null,data:null,marker:null,popupClass:OpenLayers.Popup.AnchoredBubble,popup:null,initialize:function(a,c,b){this.layer=a;
this.lonlat=c;
this.data=(b!=null)?b:{};
this.id=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_")
},destroy:function(){if((this.layer!=null)&&(this.layer.map!=null)){if(this.popup!=null){this.layer.map.removePopup(this.popup)
}}if(this.layer!=null&&this.marker!=null){this.layer.removeMarker(this.marker)
}this.layer=null;
this.id=null;
this.lonlat=null;
this.data=null;
if(this.marker!=null){this.destroyMarker(this.marker);
this.marker=null
}if(this.popup!=null){this.destroyPopup(this.popup);
this.popup=null
}},onScreen:function(){var b=false;
if((this.layer!=null)&&(this.layer.map!=null)){var a=this.layer.map.getExtent();
b=a.containsLonLat(this.lonlat)
}return b
},createMarker:function(){if(this.lonlat!=null){this.marker=new OpenLayers.Marker(this.lonlat,this.data.icon)
}return this.marker
},destroyMarker:function(){this.marker.destroy()
},createPopup:function(b){if(this.lonlat!=null){var c=this.id+"_popup";
var a=(this.marker)?this.marker.icon:null;
if(!this.popup){this.popup=new this.popupClass(c,this.lonlat,this.data.popupSize,this.data.popupContentHTML,a,b)
}if(this.data.overflow!=null){this.popup.contentDiv.style.overflow=this.data.overflow
}this.popup.feature=this
}return this.popup
},destroyPopup:function(){if(this.popup){this.popup.feature=null;
this.popup.destroy();
this.popup=null
}},CLASS_NAME:"OpenLayers.Feature"});OpenLayers.State={UNKNOWN:"Unknown",INSERT:"Insert",UPDATE:"Update",DELETE:"Delete"};
OpenLayers.Feature.Vector=OpenLayers.Class(OpenLayers.Feature,{fid:null,geometry:null,attributes:null,bounds:null,state:null,style:null,url:null,renderIntent:"default",initialize:function(c,a,b){OpenLayers.Feature.prototype.initialize.apply(this,[null,null,a]);
this.lonlat=null;
this.geometry=c?c:null;
this.state=null;
this.attributes={};
if(a){this.attributes=OpenLayers.Util.extend(this.attributes,a)
}this.style=b?b:null
},destroy:function(){if(this.layer){this.layer.removeFeatures(this);
this.layer=null
}this.geometry=null;
OpenLayers.Feature.prototype.destroy.apply(this,arguments)
},clone:function(){return new OpenLayers.Feature.Vector(this.geometry?this.geometry.clone():null,this.attributes,this.style)
},onScreen:function(d){var c=false;
if(this.layer&&this.layer.map){var a=this.layer.map.getExtent();
if(d){var b=this.geometry.getBounds();
c=a.intersectsBounds(b)
}else{var e=a.toGeometry();
c=e.intersects(this.geometry)
}}return c
},getVisibility:function(){return !(this.style&&this.style.display=="none"||!this.layer||this.layer&&this.layer.styleMap&&this.layer.styleMap.createSymbolizer(this,this.renderIntent).display=="none"||this.layer&&!this.layer.getVisibility())
},createMarker:function(){return null
},destroyMarker:function(){},createPopup:function(){return null
},atPoint:function(b,d,c){var a=false;
if(this.geometry){a=this.geometry.atPoint(b,d,c)
}return a
},destroyPopup:function(){},move:function(a){if(!this.layer||!this.geometry.move){return
}var b;
if(a.CLASS_NAME=="OpenLayers.LonLat"){b=this.layer.getViewPortPxFromLonLat(a)
}else{b=a
}var d=this.layer.getViewPortPxFromLonLat(this.geometry.getBounds().getCenterLonLat());
var c=this.layer.map.getResolution();
this.geometry.move(c*(b.x-d.x),c*(d.y-b.y));
this.layer.drawFeature(this);
return d
},toState:function(a){if(a==OpenLayers.State.UPDATE){switch(this.state){case OpenLayers.State.UNKNOWN:case OpenLayers.State.DELETE:this.state=a;
break;
case OpenLayers.State.UPDATE:case OpenLayers.State.INSERT:break
}}else{if(a==OpenLayers.State.INSERT){switch(this.state){case OpenLayers.State.UNKNOWN:break;
default:this.state=a;
break
}}else{if(a==OpenLayers.State.DELETE){switch(this.state){case OpenLayers.State.INSERT:break;
case OpenLayers.State.DELETE:break;
case OpenLayers.State.UNKNOWN:case OpenLayers.State.UPDATE:this.state=a;
break
}}else{if(a==OpenLayers.State.UNKNOWN){this.state=a
}}}}},CLASS_NAME:"OpenLayers.Feature.Vector"});
OpenLayers.Feature.Vector.style={"default":{fillColor:"#ee9900",fillOpacity:0.4,hoverFillColor:"white",hoverFillOpacity:0.8,strokeColor:"#ee9900",strokeOpacity:1,strokeWidth:1,strokeLinecap:"round",strokeDashstyle:"solid",hoverStrokeColor:"red",hoverStrokeOpacity:1,hoverStrokeWidth:0.2,pointRadius:6,hoverPointRadius:1,hoverPointUnit:"%",pointerEvents:"visiblePainted",cursor:"inherit"},select:{fillColor:"blue",fillOpacity:0.4,hoverFillColor:"white",hoverFillOpacity:0.8,strokeColor:"blue",strokeOpacity:1,strokeWidth:2,strokeLinecap:"round",strokeDashstyle:"solid",hoverStrokeColor:"red",hoverStrokeOpacity:1,hoverStrokeWidth:0.2,pointRadius:6,hoverPointRadius:1,hoverPointUnit:"%",pointerEvents:"visiblePainted",cursor:"pointer"},temporary:{fillColor:"#66cccc",fillOpacity:0.2,hoverFillColor:"white",hoverFillOpacity:0.8,strokeColor:"#66cccc",strokeOpacity:1,strokeLinecap:"round",strokeWidth:2,strokeDashstyle:"solid",hoverStrokeColor:"red",hoverStrokeOpacity:1,hoverStrokeWidth:0.2,pointRadius:6,hoverPointRadius:1,hoverPointUnit:"%",pointerEvents:"visiblePainted",cursor:"inherit"},"delete":{display:"none"}};OpenLayers.Format.XML=OpenLayers.Class(OpenLayers.Format,{namespaces:null,namespaceAlias:null,defaultPrefix:null,readers:{},writers:{},xmldom:null,initialize:function(a){if(window.ActiveXObject){this.xmldom=new ActiveXObject("Microsoft.XMLDOM")
}OpenLayers.Format.prototype.initialize.apply(this,[a]);
this.namespaces=OpenLayers.Util.extend({},this.namespaces);
this.namespaceAlias={};
for(var b in this.namespaces){this.namespaceAlias[this.namespaces[b]]=b
}},destroy:function(){this.xmldom=null;
OpenLayers.Format.prototype.destroy.apply(this,arguments)
},setNamespace:function(a,b){this.namespaces[a]=b;
this.namespaceAlias[b]=a
},read:function(c){var a=c.indexOf("<");
if(a>0){c=c.substring(a)
}var b=OpenLayers.Util.Try(OpenLayers.Function.bind((function(){var d;
if(window.ActiveXObject&&!this.xmldom){d=new ActiveXObject("Microsoft.XMLDOM")
}else{d=this.xmldom
}d.loadXML(c);
return d
}),this),function(){return new DOMParser().parseFromString(c,"text/xml")
},function(){var d=new XMLHttpRequest();
d.open("GET","data:text/xml;charset=utf-8,"+encodeURIComponent(c),false);
if(d.overrideMimeType){d.overrideMimeType("text/xml")
}d.send(null);
return d.responseXML
});
if(this.keepData){this.data=b
}return b
},write:function(b){var c;
if(this.xmldom){c=b.xml
}else{var a=new XMLSerializer();
if(b.nodeType==1){var d=document.implementation.createDocument("","",null);
if(d.importNode){b=d.importNode(b,true)
}d.appendChild(b);
c=a.serializeToString(d)
}else{c=a.serializeToString(b)
}}return c
},createElementNS:function(c,a){var b;
if(this.xmldom){if(typeof c=="string"){b=this.xmldom.createNode(1,a,c)
}else{b=this.xmldom.createNode(1,a,"")
}}else{b=document.createElementNS(c,a)
}return b
},createTextNode:function(b){var a;
if(this.xmldom){a=this.xmldom.createTextNode(b)
}else{a=document.createTextNode(b)
}return a
},getElementsByTagNameNS:function(e,d,c){var a=[];
if(e.getElementsByTagNameNS){a=e.getElementsByTagNameNS(d,c)
}else{var b=e.getElementsByTagName("*");
var j,f;
for(var g=0,h=b.length;
g<h;
++g){j=b[g];
f=(j.prefix)?(j.prefix+":"+c):c;
if((c=="*")||(f==j.nodeName)){if((d=="*")||(d==j.namespaceURI)){a.push(j)
}}}}return a
},getAttributeNodeNS:function(c,b,a){var j=null;
if(c.getAttributeNodeNS){j=c.getAttributeNodeNS(b,a)
}else{var e=c.attributes;
var h,d;
for(var f=0,g=e.length;
f<g;
++f){h=e[f];
if(h.namespaceURI==b){d=(h.prefix)?(h.prefix+":"+a):a;
if(d==h.nodeName){j=h;
break
}}}}return j
},getAttributeNS:function(e,d,a){var b="";
if(e.getAttributeNS){b=e.getAttributeNS(d,a)||""
}else{var c=this.getAttributeNodeNS(e,d,a);
if(c){b=c.nodeValue
}}return b
},getChildValue:function(a,c){var b=c||"";
if(a){for(var d=a.firstChild;
d;
d=d.nextSibling){switch(d.nodeType){case 3:case 4:b+=d.nodeValue
}}}return b
},concatChildValues:function(b,d){var c="";
var e=b.firstChild;
var a;
while(e){a=e.nodeValue;
if(a){c+=a
}e=e.nextSibling
}if(c==""&&d!=undefined){c=d
}return c
},isSimpleContent:function(a){var c=true;
for(var b=a.firstChild;
b;
b=b.nextSibling){if(b.nodeType===1){c=false;
break
}}return c
},contentType:function(c){var e=false,b=false;
var a=OpenLayers.Format.XML.CONTENT_TYPE.EMPTY;
for(var d=c.firstChild;
d;
d=d.nextSibling){switch(d.nodeType){case 1:b=true;
break;
case 8:break;
default:e=true
}if(b&&e){break
}}if(b&&e){a=OpenLayers.Format.XML.CONTENT_TYPE.MIXED
}else{if(b){return OpenLayers.Format.XML.CONTENT_TYPE.COMPLEX
}else{if(e){return OpenLayers.Format.XML.CONTENT_TYPE.SIMPLE
}}}return a
},hasAttributeNS:function(c,b,a){var d=false;
if(c.hasAttributeNS){d=c.hasAttributeNS(b,a)
}else{d=!!this.getAttributeNodeNS(c,b,a)
}return d
},setAttributeNS:function(d,c,a,e){if(d.setAttributeNS){d.setAttributeNS(c,a,e)
}else{if(this.xmldom){if(c){var b=d.ownerDocument.createNode(2,a,c);
b.nodeValue=e;
d.setAttributeNode(b)
}else{d.setAttribute(a,e)
}}else{throw"setAttributeNS not implemented"
}}},createElementNSPlus:function(b,a){a=a||{};
var d=a.uri||this.namespaces[a.prefix];
if(!d){var f=b.indexOf(":");
d=this.namespaces[b.substring(0,f)]
}if(!d){d=this.namespaces[this.defaultPrefix]
}var c=this.createElementNS(d,b);
if(a.attributes){this.setAttributes(c,a.attributes)
}var e=a.value;
if(e!=null){if(typeof e=="boolean"){e=String(e)
}c.appendChild(this.createTextNode(e))
}return c
},setAttributes:function(c,e){var d,b;
for(var a in e){if(e[a]!=null&&e[a].toString){d=e[a].toString();
b=this.namespaces[a.substring(0,a.indexOf(":"))]||null;
this.setAttributeNS(c,b,a,d)
}}},readNode:function(c,e){if(!e){e={}
}var d=this.readers[c.namespaceURI?this.namespaceAlias[c.namespaceURI]:this.defaultPrefix];
if(d){var b=c.localName||c.nodeName.split(":").pop();
var a=d[b]||d["*"];
if(a){a.apply(this,[c,e])
}}return e
},readChildNodes:function(d,e){if(!e){e={}
}var c=d.childNodes;
var f;
for(var b=0,a=c.length;
b<a;
++b){f=c[b];
if(f.nodeType==1){this.readNode(f,e)
}}return e
},writeNode:function(a,f,d){var e,c;
var b=a.indexOf(":");
if(b>0){e=a.substring(0,b);
c=a.substring(b+1)
}else{if(d){e=this.namespaceAlias[d.namespaceURI]
}else{e=this.defaultPrefix
}c=a
}var g=this.writers[e][c].apply(this,[f]);
if(d){d.appendChild(g)
}return g
},getChildEl:function(c,a,b){return c&&this.getThisOrNextEl(c.firstChild,a,b)
},getNextEl:function(c,a,b){return c&&this.getThisOrNextEl(c.nextSibling,a,b)
},getThisOrNextEl:function(d,a,c){outer:for(var b=d;
b;
b=b.nextSibling){switch(b.nodeType){case 1:if((!a||a===(b.localName||b.nodeName.split(":").pop()))&&(!c||c===b.namespaceURI)){break outer
}b=null;
break outer;
case 3:if(/^\s*$/.test(b.nodeValue)){break
}case 4:case 6:case 12:case 10:case 11:b=null;
break outer
}}return b||null
},lookupNamespaceURI:function(e,f){var d=null;
if(e){if(e.lookupNamespaceURI){d=e.lookupNamespaceURI(f)
}else{outer:switch(e.nodeType){case 1:if(e.namespaceURI!==null&&e.prefix===f){d=e.namespaceURI;
break outer
}var b=e.attributes.length;
if(b){var a;
for(var c=0;
c<b;
++c){a=e.attributes[c];
if(a.prefix==="xmlns"&&a.name==="xmlns:"+f){d=a.value||null;
break outer
}else{if(a.name==="xmlns"&&f===null){d=a.value||null;
break outer
}}}}d=this.lookupNamespaceURI(e.parentNode,f);
break outer;
case 2:d=this.lookupNamespaceURI(e.ownerElement,f);
break outer;
case 9:d=this.lookupNamespaceURI(e.documentElement,f);
break outer;
case 6:case 12:case 10:case 11:break outer;
default:d=this.lookupNamespaceURI(e.parentNode,f);
break outer
}}}return d
},CLASS_NAME:"OpenLayers.Format.XML"});
OpenLayers.Format.XML.CONTENT_TYPE={EMPTY:0,SIMPLE:1,COMPLEX:2,MIXED:3};
OpenLayers.Format.XML.lookupNamespaceURI=OpenLayers.Function.bind(OpenLayers.Format.XML.prototype.lookupNamespaceURI,OpenLayers.Format.XML.prototype);OpenLayers.Format.WMC=OpenLayers.Class({defaultVersion:"1.1.0",version:null,layerOptions:null,layerParams:null,parser:null,initialize:function(a){OpenLayers.Util.extend(this,a);
this.options=a
},read:function(f,d){if(typeof f=="string"){f=OpenLayers.Format.XML.prototype.read.apply(this,[f])
}var b=f.documentElement;
var a=this.version;
if(!a){a=b.getAttribute("version")
}var h=this.getParser(a);
var e=h.read(f,d);
var g;
if(d&&d.map){this.context=e;
if(d.map instanceof OpenLayers.Map){g=this.mergeContextToMap(e,d.map)
}else{var c=d.map;
if(OpenLayers.Util.isElement(c)||typeof c=="string"){c={div:c}
}g=this.contextToMap(e,c)
}}else{g=e
}return g
},getParser:function(a){var b=a||this.version||this.defaultVersion;
if(!this.parser||this.parser.VERSION!=b){var c=OpenLayers.Format.WMC["v"+b.replace(/\./g,"_")];
if(!c){throw"Can't find a WMC parser for version "+b
}this.parser=new c(this.options)
}return this.parser
},getLayerFromContext:function(f){var d,a;
var b={queryable:f.queryable,visibility:f.visibility,maxExtent:f.maxExtent,numZoomLevels:f.numZoomLevels,units:f.units,isBaseLayer:f.isBaseLayer,opacity:f.opacity,displayInLayerSwitcher:f.displayInLayerSwitcher,singleTile:f.singleTile,minScale:f.minScale,maxScale:f.maxScale};
if(this.layerOptions){OpenLayers.Util.applyDefaults(b,this.layerOptions)
}var h={layers:f.name,transparent:f.transparent,version:f.version};
if(f.formats&&f.formats.length>0){h.format=f.formats[0].value;
for(d=0,a=f.formats.length;
d<a;
d++){var g=f.formats[d];
if(g.current==true){h.format=g.value;
break
}}}if(f.styles&&f.styles.length>0){for(d=0,a=f.styles.length;
d<a;
d++){var e=f.styles[d];
if(e.current==true){if(e.href){h.sld=e.href
}else{if(e.body){h.sld_body=e.body
}else{h.styles=e.name
}}break
}}}if(this.layerParams){OpenLayers.Util.applyDefaults(h,this.layerParams)
}var c=new OpenLayers.Layer.WMS(f.title||f.name,f.url,h,b);
return c
},getLayersFromContext:function(c){var d=[];
for(var b=0,a=c.length;
b<a;
b++){d.push(this.getLayerFromContext(c[b]))
}return d
},contextToMap:function(b,a){a=OpenLayers.Util.applyDefaults({maxExtent:b.maxExtent,projection:b.projection},a);
var c=new OpenLayers.Map(a);
c.addLayers(this.getLayersFromContext(b.layersContext));
c.setCenter(b.bounds.getCenterLonLat(),c.getZoomForExtent(b.bounds,true));
return c
},mergeContextToMap:function(a,b){b.addLayers(this.getLayersFromContext(a.layersContext));
return b
},write:function(d,b){d=this.toContext(d);
var a=b&&b.version;
var e=this.getParser(a);
var c=e.write(d,b);
return c
},layerToContext:function(a){var c=this.getParser();
var b={queryable:a.queryable,visibility:a.visibility,name:a.params.LAYERS,title:a.name,metadataURL:a.metadataURL,version:a.params.VERSION,url:a.url,maxExtent:a.maxExtent,transparent:a.params.TRANSPARENT,numZoomLevels:a.numZoomLevels,units:a.units,isBaseLayer:a.isBaseLayer,opacity:a.opacity,displayInLayerSwitcher:a.displayInLayerSwitcher,singleTile:a.singleTile,minScale:(a.options.resolutions||a.options.scales||a.options.maxResolution||a.options.minScale)?a.minScale:undefined,maxScale:(a.options.resolutions||a.options.scales||a.options.minResolution||a.options.maxScale)?a.maxScale:undefined,formats:[{value:a.params.FORMAT,current:true}],styles:[{href:a.params.SLD,body:a.params.SLD_BODY,name:a.params.STYLES||c.defaultStyleName,title:c.defaultStyleTitle,current:true}]};
return b
},toContext:function(f){var d={};
var e=f.layers;
if(f.CLASS_NAME=="OpenLayers.Map"){d.bounds=f.getExtent();
d.maxExtent=f.maxExtent;
d.projection=f.projection;
d.size=f.getSize()
}else{OpenLayers.Util.applyDefaults(d,f);
if(d.layers!=undefined){delete (d.layers)
}}if(d.layersContext==undefined){d.layersContext=[]
}if(e!=undefined&&e instanceof Array){for(var c=0,a=e.length;
c<a;
c++){var b=e[c];
if(b instanceof OpenLayers.Layer.WMS){d.layersContext.push(this.layerToContext(b))
}}}return d
},CLASS_NAME:"OpenLayers.Format.WMC"});OpenLayers.Format.WMC.v1=OpenLayers.Class(OpenLayers.Format.XML,{namespaces:{ol:"http://openlayers.org/context",wmc:"http://www.opengis.net/context",sld:"http://www.opengis.net/sld",xlink:"http://www.w3.org/1999/xlink",xsi:"http://www.w3.org/2001/XMLSchema-instance"},schemaLocation:"",getNamespacePrefix:function(a){var b=null;
if(a==null){b=this.namespaces[this.defaultPrefix]
}else{for(b in this.namespaces){if(this.namespaces[b]==a){break
}}}return b
},defaultPrefix:"wmc",rootPrefix:null,defaultStyleName:"",defaultStyleTitle:"Default",initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},read:function(c){if(typeof c=="string"){c=OpenLayers.Format.XML.prototype.read.apply(this,[c])
}var a=c.documentElement;
this.rootPrefix=a.prefix;
var b={version:a.getAttribute("version")};
this.runChildNodes(b,a);
return b
},runChildNodes:function(e,d){var b=d.childNodes;
var a,c,g,j;
for(var f=0,h=b.length;
f<h;
++f){a=b[f];
if(a.nodeType==1){g=this.getNamespacePrefix(a.namespaceURI);
j=a.nodeName.split(":").pop();
c=this["read_"+g+"_"+j];
if(c){c.apply(this,[e,a])
}}}},read_wmc_General:function(a,b){this.runChildNodes(a,b)
},read_wmc_BoundingBox:function(a,b){a.projection=b.getAttribute("SRS");
a.bounds=new OpenLayers.Bounds(parseFloat(b.getAttribute("minx")),parseFloat(b.getAttribute("miny")),parseFloat(b.getAttribute("maxx")),parseFloat(b.getAttribute("maxy")))
},read_wmc_LayerList:function(a,b){a.layersContext=[];
this.runChildNodes(a,b)
},read_wmc_Layer:function(a,b){var c={visibility:(b.getAttribute("hidden")!="1"),queryable:(b.getAttribute("queryable")=="1"),formats:[],styles:[]};
this.runChildNodes(c,b);
a.layersContext.push(c)
},read_wmc_Extension:function(b,a){this.runChildNodes(b,a)
},read_ol_units:function(b,a){b.units=this.getChildValue(a)
},read_ol_maxExtent:function(c,b){var a=new OpenLayers.Bounds(b.getAttribute("minx"),b.getAttribute("miny"),b.getAttribute("maxx"),b.getAttribute("maxy"));
c.maxExtent=a
},read_ol_transparent:function(b,a){b.transparent=this.getChildValue(a)
},read_ol_numZoomLevels:function(b,a){b.numZoomLevels=parseInt(this.getChildValue(a))
},read_ol_opacity:function(b,a){b.opacity=parseFloat(this.getChildValue(a))
},read_ol_singleTile:function(b,a){b.singleTile=(this.getChildValue(a)=="true")
},read_ol_isBaseLayer:function(b,a){b.isBaseLayer=(this.getChildValue(a)=="true")
},read_ol_displayInLayerSwitcher:function(b,a){b.displayInLayerSwitcher=(this.getChildValue(a)=="true")
},read_wmc_Server:function(c,b){c.version=b.getAttribute("version");
var d={};
var a=b.getElementsByTagName("OnlineResource");
if(a.length>0){this.read_wmc_OnlineResource(d,a[0])
}c.url=d.href
},read_wmc_FormatList:function(b,a){this.runChildNodes(b,a)
},read_wmc_Format:function(b,a){var c={value:this.getChildValue(a)};
if(a.getAttribute("current")=="1"){c.current=true
}b.formats.push(c)
},read_wmc_StyleList:function(b,a){this.runChildNodes(b,a)
},read_wmc_Style:function(c,b){var a={};
this.runChildNodes(a,b);
if(b.getAttribute("current")=="1"){a.current=true
}c.styles.push(a)
},read_wmc_SLD:function(a,b){this.runChildNodes(a,b)
},read_sld_StyledLayerDescriptor:function(c,b){var a=OpenLayers.Format.XML.prototype.write.apply(this,[b]);
c.body=a
},read_wmc_OnlineResource:function(b,a){b.href=this.getAttributeNS(a,this.namespaces.xlink,"href")
},read_wmc_Name:function(c,b){var a=this.getChildValue(b);
if(a){c.name=a
}},read_wmc_Title:function(b,a){var c=this.getChildValue(a);
if(c){b.title=c
}},read_wmc_MetadataURL:function(c,b){var d={};
var a=b.getElementsByTagName("OnlineResource");
if(a.length>0){this.read_wmc_OnlineResource(d,a[0])
}c.metadataURL=d.href
},read_wmc_Abstract:function(c,b){var a=this.getChildValue(b);
if(a){c["abstract"]=a
}},read_wmc_LegendURL:function(c,d){var b={width:d.getAttribute("width"),height:d.getAttribute("height")};
var a=d.getElementsByTagName("OnlineResource");
if(a.length>0){this.read_wmc_OnlineResource(b,a[0])
}c.legend=b
},write:function(c,b){var a=this.createElementDefaultNS("ViewContext");
this.setAttributes(a,{version:this.VERSION,id:(b&&typeof b.id=="string")?b.id:OpenLayers.Util.createUniqueID("OpenLayers_Context_")});
this.setAttributeNS(a,this.namespaces.xsi,"xsi:schemaLocation",this.schemaLocation);
a.appendChild(this.write_wmc_General(c));
a.appendChild(this.write_wmc_LayerList(c));
return OpenLayers.Format.XML.prototype.write.apply(this,[a])
},createElementDefaultNS:function(c,b,a){var d=this.createElementNS(this.namespaces[this.defaultPrefix],c);
if(b){d.appendChild(this.createTextNode(b))
}if(a){this.setAttributes(d,a)
}return d
},setAttributes:function(b,d){var c;
for(var a in d){c=d[a].toString();
if(c.match(/[A-Z]/)){this.setAttributeNS(b,null,a,c)
}else{b.setAttribute(a,c)
}}},write_wmc_General:function(a){var c=this.createElementDefaultNS("General");
if(a.size){c.appendChild(this.createElementDefaultNS("Window",null,{width:a.size.w,height:a.size.h}))
}var b=a.bounds;
c.appendChild(this.createElementDefaultNS("BoundingBox",null,{minx:b.left.toPrecision(18),miny:b.bottom.toPrecision(18),maxx:b.right.toPrecision(18),maxy:b.top.toPrecision(18),SRS:a.projection}));
c.appendChild(this.createElementDefaultNS("Title",a.title));
c.appendChild(this.write_ol_MapExtension(a));
return c
},write_ol_MapExtension:function(b){var d=this.createElementDefaultNS("Extension");
var c=b.maxExtent;
if(c){var a=this.createElementNS(this.namespaces.ol,"ol:maxExtent");
this.setAttributes(a,{minx:c.left.toPrecision(18),miny:c.bottom.toPrecision(18),maxx:c.right.toPrecision(18),maxy:c.top.toPrecision(18)});
d.appendChild(a)
}return d
},write_wmc_LayerList:function(c){var d=this.createElementDefaultNS("LayerList");
for(var b=0,a=c.layersContext.length;
b<a;
++b){d.appendChild(this.write_wmc_Layer(c.layersContext[b]))
}return d
},write_wmc_Layer:function(a){var b=this.createElementDefaultNS("Layer",null,{queryable:a.queryable?"1":"0",hidden:a.visibility?"0":"1"});
b.appendChild(this.write_wmc_Server(a));
b.appendChild(this.createElementDefaultNS("Name",a.name));
b.appendChild(this.createElementDefaultNS("Title",a.title));
if(a.metadataURL){b.appendChild(this.write_wmc_MetadataURL(a.metadataURL))
}return b
},write_wmc_LayerExtension:function(e){var g=this.createElementDefaultNS("Extension");
var f=e.maxExtent;
var b=this.createElementNS(this.namespaces.ol,"ol:maxExtent");
this.setAttributes(b,{minx:f.left.toPrecision(18),miny:f.bottom.toPrecision(18),maxx:f.right.toPrecision(18),maxy:f.top.toPrecision(18)});
g.appendChild(b);
var d=["transparent","numZoomLevels","units","isBaseLayer","opacity","displayInLayerSwitcher","singleTile"];
var h;
for(var c=0,a=d.length;
c<a;
++c){h=this.createOLPropertyNode(e,d[c]);
if(h){g.appendChild(h)
}}return g
},createOLPropertyNode:function(b,c){var a=null;
if(b[c]!=null){a=this.createElementNS(this.namespaces.ol,"ol:"+c);
a.appendChild(this.createTextNode(b[c].toString()))
}return a
},write_wmc_Server:function(a){var b=this.createElementDefaultNS("Server");
this.setAttributes(b,{service:"OGC:WMS",version:a.version});
b.appendChild(this.write_wmc_OnlineResource(a.url));
return b
},write_wmc_MetadataURL:function(b){var a=this.createElementDefaultNS("MetadataURL");
a.appendChild(this.write_wmc_OnlineResource(b));
return a
},write_wmc_FormatList:function(c){var d=this.createElementDefaultNS("FormatList");
for(var b=0,a=c.formats.length;
b<a;
b++){var e=c.formats[b];
d.appendChild(this.createElementDefaultNS("Format",e.value,(e.current&&e.current==true)?{current:"1"}:null))
}return d
},write_wmc_StyleList:function(e){var c=this.createElementDefaultNS("StyleList");
var k=e.styles;
if(k&&k instanceof Array){var a;
for(var d=0,f=k.length;
d<f;
d++){var l=k[d];
var b=this.createElementDefaultNS("Style",null,(l.current&&l.current==true)?{current:"1"}:null);
if(l.href){a=this.createElementDefaultNS("SLD");
var g=this.write_wmc_OnlineResource(l.href);
a.appendChild(g);
a.appendChild(this.createElementDefaultNS("Name",l.name));
if(l.title){a.appendChild(this.createElementDefaultNS("Title",l.title))
}b.appendChild(a)
}else{if(l.body){a=this.createElementDefaultNS("SLD");
var h=OpenLayers.Format.XML.prototype.read.apply(this,[l.body]);
var j=h.documentElement;
if(a.ownerDocument&&a.ownerDocument.importNode){j=a.ownerDocument.importNode(j,true)
}a.appendChild(j);
a.appendChild(this.createElementDefaultNS("Name",l.name));
if(l.title){a.appendChild(this.createElementDefaultNS("Title",l.title))
}b.appendChild(a)
}else{b.appendChild(this.createElementDefaultNS("Name",l.name));
b.appendChild(this.createElementDefaultNS("Title",l.title));
if(l["abstract"]){b.appendChild(this.createElementDefaultNS("Abstract",l["abstract"]))
}}}c.appendChild(b)
}}return c
},write_wmc_OnlineResource:function(a){var b=this.createElementDefaultNS("OnlineResource");
this.setAttributeNS(b,this.namespaces.xlink,"xlink:type","simple");
this.setAttributeNS(b,this.namespaces.xlink,"xlink:href",a);
return b
},CLASS_NAME:"OpenLayers.Format.WMC.v1"});OpenLayers.Format.WMC.v1_0_0=OpenLayers.Class(OpenLayers.Format.WMC.v1,{VERSION:"1.0.0",schemaLocation:"http://www.opengis.net/context http://schemas.opengis.net/context/1.0.0/context.xsd",initialize:function(a){OpenLayers.Format.WMC.v1.prototype.initialize.apply(this,[a])
},write_wmc_Layer:function(a){var b=OpenLayers.Format.WMC.v1.prototype.write_wmc_Layer.apply(this,[a]);
b.appendChild(this.write_wmc_FormatList(a));
b.appendChild(this.write_wmc_StyleList(a));
b.appendChild(this.write_wmc_LayerExtension(a))
},CLASS_NAME:"OpenLayers.Format.WMC.v1_0_0"});OpenLayers.Format.WMC.v1_1_0=OpenLayers.Class(OpenLayers.Format.WMC.v1,{VERSION:"1.1.0",schemaLocation:"http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd",initialize:function(a){OpenLayers.Format.WMC.v1.prototype.initialize.apply(this,[a])
},read_sld_MinScaleDenominator:function(c,b){var a=parseFloat(this.getChildValue(b));
if(a>0){c.maxScale=a
}},read_sld_MaxScaleDenominator:function(b,a){b.minScale=parseFloat(this.getChildValue(a))
},write_wmc_Layer:function(b){var c=OpenLayers.Format.WMC.v1.prototype.write_wmc_Layer.apply(this,[b]);
if(b.maxScale){var d=this.createElementNS(this.namespaces.sld,"sld:MinScaleDenominator");
d.appendChild(this.createTextNode(b.maxScale.toPrecision(16)));
c.appendChild(d)
}if(b.minScale){var a=this.createElementNS(this.namespaces.sld,"sld:MaxScaleDenominator");
a.appendChild(this.createTextNode(b.minScale.toPrecision(16)));
c.appendChild(a)
}c.appendChild(this.write_wmc_FormatList(b));
c.appendChild(this.write_wmc_StyleList(b));
c.appendChild(this.write_wmc_LayerExtension(b));
return c
},CLASS_NAME:"OpenLayers.Format.WMC.v1_1_0"});OpenLayers.Format.WKT=OpenLayers.Class(OpenLayers.Format,{initialize:function(a){this.regExes={typeStr:/^\s*(\w+)\s*\(\s*(.*)\s*\)\s*$/,spaces:/\s+/,parenComma:/\)\s*,\s*\(/,doubleParenComma:/\)\s*\)\s*,\s*\(\s*\(/,trimParens:/^\s*\(?(.*?)\)?\s*$/};
OpenLayers.Format.prototype.initialize.apply(this,[a])
},read:function(f){var e,d,h;
var g=this.regExes.typeStr.exec(f);
if(g){d=g[1].toLowerCase();
h=g[2];
if(this.parse[d]){e=this.parse[d].apply(this,[h])
}if(this.internalProjection&&this.externalProjection){if(e&&e.CLASS_NAME=="OpenLayers.Feature.Vector"){e.geometry.transform(this.externalProjection,this.internalProjection)
}else{if(e&&d!="geometrycollection"&&typeof e=="object"){for(var c=0,a=e.length;
c<a;
c++){var b=e[c];
b.geometry.transform(this.externalProjection,this.internalProjection)
}}}}}return e
},write:function(a){var f,j,h,d,b;
if(a.constructor==Array){f=a;
b=true
}else{f=[a];
b=false
}var c=[];
if(b){c.push("GEOMETRYCOLLECTION(")
}for(var e=0,g=f.length;
e<g;
++e){if(b&&e>0){c.push(",")
}j=f[e].geometry;
h=j.CLASS_NAME.split(".")[2].toLowerCase();
if(!this.extract[h]){return null
}if(this.internalProjection&&this.externalProjection){j=j.clone();
j.transform(this.internalProjection,this.externalProjection)
}d=this.extract[h].apply(this,[j]);
c.push(h.toUpperCase()+"("+d+")")
}if(b){c.push(")")
}return c.join("")
},extract:{point:function(a){return a.x+" "+a.y
},multipoint:function(c){var d=[];
for(var b=0,a=c.components.length;
b<a;
++b){d.push(this.extract.point.apply(this,[c.components[b]]))
}return d.join(",")
},linestring:function(b){var d=[];
for(var c=0,a=b.components.length;
c<a;
++c){d.push(this.extract.point.apply(this,[b.components[c]]))
}return d.join(",")
},multilinestring:function(c){var d=[];
for(var b=0,a=c.components.length;
b<a;
++b){d.push("("+this.extract.linestring.apply(this,[c.components[b]])+")")
}return d.join(",")
},polygon:function(c){var d=[];
for(var b=0,a=c.components.length;
b<a;
++b){d.push("("+this.extract.linestring.apply(this,[c.components[b]])+")")
}return d.join(",")
},multipolygon:function(d){var c=[];
for(var b=0,a=d.components.length;
b<a;
++b){c.push("("+this.extract.polygon.apply(this,[d.components[b]])+")")
}return c.join(",")
}},parse:{point:function(b){var a=OpenLayers.String.trim(b).split(this.regExes.spaces);
return new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(a[0],a[1]))
},multipoint:function(e){var c=OpenLayers.String.trim(e).split(",");
var d=[];
for(var b=0,a=c.length;
b<a;
++b){d.push(this.parse.point.apply(this,[c[b]]).geometry)
}return new OpenLayers.Feature.Vector(new OpenLayers.Geometry.MultiPoint(d))
},linestring:function(e){var c=OpenLayers.String.trim(e).split(",");
var d=[];
for(var b=0,a=c.length;
b<a;
++b){d.push(this.parse.point.apply(this,[c[b]]).geometry)
}return new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(d))
},multilinestring:function(f){var c;
var b=OpenLayers.String.trim(f).split(this.regExes.parenComma);
var e=[];
for(var d=0,a=b.length;
d<a;
++d){c=b[d].replace(this.regExes.trimParens,"$1");
e.push(this.parse.linestring.apply(this,[c]).geometry)
}return new OpenLayers.Feature.Vector(new OpenLayers.Geometry.MultiLineString(e))
},polygon:function(h){var c,b,f;
var g=OpenLayers.String.trim(h).split(this.regExes.parenComma);
var e=[];
for(var d=0,a=g.length;
d<a;
++d){c=g[d].replace(this.regExes.trimParens,"$1");
b=this.parse.linestring.apply(this,[c]).geometry;
f=new OpenLayers.Geometry.LinearRing(b.components);
e.push(f)
}return new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon(e))
},multipolygon:function(f){var d;
var b=OpenLayers.String.trim(f).split(this.regExes.doubleParenComma);
var e=[];
for(var c=0,a=b.length;
c<a;
++c){d=b[c].replace(this.regExes.trimParens,"$1");
e.push(this.parse.polygon.apply(this,[d]).geometry)
}return new OpenLayers.Feature.Vector(new OpenLayers.Geometry.MultiPolygon(e))
},geometrycollection:function(e){e=e.replace(/,\s*([A-Za-z])/g,"|$1");
var d=OpenLayers.String.trim(e).split("|");
var c=[];
for(var b=0,a=d.length;
b<a;
++b){c.push(OpenLayers.Format.WKT.prototype.read.apply(this,[d[b]]))
}return c
}},CLASS_NAME:"OpenLayers.Format.WKT"});OpenLayers.Format.WMSGetFeatureInfo=OpenLayers.Class(OpenLayers.Format.XML,{layerIdentifier:"_layer",featureIdentifier:"_feature",regExes:{trimSpace:(/^\s*|\s*$/g),removeSpace:(/\s*/g),splitSpace:(/\s+/),trimComma:(/\s*,\s*/g)},gmlFormat:null,initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,arguments);
OpenLayers.Util.extend(this,a);
this.options=a
},read:function(e){var a;
if(typeof e=="string"){e=OpenLayers.Format.XML.prototype.read.apply(this,[e])
}var b=e.documentElement;
if(b){var c=this;
var d=this["read_"+b.nodeName];
if(d){a=d.call(this,b)
}else{a=new OpenLayers.Format.GML((this.options?this.options:{})).read(e)
}}else{a=e
}return a
},read_msGMLOutput:function(h){var e=[];
var b=this.getSiblingNodesByTagCriteria(h,this.layerIdentifier);
if(b){for(var k=0,o=b.length;
k<o;
++k){var c=b[k];
var l=c.nodeName;
if(c.prefix){l=l.split(":")[1]
}var l=l.replace(this.layerIdentifier,"");
var m=this.getSiblingNodesByTagCriteria(c,this.featureIdentifier);
if(m){for(var g=0;
g<m.length;
g++){var a=m[g];
var d=this.parseGeometry(a);
var f=this.parseAttributes(a);
var p=new OpenLayers.Feature.Vector(d.geometry,f,null);
p.bounds=d.bounds;
p.type=l;
e.push(p)
}}}}return e
},read_FeatureInfoResponse:function(f){var c=[];
var h=this.getElementsByTagNameNS(f,"*","FIELDS");
for(var g=0,k=h.length;
g<k;
g++){var a=h[g];
var l=null;
var e={};
for(var d=0,m=a.attributes.length;
d<m;
d++){var b=a.attributes[d];
e[b.nodeName]=b.nodeValue
}c.push(new OpenLayers.Feature.Vector(l,e,null))
}return c
},getSiblingNodesByTagCriteria:function(f,i){var a=[];
var c,e,d,g,b;
if(f&&f.hasChildNodes()){c=f.childNodes;
d=c.length;
for(var h=0;
h<d;
h++){b=c[h];
while(b&&b.nodeType!=1){b=b.nextSibling;
h++
}e=(b?b.nodeName:"");
if(e.length>0&&e.indexOf(i)>-1){a.push(b)
}else{g=this.getSiblingNodesByTagCriteria(b,i);
if(g.length>0){(a.length==0)?a=g:a.push(g)
}}}}return a
},parseAttributes:function(d){var e={};
if(d.nodeType==1){var c=d.childNodes;
n=c.length;
for(var f=0;
f<n;
++f){var b=c[f];
if(b.nodeType==1){var j=b.childNodes;
if(j.length==1){var h=j[0];
if(h.nodeType==3||h.nodeType==4){var a=(b.prefix)?b.nodeName.split(":")[1]:b.nodeName;
var g=h.nodeValue.replace(this.regExes.trimSpace,"");
e[a]=g
}}}}}return e
},parseGeometry:function(c){if(!this.gmlFormat){this.gmlFormat=new OpenLayers.Format.GML()
}var a=this.gmlFormat.parseFeature(c);
var d,b=null;
if(a&&a.geometry){d=a.geometry.clone();
b=a.bounds&&a.bounds.clone();
a.destroy()
}return{geometry:d,bounds:b}
},CLASS_NAME:"OpenLayers.Format.WMSGetFeatureInfo"});OpenLayers.Geometry=OpenLayers.Class({id:null,parent:null,bounds:null,initialize:function(){this.id=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_")
},destroy:function(){this.id=null;
this.bounds=null
},clone:function(){return new OpenLayers.Geometry()
},setBounds:function(a){if(a){this.bounds=a.clone()
}},clearBounds:function(){this.bounds=null;
if(this.parent){this.parent.clearBounds()
}},extendBounds:function(b){var a=this.getBounds();
if(!a){this.setBounds(b)
}else{this.bounds.extend(b)
}},getBounds:function(){if(this.bounds==null){this.calculateBounds()
}return this.bounds
},calculateBounds:function(){},distanceTo:function(b,a){},getVertices:function(a){},atPoint:function(e,h,f){var c=false;
var d=this.getBounds();
if((d!=null)&&(e!=null)){var b=(h!=null)?h:0;
var a=(f!=null)?f:0;
var g=new OpenLayers.Bounds(this.bounds.left-b,this.bounds.bottom-a,this.bounds.right+b,this.bounds.top+a);
c=g.containsLonLat(e)
}return c
},getLength:function(){return 0
},getArea:function(){return 0
},getCentroid:function(){return null
},toString:function(){return OpenLayers.Format.WKT.prototype.write(new OpenLayers.Feature.Vector(this))
},CLASS_NAME:"OpenLayers.Geometry"});
OpenLayers.Geometry.fromWKT=function(f){var g=arguments.callee.format;
if(!g){g=new OpenLayers.Format.WKT();
arguments.callee.format=g
}var d;
var b=g.read(f);
if(b instanceof OpenLayers.Feature.Vector){d=b.geometry
}else{if(b instanceof Array){var a=b.length;
var e=new Array(a);
for(var c=0;
c<a;
++c){e[c]=b[c].geometry
}d=new OpenLayers.Geometry.Collection(e)
}}return d
};
OpenLayers.Geometry.segmentsIntersect=function(a,H,b){var s=b&&b.point;
var z=b&&b.tolerance;
var f=false;
var B=a.x1-H.x1;
var F=a.y1-H.y1;
var o=a.x2-a.x1;
var w=a.y2-a.y1;
var t=H.y2-H.y1;
var l=H.x2-H.x1;
var D=(t*o)-(l*w);
var e=(l*F)-(t*B);
var c=(o*F)-(w*B);
if(D==0){if(e==0&&c==0){f=true
}}else{var E=e/D;
var C=c/D;
if(E>=0&&E<=1&&C>=0&&C<=1){if(!s){f=true
}else{var h=a.x1+(E*o);
var g=a.y1+(E*w);
f=new OpenLayers.Geometry.Point(h,g)
}}}if(z){var r;
if(f){if(s){var n=[a,H];
var A,h,g;
outer:for(var v=0;
v<2;
++v){A=n[v];
for(var u=1;
u<3;
++u){h=A["x"+u];
g=A["y"+u];
r=Math.sqrt(Math.pow(h-f.x,2)+Math.pow(g-f.y,2));
if(r<z){f.x=h;
f.y=g;
break outer
}}}}}else{var n=[a,H];
var q,G,h,g,m,k;
outer:for(var v=0;
v<2;
++v){q=n[v];
G=n[(v+1)%2];
for(var u=1;
u<3;
++u){m={x:q["x"+u],y:q["y"+u]};
k=OpenLayers.Geometry.distanceToSegment(m,G);
if(k.distance<z){if(s){f=new OpenLayers.Geometry.Point(m.x,m.y)
}else{f=true
}break outer
}}}}}return f
};
OpenLayers.Geometry.distanceToSegment=function(k,d){var c=k.x;
var j=k.y;
var b=d.x1;
var i=d.y1;
var a=d.x2;
var f=d.y2;
var m=a-b;
var l=f-i;
var h=((m*(c-b))+(l*(j-i)))/(Math.pow(m,2)+Math.pow(l,2));
var g,e;
if(h<=0){g=b;
e=i
}else{if(h>=1){g=a;
e=f
}else{g=b+h*m;
e=i+h*l
}}return{distance:Math.sqrt(Math.pow(g-c,2)+Math.pow(e-j,2)),x:g,y:e}
};OpenLayers.Control=OpenLayers.Class({id:null,map:null,div:null,type:null,allowSelection:false,displayClass:"",title:"",autoActivate:false,active:null,handler:null,eventListeners:null,events:null,EVENT_TYPES:["activate","deactivate"],initialize:function(a){this.displayClass=this.CLASS_NAME.replace("OpenLayers.","ol").replace(/\./g,"");
OpenLayers.Util.extend(this,a);
this.events=new OpenLayers.Events(this,null,this.EVENT_TYPES);
if(this.eventListeners instanceof Object){this.events.on(this.eventListeners)
}if(this.id==null){this.id=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_")
}},destroy:function(){if(this.events){if(this.eventListeners){this.events.un(this.eventListeners)
}this.events.destroy();
this.events=null
}this.eventListeners=null;
if(this.handler){this.handler.destroy();
this.handler=null
}if(this.handlers){for(var a in this.handlers){if(this.handlers.hasOwnProperty(a)&&typeof this.handlers[a].destroy=="function"){this.handlers[a].destroy()
}}this.handlers=null
}if(this.map){this.map.removeControl(this);
this.map=null
}},setMap:function(a){this.map=a;
if(this.handler){this.handler.setMap(a)
}},draw:function(a){if(this.div==null){this.div=OpenLayers.Util.createDiv(this.id);
this.div.className=this.displayClass;
if(!this.allowSelection){this.div.className+=" olControlNoSelect";
this.div.setAttribute("unselectable","on",0);
this.div.onselectstart=OpenLayers.Function.False
}if(this.title!=""){this.div.title=this.title
}}if(a!=null){this.position=a.clone()
}this.moveTo(this.position);
return this.div
},moveTo:function(a){if((a!=null)&&(this.div!=null)){this.div.style.left=a.x+"px";
this.div.style.top=a.y+"px"
}},activate:function(){if(this.active){return false
}if(this.handler){this.handler.activate()
}this.active=true;
if(this.map){OpenLayers.Element.addClass(this.map.viewPortDiv,this.displayClass.replace(/ /g,"")+"Active")
}this.events.triggerEvent("activate");
return true
},deactivate:function(){if(this.active){if(this.handler){this.handler.deactivate()
}this.active=false;
if(this.map){OpenLayers.Element.removeClass(this.map.viewPortDiv,this.displayClass.replace(/ /g,"")+"Active")
}this.events.triggerEvent("deactivate");
return true
}return false
},CLASS_NAME:"OpenLayers.Control"});
OpenLayers.Control.TYPE_BUTTON=1;
OpenLayers.Control.TYPE_TOGGLE=2;
OpenLayers.Control.TYPE_TOOL=3;OpenLayers.Control.PanZoom=OpenLayers.Class(OpenLayers.Control,{slideFactor:50,slideRatio:null,buttons:null,position:null,initialize:function(a){this.position=new OpenLayers.Pixel(OpenLayers.Control.PanZoom.X,OpenLayers.Control.PanZoom.Y);
OpenLayers.Control.prototype.initialize.apply(this,arguments)
},destroy:function(){OpenLayers.Control.prototype.destroy.apply(this,arguments);
this.removeButtons();
this.buttons=null;
this.position=null
},draw:function(b){OpenLayers.Control.prototype.draw.apply(this,arguments);
b=this.position;
this.buttons=[];
var c=new OpenLayers.Size(18,18);
var a=new OpenLayers.Pixel(b.x+c.w/2,b.y);
this._addButton("panup","north-mini.png",a,c);
b.y=a.y+c.h;
this._addButton("panleft","west-mini.png",b,c);
this._addButton("panright","east-mini.png",b.add(c.w,0),c);
this._addButton("pandown","south-mini.png",a.add(0,c.h*2),c);
this._addButton("zoomin","zoom-plus-mini.png",a.add(0,c.h*3+5),c);
this._addButton("zoomworld","zoom-world-mini.png",a.add(0,c.h*4+5),c);
this._addButton("zoomout","zoom-minus-mini.png",a.add(0,c.h*5+5),c);
return this.div
},_addButton:function(a,d,i,g){var f=OpenLayers.Util.getImagesLocation()+d;
var b=OpenLayers.Util.createAlphaImageDiv(this.id+"_"+a,i,g,f,"absolute");
this.div.appendChild(b);
OpenLayers.Event.observe(b,"mousedown",OpenLayers.Function.bindAsEventListener(this.buttonDown,b));
OpenLayers.Event.observe(b,"dblclick",OpenLayers.Function.bindAsEventListener(this.doubleClick,b));
OpenLayers.Event.observe(b,"click",OpenLayers.Function.bindAsEventListener(this.doubleClick,b));
b.action=a;
b.map=this.map;
if(!this.slideRatio){var c=this.slideFactor;
var e=function(){return c
}
}else{var h=this.slideRatio;
var e=function(j){return this.map.getSize()[j]*h
}
}b.getSlideFactor=e;
this.buttons.push(b);
return b
},_removeButton:function(a){OpenLayers.Event.stopObservingElement(a);
a.map=null;
a.getSlideFactor=null;
this.div.removeChild(a);
OpenLayers.Util.removeItem(this.buttons,a)
},removeButtons:function(){for(var a=this.buttons.length-1;
a>=0;
--a){this._removeButton(this.buttons[a])
}},doubleClick:function(a){OpenLayers.Event.stop(a);
return false
},buttonDown:function(a){if(!OpenLayers.Event.isLeftClick(a)){return
}switch(this.action){case"panup":this.map.pan(0,-this.getSlideFactor("h"));
break;
case"pandown":this.map.pan(0,this.getSlideFactor("h"));
break;
case"panleft":this.map.pan(-this.getSlideFactor("w"),0);
break;
case"panright":this.map.pan(this.getSlideFactor("w"),0);
break;
case"zoomin":this.map.zoomIn();
break;
case"zoomout":this.map.zoomOut();
break;
case"zoomworld":this.map.zoomToMaxExtent();
break
}OpenLayers.Event.stop(a)
},CLASS_NAME:"OpenLayers.Control.PanZoom"});
OpenLayers.Control.PanZoom.X=4;
OpenLayers.Control.PanZoom.Y=4;OpenLayers.Control.PanZoomBar=OpenLayers.Class(OpenLayers.Control.PanZoom,{zoomStopWidth:18,zoomStopHeight:11,slider:null,sliderEvents:null,zoombarDiv:null,divEvents:null,zoomWorldIcon:false,forceFixedZoomLevel:false,mouseDragStart:null,zoomStart:null,initialize:function(){OpenLayers.Control.PanZoom.prototype.initialize.apply(this,arguments)
},destroy:function(){this._removeZoomBar();
this.map.events.un({changebaselayer:this.redraw,scope:this});
OpenLayers.Control.PanZoom.prototype.destroy.apply(this,arguments);
delete this.mouseDragStart;
delete this.zoomStart
},setMap:function(a){OpenLayers.Control.PanZoom.prototype.setMap.apply(this,arguments);
this.map.events.register("changebaselayer",this,this.redraw)
},redraw:function(){if(this.div!=null){this.removeButtons();
this._removeZoomBar()
}this.draw()
},draw:function(b){OpenLayers.Control.prototype.draw.apply(this,arguments);
b=this.position.clone();
this.buttons=[];
var d=new OpenLayers.Size(18,18);
var a=new OpenLayers.Pixel(b.x+d.w/2,b.y);
var c=d.w;
if(this.zoomWorldIcon){a=new OpenLayers.Pixel(b.x+d.w,b.y)
}this._addButton("panup","north-mini.png",a,d);
b.y=a.y+d.h;
this._addButton("panleft","west-mini.png",b,d);
if(this.zoomWorldIcon){this._addButton("zoomworld","zoom-world-mini.png",b.add(d.w,0),d);
c*=2
}this._addButton("panright","east-mini.png",b.add(c,0),d);
this._addButton("pandown","south-mini.png",a.add(0,d.h*2),d);
this._addButton("zoomin","zoom-plus-mini.png",a.add(0,d.h*3+5),d);
a=this._addZoomBar(a.add(0,d.h*4+5));
this._addButton("zoomout","zoom-minus-mini.png",a,d);
return this.div
},_addZoomBar:function(a){var e=OpenLayers.Util.getImagesLocation();
var g=this.id+"_"+this.map.id;
var b=this.map.getNumZoomLevels()-1-this.map.getZoom();
var c=OpenLayers.Util.createAlphaImageDiv(g,a.add(-1,b*this.zoomStopHeight),new OpenLayers.Size(20,9),e+"slider.png","absolute");
this.slider=c;
this.sliderEvents=new OpenLayers.Events(this,c,null,true,{includeXY:true});
this.sliderEvents.on({mousedown:this.zoomBarDown,mousemove:this.zoomBarDrag,mouseup:this.zoomBarUp,dblclick:this.doubleClick,click:this.doubleClick});
var d=new OpenLayers.Size();
d.h=this.zoomStopHeight*this.map.getNumZoomLevels();
d.w=this.zoomStopWidth;
var f=null;
if(OpenLayers.Util.alphaHack()){var g=this.id+"_"+this.map.id;
f=OpenLayers.Util.createAlphaImageDiv(g,a,new OpenLayers.Size(d.w,this.zoomStopHeight),e+"zoombar.png","absolute",null,"crop");
f.style.height=d.h+"px"
}else{f=OpenLayers.Util.createDiv("OpenLayers_Control_PanZoomBar_Zoombar"+this.map.id,a,d,e+"zoombar.png")
}this.zoombarDiv=f;
this.divEvents=new OpenLayers.Events(this,f,null,true,{includeXY:true});
this.divEvents.on({mousedown:this.divClick,mousemove:this.passEventToSlider,dblclick:this.doubleClick,click:this.doubleClick});
this.div.appendChild(f);
this.startTop=parseInt(f.style.top);
this.div.appendChild(c);
this.map.events.register("zoomend",this,this.moveZoomBar);
a=a.add(0,this.zoomStopHeight*this.map.getNumZoomLevels());
return a
},_removeZoomBar:function(){this.sliderEvents.un({mousedown:this.zoomBarDown,mousemove:this.zoomBarDrag,mouseup:this.zoomBarUp,dblclick:this.doubleClick,click:this.doubleClick});
this.sliderEvents.destroy();
this.divEvents.un({mousedown:this.divClick,mousemove:this.passEventToSlider,dblclick:this.doubleClick,click:this.doubleClick});
this.divEvents.destroy();
this.div.removeChild(this.zoombarDiv);
this.zoombarDiv=null;
this.div.removeChild(this.slider);
this.slider=null;
this.map.events.unregister("zoomend",this,this.moveZoomBar)
},passEventToSlider:function(a){this.sliderEvents.handleBrowserEvent(a)
},divClick:function(a){if(!OpenLayers.Event.isLeftClick(a)){return
}var e=a.xy.y;
var d=OpenLayers.Util.pagePosition(a.object)[1];
var c=(e-d)/this.zoomStopHeight;
if(this.forceFixedZoomLevel||!this.map.fractionalZoom){c=Math.floor(c)
}var b=(this.map.getNumZoomLevels()-1)-c;
b=Math.min(Math.max(b,0),this.map.getNumZoomLevels()-1);
this.map.zoomTo(b);
OpenLayers.Event.stop(a)
},zoomBarDown:function(a){if(!OpenLayers.Event.isLeftClick(a)){return
}this.map.events.on({mousemove:this.passEventToSlider,mouseup:this.passEventToSlider,scope:this});
this.mouseDragStart=a.xy.clone();
this.zoomStart=a.xy.clone();
this.div.style.cursor="move";
this.zoombarDiv.offsets=null;
OpenLayers.Event.stop(a)
},zoomBarDrag:function(b){if(this.mouseDragStart!=null){var a=this.mouseDragStart.y-b.xy.y;
var d=OpenLayers.Util.pagePosition(this.zoombarDiv);
if((b.clientY-d[1])>0&&(b.clientY-d[1])<parseInt(this.zoombarDiv.style.height)-2){var c=parseInt(this.slider.style.top)-a;
this.slider.style.top=c+"px";
this.mouseDragStart=b.xy.clone()
}OpenLayers.Event.stop(b)
}},zoomBarUp:function(b){if(!OpenLayers.Event.isLeftClick(b)){return
}if(this.mouseDragStart){this.div.style.cursor="";
this.map.events.un({mouseup:this.passEventToSlider,mousemove:this.passEventToSlider,scope:this});
var a=this.zoomStart.y-b.xy.y;
var c=this.map.zoom;
if(!this.forceFixedZoomLevel&&this.map.fractionalZoom){c+=a/this.zoomStopHeight;
c=Math.min(Math.max(c,0),this.map.getNumZoomLevels()-1)
}else{c+=Math.round(a/this.zoomStopHeight)
}this.map.zoomTo(c);
this.mouseDragStart=null;
this.zoomStart=null;
OpenLayers.Event.stop(b)
}},moveZoomBar:function(){var a=((this.map.getNumZoomLevels()-1)-this.map.getZoom())*this.zoomStopHeight+this.startTop+1;
this.slider.style.top=a+"px"
},CLASS_NAME:"OpenLayers.Control.PanZoomBar"});OpenLayers.Tween=OpenLayers.Class({INTERVAL:10,easing:null,begin:null,finish:null,duration:null,callbacks:null,time:null,interval:null,playing:false,initialize:function(a){this.easing=(a)?a:OpenLayers.Easing.Expo.easeOut
},start:function(c,b,d,a){this.playing=true;
this.begin=c;
this.finish=b;
this.duration=d;
this.callbacks=a.callbacks;
this.time=0;
if(this.interval){window.clearInterval(this.interval);
this.interval=null
}if(this.callbacks&&this.callbacks.start){this.callbacks.start.call(this,this.begin)
}this.interval=window.setInterval(OpenLayers.Function.bind(this.play,this),this.INTERVAL)
},stop:function(){if(!this.playing){return
}if(this.callbacks&&this.callbacks.done){this.callbacks.done.call(this,this.finish)
}window.clearInterval(this.interval);
this.interval=null;
this.playing=false
},play:function(){var g={};
for(var d in this.begin){var a=this.begin[d];
var e=this.finish[d];
if(a==null||e==null||isNaN(a)||isNaN(e)){OpenLayers.Console.error("invalid value for Tween")
}var h=e-a;
g[d]=this.easing.apply(this,[this.time,a,h,this.duration])
}this.time++;
if(this.callbacks&&this.callbacks.eachStep){this.callbacks.eachStep.call(this,g)
}if(this.time>this.duration){if(this.callbacks&&this.callbacks.done){this.callbacks.done.call(this,this.finish);
this.playing=false
}window.clearInterval(this.interval);
this.interval=null
}},CLASS_NAME:"OpenLayers.Tween"});
OpenLayers.Easing={CLASS_NAME:"OpenLayers.Easing"};
OpenLayers.Easing.Linear={easeIn:function(e,a,g,f){return g*e/f+a
},easeOut:function(e,a,g,f){return g*e/f+a
},easeInOut:function(e,a,g,f){return g*e/f+a
},CLASS_NAME:"OpenLayers.Easing.Linear"};
OpenLayers.Easing.Expo={easeIn:function(e,a,g,f){return(e==0)?a:g*Math.pow(2,10*(e/f-1))+a
},easeOut:function(e,a,g,f){return(e==f)?a+g:g*(-Math.pow(2,-10*e/f)+1)+a
},easeInOut:function(e,a,g,f){if(e==0){return a
}if(e==f){return a+g
}if((e/=f/2)<1){return g/2*Math.pow(2,10*(e-1))+a
}return g/2*(-Math.pow(2,-10*--e)+2)+a
},CLASS_NAME:"OpenLayers.Easing.Expo"};
OpenLayers.Easing.Quad={easeIn:function(e,a,g,f){return g*(e/=f)*e+a
},easeOut:function(e,a,g,f){return -g*(e/=f)*(e-2)+a
},easeInOut:function(e,a,g,f){if((e/=f/2)<1){return g/2*e*e+a
}return -g/2*((--e)*(e-2)-1)+a
},CLASS_NAME:"OpenLayers.Easing.Quad"};OpenLayers.Map=OpenLayers.Class({Z_INDEX_BASE:{BaseLayer:100,Overlay:325,Feature:725,Popup:750,Control:1000},EVENT_TYPES:["preaddlayer","addlayer","removelayer","changelayer","movestart","move","moveend","zoomend","popupopen","popupclose","addmarker","removemarker","clearmarkers","mouseover","mouseout","mousemove","dragstart","drag","dragend","changebaselayer"],id:null,fractionalZoom:false,events:null,allOverlays:false,div:null,dragging:false,size:null,viewPortDiv:null,layerContainerOrigin:null,layerContainerDiv:null,layers:null,controls:null,popups:null,baseLayer:null,center:null,resolution:null,zoom:0,panRatio:1.5,viewRequestID:0,tileSize:null,projection:"EPSG:4326",units:"degrees",resolutions:null,maxResolution:1.40625,minResolution:null,maxScale:null,minScale:null,maxExtent:null,minExtent:null,restrictedExtent:null,numZoomLevels:16,theme:null,displayProjection:null,fallThrough:true,panTween:null,eventListeners:null,panMethod:OpenLayers.Easing.Expo.easeOut,panDuration:50,paddingForPopups:null,initialize:function(h,d){if(arguments.length===1&&typeof h==="object"){d=h;
h=d&&d.div
}this.tileSize=new OpenLayers.Size(OpenLayers.Map.TILE_WIDTH,OpenLayers.Map.TILE_HEIGHT);
this.maxExtent=new OpenLayers.Bounds(-180,-90,180,90);
this.paddingForPopups=new OpenLayers.Bounds(15,15,15,15);
this.theme=OpenLayers._getScriptLocation()+"theme/default/style.css";
OpenLayers.Util.extend(this,d);
this.layers=[];
this.id=OpenLayers.Util.createUniqueID("OpenLayers.Map_");
this.div=OpenLayers.Util.getElement(h);
if(!this.div){this.div=document.createElement("div");
this.div.style.height="1px";
this.div.style.width="1px"
}OpenLayers.Element.addClass(this.div,"olMap");
var g=this.id+"_OpenLayers_ViewPort";
this.viewPortDiv=OpenLayers.Util.createDiv(g,null,null,null,"relative",null,"hidden");
this.viewPortDiv.style.width="100%";
this.viewPortDiv.style.height="100%";
this.viewPortDiv.className="olMapViewport";
this.div.appendChild(this.viewPortDiv);
g=this.id+"_OpenLayers_Container";
this.layerContainerDiv=OpenLayers.Util.createDiv(g);
this.layerContainerDiv.style.zIndex=this.Z_INDEX_BASE.Popup-1;
this.viewPortDiv.appendChild(this.layerContainerDiv);
this.events=new OpenLayers.Events(this,this.div,this.EVENT_TYPES,this.fallThrough,{includeXY:true});
this.updateSize();
if(this.eventListeners instanceof Object){this.events.on(this.eventListeners)
}this.events.register("movestart",this,this.updateSize);
if(OpenLayers.String.contains(navigator.appName,"Microsoft")){this.events.register("resize",this,this.updateSize)
}else{this.updateSizeDestroy=OpenLayers.Function.bind(this.updateSize,this);
OpenLayers.Event.observe(window,"resize",this.updateSizeDestroy)
}if(this.theme){var f=true;
var c=document.getElementsByTagName("link");
for(var e=0,a=c.length;
e<a;
++e){if(OpenLayers.Util.isEquivalentUrl(c.item(e).href,this.theme)){f=false;
break
}}if(f){var b=document.createElement("link");
b.setAttribute("rel","stylesheet");
b.setAttribute("type","text/css");
b.setAttribute("href",this.theme);
document.getElementsByTagName("head")[0].appendChild(b)
}}if(this.controls==null){if(OpenLayers.Control!=null){this.controls=[new OpenLayers.Control.Navigation(),new OpenLayers.Control.PanZoom(),new OpenLayers.Control.ArgParser(),new OpenLayers.Control.Attribution()]
}else{this.controls=[]
}}for(var e=0,a=this.controls.length;
e<a;
e++){this.addControlToMap(this.controls[e])
}this.popups=[];
this.unloadDestroy=OpenLayers.Function.bind(this.destroy,this);
OpenLayers.Event.observe(window,"unload",this.unloadDestroy);
if(d&&d.layers){this.addLayers(d.layers);
if(d.center){this.setCenter(d.center,d.zoom)
}}},render:function(a){this.div=OpenLayers.Util.getElement(a);
OpenLayers.Element.addClass(this.div,"olMap");
this.events.attachToElement(this.div);
this.viewPortDiv.parentNode.removeChild(this.viewPortDiv);
this.div.appendChild(this.viewPortDiv);
this.updateSize()
},unloadDestroy:null,updateSizeDestroy:null,destroy:function(){if(!this.unloadDestroy){return false
}if(this.panTween&&this.panTween.playing){this.panTween.stop()
}OpenLayers.Event.stopObserving(window,"unload",this.unloadDestroy);
this.unloadDestroy=null;
if(this.updateSizeDestroy){OpenLayers.Event.stopObserving(window,"resize",this.updateSizeDestroy)
}else{this.events.unregister("resize",this,this.updateSize)
}this.paddingForPopups=null;
if(this.controls!=null){for(var a=this.controls.length-1;
a>=0;
--a){this.controls[a].destroy()
}this.controls=null
}if(this.layers!=null){for(var a=this.layers.length-1;
a>=0;
--a){this.layers[a].destroy(false)
}this.layers=null
}if(this.viewPortDiv){this.div.removeChild(this.viewPortDiv)
}this.viewPortDiv=null;
if(this.eventListeners){this.events.un(this.eventListeners);
this.eventListeners=null
}this.events.destroy();
this.events=null
},setOptions:function(a){OpenLayers.Util.extend(this,a)
},getTileSize:function(){return this.tileSize
},getBy:function(e,c,a){var d=(typeof a.test=="function");
var b=OpenLayers.Array.filter(this[e],function(f){return f[c]==a||(d&&a.test(f[c]))
});
return b
},getLayersBy:function(b,a){return this.getBy("layers",b,a)
},getLayersByName:function(a){return this.getLayersBy("name",a)
},getLayersByClass:function(a){return this.getLayersBy("CLASS_NAME",a)
},getControlsBy:function(b,a){return this.getBy("controls",b,a)
},getControlsByClass:function(a){return this.getControlsBy("CLASS_NAME",a)
},getLayer:function(e){var b=null;
for(var d=0,a=this.layers.length;
d<a;
d++){var c=this.layers[d];
if(c.id==e){b=c;
break
}}return b
},setLayerZIndex:function(b,a){b.setZIndex(this.Z_INDEX_BASE[b.isBaseLayer?"BaseLayer":"Overlay"]+a*5)
},resetLayersZIndex:function(){for(var c=0,a=this.layers.length;
c<a;
c++){var b=this.layers[c];
this.setLayerZIndex(b,c)
}},addLayer:function(c){for(var b=0,a=this.layers.length;
b<a;
b++){if(this.layers[b]==c){var d=OpenLayers.i18n("layerAlreadyAdded",{layerName:c.name});
OpenLayers.Console.warn(d);
return false
}}if(this.allOverlays){c.isBaseLayer=false
}if(this.events.triggerEvent("preaddlayer",{layer:c})===false){return
}c.div.className="olLayerDiv";
c.div.style.overflow="";
this.setLayerZIndex(c,this.layers.length);
if(c.isFixed){this.viewPortDiv.appendChild(c.div)
}else{this.layerContainerDiv.appendChild(c.div)
}this.layers.push(c);
c.setMap(this);
if(c.isBaseLayer||(this.allOverlays&&!this.baseLayer)){if(this.baseLayer==null){this.setBaseLayer(c)
}else{c.setVisibility(false)
}}else{c.redraw()
}this.events.triggerEvent("addlayer",{layer:c});
c.afterAdd()
},addLayers:function(c){for(var b=0,a=c.length;
b<a;
b++){this.addLayer(c[b])
}},removeLayer:function(c,e){if(e==null){e=true
}if(c.isFixed){this.viewPortDiv.removeChild(c.div)
}else{this.layerContainerDiv.removeChild(c.div)
}OpenLayers.Util.removeItem(this.layers,c);
c.removeMap(this);
c.map=null;
if(this.baseLayer==c){this.baseLayer=null;
if(e){for(var b=0,a=this.layers.length;
b<a;
b++){var d=this.layers[b];
if(d.isBaseLayer||this.allOverlays){this.setBaseLayer(d);
break
}}}}this.resetLayersZIndex();
this.events.triggerEvent("removelayer",{layer:c})
},getNumLayers:function(){return this.layers.length
},getLayerIndex:function(a){return OpenLayers.Util.indexOf(this.layers,a)
},setLayerIndex:function(d,b){var e=this.getLayerIndex(d);
if(b<0){b=0
}else{if(b>this.layers.length){b=this.layers.length
}}if(e!=b){this.layers.splice(e,1);
this.layers.splice(b,0,d);
for(var c=0,a=this.layers.length;
c<a;
c++){this.setLayerZIndex(this.layers[c],c)
}this.events.triggerEvent("changelayer",{layer:d,property:"order"});
if(this.allOverlays){if(b===0){this.setBaseLayer(d)
}else{if(this.baseLayer!==this.layers[0]){this.setBaseLayer(this.layers[0])
}}}}},raiseLayer:function(b,c){var a=this.getLayerIndex(b)+c;
this.setLayerIndex(b,a)
},setBaseLayer:function(c){if(c!=this.baseLayer){if(OpenLayers.Util.indexOf(this.layers,c)!=-1){var a=this.getCenter();
var d=OpenLayers.Util.getResolutionFromScale(this.getScale(),c.units);
if(this.baseLayer!=null&&!this.allOverlays){this.baseLayer.setVisibility(false)
}this.baseLayer=c;
this.viewRequestID++;
if(!this.allOverlays||this.baseLayer.visibility){this.baseLayer.setVisibility(true)
}if(a!=null){var b=this.getZoomForResolution(d||this.resolution,true);
this.setCenter(a,b,false,true)
}this.events.triggerEvent("changebaselayer",{layer:this.baseLayer})
}}},addControl:function(b,a){this.controls.push(b);
this.addControlToMap(b,a)
},addControls:function(b,g){var e=(arguments.length===1)?[]:g;
for(var d=0,a=b.length;
d<a;
d++){var f=b[d];
var c=(e[d])?e[d]:null;
this.addControl(f,c)
}},addControlToMap:function(b,a){b.outsideViewport=(b.div!=null);
if(this.displayProjection&&!b.displayProjection){b.displayProjection=this.displayProjection
}b.setMap(this);
var c=b.draw(a);
if(c){if(!b.outsideViewport){c.style.zIndex=this.Z_INDEX_BASE.Control+this.controls.length;
this.viewPortDiv.appendChild(c)
}}if(b.autoActivate){b.activate()
}},getControl:function(e){var b=null;
for(var c=0,a=this.controls.length;
c<a;
c++){var d=this.controls[c];
if(d.id==e){b=d;
break
}}return b
},removeControl:function(a){if((a)&&(a==this.getControl(a.id))){if(a.div&&(a.div.parentNode==this.viewPortDiv)){this.viewPortDiv.removeChild(a.div)
}OpenLayers.Util.removeItem(this.controls,a)
}},addPopup:function(a,d){if(d){for(var b=this.popups.length-1;
b>=0;
--b){this.removePopup(this.popups[b])
}}a.map=this;
this.popups.push(a);
var c=a.draw();
if(c){c.style.zIndex=this.Z_INDEX_BASE.Popup+this.popups.length;
this.layerContainerDiv.appendChild(c)
}},removePopup:function(a){OpenLayers.Util.removeItem(this.popups,a);
if(a.div){try{this.layerContainerDiv.removeChild(a.div)
}catch(b){}}a.map=null
},getSize:function(){var a=null;
if(this.size!=null){a=this.size.clone()
}return a
},updateSize:function(){var c=this.getCurrentSize();
if(c&&!isNaN(c.h)&&!isNaN(c.w)){this.events.clearMouseCache();
var f=this.getSize();
if(f==null){this.size=f=c
}if(!c.equals(f)){this.size=c;
for(var d=0,b=this.layers.length;
d<b;
d++){this.layers[d].onMapResize()
}var a=this.getCenter();
if(this.baseLayer!=null&&a!=null){var e=this.getZoom();
this.zoom=null;
this.setCenter(a,e)
}}}},getCurrentSize:function(){var a=new OpenLayers.Size(this.div.clientWidth,this.div.clientHeight);
if(a.w==0&&a.h==0||isNaN(a.w)&&isNaN(a.h)){a.w=this.div.offsetWidth;
a.h=this.div.offsetHeight
}if(a.w==0&&a.h==0||isNaN(a.w)&&isNaN(a.h)){a.w=parseInt(this.div.style.width);
a.h=parseInt(this.div.style.height)
}return a
},calculateBounds:function(a,b){var e=null;
if(a==null){a=this.getCenter()
}if(b==null){b=this.getResolution()
}if((a!=null)&&(b!=null)){var d=this.getSize();
var f=d.w*b;
var c=d.h*b;
e=new OpenLayers.Bounds(a.lon-f/2,a.lat-c/2,a.lon+f/2,a.lat+c/2)
}return e
},getCenter:function(){var a=null;
if(this.center){a=this.center.clone()
}return a
},getZoom:function(){return this.zoom
},pan:function(d,c,e){e=OpenLayers.Util.applyDefaults(e,{animate:true,dragging:false});
var f=this.getViewPortPxFromLonLat(this.getCenter());
var b=f.add(d,c);
if(!e.dragging||!b.equals(f)){var a=this.getLonLatFromViewPortPx(b);
if(e.animate){this.panTo(a)
}else{this.setCenter(a,null,e.dragging)
}}},panTo:function(b){if(this.panMethod&&this.getExtent().scale(this.panRatio).containsLonLat(b)){if(!this.panTween){this.panTween=new OpenLayers.Tween(this.panMethod)
}var a=this.getCenter();
if(b.lon==a.lon&&b.lat==a.lat){return
}var d={lon:a.lon,lat:a.lat};
var c={lon:b.lon,lat:b.lat};
this.panTween.start(d,c,this.panDuration,{callbacks:{start:OpenLayers.Function.bind(function(e){this.events.triggerEvent("movestart")
},this),eachStep:OpenLayers.Function.bind(function(e){e=new OpenLayers.LonLat(e.lon,e.lat);
this.moveTo(e,this.zoom,{dragging:true,noEvent:true})
},this),done:OpenLayers.Function.bind(function(e){e=new OpenLayers.LonLat(e.lon,e.lat);
this.moveTo(e,this.zoom,{noEvent:true});
this.events.triggerEvent("moveend")
},this)}})
}else{this.setCenter(b)
}},setCenter:function(c,a,b,d){this.moveTo(c,a,{dragging:b,forceZoomChange:d,caller:"setCenter"})
},moveTo:function(g,n,q){if(!q){q={}
}if(n!=null){n=parseFloat(n);
if(!this.fractionalZoom){n=Math.round(n)
}}var m=q.dragging;
var c=q.forceZoomChange;
var h=q.noEvent;
if(this.panTween&&q.caller=="setCenter"){this.panTween.stop()
}if(!this.center&&!this.isValidLonLat(g)){g=this.maxExtent.getCenterLonLat()
}if(this.restrictedExtent!=null){if(g==null){g=this.getCenter()
}if(n==null){n=this.getZoom()
}var d=this.getResolutionForZoom(n);
var o=this.calculateBounds(g,d);
if(!this.restrictedExtent.containsBounds(o)){var p=this.restrictedExtent.getCenterLonLat();
if(o.getWidth()>this.restrictedExtent.getWidth()){g=new OpenLayers.LonLat(p.lon,g.lat)
}else{if(o.left<this.restrictedExtent.left){g=g.add(this.restrictedExtent.left-o.left,0)
}else{if(o.right>this.restrictedExtent.right){g=g.add(this.restrictedExtent.right-o.right,0)
}}}if(o.getHeight()>this.restrictedExtent.getHeight()){g=new OpenLayers.LonLat(g.lon,p.lat)
}else{if(o.bottom<this.restrictedExtent.bottom){g=g.add(0,this.restrictedExtent.bottom-o.bottom)
}else{if(o.top>this.restrictedExtent.top){g=g.add(0,this.restrictedExtent.top-o.top)
}}}}}var b=c||((this.isValidZoomLevel(n))&&(n!=this.getZoom()));
var e=(this.isValidLonLat(g))&&(!g.equals(this.center));
if(b||e||!m){if(!this.dragging&&!h){this.events.triggerEvent("movestart")
}if(e){if((!b)&&(this.center)){this.centerLayerContainer(g)
}this.center=g.clone()
}if((b)||(this.layerContainerOrigin==null)){this.layerContainerOrigin=this.center.clone();
this.layerContainerDiv.style.left="0px";
this.layerContainerDiv.style.top="0px"
}if(b){this.zoom=n;
this.resolution=this.getResolutionForZoom(n);
this.viewRequestID++
}var a=this.getExtent();
if(this.baseLayer.visibility){this.baseLayer.moveTo(a,b,m);
if(m){this.baseLayer.events.triggerEvent("move")
}else{this.baseLayer.events.triggerEvent("moveend",{zoomChanged:b})
}}a=this.baseLayer.getExtent();
for(var f=0,k=this.layers.length;
f<k;
f++){var j=this.layers[f];
if(j!==this.baseLayer&&!j.isBaseLayer){var l=j.calculateInRange();
if(j.inRange!=l){j.inRange=l;
if(!l){j.display(false)
}this.events.triggerEvent("changelayer",{layer:j,property:"visibility"})
}if(l&&j.visibility){j.moveTo(a,b,m);
if(m){j.events.triggerEvent("move")
}else{j.events.triggerEvent("moveend",{zoomChanged:b})
}}}}if(b){for(var f=0,k=this.popups.length;
f<k;
f++){this.popups[f].updatePosition()
}}this.events.triggerEvent("move");
if(b){this.events.triggerEvent("zoomend")
}}if(!m&&!h){this.events.triggerEvent("moveend")
}this.dragging=!!m
},centerLayerContainer:function(b){var a=this.getViewPortPxFromLonLat(this.layerContainerOrigin);
var c=this.getViewPortPxFromLonLat(b);
if((a!=null)&&(c!=null)){this.layerContainerDiv.style.left=Math.round(a.x-c.x)+"px";
this.layerContainerDiv.style.top=Math.round(a.y-c.y)+"px"
}},isValidZoomLevel:function(a){return((a!=null)&&(a>=0)&&(a<this.getNumZoomLevels()))
},isValidLonLat:function(c){var b=false;
if(c!=null){var a=this.getMaxExtent();
b=a.containsLonLat(c)
}return b
},getProjection:function(){var a=this.getProjectionObject();
return a?a.getCode():null
},getProjectionObject:function(){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.projection
}return a
},getMaxResolution:function(){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.maxResolution
}return a
},getMaxExtent:function(b){var a=null;
if(b&&b.restricted&&this.restrictedExtent){a=this.restrictedExtent
}else{if(this.baseLayer!=null){a=this.baseLayer.maxExtent
}}return a
},getNumZoomLevels:function(){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.numZoomLevels
}return a
},getExtent:function(){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.getExtent()
}return a
},getResolution:function(){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.getResolution()
}else{if(this.allOverlays===true&&this.layers.length>0){a=this.layers[0].getResolution()
}}return a
},getUnits:function(){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.units
}return a
},getScale:function(){var c=null;
if(this.baseLayer!=null){var b=this.getResolution();
var a=this.baseLayer.units;
c=OpenLayers.Util.getScaleFromResolution(b,a)
}return c
},getZoomForExtent:function(c,b){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.getZoomForExtent(c,b)
}return a
},getResolutionForZoom:function(b){var a=null;
if(this.baseLayer){a=this.baseLayer.getResolutionForZoom(b)
}return a
},getZoomForResolution:function(a,c){var b=null;
if(this.baseLayer!=null){b=this.baseLayer.getZoomForResolution(a,c)
}return b
},zoomTo:function(a){if(this.isValidZoomLevel(a)){this.setCenter(null,a)
}},zoomIn:function(){this.zoomTo(this.getZoom()+1)
},zoomOut:function(){this.zoomTo(this.getZoom()-1)
},zoomToExtent:function(d,c){var b=d.getCenterLonLat();
if(this.baseLayer.wrapDateLine){var a=this.getMaxExtent();
d=d.clone();
while(d.right<d.left){d.right+=a.getWidth()
}b=d.getCenterLonLat().wrapDateLine(a)
}this.setCenter(b,this.getZoomForExtent(d,c))
},zoomToMaxExtent:function(c){var b=(c)?c.restricted:true;
var a=this.getMaxExtent({restricted:b});
this.zoomToExtent(a)
},zoomToScale:function(h,g){var d=OpenLayers.Util.getResolutionFromScale(h,this.baseLayer.units);
var c=this.getSize();
var f=c.w*d;
var b=c.h*d;
var a=this.getCenter();
var e=new OpenLayers.Bounds(a.lon-f/2,a.lat-b/2,a.lon+f/2,a.lat+b/2);
this.zoomToExtent(e,g)
},getLonLatFromViewPortPx:function(a){var b=null;
if(this.baseLayer!=null){b=this.baseLayer.getLonLatFromViewPortPx(a)
}return b
},getViewPortPxFromLonLat:function(b){var a=null;
if(this.baseLayer!=null){a=this.baseLayer.getViewPortPxFromLonLat(b)
}return a
},getLonLatFromPixel:function(a){return this.getLonLatFromViewPortPx(a)
},getPixelFromLonLat:function(b){var a=this.getViewPortPxFromLonLat(b);
a.x=Math.round(a.x);
a.y=Math.round(a.y);
return a
},getViewPortPxFromLayerPx:function(d){var c=null;
if(d!=null){var b=parseInt(this.layerContainerDiv.style.left);
var a=parseInt(this.layerContainerDiv.style.top);
c=d.add(b,a)
}return c
},getLayerPxFromViewPortPx:function(c){var d=null;
if(c!=null){var b=-parseInt(this.layerContainerDiv.style.left);
var a=-parseInt(this.layerContainerDiv.style.top);
d=c.add(b,a);
if(isNaN(d.x)||isNaN(d.y)){d=null
}}return d
},getLonLatFromLayerPx:function(a){a=this.getViewPortPxFromLayerPx(a);
return this.getLonLatFromViewPortPx(a)
},getLayerPxFromLonLat:function(b){var a=this.getPixelFromLonLat(b);
return this.getLayerPxFromViewPortPx(a)
},CLASS_NAME:"OpenLayers.Map"});
OpenLayers.Map.TILE_WIDTH=256;
OpenLayers.Map.TILE_HEIGHT=256;OpenLayers.Projection=OpenLayers.Class({proj:null,projCode:null,initialize:function(b,a){OpenLayers.Util.extend(this,a);
this.projCode=b;
if(window.Proj4js){this.proj=new Proj4js.Proj(b)
}},getCode:function(){return this.proj?this.proj.srsCode:this.projCode
},getUnits:function(){return this.proj?this.proj.units:null
},toString:function(){return this.getCode()
},equals:function(a){if(a&&a.getCode){return this.getCode()==a.getCode()
}else{return false
}},destroy:function(){delete this.proj;
delete this.projCode
},CLASS_NAME:"OpenLayers.Projection"});
OpenLayers.Projection.transforms={};
OpenLayers.Projection.addTransform=function(c,b,a){if(!OpenLayers.Projection.transforms[c]){OpenLayers.Projection.transforms[c]={}
}OpenLayers.Projection.transforms[c][b]=a
};
OpenLayers.Projection.transform=function(a,c,b){if(c.proj&&b.proj){a=Proj4js.transform(c.proj,b.proj,a)
}else{if(c&&b&&OpenLayers.Projection.transforms[c.getCode()]&&OpenLayers.Projection.transforms[c.getCode()][b.getCode()]){OpenLayers.Projection.transforms[c.getCode()][b.getCode()](a)
}}return a
};OpenLayers.Layer=OpenLayers.Class({id:null,name:null,div:null,opacity:null,alwaysInRange:null,EVENT_TYPES:["loadstart","loadend","loadcancel","visibilitychanged","move","moveend"],events:null,map:null,isBaseLayer:false,alpha:false,displayInLayerSwitcher:true,visibility:true,attribution:null,inRange:false,imageSize:null,imageOffset:null,options:null,eventListeners:null,gutter:0,projection:null,units:null,scales:null,resolutions:null,maxExtent:null,minExtent:null,maxResolution:null,minResolution:null,numZoomLevels:null,minScale:null,maxScale:null,displayOutsideMaxExtent:false,wrapDateLine:false,transitionEffect:null,SUPPORTED_TRANSITIONS:["resize"],initialize:function(b,a){this.addOptions(a);
this.name=b;
if(this.id==null){this.id=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_");
this.div=OpenLayers.Util.createDiv(this.id);
this.div.style.width="100%";
this.div.style.height="100%";
this.div.dir="ltr";
this.events=new OpenLayers.Events(this,this.div,this.EVENT_TYPES);
if(this.eventListeners instanceof Object){this.events.on(this.eventListeners)
}}if(this.wrapDateLine){this.displayOutsideMaxExtent=true
}},destroy:function(a){if(a==null){a=true
}if(this.map!=null){this.map.removeLayer(this,a)
}this.projection=null;
this.map=null;
this.name=null;
this.div=null;
this.options=null;
if(this.events){if(this.eventListeners){this.events.un(this.eventListeners)
}this.events.destroy()
}this.eventListeners=null;
this.events=null
},clone:function(a){if(a==null){a=new OpenLayers.Layer(this.name,this.getOptions())
}OpenLayers.Util.applyDefaults(a,this);
a.map=null;
return a
},getOptions:function(){var a={};
for(var b in this.options){a[b]=this[b]
}return a
},setName:function(a){if(a!=this.name){this.name=a;
if(this.map!=null){this.map.events.triggerEvent("changelayer",{layer:this,property:"name"})
}}},addOptions:function(a){if(this.options==null){this.options={}
}OpenLayers.Util.extend(this.options,a);
OpenLayers.Util.extend(this,a)
},onMapResize:function(){},redraw:function(){var b=false;
if(this.map){this.inRange=this.calculateInRange();
var c=this.getExtent();
if(c&&this.inRange&&this.visibility){var a=true;
this.moveTo(c,a,false);
this.events.triggerEvent("moveend",{zoomChanged:a});
b=true
}}return b
},moveTo:function(b,a,c){var d=this.visibility;
if(!this.isBaseLayer){d=d&&this.inRange
}this.display(d)
},setMap:function(b){if(this.map==null){this.map=b;
this.maxExtent=this.maxExtent||this.map.maxExtent;
this.projection=this.projection||this.map.projection;
if(this.projection&&typeof this.projection=="string"){this.projection=new OpenLayers.Projection(this.projection)
}this.units=this.projection.getUnits()||this.units||this.map.units;
this.initResolutions();
if(!this.isBaseLayer){this.inRange=this.calculateInRange();
var a=((this.visibility)&&(this.inRange));
this.div.style.display=a?"":"none"
}this.setTileSize()
}},afterAdd:function(){},removeMap:function(a){},getImageSize:function(a){return(this.imageSize||this.tileSize)
},setTileSize:function(a){var b=(a)?a:((this.tileSize)?this.tileSize:this.map.getTileSize());
this.tileSize=b;
if(this.gutter){this.imageOffset=new OpenLayers.Pixel(-this.gutter,-this.gutter);
this.imageSize=new OpenLayers.Size(b.w+(2*this.gutter),b.h+(2*this.gutter))
}},getVisibility:function(){return this.visibility
},setVisibility:function(a){if(a!=this.visibility){this.visibility=a;
this.display(a);
this.redraw();
if(this.map!=null){this.map.events.triggerEvent("changelayer",{layer:this,property:"visibility"})
}this.events.triggerEvent("visibilitychanged")
}},display:function(a){if(a!=(this.div.style.display!="none")){this.div.style.display=(a&&this.calculateInRange())?"block":"none"
}},calculateInRange:function(){var b=false;
if(this.alwaysInRange){b=true
}else{if(this.map){var a=this.map.getResolution();
b=((a>=this.minResolution)&&(a<=this.maxResolution))
}}return b
},setIsBaseLayer:function(a){if(a!=this.isBaseLayer){this.isBaseLayer=a;
if(this.map!=null){this.map.events.triggerEvent("changebaselayer",{layer:this})
}}},initResolutions:function(){var n=new Array("projection","units","scales","resolutions","maxScale","minScale","maxResolution","minResolution","minExtent","maxExtent","numZoomLevels","maxZoomLevel");
var b=["projection","units"];
var e=false;
var d={};
for(var f=0,j=n.length;
f<j;
f++){var p=n[f];
if(this.options[p]&&OpenLayers.Util.indexOf(b,p)==-1){e=true
}d[p]=this.options[p]||this.map[p]
}if(this.alwaysInRange==null){this.alwaysInRange=!e
}if((this.options.minScale!=null||this.options.maxScale!=null)&&this.options.scales==null){d.scales=null
}if((this.options.minResolution!=null||this.options.maxResolution!=null)&&this.options.resolutions==null){d.resolutions=null
}if((!d.numZoomLevels)&&(d.maxZoomLevel)){d.numZoomLevels=d.maxZoomLevel+1
}if((d.scales!=null)||(d.resolutions!=null)){if(d.scales!=null){d.resolutions=[];
for(var f=0,j=d.scales.length;
f<j;
f++){var c=d.scales[f];
d.resolutions[f]=OpenLayers.Util.getResolutionFromScale(c,d.units)
}}d.numZoomLevels=d.resolutions.length
}else{if(d.minScale){d.maxResolution=OpenLayers.Util.getResolutionFromScale(d.minScale,d.units)
}else{if(d.maxResolution=="auto"){var o=this.map.getSize();
var m=d.maxExtent.getWidth()/o.w;
var h=d.maxExtent.getHeight()/o.h;
d.maxResolution=Math.max(m,h)
}}if(d.maxScale!=null){d.minResolution=OpenLayers.Util.getResolutionFromScale(d.maxScale,d.units)
}else{if((d.minResolution=="auto")&&(d.minExtent!=null)){var o=this.map.getSize();
var m=d.minExtent.getWidth()/o.w;
var h=d.minExtent.getHeight()/o.h;
d.minResolution=Math.max(m,h)
}}if(d.minResolution!=null&&this.options.numZoomLevels==undefined){var l=d.maxResolution/d.minResolution;
d.numZoomLevels=Math.floor(Math.log(l)/Math.log(2))+1
}d.resolutions=new Array(d.numZoomLevels);
var a=2;
if(typeof d.minResolution=="number"&&d.numZoomLevels>1){a=Math.pow((d.maxResolution/d.minResolution),(1/(d.numZoomLevels-1)))
}for(var f=0;
f<d.numZoomLevels;
f++){var k=d.maxResolution/Math.pow(a,f);
d.resolutions[f]=k
}}d.resolutions.sort(function(q,i){return(i-q)
});
this.resolutions=d.resolutions;
this.maxResolution=d.resolutions[0];
var g=d.resolutions.length-1;
this.minResolution=d.resolutions[g];
this.scales=[];
for(var f=0,j=d.resolutions.length;
f<j;
f++){this.scales[f]=OpenLayers.Util.getScaleFromResolution(d.resolutions[f],d.units)
}this.minScale=this.scales[0];
this.maxScale=this.scales[this.scales.length-1];
this.numZoomLevels=d.numZoomLevels
},getResolution:function(){var a=this.map.getZoom();
return this.getResolutionForZoom(a)
},getExtent:function(){return this.map.calculateBounds()
},getZoomForExtent:function(b,c){var d=this.map.getSize();
var a=Math.max(b.getWidth()/d.w,b.getHeight()/d.h);
return this.getZoomForResolution(a,c)
},getDataExtent:function(){},getResolutionForZoom:function(c){c=Math.max(0,Math.min(c,this.resolutions.length-1));
var b;
if(this.map.fractionalZoom){var a=Math.floor(c);
var d=Math.ceil(c);
b=this.resolutions[a]-((c-a)*(this.resolutions[a]-this.resolutions[d]))
}else{b=this.resolutions[Math.round(c)]
}return b
},getZoomForResolution:function(e,a){var n;
if(this.map.fractionalZoom){var k=0;
var c=this.resolutions.length-1;
var d=this.resolutions[k];
var b=this.resolutions[c];
var j;
for(var f=0,g=this.resolutions.length;
f<g;
++f){j=this.resolutions[f];
if(j>=e){d=j;
k=f
}if(j<=e){b=j;
c=f;
break
}}var h=d-b;
if(h>0){n=k+((d-e)/h)
}else{n=k
}}else{var l;
var m=Number.POSITIVE_INFINITY;
for(var f=0,g=this.resolutions.length;
f<g;
f++){if(a){l=Math.abs(this.resolutions[f]-e);
if(l>m){break
}m=l
}else{if(this.resolutions[f]<e){break
}}}n=Math.max(0,f-1)
}return n
},getLonLatFromViewPortPx:function(b){var e=null;
if(b!=null){var d=this.map.getSize();
var a=this.map.getCenter();
if(a){var c=this.map.getResolution();
var g=b.x-(d.w/2);
var f=b.y-(d.h/2);
e=new OpenLayers.LonLat(a.lon+g*c,a.lat-f*c);
if(this.wrapDateLine){e=e.wrapDateLine(this.maxExtent)
}}}return e
},getViewPortPxFromLonLat:function(d){var b=null;
if(d!=null){var a=this.map.getResolution();
var c=this.map.getExtent();
b=new OpenLayers.Pixel((1/a*(d.lon-c.left)),(1/a*(c.top-d.lat)))
}return b
},setOpacity:function(b){if(b!=this.opacity){this.opacity=b;
for(var d=0,a=this.div.childNodes.length;
d<a;
++d){var c=this.div.childNodes[d].firstChild;
OpenLayers.Util.modifyDOMElement(c,null,null,null,null,null,null,b)
}if(this.map!=null){this.map.events.triggerEvent("changelayer",{layer:this,property:"opacity"})
}}},getZIndex:function(){return this.div.style.zIndex
},setZIndex:function(a){this.div.style.zIndex=a
},adjustBounds:function(b){if(this.gutter){var a=this.gutter*this.map.getResolution();
b=new OpenLayers.Bounds(b.left-a,b.bottom-a,b.right+a,b.top+a)
}if(this.wrapDateLine){var c={rightTolerance:this.getResolution()};
b=b.wrapDateLine(this.maxExtent,c)
}return b
},CLASS_NAME:"OpenLayers.Layer"});OpenLayers.Tile=OpenLayers.Class({EVENT_TYPES:["loadstart","loadend","reload","unload"],events:null,id:null,layer:null,url:null,bounds:null,size:null,position:null,isLoading:false,initialize:function(d,a,e,b,c){this.layer=d;
this.position=a.clone();
this.bounds=e.clone();
this.url=b;
this.size=c.clone();
this.id=OpenLayers.Util.createUniqueID("Tile_");
this.events=new OpenLayers.Events(this,null,this.EVENT_TYPES)
},unload:function(){if(this.isLoading){this.isLoading=false;
this.events.triggerEvent("unload")
}},destroy:function(){this.layer=null;
this.bounds=null;
this.size=null;
this.position=null;
this.events.destroy();
this.events=null
},clone:function(a){if(a==null){a=new OpenLayers.Tile(this.layer,this.position,this.bounds,this.url,this.size)
}OpenLayers.Util.applyDefaults(a,this);
return a
},draw:function(){var a=this.layer.maxExtent;
var b=(a&&this.bounds.intersectsBounds(a,false));
this.shouldDraw=(b||this.layer.displayOutsideMaxExtent);
this.clear();
return this.shouldDraw
},moveTo:function(b,a,c){if(c==null){c=true
}this.bounds=b.clone();
this.position=a.clone();
if(c){this.draw()
}},clear:function(){},getBoundsFromBaseLayer:function(a){var f=OpenLayers.i18n("reprojectDeprecated",{layerName:this.layer.name});
OpenLayers.Console.warn(f);
var d=this.layer.map.getLonLatFromLayerPx(a);
var c=a.clone();
c.x+=this.size.w;
c.y+=this.size.h;
var b=this.layer.map.getLonLatFromLayerPx(c);
if(d.lon>b.lon){if(d.lon<0){d.lon=-180-(d.lon+180)
}else{b.lon=180+b.lon+180
}}var e=new OpenLayers.Bounds(d.lon,b.lat,b.lon,d.lat);
return e
},showTile:function(){if(this.shouldDraw){this.show()
}},show:function(){},hide:function(){},CLASS_NAME:"OpenLayers.Tile"});OpenLayers.Tile.Image=OpenLayers.Class(OpenLayers.Tile,{url:null,imgDiv:null,frame:null,layerAlphaHack:null,isBackBuffer:false,lastRatio:1,isFirstDraw:true,backBufferTile:null,initialize:function(d,a,e,b,c){OpenLayers.Tile.prototype.initialize.apply(this,arguments);
this.url=b;
this.frame=document.createElement("div");
this.frame.style.overflow="hidden";
this.frame.style.position="absolute";
this.layerAlphaHack=this.layer.alpha&&OpenLayers.Util.alphaHack()
},destroy:function(){if(this.imgDiv!=null){if(this.layerAlphaHack){OpenLayers.Event.stopObservingElement(this.imgDiv.childNodes[0])
}OpenLayers.Event.stopObservingElement(this.imgDiv);
if(this.imgDiv.parentNode==this.frame){this.frame.removeChild(this.imgDiv);
this.imgDiv.map=null
}this.imgDiv.urls=null;
this.imgDiv.src=OpenLayers.Util.getImagesLocation()+"blank.gif"
}this.imgDiv=null;
if((this.frame!=null)&&(this.frame.parentNode==this.layer.div)){this.layer.div.removeChild(this.frame)
}this.frame=null;
if(this.backBufferTile){this.backBufferTile.destroy();
this.backBufferTile=null
}this.layer.events.unregister("loadend",this,this.resetBackBuffer);
OpenLayers.Tile.prototype.destroy.apply(this,arguments)
},clone:function(a){if(a==null){a=new OpenLayers.Tile.Image(this.layer,this.position,this.bounds,this.url,this.size)
}a=OpenLayers.Tile.prototype.clone.apply(this,[a]);
a.imgDiv=null;
return a
},draw:function(){if(this.layer!=this.layer.map.baseLayer&&this.layer.reproject){this.bounds=this.getBoundsFromBaseLayer(this.position)
}var a=OpenLayers.Tile.prototype.draw.apply(this,arguments);
if(OpenLayers.Util.indexOf(this.layer.SUPPORTED_TRANSITIONS,this.layer.transitionEffect)!=-1){if(a){if(!this.backBufferTile){this.backBufferTile=this.clone();
this.backBufferTile.hide();
this.backBufferTile.isBackBuffer=true;
this.events.register("loadend",this,this.resetBackBuffer);
this.layer.events.register("loadend",this,this.resetBackBuffer)
}this.startTransition()
}else{if(this.backBufferTile){this.backBufferTile.clear()
}}}else{if(a&&this.isFirstDraw){this.events.register("loadend",this,this.showTile);
this.isFirstDraw=false
}}if(!a){return false
}if(this.isLoading){this.events.triggerEvent("reload")
}else{this.isLoading=true;
this.events.triggerEvent("loadstart")
}return this.renderTile()
},resetBackBuffer:function(){this.showTile();
if(this.backBufferTile&&(this.isFirstDraw||!this.layer.numLoadingTiles)){this.isFirstDraw=false;
var a=this.layer.maxExtent;
var b=(a&&this.bounds.intersectsBounds(a,false));
if(b){this.backBufferTile.position=this.position;
this.backBufferTile.bounds=this.bounds;
this.backBufferTile.size=this.size;
this.backBufferTile.imageSize=this.layer.getImageSize(this.bounds)||this.size;
this.backBufferTile.imageOffset=this.layer.imageOffset;
this.backBufferTile.resolution=this.layer.getResolution();
this.backBufferTile.renderTile()
}this.backBufferTile.hide()
}},renderTile:function(){if(this.imgDiv==null){this.initImgDiv()
}this.imgDiv.viewRequestID=this.layer.map.viewRequestID;
if(this.layer.async){this.layer.getURLasync(this.bounds,this,"url",this.positionImage)
}else{if(this.layer.url instanceof Array){this.imgDiv.urls=this.layer.url.slice()
}this.url=this.layer.getURL(this.bounds);
this.positionImage()
}return true
},positionImage:function(){if(this.layer==null){return
}OpenLayers.Util.modifyDOMElement(this.frame,null,this.position,this.size);
var a=this.layer.getImageSize(this.bounds);
if(this.layerAlphaHack){OpenLayers.Util.modifyAlphaImageDiv(this.imgDiv,null,null,a,this.url)
}else{OpenLayers.Util.modifyDOMElement(this.imgDiv,null,null,a);
this.imgDiv.src=this.url
}},clear:function(){if(this.imgDiv){this.hide();
if(OpenLayers.Tile.Image.useBlankTile){this.imgDiv.src=OpenLayers.Util.getImagesLocation()+"blank.gif"
}}},initImgDiv:function(){var d=this.layer.imageOffset;
var b=this.layer.getImageSize(this.bounds);
if(this.layerAlphaHack){this.imgDiv=OpenLayers.Util.createAlphaImageDiv(null,d,b,null,"relative",null,null,null,true)
}else{this.imgDiv=OpenLayers.Util.createImage(null,d,b,null,"relative",null,null,true)
}this.imgDiv.className="olTileImage";
this.frame.style.zIndex=this.isBackBuffer?0:1;
this.frame.appendChild(this.imgDiv);
this.layer.div.appendChild(this.frame);
if(this.layer.opacity!=null){OpenLayers.Util.modifyDOMElement(this.imgDiv,null,null,null,null,null,null,this.layer.opacity)
}this.imgDiv.map=this.layer.map;
var c=function(){if(this.isLoading){this.isLoading=false;
this.events.triggerEvent("loadend")
}};
if(this.layerAlphaHack){OpenLayers.Event.observe(this.imgDiv.childNodes[0],"load",OpenLayers.Function.bind(c,this))
}else{OpenLayers.Event.observe(this.imgDiv,"load",OpenLayers.Function.bind(c,this))
}var a=function(){if(this.imgDiv._attempts>OpenLayers.IMAGE_RELOAD_ATTEMPTS){c.call(this)
}};
OpenLayers.Event.observe(this.imgDiv,"error",OpenLayers.Function.bind(a,this))
},checkImgURL:function(){if(this.layer){var a=this.layerAlphaHack?this.imgDiv.firstChild.src:this.imgDiv.src;
if(!OpenLayers.Util.isEquivalentUrl(a,this.url)){this.hide()
}}},startTransition:function(){if(!this.backBufferTile||!this.backBufferTile.imgDiv){return
}var d=1;
if(this.backBufferTile.resolution){d=this.backBufferTile.resolution/this.layer.getResolution()
}if(d!=this.lastRatio){if(this.layer.transitionEffect=="resize"){var c=new OpenLayers.LonLat(this.backBufferTile.bounds.left,this.backBufferTile.bounds.top);
var b=new OpenLayers.Size(this.backBufferTile.size.w*d,this.backBufferTile.size.h*d);
var a=this.layer.map.getLayerPxFromLonLat(c);
OpenLayers.Util.modifyDOMElement(this.backBufferTile.frame,null,a,b);
var e=this.backBufferTile.imageSize;
e=new OpenLayers.Size(e.w*d,e.h*d);
var f=this.backBufferTile.imageOffset;
if(f){f=new OpenLayers.Pixel(f.x*d,f.y*d)
}OpenLayers.Util.modifyDOMElement(this.backBufferTile.imgDiv,null,f,e);
this.backBufferTile.show()
}}else{if(this.layer.singleTile){this.backBufferTile.show()
}else{this.backBufferTile.hide()
}}this.lastRatio=d
},show:function(){this.frame.style.display="";
if(OpenLayers.Util.indexOf(this.layer.SUPPORTED_TRANSITIONS,this.layer.transitionEffect)!=-1){if(navigator.userAgent.toLowerCase().indexOf("gecko")!=-1){this.frame.scrollLeft=this.frame.scrollLeft
}}},hide:function(){this.frame.style.display="none"
},CLASS_NAME:"OpenLayers.Tile.Image"});
OpenLayers.Tile.Image.useBlankTile=(OpenLayers.Util.getBrowserName()=="safari"||OpenLayers.Util.getBrowserName()=="opera");OpenLayers.Layer.Image=OpenLayers.Class(OpenLayers.Layer,{isBaseLayer:true,url:null,extent:null,size:null,tile:null,aspectRatio:null,initialize:function(c,b,e,d,a){this.url=b;
this.extent=e;
this.maxExtent=e;
this.size=d;
OpenLayers.Layer.prototype.initialize.apply(this,[c,a]);
this.aspectRatio=(this.extent.getHeight()/this.size.h)/(this.extent.getWidth()/this.size.w)
},destroy:function(){if(this.tile){this.removeTileMonitoringHooks(this.tile);
this.tile.destroy();
this.tile=null
}OpenLayers.Layer.prototype.destroy.apply(this,arguments)
},clone:function(a){if(a==null){a=new OpenLayers.Layer.Image(this.name,this.url,this.extent,this.size,this.getOptions())
}a=OpenLayers.Layer.prototype.clone.apply(this,[a]);
return a
},setMap:function(a){if(this.options.maxResolution==null){this.options.maxResolution=this.aspectRatio*this.extent.getWidth()/this.size.w
}OpenLayers.Layer.prototype.setMap.apply(this,arguments)
},moveTo:function(e,a,f){OpenLayers.Layer.prototype.moveTo.apply(this,arguments);
var b=(this.tile==null);
if(a||b){this.setTileSize();
var d=new OpenLayers.LonLat(this.extent.left,this.extent.top);
var c=this.map.getLayerPxFromLonLat(d);
if(b){this.tile=new OpenLayers.Tile.Image(this,c,this.extent,null,this.tileSize);
this.addTileMonitoringHooks(this.tile)
}else{this.tile.size=this.tileSize.clone();
this.tile.position=c.clone()
}this.tile.draw()
}},setTileSize:function(){var b=this.extent.getWidth()/this.map.getResolution();
var a=this.extent.getHeight()/this.map.getResolution();
this.tileSize=new OpenLayers.Size(b,a)
},addTileMonitoringHooks:function(a){a.onLoadStart=function(){this.events.triggerEvent("loadstart")
};
a.events.register("loadstart",this,a.onLoadStart);
a.onLoadEnd=function(){this.events.triggerEvent("loadend")
};
a.events.register("loadend",this,a.onLoadEnd);
a.events.register("unload",this,a.onLoadEnd)
},removeTileMonitoringHooks:function(a){a.unload();
a.events.un({loadstart:a.onLoadStart,loadend:a.onLoadEnd,unload:a.onLoadEnd,scope:this})
},setUrl:function(a){this.url=a;
this.tile.draw()
},getURL:function(a){return this.url
},CLASS_NAME:"OpenLayers.Layer.Image"});OpenLayers.Geometry.Collection=OpenLayers.Class(OpenLayers.Geometry,{components:null,componentTypes:null,initialize:function(a){OpenLayers.Geometry.prototype.initialize.apply(this,arguments);
this.components=[];
if(a!=null){this.addComponents(a)
}},destroy:function(){this.components.length=0;
this.components=null
},clone:function(){var geometry=eval("new "+this.CLASS_NAME+"()");
for(var i=0,len=this.components.length;
i<len;
i++){geometry.addComponent(this.components[i].clone())
}OpenLayers.Util.applyDefaults(geometry,this);
return geometry
},getComponentsString:function(){var b=[];
for(var c=0,a=this.components.length;
c<a;
c++){b.push(this.components[c].toShortString())
}return b.join(",")
},calculateBounds:function(){this.bounds=null;
if(this.components&&this.components.length>0){this.setBounds(this.components[0].getBounds());
for(var b=1,a=this.components.length;
b<a;
b++){this.extendBounds(this.components[b].getBounds())
}}},addComponents:function(c){if(!(c instanceof Array)){c=[c]
}for(var b=0,a=c.length;
b<a;
b++){this.addComponent(c[b])
}},addComponent:function(b,a){var d=false;
if(b){if(this.componentTypes==null||(OpenLayers.Util.indexOf(this.componentTypes,b.CLASS_NAME)>-1)){if(a!=null&&(a<this.components.length)){var e=this.components.slice(0,a);
var c=this.components.slice(a,this.components.length);
e.push(b);
this.components=e.concat(c)
}else{this.components.push(b)
}b.parent=this;
this.clearBounds();
d=true
}}return d
},removeComponents:function(b){if(!(b instanceof Array)){b=[b]
}for(var a=b.length-1;
a>=0;
--a){this.removeComponent(b[a])
}},removeComponent:function(a){OpenLayers.Util.removeItem(this.components,a);
this.clearBounds()
},getLength:function(){var c=0;
for(var b=0,a=this.components.length;
b<a;
b++){c+=this.components[b].getLength()
}return c
},getArea:function(){var c=0;
for(var b=0,a=this.components.length;
b<a;
b++){c+=this.components[b].getArea()
}return c
},getGeodesicArea:function(b){var d=0;
for(var c=0,a=this.components.length;
c<a;
c++){d+=this.components[c].getGeodesicArea(b)
}return d
},getCentroid:function(){return this.components.length&&this.components[0].getCentroid()
},getGeodesicLength:function(b){var d=0;
for(var c=0,a=this.components.length;
c<a;
c++){d+=this.components[c].getGeodesicLength(b)
}return d
},move:function(b,d){for(var c=0,a=this.components.length;
c<a;
c++){this.components[c].move(b,d)
}},rotate:function(d,b){for(var c=0,a=this.components.length;
c<a;
++c){this.components[c].rotate(d,b)
}},resize:function(d,a,c){for(var b=0;
b<this.components.length;
++b){this.components[b].resize(d,a,c)
}return this
},distanceTo:function(h,j){var c=!(j&&j.edge===false);
var a=c&&j&&j.details;
var k,d,b;
var e=Number.POSITIVE_INFINITY;
for(var f=0,g=this.components.length;
f<g;
++f){k=this.components[f].distanceTo(h,j);
b=a?k.distance:k;
if(b<e){e=b;
d=k;
if(e==0){break
}}}return d
},equals:function(d){var b=true;
if(!d||!d.CLASS_NAME||(this.CLASS_NAME!=d.CLASS_NAME)){b=false
}else{if(!(d.components instanceof Array)||(d.components.length!=this.components.length)){b=false
}else{for(var c=0,a=this.components.length;
c<a;
++c){if(!this.components[c].equals(d.components[c])){b=false;
break
}}}}return b
},transform:function(e,c){if(e&&c){for(var d=0,a=this.components.length;
d<a;
d++){var b=this.components[d];
b.transform(e,c)
}this.bounds=null
}return this
},intersects:function(d){var b=false;
for(var c=0,a=this.components.length;
c<a;
++c){b=d.intersects(this.components[c]);
if(b){break
}}return b
},getVertices:function(b){var c=[];
for(var d=0,a=this.components.length;
d<a;
++d){Array.prototype.push.apply(c,this.components[d].getVertices(b))
}return c
},CLASS_NAME:"OpenLayers.Geometry.Collection"});OpenLayers.Geometry.Point=OpenLayers.Class(OpenLayers.Geometry,{x:null,y:null,initialize:function(a,b){OpenLayers.Geometry.prototype.initialize.apply(this,arguments);
this.x=parseFloat(a);
this.y=parseFloat(b)
},clone:function(a){if(a==null){a=new OpenLayers.Geometry.Point(this.x,this.y)
}OpenLayers.Util.applyDefaults(a,this);
return a
},calculateBounds:function(){this.bounds=new OpenLayers.Bounds(this.x,this.y,this.x,this.y)
},distanceTo:function(f,j){var d=!(j&&j.edge===false);
var a=d&&j&&j.details;
var b,e,h,c,g,i;
if(f instanceof OpenLayers.Geometry.Point){e=this.x;
h=this.y;
c=f.x;
g=f.y;
b=Math.sqrt(Math.pow(e-c,2)+Math.pow(h-g,2));
i=!a?b:{x0:e,y0:h,x1:c,y1:g,distance:b}
}else{i=f.distanceTo(this,j);
if(a){i={x0:i.x1,y0:i.y1,x1:i.x0,y1:i.y0,distance:i.distance}
}}return i
},equals:function(a){var b=false;
if(a!=null){b=((this.x==a.x&&this.y==a.y)||(isNaN(this.x)&&isNaN(this.y)&&isNaN(a.x)&&isNaN(a.y)))
}return b
},toShortString:function(){return(this.x+", "+this.y)
},move:function(a,b){this.x=this.x+a;
this.y=this.y+b;
this.clearBounds()
},rotate:function(d,b){d*=Math.PI/180;
var a=this.distanceTo(b);
var c=d+Math.atan2(this.y-b.y,this.x-b.x);
this.x=b.x+(a*Math.cos(c));
this.y=b.y+(a*Math.sin(c));
this.clearBounds()
},getCentroid:function(){return new OpenLayers.Geometry.Point(this.x,this.y)
},resize:function(c,a,b){b=(b==undefined)?1:b;
this.x=a.x+(c*b*(this.x-a.x));
this.y=a.y+(c*(this.y-a.y));
this.clearBounds();
return this
},intersects:function(b){var a=false;
if(b.CLASS_NAME=="OpenLayers.Geometry.Point"){a=this.equals(b)
}else{a=b.intersects(this)
}return a
},transform:function(b,a){if((b&&a)){OpenLayers.Projection.transform(this,b,a);
this.bounds=null
}return this
},getVertices:function(a){return[this]
},CLASS_NAME:"OpenLayers.Geometry.Point"});OpenLayers.Geometry.MultiPoint=OpenLayers.Class(OpenLayers.Geometry.Collection,{componentTypes:["OpenLayers.Geometry.Point"],initialize:function(a){OpenLayers.Geometry.Collection.prototype.initialize.apply(this,arguments)
},addPoint:function(a,b){this.addComponent(a,b)
},removePoint:function(a){this.removeComponent(a)
},CLASS_NAME:"OpenLayers.Geometry.MultiPoint"});OpenLayers.Geometry.Curve=OpenLayers.Class(OpenLayers.Geometry.MultiPoint,{componentTypes:["OpenLayers.Geometry.Point"],initialize:function(a){OpenLayers.Geometry.MultiPoint.prototype.initialize.apply(this,arguments)
},getLength:function(){var c=0;
if(this.components&&(this.components.length>1)){for(var b=1,a=this.components.length;
b<a;
b++){c+=this.components[b-1].distanceTo(this.components[b])
}}return c
},getGeodesicLength:function(b){var e=this;
if(b){var c=new OpenLayers.Projection("EPSG:4326");
if(!c.equals(b)){e=this.clone().transform(b,c)
}}var f=0;
if(e.components&&(e.components.length>1)){var h,g;
for(var d=1,a=e.components.length;
d<a;
d++){h=e.components[d-1];
g=e.components[d];
f+=OpenLayers.Util.distVincenty({lon:h.x,lat:h.y},{lon:g.x,lat:g.y})
}}return f*1000
},CLASS_NAME:"OpenLayers.Geometry.Curve"});OpenLayers.Geometry.LineString=OpenLayers.Class(OpenLayers.Geometry.Curve,{initialize:function(a){OpenLayers.Geometry.Curve.prototype.initialize.apply(this,arguments)
},removeComponent:function(a){if(this.components&&(this.components.length>2)){OpenLayers.Geometry.Collection.prototype.removeComponent.apply(this,arguments)
}},intersects:function(m){var c=false;
var l=m.CLASS_NAME;
if(l=="OpenLayers.Geometry.LineString"||l=="OpenLayers.Geometry.LinearRing"||l=="OpenLayers.Geometry.Point"){var p=this.getSortedSegments();
var n;
if(l=="OpenLayers.Geometry.Point"){n=[{x1:m.x,y1:m.y,x2:m.x,y2:m.y}]
}else{n=m.getSortedSegments()
}var s,g,e,a,r,q,d,b;
outer:for(var h=0,k=p.length;
h<k;
++h){s=p[h];
g=s.x1;
e=s.x2;
a=s.y1;
r=s.y2;
inner:for(var f=0,o=n.length;
f<o;
++f){q=n[f];
if(q.x1>e){break
}if(q.x2<g){continue
}d=q.y1;
b=q.y2;
if(Math.min(d,b)>Math.max(a,r)){continue
}if(Math.max(d,b)<Math.min(a,r)){continue
}if(OpenLayers.Geometry.segmentsIntersect(s,q)){c=true;
break outer
}}}}else{c=m.intersects(this)
}return c
},getSortedSegments:function(){var a=this.components.length-1;
var b=new Array(a),e,d;
for(var c=0;
c<a;
++c){e=this.components[c];
d=this.components[c+1];
if(e.x<d.x){b[c]={x1:e.x,y1:e.y,x2:d.x,y2:d.y}
}else{b[c]={x1:d.x,y1:d.y,x2:e.x,y2:e.y}
}}function f(h,g){return h.x1-g.x1
}return b.sort(f)
},splitWithSegment:function(r,b){var c=!(b&&b.edge===false);
var o=b&&b.tolerance;
var a=[];
var t=this.getVertices();
var n=[];
var v=[];
var h=false;
var e,d,l;
var j,q,u;
var f={point:true,tolerance:o};
var g=null;
for(var m=0,k=t.length-2;
m<=k;
++m){e=t[m];
n.push(e.clone());
d=t[m+1];
u={x1:e.x,y1:e.y,x2:d.x,y2:d.y};
l=OpenLayers.Geometry.segmentsIntersect(r,u,f);
if(l instanceof OpenLayers.Geometry.Point){if((l.x===r.x1&&l.y===r.y1)||(l.x===r.x2&&l.y===r.y2)||l.equals(e)||l.equals(d)){q=true
}else{q=false
}if(q||c){if(!l.equals(v[v.length-1])){v.push(l.clone())
}if(m===0){if(l.equals(e)){continue
}}if(l.equals(d)){continue
}h=true;
if(!l.equals(e)){n.push(l)
}a.push(new OpenLayers.Geometry.LineString(n));
n=[l.clone()]
}}}if(h){n.push(d.clone());
a.push(new OpenLayers.Geometry.LineString(n))
}if(v.length>0){var p=r.x1<r.x2?1:-1;
var s=r.y1<r.y2?1:-1;
g={lines:a,points:v.sort(function(w,i){return(p*w.x-p*i.x)||(s*w.y-s*i.y)
})}
}return g
},split:function(x,b){var n=null;
var d=b&&b.mutual;
var l,e,m,c;
if(x instanceof OpenLayers.Geometry.LineString){var w=this.getVertices();
var g,f,v,h,a,p;
var s=[];
m=[];
for(var t=0,o=w.length-2;
t<=o;
++t){g=w[t];
f=w[t+1];
v={x1:g.x,y1:g.y,x2:f.x,y2:f.y};
c=c||[x];
if(d){s.push(g.clone())
}for(var r=0;
r<c.length;
++r){h=c[r].splitWithSegment(v,b);
if(h){a=h.lines;
if(a.length>0){a.unshift(r,1);
Array.prototype.splice.apply(c,a);
r+=a.length-2
}if(d){for(var q=0,u=h.points.length;
q<u;
++q){p=h.points[q];
if(!p.equals(g)){s.push(p);
m.push(new OpenLayers.Geometry.LineString(s));
if(p.equals(f)){s=[]
}else{s=[p.clone()]
}}}}}}}if(d&&m.length>0&&s.length>0){s.push(f.clone());
m.push(new OpenLayers.Geometry.LineString(s))
}}else{n=x.splitWith(this,b)
}if(c&&c.length>1){e=true
}else{c=[]
}if(m&&m.length>1){l=true
}else{m=[]
}if(e||l){if(d){n=[m,c]
}else{n=c
}}return n
},splitWith:function(b,a){return b.split(this,a)
},getVertices:function(a){var b;
if(a===true){b=[this.components[0],this.components[this.components.length-1]]
}else{if(a===false){b=this.components.slice(1,this.components.length-1)
}else{b=this.components.slice()
}}return b
},distanceTo:function(h,g){var k=!(g&&g.edge===false);
var B=k&&g&&g.details;
var q,e={};
var t=Number.POSITIVE_INFINITY;
if(h instanceof OpenLayers.Geometry.Point){var r=this.getSortedSegments();
var p=h.x;
var o=h.y;
var z;
for(var v=0,w=r.length;
v<w;
++v){z=r[v];
q=OpenLayers.Geometry.distanceToSegment(h,z);
if(q.distance<t){t=q.distance;
e=q;
if(t===0){break
}}else{if(z.x2>p&&((o>z.y1&&o<z.y2)||(o<z.y1&&o>z.y2))){break
}}}if(B){e={distance:e.distance,x0:e.x,y0:e.y,x1:p,y1:o}
}else{e=e.distance
}}else{if(h instanceof OpenLayers.Geometry.LineString){var d=this.getSortedSegments();
var c=h.getSortedSegments();
var b,a,n,A,f;
var m=c.length;
var l={point:true};
outer:for(var v=0,w=d.length;
v<w;
++v){b=d[v];
A=b.x1;
f=b.y1;
for(var u=0;
u<m;
++u){a=c[u];
n=OpenLayers.Geometry.segmentsIntersect(b,a,l);
if(n){t=0;
e={distance:0,x0:n.x,y0:n.y,x1:n.x,y1:n.y};
break outer
}else{q=OpenLayers.Geometry.distanceToSegment({x:A,y:f},a);
if(q.distance<t){t=q.distance;
e={distance:t,x0:A,y0:f,x1:q.x,y1:q.y}
}}}}if(!B){e=e.distance
}if(t!==0){if(b){q=h.distanceTo(new OpenLayers.Geometry.Point(b.x2,b.y2),g);
var s=B?q.distance:q;
if(s<t){if(B){e={distance:t,x0:q.x1,y0:q.y1,x1:q.x0,y1:q.y0}
}else{e=s
}}}}}else{e=h.distanceTo(this,g);
if(B){e={distance:e.distance,x0:e.x1,y0:e.y1,x1:e.x0,y1:e.y0}
}}}return e
},CLASS_NAME:"OpenLayers.Geometry.LineString"});OpenLayers.Geometry.LinearRing=OpenLayers.Class(OpenLayers.Geometry.LineString,{componentTypes:["OpenLayers.Geometry.Point"],initialize:function(a){OpenLayers.Geometry.LineString.prototype.initialize.apply(this,arguments)
},addComponent:function(a,b){var c=false;
var d=this.components.pop();
if(b!=null||!a.equals(d)){c=OpenLayers.Geometry.Collection.prototype.addComponent.apply(this,arguments)
}var e=this.components[0];
OpenLayers.Geometry.Collection.prototype.addComponent.apply(this,[e]);
return c
},removeComponent:function(a){if(this.components.length>4){this.components.pop();
OpenLayers.Geometry.Collection.prototype.removeComponent.apply(this,arguments);
var b=this.components[0];
OpenLayers.Geometry.Collection.prototype.addComponent.apply(this,[b])
}},move:function(b,d){for(var c=0,a=this.components.length;
c<a-1;
c++){this.components[c].move(b,d)
}},rotate:function(d,b){for(var c=0,a=this.components.length;
c<a-1;
++c){this.components[c].rotate(d,b)
}},resize:function(e,b,d){for(var c=0,a=this.components.length;
c<a-1;
++c){this.components[c].resize(e,b,d)
}return this
},transform:function(e,c){if(e&&c){for(var d=0,a=this.components.length;
d<a-1;
d++){var b=this.components[d];
b.transform(e,c)
}this.bounds=null
}return this
},getCentroid:function(){if(this.components&&(this.components.length>2)){var h=0;
var g=0;
for(var e=0;
e<this.components.length-1;
e++){var d=this.components[e];
var k=this.components[e+1];
h+=(d.x+k.x)*(d.x*k.y-k.x*d.y);
g+=(d.y+k.y)*(d.x*k.y-k.x*d.y)
}var f=-1*this.getArea();
var a=h/(6*f);
var j=g/(6*f);
return new OpenLayers.Geometry.Point(a,j)
}else{return null
}},getArea:function(){var g=0;
if(this.components&&(this.components.length>2)){var f=0;
for(var e=0,d=this.components.length;
e<d-1;
e++){var a=this.components[e];
var h=this.components[e+1];
f+=(a.x+h.x)*(h.y-a.y)
}g=-f/2
}return g
},getGeodesicArea:function(b){var d=this;
if(b){var c=new OpenLayers.Projection("EPSG:4326");
if(!c.equals(b)){d=this.clone().transform(b,c)
}}var f=0;
var a=d.components&&d.components.length;
if(a>2){var h,g;
for(var e=0;
e<a-1;
e++){h=d.components[e];
g=d.components[e+1];
f+=OpenLayers.Util.rad(g.x-h.x)*(2+Math.sin(OpenLayers.Util.rad(h.y))+Math.sin(OpenLayers.Util.rad(g.y)))
}f=f*6378137*6378137/2
}return f
},containsPoint:function(m){var s=OpenLayers.Number.limitSigDigs;
var l=14;
var k=s(m.x,l);
var j=s(m.y,l);
function r(w,t,v,i,u){return(((t-i)*w)+((i*v)-(t*u)))/(v-u)
}var a=this.components.length-1;
var g,f,q,d,o,b,e,c;
var h=0;
for(var n=0;
n<a;
++n){g=this.components[n];
q=s(g.x,l);
d=s(g.y,l);
f=this.components[n+1];
o=s(f.x,l);
b=s(f.y,l);
if(d==b){if(j==d){if(q<=o&&(k>=q&&k<=o)||q>=o&&(k<=q&&k>=o)){h=-1;
break
}}continue
}e=s(r(j,q,d,o,b),l);
if(e==k){if(d<b&&(j>=d&&j<=b)||d>b&&(j<=d&&j>=b)){h=-1;
break
}}if(e<=k){continue
}if(q!=o&&(e<Math.min(q,o)||e>Math.max(q,o))){continue
}if(d<b&&(j>=d&&j<b)||d>b&&(j<d&&j>=b)){++h
}}var p=(h==-1)?1:!!(h&1);
return p
},intersects:function(d){var b=false;
if(d.CLASS_NAME=="OpenLayers.Geometry.Point"){b=this.containsPoint(d)
}else{if(d.CLASS_NAME=="OpenLayers.Geometry.LineString"){b=d.intersects(this)
}else{if(d.CLASS_NAME=="OpenLayers.Geometry.LinearRing"){b=OpenLayers.Geometry.LineString.prototype.intersects.apply(this,[d])
}else{for(var c=0,a=d.components.length;
c<a;
++c){b=d.components[c].intersects(this);
if(b){break
}}}}}return b
},getVertices:function(a){return(a===true)?[]:this.components.slice(0,this.components.length-1)
},CLASS_NAME:"OpenLayers.Geometry.LinearRing"});OpenLayers.Handler=OpenLayers.Class({id:null,control:null,map:null,keyMask:null,active:false,evt:null,initialize:function(c,b,a){OpenLayers.Util.extend(this,a);
this.control=c;
this.callbacks=b;
if(c.map){this.setMap(c.map)
}OpenLayers.Util.extend(this,a);
this.id=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_")
},setMap:function(a){this.map=a
},checkModifiers:function(a){if(this.keyMask==null){return true
}var b=(a.shiftKey?OpenLayers.Handler.MOD_SHIFT:0)|(a.ctrlKey?OpenLayers.Handler.MOD_CTRL:0)|(a.altKey?OpenLayers.Handler.MOD_ALT:0);
return(b==this.keyMask)
},activate:function(){if(this.active){return false
}var c=OpenLayers.Events.prototype.BROWSER_EVENTS;
for(var b=0,a=c.length;
b<a;
b++){if(this[c[b]]){this.register(c[b],this[c[b]])
}}this.active=true;
return true
},deactivate:function(){if(!this.active){return false
}var c=OpenLayers.Events.prototype.BROWSER_EVENTS;
for(var b=0,a=c.length;
b<a;
b++){if(this[c[b]]){this.unregister(c[b],this[c[b]])
}}this.active=false;
return true
},callback:function(b,a){if(b&&this.callbacks[b]){this.callbacks[b].apply(this.control,a)
}},register:function(a,b){this.map.events.registerPriority(a,this,b);
this.map.events.registerPriority(a,this,this.setEvent)
},unregister:function(a,b){this.map.events.unregister(a,this,b);
this.map.events.unregister(a,this,this.setEvent)
},setEvent:function(a){this.evt=a;
return true
},destroy:function(){this.deactivate();
this.control=this.map=null
},CLASS_NAME:"OpenLayers.Handler"});
OpenLayers.Handler.MOD_NONE=0;
OpenLayers.Handler.MOD_SHIFT=1;
OpenLayers.Handler.MOD_CTRL=2;
OpenLayers.Handler.MOD_ALT=4;OpenLayers.Handler.Point=OpenLayers.Class(OpenLayers.Handler,{point:null,layer:null,multi:false,drawing:false,mouseDown:false,lastDown:null,lastUp:null,persist:false,layerOptions:null,initialize:function(c,b,a){if(!(a&&a.layerOptions&&a.layerOptions.styleMap)){this.style=OpenLayers.Util.extend(OpenLayers.Feature.Vector.style["default"],{})
}OpenLayers.Handler.prototype.initialize.apply(this,arguments)
},activate:function(){if(!OpenLayers.Handler.prototype.activate.apply(this,arguments)){return false
}var a=OpenLayers.Util.extend({displayInLayerSwitcher:false,calculateInRange:OpenLayers.Function.True},this.layerOptions);
this.layer=new OpenLayers.Layer.Vector(this.CLASS_NAME,a);
this.map.addLayer(this.layer);
return true
},createFeature:function(a){var b=this.map.getLonLatFromPixel(a);
this.point=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(b.lon,b.lat));
this.callback("create",[this.point.geometry,this.point]);
this.point.geometry.clearBounds();
this.layer.addFeatures([this.point],{silent:true})
},deactivate:function(){if(!OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){return false
}if(this.drawing){this.cancel()
}this.destroyFeature();
if(this.layer.map!=null){this.layer.destroy(false)
}this.layer=null;
return true
},destroyFeature:function(){if(this.layer){this.layer.destroyFeatures()
}this.point=null
},finalize:function(b){var a=b?"cancel":"done";
this.drawing=false;
this.mouseDown=false;
this.lastDown=null;
this.lastUp=null;
this.callback(a,[this.geometryClone()]);
if(b||!this.persist){this.destroyFeature()
}},cancel:function(){this.finalize(true)
},click:function(a){OpenLayers.Event.stop(a);
return false
},dblclick:function(a){OpenLayers.Event.stop(a);
return false
},modifyFeature:function(a){var b=this.map.getLonLatFromPixel(a);
this.point.geometry.x=b.lon;
this.point.geometry.y=b.lat;
this.callback("modify",[this.point.geometry,this.point]);
this.point.geometry.clearBounds();
this.drawFeature()
},drawFeature:function(){this.layer.drawFeature(this.point,this.style)
},getGeometry:function(){var a=this.point&&this.point.geometry;
if(a&&this.multi){a=new OpenLayers.Geometry.MultiPoint([a])
}return a
},geometryClone:function(){var a=this.getGeometry();
return a&&a.clone()
},mousedown:function(a){if(!this.checkModifiers(a)){return true
}if(this.lastDown&&this.lastDown.equals(a.xy)){return true
}this.drawing=true;
if(this.lastDown==null){if(this.persist){this.destroyFeature()
}this.createFeature(a.xy)
}else{this.modifyFeature(a.xy)
}this.lastDown=a.xy;
return false
},mousemove:function(a){if(this.drawing){this.modifyFeature(a.xy)
}return true
},mouseup:function(a){if(this.drawing){this.finalize();
return false
}else{return true
}},CLASS_NAME:"OpenLayers.Handler.Point"});OpenLayers.Handler.Path=OpenLayers.Class(OpenLayers.Handler.Point,{line:null,freehand:false,freehandToggle:"shiftKey",initialize:function(c,b,a){OpenLayers.Handler.Point.prototype.initialize.apply(this,arguments)
},createFeature:function(a){var b=this.control.map.getLonLatFromPixel(a);
this.point=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(b.lon,b.lat));
this.line=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([this.point.geometry]));
this.callback("create",[this.point.geometry,this.getSketch()]);
this.point.geometry.clearBounds();
this.layer.addFeatures([this.line,this.point],{silent:true})
},destroyFeature:function(){OpenLayers.Handler.Point.prototype.destroyFeature.apply(this);
this.line=null
},removePoint:function(){if(this.point){this.layer.removeFeatures([this.point])
}},addPoint:function(a){this.layer.removeFeatures([this.point]);
var b=this.control.map.getLonLatFromPixel(a);
this.point=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(b.lon,b.lat));
this.line.geometry.addComponent(this.point.geometry,this.line.geometry.components.length);
this.callback("point",[this.point.geometry,this.getGeometry()]);
this.callback("modify",[this.point.geometry,this.getSketch()]);
this.drawFeature()
},freehandMode:function(a){return(this.freehandToggle&&a[this.freehandToggle])?!this.freehand:this.freehand
},modifyFeature:function(a){var b=this.control.map.getLonLatFromPixel(a);
this.point.geometry.x=b.lon;
this.point.geometry.y=b.lat;
this.callback("modify",[this.point.geometry,this.getSketch()]);
this.point.geometry.clearBounds();
this.drawFeature()
},drawFeature:function(){this.layer.drawFeature(this.line,this.style);
this.layer.drawFeature(this.point,this.style)
},getSketch:function(){return this.line
},getGeometry:function(){var a=this.line&&this.line.geometry;
if(a&&this.multi){a=new OpenLayers.Geometry.MultiLineString([a])
}return a
},mousedown:function(a){if(this.lastDown&&this.lastDown.equals(a.xy)){return false
}if(this.lastDown==null){if(this.persist){this.destroyFeature()
}this.createFeature(a.xy)
}else{if((this.lastUp==null)||!this.lastUp.equals(a.xy)){this.addPoint(a.xy)
}}this.mouseDown=true;
this.lastDown=a.xy;
this.drawing=true;
return false
},mousemove:function(a){if(this.drawing){if(this.mouseDown&&this.freehandMode(a)){this.addPoint(a.xy)
}else{this.modifyFeature(a.xy)
}}return true
},mouseup:function(a){this.mouseDown=false;
if(this.drawing){if(this.freehandMode(a)){this.removePoint();
this.finalize()
}else{if(this.lastUp==null){this.addPoint(a.xy)
}this.lastUp=a.xy
}return false
}return true
},dblclick:function(a){if(!this.freehandMode(a)){var b=this.line.geometry.components.length-1;
this.line.geometry.removeComponent(this.line.geometry.components[b]);
this.removePoint();
this.finalize()
}return false
},CLASS_NAME:"OpenLayers.Handler.Path"});OpenLayers.Geometry.Polygon=OpenLayers.Class(OpenLayers.Geometry.Collection,{componentTypes:["OpenLayers.Geometry.LinearRing"],initialize:function(a){OpenLayers.Geometry.Collection.prototype.initialize.apply(this,arguments)
},getArea:function(){var c=0;
if(this.components&&(this.components.length>0)){c+=Math.abs(this.components[0].getArea());
for(var b=1,a=this.components.length;
b<a;
b++){c-=Math.abs(this.components[b].getArea())
}}return c
},getGeodesicArea:function(b){var d=0;
if(this.components&&(this.components.length>0)){d+=Math.abs(this.components[0].getGeodesicArea(b));
for(var c=1,a=this.components.length;
c<a;
c++){d-=Math.abs(this.components[c].getGeodesicArea(b))
}}return d
},containsPoint:function(a){var e=this.components.length;
var c=false;
if(e>0){c=this.components[0].containsPoint(a);
if(c!==1){if(c&&e>1){var d;
for(var b=1;
b<e;
++b){d=this.components[b].containsPoint(a);
if(d){if(d===1){c=1
}else{c=false
}break
}}}}}return c
},intersects:function(e){var b=false;
var d,a;
if(e.CLASS_NAME=="OpenLayers.Geometry.Point"){b=this.containsPoint(e)
}else{if(e.CLASS_NAME=="OpenLayers.Geometry.LineString"||e.CLASS_NAME=="OpenLayers.Geometry.LinearRing"){for(d=0,a=this.components.length;
d<a;
++d){b=e.intersects(this.components[d]);
if(b){break
}}if(!b){for(d=0,a=e.components.length;
d<a;
++d){b=this.containsPoint(e.components[d]);
if(b){break
}}}}else{for(d=0,a=e.components.length;
d<a;
++d){b=this.intersects(e.components[d]);
if(b){break
}}}}if(!b&&e.CLASS_NAME=="OpenLayers.Geometry.Polygon"){var c=this.components[0];
for(d=0,a=c.components.length;
d<a;
++d){b=e.containsPoint(c.components[d]);
if(b){break
}}}return b
},distanceTo:function(d,b){var c=!(b&&b.edge===false);
var a;
if(!c&&this.intersects(d)){a=0
}else{a=OpenLayers.Geometry.Collection.prototype.distanceTo.apply(this,[d,b])
}return a
},CLASS_NAME:"OpenLayers.Geometry.Polygon"});
OpenLayers.Geometry.Polygon.createRegularPolygon=function(j,f,b,l){var c=Math.PI*((1/b)-(1/2));
if(l){c+=(l/180)*Math.PI
}var a,h,g;
var k=[];
for(var e=0;
e<b;
++e){a=c+(e*2*Math.PI/b);
h=j.x+(f*Math.cos(a));
g=j.y+(f*Math.sin(a));
k.push(new OpenLayers.Geometry.Point(h,g))
}var d=new OpenLayers.Geometry.LinearRing(k);
return new OpenLayers.Geometry.Polygon([d])
};OpenLayers.Handler.Polygon=OpenLayers.Class(OpenLayers.Handler.Path,{polygon:null,initialize:function(c,b,a){OpenLayers.Handler.Path.prototype.initialize.apply(this,arguments)
},createFeature:function(a){var b=this.control.map.getLonLatFromPixel(a);
this.point=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(b.lon,b.lat));
this.line=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LinearRing([this.point.geometry]));
this.polygon=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([this.line.geometry]));
this.callback("create",[this.point.geometry,this.getSketch()]);
this.point.geometry.clearBounds();
this.layer.addFeatures([this.polygon,this.point],{silent:true})
},destroyFeature:function(){OpenLayers.Handler.Path.prototype.destroyFeature.apply(this);
this.polygon=null
},drawFeature:function(){this.layer.drawFeature(this.polygon,this.style);
this.layer.drawFeature(this.point,this.style)
},getSketch:function(){return this.polygon
},getGeometry:function(){var a=this.polygon&&this.polygon.geometry;
if(a&&this.multi){a=new OpenLayers.Geometry.MultiPolygon([a])
}return a
},dblclick:function(a){if(!this.freehandMode(a)){var b=this.line.geometry.components.length-2;
this.line.geometry.removeComponent(this.line.geometry.components[b]);
this.removePoint();
this.finalize()
}return false
},CLASS_NAME:"OpenLayers.Handler.Polygon"});OpenLayers.Geometry.MultiLineString=OpenLayers.Class(OpenLayers.Geometry.Collection,{componentTypes:["OpenLayers.Geometry.LineString"],initialize:function(a){OpenLayers.Geometry.Collection.prototype.initialize.apply(this,arguments)
},split:function(n,s){var g=null;
var r=s&&s.mutual;
var o,a,q,m,b;
var e=[];
var p=[n];
for(var f=0,h=this.components.length;
f<h;
++f){a=this.components[f];
m=false;
for(var d=0;
d<p.length;
++d){o=a.split(p[d],s);
if(o){if(r){q=o[0];
for(var c=0,l=q.length;
c<l;
++c){if(c===0&&e.length){e[e.length-1].addComponent(q[c])
}else{e.push(new OpenLayers.Geometry.MultiLineString([q[c]]))
}}m=true;
o=o[1]
}if(o.length){o.unshift(d,1);
Array.prototype.splice.apply(p,o);
break
}}}if(!m){if(e.length){e[e.length-1].addComponent(a.clone())
}else{e=[new OpenLayers.Geometry.MultiLineString(a.clone())]
}}}if(e&&e.length>1){m=true
}else{e=[]
}if(p&&p.length>1){b=true
}else{p=[]
}if(m||b){if(r){g=[e,p]
}else{g=p
}}return g
},splitWith:function(n,s){var g=null;
var r=s&&s.mutual;
var o,c,q,m,a,e,p;
if(n instanceof OpenLayers.Geometry.LineString){p=[];
e=[n];
for(var f=0,h=this.components.length;
f<h;
++f){a=false;
c=this.components[f];
for(var d=0;
d<e.length;
++d){o=e[d].split(c,s);
if(o){if(r){q=o[0];
if(q.length){q.unshift(d,1);
Array.prototype.splice.apply(e,q);
d+=q.length-2
}o=o[1];
if(o.length===0){o=[c.clone()]
}}for(var b=0,l=o.length;
b<l;
++b){if(b===0&&p.length){p[p.length-1].addComponent(o[b])
}else{p.push(new OpenLayers.Geometry.MultiLineString([o[b]]))
}}a=true
}}if(!a){if(p.length){p[p.length-1].addComponent(c.clone())
}else{p=[new OpenLayers.Geometry.MultiLineString([c.clone()])]
}}}}else{g=n.split(this)
}if(e&&e.length>1){m=true
}else{e=[]
}if(p&&p.length>1){a=true
}else{p=[]
}if(m||a){if(r){g=[e,p]
}else{g=p
}}return g
},CLASS_NAME:"OpenLayers.Geometry.MultiLineString"});OpenLayers.Geometry.MultiPolygon=OpenLayers.Class(OpenLayers.Geometry.Collection,{componentTypes:["OpenLayers.Geometry.Polygon"],initialize:function(a){OpenLayers.Geometry.Collection.prototype.initialize.apply(this,arguments)
},CLASS_NAME:"OpenLayers.Geometry.MultiPolygon"});OpenLayers.Format.GML=OpenLayers.Class(OpenLayers.Format.XML,{featureNS:"http://mapserver.gis.umn.edu/mapserver",featurePrefix:"feature",featureName:"featureMember",layerName:"features",geometryName:"geometry",collectionName:"FeatureCollection",gmlns:"http://www.opengis.net/gml",extractAttributes:true,xy:true,initialize:function(a){this.regExes={trimSpace:(/^\s*|\s*$/g),removeSpace:(/\s*/g),splitSpace:(/\s+/),trimComma:(/\s*,\s*/g)};
OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},read:function(d){if(typeof d=="string"){d=OpenLayers.Format.XML.prototype.read.apply(this,[d])
}var e=this.getElementsByTagNameNS(d.documentElement,this.gmlns,this.featureName);
var c=[];
for(var b=0;
b<e.length;
b++){var a=this.parseFeature(e[b]);
if(a){c.push(a)
}}return c
},parseFeature:function(d){var e=["MultiPolygon","Polygon","MultiLineString","LineString","MultiPoint","Point","Envelope","Box"];
var k,g,l,b;
for(var j=0;
j<e.length;
++j){k=e[j];
g=this.getElementsByTagNameNS(d,this.gmlns,k);
if(g.length>0){b=this.parseGeometry[k.toLowerCase()];
if(b){l=b.apply(this,[g[0]]);
if(this.internalProjection&&this.externalProjection){l.transform(this.externalProjection,this.internalProjection)
}}else{OpenLayers.Console.error(OpenLayers.i18n("unsupportedGeometryType",{geomType:k}))
}break
}}var h;
if(this.extractAttributes){h=this.parseAttributes(d)
}var m=new OpenLayers.Feature.Vector(l,h);
m.gml={featureType:d.firstChild.nodeName.split(":")[1],featureNS:d.firstChild.namespaceURI,featureNSPrefix:d.firstChild.prefix};
var c=this.getElementsByTagNameNS(d,this.gmlns,"boundedBy");
if(c.length===1){b=this.parseGeometry.box;
if(b){m.bounds=b.apply(this,[c[0]])
}}var a=d.firstChild;
var f;
while(a){if(a.nodeType==1){f=a.getAttribute("fid")||a.getAttribute("id");
if(f){break
}}a=a.nextSibling
}m.fid=f;
return m
},parseGeometry:{point:function(d){var b,a;
var e=[];
var b=this.getElementsByTagNameNS(d,this.gmlns,"pos");
if(b.length>0){a=b[0].firstChild.nodeValue;
a=a.replace(this.regExes.trimSpace,"");
e=a.split(this.regExes.splitSpace)
}if(e.length==0){b=this.getElementsByTagNameNS(d,this.gmlns,"coordinates");
if(b.length>0){a=b[0].firstChild.nodeValue;
a=a.replace(this.regExes.removeSpace,"");
e=a.split(",")
}}if(e.length==0){b=this.getElementsByTagNameNS(d,this.gmlns,"coord");
if(b.length>0){var f=this.getElementsByTagNameNS(b[0],this.gmlns,"X");
var c=this.getElementsByTagNameNS(b[0],this.gmlns,"Y");
if(f.length>0&&c.length>0){e=[f[0].firstChild.nodeValue,c[0].firstChild.nodeValue]
}}}if(e.length==2){e[2]=null
}if(this.xy){return new OpenLayers.Geometry.Point(e[0],e[1],e[2])
}else{return new OpenLayers.Geometry.Point(e[1],e[0],e[2])
}},multipoint:function(e){var b=this.getElementsByTagNameNS(e,this.gmlns,"Point");
var d=[];
if(b.length>0){var a;
for(var c=0;
c<b.length;
++c){a=this.parseGeometry.point.apply(this,[b[c]]);
if(a){d.push(a)
}}}return new OpenLayers.Geometry.MultiPoint(d)
},linestring:function(c,e){var d,b;
var n=[];
var o=[];
d=this.getElementsByTagNameNS(c,this.gmlns,"posList");
if(d.length>0){b=this.getChildValue(d[0]);
b=b.replace(this.regExes.trimSpace,"");
n=b.split(this.regExes.splitSpace);
var h=parseInt(d[0].getAttribute("dimension"));
var f,m,l,k;
for(var g=0;
g<n.length/h;
++g){f=g*h;
m=n[f];
l=n[f+1];
k=(h==2)?null:n[f+2];
if(this.xy){o.push(new OpenLayers.Geometry.Point(m,l,k))
}else{o.push(new OpenLayers.Geometry.Point(l,m,k))
}}}if(n.length==0){d=this.getElementsByTagNameNS(c,this.gmlns,"coordinates");
if(d.length>0){b=this.getChildValue(d[0]);
b=b.replace(this.regExes.trimSpace,"");
b=b.replace(this.regExes.trimComma,",");
var a=b.split(this.regExes.splitSpace);
for(var g=0;
g<a.length;
++g){n=a[g].split(",");
if(n.length==2){n[2]=null
}if(this.xy){o.push(new OpenLayers.Geometry.Point(n[0],n[1],n[2]))
}else{o.push(new OpenLayers.Geometry.Point(n[1],n[0],n[2]))
}}}}var p=null;
if(o.length!=0){if(e){p=new OpenLayers.Geometry.LinearRing(o)
}else{p=new OpenLayers.Geometry.LineString(o)
}}return p
},multilinestring:function(e){var b=this.getElementsByTagNameNS(e,this.gmlns,"LineString");
var d=[];
if(b.length>0){var a;
for(var c=0;
c<b.length;
++c){a=this.parseGeometry.linestring.apply(this,[b[c]]);
if(a){d.push(a)
}}}return new OpenLayers.Geometry.MultiLineString(d)
},polygon:function(e){var b=this.getElementsByTagNameNS(e,this.gmlns,"LinearRing");
var d=[];
if(b.length>0){var a;
for(var c=0;
c<b.length;
++c){a=this.parseGeometry.linestring.apply(this,[b[c],true]);
if(a){d.push(a)
}}}return new OpenLayers.Geometry.Polygon(d)
},multipolygon:function(e){var a=this.getElementsByTagNameNS(e,this.gmlns,"Polygon");
var d=[];
if(a.length>0){var c;
for(var b=0;
b<a.length;
++b){c=this.parseGeometry.polygon.apply(this,[a[b]]);
if(c){d.push(c)
}}}return new OpenLayers.Geometry.MultiPolygon(d)
},envelope:function(b){var e=[];
var a;
var f;
var j=this.getElementsByTagNameNS(b,this.gmlns,"lowerCorner");
if(j.length>0){var h=[];
if(j.length>0){a=j[0].firstChild.nodeValue;
a=a.replace(this.regExes.trimSpace,"");
h=a.split(this.regExes.splitSpace)
}if(h.length==2){h[2]=null
}if(this.xy){var d=new OpenLayers.Geometry.Point(h[0],h[1],h[2])
}else{var d=new OpenLayers.Geometry.Point(h[1],h[0],h[2])
}}var g=this.getElementsByTagNameNS(b,this.gmlns,"upperCorner");
if(g.length>0){var h=[];
if(g.length>0){a=g[0].firstChild.nodeValue;
a=a.replace(this.regExes.trimSpace,"");
h=a.split(this.regExes.splitSpace)
}if(h.length==2){h[2]=null
}if(this.xy){var i=new OpenLayers.Geometry.Point(h[0],h[1],h[2])
}else{var i=new OpenLayers.Geometry.Point(h[1],h[0],h[2])
}}if(d&&i){e.push(new OpenLayers.Geometry.Point(d.x,d.y));
e.push(new OpenLayers.Geometry.Point(i.x,d.y));
e.push(new OpenLayers.Geometry.Point(i.x,i.y));
e.push(new OpenLayers.Geometry.Point(d.x,i.y));
e.push(new OpenLayers.Geometry.Point(d.x,d.y));
var c=new OpenLayers.Geometry.LinearRing(e);
f=new OpenLayers.Geometry.Polygon([c])
}return f
},box:function(e){var c=this.getElementsByTagNameNS(e,this.gmlns,"coordinates");
var b;
var f,a=null,d=null;
if(c.length>0){b=c[0].firstChild.nodeValue;
f=b.split(" ");
if(f.length==2){a=f[0].split(",");
d=f[1].split(",")
}}if(a!==null&&d!==null){return new OpenLayers.Bounds(parseFloat(a[0]),parseFloat(a[1]),parseFloat(d[0]),parseFloat(d[1]))
}}},parseAttributes:function(e){var f={};
var a=e.firstChild;
var d,g,c,k,j,b,h;
while(a){if(a.nodeType==1){d=a.childNodes;
for(g=0;
g<d.length;
++g){c=d[g];
if(c.nodeType==1){k=c.childNodes;
if(k.length==1){j=k[0];
if(j.nodeType==3||j.nodeType==4){b=(c.prefix)?c.nodeName.split(":")[1]:c.nodeName;
h=j.nodeValue.replace(this.regExes.trimSpace,"");
f[b]=h
}}else{f[c.nodeName.split(":").pop()]=null
}}}break
}a=a.nextSibling
}return f
},write:function(c){if(!(c instanceof Array)){c=[c]
}var b=this.createElementNS("http://www.opengis.net/wfs","wfs:"+this.collectionName);
for(var a=0;
a<c.length;
a++){b.appendChild(this.createFeatureXML(c[a]))
}return OpenLayers.Format.XML.prototype.write.apply(this,[b])
},createFeatureXML:function(j){var h=j.geometry;
var e=this.buildGeometryNode(h);
var i=this.createElementNS(this.featureNS,this.featurePrefix+":"+this.geometryName);
i.appendChild(e);
var a=this.createElementNS(this.gmlns,"gml:"+this.featureName);
var k=this.createElementNS(this.featureNS,this.featurePrefix+":"+this.layerName);
var c=j.fid||j.id;
k.setAttribute("fid",c);
k.appendChild(i);
for(var g in j.attributes){var f=this.createTextNode(j.attributes[g]);
var d=g.substring(g.lastIndexOf(":")+1);
var b=this.createElementNS(this.featureNS,this.featurePrefix+":"+d);
b.appendChild(f);
k.appendChild(b)
}a.appendChild(k);
return a
},buildGeometryNode:function(d){if(this.externalProjection&&this.internalProjection){d=d.clone();
d.transform(this.internalProjection,this.externalProjection)
}var c=d.CLASS_NAME;
var b=c.substring(c.lastIndexOf(".")+1);
var a=this.buildGeometry[b.toLowerCase()];
return a.apply(this,[d])
},buildGeometry:{point:function(b){var a=this.createElementNS(this.gmlns,"gml:Point");
a.appendChild(this.buildCoordinatesNode(b));
return a
},multipoint:function(f){var d=this.createElementNS(this.gmlns,"gml:MultiPoint");
var c=f.components;
var b,e;
for(var a=0;
a<c.length;
a++){b=this.createElementNS(this.gmlns,"gml:pointMember");
e=this.buildGeometry.point.apply(this,[c[a]]);
b.appendChild(e);
d.appendChild(b)
}return d
},linestring:function(b){var a=this.createElementNS(this.gmlns,"gml:LineString");
a.appendChild(this.buildCoordinatesNode(b));
return a
},multilinestring:function(f){var d=this.createElementNS(this.gmlns,"gml:MultiLineString");
var a=f.components;
var c,e;
for(var b=0;
b<a.length;
++b){c=this.createElementNS(this.gmlns,"gml:lineStringMember");
e=this.buildGeometry.linestring.apply(this,[a[b]]);
c.appendChild(e);
d.appendChild(c)
}return d
},linearring:function(b){var a=this.createElementNS(this.gmlns,"gml:LinearRing");
a.appendChild(this.buildCoordinatesNode(b));
return a
},polygon:function(g){var d=this.createElementNS(this.gmlns,"gml:Polygon");
var f=g.components;
var c,e,b;
for(var a=0;
a<f.length;
++a){b=(a==0)?"outerBoundaryIs":"innerBoundaryIs";
c=this.createElementNS(this.gmlns,"gml:"+b);
e=this.buildGeometry.linearring.apply(this,[f[a]]);
c.appendChild(e);
d.appendChild(c)
}return d
},multipolygon:function(f){var d=this.createElementNS(this.gmlns,"gml:MultiPolygon");
var a=f.components;
var e,b;
for(var c=0;
c<a.length;
++c){e=this.createElementNS(this.gmlns,"gml:polygonMember");
b=this.buildGeometry.polygon.apply(this,[a[c]]);
e.appendChild(b);
d.appendChild(e)
}return d
},bounds:function(b){var a=this.createElementNS(this.gmlns,"gml:Box");
a.appendChild(this.buildCoordinatesNode(b));
return a
}},buildCoordinatesNode:function(f){var a=this.createElementNS(this.gmlns,"gml:coordinates");
a.setAttribute("decimal",".");
a.setAttribute("cs",",");
a.setAttribute("ts"," ");
var e=[];
if(f instanceof OpenLayers.Bounds){e.push(f.left+","+f.bottom);
e.push(f.right+","+f.top)
}else{var c=(f.components)?f.components:[f];
for(var b=0;
b<c.length;
b++){e.push(c[b].x+","+c[b].y)
}}var d=this.createTextNode(e.join(" "));
a.appendChild(d);
return a
},CLASS_NAME:"OpenLayers.Format.GML"});if(!OpenLayers.Format.GML){OpenLayers.Format.GML={}
}OpenLayers.Format.GML.Base=OpenLayers.Class(OpenLayers.Format.XML,{namespaces:{gml:"http://www.opengis.net/gml",xlink:"http://www.w3.org/1999/xlink",xsi:"http://www.w3.org/2001/XMLSchema-instance",wfs:"http://www.opengis.net/wfs"},defaultPrefix:"gml",schemaLocation:null,featureType:null,featureNS:null,geometryName:"geometry",extractAttributes:true,srsName:null,xy:true,geometryTypes:null,singleFeatureType:null,regExes:{trimSpace:(/^\s*|\s*$/g),removeSpace:(/\s*/g),splitSpace:(/\s+/),trimComma:(/\s*,\s*/g)},initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a]);
this.setGeometryTypes();
if(a&&a.featureNS){this.setNamespace("feature",a.featureNS)
}this.singleFeatureType=!a||(typeof a.featureType==="string")
},read:function(e){if(typeof e=="string"){e=OpenLayers.Format.XML.prototype.read.apply(this,[e])
}if(e&&e.nodeType==9){e=e.documentElement
}var c=[];
this.readNode(e,{features:c});
if(c.length==0){var d=this.getElementsByTagNameNS(e,this.namespaces.gml,"featureMember");
if(d.length){for(var b=0,a=d.length;
b<a;
++b){this.readNode(d[b],{features:c})
}}else{var d=this.getElementsByTagNameNS(e,this.namespaces.gml,"featureMembers");
if(d.length){this.readNode(d[0],{features:c})
}}}return c
},readers:{gml:{featureMember:function(a,b){this.readChildNodes(a,b)
},featureMembers:function(a,b){this.readChildNodes(a,b)
},name:function(a,b){b.name=this.getChildValue(a)
},boundedBy:function(b,c){var a={};
this.readChildNodes(b,a);
if(a.components&&a.components.length>0){c.bounds=a.components[0]
}},Point:function(b,a){var c={points:[]};
this.readChildNodes(b,c);
if(!a.components){a.components=[]
}a.components.push(c.points[0])
},coordinates:function(e,g){var h=this.getChildValue(e).replace(this.regExes.trimSpace,"");
h=h.replace(this.regExes.trimComma,",");
var a=h.split(this.regExes.splitSpace);
var f;
var d=a.length;
var c=new Array(d);
for(var b=0;
b<d;
++b){f=a[b].split(",");
if(this.xy){c[b]=new OpenLayers.Geometry.Point(f[0],f[1],f[2])
}else{c[b]=new OpenLayers.Geometry.Point(f[1],f[0],f[2])
}}g.points=c
},coord:function(a,b){var c={};
this.readChildNodes(a,c);
if(!b.points){b.points=[]
}b.points.push(new OpenLayers.Geometry.Point(c.x,c.y,c.z))
},X:function(a,b){b.x=this.getChildValue(a)
},Y:function(a,b){b.y=this.getChildValue(a)
},Z:function(a,b){b.z=this.getChildValue(a)
},MultiPoint:function(b,a){var c={components:[]};
this.readChildNodes(b,c);
a.components=[new OpenLayers.Geometry.MultiPoint(c.components)]
},pointMember:function(a,b){this.readChildNodes(a,b)
},LineString:function(b,a){var c={};
this.readChildNodes(b,c);
if(!a.components){a.components=[]
}a.components.push(new OpenLayers.Geometry.LineString(c.points))
},MultiLineString:function(b,a){var c={components:[]};
this.readChildNodes(b,c);
a.components=[new OpenLayers.Geometry.MultiLineString(c.components)]
},lineStringMember:function(a,b){this.readChildNodes(a,b)
},Polygon:function(b,a){var c={outer:null,inner:[]};
this.readChildNodes(b,c);
c.inner.unshift(c.outer);
if(!a.components){a.components=[]
}a.components.push(new OpenLayers.Geometry.Polygon(c.inner))
},LinearRing:function(b,c){var a={};
this.readChildNodes(b,a);
c.components=[new OpenLayers.Geometry.LinearRing(a.points)]
},MultiPolygon:function(b,a){var c={components:[]};
this.readChildNodes(b,c);
a.components=[new OpenLayers.Geometry.MultiPolygon(c.components)]
},polygonMember:function(a,b){this.readChildNodes(a,b)
},GeometryCollection:function(b,a){var c={components:[]};
this.readChildNodes(b,c);
a.components=[new OpenLayers.Geometry.Collection(c.components)]
},geometryMember:function(a,b){this.readChildNodes(a,b)
}},feature:{"*":function(c,d){var a;
var b=c.localName||c.nodeName.split(":").pop();
if(d.features){if(!this.singleFeatureType&&(OpenLayers.Util.indexOf(this.featureType,b)!==-1)){a="_typeName"
}else{if(b===this.featureType){a="_typeName"
}}}else{if(c.childNodes.length==0||(c.childNodes.length==1&&c.firstChild.nodeType==3)){if(this.extractAttributes){a="_attribute"
}}else{a="_geometry"
}}if(a){this.readers.feature[a].apply(this,[c,d])
}},_typeName:function(c,d){var a={components:[],attributes:{}};
this.readChildNodes(c,a);
if(a.name){a.attributes.name=a.name
}var b=new OpenLayers.Feature.Vector(a.components[0],a.attributes);
if(!this.singleFeatureType){b.type=c.nodeName.split(":").pop();
b.namespace=c.namespaceURI
}var e=c.getAttribute("fid")||this.getAttributeNS(c,this.namespaces.gml,"id");
if(e){b.fid=e
}if(this.internalProjection&&this.externalProjection&&b.geometry){b.geometry.transform(this.externalProjection,this.internalProjection)
}if(a.bounds){b.bounds=a.bounds
}d.features.push(b)
},_geometry:function(a,b){this.readChildNodes(a,b)
},_attribute:function(b,d){var a=b.localName||b.nodeName.split(":").pop();
var c=this.getChildValue(b);
d.attributes[a]=c
}},wfs:{FeatureCollection:function(a,b){this.readChildNodes(a,b)
}}},write:function(c){var b;
if(c instanceof Array){b="featureMembers"
}else{b="featureMember"
}var a=this.writeNode("gml:"+b,c);
this.setAttributeNS(a,this.namespaces.xsi,"xsi:schemaLocation",this.schemaLocation);
return OpenLayers.Format.XML.prototype.write.apply(this,[a])
},writers:{gml:{featureMember:function(a){var b=this.createElementNSPlus("gml:featureMember");
this.writeNode("feature:_typeName",a,b);
return b
},MultiPoint:function(c){var b=this.createElementNSPlus("gml:MultiPoint");
for(var a=0;
a<c.components.length;
++a){this.writeNode("pointMember",c.components[a],b)
}return b
},pointMember:function(b){var a=this.createElementNSPlus("gml:pointMember");
this.writeNode("Point",b,a);
return a
},MultiLineString:function(c){var b=this.createElementNSPlus("gml:MultiLineString");
for(var a=0;
a<c.components.length;
++a){this.writeNode("lineStringMember",c.components[a],b)
}return b
},lineStringMember:function(b){var a=this.createElementNSPlus("gml:lineStringMember");
this.writeNode("LineString",b,a);
return a
},MultiPolygon:function(c){var b=this.createElementNSPlus("gml:MultiPolygon");
for(var a=0;
a<c.components.length;
++a){this.writeNode("polygonMember",c.components[a],b)
}return b
},polygonMember:function(b){var a=this.createElementNSPlus("gml:polygonMember");
this.writeNode("Polygon",b,a);
return a
},GeometryCollection:function(d){var c=this.createElementNSPlus("gml:GeometryCollection");
for(var b=0,a=d.components.length;
b<a;
++b){this.writeNode("geometryMember",d.components[b],c)
}return c
},geometryMember:function(b){var a=this.createElementNSPlus("gml:geometryMember");
var c=this.writeNode("feature:_geometry",b);
a.appendChild(c.firstChild);
return a
}},feature:{_typeName:function(b){var c=this.createElementNSPlus("feature:"+this.featureType,{attributes:{fid:b.fid}});
if(b.geometry){this.writeNode("feature:_geometry",b.geometry,c)
}for(var a in b.attributes){var d=b.attributes[a];
if(d!=null){this.writeNode("feature:_attribute",{name:a,value:d},c)
}}return c
},_geometry:function(c){if(this.externalProjection&&this.internalProjection){c=c.clone().transform(this.internalProjection,this.externalProjection)
}var b=this.createElementNSPlus("feature:"+this.geometryName);
var a=this.geometryTypes[c.CLASS_NAME];
var d=this.writeNode("gml:"+a,c,b);
if(this.srsName){d.setAttribute("srsName",this.srsName)
}return b
},_attribute:function(a){return this.createElementNSPlus("feature:"+a.name,{value:a.value})
}},wfs:{FeatureCollection:function(c){var d=this.createElementNSPlus("wfs:FeatureCollection");
for(var b=0,a=c.length;
b<a;
++b){this.writeNode("gml:featureMember",c[b],d)
}return d
}}},setGeometryTypes:function(){this.geometryTypes={"OpenLayers.Geometry.Point":"Point","OpenLayers.Geometry.MultiPoint":"MultiPoint","OpenLayers.Geometry.LineString":"LineString","OpenLayers.Geometry.MultiLineString":"MultiLineString","OpenLayers.Geometry.Polygon":"Polygon","OpenLayers.Geometry.MultiPolygon":"MultiPolygon","OpenLayers.Geometry.Collection":"GeometryCollection"}
},CLASS_NAME:"OpenLayers.Format.GML.Base"});OpenLayers.Format.GML.v3=OpenLayers.Class(OpenLayers.Format.GML.Base,{schemaLocation:"http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/profiles/gmlsfProfile/1.0.0/gmlsf.xsd",curve:false,multiCurve:true,surface:false,multiSurface:true,initialize:function(a){OpenLayers.Format.GML.Base.prototype.initialize.apply(this,[a])
},readers:{gml:OpenLayers.Util.applyDefaults({featureMembers:function(a,b){this.readChildNodes(a,b)
},Curve:function(b,a){var c={points:[]};
this.readChildNodes(b,c);
if(!a.components){a.components=[]
}a.components.push(new OpenLayers.Geometry.LineString(c.points))
},segments:function(a,b){this.readChildNodes(a,b)
},LineStringSegment:function(b,a){var c={};
this.readChildNodes(b,c);
if(c.points){Array.prototype.push.apply(a.points,c.points)
}},pos:function(b,d){var e=this.getChildValue(b).replace(this.regExes.trimSpace,"");
var c=e.split(this.regExes.splitSpace);
var a;
if(this.xy){a=new OpenLayers.Geometry.Point(c[0],c[1],c[2])
}else{a=new OpenLayers.Geometry.Point(c[1],c[0],c[2])
}d.points=[a]
},posList:function(a,d){var h=this.getChildValue(a).replace(this.regExes.trimSpace,"");
var m=h.split(this.regExes.splitSpace);
var e=parseInt(a.getAttribute("dimension"))||2;
var b,n,l,g;
var k=m.length/e;
var o=new Array(k);
for(var c=0,f=m.length;
c<f;
c+=e){n=m[c];
l=m[c+1];
g=(e==2)?undefined:m[c+2];
if(this.xy){o[c/e]=new OpenLayers.Geometry.Point(n,l,g)
}else{o[c/e]=new OpenLayers.Geometry.Point(l,n,g)
}}d.points=o
},Surface:function(a,b){this.readChildNodes(a,b)
},patches:function(a,b){this.readChildNodes(a,b)
},PolygonPatch:function(a,b){this.readers.gml.Polygon.apply(this,[a,b])
},exterior:function(b,a){var c={};
this.readChildNodes(b,c);
a.outer=c.components[0]
},interior:function(b,a){var c={};
this.readChildNodes(b,c);
a.inner.push(c.components[0])
},MultiCurve:function(b,a){var c={components:[]};
this.readChildNodes(b,c);
if(c.components.length>0){a.components=[new OpenLayers.Geometry.MultiLineString(c.components)]
}},curveMember:function(a,b){this.readChildNodes(a,b)
},MultiSurface:function(b,a){var c={components:[]};
this.readChildNodes(b,c);
if(c.components.length>0){a.components=[new OpenLayers.Geometry.MultiPolygon(c.components)]
}},surfaceMember:function(a,b){this.readChildNodes(a,b)
},surfaceMembers:function(a,b){this.readChildNodes(a,b)
},pointMembers:function(a,b){this.readChildNodes(a,b)
},lineStringMembers:function(a,b){this.readChildNodes(a,b)
},polygonMembers:function(a,b){this.readChildNodes(a,b)
},geometryMembers:function(a,b){this.readChildNodes(a,b)
},Envelope:function(d,b){var e={points:new Array(2)};
this.readChildNodes(d,e);
if(!b.components){b.components=[]
}var c=e.points[0];
var a=e.points[1];
b.components.push(new OpenLayers.Bounds(c.x,c.y,a.x,a.y))
},lowerCorner:function(b,a){var c={};
this.readers.gml.pos.apply(this,[b,c]);
a.points[0]=c.points[0]
},upperCorner:function(b,a){var c={};
this.readers.gml.pos.apply(this,[b,c]);
a.points[1]=c.points[0]
}},OpenLayers.Format.GML.Base.prototype.readers.gml),feature:OpenLayers.Format.GML.Base.prototype.readers.feature,wfs:OpenLayers.Format.GML.Base.prototype.readers.wfs},write:function(c){var b;
if(c instanceof Array){b="featureMembers"
}else{b="featureMember"
}var a=this.writeNode("gml:"+b,c);
this.setAttributeNS(a,this.namespaces.xsi,"xsi:schemaLocation",this.schemaLocation);
return OpenLayers.Format.XML.prototype.write.apply(this,[a])
},writers:{gml:OpenLayers.Util.applyDefaults({featureMembers:function(c){var d=this.createElementNSPlus("gml:featureMembers");
for(var b=0,a=c.length;
b<a;
++b){this.writeNode("feature:_typeName",c[b],d)
}return d
},Point:function(b){var a=this.createElementNSPlus("gml:Point");
this.writeNode("pos",b,a);
return a
},pos:function(a){var b=(this.xy)?(a.x+" "+a.y):(a.y+" "+a.x);
return this.createElementNSPlus("gml:pos",{value:b})
},LineString:function(b){var a=this.createElementNSPlus("gml:LineString");
this.writeNode("posList",b.components,a);
return a
},Curve:function(b){var a=this.createElementNSPlus("gml:Curve");
this.writeNode("segments",b,a);
return a
},segments:function(b){var a=this.createElementNSPlus("gml:segments");
this.writeNode("LineStringSegment",b,a);
return a
},LineStringSegment:function(b){var a=this.createElementNSPlus("gml:LineStringSegment");
this.writeNode("posList",b.components,a);
return a
},posList:function(d){var b=d.length;
var e=new Array(b);
var a;
for(var c=0;
c<b;
++c){a=d[c];
if(this.xy){e[c]=a.x+" "+a.y
}else{e[c]=a.y+" "+a.x
}}return this.createElementNSPlus("gml:posList",{value:e.join(" ")})
},Surface:function(b){var a=this.createElementNSPlus("gml:Surface");
this.writeNode("patches",b,a);
return a
},patches:function(b){var a=this.createElementNSPlus("gml:patches");
this.writeNode("PolygonPatch",b,a);
return a
},PolygonPatch:function(d){var c=this.createElementNSPlus("gml:PolygonPatch",{attributes:{interpolation:"planar"}});
this.writeNode("exterior",d.components[0],c);
for(var b=1,a=d.components.length;
b<a;
++b){this.writeNode("interior",d.components[b],c)
}return c
},Polygon:function(d){var c=this.createElementNSPlus("gml:Polygon");
this.writeNode("exterior",d.components[0],c);
for(var b=1,a=d.components.length;
b<a;
++b){this.writeNode("interior",d.components[b],c)
}return c
},exterior:function(a){var b=this.createElementNSPlus("gml:exterior");
this.writeNode("LinearRing",a,b);
return b
},interior:function(a){var b=this.createElementNSPlus("gml:interior");
this.writeNode("LinearRing",a,b);
return b
},LinearRing:function(a){var b=this.createElementNSPlus("gml:LinearRing");
this.writeNode("posList",a.components,b);
return b
},MultiCurve:function(d){var c=this.createElementNSPlus("gml:MultiCurve");
for(var b=0,a=d.components.length;
b<a;
++b){this.writeNode("curveMember",d.components[b],c)
}return c
},curveMember:function(b){var a=this.createElementNSPlus("gml:curveMember");
if(this.curve){this.writeNode("Curve",b,a)
}else{this.writeNode("LineString",b,a)
}return a
},MultiSurface:function(d){var c=this.createElementNSPlus("gml:MultiSurface");
for(var b=0,a=d.components.length;
b<a;
++b){this.writeNode("surfaceMember",d.components[b],c)
}return c
},surfaceMember:function(a){var b=this.createElementNSPlus("gml:surfaceMember");
if(this.surface){this.writeNode("Surface",a,b)
}else{this.writeNode("Polygon",a,b)
}return b
},Envelope:function(b){var a=this.createElementNSPlus("gml:Envelope");
this.writeNode("lowerCorner",b,a);
this.writeNode("upperCorner",b,a);
if(this.srsName){a.setAttribute("srsName",this.srsName)
}return a
},lowerCorner:function(a){var b=(this.xy)?(a.left+" "+a.bottom):(a.bottom+" "+a.left);
return this.createElementNSPlus("gml:lowerCorner",{value:b})
},upperCorner:function(a){var b=(this.xy)?(a.right+" "+a.top):(a.top+" "+a.right);
return this.createElementNSPlus("gml:upperCorner",{value:b})
}},OpenLayers.Format.GML.Base.prototype.writers.gml),feature:OpenLayers.Format.GML.Base.prototype.writers.feature,wfs:OpenLayers.Format.GML.Base.prototype.writers.wfs},setGeometryTypes:function(){this.geometryTypes={"OpenLayers.Geometry.Point":"Point","OpenLayers.Geometry.MultiPoint":"MultiPoint","OpenLayers.Geometry.LineString":(this.curve===true)?"Curve":"LineString","OpenLayers.Geometry.MultiLineString":(this.multiCurve===false)?"MultiLineString":"MultiCurve","OpenLayers.Geometry.Polygon":(this.surface===true)?"Surface":"Polygon","OpenLayers.Geometry.MultiPolygon":(this.multiSurface===false)?"MultiPolygon":"MultiSurface","OpenLayers.Geometry.Collection":"GeometryCollection"}
},CLASS_NAME:"OpenLayers.Format.GML.v3"});OpenLayers.Element={visible:function(a){return OpenLayers.Util.getElement(a).style.display!="none"
},toggle:function(){for(var c=0,a=arguments.length;
c<a;
c++){var b=OpenLayers.Util.getElement(arguments[c]);
var d=OpenLayers.Element.visible(b)?"hide":"show";
OpenLayers.Element[d](b)
}},hide:function(){for(var c=0,a=arguments.length;
c<a;
c++){var b=OpenLayers.Util.getElement(arguments[c]);
b.style.display="none"
}},show:function(){for(var c=0,a=arguments.length;
c<a;
c++){var b=OpenLayers.Util.getElement(arguments[c]);
b.style.display=""
}},remove:function(a){a=OpenLayers.Util.getElement(a);
a.parentNode.removeChild(a)
},getHeight:function(a){a=OpenLayers.Util.getElement(a);
return a.offsetHeight
},getDimensions:function(c){c=OpenLayers.Util.getElement(c);
if(OpenLayers.Element.getStyle(c,"display")!="none"){return{width:c.offsetWidth,height:c.offsetHeight}
}var b=c.style;
var f=b.visibility;
var d=b.position;
var a=b.display;
b.visibility="hidden";
b.position="absolute";
b.display="";
var g=c.clientWidth;
var e=c.clientHeight;
b.display=a;
b.position=d;
b.visibility=f;
return{width:g,height:e}
},hasClass:function(b,a){var c=b.className;
return(!!c&&new RegExp("(^|\\s)"+a+"(\\s|$)").test(c))
},addClass:function(b,a){if(!OpenLayers.Element.hasClass(b,a)){b.className+=(b.className?" ":"")+a
}return b
},removeClass:function(b,a){var c=b.className;
if(c){b.className=OpenLayers.String.trim(c.replace(new RegExp("(^|\\s+)"+a+"(\\s+|$)")," "))
}return b
},toggleClass:function(b,a){if(OpenLayers.Element.hasClass(b,a)){OpenLayers.Element.removeClass(b,a)
}else{OpenLayers.Element.addClass(b,a)
}return b
},getStyle:function(c,d){c=OpenLayers.Util.getElement(c);
var e=null;
if(c&&c.style){e=c.style[OpenLayers.String.camelize(d)];
if(!e){if(document.defaultView&&document.defaultView.getComputedStyle){var b=document.defaultView.getComputedStyle(c,null);
e=b?b.getPropertyValue(d):null
}else{if(c.currentStyle){e=c.currentStyle[OpenLayers.String.camelize(d)]
}}}var a=["left","top","right","bottom"];
if(window.opera&&(OpenLayers.Util.indexOf(a,d)!=-1)&&(OpenLayers.Element.getStyle(c,"position")=="static")){e="auto"
}}return e=="auto"?null:e
}};OpenLayers.Handler.MouseWheel=OpenLayers.Class(OpenLayers.Handler,{wheelListener:null,mousePosition:null,interval:0,delta:0,cumulative:true,initialize:function(c,b,a){OpenLayers.Handler.prototype.initialize.apply(this,arguments);
this.wheelListener=OpenLayers.Function.bindAsEventListener(this.onWheelEvent,this)
},destroy:function(){OpenLayers.Handler.prototype.destroy.apply(this,arguments);
this.wheelListener=null
},onWheelEvent:function(k){if(!this.map||!this.checkModifiers(k)){return
}var g=false;
var m=false;
var f=false;
var b=OpenLayers.Event.element(k);
while((b!=null)&&!f&&!g){if(!g){try{if(b.currentStyle){c=b.currentStyle.overflow
}else{var a=document.defaultView.getComputedStyle(b,null);
var c=a.getPropertyValue("overflow")
}g=(c&&(c=="auto")||(c=="scroll"))
}catch(d){}}if(!m){for(var h=0,j=this.map.layers.length;
h<j;
h++){if(b==this.map.layers[h].div||b==this.map.layers[h].pane){m=true;
break
}}}f=(b==this.map.div);
b=b.parentNode
}if(!g&&f){if(m){var l=0;
if(!k){k=window.event
}if(k.wheelDelta){l=k.wheelDelta/120;
if(window.opera&&window.opera.version()<9.2){l=-l
}}else{if(k.detail){l=-k.detail/3
}}this.delta=this.delta+l;
if(this.interval){window.clearTimeout(this._timeoutId);
this._timeoutId=window.setTimeout(OpenLayers.Function.bind(function(){this.wheelZoom(k)
},this),this.interval)
}else{this.wheelZoom(k)
}}OpenLayers.Event.stop(k)
}},wheelZoom:function(a){var b=this.delta;
this.delta=0;
if(b){if(this.mousePosition){a.xy=this.mousePosition
}if(!a.xy){a.xy=this.map.getPixelFromLonLat(this.map.getCenter())
}if(b<0){this.callback("down",[a,this.cumulative?b:-1])
}else{this.callback("up",[a,this.cumulative?b:1])
}}},mousemove:function(a){this.mousePosition=a.xy
},activate:function(a){if(OpenLayers.Handler.prototype.activate.apply(this,arguments)){var b=this.wheelListener;
OpenLayers.Event.observe(window,"DOMMouseScroll",b);
OpenLayers.Event.observe(window,"mousewheel",b);
OpenLayers.Event.observe(document,"mousewheel",b);
return true
}else{return false
}},deactivate:function(a){if(OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){var b=this.wheelListener;
OpenLayers.Event.stopObserving(window,"DOMMouseScroll",b);
OpenLayers.Event.stopObserving(window,"mousewheel",b);
OpenLayers.Event.stopObserving(document,"mousewheel",b);
return true
}else{return false
}},CLASS_NAME:"OpenLayers.Handler.MouseWheel"});OpenLayers.Control.ZoomToMaxExtent=OpenLayers.Class(OpenLayers.Control,{type:OpenLayers.Control.TYPE_BUTTON,trigger:function(){if(this.map){this.map.zoomToMaxExtent()
}},CLASS_NAME:"OpenLayers.Control.ZoomToMaxExtent"});OpenLayers.Style=OpenLayers.Class({name:null,title:null,description:null,layerName:null,isDefault:false,rules:null,context:null,defaultStyle:null,defaultsPerSymbolizer:false,propertyStyles:null,initialize:function(b,a){OpenLayers.Util.extend(this,a);
this.rules=[];
if(a&&a.rules){this.addRules(a.rules)
}this.setDefaultStyle(b||OpenLayers.Feature.Vector.style["default"])
},destroy:function(){for(var b=0,a=this.rules.length;
b<a;
b++){this.rules[b].destroy();
this.rules[b]=null
}this.rules=null;
this.defaultStyle=null
},createSymbolizer:function(k){var a=this.defaultsPerSymbolizer?{}:this.createLiterals(OpenLayers.Util.extend({},this.defaultStyle),k);
var j=this.rules;
var h,b;
var c=[];
var f=false;
for(var d=0,e=j.length;
d<e;
d++){h=j[d];
var g=h.evaluate(k);
if(g){if(h instanceof OpenLayers.Rule&&h.elseFilter){c.push(h)
}else{f=true;
this.applySymbolizer(h,a,k)
}}}if(f==false&&c.length>0){f=true;
for(var d=0,e=c.length;
d<e;
d++){this.applySymbolizer(c[d],a,k)
}}if(j.length>0&&f==false){a.display="none"
}return a
},applySymbolizer:function(f,d,b){var a=b.geometry?this.getSymbolizerPrefix(b.geometry):OpenLayers.Style.SYMBOLIZER_PREFIXES[0];
var c=f.symbolizer[a]||f.symbolizer;
if(this.defaultsPerSymbolizer===true){var e=this.defaultStyle;
OpenLayers.Util.applyDefaults(c,{pointRadius:e.pointRadius});
if(c.stroke===true||c.graphic===true){OpenLayers.Util.applyDefaults(c,{strokeWidth:e.strokeWidth,strokeColor:e.strokeColor,strokeOpacity:e.strokeOpacity,strokeDashstyle:e.strokeDashstyle,strokeLinecap:e.strokeLinecap})
}if(c.fill===true||c.graphic===true){OpenLayers.Util.applyDefaults(c,{fillColor:e.fillColor,fillOpacity:e.fillOpacity})
}if(c.graphic===true){OpenLayers.Util.applyDefaults(c,{pointRadius:this.defaultStyle.pointRadius,externalGraphic:this.defaultStyle.externalGraphic,graphicName:this.defaultStyle.graphicName,graphicOpacity:this.defaultStyle.graphicOpacity,graphicWidth:this.defaultStyle.graphicWidth,graphicHeight:this.defaultStyle.graphicHeight,graphicXOffset:this.defaultStyle.graphicXOffset,graphicYOffset:this.defaultStyle.graphicYOffset})
}}return this.createLiterals(OpenLayers.Util.extend(d,c),b)
},createLiterals:function(d,c){var b=OpenLayers.Util.extend({},c.attributes||c.data);
OpenLayers.Util.extend(b,this.context);
for(var a in this.propertyStyles){d[a]=OpenLayers.Style.createLiteral(d[a],b,c,a)
}return d
},findPropertyStyles:function(){var d={};
var f=this.defaultStyle;
this.addPropertyStyles(d,f);
var h=this.rules;
var e,g;
for(var c=0,a=h.length;
c<a;
c++){e=h[c].symbolizer;
for(var b in e){g=e[b];
if(typeof g=="object"){this.addPropertyStyles(d,g)
}else{this.addPropertyStyles(d,e);
break
}}}return d
},addPropertyStyles:function(b,c){var d;
for(var a in c){d=c[a];
if(typeof d=="string"&&d.match(/\$\{\w+\}/)){b[a]=true
}}return b
},addRules:function(a){this.rules=this.rules.concat(a);
this.propertyStyles=this.findPropertyStyles()
},setDefaultStyle:function(a){this.defaultStyle=a;
this.propertyStyles=this.findPropertyStyles()
},getSymbolizerPrefix:function(d){var c=OpenLayers.Style.SYMBOLIZER_PREFIXES;
for(var b=0,a=c.length;
b<a;
b++){if(d.CLASS_NAME.indexOf(c[b])!=-1){return c[b]
}}},CLASS_NAME:"OpenLayers.Style"});
OpenLayers.Style.createLiteral=function(d,b,a,c){if(typeof d=="string"&&d.indexOf("${")!=-1){d=OpenLayers.String.format(d,b,[a,c]);
d=(isNaN(d)||!d)?d:parseFloat(d)
}return d
};
OpenLayers.Style.SYMBOLIZER_PREFIXES=["Point","Line","Polygon","Text"];OpenLayers.Filter=OpenLayers.Class({initialize:function(a){OpenLayers.Util.extend(this,a)
},destroy:function(){},evaluate:function(a){return true
},clone:function(){return null
},CLASS_NAME:"OpenLayers.Filter"});OpenLayers.Filter.FeatureId=OpenLayers.Class(OpenLayers.Filter,{fids:null,initialize:function(a){this.fids=[];
OpenLayers.Filter.prototype.initialize.apply(this,[a])
},evaluate:function(c){for(var b=0,a=this.fids.length;
b<a;
b++){var d=c.fid||c.id;
if(d==this.fids[b]){return true
}}return false
},clone:function(){var a=new OpenLayers.Filter.FeatureId();
OpenLayers.Util.extend(a,this);
a.fids=this.fids.slice();
return a
},CLASS_NAME:"OpenLayers.Filter.FeatureId"});OpenLayers.Filter.Logical=OpenLayers.Class(OpenLayers.Filter,{filters:null,type:null,initialize:function(a){this.filters=[];
OpenLayers.Filter.prototype.initialize.apply(this,[a])
},destroy:function(){this.filters=null;
OpenLayers.Filter.prototype.destroy.apply(this)
},evaluate:function(c){switch(this.type){case OpenLayers.Filter.Logical.AND:for(var b=0,a=this.filters.length;
b<a;
b++){if(this.filters[b].evaluate(c)==false){return false
}}return true;
case OpenLayers.Filter.Logical.OR:for(var b=0,a=this.filters.length;
b<a;
b++){if(this.filters[b].evaluate(c)==true){return true
}}return false;
case OpenLayers.Filter.Logical.NOT:return(!this.filters[0].evaluate(c))
}},clone:function(){var c=[];
for(var b=0,a=this.filters.length;
b<a;
++b){c.push(this.filters[b].clone())
}return new OpenLayers.Filter.Logical({type:this.type,filters:c})
},CLASS_NAME:"OpenLayers.Filter.Logical"});
OpenLayers.Filter.Logical.AND="&&";
OpenLayers.Filter.Logical.OR="||";
OpenLayers.Filter.Logical.NOT="!";OpenLayers.Filter.Comparison=OpenLayers.Class(OpenLayers.Filter,{type:null,property:null,value:null,matchCase:true,lowerBoundary:null,upperBoundary:null,initialize:function(a){OpenLayers.Filter.prototype.initialize.apply(this,[a])
},evaluate:function(c){var a=false;
switch(this.type){case OpenLayers.Filter.Comparison.EQUAL_TO:var b=c[this.property];
var e=this.value;
if(!this.matchCase&&typeof b=="string"&&typeof e=="string"){a=(b.toUpperCase()==e.toUpperCase())
}else{a=(b==e)
}break;
case OpenLayers.Filter.Comparison.NOT_EQUAL_TO:var b=c[this.property];
var e=this.value;
if(!this.matchCase&&typeof b=="string"&&typeof e=="string"){a=(b.toUpperCase()!=e.toUpperCase())
}else{a=(b!=e)
}break;
case OpenLayers.Filter.Comparison.LESS_THAN:a=c[this.property]<this.value;
break;
case OpenLayers.Filter.Comparison.GREATER_THAN:a=c[this.property]>this.value;
break;
case OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO:a=c[this.property]<=this.value;
break;
case OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO:a=c[this.property]>=this.value;
break;
case OpenLayers.Filter.Comparison.BETWEEN:a=(c[this.property]>=this.lowerBoundary)&&(c[this.property]<=this.upperBoundary);
break;
case OpenLayers.Filter.Comparison.LIKE:var d=new RegExp(this.value,"gi");
a=d.test(c[this.property]);
break
}return a
},value2regex:function(d,b,a){if(d=="."){var c="'.' is an unsupported wildCard character for OpenLayers.Filter.Comparison";
OpenLayers.Console.error(c);
return null
}d=d?d:"*";
b=b?b:".";
a=a?a:"!";
this.value=this.value.replace(new RegExp("\\"+a+"(.|$)","g"),"\\$1");
this.value=this.value.replace(new RegExp("\\"+b,"g"),".");
this.value=this.value.replace(new RegExp("\\"+d,"g"),".*");
this.value=this.value.replace(new RegExp("\\\\.\\*","g"),"\\"+d);
this.value=this.value.replace(new RegExp("\\\\\\.","g"),"\\"+b);
return this.value
},regex2value:function(){var a=this.value;
a=a.replace(/!/g,"!!");
a=a.replace(/(\\)?\\\./g,function(c,b){return b?c:"!."
});
a=a.replace(/(\\)?\\\*/g,function(c,b){return b?c:"!*"
});
a=a.replace(/\\\\/g,"\\");
a=a.replace(/\.\*/g,"*");
return a
},clone:function(){return OpenLayers.Util.extend(new OpenLayers.Filter.Comparison(),this)
},CLASS_NAME:"OpenLayers.Filter.Comparison"});
OpenLayers.Filter.Comparison.EQUAL_TO="==";
OpenLayers.Filter.Comparison.NOT_EQUAL_TO="!=";
OpenLayers.Filter.Comparison.LESS_THAN="<";
OpenLayers.Filter.Comparison.GREATER_THAN=">";
OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO="<=";
OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO=">=";
OpenLayers.Filter.Comparison.BETWEEN="..";
OpenLayers.Filter.Comparison.LIKE="~";OpenLayers.Format.Filter=OpenLayers.Class(OpenLayers.Format.XML,{defaultVersion:"1.0.0",version:null,parser:null,initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},write:function(c,b){var a=(b&&b.version)||this.version||this.defaultVersion;
if(!this.parser||this.parser.VERSION!=a){var d=OpenLayers.Format.Filter["v"+a.replace(/\./g,"_")];
if(!d){throw"Can't find a Filter parser for version "+a
}this.parser=new d(this.options)
}return this.parser.write(c)
},read:function(c){if(typeof c=="string"){c=OpenLayers.Format.XML.prototype.read.apply(this,[c])
}var a=this.version;
if(!a){a=this.defaultVersion
}if(!this.parser||this.parser.VERSION!=a){var d=OpenLayers.Format.Filter["v"+a.replace(/\./g,"_")];
if(!d){throw"Can't find a Filter parser for version "+a
}this.parser=new d(this.options)
}var b=this.parser.read(c);
return b
},CLASS_NAME:"OpenLayers.Format.Filter"});OpenLayers.Protocol=OpenLayers.Class({format:null,options:null,autoDestroy:true,defaultFilter:null,initialize:function(a){a=a||{};
OpenLayers.Util.extend(this,a);
this.options=a
},mergeWithDefaultFilter:function(a){if(a){if(this.defaultFilter){a=new OpenLayers.Filter.Logical({type:OpenLayers.Filter.Logical.AND,filters:[this.defaultFilter,a]})
}}else{a=this.defaultFilter
}return a
},destroy:function(){this.options=null;
this.format=null
},read:function(a){a=a||{};
a.filter=this.mergeWithDefaultFilter(a.filter)
},create:function(){},update:function(){},"delete":function(){},commit:function(){},abort:function(a){},createCallback:function(c,a,b){return OpenLayers.Function.bind(function(){c.apply(this,[a,b])
},this)
},CLASS_NAME:"OpenLayers.Protocol"});
OpenLayers.Protocol.Response=OpenLayers.Class({code:null,requestType:null,last:true,features:null,reqFeatures:null,priv:null,initialize:function(a){OpenLayers.Util.extend(this,a)
},success:function(){return this.code>0
},CLASS_NAME:"OpenLayers.Protocol.Response"});
OpenLayers.Protocol.Response.SUCCESS=1;
OpenLayers.Protocol.Response.FAILURE=0;OpenLayers.Protocol.HTTP=OpenLayers.Class(OpenLayers.Protocol,{url:null,headers:null,params:null,callback:null,scope:null,readWithPOST:false,wildcarded:false,initialize:function(a){a=a||{};
this.params={};
this.headers={};
OpenLayers.Protocol.prototype.initialize.apply(this,arguments)
},destroy:function(){this.params=null;
this.headers=null;
OpenLayers.Protocol.prototype.destroy.apply(this)
},read:function(a){OpenLayers.Protocol.prototype.read.apply(this,arguments);
a=OpenLayers.Util.applyDefaults(a,this.options);
a.params=OpenLayers.Util.applyDefaults(a.params,this.options.params);
if(a.filter){a.params=this.filterToParams(a.filter,a.params)
}var b=(a.readWithPOST!==undefined)?a.readWithPOST:this.readWithPOST;
var c=new OpenLayers.Protocol.Response({requestType:"read"});
if(b){c.priv=OpenLayers.Request.POST({url:a.url,callback:this.createCallback(this.handleRead,c,a),data:OpenLayers.Util.getParameterString(a.params),headers:{"Content-Type":"application/x-www-form-urlencoded"}})
}else{c.priv=OpenLayers.Request.GET({url:a.url,callback:this.createCallback(this.handleRead,c,a),params:a.params,headers:a.headers})
}return c
},handleRead:function(b,a){this.handleResponse(b,a)
},filterToParams:function(d,g){g=g||{};
var c=d.CLASS_NAME;
var e=c.substring(c.lastIndexOf(".")+1);
switch(e){case"Spatial":switch(d.type){case OpenLayers.Filter.Spatial.BBOX:g.bbox=d.value.toArray();
break;
case OpenLayers.Filter.Spatial.DWITHIN:g.tolerance=d.distance;
case OpenLayers.Filter.Spatial.WITHIN:g.lon=d.value.x;
g.lat=d.value.y;
break;
default:OpenLayers.Console.warn("Unknown spatial filter type "+d.type)
}break;
case"Comparison":var h=OpenLayers.Protocol.HTTP.COMP_TYPE_TO_OP_STR[d.type];
if(h!==undefined){var f=d.value;
if(d.type==OpenLayers.Filter.Comparison.LIKE){f=this.regex2value(f);
if(this.wildcarded){f="%"+f+"%"
}}g[d.property+"__"+h]=f;
g.queryable=g.queryable||[];
g.queryable.push(d.property)
}else{OpenLayers.Console.warn("Unknown comparison filter type "+d.type)
}break;
case"Logical":if(d.type===OpenLayers.Filter.Logical.AND){for(var b=0,a=d.filters.length;
b<a;
b++){g=this.filterToParams(d.filters[b],g)
}}else{OpenLayers.Console.warn("Unsupported logical filter type "+d.type)
}break;
default:OpenLayers.Console.warn("Unknown filter type "+e)
}return g
},regex2value:function(a){a=a.replace(/%/g,"\\%");
a=a.replace(/\\\\\.(\*)?/g,function(c,b){return b?c:"\\\\_"
});
a=a.replace(/\\\\\.\*/g,"\\\\%");
a=a.replace(/(\\)?\.(\*)?/g,function(c,b,d){return b||d?c:"_"
});
a=a.replace(/(\\)?\.\*/g,function(c,b){return b?c:"%"
});
a=a.replace(/\\\./g,".");
a=a.replace(/(\\)?\\\*/g,function(c,b){return b?c:"*"
});
return a
},create:function(b,a){a=OpenLayers.Util.applyDefaults(a,this.options);
var c=new OpenLayers.Protocol.Response({reqFeatures:b,requestType:"create"});
c.priv=OpenLayers.Request.POST({url:a.url,callback:this.createCallback(this.handleCreate,c,a),headers:a.headers,data:this.format.write(b)});
return c
},handleCreate:function(b,a){this.handleResponse(b,a)
},update:function(c,b){b=b||{};
var a=b.url||c.url||this.options.url+"/"+c.fid;
b=OpenLayers.Util.applyDefaults(b,this.options);
var d=new OpenLayers.Protocol.Response({reqFeatures:c,requestType:"update"});
d.priv=OpenLayers.Request.PUT({url:a,callback:this.createCallback(this.handleUpdate,d,b),headers:b.headers,data:this.format.write(c)});
return d
},handleUpdate:function(b,a){this.handleResponse(b,a)
},"delete":function(c,b){b=b||{};
var a=b.url||c.url||this.options.url+"/"+c.fid;
b=OpenLayers.Util.applyDefaults(b,this.options);
var d=new OpenLayers.Protocol.Response({reqFeatures:c,requestType:"delete"});
d.priv=OpenLayers.Request.DELETE({url:a,callback:this.createCallback(this.handleDelete,d,b),headers:b.headers});
return d
},handleDelete:function(b,a){this.handleResponse(b,a)
},handleResponse:function(c,a){var b=c.priv;
if(a.callback){if(b.status>=200&&b.status<300){if(c.requestType!="delete"){c.features=this.parseFeatures(b)
}c.code=OpenLayers.Protocol.Response.SUCCESS
}else{c.code=OpenLayers.Protocol.Response.FAILURE
}a.callback.call(a.scope,c)
}},parseFeatures:function(a){var b=a.responseXML;
if(!b||!b.documentElement){b=a.responseText
}if(!b||b.length<=0){return null
}return this.format.read(b)
},commit:function(b,q){q=OpenLayers.Util.applyDefaults(q,this.options);
var d=[],m=0;
var k={};
k[OpenLayers.State.INSERT]=[];
k[OpenLayers.State.UPDATE]=[];
k[OpenLayers.State.DELETE]=[];
var p,l,c=[];
for(var e=0,j=b.length;
e<j;
++e){p=b[e];
l=k[p.state];
if(l){l.push(p);
c.push(p)
}}var g=(k[OpenLayers.State.INSERT].length>0?1:0)+k[OpenLayers.State.UPDATE].length+k[OpenLayers.State.DELETE].length;
var o=true;
var a=new OpenLayers.Protocol.Response({reqFeatures:c});
function h(s){var r=s.features?s.features.length:0;
var u=new Array(r);
for(var t=0;
t<r;
++t){u[t]=s.features[t].fid
}a.insertIds=u;
n.apply(this,[s])
}function n(i){this.callUserCallback(i,q);
o=o&&i.success();
m++;
if(m>=g){if(q.callback){a.code=o?OpenLayers.Protocol.Response.SUCCESS:OpenLayers.Protocol.Response.FAILURE;
q.callback.apply(q.scope,[a])
}}}var f=k[OpenLayers.State.INSERT];
if(f.length>0){d.push(this.create(f,OpenLayers.Util.applyDefaults({callback:h,scope:this},q.create)))
}f=k[OpenLayers.State.UPDATE];
for(var e=f.length-1;
e>=0;
--e){d.push(this.update(f[e],OpenLayers.Util.applyDefaults({callback:n,scope:this},q.update)))
}f=k[OpenLayers.State.DELETE];
for(var e=f.length-1;
e>=0;
--e){d.push(this["delete"](f[e],OpenLayers.Util.applyDefaults({callback:n,scope:this},q["delete"])))
}return d
},abort:function(a){if(a){a.priv.abort()
}},callUserCallback:function(c,a){var b=a[c.requestType];
if(b&&b.callback){b.callback.call(b.scope,c)
}},CLASS_NAME:"OpenLayers.Protocol.HTTP"});
(function(){var a=OpenLayers.Protocol.HTTP.COMP_TYPE_TO_OP_STR={};
a[OpenLayers.Filter.Comparison.EQUAL_TO]="eq";
a[OpenLayers.Filter.Comparison.NOT_EQUAL_TO]="ne";
a[OpenLayers.Filter.Comparison.LESS_THAN]="lt";
a[OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO]="lte";
a[OpenLayers.Filter.Comparison.GREATER_THAN]="gt";
a[OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO]="gte";
a[OpenLayers.Filter.Comparison.LIKE]="ilike"
})();OpenLayers.Renderer=OpenLayers.Class({container:null,root:null,extent:null,locked:false,size:null,resolution:null,map:null,initialize:function(a,b){this.container=OpenLayers.Util.getElement(a)
},destroy:function(){this.container=null;
this.extent=null;
this.size=null;
this.resolution=null;
this.map=null
},supported:function(){return false
},setExtent:function(a,b){this.extent=a.clone();
if(b){this.resolution=null
}},setSize:function(a){this.size=a.clone();
this.resolution=null
},getResolution:function(){this.resolution=this.resolution||this.map.getResolution();
return this.resolution
},drawFeature:function(c,d){if(d==null){d=c.style
}if(c.geometry){var e=c.geometry.getBounds();
if(e){if(!e.intersectsBounds(this.extent)){d={display:"none"}
}var f=this.drawGeometry(c.geometry,d,c.id);
if(d.display!="none"&&d.label&&f!==false){var a=c.geometry.getCentroid();
if(d.labelXOffset||d.labelYOffset){xOffset=isNaN(d.labelXOffset)?0:d.labelXOffset;
yOffset=isNaN(d.labelYOffset)?0:d.labelYOffset;
var b=this.getResolution();
a.move(xOffset*b,yOffset*b)
}this.drawText(c.id,d,a)
}else{this.removeText(c.id)
}return f
}}},drawGeometry:function(c,a,b){},drawText:function(c,b,a){},removeText:function(a){},clear:function(){},getFeatureIdFromEvent:function(a){},eraseFeatures:function(c){if(!(c instanceof Array)){c=[c]
}for(var b=0,a=c.length;
b<a;
++b){this.eraseGeometry(c[b].geometry);
this.removeText(c[b].id)
}},eraseGeometry:function(a){},moveRoot:function(a){},getRenderLayerId:function(){return this.container.id
},CLASS_NAME:"OpenLayers.Renderer"});OpenLayers.ElementsIndexer=OpenLayers.Class({maxZIndex:null,order:null,indices:null,compare:null,initialize:function(a){this.compare=a?OpenLayers.ElementsIndexer.IndexingMethods.Z_ORDER_Y_ORDER:OpenLayers.ElementsIndexer.IndexingMethods.Z_ORDER_DRAWING_ORDER;
this.order=[];
this.indices={};
this.maxZIndex=0
},insert:function(c){if(this.exists(c)){this.remove(c)
}var f=c.id;
this.determineZIndex(c);
var d=-1;
var e=this.order.length;
var a;
while(e-d>1){a=parseInt((d+e)/2);
var b=this.compare(this,c,OpenLayers.Util.getElement(this.order[a]));
if(b>0){d=a
}else{e=a
}}this.order.splice(e,0,f);
this.indices[f]=this.getZIndex(c);
return this.getNextElement(e)
},remove:function(b){var d=b.id;
var a=OpenLayers.Util.indexOf(this.order,d);
if(a>=0){this.order.splice(a,1);
delete this.indices[d];
if(this.order.length>0){var c=this.order[this.order.length-1];
this.maxZIndex=this.indices[c]
}else{this.maxZIndex=0
}}},clear:function(){this.order=[];
this.indices={};
this.maxZIndex=0
},exists:function(a){return(this.indices[a.id]!=null)
},getZIndex:function(a){return a._style.graphicZIndex
},determineZIndex:function(a){var b=a._style.graphicZIndex;
if(b==null){b=this.maxZIndex;
a._style.graphicZIndex=b
}else{if(b>this.maxZIndex){this.maxZIndex=b
}}},getNextElement:function(b){var a=b+1;
if(a<this.order.length){var c=OpenLayers.Util.getElement(this.order[a]);
if(c==undefined){c=this.getNextElement(a)
}return c
}else{return null
}},CLASS_NAME:"OpenLayers.ElementsIndexer"});
OpenLayers.ElementsIndexer.IndexingMethods={Z_ORDER:function(e,d,b){var a=e.getZIndex(d);
var f=0;
if(b){var c=e.getZIndex(b);
f=a-c
}return f
},Z_ORDER_DRAWING_ORDER:function(c,b,a){var d=OpenLayers.ElementsIndexer.IndexingMethods.Z_ORDER(c,b,a);
if(a&&d==0){d=1
}return d
},Z_ORDER_Y_ORDER:function(d,c,b){var e=OpenLayers.ElementsIndexer.IndexingMethods.Z_ORDER(d,c,b);
if(b&&e===0){var a=b._boundsBottom-c._boundsBottom;
e=(a===0)?1:a
}return e
}};
OpenLayers.Renderer.Elements=OpenLayers.Class(OpenLayers.Renderer,{rendererRoot:null,root:null,vectorRoot:null,textRoot:null,xmlns:null,indexer:null,BACKGROUND_ID_SUFFIX:"_background",LABEL_ID_SUFFIX:"_label",minimumSymbolizer:{strokeLinecap:"round",strokeOpacity:1,strokeDashstyle:"solid",fillOpacity:1,pointRadius:0},initialize:function(a,b){OpenLayers.Renderer.prototype.initialize.apply(this,arguments);
this.rendererRoot=this.createRenderRoot();
this.root=this.createRoot("_root");
this.vectorRoot=this.createRoot("_vroot");
this.textRoot=this.createRoot("_troot");
this.root.appendChild(this.vectorRoot);
this.root.appendChild(this.textRoot);
this.rendererRoot.appendChild(this.root);
this.container.appendChild(this.rendererRoot);
if(b&&(b.zIndexing||b.yOrdering)){this.indexer=new OpenLayers.ElementsIndexer(b.yOrdering)
}},destroy:function(){this.clear();
this.rendererRoot=null;
this.root=null;
this.xmlns=null;
OpenLayers.Renderer.prototype.destroy.apply(this,arguments)
},clear:function(){if(this.vectorRoot){while(this.vectorRoot.childNodes.length>0){this.vectorRoot.removeChild(this.vectorRoot.firstChild)
}}if(this.textRoot){while(this.textRoot.childNodes.length>0){this.textRoot.removeChild(this.textRoot.firstChild)
}}if(this.indexer){this.indexer.clear()
}},getNodeType:function(b,a){},drawGeometry:function(g,d,f){var c=g.CLASS_NAME;
var h=true;
if((c=="OpenLayers.Geometry.Collection")||(c=="OpenLayers.Geometry.MultiPoint")||(c=="OpenLayers.Geometry.MultiLineString")||(c=="OpenLayers.Geometry.MultiPolygon")){for(var b=0,a=g.components.length;
b<a;
b++){h=this.drawGeometry(g.components[b],d,f)&&h
}return h
}h=false;
if(d.display!="none"){if(d.backgroundGraphic){this.redrawBackgroundNode(g.id,g,d,f)
}h=this.redrawNode(g.id,g,d,f)
}if(h==false){var e=document.getElementById(g.id);
if(e){if(e._style.backgroundGraphic){e.parentNode.removeChild(document.getElementById(g.id+this.BACKGROUND_ID_SUFFIX))
}e.parentNode.removeChild(e)
}}return h
},redrawNode:function(g,f,b,e){var c=this.nodeFactory(g,this.getNodeType(f,b));
c._featureId=e;
c._boundsBottom=f.getBounds().bottom;
c._geometryClass=f.CLASS_NAME;
c._style=b;
var a=this.drawGeometryNode(c,f,b);
if(a===false){return false
}c=a.node;
if(this.indexer){var d=this.indexer.insert(c);
if(d){this.vectorRoot.insertBefore(c,d)
}else{this.vectorRoot.appendChild(c)
}}else{if(c.parentNode!==this.vectorRoot){this.vectorRoot.appendChild(c)
}}this.postDraw(c);
return a.complete
},redrawBackgroundNode:function(e,d,b,c){var a=OpenLayers.Util.extend({},b);
a.externalGraphic=a.backgroundGraphic;
a.graphicXOffset=a.backgroundXOffset;
a.graphicYOffset=a.backgroundYOffset;
a.graphicZIndex=a.backgroundGraphicZIndex;
a.graphicWidth=a.backgroundWidth||a.graphicWidth;
a.graphicHeight=a.backgroundHeight||a.graphicHeight;
a.backgroundGraphic=null;
a.backgroundXOffset=null;
a.backgroundYOffset=null;
a.backgroundGraphicZIndex=null;
return this.redrawNode(e+this.BACKGROUND_ID_SUFFIX,d,a,null)
},drawGeometryNode:function(c,e,b){b=b||c._style;
OpenLayers.Util.applyDefaults(b,this.minimumSymbolizer);
var a={isFilled:b.fill===undefined?true:b.fill,isStroked:b.stroke===undefined?!!b.strokeWidth:b.stroke};
var d;
switch(e.CLASS_NAME){case"OpenLayers.Geometry.Point":if(b.graphic===false){a.isFilled=false;
a.isStroked=false
}d=this.drawPoint(c,e);
break;
case"OpenLayers.Geometry.LineString":a.isFilled=false;
d=this.drawLineString(c,e);
break;
case"OpenLayers.Geometry.LinearRing":d=this.drawLinearRing(c,e);
break;
case"OpenLayers.Geometry.Polygon":d=this.drawPolygon(c,e);
break;
case"OpenLayers.Geometry.Surface":d=this.drawSurface(c,e);
break;
case"OpenLayers.Geometry.Rectangle":d=this.drawRectangle(c,e);
break;
default:break
}c._options=a;
if(d!=false){return{node:this.setStyle(c,b,a,e),complete:d}
}else{return false
}},postDraw:function(a){},drawPoint:function(a,b){},drawLineString:function(a,b){},drawLinearRing:function(a,b){},drawPolygon:function(a,b){},drawRectangle:function(a,b){},drawCircle:function(a,b){},drawSurface:function(a,b){},removeText:function(b){var a=document.getElementById(b+this.LABEL_ID_SUFFIX);
if(a){this.textRoot.removeChild(a)
}},getFeatureIdFromEvent:function(a){var d=a.target;
var b=d&&d.correspondingUseElement;
var c=b?b:(d||a.srcElement);
var e=c._featureId;
return e
},eraseGeometry:function(f){if((f.CLASS_NAME=="OpenLayers.Geometry.MultiPoint")||(f.CLASS_NAME=="OpenLayers.Geometry.MultiLineString")||(f.CLASS_NAME=="OpenLayers.Geometry.MultiPolygon")||(f.CLASS_NAME=="OpenLayers.Geometry.Collection")){for(var d=0,a=f.components.length;
d<a;
d++){this.eraseGeometry(f.components[d])
}}else{var c=OpenLayers.Util.getElement(f.id);
if(c&&c.parentNode){if(c.geometry){c.geometry.destroy();
c.geometry=null
}c.parentNode.removeChild(c);
if(this.indexer){this.indexer.remove(c)
}if(c._style.backgroundGraphic){var b=f.id+this.BACKGROUND_ID_SUFFIX;
var e=OpenLayers.Util.getElement(b);
if(e&&e.parentNode){e.parentNode.removeChild(e)
}}}}},nodeFactory:function(c,a){var b=OpenLayers.Util.getElement(c);
if(b){if(!this.nodeTypeCompare(b,a)){b.parentNode.removeChild(b);
b=this.nodeFactory(c,a)
}}else{b=this.createNode(a,c)
}return b
},nodeTypeCompare:function(b,a){},createNode:function(a,b){},moveRoot:function(b){var a=this.root;
if(b.root.parentNode==this.rendererRoot){a=b.root
}a.parentNode.removeChild(a);
b.rendererRoot.appendChild(a)
},getRenderLayerId:function(){return this.root.parentNode.parentNode.id
},isComplexSymbol:function(a){return(a!="circle")&&!!a
},CLASS_NAME:"OpenLayers.Renderer.Elements"});
OpenLayers.Renderer.symbol={star:[350,75,379,161,469,161,397,215,423,301,350,250,277,301,303,215,231,161,321,161,350,75],cross:[4,0,6,0,6,4,10,4,10,6,6,6,6,10,4,10,4,6,0,6,0,4,4,4,4,0],x:[0,0,25,0,50,35,75,0,100,0,65,50,100,100,75,100,50,65,25,100,0,100,35,50,0,0],square:[0,0,0,1,1,1,1,0,0,0],triangle:[0,10,10,10,5,0,0,10]};OpenLayers.Handler.Feature=OpenLayers.Class(OpenLayers.Handler,{EVENTMAP:{click:{"in":"click",out:"clickout"},mousemove:{"in":"over",out:"out"},dblclick:{"in":"dblclick",out:null},mousedown:{"in":null,out:null},mouseup:{"in":null,out:null}},feature:null,lastFeature:null,down:null,up:null,clickTolerance:4,geometryTypes:null,stopClick:true,stopDown:true,stopUp:false,initialize:function(d,b,c,a){OpenLayers.Handler.prototype.initialize.apply(this,[d,c,a]);
this.layer=b
},mousedown:function(a){this.down=a.xy;
return this.handle(a)?!this.stopDown:true
},mouseup:function(a){this.up=a.xy;
return this.handle(a)?!this.stopUp:true
},click:function(a){return this.handle(a)?!this.stopClick:true
},mousemove:function(a){if(!this.callbacks.over&&!this.callbacks.out){return true
}this.handle(a);
return true
},dblclick:function(a){return !this.handle(a)
},geometryTypeMatches:function(a){return this.geometryTypes==null||OpenLayers.Util.indexOf(this.geometryTypes,a.geometry.CLASS_NAME)>-1
},handle:function(a){if(this.feature&&!this.feature.layer){this.feature=null
}var c=a.type;
var f=false;
var e=!!(this.feature);
var d=(c=="click"||c=="dblclick");
this.feature=this.layer.getFeatureFromEvent(a);
if(this.feature&&!this.feature.layer){this.feature=null
}if(this.lastFeature&&!this.lastFeature.layer){this.lastFeature=null
}if(this.feature){var b=(this.feature!=this.lastFeature);
if(this.geometryTypeMatches(this.feature)){if(e&&b){if(this.lastFeature){this.triggerCallback(c,"out",[this.lastFeature])
}this.triggerCallback(c,"in",[this.feature])
}else{if(!e||d){this.triggerCallback(c,"in",[this.feature])
}}this.lastFeature=this.feature;
f=true
}else{if(this.lastFeature&&(e&&b||d)){this.triggerCallback(c,"out",[this.lastFeature])
}this.feature=null
}}else{if(this.lastFeature&&(e||d)){this.triggerCallback(c,"out",[this.lastFeature])
}}return f
},triggerCallback:function(d,e,b){var c=this.EVENTMAP[d][e];
if(c){if(d=="click"&&this.up&&this.down){var a=Math.sqrt(Math.pow(this.up.x-this.down.x,2)+Math.pow(this.up.y-this.down.y,2));
if(a<=this.clickTolerance){this.callback(c,b)
}}else{this.callback(c,b)
}}},activate:function(){var a=false;
if(OpenLayers.Handler.prototype.activate.apply(this,arguments)){this.moveLayerToTop();
this.map.events.on({removelayer:this.handleMapEvents,changelayer:this.handleMapEvents,scope:this});
a=true
}return a
},deactivate:function(){var a=false;
if(OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){this.moveLayerBack();
this.feature=null;
this.lastFeature=null;
this.down=null;
this.up=null;
this.map.events.un({removelayer:this.handleMapEvents,changelayer:this.handleMapEvents,scope:this});
a=true
}return a
},handleMapEvents:function(a){if(!a.property||a.property=="order"){this.moveLayerToTop()
}},moveLayerToTop:function(){var a=Math.max(this.map.Z_INDEX_BASE.Feature-1,this.layer.getZIndex())+1;
this.layer.setZIndex(a)
},moveLayerBack:function(){var a=this.layer.getZIndex()-1;
if(a>=this.map.Z_INDEX_BASE.Feature){this.layer.setZIndex(a)
}else{this.map.setLayerZIndex(this.layer,this.map.getLayerIndex(this.layer))
}},CLASS_NAME:"OpenLayers.Handler.Feature"});OpenLayers.StyleMap=OpenLayers.Class({styles:null,extendDefault:true,initialize:function(c,a){this.styles={"default":new OpenLayers.Style(OpenLayers.Feature.Vector.style["default"]),select:new OpenLayers.Style(OpenLayers.Feature.Vector.style.select),temporary:new OpenLayers.Style(OpenLayers.Feature.Vector.style.temporary),"delete":new OpenLayers.Style(OpenLayers.Feature.Vector.style["delete"])};
if(c instanceof OpenLayers.Style){this.styles["default"]=c;
this.styles.select=c;
this.styles.temporary=c;
this.styles["delete"]=c
}else{if(typeof c=="object"){for(var b in c){if(c[b] instanceof OpenLayers.Style){this.styles[b]=c[b]
}else{if(typeof c[b]=="object"){this.styles[b]=new OpenLayers.Style(c[b])
}else{this.styles["default"]=new OpenLayers.Style(c);
this.styles.select=new OpenLayers.Style(c);
this.styles.temporary=new OpenLayers.Style(c);
this.styles["delete"]=new OpenLayers.Style(c);
break
}}}}}OpenLayers.Util.extend(this,a)
},destroy:function(){for(var a in this.styles){this.styles[a].destroy()
}this.styles=null
},createSymbolizer:function(b,c){if(!b){b=new OpenLayers.Feature.Vector()
}if(!this.styles[c]){c="default"
}b.renderIntent=c;
var a={};
if(this.extendDefault&&c!="default"){a=this.styles["default"].createSymbolizer(b)
}return OpenLayers.Util.extend(a,this.styles[c].createSymbolizer(b))
},addUniqueValueRules:function(b,d,f,a){var e=[];
for(var c in f){e.push(new OpenLayers.Rule({symbolizer:f[c],context:a,filter:new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:d,value:c})}))
}this.styles[b].addRules(e)
},CLASS_NAME:"OpenLayers.StyleMap"});OpenLayers.Layer.Vector=OpenLayers.Class(OpenLayers.Layer,{EVENT_TYPES:["beforefeatureadded","beforefeaturesadded","featureadded","featuresadded","beforefeatureremoved","featureremoved","featuresremoved","beforefeatureselected","featureselected","featureunselected","beforefeaturemodified","featuremodified","afterfeaturemodified","vertexmodified","sketchstarted","sketchmodified","sketchcomplete","refresh"],isBaseLayer:false,isFixed:false,isVector:true,features:null,filter:null,selectedFeatures:null,unrenderedFeatures:null,reportError:true,style:null,styleMap:null,strategies:null,protocol:null,renderers:["SVG","VML","Canvas"],renderer:null,rendererOptions:null,geometryType:null,drawn:false,initialize:function(c,b){this.EVENT_TYPES=OpenLayers.Layer.Vector.prototype.EVENT_TYPES.concat(OpenLayers.Layer.prototype.EVENT_TYPES);
OpenLayers.Layer.prototype.initialize.apply(this,arguments);
if(!this.renderer||!this.renderer.supported()){this.assignRenderer()
}if(!this.renderer||!this.renderer.supported()){this.renderer=null;
this.displayError()
}if(!this.styleMap){this.styleMap=new OpenLayers.StyleMap()
}this.features=[];
this.selectedFeatures=[];
this.unrenderedFeatures={};
if(this.strategies){for(var d=0,a=this.strategies.length;
d<a;
d++){this.strategies[d].setLayer(this)
}}},destroy:function(){if(this.strategies){var c,b,a;
for(b=0,a=this.strategies.length;
b<a;
b++){c=this.strategies[b];
if(c.autoDestroy){c.destroy()
}}this.strategies=null
}if(this.protocol){if(this.protocol.autoDestroy){this.protocol.destroy()
}this.protocol=null
}this.destroyFeatures();
this.features=null;
this.selectedFeatures=null;
this.unrenderedFeatures=null;
if(this.renderer){this.renderer.destroy()
}this.renderer=null;
this.geometryType=null;
this.drawn=null;
OpenLayers.Layer.prototype.destroy.apply(this,arguments)
},clone:function(e){if(e==null){e=new OpenLayers.Layer.Vector(this.name,this.getOptions())
}e=OpenLayers.Layer.prototype.clone.apply(this,[e]);
var c=this.features;
var a=c.length;
var d=new Array(a);
for(var b=0;
b<a;
++b){d[b]=c[b].clone()
}e.features=d;
return e
},refresh:function(a){if(this.calculateInRange()&&this.visibility){this.events.triggerEvent("refresh",a)
}},assignRenderer:function(){for(var c=0,a=this.renderers.length;
c<a;
c++){var b=OpenLayers.Renderer[this.renderers[c]];
if(b&&b.prototype.supported()){this.renderer=new b(this.div,this.rendererOptions);
break
}}},displayError:function(){if(this.reportError){OpenLayers.Console.userError(OpenLayers.i18n("browserNotSupported",{renderers:this.renderers.join("\n")}))
}},setMap:function(a){OpenLayers.Layer.prototype.setMap.apply(this,arguments);
if(!this.renderer){this.map.removeLayer(this)
}else{this.renderer.map=this.map;
this.renderer.setSize(this.map.getSize())
}},afterAdd:function(){if(this.strategies){var c,b,a;
for(b=0,a=this.strategies.length;
b<a;
b++){c=this.strategies[b];
if(c.autoActivate){c.activate()
}}}},removeMap:function(c){if(this.strategies){var d,b,a;
for(b=0,a=this.strategies.length;
b<a;
b++){d=this.strategies[b];
if(d.autoActivate){d.deactivate()
}}}},onMapResize:function(){OpenLayers.Layer.prototype.onMapResize.apply(this,arguments);
this.renderer.setSize(this.map.getSize())
},moveTo:function(g,b,h){OpenLayers.Layer.prototype.moveTo.apply(this,arguments);
var e=true;
if(!h){this.renderer.root.style.visibility="hidden";
this.div.style.left=-parseInt(this.map.layerContainerDiv.style.left)+"px";
this.div.style.top=-parseInt(this.map.layerContainerDiv.style.top)+"px";
var f=this.map.getExtent();
e=this.renderer.setExtent(f,b);
this.renderer.root.style.visibility="visible";
if(navigator.userAgent.toLowerCase().indexOf("gecko")!=-1){this.div.scrollLeft=this.div.scrollLeft
}if(!b&&e){for(var d in this.unrenderedFeatures){var c=this.unrenderedFeatures[d];
this.drawFeature(c)
}}}if(!this.drawn||b||!e){this.drawn=true;
var c;
for(var d=0,a=this.features.length;
d<a;
d++){this.renderer.locked=(d!==(a-1));
c=this.features[d];
this.drawFeature(c)
}}},display:function(a){OpenLayers.Layer.prototype.display.apply(this,arguments);
var b=this.div.style.display;
if(b!=this.renderer.root.style.display){this.renderer.root.style.display=b
}},addFeatures:function(b,j){if(!(b instanceof Array)){b=[b]
}var g=!j||!j.silent;
if(g){var a={features:b};
var f=this.events.triggerEvent("beforefeaturesadded",a);
if(f===false){return
}b=a.features
}for(var c=0,e=b.length;
c<e;
c++){if(c!=(b.length-1)){this.renderer.locked=true
}else{this.renderer.locked=false
}var h=b[c];
if(this.geometryType&&!(h.geometry instanceof this.geometryType)){var d=OpenLayers.i18n("componentShouldBe",{geomType:this.geometryType.prototype.CLASS_NAME});
throw d
}this.features.push(h);
h.layer=this;
if(!h.style&&this.style){h.style=OpenLayers.Util.extend({},this.style)
}if(g){if(this.events.triggerEvent("beforefeatureadded",{feature:h})===false){continue
}this.preFeatureInsert(h)
}this.drawFeature(h);
if(g){this.events.triggerEvent("featureadded",{feature:h});
this.onFeatureInsert(h)
}}if(g){this.events.triggerEvent("featuresadded",{features:b})
}},removeFeatures:function(e,a){if(!e||e.length===0){return
}if(!(e instanceof Array)){e=[e]
}if(e===this.features||e===this.selectedFeatures){e=e.slice()
}var d=!a||!a.silent;
for(var c=e.length-1;
c>=0;
c--){if(c!=0&&e[c-1].geometry){this.renderer.locked=true
}else{this.renderer.locked=false
}var b=e[c];
delete this.unrenderedFeatures[b.id];
if(d){this.events.triggerEvent("beforefeatureremoved",{feature:b})
}this.features=OpenLayers.Util.removeItem(this.features,b);
b.layer=null;
if(b.geometry){this.renderer.eraseFeatures(b)
}if(OpenLayers.Util.indexOf(this.selectedFeatures,b)!=-1){OpenLayers.Util.removeItem(this.selectedFeatures,b)
}if(d){this.events.triggerEvent("featureremoved",{feature:b})
}}if(d){this.events.triggerEvent("featuresremoved",{features:e})
}},destroyFeatures:function(d,a){var c=(d==undefined);
if(c){d=this.features
}if(d){this.removeFeatures(d,a);
for(var b=d.length-1;
b>=0;
b--){d[b].destroy()
}}},drawFeature:function(a,b){if(!this.drawn){return
}if(typeof b!="object"){if(!b&&a.state===OpenLayers.State.DELETE){b="delete"
}var c=b||a.renderIntent;
b=a.style||this.style;
if(!b){b=this.styleMap.createSymbolizer(a,c)
}}if(!this.renderer.drawFeature(a,b)){this.unrenderedFeatures[a.id]=a
}else{delete this.unrenderedFeatures[a.id]
}},eraseFeatures:function(a){this.renderer.eraseFeatures(a)
},getFeatureFromEvent:function(a){if(!this.renderer){OpenLayers.Console.error(OpenLayers.i18n("getFeatureError"));
return null
}var b=this.renderer.getFeatureIdFromEvent(a);
return this.getFeatureById(b)
},getFeatureById:function(d){var c=null;
for(var b=0,a=this.features.length;
b<a;
++b){if(this.features[b].id==d){c=this.features[b];
break
}}return c
},onFeatureInsert:function(a){},preFeatureInsert:function(a){},getDataExtent:function(){var b=null;
var d=this.features;
if(d&&(d.length>0)){b=new OpenLayers.Bounds();
var e=null;
for(var c=0,a=d.length;
c<a;
c++){e=d[c].geometry;
if(e){b.extend(e.getBounds())
}}}return b
},CLASS_NAME:"OpenLayers.Layer.Vector"});OpenLayers.Layer.Markers=OpenLayers.Class(OpenLayers.Layer,{isBaseLayer:false,markers:null,drawn:false,initialize:function(b,a){OpenLayers.Layer.prototype.initialize.apply(this,arguments);
this.markers=[]
},destroy:function(){this.clearMarkers();
this.markers=null;
OpenLayers.Layer.prototype.destroy.apply(this,arguments)
},setOpacity:function(b){if(b!=this.opacity){this.opacity=b;
for(var c=0,a=this.markers.length;
c<a;
c++){this.markers[c].setOpacity(this.opacity)
}}},moveTo:function(d,b,e){OpenLayers.Layer.prototype.moveTo.apply(this,arguments);
if(b||!this.drawn){for(var c=0,a=this.markers.length;
c<a;
c++){this.drawMarker(this.markers[c])
}this.drawn=true
}},addMarker:function(a){this.markers.push(a);
if(this.opacity!=null){a.setOpacity(this.opacity)
}if(this.map&&this.map.getExtent()){a.map=this.map;
this.drawMarker(a)
}},removeMarker:function(a){if(this.markers&&this.markers.length){OpenLayers.Util.removeItem(this.markers,a);
a.erase()
}},clearMarkers:function(){if(this.markers!=null){while(this.markers.length>0){this.removeMarker(this.markers[0])
}}},drawMarker:function(a){var b=this.map.getLayerPxFromLonLat(a.lonlat);
if(b==null){a.display(false)
}else{if(!a.isDrawn()){var c=a.draw(b);
this.div.appendChild(c)
}else{if(a.icon){a.icon.moveTo(b)
}}}},getDataExtent:function(){var b=null;
if(this.markers&&(this.markers.length>0)){var b=new OpenLayers.Bounds();
for(var d=0,a=this.markers.length;
d<a;
d++){var c=this.markers[d];
b.extend(c.lonlat)
}}return b
},CLASS_NAME:"OpenLayers.Layer.Markers"});OpenLayers.Layer.Vector.RootContainer=OpenLayers.Class(OpenLayers.Layer.Vector,{displayInLayerSwitcher:false,layers:null,initialize:function(b,a){OpenLayers.Layer.Vector.prototype.initialize.apply(this,arguments)
},display:function(){},getFeatureFromEvent:function(a){var d=this.layers;
var c;
for(var b=0;
b<d.length;
b++){c=d[b].getFeatureFromEvent(a);
if(c){return c
}}},setMap:function(a){OpenLayers.Layer.Vector.prototype.setMap.apply(this,arguments);
this.collectRoots();
a.events.register("changelayer",this,this.handleChangeLayer)
},removeMap:function(a){a.events.unregister("changelayer",this,this.handleChangeLayer);
this.resetRoots();
OpenLayers.Layer.Vector.prototype.removeMap.apply(this,arguments)
},collectRoots:function(){var b;
for(var a=0;
a<this.map.layers.length;
++a){b=this.map.layers[a];
if(OpenLayers.Util.indexOf(this.layers,b)!=-1){b.renderer.moveRoot(this.renderer)
}}},resetRoots:function(){var b;
for(var a=0;
a<this.layers.length;
++a){b=this.layers[a];
if(this.renderer&&b.renderer.getRenderLayerId()==this.id){this.renderer.moveRoot(b.renderer)
}}},handleChangeLayer:function(a){var b=a.layer;
if(a.property=="order"&&OpenLayers.Util.indexOf(this.layers,b)!=-1){this.resetRoots();
this.collectRoots()
}},CLASS_NAME:"OpenLayers.Layer.Vector.RootContainer"});OpenLayers.Control.SelectFeature=OpenLayers.Class(OpenLayers.Control,{EVENT_TYPES:["beforefeaturehighlighted","featurehighlighted","featureunhighlighted"],multipleKey:null,toggleKey:null,multiple:false,clickout:true,toggle:false,hover:false,highlightOnly:false,box:false,onBeforeSelect:function(){},onSelect:function(){},onUnselect:function(){},scope:null,geometryTypes:null,layer:null,layers:null,callbacks:null,selectStyle:null,renderIntent:"select",handlers:null,initialize:function(c,a){this.EVENT_TYPES=OpenLayers.Control.SelectFeature.prototype.EVENT_TYPES.concat(OpenLayers.Control.prototype.EVENT_TYPES);
OpenLayers.Control.prototype.initialize.apply(this,[a]);
if(this.scope===null){this.scope=this
}this.initLayer(c);
var b={click:this.clickFeature,clickout:this.clickoutFeature};
if(this.hover){b.over=this.overFeature;
b.out=this.outFeature
}this.callbacks=OpenLayers.Util.extend(b,this.callbacks);
this.handlers={feature:new OpenLayers.Handler.Feature(this,this.layer,this.callbacks,{geometryTypes:this.geometryTypes})};
if(this.box){this.handlers.box=new OpenLayers.Handler.Box(this,{done:this.selectBox},{boxDivClassName:"olHandlerBoxSelectFeature"})
}},initLayer:function(a){if(a instanceof Array){this.layers=a;
this.layer=new OpenLayers.Layer.Vector.RootContainer(this.id+"_container",{layers:a})
}else{this.layer=a
}},destroy:function(){if(this.active&&this.layers){this.map.removeLayer(this.layer)
}OpenLayers.Control.prototype.destroy.apply(this,arguments);
if(this.layers){this.layer.destroy()
}},activate:function(){if(!this.active){if(this.layers){this.map.addLayer(this.layer)
}this.handlers.feature.activate();
if(this.box&&this.handlers.box){this.handlers.box.activate()
}}return OpenLayers.Control.prototype.activate.apply(this,arguments)
},deactivate:function(){if(this.active){this.handlers.feature.deactivate();
if(this.handlers.box){this.handlers.box.deactivate()
}if(this.layers){this.map.removeLayer(this.layer)
}}return OpenLayers.Control.prototype.deactivate.apply(this,arguments)
},unselectAll:function(b){var f=this.layers||[this.layer];
var e,d;
for(var a=0;
a<f.length;
++a){e=f[a];
for(var c=e.selectedFeatures.length-1;
c>=0;
--c){d=e.selectedFeatures[c];
if(!b||b.except!=d){this.unselect(d)
}}}},clickFeature:function(a){if(!this.hover){var b=(OpenLayers.Util.indexOf(a.layer.selectedFeatures,a)>-1);
if(b){if(this.toggleSelect()){this.unselect(a)
}else{if(!this.multipleSelect()){this.unselectAll({except:a})
}}}else{if(!this.multipleSelect()){this.unselectAll({except:a})
}this.select(a)
}}},multipleSelect:function(){return this.multiple||(this.handlers.feature.evt&&this.handlers.feature.evt[this.multipleKey])
},toggleSelect:function(){return this.toggle||(this.handlers.feature.evt&&this.handlers.feature.evt[this.toggleKey])
},clickoutFeature:function(a){if(!this.hover&&this.clickout){this.unselectAll()
}},overFeature:function(b){var a=b.layer;
if(this.hover){if(this.highlightOnly){this.highlight(b)
}else{if(OpenLayers.Util.indexOf(a.selectedFeatures,b)==-1){this.select(b)
}}}},outFeature:function(a){if(this.hover){if(this.highlightOnly){if(a._lastHighlighter==this.id){if(a._prevHighlighter&&a._prevHighlighter!=this.id){delete a._lastHighlighter;
var b=this.map.getControl(a._prevHighlighter);
if(b){b.highlight(a)
}}else{this.unhighlight(a)
}}}else{this.unselect(a)
}}},highlight:function(c){var b=c.layer;
var a=this.events.triggerEvent("beforefeaturehighlighted",{feature:c});
if(a!==false){c._prevHighlighter=c._lastHighlighter;
c._lastHighlighter=this.id;
var d=this.selectStyle||this.renderIntent;
b.drawFeature(c,d);
this.events.triggerEvent("featurehighlighted",{feature:c})
}},unhighlight:function(b){var a=b.layer;
b._lastHighlighter=b._prevHighlighter;
delete b._prevHighlighter;
a.drawFeature(b,b.style||b.layer.style||"default");
this.events.triggerEvent("featureunhighlighted",{feature:b})
},select:function(c){var a=this.onBeforeSelect.call(this.scope,c);
var b=c.layer;
if(a!==false){a=b.events.triggerEvent("beforefeatureselected",{feature:c});
if(a!==false){b.selectedFeatures.push(c);
this.highlight(c);
if(!this.handlers.feature.lastFeature){this.handlers.feature.lastFeature=b.selectedFeatures[0]
}b.events.triggerEvent("featureselected",{feature:c});
this.onSelect.call(this.scope,c)
}}},unselect:function(b){var a=b.layer;
this.unhighlight(b);
OpenLayers.Util.removeItem(a.selectedFeatures,b);
a.events.triggerEvent("featureunselected",{feature:b});
this.onUnselect.call(this.scope,b)
},selectBox:function(e){if(e instanceof OpenLayers.Bounds){var h=this.map.getLonLatFromPixel(new OpenLayers.Pixel(e.left,e.bottom));
var k=this.map.getLonLatFromPixel(new OpenLayers.Pixel(e.right,e.top));
var a=new OpenLayers.Bounds(h.lon,h.lat,k.lon,k.lat);
if(!this.multipleSelect()){this.unselectAll()
}var j=this.multiple;
this.multiple=true;
var d=this.layers||[this.layer];
var f;
for(var b=0;
b<d.length;
++b){f=d[b];
for(var c=0,g=f.features.length;
c<g;
++c){var m=f.features[c];
if(!m.getVisibility()){continue
}if(this.geometryTypes==null||OpenLayers.Util.indexOf(this.geometryTypes,m.geometry.CLASS_NAME)>-1){if(a.toGeometry().intersects(m.geometry)){if(OpenLayers.Util.indexOf(f.selectedFeatures,m)==-1){this.select(m)
}}}}}this.multiple=j
}},setMap:function(a){this.handlers.feature.setMap(a);
if(this.box){this.handlers.box.setMap(a)
}OpenLayers.Control.prototype.setMap.apply(this,arguments)
},setLayer:function(b){var a=this.active;
this.unselectAll();
this.deactivate();
if(this.layers){this.layer.destroy();
this.layers=null
}this.initLayer(b);
this.handlers.feature.layer=this.layer;
if(a){this.activate()
}},CLASS_NAME:"OpenLayers.Control.SelectFeature"});OpenLayers.Control.NavigationHistory=OpenLayers.Class(OpenLayers.Control,{type:OpenLayers.Control.TYPE_TOGGLE,previous:null,previousOptions:null,next:null,nextOptions:null,limit:50,autoActivate:true,clearOnDeactivate:false,registry:null,nextStack:null,previousStack:null,listeners:null,restoring:false,initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,[a]);
this.registry=OpenLayers.Util.extend({moveend:this.getState},this.registry);
var b={trigger:OpenLayers.Function.bind(this.previousTrigger,this),displayClass:this.displayClass+" "+this.displayClass+"Previous"};
OpenLayers.Util.extend(b,this.previousOptions);
this.previous=new OpenLayers.Control.Button(b);
var c={trigger:OpenLayers.Function.bind(this.nextTrigger,this),displayClass:this.displayClass+" "+this.displayClass+"Next"};
OpenLayers.Util.extend(c,this.nextOptions);
this.next=new OpenLayers.Control.Button(c);
this.clear()
},onPreviousChange:function(b,a){if(b&&!this.previous.active){this.previous.activate()
}else{if(!b&&this.previous.active){this.previous.deactivate()
}}},onNextChange:function(b,a){if(b&&!this.next.active){this.next.activate()
}else{if(!b&&this.next.active){this.next.deactivate()
}}},destroy:function(){OpenLayers.Control.prototype.destroy.apply(this);
this.previous.destroy();
this.next.destroy();
this.deactivate();
for(var a in this){this[a]=null
}},setMap:function(a){this.map=a;
this.next.setMap(a);
this.previous.setMap(a)
},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
this.next.draw();
this.previous.draw()
},previousTrigger:function(){var b=this.previousStack.shift();
var a=this.previousStack.shift();
if(a!=undefined){this.nextStack.unshift(b);
this.previousStack.unshift(a);
this.restoring=true;
this.restore(a);
this.restoring=false;
this.onNextChange(this.nextStack[0],this.nextStack.length);
this.onPreviousChange(this.previousStack[1],this.previousStack.length-1)
}else{this.previousStack.unshift(b)
}return a
},nextTrigger:function(){var a=this.nextStack.shift();
if(a!=undefined){this.previousStack.unshift(a);
this.restoring=true;
this.restore(a);
this.restoring=false;
this.onNextChange(this.nextStack[0],this.nextStack.length);
this.onPreviousChange(this.previousStack[1],this.previousStack.length-1)
}return a
},clear:function(){this.previousStack=[];
this.previous.deactivate();
this.nextStack=[];
this.next.deactivate()
},getState:function(){return{center:this.map.getCenter(),resolution:this.map.getResolution()}
},restore:function(b){var a=this.map.getZoomForResolution(b.resolution);
this.map.setCenter(b.center,a)
},setListeners:function(){this.listeners={};
for(var a in this.registry){this.listeners[a]=OpenLayers.Function.bind(function(){if(!this.restoring){var b=this.registry[a].apply(this,arguments);
this.previousStack.unshift(b);
if(this.previousStack.length>1){this.onPreviousChange(this.previousStack[1],this.previousStack.length-1)
}if(this.previousStack.length>(this.limit+1)){this.previousStack.pop()
}if(this.nextStack.length>0){this.nextStack=[];
this.onNextChange(null,0)
}}return true
},this)
}},activate:function(){var a=false;
if(this.map){if(OpenLayers.Control.prototype.activate.apply(this)){if(this.listeners==null){this.setListeners()
}for(var b in this.listeners){this.map.events.register(b,this,this.listeners[b])
}a=true;
if(this.previousStack.length==0){this.initStack()
}}}return a
},initStack:function(){if(this.map.getCenter()){this.listeners.moveend()
}},deactivate:function(){var b=false;
if(this.map){if(OpenLayers.Control.prototype.deactivate.apply(this)){for(var a in this.listeners){this.map.events.unregister(a,this,this.listeners[a])
}if(this.clearOnDeactivate){this.clear()
}b=true
}}return b
},CLASS_NAME:"OpenLayers.Control.NavigationHistory"});OpenLayers.Control.Attribution=OpenLayers.Class(OpenLayers.Control,{separator:", ",initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,arguments)
},destroy:function(){this.map.events.un({removelayer:this.updateAttribution,addlayer:this.updateAttribution,changelayer:this.updateAttribution,changebaselayer:this.updateAttribution,scope:this});
OpenLayers.Control.prototype.destroy.apply(this,arguments)
},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
this.map.events.on({changebaselayer:this.updateAttribution,changelayer:this.updateAttribution,addlayer:this.updateAttribution,removelayer:this.updateAttribution,scope:this});
this.updateAttribution();
return this.div
},updateAttribution:function(){var d=[];
if(this.map&&this.map.layers){for(var c=0,a=this.map.layers.length;
c<a;
c++){var b=this.map.layers[c];
if(b.attribution&&b.getVisibility()){if(OpenLayers.Util.indexOf(d,b.attribution)===-1){d.push(b.attribution)
}}}this.div.innerHTML=d.join(this.separator)
}},CLASS_NAME:"OpenLayers.Control.Attribution"});OpenLayers.Request={DEFAULT_CONFIG:{method:"GET",url:window.location.href,async:true,user:undefined,password:undefined,params:null,proxy:OpenLayers.ProxyHost,headers:{},data:null,callback:function(){},success:null,failure:null,scope:null},events:new OpenLayers.Events(this,null,["complete","success","failure"]),issue:function(b){var e=OpenLayers.Util.extend(this.DEFAULT_CONFIG,{proxy:OpenLayers.ProxyHost});
b=OpenLayers.Util.applyDefaults(b,e);
var d=new OpenLayers.Request.XMLHttpRequest();
var a=b.url;
if(b.params){var c=OpenLayers.Util.getParameterString(b.params);
if(c.length>0){var g=(a.indexOf("?")>-1)?"&":"?";
a+=g+c
}}if(b.proxy&&(a.indexOf("http")==0)){if(typeof b.proxy=="function"){a=b.proxy(a)
}else{a=b.proxy+encodeURIComponent(a)
}}d.open(b.method,a,b.async,b.user,b.password);
for(var f in b.headers){d.setRequestHeader(f,b.headers[f])
}var i=this.events;
var h=this;
d.onreadystatechange=function(){if(d.readyState==OpenLayers.Request.XMLHttpRequest.DONE){var j=i.triggerEvent("complete",{request:d,config:b,requestUrl:a});
if(j!==false){h.runCallbacks({request:d,config:b,requestUrl:a})
}}};
if(b.async===false){d.send(b.data)
}else{window.setTimeout(function(){d.send(b.data)
},0)
}return d
},runCallbacks:function(d){var e=d.request;
var c=d.config;
var a=(c.scope)?OpenLayers.Function.bind(c.callback,c.scope):c.callback;
var f;
if(c.success){f=(c.scope)?OpenLayers.Function.bind(c.success,c.scope):c.success
}var b;
if(c.failure){b=(c.scope)?OpenLayers.Function.bind(c.failure,c.scope):c.failure
}a(e);
if(!e.status||(e.status>=200&&e.status<300)){this.events.triggerEvent("success",d);
if(f){f(e)
}}if(e.status&&(e.status<200||e.status>=300)){this.events.triggerEvent("failure",d);
if(b){b(e)
}}},GET:function(a){a=OpenLayers.Util.extend(a,{method:"GET"});
return OpenLayers.Request.issue(a)
},POST:function(a){a=OpenLayers.Util.extend(a,{method:"POST"});
a.headers=a.headers?a.headers:{};
if(!("CONTENT-TYPE" in OpenLayers.Util.upperCaseObject(a.headers))){a.headers["Content-Type"]="application/xml"
}return OpenLayers.Request.issue(a)
},PUT:function(a){a=OpenLayers.Util.extend(a,{method:"PUT"});
a.headers=a.headers?a.headers:{};
if(!("CONTENT-TYPE" in OpenLayers.Util.upperCaseObject(a.headers))){a.headers["Content-Type"]="application/xml"
}return OpenLayers.Request.issue(a)
},DELETE:function(a){a=OpenLayers.Util.extend(a,{method:"DELETE"});
return OpenLayers.Request.issue(a)
},HEAD:function(a){a=OpenLayers.Util.extend(a,{method:"HEAD"});
return OpenLayers.Request.issue(a)
},OPTIONS:function(a){a=OpenLayers.Util.extend(a,{method:"OPTIONS"});
return OpenLayers.Request.issue(a)
}};(function(){var d=window.XMLHttpRequest;
var h=!!window.controllers,e=window.document.all&&!window.opera;
function c(){this._object=d?new d:new window.ActiveXObject("Microsoft.XMLHTTP")
}if(h&&d.wrapped){c.wrapped=d.wrapped
}c.UNSENT=0;
c.OPENED=1;
c.HEADERS_RECEIVED=2;
c.LOADING=3;
c.DONE=4;
c.prototype.readyState=c.UNSENT;
c.prototype.responseText="";
c.prototype.responseXML=null;
c.prototype.status=0;
c.prototype.statusText="";
c.prototype.onreadystatechange=null;
c.onreadystatechange=null;
c.onopen=null;
c.onsend=null;
c.onabort=null;
c.prototype.open=function(l,o,k,p,j){this._async=k;
var n=this,m=this.readyState;
if(e){var i=function(){if(n._object.readyState!=c.DONE){a(n)
}};
if(k){window.attachEvent("onunload",i)
}}this._object.onreadystatechange=function(){if(h&&!k){return
}n.readyState=n._object.readyState;
g(n);
if(n._aborted){n.readyState=c.UNSENT;
return
}if(n.readyState==c.DONE){a(n);
if(e&&k){window.detachEvent("onunload",i)
}}if(m!=n.readyState){f(n)
}m=n.readyState
};
if(c.onopen){c.onopen.apply(this,arguments)
}this._object.open(l,o,k,p,j);
if(!k&&h){this.readyState=c.OPENED;
f(this)
}};
c.prototype.send=function(i){if(c.onsend){c.onsend.apply(this,arguments)
}if(i&&i.nodeType){i=window.XMLSerializer?new window.XMLSerializer().serializeToString(i):i.xml;
if(!this._headers["Content-Type"]){this._object.setRequestHeader("Content-Type","application/xml")
}}this._object.send(i);
if(h&&!this._async){this.readyState=c.OPENED;
g(this);
while(this.readyState<c.DONE){this.readyState++;
f(this);
if(this._aborted){return
}}}};
c.prototype.abort=function(){if(c.onabort){c.onabort.apply(this,arguments)
}if(this.readyState>c.UNSENT){this._aborted=true
}this._object.abort();
a(this)
};
c.prototype.getAllResponseHeaders=function(){return this._object.getAllResponseHeaders()
};
c.prototype.getResponseHeader=function(i){return this._object.getResponseHeader(i)
};
c.prototype.setRequestHeader=function(i,j){if(!this._headers){this._headers={}
}this._headers[i]=j;
return this._object.setRequestHeader(i,j)
};
c.prototype.toString=function(){return"[object XMLHttpRequest]"
};
c.toString=function(){return"[XMLHttpRequest]"
};
function f(i){if(i.onreadystatechange){i.onreadystatechange.apply(i)
}if(c.onreadystatechange){c.onreadystatechange.apply(i)
}}function b(j){var i=j.responseXML;
if(e&&i&&!i.documentElement&&j.getResponseHeader("Content-Type").match(/[^\/]+\/[^\+]+\+xml/)){i=new ActiveXObject("Microsoft.XMLDOM");
i.loadXML(j.responseText)
}if(i){if((e&&i.parseError!=0)||(i.documentElement&&i.documentElement.tagName=="parsererror")){return null
}}return i
}function g(i){try{i.responseText=i._object.responseText
}catch(j){}try{i.responseXML=b(i._object)
}catch(j){}try{i.status=i._object.status
}catch(j){}try{i.statusText=i._object.statusText
}catch(j){}}function a(i){i._object.onreadystatechange=new window.Function;
delete i._headers
}if(!window.Function.prototype.apply){window.Function.prototype.apply=function(i,j){if(!j){j=[]
}i.__func=this;
i.__func(j[0],j[1],j[2],j[3],j[4]);
delete i.__func
}
}OpenLayers.Request.XMLHttpRequest=c
})();OpenLayers.ProxyHost="";
OpenLayers.nullHandler=function(a){OpenLayers.Console.userError(OpenLayers.i18n("unhandledRequest",{statusText:a.statusText}))
};
OpenLayers.loadURL=function(d,g,b,e,c){if(typeof g=="string"){g=OpenLayers.Util.getParameters(g)
}var f=(e)?e:OpenLayers.nullHandler;
var a=(c)?c:OpenLayers.nullHandler;
return OpenLayers.Request.GET({url:d,params:g,success:f,failure:a,scope:b})
};
OpenLayers.parseXMLString=function(c){var a=c.indexOf("<");
if(a>0){c=c.substring(a)
}var b=OpenLayers.Util.Try(function(){var d=new ActiveXObject("Microsoft.XMLDOM");
d.loadXML(c);
return d
},function(){return new DOMParser().parseFromString(c,"text/xml")
},function(){var d=new XMLHttpRequest();
d.open("GET","data:text/xml;charset=utf-8,"+encodeURIComponent(c),false);
if(d.overrideMimeType){d.overrideMimeType("text/xml")
}d.send(null);
return d.responseXML
});
return b
};
OpenLayers.Ajax={emptyFunction:function(){},getTransport:function(){return OpenLayers.Util.Try(function(){return new XMLHttpRequest()
},function(){return new ActiveXObject("Msxml2.XMLHTTP")
},function(){return new ActiveXObject("Microsoft.XMLHTTP")
})||false
},activeRequestCount:0};
OpenLayers.Ajax.Responders={responders:[],register:function(b){for(var a=0;
a<this.responders.length;
a++){if(b==this.responders[a]){return
}}this.responders.push(b)
},unregister:function(a){OpenLayers.Util.removeItem(this.reponders,a)
},dispatch:function(g,c,f){var a;
for(var b=0;
b<this.responders.length;
b++){a=this.responders[b];
if(a[g]&&typeof a[g]=="function"){try{a[g].apply(a,[c,f])
}catch(d){}}}}};
OpenLayers.Ajax.Responders.register({onCreate:function(){OpenLayers.Ajax.activeRequestCount++
},onComplete:function(){OpenLayers.Ajax.activeRequestCount--
}});
OpenLayers.Ajax.Base=OpenLayers.Class({initialize:function(a){this.options={method:"post",asynchronous:true,contentType:"application/xml",parameters:""};
OpenLayers.Util.extend(this.options,a||{});
this.options.method=this.options.method.toLowerCase();
if(typeof this.options.parameters=="string"){this.options.parameters=OpenLayers.Util.getParameters(this.options.parameters)
}}});
OpenLayers.Ajax.Request=OpenLayers.Class(OpenLayers.Ajax.Base,{_complete:false,initialize:function(b,a){OpenLayers.Ajax.Base.prototype.initialize.apply(this,[a]);
if(OpenLayers.ProxyHost&&OpenLayers.String.startsWith(b,"http")){b=OpenLayers.ProxyHost+encodeURIComponent(b)
}this.transport=OpenLayers.Ajax.getTransport();
this.request(b)
},request:function(b){this.url=b;
this.method=this.options.method;
var d=OpenLayers.Util.extend({},this.options.parameters);
if(this.method!="get"&&this.method!="post"){d._method=this.method;
this.method="post"
}this.parameters=d;
if(d=OpenLayers.Util.getParameterString(d)){if(this.method=="get"){this.url+=((this.url.indexOf("?")>-1)?"&":"?")+d
}else{if(/Konqueror|Safari|KHTML/.test(navigator.userAgent)){d+="&_="
}}}try{var a=new OpenLayers.Ajax.Response(this);
if(this.options.onCreate){this.options.onCreate(a)
}OpenLayers.Ajax.Responders.dispatch("onCreate",this,a);
this.transport.open(this.method.toUpperCase(),this.url,this.options.asynchronous);
if(this.options.asynchronous){window.setTimeout(OpenLayers.Function.bind(this.respondToReadyState,this,1),10)
}this.transport.onreadystatechange=OpenLayers.Function.bind(this.onStateChange,this);
this.setRequestHeaders();
this.body=this.method=="post"?(this.options.postBody||d):null;
this.transport.send(this.body);
if(!this.options.asynchronous&&this.transport.overrideMimeType){this.onStateChange()
}}catch(c){this.dispatchException(c)
}},onStateChange:function(){var a=this.transport.readyState;
if(a>1&&!((a==4)&&this._complete)){this.respondToReadyState(this.transport.readyState)
}},setRequestHeaders:function(){var e={"X-Requested-With":"XMLHttpRequest",Accept:"text/javascript, text/html, application/xml, text/xml, */*",OpenLayers:true};
if(this.method=="post"){e["Content-type"]=this.options.contentType+(this.options.encoding?"; charset="+this.options.encoding:"");
if(this.transport.overrideMimeType&&(navigator.userAgent.match(/Gecko\/(\d{4})/)||[0,2005])[1]<2005){e.Connection="close"
}}if(typeof this.options.requestHeaders=="object"){var c=this.options.requestHeaders;
if(typeof c.push=="function"){for(var b=0,d=c.length;
b<d;
b+=2){e[c[b]]=c[b+1]
}}else{for(var b in c){e[b]=c[b]
}}}for(var a in e){this.transport.setRequestHeader(a,e[a])
}},success:function(){var a=this.getStatus();
return !a||(a>=200&&a<300)
},getStatus:function(){try{return this.transport.status||0
}catch(a){return 0
}},respondToReadyState:function(a){var c=OpenLayers.Ajax.Request.Events[a];
var b=new OpenLayers.Ajax.Response(this);
if(c=="Complete"){try{this._complete=true;
(this.options["on"+b.status]||this.options["on"+(this.success()?"Success":"Failure")]||OpenLayers.Ajax.emptyFunction)(b)
}catch(d){this.dispatchException(d)
}var f=b.getHeader("Content-type")
}try{(this.options["on"+c]||OpenLayers.Ajax.emptyFunction)(b);
OpenLayers.Ajax.Responders.dispatch("on"+c,this,b)
}catch(d){this.dispatchException(d)
}if(c=="Complete"){this.transport.onreadystatechange=OpenLayers.Ajax.emptyFunction
}},getHeader:function(a){try{return this.transport.getResponseHeader(a)
}catch(b){return null
}},dispatchException:function(c){var d=this.options.onException;
if(d){d(this,c);
OpenLayers.Ajax.Responders.dispatch("onException",this,c)
}else{var e=false;
var a=OpenLayers.Ajax.Responders.responders;
for(var b=0;
b<a.length;
b++){if(a[b].onException){e=true;
break
}}if(e){OpenLayers.Ajax.Responders.dispatch("onException",this,c)
}else{throw c
}}}});
OpenLayers.Ajax.Request.Events=["Uninitialized","Loading","Loaded","Interactive","Complete"];
OpenLayers.Ajax.Response=OpenLayers.Class({status:0,statusText:"",initialize:function(c){this.request=c;
var d=this.transport=c.transport,a=this.readyState=d.readyState;
if((a>2&&!(!!(window.attachEvent&&!window.opera)))||a==4){this.status=this.getStatus();
this.statusText=this.getStatusText();
this.responseText=d.responseText==null?"":String(d.responseText)
}if(a==4){var b=d.responseXML;
this.responseXML=b===undefined?null:b
}},getStatus:OpenLayers.Ajax.Request.prototype.getStatus,getStatusText:function(){try{return this.transport.statusText||""
}catch(a){return""
}},getHeader:OpenLayers.Ajax.Request.prototype.getHeader,getResponseHeader:function(a){return this.transport.getResponseHeader(a)
}});
OpenLayers.Ajax.getElementsByTagNameNS=function(b,a,c,e){var d=null;
if(b.getElementsByTagNameNS){d=b.getElementsByTagNameNS(a,e)
}else{d=b.getElementsByTagName(c+":"+e)
}return d
};
OpenLayers.Ajax.serializeXMLToString=function(a){var b=new XMLSerializer();
var c=b.serializeToString(a);
return c
};OpenLayers.LonLat=OpenLayers.Class({lon:0,lat:0,initialize:function(b,a){this.lon=OpenLayers.Util.toFloat(b);
this.lat=OpenLayers.Util.toFloat(a)
},toString:function(){return("lon="+this.lon+",lat="+this.lat)
},toShortString:function(){return(this.lon+", "+this.lat)
},clone:function(){return new OpenLayers.LonLat(this.lon,this.lat)
},add:function(c,a){if((c==null)||(a==null)){var b=OpenLayers.i18n("lonlatAddError");
OpenLayers.Console.error(b);
return null
}return new OpenLayers.LonLat(this.lon+c,this.lat+a)
},equals:function(b){var a=false;
if(b!=null){a=((this.lon==b.lon&&this.lat==b.lat)||(isNaN(this.lon)&&isNaN(this.lat)&&isNaN(b.lon)&&isNaN(b.lat)))
}return a
},transform:function(c,b){var a=OpenLayers.Projection.transform({x:this.lon,y:this.lat},c,b);
this.lon=a.x;
this.lat=a.y;
return this
},wrapDateLine:function(a){var b=this.clone();
if(a){while(b.lon<a.left){b.lon+=a.getWidth()
}while(b.lon>a.right){b.lon-=a.getWidth()
}}return b
},CLASS_NAME:"OpenLayers.LonLat"});
OpenLayers.LonLat.fromString=function(b){var a=b.split(",");
return new OpenLayers.LonLat(parseFloat(a[0]),parseFloat(a[1]))
};OpenLayers.Size=OpenLayers.Class({w:0,h:0,initialize:function(a,b){this.w=parseFloat(a);
this.h=parseFloat(b)
},toString:function(){return("w="+this.w+",h="+this.h)
},clone:function(){return new OpenLayers.Size(this.w,this.h)
},equals:function(b){var a=false;
if(b!=null){a=((this.w==b.w&&this.h==b.h)||(isNaN(this.w)&&isNaN(this.h)&&isNaN(b.w)&&isNaN(b.h)))
}return a
},CLASS_NAME:"OpenLayers.Size"});OpenLayers.Pixel=OpenLayers.Class({x:0,y:0,initialize:function(a,b){this.x=parseFloat(a);
this.y=parseFloat(b)
},toString:function(){return("x="+this.x+",y="+this.y)
},clone:function(){return new OpenLayers.Pixel(this.x,this.y)
},equals:function(a){var b=false;
if(a!=null){b=((this.x==a.x&&this.y==a.y)||(isNaN(this.x)&&isNaN(this.y)&&isNaN(a.x)&&isNaN(a.y)))
}return b
},add:function(a,c){if((a==null)||(c==null)){var b=OpenLayers.i18n("pixelAddError");
OpenLayers.Console.error(b);
return null
}return new OpenLayers.Pixel(this.x+a,this.y+c)
},offset:function(a){var b=this.clone();
if(a){b=this.add(a.x,a.y)
}return b
},CLASS_NAME:"OpenLayers.Pixel"});OpenLayers.Lang={code:null,defaultCode:"en",getCode:function(){if(!OpenLayers.Lang.code){OpenLayers.Lang.setCode()
}return OpenLayers.Lang.code
},setCode:function(b){var d;
if(!b){b=(OpenLayers.Util.getBrowserName()=="msie")?navigator.userLanguage:navigator.language
}var c=b.split("-");
c[0]=c[0].toLowerCase();
if(typeof OpenLayers.Lang[c[0]]=="object"){d=c[0]
}if(c[1]){var a=c[0]+"-"+c[1].toUpperCase();
if(typeof OpenLayers.Lang[a]=="object"){d=a
}}if(!d){OpenLayers.Console.warn("Failed to find OpenLayers.Lang."+c.join("-")+" dictionary, falling back to default language");
d=OpenLayers.Lang.defaultCode
}OpenLayers.Lang.code=d
},translate:function(b,a){var d=OpenLayers.Lang[OpenLayers.Lang.getCode()];
var c=d[b];
if(!c){c=b
}if(a){c=OpenLayers.String.format(c,a)
}return c
}};
OpenLayers.i18n=OpenLayers.Lang.translate;OpenLayers.Lang.en={unhandledRequest:"Unhandled request return ${statusText}",permalink:"Permalink",overlays:"Overlays",baseLayer:"Base Layer",sameProjection:"The overview map only works when it is in the same projection as the main map",readNotImplemented:"Read not implemented.",writeNotImplemented:"Write not implemented.",noFID:"Can't update a feature for which there is no FID.",errorLoadingGML:"Error in loading GML file ${url}",browserNotSupported:"Your browser does not support vector rendering. Currently supported renderers are:\n${renderers}",componentShouldBe:"addFeatures : component should be an ${geomType}",getFeatureError:"getFeatureFromEvent called on layer with no renderer. This usually means you destroyed a layer, but not some handler which is associated with it.",minZoomLevelError:"The minZoomLevel property is only intended for use with the FixedZoomLevels-descendent layers. That this wfs layer checks for minZoomLevel is a relic of thepast. We cannot, however, remove it without possibly breaking OL based applications that may depend on it. Therefore we are deprecating it -- the minZoomLevel check below will be removed at 3.0. Please instead use min/max resolution setting as described here: http://trac.openlayers.org/wiki/SettingZoomLevels",commitSuccess:"WFS Transaction: SUCCESS ${response}",commitFailed:"WFS Transaction: FAILED ${response}",googleWarning:"The Google Layer was unable to load correctly.<br><br>To get rid of this message, select a new BaseLayer in the layer switcher in the upper-right corner.<br><br>Most likely, this is because the Google Maps library script was either not included, or does not contain the correct API key for your site.<br><br>Developers: For help getting this working correctly, <a href='http://trac.openlayers.org/wiki/Google' target='_blank'>click here</a>",getLayerWarning:"The ${layerType} Layer was unable to load correctly.<br><br>To get rid of this message, select a new BaseLayer in the layer switcher in the upper-right corner.<br><br>Most likely, this is because the ${layerLib} library script was not correctly included.<br><br>Developers: For help getting this working correctly, <a href='http://trac.openlayers.org/wiki/${layerLib}' target='_blank'>click here</a>",scale:"Scale = 1 : ${scaleDenom}",W:"W",E:"E",N:"N",S:"S",layerAlreadyAdded:"You tried to add the layer: ${layerName} to the map, but it has already been added",reprojectDeprecated:"You are using the 'reproject' option on the ${layerName} layer. This option is deprecated: its use was designed to support displaying data over commercial basemaps, but that functionality should now be achieved by using Spherical Mercator support. More information is available from http://trac.openlayers.org/wiki/SphericalMercator.",methodDeprecated:"This method has been deprecated and will be removed in 3.0. Please use ${newMethod} instead.",boundsAddError:"You must pass both x and y values to the add function.",lonlatAddError:"You must pass both lon and lat values to the add function.",pixelAddError:"You must pass both x and y values to the add function.",unsupportedGeometryType:"Unsupported geometry type: ${geomType}",pagePositionFailed:"OpenLayers.Util.pagePosition failed: element with id ${elemId} may be misplaced.",end:"",filterEvaluateNotImplemented:"evaluate is not implemented for this filter type."};OpenLayers.Handler.Drag=OpenLayers.Class(OpenLayers.Handler,{started:false,stopDown:true,dragging:false,last:null,start:null,oldOnselectstart:null,interval:0,timeoutId:null,documentDrag:false,documentEvents:null,initialize:function(c,b,a){OpenLayers.Handler.prototype.initialize.apply(this,arguments)
},down:function(a){},move:function(a){},up:function(a){},out:function(a){},mousedown:function(b){var a=true;
this.dragging=false;
if(this.checkModifiers(b)&&OpenLayers.Event.isLeftClick(b)){this.started=true;
this.start=b.xy;
this.last=b.xy;
OpenLayers.Element.addClass(this.map.viewPortDiv,"olDragDown");
this.down(b);
this.callback("down",[b.xy]);
OpenLayers.Event.stop(b);
if(!this.oldOnselectstart){this.oldOnselectstart=(document.onselectstart)?document.onselectstart:OpenLayers.Function.True;
document.onselectstart=OpenLayers.Function.False
}a=!this.stopDown
}else{this.started=false;
this.start=null;
this.last=null
}return a
},mousemove:function(a){if(this.started&&!this.timeoutId&&(a.xy.x!=this.last.x||a.xy.y!=this.last.y)){if(this.documentDrag===true&&this.documentEvents){if(a.element===document){this.adjustXY(a);
this.setEvent(a)
}else{this.destroyDocumentEvents()
}}if(this.interval>0){this.timeoutId=setTimeout(OpenLayers.Function.bind(this.removeTimeout,this),this.interval)
}this.dragging=true;
this.move(a);
this.callback("move",[a.xy]);
if(!this.oldOnselectstart){this.oldOnselectstart=document.onselectstart;
document.onselectstart=OpenLayers.Function.False
}this.last=this.evt.xy
}return true
},removeTimeout:function(){this.timeoutId=null
},mouseup:function(b){if(this.started){if(this.documentDrag===true&&this.documentEvents){this.adjustXY(b);
this.destroyDocumentEvents()
}var a=(this.start!=this.last);
this.started=false;
this.dragging=false;
OpenLayers.Element.removeClass(this.map.viewPortDiv,"olDragDown");
this.up(b);
this.callback("up",[b.xy]);
if(a){this.callback("done",[b.xy])
}document.onselectstart=this.oldOnselectstart
}return true
},mouseout:function(b){if(this.started&&OpenLayers.Util.mouseLeft(b,this.map.div)){if(this.documentDrag===true){this.documentEvents=new OpenLayers.Events(this,document,null,null,{includeXY:true});
this.documentEvents.on({mousemove:this.mousemove,mouseup:this.mouseup});
OpenLayers.Element.addClass(document.body,"olDragDown")
}else{var a=(this.start!=this.last);
this.started=false;
this.dragging=false;
OpenLayers.Element.removeClass(this.map.viewPortDiv,"olDragDown");
this.out(b);
this.callback("out",[]);
if(a){this.callback("done",[b.xy])
}if(document.onselectstart){document.onselectstart=this.oldOnselectstart
}}}return true
},click:function(a){return(this.start==this.last)
},activate:function(){var a=false;
if(OpenLayers.Handler.prototype.activate.apply(this,arguments)){this.dragging=false;
a=true
}return a
},deactivate:function(){var a=false;
if(OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){this.started=false;
this.dragging=false;
this.start=null;
this.last=null;
a=true;
OpenLayers.Element.removeClass(this.map.viewPortDiv,"olDragDown")
}return a
},adjustXY:function(a){var b=OpenLayers.Util.pagePosition(this.map.div);
a.xy.x-=b[0];
a.xy.y-=b[1]
},destroyDocumentEvents:function(){OpenLayers.Element.removeClass(document.body,"olDragDown");
this.documentEvents.destroy();
this.documentEvents=null
},CLASS_NAME:"OpenLayers.Handler.Drag"});OpenLayers.Handler.Box=OpenLayers.Class(OpenLayers.Handler,{dragHandler:null,boxDivClassName:"olHandlerBoxZoomBox",boxCharacteristics:null,initialize:function(c,b,a){OpenLayers.Handler.prototype.initialize.apply(this,arguments);
var b={down:this.startBox,move:this.moveBox,out:this.removeBox,up:this.endBox};
this.dragHandler=new OpenLayers.Handler.Drag(this,b,{keyMask:this.keyMask})
},setMap:function(a){OpenLayers.Handler.prototype.setMap.apply(this,arguments);
if(this.dragHandler){this.dragHandler.setMap(a)
}},startBox:function(a){this.zoomBox=OpenLayers.Util.createDiv("zoomBox",this.dragHandler.start);
this.zoomBox.className=this.boxDivClassName;
this.zoomBox.style.zIndex=this.map.Z_INDEX_BASE.Popup-1;
this.map.viewPortDiv.appendChild(this.zoomBox);
OpenLayers.Element.addClass(this.map.viewPortDiv,"olDrawBox")
},moveBox:function(f){var d=this.dragHandler.start.x;
var b=this.dragHandler.start.y;
var c=Math.abs(d-f.x);
var a=Math.abs(b-f.y);
this.zoomBox.style.width=Math.max(1,c)+"px";
this.zoomBox.style.height=Math.max(1,a)+"px";
this.zoomBox.style.left=f.x<d?f.x+"px":d+"px";
this.zoomBox.style.top=f.y<b?f.y+"px":b+"px";
var e=this.getBoxCharacteristics();
if(e.newBoxModel){if(f.x>d){this.zoomBox.style.width=Math.max(1,c-e.xOffset)+"px"
}if(f.y>b){this.zoomBox.style.height=Math.max(1,a-e.yOffset)+"px"
}}},endBox:function(b){var a;
if(Math.abs(this.dragHandler.start.x-b.x)>5||Math.abs(this.dragHandler.start.y-b.y)>5){var g=this.dragHandler.start;
var f=Math.min(g.y,b.y);
var c=Math.max(g.y,b.y);
var e=Math.min(g.x,b.x);
var d=Math.max(g.x,b.x);
a=new OpenLayers.Bounds(e,c,d,f)
}else{a=this.dragHandler.start.clone()
}this.removeBox();
this.callback("done",[a])
},removeBox:function(){this.map.viewPortDiv.removeChild(this.zoomBox);
this.zoomBox=null;
this.boxCharacteristics=null;
OpenLayers.Element.removeClass(this.map.viewPortDiv,"olDrawBox")
},activate:function(){if(OpenLayers.Handler.prototype.activate.apply(this,arguments)){this.dragHandler.activate();
return true
}else{return false
}},deactivate:function(){if(OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){this.dragHandler.deactivate();
return true
}else{return false
}},getBoxCharacteristics:function(){if(!this.boxCharacteristics){var a=parseInt(OpenLayers.Element.getStyle(this.zoomBox,"border-left-width"))+parseInt(OpenLayers.Element.getStyle(this.zoomBox,"border-right-width"))+1;
var c=parseInt(OpenLayers.Element.getStyle(this.zoomBox,"border-top-width"))+parseInt(OpenLayers.Element.getStyle(this.zoomBox,"border-bottom-width"))+1;
var b=OpenLayers.Util.getBrowserName()=="msie"?document.compatMode!="BackCompat":true;
this.boxCharacteristics={xOffset:a,yOffset:c,newBoxModel:b}
}return this.boxCharacteristics
},CLASS_NAME:"OpenLayers.Handler.Box"});OpenLayers.Control.ZoomBox=OpenLayers.Class(OpenLayers.Control,{type:OpenLayers.Control.TYPE_TOOL,out:false,alwaysZoom:false,draw:function(){this.handler=new OpenLayers.Handler.Box(this,{done:this.zoomBox},{keyMask:this.keyMask})
},zoomBox:function(h){if(h instanceof OpenLayers.Bounds){var b;
if(!this.out){var i=this.map.getLonLatFromPixel(new OpenLayers.Pixel(h.left,h.bottom));
var m=this.map.getLonLatFromPixel(new OpenLayers.Pixel(h.right,h.top));
b=new OpenLayers.Bounds(i.lon,i.lat,m.lon,m.lat)
}else{var g=Math.abs(h.right-h.left);
var j=Math.abs(h.top-h.bottom);
var e=Math.min((this.map.size.h/j),(this.map.size.w/g));
var n=this.map.getExtent();
var a=this.map.getLonLatFromPixel(h.getCenterPixel());
var c=a.lon-(n.getWidth()/2)*e;
var f=a.lon+(n.getWidth()/2)*e;
var l=a.lat-(n.getHeight()/2)*e;
var d=a.lat+(n.getHeight()/2)*e;
b=new OpenLayers.Bounds(c,l,f,d)
}var k=this.map.getZoom();
this.map.zoomToExtent(b);
if(k==this.map.getZoom()&&this.alwaysZoom==true){this.map.zoomTo(k+(this.out?-1:1))
}}else{if(!this.out){this.map.setCenter(this.map.getLonLatFromPixel(h),this.map.getZoom()+1)
}else{this.map.setCenter(this.map.getLonLatFromPixel(h),this.map.getZoom()-1)
}}},CLASS_NAME:"OpenLayers.Control.ZoomBox"});OpenLayers.Control.DragPan=OpenLayers.Class(OpenLayers.Control,{type:OpenLayers.Control.TYPE_TOOL,panned:false,interval:25,documentDrag:false,draw:function(){this.handler=new OpenLayers.Handler.Drag(this,{move:this.panMap,done:this.panMapDone},{interval:this.interval,documentDrag:this.documentDrag})
},panMap:function(a){this.panned=true;
this.map.pan(this.handler.last.x-a.x,this.handler.last.y-a.y,{dragging:this.handler.dragging,animate:false})
},panMapDone:function(a){if(this.panned){this.panMap(a);
this.panned=false
}},CLASS_NAME:"OpenLayers.Control.DragPan"});OpenLayers.Handler.Click=OpenLayers.Class(OpenLayers.Handler,{delay:300,single:true,"double":false,pixelTolerance:0,stopSingle:false,stopDouble:false,timerId:null,down:null,rightclickTimerId:null,initialize:function(c,b,a){OpenLayers.Handler.prototype.initialize.apply(this,arguments);
if(this.pixelTolerance!=null){this.mousedown=function(d){this.down=d.xy;
return true
}
}},mousedown:null,mouseup:function(b){var a=true;
if(this.checkModifiers(b)&&this.control.handleRightClicks&&OpenLayers.Event.isRightClick(b)){a=this.rightclick(b)
}return a
},rightclick:function(b){if(this.passesTolerance(b)){if(this.rightclickTimerId!=null){this.clearTimer();
this.callback("dblrightclick",[b]);
return !this.stopDouble
}else{var a=this["double"]?OpenLayers.Util.extend({},b):this.callback("rightclick",[b]);
var c=OpenLayers.Function.bind(this.delayedRightCall,this,a);
this.rightclickTimerId=window.setTimeout(c,this.delay)
}}return !this.stopSingle
},delayedRightCall:function(a){this.rightclickTimerId=null;
if(a){this.callback("rightclick",[a])
}return !this.stopSingle
},dblclick:function(a){if(this.passesTolerance(a)){if(this["double"]){this.callback("dblclick",[a])
}this.clearTimer()
}return !this.stopDouble
},click:function(b){if(this.passesTolerance(b)){if(this.timerId!=null){this.clearTimer()
}else{var a=this.single?OpenLayers.Util.extend({},b):null;
this.timerId=window.setTimeout(OpenLayers.Function.bind(this.delayedCall,this,a),this.delay)
}}return !this.stopSingle
},passesTolerance:function(b){var c=true;
if(this.pixelTolerance!=null&&this.down){var a=Math.sqrt(Math.pow(this.down.x-b.xy.x,2)+Math.pow(this.down.y-b.xy.y,2));
if(a>this.pixelTolerance){c=false
}}return c
},clearTimer:function(){if(this.timerId!=null){window.clearTimeout(this.timerId);
this.timerId=null
}if(this.rightclickTimerId!=null){window.clearTimeout(this.rightclickTimerId);
this.rightclickTimerId=null
}},delayedCall:function(a){this.timerId=null;
if(a){this.callback("click",[a])
}},deactivate:function(){var a=false;
if(OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){this.clearTimer();
this.down=null;
a=true
}return a
},CLASS_NAME:"OpenLayers.Handler.Click"});OpenLayers.Control.Navigation=OpenLayers.Class(OpenLayers.Control,{dragPan:null,dragPanOptions:null,documentDrag:false,zoomBox:null,zoomBoxEnabled:true,zoomWheelEnabled:true,mouseWheelOptions:null,handleRightClicks:false,zoomBoxKeyMask:OpenLayers.Handler.MOD_SHIFT,autoActivate:true,initialize:function(a){this.handlers={};
OpenLayers.Control.prototype.initialize.apply(this,arguments)
},destroy:function(){this.deactivate();
if(this.dragPan){this.dragPan.destroy()
}this.dragPan=null;
if(this.zoomBox){this.zoomBox.destroy()
}this.zoomBox=null;
OpenLayers.Control.prototype.destroy.apply(this,arguments)
},activate:function(){this.dragPan.activate();
if(this.zoomWheelEnabled){this.handlers.wheel.activate()
}this.handlers.click.activate();
if(this.zoomBoxEnabled){this.zoomBox.activate()
}return OpenLayers.Control.prototype.activate.apply(this,arguments)
},deactivate:function(){this.zoomBox.deactivate();
this.dragPan.deactivate();
this.handlers.click.deactivate();
this.handlers.wheel.deactivate();
return OpenLayers.Control.prototype.deactivate.apply(this,arguments)
},draw:function(){if(this.handleRightClicks){this.map.viewPortDiv.oncontextmenu=OpenLayers.Function.False
}var a={dblclick:this.defaultDblClick,dblrightclick:this.defaultDblRightClick};
var b={"double":true,stopDouble:true};
this.handlers.click=new OpenLayers.Handler.Click(this,a,b);
this.dragPan=new OpenLayers.Control.DragPan(OpenLayers.Util.extend({map:this.map,documentDrag:this.documentDrag},this.dragPanOptions));
this.zoomBox=new OpenLayers.Control.ZoomBox({map:this.map,keyMask:this.zoomBoxKeyMask});
this.dragPan.draw();
this.zoomBox.draw();
this.handlers.wheel=new OpenLayers.Handler.MouseWheel(this,{up:this.wheelUp,down:this.wheelDown},this.mouseWheelOptions)
},defaultDblClick:function(b){var a=this.map.getLonLatFromViewPortPx(b.xy);
this.map.setCenter(a,this.map.zoom+1)
},defaultDblRightClick:function(b){var a=this.map.getLonLatFromViewPortPx(b.xy);
this.map.setCenter(a,this.map.zoom-1)
},wheelChange:function(i,c){var h=this.map.getZoom();
var f=this.map.getZoom()+Math.round(c);
f=Math.max(f,0);
f=Math.min(f,this.map.getNumZoomLevels());
if(f===h){return
}var j=this.map.getSize();
var e=j.w/2-i.xy.x;
var d=i.xy.y-j.h/2;
var g=this.map.baseLayer.getResolutionForZoom(f);
var a=this.map.getLonLatFromPixel(i.xy);
var b=new OpenLayers.LonLat(a.lon+e*g,a.lat+d*g);
this.map.setCenter(b,f)
},wheelUp:function(a,b){this.wheelChange(a,b||1)
},wheelDown:function(a,b){this.wheelChange(a,b||-1)
},disableZoomBox:function(){this.zoomBoxEnabled=false;
this.zoomBox.deactivate()
},enableZoomBox:function(){this.zoomBoxEnabled=true;
if(this.active){this.zoomBox.activate()
}},disableZoomWheel:function(){this.zoomWheelEnabled=false;
this.handlers.wheel.deactivate()
},enableZoomWheel:function(){this.zoomWheelEnabled=true;
if(this.active){this.handlers.wheel.activate()
}},CLASS_NAME:"OpenLayers.Control.Navigation"});OpenLayers.Layer.HTTPRequest=OpenLayers.Class(OpenLayers.Layer,{URL_HASH_FACTOR:(Math.sqrt(5)-1)/2,url:null,params:null,reproject:false,initialize:function(d,c,e,b){var a=arguments;
a=[d,b];
OpenLayers.Layer.prototype.initialize.apply(this,a);
this.url=c;
this.params=OpenLayers.Util.extend({},e)
},destroy:function(){this.url=null;
this.params=null;
OpenLayers.Layer.prototype.destroy.apply(this,arguments)
},clone:function(a){if(a==null){a=new OpenLayers.Layer.HTTPRequest(this.name,this.url,this.params,this.getOptions())
}a=OpenLayers.Layer.prototype.clone.apply(this,[a]);
return a
},setUrl:function(a){this.url=a
},mergeNewParams:function(b){this.params=OpenLayers.Util.extend(this.params,b);
var a=this.redraw();
if(this.map!=null){this.map.events.triggerEvent("changelayer",{layer:this,property:"params"})
}return a
},redraw:function(a){if(a){return this.mergeNewParams({_olSalt:Math.random()})
}else{return OpenLayers.Layer.prototype.redraw.apply(this,[])
}},selectUrl:function(e,d){var c=1;
for(var b=0,a=e.length;
b<a;
b++){c*=e.charCodeAt(b)*this.URL_HASH_FACTOR;
c-=Math.floor(c)
}return d[Math.floor(c*d.length)]
},getFullRequestString:function(g,d){var b=d||this.url;
var f=OpenLayers.Util.extend({},this.params);
f=OpenLayers.Util.extend(f,g);
var e=OpenLayers.Util.getParameterString(f);
if(b instanceof Array){b=this.selectUrl(e,b)
}var a=OpenLayers.Util.upperCaseObject(OpenLayers.Util.getParameters(b));
for(var c in f){if(c.toUpperCase() in a){delete f[c]
}}e=OpenLayers.Util.getParameterString(f);
return OpenLayers.Util.urlAppend(b,e)
},CLASS_NAME:"OpenLayers.Layer.HTTPRequest"});OpenLayers.Layer.Grid=OpenLayers.Class(OpenLayers.Layer.HTTPRequest,{tileSize:null,grid:null,singleTile:false,ratio:1.5,buffer:2,numLoadingTiles:0,initialize:function(c,b,d,a){OpenLayers.Layer.HTTPRequest.prototype.initialize.apply(this,arguments);
this.events.addEventType("tileloaded");
this.grid=[]
},destroy:function(){this.clearGrid();
this.grid=null;
this.tileSize=null;
OpenLayers.Layer.HTTPRequest.prototype.destroy.apply(this,arguments)
},clearGrid:function(){if(this.grid){for(var f=0,b=this.grid.length;
f<b;
f++){var e=this.grid[f];
for(var c=0,a=e.length;
c<a;
c++){var d=e[c];
this.removeTileMonitoringHooks(d);
d.destroy()
}}this.grid=[]
}},clone:function(a){if(a==null){a=new OpenLayers.Layer.Grid(this.name,this.url,this.params,this.getOptions())
}a=OpenLayers.Layer.HTTPRequest.prototype.clone.apply(this,[a]);
if(this.tileSize!=null){a.tileSize=this.tileSize.clone()
}a.grid=[];
return a
},moveTo:function(d,a,e){OpenLayers.Layer.HTTPRequest.prototype.moveTo.apply(this,arguments);
d=d||this.map.getExtent();
if(d!=null){var c=!this.grid.length||a;
var b=this.getTilesBounds();
if(this.singleTile){if(c||(!e&&!b.containsBounds(d))){this.initSingleTile(d)
}}else{if(c||!b.containsBounds(d,true)){this.initGriddedTiles(d)
}else{this.moveGriddedTiles(d)
}}}},setTileSize:function(a){if(this.singleTile){a=this.map.getSize().clone();
a.h=parseInt(a.h*this.ratio);
a.w=parseInt(a.w*this.ratio)
}OpenLayers.Layer.HTTPRequest.prototype.setTileSize.apply(this,[a])
},getGridBounds:function(){var a="The getGridBounds() function is deprecated. It will be removed in 3.0. Please use getTilesBounds() instead.";
OpenLayers.Console.warn(a);
return this.getTilesBounds()
},getTilesBounds:function(){var e=null;
if(this.grid.length){var a=this.grid.length-1;
var d=this.grid[a][0];
var b=this.grid[0].length-1;
var c=this.grid[0][b];
e=new OpenLayers.Bounds(d.bounds.left,d.bounds.bottom,c.bounds.right,c.bounds.top)
}return e
},initSingleTile:function(f){var a=f.getCenterLonLat();
var h=f.getWidth()*this.ratio;
var b=f.getHeight()*this.ratio;
var g=new OpenLayers.Bounds(a.lon-(h/2),a.lat-(b/2),a.lon+(h/2),a.lat+(b/2));
var d=new OpenLayers.LonLat(g.left,g.top);
var c=this.map.getLayerPxFromLonLat(d);
if(!this.grid.length){this.grid[0]=[]
}var e=this.grid[0][0];
if(!e){e=this.addTile(g,c);
this.addTileMonitoringHooks(e);
e.draw();
this.grid[0][0]=e
}else{e.moveTo(g,c)
}this.removeExcessTiles(1,1)
},calculateGridLayout:function(a,o,e){var k=e*this.tileSize.w;
var c=e*this.tileSize.h;
var i=a.left-o.left;
var l=Math.floor(i/k)-this.buffer;
var j=i/k-l;
var f=-j*this.tileSize.w;
var m=o.left+l*k;
var b=a.top-(o.bottom+c);
var h=Math.ceil(b/c)+this.buffer;
var n=h-b/c;
var d=-n*this.tileSize.h;
var g=o.bottom+h*c;
return{tilelon:k,tilelat:c,tileoffsetlon:m,tileoffsetlat:g,tileoffsetx:f,tileoffsety:d}
},initGriddedTiles:function(i){var g=this.map.getSize();
var v=Math.ceil(g.h/this.tileSize.h)+Math.max(1,2*this.buffer);
var z=Math.ceil(g.w/this.tileSize.w)+Math.max(1,2*this.buffer);
var o=this.maxExtent;
var r=this.map.getResolution();
var q=this.calculateGridLayout(i,o,r);
var f=Math.round(q.tileoffsetx);
var c=Math.round(q.tileoffsety);
var k=q.tileoffsetlon;
var n=q.tileoffsetlat;
var e=q.tilelon;
var j=q.tilelat;
this.origin=new OpenLayers.Pixel(f,c);
var u=f;
var w=k;
var t=0;
var a=parseInt(this.map.layerContainerDiv.style.left);
var s=parseInt(this.map.layerContainerDiv.style.top);
do{var h=this.grid[t++];
if(!h){h=[];
this.grid.push(h)
}k=w;
f=u;
var d=0;
do{var b=new OpenLayers.Bounds(k,n,k+e,n+j);
var m=f;
m-=a;
var l=c;
l-=s;
var p=new OpenLayers.Pixel(m,l);
var A=h[d++];
if(!A){A=this.addTile(b,p);
this.addTileMonitoringHooks(A);
h.push(A)
}else{A.moveTo(b,p,false)
}k+=e;
f+=this.tileSize.w
}while((k<=i.right+e*this.buffer)||d<z);
n-=j;
c+=this.tileSize.h
}while((n>=i.bottom-j*this.buffer)||t<v);
this.removeExcessTiles(t,d);
this.spiralTileLoad()
},spiralTileLoad:function(){var b=[];
var h=["right","down","left","up"];
var g=0;
var a=-1;
var k=OpenLayers.Util.indexOf(h,"right");
var l=0;
while(l<h.length){var j=g;
var c=a;
switch(h[k]){case"right":c++;
break;
case"down":j++;
break;
case"left":c--;
break;
case"up":j--;
break
}var f=null;
if((j<this.grid.length)&&(j>=0)&&(c<this.grid[0].length)&&(c>=0)){f=this.grid[j][c]
}if((f!=null)&&(!f.queued)){b.unshift(f);
f.queued=true;
l=0;
g=j;
a=c
}else{k=(k+1)%4;
l++
}}for(var d=0,e=b.length;
d<e;
d++){var f=b[d];
f.draw();
f.queued=false
}},addTile:function(b,a){},addTileMonitoringHooks:function(a){a.onLoadStart=function(){if(this.numLoadingTiles==0){this.events.triggerEvent("loadstart")
}this.numLoadingTiles++
};
a.events.register("loadstart",this,a.onLoadStart);
a.onLoadEnd=function(){this.numLoadingTiles--;
this.events.triggerEvent("tileloaded");
if(this.numLoadingTiles==0){this.events.triggerEvent("loadend")
}};
a.events.register("loadend",this,a.onLoadEnd);
a.events.register("unload",this,a.onLoadEnd)
},removeTileMonitoringHooks:function(a){a.unload();
a.events.un({loadstart:a.onLoadStart,loadend:a.onLoadEnd,unload:a.onLoadEnd,scope:this})
},moveGriddedTiles:function(c){var b=this.buffer||1;
while(true){var a=this.grid[0][0].position;
var d=this.map.getViewPortPxFromLayerPx(a);
if(d.x>-this.tileSize.w*(b-1)){this.shiftColumn(true)
}else{if(d.x<-this.tileSize.w*b){this.shiftColumn(false)
}else{if(d.y>-this.tileSize.h*(b-1)){this.shiftRow(true)
}else{if(d.y<-this.tileSize.h*b){this.shiftRow(false)
}else{break
}}}}}},shiftRow:function(n){var c=(n)?0:(this.grid.length-1);
var b=this.grid;
var f=b[c];
var e=this.map.getResolution();
var h=(n)?-this.tileSize.h:this.tileSize.h;
var g=e*-h;
var m=(n)?b.pop():b.shift();
for(var j=0,l=f.length;
j<l;
j++){var d=f[j];
var a=d.bounds.clone();
var k=d.position.clone();
a.bottom=a.bottom+g;
a.top=a.top+g;
k.y=k.y+h;
m[j].moveTo(a,k)
}if(n){b.unshift(m)
}else{b.push(m)
}},shiftColumn:function(m){var d=(m)?-this.tileSize.w:this.tileSize.w;
var c=this.map.getResolution();
var k=c*d;
for(var e=0,g=this.grid.length;
e<g;
e++){var l=this.grid[e];
var j=(m)?0:(l.length-1);
var b=l[j];
var a=b.bounds.clone();
var f=b.position.clone();
a.left=a.left+k;
a.right=a.right+k;
f.x=f.x+d;
var h=m?this.grid[e].pop():this.grid[e].shift();
h.moveTo(a,f);
if(m){l.unshift(h)
}else{l.push(h)
}}},removeExcessTiles:function(e,c){while(this.grid.length>e){var f=this.grid.pop();
for(var b=0,a=f.length;
b<a;
b++){var d=f[b];
this.removeTileMonitoringHooks(d);
d.destroy()
}}while(this.grid[0].length>c){for(var b=0,a=this.grid.length;
b<a;
b++){var f=this.grid[b];
var d=f.pop();
this.removeTileMonitoringHooks(d);
d.destroy()
}}},onMapResize:function(){if(this.singleTile){this.clearGrid();
this.setTileSize()
}},getTileBounds:function(d){var c=this.maxExtent;
var f=this.getResolution();
var e=f*this.tileSize.w;
var b=f*this.tileSize.h;
var h=this.getLonLatFromViewPortPx(d);
var a=c.left+(e*Math.floor((h.lon-c.left)/e));
var g=c.bottom+(b*Math.floor((h.lat-c.bottom)/b));
return new OpenLayers.Bounds(a,g,a+e,g+b)
},CLASS_NAME:"OpenLayers.Layer.Grid"});OpenLayers.Layer.WMS=OpenLayers.Class(OpenLayers.Layer.Grid,{DEFAULT_PARAMS:{service:"WMS",version:"1.1.1",request:"GetMap",styles:"",exceptions:"application/vnd.ogc.se_inimage",format:"image/jpeg"},reproject:false,isBaseLayer:true,encodeBBOX:false,noMagic:false,yx:["EPSG:4326"],initialize:function(d,c,e,b){var a=[];
e=OpenLayers.Util.upperCaseObject(e);
a.push(d,c,e,b);
OpenLayers.Layer.Grid.prototype.initialize.apply(this,a);
OpenLayers.Util.applyDefaults(this.params,OpenLayers.Util.upperCaseObject(this.DEFAULT_PARAMS));
if(!this.noMagic&&this.params.TRANSPARENT&&this.params.TRANSPARENT.toString().toLowerCase()=="true"){if((b==null)||(!b.isBaseLayer)){this.isBaseLayer=false
}if(this.params.FORMAT=="image/jpeg"){this.params.FORMAT=OpenLayers.Util.alphaHack()?"image/gif":"image/png"
}}},destroy:function(){OpenLayers.Layer.Grid.prototype.destroy.apply(this,arguments)
},clone:function(a){if(a==null){a=new OpenLayers.Layer.WMS(this.name,this.url,this.params,this.getOptions())
}a=OpenLayers.Layer.Grid.prototype.clone.apply(this,[a]);
return a
},reverseAxisOrder:function(){return(parseFloat(this.params.VERSION)>=1.3&&OpenLayers.Util.indexOf(this.yx,this.map.getProjectionObject().getCode())!==-1)
},getURL:function(c){c=this.adjustBounds(c);
var d=this.getImageSize();
var e={};
var b=this.reverseAxisOrder();
e.BBOX=this.encodeBBOX?c.toBBOX(null,b):c.toArray(b);
e.WIDTH=d.w;
e.HEIGHT=d.h;
var a=this.getFullRequestString(e);
return a
},addTile:function(b,a){return new OpenLayers.Tile.Image(this,a,b,null,this.tileSize)
},mergeNewParams:function(c){var b=OpenLayers.Util.upperCaseObject(c);
var a=[b];
return OpenLayers.Layer.Grid.prototype.mergeNewParams.apply(this,a)
},getFullRequestString:function(d,b){var a=this.map.getProjection();
var c=(a=="none")?null:a;
if(parseFloat(this.params.VERSION)>=1.3){this.params.CRS=c
}else{this.params.SRS=c
}return OpenLayers.Layer.Grid.prototype.getFullRequestString.apply(this,arguments)
},CLASS_NAME:"OpenLayers.Layer.WMS"});OpenLayers.Renderer.SVG=OpenLayers.Class(OpenLayers.Renderer.Elements,{xmlns:"http://www.w3.org/2000/svg",xlinkns:"http://www.w3.org/1999/xlink",MAX_PIXEL:15000,translationParameters:null,symbolMetrics:null,isGecko:null,supportUse:null,initialize:function(a){if(!this.supported()){return
}OpenLayers.Renderer.Elements.prototype.initialize.apply(this,arguments);
this.translationParameters={x:0,y:0};
this.supportUse=(navigator.userAgent.toLowerCase().indexOf("applewebkit/5")==-1);
this.isGecko=(navigator.userAgent.toLowerCase().indexOf("gecko/")!=-1);
this.symbolMetrics={}
},destroy:function(){OpenLayers.Renderer.Elements.prototype.destroy.apply(this,arguments)
},supported:function(){var a="http://www.w3.org/TR/SVG11/feature#";
return(document.implementation&&(document.implementation.hasFeature("org.w3c.svg","1.0")||document.implementation.hasFeature(a+"SVG","1.1")||document.implementation.hasFeature(a+"BasicStructure","1.1")))
},inValidRange:function(a,e,b){var d=a+(b?0:this.translationParameters.x);
var c=e+(b?0:this.translationParameters.y);
return(d>=-this.MAX_PIXEL&&d<=this.MAX_PIXEL&&c>=-this.MAX_PIXEL&&c<=this.MAX_PIXEL)
},setExtent:function(b,d){OpenLayers.Renderer.Elements.prototype.setExtent.apply(this,arguments);
var a=this.getResolution();
var f=-b.left/a;
var e=b.top/a;
if(d){this.left=f;
this.top=e;
var c="0 0 "+this.size.w+" "+this.size.h;
this.rendererRoot.setAttributeNS(null,"viewBox",c);
this.translate(0,0);
return true
}else{var g=this.translate(f-this.left,e-this.top);
if(!g){this.setExtent(b,true)
}return g
}},translate:function(a,c){if(!this.inValidRange(a,c,true)){return false
}else{var b="";
if(a||c){b="translate("+a+","+c+")"
}this.root.setAttributeNS(null,"transform",b);
this.translationParameters={x:a,y:c};
return true
}},setSize:function(a){OpenLayers.Renderer.prototype.setSize.apply(this,arguments);
this.rendererRoot.setAttributeNS(null,"width",this.size.w);
this.rendererRoot.setAttributeNS(null,"height",this.size.h)
},getNodeType:function(c,b){var a=null;
switch(c.CLASS_NAME){case"OpenLayers.Geometry.Point":if(b.externalGraphic){a="image"
}else{if(this.isComplexSymbol(b.graphicName)){a=this.supportUse===false?"svg":"use"
}else{a="circle"
}}break;
case"OpenLayers.Geometry.Rectangle":a="rect";
break;
case"OpenLayers.Geometry.LineString":a="polyline";
break;
case"OpenLayers.Geometry.LinearRing":a="polygon";
break;
case"OpenLayers.Geometry.Polygon":case"OpenLayers.Geometry.Curve":case"OpenLayers.Geometry.Surface":a="path";
break;
default:break
}return a
},setStyle:function(o,s,b){s=s||o._style;
b=b||o._options;
var j=parseFloat(o.getAttributeNS(null,"r"));
var i=1;
var d;
if(o._geometryClass=="OpenLayers.Geometry.Point"&&j){o.style.visibility="";
if(s.graphic===false){o.style.visibility="hidden"
}else{if(s.externalGraphic){d=this.getPosition(o);
if(s.graphicTitle){o.setAttributeNS(null,"title",s.graphicTitle)
}if(s.graphicWidth&&s.graphicHeight){o.setAttributeNS(null,"preserveAspectRatio","none")
}var n=s.graphicWidth||s.graphicHeight;
var l=s.graphicHeight||s.graphicWidth;
n=n?n:s.pointRadius*2;
l=l?l:s.pointRadius*2;
var t=(s.graphicXOffset!=undefined)?s.graphicXOffset:-(0.5*n);
var f=(s.graphicYOffset!=undefined)?s.graphicYOffset:-(0.5*l);
var a=s.graphicOpacity||s.fillOpacity;
o.setAttributeNS(null,"x",(d.x+t).toFixed());
o.setAttributeNS(null,"y",(d.y+f).toFixed());
o.setAttributeNS(null,"width",n);
o.setAttributeNS(null,"height",l);
o.setAttributeNS(this.xlinkns,"href",s.externalGraphic);
o.setAttributeNS(null,"style","opacity: "+a)
}else{if(this.isComplexSymbol(s.graphicName)){var c=s.pointRadius*3;
var k=c*2;
var m=this.importSymbol(s.graphicName);
d=this.getPosition(o);
i=this.symbolMetrics[m][0]*3/k;
var g=o.parentNode;
var h=o.nextSibling;
if(g){g.removeChild(o)
}if(this.supportUse===false){var e=document.getElementById(m);
o.firstChild&&o.removeChild(o.firstChild);
o.appendChild(e.firstChild.cloneNode(true));
o.setAttributeNS(null,"viewBox",e.getAttributeNS(null,"viewBox"))
}else{o.setAttributeNS(this.xlinkns,"href","#"+m)
}o.setAttributeNS(null,"width",k);
o.setAttributeNS(null,"height",k);
o.setAttributeNS(null,"x",d.x-c);
o.setAttributeNS(null,"y",d.y-c);
if(h){g.insertBefore(o,h)
}else{if(g){g.appendChild(o)
}}}else{o.setAttributeNS(null,"r",s.pointRadius)
}}}var q=s.rotation;
if((q!==undefined||o._rotation!==undefined)&&d){o._rotation=q;
q|=0;
if(o.nodeName!=="svg"){o.setAttributeNS(null,"transform","rotate("+q+" "+d.x+" "+d.y+")")
}else{var p=this.symbolMetrics[m];
o.firstChild.setAttributeNS(null,"transform","rotate("+s.rotation+" "+p[1]+" "+p[2]+")")
}}}if(b.isFilled){o.setAttributeNS(null,"fill",s.fillColor);
o.setAttributeNS(null,"fill-opacity",s.fillOpacity)
}else{o.setAttributeNS(null,"fill","none")
}if(b.isStroked){o.setAttributeNS(null,"stroke",s.strokeColor);
o.setAttributeNS(null,"stroke-opacity",s.strokeOpacity);
o.setAttributeNS(null,"stroke-width",s.strokeWidth*i);
o.setAttributeNS(null,"stroke-linecap",s.strokeLinecap);
o.setAttributeNS(null,"stroke-linejoin","round");
o.setAttributeNS(null,"stroke-dasharray",this.dashStyle(s,i))
}else{o.setAttributeNS(null,"stroke","none")
}if(s.pointerEvents){o.setAttributeNS(null,"pointer-events",s.pointerEvents)
}if(s.cursor!=null){o.setAttributeNS(null,"cursor",s.cursor)
}return o
},dashStyle:function(c,b){var a=c.strokeWidth*b;
var d=c.strokeDashstyle;
switch(d){case"solid":return"none";
case"dot":return[1,4*a].join();
case"dash":return[4*a,4*a].join();
case"dashdot":return[4*a,4*a,1,4*a].join();
case"longdash":return[8*a,4*a].join();
case"longdashdot":return[8*a,4*a,1,4*a].join();
default:return OpenLayers.String.trim(d).replace(/\s+/g,",")
}},createNode:function(a,c){var b=document.createElementNS(this.xmlns,a);
if(c){b.setAttributeNS(null,"id",c)
}return b
},nodeTypeCompare:function(b,a){return(a==b.nodeName)
},createRenderRoot:function(){return this.nodeFactory(this.container.id+"_svgRoot","svg")
},createRoot:function(a){return this.nodeFactory(this.container.id+a,"g")
},createDefs:function(){var a=this.nodeFactory(this.container.id+"_defs","defs");
this.rendererRoot.appendChild(a);
return a
},drawPoint:function(a,b){return this.drawCircle(a,b,1)
},drawCircle:function(d,e,b){var c=this.getResolution();
var a=(e.x/c+this.left);
var f=(this.top-e.y/c);
if(this.inValidRange(a,f)){d.setAttributeNS(null,"cx",a);
d.setAttributeNS(null,"cy",f);
d.setAttributeNS(null,"r",b);
return d
}else{return false
}},drawLineString:function(b,c){var a=this.getComponentsString(c.components);
if(a.path){b.setAttributeNS(null,"points",a.path);
return(a.complete?b:null)
}else{return false
}},drawLinearRing:function(b,c){var a=this.getComponentsString(c.components);
if(a.path){b.setAttributeNS(null,"points",a.path);
return(a.complete?b:null)
}else{return false
}},drawPolygon:function(b,h){var g="";
var i=true;
var a=true;
var c,k;
for(var e=0,f=h.components.length;
e<f;
e++){g+=" M";
c=this.getComponentsString(h.components[e].components," ");
k=c.path;
if(k){g+=" "+k;
a=c.complete&&a
}else{i=false
}}g+=" z";
if(i){b.setAttributeNS(null,"d",g);
b.setAttributeNS(null,"fill-rule","evenodd");
return a?b:null
}else{return false
}},drawRectangle:function(c,d){var b=this.getResolution();
var a=(d.x/b+this.left);
var e=(this.top-d.y/b);
if(this.inValidRange(a,e)){c.setAttributeNS(null,"x",a);
c.setAttributeNS(null,"y",e);
c.setAttributeNS(null,"width",d.width/b);
c.setAttributeNS(null,"height",d.height/b);
return c
}else{return false
}},drawSurface:function(f,h){var g=null;
var b=true;
for(var e=0,a=h.components.length;
e<a;
e++){if((e%3)==0&&(e/3)==0){var c=this.getShortString(h.components[e]);
if(!c){b=false
}g="M "+c
}else{if((e%3)==1){var c=this.getShortString(h.components[e]);
if(!c){b=false
}g+=" C "+c
}else{var c=this.getShortString(h.components[e]);
if(!c){b=false
}g+=" "+c
}}}g+=" Z";
if(b){f.setAttributeNS(null,"d",g);
return f
}else{return false
}},drawText:function(c,a,i){var b=this.getResolution();
var h=(i.x/b+this.left);
var e=(i.y/b-this.top);
var g=this.nodeFactory(c+this.LABEL_ID_SUFFIX,"text");
var f=this.nodeFactory(c+this.LABEL_ID_SUFFIX+"_tspan","tspan");
g.setAttributeNS(null,"x",h);
g.setAttributeNS(null,"y",-e);
if(a.fontColor){g.setAttributeNS(null,"fill",a.fontColor)
}if(a.fontOpacity){g.setAttributeNS(null,"opacity",a.fontOpacity)
}if(a.fontFamily){g.setAttributeNS(null,"font-family",a.fontFamily)
}if(a.fontSize){g.setAttributeNS(null,"font-size",a.fontSize)
}if(a.fontWeight){g.setAttributeNS(null,"font-weight",a.fontWeight)
}if(a.labelSelect===true){g.setAttributeNS(null,"pointer-events","visible");
g._featureId=c;
f._featureId=c;
f._geometry=i;
f._geometryClass=i.CLASS_NAME
}else{g.setAttributeNS(null,"pointer-events","none")
}var d=a.labelAlign||"cm";
g.setAttributeNS(null,"text-anchor",OpenLayers.Renderer.SVG.LABEL_ALIGN[d[0]]||"middle");
if(this.isGecko){g.setAttributeNS(null,"dominant-baseline",OpenLayers.Renderer.SVG.LABEL_ALIGN[d[1]]||"central")
}else{f.setAttributeNS(null,"baseline-shift",OpenLayers.Renderer.SVG.LABEL_VSHIFT[d[1]]||"-35%")
}f.textContent=a.label;
if(!g.parentNode){g.appendChild(f);
this.textRoot.appendChild(g)
}},getComponentsString:function(e,d){var g=[];
var a=true;
var f=e.length;
var l=[];
var h,k,b;
for(var c=0;
c<f;
c++){k=e[c];
g.push(k);
h=this.getShortString(k);
if(h){l.push(h)
}else{if(c>0){if(this.getShortString(e[c-1])){l.push(this.clipLine(e[c],e[c-1]))
}}if(c<f-1){if(this.getShortString(e[c+1])){l.push(this.clipLine(e[c],e[c+1]))
}}a=false
}}return{path:l.join(d||","),complete:a}
},clipLine:function(e,h){if(h.equals(e)){return""
}var f=this.getResolution();
var b=this.MAX_PIXEL-this.translationParameters.x;
var a=this.MAX_PIXEL-this.translationParameters.y;
var d=h.x/f+this.left;
var j=this.top-h.y/f;
var c=e.x/f+this.left;
var i=this.top-e.y/f;
var g;
if(c<-b||c>b){g=(i-j)/(c-d);
c=c<0?-b:b;
i=j+(c-d)*g
}if(i<-a||i>a){g=(c-d)/(i-j);
i=i<0?-a:a;
c=d+(i-j)*g
}return c+","+i
},getShortString:function(b){var c=this.getResolution();
var a=(b.x/c+this.left);
var d=(this.top-b.y/c);
if(this.inValidRange(a,d)){return a+","+d
}else{return false
}},getPosition:function(a){return({x:parseFloat(a.getAttributeNS(null,"cx")),y:parseFloat(a.getAttributeNS(null,"cy"))})
},importSymbol:function(e){if(!this.defs){this.defs=this.createDefs()
}var b=this.container.id+"-"+e;
if(document.getElementById(b)!=null){return b
}var d=OpenLayers.Renderer.symbol[e];
if(!d){throw new Error(e+" is not a valid symbol name");
return
}var g=this.nodeFactory(b,"symbol");
var c=this.nodeFactory(null,"polygon");
g.appendChild(c);
var m=new OpenLayers.Bounds(Number.MAX_VALUE,Number.MAX_VALUE,0,0);
var k="";
var j,h;
for(var f=0;
f<d.length;
f=f+2){j=d[f];
h=d[f+1];
m.left=Math.min(m.left,j);
m.bottom=Math.min(m.bottom,h);
m.right=Math.max(m.right,j);
m.top=Math.max(m.top,h);
k+=" "+j+","+h
}c.setAttributeNS(null,"points",k);
var a=m.getWidth();
var l=m.getHeight();
var n=[m.left-a,m.bottom-l,a*3,l*3];
g.setAttributeNS(null,"viewBox",n.join(" "));
this.symbolMetrics[b]=[Math.max(a,l),m.getCenterLonLat().lon,m.getCenterLonLat().lat];
this.defs.appendChild(g);
return g.id
},getFeatureIdFromEvent:function(a){var c=OpenLayers.Renderer.Elements.prototype.getFeatureIdFromEvent.apply(this,arguments);
if(this.supportUse===false&&!c){var b=a.target;
c=b.parentNode&&b!=this.rendererRoot&&b.parentNode._featureId
}return c
},CLASS_NAME:"OpenLayers.Renderer.SVG"});
OpenLayers.Renderer.SVG.LABEL_ALIGN={l:"start",r:"end",b:"bottom",t:"hanging"};
OpenLayers.Renderer.SVG.LABEL_VSHIFT={t:"-70%",b:"0"};OpenLayers.Layer.TileCache=OpenLayers.Class(OpenLayers.Layer.Grid,{isBaseLayer:true,format:"image/png",serverResolutions:null,initialize:function(c,b,d,a){this.layername=d;
OpenLayers.Layer.Grid.prototype.initialize.apply(this,[c,b,{},a]);
this.extension=this.format.split("/")[1].toLowerCase();
this.extension=(this.extension=="jpg")?"jpeg":this.extension
},clone:function(a){if(a==null){a=new OpenLayers.Layer.TileCache(this.name,this.url,this.layername,this.getOptions())
}a=OpenLayers.Layer.Grid.prototype.clone.apply(this,[a]);
return a
},getURL:function(b){var f=this.map.getResolution();
var g=this.maxExtent;
var k=this.tileSize;
var a=Math.round((b.left-g.left)/(f*k.w));
var j=Math.round((b.bottom-g.bottom)/(f*k.h));
var h=this.serverResolutions!=null?OpenLayers.Util.indexOf(this.serverResolutions,f):this.map.getZoom();
function e(o,n){o=String(o);
var l=[];
for(var m=0;
m<n;
++m){l.push("0")
}return l.join("").substring(0,n-o.length)+o
}var d=[this.layername,e(h,2),e(parseInt(a/1000000),3),e((parseInt(a/1000)%1000),3),e((parseInt(a)%1000),3),e(parseInt(j/1000000),3),e((parseInt(j/1000)%1000),3),e((parseInt(j)%1000),3)+"."+this.extension];
var i=d.join("/");
var c=this.url;
if(c instanceof Array){c=this.selectUrl(i,c)
}c=(c.charAt(c.length-1)=="/")?c:c+"/";
return c+i
},addTile:function(c,a){var b=this.getURL(c);
return new OpenLayers.Tile.Image(this,a,c,b,this.tileSize)
},CLASS_NAME:"OpenLayers.Layer.TileCache"});OpenLayers.Control.Button=OpenLayers.Class(OpenLayers.Control,{type:OpenLayers.Control.TYPE_BUTTON,trigger:function(){},CLASS_NAME:"OpenLayers.Control.Button"});OpenLayers.Control.ScaleLine=OpenLayers.Class(OpenLayers.Control,{maxWidth:100,topOutUnits:"km",topInUnits:"m",bottomOutUnits:"mi",bottomInUnits:"ft",eTop:null,eBottom:null,geodesic:false,initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,[a])
},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
if(!this.eTop){this.eTop=document.createElement("div");
this.eTop.className=this.displayClass+"Top";
var a=this.topInUnits.length;
this.div.appendChild(this.eTop);
if((this.topOutUnits=="")||(this.topInUnits=="")){this.eTop.style.visibility="hidden"
}else{this.eTop.style.visibility="visible"
}this.eBottom=document.createElement("div");
this.eBottom.className=this.displayClass+"Bottom";
this.div.appendChild(this.eBottom);
if((this.bottomOutUnits=="")||(this.bottomInUnits=="")){this.eBottom.style.visibility="hidden"
}else{this.eBottom.style.visibility="visible"
}}this.map.events.register("moveend",this,this.update);
this.update();
return this.div
},getBarLen:function(b){var d=parseInt(Math.log(b)/Math.log(10));
var a=Math.pow(10,d);
var c=parseInt(b/a);
var e;
if(c>5){e=5
}else{if(c>2){e=2
}else{e=1
}}return e*a
},update:function(){var j=this.map.getResolution();
if(!j){return
}var o=this.map.getUnits();
var e=OpenLayers.INCHES_PER_UNIT;
var l=this.maxWidth*j*e[o];
var n=1;
if(this.geodesic===true){var b=this.getGeodesicLength(this.maxWidth);
var f=l/e.km;
n=b/f;
l*=n
}var a;
var d;
if(l>100000){a=this.topOutUnits;
d=this.bottomOutUnits
}else{a=this.topInUnits;
d=this.bottomInUnits
}var h=l/e[a];
var k=l/e[d];
var i=this.getBarLen(h);
var g=this.getBarLen(k);
h=i/e[o]*e[a];
k=g/e[o]*e[d];
var c=h/j/n;
var m=k/j/n;
if(this.eBottom.style.visibility=="visible"){this.eBottom.style.width=Math.round(m)+"px";
this.eBottom.innerHTML=g+" "+d
}if(this.eTop.style.visibility=="visible"){this.eTop.style.width=Math.round(c)+"px";
this.eTop.innerHTML=i+" "+a
}},getGeodesicLength:function(g){var f=this.map;
var c=f.getPixelFromLonLat(f.getCenter());
var a=f.getLonLatFromPixel(c.add(0,-g/2));
var e=f.getLonLatFromPixel(c.add(0,g/2));
var d=f.getProjectionObject();
var b=new OpenLayers.Projection("EPSG:4326");
if(!d.equals(b)){a.transform(d,b);
e.transform(d,b)
}return OpenLayers.Util.distVincenty(a,e)
},CLASS_NAME:"OpenLayers.Control.ScaleLine"});OpenLayers.Format.GML.v2=OpenLayers.Class(OpenLayers.Format.GML.Base,{schemaLocation:"http://www.opengis.net/gml http://schemas.opengis.net/gml/2.1.2/feature.xsd",initialize:function(a){OpenLayers.Format.GML.Base.prototype.initialize.apply(this,[a])
},readers:{gml:OpenLayers.Util.applyDefaults({outerBoundaryIs:function(b,a){var c={};
this.readChildNodes(b,c);
a.outer=c.components[0]
},innerBoundaryIs:function(b,a){var c={};
this.readChildNodes(b,c);
a.inner.push(c.components[0])
},Box:function(d,b){var e={};
this.readChildNodes(d,e);
if(!b.components){b.components=[]
}var c=e.points[0];
var a=e.points[1];
b.components.push(new OpenLayers.Bounds(c.x,c.y,a.x,a.y))
}},OpenLayers.Format.GML.Base.prototype.readers.gml),feature:OpenLayers.Format.GML.Base.prototype.readers.feature,wfs:OpenLayers.Format.GML.Base.prototype.readers.wfs},write:function(c){var b;
if(c instanceof Array){b="wfs:FeatureCollection"
}else{b="gml:featureMember"
}var a=this.writeNode(b,c);
this.setAttributeNS(a,this.namespaces.xsi,"xsi:schemaLocation",this.schemaLocation);
return OpenLayers.Format.XML.prototype.write.apply(this,[a])
},writers:{gml:OpenLayers.Util.applyDefaults({Point:function(b){var a=this.createElementNSPlus("gml:Point");
this.writeNode("coordinates",[b],a);
return a
},coordinates:function(d){var c=d.length;
var e=new Array(c);
var a;
for(var b=0;
b<c;
++b){a=d[b];
if(this.xy){e[b]=a.x+","+a.y
}else{e[b]=a.y+","+a.x
}if(a.z!=undefined){e[b]+=","+a.z
}}return this.createElementNSPlus("gml:coordinates",{attributes:{decimal:".",cs:",",ts:" "},value:(c==1)?e[0]:e.join(" ")})
},LineString:function(b){var a=this.createElementNSPlus("gml:LineString");
this.writeNode("coordinates",b.components,a);
return a
},Polygon:function(c){var b=this.createElementNSPlus("gml:Polygon");
this.writeNode("outerBoundaryIs",c.components[0],b);
for(var a=1;
a<c.components.length;
++a){this.writeNode("innerBoundaryIs",c.components[a],b)
}return b
},outerBoundaryIs:function(a){var b=this.createElementNSPlus("gml:outerBoundaryIs");
this.writeNode("LinearRing",a,b);
return b
},innerBoundaryIs:function(a){var b=this.createElementNSPlus("gml:innerBoundaryIs");
this.writeNode("LinearRing",a,b);
return b
},LinearRing:function(a){var b=this.createElementNSPlus("gml:LinearRing");
this.writeNode("coordinates",a.components,b);
return b
},Box:function(b){var a=this.createElementNSPlus("gml:Box");
this.writeNode("coordinates",[{x:b.left,y:b.bottom},{x:b.right,y:b.top}],a);
if(this.srsName){a.setAttribute("srsName",this.srsName)
}return a
}},OpenLayers.Format.GML.Base.prototype.writers.gml),feature:OpenLayers.Format.GML.Base.prototype.writers.feature,wfs:OpenLayers.Format.GML.Base.prototype.writers.wfs},CLASS_NAME:"OpenLayers.Format.GML.v2"});OpenLayers.Format.JSON=OpenLayers.Class(OpenLayers.Format,{indent:"    ",space:" ",newline:"\n",level:0,pretty:false,initialize:function(a){OpenLayers.Format.prototype.initialize.apply(this,[a])
},read:function(json,filter){try{if(/^[\],:{}\s]*$/.test(json.replace(/\\["\\\/bfnrtu]/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,"]").replace(/(?:^|:|,)(?:\s*\[)+/g,""))){var object=eval("("+json+")");
if(typeof filter==="function"){function walk(k,v){if(v&&typeof v==="object"){for(var i in v){if(v.hasOwnProperty(i)){v[i]=walk(i,v[i])
}}}return filter(k,v)
}object=walk("",object)
}if(this.keepData){this.data=object
}return object
}}catch(e){}return null
},write:function(e,c){this.pretty=!!c;
var a=null;
var b=typeof e;
if(this.serialize[b]){try{a=this.serialize[b].apply(this,[e])
}catch(d){OpenLayers.Console.error("Trouble serializing: "+d)
}}return a
},writeIndent:function(){var b=[];
if(this.pretty){for(var a=0;
a<this.level;
++a){b.push(this.indent)
}}return b.join("")
},writeNewline:function(){return(this.pretty)?this.newline:""
},writeSpace:function(){return(this.pretty)?this.space:""
},serialize:{object:function(c){if(c==null){return"null"
}if(c.constructor==Date){return this.serialize.date.apply(this,[c])
}if(c.constructor==Array){return this.serialize.array.apply(this,[c])
}var f=["{"];
this.level+=1;
var d,b,e;
var a=false;
for(d in c){if(c.hasOwnProperty(d)){b=OpenLayers.Format.JSON.prototype.write.apply(this,[d,this.pretty]);
e=OpenLayers.Format.JSON.prototype.write.apply(this,[c[d],this.pretty]);
if(b!=null&&e!=null){if(a){f.push(",")
}f.push(this.writeNewline(),this.writeIndent(),b,":",this.writeSpace(),e);
a=true
}}}this.level-=1;
f.push(this.writeNewline(),this.writeIndent(),"}");
return f.join("")
},array:function(e){var c;
var d=["["];
this.level+=1;
for(var b=0,a=e.length;
b<a;
++b){c=OpenLayers.Format.JSON.prototype.write.apply(this,[e[b],this.pretty]);
if(c!=null){if(b>0){d.push(",")
}d.push(this.writeNewline(),this.writeIndent(),c)
}}this.level-=1;
d.push(this.writeNewline(),this.writeIndent(),"]");
return d.join("")
},string:function(b){var a={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"};
if(/["\\\x00-\x1f]/.test(b)){return'"'+b.replace(/([\x00-\x1f\\"])/g,function(e,d){var f=a[d];
if(f){return f
}f=d.charCodeAt();
return"\\u00"+Math.floor(f/16).toString(16)+(f%16).toString(16)
})+'"'
}return'"'+b+'"'
},number:function(a){return isFinite(a)?String(a):"null"
},"boolean":function(a){return String(a)
},date:function(a){function b(c){return(c<10)?"0"+c:c
}return'"'+a.getFullYear()+"-"+b(a.getMonth()+1)+"-"+b(a.getDate())+"T"+b(a.getHours())+":"+b(a.getMinutes())+":"+b(a.getSeconds())+'"'
}},CLASS_NAME:"OpenLayers.Format.JSON"});OpenLayers.Format.Filter.v1=OpenLayers.Class(OpenLayers.Format.XML,{namespaces:{ogc:"http://www.opengis.net/ogc",gml:"http://www.opengis.net/gml",xlink:"http://www.w3.org/1999/xlink",xsi:"http://www.w3.org/2001/XMLSchema-instance"},defaultPrefix:"ogc",schemaLocation:null,initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},read:function(a){var b={};
this.readers.ogc.Filter.apply(this,[a,b]);
return b.filter
},readers:{ogc:{Filter:function(b,a){var c={fids:[],filters:[]};
this.readChildNodes(b,c);
if(c.fids.length>0){a.filter=new OpenLayers.Filter.FeatureId({fids:c.fids})
}else{if(c.filters.length>0){a.filter=c.filters[0]
}}},FeatureId:function(a,b){var c=a.getAttribute("fid");
if(c){b.fids.push(c)
}},And:function(b,c){var a=new OpenLayers.Filter.Logical({type:OpenLayers.Filter.Logical.AND});
this.readChildNodes(b,a);
c.filters.push(a)
},Or:function(b,c){var a=new OpenLayers.Filter.Logical({type:OpenLayers.Filter.Logical.OR});
this.readChildNodes(b,a);
c.filters.push(a)
},Not:function(b,c){var a=new OpenLayers.Filter.Logical({type:OpenLayers.Filter.Logical.NOT});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsLessThan:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LESS_THAN});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsGreaterThan:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.GREATER_THAN});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsLessThanOrEqualTo:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsGreaterThanOrEqualTo:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsBetween:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.BETWEEN});
this.readChildNodes(b,a);
c.filters.push(a)
},Literal:function(a,b){b.value=OpenLayers.String.numericIf(this.getChildValue(a))
},PropertyName:function(b,a){a.property=this.getChildValue(b)
},LowerBoundary:function(b,a){a.lowerBoundary=OpenLayers.String.numericIf(this.readOgcExpression(b))
},UpperBoundary:function(b,a){a.upperBoundary=OpenLayers.String.numericIf(this.readOgcExpression(b))
},Intersects:function(a,b){this.readSpatial(a,b,OpenLayers.Filter.Spatial.INTERSECTS)
},Within:function(a,b){this.readSpatial(a,b,OpenLayers.Filter.Spatial.WITHIN)
},Contains:function(a,b){this.readSpatial(a,b,OpenLayers.Filter.Spatial.CONTAINS)
},DWithin:function(a,b){this.readSpatial(a,b,OpenLayers.Filter.Spatial.DWITHIN)
},Distance:function(a,b){b.distance=parseInt(this.getChildValue(a));
b.distanceUnits=a.getAttribute("units")
}}},readSpatial:function(c,d,b){var a=new OpenLayers.Filter.Spatial({type:b});
this.readChildNodes(c,a);
a.value=a.components[0];
delete a.components;
d.filters.push(a)
},readOgcExpression:function(a){var c={};
this.readChildNodes(a,c);
var b=c.value;
if(!b){b=this.getChildValue(a)
}return b
},write:function(a){return this.writers.ogc.Filter.apply(this,[a])
},writers:{ogc:{Filter:function(c){var d=this.createElementNSPlus("ogc:Filter");
var b=c.CLASS_NAME.split(".").pop();
if(b=="FeatureId"){for(var a=0;
a<c.fids.length;
++a){this.writeNode("FeatureId",c.fids[a],d)
}}else{this.writeNode(this.getFilterType(c),c,d)
}return d
},FeatureId:function(a){return this.createElementNSPlus("ogc:FeatureId",{attributes:{fid:a}})
},And:function(c){var d=this.createElementNSPlus("ogc:And");
var b;
for(var a=0;
a<c.filters.length;
++a){b=c.filters[a];
this.writeNode(this.getFilterType(b),b,d)
}return d
},Or:function(c){var d=this.createElementNSPlus("ogc:Or");
var b;
for(var a=0;
a<c.filters.length;
++a){b=c.filters[a];
this.writeNode(this.getFilterType(b),b,d)
}return d
},Not:function(b){var c=this.createElementNSPlus("ogc:Not");
var a=b.filters[0];
this.writeNode(this.getFilterType(a),a,c);
return c
},PropertyIsLessThan:function(a){var b=this.createElementNSPlus("ogc:PropertyIsLessThan");
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsGreaterThan:function(a){var b=this.createElementNSPlus("ogc:PropertyIsGreaterThan");
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsLessThanOrEqualTo:function(a){var b=this.createElementNSPlus("ogc:PropertyIsLessThanOrEqualTo");
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsGreaterThanOrEqualTo:function(a){var b=this.createElementNSPlus("ogc:PropertyIsGreaterThanOrEqualTo");
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsBetween:function(a){var b=this.createElementNSPlus("ogc:PropertyIsBetween");
this.writeNode("PropertyName",a,b);
this.writeNode("LowerBoundary",a,b);
this.writeNode("UpperBoundary",a,b);
return b
},PropertyName:function(a){return this.createElementNSPlus("ogc:PropertyName",{value:a.property})
},Literal:function(a){return this.createElementNSPlus("ogc:Literal",{value:a})
},LowerBoundary:function(a){var b=this.createElementNSPlus("ogc:LowerBoundary");
this.writeNode("Literal",a.lowerBoundary,b);
return b
},UpperBoundary:function(a){var b=this.createElementNSPlus("ogc:UpperBoundary");
this.writeNode("Literal",a.upperBoundary,b);
return b
},INTERSECTS:function(a){return this.writeSpatial(a,"Intersects")
},WITHIN:function(a){return this.writeSpatial(a,"Within")
},CONTAINS:function(a){return this.writeSpatial(a,"Contains")
},DWITHIN:function(a){var b=this.writeSpatial(a,"DWithin");
this.writeNode("Distance",a,b);
return b
},Distance:function(a){return this.createElementNSPlus("ogc:Distance",{attributes:{units:a.distanceUnits},value:a.distance})
}}},getFilterType:function(a){var b=this.filterMap[a.type];
if(!b){throw"Filter writing not supported for rule type: "+a.type
}return b
},filterMap:{"&&":"And","||":"Or","!":"Not","==":"PropertyIsEqualTo","!=":"PropertyIsNotEqualTo","<":"PropertyIsLessThan",">":"PropertyIsGreaterThan","<=":"PropertyIsLessThanOrEqualTo",">=":"PropertyIsGreaterThanOrEqualTo","..":"PropertyIsBetween","~":"PropertyIsLike",BBOX:"BBOX",DWITHIN:"DWITHIN",WITHIN:"WITHIN",CONTAINS:"CONTAINS",INTERSECTS:"INTERSECTS"},CLASS_NAME:"OpenLayers.Format.Filter.v1"});OpenLayers.Format.Filter.v1_0_0=OpenLayers.Class(OpenLayers.Format.GML.v2,OpenLayers.Format.Filter.v1,{VERSION:"1.0.0",schemaLocation:"http://www.opengis.net/ogc/filter/1.0.0/filter.xsd",initialize:function(a){OpenLayers.Format.GML.v2.prototype.initialize.apply(this,[a])
},readers:{ogc:OpenLayers.Util.applyDefaults({PropertyIsEqualTo:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsNotEqualTo:function(b,c){var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.NOT_EQUAL_TO});
this.readChildNodes(b,a);
c.filters.push(a)
},PropertyIsLike:function(d,e){var c=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LIKE});
this.readChildNodes(d,c);
var f=d.getAttribute("wildCard");
var b=d.getAttribute("singleChar");
var a=d.getAttribute("escape");
c.value2regex(f,b,a);
e.filters.push(c)
}},OpenLayers.Format.Filter.v1.prototype.readers.ogc),gml:OpenLayers.Format.GML.v2.prototype.readers.gml,feature:OpenLayers.Format.GML.v2.prototype.readers.feature},writers:{ogc:OpenLayers.Util.applyDefaults({PropertyIsEqualTo:function(a){var b=this.createElementNSPlus("ogc:PropertyIsEqualTo");
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsNotEqualTo:function(a){var b=this.createElementNSPlus("ogc:PropertyIsNotEqualTo");
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsLike:function(a){var b=this.createElementNSPlus("ogc:PropertyIsLike",{attributes:{wildCard:"*",singleChar:".",escape:"!"}});
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.regex2value(),b);
return b
},BBOX:function(a){var c=this.createElementNSPlus("ogc:BBOX");
this.writeNode("PropertyName",a,c);
var b=this.writeNode("gml:Box",a.value,c);
if(a.projection){b.setAttribute("srsName",a.projection)
}return c
}},OpenLayers.Format.Filter.v1.prototype.writers.ogc),gml:OpenLayers.Format.GML.v2.prototype.writers.gml,feature:OpenLayers.Format.GML.v2.prototype.writers.feature},writeSpatial:function(b,a){var c=this.createElementNSPlus("ogc:"+a);
this.writeNode("PropertyName",b,c);
var d;
if(b.value instanceof OpenLayers.Geometry){d=this.writeNode("feature:_geometry",b.value).firstChild
}else{d=this.writeNode("gml:Box",b.value)
}if(b.projection){d.setAttribute("srsName",b.projection)
}c.appendChild(d);
return c
},CLASS_NAME:"OpenLayers.Format.Filter.v1_0_0"});OpenLayers.Format.Filter.v1_1_0=OpenLayers.Class(OpenLayers.Format.GML.v3,OpenLayers.Format.Filter.v1,{VERSION:"1.1.0",schemaLocation:"http://www.opengis.net/ogc/filter/1.1.0/filter.xsd",initialize:function(a){OpenLayers.Format.GML.v3.prototype.initialize.apply(this,[a])
},readers:{ogc:OpenLayers.Util.applyDefaults({PropertyIsEqualTo:function(b,d){var c=b.getAttribute("matchCase");
var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,matchCase:!(c==="false"||c==="0")});
this.readChildNodes(b,a);
d.filters.push(a)
},PropertyIsNotEqualTo:function(b,d){var c=b.getAttribute("matchCase");
var a=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.NOT_EQUAL_TO,matchCase:!(c==="false"||c==="0")});
this.readChildNodes(b,a);
d.filters.push(a)
},PropertyIsLike:function(d,e){var c=new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LIKE});
this.readChildNodes(d,c);
var f=d.getAttribute("wildCard");
var b=d.getAttribute("singleChar");
var a=d.getAttribute("escapeChar");
c.value2regex(f,b,a);
e.filters.push(c)
}},OpenLayers.Format.Filter.v1.prototype.readers.ogc),gml:OpenLayers.Format.GML.v3.prototype.readers.gml,feature:OpenLayers.Format.GML.v3.prototype.readers.feature},writers:{ogc:OpenLayers.Util.applyDefaults({PropertyIsEqualTo:function(a){var b=this.createElementNSPlus("ogc:PropertyIsEqualTo",{attributes:{matchCase:a.matchCase}});
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsNotEqualTo:function(a){var b=this.createElementNSPlus("ogc:PropertyIsNotEqualTo",{attributes:{matchCase:a.matchCase}});
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.value,b);
return b
},PropertyIsLike:function(a){var b=this.createElementNSPlus("ogc:PropertyIsLike",{attributes:{wildCard:"*",singleChar:".",escapeChar:"!"}});
this.writeNode("PropertyName",a,b);
this.writeNode("Literal",a.regex2value(),b);
return b
},BBOX:function(a){var c=this.createElementNSPlus("ogc:BBOX");
this.writeNode("PropertyName",a,c);
var b=this.writeNode("gml:Envelope",a.value);
if(a.projection){b.setAttribute("srsName",a.projection)
}c.appendChild(b);
return c
}},OpenLayers.Format.Filter.v1.prototype.writers.ogc),gml:OpenLayers.Format.GML.v3.prototype.writers.gml,feature:OpenLayers.Format.GML.v3.prototype.writers.feature},writeSpatial:function(b,a){var c=this.createElementNSPlus("ogc:"+a);
this.writeNode("PropertyName",b,c);
var d;
if(b.value instanceof OpenLayers.Geometry){d=this.writeNode("feature:_geometry",b.value).firstChild
}else{d=this.writeNode("gml:Envelope",b.value)
}if(b.projection){d.setAttribute("srsName",b.projection)
}c.appendChild(d);
return c
},CLASS_NAME:"OpenLayers.Format.Filter.v1_1_0"});OpenLayers.Format.CSWGetRecords.v2_0_2=OpenLayers.Class(OpenLayers.Format.XML,{namespaces:{xlink:"http://www.w3.org/1999/xlink",xsi:"http://www.w3.org/2001/XMLSchema-instance",csw:"http://www.opengis.net/cat/csw/2.0.2",dc:"http://purl.org/dc/elements/1.1/",dct:"http://purl.org/dc/terms/",ows:"http://www.opengis.net/ows"},defaultPrefix:"csw",version:"2.0.2",schemaLocation:"http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd",requestId:null,resultType:null,outputFormat:null,outputSchema:null,startPosition:null,maxRecords:null,DistributedSearch:null,ResponseHandler:null,Query:null,initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},read:function(a){if(typeof a=="string"){a=OpenLayers.Format.XML.prototype.read.apply(this,[a])
}if(a&&a.nodeType==9){a=a.documentElement
}var b={};
this.readNode(a,b);
return b
},readers:{csw:{GetRecordsResponse:function(b,c){c.records=[];
this.readChildNodes(b,c);
var a=this.getAttributeNS(b,"","version");
if(a!=""){c.version=a
}},RequestId:function(a,b){b.RequestId=this.getChildValue(a)
},SearchStatus:function(a,c){c.SearchStatus={};
var b=this.getAttributeNS(a,"","timestamp");
if(b!=""){c.SearchStatus.timestamp=b
}},SearchResults:function(d,e){this.readChildNodes(d,e);
var b=d.attributes;
var f={};
for(var c=0,a=b.length;
c<a;
++c){if((b[c].name=="numberOfRecordsMatched")||(b[c].name=="numberOfRecordsReturned")||(b[c].name=="nextRecord")){f[b[c].name]=parseInt(b[c].nodeValue)
}else{f[b[c].name]=b[c].nodeValue
}}e.SearchResults=f
},SummaryRecord:function(b,c){var a={type:"SummaryRecord"};
this.readChildNodes(b,a);
c.records.push(a)
},BriefRecord:function(b,c){var a={type:"BriefRecord"};
this.readChildNodes(b,a);
c.records.push(a)
},DCMIRecord:function(b,c){var a={type:"DCMIRecord"};
this.readChildNodes(b,a);
c.records.push(a)
},Record:function(b,c){var a={type:"Record"};
this.readChildNodes(b,a);
c.records.push(a)
}},dc:{"*":function(f,g){var d=f.localName||f.nodeName.split(":").pop();
if(!(g[d] instanceof Array)){g[d]=new Array()
}var c={};
var b=f.attributes;
for(var e=0,a=b.length;
e<a;
++e){c[b[e].name]=b[e].nodeValue
}c.value=this.getChildValue(f);
g[d].push(c)
}},dct:{"*":function(b,c){var a=b.localName||b.nodeName.split(":").pop();
if(!(c[a] instanceof Array)){c[a]=new Array()
}c[a].push(this.getChildValue(b))
}},ows:{WGS84BoundingBox:function(g,h){if(!(h.BoundingBox instanceof Array)){h.BoundingBox=new Array()
}var f=this.getChildValue(this.getElementsByTagNameNS(g,this.namespaces.ows,"LowerCorner")[0]).split(" ",2);
var b=this.getChildValue(this.getElementsByTagNameNS(g,this.namespaces.ows,"UpperCorner")[0]).split(" ",2);
var d={value:[parseFloat(f[0]),parseFloat(f[1]),parseFloat(b[0]),parseFloat(b[1])]};
var c=g.attributes;
for(var e=0,a=c.length;
e<a;
++e){d[c[e].name]=c[e].nodeValue
}h.BoundingBox.push(d)
},BoundingBox:function(a,b){this.readers.ows["WGS84BoundingBox"].apply(this,[a,b])
}}},write:function(a){var b=this.writeNode("csw:GetRecords",a);
return OpenLayers.Format.XML.prototype.write.apply(this,[b])
},writers:{csw:{GetRecords:function(b){if(!b){b={}
}var e=this.createElementNSPlus("csw:GetRecords",{attributes:{service:"CSW",version:this.version,requestId:b.requestId||this.requestId,resultType:b.resultType||this.resultType,outputFormat:b.outputFormat||this.outputFormat,outputSchema:b.outputSchema||this.outputSchema,startPosition:b.startPosition||this.startPosition,maxRecords:b.maxRecords||this.maxRecords}});
if(b.DistributedSearch||this.DistributedSearch){this.writeNode("csw:DistributedSearch",b.DistributedSearch||this.DistributedSearch,e)
}var d=b.ResponseHandler||this.ResponseHandler;
if(d instanceof Array&&d.length>0){for(var c=0,a=d.length;
c<a;
c++){this.writeNode("csw:ResponseHandler",d[c],e)
}}this.writeNode("Query",b.Query||this.Query,e);
return e
},DistributedSearch:function(a){var b=this.createElementNSPlus("csw:DistributedSearch",{attributes:{hopCount:a.hopCount}});
return b
},ResponseHandler:function(a){var b=this.createElementNSPlus("csw:ResponseHandler",{value:a.value});
return b
},Query:function(b){if(!b){b={}
}var e=this.createElementNSPlus("csw:Query",{attributes:{typeNames:b.typeNames||"csw:Record"}});
var d=b.ElementName;
if(d instanceof Array&&d.length>0){for(var c=0,a=d.length;
c<a;
c++){this.writeNode("csw:ElementName",d[c],e)
}}else{this.writeNode("csw:ElementSetName",b.ElementSetName||{value:"summary"},e)
}if(b.Constraint){this.writeNode("csw:Constraint",b.Constraint,e)
}return e
},ElementName:function(a){var b=this.createElementNSPlus("csw:ElementName",{value:a.value});
return b
},ElementSetName:function(a){var b=this.createElementNSPlus("csw:ElementSetName",{attributes:{typeNames:a.typeNames},value:a.value});
return b
},Constraint:function(a){var b=this.createElementNSPlus("csw:Constraint",{attributes:{version:a.version}});
if(a.Filter){var c=new OpenLayers.Format.Filter({version:a.version});
b.appendChild(c.write(a.Filter))
}else{if(a.CqlText){var d=this.createElementNSPlus("CqlText",{value:a.CqlText.value});
b.appendChild(d)
}}return b
}}},CLASS_NAME:"OpenLayers.Format.CSWGetRecords.v2_0_2"});OpenLayers.Format.WMSCapabilities=OpenLayers.Class(OpenLayers.Format.XML,{defaultVersion:"1.1.1",version:null,parser:null,initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a]);
this.options=a
},read:function(e){if(typeof e=="string"){e=OpenLayers.Format.XML.prototype.read.apply(this,[e])
}var c=e.documentElement;
var b=this.version||c.getAttribute("version")||this.defaultVersion;
if(!this.parser||this.parser.version!==b){var d=OpenLayers.Format.WMSCapabilities["v"+b.replace(/\./g,"_")];
if(!d){throw"Can't find a WMS capabilities parser for version "+b
}var f=new d(this.options)
}var a=f.read(e);
a.version=b;
return a
},CLASS_NAME:"OpenLayers.Format.WMSCapabilities"});OpenLayers.Format.WMSCapabilities.v1=OpenLayers.Class(OpenLayers.Format.XML,{namespaces:{wms:"http://www.opengis.net/wms",xlink:"http://www.w3.org/1999/xlink",xsi:"http://www.w3.org/2001/XMLSchema-instance"},defaultPrefix:"wms",initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},read:function(b){if(typeof b=="string"){b=OpenLayers.Format.XML.prototype.read.apply(this,[b])
}if(b&&b.nodeType==9){b=b.documentElement
}var a={};
this.readNode(b,a);
this.postProcessLayers(a);
return a
},postProcessLayers:function(b){if(b.capability){b.capability.layers=[];
var e=b.capability.nestedLayers;
for(var d=0,a=e.length;
d<a;
++d){var c=e[d];
this.processLayer(b.capability,c)
}}},processLayer:function(a,k,c){if(k.formats===undefined){k.formats=a.request.getmap.formats
}if(c){k.styles=k.styles.concat(c.styles);
var f=["queryable","cascaded","fixedWidth","fixedHeight","opaque","noSubsets","llbbox","minScale","maxScale","attribution"];
var n=["srs","bbox","dimensions","authorityURLs"];
var m;
for(var e=0;
e<f.length;
e++){m=f[e];
if(m in c){if(k[m]==null){k[m]=c[m]
}if(k[m]==null){var h=["cascaded","fixedWidth","fixedHeight"];
var b=["queryable","opaque","noSubsets"];
if(OpenLayers.Util.indexOf(h,m)!=-1){k[m]=0
}if(OpenLayers.Util.indexOf(b,m)!=-1){k[m]=false
}}}}for(var e=0;
e<n.length;
e++){m=n[e];
k[m]=OpenLayers.Util.extend(k[m],c[m])
}}for(var g=0,l=k.nestedLayers.length;
g<l;
g++){var d=k.nestedLayers[g];
this.processLayer(a,d,k)
}if(k.name){a.layers.push(k)
}},readers:{wms:{Service:function(a,b){b.service={};
this.readChildNodes(a,b.service)
},Name:function(a,b){b.name=this.getChildValue(a)
},Title:function(a,b){b.title=this.getChildValue(a)
},Abstract:function(a,b){b["abstract"]=this.getChildValue(a)
},BoundingBox:function(b,c){var d={};
d.bbox=[parseFloat(b.getAttribute("minx")),parseFloat(b.getAttribute("miny")),parseFloat(b.getAttribute("maxx")),parseFloat(b.getAttribute("maxy"))];
var a={x:parseFloat(b.getAttribute("resx")),y:parseFloat(b.getAttribute("resy"))};
if(!(isNaN(a.x)&&isNaN(a.y))){d.res=a
}return d
},OnlineResource:function(a,b){b.href=this.getAttributeNS(a,this.namespaces.xlink,"href")
},ContactInformation:function(a,b){b.contactInformation={};
this.readChildNodes(a,b.contactInformation)
},ContactPersonPrimary:function(a,b){b.personPrimary={};
this.readChildNodes(a,b.personPrimary)
},ContactPerson:function(a,b){b.person=this.getChildValue(a)
},ContactOrganization:function(a,b){b.organization=this.getChildValue(a)
},ContactPosition:function(a,b){b.position=this.getChildValue(a)
},ContactAddress:function(a,b){b.contactAddress={};
this.readChildNodes(a,b.contactAddress)
},AddressType:function(a,b){b.type=this.getChildValue(a)
},Address:function(a,b){b.address=this.getChildValue(a)
},City:function(a,b){b.city=this.getChildValue(a)
},StateOrProvince:function(a,b){b.stateOrProvince=this.getChildValue(a)
},PostCode:function(a,b){b.postcode=this.getChildValue(a)
},Country:function(a,b){b.country=this.getChildValue(a)
},ContactVoiceTelephone:function(a,b){b.phone=this.getChildValue(a)
},ContactFacsimileTelephone:function(a,b){b.fax=this.getChildValue(a)
},ContactElectronicMailAddress:function(a,b){b.email=this.getChildValue(a)
},Fees:function(b,c){var a=this.getChildValue(b);
if(a&&a.toLowerCase()!="none"){c.fees=a
}},AccessConstraints:function(a,b){var c=this.getChildValue(a);
if(c&&c.toLowerCase()!="none"){b.accessConstraints=c
}},Capability:function(a,b){b.capability={nestedLayers:[]};
this.readChildNodes(a,b.capability)
},Request:function(a,b){b.request={};
this.readChildNodes(a,b.request)
},GetCapabilities:function(a,b){b.getcapabilities={formats:[]};
this.readChildNodes(a,b.getcapabilities)
},Format:function(a,b){if(b.formats instanceof Array){b.formats.push(this.getChildValue(a))
}else{b.format=this.getChildValue(a)
}},DCPType:function(a,b){this.readChildNodes(a,b)
},HTTP:function(a,b){this.readChildNodes(a,b)
},Get:function(a,b){this.readChildNodes(a,b)
},Post:function(a,b){this.readChildNodes(a,b)
},GetMap:function(a,b){b.getmap={formats:[]};
this.readChildNodes(a,b.getmap)
},GetFeatureInfo:function(a,b){b.getfeatureinfo={formats:[]};
this.readChildNodes(a,b.getfeatureinfo)
},Exception:function(a,b){b.exception={formats:[]};
this.readChildNodes(a,b.exception)
},Layer:function(b,e){var k=b.getAttributeNode("queryable");
var c=(k&&k.specified)?b.getAttribute("queryable"):null;
k=b.getAttributeNode("cascaded");
var i=(k&&k.specified)?b.getAttribute("cascaded"):null;
k=b.getAttributeNode("opaque");
var f=(k&&k.specified)?b.getAttribute("opaque"):null;
var j=b.getAttribute("noSubsets");
var a=b.getAttribute("fixedWidth");
var h=b.getAttribute("fixedHeight");
var g={nestedLayers:[],styles:[],srs:{},metadataURLs:[],bbox:{},dimensions:{},authorityURLs:{},identifiers:{},keywords:[],queryable:(c&&c!=="")?(c==="1"||c==="true"):null,cascaded:(i!==null)?parseInt(i):null,opaque:f?(f==="1"||f==="true"):null,noSubsets:(j!==null)?(j==="1"||j==="true"):null,fixedWidth:(a!=null)?parseInt(a):null,fixedHeight:(h!=null)?parseInt(h):null};
e.nestedLayers.push(g);
this.readChildNodes(b,g);
if(g.name){var d=g.name.split(":");
if(d.length>0){g.prefix=d[0]
}}},Attribution:function(a,b){b.attribution={};
this.readChildNodes(a,b.attribution)
},LogoURL:function(a,b){b.logo={width:a.getAttribute("width"),height:a.getAttribute("height")};
this.readChildNodes(a,b.logo)
},Style:function(b,c){var a={};
c.styles.push(a);
this.readChildNodes(b,a)
},LegendURL:function(b,c){var a={width:b.getAttribute("width"),height:b.getAttribute("height")};
c.legend=a;
this.readChildNodes(b,a)
},MetadataURL:function(a,b){var c={type:a.getAttribute("type")};
b.metadataURLs.push(c);
this.readChildNodes(a,c)
},DataURL:function(a,b){b.dataURL={};
this.readChildNodes(a,b.dataURL)
},FeatureListURL:function(a,b){b.featureListURL={};
this.readChildNodes(a,b.featureListURL)
},AuthorityURL:function(b,d){var a=b.getAttribute("name");
var c={};
this.readChildNodes(b,c);
d.authorityURLs[a]=c.href
},Identifier:function(a,c){var b=a.getAttribute("authority");
c.identifiers[b]=this.getChildValue(a)
},KeywordList:function(a,b){this.readChildNodes(a,b)
},SRS:function(a,b){b.srs[this.getChildValue(a)]=true
}}},CLASS_NAME:"OpenLayers.Format.WMSCapabilities.v1"});OpenLayers.Format.WMSCapabilities.v1_1=OpenLayers.Class(OpenLayers.Format.WMSCapabilities.v1,{readers:{wms:OpenLayers.Util.applyDefaults({WMT_MS_Capabilities:function(a,b){this.readChildNodes(a,b)
},Keyword:function(a,b){if(b.keywords){b.keywords.push(this.getChildValue(a))
}},DescribeLayer:function(a,b){b.describelayer={formats:[]};
this.readChildNodes(a,b.describelayer)
},GetLegendGraphic:function(a,b){b.getlegendgraphic={formats:[]};
this.readChildNodes(a,b.getlegendgraphic)
},GetStyles:function(a,b){b.getstyles={formats:[]};
this.readChildNodes(a,b.getstyles)
},PutStyles:function(a,b){b.putstyles={formats:[]};
this.readChildNodes(a,b.putstyles)
},UserDefinedSymbolization:function(a,b){var c={supportSLD:parseInt(a.getAttribute("SupportSLD"))==1,userLayer:parseInt(a.getAttribute("UserLayer"))==1,userStyle:parseInt(a.getAttribute("UserStyle"))==1,remoteWFS:parseInt(a.getAttribute("RemoteWFS"))==1};
b.userSymbols=c
},LatLonBoundingBox:function(a,b){b.llbbox=[parseFloat(a.getAttribute("minx")),parseFloat(a.getAttribute("miny")),parseFloat(a.getAttribute("maxx")),parseFloat(a.getAttribute("maxy"))]
},BoundingBox:function(a,b){var c=OpenLayers.Format.WMSCapabilities.v1.prototype.readers.wms.BoundingBox.apply(this,[a,b]);
c.srs=a.getAttribute("SRS");
b.bbox[c.srs]=c
},ScaleHint:function(e,f){var d=e.getAttribute("min");
var a=e.getAttribute("max");
var c=Math.pow(2,0.5);
var b=OpenLayers.INCHES_PER_UNIT.m;
f.maxScale=parseFloat(((d/c)*b*OpenLayers.DOTS_PER_INCH).toPrecision(13));
f.minScale=parseFloat(((a/c)*b*OpenLayers.DOTS_PER_INCH).toPrecision(13))
},Dimension:function(b,d){var a=b.getAttribute("name").toLowerCase();
var c={name:a,units:b.getAttribute("units"),unitsymbol:b.getAttribute("unitSymbol")};
d.dimensions[c.name]=c
},Extent:function(d,e){var b=d.getAttribute("name").toLowerCase();
if(b in e.dimensions){var c=e.dimensions[b];
c.nearestVal=d.getAttribute("nearestValue")==="1";
c.multipleVal=d.getAttribute("multipleValues")==="1";
c.current=d.getAttribute("current")==="1";
c["default"]=d.getAttribute("default")||"";
var a=this.getChildValue(d);
c.values=a.split(",")
}}},OpenLayers.Format.WMSCapabilities.v1.prototype.readers.wms)},CLASS_NAME:"OpenLayers.Format.WMSCapabilities.v1_1"});OpenLayers.Format.WMSCapabilities.v1_1_0=OpenLayers.Class(OpenLayers.Format.WMSCapabilities.v1_1,{version:"1.1.0",initialize:function(a){OpenLayers.Format.WMSCapabilities.v1_1.prototype.initialize.apply(this,[a])
},readers:{wms:OpenLayers.Util.applyDefaults({SRS:function(e,f){var d=this.getChildValue(e);
var b=d.split(/ +/);
for(var c=0,a=b.length;
c<a;
c++){f.srs[b[c]]=true
}}},OpenLayers.Format.WMSCapabilities.v1_1.prototype.readers.wms)},CLASS_NAME:"OpenLayers.Format.WMSCapabilities.v1_1_0"});OpenLayers.Format.WMSCapabilities.v1_1_1=OpenLayers.Class(OpenLayers.Format.WMSCapabilities.v1_1,{version:"1.1.1",initialize:function(a){OpenLayers.Format.WMSCapabilities.v1_1.prototype.initialize.apply(this,[a])
},readers:{wms:OpenLayers.Util.applyDefaults({SRS:function(a,b){b.srs[this.getChildValue(a)]=true
}},OpenLayers.Format.WMSCapabilities.v1_1.prototype.readers.wms)},CLASS_NAME:"OpenLayers.Format.WMSCapabilities.v1_1_1"});OpenLayers.Format.WMSCapabilities.v1_3=OpenLayers.Class(OpenLayers.Format.WMSCapabilities.v1,{readers:{wms:OpenLayers.Util.applyDefaults({WMS_Capabilities:function(a,b){this.readChildNodes(a,b)
},LayerLimit:function(a,b){b.layerLimit=parseInt(this.getChildValue(a))
},MaxWidth:function(a,b){b.maxWidth=parseInt(this.getChildValue(a))
},MaxHeight:function(a,b){b.maxHeight=parseInt(this.getChildValue(a))
},BoundingBox:function(a,b){var c=OpenLayers.Format.WMSCapabilities.v1.prototype.readers.wms.BoundingBox.apply(this,[a,b]);
c.srs=a.getAttribute("CRS");
b.bbox[c.srs]=c
},CRS:function(a,b){this.readers.wms.SRS.apply(this,[a,b])
},EX_GeographicBoundingBox:function(a,b){b.llbbox=[];
this.readChildNodes(a,b.llbbox)
},westBoundLongitude:function(a,b){b[0]=this.getChildValue(a)
},eastBoundLongitude:function(a,b){b[2]=this.getChildValue(a)
},southBoundLatitude:function(a,b){b[1]=this.getChildValue(a)
},northBoundLatitude:function(a,b){b[3]=this.getChildValue(a)
},MinScaleDenominator:function(a,b){b.maxScale=parseFloat(this.getChildValue(a)).toPrecision(16)
},MaxScaleDenominator:function(a,b){b.minScale=parseFloat(this.getChildValue(a)).toPrecision(16)
},Dimension:function(b,d){var a=b.getAttribute("name").toLowerCase();
var c={name:a,units:b.getAttribute("units"),unitsymbol:b.getAttribute("unitSymbol"),nearestVal:b.getAttribute("nearestValue")==="1",multipleVal:b.getAttribute("multipleValues")==="1","default":b.getAttribute("default")||"",current:b.getAttribute("current")==="1",values:this.getChildValue(b).split(",")};
d.dimensions[c.name]=c
},Keyword:function(b,c){var a={value:this.getChildValue(b),vocabulary:b.getAttribute("vocabulary")};
if(c.keywords){c.keywords.push(a)
}}},OpenLayers.Format.WMSCapabilities.v1.prototype.readers.wms),sld:{UserDefinedSymbolization:function(a,b){this.readers.wms.UserDefinedSymbolization.apply(this,[a,b]);
b.userSymbols.inlineFeature=parseInt(a.getAttribute("InlineFeature"))==1;
b.userSymbols.remoteWCS=parseInt(a.getAttribute("RemoteWCS"))==1
},DescribeLayer:function(a,b){this.readers.wms.DescribeLayer.apply(this,[a,b])
},GetLegendGraphic:function(a,b){this.readers.wms.GetLegendGraphic.apply(this,[a,b])
}}},CLASS_NAME:"OpenLayers.Format.WMSCapabilities.v1_3"});OpenLayers.Format.WMSCapabilities.v1_3_0=OpenLayers.Class(OpenLayers.Format.WMSCapabilities.v1_3,{version:"1.3.0",CLASS_NAME:"OpenLayers.Format.WMSCapabilities.v1_3_0"});OpenLayers.Control.Scale=OpenLayers.Class(OpenLayers.Control,{element:null,initialize:function(b,a){OpenLayers.Control.prototype.initialize.apply(this,[a]);
this.element=OpenLayers.Util.getElement(b)
},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
if(!this.element){this.element=document.createElement("div");
this.div.appendChild(this.element)
}this.map.events.register("moveend",this,this.updateScale);
this.updateScale();
return this.div
},updateScale:function(){var a=this.map.getScale();
if(!a){return
}if(a>=9500&&a<=950000){a=Math.round(a/1000)+"K"
}else{if(a>=950000){a=Math.round(a/1000000)+"M"
}else{a=Math.round(a)
}}this.element.innerHTML=OpenLayers.i18n("scale",{scaleDenom:a})
},CLASS_NAME:"OpenLayers.Control.Scale"});OpenLayers.Control.DrawFeature=OpenLayers.Class(OpenLayers.Control,{layer:null,callbacks:null,EVENT_TYPES:["featureadded"],multi:false,featureAdded:function(){},handlerOptions:null,initialize:function(b,c,a){this.EVENT_TYPES=OpenLayers.Control.DrawFeature.prototype.EVENT_TYPES.concat(OpenLayers.Control.prototype.EVENT_TYPES);
OpenLayers.Control.prototype.initialize.apply(this,[a]);
this.callbacks=OpenLayers.Util.extend({done:this.drawFeature,modify:function(f,e){this.layer.events.triggerEvent("sketchmodified",{vertex:f,feature:e})
},create:function(f,e){this.layer.events.triggerEvent("sketchstarted",{vertex:f,feature:e})
}},this.callbacks);
this.layer=b;
this.handlerOptions=this.handlerOptions||{};
if(!("multi" in this.handlerOptions)){this.handlerOptions.multi=this.multi
}var d=this.layer.styleMap&&this.layer.styleMap.styles.temporary;
if(d){this.handlerOptions.layerOptions=OpenLayers.Util.applyDefaults(this.handlerOptions.layerOptions,{styleMap:new OpenLayers.StyleMap({"default":d})})
}this.handler=new c(this,this.callbacks,this.handlerOptions)
},drawFeature:function(c){var a=new OpenLayers.Feature.Vector(c);
var b=this.layer.events.triggerEvent("sketchcomplete",{feature:a});
if(b!==false){a.state=OpenLayers.State.INSERT;
this.layer.addFeatures([a]);
this.featureAdded(a);
this.events.triggerEvent("featureadded",{feature:a})
}},CLASS_NAME:"OpenLayers.Control.DrawFeature"});OpenLayers.Rule=OpenLayers.Class({id:null,name:"default",title:null,description:null,context:null,filter:null,elseFilter:false,symbolizer:null,minScaleDenominator:null,maxScaleDenominator:null,initialize:function(a){this.symbolizer={};
OpenLayers.Util.extend(this,a);
this.id=OpenLayers.Util.createUniqueID(this.CLASS_NAME+"_")
},destroy:function(){for(var a in this.symbolizer){this.symbolizer[a]=null
}this.symbolizer=null
},evaluate:function(c){var b=this.getContext(c);
var a=true;
if(this.minScaleDenominator||this.maxScaleDenominator){var d=c.layer.map.getScale()
}if(this.minScaleDenominator){a=d>=OpenLayers.Style.createLiteral(this.minScaleDenominator,b)
}if(a&&this.maxScaleDenominator){a=d<OpenLayers.Style.createLiteral(this.maxScaleDenominator,b)
}if(a&&this.filter){if(this.filter.CLASS_NAME=="OpenLayers.Filter.FeatureId"){a=this.filter.evaluate(c)
}else{a=this.filter.evaluate(b)
}}return a
},getContext:function(b){var a=this.context;
if(!a){a=b.attributes||b.data
}if(typeof this.context=="function"){a=this.context(b)
}return a
},clone:function(){var a=OpenLayers.Util.extend({},this);
a.symbolizer={};
for(var b in this.symbolizer){value=this.symbolizer[b];
type=typeof value;
if(type==="object"){a.symbolizer[b]=OpenLayers.Util.extend({},value)
}else{if(type==="string"){a.symbolizer[b]=value
}}}a.filter=this.filter&&this.filter.clone();
a.context=this.context&&OpenLayers.Util.extend({},this.context);
return new OpenLayers.Rule(a)
},CLASS_NAME:"OpenLayers.Rule"});OpenLayers.Handler.Hover=OpenLayers.Class(OpenLayers.Handler,{delay:500,pixelTolerance:null,stopMove:false,px:null,timerId:null,initialize:function(c,b,a){OpenLayers.Handler.prototype.initialize.apply(this,arguments)
},mousemove:function(a){if(this.passesTolerance(a.xy)){this.clearTimer();
this.callback("move",[a]);
this.px=a.xy;
a=OpenLayers.Util.extend({},a);
this.timerId=window.setTimeout(OpenLayers.Function.bind(this.delayedCall,this,a),this.delay)
}return !this.stopMove
},mouseout:function(a){if(OpenLayers.Util.mouseLeft(a,this.map.div)){this.clearTimer();
this.callback("move",[a])
}return true
},passesTolerance:function(b){var c=true;
if(this.pixelTolerance&&this.px){var a=Math.sqrt(Math.pow(this.px.x-b.x,2)+Math.pow(this.px.y-b.y,2));
if(a<this.pixelTolerance){c=false
}}return c
},clearTimer:function(){if(this.timerId!=null){window.clearTimeout(this.timerId);
this.timerId=null
}},delayedCall:function(a){this.callback("pause",[a])
},deactivate:function(){var a=false;
if(OpenLayers.Handler.prototype.deactivate.apply(this,arguments)){this.clearTimer();
a=true
}return a
},CLASS_NAME:"OpenLayers.Handler.Hover"});OpenLayers.Control.MouseDefaults=OpenLayers.Class(OpenLayers.Control,{performedDrag:false,wheelObserver:null,initialize:function(){OpenLayers.Control.prototype.initialize.apply(this,arguments)
},destroy:function(){if(this.handler){this.handler.destroy()
}this.handler=null;
this.map.events.un({click:this.defaultClick,dblclick:this.defaultDblClick,mousedown:this.defaultMouseDown,mouseup:this.defaultMouseUp,mousemove:this.defaultMouseMove,mouseout:this.defaultMouseOut,scope:this});
OpenLayers.Event.stopObserving(window,"DOMMouseScroll",this.wheelObserver);
OpenLayers.Event.stopObserving(window,"mousewheel",this.wheelObserver);
OpenLayers.Event.stopObserving(document,"mousewheel",this.wheelObserver);
this.wheelObserver=null;
OpenLayers.Control.prototype.destroy.apply(this,arguments)
},draw:function(){this.map.events.on({click:this.defaultClick,dblclick:this.defaultDblClick,mousedown:this.defaultMouseDown,mouseup:this.defaultMouseUp,mousemove:this.defaultMouseMove,mouseout:this.defaultMouseOut,scope:this});
this.registerWheelEvents()
},registerWheelEvents:function(){this.wheelObserver=OpenLayers.Function.bindAsEventListener(this.onWheelEvent,this);
OpenLayers.Event.observe(window,"DOMMouseScroll",this.wheelObserver);
OpenLayers.Event.observe(window,"mousewheel",this.wheelObserver);
OpenLayers.Event.observe(document,"mousewheel",this.wheelObserver)
},defaultClick:function(b){if(!OpenLayers.Event.isLeftClick(b)){return
}var a=!this.performedDrag;
this.performedDrag=false;
return a
},defaultDblClick:function(b){var a=this.map.getLonLatFromViewPortPx(b.xy);
this.map.setCenter(a,this.map.zoom+1);
OpenLayers.Event.stop(b);
return false
},defaultMouseDown:function(a){if(!OpenLayers.Event.isLeftClick(a)){return
}this.mouseDragStart=a.xy.clone();
this.performedDrag=false;
if(a.shiftKey){this.map.div.style.cursor="crosshair";
this.zoomBox=OpenLayers.Util.createDiv("zoomBox",this.mouseDragStart,null,null,"absolute","2px solid red");
this.zoomBox.style.backgroundColor="white";
this.zoomBox.style.filter="alpha(opacity=50)";
this.zoomBox.style.opacity="0.50";
this.zoomBox.style.fontSize="1px";
this.zoomBox.style.zIndex=this.map.Z_INDEX_BASE.Popup-1;
this.map.viewPortDiv.appendChild(this.zoomBox)
}document.onselectstart=OpenLayers.Function.False;
OpenLayers.Event.stop(a)
},defaultMouseMove:function(e){this.mousePosition=e.xy.clone();
if(this.mouseDragStart!=null){if(this.zoomBox){var d=Math.abs(this.mouseDragStart.x-e.xy.x);
var b=Math.abs(this.mouseDragStart.y-e.xy.y);
this.zoomBox.style.width=Math.max(1,d)+"px";
this.zoomBox.style.height=Math.max(1,b)+"px";
if(e.xy.x<this.mouseDragStart.x){this.zoomBox.style.left=e.xy.x+"px"
}if(e.xy.y<this.mouseDragStart.y){this.zoomBox.style.top=e.xy.y+"px"
}}else{var d=this.mouseDragStart.x-e.xy.x;
var b=this.mouseDragStart.y-e.xy.y;
var f=this.map.getSize();
var a=new OpenLayers.Pixel(f.w/2+d,f.h/2+b);
var c=this.map.getLonLatFromViewPortPx(a);
this.map.setCenter(c,null,true);
this.mouseDragStart=e.xy.clone();
this.map.div.style.cursor="move"
}this.performedDrag=true
}},defaultMouseUp:function(a){if(!OpenLayers.Event.isLeftClick(a)){return
}if(this.zoomBox){this.zoomBoxEnd(a)
}else{if(this.performedDrag){this.map.setCenter(this.map.center)
}}document.onselectstart=null;
this.mouseDragStart=null;
this.map.div.style.cursor=""
},defaultMouseOut:function(a){if(this.mouseDragStart!=null&&OpenLayers.Util.mouseLeft(a,this.map.div)){if(this.zoomBox){this.removeZoomBox()
}this.mouseDragStart=null
}},defaultWheelUp:function(a){if(this.map.getZoom()<=this.map.getNumZoomLevels()){this.map.setCenter(this.map.getLonLatFromPixel(a.xy),this.map.getZoom()+1)
}},defaultWheelDown:function(a){if(this.map.getZoom()>0){this.map.setCenter(this.map.getLonLatFromPixel(a.xy),this.map.getZoom()-1)
}},zoomBoxEnd:function(b){if(this.mouseDragStart!=null){if(Math.abs(this.mouseDragStart.x-b.xy.x)>5||Math.abs(this.mouseDragStart.y-b.xy.y)>5){var h=this.map.getLonLatFromViewPortPx(this.mouseDragStart);
var a=this.map.getLonLatFromViewPortPx(b.xy);
var g=Math.max(h.lat,a.lat);
var c=Math.min(h.lat,a.lat);
var f=Math.min(h.lon,a.lon);
var d=Math.max(h.lon,a.lon);
var e=new OpenLayers.Bounds(f,c,d,g);
this.map.zoomToExtent(e)
}else{var a=this.map.getLonLatFromViewPortPx(b.xy);
this.map.setCenter(new OpenLayers.LonLat((a.lon),(a.lat)),this.map.getZoom()+1)
}this.removeZoomBox()
}},removeZoomBox:function(){this.map.viewPortDiv.removeChild(this.zoomBox);
this.zoomBox=null
},onWheelEvent:function(c){var b=false;
var a=OpenLayers.Event.element(c);
while(a!=null){if(this.map&&a==this.map.div){b=true;
break
}a=a.parentNode
}if(b){var d=0;
if(!c){c=window.event
}if(c.wheelDelta){d=c.wheelDelta/120;
if(window.opera&&window.opera.version()<9.2){d=-d
}}else{if(c.detail){d=-c.detail/3
}}if(d){c.xy=this.mousePosition;
if(d<0){this.defaultWheelDown(c)
}else{this.defaultWheelUp(c)
}}OpenLayers.Event.stop(c)
}},CLASS_NAME:"OpenLayers.Control.MouseDefaults"});OpenLayers.Control.MousePosition=OpenLayers.Class(OpenLayers.Control,{element:null,prefix:"",separator:", ",suffix:"",numDigits:5,granularity:10,emptyString:null,lastXy:null,displayProjection:null,initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,arguments)
},destroy:function(){if(this.map){this.map.events.unregister("mousemove",this,this.redraw)
}OpenLayers.Control.prototype.destroy.apply(this,arguments)
},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
if(!this.element){this.div.left="";
this.div.top="";
this.element=this.div
}this.redraw();
return this.div
},redraw:function(a){var c;
if(a==null){this.reset();
return
}else{if(this.lastXy==null||Math.abs(a.xy.x-this.lastXy.x)>this.granularity||Math.abs(a.xy.y-this.lastXy.y)>this.granularity){this.lastXy=a.xy;
return
}c=this.map.getLonLatFromPixel(a.xy);
if(!c){return
}if(this.displayProjection){c.transform(this.map.getProjectionObject(),this.displayProjection)
}this.lastXy=a.xy
}var b=this.formatOutput(c);
if(b!=this.element.innerHTML){this.element.innerHTML=b
}},reset:function(a){if(this.emptyString!=null){this.element.innerHTML=this.emptyString
}},formatOutput:function(b){var c=parseInt(this.numDigits);
var a=this.prefix+b.lon.toFixed(c)+this.separator+b.lat.toFixed(c)+this.suffix;
return a
},setMap:function(){OpenLayers.Control.prototype.setMap.apply(this,arguments);
this.map.events.register("mousemove",this,this.redraw);
this.map.events.register("mouseout",this,this.reset)
},CLASS_NAME:"OpenLayers.Control.MousePosition"});OpenLayers.Handler.RegularPolygon=OpenLayers.Class(OpenLayers.Handler.Drag,{sides:4,radius:null,snapAngle:null,snapToggle:"shiftKey",persist:false,irregular:false,angle:null,fixedRadius:false,feature:null,layer:null,origin:null,initialize:function(c,b,a){this.style=OpenLayers.Util.extend(OpenLayers.Feature.Vector.style["default"],{});
OpenLayers.Handler.prototype.initialize.apply(this,[c,b,a]);
this.options=(a)?a:new Object()
},setOptions:function(a){OpenLayers.Util.extend(this.options,a);
OpenLayers.Util.extend(this,a)
},activate:function(){var a=false;
if(OpenLayers.Handler.prototype.activate.apply(this,arguments)){var b={displayInLayerSwitcher:false,calculateInRange:OpenLayers.Function.True};
this.layer=new OpenLayers.Layer.Vector(this.CLASS_NAME,b);
this.map.addLayer(this.layer);
a=true
}return a
},deactivate:function(){var a=false;
if(OpenLayers.Handler.Drag.prototype.deactivate.apply(this,arguments)){if(this.dragging){this.cancel()
}if(this.layer.map!=null){this.layer.destroy(false);
if(this.feature){this.feature.destroy()
}}this.layer=null;
this.feature=null;
a=true
}return a
},down:function(a){this.fixedRadius=!!(this.radius);
var b=this.map.getLonLatFromPixel(a.xy);
this.origin=new OpenLayers.Geometry.Point(b.lon,b.lat);
if(!this.fixedRadius||this.irregular){this.radius=this.map.getResolution()
}if(this.persist){this.clear()
}this.feature=new OpenLayers.Feature.Vector();
this.createGeometry();
this.callback("create",[this.origin,this.feature]);
this.layer.addFeatures([this.feature],{silent:true});
this.layer.drawFeature(this.feature,this.style)
},move:function(c){var f=this.map.getLonLatFromPixel(c.xy);
var a=new OpenLayers.Geometry.Point(f.lon,f.lat);
if(this.irregular){var g=Math.sqrt(2)*Math.abs(a.y-this.origin.y)/2;
this.radius=Math.max(this.map.getResolution()/2,g)
}else{if(this.fixedRadius){this.origin=a
}else{this.calculateAngle(a,c);
this.radius=Math.max(this.map.getResolution()/2,a.distanceTo(this.origin))
}}this.modifyGeometry();
if(this.irregular){var d=a.x-this.origin.x;
var b=a.y-this.origin.y;
var e;
if(b==0){e=d/(this.radius*Math.sqrt(2))
}else{e=d/b
}this.feature.geometry.resize(1,this.origin,e);
this.feature.geometry.move(d/2,b/2)
}this.layer.drawFeature(this.feature,this.style)
},up:function(a){this.finalize();
if(this.start==this.last){this.callback("done",[a.xy])
}},out:function(a){this.finalize()
},createGeometry:function(){this.angle=Math.PI*((1/this.sides)-(1/2));
if(this.snapAngle){this.angle+=this.snapAngle*(Math.PI/180)
}this.feature.geometry=OpenLayers.Geometry.Polygon.createRegularPolygon(this.origin,this.radius,this.sides,this.snapAngle)
},modifyGeometry:function(){var f,c,b,a;
var d=this.feature.geometry.components[0];
if(d.components.length!=(this.sides+1)){this.createGeometry();
d=this.feature.geometry.components[0]
}for(var e=0;
e<this.sides;
++e){a=d.components[e];
f=this.angle+(e*2*Math.PI/this.sides);
a.x=this.origin.x+(this.radius*Math.cos(f));
a.y=this.origin.y+(this.radius*Math.sin(f));
a.clearBounds()
}},calculateAngle:function(a,b){var d=Math.atan2(a.y-this.origin.y,a.x-this.origin.x);
if(this.snapAngle&&(this.snapToggle&&!b[this.snapToggle])){var c=(Math.PI/180)*this.snapAngle;
this.angle=Math.round(d/c)*c
}else{this.angle=d
}},cancel:function(){this.callback("cancel",null);
this.finalize()
},finalize:function(){this.origin=null;
this.radius=this.options.radius
},clear:function(){this.layer.renderer.clear();
this.layer.destroyFeatures()
},callback:function(b,a){if(this.callbacks[b]){this.callbacks[b].apply(this.control,[this.feature.geometry.clone()])
}if(!this.persist&&(b=="done"||b=="cancel")){this.clear()
}},CLASS_NAME:"OpenLayers.Handler.RegularPolygon"});OpenLayers.Renderer.VML=OpenLayers.Class(OpenLayers.Renderer.Elements,{xmlns:"urn:schemas-microsoft-com:vml",symbolCache:{},offset:null,initialize:function(b){if(!this.supported()){return
}if(!document.namespaces.olv){document.namespaces.add("olv",this.xmlns);
var e=document.createStyleSheet();
var c=["shape","rect","oval","fill","stroke","imagedata","group","textbox"];
for(var d=0,a=c.length;
d<a;
d++){e.addRule("olv\\:"+c[d],"behavior: url(#default#VML); position: absolute; display: inline-block;")
}}OpenLayers.Renderer.Elements.prototype.initialize.apply(this,arguments)
},destroy:function(){OpenLayers.Renderer.Elements.prototype.destroy.apply(this,arguments)
},supported:function(){return !!(document.namespaces)
},setExtent:function(j,a){OpenLayers.Renderer.Elements.prototype.setExtent.apply(this,arguments);
var c=this.getResolution();
var b=(j.left/c)|0;
var f=(j.top/c-this.size.h)|0;
if(a||!this.offset){this.offset={x:b,y:f};
b=0;
f=0
}else{b=b-this.offset.x;
f=f-this.offset.y
}var l=b+" "+f;
this.root.coordorigin=l;
var h=[this.root,this.vectorRoot,this.textRoot];
var g;
for(var d=0,e=h.length;
d<e;
++d){g=h[d];
var k=this.size.w+" "+this.size.h;
g.coordsize=k
}this.root.style.flip="y";
return true
},setSize:function(f){OpenLayers.Renderer.prototype.setSize.apply(this,arguments);
var d=[this.rendererRoot,this.root,this.vectorRoot,this.textRoot];
var c=this.size.w+"px";
var g=this.size.h+"px";
var b;
for(var e=0,a=d.length;
e<a;
++e){b=d[e];
b.style.width=c;
b.style.height=g
}},getNodeType:function(c,b){var a=null;
switch(c.CLASS_NAME){case"OpenLayers.Geometry.Point":if(b.externalGraphic){a="olv:rect"
}else{if(this.isComplexSymbol(b.graphicName)){a="olv:shape"
}else{a="olv:oval"
}}break;
case"OpenLayers.Geometry.Rectangle":a="olv:rect";
break;
case"OpenLayers.Geometry.LineString":case"OpenLayers.Geometry.LinearRing":case"OpenLayers.Geometry.Polygon":case"OpenLayers.Geometry.Curve":case"OpenLayers.Geometry.Surface":a="olv:shape";
break;
default:break
}return a
},setStyle:function(l,p,a,c){p=p||l._style;
a=a||l._options;
var g=1;
var b=p.fillColor;
if(l._geometryClass==="OpenLayers.Geometry.Point"){if(p.externalGraphic){if(p.graphicTitle){l.title=p.graphicTitle
}var k=p.graphicWidth||p.graphicHeight;
var i=p.graphicHeight||p.graphicWidth;
k=k?k:p.pointRadius*2;
i=i?i:p.pointRadius*2;
var n=this.getResolution();
var r=(p.graphicXOffset!=undefined)?p.graphicXOffset:-(0.5*k);
var e=(p.graphicYOffset!=undefined)?p.graphicYOffset:-(0.5*i);
l.style.left=(((c.x/n-this.offset.x)+r)|0)+"px";
l.style.top=(((c.y/n-this.offset.y)-(e+i))|0)+"px";
l.style.width=k+"px";
l.style.height=i+"px";
l.style.flip="y";
b="none";
a.isStroked=false
}else{if(this.isComplexSymbol(p.graphicName)){var f=this.importSymbol(p.graphicName);
l.path=f.path;
l.coordorigin=f.left+","+f.bottom;
var h=f.size;
l.coordsize=h+","+h;
this.drawCircle(l,c,p.pointRadius);
l.style.flip="y"
}else{this.drawCircle(l,c,p.pointRadius)
}}}if(a.isFilled){l.fillcolor=b
}else{l.filled="false"
}var j=l.getElementsByTagName("fill");
var o=(j.length==0)?null:j[0];
if(!a.isFilled){if(o){l.removeChild(o)
}}else{if(!o){o=this.createNode("olv:fill",l.id+"_fill")
}o.opacity=p.fillOpacity;
if(l._geometryClass==="OpenLayers.Geometry.Point"&&p.externalGraphic){if(p.graphicOpacity){o.opacity=p.graphicOpacity
}o.src=p.externalGraphic;
o.type="frame";
if(!(p.graphicWidth&&p.graphicHeight)){o.aspect="atmost"
}}if(o.parentNode!=l){l.appendChild(o)
}}var m=p.rotation;
if(m!==l._rotation){l._rotation=m;
if(p.externalGraphic){this.graphicRotate(l,r,e,p);
o.opacity=0
}else{if(l._geometryClass==="OpenLayers.Geometry.Point"){l.style.rotation=m||0
}}}if(a.isStroked){l.strokecolor=p.strokeColor;
l.strokeweight=p.strokeWidth+"px"
}else{l.stroked=false
}var q=l.getElementsByTagName("stroke");
var d=(q.length==0)?null:q[0];
if(!a.isStroked){if(d){l.removeChild(d)
}}else{if(!d){d=this.createNode("olv:stroke",l.id+"_stroke");
l.appendChild(d)
}d.opacity=p.strokeOpacity;
d.endcap=!p.strokeLinecap||p.strokeLinecap=="butt"?"flat":p.strokeLinecap;
d.dashstyle=this.dashStyle(p)
}if(p.cursor!="inherit"&&p.cursor!=null){l.style.cursor=p.cursor
}return l
},graphicRotate:function(o,s,f,r){var r=r||o._style;
var d=o._options;
var q=r.rotation||0;
var a,k;
if(!(r.graphicWidth&&r.graphicHeight)){var t=new Image();
t.onreadystatechange=OpenLayers.Function.bind(function(){if(t.readyState=="complete"||t.readyState=="interactive"){a=t.width/t.height;
k=Math.max(r.pointRadius*2,r.graphicWidth||0,r.graphicHeight||0);
s=s*a;
r.graphicWidth=k*a;
r.graphicHeight=k;
this.graphicRotate(o,s,f,r)
}},this);
t.src=r.externalGraphic;
return
}else{k=Math.max(r.graphicWidth,r.graphicHeight);
a=r.graphicWidth/r.graphicHeight
}var n=Math.round(r.graphicWidth||k*a);
var l=Math.round(r.graphicHeight||k);
o.style.width=n+"px";
o.style.height=l+"px";
var m=document.getElementById(o.id+"_image");
if(!m){m=this.createNode("olv:imagedata",o.id+"_image");
o.appendChild(m)
}m.style.width=n+"px";
m.style.height=l+"px";
m.src=r.externalGraphic;
m.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='', sizingMethod='scale')";
var g=q*Math.PI/180;
var i=Math.sin(g);
var e=Math.cos(g);
var h="progid:DXImageTransform.Microsoft.Matrix(M11="+e+",M12="+(-i)+",M21="+i+",M22="+e+",SizingMethod='auto expand')\n";
var b=r.graphicOpacity||r.fillOpacity;
if(b&&b!=1){h+="progid:DXImageTransform.Microsoft.BasicImage(opacity="+b+")\n"
}o.style.filter=h;
var p=new OpenLayers.Geometry.Point(-s,-f);
var c=new OpenLayers.Bounds(0,0,n,l).toGeometry();
c.rotate(r.rotation,p);
var j=c.getBounds();
o.style.left=Math.round(parseInt(o.style.left)+j.left)+"px";
o.style.top=Math.round(parseInt(o.style.top)-j.bottom)+"px"
},postDraw:function(a){a.style.visibility="visible";
var c=a._style.fillColor;
var b=a._style.strokeColor;
if(c=="none"&&a.fillcolor!=c){a.fillcolor=c
}if(b=="none"&&a.strokecolor!=b){a.strokecolor=b
}},setNodeDimension:function(b,e){var d=e.getBounds();
if(d){var a=this.getResolution();
var c=new OpenLayers.Bounds((d.left/a-this.offset.x)|0,(d.bottom/a-this.offset.y)|0,(d.right/a-this.offset.x)|0,(d.top/a-this.offset.y)|0);
b.style.left=c.left+"px";
b.style.top=c.top+"px";
b.style.width=c.getWidth()+"px";
b.style.height=c.getHeight()+"px";
b.coordorigin=c.left+" "+c.top;
b.coordsize=c.getWidth()+" "+c.getHeight()
}},dashStyle:function(a){var c=a.strokeDashstyle;
switch(c){case"solid":case"dot":case"dash":case"dashdot":case"longdash":case"longdashdot":return c;
default:var b=c.split(/[ ,]/);
if(b.length==2){if(1*b[0]>=2*b[1]){return"longdash"
}return(b[0]==1||b[1]==1)?"dot":"dash"
}else{if(b.length==4){return(1*b[0]>=2*b[1])?"longdashdot":"dashdot"
}}return"solid"
}},createNode:function(a,c){var b=document.createElement(a);
if(c){b.id=c
}b.unselectable="on";
b.onselectstart=OpenLayers.Function.False;
return b
},nodeTypeCompare:function(c,b){var d=b;
var a=d.indexOf(":");
if(a!=-1){d=d.substr(a+1)
}var e=c.nodeName;
a=e.indexOf(":");
if(a!=-1){e=e.substr(a+1)
}return(d==e)
},createRenderRoot:function(){return this.nodeFactory(this.container.id+"_vmlRoot","div")
},createRoot:function(a){return this.nodeFactory(this.container.id+a,"olv:group")
},drawPoint:function(a,b){return this.drawCircle(a,b,1)
},drawCircle:function(d,e,a){if(!isNaN(e.x)&&!isNaN(e.y)){var b=this.getResolution();
d.style.left=(((e.x/b-this.offset.x)|0)-a)+"px";
d.style.top=(((e.y/b-this.offset.y)|0)-a)+"px";
var c=a*2;
d.style.width=c+"px";
d.style.height=c+"px";
return d
}return false
},drawLineString:function(a,b){return this.drawLine(a,b,false)
},drawLinearRing:function(a,b){return this.drawLine(a,b,true)
},drawLine:function(b,k,g){this.setNodeDimension(b,k);
var c=this.getResolution();
var a=k.components.length;
var e=new Array(a);
var h,l,j;
for(var f=0;
f<a;
f++){h=k.components[f];
l=(h.x/c-this.offset.x)|0;
j=(h.y/c-this.offset.y)|0;
e[f]=" "+l+","+j+" l "
}var d=(g)?" x e":" e";
b.path="m"+e.join("")+d;
return b
},drawPolygon:function(b,l){this.setNodeDimension(b,l);
var c=this.getResolution();
var n=[];
var f,e,d,h,a,g,m,k;
for(d=0,h=l.components.length;
d<h;
d++){f=l.components[d];
n.push("m");
for(e=0,a=f.components.length;
e<a;
e++){g=f.components[e];
m=(g.x/c-this.offset.x)|0;
k=(g.y/c-this.offset.y)|0;
n.push(" "+m+","+k);
if(e==0){n.push(" l")
}}n.push(" x ")
}n.push("e");
b.path=n.join("");
return b
},drawRectangle:function(b,c){var a=this.getResolution();
b.style.left=((c.x/a-this.offset.x)|0)+"px";
b.style.top=((c.y/a-this.offset.y)|0)+"px";
b.style.width=((c.width/a)|0)+"px";
b.style.height=((c.height/a)|0)+"px";
return b
},drawText:function(d,a,h){var g=this.nodeFactory(d+this.LABEL_ID_SUFFIX,"olv:rect");
var f=this.nodeFactory(d+this.LABEL_ID_SUFFIX+"_textbox","olv:textbox");
var c=this.getResolution();
g.style.left=((h.x/c-this.offset.x)|0)+"px";
g.style.top=((h.y/c-this.offset.y)|0)+"px";
g.style.flip="y";
f.innerText=a.label;
if(a.fontColor){f.style.color=a.fontColor
}if(a.fontOpacity){f.style.filter="alpha(opacity="+(a.fontOpacity*100)+")"
}if(a.fontFamily){f.style.fontFamily=a.fontFamily
}if(a.fontSize){f.style.fontSize=a.fontSize
}if(a.fontWeight){f.style.fontWeight=a.fontWeight
}if(a.labelSelect===true){g._featureId=d;
f._featureId=d;
f._geometry=h;
f._geometryClass=h.CLASS_NAME
}f.style.whiteSpace="nowrap";
f.inset="1px,0px,0px,0px";
if(!g.parentNode){g.appendChild(f);
this.textRoot.appendChild(g)
}var e=a.labelAlign||"cm";
var i=f.clientWidth*(OpenLayers.Renderer.VML.LABEL_SHIFT[e[0]||"c"]);
var b=f.clientHeight*(OpenLayers.Renderer.VML.LABEL_SHIFT[e[1]||"m"]);
g.style.left=parseInt(g.style.left)-i-1+"px";
g.style.top=parseInt(g.style.top)+b+"px"
},drawSurface:function(a,g){this.setNodeDimension(a,g);
var b=this.getResolution();
var j=[];
var d,h,f;
for(var c=0,e=g.components.length;
c<e;
c++){d=g.components[c];
h=(d.x/b-this.offset.x)|0;
f=(d.y/b-this.offset.y)|0;
if((c%3)==0&&(c/3)==0){j.push("m")
}else{if((c%3)==1){j.push(" c")
}}j.push(" "+h+","+f)
}j.push(" x e");
a.path=j.join("");
return a
},moveRoot:function(b){var a=this.map.getLayer(b.container.id);
if(a instanceof OpenLayers.Layer.Vector.RootContainer){a=this.map.getLayer(this.container.id)
}a&&a.renderer.clear();
OpenLayers.Renderer.Elements.prototype.moveRoot.apply(this,arguments);
a&&a.redraw()
},importSymbol:function(d){var b=this.container.id+"-"+d;
var a=this.symbolCache[b];
if(a){return a
}var c=OpenLayers.Renderer.symbol[d];
if(!c){throw new Error(d+" is not a valid symbol name");
return
}var k=new OpenLayers.Bounds(Number.MAX_VALUE,Number.MAX_VALUE,0,0);
var e=["m"];
for(var f=0;
f<c.length;
f=f+2){var h=c[f];
var g=c[f+1];
k.left=Math.min(k.left,h);
k.bottom=Math.min(k.bottom,g);
k.right=Math.max(k.right,h);
k.top=Math.max(k.top,g);
e.push(h);
e.push(g);
if(f==0){e.push("l")
}}e.push("x e");
var l=e.join(" ");
var j=(k.getWidth()-k.getHeight())/2;
if(j>0){k.bottom=k.bottom-j;
k.top=k.top+j
}else{k.left=k.left+j;
k.right=k.right-j
}a={path:l,size:k.getWidth(),left:k.left,bottom:k.bottom};
this.symbolCache[b]=a;
return a
},CLASS_NAME:"OpenLayers.Renderer.VML"});
OpenLayers.Renderer.VML.LABEL_SHIFT={l:0,c:0.5,r:1,t:0,m:0.5,b:1};OpenLayers.Control.ArgParser=OpenLayers.Class(OpenLayers.Control,{center:null,zoom:null,layers:null,displayProjection:null,initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,arguments)
},setMap:function(e){OpenLayers.Control.prototype.setMap.apply(this,arguments);
for(var c=0,a=this.map.controls.length;
c<a;
c++){var d=this.map.controls[c];
if((d!=this)&&(d.CLASS_NAME=="OpenLayers.Control.ArgParser")){if(d.displayProjection!=this.displayProjection){this.displayProjection=d.displayProjection
}break
}}if(c==this.map.controls.length){var b=OpenLayers.Util.getParameters();
if(b.layers){this.layers=b.layers;
this.map.events.register("addlayer",this,this.configureLayers);
this.configureLayers()
}if(b.lat&&b.lon){this.center=new OpenLayers.LonLat(parseFloat(b.lon),parseFloat(b.lat));
if(b.zoom){this.zoom=parseInt(b.zoom)
}this.map.events.register("changebaselayer",this,this.setCenter);
this.setCenter()
}}},setCenter:function(){if(this.map.baseLayer){this.map.events.unregister("changebaselayer",this,this.setCenter);
if(this.displayProjection){this.center.transform(this.displayProjection,this.map.getProjectionObject())
}this.map.setCenter(this.center,this.zoom)
}},configureLayers:function(){if(this.layers.length==this.map.layers.length){this.map.events.unregister("addlayer",this,this.configureLayers);
for(var d=0,a=this.layers.length;
d<a;
d++){var b=this.map.layers[d];
var e=this.layers.charAt(d);
if(e=="B"){this.map.setBaseLayer(b)
}else{if((e=="T")||(e=="F")){b.setVisibility(e=="T")
}}}}},CLASS_NAME:"OpenLayers.Control.ArgParser"});(function(){var a=function(c,b){return function(d){if(b&&b[c]){b[c].call(b.scope||window,{responseText:d.responseText,responseXML:d.responseXML,argument:b.argument})
}}
};
Ext.apply(Ext.lib.Ajax,{request:function(g,e,b,f,c){c=c||{};
g=g||c.method;
var d=c.headers;
if(c.xmlData){if(!d||!d["Content-Type"]){d=d||{};
d["Content-Type"]="text/xml"
}g=g||"POST";
f=c.xmlData
}else{if(c.jsonData){if(!d||!d["Content-Type"]){d=d||{};
d["Content-Type"]="application/json"
}g=g||"POST";
f=typeof c.jsonData=="object"?Ext.encode(c.jsonData):c.jsonData
}}if((g&&g.toLowerCase()=="post")&&(c.form||c.params)&&(!d||!d["Content-Type"])){d=d||{};
d["Content-Type"]="application/x-www-form-urlencoded"
}return OpenLayers.Request.issue({success:a("success",b),failure:a("failure",b),method:g,headers:d,data:f,url:e})
},isCallInProgress:function(b){return true
},abort:function(b){b.abort()
}})
})();Ext.namespace("GeoExt");
GeoExt.singleFile=true;(function(){var j=(typeof GeoExt=="object"&&GeoExt.singleFile);
var a=j?"GeoExt.js":"lib/GeoExt.js";
var k=function(){var s="";
var o=document.documentElement.getElementsByTagName("script");
for(var q=0,h=o.length;
q<h;
q++){var t=o[q].getAttribute("src");
if(t){var p=t.lastIndexOf(a);
var r=t.lastIndexOf("?");
if(r<0){r=t.length
}if((p>-1)&&(p+a.length==r)){s=t.slice(0,r-a.length);
break
}}}return s
};
if(!j){var l=new Array("GeoExt/data/AttributeReader.js","GeoExt/data/AttributeStore.js","GeoExt/data/FeatureRecord.js","GeoExt/data/FeatureReader.js","GeoExt/data/FeatureStore.js","GeoExt/data/LayerRecord.js","GeoExt/data/LayerReader.js","GeoExt/data/LayerStore.js","GeoExt/data/ScaleStore.js","GeoExt/data/WMSCapabilitiesReader.js","GeoExt/data/WMSCapabilitiesStore.js","GeoExt/data/WFSCapabilitiesReader.js","GeoExt/data/WFSCapabilitiesStore.js","GeoExt/data/WMSDescribeLayerReader.js","GeoExt/data/WMSDescribeLayerStore.js","GeoExt/data/WMCReader.js","GeoExt/widgets/Action.js","GeoExt/data/ProtocolProxy.js","GeoExt/widgets/FeatureRenderer.js","GeoExt/widgets/MapPanel.js","GeoExt/widgets/Popup.js","GeoExt/widgets/form.js","GeoExt/widgets/form/SearchAction.js","GeoExt/widgets/form/BasicForm.js","GeoExt/widgets/form/FormPanel.js","GeoExt/widgets/tips/SliderTip.js","GeoExt/widgets/tips/LayerOpacitySliderTip.js","GeoExt/widgets/tips/ZoomSliderTip.js","GeoExt/widgets/tree/LayerNode.js","GeoExt/widgets/tree/TreeNodeUIEventMixin.js","GeoExt/plugins/TreeNodeComponent.js","GeoExt/plugins/TreeNodeRadioButton.js","GeoExt/widgets/tree/LayerLoader.js","GeoExt/widgets/tree/LayerContainer.js","GeoExt/widgets/tree/BaseLayerContainer.js","GeoExt/widgets/tree/OverlayLayerContainer.js","GeoExt/widgets/tree/LayerParamNode.js","GeoExt/widgets/tree/LayerParamLoader.js","GeoExt/widgets/tree/WMSCapabilitiesLoader.js","GeoExt/widgets/LayerOpacitySlider.js","GeoExt/widgets/LayerLegend.js","GeoExt/widgets/LegendImage.js","GeoExt/widgets/UrlLegend.js","GeoExt/widgets/WMSLegend.js","GeoExt/widgets/VectorLegend.js","GeoExt/widgets/LegendPanel.js","GeoExt/widgets/ZoomSlider.js","GeoExt/widgets/grid/FeatureSelectionModel.js","GeoExt/data/PrintPage.js","GeoExt/data/PrintProvider.js","GeoExt/plugins/PrintPageField.js","GeoExt/plugins/PrintProviderField.js","GeoExt/plugins/PrintExtent.js","GeoExt/plugins/AttributeForm.js","GeoExt/widgets/PrintMapPanel.js","GeoExt/state/PermalinkProvider.js");
var c=navigator.userAgent;
var e=(c.match("MSIE")||c.match("Safari"));
if(e){var b=new Array(l.length)
}var m=k()+"lib/";
for(var d=0,g=l.length;
d<g;
d++){if(e){b[d]="<script src='"+m+l[d]+"'><\/script>"
}else{var n=document.createElement("script");
n.src=m+l[d];
var f=document.getElementsByTagName("head").length?document.getElementsByTagName("head")[0]:document.body;
f.appendChild(n)
}}if(e){document.write(b.join(""))
}}})();Ext.namespace("GeoExt.data");
GeoExt.data.AttributeReader=function(a,b){a=a||{};
if(!a.format){a.format=new OpenLayers.Format.WFSDescribeFeatureType()
}GeoExt.data.AttributeReader.superclass.constructor.call(this,a,b||a.fields);
if(a.feature){this.recordType.prototype.fields.add(new Ext.data.Field("value"))
}};
Ext.extend(GeoExt.data.AttributeReader,Ext.data.DataReader,{read:function(a){var b=a.responseXML;
if(!b||!b.documentElement){b=a.responseText
}return this.readRecords(b)
},readRecords:function(h){var e;
if(h instanceof Array){e=h
}else{e=this.meta.format.read(h).featureTypes[0].properties
}var r=this.meta.feature;
var c=this.recordType;
var l=c.prototype.fields;
var f=l.length;
var n,q,a,k,o,p,b=[];
for(var g=0,m=e.length;
g<m;
++g){o=false;
n=e[g];
q={};
for(var d=0;
d<f;
++d){a=l.items[d].name;
p=n[a];
if(this.ignoreAttribute(a,p)){o=true;
break
}q[a]=p
}if(r){p=r.attributes[q.name];
if(p!==undefined){if(this.ignoreAttribute("value",p)){o=true
}else{q.value=p
}}}if(!o){b[b.length]=new c(q)
}}return{success:true,records:b,totalRecords:b.length}
},ignoreAttribute:function(a,c){var d=false;
if(this.meta.ignore&&this.meta.ignore[a]){var b=this.meta.ignore[a];
if(typeof b=="string"){d=(b===c)
}else{if(b instanceof Array){d=(b.indexOf(c)>-1)
}else{if(b instanceof RegExp){d=(b.test(c))
}}}}return d
}});Ext.namespace("GeoExt.data");
GeoExt.data.AttributeStoreMixin=function(){return{constructor:function(a){a=a||{};
arguments.callee.superclass.constructor.call(this,Ext.apply(a,{proxy:a.proxy||(!a.data?new Ext.data.HttpProxy({url:a.url,disableCaching:false,method:"GET"}):undefined),reader:new GeoExt.data.AttributeReader(a,a.fields||["name","type","restriction"])}));
if(this.feature){this.bind()
}},bind:function(){this.on({update:this.onUpdate,load:this.onLoad,add:this.onAdd,scope:this});
var a=[];
this.each(function(b){a.push(b)
});
this.updateFeature(a)
},onUpdate:function(c,a,b){this.updateFeature([a])
},onLoad:function(b,a,c){if(!c||c.add!==true){this.updateFeature(a)
}},onAdd:function(b,a,c){this.updateFeature(a)
},updateFeature:function(d){var k=this.feature,g=k.layer;
var e,h,f,c,j,a,b;
for(e=0,h=d.length;
e<h;
e++){f=d[e];
c=f.get("name");
j=f.get("value");
a=k.attributes[c];
if(a!==j){b=true
}}if(b&&g&&g.events&&g.events.triggerEvent("beforefeaturemodified",{feature:k})!==false){for(e=0,h=d.length;
e<h;
e++){f=d[e];
c=f.get("name");
j=f.get("value");
k.attributes[c]=j
}g.events.triggerEvent("featuremodified",{feature:k});
g.drawFeature(k)
}}}
};
GeoExt.data.AttributeStore=Ext.extend(Ext.data.Store,GeoExt.data.AttributeStoreMixin());Ext.namespace("GeoExt","GeoExt.data");
GeoExt.data.FeatureReader=function(a,b){a=a||{};
if(!(b instanceof Function)){b=GeoExt.data.FeatureRecord.create(b||a.fields||{})
}GeoExt.data.FeatureReader.superclass.constructor.call(this,a,b)
};
Ext.extend(GeoExt.data.FeatureReader,Ext.data.DataReader,{totalRecords:null,read:function(a){return this.readRecords(a.features)
},readRecords:function(b){var c=[];
if(b){var f=this.recordType,l=f.prototype.fields;
var k,g,h,d,q,p,n,o;
for(k=0,g=b.length;
k<g;
k++){q=b[k];
p={};
if(q.attributes){for(h=0,d=l.length;
h<d;
h++){n=l.items[h];
if(/[\[\.]/.test(n.mapping)){try{o=new Function("obj","return obj."+n.mapping)(q.attributes)
}catch(m){o=n.defaultValue
}}else{o=q.attributes[n.mapping||n.name]||n.defaultValue
}if(n.convert){o=n.convert(o)
}p[n.name]=o
}}p.feature=q;
p.state=q.state;
p.fid=q.fid;
var a=(q.state===OpenLayers.State.INSERT)?undefined:q.id;
c[c.length]=new f(p,a)
}}return{records:c,totalRecords:this.totalRecords!=null?this.totalRecords:c.length}
}});Ext.namespace("GeoExt.data");
GeoExt.data.FeatureStoreMixin=function(){return{layer:null,reader:null,featureFilter:null,constructor:function(b){b=b||{};
b.reader=b.reader||new GeoExt.data.FeatureReader({},b.fields);
var c=b.layer;
delete b.layer;
if(b.features){b.data=b.features
}delete b.features;
var a={initDir:b.initDir};
delete b.initDir;
arguments.callee.superclass.constructor.call(this,b);
if(c){this.bind(c,a)
}},bind:function(d,b){if(this.layer){return
}this.layer=d;
b=b||{};
var f=b.initDir;
if(b.initDir==undefined){f=GeoExt.data.FeatureStore.LAYER_TO_STORE|GeoExt.data.FeatureStore.STORE_TO_LAYER
}var e=d.features.slice(0);
if(f&GeoExt.data.FeatureStore.STORE_TO_LAYER){var a=this.getRange();
for(var c=a.length-1;
c>=0;
c--){this.layer.addFeatures([a[c].getFeature()])
}}if(f&GeoExt.data.FeatureStore.LAYER_TO_STORE){this.loadData(e,true)
}d.events.on({featuresadded:this.onFeaturesAdded,featuresremoved:this.onFeaturesRemoved,featuremodified:this.onFeatureModified,scope:this});
this.on({load:this.onLoad,clear:this.onClear,add:this.onAdd,remove:this.onRemove,update:this.onUpdate,scope:this})
},unbind:function(){if(this.layer){this.layer.events.un({featuresadded:this.onFeaturesAdded,featuresremoved:this.onFeaturesRemoved,featuremodified:this.onFeatureModified,scope:this});
this.un("load",this.onLoad,this);
this.un("clear",this.onClear,this);
this.un("add",this.onAdd,this);
this.un("remove",this.onRemove,this);
this.un("update",this.onUpdate,this);
this.layer=null
}},getRecordFromFeature:function(c){var a=null;
if(c.state!==OpenLayers.State.INSERT){a=this.getById(c.id)
}else{var b=this.findBy(function(d){return d.getFeature()===c
});
if(b>-1){a=this.getAt(b)
}}return a
},onFeaturesAdded:function(b){if(!this._adding){var f=b.features,e=f;
if(this.featureFilter){e=[];
var d,a,c;
for(var d=0,a=f.length;
d<a;
d++){c=f[d];
if(this.featureFilter.evaluate(c)!==false){e.push(c)
}}}this._adding=true;
this.loadData(e,true);
delete this._adding
}},onFeaturesRemoved:function(b){if(!this._removing){var e=b.features,d,a,c;
for(c=e.length-1;
c>=0;
c--){d=e[c];
a=this.getRecordFromFeature(d);
if(a!==undefined){this._removing=true;
this.remove(a);
delete this._removing
}}}},onFeatureModified:function(h){if(!this._updating){var j=h.feature;
var c=this.getRecordFromFeature(j);
if(c!==undefined){c.beginEdit();
var a=j.attributes;
if(a){var d=this.recordType.prototype.fields;
for(var b=0,e=d.length;
b<e;
b++){var f=d.items[b];
var g=f.mapping||f.name;
if(g in a){c.set(f.name,f.convert(a[g]))
}}}c.set("state",j.state);
c.set("fid",j.fid);
c.setFeature(j);
this._updating=true;
c.endEdit();
delete this._updating
}}},addFeaturesToLayer:function(b){var c,a,d;
d=new Array((a=b.length));
for(c=0;
c<a;
c++){d[c]=b[c].getFeature()
}if(d.length>0){this._adding=true;
this.layer.addFeatures(d);
delete this._adding
}},onLoad:function(b,a,c){if(!c||c.add!==true){this._removing=true;
this.layer.removeFeatures(this.layer.features);
delete this._removing;
this.addFeaturesToLayer(a)
}},onClear:function(a){this._removing=true;
this.layer.removeFeatures(this.layer.features);
delete this._removing
},onAdd:function(b,a,c){if(!this._adding){this.addFeaturesToLayer(a)
}},onRemove:function(b,a,c){if(!this._removing){var d=a.getFeature();
if(this.layer.getFeatureById(d.id)!=null){this._removing=true;
this.layer.removeFeatures([a.getFeature()]);
delete this._removing
}}},onUpdate:function(e,b,d){if(!this._updating){var g=new GeoExt.data.FeatureRecord().fields;
var f=b.getFeature();
if(b.fields){var a=this.layer.events.triggerEvent("beforefeaturemodified",{feature:f});
if(a!==false){var c=f.attributes;
b.fields.each(function(i){var h=i.mapping||i.name;
if(!g.containsKey(h)){c[h]=b.get(i.name)
}});
this._updating=true;
this.layer.events.triggerEvent("featuremodified",{feature:f});
delete this._updating;
if(this.layer.getFeatureById(f.id)!=null){this.layer.drawFeature(f)
}}}}}}
};
GeoExt.data.FeatureStore=Ext.extend(Ext.data.Store,new GeoExt.data.FeatureStoreMixin);
GeoExt.data.FeatureStore.LAYER_TO_STORE=1;
GeoExt.data.FeatureStore.STORE_TO_LAYER=2;Ext.namespace("GeoExt.data");
GeoExt.data.LayerRecord=Ext.data.Record.create([{name:"layer"},{name:"title",type:"string",mapping:"name"}]);
GeoExt.data.LayerRecord.prototype.getLayer=function(){return this.get("layer")
};
GeoExt.data.LayerRecord.prototype.setLayer=function(a){if(a!==this.data.layer){this.dirty=true;
if(!this.modified){this.modified={}
}if(this.modified.layer===undefined){this.modified.layer=this.data.layer
}this.data.layer=a;
if(!this.editing){this.afterEdit()
}}};
GeoExt.data.LayerRecord.prototype.clone=function(b){var a=this.getLayer()&&this.getLayer().clone();
return new this.constructor(Ext.applyIf({layer:a},this.data),b||a.id)
};
GeoExt.data.LayerRecord.create=function(e){var c=Ext.extend(GeoExt.data.LayerRecord,{});
var d=c.prototype;
d.fields=new Ext.util.MixedCollection(false,function(f){return f.name
});
GeoExt.data.LayerRecord.prototype.fields.each(function(g){d.fields.add(g)
});
if(e){for(var b=0,a=e.length;
b<a;
b++){d.fields.add(new Ext.data.Field(e[b]))
}}c.getField=function(f){return d.fields.get(f)
};
return c
};Ext.namespace("GeoExt","GeoExt.data");
GeoExt.data.LayerReader=function(a,b){a=a||{};
if(!(b instanceof Function)){b=GeoExt.data.LayerRecord.create(b||a.fields||{})
}GeoExt.data.LayerReader.superclass.constructor.call(this,a,b)
};
Ext.extend(GeoExt.data.LayerReader,Ext.data.DataReader,{totalRecords:null,readRecords:function(f){var a=[];
if(f){var c=this.recordType,k=c.prototype.fields;
var g,d,e,b,h,n,l,m;
for(g=0,d=f.length;
g<d;
g++){h=f[g];
n={};
for(e=0,b=k.length;
e<b;
e++){l=k.items[e];
m=h[l.mapping||l.name]||l.defaultValue;
m=l.convert(m);
n[l.name]=m
}n.layer=h;
a[a.length]=new c(n,h.id)
}}return{records:a,totalRecords:this.totalRecords!=null?this.totalRecords:a.length}
}});Ext.namespace("GeoExt.data");
GeoExt.data.LayerStoreMixin=function(){return{map:null,reader:null,constructor:function(b){b=b||{};
b.reader=b.reader||new GeoExt.data.LayerReader({},b.fields);
delete b.fields;
var c=b.map instanceof GeoExt.MapPanel?b.map.map:b.map;
delete b.map;
if(b.layers){b.data=b.layers
}delete b.layers;
var a={initDir:b.initDir};
delete b.initDir;
arguments.callee.superclass.constructor.call(this,b);
if(c){this.bind(c,a)
}},bind:function(d,a){if(this.map){return
}this.map=d;
a=a||{};
var b=a.initDir;
if(a.initDir==undefined){b=GeoExt.data.LayerStore.MAP_TO_STORE|GeoExt.data.LayerStore.STORE_TO_MAP
}var c=d.layers.slice(0);
if(b&GeoExt.data.LayerStore.STORE_TO_MAP){this.each(function(e){this.map.addLayer(e.getLayer())
},this)
}if(b&GeoExt.data.LayerStore.MAP_TO_STORE){this.loadData(c,true)
}d.events.on({changelayer:this.onChangeLayer,addlayer:this.onAddLayer,removelayer:this.onRemoveLayer,scope:this});
this.on({load:this.onLoad,clear:this.onClear,add:this.onAdd,remove:this.onRemove,update:this.onUpdate,scope:this});
this.data.on({replace:this.onReplace,scope:this})
},unbind:function(){if(this.map){this.map.events.un({changelayer:this.onChangeLayer,addlayer:this.onAddLayer,removelayer:this.onRemoveLayer,scope:this});
this.un("load",this.onLoad,this);
this.un("clear",this.onClear,this);
this.un("add",this.onAdd,this);
this.un("remove",this.onRemove,this);
this.data.un("replace",this.onReplace,this);
this.map=null
}},onChangeLayer:function(b){var e=b.layer;
var c=this.findBy(function(f,g){return f.getLayer()===e
});
if(c>-1){var a=this.getAt(c);
if(b.property==="order"){if(!this._adding&&!this._removing){var d=this.map.getLayerIndex(e);
if(d!==c){this._removing=true;
this.remove(a);
delete this._removing;
this._adding=true;
this.insert(d,[a]);
delete this._adding
}}}else{if(b.property==="name"){a.set("title",e.name)
}else{this.fireEvent("update",this,a,Ext.data.Record.EDIT)
}}}},onAddLayer:function(a){if(!this._adding){var b=a.layer;
this._adding=true;
this.loadData([b],true);
delete this._adding
}},onRemoveLayer:function(a){if(this.map.unloadDestroy){if(!this._removing){var b=a.layer;
this._removing=true;
this.remove(this.getById(b.id));
delete this._removing
}}else{this.unbind()
}},onLoad:function(c,b,e){if(!Ext.isArray(b)){b=[b]
}if(e&&!e.add){this._removing=true;
for(var f=this.map.layers.length-1;
f>=0;
f--){this.map.removeLayer(this.map.layers[f])
}delete this._removing;
var a=b.length;
if(a>0){var g=new Array(a);
for(var d=0;
d<a;
d++){g[d]=b[d].getLayer()
}this._adding=true;
this.map.addLayers(g);
delete this._adding
}}},onClear:function(a){this._removing=true;
for(var b=this.map.layers.length-1;
b>=0;
b--){this.map.removeLayer(this.map.layers[b])
}delete this._removing
},onAdd:function(b,a,c){if(!this._adding){this._adding=true;
var e;
for(var d=a.length-1;
d>=0;
--d){e=a[d].getLayer();
this.map.addLayer(e);
if(c!==this.map.layers.length-1){this.map.setLayerIndex(e,c)
}}delete this._adding
}},onRemove:function(b,a,c){if(!this._removing){var d=a.getLayer();
if(this.map.getLayer(d.id)!=null){this._removing=true;
this.removeMapLayer(a);
delete this._removing
}}},onUpdate:function(c,a,b){if(b===Ext.data.Record.EDIT){if(a.modified&&a.modified.title){var d=a.getLayer();
var e=a.get("title");
if(e!==d.name){d.setName(e)
}}}},removeMapLayer:function(a){this.map.removeLayer(a.getLayer())
},onReplace:function(c,a,b){this.removeMapLayer(a)
},getByLayer:function(b){var a=this.findBy(function(c){return c.getLayer()===b
});
if(a>-1){return this.getAt(a)
}},destroy:function(){this.unbind();
GeoExt.data.LayerStore.superclass.destroy.call(this)
}}
};
GeoExt.data.LayerStore=Ext.extend(Ext.data.Store,new GeoExt.data.LayerStoreMixin);
GeoExt.data.LayerStore.MAP_TO_STORE=1;
GeoExt.data.LayerStore.STORE_TO_MAP=2;Ext.namespace("GeoExt.data");
GeoExt.data.ScaleStore=Ext.extend(Ext.data.Store,{map:null,constructor:function(a){var b=(a.map instanceof GeoExt.MapPanel?a.map.map:a.map);
delete a.map;
a=Ext.applyIf(a,{reader:new Ext.data.JsonReader({},["level","resolution","scale"])});
GeoExt.data.ScaleStore.superclass.constructor.call(this,a);
if(b){this.bind(b)
}},bind:function(b,a){this.map=(b instanceof GeoExt.MapPanel?b.map:b);
this.map.events.register("changebaselayer",this,this.populateFromMap);
if(this.map.baseLayer){this.populateFromMap()
}else{this.map.events.register("addlayer",this,this.populateOnAdd)
}},unbind:function(){if(this.map){this.map.events.unregister("addlayer",this,this.populateOnAdd);
this.map.events.unregister("changebaselayer",this,this.populateFromMap);
delete this.map
}},populateOnAdd:function(a){if(a.layer.isBaseLayer){this.populateFromMap();
this.map.events.unregister("addlayer",this,this.populateOnAdd)
}},populateFromMap:function(){var c=[];
var a=this.map.baseLayer.resolutions;
var b=this.map.baseLayer.units;
for(var e=a.length-1;
e>=0;
e--){var d=a[e];
c.push({level:e,resolution:d,scale:OpenLayers.Util.getScaleFromResolution(d,b)})
}this.loadData(c)
},destroy:function(){this.unbind();
GeoExt.data.ScaleStore.superclass.destroy.apply(this,arguments)
}});Ext.namespace("GeoExt.data");
GeoExt.data.WMSCapabilitiesReader=function(a,b){a=a||{};
if(!a.format){a.format=new OpenLayers.Format.WMSCapabilities()
}if(typeof b!=="function"){b=GeoExt.data.LayerRecord.create(b||a.fields||[{name:"name",type:"string"},{name:"title",type:"string"},{name:"abstract",type:"string"},{name:"queryable",type:"boolean"},{name:"opaque",type:"boolean"},{name:"noSubsets",type:"boolean"},{name:"cascaded",type:"int"},{name:"fixedWidth",type:"int"},{name:"fixedHeight",type:"int"},{name:"minScale",type:"float"},{name:"maxScale",type:"float"},{name:"prefix",type:"string"},{name:"formats"},{name:"styles"},{name:"srs"},{name:"dimensions"},{name:"bbox"},{name:"llbbox"},{name:"attribution"},{name:"keywords"},{name:"identifiers"},{name:"authorityURLs"},{name:"metadataURLs"}])
}GeoExt.data.WMSCapabilitiesReader.superclass.constructor.call(this,a,b)
};
Ext.extend(GeoExt.data.WMSCapabilitiesReader,Ext.data.DataReader,{attributionCls:"gx-attribution",read:function(a){var b=a.responseXML;
if(!b||!b.documentElement){b=a.responseText
}return this.readRecords(b)
},serviceExceptionFormat:function(a){if(OpenLayers.Util.indexOf(a,"application/vnd.ogc.se_inimage")>-1){return"application/vnd.ogc.se_inimage"
}if(OpenLayers.Util.indexOf(a,"application/vnd.ogc.se_xml")>-1){return"application/vnd.ogc.se_xml"
}return a[0]
},imageFormat:function(b){var a=b.formats;
if(b.opaque&&OpenLayers.Util.indexOf(a,"image/jpeg")>-1){return"image/jpeg"
}if(OpenLayers.Util.indexOf(a,"image/png")>-1){return"image/png"
}if(OpenLayers.Util.indexOf(a,"image/png; mode=24bit")>-1){return"image/png; mode=24bit"
}if(OpenLayers.Util.indexOf(a,"image/gif")>-1){return"image/gif"
}return a[0]
},imageTransparent:function(a){return a.opaque==undefined||!a.opaque
},readRecords:function(u){if(typeof u==="string"||u.nodeType){u=this.meta.format.read(u)
}var e=u.version;
var c=u.capability||{};
var f=c.request&&c.request.getmap&&c.request.getmap.href;
var h=c.layers;
var g=c.exception?c.exception.formats:[];
var p=this.serviceExceptionFormat(g);
var o=[];
if(f&&h){var l=this.recordType.prototype.fields;
var t,b,d,s,a,k;
for(var n=0,r=h.length;
n<r;
n++){t=h[n];
if(t.name){b={};
for(var m=0,q=l.length;
m<q;
m++){a=l.items[m];
k=t[a.mapping||a.name]||a.defaultValue;
k=a.convert(k);
b[a.name]=k
}d={attribution:t.attribution?this.attributionMarkup(t.attribution):undefined,minScale:t.minScale,maxScale:t.maxScale};
if(this.meta.layerOptions){Ext.apply(d,this.meta.layerOptions)
}s={layers:t.name,exceptions:p,format:this.imageFormat(t),transparent:this.imageTransparent(t),version:e};
if(this.meta.layerParams){Ext.apply(s,this.meta.layerParams)
}b.layer=new OpenLayers.Layer.WMS(t.title||t.name,f,s,d);
o.push(new this.recordType(b,b.layer.id))
}}}return{totalRecords:o.length,success:true,records:o}
},attributionMarkup:function(a){var b=[];
if(a.logo){b.push("<img class='"+this.attributionCls+"-image' src='"+a.logo.href+"' />")
}if(a.title){b.push("<span class='"+this.attributionCls+"-title'>"+a.title+"</span>")
}if(a.href){for(var c=0;
c<b.length;
c++){b[c]="<a class='"+this.attributionCls+"-link' href="+a.href+">"+b[c]+"</a>"
}}return b.join(" ")
}});Ext.namespace("GeoExt.data");
GeoExt.data.WMSCapabilitiesStore=function(a){a=a||{};
GeoExt.data.WMSCapabilitiesStore.superclass.constructor.call(this,Ext.apply(a,{proxy:a.proxy||(!a.data?new Ext.data.HttpProxy({url:a.url,disableCaching:false,method:"GET"}):undefined),reader:new GeoExt.data.WMSCapabilitiesReader(a,a.fields)}))
};
Ext.extend(GeoExt.data.WMSCapabilitiesStore,Ext.data.Store);Ext.namespace("GeoExt.data");
GeoExt.data.WMSDescribeLayerReader=function(a,b){a=a||{};
if(!a.format){a.format=new OpenLayers.Format.WMSDescribeLayer()
}if(!(typeof b==="function")){b=Ext.data.Record.create(b||a.fields||[{name:"owsType",type:"string"},{name:"owsURL",type:"string"},{name:"typeName",type:"string"}])
}GeoExt.data.WMSDescribeLayerReader.superclass.constructor.call(this,a,b)
};
Ext.extend(GeoExt.data.WMSDescribeLayerReader,Ext.data.DataReader,{read:function(a){var b=a.responseXML;
if(!b||!b.documentElement){b=a.responseText
}return this.readRecords(b)
},readRecords:function(e){if(typeof e==="string"||e.nodeType){e=this.meta.format.read(e)
}var b=[],d;
for(var c=0,a=e.length;
c<a;
c++){d=e[c];
if(d){b.push(new this.recordType(d))
}}return{totalRecords:b.length,success:true,records:b}
}});Ext.namespace("GeoExt.data");
GeoExt.data.WMSDescribeLayerStore=function(a){a=a||{};
GeoExt.data.WMSDescribeLayerStore.superclass.constructor.call(this,Ext.apply(a,{proxy:a.proxy||(!a.data?new Ext.data.HttpProxy({url:a.url,disableCaching:false,method:"GET"}):undefined),reader:new GeoExt.data.WMSDescribeLayerReader(a,a.fields)}))
};
Ext.extend(GeoExt.data.WMSDescribeLayerStore,Ext.data.Store);Ext.namespace("GeoExt");
GeoExt.Action=Ext.extend(Ext.Action,{control:null,map:null,uScope:null,uHandler:null,uToggleHandler:null,uCheckHandler:null,constructor:function(a){this.uScope=a.scope;
this.uHandler=a.handler;
this.uToggleHandler=a.toggleHandler;
this.uCheckHandler=a.checkHandler;
a.scope=this;
a.handler=this.pHandler;
a.toggleHandler=this.pToggleHandler;
a.checkHandler=this.pCheckHandler;
var b=this.control=a.control;
delete a.control;
if(b){if(a.map){a.map.addControl(b);
delete a.map
}if((a.pressed||a.checked)&&b.map){b.activate()
}b.events.on({activate:this.onCtrlActivate,deactivate:this.onCtrlDeactivate,scope:this})
}arguments.callee.superclass.constructor.call(this,a)
},pHandler:function(a){var b=this.control;
if(b&&b.type==OpenLayers.Control.TYPE_BUTTON){b.trigger()
}if(this.uHandler){this.uHandler.apply(this.uScope,arguments)
}},pToggleHandler:function(a,b){this.changeControlState(b);
if(this.uToggleHandler){this.uToggleHandler.apply(this.uScope,arguments)
}},pCheckHandler:function(a,b){this.changeControlState(b);
if(this.uCheckHandler){this.uCheckHandler.apply(this.uScope,arguments)
}},changeControlState:function(a){if(a){if(!this._activating){this._activating=true;
this.control.activate();
this._activating=false
}}else{if(!this._deactivating){this._deactivating=true;
this.control.deactivate();
this._deactivating=false
}}},onCtrlActivate:function(){var a=this.control;
if(a.type==OpenLayers.Control.TYPE_BUTTON){this.enable()
}else{this.safeCallEach("toggle",[true]);
this.safeCallEach("setChecked",[true])
}},onCtrlDeactivate:function(){var a=this.control;
if(a.type==OpenLayers.Control.TYPE_BUTTON){this.disable()
}else{this.safeCallEach("toggle",[false]);
this.safeCallEach("setChecked",[false])
}},safeCallEach:function(e,b){var d=this.items;
for(var c=0,a=d.length;
c<a;
c++){if(d[c][e]){d[c].rendered?d[c][e].apply(d[c],b):d[c].on({render:d[c][e].createDelegate(d[c],b),single:true})
}}}});Ext.namespace("GeoExt");
GeoExt.MapPanel=Ext.extend(Ext.Panel,{map:null,layers:null,center:null,zoom:null,prettyStateKeys:false,extent:null,stateEvents:["aftermapmove","afterlayervisibilitychange","afterlayeropacitychange"],initComponent:function(){if(!(this.map instanceof OpenLayers.Map)){this.map=new OpenLayers.Map(Ext.applyIf(this.map||{},{allOverlays:true}))
}var a=this.layers;
if(!a||a instanceof Array){this.layers=new GeoExt.data.LayerStore({layers:a,map:this.map.layers.length>0?this.map:null})
}if(typeof this.center=="string"){this.center=OpenLayers.LonLat.fromString(this.center)
}else{if(this.center instanceof Array){this.center=new OpenLayers.LonLat(this.center[0],this.center[1])
}}if(typeof this.extent=="string"){this.extent=OpenLayers.Bounds.fromString(this.extent)
}else{if(this.extent instanceof Array){this.extent=OpenLayers.Bounds.fromArray(this.extent)
}}GeoExt.MapPanel.superclass.initComponent.call(this);
this.addEvents("aftermapmove","afterlayervisibilitychange","afterlayeropacitychange");
this.map.events.on({moveend:this.onMoveend,changelayer:this.onLayerchange,scope:this})
},onMoveend:function(){this.fireEvent("aftermapmove")
},onLayerchange:function(a){if(a.property){if(a.property==="visibility"){this.fireEvent("afterlayervisibilitychange")
}else{if(a.property==="opacity"){this.fireEvent("afterlayeropacitychange")
}}}},applyState:function(g){this.center=new OpenLayers.LonLat(g.x,g.y);
this.zoom=g.zoom;
var f,c,e,b,a,d;
var h=this.map.layers;
for(f=0,c=h.length;
f<c;
f++){e=h[f];
b=this.prettyStateKeys?e.name:e.id;
a=g["visibility_"+b];
if(a!==undefined){a=(/^true$/i).test(a);
if(e.isBaseLayer){if(a){this.map.setBaseLayer(e)
}}else{e.setVisibility(a)
}}d=g["opacity_"+b];
if(d!==undefined){e.setOpacity(d)
}}},getState:function(){var f;
if(!this.map){return
}var a=this.map.getCenter();
f={x:a.lon,y:a.lat,zoom:this.map.getZoom()};
var e,c,d,b,g=this.map.layers;
for(e=0,c=g.length;
e<c;
e++){d=g[e];
b=this.prettyStateKeys?d.name:d.id;
f["visibility_"+b]=d.getVisibility();
f["opacity_"+b]=d.opacity==null?1:d.opacity
}return f
},updateMapSize:function(){if(this.map){this.map.updateSize()
}},renderMap:function(){var a=this.map;
a.render(this.body.dom);
this.layers.bind(a);
if(a.layers.length>0){if(this.center||this.zoom!=null){a.setCenter(this.center,this.zoom)
}else{if(this.extent){a.zoomToExtent(this.extent)
}else{a.zoomToMaxExtent()
}}}},afterRender:function(){GeoExt.MapPanel.superclass.afterRender.apply(this,arguments);
if(!this.ownerCt){this.renderMap()
}else{this.ownerCt.on("move",this.updateMapSize,this);
this.ownerCt.on({afterlayout:{fn:this.renderMap,scope:this,single:true}})
}},onResize:function(){GeoExt.MapPanel.superclass.onResize.apply(this,arguments);
this.updateMapSize()
},onBeforeAdd:function(a){if(typeof a.addToMapPanel==="function"){a.addToMapPanel(this)
}GeoExt.MapPanel.superclass.onBeforeAdd.apply(this,arguments)
},remove:function(b,a){if(typeof b.removeFromMapPanel==="function"){b.removeFromMapPanel(this)
}GeoExt.MapPanel.superclass.remove.apply(this,arguments)
},beforeDestroy:function(){if(this.ownerCt){this.ownerCt.un("move",this.updateMapSize,this)
}if(this.map&&this.map.events){this.map.events.un({moveend:this.onMoveend,changelayer:this.onLayerchange,scope:this})
}if(!this.initialConfig.map||!(this.initialConfig.map instanceof OpenLayers.Map)){if(this.map&&this.map.destroy){this.map.destroy()
}}delete this.map;
GeoExt.MapPanel.superclass.beforeDestroy.apply(this,arguments)
}});
GeoExt.MapPanel.guess=function(){return Ext.ComponentMgr.all.find(function(a){return a instanceof GeoExt.MapPanel
})
};
Ext.reg("gx_mappanel",GeoExt.MapPanel);Ext.namespace("GeoExt");
GeoExt.LegendImage=Ext.extend(Ext.BoxComponent,{url:null,defaultImgSrc:null,imgCls:null,initComponent:function(){GeoExt.LegendImage.superclass.initComponent.call(this);
if(this.defaultImgSrc===null){this.defaultImgSrc=Ext.BLANK_IMAGE_URL
}this.autoEl={tag:"img","class":(this.imgCls?this.imgCls:""),src:this.defaultImgSrc}
},setUrl:function(a){this.url=a;
var b=this.getEl();
if(b){b.un("error",this.onImageLoadError,this);
b.on("error",this.onImageLoadError,this,{single:true});
b.dom.src=a
}},onRender:function(b,a){GeoExt.LegendImage.superclass.onRender.call(this,b,a);
if(this.url){this.setUrl(this.url)
}},onDestroy:function(){var a=this.getEl();
if(a){a.un("error",this.onImageLoadError,this)
}GeoExt.LegendImage.superclass.onDestroy.apply(this,arguments)
},onImageLoadError:function(){this.getEl().dom.src=this.defaultImgSrc
}});
Ext.reg("gx_legendimage",GeoExt.LegendImage);Ext.namespace("GeoExt");
GeoExt.LegendPanel=Ext.extend(Ext.Panel,{dynamic:true,layerStore:null,preferredTypes:null,filter:function(a){return true
},initComponent:function(){GeoExt.LegendPanel.superclass.initComponent.call(this)
},onRender:function(){GeoExt.LegendPanel.superclass.onRender.apply(this,arguments);
if(!this.layerStore){this.layerStore=GeoExt.MapPanel.guess().layers
}this.layerStore.each(function(a){this.addLegend(a)
},this);
if(this.dynamic){this.layerStore.on({add:this.onStoreAdd,remove:this.onStoreRemove,clear:this.onStoreClear,scope:this})
}},recordIndexToPanelIndex:function(h){var j=this.layerStore;
var g=j.getCount();
var c=-1;
var a=this.items?this.items.length:0;
var d,e;
for(var b=g-1;
b>=0;
--b){d=j.getAt(b);
e=d.getLayer();
var f=GeoExt.LayerLegend.getTypes(d);
if(e.displayInLayerSwitcher&&f.length>0&&(j.getAt(b).get("hideInLegend")!==true)){++c;
if(h===b||c>a-1){break
}}}return c
},getIdForLayer:function(a){return this.id+"-"+a.id
},onStoreAdd:function(c,b,d){var f=this.recordIndexToPanelIndex(d+b.length-1);
for(var e=0,a=b.length;
e<a;
e++){this.addLegend(b[e],f)
}this.doLayout()
},onStoreRemove:function(b,a,c){this.removeLegend(a)
},removeLegend:function(a){if(this.items){var b=this.getComponent(this.getIdForLayer(a.getLayer()));
if(b){this.remove(b,true);
this.doLayout()
}}},onStoreClear:function(a){this.removeAllLegends()
},removeAllLegends:function(){this.removeAll(true);
this.doLayout()
},addLegend:function(a,b){if(this.filter(a)===true){var d=a.getLayer();
b=b||0;
var e;
var c=GeoExt.LayerLegend.getTypes(a,this.preferredTypes);
if(d.displayInLayerSwitcher&&!a.get("hideInLegend")&&c.length>0){this.insert(b,{xtype:c[0],id:this.getIdForLayer(d),layerRecord:a,hidden:!((!d.map&&d.visibility)||(d.getVisibility()&&d.calculateInRange()))})
}}},onDestroy:function(){if(this.layerStore){this.layerStore.un("add",this.onStoreAdd,this);
this.layerStore.un("remove",this.onStoreRemove,this);
this.layerStore.un("clear",this.onStoreClear,this)
}GeoExt.LegendPanel.superclass.onDestroy.apply(this,arguments)
}});
Ext.reg("gx_legendpanel",GeoExt.LegendPanel);Ext.namespace("GeoExt");
GeoExt.LayerLegend=Ext.extend(Ext.Container,{layerRecord:null,showTitle:true,legendTitle:null,labelCls:null,layerStore:null,initComponent:function(){GeoExt.LayerLegend.superclass.initComponent.call(this);
this.autoEl={};
this.add({xtype:"label",text:this.getLayerTitle(this.layerRecord),cls:"x-form-item x-form-item-label"+(this.labelCls?" "+this.labelCls:"")});
if(this.layerRecord&&this.layerRecord.store){this.layerStore=this.layerRecord.store;
this.layerStore.on("update",this.onStoreUpdate,this)
}},onStoreUpdate:function(c,a,b){if(a===this.layerRecord&&this.items.getCount()>0){var d=a.getLayer();
this.setVisible(d.getVisibility()&&d.calculateInRange()&&d.displayInLayerSwitcher&&!a.get("hideInLegend"));
this.update()
}},update:function(){var a=this.getLayerTitle(this.layerRecord);
if(this.items.get(0).text!==a){this.items.get(0).setText(a)
}},getLayerTitle:function(a){var b=this.legendTitle||"";
if(this.showTitle&&!b){if(a&&!a.get("hideTitle")){b=a.get("title")||a.get("name")||a.getLayer().name||""
}}return b
},beforeDestroy:function(){this.layerStore&&this.layerStore.un("update",this.onStoreUpdate,this);
GeoExt.LayerLegend.superclass.beforeDestroy.apply(this,arguments)
}});
GeoExt.LayerLegend.getTypes=function(b,a){var d=(a||[]).concat();
var c=[];
for(var e in GeoExt.LayerLegend.types){if(GeoExt.LayerLegend.types[e].supports(b)){d.indexOf(e)==-1&&c.push(e)
}else{d.remove(e)
}}return d.concat(c)
};
GeoExt.LayerLegend.supports=function(a){};
GeoExt.LayerLegend.types={};Ext.namespace("GeoExt");
GeoExt.WMSLegend=Ext.extend(GeoExt.LayerLegend,{defaultStyleIsFirst:true,useScaleParameter:true,baseParams:null,initComponent:function(){GeoExt.WMSLegend.superclass.initComponent.call(this);
var a=this.layerRecord.getLayer();
this._noMap=!a.map;
a.events.register("moveend",this,this.onLayerMoveend);
this.update()
},onLayerMoveend:function(a){if((a.zoomChanged===true&&this.useScaleParameter===true)||this._noMap){delete this._noMap;
this.update()
}},getLegendUrl:function(g,h){var e=this.layerRecord;
var a;
var k=e&&e.get("styles");
var f=e.getLayer();
h=h||[f.params.LAYERS].join(",").split(",");
var j=f.params.STYLES&&[f.params.STYLES].join(",").split(",");
var i=h.indexOf(g);
var b=j&&j[i];
if(k&&k.length>0){if(b){Ext.each(k,function(l){a=(l.name==b&&l.legend)&&l.legend.href;
return !a
})
}else{if(this.defaultStyleIsFirst===true&&!j&&!f.params.SLD&&!f.params.SLD_BODY){a=k[0].legend&&k[0].legend.href
}}}if(!a){a=f.getFullRequestString({REQUEST:"GetLegendGraphic",WIDTH:null,HEIGHT:null,EXCEPTIONS:"application/vnd.ogc.se_xml",LAYER:g,LAYERS:null,STYLE:(b!=="")?b:null,STYLES:null,SRS:null,FORMAT:null})
}if(this.useScaleParameter===true&&a.toLowerCase().indexOf("request=getlegendgraphic")!=-1){var c=f.map.getScale();
a=Ext.urlAppend(a,"SCALE="+c)
}var d=this.baseParams||{};
Ext.applyIf(d,{FORMAT:"image/gif"});
if(a.indexOf("?")>0){a=Ext.urlEncode(d,a)
}return a
},update:function(){var d=this.layerRecord.getLayer();
if(!(d&&d.map)){return
}GeoExt.WMSLegend.superclass.update.apply(this,arguments);
var h,b,c,a;
h=[d.params.LAYERS].join(",").split(",");
var e=[];
var g=this.items.get(0);
this.items.each(function(i){c=h.indexOf(i.itemId);
if(c<0&&i!=g){e.push(i)
}else{if(i!==g){b=h[c];
var j=this.getLegendUrl(b,h);
if(!OpenLayers.Util.isEquivalentUrl(j,i.url)){i.setUrl(j)
}}}},this);
for(c=0,a=e.length;
c<a;
c++){var f=e[c];
this.remove(f);
f.destroy()
}for(c=0,a=h.length;
c<a;
c++){b=h[c];
if(!this.items||!this.getComponent(b)){this.add({xtype:"gx_legendimage",url:this.getLegendUrl(b,h),itemId:b})
}}this.doLayout()
},beforeDestroy:function(){if(this.useScaleParameter===true){var a=this.layerRecord.getLayer();
a&&a.events&&a.events.unregister("moveend",this,this.onLayerMoveend)
}GeoExt.WMSLegend.superclass.beforeDestroy.apply(this,arguments)
}});
GeoExt.WMSLegend.supports=function(a){return a.getLayer() instanceof OpenLayers.Layer.WMS
};
GeoExt.LayerLegend.types.gx_wmslegend=GeoExt.WMSLegend;
Ext.reg("gx_wmslegend",GeoExt.WMSLegend);Ext.namespace("GeoExt.tree");
GeoExt.tree.LayerContainer=Ext.extend(Ext.tree.AsyncTreeNode,{constructor:function(a){a=Ext.applyIf(a||{},{text:"Layers"});
this.loader=a.loader instanceof GeoExt.tree.LayerLoader?a.loader:new GeoExt.tree.LayerLoader(Ext.applyIf(a.loader||{},{store:a.layerStore}));
GeoExt.tree.LayerContainer.superclass.constructor.call(this,a)
},recordIndexToNodeIndex:function(c){var b=this.loader.store;
var e=b.getCount();
var a=this.childNodes.length;
var f=-1;
for(var d=e-1;
d>=0;
--d){if(this.loader.filter(b.getAt(d))===true){++f;
if(c===d||f>a-1){break
}}}return f
},destroy:function(){delete this.layerStore;
GeoExt.tree.LayerContainer.superclass.destroy.apply(this,arguments)
}});
Ext.tree.TreePanel.nodeTypes.gx_layercontainer=GeoExt.tree.LayerContainer;Ext.namespace("GeoExt.tree");
GeoExt.tree.BaseLayerContainer=Ext.extend(GeoExt.tree.LayerContainer,{constructor:function(a){a=Ext.applyIf(a||{},{text:"Base Layer",loader:{}});
a.loader=Ext.applyIf(a.loader,{baseAttrs:Ext.applyIf(a.loader.baseAttrs||{},{iconCls:"gx-tree-baselayer-icon",checkedGroup:"baselayer"}),filter:function(b){var c=b.getLayer();
return c.displayInLayerSwitcher===true&&c.isBaseLayer===true
}});
GeoExt.tree.BaseLayerContainer.superclass.constructor.call(this,a)
}});
Ext.tree.TreePanel.nodeTypes.gx_baselayercontainer=GeoExt.tree.BaseLayerContainer;Ext.namespace("GeoExt.tree");
GeoExt.tree.LayerLoader=function(a){Ext.apply(this,a);
this.addEvents("beforeload","load");
GeoExt.tree.LayerLoader.superclass.constructor.call(this)
};
Ext.extend(GeoExt.tree.LayerLoader,Ext.util.Observable,{store:null,filter:function(a){return a.getLayer().displayInLayerSwitcher==true
},baseAttrs:null,uiProviders:null,load:function(a,b){if(this.fireEvent("beforeload",this,a)){this.removeStoreHandlers();
while(a.firstChild){a.removeChild(a.firstChild)
}if(!this.uiProviders){this.uiProviders=a.getOwnerTree().getLoader().uiProviders
}if(!this.store){this.store=GeoExt.MapPanel.guess().layers
}this.store.each(function(c){this.addLayerNode(a,c)
},this);
this.addStoreHandlers(a);
if(typeof b=="function"){b()
}this.fireEvent("load",this,a)
}},onStoreAdd:function(b,a,c,e){if(!this._reordering){var f=e.recordIndexToNodeIndex(c+a.length-1);
for(var d=0;
d<a.length;
++d){this.addLayerNode(e,a[d],f)
}}},onStoreRemove:function(b,a,c,d){if(!this._reordering){this.removeLayerNode(d,a)
}},addLayerNode:function(d,a,b){b=b||0;
if(this.filter(a)===true){var e=this.createNode({nodeType:"gx_layer",layer:a.getLayer(),layerStore:this.store});
var c=d.item(b);
if(c){d.insertBefore(e,c)
}else{d.appendChild(e)
}e.on("move",this.onChildMove,this)
}},removeLayerNode:function(b,a){if(this.filter(a)===true){var c=b.findChildBy(function(d){return d.layer==a.getLayer()
});
if(c){c.un("move",this.onChildMove,this);
c.remove();
b.reload()
}}},onChildMove:function(j,b,h,i,f){this._reordering=true;
var e=this.store.getByLayer(b.layer);
if(i instanceof GeoExt.tree.LayerContainer&&this.store===i.loader.store){i.loader._reordering=true;
this.store.remove(e);
var a;
if(i.childNodes.length>1){var g=(f===0)?f+1:f-1;
a=this.store.findBy(function(k){return i.childNodes[g].layer===k.getLayer()
});
f===0&&a++
}else{if(h.parentNode===i.parentNode){var c=i;
do{c=c.previousSibling
}while(c&&!(c instanceof GeoExt.tree.LayerContainer&&c.lastChild));
if(c){a=this.store.findBy(function(k){return c.lastChild.layer===k.getLayer()
})
}else{var d=i;
do{d=d.nextSibling
}while(d&&!(d instanceof GeoExt.tree.LayerContainer&&d.firstChild));
if(d){a=this.store.findBy(function(k){return d.firstChild.layer===k.getLayer()
})
}a++
}}}if(a!==undefined){this.store.insert(a,[e]);
window.setTimeout(function(){i.reload();
h.reload()
})
}else{this.store.insert(oldRecordIndex,[e])
}delete i.loader._reordering
}delete this._reordering
},addStoreHandlers:function(b){if(!this._storeHandlers){this._storeHandlers={add:this.onStoreAdd.createDelegate(this,[b],true),remove:this.onStoreRemove.createDelegate(this,[b],true)};
for(var a in this._storeHandlers){this.store.on(a,this._storeHandlers[a],this)
}}},removeStoreHandlers:function(){if(this._storeHandlers){for(var a in this._storeHandlers){this.store.un(a,this._storeHandlers[a],this)
}delete this._storeHandlers
}},createNode:function(attr){if(this.baseAttrs){Ext.apply(attr,this.baseAttrs)
}if(typeof attr.uiProvider=="string"){attr.uiProvider=this.uiProviders[attr.uiProvider]||eval(attr.uiProvider)
}attr.nodeType=attr.nodeType||"gx_layer";
return new Ext.tree.TreePanel.nodeTypes[attr.nodeType](attr)
},destroy:function(){this.removeStoreHandlers()
}});Ext.namespace("GeoExt.tree");
GeoExt.tree.LayerNodeUI=Ext.extend(Ext.tree.TreeNodeUI,{constructor:function(a){GeoExt.tree.LayerNodeUI.superclass.constructor.apply(this,arguments)
},render:function(d){var c=this.node.attributes;
if(c.checked===undefined){c.checked=this.node.layer.getVisibility()
}GeoExt.tree.LayerNodeUI.superclass.render.apply(this,arguments);
var b=this.checkbox;
if(c.checkedGroup){var e=Ext.DomHelper.insertAfter(b,['<input type="radio" name="',c.checkedGroup,'_checkbox" class="',b.className,b.checked?'" checked="checked"':"",'"></input>'].join(""));
e.defaultChecked=b.defaultChecked;
Ext.get(b).remove();
this.checkbox=e
}this.enforceOneVisible()
},onClick:function(a){if(a.getTarget(".x-tree-node-cb",1)){this.toggleCheck(this.isChecked())
}else{GeoExt.tree.LayerNodeUI.superclass.onClick.apply(this,arguments)
}},toggleCheck:function(a){a=(a===undefined?!this.isChecked():a);
GeoExt.tree.LayerNodeUI.superclass.toggleCheck.call(this,a);
this.enforceOneVisible()
},enforceOneVisible:function(){var b=this.node.attributes;
var e=b.checkedGroup;
if(e&&e!=="gx_baselayer"){var d=this.node.layer;
var a=this.node.getOwnerTree().getChecked();
var c=0;
Ext.each(a,function(g){var f=g.layer;
if(!g.hidden&&g.attributes.checkedGroup===e){c++;
if(f!=d&&b.checked){f.setVisibility(false)
}}});
if(c===0&&b.checked==false){d.setVisibility(true)
}}},appendDDGhost:function(c){var b=this.elNode.cloneNode(true);
var a=Ext.DomQuery.select("input[type='radio']",b);
Ext.each(a,function(d){d.name=d.name+"_clone"
});
c.appendChild(b)
}});
GeoExt.tree.LayerNode=Ext.extend(Ext.tree.AsyncTreeNode,{layer:null,layerStore:null,constructor:function(a){a.leaf=a.leaf||!(a.children||a.loader);
if(!a.iconCls&&!a.children){a.iconCls="gx-tree-layer-icon"
}if(a.loader&&!(a.loader instanceof Ext.tree.TreeLoader)){a.loader=new GeoExt.tree.LayerParamLoader(a.loader)
}this.defaultUI=this.defaultUI||GeoExt.tree.LayerNodeUI;
Ext.apply(this,{layer:a.layer,layerStore:a.layerStore});
if(a.text){this.fixedText=true
}GeoExt.tree.LayerNode.superclass.constructor.apply(this,arguments)
},render:function(a){var c=this.layer instanceof OpenLayers.Layer&&this.layer;
if(!c){if(!this.layerStore||this.layerStore=="auto"){this.layerStore=GeoExt.MapPanel.guess().layers
}var b=this.layerStore.findBy(function(e){return e.get("title")==this.layer
},this);
if(b!=-1){c=this.layerStore.getAt(b).getLayer()
}}if(!this.rendered||!c){var d=this.getUI();
if(c){this.layer=c;
if(c.isBaseLayer){this.draggable=false;
Ext.applyIf(this.attributes,{checkedGroup:"gx_baselayer"})
}if(!this.text){this.text=c.name
}d.show();
this.addVisibilityEventHandlers()
}else{d.hide()
}if(this.layerStore instanceof GeoExt.data.LayerStore){this.addStoreEventHandlers(c)
}}GeoExt.tree.LayerNode.superclass.render.apply(this,arguments)
},addVisibilityEventHandlers:function(){this.layer.events.on({visibilitychanged:this.onLayerVisibilityChanged,scope:this});
this.on({checkchange:this.onCheckChange,scope:this})
},onLayerVisibilityChanged:function(){if(!this._visibilityChanging){this.getUI().toggleCheck(this.layer.getVisibility())
}},onCheckChange:function(c,b){if(b!=this.layer.getVisibility()){this._visibilityChanging=true;
var a=this.layer;
if(b&&a.isBaseLayer&&a.map){a.map.setBaseLayer(a)
}else{a.setVisibility(b)
}delete this._visibilityChanging
}},addStoreEventHandlers:function(){this.layerStore.on({add:this.onStoreAdd,remove:this.onStoreRemove,update:this.onStoreUpdate,scope:this})
},onStoreAdd:function(c,b,d){var a;
for(var e=0;
e<b.length;
++e){a=b[e].getLayer();
if(this.layer==a){this.getUI().show();
break
}else{if(this.layer==a.name){this.render();
break
}}}},onStoreRemove:function(b,a,c){if(this.layer==a.getLayer()){this.getUI().hide()
}},onStoreUpdate:function(c,a,b){var d=a.getLayer();
if(!this.fixedText&&(this.layer==d&&this.text!==d.name)){this.setText(d.name)
}},destroy:function(){var b=this.layer;
if(b instanceof OpenLayers.Layer){b.events.un({visibilitychanged:this.onLayerVisibilityChanged,scope:this})
}delete this.layer;
var a=this.layerStore;
if(a){a.un("add",this.onStoreAdd,this);
a.un("remove",this.onStoreRemove,this);
a.un("update",this.onStoreUpdate,this)
}delete this.layerStore;
this.un("checkchange",this.onCheckChange,this);
GeoExt.tree.LayerNode.superclass.destroy.apply(this,arguments)
}});
Ext.tree.TreePanel.nodeTypes.gx_layer=GeoExt.tree.LayerNode;Ext.namespace("GeoExt.tree");
GeoExt.tree.LayerParamLoader=function(a){Ext.apply(this,a);
this.addEvents("beforeload","load");
GeoExt.tree.LayerParamLoader.superclass.constructor.call(this)
};
Ext.extend(GeoExt.tree.LayerParamLoader,Ext.util.Observable,{param:null,delimiter:",",load:function(b,d){if(this.fireEvent("beforeload",this,b)){while(b.firstChild){b.removeChild(b.firstChild)
}var c=(b.layer instanceof OpenLayers.Layer.HTTPRequest)&&b.layer.params[this.param];
if(c){var a=(c instanceof Array)?c.slice():c.split(this.delimiter);
Ext.each(a,function(g,e,f){this.addParamNode(g,f,b)
},this)
}if(typeof d=="function"){d()
}this.fireEvent("load",this,b)
}},addParamNode:function(a,b,d){var e=this.createNode({layer:d.layer,param:this.param,item:a,allItems:b,delimiter:this.delimiter});
var c=d.item(0);
if(c){d.insertBefore(e,c)
}else{d.appendChild(e)
}},createNode:function(attr){if(this.baseAttrs){Ext.apply(attr,this.baseAttrs)
}if(typeof attr.uiProvider=="string"){attr.uiProvider=this.uiProviders[attr.uiProvider]||eval(attr.uiProvider)
}attr.nodeType=attr.nodeType||"gx_layerparam";
return new Ext.tree.TreePanel.nodeTypes[attr.nodeType](attr)
}});Ext.namespace("GeoExt.tree");
GeoExt.tree.LayerParamNode=Ext.extend(Ext.tree.TreeNode,{layer:null,param:null,item:null,delimiter:null,allItems:null,constructor:function(a){var b=a||{};
b.iconCls=b.iconCls||"gx-tree-layerparam-icon";
b.text=b.text||b.item;
this.param=b.param;
this.item=b.item;
this.delimiter=b.delimiter||",";
this.allItems=b.allItems;
GeoExt.tree.LayerParamNode.superclass.constructor.apply(this,arguments);
this.getLayer();
if(this.layer){if(!this.allItems){this.allItems=this.getItemsFromLayer()
}if(this.attributes.checked==null){this.attributes.checked=this.layer.getVisibility()&&this.getItemsFromLayer().indexOf(this.item)>=0
}else{this.onCheckChange(this,this.attributes.checked)
}this.layer.events.on({visibilitychanged:this.onLayerVisibilityChanged,scope:this});
this.on({checkchange:this.onCheckChange,scope:this})
}},getLayer:function(){if(!this.layer){var c=this.attributes.layer;
if(typeof c=="string"){var a=this.attributes.layerStore||GeoExt.MapPanel.guess().layers;
var b=a.findBy(function(d){return d.get("title")==c
});
c=b!=-1?a.getAt(b).getLayer():null
}this.layer=c
}return this.layer
},getItemsFromLayer:function(){var a=this.layer.params[this.param];
return a instanceof Array?a:(a?a.split(this.delimiter):[])
},createParams:function(a){var b={};
b[this.param]=this.layer.params[this.param] instanceof Array?a:a.join(this.delimiter);
return b
},onLayerVisibilityChanged:function(){if(this.getItemsFromLayer().length===0){this.layer.mergeNewParams(this.createParams(this.allItems))
}var a=this.layer.getVisibility();
if(a&&this.getItemsFromLayer().indexOf(this.item)!==-1){this.getUI().toggleCheck(true)
}if(!a){this.layer.mergeNewParams(this.createParams([]));
this.getUI().toggleCheck(false)
}},onCheckChange:function(e,d){var c=this.layer;
var b=[];
var a=this.getItemsFromLayer();
if(d===true&&c.getVisibility()===false&&a.length===this.allItems.length){a=[]
}Ext.each(this.allItems,function(g){if((g!==this.item&&a.indexOf(g)!==-1)||(d===true&&g===this.item)){b.push(g)
}},this);
var f=(b.length>0);
f&&c.mergeNewParams(this.createParams(b));
if(f!==c.getVisibility()){c.setVisibility(f)
}(!f)&&c.mergeNewParams(this.createParams([]))
},destroy:function(){var a=this.layer;
if(a instanceof OpenLayers.Layer){a.events.un({visibilitychanged:this.onLayerVisibilityChanged,scope:this})
}delete this.layer;
this.un("checkchange",this.onCheckChange,this);
GeoExt.tree.LayerParamNode.superclass.destroy.apply(this,arguments)
}});
Ext.tree.TreePanel.nodeTypes.gx_layerparam=GeoExt.tree.LayerParamNode;Ext.namespace("GeoExt.tree");
GeoExt.tree.OverlayLayerContainer=Ext.extend(GeoExt.tree.LayerContainer,{constructor:function(a){a=Ext.applyIf(a||{},{text:"Overlays"});
a.loader=Ext.applyIf(a.loader||{},{filter:function(b){var c=b.getLayer();
return c.displayInLayerSwitcher===true&&c.isBaseLayer===false
}});
GeoExt.tree.OverlayLayerContainer.superclass.constructor.call(this,a)
}});
Ext.tree.TreePanel.nodeTypes.gx_overlaylayercontainer=GeoExt.tree.OverlayLayerContainer;Ext.namespace("GeoExt.tree");
GeoExt.tree.TreeNodeUIEventMixin=function(){return{constructor:function(a){a.addEvents("rendernode","rawclicknode");
this.superclass=arguments.callee.superclass;
this.superclass.constructor.apply(this,arguments)
},render:function(a){if(!this.rendered){this.superclass.render.apply(this,arguments);
this.fireEvent("rendernode",this.node)
}},onClick:function(a){if(this.fireEvent("rawclicknode",this.node,a)!==false){this.superclass.onClick.apply(this,arguments)
}}}
};Ext.namespace("GeoExt.plugins");
GeoExt.plugins.TreeNodeRadioButton=Ext.extend(Ext.util.Observable,{constructor:function(a){Ext.apply(this.initialConfig,Ext.apply({},a));
Ext.apply(this,a);
this.addEvents("radiochange");
GeoExt.plugins.TreeNodeRadioButton.superclass.constructor.apply(this,arguments)
},init:function(a){a.on({rendernode:this.onRenderNode,rawclicknode:this.onRawClickNode,beforedestroy:this.onBeforeDestroy,scope:this})
},onRenderNode:function(c){var b=c.attributes;
if(b.radioGroup&&!b.radio){b.radio=Ext.DomHelper.insertBefore(c.ui.anchor,['<input type="radio" class="gx-tree-radio" name="',b.radioGroup,'_radio"></input>'].join(""))
}},onRawClickNode:function(b,c){var a=c.getTarget(".gx-tree-radio",1);
if(a){a.defaultChecked=a.checked;
this.fireEvent("radiochange",b);
return false
}},onBeforeDestroy:function(a){a.un("rendernode",this.onRenderNode,this);
a.un("rawclicknode",this.onRenderNode,this);
a.un("beforedestroy",this.onBeforeDestroy,this)
}});
Ext.preg("gx_treenoderadiobutton",GeoExt.plugins.TreeNodeRadioButton);Ext.namespace("GeoExt");
GeoExt.SliderTip=Ext.extend(Ext.slider.Tip,{hover:true,minWidth:10,offsets:[0,-10],dragging:false,init:function(a){GeoExt.SliderTip.superclass.init.apply(this,arguments);
if(this.hover){a.on("render",this.registerThumbListeners,this)
}this.slider=a
},registerThumbListeners:function(){var a,d;
for(var b=0,c=this.slider.thumbs.length;
b<c;
++b){a=this.slider.thumbs[b];
d=a.tracker.el;
(function(e,f){f.on({mouseover:function(g){this.onSlide(this.slider,g,e);
this.dragging=false
},mouseout:function(){if(!this.dragging){this.hide.apply(this,arguments)
}},scope:this})
}).apply(this,[a,d])
}},onSlide:function(b,c,a){this.dragging=true;
return GeoExt.SliderTip.superclass.onSlide.apply(this,arguments)
}});Ext.namespace("GeoExt");
GeoExt.LayerOpacitySliderTip=Ext.extend(GeoExt.SliderTip,{template:"<div>{opacity}%</div>",compiledTemplate:null,init:function(a){this.compiledTemplate=new Ext.Template(this.template);
GeoExt.LayerOpacitySliderTip.superclass.init.call(this,a)
},getText:function(a){var b={opacity:a.value};
return this.compiledTemplate.apply(b)
}});Ext.namespace("GeoExt.tree");
GeoExt.tree.LayerOpacitySliderPlugin=Ext.extend(Ext.util.Observable,{constructor:function(a){Ext.apply(this.initialConfig,Ext.apply({},a));
Ext.apply(this,a);
this.addEvents("opacityslide");
GeoExt.tree.LayerOpacitySliderPlugin.superclass.constructor.apply(this,arguments)
},init:function(a){a.on({rendernode:this.onRenderNode,scope:this})
},onRenderNode:function(g){var b=g.attributes;
var d=g.layer;
if(b.slider){this.indentMarkup=g.parentNode?g.parentNode.ui.getChildIndent():"";
var c=g.id+"-tree-slider-";
c=Ext.id(null,c);
buf=["<br/>","<table><tr>","<td>",'<span class="x-tree-node-indent">',this.indentMarkup,"</span>","</td><td>",'<span class="x-tree-node-indent">',this.indentMarkup,"</span>","</td><td>","<a id=",c,"></a>","</td>","</tr></table>"];
Ext.DomHelper.insertAfter(g.ui.anchor,buf.join(""));
var e=100;
var f=new Ext.Slider({minValue:0,maxValue:100,value:e,width:100,aggressive:true,layer:d,plugins:new GeoExt.LayerOpacitySliderTip()});
Ext.override(Ext.Slider,{getRatio:function(){var a=this.innerEl.getComputedWidth();
var h=this.maxValue-this.minValue;
return h==0?a:(a/h)
}});
f.on("change",function(h,i,a){this.layer.setOpacity(i/100)
});
f.render(c)
}}});
Ext.preg&&Ext.preg("gx_layeropacitysliderplugin",GeoExt.tree.LayerOpacitySliderPlugin);OpenLayers.Control.LoadingPanel=OpenLayers.Class(OpenLayers.Control,{counter:0,maximized:false,visible:true,initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,[a])
},setVisible:function(a){this.visible=a;
if(a){OpenLayers.Element.show(this.div)
}else{OpenLayers.Element.hide(this.div)
}},getVisible:function(){return this.visible
},hide:function(){this.setVisible(false)
},show:function(){this.setVisible(true)
},toggle:function(){this.setVisible(!this.getVisible())
},addLayer:function(a){if(a.layer){a.layer.events.register("loadstart",this,this.increaseCounter);
a.layer.events.register("loadend",this,this.decreaseCounter)
}},setMap:function(c){OpenLayers.Control.prototype.setMap.apply(this,arguments);
this.map.events.register("preaddlayer",this,this.addLayer);
for(var b=0;
b<this.map.layers.length;
b++){var a=this.map.layers[b];
a.events.register("loadstart",this,this.increaseCounter);
a.events.register("loadend",this,this.decreaseCounter)
}},increaseCounter:function(){this.counter++;
if(this.counter>0){if(!this.maximized&&this.visible){this.maximizeControl()
}}},decreaseCounter:function(){if(this.counter>0){this.counter--
}if(this.counter==0){if(this.maximized&&this.visible){this.minimizeControl()
}}},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
return this.div
},minimizeControl:function(a){this.div.style.display="none";
this.div.style.width="0px";
this.div.style.height="0px";
this.maximized=false;
if(a!=null){OpenLayers.Event.stop(a)
}},maximizeControl:function(a){var d=this.map.getSize();
var b=d.w;
var c=d.h;
this.div.style.width=b+"px";
this.div.style.height=c+"px";
this.div.style.display="block";
this.maximized=true;
if(a!=null){OpenLayers.Event.stop(a)
}},destroy:function(){if(this.map){this.map.events.unregister("preaddlayer",this,this.addLayer);
if(this.map.layers){for(var b=0;
b<this.map.layers.length;
b++){var a=this.map.layers[b];
a.events.unregister("loadstart",this,this.increaseCounter);
a.events.unregister("loadend",this,this.decreaseCounter)
}}}OpenLayers.Control.prototype.destroy.apply(this,arguments)
},CLASS_NAME:"OpenLayers.Control.LoadingPanel"});OpenLayers.Control.ScaleBar=OpenLayers.Class(OpenLayers.Control,{element:null,scale:1,displaySystem:"metric",minWidth:100,maxWidth:200,divisions:2,subdivisions:2,showMinorMeasures:false,abbreviateLabel:false,singleLine:false,align:"left",div:null,scaleText:"scale 1:",thousandsSeparator:"",measurementProperties:{english:{units:["miles","feet","inches"],abbr:["mi","ft","in"],inches:[63360,12,1]},metric:{units:["kilometers","meters","centimeters"],abbr:["km","m","cm"],inches:[39370.07874,39.370079,0.393701]}},limitedStyle:false,customStyles:null,defaultStyles:{Bar:{height:11,top:12,borderLeftWidth:0,borderRightWidth:0},BarAlt:{height:11,top:12,borderLeftWidth:0,borderRightWidth:0},MarkerMajor:{height:13,width:13,top:12,borderLeftWidth:0,borderRightWidth:0},MarkerMinor:{height:13,width:13,top:12,borderLeftWidth:0,borderRightWidth:0},NumbersBox:{height:13,width:40,top:24},LabelBox:{height:15,top:-2},LabelBoxSingleLine:{height:15,width:35,top:5,left:10}},appliedStyles:null,initialize:function(a){OpenLayers.Control.prototype.initialize.apply(this,[a]);
if(!document.styleSheets){this.limitedStyle=true
}if(this.limitedStyle){this.appliedStyles=OpenLayers.Util.extend({},this.defaultStyles);
OpenLayers.Util.extend(this.appliedStyles,this.customStyles)
}this.element=document.createElement("div");
this.element.style.position="relative";
this.element.className=this.displayClass+"Wrapper";
this.labelContainer=document.createElement("div");
this.labelContainer.className=this.displayClass+"Units";
this.labelContainer.style.position="absolute";
this.graphicsContainer=document.createElement("div");
this.graphicsContainer.style.position="absolute";
this.graphicsContainer.className=this.displayClass+"Graphics";
this.numbersContainer=document.createElement("div");
this.numbersContainer.style.position="absolute";
this.numbersContainer.className=this.displayClass+"Numbers";
this.element.appendChild(this.graphicsContainer);
this.element.appendChild(this.labelContainer);
this.element.appendChild(this.numbersContainer)
},destroy:function(){this.map.events.unregister("moveend",this,this.onMoveend);
this.div.innerHTML="";
OpenLayers.Control.prototype.destroy.apply(this)
},draw:function(){OpenLayers.Control.prototype.draw.apply(this,arguments);
this.dxMarkerMajor=(this.styleValue("MarkerMajor","borderLeftWidth")+this.styleValue("MarkerMajor","width")+this.styleValue("MarkerMajor","borderRightWidth"))/2;
this.dxMarkerMinor=(this.styleValue("MarkerMinor","borderLeftWidth")+this.styleValue("MarkerMinor","width")+this.styleValue("MarkerMinor","borderRightWidth"))/2;
this.dxBar=(this.styleValue("Bar","borderLeftWidth")+this.styleValue("Bar","borderRightWidth"))/2;
this.dxBarAlt=(this.styleValue("BarAlt","borderLeftWidth")+this.styleValue("BarAlt","borderRightWidth"))/2;
this.dxNumbersBox=this.styleValue("NumbersBox","width")/2;
var d=["Bar","BarAlt","MarkerMajor","MarkerMinor"];
if(this.singleLine){d.push("LabelBoxSingleLine")
}else{d.push("NumbersBox","LabelBox")
}var a=0;
for(var c=0;
c<d.length;
++c){var b=d[c];
a=Math.max(a,this.styleValue(b,"top")+this.styleValue(b,"height"))
}this.element.style.height=a+"px";
this.xOffsetSingleLine=this.styleValue("LabelBoxSingleLine","width")+this.styleValue("LabelBoxSingleLine","left");
this.div.appendChild(this.element);
this.map.events.register("moveend",this,this.onMoveend);
this.update();
return this.div
},onMoveend:function(){this.update()
},update:function(e){if(this.map.baseLayer==null||!this.map.getScale()){return
}this.scale=(e!=undefined)?e:this.map.getScale();
this.element.title=this.scaleText+OpenLayers.Number.format(this.scale);
this.element.style.width=this.maxWidth+"px";
var i=this.getComp();
this.setSubProps(i);
this.labelContainer.innerHTML="";
this.graphicsContainer.innerHTML="";
this.numbersContainer.innerHTML="";
var d=this.divisions*this.subdivisions;
var j={left:0+(this.singleLine?0:this.dxNumbersBox),center:(this.maxWidth/2)-(d*this.subProps.pixels/2)-(this.singleLine?this.xOffsetSingleLine/2:0),right:this.maxWidth-(d*this.subProps.pixels)-(this.singleLine?this.xOffsetSingleLine:this.dxNumbersBox)};
var h,a,f,m,c;
for(var k=0;
k<this.divisions;
++k){h=k*this.subdivisions*this.subProps.pixels+j[this.align];
this.graphicsContainer.appendChild(this.createElement("MarkerMajor"," ",h-this.dxMarkerMajor));
if(!this.singleLine){a=(k==0)?0:OpenLayers.Number.format((k*this.subdivisions)*this.subProps.length,this.subProps.dec,this.thousandsSeparator);
this.numbersContainer.appendChild(this.createElement("NumbersBox",a,h-this.dxNumbersBox))
}for(var g=0;
g<this.subdivisions;
++g){if((g%2)==0){m="Bar";
c=h-this.dxBar
}else{m="BarAlt";
c=h-this.dxBarAlt
}this.graphicsContainer.appendChild(this.createElement(m," ",c,this.subProps.pixels));
if(g<this.subdivisions-1){f=(k*this.subdivisions)+g+1;
h=f*this.subProps.pixels+j[this.align];
this.graphicsContainer.appendChild(this.createElement("MarkerMinor"," ",h-this.dxMarkerMinor));
if(this.showMinorMeasures&&!this.singleLine){a=f*this.subProps.length;
this.numbersContainer.appendChild(this.createElement("NumbersBox",a,h-this.dxNumbersBox))
}}}}h=d*this.subProps.pixels;
h+=j[this.align];
this.graphicsContainer.appendChild(this.createElement("MarkerMajor"," ",h-this.dxMarkerMajor));
a=OpenLayers.Number.format(d*this.subProps.length,this.subProps.dec,this.thousandsSeparator);
if(!this.singleLine){this.numbersContainer.appendChild(this.createElement("NumbersBox",a,h-this.dxNumbersBox))
}var l=document.createElement("div");
l.style.position="absolute";
var b;
if(this.singleLine){b=a;
l.className=this.displayClass+"LabelBoxSingleLine";
l.style.left=Math.round(h+this.styleValue("LabelBoxSingleLine","left"))+"px"
}else{b="";
l.className=this.displayClass+"LabelBox";
l.style.textAlign="center";
l.style.width=Math.round(d*this.subProps.pixels)+"px";
l.style.left=Math.round(j[this.align])+"px";
l.style.overflow="hidden"
}if(this.abbreviateLabel){b+=" "+this.subProps.abbr
}else{b+=" "+this.subProps.units
}l.appendChild(document.createTextNode(b));
this.labelContainer.appendChild(l)
},createElement:function(a,e,d,c){var b=document.createElement("div");
b.className=this.displayClass+a;
OpenLayers.Util.extend(b.style,{position:"absolute",textAlign:"center",overflow:"hidden",left:Math.round(d)+"px"});
b.appendChild(document.createTextNode(e));
if(c){b.style.width=Math.round(c)+"px"
}return b
},getComp:function(){var d=this.measurementProperties[this.displaySystem];
var j=d.units.length;
var n=new Array(j);
var m=this.divisions*this.subdivisions;
for(var l=0;
l<j;
++l){n[l]={};
var e=OpenLayers.DOTS_PER_INCH*d.inches[l]/this.scale;
var o=((this.minWidth-this.dxNumbersBox)/e)/m;
var a=((this.maxWidth-this.dxNumbersBox)/e)/m;
for(var p=0;
p<m;
++p){var f=o*(p+1);
var c=a*(p+1);
var h=this.getHandsomeNumber(f,c);
var g={value:(h.value/(p+1)),score:0,tie:0,dec:0,displayed:0};
for(var q=0;
q<m;
++q){var r=h.value*(q+1)/(p+1);
var b=this.getHandsomeNumber(r,r);
var k=((q+1)%this.subdivisions==0);
var i=((q+1)==m);
if((this.singleLine&&i)||(!this.singleLine&&(k||this.showMinorMeasures))){g.score+=b.score;
g.tie+=b.tie;
g.dec=Math.max(g.dec,b.dec);
g.displayed+=1
}else{g.score+=b.score/this.subdivisions;
g.tie+=b.tie/this.subdivisions
}}g.score*=(l+1)*g.tie/g.displayed;
n[l][p]=g
}}return n
},setSubProps:function(b){var e=this.measurementProperties[this.displaySystem];
var h=Number.POSITIVE_INFINITY;
var f=Number.POSITIVE_INFINITY;
for(var d=0;
d<b.length;
++d){var a=OpenLayers.DOTS_PER_INCH*e.inches[d]/this.scale;
for(var g in b[d]){var c=b[d][g];
if((c.score<h)||((c.score==h)&&(c.tie<f))){this.subProps={length:c.value,pixels:a*c.value,units:e.units[d],abbr:e.abbr[d],dec:c.dec};
h=c.score;
f=c.tie
}}}},styleValue:function(a,h){var g=0;
if(this.limitedStyle){g=this.appliedStyles[a][h]
}else{a="."+this.displayClass+a;
rules:for(var d=document.styleSheets.length-1;
d>=0;
--d){var e=document.styleSheets[d];
if(!e.disabled){var j;
try{if(typeof(e.cssRules)=="undefined"){if(typeof(e.rules)=="undefined"){continue
}else{j=e.rules
}}else{j=e.cssRules
}}catch(b){continue
}for(var c=0;
c<j.length;
++c){var f=j[c];
if(f.selectorText&&(f.selectorText.toLowerCase()==a.toLowerCase())){if(f.style[h]!=""){g=parseInt(f.style[h]);
break rules
}}}}}}return g?g:0
},getHandsomeNumber:function(i,g,d){d=(d==null)?10:d;
var j={value:i,score:Number.POSITIVE_INFINITY,tie:Number.POSITIVE_INFINITY,dec:3};
var l,k,f,h,m,c,e;
for(var b=0;
b<3;
++b){l=Math.pow(2,(-1*b));
k=Math.floor(Math.log(g/l)/Math.LN10);
for(var a=k;
a>(k-d+1);
--a){f=Math.max(b-a,0);
h=l*Math.pow(10,a);
if((h*Math.floor(g/h))>=i){if(i%h==0){m=i/h
}else{m=Math.floor(i/h)+1
}c=m+(2*b);
e=(a<0)?(Math.abs(a)+1):a;
if((c<j.score)||((c==j.score)&&(e<j.tie))){j.value=parseFloat((h*m).toFixed(f));
j.score=c;
j.tie=e;
j.dec=f
}}}}return j
},CLASS_NAME:"OpenLayers.Control.ScaleBar"});OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork=OpenLayers.Class(OpenLayers.Format.CSWGetRecords.v2_0_2,{namespaces:{xlink:"http://www.w3.org/1999/xlink",xsi:"http://www.w3.org/2001/XMLSchema-instance",csw:"http://www.opengis.net/cat/csw/2.0.2",dc:"http://purl.org/dc/elements/1.1/",dct:"http://purl.org/dc/terms/",ows:"http://www.opengis.net/ows",geonet:"http://www.fao.org/geonetwork"},initialize:function(a){OpenLayers.Format.CSWGetRecords.v2_0_2.prototype.initialize.apply(this,[a])
},readers:{csw:{GetRecordsResponse:function(b,c){c.records=[];
this.readChildNodes(b,c);
var a=this.getAttributeNS(b,"","version");
if(a!=""){c.version=a
}},RequestId:function(a,b){b.RequestId=this.getChildValue(a)
},SearchStatus:function(a,c){c.SearchStatus={};
var b=this.getAttributeNS(a,"","timestamp");
if(b!=""){c.SearchStatus.timestamp=b
}},SearchResults:function(d,e){this.readChildNodes(d,e);
var b=d.attributes;
var f={};
for(var c=0,a=b.length;
c<a;
++c){if((b[c].name=="numberOfRecordsMatched")||(b[c].name=="numberOfRecordsReturned")||(b[c].name=="nextRecord")){f[b[c].name]=parseInt(b[c].nodeValue)
}else{f[b[c].name]=b[c].nodeValue
}}e.SearchResults=f
},SummaryRecord:function(b,c){var a={type:"SummaryRecord"};
this.readChildNodes(b,a);
c.records.push(a)
},BriefRecord:function(b,c){var a={type:"BriefRecord"};
this.readChildNodes(b,a);
c.records.push(a)
},DCMIRecord:function(b,c){var a={type:"DCMIRecord"};
this.readChildNodes(b,a);
c.records.push(a)
},Record:function(b,c){var a={type:"Record"};
this.readChildNodes(b,a);
c.records.push(a)
},"*":function(a,d){var c=a.localName;
if(!(d.geonet_info[c] instanceof Array)){d.geonet_info[c]=new Array()
}var b=this.getChildValue(a);
d.geonet_info[c].push(b)
}},dc:{"*":function(f,g){var d=f.localName||f.nodeName.split(":").pop();
if(!(g[d] instanceof Array)){g[d]=new Array()
}var c={};
var b=f.attributes;
for(var e=0,a=b.length;
e<a;
++e){c[b[e].name]=b[e].nodeValue
}c.value=this.getChildValue(f);
g[d].push(c)
}},dct:{"*":function(b,c){var a=b.localName||b.nodeName.split(":").pop();
if(!(c[a] instanceof Array)){c[a]=new Array()
}c[a].push(this.getChildValue(b))
}},geonet:{info:function(a,b){if(!(b.geonet_info instanceof Array)){b.geonet_info=new Array()
}this.readChildNodes(a,b)
}},ows:{WGS84BoundingBox:function(g,h){if(!(h.BoundingBox instanceof Array)){h.BoundingBox=new Array()
}var f=this.getChildValue(this.getElementsByTagNameNS(g,this.namespaces.ows,"LowerCorner")[0]).split(" ",2);
var b=this.getChildValue(this.getElementsByTagNameNS(g,this.namespaces.ows,"UpperCorner")[0]).split(" ",2);
var d={value:[parseFloat(f[0]),parseFloat(f[1]),parseFloat(b[0]),parseFloat(b[1])]};
var c=g.attributes;
for(var e=0,a=c.length;
e<a;
++e){d[c[e].name]=c[e].nodeValue
}h.BoundingBox.push(d)
},BoundingBox:function(a,b){this.readers.ows["WGS84BoundingBox"].apply(this,[a,b])
}}},CLASS_NAME:"OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork"});OpenLayers.Format.GeoNetworkRecords=OpenLayers.Class(OpenLayers.Format.XML,{defaultPrefix:"nons",namespaces:{nons:"",geonet:"http://www.fao.org/geonetwork"},initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a])
},read:function(a){if(typeof a=="string"){a=OpenLayers.Format.XML.prototype.read.apply(this,[a])
}if(a&&a.nodeType==9){a=a.documentElement
}var b={};
this.readNode(a,b);
return b
},readers:{nons:{response:function(a,b){b.records=[];
this.readChildNodes(a,b);
var d=this.getAttributeNS(a,"","from");
if(d!=""){b.from=d
}var c=this.getAttributeNS(a,"","to");
if(c!=""){b.to=c
}},summary:function(d,e){e.summary={};
var b=d.attributes;
for(var c=0,a=b.length;
c<a;
++c){e.summary[b[c].name]=b[c].nodeValue
}this.readChildNodes(d,e.summary)
},metadata:function(b,c){var a={type:"metadata"};
this.readChildNodes(b,a);
c.records.push(a)
},geoBox:function(d,g){if(!(g.BoundingBox instanceof Array)){g.BoundingBox=new Array()
}var c,a,f,i;
for(var h=d.firstChild;
h;
h=h.nextSibling){switch(h.nodeName){case"southBL":c=this.getChildValue(h);
break;
case"westBL":a=this.getChildValue(h);
break;
case"eastBL":f=this.getChildValue(h);
break;
case"northBL":i=this.getChildValue(h);
break
}}var b={value:[parseFloat(a),parseFloat(c),parseFloat(f),parseFloat(i)]};
g.BoundingBox.push(b)
},"*":function(e,h){var c=e.localName||e.nodeName.split(":").pop();
var g=e.parentNode.localName||e.parentNode.nodeName.split(":").pop();
var f={};
f.value=this.getChildValue(e);
if(g=="info"){if(!(h.geonet_info[c] instanceof Array)){h.geonet_info[c]=new Array()
}h.geonet_info[c].push(f);
if(c=="harvestInfo"){this.readChildNodes(e,h.geonet_info[c])
}}else{if(!(h[c] instanceof Array)){h[c]=new Array()
}var b=e.attributes;
for(var d=0,a=b.length;
d<a;
++d){f[b[d].name]=b[d].nodeValue
}h[c].push(f);
this.readChildNodes(e,h[c])
}}},geonet:{info:function(a,b){if(!(b.geonet_info instanceof Array)){b.geonet_info=new Array()
}this.readChildNodes(a,b)
}}},CLASS_NAME:"OpenLayers.Format.GeoNetworkRecords"});Ext.namespace("GeoNetwork");
GeoNetwork.singleFile=true;Ext.namespace("GeoNetwork");
GeoNetwork.Catalogue=Ext.extend(Ext.util.Observable,{SERVERURL:null,URL:null,LANG:"en",DEFAULT_LANG:"en",hostUrl:null,servlet:null,extentMap:null,services:{},windowOption:"menubar=no,location=no,toolbar=no,directories=no",windowName:"",startRecord:1,mdStore:null,summaryStore:null,statusBarId:null,mdDisplayPanelId:undefined,mdOverlayedCmpId:undefined,identifiedUser:undefined,adminUser:false,adminAppUrl:"",selectedRecords:0,constructor:function(a){a=a||{};
Ext.apply(this,a);
if(this.hostUrl){this.SERVERURL=this.hostUrl
}else{this.SERVERURL="http://"+window.location.host+"/"
}if(this.servlet){this.URL=this.SERVERURL+this.servlet
}else{this.URL=this.SERVERURL+"geonetwork"
}this.LANG=(this.lang?this.lang:this.DEFAULT_LANG);
var b=this.URL+"/srv/"+this.LANG+"/";
this.services.rootUrl=b;
this.services.csw=b+"csw";
this.services.xmlSearch=b+"xml.search";
this.services.mdSelect=b+"metadata.select";
this.services.mdShow=b+"metadata.show.embedded";
this.services.mdXMLGet=b+"xml.metadata.get";
this.services.mdXMLGet19139=b+"iso19139.xml";
this.services.mdXMLGetDC=b+"dc.xml";
this.services.mdXMLGetFGDC=b+"fgdc.xml";
this.services.mdXMLGet19115=b+"iso19115to19139.xml";
this.services.mdDuplicate=b+"metadata.duplicate.form";
this.services.mdDelete=b+"metadata.delete";
this.services.mdEdit=b+"metadata.edit";
this.services.login=b+"xml.user.login";
this.services.logout=b+"xml.user.logout";
this.services.mef=b+"mef.export?format=full&version=2";
this.services.csv=b+"csv.search";
this.services.pdf=b+"pdf.selection.search";
this.services.harvestingStart=b+"xml.harvesting.start";
this.services.harvestingStop=b+"xml.harvesting.stop";
this.services.harvestingRun=b+"xml.harvesting.run";
this.services.harvestingAdd=b+"xml.harvesting.add";
this.services.harvestingUpdate=b+"xml.harvesting.update";
this.services.harvestingRemove=b+"xml.harvesting.remove";
this.services.opensearchSuggest=b+"main.search.suggest";
this.services.massiveOp=[];
this.services.massiveOp.NewOwner=b+"metadata.massive.newowner.form";
this.services.massiveOp.Categories=b+"metadata.massive.category.form";
this.services.massiveOp.Delete=b+"metadata.massive.delete";
this.services.massiveOp.Privileges=b+"metadata.massive.admin.form";
this.services.metadataAdmin=b+"metadata.admin.form";
this.services.metadataCategory=b+"metadata.category.form";
this.services.getGroups=b+"xml.info?type=groups";
this.services.getRegions=b+"xml.info?type=regions";
this.services.getSources=b+"xml.info?type=sources";
this.services.getUsers=b+"xml.info?type=users";
this.services.getZ3950repositories=b+"xml.info?type=z3950repositories";
this.services.getCategories=b+"xml.info?type=categories";
this.services.getHarvesters=b+"xml.harvesting.get";
this.services.rate=b+"xml.metadata.rate";
this.services.metadataMassiveUpdatePrivilege=b+"metadata.massive.update.privileges";
this.services.metadataMassiveUpdateCategories=b+"metadata.massive.update.categories";
this.services.xmlConfig=b+"xml.config.get";
this.services.admin=b+"admin";
this.services.logoUrl=this.URL+"/images/logos/";
this.extentMap=new GeoNetwork.map.ExtentMap();
this.addEvents("selectionchange","afterLogin","afterLogout","afterBadLogin","afterBadLogout");
GeoNetwork.Catalogue.superclass.constructor.call(this,a)
},isIdentified:function(){return(this.identifiedUser==undefined?false:true)
},isAdmin:function(){return this.adminUser
},onAfterLogin:function(){this.fireEvent("afterLogin",this,this.isIdentified())
},onAfterBadLogin:function(){this.fireEvent("afterBadLogin",this,this.isIdentified())
},onAfterLogout:function(){this.fireEvent("afterLogout",this,this.isIdentified())
},onAfterBadLogout:function(){this.fireEvent("afterBadLogout",this,this.isIdentified())
},setSelectedRecords:function(a){this.selectedRecords=parseInt(a);
this.onSelectionChange()
},getSelectedRecords:function(){return this.selectedRecords
},onSelectionChange:function(){this.fireEvent("selectionchange",this,this.getSelectedRecords())
},updateStatus:function(b){if(this.statusBarId){var a=Ext.getCmp(this.statusBarId);
if(a){a.update(b);
return
}a=Ext.getDom(this.statusBarId);
if(a){a.innerHTML=b;
return
}}},search:function(g,f,c,b,e,a,d){this.updateStatus("Searching ...");
if(e!=false){e=true
}if(b==undefined){b=this.startRecord
}if(f==null){f=Ext.emptyFn
}if(c==null){c=Ext.emptyFn
}if(a==undefined){a=this.metadataStore
}if(d==undefined){d=this.summaryStore
}if(e){a.removeAll()
}GeoNetwork.util.SearchTools.doQueryFromForm(g,this,b,f,c,e,a,d)
},kvpSearch:function(f,g,c,b,e,a,d){if(e!=false){e=true
}if(b==undefined){b=this.startRecord
}if(g==null){g=Ext.emptyFn
}if(c==null){c=Ext.emptyFn
}if(a==undefined){a=this.metadataStore
}if(d==undefined){d=this.summaryStore
}if(e){a.removeAll()
}GeoNetwork.util.SearchTools.doQuery(f,this,b,g,c,e,a,d)
},cswSearch:function(d,c,b,a){this.metadataStore.removeAll();
if(c==null){c=Ext.emptyFn
}if(b==null){b=Ext.emptyFn
}if(a==undefined){a=this.startRecord
}GeoNetwork.util.CSWSearchTools.doCSWQueryFromForm(d,this,a,c,null,b)
},metadataSelect:function(c,b){this.setSelectedRecords(0);
var d=this;
for(var a=0;
a<b.length;
a++){OpenLayers.Request.GET({url:this.services.mdSelect,params:{id:b[a],selected:c},success:function(f){var e=f.responseXML.documentElement.getElementsByTagName("Selected")[0].childNodes[0].nodeValue;
if(e){d.setSelectedRecords(e)
}},failure:function(e){Ext.Msg.alert("Selection failed",e.responseText)
}})
}},metadataRate:function(b,a,d){var c=this;
OpenLayers.Request.GET({url:this.services.rate,params:{uuid:b,rating:a},success:function(f){var e=f.responseXML.documentElement.childNodes[0].nodeValue;
if(d){d(e)
}return e
},failure:function(e){Ext.Msg.alert("Rating failed",e.responseText)
}})
},metadataSelectAll:function(a){this.metadataSelection("add-all",a)
},metadataSelectNone:function(a){this.metadataSelection("remove-all",a)
},metadataSelection:function(a,c){var b=this;
OpenLayers.Request.GET({url:this.services.mdSelect,params:{selected:a},success:function(e){var d=e.responseXML.documentElement.getElementsByTagName("Selected")[0].childNodes[0].nodeValue;
if(d){b.setSelectedRecords(d)
}if(c){c()
}},failure:function(d){}})
},csvExport:function(){window.open(this.services.csv,this.windowName,this.windowOption)
},mefExport:function(){window.open(this.services.mef,this.windowName,this.windowOption)
},pdfExport:function(){window.open(this.services.pdf,this.windowName,this.windowOption)
},metadataShow:function(a,e,l){var c=this.services.mdShow+"?uuid="+a+"&currTab=simple";
var k,g,b,f;
if(this.mdDisplayPanelId!=undefined){var d=Ext.getCmp(this.mdDisplayPanelId);
if(!d.isVisible()){d.show()
}d.load({url:c});
return
}else{if(this.mdOverlayedCmpId!=undefined){var j=Ext.getCmp(this.mdOverlayedCmpId);
k=j.getWidth();
g=500;
b=Ext.getBody();
f="t-t"
}else{k=e?e:600;
g=l?l:700;
b=Ext.getBody();
f="t-t"
}}var i=new Ext.Window({layout:"fit",width:k,height:g,border:false,maximizable:true,title:"Metadata (uuid: "+a+")",items:new Ext.Panel({autoLoad:{url:c,callback:function(){this.extentMap.initMapDiv()
},scope:this},border:false,frame:false,autoScroll:true})});
i.show(this);
i.alignTo(b,f)
},metadataXMLShow:function(c,d){var a=this.services.mdXMLGet19139;
if(d=="dublin-core"){a=this.services.mdXMLGetDC
}else{if(d=="fgdc"){a=this.services.mdXMLGetFGDC
}else{if(d=="iso19115"){a=this.services.mdXMLGet19115
}}}var b=a+"?uuid="+c;
window.open(b,this.windowName,this.windowOption)
},metadataEdit:function(a){window.open(this.services.mdEdit+"?id="+a,this.windowName,this.windowOption)
},metadataDuplicate:function(a){window.open(this.services.mdDuplicate+"?uuid="+a,this.windowName,this.windowOption)
},metadataCreateChild:function(a){window.open(this.services.mdDuplicate+"?child=y&uuid="+a,this.windowName,this.windowOption)
},metadataDuplicateWithSchema:function(a,b){window.open(this.services.mdDuplicate+"?uuid="+a+"&schema="+b,this.windowName,this.windowOption)
},metadataDelete:function(a){Ext.Msg.confirm("Delete ?","Are you sure to delete this metadata record?",this.metadataDeleteDo,a)
},metadataDeleteDo:function(a){if(a=="yes"){var b={uuid:this};
catalogue.doAction(catalogue.services.mdDelete,b,"Delete operation successful","Delete operation failed")
}},doAction:function(a,e,f,c,d,b){if(a.indexOf("http")==-1){a=this.services.rootUrl+a
}OpenLayers.Request.GET({url:a,params:e,success:function(g){if(f){Ext.Msg.alert(f,g.responseText)
}if(d){d(g)
}},failure:function(g){if(c){Ext.Msg.alert(c,g.responseText)
}if(b){b(g)
}}})
},isLoggedIn:function(){var a=OpenLayers.Request.GET({url:this.services.admin,async:false});
if(a.status===200){this.identifiedUser={firstName:"TODO",surName:"TODO",role:"admin"};
return true
}else{return false
}},login:function(c,a){var b=this;
OpenLayers.Request.GET({url:this.services.login,params:{username:c,password:a},success:function(d){b.identifiedUser={firstName:"TODO",surName:"TODO",role:"admin"};
b.onAfterLogin()
},failure:function(d){b.identifiedUser=undefined;
b.onAfterBadLogin()
}})
},logout:function(){var a=this;
OpenLayers.Request.GET({url:this.services.logout,success:function(b){a.identifiedUser=undefined;
a.onAfterLogout()
},failure:function(b){a.identifiedUser=undefined;
a.onAfterBadLogout()
}})
},admin:function(){location.replace(this.adminAppUrl)
},massiveOp:function(b){var a=this.services.massiveOp[b];
this.modalAction("Massive operation "+b,a)
},modalAction:function(c,a){if(a){var b=new Ext.Window({layout:"fit",width:700,height:400,closeAction:"hide",plain:true,modal:true,draggable:false,title:c,items:new Ext.Panel({autoLoad:a,border:false,frame:false})});
b.show(this);
b.alignTo(Ext.getBody(),"t-t")
}},metadataAdmin:function(b){var a=this.services.metadataAdmin+"?id="+b;
this.modalAction("Set privileges",a)
},metadataCategory:function(b){var a=this.services.metadataCategory+"?id="+b;
this.modalAction("Set categories",a)
}});
Ext.reg("gn_catalogue",GeoNetwork.Catalogue);Ext.namespace("GeoNetwork.util");
GeoNetwork.util.SearchTools={fast:"false",output:"full",sortBy:"relevance",hitsPerPage:"50",doQuery:function(g,a,c,h,d,f,b,e){OpenLayers.Request.GET({url:a.services.xmlSearch+"?"+g,success:function(i){if(f){var m=new OpenLayers.Format.GeoNetworkRecords();
var j=m.read(i.responseText);
var k=j.records;
if(k.length>0){b.loadData(j)
}var l=j.summary;
if(l.count>0&&e){e.loadData(l)
}if(a){a.updateStatus(j.from+"-"+j.to+" result(s) / "+l.count)
}}if(h){h(i,g)
}},failure:function(i){if(d){d(i)
}}})
},doQueryFromForm:function(i,h,g,d,a,f,c,b){var e=GeoNetwork.util.SearchTools.buildQueryFromForm(Ext.getCmp(i),g,GeoNetwork.util.SearchTools.sortBy);
GeoNetwork.util.SearchTools.doQuery(e,h,g,d,a,f,c,b)
},buildQueryFromForm:function(d,b,e){var a=GeoNetwork.util.SearchTools.getFormValues(d);
var c=[];
GeoNetwork.util.SearchTools.addFiltersFromPropertyMap(a,c,b);
return GeoNetwork.util.SearchTools.buildQueryGET(c,b,e)
},populateFormFromParams:function(a,b){a.cascade(function(d){var e=d.getId();
if(d.getName){var c=d.getName();
if(c.indexOf("_")!=-1){c=c.substring(2);
if(b[c]){d.setValue(b[c])
}}else{if(b[c]){d.setValue(b[c])
}}}})
},addFiltersFromPropertyMap:function(i,a,e){var d=".8";
var b=i.E_similarity;
if(b!=null){d=i.E_similarity;
GeoNetwork.util.SearchTools.addFilter(a,"E_similarity",d)
}var c=i.E_hitsperpage;
if(c==undefined||c==""){c=GeoNetwork.util.SearchTools.hitsPerPage
}var g=parseInt(e)+parseInt(c)-1;
GeoNetwork.util.SearchTools.addFilter(a,"E_from",e);
GeoNetwork.util.SearchTools.addFilter(a,"E_to",g);
for(var h in i){var f=i[h];
if(f!=""&&h!="E_similarity"){GeoNetwork.util.SearchTools.addFilter(a,h,f)
}}},addFilter:function(d,c,e){var g=c.match("^(\\[?)([^_]+)_(.*)$");
if(g){if(g[1]=="["){var f=[];
var a=e.split(",");
for(var b=0;
b<a.length;
++b){GeoNetwork.util.SearchTools.addFilterImpl(a.length>1?f:d,g[2],g[3],a[b])
}}else{GeoNetwork.util.SearchTools.addFilterImpl(d,g[2],g[3],e)
}}},addFilterImpl:function(c,b,a,d){if(b.charAt(0)=="E"){c.push(a+"="+escape(d)+"")
}else{if(b=="B"){c.push(a+"="+(d?"on":"off")+"")
}else{alert("Cannot parse "+b)
}}},sortByMappings:{relevance:{name:"relevance",order:""},rating:{name:"changeDate",order:""},popularity:{name:"popularity",order:""},date:{name:"date",order:""},title:{name:"title",order:"reverse"}},buildQueryGET:function(b,a,d){var c="fast="+GeoNetwork.util.SearchTools.fast+"&";
if(d){}if(b){c+=b.join("&")
}return c
},getFormValues:function(b){var a=b.getForm().getValues()||{};
b.cascade(function(d){if(d.disabled!=true&&d.rendered){if(d.isXType("boxselect")){if(d.getValue&&d.getValue()){a[d.getName()]=d.getValue()
}}else{if(d.isXType("combo")){if(d.getValue&&d.getValue()){a[d.getName()]=d.getValue()
}}else{if(d.isXType("fieldset")){if(d.checkbox){a[d.checkboxName]=!d.collapsed
}}else{if(d.isXType("radiogroup")){var c=d.items.get(0);
a[c.getName()]=c.getGroupValue()
}else{if(d.isXType("checkbox")){a[d.getName()]=d.getValue()
}else{if(d.isXType("datefield")){if(d.getValue()!=""){a[d.getName()]=d.getValue().format("Y-m-d")+(d.postfix?d.postfix:"")
}}else{if(d.getName){if(d.getValue&&d.getValue()!=""){a[d.getName()]=d.getValue()
}}}}}}}}}return true
});
return a
}};Ext.namespace("GeoNetwork.util");
GeoNetwork.util.SearchFormTools={getSimpleFormFields:function(c,e,b,d){var a=[];
if(c){a.push(GeoNetwork.util.SearchFormTools.getFullTextFieldWithOpenSearchSuggestion(c.opensearchSuggest,null))
}else{a.push(GeoNetwork.util.SearchFormTools.getFullTextField())
}if(d){a.push(GeoNetwork.util.SearchFormTools.getTypesField())
}if(e){a.push(GeoNetwork.util.SearchFormTools.getSimpleMap(e,b))
}a.push(GeoNetwork.util.SearchFormTools.getOptions());
return a
},getAdvancedFormFields:function(b,j,z){var t=[];
var u,h;
if(b){u=GeoNetwork.util.SearchFormTools.getFullTextFieldWithOpenSearchSuggestion(b.opensearchSuggest,null);
h=GeoNetwork.util.SearchFormTools.getFieldWithOpenSearchSuggestion("keyword","E_themekey","Keyword",b.opensearchSuggest,null)
}else{u=GeoNetwork.util.SearchFormTools.getFullTextField();
h=GeoNetwork.util.SearchFormTools.getKeywordsField()
}var m=GeoNetwork.util.SearchFormTools.getAdvancedTextFields();
var c=GeoNetwork.util.SearchFormTools.getTitleField();
var p=GeoNetwork.util.SearchFormTools.getAbstractField();
var n=GeoNetwork.util.SearchFormTools.getTypesField();
var s={xtype:"fieldset",title:"Map types",autoHeight:true,autoWidth:true,collapsible:true,collapsed:true,defaultType:"checkbox",items:GeoNetwork.util.SearchFormTools.getMapTypesField()};
var g={xtype:"fieldset",title:"Search accuracy",autoHeight:true,autoWidth:true,layout:"column",collapsible:true,collapsed:true,items:[GeoNetwork.util.SearchFormTools.getSimilarityField()]};
var a=new Ext.form.TextField({name:"E_geometry",id:"geometry",fieldLabel:"WKT geometry",hideLabel:false});
var k=[];
for(var v=0;
v<j.length;
v++){k.push(new OpenLayers.Layer.WMS(j[v][0],j[v][1],j[v][2],j[v][3]))
}var l=new GeoNetwork.form.GeometryMapField({geometryFieldId:"geometry",id:"geometryMap",layers:k,extent:z.maxExtent,zoom:1});
var A=GeoNetwork.util.SearchFormTools.getNearYouButton("geometry");
var w=GeoNetwork.util.SearchFormTools.getRelationField();
var x=new Ext.form.FieldSet({title:"Spatial search",autoWidth:true,collapsible:true,collapsed:true,items:[a,A,l,w]});
var f=new Ext.form.FieldSet({title:"Metadata change date",autoWidth:true,layout:"column",defaultType:"datefield",collapsible:true,collapsed:true,items:GeoNetwork.util.SearchFormTools.getMetadataDateField()});
var o=new Ext.form.FieldSet({title:"Temporal extent",autoWidth:true,layout:"column",defaultType:"datefield",collapsible:true,collapsed:true,items:GeoNetwork.util.SearchFormTools.getTemporalExtentField()});
var r=GeoNetwork.util.SearchFormTools.getCatalogueField(b.getSources,b.logoUrl);
var y=GeoNetwork.util.SearchFormTools.getGroupField(b.getGroups);
var e=GeoNetwork.util.SearchFormTools.getMetadataTypeField();
var q=GeoNetwork.util.SearchFormTools.getCategoryField(b.getCategories);
var d=GeoNetwork.util.SearchFormTools.getOptions();
t.push(u,m,c,p,h,x,n,s,g,f,o,r,y,e,q,d);
return t
},getSimpleMap:function(g,e){var a=[];
var c=new Ext.form.TextField({name:"E_geometry",id:"geometry",inputType:"hidden"});
var d=[];
for(var f=0;
f<g.length;
f++){d.push(new OpenLayers.Layer.WMS(g[f][0],g[f][1],g[f][2],g[f][3]))
}var b=new GeoNetwork.form.GeometryMapField({geometryFieldId:"geometry",id:"geometryMap",layers:d,extent:e.maxExtent});
a.push(c,b);
return a
},getOptions:function(f){var b=f||[["10"],["20"],["50"],["100"]];
var a=new Ext.form.TextField({name:"E_sortOrder",id:"sortOrder",inputType:"hidden"});
var d=GeoNetwork.util.SearchFormTools.getSortByCombo();
var e=new Ext.form.ComboBox({id:"E_hitsperpage",name:"E_hitsperpage",mode:"local",triggerAction:"all",fieldLabel:"Hits per page",value:b[1],store:new Ext.data.ArrayStore({id:0,fields:["id"],data:b}),valueField:"id",displayField:"id"});
var c=new Ext.form.FieldSet({title:"Options",autoWidth:true,collapsible:true,collapsed:true,defaults:{width:120},items:[a,d,e]});
return c
},getSortByCombo:function(){return new Ext.form.ComboBox({id:"E_sortBy",name:"E_sortBy",mode:"local",fieldLabel:"Sort by",triggerAction:"all",value:"relevance",store:GeoNetwork.util.SearchFormTools.getSortByStore(),valueField:"id",displayField:"name",listeners:{change:function(a,c,b){if(c=="title"){Ext.getCmp("sortOrder").setValue("reverse")
}else{Ext.getCmp("sortOrder").setValue("")
}}}})
},getSortByStore:function(){return new Ext.data.ArrayStore({id:0,fields:["id","name"],data:[["relevance","Relevance"],["changeDate","Change date"],["rating","Rating"],["popularity","Popularity"],["title","Title"]]})
},getFullTextField:function(){return new Ext.form.TextField({name:"E_any",id:"E_any",fieldLabel:"Full text search",hideLabel:false})
},getFullTextFieldWithOpenSearchSuggestion:function(a,b){return new GeoNetwork.form.OpenSearchSuggestionTextField({url:a})
},getFieldWithOpenSearchSuggestion:function(e,d,c,a,b){return new GeoNetwork.form.OpenSearchSuggestionTextField({field:e,name:d,fieldLabel:c,url:a,hideTrigger:true})
},getTitleField:function(){return GeoNetwork.util.SearchFormTools.getTextField("E_title","Title")
},getAbstractField:function(){return GeoNetwork.util.SearchFormTools.getTextField("E_abstract","Abstract")
},getKeywordsField:function(){return GeoNetwork.util.SearchFormTools.getTextField("E_themekey","Keyword")
},getTextField:function(b,a){return new Ext.form.TextField({name:b,fieldLabel:a,hideLabel:false})
},getCatalogueField:function(b,c){var a=GeoNetwork.data.CatalogueSourceStore(b);
a.load();
return new Ext.form.ComboBox({name:"E_siteId",mode:"local",triggerAction:"all",fieldLabel:"Catalogue",store:a,valueField:"id",displayField:"name",tpl:'<tpl for="."><div class="x-combo-list-item logo"><img src="'+c+'{id}.gif"/>{name}</div></tpl>'})
},getGroupField:function(a){var b=GeoNetwork.data.GroupStore(a);
b.load();
return new Ext.form.ComboBox({name:"E_group",mode:"local",triggerAction:"all",fieldLabel:"Group",store:b,valueField:"id",displayField:"name"})
},getMetadataTypeField:function(){return new Ext.form.ComboBox({name:"E_template",mode:"local",triggerAction:"all",fieldLabel:"Kind",store:new Ext.data.ArrayStore({id:0,fields:["id","name"],data:[["n","Metadata"],["y","Template"]]}),valueField:"id",displayField:"name"})
},getCategoryField:function(b,c){var a=GeoNetwork.data.CategoryStore(b);
a.load();
if(c==undefined){c="en"
}return new Ext.form.ComboBox({name:"E_category",mode:"local",fieldLabel:"Category",triggerAction:"all",store:a,valueField:"name",displayField:"name",tpl:'<tpl for="."><div class="x-combo-list-item">{[values.label.'+c+"]}</div></tpl>"})
},getAdvancedTextFields:function(){return{xtype:"fieldset",title:"Advanced text search options",autoHeight:true,autoWidth:true,collapsible:true,collapsed:true,defaultType:"textfield",items:[{name:"E_or",fieldLabel:"Either of the words",hideLabel:false},{name:"E_phrase",fieldLabel:"Exact phrase",hideLabel:false},{name:"E_all",fieldLabel:"All of the words",hideLabel:false},{name:"E_without",fieldLabel:"Without the words",hideLabel:false}]}
},getSimilarityField:function(){return{xtype:"radiogroup",items:[{xtype:"label",text:"Precise"},{name:"E_similarity",inputValue:1},{name:"E_similarity",inputValue:0.8,checked:true},{name:"E_similarity",inputValue:0.6},{name:"E_similarity",inputValue:0.4},{name:"E_similarity",inputValue:0.2},{xtype:"label",text:"Imprecise"}]}
},getRelationField:function(){return new Ext.form.ComboBox({name:"E_relation",mode:"local",width:150,triggerAction:"all",fieldLabel:"Relation type",store:new Ext.data.ArrayStore({id:0,fields:["relation"],data:[[""],["intersection"],["overlaps"],["encloses"],["fullyOutsideOf"],["crosses"],["touches"],["within"]]}),valueField:"relation",displayField:"relation"})
},getNearYouButton:function(a){return new Ext.Button({text:"Near you",iconCls:"md-mn mn-user-location",iconAlign:"right",listeners:{click:function(){if(navigator.geolocation){navigator.geolocation.getCurrentPosition(function(b){Ext.getCmp(a).setValue("POINT("+b.coords.latitude+" "+b.coords.longitude+")")
})
}}}})
},registerDateVtype:function(){if(Ext.form.VTypes.daterange){return
}Ext.apply(Ext.form.VTypes,{daterange:function(d,c){var b=c.parseDate(d);
if(!b){return false
}if(c.startDateField&&(!this.dateRangeMax||(b.getTime()!=this.dateRangeMax.getTime()))){var e=Ext.getCmp(c.startDateField);
e.setMaxValue(b);
e.validate();
this.dateRangeMax=b
}else{if(c.endDateField&&(!this.dateRangeMin||(b.getTime()!=this.dateRangeMin.getTime()))){var a=Ext.getCmp(c.endDateField);
a.setMinValue(b);
a.validate();
this.dateRangeMin=b
}}return true
}})
},getMetadataDateField:function(){GeoNetwork.util.SearchFormTools.registerDateVtype();
return[{xtype:"label",text:"From"},{fieldLabel:"From",name:"E_dateFrom",id:"dateFrom",vtype:"daterange",endDateField:"dateTo",format:"d/m/Y"},{xtype:"label",text:"To"},{fieldLabel:"To",name:"E_dateTo",id:"dateTo",vtype:"daterange",startDateField:"dateFrom",format:"d/m/Y"}]
},getTemporalExtentField:function(){GeoNetwork.util.SearchFormTools.registerDateVtype();
return[{xtype:"label",text:"From"},{fieldLabel:"From",name:"E_extFrom",id:"extFrom",vtype:"daterange",endDateField:"extTo",format:"d/m/Y"},{xtype:"label",text:"To"},{fieldLabel:"To",name:"E_extTo",id:"extTo",vtype:"daterange",startDateField:"extFrom",format:"d/m/Y"}]
},getMapTypesField:function(){return[{hideLabel:true,boxLabel:"Digital",name:"B_digital"},{hideLabel:true,boxLabel:"Hard copy",name:"B_paper"},{hideLabel:true,boxLabel:"Download",name:"B_download"},{hideLabel:true,boxLabel:"Interactive",name:"B_dynamic"}]
},getTypesField:function(){return new Ext.form.ComboBox({name:"E_type",mode:"local",autoSelect:false,triggerAction:"all",fieldLabel:"Resource type",store:new Ext.data.ArrayStore({id:0,fields:["id","name"],data:[["dataset","Datasets"],["service","Service"]]}),valueField:"id",displayField:"name"})
}};Ext.namespace("GeoNetwork.util");
GeoNetwork.util.CSWSearchTools={cswMethod:"POST",resultsMode:"results_with_summary",sortBy:"",maxRecords:"50",doCSWQueryFromForm:function(h,g,d,e,b,i){var a=g.services.csw;
var f=GeoNetwork.util.CSWSearchTools.buildCSWQueryFromForm(GeoNetwork.util.CSWSearchTools.cswMethod,Ext.getCmp(h),d,GeoNetwork.util.CSWSearchTools.sortBy,i);
if(GeoNetwork.util.CSWSearchTools.cswMethod=="POST"){var c=GeoNetwork.util.CSWSearchTools.buildCSWQueryFromForm("GET",Ext.getCmp(h),d,GeoNetwork.util.CSWSearchTools.sortBy,i);
OpenLayers.Request.POST({url:a,data:f,success:function(j){var l=new OpenLayers.Format.CSWGetRecords.v2_0_2();
g.currentRecords=l.read(j.responseText);
var k=g.currentRecords.records;
if(k!=undefined){g.metadataStore.loadData(g.currentRecords)
}if(e){e(j,c)
}},failure:b})
}else{OpenLayers.Request.GET({url:a,params:f,success:function(j){e(j,f)
},failure:b})
}},buildCSWQueryFromForm:function(h,e,b,g,f){var a=GeoNetwork.util.CSWSearchTools.getFormValues(e);
var d=[];
GeoNetwork.util.CSWSearchTools.addFiltersFromPropertyMap(a,d);
f(a,d);
if(d.length==0){d.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LIKE,property:"anyText",value:".*"}))
}var c=new OpenLayers.Filter.Logical({type:OpenLayers.Filter.Logical.AND,filters:d});
if(h=="POST"){return GeoNetwork.util.CSWSearchTools.buildCSWQueryPOST(c,b,g)
}else{return GeoNetwork.util.CSWSearchTools.buildCSWQueryGET(c,b,g)
}},addFiltersFromPropertyMap:function(b,e){var a=".8";
var c=b.E_similarity;
if(c!=null){a=b.E_similarity;
GeoNetwork.util.CSWSearchTools.addFilter(e,"E_similarity",a,a)
}for(var d in b){var f=b[d];
if(f!=""&&d!="E_similarity"){GeoNetwork.util.CSWSearchTools.addFilter(e,d,f,a)
}}},addFilter:function(e,d,f,a){var h=d.match("^(\\[?)([^_]+)_(.*)$");
if(h){if(h[1]=="["){var g=[];
var b=f.split(",");
for(var c=0;
c<b.length;
++c){GeoNetwork.util.CSWSearchTools.addFilterImpl(b.length>1?g:e,h[2],h[3],b[c],a)
}if(b.length>1){e.push(new OpenLayers.Filter.Logical({type:OpenLayers.Filter.Logical.OR,filters:g}))
}}else{GeoNetwork.util.CSWSearchTools.addFilterImpl(e,h[2],h[3],f,a)
}}},addFilterImpl:function(e,d,b,f,a){if(d=="S"){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LIKE,property:b,value:f+".*"}))
}else{if(d=="C"){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LIKE,property:b,value:".*"+f+".*"}))
}else{if(d.charAt(0)=="E"){if(d.length>1){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:"similarity",value:d.substring(1)}))
}e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:b,value:f}));
if(d.length>1){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:"similarity",value:a}))
}}else{if(d==">="){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,property:b,value:f}))
}else{if(d=="<="){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO,property:b,value:f}))
}else{if(d=="T"){var h=f.split(" ");
for(var c=0;
c<h.length;
++c){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:b,value:h[c]}))
}}else{if(d=="B"){e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:b,value:f?1:0}))
}else{if(d=="V"){var g=f.match("^([^/]+)/(.*)$");
e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:"similarity",value:"1.0"}));
e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:g[1],value:g[2]}));
e.push(new OpenLayers.Filter.Comparison({type:OpenLayers.Filter.Comparison.EQUAL_TO,property:"similarity",value:a}))
}else{alert("Cannot parse "+d)
}}}}}}}}},sortByMappings:{relevance:{name:"relevance",order:"D"},rating:{name:"rating",order:"D"},popularity:{name:"popularity",order:"D"},date:{name:"date",order:"D"},title:{name:"title",order:"A"}},buildCSWQueryPOST:function(d,b,f){var a='<?xml version="1.0" encoding="UTF-8"?>\n<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" resultType="'+this.resultsMode+'" startPosition="'+b+'" maxRecords="'+this.maxRecords+'">\n  <csw:Query typeNames="csw:Record">\n    <csw:ElementSetName>full</csw:ElementSetName>\n';
if(f){var e=GeoNetwork.util.CSWSearchTools.sortByMappings[f];
a+='    <ogc:SortBy xmlns:ogc="http://www.opengis.net/ogc">\n      <ogc:SortProperty>\n        <ogc:PropertyName>'+e.name+"</ogc:PropertyName>\n        <ogc:SortOrder>"+e.order+"</ogc:SortOrder>\n      </ogc:SortProperty>\n    </ogc:SortBy>\n"
}if(d){var c=new OpenLayers.Format.XML().write(new OpenLayers.Format.Filter().write(d));
c=c.replace(/^<\?xml[^?]*\?>/,"");
a+='    <csw:Constraint version="1.0.0">\n';
a+=c;
a+="    </csw:Constraint>\n"
}a+="  </csw:Query>\n</csw:GetRecords>";
return a
},buildCSWQueryGET:function(d,b,f){var a={request:"GetRecords",service:"CSW",version:"2.0.2",resultType:this.resultsMode,namespace:"csw:http://www.opengis.net/cat/csw/2.0.2",typeNames:"csw:Record",constraintLanguage:"FILTER",constraint_language_version:"1.1.0",elementSetName:"full",startPosition:b,maxRecords:this.maxRecords};
if(f){var e=GeoNetwork.util.CSWSearchTools.sortByMappings[f];
a.sortBy=e.name+":"+e.order
}if(d){var c=new OpenLayers.Format.XML().write(new OpenLayers.Format.Filter().write(d));
c=c.replace(/^<\?xml[^?]*\?>/,"");
a.constraint=c
}return a
},getFormValues:function(b){var a=b.getForm().getValues()||{};
b.cascade(function(d){if(d.disabled!=true){if(d.isXType("boxselect")){if(d.getValue&&d.getValue()){a[d.getName()]=d.getValue()
}}else{if(d.isXType("combo")){if(d.getValue&&d.getValue()){a[d.getName()]=d.getValue()
}}else{if(d.isXType("fieldset")){if(d.checkbox){a[d.checkboxName]=!d.collapsed
}}else{if(d.isXType("radiogroup")){var c=d.items.get(0);
a[c.getName()]=c.getGroupValue()
}else{if(d.isXType("checkbox")){a[d.getName()]=d.getValue()
}else{if(d.isXType("datefield")){if(d.getValue()!=""){a[d.getName()]=d.getValue().format("Y-m-d")+(d.postfix?d.postfix:"")
}}else{if(d.getName){if(d.getValue&&d.getValue()!=""){a[d.getName()]=d.getValue()
}}}}}}}}}return true
});
return a
}};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.UserStore=function(a){return new Ext.data.XmlStore({autoDestroy:true,proxy:new Ext.data.HttpProxy({method:"GET",url:a,disableCaching:false}),record:"user",idPath:"id",fields:[{name:"id"},{name:"username"},{name:"surname"},{name:"name"},{name:"profile"},{name:"address"},{name:"email"},{name:"city"},{name:"state"},{name:"zip"},{name:"country"},{name:"organisation"},{name:"kind"}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.GroupStore=function(a){var b=function(e,d){var k={};
var f=d.getElementsByTagName("label");
if(f.length==1){var h=f[0].childNodes;
var j;
for(var g=0,c=h.length;
g<c;
++g){j=h[g];
if(j.nodeType==1){k[j.nodeName]=j.firstChild.nodeValue
}}}return k
};
return new Ext.data.XmlStore({autoDestroy:true,proxy:new Ext.data.HttpProxy({method:"GET",url:a,disableCaching:false}),record:"group",idPath:"id",fields:[{name:"id",mapping:"@id"},{name:"name"},{name:"description"},{name:"email"},{name:"referrer"},{name:"label",convert:b}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.CategoryStore=function(a){var b=function(e,d){var k={};
var f=d.getElementsByTagName("label");
if(f.length==1){var h=f[0].childNodes;
var j;
for(var g=0,c=h.length;
g<c;
++g){j=h[g];
if(j.nodeType==1){k[j.nodeName]=j.firstChild.nodeValue
}}}return k
};
return new Ext.data.XmlStore({autoDestroy:true,proxy:new Ext.data.HttpProxy({method:"GET",url:a,disableCaching:false}),record:"category",idPath:"id",fields:[{name:"id",mapping:"@id"},{name:"name"},{name:"label",convert:b}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.CatalogueSourceStore=function(a){return new Ext.data.XmlStore({autoDestroy:true,proxy:new Ext.data.HttpProxy({method:"GET",url:a,disableCaching:false}),record:"source",idPath:"id",fields:[{name:"id",mapping:"uuid"},{name:"name"}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.HarvesterStore=function(a){var b=function(h,f){var g=[];
var l=f.getElementsByTagName("categories");
if(l.length==1){var j=l[0].getElementsByTagName("category");
for(var k=0;
k<j.length;
k++){var e=j[k];
g.push(e.getAttribute("id"))
}}return g
};
var d=function(g,e){var f=[];
var j=e.getElementsByTagName("operation");
for(var h=0;
h<j.length;
h++){var k=j[h];
f.push(k.getAttribute("name"))
}return f
};
var c=function(g,e){var f=Ext.data.Record.create([{name:"group_id",mapping:"@id"},{name:"operation",convert:d}]);
var i=new Ext.data.XmlReader({record:"group"},f);
var h=i.readRecords(e);
return h
};
return new Ext.data.XmlStore({autoDestroy:true,url:a,record:"node",idPath:"@id",fields:[{name:"id",mapping:"@id"},{name:"type",mapping:"@type"},{name:"site_name",mapping:"site/name"},{name:"site_uuid",mapping:"site/uuid"},{name:"site_host",mapping:"site/host"},{name:"site_servlet",mapping:"site/servlet"},{name:"site_port",mapping:"site/port"},{name:"site_url",mapping:"site/url"},{name:"site_icon",mapping:"site/icon"},{name:"site_ogctype",mapping:"site/ogctype"},{name:"options_lang",mapping:"options/lang"},{name:"options_topic",mapping:"options/topic"},{name:"options_createthumbnails",mapping:"options/createthumbnails"},{name:"options_uselayer",mapping:"options/uselayer"},{name:"options_uselayermd",mapping:"options/uselayermd"},{name:"options_datasetcategory",mapping:"options/datasetcategory"},{name:"privileges",convert:c},{name:"categories",convert:b},{name:"site_account_use",mapping:"site/account/use"},{name:"site_account_username",mapping:"site/account/username"},{name:"site_account_password",mapping:"site/account/password"},{name:"options_every",mapping:"options/every"},{name:"options_onerunonly",mapping:"options/onerunonly"},{name:"options_status",mapping:"options/status"},{name:"info_lastrun",mapping:"info/lastRun"},{name:"info_running",mapping:"info/running"},{name:"info_result_total",mapping:"info/result/total"},{name:"info_result_added",mapping:"info/result/added"},{name:"info_result_layer",mapping:"info/result/layer"},{name:"info_result_layerUuidExist",mapping:"info/result/layerUuidExist"},{name:"info_result_layerUsingMdUrl",mapping:"info/result/layerUsingMdUrl"},{name:"info_result_unknownSchema",mapping:"info/result/unknownSchema"},{name:"info_result_removed",mapping:"info/result/removed"},{name:"info_result_unretrievable",mapping:"info/result/unretrievable"},{name:"info_result_badFormat",mapping:"info/result/badFormat"},{name:"info_result_doesNotValidate",mapping:"info/result/doesNotValidate"},{name:"info_result_thumbnails",mapping:"info/result/thumbnails"},{name:"info_result_thumbnailsFailed",mapping:"info/result/thumbnailsFailed"},{name:"error_message",mapping:"error/message"},{name:"error_class",mapping:"error/class"},{name:"error_stack"}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.MetadataResultsStore=function(){var j=function(t,s){if(s.title&&s.title){return s.title[0].value
}else{return""
}};
var f=function(t,s){if(s.geonet_info.uuid&&s.geonet_info.uuid){return s.geonet_info.uuid[0].value
}else{return""
}};
var e=function(t,s){if(s.image){for(var u=0;
u<s.image.length;
u++){var w=s.image[u].value;
if(w.indexOf("http")==-1){return w
}else{return w
}}}return""
};
var l=function(t,s){if(s.link){return s.link
}return[]
};
var m=function(t,s){if(s.geonet_info&&s.geonet_info.source){return s.geonet_info.source[0].value
}else{return""
}};
var q=function(t,s){if(s.geonet_info&&s.geonet_info.popularity){return s.geonet_info.popularity[0].value
}else{return""
}};
var i=function(t,s){if(s.geonet_info&&s.geonet_info.rating){return s.geonet_info.rating[0].value
}else{return""
}};
var a=function(t,s){if(s.geonet_info&&s.geonet_info.download){return s.geonet_info.download[0].value
}else{return""
}};
var h=function(t,s){if(s.geonet_info&&s.geonet_info.ownername){return s.geonet_info.ownername[0].value
}else{return""
}};
var r=function(t,s){if(s.geonet_info&&s.geonet_info.isHarvested){return s.geonet_info.isHarvested[0].value
}else{return""
}};
var g=function(t,s){if(s.geonet_info&&s.geonet_info.displayOrder){return s.geonet_info.displayOrder[0].value
}else{return""
}};
var p=function(t,s){if(s.geonet_info&&s.geonet_info.harvestInfo&&s.geonet_info.harvestInfo.type){return s.geonet_info.harvestInfo.type[0].value
}else{return""
}};
var d=function(t,s){if(s.geonet_info&&s.geonet_info.category){return s.geonet_info.category
}else{return""
}};
var c=function(t,s){if(s.geonet_info&&s.geonet_info.changeDate){return s.geonet_info.changeDate[0].value
}else{return""
}};
var k=function(t,s){if(s.geonet_info&&s.geonet_info.createDate){return s.geonet_info.createDate[0].value
}else{return""
}};
var n=function(t,s){if(s.geonet_info&&s.geonet_info.selected){return s.geonet_info.selected[0].value
}else{return""
}};
var o=function(t,s){if(s["abstract"]){return s["abstract"][0].value
}else{return""
}};
var b=function(t,s){if(s.geonet_info&&s.geonet_info.edit){return s.geonet_info.edit[0].value
}else{return"false"
}};
return new Ext.data.JsonStore({totalProperty:"summary.count",root:"records",fields:[{name:"title",convert:j},{name:"abstract",convert:o},{name:"subject",mapping:"keyword",defaultValue:""},{name:"uuid",mapping:"geonet_info.uuid[0].value",defaultValue:""},{name:"id",mapping:"geonet_info.id[0].value",defaultValue:""},{name:"schema",mapping:"geonet_info.schema[0].value",defaultValue:""},{name:"thumbnail",convert:e},{name:"links",convert:l},{name:"uri",mapping:"uri",defaultValue:""},{name:"isharvested",convert:r},{name:"harvestertype",convert:p},{name:"displayorder",convert:g},{name:"createdate",convert:k},{name:"changedate",convert:c},{name:"selected",convert:n},{name:"source",convert:m},{name:"category",convert:d},{name:"rating",convert:i},{name:"popularity",convert:q},{name:"download",convert:a},{name:"ownername",convert:h},{name:"edit",convert:b},{name:"bbox",mapping:"BoundingBox",defaultValue:""}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.MetadataCSWResultsStore=function(){var g=function(k,j){if(j.URI){for(var l=0;
l<j.URI.length;
l++){var m=j.URI[l];
if(m.name=="thumbnail"){if(m.value.indexOf("http")==-1){return GeoNetwork.Catalogue.URL+"/srv/en/"+m.value
}else{return m.value
}}}}return""
};
var a=function(l,j){var k=[];
return k
};
var d=function(k,j){if(j.geonet_info){return j.geonet_info.source[0]
}else{return""
}};
var i=function(k,j){if(j.geonet_info){return j.geonet_info.popularity[0]
}else{return""
}};
var h=function(k,j){if(j.geonet_info){return j.geonet_info.rating[0]
}else{return""
}};
var b=function(k,j){if(j.geonet_info){return j.geonet_info.download[0]
}else{return""
}};
var c=function(k,j){if(j.geonet_info){return j.geonet_info.ownername[0]
}else{return""
}};
var e=function(k,j){if(j.geonet_info&&j.geonet_info.is_harvested){return j.geonet_info.is_harvested[0]
}else{return""
}};
var f=function(k,j){if(j.geonet_info&&j.geonet_info.edit){return j.geonet_info.edit
}else{return"false"
}};
return new Ext.data.JsonStore({totalProperty:"SearchResults.numberOfRecordsMatched",root:"records",fields:[{name:"title",mapping:"title[0].value",defaultValue:""},{name:"abstract",mapping:"abstract",defaultValue:""},{name:"subject",mapping:"subject"},{name:"uuid",mapping:"identifier[0].value",defaultValue:""},{name:"thumbnail",convert:g},{name:"uri",mapping:"uri",defaultValue:""},{name:"isharvested",convert:e},{name:"source",convert:d},{name:"rating",convert:h},{name:"popularity",convert:i},{name:"download",convert:b},{name:"ownername",convert:c},{name:"edit",convert:f},{name:"bbox",mapping:"BoundingBox",defaultValue:""}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.MetadataSummaryStore=function(){return new Ext.data.JsonStore({totalProperty:"count",root:"keywords.keyword",listeners:{load:function(b,a,d){var c="count";
var g="class";
var f="type";
var h={};
var e=6;
b.each(function(i){i.set(f,b.root);
var j=i.get(f);
if(!(h[j] instanceof Object)){h[j]={maxValue:0,minValue:1}
}h[j].maxValue=Math.max(i.get(c),h[j].maxValue);
h[j].minValue=Math.min(i.get(c),h[j].minValue)
});
b.each(function(i){var j=i.get(f);
var k=0;
if(h[j].maxValue!=h[j].minValue){k=(i.get(c)-h[j].minValue)/(h[j].maxValue-h[j].minValue)*e
}i.set(g,Math.round(k))
})
}},sortInfo:{field:"value",direction:"ASC"},fields:[{name:"type",defaultValue:null},{name:"value",mapping:"name",defaultValue:""},{name:"count",mapping:"count",defaultValue:""},{name:"class",defaultValue:"0"}]})
};Ext.namespace("GeoNetwork.data");
GeoNetwork.data.OpenSearchSuggestionReader=function(a,b){a=a||{};
GeoNetwork.data.OpenSearchSuggestionReader.superclass.constructor.call(this,a,b||a.fields)
};
Ext.extend(GeoNetwork.data.OpenSearchSuggestionReader,Ext.data.JsonReader,{rootId:undefined,readRecords:function(r){this.arrayData=r;
var l=this.meta,d=l?Ext.num(l.idIndex,l.id):null,b=this.recordType,q=b.prototype.fields,z=[],e=true,g;
this.rootId=1;
var u=(!this.rootId?this.getRoot(r):r[this.rootId]);
for(var y=0,A=u.length;
y<A;
y++){var t=u[y],a={},p=((d||d===0)&&t[d]!==undefined&&t[d]!==""?t[d]:null);
for(var x=0,m=q.length;
x<m;
x++){var B=q.items[x],w=B.mapping!==undefined&&B.mapping!==null?B.mapping:x;
g=t[w]!==undefined?t[w]:B.defaultValue;
g=B.convert(g,t);
a[B.name]=t
}var c=new b(a,p);
c.json=t;
z[z.length]=c
}var h=z.length;
if(l.totalProperty){g=parseInt(this.getTotal(r),10);
if(!isNaN(g)){h=g
}}if(l.successProperty){g=this.getSuccess(r);
if(g===false||g==="false"){e=false
}}return{success:true,records:z,totalRecords:z.length}
}});Ext.namespace("GeoNetwork.data");
Ext.namespace("GeoNetwork.data");
GeoNetwork.data.OpenSearchSuggestionStore=function(a){a=a||{};
GeoNetwork.data.OpenSearchSuggestionStore.superclass.constructor.call(this,Ext.apply(a,{proxy:a.proxy||(!a.data?new Ext.data.HttpProxy({url:a.url,disableCaching:false,method:"GET"}):undefined),reader:new GeoNetwork.data.OpenSearchSuggestionReader(a,a.fields||["value"])}))
};
Ext.extend(GeoNetwork.data.OpenSearchSuggestionStore,Ext.data.Store);Ext.namespace("GeoNetwork.form");
GeoNetwork.form.SearchField=Ext.extend(Ext.form.TwinTriggerField,{initComponent:function(){if(!this.store.baseParams){this.store.baseParams={}
}GeoNetwork.form.SearchField.superclass.initComponent.call(this);
this.on("specialkey",function(a,b){if(b.getKey()==b.ENTER){this.onTrigger2Click()
}},this)
},validationEvent:false,validateOnBlur:false,trigger1Class:"x-form-clear-trigger",trigger2Class:"x-form-search-trigger",hideTrigger1:true,width:180,hasSearch:false,paramName:"query",onTrigger1Click:function(){if(this.hasSearch){this.store.baseParams[this.paramName]="";
this.store.removeAll();
this.el.dom.value="";
this.triggers[0].hide();
this.hasSearch=false;
this.focus()
}},onTrigger2Click:function(){var a=this.getRawValue();
if(a.length<1){this.store.baseParams[this.paramName]="*"
}else{this.store.baseParams[this.paramName]=a
}if(this.triggerAction){this.triggerAction(this.scope,a)
}else{this.store.reload()
}this.hasSearch=true;
this.triggers[0].show();
this.focus()
}});
Ext.reg("gn_searchfield",GeoNetwork.form.SearchField);Ext.namespace("GeoNetwork.form");
GeoNetwork.form.GeometryMapField=Ext.extend(GeoExt.MapPanel,{projection:"EPSG:4326",geometryFieldId:undefined,geometryField:undefined,nearYou:undefined,createToolbar:function(){var b=[];
var a=this;
this.nearYou=new Ext.Button({iconCls:"md-mn mn-user-location",iconAlign:"right",enableToggle:true,listeners:{toggle:function(d,e){var f=this;
if(e){if(navigator.geolocation){navigator.geolocation.getCurrentPosition(function(g){f.map.panTo(new OpenLayers.LonLat(g.coords.longitude,g.coords.latitude));
f.geometryField.setValue("POINT("+g.coords.latitude+" "+g.coords.longitude+")")
})
}}else{f.geometryField.setValue("")
}},scope:a}});
var c=new Ext.form.Checkbox({boxLabel:"Restrict search to map extent",checked:this.activated?true:false,listeners:{check:function(d,e){this.geometryField.setValue("");
this.nearYou.toggle(false);
if(e){this.on("aftermapmove",this.setField,this);
this.fireEvent("aftermapmove")
}else{this.un("aftermapmove",this.setField,this)
}},scope:a}});
b.push(c,"->",this.nearYou);
return new Ext.Toolbar({items:b})
},defaultConfig:{id:"geometryMap",width:270,height:270,border:false,activated:false},initComponent:function(a){Ext.apply(this,a);
Ext.applyIf(this,this.defaultConfig);
this.tbar=this.createToolbar();
this.geometryField=Ext.getCmp(this.geometryFieldId);
GeoNetwork.form.GeometryMapField.superclass.initComponent.call(this);
if(this.activated){this.on("aftermapmove",this.setField,this)
}},setField:function(){if(this.geometryField!=undefined){var b=this.map.getExtent().toArray();
var a="POLYGON(("+b[0]+" "+b[1]+","+b[0]+" "+b[3]+","+b[2]+" "+b[3]+","+b[2]+" "+b[1]+","+b[0]+" "+b[1]+"))";
this.geometryField.setValue(a)
}}});
Ext.reg("gn_geometrymapfield",GeoNetwork.form.GeometryMapField);Ext.namespace("GeoNetwork.form");
GeoNetwork.form.OpenSearchSuggestionTextField=Ext.extend(Ext.form.ComboBox,{url:undefined,field:"any",fieldLabel:"Full text search",name:"E_any",displayField:"value",tpl:undefined,minChars:2,mode:"remote",loadingText:"...",store:undefined,queryParam:"q",itemSelector:"div.search-item",initComponent:function(){this.id="E_"+this.field;
GeoNetwork.form.OpenSearchSuggestionTextField.superclass.initComponent.call(this);
if(this.tpl==undefined){var a='<tpl for="."><div class="search-item"><h3>{[values.value.replace(Ext.getDom(\''+this.id+"').value, '<span>' + Ext.getDom('"+this.id+"').value + '</span>')]}</h3></div></tpl>";
this.tpl=new Ext.XTemplate(a)
}this.store=new GeoNetwork.data.OpenSearchSuggestionStore({url:this.url,rootId:1,baseParams:{field:this.field}})
}});
Ext.reg("gn_opensearchsuggestiontextfield",GeoNetwork.form.OpenSearchSuggestionTextField);Ext.namespace("GeoNetwork");
GeoNetwork.LoginForm=Ext.extend(Ext.FormPanel,{url:"",id:"loginForm",width:250,border:false,layout:"hbox",catalogue:undefined,defaults:{},defaultType:"textfield",username:new Ext.form.TextField({name:"username",id:"username",width:70,hideLabel:true,allowBlank:false,emptyText:"username"}),password:new Ext.form.TextField({name:"password",id:"password",width:70,hideLabel:true,allowBlank:false,emptyText:"password",inputType:"password"}),userInfo:new Ext.form.Label({id:"userInfoLabel",maxWidth:40,text:""}),loginBt:new Ext.Button({width:50,text:"Login",iconCls:"md-mn mn-login",listeners:{click:function(){this.catalogue.login(Ext.getCmp("username").getValue(),Ext.getCmp("password").getValue())
},scope:this}}),adminBt:new Ext.Button({width:70,text:"Administration",listeners:{click:function(){catalogue.admin()
},scope:this}}),logoutBt:new Ext.Button({width:50,text:"Logout",iconCls:"md-mn mn-logout",listeners:{click:function(){catalogue.logout()
},scope:this}}),catalogue:undefined,initComponent:function(){this.items=[this.username,this.password,this.loginBt,this.adminBt,this.logoutBt];
GeoNetwork.LoginForm.superclass.initComponent.call(this);
var a=this.catalogue.isLoggedIn();
this.login(this.catalogue,a);
this.catalogue.on("afterLogin",this.login,this);
this.catalogue.on("afterLogout",this.login,this)
},login:function(a,c){var b=c?true:false;
this.username.setVisible(!b);
this.password.setVisible(!b);
this.userInfo.setText("");
this.userInfo.setVisible(b);
this.loginBt.setVisible(!b);
this.logoutBt.setVisible(b);
if(this.catalogue.adminAppUrl!=""){this.adminBt.setVisible(b)
}this.doLayout()
}});
Ext.reg("gn_loginform",GeoNetwork.LoginForm);Ext.namespace("GeoNetwork");
GeoNetwork.Templates=Ext.extend(Ext.XTemplate,{compiled:false,disableFormats:false,catalogue:null,sortOrder:0,abstractMaxSize:50,xmlTplMarkup:['<?xml version="1.0" encoding="UTF-8"?>','<node id="{id}" type="{type}">',"<site>","<name>{site_name}</name>",'<tpl if="values.site_ogctype">',"<ogctype>{site_ogctype}</ogctype>","</tpl>",'<tpl if="values.site_url">',"<url>{site_url}</url>","</tpl>",'<tpl if="values.site_icon">',"<icon>{site_icon}</icon>","</tpl>",'<tpl if="values.site_account_use">',"<account>","<use>{site_account_use}</use>","<username>{site_account_username}</username>","<password>{site_account_password}</password>","</account>","</tpl>","</site>","<options>",'<tpl if="values.options_every">',"<every>{options_every}</every>","</tpl>",'<tpl if="values.options_onerunonly">',"<oneRunOnly>{options_onerunonly}</oneRunOnly>","</tpl>",'<tpl if="values.options_lang">',"<lang>{options_lang}</lang>","</tpl>",'<tpl if="values.options_topic">',"<topic>{options_topic}</topic>","</tpl>",'<tpl if="values.options_createthumbnails">',"<createThumbnails>{options_createthumbnails}</createThumbnails>","</tpl>",'<tpl if="values.options_uselayer">',"<useLayer>{options_uselayer}</useLayer>","</tpl>",'<tpl if="values.options_uselayermd">',"<useLayerMd>{options_uselayermd}</useLayerMd>","</tpl>",'<tpl if="values.options_datasetcategory">',"<datasetcategory>{options_datasetcategory}</datasetcategory>","</tpl>","</options>","<content>","</content>","<privileges>","</privileges>",'<group id="1">','<operation name="view" />','<operation name="dynamic" />',"</group>","<categories>","</categories>",'<tpl if="values.info_result_total">',"</tpl>","</node>"],initComponent:function(){GeoNetwork.Templates.superclass.initComponent.call(this)
},getHarvesterTemplate:function(){return new Ext.XTemplate(this.xmlTplMarkup)
}});
GeoNetwork.Templates.TITLE='<h1><input type="checkbox" <tpl if="selected==\'true\'">checked="true"</tpl> class="selector" onclick="javascript:catalogue.metadataSelect((this.checked?\'add\':\'remove\'), [\'{uuid}\']);"/><a href="#" onclick="javascript:catalogue.metadataShow(\'{uuid}\');">{title}</a></h1>';
GeoNetwork.Templates.RATING_TPL='<tpl if="isharvested==\'n\' || harvestertype==\'geonetwork\'"><div class="rating"><input type="radio" name="rating{[xindex]}" <tpl if="rating==\'1\'">checked="true"</tpl> value="1"/><input type="radio" name="rating{[xindex]}" <tpl if="rating==\'2\'">checked="true"</tpl> value="2"/><input type="radio" name="rating{[xindex]}" <tpl if="rating==\'3\'">checked="true"</tpl> value="3"/><input type="radio" name="rating{[xindex]}" <tpl if="rating==\'4\'">checked="true"</tpl> value="4"/><input type="radio" name="rating{[xindex]}" <tpl if="rating==\'5\'">checked="true"</tpl> value="5"/></div></tpl>';
GeoNetwork.Templates.SIMPLE=new Ext.XTemplate("<ul>",'<tpl for=".">','<li class="md md-simple">','<div class="md-wrap" id="{uuid}" alt="{abstract}" title="{abstract}">','<span class="md-logo"><img src="{[catalogue.URL]}/images/logos/{source}.gif"/></span>',GeoNetwork.Templates.TITLE,"</div>",GeoNetwork.Templates.RATING_TPL,'<span class="subject"><tpl for="subject">','{value}{[xindex==xcount?"":", "]}',"</tpl></span>","</li>","</tpl>","</ul>");
GeoNetwork.Templates.THUMBNAIL=new Ext.XTemplate("<ul>",'<tpl for=".">','<li class="md md-thumbnail">','<div class="md-wrap" id="{uuid}" alt="{abstract}">','<span class="md-logo"><img src="{[catalogue.URL]}/images/logos/{source}.gif"/></span>',GeoNetwork.Templates.TITLE,'<tpl if="thumbnail">','<img class="thumbnail" src="{thumbnail}" alt="thumbnail"/>',"</tpl>",'<tpl for="links">',"<tpl if=\"values.type == 'application/vnd.ogc.wms_xml'\">",'<a href="#" class="md-mn addLayer" title="{title}" alt="{title}" onclick="app.switchMode(1, true);app.getIMap().addWMSLayer([[\'{title}\', \'{href}\', \'{name}\', \'{id}\']]);">&nbsp;</a>',"</tpl>","</tpl>","</div>","</li>","</tpl>","</ul>");
GeoNetwork.Templates.FULL=new Ext.XTemplate("<ul>",'<tpl for=".">','<li class="md md-full">','<span class="md-logo"><img src="{[catalogue.URL]}/images/logos/{source}.gif"/></span>','<table class="md-wrap md-desc" id="{uuid}">',"<tr><td>",GeoNetwork.Templates.TITLE,"<span>{[values.abstract.substring(0, 250)]} ...</span><br/>",'<span class="subject"><tpl for="subject">','{value}{[xindex==xcount?"":", "]}',"</tpl></span><br/>",'<div class="md-links">','<tpl for="links">',"<tpl if=\"values.type == 'application/vnd.ogc.wms_xml'\">",'<a href="#" class="md-mn addLayer" title="{title}" alt="{title}" onclick="app.switchMode(1, true);app.getIMap().addWMSLayer([[\'{[escape(values.title)]}\', \'{href}\', \'{name}\', \'{id}\']]);">&nbsp;</a>',"</tpl>","<tpl if=\"values.type == 'application/vnd.google-earth.kml+xml'\">",'<a href="{href}" class="md-mn md-mn-kml" title="{title} (kml)" alt="{title} (kml)">&nbsp;</a>',"</tpl>","<tpl if=\"values.type == 'application/zip'\">",'<a href="{href}" class="md-mn md-mn-zip" title="{title}" alt="{title}">&nbsp;</a>',"</tpl>","<tpl if=\"values.type == 'text/html'\">",'<a href="{href}" class="md-mn md-mn-www" title="{title}" alt="{title}">&nbsp;</a>',"</tpl>","</tpl>","</span></div>","</td>",'<td class="thumbnail">',GeoNetwork.Templates.RATING_TPL,'<tpl if="thumbnail">','<div class="thumbnail"><img class="thumbnail" src="{thumbnail}" alt="thumbnail"/></div>',"</tpl>","<tpl if=\"thumbnail==''\">",'<div class="thumbnail"></div>',"</tpl>","<tpl if=\"edit=='true' && isharvested!='y'\">",'<div class="md-mn md-mn-user">{ownername}</div>',"</tpl>","</td></tr>","</table>","</li>","</tpl>","</ul>");Ext.namespace("GeoNetwork");
GeoNetwork.MetadataResultsView=Ext.extend(Ext.DataView,{catalogue:undefined,templates:null,overClass:"md-over",itemSelector:"li.md",emptyText:"",autoWidth:true,maps:[],mdSelectionUuids:[],layer_style:OpenLayers.Util.extend({},OpenLayers.Feature.Vector.style["default"]),layer_style_hover:OpenLayers.Util.extend({},OpenLayers.Feature.Vector.style["default"]),layer_style_selected:OpenLayers.Util.extend({},OpenLayers.Feature.Vector.style["default"]),features:[],hover_feature:[],contextMenu:undefined,contextMenuNodeId:undefined,ratingWidget:undefined,editAction:undefined,deleteAction:undefined,zoomToAction:undefined,otherActions:undefined,adminMenuSeparator:undefined,duplicateAction:undefined,createChildAction:undefined,adminAction:undefined,categoryAction:undefined,viewAction:undefined,viewXMLAction:undefined,plugins:[],listeners:{dblclick:{fn:function(c,a,f,g){if(this.maps){var b=this.getStore().getAt(a);
var d=b.get("uuid");
this.zoomTo(d)
}}},mouseenter:{fn:function(b,m,c,k){if(this.maps){var h=this.getStore().getAt(m);
var a=h.get("uuid");
for(var f=0;
f<this.maps.length;
f++){var d=this.maps[f].layer;
if(d.features){for(var g=0;
g<d.features.length;
g++){if(a==d.features[g].attributes.id){d.drawFeature(d.features[g],this.layer_style_hover);
this.hover_feature[f]=d.features[g];
continue
}}}}}}},mouseleave:{fn:function(d,a,f,g){if(this.maps){for(var c=0;
c<this.maps.length;
c++){var b=this.maps[c].layer;
if(this.hover_feature[c]!=null){b.drawFeature(this.hover_feature[c],this.layer_style);
this.hover_feature[c]=null;
continue
}}}}},contextmenu:{fn:function(a,i,c,g){this.contextMenuNodeId=i;
var d=this.getStore().getAt(i);
var j=d.get("edit")=="true"?true:false;
var f=d.get("isharvested")=="y"?true:false;
var h=d.get("harvestertype");
var b=this.catalogue.isIdentified();
this.createMenu(i,a);
if(!b){this.editAction.hide();
this.deleteAction.hide()
}else{this.editAction.show();
this.deleteAction.show()
}this.otherActions.setVisible(b);
this.adminMenuSeparator.setVisible(b);
this.editAction.setDisabled(!j);
this.adminAction.setDisabled(!j);
this.categoryAction.setDisabled(!j);
this.deleteAction.setDisabled(!j);
if(this.ratingWidget){this.ratingWidget.reset();
if(f&&h!="geonetwork"){this.ratingWidget.disable()
}else{this.ratingWidget.enable()
}}this.contextMenu.showAt(g.getXY());
g.stopEvent()
}}},createMenu:function(f,e){var b=this.getStore().getAt(f);
var d=b.get("edit")=="true"?true:false;
var a=b.get("isharvested")=="y"?true:false;
var c=b.get("harvestertype");
if(!this.contextMenu){this.contextMenu=new Ext.menu.Menu({floating:true});
this.editAction=new Ext.Action({text:"Edit",iconCls:"md-mn-edit",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("id");
this.catalogue.metadataEdit(h)
},scope:e});
this.contextMenu.add(this.editAction);
this.deleteAction=new Ext.Action({text:"Delete",iconCls:"md-mn-del",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("uuid");
this.catalogue.metadataDelete(h)
},scope:e});
this.contextMenu.add(this.deleteAction);
this.duplicateAction=new Ext.Action({text:"Duplicate",iconCls:"md-mn-copy",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("uuid");
this.catalogue.metadataDuplicate(h)
},scope:e});
this.createChildAction=new Ext.Action({text:"Create child",iconCls:"md-mn-copy",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("uuid");
this.catalogue.metadataCreateChild(h)
},scope:e});
this.adminAction=new Ext.Action({text:"Privileges",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("id");
this.catalogue.metadataAdmin(h)
},scope:e});
this.categoryAction=new Ext.Action({text:"Categories",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("id");
this.catalogue.metadataCategory(h)
},scope:e});
this.otherActions=new Ext.menu.Item({text:"Other actions",menu:{items:[this.duplicateAction,this.createChildAction,this.adminAction,this.categoryAction]}});
this.contextMenu.add(this.otherActions);
this.adminMenuSeparator=new Ext.menu.Separator();
this.contextMenu.add(this.adminMenuSeparator);
this.viewAction=new Ext.Action({text:"View",iconCls:"md-mn-view",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("uuid");
this.catalogue.metadataShow(h)
},scope:e});
this.contextMenu.add(this.viewAction);
this.zoomToAction=new Ext.Action({text:"Zoom to",iconCls:"zoomlayer",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("uuid");
this.zoomTo(h)
},scope:e});
this.contextMenu.add(this.zoomToAction);
this.viewXMLAction=new Ext.Action({text:"Save as XML",iconCls:"md-mn-view",handler:function(){var g=this.getStore().getAt(this.contextMenuNodeId);
var i=g.get("uuid");
var h=g.get("schema");
this.catalogue.metadataXMLShow(i,h)
},scope:e});
this.contextMenu.add(this.viewXMLAction);
if(Ext.ux.RatingItem){this.ratingWidget=new Ext.ux.RatingItem(null,{canReset:false,name:"rating",disabled:true,nbStars:5,cls:"ux-menu-rating-item",listeners:{change:function(k,j,i){if(j){var g=this.getStore().getAt(this.contextMenuNodeId);
var h=g.get("uuid");
this.catalogue.metadataRate(h,j,this.contextMenu.hide())
}},scope:e}});
this.contextMenu.add(this.ratingWidget)
}}},zoomTo:function(d){for(var b=0;
b<this.maps.length;
b++){var a=this.maps[b].layer;
if(a.features){for(var c=0;
c<a.features.length;
c++){if(d==a.features[c].attributes.id){var e=a.features[c].geometry.getBounds();
if(e){this.maps[b].map.zoomToExtent(e)
}break
}}}}},initComponent:function(b){this.templates={SIMPLE:GeoNetwork.Templates.SIMPLE,THUMBNAIL:GeoNetwork.Templates.THUMBNAIL,FULL:GeoNetwork.Templates.FULL};
GeoNetwork.MetadataResultsView.superclass.initComponent.call(this);
this.store=this.catalogue.metadataStore;
this.initStyle();
if(this.maps!=undefined){if(this.maps instanceof OpenLayers.Map){var d=this.maps;
this.maps=[];
this.addMap(d)
}else{for(var c=0;
c<this.maps.length;
c++){var a=this.maps[c];
this.initMap(a)
}}}else{this.maps=[]
}this.getStore().on({load:this.resultsLoaded,clear:this.destroyMetadataBbox,scope:this});
this.on("selectionchange",this.selectionChange)
},addMap:function(d,e){var c=false;
for(var b=0;
b<this.maps.length;
b++){if(this.maps[b].map.id==d.id){c=true;
break
}}if(!c){if(e==undefined){e=true
}var a={map:d,zoomToExtentOnSearch:e};
this.maps.push(a);
this.initMap(a)
}},initMap:function(b){var a=new OpenLayers.Layer.Vector("Metadata results",{style:this.layer_style});
this.addCurrentFeatures(a);
b.layer=a;
b.map.addLayer(a)
},removeMap:function(a){this.maps.splice(a,1)
},getTemplates:function(){return this.templates
},removeTemplate:function(a){delete this.templates[a]
},addTemplate:function(a,b){this.templates[a]=b
},applyTemplate:function(a){this.tpl=this.templates[a];
this.refresh()
},initStyle:function(){this.layer_style.fillOpacity=0;
this.layer_style.graphicOpacity=1;
this.layer_style_selected.fillOpacity=0.1;
this.layer_style_hover.fillOpacity=0.3;
this.layer_style_hover.strokeWidth=3
},destroyMetadataBbox:function(){for(var b=0;
b<this.maps.length;
b++){var a=this.maps[b].layer;
if(a.features){if(a.features.length>0){this.features=[];
a.destroyFeatures()
}}}},resultsLoaded:function(a,b,c){this.drawMetadataBbox(a,b,c);
this.initRatingWidget();
this.initSelector()
},drawMetadataBbox:function(b,c,d){Ext.each(c,function(g){var l=g.get("bbox");
if(l){var o=[];
for(var h=0;
h<l.length;
h++){var s=l[h].value;
var t=new OpenLayers.Geometry.Point(s[2],s[1]);
var q=new OpenLayers.Geometry.Point(s[2],s[3]);
var p=new OpenLayers.Geometry.Point(s[0],s[3]);
var n=new OpenLayers.Geometry.Point(s[0],s[1]);
var f=[t,q,p,n,t];
var i=new OpenLayers.Geometry.LinearRing(f);
var m=new OpenLayers.Geometry.Polygon([i]);
o.push(m.clone())
}var k=new OpenLayers.Feature.Vector(new OpenLayers.Geometry.MultiPolygon(o),{id:g.get("uuid")});
this.features.push(k.clone())
}},this);
for(var e=0;
e<this.maps.length;
e++){this.addCurrentFeatures(this.maps[e].layer)
}if(this.features.length>0){for(var e=0;
e<this.maps.length;
e++){var a=this.maps[e];
if(a.zoomToExtentOnSearch){a.map.zoomToExtent(a.layer.getDataExtent())
}}}},initRatingWidget:function(){var b=Ext.DomQuery.select("div.rating");
for(var a=0;
a<b.length;
++a){if(Ext.ux.RatingItem){new Ext.ux.RatingItem(b[a],{disabled:true,name:"rating"+a})
}else{b[a].style.display="none"
}}},initSelector:function(){},selectAll:function(){var b=Ext.DomQuery.select("input.selector");
for(var a=0;
a<b.length;
++a){b[a].checked=true
}},selectAllInPage:function(){var b=Ext.DomQuery.select("input.selector");
for(var a=0;
a<b.length;
++a){b[a].checked=true;
Ext.each(this.getRecords(this.getNodes()),function(d){var c=d.get("uuid");
this.catalogue.metadataSelect("add",[c])
},this)
}},selectNone:function(){var b=Ext.DomQuery.select("input.selector");
for(var a=0;
a<b.length;
++a){b[a].checked=false
}},initSelection:function(a){var b=[];
Ext.each(a,function(d){var c=d.get("selected");
if(c=="true"){b.push(d)
}});
this.select(b);
this.on("selectionchange",this.selectionChange)
},selectionChange:function(e,b){if(this.catalogue){var a=this.getRecords(b);
var h={};
var d=[];
var g=[];
for(var c=0;
c<a.length;
c++){var f=a[c].get("uuid");
h[f]=true
}for(var c=0;
c<this.mdSelectionUuids.length;
c++){var f=this.mdSelectionUuids[c];
if(h[f]){d.push(f)
}else{g.push(f)
}}for(var f in h){if(!this.mdSelectionUuids[f]){d.push(f)
}}this.catalogue.metadataSelect("add",d);
this.catalogue.metadataSelect("remove",g);
this.mdSelectionUuids=d
}},addCurrentFeatures:function(c){var a=this.features.length;
if(a>0){var d=[];
d=new Array(a);
var f,e;
for(var b=0;
b<a;
b++){f=this.features[b];
e=f.clone();
d[b]=e
}c.addFeatures(d)
}},onDestroy:function(){if(this.mdSelectionInfoCmp){}GeoNetwork.MetadataResultsView.superclass.onDestroy.apply(this,arguments)
}});
Ext.reg("gn_metadataresultsview",GeoNetwork.MetadataResultsView);Ext.namespace("GeoNetwork");
GeoNetwork.MetadataResultsToolbar=Ext.extend(Ext.Toolbar,{catalogue:undefined,metadataResultsView:undefined,mdSelectionInfoCmp:undefined,searchBtCmp:undefined,sortByCmp:undefined,sortByCombo:undefined,mdSelectionInfo:"md-selection-info",deleteAction:undefined,ownerAction:undefined,updateCategoriesAction:undefined,updatePrivilegesAction:undefined,item:null,actionOnSelectionMenu:undefined,admin:false,initComponent:function(){var a=[];
a.push(this.createSelectionToolBar());
a.push(["->"]);
var b=this.getSortByCombo();
a.push("Sort by ",b,"|");
a.push(this.createTemplateMenu());
a.push(this.createOtherActionMenu());
GeoNetwork.MetadataResultsToolbar.superclass.initComponent.call(this);
this.add(a);
this.catalogue.on("selectionchange",this.updateSelectionInfo,this);
this.catalogue.on("afterLogin",this.updatePrivileges,this);
this.catalogue.on("afterLogout",this.updatePrivileges,this)
},getSortByCombo:function(){var a=this;
this.sortByCombo=new Ext.form.ComboBox({mode:"local",id:"sortByToolBar",triggerAction:"all",value:"relevance",store:GeoNetwork.util.SearchFormTools.getSortByStore(),valueField:"id",displayField:"name",listeners:{select:function(c,d,b){if(this.sortByCmp!=undefined){this.sortByCmp.setValue(c.getValue());
if(c.getValue()=="title"){Ext.getCmp("sortOrder").setValue("reverse")
}else{Ext.getCmp("sortOrder").setValue("")
}}if(this.searchBtCmp!=undefined){this.searchBtCmp.fireEvent("click")
}},scope:a}});
return this.sortByCombo
},createTemplateMenu:function(){var d=this.metadataResultsView.getTemplates();
var c=[];
for(var a in d){var b=new Ext.Button({text:"",enableToggle:true,toggleGroup:"tpl",id:a,iconCls:"mn-view-"+a.toLowerCase(),toggleHandler:function(e,f){if(f){this.applyTemplate(e.getId())
}this.initRatingWidget()
},scope:this.metadataResultsView,pressed:(a=="FULL"?true:false)});
c.push(b)
}return[c,"|"]
},createOtherActionMenu:function(){var b=new Ext.menu.Menu();
var c=new Ext.Action({text:"Export (CSV)",handler:function(){this.catalogue.csvExport()
},scope:this});
var a=new Ext.Action({text:"Export (ZIP)",iconCls:"md-mn-zip",handler:function(){this.catalogue.mefExport()
},scope:this});
b.add(a,c,{text:"Print selection",iconCls:"md-mn-pdf",handler:function(){this.catalogue.pdfExport()
},scope:this});
this.deleteAction=new Ext.menu.Item({text:"Delete",id:"deleteAction",handler:function(){this.catalogue.massiveOp("Delete")
},scope:this,hidden:true});
this.ownerAction=new Ext.menu.Item({text:"New owner",id:"ownerAction",handler:function(){this.catalogue.massiveOp("NewOwner")
},scope:this,hidden:true});
this.updateCategoriesAction=new Ext.menu.Item({text:"Update categories",id:"updateCategoriesAction",handler:function(){this.catalogue.massiveOp("Categories")
},scope:this,hidden:true});
this.updatePrivilegesAction=new Ext.menu.Item({text:"Update privileges",id:"updatePrivilegesAction",handler:function(){this.catalogue.massiveOp("Privileges")
},scope:this,hidden:true});
b.addItem(this.deleteAction);
b.addItem(this.ownerAction);
b.addItem(this.updateCategoriesAction);
b.addItem(this.updatePrivilegesAction);
this.actionOnSelectionMenu=new Ext.Button({text:"Other actions",menu:b});
return this.actionOnSelectionMenu
},createSelectionToolBar:function(){var b=new Ext.Action({text:"all in page",handler:function(){this.metadataResultsView.selectAllInPage()
},scope:this});
var a=new Ext.Action({text:"all",handler:function(){this.catalogue.metadataSelectAll();
this.metadataResultsView.selectAll()
},scope:this});
var c=new Ext.Action({text:"none",handler:function(){this.catalogue.metadataSelectNone();
this.metadataResultsView.selectNone()
},scope:this});
var d={id:"md-selection-info",xtype:"tbtext",text:"None selected"};
this.catalogue.on("selectionchange",this.updateSelectionInfo,this);
return[d,"|",{xtype:"tbtext",text:"Select: "},b,{xtype:"tbtext",text:", "},a,{xtype:"tbtext",text:", "},c]
},updateSelectionInfo:function(b,a){if(this.mdSelectionInfoCmp==undefined){this.mdSelectionInfoCmp=Ext.getCmp(this.mdSelectionInfo)
}if(a==0){this.actionOnSelectionMenu.disable()
}else{this.actionOnSelectionMenu.enable()
}this.mdSelectionInfoCmp.setText(a+" selected")
},updatePrivileges:function(b,a){if(a&&a.role=="admin"){this.deleteAction.show();
this.ownerAction.show();
this.updateCategoriesAction.show();
this.updatePrivilegesAction.show()
}else{this.deleteAction.hide();
this.ownerAction.hide();
this.updateCategoriesAction.hide();
this.updatePrivilegesAction.hide()
}},onDestroy:function(){if(this.mdSelectionInfoCmp){}GeoNetwork.MetadataResultsToolbar.superclass.onDestroy.apply(this,arguments)
}});
Ext.reg("gn_metadataresultstoolbar",GeoNetwork.MetadataResultsToolbar);Ext.namespace("GeoNetwork");
GeoNetwork.TagCloudView=Ext.extend(Ext.DataView,{catalogue:undefined,multiSelect:true,root:"keywords.keyword",qurey:undefined,searchField:"themekey",onSuccess:null,onFailure:null,overClass:"tag-cloud-hover",itemSelector:"li.tag-cloud",emptyText:"",autoWidth:true,listeners:{selectionchange:{fn:function(b,a){}},dblclick:{fn:function(b,a,c,d){}}},initComponent:function(){GeoNetwork.TagCloudView.superclass.initComponent.call(this);
this.tpl=new Ext.XTemplate("<ul>",'<tpl for=".">','<li class="tag-cloud tag-cloud-{class}">','<a href="#" onclick="javascript:catalogue.kvpSearch(\'fast=false&'+this.searchField+"={value}', "+this.onSuccess+","+this.onFailure+', null);" alt="{value}" title="{count} records">{value}</a>',"</li>","</tpl>","</ul>");
this.store=this.catalogue.summaryStore;
if(this.query!=undefined){this.catalogue.kvpSearch(this.query,null,null,null,true)
}}});
Ext.reg("gn_tagcloudview",GeoNetwork.TagCloudView);Ext.namespace("GeoNetwork.admin");
GeoNetwork.admin.HarvesterPanel=Ext.extend(Ext.Panel,{border:false,frame:false,layout:"border",height:800,catalogue:undefined,harvesterStore:undefined,harvesterTplMarkup:["Information: {site_name}<br/>","Last run: {info_lastrun}<br/>",'<tpl if="info_result_total">',"Total: {info_result_total}<br/>","Added: {info_result_added}<br/>","Removed: {info_result_removed}<br/>","Schema unknwon: {info_result_unknowSchema}<br/>","</tpl>"],harvesterTpl:undefined,xmlTpl:undefined,harvesterGrid:undefined,currentHarvester:undefined,initComponent:function(){GeoNetwork.admin.HarvesterPanel.superclass.initComponent.call(this);
panel=this;
this.createGrid();
this.harvesterTpl=new Ext.XTemplate(this.harvesterTplMarkup);
this.xmlTpl=new GeoNetwork.Templates().getHarvesterTemplate();
this.harvesterGrid.getSelectionModel().on("rowselect",function(f,e,d){var c=Ext.getCmp("harvesterDetailPanel");
this.harvesterTpl.overwrite(c.body,d.data)
},this);
var b={id:"harvesterDetailPanel",region:"east",split:true,minWidth:200,width:200,collapsible:true,hideCollapseTool:true,collapseMode:"mini",bodyStyle:{background:"#ffffff",padding:"7px"},html:"Please select a harvester to see additional details."};
this.add(b);
var a={id:"harvesterEditorPanel",region:"south",title:"Harvester configuration",split:true,autoScroll:true,minHeigth:400,items:[new Ext.FormPanel({border:false,frame:false,maxHeight:600,id:"harvesterEditorForm",items:[this.getHarvesterTypeField(),this.getSiteFields("geonetwork"),this.getOptionsFields("geonetwork")],buttons:[{text:"Save",listeners:{click:function(){var d=Ext.getCmp("harvesterEditorForm").getForm();
if(d.isValid()){d.updateRecord(this.currentHarvester);
var c=this.xmlTpl.apply(this.currentHarvester.data);
this.addHarvester(this.catalogue.services.harvestingAdd,c)
}},scope:panel}}]})]};
this.add(a)
},harvesterType:[["geonetwork","GeoNetwork"],["geonetwork20","GeoNetwork 2.0"],["webdav","webdav"],["csw","csw"],["ogcwxs","OGC WxS"],["thredds","thredds"],["z3950","z3950"],["oaipmh","oaipmh"],["metadatafragments","metadatafragments"],["arcsde","arcsde"],["filesystem","filesystem"]],switchMode:function(a){this.switchElement("site_servlet",false);
this.switchElement("site_port",false);
this.switchElement("site_host",false);
this.switchElement("site_url",false);
if(a=="geonetwork"||a=="geonetwork20"){this.switchElement("site_servlet",true);
this.switchElement("site_port",true);
this.switchElement("site_host",true)
}else{this.switchElement("site_url",true)
}if(a=="ogcwxs"){}},switchElement:function(b,a){var g=Ext.getCmp(b);
var c=Ext.getCmp("harvesterEditorForm");
var d=c.getForm().findField(b);
if(a){d.container.up("div.x-form-item").show();
g.enable()
}else{d.container.up("div.x-form-item").hide();
g.disable()
}c.doLayout(false,true)
},getHarvesterTypeField:function(){return new Ext.form.ComboBox({id:"type",name:"type",mode:"local",triggerAction:"all",fieldLabel:"Harvester type",store:new Ext.data.ArrayStore({id:0,fields:["id","name"],data:this.harvesterType}),valueField:"id",displayField:"name",listeners:{change:function(c,b,a){this.switchMode(b)
},scope:this}})
},ogcwxsType:[["WMS1.0.0","WMS 1.0.0"],["WMS1.1.1","WMS 1.1.1"],["WFS1.0.0","WFS 1.0.0"],["WFS1.1.0","WFS 1.1.0"],["WCS1.0.0","WCS 1.0.0"],["WPS0.4.0","WPS 0.4.0"],["WPS1.0.0","WPS 1.0.0"]],getSiteFields:function(h){var g=[];
var b=new Ext.form.TextField({id:"id",name:"id",mode:"local",hidden:true});
var b=new Ext.form.TextField({name:"site_name",mode:"local",fieldLabel:"Name"});
g.push(b);
var c=new Ext.form.TextField({id:"site_url",name:"site_url",mode:"local",fieldLabel:"URL"});
var h=new Ext.form.ComboBox({id:"site_ogctype",name:"site_ogctype",mode:"local",fieldLabel:"Type of service",store:new Ext.data.ArrayStore({id:0,fields:["id","name"],data:this.ogcwxsType}),valueField:"id",displayField:"name"});
g.push(c,h);
var i=new Ext.form.TextField({id:"site_servlet",name:"site_servlet",mode:"local",fieldLabel:"Servlet"});
var j=new Ext.form.TextField({id:"site_host",name:"site_host",mode:"local",fieldLabel:"Host"});
var d=new Ext.form.TextField({id:"site_port",name:"site_port",mode:"local",fieldLabel:"Port"});
g.push(j,d,i);
var e=new Ext.form.Checkbox({id:"site_account_use",name:"site_account_use",mode:"local",fieldLabel:"Use account"});
var f=new Ext.form.TextField({id:"site_account_username",name:"site_account_username",mode:"local",fieldLabel:"Username"});
var a=new Ext.form.TextField({id:"site_account_password",name:"site_account_password",mode:"local",fieldLabel:"Password"});
g.push(e,f,a);
return{xtype:"fieldset",title:"Harvester main information",collapsible:true,items:g}
},getOptionsFields:function(){var b=[];
var f=new Ext.form.Checkbox({id:"options_createthumbnails",name:"options_createthumbnails",mode:"local",fieldLabel:"Create thumbnail for WMS layers"});
var e=new Ext.form.Checkbox({id:"options_uselayer",name:"options_uselayer",mode:"local",fieldLabel:"Create metadata for layer elements using GetCapabilities information"});
var f=new Ext.form.Checkbox({id:"options_uselayermd",name:"options_uselayermd",mode:"local",fieldLabel:"Create metadata for layer elements using MetadataURL attributes (if existing, if not use GetCapabilities)"});
var d=new Ext.form.TextField({id:"options_lang",name:"options_lang",mode:"local",defaultValue:"eng",fieldLabel:"Language"});
var c=new Ext.form.TextField({id:"options_topic",name:"options_topic",mode:"local",defaultValue:"",fieldLabel:"Topic category"});
b.push(f,e,f,d,c);
var a=new Ext.form.Checkbox({id:"options_onerunonly",name:"options_onerunonly",mode:"local",fieldLabel:"One run only"});
var g=new Ext.form.TextField({id:"options_every",name:"options_every",mode:"local",defaultValue:"90",fieldLabel:"Every"});
b.push(a,g);
return{xtype:"fieldset",title:"Harvester options",collapsible:true,items:b}
},getPrivilegesFields:function(){return
},getCategoriesFields:function(){return
},createEditor:function(){var a=Ext.data.Record.create([{name:"id",mapping:"node/@id"},{name:"occupation"}])
},createGrid:function(){var a=new Ext.grid.CheckboxSelectionModel({singleSelect:true,header:"",listeners:{selectionchange:function(){Ext.getCmp("harvestEditBt").setDisabled(this.getSelections().length<1);
Ext.getCmp("harvestRemoveBt").setDisabled(this.getSelections().length<1);
Ext.getCmp("harvestActivateBt").setDisabled(this.getSelections().length<1);
Ext.getCmp("harvestDesactivateBt").setDisabled(this.getSelections().length<1);
Ext.getCmp("harvestRunBt").setDisabled(this.getSelections().length<1)
}}});
this.harvesterGrid=new Ext.grid.GridPanel({store:this.harvesterStore,split:true,id:"harvesterGrid",region:"center",sm:a,columns:[a,{id:"id",header:"id",width:60,sortable:true,dataIndex:"id",hidden:true},{header:"Name",width:125,sortable:true,dataIndex:"site_name"},{header:"Type",width:75,sortable:true,dataIndex:"type"},{header:"Status",width:80,sortable:true,dataIndex:"site_status"},{header:"Running",width:80,sortable:true,dataIndex:"info_running"},{header:"Every",width:80,sortable:true,dataIndex:"options_every"},{header:"Last run",width:160,sortable:true,dataIndex:"info_lastrun"}],stripeRows:true,maxHeight:250,height:250,stateful:true,stateId:"grid",listeners:{rowclick:function(b,g,f){var c=this.harvesterGrid.getStore().getAt(g);
this.currentHarvester=c;
console.log(c);
var d=Ext.getCmp("harvesterEditorForm").getForm();
d.loadRecord(c)
},scope:this},buttons:[{text:"Add",listeners:{click:function(){}}},{text:"Activate",id:"harvestActivateBt",disabled:true,listeners:{click:function(){var b=Ext.getCmp("harvesterGrid").getSelectionModel().getSelected();
var c={id:b.data.id};
this.doAction(this.catalogue.services.harvestingStart,c,this.refreshHarvester,null,null,"Failed to activate the harvester")
},scope:this}},{text:"Desactivate",id:"harvestDesactivateBt",disabled:true,listeners:{click:function(){var b=Ext.getCmp("harvesterGrid").getSelectionModel().getSelected();
var c={id:b.data.id};
this.doAction(this.catalogue.services.harvestingStop,c,this.refreshHarvester,null,null,"Failed to desactivate the harvester")
},scope:this}},{id:"harvestEditBt",text:"Edit",disabled:true,listeners:{click:function(){},scope:this}},{text:"Run",id:"harvestRunBt",disabled:true,listeners:{click:function(){var b=Ext.getCmp("harvesterGrid").getSelectionModel().getSelected();
var c={id:b.data.id};
this.doAction(this.catalogue.services.harvestingRun,c,this.refreshHarvester,null,null,"Failed to start the harvester")
},scope:this}},{id:"harvestRemoveBt",text:"Remove",icon:"../images/default/cross.png",disabled:true,listeners:{click:function(){var b=Ext.getCmp("harvesterGrid").getSelectionModel().getSelected();
Ext.Msg.confirm("Delete ?","Are you sure to delete selected harvester and all its records?",this.removeHarvester,b.data.id)
},scope:this}},{text:"Refresh",id:"harvestRefreshBt",listeners:{click:function(){this.harvesterGrid.getStore().reload()
},scope:this}}]});
this.add(this.harvesterGrid)
},addHarvester:function(b,a){var c={url:b,data:a};
OpenLayers.Util.applyDefaults(c,{success:function(d){Ext.Msg.alert("ok",d.responseText)
},failure:function(d){Ext.Msg.alert("Failed",d.responseText)
}});
OpenLayers.Request.POST(c)
},doAction:function(a,e,d,b,f,c){OpenLayers.Request.GET({url:a,params:e,success:function(g){if(f!=null){Ext.Msg.alert(f,g.responseText)
}if(d){d(g)
}},failure:function(g){if(c!=null){Ext.Msg.alert(c,g.responseText)
}if(d){d(g)
}}})
},removeHarvester:function(a){if(a=="yes"){var b={id:this};
this.doAction(this.catalogue.services.harvestingRemove,b,this.refreshHarvester,null,null,"Failed to delete the harvester")
}},refreshHarvester:function(){Ext.getCmp("harvestRefreshBt").fireEvent("click")
}});
Ext.reg("gn_admin_harvesterpanel",GeoNetwork.admin.HarvesterPanel);Ext.namespace("GeoNetwork");
GeoNetwork.OGCServiceQuickRegister=Ext.extend(Ext.menu.Menu,{catalogue:undefined,harvesterStore:undefined,textInfo:"Enter a GetCapabilities URL to register a new service.",textExample:"eg. http://services.sandre.eaufrance.fr/geo/ouvrage?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities",textValidButton:"Check & Add",urlFieldWidth:250,urlField:undefined,initComponent:function(){var b=this;
var a=[];
a.push({text:this.textInfo,canActivate:false});
this.urlField={xtype:"textfield",width:this.urlFieldWidth,margins:"0 0 0 30",id:"OGCServiceQuickRegisterUrl",value:"",iconCls:"no-icon"};
a.push(this.urlField);
a.push({text:this.textExample,canActivate:false});
a.push({xtype:"buttongroup",autoWidth:true,border:false,frame:false,columns:2,iconCls:"no-icon",defaults:{xtype:"button",iconAlign:"right"},items:[{text:this.textValidButton,iconCls:"md-mn md-mn-badd",handler:this.getCapabilities}]});
Ext.apply(this,{defaultType:"menuitem",items:a});
GeoNetwork.OGCServiceQuickRegister.superclass.initComponent.call(this)
},getCapabilities:function(){var a=Ext.getCmp("OGCServiceQuickRegisterUrl").getValue();
var b=this.ownerCt.ownerCt;
OpenLayers.Request.GET({url:a,success:function(c){if(c.responseText.indexOf("Some unexpected")!=-1){Ext.Msg.alert("Failed to check service url.",c.responseText);
return
}var d=c.responseXML;
if(d==null){Ext.Msg.alert("GetCapabilities failed","Null response returned - check proxy or service configuration.");
return
}var f=new OpenLayers.Format.WMSCapabilities();
var e=f.read(d);
b.addHarvester(e.service.title,a,e.version)
},failure:function(c){Ext.Msg.alert("Failed to check service url.",c.responseText)
}})
},addHarvester:function(f,d,a){var e=new Ext.data.Record({id:"",type:"ogcwxs",site_name:f,site_url:d.replace(/&/g,"&amp;"),site_ogctype:"WMS"+a,site_icon:"default.gif",site_account_use:"false",options_every:"90",options_topic:"",options_onerunonly:"true",options_lang:"en",options_createthumbnails:"true",options_uselayer:"true",options_uselayermd:"false"});
var b=new GeoNetwork.Templates().getHarvesterTemplate();
var c=b.apply(e.data);
this.saveHarvester(c)
},saveHarvester:function(a){var c=this;
var b={url:this.catalogue.services.harvestingAdd,data:a};
OpenLayers.Util.applyDefaults(b,{success:function(d){var e=d.responseXML;
var f=e.firstChild.getAttribute("id");
c.runHarvester(this.catalogue.services.harvestingRun,f)
},failure:function(d){Ext.Msg.alert("Failed",d.responseText)
}});
OpenLayers.Request.POST(b)
},runHarvester:function(a,c){var b=this;
OpenLayers.Request.GET({url:a+"?id="+c,success:function(d){if(d.responseText.indexOf("nexpected")!=-1){Ext.Msg.alert("Activation or start failed.",d.responseText);
return
}Ext.Msg.alert("Successfull registration","Wait a few minutes before having all layers and services registered in the catalogue.");
if(b.harvesterStore){b.harvesterStore.reload()
}},failure:function(d){Ext.Msg.alert("Registration failed",d.responseText)
}})
}});
Ext.reg("gn_ogcservicequickregister",GeoNetwork.OGCServiceQuickRegister);var translations={};
function translate(a){return translations[a]||a
}function checkAllInputsIn(d,b){var c=Ext.getDom(d).getElementsByTagName("input");
for(var a=0;
a<c.length;
a++){c[a].checked=b
}}function setAll(a){checkAllInputsIn(a,true)
}function clearAll(a){checkAllInputsIn(a,false)
}function checkBoxModalUpdate(f,a,e,d){var c=Ext.DomQuery.select('input[type="checkbox"]');
var b="?";
if(a=="metadata.admin"||a=="metadata.category"){b+="id="+Ext.getDom("metadataid").value
}Ext.each(c,function(g){if(g.checked){b+="&"+g.name+"=on"
}});
catalogue.doAction(a+b,null,null,d,function(g){Ext.getDom(f).innerHTML=g.responseText
},null)
}function doGroups(a){catalogue.doAction("xml.usergroups.list?id="+a,null,null,"Error retrieving groups",function(c){if(c.nodeName=="error"){Ext.getDom("group").options.length=0;
Ext.getDom("group").value="";
var b=Ext.getDom("user");
for(i=0;
i<b.options.length;
i++){b.options[i].selected=false
}}else{addGroups(c.responseXML)
}})
}function addGroups(b){var e=b.getElementsByTagName("group");
Ext.getDom("group").options.length=0;
for(var d=0;
d<e.length;
d++){var f=e[d].getElementsByTagName("id")[0].firstChild.nodeValue;
var a=e[d].getElementsByTagName("name")[0].firstChild.nodeValue;
var c=document.createElement("option");
c.text=a;
c.value=f;
if(e.length==1){c.selected=true
}Ext.getDom("group").options.add(c)
}}function checkMassiveNewOwner(b,d){var a=Ext.getDom("user").value;
var c=Ext.getDom("group").value;
if(a==""){Ext.Msg.alert(d,"selectNewOwner");
return false
}if(c.value==""){Ext.Msg.alert(d,"selectOwnerGroup");
return false
}catalogue.doAction(b+"?user="+a+"&group="+c,null,null,null,function(e){Ext.getDom("massivenewowner").parentNode.innerHTML=e.responseText
})
};Ext.namespace("GeoNetwork.map");
GeoNetwork.map.ExtentMap=function(){var y=null;
var k=new Array();
var f=null;
var d=OpenLayers.Feature.Vector.style["default"];
var s=null;
var r=null;
var g=null;
var l=null;
var t=null;
var e=5;
var m=OpenLayers.Class(OpenLayers.Geometry,{id:null,initialize:function(z){this.id=z
},CLASS_NAME:"GeoNetwork.map.ExtentMap.MultiPolygonReference"});
var p="EPSG:4326";
var v=new OpenLayers.Projection("EPSG:4326");
var x=null;
var o="m";
var c=null;
function u(z,A){var D=new OpenLayers.Projection(A);
var B=(D==v?new OpenLayers.Format.GML.v3():new OpenLayers.Format.GML.v3({externalProjection:D,internalProjection:v}));
var C=B.writeNode("feature:_geometry",z.geometry);
return OpenLayers.Format.XML.prototype.write.call(B,C.firstChild)
}function n(){var z={units:o,projection:p,theme:null};
y=new OpenLayers.Map(z);
if(!l){var B=y.getControlsByClass("OpenLayers.Control.Navigation")[0];
B.disableZoomWheel();
y.removeControl(y.getControlsByClass("OpenLayers.Control.PanZoom")[0])
}y.addControl(new OpenLayers.Control.MousePosition());
var A=new OpenLayers.Layer.WMS("Global Imagery","http://maps.opengeo.org/geowebcache/service/wms",{layers:"bluemarble"});
y.addLayer(A);
f=new OpenLayers.Layer.Vector("VectorLayer",{});
y.addLayer(f);
f.events.on({sketchstarted:function(){this.destroyFeatures()
},scope:f});
return y
}function q(C,B,D,E){var z=B.split(",");
for(var A=0;
A<z.length;
++A){Ext.get(z[A]).on("change",function(){b(E,B,D,false)
});
Ext.get("_"+z[A]).on("change",function(){b(E,B,D,true)
})
}}function b(B,F,H,E){var D=B.getLayersByName("VectorLayer")[0];
var z;
var A=F.split(",");
if(E){var J=new Array(A.length);
J[0]=Ext.get("_"+A[0]).getValue();
J[1]=Ext.get("_"+A[1]).getValue();
J[2]=Ext.get("_"+A[2]).getValue();
J[3]=Ext.get("_"+A[3]).getValue();
z=OpenLayers.Bounds.fromArray(J);
if(E!=v){z=z.clone().transform(E,v)
}}else{var J=new Array(A.length);
J[0]=Ext.get(A[0]).getValue();
J[1]=Ext.get(A[1]).getValue();
J[2]=Ext.get(A[2]).getValue();
J[3]=Ext.get(A[3]).getValue();
z=OpenLayers.Bounds.fromArray(J);
var G=null;
var C=document.getElementsByName("proj_"+H);
for(i=0;
i<C.length;
i++){if(C[i].checked==true){G=C[i].value
}}if(G!=p){z.transform(new OpenLayers.Projection(G),E)
}else{var I=z.clone();
I.transform(E,c);
J[0]=I.left;
J[1]=I.bottom;
J[2]=I.right;
J[3]=I.top
}Ext.get("_"+A[0]).dom.value=z.left;
Ext.get("_"+A[1]).dom.value=z.bottom;
Ext.get("_"+A[2]).dom.value=z.right;
Ext.get("_"+A[3]).dom.value=z.top
}Ext.get(A[0]).dom.onkeyup();
Ext.get(A[1]).dom.onkeyup();
Ext.get(A[2]).dom.onkeyup();
Ext.get(A[3]).dom.onkeyup();
var K=new OpenLayers.Feature.Vector(z.toGeometry());
D.destroyFeatures();
D.addFeatures(K);
w(B,D);
h(F,H)
}function h(C,D){function E(H,N,J){var I=H.split(",");
var P=Ext.get("_"+I[0]).getValue();
var R=Ext.get("_"+I[1]).getValue();
var M=Ext.get("_"+I[2]).getValue();
var K=Ext.get("_"+I[3]).getValue();
var L=P!=""?P:"0";
var O=R!=""?R:"0";
var G=M!=""?M:"0";
var Q=K!=""?K:"0";
var F=OpenLayers.Bounds.fromString(L+","+O+","+G+","+Q);
if(!N.equals(GeoNetwork.map.ExtentMap.mainProj)){F.transform(x,N)
}if(P!=""){P=F.left.toFixed(J)+""
}Ext.get(I[0]).dom.value=P;
if(R!=""){R=F.bottom.toFixed(J)+""
}Ext.get(I[1]).dom.value=R;
if(M!=""){M=F.right.toFixed(J)+""
}Ext.get(I[2]).dom.value=M;
if(K!=""){K=F.top.toFixed(J)+""
}Ext.get(I[3]).dom.value=K
}var A=Ext.DomQuery.select("input.proj");
for(var z=0;
z<A.length;
++z){var B=A[z];
if(B.id.indexOf(D)!=-1){if(B.checked){E(C,new OpenLayers.Projection(B.value),e)
}Ext.get(B.id).on("click",function(){E(C,new OpenLayers.Projection(B.value),e)
})
}}}function a(z){var C="WKT";
var E,D;
if(z!=null){C=z.format||"WKT";
E=z.from;
D=z.to
}var B=new OpenLayers.Format[C]();
if(E!=null&&D!=null){B.internalProjection=E;
B.externalProjection=D
}if(!this.vectorLayer.features.length){return null
}var A=this.vectorLayer.features[this.vectorLayer.features.length-1];
return B.write(A)
}function j(B,A){if(B==""){return false
}var D="WKT";
var F,E;
if(A!=null){D=A.format||"WKT";
F=A.from;
E=A.to
}var z=new OpenLayers.Format[D]();
if(F!=null&&E!=null){z.externalProjection=F;
z.internalProjection=E
}B=B.replace(/\n/g,"");
var C=z.read(B);
if(!C){return false
}if(C.length){C=C[0]
}f.addFeatures(C);
if(A.zoomToFeatures){w(y,f)
}return true
}function w(D,A){var C=A.getDataExtent();
if(C&&!isNaN(C.left)){var B=C.getWidth()/2;
var z=C.getHeight()/2;
C.left-=B;
C.right+=B;
C.bottom-=z;
C.top+=z;
D.zoomToExtent(C)
}else{D.zoomToMaxExtent()
}}return{init:function(){},initMapDiv:function(){var J,B;
x=new OpenLayers.Projection(GeoNetwork.map.ExtentMap.mainProjCode);
c=GeoNetwork.map.ExtentMap.mainProj;
J=Ext.DomQuery.select(".extentViewer");
B=Ext.id;
for(var K=0;
K<J.length;
++K){var H=J[K];
s=H.getAttribute("target_polygon");
r=H.getAttribute("watched_bbox");
l=H.getAttribute("edit")=="true";
t=H.getAttribute("elt_ref");
g=H.getAttribute("mode");
var C=H.childNodes;
var F=[];
for(var G=0;
G<C.length;
G++){if(C[G].nodeType==1){F.push(C[G])
}}C=F;
if(C.length>1){continue
}var A;
A=Ext.id(H);
var z=n();
k[t]=z;
if(l){var I=[],E;
if(g=="bbox"){E=new OpenLayers.Control.DrawFeature(f,OpenLayers.Handler.RegularPolygon,{handlerOptions:{irregular:true,sides:4},featureAdded:function(M){var O=M.geometry.getBounds();
var N=O.clone().transform(x,v);
var L=this.watchedBbox.split(",");
Ext.get("_"+L[0]).dom.value=O.left;
Ext.get("_"+L[1]).dom.value=O.bottom;
Ext.get("_"+L[2]).dom.value=O.right;
Ext.get("_"+L[3]).dom.value=O.top;
h(this.watchedBbox,this.eltRef)
}.bind({watchedBbox:r,eltRef:t})});
I.push(new GeoExt.Action({map:z,control:E,text:"draw rectangle",pressed:false,allowDepress:true,toggleGroup:"tool",iconCls:"drawRectangle"}))
}if(g=="polygon"){E=new OpenLayers.Control.DrawFeature(f,OpenLayers.Handler.Polygon,{featureAdded:function(L){document.getElementById("_X"+this).value=u(L,p)
}.bind(s)});
I.push(new GeoExt.Action({map:z,control:E,text:"Draw polygon",pressed:false,allowDepress:true,toggleGroup:"tool",iconCls:"drawPolygon"}));
E=new OpenLayers.Control.DrawFeature(f,OpenLayers.Handler.RegularPolygon,{handlerOptions:{irregular:true,sides:60},featureAdded:function(L){document.getElementById("_X"+this).value=u(L,p)
}.bind(s)});
I.push(new GeoExt.Action({map:z,control:E,text:"Draw circle",pressed:false,allowDepress:true,toggleGroup:"tool",iconCls:"drawCircle"}))
}I.push({text:"Clear",iconCls:"clearPolygon",handler:function(){this.vectorLayer.destroyFeatures();
var M=document.getElementById("_X"+this.targetPolygon);
if(M!=null){M.value=""
}if(this.targetBbox!=""){var L=this.targetBbox.split(",");
Ext.get(L[0]).dom.value="";
Ext.get(L[1]).dom.value="";
Ext.get(L[2]).dom.value="";
Ext.get(L[3]).dom.value="";
Ext.get("_"+L[0]).dom.value="";
Ext.get("_"+L[1]).dom.value="";
Ext.get("_"+L[2]).dom.value="";
Ext.get("_"+L[3]).dom.value="";
$(L[0]).onkeyup();
$(L[1]).onkeyup();
$(L[2]).onkeyup();
$(L[3]).onkeyup()
}},scope:{vectorLayer:f,targetPolygon:s,targetBbox:r,eltRef:t}})
}var D=new GeoExt.MapPanel({renderTo:A,height:300,width:400,map:z,tbar:(l?I:null)});
if(C.length>0){j(C[0].innerHTML,{format:"WKT",zoomToFeatures:true,from:v,to:x})
}if(r!=""){h(r,t);
q(f,r,t,z)
}}}}
};
GeoNetwork.map.ExtentMap.prev_geometry=OpenLayers.Format.GML.Base.prototype.writers.feature._geometry;
OpenLayers.Format.GML.Base.prototype.writers.feature._geometry=function(c){if(c.CLASS_NAME=="GeoNetwork.map.ExtentMap.MultiPolygonReference"){var b=this.createElementNS(this.namespaces.gml,"gml:MultiPolygon");
var a=this.createElementNS(this.namespaces.gml,"gml:MultiPolygon");
a.setAttribute("gml:id",c.id);
b.appendChild(a);
return b
}else{return GeoNetwork.map.ExtentMap.prev_geometry.apply(this,arguments)
}};Ext.namespace("GeoNetwork","GeoNetwork.OGCUtil");
GeoNetwork.OGCUtil.ensureProperUrlEnd=function(a){if(a.indexOf("?")==-1){a+="?"
}else{var b=a.substring(a.length-1);
if(b!="&"&&b!="?"){a+="&"
}}return a
};
GeoNetwork.OGCUtil.reprojectMap=function(b,c,f){if(b.projection!=c.projCode){b.baseLayer.options.scales=b.scales;
var k=b.getProjectionObject();
b.projection=c.projCode;
if(c.getUnits()===null){b.units="degrees"
}else{b.units=c.getUnits()
}var e=null;
if(b.getControlsByClass("GeoNetwork.Control.CursorPos").length>0){e=b.getControlsByClass("GeoNetwork.Control.CursorPos")[0]
}if(b.units=="m"&&e!==null){e.numdigits=0
}else{if(b.units=="degrees"&&e!==null){e.numdigits=4
}}b.maxExtent=b.maxExtent.transform(k,c);
b.baseLayer.extent=b.maxExtent;
var a=b.getExtent().transform(k,c);
for(var g=0;
g<b.layers.length;
g++){var h=b.layers[g];
h.units=b.units;
h.projection=c;
h.maxExtent=b.maxExtent;
if(h.isBaseLayer){h.initResolutions()
}else{h.resolutions=b.baseLayer.resolutions;
h.minResolution=b.baseLayer.minResolution;
h.maxResolution=b.baseLayer.maxResolution
}if(h instanceof OpenLayers.Layer.Vector){for(var d=0;
d<h.features.length;
d++){var l=h.features[d];
if(l.geometry.projection!=b.projection){l.geometry.transform(new OpenLayers.Projection(l.geometry.projection),b.getProjectionObject());
l.geometry.projection=b.projection
}}}}if(!f){b.zoomToExtent(a)
}}};
GeoNetwork.OGCUtil.layerExistsInMap=function(d,j){var f=false;
for(var c=0,a=j.layers.length;
c<a;
c++){if(j.layers){var b=j.layers[c];
if(b.params){try{var h=b.params.LAYERS.split(",");
if((h.indexOf(d.params.LAYERS)!=-1)&&b.params.SERVICE==d.params.SERVICE&&b.url==d.url){f=b;
break
}}catch(g){}}}}return f
};Ext.namespace("GeoNetwork","GeoNetwork.CatalogueInterface");
GeoNetwork.CatalogueInterface=function(){var f;
var a;
var e;
var d=function(h){f=h
};
var g=function(h,p,l){var q=null;
for(var j=0,n=p.length;
j<n;
++j){var k=p[j];
try{var m=k.name.split(",");
if(m.indexOf(l.params.LAYERS)!=-1){q=k;
break
}}catch(o){}if(typeof(k.childLayers)!="undefined"){q=g(h,k.childLayers,l);
if(q!==null){break
}}}return q
};
var b=function(p){a.hide();
var j=new GeoNetwork.Format.WMSCapabilities();
var o=j.read(p.responseXML||p.responseText);
if(o.capability){var n=o.service.accessContraints;
if((n)&&(n.toLowerCase()!="none")&&(n!="-")){var k=new GeoNetwork.DisclaimerWindow({disclaimer:n});
k.show();
k=null
}if(f){for(var r=0,t=e.length;
r<t;
r++){var l=e[r][0];
var h=e[r][1];
var s=e[r][2];
var m=e[r][3];
var q=new OpenLayers.Layer.WMS(l,h,{layers:s,format:"image/png",transparent:"TRUE"},{queryable:true,singleTile:true,ratio:1,buffer:0,transitionEffect:"resize",metadata_id:m});
if(!GeoNetwork.OGCUtil.layerExistsInMap(q,f)){q.events.on({loadstart:function(){this.isLoading=true
}});
q.events.on({loadend:function(){this.isLoading=false
}});
var u=g(o,o.capability.layers,q);
if(u){q.queryable=u.queryable;
q.name=u.title||q.name;
q.llbbox=u.llbbox;
q.styles=u.styles;
q.dimensions=u.dimensions
}f.addLayer(q)
}}}}};
var c=function(h){a.hide();
Ext.MessageBox.alert(OpenLayers.i18n("loadLayer.error.title"),OpenLayers.i18n("loadLayer.error.message"))
};
return{init:function(h){d(h)
},addLayers:function(k){if(k.length===0){return
}var i=k[0][1];
if(k[0][2]==""){GeoNetwork.WindowManager.showWindow("addwms");
var h=Ext.getCmp(GeoNetwork.WindowManager.getWindow("addwms").browserPanel.id);
h.setURL(i);
return
}a=new Ext.LoadMask(f.div,{msg:OpenLayers.Lang.translate("loadLayer.loadingMessage")});
a.show();
e=k;
var n={service:"WMS",request:"GetCapabilities",version:"1.1.1"};
var m=OpenLayers.Util.getParameterString(n);
var l=(i.indexOf("?")>-1)?"&":"?";
i+=l+m;
var j=Ext.Ajax.request({url:i,method:"GET",success:b,failure:c,timeout:10000})
}}
};
GeoNetwork.CatalogueInterface=new GeoNetwork.CatalogueInterface();Ext.namespace("GeoNetwork","GeoNetwork.WMC");
GeoNetwork.WMC=function(){return{loadWmc:function(f,a){try{var e=f.layers;
for(var b=e.length-1;
b>0;
b--){if(!e[b].isBaseLayer){f.removeLayer(e[b])
}}Ext.getCmp("toctree").getSelectionModel().clearSelections();
var d=new OpenLayers.Format.WMC({layerOptions:{buffer:0}});
f=d.read(a,{map:f})
}catch(c){Ext.MessageBox.alert(OpenLayers.i18n("selectWMCFile.errorLoadingWMC"))
}},mergeWmc:function(d,a){try{var c=new OpenLayers.Format.WMC({layerOptions:{buffer:0}});
d=c.read(a,{map:d})
}catch(b){Ext.MessageBox.alert(OpenLayers.i18n("selectWMCFile.errorLoadingWMC"))
}},saveContext:function(b){var a=new OpenLayers.Format.WMC();
OpenLayers.Request.POST({url:"../../wmc/create.wmc",data:a.write(b),success:this.onSaveContextSuccess,failure:this.onSaveContextFailure})
},onSaveContextSuccess:function(a){var b=a.responseText;
var c=Ext.decode(b);
if(c.success){window.location=c.url
}else{this.onSaveContextFailure()
}},onSaveContextFailure:function(a,b){Ext.MessageBox.show({icon:Ext.MessageBox.ERROR,title:OpenLayers.i18n("saveWMCFile.windowTitle"),msg:OpenLayers.i18n("saveWMCFile.errorSaveWMC"),buttons:Ext.MessageBox.OK})
}}
};
GeoNetwork.WMCManager=new GeoNetwork.WMC();if(!window.GeoNetwork){window.GeoNetwork={}
}if(!GeoNetwork.Control){GeoNetwork.Control={}
}GeoNetwork.Control.ExtentBox=OpenLayers.Class(OpenLayers.Control,{type:OpenLayers.Control.TYPE_TOOL,minxelement:null,minyelement:null,maxxelement:null,maxyelement:null,EVENT_TYPES:["finishBox"],initialize:function(a){this.EVENT_TYPES=GeoNetwork.Control.ExtentBox.prototype.EVENT_TYPES.concat(OpenLayers.Control.prototype.EVENT_TYPES);
OpenLayers.Control.prototype.initialize.apply(this,arguments);
this.handler=new OpenLayers.Handler.RegularPolygon(this,{create:this.startBox,done:this.endBox},{irregular:true})
},setMap:function(a){OpenLayers.Control.prototype.setMap.apply(this,arguments)
},startBox:function(){this.getOrCreateLayer();
this.vectorLayer.destroyFeatures()
},endBox:function(){var d=this.handler.feature.geometry.getBounds();
var f=new OpenLayers.Feature.Vector(d.toGeometry(),null,this.vectorLayerStyle);
this.vectorLayer.addFeatures([f]);
this.vectorLayer.refresh();
var e=this.map.getProjectionObject();
var b=new OpenLayers.Projection("WGS84");
var c=new OpenLayers.LonLat(d.left,d.bottom).transform(e,b);
var a=new OpenLayers.LonLat(d.right,d.top).transform(e,b);
if(this.minxelement){this.minxelement.dom.value=c.lon.toFixed(4)
}if(this.maxxelement){this.maxxelement.dom.value=a.lon.toFixed(4)
}if(this.minyelement){this.minyelement.dom.value=c.lat.toFixed(4)
}if(this.maxyelement){this.maxyelement.dom.value=a.lat.toFixed(4)
}this.events.triggerEvent("finishBox",null)
},updateMap:function(){if((!this.minxelement)||(!this.maxxelement)||(!this.minxelement)||(!this.maxxelement)){return
}this.getOrCreateLayer();
var i=this.map.getProjectionObject();
var c=new OpenLayers.Projection("WGS84");
var h=new OpenLayers.LonLat(this.map.getExtent().left,this.map.getExtent().bottom).transform(i,c);
var e=new OpenLayers.LonLat(this.map.getExtent().right,this.map.getExtent().top).transform(i,c);
var d=parseFloat(this.minxelement.dom.value);
if(isNaN(d)){this.minxelement.dom.value=h.lon
}d=parseFloat(this.maxxelement.dom.value);
if(isNaN(d)){this.maxxelement.dom.value=e.lon
}d=parseFloat(this.minyelement.dom.value);
if(isNaN(d)){this.minyelement.dom.value=h.lat
}d=parseFloat(this.maxyelement.dom.value);
if(isNaN(d)){this.maxyelement.dom.value=e.lat
}this.minxelement.dom.value=parseFloat(this.minxelement.dom.value).toFixed(4);
this.maxxelement.dom.value=parseFloat(this.maxxelement.dom.value).toFixed(4);
this.minyelement.dom.value=parseFloat(this.minyelement.dom.value).toFixed(4);
this.maxyelement.dom.value=parseFloat(this.maxyelement.dom.value).toFixed(4);
this.vectorLayer.destroyFeatures();
var a=new OpenLayers.LonLat(this.minxelement.dom.value,this.minyelement.dom.value).transform(c,i);
var f=new OpenLayers.LonLat(this.maxxelement.dom.value,this.maxyelement.dom.value).transform(c,i);
var b=new OpenLayers.Bounds();
b.extend(a);
b.extend(f);
var g=new OpenLayers.Feature.Vector(b.toGeometry(),null,this.vectorLayerStyle);
this.vectorLayer.addFeatures([g]);
this.vectorLayer.refresh()
},getOrCreateLayer:function(){if(!this.vectorLayer){this.vectorLayer=this.vectorLayer||new OpenLayers.Layer.Vector("ExtentBox",{style:this.vectorLayerStyle});
this.map.addLayer(this.vectorLayer)
}return this.vectorLayer
},clear:function(){if(this.vectorLayer){this.vectorLayer.destroyFeatures()
}},zoomTo:function(){var e=this.map.getProjectionObject();
var b=new OpenLayers.Projection("WGS84");
var d=new OpenLayers.LonLat(this.minxelement.dom.value,this.minyelement.dom.value).transform(b,e);
var a=new OpenLayers.LonLat(this.maxxelement.dom.value,this.maxyelement.dom.value).transform(b,e);
var c=new OpenLayers.Bounds();
c.extend(d);
c.extend(a);
this.map.zoomToExtent(c)
},CLASS_NAME:"GeoNetwork.Control.ExtentBox"});if(!window.GeoNetwork){window.GeoNetwork={}
}if(!GeoNetwork.Control){GeoNetwork.Control={}
}GeoNetwork.Control.ZoomWheel=OpenLayers.Class(OpenLayers.Control,{wheelChange:OpenLayers.Control.Navigation.prototype.wheelChange,draw:function(){this.handler=new OpenLayers.Handler.MouseWheel(this,{up:OpenLayers.Control.Navigation.prototype.wheelUp,down:OpenLayers.Control.Navigation.prototype.wheelDown});
this.activate()
},CLASS_NAME:"GeoNetwork.Control.ZoomWheel"});if(!window.GeoNetwork){window.GeoNetwork={}
}if(!GeoNetwork.Control){GeoNetwork.Control={}
}GeoNetwork.Control.WMSGetFeatureInfo=OpenLayers.Class(OpenLayers.Control,{showMarker:true,markerIcon:OpenLayers.Marker.defaultIcon(),EVENT_TYPES:["featureinfostart","featureinfoend"],callbacks:null,markerLayer:null,location:null,feature:null,counter:null,format:new OpenLayers.Format.WMSGetFeatureInfo(),initialize:function(a){this.EVENT_TYPES=GeoNetwork.Control.WMSGetFeatureInfo.prototype.EVENT_TYPES.concat(OpenLayers.Control.prototype.EVENT_TYPES);
OpenLayers.Control.prototype.initialize.apply(this,[a]);
this.handler=new OpenLayers.Handler.Click(this,OpenLayers.Util.extend({click:this.click},this.callbacks))
},destroy:function(){if(this.markerLayer){this.markerLayer.destroy();
this.markerLayer=null;
if(this.feature){this.feature.destroy()
}}OpenLayers.Control.prototype.destroy.apply(this,arguments)
},performRequest:function(b,c,a){var d={REQUEST:"GetFeatureInfo",EXCEPTIONS:"application/vnd.ogc.se_xml",BBOX:this.map.getExtent().toBBOX(),X:a.xy.x,Y:a.xy.y,INFO_FORMAT:"application/vnd.ogc.gml",QUERY_LAYERS:c,LAYERS:c,WIDTH:this.map.size.w,HEIGHT:this.map.size.h,VERSION:"1.1.1",SERVICE:"WMS",SRS:this.map.getProjection()};
b=GeoNetwork.OGCUtil.ensureProperUrlEnd(b)+OpenLayers.Util.getParameterString(d);
OpenLayers.Request.GET({url:b,success:this.returnResponse,scope:this})
},click:function(b){this.events.triggerEvent("featureinfostart");
var e;
this.counter=0;
this.start();
this.location=this.map.getLonLatFromPixel(b.xy);
for(var d=0,a=this.map.layers.length;
d<a;
d++){e=this.map.layers[d];
if(e instanceof OpenLayers.Layer.WMS){if(e.visibility&&e.queryable){this.addToRequestQueue(e)
}}}for(var c=0;
c<this.queue.length;
c++){this.performRequest(this.queue[c].url,this.queue[c].layers.join(","),b)
}if(this.queue.length===0){OpenLayers.Element.removeClass(this.map.viewPortDiv,"olCursorWait")
}},getLayerTitle:function(b){for(var c=0,a=this.layerTitles.length;
c<a;
c++){if(this.layerTitles[c].name===b){return this.layerTitles[c].title
}}},addToRequestQueue:function(g){var f=g.params.LAYERS.split(",");
var e,h;
for(e=0,h=f.length;
e<h;
e++){this.layerTitles.push({name:f[e],title:g.name})
}var a=g.url;
var l=false;
var c=g.params.LAYERS;
for(e=0,h=this.queue.length;
e<h;
e++){var d=this.queue[e];
if(d.url===a){l=true;
f=c.split(",");
for(var b=0,k=f.length;
b<k;
b++){d.layers.push(f[b])
}}}if(!l){f=c.split(",");
this.queue.push({url:a,layers:f})
}},returnResponse:function(a){this.counter++;
this.info(a);
if(this.counter===this.queue.length){this.end()
}},start:function(){OpenLayers.Element.addClass(this.map.viewPortDiv,"olCursorWait");
this.featurelist=[];
this.queue=[];
this.layerTitles=[]
},end:function(){if(this.showMarker){var b=-1;
for(var e=0,a=this.map.layers.length;
e<a;
e++){var d=this.map.layers[e];
if(d!=this.markerLayer){b=Math.max(this.map.getLayerIndex(this.map.layers[e]),b)
}}if(this.map.getLayerIndex(this.markerLayer)<b){this.map.setLayerIndex(this.markerLayer,b+1)
}this.feature=new OpenLayers.Feature(this.markerLayer,this.location,{icon:this.markerIcon});
var c=this.feature.createMarker();
this.markerLayer.clearMarkers();
this.markerLayer.addMarker(c)
}this.events.triggerEvent("featureinfoend",{featurelist:this.featurelist});
OpenLayers.Element.removeClass(this.map.viewPortDiv,"olCursorWait")
},info:function(b){var e=this.format.read(b.responseXML||b.responseText);
for(var d=0,a=e.length;
d<a;
d++){var c=e[d];
var f={title:this.getLayerTitle(c.type),features:[c]};
this.featurelist.push(f)
}},setMap:function(a){OpenLayers.Control.prototype.setMap.apply(this,arguments);
if(this.showMarker){this.markerLayer=new OpenLayers.Layer.Markers("featureinfo",{displayInLayerSwitcher:false});
this.map.addLayer(this.markerLayer)
}},deactivate:function(){if(this.markerLayer){this.markerLayer.clearMarkers()
}return OpenLayers.Control.prototype.deactivate.apply(this,arguments)
},CLASS_NAME:"GeoNetwork.Control.WMSGetFeatureInfo"});if(!window.GeoNetwork){window.GeoNetwork={}
}if(!GeoNetwork.Format){GeoNetwork.Format={}
}GeoNetwork.Format.WMSCapabilities=OpenLayers.Class(OpenLayers.Format.XML,{defaultVersion:"1.1.1",version:null,initialize:function(a){OpenLayers.Format.XML.prototype.initialize.apply(this,[a]);
this.options=a
},read:function(e){if(typeof e=="string"){e=OpenLayers.Format.XML.prototype.read.apply(this,[e])
}var c=e.documentElement;
var b=this.version;
if(!b){b=c.getAttribute("version");
if(!b){b=this.defaultVersion
}}var d=GeoNetwork.Format["WMSCapabilities_"+b.replace(/\./g,"_")];
if(!d){throw"Can't find a WMS capabilities parser for version "+b
}var f=new d(this.options);
var a=f.read(e);
a.version=b;
return a
},CLASS_NAME:"GeoNetwork.Format.WMSCapabilities"});GeoNetwork.Format.WMSCapabilities_1_1_1=OpenLayers.Class(GeoNetwork.Format.WMSCapabilities,{initialize:function(a){GeoNetwork.Format.WMSCapabilities.prototype.initialize.apply(this,[a])
},read:function(c){if(typeof c=="string"){c=OpenLayers.Format.XML.prototype.read.apply(this,[c])
}var a={};
var b=c.documentElement;
this.runChildNodes(a,b);
return a
},runChildNodes:function(f,e){var c=e.childNodes;
var b,d;
for(var a=0;
a<c.length;
++a){b=c[a];
if(b.nodeType==1){d=this["process"+b.nodeName];
if(d){d.apply(this,[f,b])
}}}},processRequest:function(c,b){var a={};
this.runChildNodes(a,b);
c.request=a
},processGetMap:function(c,b){var a={formats:[]};
this.runChildNodes(a,b);
c.getmap=a
},processDCPType:function(c,b){var a=b.getElementsByTagName("OnlineResource");
if(a.length>0){this.processOnlineResource(c,a[0])
}},processCapability:function(a,c){var b={layers:[]};
this.runChildNodes(b,c);
a.capability=b
},processService:function(b,c){var a={};
this.runChildNodes(a,c);
b.service=a
},processLayer:function(b,e,f){var h={styles:[],dimensions:{}};
h.queryable=(e.getAttribute("queryable")=="1");
if(f){if(!f.childLayers){f.childLayers=[]
}f.childLayers.push(h);
h.styles=h.styles.concat(f.styles)
}var c=e.childNodes;
var a,j,d;
for(var g=0;
g<c.length;
++g){a=c[g];
j=a.nodeName;
d=this["process"+a.nodeName];
if(d){if(j=="Layer"){d.apply(this,[b,a,h])
}else{d.apply(this,[h,a])
}}}if(h.childLayers){b.layers=[];
b.layers.push(h)
}},processName:function(c,b){var a=this.getChildValue(b);
if(a){c.name=a
}},processTitle:function(b,a){var c=this.getChildValue(a);
if(c){b.title=c
}},processAbstract:function(c,b){var a=this.getChildValue(b);
if(a){c.abstrack=a
}},processLatLonBoundingBox:function(a,b){a.llbbox=[parseFloat(b.getAttribute("minx")),parseFloat(b.getAttribute("miny")),parseFloat(b.getAttribute("maxx")),parseFloat(b.getAttribute("maxy"))]
},processStyle:function(a,c){var b={};
this.runChildNodes(b,c);
a.styles.push(b)
},processDimension:function(b,c){var a=c.getAttribute("name").toLowerCase();
if(a in b.dimensions){return
}var d={name:a,units:c.getAttribute("units"),unitsymbol:c.getAttribute("unitSymbol")};
b.dimensions[d.name]=d
},processExtent:function(c,e){var b=e.getAttribute("name").toLowerCase();
if(b in c.dimensions){var d=c.dimensions[b];
d.nearestVal=e.getAttribute("nearestValue")==="1";
d.multipleVal=e.getAttribute("multipleValues")==="1";
d.current=e.getAttribute("current")==="1";
d["default"]=e.getAttribute("default")||"";
var a=this.getChildValue(e);
d.values=a.split(",")
}},processLegendURL:function(c,d){var b={width:d.getAttribute("width"),height:d.getAttribute("height")};
var a=d.getElementsByTagName("OnlineResource");
if(a.length>0){this.processOnlineResource(b,a[0])
}c.legend=b
},processMetadataURL:function(b,c){var d={};
var a=c.getElementsByTagName("OnlineResource");
if(a.length>0){this.processOnlineResource(d,a[0])
}b.metadataURL=d.href
},calculateScale:function(a){return Math.round((OpenLayers.DOTS_PER_INCH*39.3701*a)/Math.sqrt(2))
},processScaleHint:function(a,b){a.minScale=this.calculateScale(b.getAttribute("max"));
if(a.minScale===0){a.minScale=null
}a.maxScale=this.calculateScale(b.getAttribute("min"))
},processOnlineResource:function(b,a){b.href=this.getAttributeNS(a,"http://www.w3.org/1999/xlink","href")
},processAccessConstraints:function(b,a){b.accessContraints=a.textContent
},processBoundingBox:function(h,f){if(h.llbbox){return
}var g=((f.getAttribute("minx"))&&(f.getAttribute("miny"))&&(f.getAttribute("maxx"))&&(f.getAttribute("maxy")));
if(g){var d=f.getAttribute("SRS");
var e=new OpenLayers.Projection(d);
var c=new OpenLayers.Projection("WGS84");
var a=new OpenLayers.LonLat(parseFloat(f.getAttribute("minx")),parseFloat(f.getAttribute("miny"))).transform(e,c);
var b=new OpenLayers.LonLat(parseFloat(f.getAttribute("maxx")),parseFloat(f.getAttribute("maxy"))).transform(e,c);
h.llbbox=[parseFloat(a.lon),parseFloat(a.lat),parseFloat(b.lon),parseFloat(b.lat)]
}},CLASS_NAME:"GeoNetwork.Format.WMSCapabilities_1_1_1"});Ext.namespace("GeoNetwork","GeoNetwork.lang");
GeoNetwork.lang.en={featureInfoTooltipTitle:"Feature info",featureInfoTooltipText:"Click in the map to get feature info from all visible layers.",zoomToMaxExtentTooltipTitle:"Full extent",zoomToMaxExtentTooltipText:"Use this button to go to the full extent of the map.",zoominTooltipTitle:"Zoom in",zoominTooltipText:"Draw a box in the map to zoom in. You can also click in the map and map will zoom in by a factor of 2.",zoomoutTooltipTitle:"Zoom out",zoomoutTooltipText:"Click in the map or draw a box to zoom out.",dragTooltipTitle:"Pan map",dragTooltipText:"Press the left mouse button to drag the map.",previousTooltipTitle:"Previous map extent",previosTooltipText:"Click this button to go back to the previous map extent",nextTooltipTitle:"Next map extent",nextTooltipText:"Click this button to go to the next map extent",featureInfoTitle:"Feature info",layerManagerTabTitle:"Layer management",legendTabTitle:"Legend",scaleTitle:"Scale",xTitle:"X",yTitle:"Y",projectionTitle:"Projection",FeatureInfoNotQueryable:"There is no queryable map layer",WMSBrowserTab1:"Select",WMSBrowserTab3:"External",WMSBrowserPreviewTitle:"Preview layer",WMSBrowserAddButton:"Add",WMSBrowserDuplicateMsg:"Layer is already in the map",WMSBrowserPreviewWaitMsg:"Retrieving preview image",WMSBrowserConnectButton:"Connect",WMSBrowserConnectError:"There was an error connecting to the Web Map Service, please check the URL.",infoTitle:"Information","mf.print.mapTitle":"Title","mf.print.comment":"Comment","mf.print.dpi":"Resolution","mf.print.scale":"Scale","mf.print.rotation":"Angle","mf.print.resetPos":"Reset","mf.print.print":"Print","mf.print.generatingPDF":"Generating PDF","mf.print.unableToPrint":"It was not possible to print, try later","mf.error":"Error",selectExtentTooltipTitle:"Select extent",selectExtentTooltipText:"Select extent for catalog searches",printTooltipTitle:"Print",printTooltipText:"Print the current map",savewmcTooltipTitle:"Save web map context",savewmcTooltipText:"Saves web map context for current map","saveWMCFile.windowTitle":"Save Web Map Context","saveWMCFile.errorSaveWMC":"Could not save Web Map Context",loadwmcTooltipTitle:"Load web map context",loadwmcTooltipText:"Loads web map context in current map","selectWMCFile.windowTitle":"Load Web Map Context",selectWMCFile:"Select Web Map Context file","selectWMCFile.waitLoadingWMC":"Loading Web Map Context","selectWMCFile.loadButtonText":"Load","selectWMCFile.mergeButtonText":"Merge","selectWMCFile.errorLoadingWMC":"Could not load Web Map Context",opacityButtonText:"Opacity",opacityWindowTitle:"Opacity",metadataButtonText:"Metadata",removeButtonText:"Remove",addWMSButtonText:"Add WMS",addWMSWindowTitle:"Add layer from a WMS service","layerInfoPanel.windowTitle":"Layer metadata","layerInfoPanel.titleField":"Title","layerInfoPanel.descriptionField":"Description","layerInfoPanel.queryableField":"Queryable",layerList:"Layer list",baseLayerList:"Base layers","metadataResults.buttonText":"Metadata results","metadataResults.tooltipTitle":"Metadata results","metadataResults.tooltipText":"Go to metadata results page","metadataResults.alertTitle":"Metadata results","metadataResults.alertText":"No metadata results to show","featureInfoWindow.windowTitle":"Feature info","mf.information":"Print","mf.print.pdfReady":"PDF ready","loadLayer.loadingMessage":"Loading layer","loadLayer.error.title":"Load layer","loadLayer.error.message":"The layer could not be loaded","disclaimer.windowTitle":"Disclaimer","disclaimer.loading":"Loading...","disclaimer.buttonClose":"Close",gazetteerAddressField1Label:"Postcode",gazetteerAddressField2Label:"Street",gazetteerAddressField3Label:"Place",gazetteerAddressField4Label:"'Gemeente'",gazetteerAddressField5Label:"House number",searchOptionButton:"Search",GazetteerWindowTitle:"Search for address",searchResults:"Search results",errorTitle:"Error",SearchOptionLoadFailureMsg:"No results available. \n The service may be unavailable. \n",gazetteerTooltipTitle:"Search based on an address",gazetteerTooltipText:"Search for a location, based on postcode, street name, 'gemeente' or place.",SearchOptionNoResults:"No results found",zoomlayerTooltipTitle:"Zoom to layer",zoomlayerTooltipText:"Use this button to go to the full extent of the selected layer.","zoomlayer.selectLayerTitle":"Zoom to layer","zoomlayer.selectLayerText":"No layer selected",layerStylesWindowTitle:"Layer styles",layerStylesPreviewTitle:"Preview legend",selectStyleButton:"Select style",WMSTimeWindowTitle:"WMS Time",WMSTimePositionTitle:"Pick a time position",WMSTimeMovieTitle:"Play animation",wmsTimeUpdateButtonText:"Apply",WMSTimeAnimationCheckbox:"Play a movie loop spanning the last ${steps} time positions"};
OpenLayers.Util.extend(OpenLayers.Lang.en,GeoNetwork.lang.en);Ext.namespace("GeoNetwork","GeoNetwork.tree");
GeoNetwork.tree.WMSListGenerator=function(a){Ext.apply(this,a);
if(this.node&&this.wmsStore){this.createWMSList()
}};
GeoNetwork.tree.WMSListGenerator.prototype={node:null,wmsStore:null,click:null,scope:null,createWMSList:function(){this.wmsStore.each(this.appendRecord,this)
},appendRecord:function(a){var b=new Ext.tree.TreeNode({url:a.get("url"),text:a.get("title"),cls:"folder",leaf:false});
b.appendChild(new Ext.tree.TreeNode({text:"",dummy:true}));
b.addListener("beforeexpand",this.addNodesFromWMS,this);
this.node.appendChild(b)
},replaceNode:function(a){this.currentNode.parentNode.replaceChild(a,this.currentNode);
a.ui.afterLoad();
a.expand()
},addNodesFromWMS:function(b){if(b.firstChild&&b.firstChild.attributes.dummy){b.removeChild(b.firstChild);
b.ui.beforeLoad();
this.scope.currentNode=b;
var a=new GeoNetwork.tree.WMSTreeGenerator({click:this.click,callback:this.replaceNode,scope:this.scope});
a.loadWMS(b.attributes.url)
}}};Ext.namespace("GeoNetwork","GeoNetwork.tree");
GeoNetwork.tree.WMSTreeGenerator=function(a){Ext.apply(this,a)
};
GeoNetwork.tree.WMSTreeGenerator.prototype={layerParams:{format:"image/png",transparent:"TRUE"},layerOptions:{ratio:1,singleTile:true,isBaseLayer:false},click:null,callback:null,scope:null,loadWMS:function(a){var e={service:"WMS",request:"GetCapabilities",version:"1.1.1"};
var d=OpenLayers.Util.getParameterString(e);
var c=(a.indexOf("?")>-1)?"&":"?";
a+=c+d;
var b=OpenLayers.Request.GET({url:a,failure:this.processFailure,success:this.processSuccess,disableCaching:false,scope:this})
},processSuccess:function(b){if(!this.parser){this.parser=new GeoNetwork.Format.WMSCapabilities()
}var f=this.parser.read(b.responseXML||b.responseText);
var e;
if(f.capability){for(var d=0,a=f.capability.layers.length;
d<a;
++d){var c=f.capability.layers[d];
e=this.addLayer(c,f.capability.request.getmap.href,null);
this.processLayer(c,f.capability.request.getmap.href,e)
}}Ext.callback(this.callback,this.scope,[e,f])
},processFailure:function(a){Ext.callback(this.callback,this.scope,null)
},createWMSLayer:function(b,a){return new OpenLayers.Layer.WMS(b.title,a,OpenLayers.Util.extend({layers:b.name},this.layerParams),OpenLayers.Util.extend({minScale:b.minScale,queryable:b.queryable,maxScale:b.maxScale,metadataURL:b.metadataURL,dimensions:b.dimensions,styles:b.styles,llbbox:b.llbbox},this.layerOptions))
},addLayer:function(c,b,a){var f=null;
if(c.name){f=this.createWMSLayer(c,b);
if(c.styles&&c.styles.length>0){var d=c.styles[0];
if(d.legend&&d.legend.href){f.legendURL=d.legend.href
}}}var e=new Ext.tree.TreeNode({wmsLayer:f,text:c.title});
e.addListener("click",this.click,this.scope);
if(a){a.appendChild(e)
}return e
},processLayer:function(b,a,c){Ext.each(b.childLayers,function(e){var d=this.addLayer(e,a,c);
if(e.childLayers){this.processLayer(e,a,d)
}},this)
}};Ext.namespace("GeoNetwork","GeoNetwork.wms");
GeoNetwork.wms.BrowserPanel=function(a){Ext.apply(this,a);
GeoNetwork.wms.BrowserPanel.superclass.constructor.call(this)
};
GeoNetwork.wms.BrowserPanel.ADDWMS=0;
GeoNetwork.wms.BrowserPanel.WMSLIST=1;
Ext.extend(GeoNetwork.wms.BrowserPanel,Ext.Panel,{previewPanel:null,treePanel:null,map:null,previewCenterPoint:null,mode:GeoNetwork.wms.BrowserPanel.WMSLIST,wmsStore:null,urlField:null,searchResultsGrid:null,searchField:null,parseBt:null,typeRadio:null,defaultConfig:{border:false,frame:false,layout:"border"},initComponent:function(d){Ext.apply(this,d);
Ext.applyIf(this,this.defaultConfig);
GeoNetwork.wms.BrowserPanel.superclass.initComponent.call(this);
this.previewPanel=new GeoNetwork.wms.PreviewPanel({map:this.map,previewCenterPoint:this.previewCenterPoint});
var c;
this.treePanel=new Ext.tree.TreePanel({rootVisible:false,autoScroll:true,autoHeight:true});
c=new Ext.tree.TreeNode({text:"",draggable:false,cls:"folder"});
this.treePanel.setRootNode(c);
var b=[];
if(this.mode==GeoNetwork.wms.BrowserPanel.ADDWMS){this.createForm();
b.push(this.form)
}b.push(this.treePanel);
var a={autoScroll:true,region:"center",items:b,split:true,width:300,minWidth:300,border:false};
var e={region:"east",border:false,items:[this.previewPanel],split:true,plain:true,cls:"popup-variant1",width:250,maxSize:250,minSize:250};
this.add(a);
this.add(e);
if(this.mode==GeoNetwork.wms.BrowserPanel.WMSLIST){this.treeGen=new GeoNetwork.tree.WMSListGenerator({click:this.nodeClick,scope:this,node:this.treePanel.getRootNode(),wmsStore:this.wmsStore})
}else{if(this.mode==GeoNetwork.wms.BrowserPanel.ADDWMS){new GeoNetwork.tree.WMSListGenerator({click:this.nodeClick,scope:this,node:this.treePanel.getRootNode(),wmsStore:this.wmsStore});
this.treeGen=new GeoNetwork.tree.WMSTreeGenerator({click:this.nodeClick,callback:this.showTree,scope:this})
}}this.addButton({text:OpenLayers.i18n("WMSBrowserAddButton"),iconCls:"addLayerIcon",width:150},this.addLayerToMap,this);
this.doLayout()
},createForm:function(){this.form=new Ext.form.FormPanel({labelWidth:15,id:"serviceSearchForm"});
this.typeRadio=new Ext.form.RadioGroup({items:[{name:"addWmsType",fieldLabel:"Search WMS",labelSeparator:"",inputValue:0,checked:true},{name:"addWmsType",labelSeparator:"",fieldLabel:" or add by URL",inputValue:1}],listeners:{change:function(e,g){var f=(g.getGroupValue()=="0");
this.urlField.setVisible(!f);
this.searchField.setVisible(f);
this.searchResultsGrid.setVisible(f)
},scope:this}});
this.form.add(this.typeRadio);
this.urlField=new Ext.form.TextField({name:"wmsurl",hideLabel:true,hidden:true,emptyText:"WMS server URL ...",width:250,autoHeight:true});
this.form.add(this.urlField);
var d=GeoNetwork.data.MetadataResultsStore();
var a=GeoNetwork.data.MetadataSummaryStore();
this.searchField=new GeoNetwork.form.SearchField({name:"E_any",hideLabel:true,width:250,minWidth:250,store:d,triggerAction:function(e){e.search("serviceSearchForm",null,null,1,true,d,a)
},scope:catalogue});
var c=new Ext.grid.CheckboxSelectionModel({singleSelect:this.singleSelect,header:"",});
this.searchResultsGrid=new Ext.grid.GridPanel({layout:"fit",height:80,border:false,store:d,columns:[c,{id:"title",header:"Title",dataIndex:"title"}],sm:c,autoExpandColumn:"title",listeners:{rowclick:function(f,i,h){var g=f.getStore().getAt(i).data;
this.setValue(g.links[0].href)
},scope:this.urlField}});
var b=new Ext.form.TextField({inputType:"hidden",name:"E_serviceType",value:"OGC:WMS"});
this.form.add(this.searchField,b,this.searchResultsGrid);
this.parseBt=new Ext.Button({id:"parse",text:OpenLayers.i18n("WMSBrowserConnectButton"),iconCls:"connectIcon",width:150});
this.form.addButton(this.parseBt,this.getWMSCaps,this)
},showTree:function(c,b){if(!c){Ext.MessageBox.alert(OpenLayers.i18n("errorTitle"),OpenLayers.i18n("WMSBrowserConnectError"));
this.body.dom.style.cursor="default"
}var d=b.service.accessContraints;
if((d)&&(d.toLowerCase()!="none")&&(d!="-")){var e=new GeoNetwork.DisclaimerWindow({disclaimer:d});
e.show();
e=null
}var a=this.treePanel.getRootNode();
if(c){this.treePanel.getRootNode().appendChild(c)
}this.treePanel.show();
this.body.dom.style.cursor="default"
},getWMSCaps:function(b){var a=this.urlField.getValue();
a=a.replace(/^\s+|\s+$/g,"");
if(a!=""){this.body.dom.style.cursor="wait";
this.treeGen.loadWMS(a)
}},setURL:function(a){var a=this.urlField.setValue(a);
this.typeRadio.setValue(1);
this.getWMSCaps(this.parseBt)
},nodeClick:function(a){this.previewPanel.showPreview(a.attributes.wmsLayer)
},addLayerToMap:function(){if(this.previewPanel.currentLayer){var a=GeoNetwork.OGCUtil.layerExistsInMap(this.previewPanel.currentLayer,this.map);
if(!a){this.previewPanel.currentLayer.events.on({loadstart:function(){this.isLoading=true
}});
this.previewPanel.currentLayer.events.on({loadend:function(){this.isLoading=false
}});
this.map.addLayers([this.previewPanel.currentLayer])
}else{Ext.MessageBox.alert(OpenLayers.i18n("infoTitle"),OpenLayers.i18n("WMSBrowserDuplicateMsg"))
}}}});
Ext.reg("gn_wmsbrowserpanel",GeoNetwork.wms.BrowserPanel);Ext.namespace("GeoNetwork","GeoNetwork.wms");
GeoNetwork.wms.LayerInfoPanel=function(a){Ext.apply(this,a);
GeoNetwork.wms.LayerInfoPanel.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.wms.LayerInfoPanel,Ext.Panel,{previewPanel:null,map:null,previewCenterPoint:null,onlineresource:null,layer:null,initComponent:function(){GeoNetwork.wms.LayerInfoPanel.superclass.initComponent.call(this);
this.layout="border";
this.border=false;
this.previewPanel=new GeoNetwork.wms.PreviewPanel({map:this.map,previewCenterPoint:this.previewCenterPoint});
this.store=new Ext.data.SimpleStore({reader:new Ext.data.ArrayReader({},[{name:"title",type:"string"},{name:"field",type:"string"}]),fields:["title","field"]});
this.gridPanel=new Ext.grid.GridPanel({title:"",store:this.store,autoScroll:true,hideHeaders:false,columns:[]});
this.layerInfo=new GeoNetwork.wms.WMSLayerInfo({callback:this._showLayerInfo,scope:this});
var a={region:"center",layout:"fit",items:[this.gridPanel],split:true,width:300,minWidth:300};
var b={region:"east",items:[this.previewPanel],split:true,plain:true,cls:"popup-variant1",width:250,maxSize:250,minSize:250};
this.add(a);
this.add(b);
this.doLayout()
},showLayerInfo:function(){this.previewPanel.showPreview(this.layer);
this.layerInfo.loadWMS(this.onlineresource,this.layer)
},_showLayerInfo:function(a){if(!a){Ext.MessageBox.alert(OpenLayers.i18n("errorTitle"),OpenLayers.i18n("WMSBrowserConnectError"));
this.body.dom.style.cursor="default"
}else{var b=[[OpenLayers.i18n("layerInfoPanel.titleField"),a.title||a.name],[OpenLayers.i18n("layerInfoPanel.descriptionField"),a.description],[OpenLayers.i18n("layerInfoPanel.queryableField"),a.queryable]];
this.gridPanel.reconfigure(this.store,new Ext.grid.ColumnModel([{header:"Field",dataIndex:"title",sortable:true},{id:"value",header:"Value",dataIndex:"field",sortable:true}]));
this.gridPanel.autoExpandColumn="value";
this.gridPanel.getStore().loadData(b)
}this.body.dom.style.cursor="default"
}});
Ext.reg("gn_infolayerpanel",GeoNetwork.wms.LayerInfoPanel);Ext.namespace("GeoNetwork","GeoNetwork.wms");
GeoNetwork.wms.LayerStylesPanel=function(a){Ext.apply(this,a);
GeoNetwork.wms.LayerStylesPanel.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.wms.LayerStylesPanel,Ext.Panel,{previewPanel:null,map:null,layer:null,selectedStyle:null,initComponent:function(){GeoNetwork.wms.LayerStylesPanel.superclass.initComponent.call(this);
this.layout="border";
this.border=false;
this.previewPanel=new GeoNetwork.wms.PreviewPanel({map:this.map,title:OpenLayers.i18n("layerStylesPreviewTitle")});
this.store=new Ext.data.SimpleStore({reader:new Ext.data.ArrayReader({},[{name:"name",type:"string"},{name:"title",type:"string"},{name:"legendUrl",type:"string"}]),fields:["name","title","legendUrl"]});
this.gridPanel=new Ext.grid.GridPanel({title:"",border:false,autoScroll:true,store:this.store,hideHeaders:false,columns:[{header:"Style",width:120,dataIndex:"name",sortable:false},{id:"description",header:"Description",width:180,dataIndex:"title",sortable:false}],autoExpandColumn:"description"});
this.gridPanel.on("rowclick",this._selectStyle,this);
var a={region:"center",layout:"fit",items:[this.gridPanel],split:true,width:300,minWidth:300};
var b={region:"east",items:[this.previewPanel],split:true,plain:true,cls:"popup-variant1",width:250,maxSize:250,minSize:250};
this.add(a);
this.add(b);
this.doLayout()
},showLayerStyles:function(c){var e=[];
for(var b=0;
b<c.styles.length;
b++){var a="";
if(c.styles[b].legend){a=c.styles[b].legend.href
}var d=[c.styles[b].name,c.styles[b].title,a];
e.push(d)
}this.gridPanel.getStore().loadData(e)
},_selectStyle:function(b,g,c){var d=b.store.getAt(g);
this.selectedStyle=d.get("name");
b.getView().focusEl.focus();
var a=d.get("legendUrl");
if(a==""){return
}a=unescape(a);
this.selectedStyleLegendUrl=a;
var f=d.get("legendUrl")+"&style="+this.selectedStyle;
this.previewPanel.showPreviewLegend(unescape(f))
}});
Ext.reg("gn_layerstylespanel",GeoNetwork.wms.LayerStylesPanel);Ext.namespace("GeoNetwork","GeoNetwork.wms");
GeoNetwork.wms.PreviewPanel=function(a){Ext.apply(this,a);
GeoNetwork.wms.PreviewPanel.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.wms.PreviewPanel,Ext.Panel,{title:OpenLayers.i18n("WMSBrowserPreviewTitle"),baseCls:"x-plain",cls:"x-panel-title-variant1",imgCls:"preview-image",width:250,height:250,currentLayer:null,initComponent:function(){GeoNetwork.wms.PreviewPanel.superclass.initComponent.call(this);
this.image=new Ext.BoxComponent({autoEl:{tag:"img","class":this.imgCls,src:Ext.BLANK_IMAGE_URL,width:this.width,height:this.height}});
this.add(this.image)
},hideMask:function(){if(this.mask){this.mask.hide()
}},showMask:function(){if(!this.mask){this.mask=new Ext.LoadMask(this.getEl(),{msg:OpenLayers.i18n("WMSBrowserPreviewWaitMsg")});
Ext.EventManager.addListener(this.image.getEl(),"load",this.hideMask,this);
Ext.EventManager.addListener(this.image.getEl(),"error",this.hideMask,this)
}this.mask.show()
},calculateBBOX:function(f){var j;
if(f.llbbox){if(this.map.getProjection()!=="EPSG:4326"){var b=OpenLayers.Bounds.fromArray(f.llbbox);
b=b.transform(new OpenLayers.Projection("EPSG:4326"),this.map.getProjectionObject());
j=b.toArray()
}else{j=f.llbbox
}}else{j=this.map.maxExtent.toArray()
}var a=OpenLayers.Bounds.fromArray(j).getCenterLonLat();
if(f.minScale>0){var c;
if(f.maxScale>0){c=(f.maxScale+f.minScale)/2
}else{c=0.9*f.minScale
}var g=OpenLayers.Util.getResolutionFromScale(c,this.map.units);
var e=Math.round(g*this.width);
var d=Math.round(g*this.height);
var i=a.lon;
var h=a.lat;
if(e!==0&&d!==0){j=[i-0.5*e,h-0.5*d,i+0.5*e,h+0.5*d]
}}return j.join(",")
},showPreview:function(b){if(!b){return
}this.showMask();
var c=b.map;
if(c===null){b.map=this.map
}var a=b.getFullRequestString({BBOX:this.calculateBBOX(b),WIDTH:this.width,HEIGHT:this.height});
if(c===null){b.map=c
}this.currentLayer=b;
this.image.getEl().dom.src=a
},showPreviewLegend:function(a){this.remove(this.image);
this.image=null;
this.image=new Ext.BoxComponent({autoEl:{tag:"img","class":this.imgCls,src:a}});
this.add(this.image);
this.doLayout()
}});
Ext.reg("gn_wmspreview",GeoNetwork.wms.PreviewPanel);Ext.namespace("GeoNetwork","GeoNetwork.wms");
GeoNetwork.wms.WMSLayerInfo=function(a){Ext.apply(this,a)
};
GeoNetwork.wms.WMSLayerInfo.prototype={layerParams:{format:"image/png",transparent:"TRUE"},layerOptions:{ratio:1,singleTile:true,isBaseLayer:false},callback:null,scope:null,layer:null,loadWMS:function(b,a){this.layer=a;
var f={service:"WMS",request:"GetCapabilities",version:"1.1.1"};
var e=OpenLayers.Util.getParameterString(f);
var d=(b.indexOf("?")>-1)?"&":"?";
b+=d+e;
var c=OpenLayers.Request.GET({url:b,failure:this.processFailure,success:this.processSuccess,timeout:10000,scope:this})
},processSuccess:function(a){if(!this.parser){this.parser=new GeoNetwork.Format.WMSCapabilities()
}var c=this.parser.read(a.responseXML||a.responseText);
var b;
if(c.capability){b=this.processLayers(c,c.capability.layers)
}Ext.callback(this.callback,this.scope,[b,this.layer])
},processFailure:function(a){Ext.callback(this.callback,this.scope,[null,this.layer])
},createWMSLayer:function(b,a){return new OpenLayers.Layer.WMS(b.title,a,OpenLayers.Util.extend({layers:b.name},this.layerParams),OpenLayers.Util.extend({minScale:b.minScale,queryable:b.queryable,maxScale:b.maxScale,description:b.abstrack,metadataURL:b.metadataURL,llbbox:b.llbbox},this.layerOptions))
},processLayers:function(g,j){var d=null;
for(var f=0,a=j.length;
f<a;
++f){var b=j[f];
try{var c=b.name.split(",");
if(c.indexOf(this.layer.params.LAYERS)!=-1){d=this.createWMSLayer(b,g.service.href);
break
}}catch(h){}if(typeof(b.childLayers)!="undefined"){d=this.processLayers(g,b.childLayers);
if(d!=null){break
}}}return d
}};Ext.namespace("GeoNetwork");
GeoNetwork.FeatureInfoPanel=function(a){Ext.apply(this,a);
GeoNetwork.FeatureInfoPanel.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.FeatureInfoPanel,Ext.Panel,{features:null,treePanel:null,infoPanel:null,initComponent:function(){GeoNetwork.FeatureInfoPanel.superclass.initComponent.call(this);
this.layout="border";
this.treePanel=new Ext.tree.TreePanel({rootVisible:true,autoScroll:true});
var b=new Ext.tree.TreeNode({text:OpenLayers.i18n("featureInfoTitle"),draggable:false,expanded:true,cls:"folder"});
this.treePanel.setRootNode(b);
var a={region:"center",items:[this.treePanel],split:true,minWidth:100};
this.infoPanel=new Ext.Panel();
this.infoPanel.on("render",function(){if(this.features){this.showFeatures(this.features)
}},this);
var c={region:"east",items:[this.infoPanel],split:true,plain:true,cls:"popup-variant1",width:400,autoScroll:true};
this.add(a);
this.add(c);
this.doLayout()
},featureToHTML:function(d){var b='<table class="olFeatureInfoTable" cellspacing="1" cellpadding="1"><tbody>';
for(var a in d.attributes){if(a){b+='<tr class="olFeatureInfoRow"><td width="50%" class="olFeatureInfoColumn">'+a+'</td><td width="50%" class="olFeatureInfoValue">'+d.attributes[a]+"</td></tr>"
}}b+="</tbody></table>";
var c=new Ext.XTemplate(b);
c.overwrite(this.infoPanel.body,d)
},click:function(e){if(e.attributes.features.length===0){var d='<table class="olFeatureInfoTable" cellpadding="1" cellspacing="1"><tbody>';
d+='<tr class="olFeatureInfoRow"><td colspan="2" class="olFeatureInfoValue">'+OpenLayers.i18n("FeatureInfoNoInfo")+"</td></tr>";
d+="</tbody></table>";
Ext.DomHelper.overwrite(this.infoPanel.body,d)
}for(var c=0,a=e.attributes.features.length;
c<a;
c++){var b=e.attributes.features[c];
this.featureToHTML(b)
}},clearInfoPanel:function(){if(this.infoPanel.body){Ext.DomHelper.overwrite(this.infoPanel.body,"")
}},showFeatures:function(d){this.clearInfoPanel();
var b=this.treePanel.getRootNode();
while(b.firstChild){b.removeChild(b.firstChild)
}for(var c=0,a=d.length;
c<a;
c++){var e=new Ext.tree.TreeNode({text:d[c].title,features:d[c].features});
e.addListener("click",this.click,this);
b.appendChild(e);
if(c===0){b.expand();
this.click(e);
this.treePanel.getSelectionModel().select(e)
}}b.expand()
}});
Ext.reg("gn_featureinfo",GeoNetwork.FeatureInfoPanel);Ext.namespace("GeoNetwork");
GeoNetwork.LegendPanel=Ext.extend(GeoExt.LegendPanel,{initComponent:function(){GeoNetwork.LegendPanel.superclass.initComponent.call(this)
},onStoreAdd:function(d,c,e){GeoNetwork.LegendPanel.superclass.onStoreAdd.apply(this,arguments);
for(var f=0,a=c.length;
f<a;
f++){var b=c[f];
if(b.get("layer").legendURL!==undefined){b.set("legendURL",b.get("layer").legendURL)
}}}});
Ext.reg("gn_legendpanel",GeoNetwork.LegendPanel);Ext.namespace("GeoNetwork");
GeoNetwork.ProjectionSelector=function(a){Ext.apply(this,a);
GeoNetwork.ProjectionSelector.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.ProjectionSelector,Ext.form.ComboBox,{projections:null,initComponent:function(){GeoNetwork.ProjectionSelector.superclass.initComponent.call(this);
this.on("select",this.reproject,this);
this.valueField="value";
this.autoWidth=true;
this.autoHeight=true;
this.displayField="text";
this.triggerAction="all";
this.mode="local";
this.store=new Ext.data.Store({reader:new Ext.data.ArrayReader({},[{name:"value"},{name:"text"}]),data:this.projections});
this.value=this.map.getProjection()
},reproject:function(b,a){GeoNetwork.OGCUtil.reprojectMap(this.map,new OpenLayers.Projection(a.get("value")),false)
}});
Ext.reg("gn_projectionselector",GeoNetwork.ProjectionSelector);Ext.namespace("GeoNetwork");
GeoNetwork.TimeSelector=function(a){Ext.apply(this,a);
GeoNetwork.TimeSelector.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.TimeSelector,Ext.form.FormPanel,{layer:null,numberOfSteps:12,border:false,originalFormat:null,initComponent:function(){this.buttons=[new Ext.Button({text:OpenLayers.i18n("wmsTimeUpdateButtonText"),handler:this.updateValue,scope:this})];
GeoNetwork.TimeSelector.superclass.initComponent.call(this)
},beforeDestroy:function(){this.updateValue();
GeoNetwork.TimeSelector.superclass.beforeDestroy.call(this)
},setLayer:function(a){this.layer=a;
this.originalFormat=this.layer.params.FORMAT||this.layer.params.format;
if(this.layer.dimensions&&this.layer.dimensions.time){this.add(new Ext.form.Label({text:OpenLayers.i18n("WMSTimePositionTitle")}));
this.add(new Ext.BoxComponent({height:10}));
this.add(this.createDateTimeField());
if(this.layer.dimensions.time.multipleVal){this.add(new Ext.BoxComponent({height:25}));
this.add(new Ext.form.Label({text:OpenLayers.i18n("WMSTimeMovieTitle")}));
this.add(new Ext.BoxComponent({height:10}));
this.add({xtype:"checkbox",listeners:{check:{fn:this.playMovie,scope:this}},hideLabel:true,boxLabel:OpenLayers.i18n("WMSTimeAnimationCheckbox",{steps:this.numberOfSteps})})
}this.doLayout()
}},getInterval:function(a){return parseInt(a.substring(a.indexOf("PT")+2,a.indexOf("M")))
},playMovie:function(d,g){if(g){var c,f,b;
if(this.layer.dimensions.time.values&&this.layer.dimensions.time.values.length>0){var e=this.layer.dimensions.time.values[0].split("/");
c=e[1];
f=this.getInterval(e[2]);
var h=Date.parseDate(c,"c");
h=h-(1000*60*f*this.numberOfSteps);
h=new Date(h);
b=this.formatTimeAsUTC(h)+"/"+c
}this.layer.mergeNewParams({TIME:b,FORMAT:"image/gif"})
}else{this.updateValue()
}},formatTimeAsUTC:function(a){var b=a.dateFormat("c");
var c=""+a.getUTCHours();
if(c.length<2){c="0"+c
}b=b.replace(b.substring(b.indexOf("T"),b.indexOf("T")+3),"T"+c);
b=b.replace(b.substring(b.indexOf("+"),b.indexOf("+")+6),"Z");
return b
},updateValue:function(){this.layer.mergeNewParams({TIME:this.formatTimeAsUTC(this.getForm().findField("current").getValue()),FORMAT:this.originalFormat})
},createDateTimeField:function(){var d,a,c;
if(this.layer.dimensions.time.values&&this.layer.dimensions.time.values.length>0){var b=this.layer.dimensions.time.values[0].split("/");
d=b[0];
a=b[1];
c=this.getInterval(b[2])
}return new Ext.ux.form.DateTime({hiddenFormat:"c",dateFormat:null,hideLabel:true,name:"current",dateConfig:{minValue:Date.parseDate(d,"c"),maxValue:Date.parseDate(a,"c")},timeConfig:{increment:c},value:(this.layer.params.TIME)?this.layer.params.TIME:this.layer.dimensions.time["default"],width:340})
}});
Ext.reg("gn_timeselector",GeoNetwork.TimeSelector);Ext.namespace("GeoNetwork");
GeoNetwork.BaseWindow=function(a){Ext.apply(this,a);
GeoNetwork.BaseWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.BaseWindow,Ext.Window,{map:null,initComponent:function(){GeoNetwork.BaseWindow.superclass.initComponent.call(this);
this.constrainHeader=true;
this.collapsible=true;
this.layout="fit";
this.plain=true;
this.stateful=false
}});Ext.namespace("GeoNetwork");
GeoNetwork.SingletonWindowManager=function(){var a=new Object();
var b=new Array();
return{registerWindow:function(f,d,c){var e=new d(c);
a[f]={windowz:e,classz:d,configz:c}
},getWindow:function(c){if(a[c]){return a[c].windowz
}else{return null
}},showWindow:function(e){if(a[e]){if(Ext.isEmpty(Ext.getCmp(e))){var c=a[e];
var d=new c.classz(c.configz);
a[e]={windowz:d,classz:c.classz,configz:c.configz}
}a[e].windowz.show();
return true
}else{return false
}},hideAllWindows:function(){for(key in a){if(a[key].windowz.isVisible()){a[key].windowz.setVisible(false);
b[b.length]=key
}}},restoreHiddenWindows:function(){for(var d=0,c=b.length;
d<c;
++d){a[b[d]].windowz.setVisible(true)
}b=new Array()
}}
};
GeoNetwork.WindowManager=new GeoNetwork.SingletonWindowManager();Ext.namespace("GeoNetwork");
GeoNetwork.AddWmsLayerWindow=function(a){Ext.apply(this,a);
GeoNetwork.AddWmsLayerWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.AddWmsLayerWindow,GeoNetwork.BaseWindow,{iconCls:"addLayerIcon",browserPanel:null,initComponent:function(){GeoNetwork.AddWmsLayerWindow.superclass.initComponent.call(this);
this.title=this.title||OpenLayers.i18n("addWMSWindowTitle");
this.width=600;
this.height=500;
var b=new Ext.data.Store({data:GeoNetwork.WMSList,reader:new Ext.data.ArrayReader({},[{name:"title"},{name:"url"}])});
this.browserPanel={id:this.id+"wmsbrowserpanel",xtype:"gn_wmsbrowserpanel",mode:GeoNetwork.wms.BrowserPanel.ADDWMS,wmsStore:b,map:this.map};
var a=new Ext.Panel({border:false,deferredRender:false,layout:"fit",items:[this.browserPanel]});
this.add(a);
this.doLayout()
}});Ext.namespace("GeoNetwork");
GeoNetwork.FeatureInfoWindow=function(a){Ext.apply(this,a);
GeoNetwork.FeatureInfoWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.FeatureInfoWindow,GeoNetwork.BaseWindow,{control:null,initComponent:function(){GeoNetwork.FeatureInfoWindow.superclass.initComponent.call(this);
this.title=this.title||OpenLayers.i18n("featureInfoWindow.windowTitle");
this.width=600;
this.height=250;
this.cls="popup-variant1";
var a=new GeoNetwork.FeatureInfoPanel();
this.add(a);
this.doLayout()
},setFeatures:function(a){this.items.items[0].showFeatures(a)
}});Ext.namespace("GeoNetwork");
GeoNetwork.LoadWmcWindow=function(a){Ext.apply(this,a);
GeoNetwork.LoadWmcWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.LoadWmcWindow,GeoNetwork.BaseWindow,{initComponent:function(){GeoNetwork.LoadWmcWindow.superclass.initComponent.call(this);
this.width=480;
this.title=this.title||OpenLayers.i18n("selectWMCFile.windowTitle");
this.resizable=false;
this.charset="UTF-8";
var a=new Ext.FormPanel({fileUpload:true,height:100,bodyStyle:"padding: 10px 10px 0 10px;",labelWidth:0,plain:true,frame:true,border:false,defaults:{anchor:"90%",msgTarget:"side",allowBlank:false},items:[{xtype:"fileuploadfield",id:"form-file",width:120,emptyText:OpenLayers.i18n("selectWMCFile"),hideLabel:true,buttonText:"",name:"Fileconten",buttonCfg:{text:"",iconCls:"selectfile"}}],buttons:[{text:OpenLayers.i18n("selectWMCFile.loadButtonText"),scope:this,handler:function(){if(a.getForm().isValid()){a.getForm().submit({url:"../../wmc/load.wmc",success:this.onSuccessLoad,failure:this.onFailure,scope:this})
}}},{text:OpenLayers.i18n("selectWMCFile.mergeButtonText"),scope:this,handler:function(){if(a.getForm().isValid()){a.getForm().submit({url:"../../wmc/load.wmc",success:this.onSuccessMerge,failure:this.onFailure,scope:this})
}}}]});
this.add(a);
this.doLayout()
},onSuccessLoad:function(c,d){var b=d.response.responseText;
var e=Ext.decode(b);
if(e.success){var a=OpenLayers.Function.bind(this.parseWMCLoad,this);
OpenLayers.loadURL(e.url,null,null,a)
}else{this.onAjaxFailure()
}},onSuccessMerge:function(c,d){var b=d.response.responseText;
var e=Ext.decode(b);
if(e.success){var a=OpenLayers.Function.bind(this.parseWMCMerge,this);
OpenLayers.loadURL(e.url,null,null,a)
}else{this.onAjaxFailure()
}},onFailure:function(a,b){Ext.MessageBox.show({icon:Ext.MessageBox.ERROR,title:OpenLayers.i18n("errorTitle"),msg:OpenLayers.i18n("InvalidWMC"),buttons:Ext.MessageBox.OK})
},parseWMCLoad:function(a){GeoNetwork.WMCManager.loadWmc(this.map,a.responseText);
Ext.WindowMgr.getActive().close()
},parseWMCMerge:function(a){GeoNetwork.WMCManager.mergeWmc(this.map,a.responseText);
Ext.WindowMgr.getActive().close()
}});Ext.namespace("GeoNetwork");
GeoNetwork.DisclaimerWindow=function(a){Ext.apply(this,a);
GeoNetwork.DisclaimerWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.DisclaimerWindow,Ext.Window,{disclaimer:null,initComponent:function(){GeoNetwork.BaseWindow.superclass.initComponent.call(this);
this.id="disclaimerwindow";
this.constrainHeader=true;
this.layout="fit";
this.plain=true;
this.stateful=false;
this.title=OpenLayers.i18n("disclaimer.windowTitle");
this.minWidth=440;
this.minHeight=280;
this.width=440;
this.height=280;
this.autoScroll=true;
this.modal=true;
this.addButton(OpenLayers.i18n("disclaimer.buttonClose"),function(){this.close()
},this);
if(this.disclaimer.startsWith("http://")){this.on("show",this.showDisclaimerUrl)
}else{var a=new Ext.form.TextArea({hideLabel:true,name:"msg",value:this.disclaimer,anchor:"100% -53",enableKeyEvents:true,listeners:{keydown:function(c,b){if(!(b.getKey()==67&&b.ctrlKey)){b.stopEvent()
}}}});
this.add(a)
}this.doLayout()
},showDisclaimerUrl:function(){this.load({url:OpenLayers.ProxyHost+this.disclaimer,text:OpenLayers.i18n("disclaimer.loading"),timeout:30,scripts:false})
}});Ext.namespace("GeoNetwork");
GeoNetwork.LayerStylesWindow=function(a){Ext.apply(this,a);
GeoNetwork.LayerStylesWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.LayerStylesWindow,GeoNetwork.BaseWindow,{initComponent:function(){GeoNetwork.LayerStylesWindow.superclass.initComponent.call(this);
this.title=this.title||OpenLayers.i18n("layerStylesWindowTitle");
this.width=575;
this.height=300;
this.layerStylesPanel=new GeoNetwork.wms.LayerStylesPanel({map:this.map});
this.add(this.layerStylesPanel);
this.addButton(OpenLayers.i18n("selectStyleButton"),this._selectStyle,this);
this.doLayout()
},showLayerStyles:function(a){this.layer=a;
this.layerStylesPanel.showLayerStyles(a)
},_selectStyle:function(){this.layer.mergeNewParams({styles:this.layerStylesPanel.selectedStyle});
this.layer.legendURL=this.layerStylesPanel.selectedStyleLegendUrl
}});Ext.namespace("GeoNetwork");
GeoNetwork.WmsLayerMetadataWindow=function(a){Ext.apply(this,a);
GeoNetwork.WmsLayerMetadataWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.WmsLayerMetadataWindow,GeoNetwork.BaseWindow,{layer:null,initComponent:function(){GeoNetwork.WmsLayerMetadataWindow.superclass.initComponent.call(this);
this.title=this.title||OpenLayers.i18n("layerInfoPanel.windowTitle");
this.width=575;
this.height=300;
this.infoLayerPanel=new GeoNetwork.wms.LayerInfoPanel({map:this.map});
this.add(this.infoLayerPanel);
this.doLayout()
},showLayerInfo:function(a){this.infoLayerPanel.layer=a;
this.infoLayerPanel.onlineresource=a.url;
this.infoLayerPanel.showLayerInfo()
}});Ext.namespace("GeoNetwork");
GeoNetwork.WMSTimeWindow=function(a){Ext.apply(this,a);
GeoNetwork.WMSTimeWindow.superclass.constructor.call(this)
};
Ext.extend(GeoNetwork.WMSTimeWindow,GeoNetwork.BaseWindow,{initComponent:function(){GeoNetwork.WMSTimeWindow.superclass.initComponent.call(this);
this.title=this.title||OpenLayers.i18n("WMSTimeWindowTitle");
this.width=450;
this.height=300;
this.timeSelector=new GeoNetwork.TimeSelector({bodyStyle:"padding: 10px 10px 0 10px;"});
this.add(this.timeSelector);
this.doLayout()
},setLayer:function(a){this.timeSelector.setLayer(a)
}});