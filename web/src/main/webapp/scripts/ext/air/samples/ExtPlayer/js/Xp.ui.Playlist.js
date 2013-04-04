/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Xp','Xp.ui');

Xp.ui.Playlist = Ext.extend(Ext.grid.GridPanel, {
	title: 'Playlist Manager',
	initComponent: function() {
		this.store = new Ext.data.SimpleStore({
			storeId: 'playlistDs',
			fields: ['songName','artist','length','url'],
			id: 3
		});

		this.columns = [{
			id: 'title',
			header: 'Title',			
			dataIndex: 'songName',
			sortable: true,
			renderer: function(val) {
				return Ext.util.Format.defaultValue(val, 'Unknown');
			}
		},{
			id: 'artist',
			header: 'Artist',
			dataIndex: 'artist',
			sortable: true,
			renderer: function(val) {
				return Ext.util.Format.defaultValue(val, 'Unknown');
			}
		},{
			id: 'duration',
			header: 'Duration',
			dataIndex: 'length',
			sortable: true,
			timeString: '{0}:{1}',
			renderer: function(value) {
				var timeString = '{0}:{1}';
				value = value || 0;
				var s = value / 1000;
				var seconds = Math.ceil(s % 60);
				var mins = Math.floor(s  / 60);				
				return String.format(timeString, mins, seconds < 10 ? '0'+seconds : seconds);								
			}
		}];
		this.bbar = [{
			text: 'Add',
			icon: '../famfamfam/table_add.png',
			cls: 'x-btn-text-icon',
			handler: this.addBtnClick,
			scope: this
		},{
			text: 'Clear',
			icon: '../famfamfam/table_lightning.png',
			cls: 'x-btn-text-icon',
			handler: this.clearBtnClick,
			scope: this
		},{
			text: 'Remove',
			icon: '../famfamfam/table_delete.png',
			cls: 'x-btn-text-icon',
			handler: this.removeBtnClick,
			scope: this
		}];
		
		this.viewConfig = {
			forceFit: true,
			emptyText: 'Please add some songs to your playlist.',
			deferEmptyText: false
		};
		this.selModel = new Ext.grid.RowSelectionModel();
		Xp.ui.Playlist.superclass.initComponent.call(this);
		this.on('rowdblclick', this.onRowDblClick, this);
	},
	onRowDblClick: function(grid, rowIdx, e) {
		grid.getSelectionModel().clearSelections();
		var ds = grid.getStore();
		var r = ds.getAt(rowIdx);
		var win = Ext.air.NativeWindow.getRootHtmlWindow();
		win.Ext.getCmp('xpm').play(r.get('url'));
	},
	addBtnClick: function() {
		var browse = new air.File();
		browse.addEventListener(air.Event.SELECT, this.onDirSelected.createDelegate(this));
		browse.browseForDirectory('Select a Directory');
	},
	onDirSelected: function(event) {
		var songs = event.target.getDirectoryListing();
		var Song = this.store.recordType;
		var r;
		for (var i = 0; i < songs.length; i++) {
			if (songs[i].extension === 'mp3') {
				r = new Song({
					url: songs[i].url,
					songName: songs[i].url,
					length: 0,
					artist: 'Unknown'
				}, songs[i].url);
				this.store.add(r);
				
				var sound = new air.Sound(new air.URLRequest(r.id));
				var id3Handler = (function(event) {
					var currR = this.store.getById(event.target.url);
					if (currR.get('artist') === 'Unknown') {
						var id3 = event.target.id3;
						currR.set('songName', id3.songName);
						currR.set('artist', id3.artist);
						currR.commit();						
					}
				}).createDelegate(this);
				sound.addEventListener(air.Event.ID3, id3Handler);
				
				sound.addEventListener(air.Event.COMPLETE, (function(e) {
					var currR = this.store.getById(e.target.url);
					currR.set('length', e.target.length);
					currR.commit();					
				}).createDelegate(this));				
			}			
		}
	},
	clearBtnClick: function() {
		this.store.removeAll();
	},
	removeBtnClick: function() {
		var rs = this.getSelectionModel().getSelections();
		for (var i = 0; i < rs.length; i++) {
			this.store.remove(rs[i]);
		}
	}
});
Ext.reg('xp:ui:playlist',Xp.ui.Playlist);
