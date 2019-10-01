pipeline {
	agent { 
		docker {
			image 'jordibetting/jordibetting:java8build-13' // published by buildagent branch
		}
	}

	stages {
		stage("Test") {
			steps {
				sh './gradlew --no-daemon -b build.gradle test jacocoRootReport'
			}
			post {
				always {
					sh 'touch automation_framework/build/test-results/test/*.xml'
					
					junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
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
			steps {
				sh './gradlew --no-daemon -b build.gradle clean assemble'
			}	
		}
		
		stage("Archive jar") {
			steps {
				archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar, **/.gradle/**', onlyIfSuccessful: true
			}
		}
		
		stage("Static code analysis")
		{
			when { 
				anyOf{
					expression{ env.BRANCNAME == 'master'{
					changeRequest()
				}
			}
			steps {
				sh './gradlew --no-daemon -b build.gradle check'
			}
			post {
				always {
					findbugs canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/build/reports/spotbugs/*.xml', unHealthy: ''
				}
			}
		}

		stage("Publish") {
			when { branch 'master' }
			steps {
				sh './gradlew --no-daemon -b build.gradle assemble publishToMavenLocal'
			}

			post {
				always {
					cleanWs()
				}
			}
		}
	}
}
