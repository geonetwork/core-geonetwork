.. highlight:: javascript

====================
 Primer: OpenLayers
====================

The OpenLayers mapping library is the key component of GeoExt, performing the
core map-related functions of every GeoExt-based application. To get up to speed
with GeoExt, let's discover some OpenLayers basics.

Layers
======

As its name suggests, OpenLayers manages a list of layers that together form a
web-based mapping client. Each layer represents a different set of data. For
instance, one layer might be responsible for displaying the boundary of a
country. Another layer responsible for that country's roads.

OpenLayers contains many types of layers (you can see them all at the
`OpenLayers website
<http://trac.openlayers.org/browser/trunk/openlayers/lib/OpenLayers/Layer>`_).
For this primer, we'll focus on two different layer types: ``WMS`` and
``Vector``.

The WMS Layer
-------------

This is the canonical layer type found in almost all GeoExt applications, where
one or more images are used to display map-related information to the user. This
type is named 'WMS' because it implements the `Web Map Service
<http://www.opengeospatial.org/standards/wms>`_ standard set by the `Open
Geospatial Consortium. <http://www.opengeospatial.org/>`_

If you followed the :doc:`/tutorials/quickstart` guide, you will have already
encountered a ``MapPanel`` and created your very own WMS layer. Let's dissect
what you did:

    .. code-block:: javascript
        :linenos:
    
        var layer = new OpenLayers.Layer.WMS(
            "Global Imagery",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {layers: "bluemarble"}
        );
        map.addLayer(layer);
    
This tells OpenLayers that you'd like to create a new WMS layer referenced by
the ``layer`` variable, and that you'd like to add that layer to the map. In
this case, we're adding the `Blue Marble data set
<http://earthobservatory.nasa.gov/Features/BlueMarble/>`_ provided by NASA.

In **line 2** we provide "Global Imagery" as the name of the layer. This can be
anything, and is only used to reference the layer on screen.

In **line 3** we provide the location of the WMS server tasked with providing
the images. Here, we use a GeoWebCache instance located at
``maps.opengeo.org``\ .

In **line 4** we provide extra parameters for the WMS server. Since many servers
host different data sets, we need to specify which set we'd like. We do this by
creating a new object and setting the ``layers`` property to ``"bluemarble"``\ ,
the identifier for the Blue Marble data set.

Note that ``layers`` isn't the only WMS parameter we can provide. You can find
out more in the `OpenLayers API Documentation`_, by selecting 'Layer' and then
'WMS' in the navigation.

And that's it! Now let's move on to the vector layer.

The Vector Layer
----------------

The WMS Layer, and many of the layer types provided by OpenLayers, use raster
files (images like JPG, GIF, and PNG) to display maps. However, OpenLayers can
also render map features directly in the browser, simply by adding an
``OpenLayers.Layer.Vector`` to the map. This is useful when displaying data from
an OGC `Web Feature Service <http://www.opengeospatial.org/standards/wfs>`, a
KML document, or even sketched in by the user. Here's an example that generates
some random data and displays it in a vector layer::

    var vectorLayer = new OpenLayers.Layer.Vector();
    for (var i = 0; i < 10; i++){
        var x = -180 + Math.random() * 360;
        var y = -90 + Math.random() * 180;
        var numSides = 3 + Math.round(Math.random() * 6);
        vectorLayer.addFeature(
            new OpenLayers.Feature.Vector(
                OpenLayers.Geometry.Polygon.createRegularPolygon(
                    new OpenLayers.Geometry.Point(x, y),
                    numSides)));
    }

    var map = new OpenLayers.Map();
    map.addLayer(vectorLayer);

While OpenLayers provides customized vector layers for loading data from
existing sources, the GeoExt team recommends that you use the generic vector
layer and populate it using :class:`GeoExt.data.FeatureStore`\ . For more
information on doing this, see :doc:`/tutorials/remote-features-tutorial`\ .

Other Layers
------------

WMS and Vector are not the only layer types in OpenLayers. There are plenty more
available, including Google Maps, Virtual Earth, and many more. Browse the
`OpenLayers API documentation <http://dev.openlayers.org/apidocs>`_ for more
information. 

Controls
========

Although OpenLayers is great at managing layers, it also provides a way to
interact with those layers, primarily through the use of controls.

