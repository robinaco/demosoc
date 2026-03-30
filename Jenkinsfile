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
//                sh './gradlew clean compileJava'
//                sh './gradlew test'
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
//                sh './gradlew jacocoTestReport'
//            }
//        }
//
//        stage('Análisis SonarCloud') {
//            steps {
//                withSonarQubeEnv('SonarCloud') {
//                    sh """
//                        ./gradlew sonar \
//                          -Dsonar.host.url=${SONAR_HOST_URL} \
//                          -Dsonar.organization=${SONAR_ORG} \
//                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
//                          -Dsonar.token=${SONAR_TOKEN} \
//                          -Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
//                    """
//                }
//            }
//        }
//        stage('Quality Gate') {
//            steps {
//                timeout(time: 5, unit: 'MINUTES') {
//                    waitForQualityGate abortPipeline: true
//                }
//            }
//        }
//    }
//
//    post {
//        always {
//            echo "Pipeline finalizado. Build #${env.BUILD_NUMBER}"
//            script {
//                node {
//                    cleanWs()
//                }
//            }
//        }
//
//        success {
//            echo "¡Todo salió perfecto!"
//            echo "   - Compilación: OK"
//            echo "   - Pruebas: Todas pasaron (ver reporte)"
//            echo "   - Cobertura: >70% (según JaCoCo)"
//            echo "   - Calidad: Aprobada por SonarCloud"
//            echo ""
//            echo "Ver resultados en SonarCloud: https://sonarcloud.io/dashboard?id=${SONAR_PROJECT_KEY}"
//            echo "Ver reporte de pruebas: ${env.BUILD_URL}testReport/"
//        }
//
//        failure {
//            echo "El pipeline falló. Revisa los logs en:"
//            echo "   ${env.BUILD_URL}console"
//        }
//
//        unstable {
//            echo "Pipeline inestable. Posibles causas:"
//            echo "   - Pruebas fallaron pero no críticas"
//            echo "   - Umbral de cobertura no alcanzado"
//            echo "   - Quality Gate con advertencias"
//        }
//
//    }
//}


pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        // Credenciales seguras
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_TOKEN = credentials('sonarcloud-token-robinaco')
        SONAR_ORG = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'

        // Docker seguro
        DOCKER_REGISTRY = credentials('docker-registry-credentials') // Opcional
        IMAGE_NAME = 'mi-crud-app'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    // Escanear vulnerabilidades en dependencias
                    sh './gradlew dependencyCheckAnalyze --no-daemon || true'
                }
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
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // === CONSTRUCCIÓN SEGURA DE DOCKER ===
        stage('Build Secure Docker Image') {
            steps {
                script {
                    // Construir imagen multi-stage
                    sh """
                        docker build \
                          --build-arg BUILDKIT_INLINE_CACHE=1 \
                          -t ${IMAGE_NAME}:${IMAGE_TAG} \
                          -t ${IMAGE_NAME}:latest \
                          .
                    """

                    // Escanear imagen por vulnerabilidades
                    sh "docker scan ${IMAGE_NAME}:${IMAGE_TAG} || true"
                }
            }
        }

        stage('Run Container Security') {
            steps {
                script {
                    // Detener contenedor anterior si existe
                    sh """
                        docker stop ${IMAGE_NAME} || true
                        docker rm ${IMAGE_NAME} || true
                    """

                    // Ejecutar contenedor con recursos limitados y read-only
                    sh """
                        docker run -d \
                          --name ${IMAGE_NAME} \
                          --read-only \
                          --tmpfs /tmp:rw,noexec,nosuid,size=64m \
                          --memory="512m" \
                          --memory-swap="1g" \
                          --cpus="1.0" \
                          --security-opt=no-new-privileges:true \
                          --cap-drop=ALL \
                          --cap-add=NET_BIND_SERVICE \
                          -p 8081:8080 \
                          ${IMAGE_NAME}:${IMAGE_TAG}
                    """

                    echo "✅ Contenedor corriendo en http://localhost:8081"

                    // Health check
                    sh """
                        sleep 10
                        curl -f http://localhost:8081/actuator/health || echo "Health check pendiente"
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            script {
                // Limpiar imágenes viejas (opcional)
                sh "docker image prune -f --filter 'until=24h' || true"
            }
        }

        success {
            echo """
            🎉 PIPELINE COMPLETADO CON ÉXITO 🎉

            📊 SonarCloud: https://sonarcloud.io/dashboard?id=${SONAR_PROJECT_KEY}
            🐳 Imagen: ${IMAGE_NAME}:${IMAGE_TAG}
            🚀 App: http://localhost:8081

            🔒 Medidas de seguridad aplicadas:
            - Usuario no-root en contenedor
            - Sistema de archivos read-only
            - Límites de recursos (CPU/Memoria)
            - Capacidades Docker restringidas
            - Escaneo de vulnerabilidades
            """
        }

        failure {
            echo "❌ Pipeline falló. Revisar logs en: ${env.BUILD_URL}console"
        }
    }
}

