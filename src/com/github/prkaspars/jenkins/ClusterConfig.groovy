package com.github.prkaspars.jenkins

@groovy.transform.Immutable
class ClusterConfig {
    String name
    String profile
    boolean enabled
    int priority
}
