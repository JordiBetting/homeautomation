pipeline {
	env.JENKINS_NODE_COOKIE = 'dontKillMe' // this is necessary for the Gradle daemon to be kept alive	
	agent { label 'java'
	}

	stages {
		stage("Build") {
			tools {
				gradle 'gradle4.10.2'
			}
			steps {
				sh 'echo Building with gradle'
				sh 'gradle -b build.gradle test check build jacocoTestReport'
			}
			post {
				always {
					sh 'touch automation_framework/build/test-results/test/*.xml'
					junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
				}
			}
		}
		stage("Analysis") {
			steps {
				sh 'touch automation_framework/build/test-results/test/*.xml'
				junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
				findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/build/reports/findbugs/*.xml', unHealthy: ''
				jacoco sourceExclusionPattern: '*/test/**'
				sh 'sloccount --duplicates --wide --details ./ > sloccount.sc'
				sloccountPublish encoding: '', pattern: ''
				archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar', onlyIfSuccessful: true
				publishHTML (target: [
					allowMissing: false,
					alwaysLinkToLastBuild: true,
					keepAll: true,
					reportDir: 'automation_framework/build/reports/jacoco/test/html/',
					reportFiles: 'index.html',
					reportName: "Coverage Report (Excl. tests)"
					])
			}
			post {
				always {
					cleanWs()
				}
			}
		}
	}
}
