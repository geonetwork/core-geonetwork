Ext4.require('Ext4.chart.*');
Ext4.require('Ext4.layout.container.Fit');


var catalogue, urlParameters, store, data = {};

var buildChart = function(title, data) {
	var chartConfig = {
		type : {
			legend : {
				position : 'right'
			}
		},
		denominator: {
			legend : {
				position : 'right'
			}
		},
		inspireTheme : {
		}
	};

	var store = Ext4.create('Ext4.data.Store', {
		fields: ['name', 'count']
	});
	
	data.each(function(record) {
		store.add({
			name : record.get('name'),
			count : record.get('count')
		});
	});
	var donut = false;
	var chart = Ext4.create('Ext.chart.Chart', {
		xtype : 'chart',
		animate : true,
		renderTo : 'chart' + title,
		width : 500,
		height : 500,
		store : store,
		legend : chartConfig[title] && chartConfig[title].legend || false,
//		legend: {
//			position : 'right'
//		},
		insetPadding : 25,
		//theme : 'Base:gradients',
		series : [ {
			type : 'pie',
			field : 'count',
			showInLegend : true,
			donut : donut,
			tips : {
				trackMouse : true,
				width : 140,
				height : 80,
				renderer : function(storeItem, item) {
					// calculate percentage.
					var total = 0;
					store.each(function(rec) {
						total += parseInt(rec.get('count'));
					});
					this.setTitle(storeItem.get('name') + '<br/>'
							+ storeItem.get('count') + ' records ('
							+ Math.round(storeItem.get('count') / total * 100)
							+ '%)');
				}
			},
			highlight : {
				segment : {
					margin : 20
				}
			},
			label : {
				field : 'name',
				display : 'rotate',
				contrast : true,
				font : '10px Arial'
			}
		} ]
	});
}

var facetLoaded = function(response) {
	store = GeoNetwork.util.SearchTools.parseFacets(response);

	var facets = store.collect("facet");
	
	Ext.each(facets, function(value) {
		if (!urlParameters.chart || urlParameters.chart.indexOf(value)!=-1) {
			data[value] = store.query("facet", value);
			Ext.getDom('content').innerHTML = Ext.getDom('content').innerHTML
			+ "<div class='graph'><h1>" + value + "</h1>"
			+ "<div id='chart" + value + "'/>" + "</div>";
		}
	});
	// When building in chart on the above loop, the animate and tips works only
	// for the latest chart built.
	Ext.each(facets, function(value) {
		if (!urlParameters.chart || urlParameters.chart.indexOf(value)!=-1) {
			buildChart(value, data[value]);
		}
	});
}


Ext.onReady(function() {
    urlParameters = GeoNetwork.Util.getParameters(location.href);

	catalogue = new GeoNetwork.Catalogue();

	metadataStore = GeoNetwork.data.MetadataResultsFastStore();
	
	this.catalogue.search({
		E_summaryOnly : 'true'
	}, facetLoaded, null, 1, true, metadataStore, null);
});
