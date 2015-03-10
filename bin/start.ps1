param (
	[string]$mode = "build"
)

$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition
. "$scriptPath\config.ps1"

# Note:  JREBEL_HOME must _NOT_ have any spaces in path
if (!(test-path Env:\JREBEL_HOME)) {
	$JREBEL_OPTS=""
    echo "you do not have JREBEL installed.  If you want to use JREBEL install it and define JREBEL_HOME.  It can be defined in the config.sh file in your profile"
} else {
	if ($Env:JREBEL_HOME.contains(" ")) {
		echo "JREBEL_HOME: <<$Env:JREBEL_HOME>> must not contains spaces"
		exit 1
	}
	$JREBEL_OPTS="-noverify -javaagent:$Env:JREBEL_HOME\jrebel.jar"
	echo "Env:JREBEL_HOME is found.  Configuring JRebel. JREBEL_OPTS = $JREBEL_OPTS"
}

if ($mode -eq "build") {
	$Env:MAVEN_OPTS="$MEMORY"

    cmd /c "cd $scriptPath\.. && mvn install -P-all -DskipTests $args"
	if ($LastExitCode -ne 0) {
 	   throw "Command failed with exit code $LastExitCode."
}
}

$Env:MAVEN_OPTS="$JREBEL_OPTS $DEBUG $OVERRIDES $MEMORY -Dgeonetwork.dir=$DATA_DIR -Dfile.encoding=UTF8"
echo "Data dir = $DATA_DIR"
cmd /c "cd $WEB_DIR &&  mvn jetty:run -Penv-dev $args"
cd $scriptPath
