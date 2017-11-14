pipeline {
  agent any

  environment {
    MAJOR_VERSION = 1
  }

  stages {
    stage('build') {
      steps {
        sh 'javac -d . src/*.java'
        sh 'echo Main-Class: AxisAssetState > MANIFEST.MF'
        sh 'jar -cvmf MANIFEST.MF AxisAssetState.jar *.class'
      }
      post {
        success {
          archiveArtifacts artifacts: 'dist/*.jar'', fingerprint: true
        }
      }
    }
    stage('run') {
      steps {
        sh 'java -jar AxisAssetState.jar'
      }
    }
    stage('Promote Development to Master') {
      when {
        branch 'development'
      }
      steps {
        echo "Stashing Local Changes"
        sh "git stash"
        echo "Checking Out Development"
        sh 'git checkout development'
        sh 'git pull origin'
        echo 'Checking Out Master'
        sh 'git checkout master'
        echo "Merging Development into Master"
        sh 'git merge development'
        echo "Git Push to Origin"
        sh 'git push origin master'
      }
      post {
        success {
          emailext(
            subject: "${env.JOB_NAME} [${env.BUILD_NUMBER}] Development Promoted to Master",
            body: """<p>'${env.JOB_NAME} [${env.BUILD_NUMBER}]' Development Promoted to Master":</p>
            <p>Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
            to: "Mark.Fieldhouse@mafitconsulting.co.uk"
          )
        }
      }
    }
    stage('Archive Artifact') {
      when {
        branch 'master'
      }
      steps {
         sh "if [ ! -d '/var/www/html/AxisAssetState/all/${env.BRANCH_NAME}' ];then mkdir -p /var/www/html/AxisAssetState/all/${env.BRANCH_NAME};fi"
         sh "cp dist/AxisAssetState_${env.MAJOR_VERSION}.${env.BUILD_NUMBER}.jar /var/www/html/AxisAssetState/all/${env.BRANCH_NAME}/" 
      }
    }

    stage('Tagging the Release') {
      when {
        branch 'master'
      }
      steps {
        sh "git tag AxisAssetState-${env.MAJOR_VERSION}.${BUILD_NUMBER}"
        sh "git push origin AxisAssetState-${env.MAJOR_VERSION}.${BUILD_NUMBER}"
      }
      post {
        success {
          emailext(
            subject: "${env.JOB_NAME} [${env.BUILD_NUMBER}] NEW RELEASE",
            body: """<p>'${env.JOB_NAME} [${env.BUILD_NUMBER}]' NEW RELEASE":</p>
            <p>Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
            to: "Mark.Fieldhouse@mafitconsulting.co.uk"
          )
        }
      }
    }
  }
}
