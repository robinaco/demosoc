pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6'  // Configurar en Jenkins previamente
        jdk 'JDK 17'          // Configurar en Jenkins previamente
    }

    environment {
        // Credenciales configuradas en Jenkins
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        SONAR_TOKEN = credentials('sonarqube-token')
        SONAR_HOST_URL = 'http://localhost:9000'
        DOCKER_IMAGE = 'tuusuario/mi-app:${BUILD_NUMBER}'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/tuusuario/mi-app.git'
            }
        }

        stage('Compilar y Pruebas Unitarias') {
            steps {
                sh 'mvn clean compile'
                sh 'mvn test'
            }
            post {
                success {
                    // Publicar reporte de pruebas
                    junit '**/target/surefire-reports/*.xml'
                    // Publicar reporte de cobertura (JaCoCo)
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        stage('Análisis Estático con SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {  // Configurar en Jenkins
                    sh 'mvn sonar:sonar -Dsonar.projectKey=mi-app -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}'
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

        stage('Empaquetar') {
            steps {
                sh 'mvn package -DskipTests'  // Ya probamos antes
            }
        }

        stage('Construir Imagen Docker') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}")
                }
            }
        }

        stage('Publicar Imagen en Docker Hub') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_HUB_CREDENTIALS) {
                        docker.image("${DOCKER_IMAGE}").push()
                        docker.image("${DOCKER_IMAGE}").push('latest')
                    }
                }
            }
        }

        stage('Desplegar en Entorno de Pruebas') {
            steps {
                sh """
                    docker stop mi-app || true
                    docker rm mi-app || true
                    docker run -d --name mi-app -p 8081:8080 ${DOCKER_IMAGE}
                """
            }
        }
    }

    post {
        always {
            cleanWs()  // Limpiar workspace
        }
        success {
            echo 'Pipeline completado exitosamente!'
            // Podrías enviar un correo de notificación
        }
        failure {
            echo 'Pipeline falló :('
        }
    }
}