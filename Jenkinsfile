pipeline {
	agent { label 'java'
	}

	environment {
		JENKINS_NODE_COOKIE = 'dontKillMe' // this is necessary for the Gradle daemon to be kept alive
	}

	stages {
		stage("Test") {
			tools {
				gradle 'gradle5.1.1'
			}
			steps {
				sh 'gradle -b build.gradle test check jacocoRootReport'
			}
			post {
				always {
					sh 'touch automation_framework/build/test-results/test/*.xml'
					junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
					findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/build/reports/findbugs/*.xml', unHealthy: ''
					jacoco sourceExclusionPattern: '*/test/**'
					publishHTML (target: [
						allowMissing: false,
						alwaysLinkToLastBuild: true,
						keepAll: true,
						reportDir: 'build/reports/jacoco/jacocoRootReport/html',
						reportFiles: 'index.html',
						reportName: "Coverage Report (Excl. tests)"
						])
				}
			}
		}
		stage("Build") {
			tools {
				gradle 'gradle5.1.1'
			}
			steps {
				sh 'gradle -b build.gradle clean assemble'
			}	
		}

		stage("Analysis") {
			steps {
				sh 'sloccount --duplicates --wide --details ./ > sloccount.sc'
				sloccountPublish encoding: '', pattern: ''
				archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar', onlyIfSuccessful: true
			}
		}
			
		stage("Publish") {
			tools {
				gradle 'gradle5.1.1'
			}
			when { branch 'master' }
			steps {
				sh 'gradle -b build.gradle assemble publishToMavenLocal'
			}

			post {
				always {
					cleanWs()
				}
			}
		}
	}
}
