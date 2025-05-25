#!/usr/bin/env groovy

@Library('ciconia-jenkins-pipeline') _

import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException

properties([
    [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '10']],
    gitLabConnection('Ciconia GitLab'),
    pipelineTriggers([ciconiaGitlabPushTrigger("i9839458m3fv9tb329ierops9ri034m9rt039imr")])
])

node {

    try {

        stage('Clean workspace') {
            deleteDir()
        }

        stage('Checkout source') {
            checkout scm
            notifyGitlab('STARTED')
        }

        stage('Test application') {
            sh "chmod +x ./mvnw"
            timeout(15) {
                sh "./mvnw clean verify"
            }
        }

        stage('Package application') {
            when (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'development') {
                timeout(5) {
                    sh "./mvnw package -DskipTests"
                }
            }
        }

        stage('Archive artifacts') {
            when (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'development') {
                junit allowEmptyResults: true, testResults: '**/target/**/TEST*.xml'
                archiveArtifacts artifacts: '**/*.jar', fingerprint: true
            }
        }

        docker.withRegistry('https://registry.ciconia.cloud', 'dockerregistry') {
            stage('Build container') {
                when (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'development') {
                    sh "mv target/ecommerceapp-backend.jar docker"
                    app = docker.build("ecommerceapp/backend", 'docker')
                }
            }

            stage('Publish container') {
                when (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'development') {
                    def shortCommit = gitShortCommit()
                    app.push(shortCommit)

                    if (env.BRANCH_NAME == 'main') {
                        app.push("latestrelease")
                    } else if (env.BRANCH_NAME == 'development') {
                        app.push("latestsnapshot")
                    }

                    app.push("latest")
                }
            }
        }

    } catch (FlowInterruptedException e) {
        currentBuild.result = "ABORTED"
        throw e
    } catch (e) {
        currentBuild.result = "FAILED"
        throw e
    } finally {
        notifyGitlab(currentBuild.result)
        notifySlack(currentBuild.result, 'ciconia-internship')
    }
}