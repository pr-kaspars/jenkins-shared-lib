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
                        deployStages(clusters)
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

def deployStages(List<Cluster> clusters) {
    Comparator<Cluster> comparator = { a, b ->
        int p = (a.profile <=> b.profile)
        return (p == 0) ? (a.priority <=> b.priority) : p
    }

    clusters.stream()
            .sorted(comparator)
            .each { it -> deployStage(it) }
}

def deployStage(Cluster cluster) {
    stage(cluster.name) {
        script {
            if (cluster.name == 'baz') {
                Utils.markStageSkippedForConditional(cluster.name)
            }
            echo "deploy ${cluster.name}"
        }
    }
}
