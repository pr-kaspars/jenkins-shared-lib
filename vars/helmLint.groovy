import com.github.prkaspars.jenkins.ClusterConfig
import com.github.prkaspars.jenkins.HelmCommandFactory

void call(Map args = [:]) {
    String path = args['path'] as String
    String valuesPath = args['valuesPath'] ?: '.'
    List<ClusterConfig> clusters = (args['clusters'] ?: []).collect { new ClusterConfig(it) }

    def tasks = clusters
            .findAll { it.enabled }
            .collectEntries {
                List<GString> valuesList = [
                        "${valuesPath}/values.yaml",
                        "${valuesPath}/values.${it.profile}.yaml",
                        "${valuesPath}/values.${it.name}.yaml",
                ]

                def options = valuesList
                        .findAll { new File("${valuesPath}/values.${it}.yaml").canRead() }
                        .collect { "-f ${it}" }

                def task = {
                    echo HelmCommandFactory.lint(path, options)
                }
                [("${it.name}:${it.profile}"): task]
            }

    parallel tasks
}
