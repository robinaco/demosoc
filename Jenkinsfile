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
        USE_LOCALSTACK = 'true' 
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
                // expression { env.IS_PR == 'true' }
                 branch 'main'
                 expression { env.USE_LOCALSTACK == 'true' }
            }
            steps {
                script {
                    if (env.USE_LOCALSTACK == 'true') {
                        echo "📦 Usando LocalStack (simulación local)"
                        sh """
                            aws --endpoint-url=http://localhost:4566 ecr create-repository \
                                --repository-name ${ECR_REPOSITORY} 2>/dev/null || true
                        """
                        sh """
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}
                            docker push localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}
                        """
                        echo "✅ Imagen subida a LocalStack ECR"
                    } else {
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | \
                                docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}
                            docker push ${DOCKER_IMAGE}
                        """
                        echo "Imagen subida a AWS ECR: ${DOCKER_IMAGE}"
                    }
                }
            }
        }

        stage('Deploy to ECS') {
            when {
                // expression { env.IS_PR == 'true' }
                branch 'main'
                expression { env.USE_LOCALSTACK == 'true' }
            }
            steps {
                script {
                    sh "mkdir -p terraform/ecs"
                    dir('terraform/ecs') {
                        if (env.USE_LOCALSTACK == 'true') {
                            sh """
                                cat > provider.tf << 'EOF'
                                provider "aws" {
                                    region                      = "us-east-1"
                                    access_key                  = "test"
                                    secret_key                  = "test"
                                    skip_credentials_validation = true
                                    skip_metadata_api_check     = true
                                    skip_requesting_account_id  = true
                                    endpoints {
                                        ecs = "http://host.docker.internal:4566"
                                        ecr = "http://host.docker.internal:4566"
                                        iam = "http://host.docker.internal:4566"
                                    }
                                }
                                EOF
                            """
                        }
                        
                        sh """
                            cat > terraform.tfvars << 'EOF'
                            app_image = "${env.USE_LOCALSTACK == 'true' ? 'localhost:4566/' : ''}${ECR_REPOSITORY}:${IMAGE_TAG}"
                            environment = "pr-${env.PR_NUMBER}"
                            pr_number = "${env.PR_NUMBER}"
                            desired_count = 1
                            aws_region = "${AWS_REGION}"
                            EOF
                        """
                        
                        sh """
                            terraform init -reconfigure
                            terraform plan
                            terraform apply -auto-approve
                        """
                        
                        def app_url = env.USE_LOCALSTACK == 'true' 
                            ? "http://localhost:8083/api/personas (simulado con LocalStack)"
                            : sh(script: "terraform output -raw app_url", returnStdout: true).trim()
                        
                        echo "App desplegada en: ${app_url}"
                        
                        comentarEnPR("""
                         **Pipeline exitoso para PR #${env.PR_NUMBER}**
                            
                            - **Quality Gate**: APROBADO
                            - **Imagen**: `${DOCKER_IMAGE}`
                            - **Ambiente**: pr-${env.PR_NUMBER}
                            - **URL**: ${app_url}
                            - **Modo**: ${env.USE_LOCALSTACK == 'true' ? 'LocalStack (simulación)' : 'AWS Real'}
                        """)
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