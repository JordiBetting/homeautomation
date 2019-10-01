pipeline {
	agent { 
		docker {
			image 'jordibetting/jordibetting:java8build-13' // published by buildagent branch
			args '--network jenkins-network'
		}
	}

	stages {
		stage("Build") {
			steps {
				gradleBuild 'clean assemble'
			}
			post {
				success {
					archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar, **/.gradle/**', onlyIfSuccessful: true
				}
			}
		}
		
		stage("Test") {
			steps {
				gradleBuild 'test jacocoRootReport'
			}
			post {
				always {
					sh script: 'touch automation_framework/build/test-results/test/*.xml', label: "Ensure always publishing junit test results"
					
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
		
		stage("Static code analysis") {
			when { 
				anyOf{
					branch 'master'
					changeRequest()
				}
			}
			steps {
				gradleBuild 'check'
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
				gradleBuild 'assemble publish'
			}

			post {
				always {
					cleanWs()
				}
			}
		}
	}
}

def gradleBuild(String tasks) {
	configFileProvider([configFile(fileId: "a1532914-342a-45f0-b94d-a6b1f8ea1385", targetLocation: "gradle.properties")]) {//gradle.properties
		sh script: "./gradlew --no-daemon -b build.gradle ${tasks}", label: "Gradle: ${tasks}"
	}
}