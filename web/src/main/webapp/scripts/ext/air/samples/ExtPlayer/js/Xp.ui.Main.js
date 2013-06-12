/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Xp','Xp.ui');

Xp.ui.Main = Ext.extend(Ext.Panel, {
	layout: 'anchor', 
	frame: true, 
	title: 'ExtPlayer',
	ctCls: 'xplayer-main',
	paused: false, 
	activeUrl: null,
	titleTpl: Xp.ui.Templates.mainTitle,
	initComponent: function() {
		this.tools = [{
			id: 'close',
			qtip: 'Close ExtPlayer',
			handler: this.appExit,
			scope: this
		}];
		this.currInfo = new Xp.ui.CurrentInfo({anchor: '100%'});
		this.slider = new Ext.Slider({
			value: this.pausePosition,
			anchor: '100%',
			increment: 1,
			minValue: 0,
			maxValue: 100
		});
		this.mp = new Ext.air.MusicPlayer();
		this.playListWin = new Ext.air.NativeWindow({
			id: 'playlistwin',
			chrome: 'none',
			transparent: true,
			type: 'lightweight',
			file: '../html/Playlist.html',
			width: 600,
			height: 300
		});
		this.items = [this.currInfo, this.slider];
		this.bbar = [{
			tooltip: 'Previous',
			icon: '../famfamfam/control_start_blue.png',
			cls: 'x-btn-icon',
			handler: this.prev,
			scope: this			
		},{
			tooltip: 'Pause',
			icon: '../famfamfam/control_pause_blue.png',					
			cls: 'x-btn-icon',					
			handler: this.pause,
			scope: this
		},{
			tooltip: 'Stop',
			icon: '../famfamfam/control_stop_blue.png',					
			cls: 'x-btn-icon',					
			handler: this.stop,
			scope: this
		},{
			tooltip: 'Play',
			icon: '../famfamfam/control_play_blue.png',					
			cls: 'x-btn-icon',					
			handler: this.onPlayBtnPress,
			scope: this
		},{
			tooltip: 'Next',
			icon: '../famfamfam/control_end_blue.png',
			cls: 'x-btn-icon',
			handler: this.next,
			scope: this
		},{
			tooltip: 'Adjust Volume',
			icon: '../famfamfam/sound_low.png',
			cls: 'x-btn-icon',										
			menu: new Ext.menu.SliderMenu({
				vertical: true,
				height: 80,
				value: 100,
				minValue: 0,
				maxValue: 100,
				changeHandler: this.adjustVolume,
				scope: this
			})
		},'->',{
			tooltip: 'Playlist Manager',
			icon: '../famfamfam/bullet_arrow_bottom.png',
			handler: this.openPlayList,
			scope: this,
			cls: 'x-btn-icon'					
		}];
		Xp.ui.Main.superclass.initComponent.call(this);

		this.slider.on('changecomplete', this.onSliderChange, this);
		this.slider.on('beforechange', this.beforeSliderChange, this);
		
		// id3info appears to fire 2x on every load, buffer it
		this.mp.on('id3info', this.setId3Info, this);
		this.mp.on('id3info', this.showNotify, this);
		this.mp.on('progress', this.updateUI, this);
		this.mp.on('complete', this.next, this);
		
		air.NativeApplication.nativeApplication.addEventListener(air.Event.EXITING, this.onExiting.createDelegate(this));
		var root = Ext.air.NativeWindow.getRootWindow();
		root.addEventListener(air.Event.CLOSING, this.onClosing.createDelegate(this));
		root.alwaysInFront = true;
		root.addEventListener(air.Event.ACTIVATE, function(ev) {
			Ext.air.NativeWindowManager.each(function(win) {
				win.instance.orderToFront();						
			});
		});
	},
	getCurrRecordInfo: function() {
		if (this.playListWin) {
			var win = this.playListWin.instance.stage.getChildAt(0).window;
			var ds = win.Ext.StoreMgr.lookup('playlistDs');
			var max = ds.getCount();
			return {
				idx: ds.indexOfId(this.activeUrl),
				max: max,
				ds: ds
			};
		}
		return false;		
	},
	prev: function() {
		var rInfo = this.getCurrRecordInfo();
		var prevIdx = rInfo.idx === 0 ? rInfo.max - 1 : rInfo.idx - 1;
		var r = rInfo.ds.getAt(prevIdx);
		this.play(r.id);		
	},
	next: function() {
		var rInfo = this.getCurrRecordInfo();
		var nextIdx = rInfo.idx >= (rInfo.max - 1) ? 0 : rInfo.idx + 1;
		var r = rInfo.ds.getAt(nextIdx);
		this.play(r.id);
	},
	pause: function() {
		this.mp.pause();
		this.paused = true;
	},
	stop: function() {
		this.mp.stop();
		this.moveSliderUI(0);
	},
	onPlayBtnPress: function(btn) {
		this.play();
	},
	play: function(url){
		if (this.paused && !url) {
			this.paused = false;
			this.mp.play();
		} else {
			this.moveSliderUI(0);
			this.mp.play(url);
			this.activeUrl = url;
		}
	},
	showNotify: function(id3info) {
		var msg = 'Title: {0}<br/>Artist: {1}';
		if (id3info && id3info.songName && id3info.artist) {
			var sample = new Ext.air.Notify({
				msg: String.format(msg, id3info.songName, id3info.artist),
				icon: '../famfamfam/music.png'
			});						
		}
	},
	adjustVolume: function(slider, value) {
		this.mp.adjustVolume(value / 100);
	},
	openPlayList: function() {		
		this.playListWin.show();
	},
	setId3Info: function(id3Info) {
		this.currInfo.setId3Info(id3Info);
		this.setTitle(this.titleTpl.apply(id3Info));
	},
	updateUI: function(activeChannel, activeSound) {
		var playbackPercent = 100 * (activeChannel.position / activeSound.length);
		this.moveSliderUI(playbackPercent);
		this.currInfo.update({
			position: activeChannel.position,
			length: activeSound.length
		});
	},
	moveSliderUI: function(value) {
		var sliderV = this.slider.translateValue(value);
		this.slider.moveThumb(sliderV, true);
	},
	
	beforeSliderChange: function() {
		return this.mp.hasActiveChannel();
	},
	
	// skip forward
	onSliderChange: function(slider, newValue){
		var pos = (newValue / 100) * this.mp.activeSound.length;
		this.mp.skipTo(pos);
	},
	appExit: function() {
		var exitingEvent = new air.Event(air.Event.EXITING, false, true);
		air.NativeApplication.nativeApplication.dispatchEvent(exitingEvent);
		if (!exitingEvent.isDefaultPrevented()) {
			air.NativeApplication.nativeApplication.exit();
		}
	},
	onExiting: function(exitEvent) {
		this.stop();
		var winClosingEvent;
		Ext.air.NativeWindowManager.each(function(win) {
			winClosingEvent = new air.Event(air.Event.CLOSING, false, true);
			win.instance.dispatchEvent(winClosingEvent);
			if (!winClosingEvent.isDefaultPrevented()) {
				win.instance.close();
			} else {
				exitEvent.preventDefault();
			}
		});
	},
	// when user closes main window exit app
	onClosing: function(closeEvent) {
		this.appExit();
	}
});
Ext.reg('xp:ui:main', Xp.ui.Main);