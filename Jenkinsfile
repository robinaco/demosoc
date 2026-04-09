pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    environment {
        // SonarCloud
        SONAR_HOST_URL   = 'https://sonarcloud.io'
        SONAR_TOKEN      = credentials('token_sonar_cloud')
        SONAR_ORG        = 'robinaco'
        SONAR_PROJECT_KEY = 'robinaco_demosoc'

        // AWS / ECR / ECS
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
        stage('Detectar Contexto') {
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

                    echo "=== Contexto ==="
                    echo "Branch: ${env.DETECTED_BRANCH}"
                    echo "PR: ${env.IS_PR}"
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


        // stage('Push to ECR') {
        //     when {
        //         expression { env.DETECTED_BRANCH == 'main' }
        //     }
        //     steps {
        //         withCredentials([
        //               string(credentialsId: 'jenkins_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
        //               string(credentialsId: 'jenkins_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY')
        //         ]) {
        //             sh '''
        //                 aws ecr get-login-password --region ${AWS_REGION} | \
        //                 docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

        //                 docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}
        //                 docker push ${DOCKER_IMAGE}
        //             '''
        //         }
        //     }
        // }

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
        // stage('Deploy to ECS') {
        //     when {
        //         expression { env.DETECTED_BRANCH == 'main' }
        //     }
        //     steps {
        //         withCredentials([
        //               string(credentialsId: 'jenkins_access_key_id', variable: 'AWS_ACCESS_KEY_ID'),
        //               string(credentialsId: 'jenkins_secret_access_key', variable: 'AWS_SECRET_ACCESS_KEY')
        //         ]) {
        //             sh '''
        //                 set -euo pipefail

        //                 aws ecs describe-task-definition \
        //                   --task-definition "${ECS_TASK_FAMILY}" \
        //                   --region "${AWS_REGION}" \
        //                   --query 'taskDefinition' \
        //                   > current-task-def.json

        //                 jq --arg IMAGE "${DOCKER_IMAGE}" --arg CONTAINER "${ECS_CONTAINER_NAME}" '
        //                   {
        //                     family: .family,
        //                     taskRoleArn: .taskRoleArn,
        //                     executionRoleArn: .executionRoleArn,
        //                     networkMode: .networkMode,
        //                     containerDefinitions: (
        //                       .containerDefinitions
        //                       | map(if .name == $CONTAINER then .image = $IMAGE else . end)
        //                     ),
        //                     volumes: .volumes,
        //                     placementConstraints: .placementConstraints,
        //                     requiresCompatibilities: .requiresCompatibilities,
        //                     cpu: .cpu,
        //                     memory: .memory,
        //                     runtimePlatform: .runtimePlatform
        //                   }
        //                 ' current-task-def.json > new-task-def.json

        //                 NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
        //                   --cli-input-json file://new-task-def.json \
        //                   --region "${AWS_REGION}" \
        //                   --query 'taskDefinition.taskDefinitionArn' \
        //                   --output text)

        //                 aws ecs update-service \
        //                   --cluster "${ECS_CLUSTER}" \
        //                   --service "${ECS_SERVICE}" \
        //                   --task-definition "${NEW_TASK_DEF_ARN}" \
        //                   --region "${AWS_REGION}" \
        //                   --force-new-deployment

        //                 aws ecs wait services-stable \
        //                   --cluster "${ECS_CLUSTER}" \
        //                   --services "${ECS_SERVICE}" \
        //                   --region "${AWS_REGION}"
        //             '''
        //         }
        //     }
        // }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Pipeline exitoso"
        }
        failure {
            echo "Pipeline falló"
        }
    }
}
