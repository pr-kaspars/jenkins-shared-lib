package com.github.prkaspars.jenkins

class HelmCommandFactory {

    static String upgrade(String release, String chart, String[] flags) {
        List<String> cmd = ["helm", "upgrade", release, chart]
        cmd.addAll(flags)
        return cmd.join(" ")
    }

    static String lint(String path, String[] flags) {
        List<String> cmd = ["helm", "lint", path]
        cmd.addAll(flags)
        return cmd.join(" ")
    }
}
