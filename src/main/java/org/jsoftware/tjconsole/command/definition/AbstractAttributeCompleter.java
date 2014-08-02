package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;

import javax.management.MBeanAttributeInfo;
import java.util.List;

public abstract class AbstractAttributeCompleter implements Completer {
    private final TJContext ctx;
    private final String prefix;
    private final String appendTo;

    public AbstractAttributeCompleter(TJContext ctx, String prefix, String appendTo) {
        this.ctx = ctx;
        this.prefix = prefix;
        this.appendTo = appendTo;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        if (matches(buffer) && ctx.isBeanSelected()) {
            String namePrefix = buffer.substring(prefix.length()).trim();
            try {
                for (MBeanAttributeInfo ai : ctx.getAttributes()) {
                    if (condition(ai) && ai.getName().startsWith(namePrefix)) {
                        candidates.add(" " + ai.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();    // FIXME
                return -1;
            }
            return prefix.length();
        }
        return -1;
    }

    protected abstract boolean condition(MBeanAttributeInfo ai);

    private boolean matches(String input) {
        return input.trim().startsWith(prefix);
    }

}
