#author : illumenum

setlocal EnableDelayedExpansion
set CP=lib/csw-client.jar;lib/csw-common.jar
for %%g in (../web/geonetwork/WEB-INF/lib/*.jar) do set CP=!CP!;"../web/geonetwork/WEB-INF/lib/%%g"
start javaw -cp %CP% org.fao.geonet.csw.client.CswClient
endlocal
