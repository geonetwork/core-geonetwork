#author : illumenum

setlocal EnableDelayedExpansion
set CP=lib/csw-client.jar;lib/csw-common.jar
for %%g in (../web/WEB-INF/lib/*.jar) do set CP=!CP!;"../web/WEB-INF/lib/%%g"
java -cp %CP% org.fao.geonet.csw.client.CswClient
endlocal
