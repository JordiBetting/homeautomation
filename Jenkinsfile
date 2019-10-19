pipeline {
	
	agent { 
		docker {
			image 'jordibetting/jordibetting:java8build-15' // published by buildagent branch
			args '--network jenkins-network -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock'
		}
	}

	stages {
		stage("Build Java") {
			steps {
				gradleBuild 'clean assemble'
//				stash includes: '**/automation_framework-*.jar,**/automation_autocontrol-*.jar', name: 'jars'
				archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar, **/.gradle/**, gradle/', onlyIfSuccessful: true
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
					publishJacoco()
				}
			}
		}
				
		stage("Build docker") {
			steps {
				dir("docker") {
//					unstash 'jars'
					sh './buildDockerImage.sh $(git -C ${WORKSPACE} rev-list --count HEAD)'
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
			parallel {
				stage("Publish jar") {
					steps {
						gradleBuild 'assemble publish'
					}

					post {
						always {
							cleanWs()
						}
					}
				}
				stage("Publish docker") {
					steps {
						dir("docker") {
							dockerSh './publishDockerImage.sh $(git -C ${WORKSPACE} rev-list --count HEAD)'
						}
					}
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

def dockerSh(String command) {
	script {
		withCredentials([usernamePassword( credentialsId: 'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
			docker.withRegistry('', 'dockerhub') {
				sh command
			}
		}
	}
}

def publishJacoco() {
	publishHTML (target: [
		allowMissing: false,
		alwaysLinkToLastBuild: true,
		keepAll: true,
		reportDir: 'build/reports/jacoco/jacocoRootReport/html',
		reportFiles: 'index.html',
		reportName: "Coverage Report (Excl. tests)"
		])
}