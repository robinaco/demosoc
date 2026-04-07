pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        // SonarCloud
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_TOKEN = credentials('token_sonar_cloud')
        SONAR_ORG = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'

        // AWS / ECR / ECS
        AWS_ACCOUNT_ID = credentials('aws-account-id')
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
        stage('Detectar Contexto') {
            steps {
                script {
                    env.IS_PR = env.CHANGE_ID != null ? 'true' : 'false'
                    env.PR_NUMBER = env.CHANGE_ID ?: ''
                    
                    // Detectar si estamos en local o AWS
                    //def isLocal = fileExists('/.dockerenv') || hostname.contains('jenkins')
                    def isLocal = fileExists('/.dockerenv')
                    
                    // Configuración según entorno
                    if (env.BRANCH_NAME == 'main') {
                        env.USE_LOCALSTACK = 'false'
                        env.ENVIRONMENT = 'production'
                        env.DEPLOY_REAL = 'true'
                    } else if (isLocal && env.IS_PR == 'true') {
                        env.USE_LOCALSTACK = 'true'
                        env.ENVIRONMENT = "pr-${env.PR_NUMBER}"
                        env.DEPLOY_REAL = 'false'
                    } else {
                        env.USE_LOCALSTACK = 'false'
                        env.ENVIRONMENT = env.BRANCH_NAME ?: 'unknown'
                        env.DEPLOY_REAL = 'false'
                    }
                    
                    echo "=== Contexto ==="
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "PR: ${env.IS_PR}"
                    echo "Ambiente: ${env.ENVIRONMENT}"
                    echo "LocalStack: ${env.USE_LOCALSTACK}"
                    echo "Deploy Real: ${env.DEPLOY_REAL}"
                    echo "================"
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

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    echo "magen construida: ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Push to ECR') {
            when {
                anyOf {
                    branch('main')
                    expression { env.USE_LOCALSTACK == 'true' }
                }
            }
            steps {
                script {
                    if (env.USE_LOCALSTACK == 'true') {
                        echo "LOCAL: Push a LocalStack ECR"
                        sh """
                            aws --endpoint-url=http://localhost:4566 ecr create-repository \
                                --repository-name ${ECR_REPOSITORY} 2>/dev/null || true
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}
                            docker push localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}
                        """
                    } else if (env.DEPLOY_REAL == 'true') {
                        echo "AWS: Push a ECR real"
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | \
                                docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}
                            docker push ${DOCKER_IMAGE}
                        """
                    }
                    echo "Push completado"
                }
            }
        }

        stage('Deploy to ECS') {
            when {
                anyOf {
                    branch('main')
                    expression { env.USE_LOCALSTACK == 'true' }
                }
            }
            steps {
                script {
                    dir('terraform/ecs') {
                        def tfVars = """
                            app_image       = "${env.USE_LOCALSTACK == 'true' ? "localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}" : DOCKER_IMAGE}"
                            environment     = "${env.ENVIRONMENT}"
                            pr_number       = "${env.PR_NUMBER}"
                            aws_region      = "${AWS_REGION}"
                            use_localstack  = ${env.USE_LOCALSTACK == 'true'}
                            desired_count   = ${env.ENVIRONMENT == 'production' ? 2 : 1}
                        """
                        
                        writeFile file: 'terraform.tfvars', text: tfVars
                        
                        sh """
                            terraform init -reconfigure
                            terraform plan
                            terraform apply -auto-approve
                        """
                        
                        echo "Deploy completado en ${env.USE_LOCALSTACK == 'true' ? 'LocalStack' : 'AWS'}"
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
            echo "Pipeline exitoso para ${env.ENVIRONMENT}"
        }
        failure {
            echo "Pipeline falló"
        }
    }
}