$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition
. "$scriptPath\config.ps1"

if ( Test-Path "$DATA_DIR\config\schema_plugins" ) {
	echo "Deleting $DATA_DIR\config\schema_plugins"
	rm -Recurse -Force "$DATA_DIR\config\schema_plugins"
}

echo "Making soft link to $DATA_DIR\config\schema_plugins"
cmd /c mklink "$WEB_DIR\src\main\webapp\WEB-INF\data\config\schema_plugins" "$DATA_DIR\config\schema_plugins"
