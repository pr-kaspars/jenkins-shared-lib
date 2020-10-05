void call(Map args = [:]) {
    def clusters = args['clusters'] ?: []
    def rolling = args['rolling'] ?: true

    helmLint([
            path      : 'chats/app',
            valuesPath: './helm',
            profiles  : ['prod', 'qa']
    ])
}
