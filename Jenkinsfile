pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        // SonarCloud
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_TOKEN = credentials('sonarcloud-token-robinaco')
        SONAR_ORG = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'

        // Docker / ECR (opcional por ahora)
        AWS_ACCOUNT_ID = '123456789012'  // Reemplaza después
        AWS_REGION = 'us-east-1'
        ECR_REPOSITORY = 'mi-crud-app'
        IMAGE_NAME = "${ECR_REPOSITORY}"
        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.substring(0,7) ?: 'local'}"
        DOCKER_IMAGE = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"
        
        // GitHub
        GITHUB_TOKEN = credentials('github-token')
        GITHUB_REPO = 'robinaco/demosoc'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detectar Contexto') {
            steps {
                script {
                    env.IS_PR = env.CHANGE_ID != null ? 'true' : 'false'
                    env.PR_NUMBER = env.CHANGE_ID ?: ''
                    echo "¿Es Pull Request? ${env.IS_PR}"
                    if (env.IS_PR == 'true') {
                        echo "PR #${env.PR_NUMBER} - Branch: ${env.CHANGE_BRANCH} → ${env.CHANGE_TARGET}"
                    }
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
                    script {
                        def sonarParams = """
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.organization=${SONAR_ORG} \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
                        """
                        
                        if (env.IS_PR == 'true') {
                            sonarParams += """
                                -Dsonar.pullrequest.key=${env.PR_NUMBER} \
                                -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH} \
                                -Dsonar.pullrequest.base=${env.CHANGE_TARGET} \
                                -Dsonar.pullrequest.provider=github
                            """
                        }
                        
                        sh "./gradlew sonar --no-daemon ${sonarParams}"
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
            post {
                failure {
                    script {
                        if (env.IS_PR == 'true') {
                            comentarEnPR("❌ Quality Gate Falló. Revisa: ${SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}")
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    echo "Imagen Docker construida: ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy Local Container') {
            when {
                expression { env.IS_PR == 'true' }
            }
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
                    echo "✅ Contenedor iniciado en http://localhost:8083/api/personas"
                    
                    comentarEnPR("""
                        ✅ **Pipeline exitoso para PR #${env.PR_NUMBER}**
                        - Quality Gate: APROBADO
                        - App: http://localhost:8083/api/personas
                    """)
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "🎉 Pipeline exitoso!"
        }
        failure {
            echo "❌ Pipeline falló"
        }
    }
}

// Función para comentar en GitHub PR
def comentarEnPR(String mensaje) {
    script {
        def escapedBody = mensaje.replace('"', '\\"').replace('\n', '\\n')
        sh """
            curl -X POST \
                -H "Authorization: token ${GITHUB_TOKEN}" \
                -H "Accept: application/vnd.github.v3+json" \
                https://api.github.com/repos/${GITHUB_REPO}/issues/${env.PR_NUMBER}/comments \
                -d '{"body": "${escapedBody}"}'
        """ 2>/dev/null || echo "No se pudo comentar en PR"
    }
}