pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
        // BRANCH_NAME = "${env.BRANCH_NAME}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compilar y Pruebas') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean compileJava'
                sh './gradlew test'
            }
            post {
                success {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Cobertura') {
            steps {
                sh './gradlew jacocoTestReport'
            }
            post {
                success {
                    // Opcional: publicar reporte de JaCoCo en Jenkins
                    jacoco execPattern: 'build/jacoco/test.exec'
                }
            }
        }

        stage('Análisis SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './gradlew sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completado exitosamente!'
        }
        failure {
            echo 'Pipeline falló. Revisa los logs.'
        }
    }
}