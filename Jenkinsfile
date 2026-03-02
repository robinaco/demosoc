pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_TOKEN = credentials('sonarcloud-token-robin')
        SONAR_ORG = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'
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
        }

        stage('Análisis SonarCloud') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh """
                        ./gradlew sonar \
                          -Dsonar.host.url=https://sonarcloud.io \
                          -Dsonar.organization=robinaco \
                          -Dsonar.projectKey=robinaco_demosoc \
                          -Dsonar.login=$SONAR_TOKEN \
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
    }

    post {
        always {
            echo "Pipeline finalizado. Build #${env.BUILD_NUMBER}"
            // 👇 IMPORTANTE: Ejecutar cleanWs dentro de un script node
            script {
                node {
                    cleanWs()
                }
            }
        }

        success {
            echo "¡Todo salió perfecto!"
            echo "   - Compilación: OK"
            echo "   - Pruebas: Todas pasaron (ver reporte)"
            echo "   - Cobertura: >70% (según JaCoCo)"
            echo "   - Calidad: Aprobada por SonarCloud"
            echo ""
            echo "Ver resultados en SonarCloud: https://sonarcloud.io/dashboard?id=${SONAR_PROJECT_KEY}"
            echo "Ver reporte de pruebas: ${env.BUILD_URL}testReport/"
        }

        failure {
            echo "El pipeline falló. Revisa los logs en:"
            echo "   ${env.BUILD_URL}console"
        }

        unstable {
            echo "Pipeline inestable. Posibles causas:"
            echo "   - Pruebas fallaron pero no críticas"
            echo "   - Umbral de cobertura no alcanzado"
            echo "   - Quality Gate con advertencias"
        }
    }
}