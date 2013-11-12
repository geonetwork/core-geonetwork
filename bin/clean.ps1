$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition
. "$scriptPath\config.ps1"

if (Test-Path "$DATA_DIR" -PathType Any) {rm -Recurse -Force $DATA_DIR}
mkdir $DATA_DIR/config/

cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/config/codelist $DATA_DIR/config/
cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/config/schema_plugins $DATA_DIR/config/

rm web/*.db