Controls are primary user interface elements and/or API hooks that control and
manage interaction with an OpenLayers map. For instance, panning and navigating
a map is handled by the ``OpenLayers.Control.Navigation`` control. If you want a
zoom bar in addition to zoom buttons, you'd add a ``PanZoomBar`` control. If you
then want to see where you've navigated, you'd use the ``NavigationHistory``
control.

Each control provides different and unique functionality. For this primer, we'll
focus only on the ``NavigationHistory`` control.


NavigationHistory Control
-------------------------

Take a look at the OpenLayers `NavigationHistory control example
<http://openlayers.org/dev/examples/navigation-history.html>`_. If you view the
source, you'll come across code like this:

    .. code-block:: javascript
       
        var map = new OpenLayers.Map('map');
        var nav = new OpenLayers.Control.NavigationHistory();
        map.addControl(nav);
       
The above code is fairly straightforward. First create a map, then a
``NavigationHistory`` control, and then finally add that control to the map. If
you were to then look at your map in a web browser, you would only see the
layers that you had added -- no special user interface elements for exploring
the navigation history.

This is because without more intervention, the NavigationHistory control only
provides an API allowing you to scroll through the history using a programmable
interface.

But the ``NavigationHistory`` control also provides a user interface. Let's
continue on through the example:

    .. code-block:: javascript
       
        panel = new OpenLayers.Control.Panel({
            div: document.getElementById("panel")
        });
        panel.addControls([nav.next, nav.previous]);
        map.addControl(panel);
       
To expose this interface, we first create a ``Panel`` control, and then add the
``next`` and ``previous`` buttons to the panel giving the user something to
click on. We finally add the panel to the map.

Now try the example again in your browser. *Beautiful ain't it?*

Initialization w/ Controls
--------------------------

In the above examples, we only added controls to the map using the
``map.addControl()`` method. Often, controls are added when the map is
initialized bypassing the ``map.addControl()`` method. This is done simply by
using the ``controls`` key and passing an array of controls, as seen below.

    .. code-block:: javascript
       
        var map = new OpenLayers.Map({
            controls: [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.Measure()
            ]
        });
       
.. note:: If you use the ``controls`` key, **you will not be given the default
    controls** when initializing the map. You will have to add those controls
    yourself instead. `Find out more.
    <http://docs.openlayers.org/library/controls.html>`_

More Controls
--------------

You can find more controls by 
`browsing the OpenLayers source code
<http://trac.openlayers.org/browser/trunk/openlayers/lib/OpenLayers/Control>`_
or by reading `OpenLayers' Control documentation
<http://docs.openlayers.org/library/controls.html>`_.



Events
======

Events are the main mechanism for notifying multiple objects that something has
happened. For instance, the ``NavigationHistory`` control listens to the map's
``zoomend`` event to save the user's zoom history for a later date; similarly,
other objects may listen to the same event without interfering or knowing about
the ``NavigationHistory`` control. This makes events very powerful, allowing
objects to perform their desired function while decreasing coupling within
OpenLayers and Ext applications.

Both GeoExt and OpenLayers make extensive use of events. However, the OpenLayers
events are slightly different from those in GeoExt, though they provide the same
functionality. Let's explore those differences.

GeoExt Events
-------------

GeoExt uses the event library that comes standard with Ext. GeoExt events are
synonymous with Ext events.

Ext events can be used in any Ext or GeoExt components that extend the
``Ext.util.Observable`` class. `More here.
<http://www.slideshare.net/sdhjl2000/ext-j-s-observable>`_

To throw an event in any component that extends ``Ext.util.Observable``, you
must first tell the component that the event may be thrown. For instance, in a
custom ``Ext.Panel`` class, this is done using the ``addEvents()`` method below.

.. code-block:: javascript
   
    var MyPanel = Ext.extend(Ext.Panel, {
        initComponent: function() {
            // ...
            this.addEvents("event1" /*, "event2", ... etc.*/ ); 
            
            MyPanel.superclass.initComponent.call(this);
        }
    });

Finally triggering the event is easy: 

.. code-block:: javascript
   
    var MyPanel = Ext.extend(Ext.Panel, {
         
        // ...
         
        myFunction: function() {
            var arg1 = "somevalue";
            this.fireEvent("event1", arg1 /*, arg2, ... etc. */);
        }
    });

