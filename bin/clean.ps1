$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition
. "$scriptPath\config.ps1"

if (Test-Path "$DATA_DIR" -PathType Any) {rm -Recurse -Force $DATA_DIR}
mkdir $DATA_DIR/config/
mkdir $DATA_DIR/data

cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/config/codelist $DATA_DIR/config/
cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/data/formatter $DATA_DIR/data/

cp -R $scriptPath/../schemas/csw-record/src/main/plugin/* $DATA_DIR/config/schema_plugins
cp -R $scriptPath/../schemas/dublin-core/src/main/plugin/* $DATA_DIR/config/schema_plugins
cp -R $scriptPath/../schemas/fgdc-std/src/main/plugin/* $DATA_DIR/config/schema_plugins
cp -R $scriptPath/../schemas/iso19110/src/main/plugin/* $DATA_DIR/config/schema_plugins
cp -R $scriptPath/../schemas/iso19115/src/main/plugin/* $DATA_DIR/config/schema_plugins
cp -R $scriptPath/../schemas/iso19139/src/main/plugin/* $DATA_DIR/config/schema_plugins

rm web/*.db