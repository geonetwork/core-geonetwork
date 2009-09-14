/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.air.Notify = Ext.extend(Ext.air.NativeWindow, {
	winType: 'notify',
	type: 'lightweight',
	width: 400,
	height: 50,
	chrome: 'none',
	transparent: true,
	alwaysOnTop: true,
	extraHeight: 22,
	hideDelay: 3000,
	msgId: 'msg',
	iconId: 'icon',
	icon: Ext.BLANK_IMAGE_URL,
	boxCls: 'x-box',
	extAllCSS: '../extjs/resources/css/ext-all.css',
	xtpl: new Ext.XTemplate(
		'<html><head><link rel="stylesheet" href="{extAllCSS}" /></head>',
			'<body>',
				'<div class="{boxCls}-tl"><div class="{boxCls}-tr"><div class="{boxCls}-tc"></div></div></div><div class="{boxCls}-ml"><div class="{boxCls}-mr"><div class="{boxCls}-mc">',
			    	'<div id="{msgId}">',
			    		'<span>{msg}</span>',
						'<div id="{iconId}" style="float: right;"><img src="{icon}"></div>',
			    	'</div>',
				'</div></div></div><div class="{boxCls}-bl"><div class="{boxCls}-br"><div class="{boxCls}-bc"></div></div></div>',
			'</body>',
		'</html>'
	),
	constructor: function(config) {
		config = config || {};
		Ext.apply(this, config);
		config.html = this.xtpl.apply(this);
		Ext.air.Notify.superclass.constructor.call(this, config);
		this.getNative().alwaysInFront = true;
		this.onCompleteDelegate = this.onComplete.createDelegate(this);
		this.loader.addEventListener(air.Event.COMPLETE, this.onCompleteDelegate);
	},
	onComplete: function(event) {
		this.loader.removeEventListener(air.Event.COMPLETE, this.onCompleteDelegate);
		this.show(event);												
	}, 
	show: function(event) {
		var h = event.target.window.document.getElementById(this.msgId).clientHeight + this.extraHeight;
		var main = air.Screen.mainScreen;
		var xy = [0,0];						
		xy[0] = main.visibleBounds.bottomRight.x - this.width;
		xy[1] = main.visibleBounds.bottomRight.y - this.height;	
		this.moveTo(xy[0], xy[1]);
		Ext.air.Notify.superclass.show.call(this);
		this.close.defer(this.hideDelay, this);
	}
});	