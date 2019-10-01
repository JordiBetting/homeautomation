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
				sh './publish.sh'
			}
		}
	}
	post {
        always {
            deleteDir()
        }
	}
}
