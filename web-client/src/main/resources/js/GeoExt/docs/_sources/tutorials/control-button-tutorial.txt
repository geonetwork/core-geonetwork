.. highlight:: javascript

============================
Map Tool Tutorial
============================

Okay, so now you know how to :doc:`add a map to a web page <mappanel-tutorial>`
and load some data into it. Your users can drag and zoom to their hearts'
content. You even followed the :doc:`layertree-tutorial` so they could switch
between different datasets. (You did follow that tutorial, right?) But now you
want to do more than just view some pretty pictures. You want to let your users
analyze data, or get more info about particular features on your map, or just
draw things. Basically, you want to give them some **tools**\ .

.. note:: This tutorial makes heavy use of the OpenLayers mapping library.  If
    you're not familiar with it, you might want to take a look at the
    :doc:`/primers/openlayers-primer` before moving forward.

OpenLayers Controls
===================

In `OpenLayers <http://openlayers.org/>`_\ , these tools for interacting with a
map are called ``Controls``\ . For the purposes of this tutorial, we'll just
stick to the ``Measure`` control, a handy little tool that lets you draw a line
on the map and tells you its length in real-world units.

.. seealso:: The OpenLayer API documentation for a comprehensive listing of standard controls.

ExtJS Buttons
=============

While OpenLayers ``Control``\ s provide a number of excellent ways of
interacting with maps, they have only limited support for actually manipulating
the controls; ie, choosing which tool to use and providing user feedback about
which tool is active. ExtJS provides a richer array of options for managing
tools. Here is the idiomatic way to create an ``Ext.Button`` which activates and
deactivates an OpenLayers ``Control``\ , and stays depressed while the control
is active::
    
    var control = new OpenLayers.Control.Measure(OpenLayers.Handler.Path, {
        eventListeners: {
            measure: function(evt) {
                alert("The measurement was " + evt.measure + evt.units);
            }
        }
    });

    mapPanel.map.addControl(control);
    var button = new Ext.Button({
        text: 'Measure Things',
        enableToggle: true,
        handler: function(toggled){
            if (toggled) {
                control.activate();
            } else {
                control.deactivate();
            }
        }
    });

The ``Button`` can be added to an ExtJS toolbar or to a panel, in whatever
layout we choose. For example, you could add the button to a ``MapPanel``\ 's
top toolbar::

    mapPanel.getTopToolbar().addButton(button);
   

There Can Be Only One
=====================

In general, when you have multiple tools associated with a map, you want to
avoid having more than one tool active at the same time. It would be somewhat
confusing if the user starts deleting data while he or she is trying to find the
distance from one end of town to the other! Fortunately, ExtJS makes it very
simple to ensure that only one toggle button from a group is toggled at a time,
through the ``toggleGroup`` property of the ``Button`` object. This is a string
identifying a group of buttons, only one of which can be pressed at a time.
Let's extend our example from before, this time adding the option to measure
area instead of length::
    
    var length = new OpenLayers.Control.Measure(OpenLayers.Handler.Path, {
        eventListeners: {
            measure: function(evt) {
                alert("The length was " + evt.measure + evt.units);
            }
        }
    });

    var area = new OpenLayers.Control.Measure(OpenLayers.Handler.Polygon, {
        eventListeners: {
            measure: function(evt) {
                alert("The area was " + evt.measure + evt.units);
            }
        }
    });

    mapPanel.map.addControl(length);
    mapPanel.map.addControl(area);

    var toggleGroup = "measure controls";

    var lengthButton = new Ext.Button({
        text: 'Measure Length',
        enableToggle: true,
        toggleGroup: toggleGroup,
        handler: function(toggled){
            if (toggled) {
                length.activate();
            } else {
                length.deactivate();
            }
        }
    });

    var area = new Ext.Button({
        text: 'Measure Area',
        enableToggle: true,
        toggleGroup: toggleGroup,
        handler: function(toggled){
            if (toggled) {
                area.activate();
            } else {
                area.deactivate();
            }
        }
    });

All right, you've got all you need to add and activate tools to help users get
the most out of your maps.
