pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        SONAR_HOST_URL   = 'https://sonarcloud.io'
        SONAR_TOKEN      = credentials('token_sonar_cloud')
        SONAR_ORG        = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'

        AWS_ACCOUNT_ID = credentials('aws-account-id')
        AWS_REGION     = 'us-east-1'
        ECR_REPOSITORY = 'qg-sonar-pipelines'
        IMAGE_NAME     = "${ECR_REPOSITORY}"

        ECS_CLUSTER        = 'qg-sonar-pipelines'
        ECS_SERVICE        = 'qg-sonar-pipelines-service'
        ECS_TASK_FAMILY    = 'qg-sonar-pipelines-task'
        ECS_CONTAINER_NAME = 'qg-sonar-pipelines-container'
    }

    stages {
        stage('Setup Context') {
            steps {
                script {
                    env.IS_PR = env.CHANGE_ID ? 'true' : 'false'
                    env.PR_NUMBER = env.CHANGE_ID ?: ''

                    def detectedBranch = env.BRANCH_NAME

                    if (!detectedBranch?.trim() || detectedBranch == 'HEAD') {
                        detectedBranch = sh(
                            script: "git branch -r --contains HEAD | grep origin/ | head -n 1 | sed 's|.*origin/||' | xargs",
                            returnStdout: true
                        ).trim()
                    }

                    if (!detectedBranch?.trim()) {
                        detectedBranch = 'unknown'
                    }

                    env.DETECTED_BRANCH = detectedBranch
                    env.DEPLOY_REAL = env.DETECTED_BRANCH == 'main' ? 'true' : 'false'

                    echo "=== Context ==="
                    echo "Branch: ${env.DETECTED_BRANCH}"
                    echo "PR: ${env.IS_PR}"
                    echo "Deploy Real: ${env.DEPLOY_REAL}"
                    echo "================"
                }
            }
        }

        stage('Compile and Test') {
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

        stage('SonarCloud Analysis') {
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
            when {
                expression { env.DETECTED_BRANCH == 'main' }
            }
            steps {
                script {
                    def shortCommit = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"
                    env.DOCKER_IMAGE = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"

                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    echo "Imagen construida: ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Push to ECR') {
            when {
                expression { env.DETECTED_BRANCH == 'main' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'jenkins_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'jenkins_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh 'chmod +x scripts/push-ecr.sh'
                    sh './scripts/push-ecr.sh'
                }
            }
        }

        stage('Deploy to ECS') {
            when {
                expression { env.DETECTED_BRANCH == 'main' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'jenkins_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'jenkins_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh 'chmod +x scripts/deploy-ecs.sh'
                    sh './scripts/deploy-ecs.sh'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Successful pipeline"
        }
        failure {
            echo "Pipeline failed"
        }
    }
}
