var currentOperation = "";
var i = 0;
var operations = [];
operations[i++] = [ 'csw-GetCapabilities', 'csw-GetCapabilities', '' ];

// DescribeRecord ------------- START -- //
operations[i++] = [ 'csw-DescribeRecord', 'csw-DescribeRecord', '' ];
operations[i++] = [ 'csw-DescribeRecordWithMD_Metadata',
		'csw-DescribeRecord with gmd:MD_Metadata', '' ];
operations[i++] = [ 'csw-DescribeRecordWithMultipleTypeName',
            		'csw-DescribeRecord multiple typenames', '' ];
// DescribeRecord ------------- END -- //

// GetRecords ------------- START -- //
operations[i++] = [ 'csw-GetRecordsNoFilter', 'csw-GetRecords | no filter', '' ];
operations[i++] = [ 'csw-GetRecordsNoFilterResults',
		'csw-GetRecords | no filter | results', '' ];
operations[i++] = [ 'csw-GetRecordsNoFilterValidate',
		'csw-GetRecords | no filter | validate', '' ];
operations[i++] = [
		'csw-GetRecordsNoFilterResultsWithSummary',
		'csw-GetRecords | no filter | results_with_summary (CSW GeoNetwork extension)',
		'' ];
operations[i++] = [ 'csw-GetRecordsNoFilterIsoRecord',
		'csw-GetRecords | no filter | outputSchema: csw-IsoRecord', '' ];
operations[i++] = [ 'csw-GetRecordsNoFilterCswIsoRecord',
		'csw-GetRecords | no filter | outputSchema: csw-Record', '' ];
operations[i++] = [ 'csw-GetRecordsNoFilterFraIsoRecord',
		'csw-GetRecords | no filter | outputSchema: fra:IsoRecord', '' ];
operations[i++] = [
		'csw-GetRecordsNoFilterOwn',
		'csw-GetRecords | no filter | outputSchema: own (CSW GeoNetwork extension)',
		'' ];
operations[i++] = [ 'csw-GetRecordsElementName',
		'csw-GetRecords | Element name', '' ];
//GetRecords ------------- FILTER -- //
operations[i++] = [ 'csw-GetRecordsSortBy',
            		'csw-GetRecords | SortBy title', '' ];
// GetRecords ------------- FILTER -- //
operations[i++] = [ 'csw-GetRecordsFilterService',
		'csw-GetRecords | filter \'service\'', '' ];
operations[i++] = [ 'csw-GetRecordsFilterRangeCswIsoRecord',
            		'csw-GetRecords | filter range', '' ];
// GetRecords ------------- GEOFILTER -- //
operations[i++] = [ 'csw-GetRecordsFilterGeoBboxEquals',
		'csw-GetRecords | filter ogc:Equals+bbox -90,-180 90,180', '' ];
operations[i++] = [ 'csw-GetRecordsFilterGeoBbox2Equals',
		'csw-GetRecords | filter ogc:Equals+bbox -90,-180 0,0', '' ];
operations[i++] = [ 'csw-GetRecordsFilterGeoEnvelope',
		'csw-GetRecords | filter ogc:BBOX+gml:Envelope', '' ];
operations[i++] = [ 'csw-GetRecordsFilterGeoBox',
		'csw-GetRecords | filter ogc:BBOX+gml:Box',
		'note: Take care of Filter version, here 1.0.0.' ];
operations[i++] = [ 'csw-GetRecordsFilterGeoBoxIntersects',
		'csw-GetRecords | filter ogc:Intersects+gml:Box', '' ];

// GetRecords ------------- CQL -- //
operations[i++] = [ 'csw-GetRecordsCQLAny',
		'csw-GetRecords | CQL \'service\' operator any',
		'note: like operator not supported by ZING parser.' ];
operations[i++] = [ 'csw-GetRecordsCQLEquals',
		'csw-GetRecords | CQL \'service\'', '' ];
