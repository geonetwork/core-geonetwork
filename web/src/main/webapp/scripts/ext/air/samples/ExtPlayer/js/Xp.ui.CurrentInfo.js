/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Xp','Xp.ui');

Xp.ui.CurrentInfo = Ext.extend(Ext.Panel, {
	tpl: Xp.ui.Templates.currentInfo,
	ctCls: 'xplayer-current',
	currInfo: {
		position: 0,
		length: 0
	},
	// this is applied into currInfo
	id3Info: {artist:'----------', songName: '----------'},
	afterRender: function() {
		Xp.ui.CurrentInfo.superclass.afterRender.apply(this, arguments);
		this.update(this.currInfo);
	},
	update: function(info) {
		Ext.apply(info, this.id3Info);
		this.tpl.overwrite(this.body, info);
	},
	setId3Info: function(id3Info) {
		this.id3Info.album = id3Info.album;
		this.id3Info.artist = id3Info.artist;
		this.id3Info.comment = id3Info.comment;
		this.id3Info.genre = id3Info.genre;
		this.id3Info.songName = id3Info.songName;
		this.id3Info.track = id3Info.track;
		this.id3Info.year = id3Info.year;
	}
});
Ext.reg('xp:ui:currentinfo', Xp.ui.CurrentInfo);
