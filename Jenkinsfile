
pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_TOKEN = credentials('sonarcloud-token-robinaco')
        SONAR_ORG = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'

        IMAGE_NAME = 'mi-crud-app'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
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
                sh './gradlew clean compileJava --no-daemon'
                sh './gradlew test --no-daemon'
            }
            post {
                success {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Cobertura') {
            steps {
                sh './gradlew jacocoTestReport --no-daemon'
            }
        }

        stage('Análisis SonarCloud') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh """
                        ./gradlew sonar --no-daemon \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.organization=${SONAR_ORG} \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.token=${SONAR_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    echo "Imagen Docker construida"
                }
            }
        }


        stage('Run Container') {
            steps {
                script {
                    sh """
                docker stop ${IMAGE_NAME} || true
                docker rm ${IMAGE_NAME} || true
                docker run -d \
                  --name ${IMAGE_NAME} \
                  --restart unless-stopped \
                  -p 8083:8080 \
                  ${IMAGE_NAME}:${IMAGE_TAG}
            """
                    echo "Contenedor iniciado en http://localhost:8083/api/personas"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo " Pipeline exitoso! App en http://localhost:8083/api/personas"
        }
        failure {
            echo "Pipeline falló"
        }
    }
}



