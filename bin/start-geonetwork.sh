cd ../jetty

DATETIME=$(date +"%Y-%m-%d_%H-%M-%S")
mv log/jeeves.log log/archive/$DATETIME.jeeves.log
rm log/*.log

java -Xmx512m -DSTOP.PORT=8079 -jar start.jar ../bin/jetty.xml 2>&1 | tee log/console.log &
