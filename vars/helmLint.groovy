import com.github.prkaspars.jenkins.HelmCommandFactory

void call(Map args = [:]) {
    String path = args['path'] as String
    String valuesPath = args['valuesPath'] ?: '.'
    String[] profiles = args['profiles'] ?: []

    stage('Helm lint') {
        def tasks = profiles.collectEntries {
            String[] options = [
                    "-f ${valuesPath}/values.yaml",
                    "-f ${valuesPath}/values.${it}.yaml",
            ]
            def task = {
                echo HelmCommandFactory.lint(path, options)
            }
            [(it): task]
        }

        parallel tasks
    }
}
