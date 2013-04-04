/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Xp','Xp.ui');

Xp.ui.Templates = {
	currentInfo: new Ext.Template(
		'<div class="xplayer-current-artist"><div class="xplayer-current-desc">Artist:</div> <div class="xplayer-current-content">{artist:defaultValue("Unknown")}</div></div>',
		'<div class="xplayer-current-title"><div class="xplayer-current-desc">Title:</div> <div class="xplayer-current-content">{songName:defaultValue("Unknown")}</div></div>',
		'<div class="xplayer-current-time">{position:this.formatTime} / {length:this.formatTime}</div>',
		{
			timeString: '{0}:{1}',
			formatTime: function(value) {
				var s = value / 1000;
				var seconds = Math.ceil(s % 60);
				var mins = Math.floor(s  / 60);				
				return String.format(this.timeString, mins, seconds < 10 ? '0'+seconds : seconds);
			}
		}
	),
	mainTitle: new Ext.Template('ExtPlayer - {artist:defaultValue("Unknown")} - {songName:defaultValue("Unknown")}')
};
