package com.github.prkaspars.jenkins

class Cluster {
    boolean enabled
    String name
    String namespace
    String profile
    int priority

    String toString() {
        return name
    }
}
