
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


//pipeline {
//    agent any
//
//    tools {
//        jdk 'JDK17'
//    }
//
//    environment {
//        SONAR_HOST_URL = 'https://sonarcloud.io'
//        SONAR_TOKEN = credentials('sonarcloud-token-robinaco')
//        SONAR_ORG = 'robinaco'
//        SONAR_PROJECT_KEY = 'robinaco_demosoc'
//
//        IMAGE_NAME = 'mi-crud-app'
//        IMAGE_TAG = "${env.BUILD_NUMBER}"
//
//        // Usar docker-dind
//        DOCKER_HOST = 'tcp://docker-dind:2375'
//    }
//
//    stages {
//        stage('Checkout') {
//            steps {
//                checkout scm
//            }
//        }
//
//        stage('Compilar y Pruebas') {
//            steps {
//                sh 'chmod +x gradlew'
//                sh './gradlew clean compileJava --no-daemon'
//                sh './gradlew test --no-daemon'
//            }
//            post {
//                success {
//                    junit 'build/test-results/test/*.xml'
//                }
//            }
//        }
//
//        stage('Cobertura') {
//            steps {
//                sh './gradlew jacocoTestReport --no-daemon'
//            }
//        }
//
//        stage('Análisis SonarCloud') {
//            steps {
//                withSonarQubeEnv('SonarCloud') {
//                    sh """
//                        ./gradlew sonar --no-daemon \
//                          -Dsonar.host.url=${SONAR_HOST_URL} \
//                          -Dsonar.organization=${SONAR_ORG} \
//                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
//                          -Dsonar.token=${SONAR_TOKEN} \
//                          -Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
//                    """
//                }
//            }
//        }
//
//        stage('Quality Gate') {
//            steps {
//                timeout(time: 5, unit: 'MINUTES') {
//                    waitForQualityGate abortPipeline: true
//                }
//            }
//        }
//
//        stage('Validación QA y Aprobación Técnica') {
//            when {
//                expression {
//                    env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'origin/master'
//                }
//            }
//            steps {
//                script {
//                    def userInput = input(
//                            message: '✅ Validación requerida para despliegue en MAIN/MASTER',
//                            ok: 'Aprobar y Continuar',
//                            parameters: [
//                                    booleanParam(name: 'QA_APPROVED', defaultValue: false, description: '¿El equipo de QA ha validado los Quality Gates?'),
//                                    booleanParam(name: 'TECH_LEAD_APPROVED', defaultValue: false, description: '¿El Líder Técnico ha aprobado el Pull Request?')
//                            ]
//                    )
//
//                    if (!userInput['QA_APPROVED']) {
//                        error("❌ Pipeline detenido: QA no aprobó los Quality Gates")
//                    }
//                    if (!userInput['TECH_LEAD_APPROVED']) {
//                        error("❌ Pipeline detenido: Tech Lead no aprobó el PR")
//                    }
//                    echo "✅ Validaciones aprobadas - Continuando con despliegue"
//                }
//            }
//        }
//
//        stage('Build Docker Image') {
//            steps {
//                script {
//                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
//                    echo "Imagen Docker construida"
//                }
//            }
//        }
//
//        stage('Run Container') {
//            steps {
//                script {
//                    sh """
//                        docker stop ${IMAGE_NAME} || true
//                        docker rm ${IMAGE_NAME} || true
//                        docker run -d \
//                          --name ${IMAGE_NAME} \
//                          --restart unless-stopped \
//                          -p 8083:8080 \
//                          ${IMAGE_NAME}:${IMAGE_TAG}
//                    """
//                    echo "Contenedor iniciado en http://localhost:8083/api/personas"
//                }
//            }
//        }
//    }
//
//    post {
//        always {
//            cleanWs()
//        }
//        success {
//            echo " Pipeline exitoso! App en http://localhost:8083/api/personas"
//        }
//        failure {
//            echo "Pipeline falló"
//        }
//    }
//}
