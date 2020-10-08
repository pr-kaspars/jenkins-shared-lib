import com.github.prkaspars.jenkins.ClusterConfig
import com.github.prkaspars.jenkins.HelmCommandFactory

void call(Map args = [:]) {
    args["releaseName"]

    String chartDirectory = args["chartDirectory"] ?: "charts"
    String valuesDirectory = args["valuesDirectory"] ?: "."
    List<ClusterConfig> clusters = (args["clusters"] ?: []).collect { new ClusterConfig(it) }

    List<String> baseOptions = ["--install"]

    if (args["wait"] ?: false) {
        baseOptions.add("--wait")
    }

    String release = null
    String chart = null

    Closure<List<GString>> createValuesList = {
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

                options.addAll(baseOptions)

                def task = {
                    echo HelmCommandFactory.upgrade(release, chart, options)
                }

                def key = "${it.name}:${it.profile}".toString()

                [(key): task]
            }

    parallel tasks
}