// GetRecords ------------- END -- //
// GetRecordById ------------- START -- //
operations[i++] = [ 'csw-GetRecordById', 'csw-GetRecordById', '' ];
operations[i++] = [ 'csw-GetRecordByIdIsoRecord',
		'csw-GetRecordById | outputSchema: IsoRecord', '' ];
operations[i++] = [ 'csw-GetRecordByIdFraIsoRecord',
		'csw-GetRecordById | outputSchema: fra:IsoRecord', '' ];
// GetRecordById ------------- END -- //
// GetDomain ------------- START -- //
operations[i++] = [ 'csw-GetDomainParameterName', 'csw-GetDomain | ParameterName', '' ];
operations[i++] = [ 'csw-GetDomainPropertyName', 'csw-GetDomain | PropertyName', '' ];
// GetDomain ------------- END -- //
// TRANSACTION ------------- START -- //
operations[i++] = [ 'csw-TransactionInsert', 'csw-Transaction | Insert', '' ];
operations[i++] = [ 'csw-TransactionUpdate', 'csw-Transaction | Update full record', '' ];
operations[i++] = [ 'csw-TransactionUpdateProperties', 'csw-Transaction | Update properties', '' ];
operations[i++] = [ 'csw-TransactionDelete', 'csw-Transaction | Delete', '' ];
// TRANSACTION ------------- END -- //
// HARVEST ------------- START -- //
operations[i++] = [ 'csw-Harvest', 'csw-Harvest', '' ];
// HARVEST ------------- END -- //

function init() {
	var operationsList = document.getElementById('request');
	for (i = 0; i < operations.length; i++) {
		var op = new Option(operations[i][1], operations[i][0]);
		operationsList.options[i] = op;
	}
	updateOperation(operationsList.options[operationsList.selectedIndex]);
}

function updateOperation(option) {
	for (i = 0; i < operations.length; i++) {
		if (operations[i][0] == option.value) {
			var request = OpenLayers.Request
					.GET( {
						url :'../../xml/csw/test/' + operations[i][0] + '.xml',
						success : function(response) {
							document.getElementById('body').value = response.responseText;
						}
					});

			if (operations[i][2] != undefined)
				document.getElementById('info').innerHTML = operations[i][2];
			else
				document.getElementById('info').innerHTML = '';
		}
	}
}

function submit() {

	var opts = {
		url :document.getElementById('url').value,
		data :document.getElementById('body').value
	};
	OpenLayers.Util.applyDefaults(opts, {
		success : function(response) {
			document.getElementById('response').value = response.responseText;
			// document.getElementById('response').contentDocument.innerHTML
			// = response.responseXML;
			// window.frames['response'].document

		}
	});
	var request = OpenLayers.Request.POST(opts);

}

/**
 * JS example to run a request with a login action first.
 * Log in is mainly required for transaction operation using CSW.
 * First login to the remote node using xml.user.login service, 
 * then run the request.
 * 
 * @return
 */
function loginAndRun(context) {
	// Logout first
	var request = OpenLayers.Request.GET(
			{async:false, url: context+'/j_spring_security_logout'});

	// Login
	var opts = {
		data: "username="+document.getElementById('username').value+"&password="+document.getElementById('password').value,
		headers: {
			"Content-Type": "application/x-www-form-urlencoded"
		},
		url: context+'/j_spring_security_check',
	    async: false
	};
	var request = OpenLayers.Request.POST(opts);
	
	// Run the request
	var opts = {
			user: document.getElementById('username').value,
			password: document.getElementById('password').value,
			url :document.getElementById('url').value,
			data :document.getElementById('body').value,
		    async: false
		};
	OpenLayers.Util.applyDefaults(opts, {
		success : function(response) {
			document.getElementById('response').value = response.responseText;
		}
	});
	var request = OpenLayers.Request.POST(opts);
}

