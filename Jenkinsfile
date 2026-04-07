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

    stage('Detectar Contexto') {
    steps {
        script {
            env.IS_PR = env.CHANGE_ID ? 'true' : 'false'
            env.PR_NUMBER = env.CHANGE_ID ?: ''

            // Detectar branch real (fallback cuando BRANCH_NAME viene null)
            def detectedBranch = env.BRANCH_NAME
            if (!detectedBranch?.trim()) {
                detectedBranch = sh(
                    script: 'git rev-parse --abbrev-ref HEAD',
                    returnStdout: true
                ).trim()
            }

            env.DETECTED_BRANCH = detectedBranch

            // Detectar si estamos en local o AWS
            def isLocal = fileExists('/.dockerenv')

            // Configuración según entorno
            if (env.DETECTED_BRANCH == 'main') {
                env.USE_LOCALSTACK = 'false'
                env.ENVIRONMENT = 'production'
                env.DEPLOY_REAL = 'true'
            } else if (isLocal && env.IS_PR == 'true') {
                env.USE_LOCALSTACK = 'true'
                env.ENVIRONMENT = "pr-${env.PR_NUMBER}"
                env.DEPLOY_REAL = 'false'
            } else {
                env.USE_LOCALSTACK = 'false'
                env.ENVIRONMENT = env.DETECTED_BRANCH ?: 'unknown'
                env.DEPLOY_REAL = 'false'
            }

            echo "=== Contexto ==="
            echo "Branch: ${env.DETECTED_BRANCH}"
            echo "PR: ${env.IS_PR}"
            echo "Ambiente: ${env.ENVIRONMENT}"
            echo "LocalStack: ${env.USE_LOCALSTACK}"
            echo "Deploy Real: ${env.DEPLOY_REAL}"
            echo "================"
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