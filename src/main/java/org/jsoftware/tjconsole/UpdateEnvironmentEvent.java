package org.jsoftware.tjconsole;

/**
 * @author szalik
 */
public class UpdateEnvironmentEvent {
    private final String key;
    private final Object previous, current;

    public UpdateEnvironmentEvent(String key, Object previous, Object current) {
        this.key = key;
        this.previous = previous;
        this.current = current;
    }

    public String getKey() {
        return key;
    }

    public Object getPrevious() {
        return previous;
    }

    public Object getCurrent() {
        return current;
    }
}
