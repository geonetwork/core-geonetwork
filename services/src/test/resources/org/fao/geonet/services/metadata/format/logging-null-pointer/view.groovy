handlers.roots ('gmd:distributionInfo//gmd:onLine[1]', 'gmd:identificationInfo/*', 'gmd:referenceSystemInfo')

handlers.add 'gmd:abstract', { el ->
    handlers.processElements(el.children(), null)
    '<div/>'
}