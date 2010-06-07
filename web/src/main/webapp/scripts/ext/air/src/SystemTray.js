/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/**
 * @class Ext.air.SystemTray
 * @singleton
 *
 * 
 *
 */
Ext.air.SystemTray = function(){
	var app = air.NativeApplication.nativeApplication;
	var icon, isWindows = false, bitmaps;
	
	// windows
	if(air.NativeApplication.supportsSystemTrayIcon) {
                icon = app.icon;
                isWindows = true;
        }
    
	// mac
        if(air.NativeApplication.supportsDockIcon) {
            icon = app.icon;
        }
	
	return {
		/**
                 * Sets the Icon and tooltip for the currently running application in the
                 * SystemTray or Dock depending on the operating system.
                 * @param {String} icon Icon to load with a URLRequest
                 * @param {String} tooltip Tooltip to use when mousing over the icon
                 * @param {Boolean} initWithIcon Boolean to initialize with icon immediately
                 */
		setIcon : function(icon, tooltip, initWithIcon){
			if(!icon){ // not supported OS
                        	return;
			}
			var loader = new air.Loader();
			loader.contentLoaderInfo.addEventListener(air.Event.COMPLETE, function(e){
				bitmaps = new runtime.Array(e.target.content.bitmapData);
				if (initWithIcon) {
					icon.bitmaps = bitmaps;
				}
			});
                        
                        loader.load(new air.URLRequest(icon));
			if(tooltip && air.NativeApplication.supportsSystemTrayIcon) {
				app.icon.tooltip = tooltip;
			}
		},
		
                /**
                 * Bounce the OS X dock icon. Accepts a priority to notify the user
                 * whether the event which has just occurred is informational (single bounce)
                 * or critcal (continual bounce).
                 * @param priority {air.NotificationType} The priorities are air.NotificationType.INFORMATIONAL and air.NotificationType.CRITICAL.
                 */
		bounce : function(priority){
			icon.bounce(priority);
		},
		
		on : function(eventName, fn, scope){
			icon.addEventListener(eventName, function(){
				fn.apply(scope || this, arguments);
			});
		},
		
                /**
                 * Hide the custom icon
                 */
		hideIcon : function(){
			if(!icon){ // not supported OS
				return;
			}
			icon.bitmaps = [];
		},
		
                /**
                 * Show the custom icon
                 */
		showIcon : function(){
			if(!icon){ // not supported OS
				return;
			}
			icon.bitmaps = bitmaps;
		},
		
                /**
                 * Sets a menu for the icon
                 * @param {Array} actions Configurations for Ext.air.MenuItem's
                 */
		setMenu: function(actions, _parentMenu){
			if(!icon){ // not supported OS
				return;
			}
			var menu = new air.NativeMenu();
			
			for (var i = 0, len = actions.length; i < len; i++) {
				var a = actions[i];
				if(a == '-'){
					menu.addItem(new air.NativeMenuItem("", true));
				}else{
					var item = menu.addItem(Ext.air.MenuItem(a));
					if(a.menu || (a.initialConfig && a.initialConfig.menu)){
						item.submenu = Ext.air.SystemTray.setMenu(a.menu || a.initialConfig.menu, menu);
					}
				}
				
				if(!_parentMenu){
					icon.menu = menu;
				}
			}
			
			return menu;
		}
	};	
}();