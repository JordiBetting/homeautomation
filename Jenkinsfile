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
		stage("Static code analysis")
		{
			steps {
				sh './gradlew --no-daemon -b build.gradle check'
			}
			post {
				always {
					findbugs canComputeNew: false, defaultEncoding: '', excludePattern: '', healthy: '', includePattern: '', pattern: '**/build/reports/spotbugs/*.xml', unHealthy: ''
				}
			}
		}
		stage("Archive") {
			steps {
				archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar', onlyIfSuccessful: true
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
