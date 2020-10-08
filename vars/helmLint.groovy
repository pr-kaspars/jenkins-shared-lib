import com.github.prkaspars.jenkins.ClusterConfig
import com.github.prkaspars.jenkins.HelmCommandFactory

void call(Map args = [:]) {
    String chartDirectory = args['chartDirectory'] ?: 'charts'
    String valuesDirectory = args['valuesDirectory'] ?: '.'
    List<ClusterConfig> clusters = (args['clusters'] ?: []).collect { new ClusterConfig(it) }

    Closure<List<GString>> createValuesList = { it ->
        [
                "${valuesDirectory}/values.yaml",
                "${valuesDirectory}/values.${it.profile}.yaml",
                "${valuesDirectory}/values.${it.name}.yaml",
        ]
    }

    def tasks = clusters
            .collectEntries {
                def options = createValuesList(it)
                        .findAll { new File("${valuesDirectory}/values.${it}.yaml").canRead() }
                        .collect { "--values ${it}" }
                        .collect { it.toString() }

                def task = {
                    echo HelmCommandFactory.lint(chartDirectory, options)
                }

                def key = "${it.name}:${it.profile}".toString()

                [(key): task]
            }

    parallel tasks
}
