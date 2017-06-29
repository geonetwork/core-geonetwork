(function(factory){
	if(typeof define === "function"  && define.amd)
	{
		define(['d3','d3-tip'],factory)
	}else{
		if(d3 && d3.tip)
			d3.timeseries = factory(d3,d3.tip)
		else
			d3.timeseries = factory(d3)
	}
}(
function(d3,d3tip)
{

  var default_colors = ["#a6cee3","#ff7f00","#b2df8a","#1f78b4","#fdbf6f","#33a02c","#cab2d6","#6a3d9a","#fb9a99","#e31a1c","#ffff99","#b15928"];


  var timeseries = function()
  {
    //default
    var height = 480
    var width = 600

		var drawerHeight = 80
		var drawerTopMargin = 10
    var margin = {top:10,bottom:20,left:30,right:10}

    var series = []

    var yscale = d3.scale.linear()
    var xscale = d3.time.scale()
		yscale.label = ""
		xscale.label = ""

		var brush = d3.svg.brush()

    var svg,container,serieContainer,annotationsContainer,drawerContainer,tip,mousevline;
		var fullxscale,tooltipDiv;

		yscale.setformat = function(n){return n.toLocaleString()}
		xscale.setformat = xscale.tickFormat()

		//default tool tip function
		var _tipFunction = function(date,series) {

				var spans = '<table style="border:none">'+series.filter(function(d){
						return d.item!==undefined && d.item!==null
					}).map(function(d){
						return '<tr><td style="color:'+d.options.color+'">'+d.options.label+' </td>'+
									'<td style="color:#333333;text-align:right">'+yscale.setformat(d.item[d.aes.y])+'</td></tr>'
					}).join('')+'</table>'

				return '<h4>'+xscale.setformat(d3.time.day(date))+'</h4>'+spans
			}



    function createLines(serie){
			var aes = serie.aes
			//https://github.com/mbostock/d3/wiki/SVG-Shapes#line_interpolate
			if(! serie.options.interpolate)
				serie.options.interpolate = 'linear'

      var line = d3.svg.line()
              .x(functorkeyscale(aes.x,xscale))
              .y(functorkeyscale(aes.y,yscale))
							.interpolate(serie.options.interpolate)
							.defined(keyNotNull(aes.y))

			serie.line = line
			serie.options.label = serie.options.label ||
														serie.options.name ||
														serie.aes.label ||
														serie.aes.y;

			if(aes.ci_up && aes.ci_down){
				var ciArea = d3.svg.area()
											.x(functorkeyscale(aes.x,xscale))
											.y0(functorkeyscale(aes.ci_down,yscale))
											.y1(functorkeyscale(aes.ci_up,yscale))
											.interpolate(serie.options.interpolate)
				serie.ciArea = ciArea
			}

			if(aes.diff){
				serie.diffAreas = [d3.svg.area()
															.x(functorkeyscale(aes.x,xscale))
															.y0(functorkeyscale(aes.y,yscale))
															.y1(function(d){
																if(d[aes.y]>d[aes.diff])
																	return yscale(d[aes.diff])
																return yscale(d[aes.y])
															})
															.interpolate(serie.options.interpolate)
															,
														d3.svg.area()
															.x(functorkeyscale(aes.x,xscale))
															.y1(functorkeyscale(aes.y,yscale))
															.y0(function(d){
																if(d[aes.y]<d[aes.diff])
																	return yscale(d[aes.diff])
																return yscale(d[aes.y])
															})
															.interpolate(serie.options.interpolate)
														]
			}

			serie.find = function(date){
					var bisect = d3.bisector(fk(aes.x)).left;
					var i = bisect(serie.data,date)-1
					if(i==-1)
						return null
					//look to far after serie is defined
					if(i==serie.data.length-1 && serie.data.length>1 &&
						 Number(date)-Number(serie.data[i][aes.x])>Number(serie.data[i][aes.x])-Number(serie.data[i-1][aes.x]))
						 return null
					return serie.data[i]
			}

    }

		function drawSerie(serie){
			if(! serie.linepath){
				var aes = serie.aes
	      var linepath = serieContainer.append("path")
	              .datum(serie.data)
	              .attr('class','d3_timeseries line')
	              .attr('d',serie.line)
	              .attr('stroke',serie.options.color)
								.attr('stroke-linecap','round')
	              .attr('stroke-width',serie.options.width || 1.5)
	              .attr('fill','none')

	      if(serie.options.dashed)
	      {
	        if(serie.options.dashed==true || serie.options.dashed=='dashed')
	        {
						serie['stroke-dasharray'] = '5,5'
					}else if(serie.options.dashed=='long'){
						serie['stroke-dasharray'] = '10,10'
	        }else if(serie.options.dashed=='dot'){
						serie['stroke-dasharray'] = '2,4'
	        }else{
						serie['stroke-dasharray'] = serie.options.dashed
	        }
					linepath.attr('stroke-dasharray',serie['stroke-dasharray'])
	      }
				serie.linepath = linepath

				if(serie.ciArea){
						serie.cipath = serieContainer.insert("path",":first-child")
													.datum(serie.data)
													.attr('class','d3_timeseries ci-area')
													.attr('d',serie.ciArea)
													.attr('stroke','none')
													.attr('fill',serie.options.color)
													.attr('opacity',serie.options.ci_opacity || 0.3)
				}
				if(serie.diffAreas)
				{
					serie.diffpaths = serie.diffAreas.map(function(area,i){
						var c = (serie.options.diff_colors ? serie.options.diff_colors : ['green','red'])[i]

						return serieContainer.insert("path",function(){return linepath[0][0]})
												.datum(serie.data)
												.attr('class','d3_timeseries diff-area')
												.attr('d',area)
												.attr('stroke','none')
												.attr('fill',c)
												.attr('opacity',serie.options.diff_opacity || 0.5)
					})
				}


			}else{
				serie.linepath.attr('d',serie.line)
				if(serie.ciArea){
					serie.cipath.attr('d',serie.ciArea)
				}
				if(serie.diffAreas){
					serie.diffpaths[0].attr('d',serie.diffAreas[0])
					serie.diffpaths[1].attr('d',serie.diffAreas[1])
				}
			}
		}

		function updatefocusRing(xdate){
			var s = annotationsContainer.selectAll("circle.d3_timeseries.focusring")

			if(xdate==null){
				s=s.data([])
			}else{
				s=s.data(series.map(function(s,i){
						return { x:xdate,item:s.find(xdate),
									aes:s.aes,
									color:s.options.color}
					}).filter(function(d){
						return d.item!==undefined && d.item!==null &&
										d.item[d.aes.y]!==null && !isNaN(d.item[d.aes.y])
					}))
			}

			s.transition().duration(50)
					.attr('cx',function(d){
						return xscale(d.item[d.aes.x])
					})
					.attr('cy',function(d){
						return yscale(d.item[d.aes.y])
					})

			s.enter().append("circle")
					.attr('class','d3_timeseries focusring')
					.attr('fill','none')
					.attr('stroke-width',2)
					.attr('r',5)
					.attr('stroke',fk('color'))

			s.exit().remove()

		}

		function updateTip(xdate){
			if(xdate==null){
				tooltipDiv.style('opacity',0)
			}else{
				var s=series.map(function(s,i){
						return {item:s.find(xdate),
									aes:s.aes,options:s.options}
					})

				tooltipDiv.style('opacity',0.9)
									.style('left',(margin.left+5+xscale(xdate))+'px')
									.style('top',"0px")
									.html(_tipFunction(xdate,s))

				}
		}

		function drawMiniDrawer(){
				var smallyscale = yscale.copy()
																.range([drawerHeight - drawerTopMargin,0])
				var serie = series[0]
				var line = d3.svg.line()
										.x(functorkeyscale(serie.aes.x,fullxscale))
										.y(functorkeyscale(serie.aes.y,smallyscale))
										.interpolate(serie.options.interpolate)
										.defined(keyNotNull(serie.aes.y))
				var linepath = drawerContainer.insert("path",":first-child")
	              .datum(serie.data)
	              .attr('class','d3_timeseries.line')
								.attr('transform','translate(0,'+drawerTopMargin+')')
	              .attr('d',line)
	              .attr('stroke',serie.options.color)
	              .attr('stroke-width',serie.options.width || 1.5)
	              .attr('fill','none')
				if(serie.hasOwnProperty('stroke-dasharray'))
					linepath.attr('stroke-dasharray',serie['stroke-dasharray'])
		}

		function mouseMove(e){
			var x = d3.mouse(container[0][0])[0]
			x = xscale.invert(x)
			mousevline.datum({x:x,visible:true})
			mousevline.update()
			updatefocusRing(x)
			updateTip(x)
		}
		function mouseOut(e){
			mousevline.datum({x:null,visible:false})
			mousevline.update()
			updatefocusRing(null)
			updateTip(null)
		}
    var chart = function(elem)
    {
      //compute mins max on all series
      series = series.map(function(s){
        var extent = d3.extent(s.data.map(functorkey(s.aes.y)))
        s.min = extent[0]
        s.max = extent[1]
        extent = d3.extent(s.data.map(functorkey(s.aes.x)))
        s.dateMin = extent[0]
        s.dateMax = extent[1]
        return s
      })


      //set scales

      yscale.range([height - margin.top - margin.bottom - drawerHeight - drawerTopMargin,0])
            .domain([d3.min(series.map(fk('min'))),
                    d3.max(series.map(fk('max')))])
            .nice()



      xscale.range([0,width-margin.left-margin.right])
            .domain([d3.min(series.map(fk('dateMin'))),
                    d3.max(series.map(fk('dateMax')))])
            .nice()

			// if user specify domain
			if(yscale.fixedomain)
			{
				// for showing 0 :
				//chart.addSerie(...)
				//		.yscale.domain([0])
				if(yscale.fixedomain.length==1)
					yscale.fixedomain.push(yscale.domain()[1])
				yscale.domain(yscale.fixedomain)
			}
			if(xscale.fixedomain)
				xscale.domain(yscale.fixedomain)

			fullxscale = xscale.copy()

			//create svg
      svg = d3.select(elem).append('svg')
            .attr('width',width)
            .attr('height',height)



			// clipping for scrolling in focus area
			svg.append('defs').append('clipPath')
			    .attr('id', 'clip')
			  .append('rect')
			    .attr('width', width-margin.left-margin.right)
			    .attr('height', height - margin.bottom - drawerHeight - drawerTopMargin)
					.attr('y',-margin.top)

			// container for focus area
      container = svg.insert('g',"rect.mouse-catch")
                          .attr('transform','translate('+margin.left+','+margin.top+')')
													.attr('clip-path','url(#clip)')
			serieContainer = container.append('g')
			annotationsContainer = container.append('g')

			// mini container at the bottom
			drawerContainer = svg.append('g')
													.attr('transform','translate('+margin.left+','+(height-drawerHeight-margin.bottom)+')')

			// vertical line moving with mouse tip
			mousevline = svg.append('g')
												.datum({
													x:new Date(),
													visible:false
												})
			mousevline.append('line')
									.attr('x1',0)
									.attr('x2',0)
									.attr('y1',yscale.range()[0])
									.attr('y2',yscale.range()[1])
									.attr('class','d3_timeseries mousevline')
			//update mouse vline
			mousevline.update = function()
			{
				this.attr('transform',function(d){
							return 'translate('+(margin.left+xscale(d.x))+','+margin.top+')'
						})
						.style('opacity',function(d){return d.visible ? 1 : 0})
			}
			mousevline.update()

      var xAxis = d3.svg.axis().scale(xscale).orient('bottom').tickFormat(xscale.setformat)
      var yAxis = d3.svg.axis().scale(yscale).orient('left').tickFormat(yscale.setformat)

			brush.x(fullxscale)
						.on('brush',function(){
						  xscale.domain(brush.empty() ? fullxscale.domain() : brush.extent());

							series.forEach(drawSerie)
						  svg.select(".focus.x.axis").call(xAxis);
							mousevline.update()
							updatefocusRing()
						})

			svg.append('g')
						.attr('class','d3_timeseries focus x axis')
						.attr("transform", "translate("+margin.left+","+ (height-margin.bottom-drawerHeight - drawerTopMargin) +")")
						.call(xAxis);

			drawerContainer.append('g')
						.attr('class','d3_timeseries x axis')
						.attr("transform", "translate(0,"+ (drawerHeight) +")")
						.call(xAxis);

			drawerContainer.append("g")
		       .attr("class", "d3_timeseries brush")
		       .call(brush)
		     .selectAll("rect")
				 		.attr('y',drawerTopMargin)
		       .attr("height", (drawerHeight - drawerTopMargin));


			svg.append('g')
						.attr('class','d3_timeseries y axis')
						.attr("transform", "translate("+margin.left+","+ margin.top +")")
						.call(yAxis)
					.append("text")
						.attr("transform", "rotate(-90)")
						.attr("x", -margin.top-d3.mean(yscale.range()))
						.attr("dy", ".71em")
						.attr('y',-margin.left+5)
						.style("text-anchor", "middle")
						.text(yscale.label);

			//catch event for mouse tip
			var mouseCatcher = svg.append('rect')
					.attr('width',width)
					.attr('class','d3_timeseries mouse-catch')
					.attr('height',height - drawerHeight)
					//.style('fill','green')
					.style('opacity',0)
					.on('mousemove',mouseMove)
					.on('mouseout',mouseOut)

			tooltipDiv = d3.select(elem)
													.style('position','relative')
													.append('div')
													.attr('class','d3_timeseries tooltip')
													.style('opacity',0)


			series.forEach(createLines)
			series.forEach(drawSerie)
			drawMiniDrawer()
    };


    chart.width = function(_) {
      if (!arguments.length) return width;
      width = _;
      return chart;
    };

    chart.height = function(_) {
      if (!arguments.length) return height;
      height = _;
      return chart;
    };

    chart.margin = function(_) {
      if (!arguments.length) return margin;
      margin = _;
      return chart;
    };
		//accessors for margin.left(), margin.right(), margin.top(), margin.bottom()
		d3.keys(margin).forEach(function(k){
			chart.margin[k] = function(_){
				if (!arguments.length) return margin[k];
				margin[k] = _;
				return chart;
			}
		})




		// scales accessors
		var scaleGetSet = function(scale){
			return {
				tickFormat:function(_){
						if (!arguments.length) return scale.setformat
						scale.setformat = _
						return chart;
				},
				label:function(_){
					if (!arguments.length) return scale.label;
					scale.label = _;
					return chart;
				},
				domain:function(_){
					if (!arguments.length && scale.fixedomain) return scale.fixedomain;
					if (!arguments.length) return null
					scale.fixedomain = _
					return chart
				}
			}
		}

    chart.yscale = scaleGetSet(yscale)
		chart.xscale = scaleGetSet(xscale)

    chart.addSerie = function(data,aes,options){
      if(!data && series.length>0)
        data = series[0].data
			if(!options.color)
				options.color = default_colors[series.length % default_colors.length]
      series.push({data:data,aes:aes,options:options})
      return chart
    }


    return chart;

  }

  //utils
  function functorkey(v)
  {
    return typeof v === "function" ? v : function(d) { return d[v]; };
  }

  function functorkeydefault(v,_default)
  {
    if(!v)
      return _default;
    return typeof v === "function" ? v : function(d) { return d[v]; };
  }

  function functorkeyscale(v,scale)
  {
    var f = typeof v === "function" ? v : function(d) { return d[v]; };
    return function(d){
      return scale(f(d))
    }
  }

	function keyNotNull(k){
		return function(d)
		{
			return d.hasOwnProperty(k) && d[k]!==null && !isNaN(d[k])
		}
	}


  //i want arrows function...
  function fk(v){
    return function(d){return d[v]};
  }


  return timeseries;
}

));
