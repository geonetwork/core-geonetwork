/**
 * @requires OpenLayers/Lang/fr.js
 */
OpenLayers.Util.extend(OpenLayers.Lang.fr, {
    'scale': "Echelle = 1 : ${scaleDenom}",
    'mf.tools': 'Outils',

    'mf.layertree': 'Arbre des couches',
    'mf.layertree.opacity': 'Opacité',
    'mf.layertree.remove': 'Supprimer',
    'mf.layertree.zoomToExtent': 'Zoom sur l\'emprise',    

    'mf.print.mapTitle': 'Titre',
    'mf.print.comment': 'Commentaires',
    'mf.print.unableToPrint': "Impossible d\'imprimer",
    'mf.print.generatingPDF': "Génération du PDF...",
    'mf.print.dpi': 'Résolution',
    'mf.print.scale': 'Échelle',
    'mf.print.rotation': 'Rotation',
    'mf.print.print': 'Imprimer',
    'mf.print.resetPos': 'Réinit. pos.',
    'mf.print.layout': 'Format',
    'mf.print.addPage': 'Ajouter page',
    'mf.print.remove': 'Enlever page',
    'mf.print.clearAll': 'Supprimer toutes',
    'mf.print.pdfReady': 'Votre document PDF est prêt.',
    'mf.print.noPage': 'Pas de page sélectionnée, appuyez sur le bouton "Ajouter page" pour en créer une.',
    'mf.print.print-tooltip': 'Générer un PDF contenant au moins l\'étendue de la carte',
    'mf.print.print-tooltip-email': 'Envoyer le PDF par email',
    'mf.print.serverDown': "Le service d\'impression ne fonctionne pas",

    'mf.error': 'Erreur',
    'mf.warning': 'Attention',
    'mf.information': 'Information',
    'mf.cancel': 'Annuler',

    'mf.recenter.x': 'X',
    'mf.recenter.y': 'Y',
    'mf.recenter.submit': 'Recentrer',
    'mf.recenter.missingCoords': 'Les coordonnées sont incomplètes.',
    'mf.recenter.outOfRangeCoords': 'Les coordonnées fournies (${myX}, ${myY}) sont en dehors de la carte.<br />' +
                                   'Elles doivent être comprises dans les limites suivantes :<br/>' +
                                  '${coordX} entre ${minCoordX} et ${maxCoordX},<br />' +
                                  '${coordY} entre ${minCoordY} et ${maxCoordY}',
    'mf.recenter.ws.error': 'Une erreur est survenue lors de l\'accès au webservice distant:',
    'mf.recenter.ws.service': 'Service sélectionné',

    'mf.control.previous': 'Vue précédente',
    'mf.control.next': 'Vue suivante',
    'mf.control.pan': 'Déplacer',
    'mf.control.zoomIn': 'Zoom avant',
    'mf.control.zoomOut': 'Zoom arrière',
    'mf.control.zoomAll': 'Vue globale',

    'mf.editing.comboNoneName': 'Aucun',
    'mf.editing.import': 'Importer',
    'mf.editing.importTooltip': 'Importer les données',
    'mf.editing.commit': 'Sauver',
    'mf.editing.commitTooltip': 'Sauvegarder les données',
    'mf.editing.delete': 'Supprimer',
    'mf.editing.deleteTooltip': 'Supprimer l\'élément sélectionné',
    'mf.editing.comboLabel': 'Couche à éditer',
    'mf.editing.confirmMessageTitle': 'Eléments édités',
    'mf.editing.confirmMessage': 'Certains éléments ne sont pas sauvegardés, ' +
                                 'changer de couche?',
    'mf.editing.selectModifyFeature': 'Modifier des éléments',
    'mf.editing.drawPointTitle': 'Dessiner des points',
    'mf.editing.drawLineTitle': 'Dessiner des lignes',
    'mf.editing.drawPolygonTitle': 'Dessiner des polygones',
    'mf.editing.formTitle': 'Attributs',
    'mf.editing.gridIdHeader': 'Id',
    'mf.editing.gridStateHeader': 'Etat',
    'mf.editing.gridTitle': 'Eléments sélectionnés',
    'mf.editing.onContextClickMessage': 'Editer cet élément',
    'mf.editing.onBeforeUnloadMessage': 'Le panneau d\'édition contient ' +
                                        'des éléments non sauvegardés'
});
