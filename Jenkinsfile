pipeline {
	
	agent { 
		docker {
			image 'jordibetting/jordibetting:java8build-24' // published by buildagent branch
			args '-v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -u jenkins:docker --network="sjenkins_jenkins-swarm"'
		}
	}
	options { 
		timestamps()
		preserveStashes() 
	}

	stages {
		stage("Prep") {
			steps {
				script {
					env.revnumber = sh (script: 'git rev-list --count HEAD', returnStdout: true).trim()
					env.gitbranch = sh (script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
					env.branchindicator = (env.gitbranch == 'master') ? '' : "${env.gitbranch}-"
					env.dockerTag = "jordibetting/jordibetting:gingerbeard-domotica-framework-${env.branchindicator}${env.revnumber}"
				}
				echo "Building ${env.dockerTag}"
			}
		}	
	
		stage("Build + test Java") {
			steps {
				gradleBuild 'assemble test jacocoRootReport'
			}
			post {
				success {
					archiveArtifacts artifacts: '**/*.jar', excludes: '**/jacocoagent.jar, **/.gradle/**, gradle/', onlyIfSuccessful: true
				}
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
					sh '''./buildDockerImage.sh ''' + env.dockerTag + ''' $(git rev-parse HEAD)'''
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
				}
				stage("Publish docker") {
					steps {
						dir("docker") {
							dockerSh './publishDockerImage.sh ${env.dockerTag}'
						}
						script {
							currentBuild.description = "${env.dockerTag}"
						}
					}
				}
			}
			post {
				always {
					cleanWs()
					sh '''docker image rm -f ''' + env.dockerTag
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