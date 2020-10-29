import com.github.prkaspars.jenkins.Cluster
import com.github.prkaspars.jenkins.HelmCommandFactory
import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

void call(Map args = [:]) {
    String chartsDirectory = args.get("chartsDirectory", "charts")
    String namespace = args["namespace"]
    Closure postDeploy = args["postDeploy"]

    Map<String, Serializable> clusterDefaults = [
            "enabled"  : true,
            "namespace": namespace,
            "priority" : 1,
    ]

    List<Cluster> clusters = args.get("clusters", [])
            .collect { clusterDefaults + it }
            .collect { cluster ->
                Cluster obj = new Cluster()
                cluster.each { key, value -> obj."${key}" = value }
                return obj
            }

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
                        clusters.each {
                            List<String> flags = [
                                    "-f values.yaml",
                                    "-f values.${it.profile}.yaml",
                                    "-f values.${it.name}.yaml",
                            ].collect { it.toString() }
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