Great! Now in order for the event to be useful, we have to listen to it. Below
is an example of adding two listeners to an instance of ``MyPanel`` using the
``on()`` function, and then finally triggering the event by calling
``myFunction()``. 

.. code-block:: javascript
   
    var panel = new MyPanel(/* ... */);
    
    // First listener.
    panel.on("event1", function(arg1) {
        alert("First listener responded. Got " + arg1 + "!");
    });
    
    // Second listener.
    panel.on("event1", function(arg1) {
        alert("Second listener responded. Got " + arg1 + "!");
    });

    panel.myFunction();
       
.. note:: The ``on()`` function takes an optional third parameter that specifies
    the scope of the listening function. If given, the ``this`` identifier
    within the listening function will refer to the object passed.
   
And that's it! Now let's see how to do the same thing in OpenLayers.

OpenLayers Events
-----------------

OpenLayers provides similar functionality as the ``Ext.util.Observable`` class,
but it does so using the ``OpenLayers.Events`` class. Unlike
``Ext.util.Observable``, OpenLayers classes do not extend ``OpenLayers.Events``.

Instead, it is customary for OpenLayers classes to create an attribute called
``events`` that is an instance of ``OpenLayers.Events``, as per the code below.

.. code-block:: javascript
   
    var MyControl = new OpenLayers.Class(OpenLayers.Control, {

        events: null,
        
        initialize: function() {
            this.events = new OpenLayers.Events(
                this,
                null,
                ["event1" /*, "event2", ... etc. */]
                false
            );
            
            OpenLayers.Control.prototype.initialize.call(this);
        }
    });
       
The first parameter to the ``OpenLayers.Events`` constructor is the object that
will 'own' these events -- in other words, the caller that triggers the event.
In situations like the example above, it is usually ``this``.

The second parameter specifies a ``div`` that will listen to events thrown by
the browser. Here, this functionality is ignored; see the note below.

The third parameter is an array specifying the events that this
``OpenLayers.Events`` object can throw. This is analogous to
``Ext.util.Observable``'s ``addEvents()`` method, and can accept any number of
events.

The fourth parameter is the ``fallthrough``, a boolean that is related to the
second parameter above. For our purposes, we'll leave it as ``false``.

.. note:: The ``OpenLayers.Events`` class handles both browser events like when
    the window resizes, as well as handling developer-created events like
    ``event1`` above. This makes initializing an ``OpenLayers.Events`` object
    fairly mucky, though using it like we did above is nearly the same. See more
    below. 

Triggering an event is just as easy as Ext's ``fireEvent()``, except here we use
``triggerEvent()``:

.. code-block:: javascript
   
    var MyControl = new OpenLayers.Class(OpenLayers.Control, {

        // ...
        
        myFunction: function() {
            var evt = {
                arg1: "somevalue" /*, arg2: ..., ... etc.*/
            }
            this.events.triggerEvent("event1", evt);
        }
    });
       
.. note:: ``OpenLayers.Events`` passes data to listeners using a single object
    with properties -- otherwise called 'the event object' -- instead of passing
    function arguments like Ext. All listener functions, then, should only
    expect one named argument. See example below.

Finally, let's add two listeners and call ``myFunction()``:

.. code-block:: javascript
   
    var control = new MyControl(/* ... */);
    
    // First listener.
    control.events.register("event1", null, function(evt) {
        alert("First listener responded. Got " + evt.arg1 + "!");
    });
   
    // Second listener.
    control.events.register("event1", null, function(evt) {
        alert("Second listener responded. Got " + evt.arg1 + "!");
    });

    control.myFunction();
       
.. note:: Like Ext's ``on()`` function, OpenLayer's ``register()`` function also
    takes an optional ``scope`` value in order to specify the scope of the
    listening function, but it expects this value as the second parameter passed
    to the function. We don't have a scope for our listeners in this example,
    hence the ``null`` parameters.
   
And that's it! Events in GeoExt should now be old hat. Fire away!

More Information
----------------

More information about both event types can be found at the links below:

* `OpenLayers Events Class Documentation <http://dev.openlayers.org/docs/files/OpenLayers/Events-js.html>`_
* `Ext.util.Observable Class Documentation <http://dev.sencha.com/deploy/dev/docs/?class=Ext.util.Observable>`_
* `Ext.util.Observable SlideShare <http://www.slideshare.net/sdhjl2000/ext-j-s-observable>`_
