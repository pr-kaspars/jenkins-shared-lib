import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

void call(Map args = [:]) {
    List<String> clusters = [
            "foo", "bar", "baz", "qux"
    ]

    echo "${this}"

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

def deployStages(List<String> clusters) {
    clusters.each { deployStage(it) }
}

def deployStage(String cluster) {
    stage(cluster) {
        script {
            if (cluster == 'baz') (
                Utils.markStageSkippedForConditional(cluster)
            )
            echo "deploy ${cluster}"
        }
    }
}
