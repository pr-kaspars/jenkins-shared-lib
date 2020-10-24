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

def deployStages(clusters) {
    clusters.each { deployStage(it) }
}

def deployStage(cluster) {
    stage(cluster) {
        script {
            echo "deploy ${cluster}"
        }
    }
}