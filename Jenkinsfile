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

        // AWS / ECR / ECS
        AWS_ACCOUNT_ID = 'TU_CUENTA_AWS'
        AWS_REGION = 'us-east-1'
        ECR_REPOSITORY = 'mi-crud-app'
        IMAGE_NAME = "${ECR_REPOSITORY}"
        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.substring(0,7) ?: 'local'}"
        DOCKER_IMAGE = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"
        
        // GitHub
        GITHUB_TOKEN = credentials('github-token')
        GITHUB_REPO = 'robinaco/demosoc'

            ECS_CLUSTER_NAME = 'demosoc-cluster'
    ECS_SERVICE_NAME = 'demosoc-service'
    ECS_TASK_FAMILY = 'demosoc-task'
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
                    
                    // Detectar si estamos en Jenkins local (Docker)
                    def isLocalJenkins = fileExists('/.dockerenv') || sh(script: 'hostname', returnStdout: true).contains('jenkins')
                    
                    // Usar LocalStack si es PR O si es Jenkins local
                    env.USE_LOCALSTACK = (env.IS_PR == 'true' || isLocalJenkins) ? 'true' : 'false'
                    env.AWS_ENDPOINT_URL = env.USE_LOCALSTACK == 'true' ? 'http://host.docker.internal:4566' : ''
                    
                    echo "¿Es Pull Request? ${env.IS_PR}"
                    echo "¿Es Jenkins local? ${isLocalJenkins}"
                    echo "Usando LocalStack: ${env.USE_LOCALSTACK}"
                }
            }
        }

        stage('Debug Variables') {
            steps {
                script {
                    echo "=== DEBUG ==="
                    echo "CHANGE_ID: ${env.CHANGE_ID}"
                    echo "CHANGE_BRANCH: ${env.CHANGE_BRANCH}"
                    echo "CHANGE_TARGET: ${env.CHANGE_TARGET}"
                    echo "IS_PR: ${env.IS_PR}"
                    echo "PR_NUMBER: ${env.PR_NUMBER}"
                    echo "USE_LOCALSTACK: ${env.USE_LOCALSTACK}"
                    echo "============="
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
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.organization=robinaco \
                            -Dsonar.projectKey=robinaco_demosoc \
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

        stage('Push to ECR') {
    when {
        anyOf {
            branch 'main'
            expression { env.USE_LOCALSTACK == 'true' }
        }
    }
    steps {
        script {
            if (env.USE_LOCALSTACK == 'true') {
                echo "========================================="
                echo "📦 MODO LOCAL - Simulando Push a ECR"
                echo "========================================="
                echo "✅ SIMULACIÓN EXITOSA"
                echo "   Imagen: ${IMAGE_NAME}:${IMAGE_TAG}"
                echo "   Repositorio simulado: ${ECR_REPOSITORY}"
                echo "   Nota: En AWS real esto haría push a ECR"
                echo "========================================="
                
                // Crear archivo marker para que deploy sepa que es simulación
                sh "touch .localstack-simulation"
            } else {
                echo "========================================="
                echo "☁️ MODO AWS - Push a ECR Real"
                echo "========================================="
                sh """
                    aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                    
                    # Crear repositorio si no existe
                    aws ecr describe-repositories --repository-names ${ECR_REPOSITORY} || \
                        aws ecr create-repository --repository-name ${ECR_REPOSITORY}
                    
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}
                    docker push ${DOCKER_IMAGE}
                """
                echo "✅ Imagen subida a AWS ECR: ${DOCKER_IMAGE}"
                echo "========================================="
            }
        }
    }
}

        stage('Deploy to ECS') {
    when {
        anyOf {
            branch 'main'
            expression { env.USE_LOCALSTACK == 'true' }
        }
    }
    steps {
        script {
            if (env.USE_LOCALSTACK == 'true') {
                echo "========================================="
                echo "🚀 MODO LOCAL - Simulando Deploy a ECS"
                echo "========================================="
                echo "✅ SIMULACIÓN EXITOSA"
                echo "   Imagen: ${IMAGE_NAME}:${IMAGE_TAG}"
                echo "   Cluster simulado: demosoc-cluster"
                echo "   Servicio simulado: demosoc-service"
                echo "   URL simulada: http://localhost:8083/api/personas"
                echo ""
                echo "📝 NOTA: En AWS real esto desplegaría en ECS con:"
                echo "   - Task Definition actualizada"
                echo "   - Service actualizado"
                echo "   - Load Balancer configurado"
                echo "========================================="
            } else {
                echo "========================================="
                echo "☁️ MODO AWS - Deploy a ECS Real"
                echo "========================================="
                
                // Aquí va tu código real de Terraform o AWS CLI
                sh "mkdir -p terraform/ecs"
                dir('terraform/ecs') {
                    // Crear o actualizar task definition
                    sh """
                        cat > task-definition.json << 'EOF'
                        {
                            "family": "demosoc-task",
                            "containerDefinitions": [{
                                "name": "demosoc-app",
                                "image": "${DOCKER_IMAGE}",
                                "memory": 512,
                                "cpu": 256,
                                "essential": true,
                                "portMappings": [{
                                    "containerPort": 8080,
                                    "hostPort": 8080,
                                    "protocol": "tcp"
                                }]
                            }]
                        }
                        EOF
                        
                        # Registrar nueva task definition
                        aws ecs register-task-definition --cli-input-json file://task-definition.json
                        
                        # Actualizar servicio
                        aws ecs update-service \
                            --cluster demosoc-cluster \
                            --service demosoc-service \
                            --task-definition demosoc-task \
                            --force-new-deployment
                    """
                    
                    echo "✅ Deploy completado en AWS ECS"
                }
                
                def app_url = "http://tu-load-balancer.amazonaws.com/api/personas"
                echo "✅ App desplegada en: ${app_url}"
                echo "========================================="
            }
        }
    }
}
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Pipeline exitoso!"
        }
        failure {
            echo "Pipeline falló"
        }
    }
}

def comentarEnPR(String mensaje) {
    script {
        def escapedBody = mensaje.replace('"', '\\"').replace('\n', '\\n')
        sh """
            curl -X POST \
                -H "Authorization: token ${GITHUB_TOKEN}" \
                -H "Accept: application/vnd.github.v3+json" \
                https://api.github.com/repos/${GITHUB_REPO}/issues/${env.PR_NUMBER}/comments \
                -d '{"body": "${escapedBody}"}'
        """
    }
}