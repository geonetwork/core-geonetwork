# HTML 5 UI 

This is an html5 based UI which uses the wigets from geonetwork and the library ExtJS.

It contains two maps: preview map and big map. You can access the big map
clicking on the preview map. Both maps have synchronized layers, so if you add
or remove (or change style) on one map layer, you will see that the other map
also changes.

Tested in Chrome, Firefox and IE>8 (also works but with some penalties on IE7).

## Changing Style

Basic changing styling is pretty easy with this UI:

### Colors
 
There is a file on web-client/src/main/resources/apps/html5ui/css/colors.css
which contains all the colors of the app.

### Page design

The html is loaded from web/src/main/webapp/xsl/search.xsl This xsl is
interpreted by jeeves and transformed to show the basic page. The page scheme is
basically the same on all links, so if you change the position of some html
elements, they will be changed on all views.

Don't forget that some of the elements are also placed with css.

Specific css for this UI (not shared with other UIs like search or tabsearch) is placed on web-client/src/main/resources/apps/html5ui/css/main.css

### Results view templates

They are on web/src/main/resources/apps/html5ui/js/Templates.js

### Add more tabs

To add more tabs just look on search.xsl around line 240 (id="main-navigation") and add a new element like this:


	<li>
		<a id="new-tab" href="//TODO">
			<xsl:value-of select="/root/gui/strings/new-tab" />
		</a>
	</li>

The value of the string will be taken from the strings.xml file that
corresponds to the language used.

### Add a footer link

Look for the div element with id = "footer" and just add it:
	
	<li>
		<a href="http://geonetwork-opensource.org/">GeoNetwork OpenSource</a>
	</li>

### Maps and other elements: change display behaviour 

Maps are always loaded even if they are not displayed. You can change this
behaviour and allow (for example) the big map to be shown at all times. This is
the same for all elements you see that disappear.

To change this behaviour you should take a look at GlobalFunctions.js file. For
each "view" you have one function that shows it and hides it. You can change
them to allow, for example, that the big map is not hidden when results are
shown:

 * showBrowse
 * hideBrowse
 * showAdvancedSearch
 * hideAdvancedSearch
 * showBigMap
 * ...
 
If you add a new "view", you should update all this functions so the view is
 hidden or shown when you want.

## Changing more complex features

### Debugging

To debug javascript you only have to add a "debug" or "debug=true" parameter to
the url like this: http://....../srv/eng/search?debug

### Adding more widgets

Widgets are usually added on the file
/web-client/src/main/resources/apps/html5ui/js/App.js or one of its children
(see next section).

### Global variable app
App.js creates the *app* global variable wich has (or should have) all the
information needed for the app to run.

It also initializes some secondary objects which contains information and loads more widgets:

        init : function() {

            this.initializeEnvironment();

            // Initialize utils
            this.loginApp = new GeoNetwork.loginApp();
            this.loginApp.init();
            this.mapApp = new GeoNetwork.mapApp();
            this.mapApp.init();
            this.searchApp = new GeoNetwork.searchApp();
            this.searchApp.init();

            if (urlParameters.create !== undefined && catalogue.isIdentified()) {
                var actionCtn = Ext.getCmp('resultsPanel').getTopToolbar();
                actionCtn.createMetadataAction.handler.apply(actionCtn);
            }
        }

        
#### app.loginApp

Should contain everything related to the authentication of the user like
control buttons to log in and log out and handles the cookie.

#### app.mapApp

Should control everything related to maps. For example, if you want to add a new layer to
the maps you should look here.

Also initializes the maps (preview and big).

#### app.searchApp

Closely related to Catalogue.js, it launches searches and initializes the
results view. To change the advanced search you have to look here too.

## More info

### History

The ExtJS History plugin is also used on this UI. It is not quite stable (not
at all on IE) but it can be modified to allow back button from browser to work.

### What is the div id="only_for_spiders"?

As the name says, this is for spiders or crawlers. When you access with the
direct link to a metadata, that div will be used to load plain xml data so
browsers can process it. Don't worry, if you are human you will not see it at
all.