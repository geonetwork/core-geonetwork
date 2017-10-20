#!/usr/bin/groovy

// Load shared library
@Library('c2c-pipeline-library') import static com.camptocamp.utils.*

selectNodes {
    (it.memorysize_mb as Float) > 12000
}

def spawnContainer(def containerName, def containerImage) {
     sh "docker pull ${containerImage}"
     sh "docker run -it -d --privileged -v `pwd`:/home/build --name ${containerName} -w /home/build ${containerImage} /bin/bash"
}

def destroyContainer(def containerName) {
      sh "docker rm -f ${containerName} || true"
}

def executeInContainer(def containerName, def cmd) {
     sh "docker exec -i ${containerName} /bin/bash -c '${cmd}'"
}

dockerBuild {

    def mavenOpts = "-B -Dmaven.repo.local=./.m2_repo"
    def containerName = "sextant-geonetwork-builder"
    def containerImage = "3-jdk-8"

    stage('Getting the sources') {
        git url: 'git@github.com:camptocamp/sextant-geonetwork.git', branch: env.BRANCH_NAME, credentialsId: 'sextant-geonetwork-deploy-key'
        sh 'git submodule update --init --recursive'
    }
    stage('Spawning a container') {
        spawnContainer(containerName, containerImage)
    }
    stage('First build without test') {
        executeInContainer(containerName, "mvn clean install ${mavenOpts} -DskipTests")
    }

    stage('Second build with tests') {
        executeInContainer(containerName, "mvn clean install ${mavenOpts}  -fn")
    }
    stage('calculating coverage') {
        executeInContainer(containerName, "mvn cobertura:cobertura ${mavenOpts} -fn -Dcobertura.report.format=xml")
        step([$class: 'CoberturaPublisher',
              autoUpdateHealth: false,
              autoUpdateStability: false,
              coberturaReportFile: '**/target/site/cobertura/coverage.xml',
              failNoReports: true,
              failUnhealthy: false,
              failUnstable: false,
              maxNumberOfBuilds: 0,
              onlyStable: false,
              sourceEncoding: 'UTF_8',
              zoomCoverageChart: true])
    }
    stage("Saving tests results") {
        junit '**/target/surefire-reports/TEST-*.xml'
    }
    stage("Destroying build container") {
        destroyContainer(containerName)
    }
}

