pipeline {
	agent { 
		label 'docker'
	}

	stages {
		stage("Prep") {
			steps {
				sh 'ln -s build.sh publish.sh'
			}
		}
		stage("Build") {
			steps {
				sh './build.sh'
			}	
		}

		stage("Publish") {
			steps {
				script }
					withCredentials([usernamePassword( credentialsId: 'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {

						docker.withRegistry('', 'dockerhub') {
							sh "docker login -u ${USERNAME} -p ${PASSWORD}"
							sh './publish.sh'
						}
					}
				}
			}			
		}
	}
	post {
        always {
            deleteDir()
        }
	}
}
