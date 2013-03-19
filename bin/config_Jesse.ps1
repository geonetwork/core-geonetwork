$LOGFILE="file://$WEB_DIR/dev-config/log4j-jeichar.cfg"

$POSTGIS_INSTALL="POSTGIS 2 create"

$LOG = ".\web\geonetwork.log"
If (Test-Path $LOG -PathType Leaf) 
{
	rm $LOG
}