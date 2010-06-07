/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Ext.air');

Ext.air.MusicPlayer = Ext.extend(Ext.util.Observable, {
	/**
	 * The currently active Sound. Read-only.
	 * @type air.Sound
	 * @property activeSound
	 */
	activeSound: null,
	/**
	 * The currently active SoundChannel. Read-only.
	 * @type air.SoundChannel
	 * @property activeChannel
	 */
	activeChannel: null,
	/**
	 * The currently active Transform. Read-only.
	 * @type air.SoundTransform
	 * @property activeTransform
	 */
	activeTransform: new air.SoundTransform(1, 0),
	// private 
	pausePosition: 0,
	/**
	 * @cfg {Number} progressInterval
	 * How often to fire the progress event when playing music in milliseconds
	 * Defaults to 500.
	 */
	progressInterval: 500,
	
	constructor: function(config) {
		config = config || {};
		Ext.apply(this, config);
		
		this.addEvents(
			/**
			 * @event stop
			 */
			'stop',
			/**
			 * @event pause
			 */
			'pause',
			/**
			 * @event play
			 */
			'play',
			/**
			 * @event load
			 */
			'load',
			/**
			 * @event id3info
			 */
			'id3info',
			/**
			 * @event complete
			 */
			'complete',
			/**
			 * @event progress
			 */
			'progress',
			/**
			 * @event skip
			 */
			'skip'
		);
		
		Ext.air.MusicPlayer.superclass.constructor.call(this, config);
		this.onSoundFinishedDelegate = this.onSoundFinished.createDelegate(this);
		this.onSoundLoadDelegate = this.onSoundLoad.createDelegate(this);
		this.onSoundID3LoadDelegate = this.onSoundID3Load.createDelegate(this);

		Ext.TaskMgr.start({
			run: this.notifyProgress,
			scope: this,
			interval: this.progressInterval
		});		
	},	

	/**
	 * Adjust the volume
	 * @param {Object} percent
	 * Ranges from 0 to 1 specifying volume of sound.
	 */
	adjustVolume: function(percent) {
		this.activeTransform.volume = percent;
		if (this.activeChannel) {		
			this.activeChannel.soundTransform = this.activeTransform;		
		}		
	},
	/**
	 * Stop the player
	 */
	stop: function() {
		this.pausePosition = 0;		
		if (this.activeChannel) {
			this.activeChannel.stop();			
			this.activeChannel = null;			
		}		
		if (this.activeSound) {
			this.activeSound.removeEventListener(air.Event.COMPLETE, this.onSoundLoadDelegate);
			this.activeSound.removeEventListener(air.Event.ID3, this.onSoundID3LoadDelegate);
			this.activeSound.removeEventListener(air.Event.SOUND_COMPLETE, this.onSoundFinishedDelegate);						
		}
	},
	/**
	 * Pause the player if there is an activeChannel
	 */
	pause: function() {
		if (this.activeChannel) {
			this.pausePosition = this.activeChannel.position;
			this.activeChannel.stop();			
		}		
	},
	/**
	 * Play a sound, if no url is specified will attempt to resume the activeSound
	 * @param {String} url (optional)
	 * Url resource to play
	 */
	play: function(url) {
		if (url) {			
			this.stop();			
			var req = new air.URLRequest(url);
			this.activeSound = new air.Sound();
			this.activeSound.addEventListener(air.Event.SOUND_COMPLETE, this.onSoundFinishedDelegate);						
			this.activeSound.addEventListener(air.Event.COMPLETE, this.onSoundLoadDelegate);			
			this.activeSound.addEventListener(air.Event.ID3, this.onSoundID3LoadDelegate);
			this.activeSound.load(req);						
		} else {
			this.onSoundLoad();	
		}	
	},
	
	/**
	 * Skip to a specific position in the song currently playing.
	 * @param {Object} pos
	 */
	skipTo: function(pos) {
		if (this.activeChannel) {
			this.activeChannel.stop();		
			this.activeChannel = this.activeSound.play(pos);	
			this.activeChannel.soundTransform = this.activeTransform;		
			this.fireEvent('skip', this.activeChannel, this.activeSound, pos);
		}
	},
	
	/**
	 * Returns whether or not there is an active SoundChannel.
	 */
	hasActiveChannel: function() {
		return !!this.activeChannel;
	},
	
	// private
	onSoundLoad: function(event) {
		if (this.activeSound) {
			if (this.activeChannel) {
				this.activeChannel.stop();
			}
			this.activeChannel = this.activeSound.play(this.pausePosition);
			this.activeChannel.soundTransform = this.activeTransform;
			this.fireEvent('load', this.activeChannel, this.activeSound);
		}		
	},
	// private
	onSoundFinished: function(event) {
		// relay AIR event
		this.fireEvent('complete', event);
	},
	// private
	onSoundID3Load: function(event) {
		this.activeSound.removeEventListener(air.Event.ID3, this.onSoundID3LoadDelegate);		
		var id3 = event.target.id3;		
		this.fireEvent('id3info', id3);
	},
	// private
	notifyProgress: function() {
		if (this.activeChannel && this.activeSound) {
			var playbackPercent = 100 * (this.activeChannel.position / this.activeSound.length);			
			// SOUND_COMPLETE does not seem to work consistently.
			if (playbackPercent > 99.7) {
				this.onSoundFinished();				
			} else {
				this.fireEvent('progress', this.activeChannel, this.activeSound);
			}	
		}		
	}		
});