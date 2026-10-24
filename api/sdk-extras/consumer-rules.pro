# The server instantiates the shell service by name, through this constructor, in its own process.
-keep class eu.darken.porter.sdk.extras.internal.PorterShellService {
    public <init>();
}
