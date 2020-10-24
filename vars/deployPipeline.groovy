void call(Map args = [:]) {
    List<String> clusters = [
            "foo", "bar", "baz", "qux"
    ]

    // Pipeline
    pipeline {
        agent any

        environment {
            ROLL_BACK = 'false'
        }

        stages {

            stage('Deploy') {
                stages {
                    deployStages(clusters)
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

def deployStages(clusters) {
    clusters.each { deployStage(it) }
}

def deployStage(cluster) {
    stage(cluster) {
        steps {
            echo "deploy ${cluster}"
        }
    }
}