cd src/main/webapp
./buildJS
cd ../../..
mvn clean install -Dmaven.test.skip=true -U -o
