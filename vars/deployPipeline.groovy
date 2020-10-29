import com.github.prkaspars.jenkins.Cluster
import com.github.prkaspars.jenkins.HelmCommandFactory
import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

import java.nio.file.Path
import java.nio.file.Paths

void call(Map args = [:]) {
    Closure<Iterable<String>> defaultCreateValues = { Cluster cluster ->
        [
                "values.yaml",
                "values.${cluster.profile}.yaml",
                "values.${cluster.name}.yaml"
        ].collect { it.toString() }
    }


    String baseDirectory = args.get("baseDirectory", ".")

    String chartsDirectory = args.get("chartsDirectory", "charts")
    String chartName = args["chartName"]

    String namespace = args["namespace"]

    Closure postDeploy = args["postDeploy"]
    Closure<Iterable<String>> createValues = args.get("createValues", defaultCreateValues)

    Map<String, Object> clusterDefaults = [
            "enabled"  : true,
            "namespace": namespace,
            "priority" : 1,
    ]

    List<Cluster> clusters = args.get("clusters", [])
            .collect { (clusterDefaults + it) as Map<String, Object> }
            .collect { createClusterObj() }

    Path basePath = Paths.get(baseDirectory, chartsDirectory, chartName)

    // Pipeline
    pipeline {
        agent any

        environment {
            ROLL_BACK = 'false'
        }

        stages {

            stage('Helm Lint') {
                steps {
                    script {
                        clusters.each { Cluster cluster ->
                            List<String> flags = createValues(cluster)
                                    .collect { basePath.resolve(it.toString()).toString() }
//                                    .findAll { fileExists(it) }
                                    .collect { "--values ${it}".toString() }

                            echo HelmCommandFactory.lint(chartsDirectory, flags)
                        }
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        clusters.each { deployStage(it, params.PROFILE, postDeploy) }
                    }
                }
            }

            stage('Rollback') {
                when {
                    expression {
                        return ROLL_BACK == 'true'
                    }
                }
                steps {
                    echo 'Rollback'
                }
            }
        }
    }
}

def createClusterObj(Map<String, Object> fieldValues) {
    Cluster cluster = new Cluster()
    fieldValues.each { field, value -> cluster."${field}" = value }
    return cluster
}

def deployStage(Cluster cluster, String profile, Closure postDeploy) {
    stage(cluster.name) {
        script {
            if (cluster.profile != profile || !cluster.enabled) {
                log.info "Stage '${cluster.name}' skipped"
                Utils.markStageSkippedForConditional(cluster.name)
            } else {
                log.info "deploy ${cluster.name}"
                if (postDeploy != null) {
                    postDeploy(cluster)
                }
            }
        }
    }
}
