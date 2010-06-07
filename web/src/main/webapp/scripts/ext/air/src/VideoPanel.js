/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/**
 * @class Ext.air.VideoPanel
 * @extends Ext.Panel
 */
Ext.air.VideoPanel = Ext.extend(Ext.Panel, {
    // Properties
    autoResize: true,

    // Overriden methods
    initComponent: function() {
	var connection = new air.NetConnection();
	connection.connect(null);

	this.stream = new runtime.flash.net.NetStream(connection);
	this.stream.client = {
	    onMetaData: Ext.emptyFn
	};
	
        Ext.air.VideoPanel.superclass.initComponent.call(this);
	this.on('bodyresize', this.onVideoResize, this);
    },
    
    afterRender: function() {
        Ext.air.VideoPanel.superclass.afterRender.call(this);
	(function() {
            var box = this.body.getBox();
            this.video = new air.Video(this.getInnerWidth(), this.getInnerHeight());
            if (this.url) {
                this.video.attachNetStream(this.stream);
                this.stream.play(this.url);
            }
            nativeWindow.stage.addChild(this.video);
            this.video.x = box.x;
            this.video.y = box.y;
	}).defer(500, this);
    },
    
    // Custom Methods
    onVideoResize: function(pnl, w, h) {
	if (this.video && this.autoResize) {
            var iw = this.getInnerWidth();
            var ih = this.getInnerHeight();
            this.video.width = iw
            this.video.height = ih;
            var xy = this.body.getXY();
            if (xy[0] !== this.video.x) {
                    this.video.x = xy[0];
            }
            if (xy[1] !== this.video.y) {
                    this.video.y = xy[1];
            }
	}
    },
    
    loadVideo: function(url) {
	this.stream.close();
	this.video.attachNetStream(this.stream);
	this.stream.play(url);		
    }
    
});
Ext.reg('videopanel', Ext.air.VideoPanel);

