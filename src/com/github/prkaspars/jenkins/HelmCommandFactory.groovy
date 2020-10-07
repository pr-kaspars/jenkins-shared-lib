package com.github.prkaspars.jenkins

class HelmCommandFactory {

    static String upgrade(String release, String chart, Iterable<String> flags) {
        return (['helm', 'upgrade', release, chart] + flags).join(' ')
    }

    static String lint(String path, Iterable<String> flags) {
        return (['helm', 'lint', path] + flags).join(' ')
    }
}
