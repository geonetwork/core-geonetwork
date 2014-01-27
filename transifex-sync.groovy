@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6' )

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class TranslationFile {
    String contentType
    String url
    String githubModifiedDate
}

def github = new HttpBuilder('https://api.github.com')
def githubBaseUrl = '/repos/geonetwork/core-geonetwork/contents/'

localizationDirectories = [ 
    [ url: 'web/src/main/webapp/loc', type: 'gnLoc']
    [ url: 'https://github.com/geonetwork/core-geonetwork/tree/develop/web/src/main/webapp/loc', type: 'gnLoc']
    
]

List<TranslationFile> listTranslationFiles = []



/* 
def http = new HTTPBuilder('https://www.transifex.com')

http.request( GET, JSON ) {
    uri.path = '/projects/p/core-geonetwork/resource/'
    uri.query = [ v:'1.0', q: 'Calvin and Hobbes' ]

    headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
    
*/