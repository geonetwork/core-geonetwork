/********************************************************************
* gn_search.js
*
* This file contains functions related to the statistics on search part of Geonetwork
********************************************************************/

// the variable for services URL
var serviceUrlPrefix = Env.locService + "/";

function initStat() {
	updateDiv('stat.tagCloud', 'stat.tagCloudDiv');
	initCalendar();	
}

// injects the response of the given service into the given div element
// should change Request object by an Updater (see: http://www.prototypejs.org/api/ajax/updater)
function injectServiceResponse(serviceName, divEl, params) {
	var myAjax = new Ajax.Request(
		serviceUrlPrefix + serviceName, 
		{
			method: 'get',
			parameters: params,
			evalScripts:true,
			onSuccess: function(req) {
			    $(divEl).innerHTML = req.responseText;
			    $(divEl).show();
			},
			onFailure: function(req) {
			    $('serviceFailureDiv').innerHTML = req.responseText;
			    $('serviceFailureDiv').show();
			}
		}
	);
}

//updates the given div element with content from the given URL
function updateDiv(serviceName, divEl, params) {
	//txt = "10,nicolas\n20,toto\n,30tutut\n,12fffd";
	//generateCloud(txt, "stat.tagCloudDiv");
	var myAjax = new Ajax.Request(
			serviceUrlPrefix + serviceName, 
			{
				method: 'get',
				parameters: params,
				onSuccess: function(req) {
					var r = req.responseText;
					//alert(r);
					generateCloud(r, "stat.tagCloudDiv");
				},
				onFailure: function(req) {
				    $('serviceFailureDiv').innerHTML = req.responseText;
				    $('serviceFailureDiv').show();
				}
			}
		);
}

// collapses all given element id (array of strings)
function collapseSearch(arrayOfDiv) {
	if (!arrayOfDiv) return;
	
	for (var i = 0; i < arrayOfDiv.length; i++) {
		$(arrayOfDiv[i]).hide();
	}
}

function displayGraphic() {
	var dateFrom = $('f_date_from').value;
	var dateTo = $('f_date_to').value;
	if (!dateFrom) {
		alert("Please choose a date from");
		return;
	}
	if (!dateTo) {
		alert("Please choose a date to");
		return;
	}
	
  	var gt;
  	for (var i = 0; i < document.statForm.elements.length; i++) {
  		if (document.statForm.elements[i].type == "radio" && 
  			document.statForm.elements[i].checked) {
  			
  			gt = document.statForm.elements[i].value;
  		}
  	}
	var params = "dateFrom=" + dateFrom + "T00:00:00&dateTo=" + dateTo + "T23:59:59&graphicType=" + gt;
	injectServiceResponse('stat.graphByDate', 'stat.graphicDiv', params);	
}

// called when a date has changed: ask for a new graphic
function dateChanged(cal) {
    var date = cal.date;
    var time = date.getTime()
    // use the _other_ field
    /*
    var field = document.getElementById("f_date_to");
    if (field == cal.params.inputField) {
        dateTo = date.print("%Y-%m-%dT%H:%M:%S");
        //time -= Date.WEEK; // substract one week
    } else {
    	dateFrom = date.print("%Y-%m-%dT%H:%M:%S");
        //time += Date.WEEK; // add one week
    }
    //var date2 = new Date(time);
    //field.value = date2.print("%Y-%m-%d %H:%M");
    */
    
  	// fuck, no direct access to checked value ??
}


//---------------------------- for tagcloud -----------------------
// JS sources from: http://www.tocloud.com/javascript_cloud_generator.html
// adapted to make it more generic
function getFontSize(min,max,val) {
  return Math.round((150.0*(1.0+(1.5*val-max/2)/max)));
}

/**
 * Generates the cloud
 * txt: the could text, as a (weight, text) pair where weith is a 0-100 value.
 */
function generateCloud(txt, cloudDiv) {
  var lines = txt.split(/;\r?\n/);
  var min = 10000000000;
  var max = 0;
  for(var i=0;i<lines.length;i++) {
    var line = lines[i];
    var data = line.split(/,/);
    if(data.length != 2) {
      lines.splice(i,1);
      continue;
    }
    data[0] = parseFloat(data[0]);
    lines[i] = data;
    if(data[0] > max) 
      max = data[0];
    if(data[0] < min)
      min = data[0];
  }

  lines.sort(function (a,b) {
               var A = a[1].toLowerCase();
               var B = b[1].toLowerCase();
               return A>B ? 1 : (A<B ? -1 : 0);
              });

  var html = "<style type='text/css'>#jscloud a:hover { text-decoration: underline; }</style> <div id='jscloud'>";
  for(var i=0;i<lines.length;i++) {
    var val = lines[i][0];
    var fsize = getFontSize(min,max,val);
    //html += " <a style='font-size:"+fsize+"%;' title='"+lines[i][0]+"'>"+lines[i][1]+"</a> ";
    html += " <a style='font-size:"+fsize+"%;'>"+lines[i][1]+"</a> ";
  }
  html += "</div>";
  var cloud = document.getElementById(cloudDiv);
  cloud.innerHTML = html;
}

function setClass(layer,cls) {
  layer.setAttribute("class",cls);
  layer.setAttribute("className",cls);
}


