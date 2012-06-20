/**
 * @requires OpenLayers/Lang/de.js
 */
OpenLayers.Util.extend(OpenLayers.Lang.de, {
    'mf.tools': 'Werkzeuge', 

    'mf.layertree': 'Legendendarstellung',
    'mf.layertree.opacity': 'Transparenz',
    'mf.layertree.remove': 'Ausblenden',
    'mf.layertree.zoomToExtent': 'Auf die Ausdehnung zoomen',

    'mf.print.mapTitle': 'Titel',
    'mf.print.comment': 'Kommentar',
    'mf.print.loadingConfig': 'Laden der Konfiguration...',
    'mf.print.serverDown': 'Der Druck-Systemdienst funktioniert nicht',
    'mf.print.unableToPrint': "Kann nicht drucken",
    'mf.print.generatingPDF': "Generierung des PDFs...",
    'mf.print.dpi': 'DPI',
    'mf.print.scale': 'Maßstab',
    'mf.print.rotation': 'Rotation',
    'mf.print.print': 'Drucken',
    'mf.print.resetPos': 'Zurücksetzen',
    'mf.print.layout': 'Layout',
    'mf.print.addPage': 'Seite hinzufügen',
    'mf.print.remove': 'Seite entfernen',
    'mf.print.clearAll': 'Alles löschen',
    'mf.print.pdfReady': 'Das PDF-Dokument kann heruntergeladen werden.',
    'mf.print.noPage': 'Keine Seite ausgewählt, bitte auf "' + this['mf.print.addPage'] + '"-' +
                       'Button klicken um eine Seite hinzuzufügen.',
    'mf.print.print-tooltip': 'Ein PDF generieren, das mindestens den angezeigten Ausschnitt umfasst',
    'mf.print.print-tooltip-email': 'Ein PDF generieren und per E-mail versenden',

    'mf.error': 'Fehler',
    'mf.warning': 'Warnung',
    'mf.information': 'Information',
    'mf.cancel': 'Abbrechen',

    'mf.recenter.x': 'X',
    'mf.recenter.y': 'Y',
    'mf.recenter.submit': 'zentrieren',
    'mf.recenter.missingCoords': 'Fehlende Koordinaten.',
    'mf.recenter.outOfRangeCoords': 'Eingegebene Koordinaten (${myX}, ${myY}) sind außerhalb des Kartenperimeters<br />' +
                                    'und sollen im folgenden Auschnitt sein:<br/>' +
                                    '${coordX} zwischen ${minCoordX} und ${maxCoordX},<br />' +
                                    '${coordY} zwischen ${minCoordY} und ${maxCoordY}',
    'mf.recenter.ws.error': 'Ein Fehler ist beim Zugang zum Webdienst aufgetreten:',
    'mf.recenter.ws.service': 'Ausgewählter Webdienst',

    'mf.control.previous': 'Vorherige Ansicht',
    'mf.control.next': 'Nächste Ansicht',
    'mf.control.pan': 'Verschieben',
    'mf.control.zoomIn': 'Hineinzoomen',
    'mf.control.zoomOut': 'Herauszoomen',
    'mf.control.zoomAll': 'Gesamtansicht'
});
