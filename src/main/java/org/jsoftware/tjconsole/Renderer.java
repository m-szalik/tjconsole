package org.jsoftware.tjconsole;

/**
 * How to render a data type
 * @author szalik
 */
public interface Renderer {

    CharSequence render(TJContext tjContext, Object data);

}
