import com.github.prkaspars.jenkins.Cluster
import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

void call(Map args = [:]) {
    String namespace = args["namespace"]

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

    Closure postDeploy = { Cluster cluster ->
        echo "Post Deploy: ${cluster.name}"
    }

    // Pipeline
    pipeline {
        agent any

        environment {
            ROLL_BACK = 'false'
        }

        stages {

            stage('Deploy') {
                steps {
                    script {
                        deployStages(clusters, params.PROFILE, postDeploy)
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

def deployStages(List<Cluster> clusters, String profile, Closure postDeploy) {
    clusters.each { deployStage(it, profile, postDeploy) }
}

def deployStage(Cluster cluster, String profile, Closure postDeploy) {
    stage(cluster.name) {
        script {
            if (cluster.profile != profile) {
                log.info("Stage '${cluster.name}' skipped")
                Utils.markStageSkippedForConditional(cluster.name)
            } else {
                log.info("deploy ${cluster.name}")
                postDeploy(cluster)
            }
        }
    }
}
