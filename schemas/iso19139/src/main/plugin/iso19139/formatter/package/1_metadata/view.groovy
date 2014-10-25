def isoHandlers = new iso19139.Handlers(handlers, f, env)

isoHandlers.addDefaultHandlers()

def otherPackageViews = [
        'gmd:identificationInfo', 'gmd:metadataMaintenance', 'gmd:metadataConstraints', 'gmd:spatialRepresentationInfo',
        'gmd:distributionInfo', 'gmd:applicationSchemaInfo', 'gmd:dataQualityInfo', 'gmd:portrayalCatalogueInfo',
        'gmd:contentInfo', 'gmd:metadataExtensionInfo']
handlers.add name: 'All element non-tab elements', select: {otherPackageViews.contains(it.name())}, priority: 10, {
    // do nothing.  we don't want to show the data from the tab elements
}