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

	cmd /c "cd $scriptPath\..\common && mvn install $args"
	cmd /c "cd $scriptPath\..\domain && mvn install $args"
	cmd /c "cd $JEEVES_DIR && mvn install $args"
	cmd /c "cd $scriptPath\..\core && mvn install $args"
	cmd /c "cd $scriptPath\..\csw-server && mvn install $args"
	cmd /c "cd $scriptPath\..\healthmonitor && mvn install $args"
	cmd /c "cd $scriptPath\..\harvesters && mvn install $args"
	cmd /c "cd $scriptPath\..\services && mvn install $args"
}

$Env:MAVEN_OPTS="$JREBEL_OPTS $DEBUG $OVERRIDES $MEMORY -Dgeonetwork.dir=$DATA_DIR -Dfile.encoding=UTF8"

cmd /c "cd $WEB_DIR &&  mvn jetty:run -Penv-dev -Pwidgets $args"
cd $scriptPath
