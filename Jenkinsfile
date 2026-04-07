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
                    
                    def isLocalJenkins = fileExists('/.dockerenv') || sh(script: 'hostname', returnStdout: true).contains('jenkins')
                    env.USE_LOCALSTACK = (env.IS_PR == 'true' || isLocalJenkins) ? 'true' : 'false'
                    env.ENVIRONMENT = env.IS_PR == 'true' ? "pr-${env.PR_NUMBER}" : 'production'
                    
                    echo "=== Contexto ==="
                    echo "Pull Request: ${env.IS_PR}"
                    echo "Ambiente: ${env.ENVIRONMENT}"
                    echo "LocalStack: ${env.USE_LOCALSTACK}"
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
                    echo "✅ Imagen construida: ${IMAGE_NAME}:${IMAGE_TAG}"
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
                        echo "📦 LOCAL: Simulando push a ECR"
                        echo "✅ Simulación exitosa - ${IMAGE_NAME}:${IMAGE_TAG}"
                    } else {
                        echo "☁️ AWS: Push a ECR real"
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | \
                                docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                            
                            aws ecr describe-repositories --repository-names ${ECR_REPOSITORY} 2>/dev/null || \
                                aws ecr create-repository --repository-name ${ECR_REPOSITORY}
                            
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}
                            docker push ${DOCKER_IMAGE}
                        """
                        echo "✅ Imagen subida: ${DOCKER_IMAGE}"
                    }
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
                        if (env.USE_LOCALSTACK == 'true') {
                            echo "🚀 LOCAL: Simulando deploy en ECS"
                            sh """
                                cat > terraform.tfvars << EOF
                                app_image     = "localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}"
                                environment   = "${env.ENVIRONMENT}"
                                pr_number     = "${env.PR_NUMBER}"
                                use_localstack = true
                                EOF
                            """
                            sh """
                                terraform init -reconfigure
                                terraform plan
                                terraform apply -auto-approve
                            """
                            echo "✅ Simulación completada"
                        } else {
                            echo "☁️ AWS: Desplegando en ECS"
                            sh """
                                cat > terraform.tfvars << EOF
                                app_image     = "${DOCKER_IMAGE}"
                                environment   = "${env.ENVIRONMENT}"
                                pr_number     = "${env.PR_NUMBER}"
                                use_localstack = false
                                EOF
                            """
                            sh """
                                terraform init -reconfigure
                                terraform plan
                                terraform apply -auto-approve
                            """
                            
                            def output = sh(script: "terraform output -json", returnStdout: true).trim()
                            echo "✅ Deploy completado: ${output}"
                        }
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
            echo "🎉 Pipeline exitoso para ${env.ENVIRONMENT}"
            script {
                if (env.IS_PR == 'true') {
                    comentarEnPR("✅ Pipeline exitoso para PR #${env.PR_NUMBER}")
                }
            }
        }
        failure {
            echo "❌ Pipeline falló para ${env.ENVIRONMENT}"
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