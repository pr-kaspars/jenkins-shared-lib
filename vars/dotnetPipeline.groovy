void call(Map args = [:]) {
    def clusters = args['clusters'] ?: []
    def rolling = args['rolling'] ?: true
    def wait = args['wait'] ?: true

    stages {
        helmLint([
                path      : 'chats/app',
                valuesPath: './helm',
                profiles  : ['prod', 'qa']
        ])
    }
}
