import com.github.prkaspars.jenkins.ClusterConfig

void call(Map args = [:]) {
    List<ClusterConfig> clusters = args['clusters'] ?: [] as List<ClusterConfig>


}
