//
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
//                docker stop ${IMAGE_NAME} || true
//                docker rm ${IMAGE_NAME} || true
//                docker run -d \
//                  --name ${IMAGE_NAME} \
//                  --restart unless-stopped \
//                  -p 8083:8080 \
//                  ${IMAGE_NAME}:${IMAGE_TAG}
//            """
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
//

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

        stage('Validación QA y Aprobación Técnica') {
            when {
                expression {
                    env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'origin/master'
                }
            }
            steps {
                script {
                    timeout(time: 24, unit: 'HOURS') {
                        def userInput = input(
                                id: 'deploymentApproval',
                                message: '✅ VALIDACIÓN REQUERIDA PARA DESPLIEGUE EN MAIN',
                                description: 'Ambas aprobaciones son obligatorias para continuar con el despliegue',
                                ok: 'Aprobar y Desplegar',
                                parameters: [
                                        booleanParam(
                                                name: 'QA_APPROVED',
                                                defaultValue: true,
                                                description: '✅ El equipo de QA ha validado los Quality Gates (marcar si está aprobado)'
                                        ),
                                        booleanParam(
                                                name: 'TECH_LEAD_APPROVED',
                                                defaultValue: true,
                                                description: '✅ El Líder Técnico ha aprobado el Pull Request (marcar si está aprobado)'
                                        ),
                                        string(
                                                name: 'QA_COMMENTS',
                                                defaultValue: 'Aprobado por QA',
                                                description: '📝 Comentarios de QA'
                                        ),
                                        string(
                                                name: 'TECH_LEAD_COMMENTS',
                                                defaultValue: 'Aprobado por Tech Lead',
                                                description: '📝 Comentarios del Líder Técnico'
                                        )
                                ]
                        )

                        // Mostrar resumen de la aprobación
                        echo "═══════════════════════════════════════════════════════════"
                        echo "📋 RESUMEN DE APROBACIÓN"
                        echo "───────────────────────────────────────────────────────────"
                        echo "✅ QA Aprobado: ${userInput['QA_APPROVED']}"
                        echo "   Comentarios QA: ${userInput['QA_COMMENTS']}"
                        echo "✅ Tech Lead Aprobado: ${userInput['TECH_LEAD_APPROVED']}"
                        echo "   Comentarios Tech Lead: ${userInput['TECH_LEAD_COMMENTS']}"
                        echo "═══════════════════════════════════════════════════════════"

                        // Validar aprobaciones
                        if (!userInput['QA_APPROVED']) {
                            error("""
❌ PIPELINE DETENIDO ❌
─────────────────────────────────────────────────────
El equipo de QA NO ha aprobado los Quality Gates.
Comentarios: ${userInput['QA_COMMENTS']}
Por favor, ejecuta el pipeline nuevamente cuando QA apruebe.
─────────────────────────────────────────────────────
                            """.stripIndent())
                        }

                        if (!userInput['TECH_LEAD_APPROVED']) {
                            error("""
❌ PIPELINE DETENIDO ❌
─────────────────────────────────────────────────────
El Líder Técnico NO ha aprobado el Pull Request.
Comentarios: ${userInput['TECH_LEAD_COMMENTS']}
Por favor, ejecuta el pipeline nuevamente cuando el Tech Lead apruebe.
─────────────────────────────────────────────────────
                            """.stripIndent())
                        }

                        echo "✅ ¡Todas las aprobaciones han sido concedidas! Continuando con el despliegue..."
                    }
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression {
                    env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'origin/master'
                }
            }
            steps {
                script {
                    echo "═══════════════════════════════════════════════════════════"
                    echo "🏗️  CONSTRUYENDO IMAGEN DOCKER"
                    echo "📦 Imagen: ${IMAGE_NAME}:${IMAGE_TAG}"
                    echo "═══════════════════════════════════════════════════════════"
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    echo "✅ Imagen Docker construida exitosamente"
                }
            }
        }

        stage('Run Container') {
            when {
                expression {
                    env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'origin/master'
                }
            }
            steps {
                script {
                    echo "═══════════════════════════════════════════════════════════"
                    echo "🚀 INICIANDO CONTENEDOR"
                    echo "───────────────────────────────────────────────────────────"
                    sh """
                        docker stop ${IMAGE_NAME} 2>/dev/null || true
                        docker rm ${IMAGE_NAME} 2>/dev/null || true
                        docker run -d \
                          --name ${IMAGE_NAME} \
                          --restart unless-stopped \
                          -p 8083:8080 \
                          ${IMAGE_NAME}:${IMAGE_TAG}
                    """
                    echo "═══════════════════════════════════════════════════════════"
                    echo "✅ Contenedor iniciado exitosamente"
                    echo "🌐 Aplicación disponible en: http://localhost:8083/api/personas"
                    echo "═══════════════════════════════════════════════════════════"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "═══════════════════════════════════════════════════════════"
            echo "🎉 PIPELINE EXITOSO 🎉"
            echo "───────────────────────────────────────────────────────────"
            echo "🌐 App: http://localhost:8083/api/personas"
            echo "📊 SonarCloud: https://sonarcloud.io/project/overview?id=${SONAR_PROJECT_KEY}"
            echo "═══════════════════════════════════════════════════════════"
        }
        failure {
            echo "═══════════════════════════════════════════════════════════"
            echo "❌ PIPELINE FALLÓ ❌"
            echo "───────────────────────────────────────────────────────────"
            echo "Revisa los logs para más detalles"
            echo "═══════════════════════════════════════════════════════════"
        }
    }
}
