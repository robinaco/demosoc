

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
        AWS_ACCOUNT_ID = 'TU_CUENTA_AWS'        // Reemplaza con tu cuenta
        AWS_REGION = 'us-east-1'                 // Reemplaza con tu región
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

        // stage('Detectar Contexto') {
        //     steps {
        //         script {
        //             env.IS_PR = env.CHANGE_ID != null ? 'true' : 'false'
        //             env.PR_NUMBER = env.CHANGE_ID ?: ''
        //             echo "¿Es Pull Request? ${env.IS_PR}"
        //             if (env.IS_PR == 'true') {
        //                 echo "PR #${env.PR_NUMBER} - Branch: ${env.CHANGE_BRANCH} → ${env.CHANGE_TARGET}"
        //             }
        //         }
        //     }
        // }
        stage('Detectar Contexto') {
    steps {
        script {
            env.IS_PR = env.CHANGE_ID != null ? 'true' : 'false'
            env.PR_NUMBER = env.CHANGE_ID ?: ''
            // Definir aquí las variables que dependen de IS_PR
            env.USE_LOCALSTACK = env.IS_PR == 'true' ? 'true' : 'false'
            env.AWS_ENDPOINT_URL = env.IS_PR == 'true' ? 'http://host.docker.internal:4566' : ''
            echo "¿Es Pull Request? ${env.IS_PR}"
            if (env.IS_PR == 'true') {
                echo "PR #${env.PR_NUMBER} - Branch: ${env.CHANGE_BRANCH} → ${env.CHANGE_TARGET}"
                echo "Usando LocalStack: ${env.USE_LOCALSTACK}"
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
                            comentarEnPR("Quality Gate Falló. Revisa: ${SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}")
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

        // ============================================
        // NUEVO: Push a ECR (reemplaza Deploy Local)
        // ============================================
        // stage('Push to ECR') {
        //     when {
        //         expression { env.IS_PR == 'true' }
        //     }
        //     steps {
        //         script {
        //             // Login a AWS ECR
        //             sh """
        //                 aws ecr get-login-password --region ${AWS_REGION} | \
        //                     docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                        
        //                 docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}
        //                 docker push ${DOCKER_IMAGE}
        //             """
        //             echo "Imagen subida a ECR: ${DOCKER_IMAGE}"
        //         }
        //     }
        // }

        stage('Push to ECR') {
    when {
        expression { env.IS_PR == 'true' }
    }
    steps {
        script {
            if (env.USE_LOCALSTACK == 'true') {
                echo "📦 Usando LocalStack (simulación local)"
                // Crear repositorio en LocalStack si no existe
                sh """
                    aws --endpoint-url=http://localhost:4566 ecr create-repository \
                        --repository-name ${ECR_REPOSITORY} 2>/dev/null || true
                """
                // Taggear imagen para LocalStack
                sh """
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}
                    docker push localhost:4566/${ECR_REPOSITORY}:${IMAGE_TAG}
                """
                echo "✅ Imagen subida a LocalStack ECR"
            } else {
                // AWS real
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

        // ============================================
        // NUEVO: Terraform Deploy a ECS
        // ============================================
        // stage('Deploy to ECS') {
        //     when {
        //         expression { env.IS_PR == 'true' }
        //     }
        //     steps {
        //         script {
        //             // Crear directorio para Terraform si no existe
        //             sh "mkdir -p terraform/ecs"
                    
        //             // Crear archivos de Terraform
        //             dir('terraform/ecs') {
        //                 // Crear terraform.tfvars
        //                 sh """
        //                     cat > terraform.tfvars << 'EOF'
        //                     app_image = "${DOCKER_IMAGE}"
        //                     environment = "pr-${env.PR_NUMBER}"
        //                     pr_number = "${env.PR_NUMBER}"
        //                     desired_count = 1
        //                     aws_region = "${AWS_REGION}"
        //                     EOF
        //                 """
                        
        //                 // Inicializar y aplicar Terraform
        //                 sh """
        //                     terraform init
        //                     terraform plan
        //                     terraform apply -auto-approve
        //                 """
                        
        //                 // Obtener URL del ALB
        //                 def app_url = sh(script: "terraform output -raw app_url", returnStdout: true).trim()
        //                 echo "App desplegada en: ${app_url}"
                        
        //                 // Comentar en PR con la URL
        //                 comentarEnPR("""
        //                  **Pipeline exitoso para PR #${env.PR_NUMBER}**
                            
        //                     - **Quality Gate**: APROBADO
        //                     - **Imagen**: `${DOCKER_IMAGE}`
        //                     - **Ambiente**: pr-${env.PR_NUMBER}
        //                     - **URL**: ${app_url}
        //                 """)
        //             }
        //         }
        //     }
        // }
        stage('Deploy to ECS') {
    when {
        expression { env.IS_PR == 'true' }
    }
    steps {
        script {
            sh "mkdir -p terraform/ecs"
            dir('terraform/ecs') {
                if (env.USE_LOCALSTACK == 'true') {
                    // Configuración para LocalStack
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
                                ecs = "http://localhost:4566"
                                ecr = "http://localhost:4566"
                                iam = "http://localhost:4566"
                            }
                        }
                        EOF
                    """
                }
                
                // Crear terraform.tfvars
                sh """
                    cat > terraform.tfvars << 'EOF'
                    app_image = "${env.USE_LOCALSTACK == 'true' ? 'localhost:4566/' : ''}${ECR_REPOSITORY}:${IMAGE_TAG}"
                    environment = "pr-${env.PR_NUMBER}"
                    pr_number = "${env.PR_NUMBER}"
                    desired_count = 1
                    aws_region = "${AWS_REGION}"
                    EOF
                """
                
                // Inicializar y aplicar Terraform
                sh """
                    terraform init -reconfigure
                    terraform plan
                    terraform apply -auto-approve
                """
                
                // Obtener URL (simulada para LocalStack)
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