$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition
. "$scriptPath\config.ps1"

if (!(test-path variable:\var)) {
	$JREBEL_OPTS=""
    echo "you do not have JREBEL installed.  If you want to use JREBEL install it and define JREBEL_HOME.  It can be defined in the config.sh file in your profile"
} else {
  $JREBEL_OPTS="-noverify -javaagent:$JREBEL_HOME/jrebel.jar"
}

$Env:MAVEN_OPTS=""

cmd /c "cd $JEEVES_DIR && mvn install $args"

#if (  ]; then
#    echo "[FAILURE] [deploy] Failed to execute 'jeeves' correctly"
#    exit -1
#fi

$Env:MAVEN_OPTS="$JREBEL_OPTS $DEBUG $OVERRIDES $MEMORY -Dgeonetwork.dir=$DATA_DIR $LOGGING -Dfile.encoding=UTF8 "

cmd /c "cd $WEB_DIR &&  mvn jetty:run -Penv-dev -Pwidgets $args"
cd $scriptPath