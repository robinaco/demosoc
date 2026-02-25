pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
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

        stage('Análisis SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './gradlew sonar'
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
            cleanWs()
            echo "Pipeline finalizado. Build #${env.BUILD_NUMBER}"
        }


        success {
            echo "¡Todo salió perfecto!"
            echo "   - Compilación: OK"
            echo "   - Pruebas: Todas pasaron (ver reporte)"
            echo "   - Cobertura: >70% (según JaCoCo)"
            echo "   - Calidad: Aprobada por SonarQube"
            echo ""
            echo "Ver resultados en SonarQube: http://sonarqube:9000/dashboard?id=demo"
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