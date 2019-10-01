pipeline {
	agent { 
		docker {
			image 'jordibetting/jordibetting:java8build-13' // published by buildagent branch
		}
	}

	stages {
		stage("Prepare") {
			steps {
				configFileProvider([configFile(fileId: "a1532914-342a-45f0-b94d-a6b1f8ea1385")]) {//gradle.properties
					sh 'ls -la'
					sh 'exit 1'
				}
			}
		}
		
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
				gradleBuild 'assemble publishToMavenRepository'
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
	sh script: "./gradlew --no-daemon -b build.gradle ${tasks}", label: "Gradle: ${tasks}"
}