// This file should throw an error because env cannot be used in the config.  it has to be used in a closure that is executed during
// the Metadata processing stage

if (env.lang2 == "en") {
    handlers.roots('gmd:distributionInfo//gmd:onLine[1]', 'gmd:identificationInfo/*', 'gmd:referenceSystemInfo')
} else {
    handlers.roots('gmd:distributionInfo//gmd:onLine[1]')
}
