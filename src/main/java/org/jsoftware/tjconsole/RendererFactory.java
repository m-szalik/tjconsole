package org.jsoftware.tjconsole;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * How to render values
 * @author szalik
 */
public class RendererFactory {
    static final Renderer NULL_VALUE_RENDERER = new NullValueRenderer();
    private static final String SHIFT_SPACE = "   ";
    private static final RendererFactory INSTANCE = new RendererFactory();
    private final Renderer defaultRenderer = new ToStringRenderer();
    private final List<AbstractRendererMatcher> rendererList = new LinkedList<AbstractRendererMatcher>();

    public static RendererFactory getInstance() {
        return INSTANCE;
    }

    private RendererFactory() {
        rendererList.add(new EqRendererMatcher(new CompositeDataRenderer(), new String[] {CompositeDataSupport.class.getName(), CompositeData.class.getName()}));
        rendererList.add(new EqRendererMatcher(new TabularDataRenderer(), new String[] {TabularDataSupport.class.getName(), TabularData.class.getName()}));
        rendererList.add(new EqRendererMatcher(new DateRenderer(), new String[] {Date.class.getName()}));
        rendererList.add(new EqRendererMatcher(new VoidRenderer(), new String[] {"void"}));
        rendererList.add(new AbstractRendererMatcher(new ArrayRenderer()) {
            @Override
            boolean matches(String typeName) {
                return typeName.startsWith("[L");
            }
        });
    }

    public Renderer getRendererByTypeName(String typeName) {
        for(AbstractRendererMatcher rm : rendererList) {
            if (rm.matches(typeName)) {
                return rm.getRenderer();
            }
        }
        return defaultRenderer;
    }

    class TabularDataRenderer extends NullValueAwareAbstractRenderer<TabularData> {
        @Override
        public void renderMeTo(TJContext tjContext, TabularData td, StringBuilder output) {
            output.append("TabularData of ").append(td.getTabularType().getRowType().getTypeName()).append(" {\n");
            int index = 0;
            for (Object o : td.values()) {
                output.append(SHIFT_SPACE).append("[").append(Integer.toString(index)).append("]: ");
                Renderer renderer = getRendererByTypeName(o.getClass().getName());
                CharSequence vOut = renderer.render(tjContext, o);
                vOut = vOut.toString().trim().replace("\n", "\n" + SHIFT_SPACE);
                output.append(vOut).append('\n');
            }
            output.append("}\n");
        }
    }

    class CompositeDataRenderer extends NullValueAwareAbstractRenderer<CompositeData> {
        @Override
        public void renderMeTo(TJContext tjContext, CompositeData cd, StringBuilder output) {
            output.append("CompositeData of ").append(cd.getCompositeType().getTypeName()).append(" {\n");
            for (String key : cd.getCompositeType().keySet()) {
                output.append(SHIFT_SPACE).append("@|cyan ").append(key).append("|@ = ");
                Object o = cd.get(key);
                CharSequence vOut;
                if (o != null) {
                    Renderer renderer = getRendererByTypeName(o.getClass().getName());
                    vOut = renderer.render(tjContext, o);
                } else {
                    vOut = RendererFactory.NULL_VALUE_RENDERER.render(tjContext, o);
                }
                output.append(vOut.toString().trim().replace("\n", "\n" + SHIFT_SPACE)).append('\n');
            }
            output.append("}");
        }
    }

    class ArrayRenderer extends NullValueAwareAbstractRenderer<Object> {
        @Override
        public void renderMeTo(TJContext tjContext, Object data, StringBuilder output) {
            output.append("Array[\n");
            for (int i = 0; i < Array.getLength(data); i++) {
                Object o = Array.get(data, i);
                output.append("[").append(String.valueOf(i)).append("]: ");
                CharSequence oOut = getRendererByTypeName(o.getClass().getName()).render(tjContext, o);
                output.append(oOut);
                output.append('\n');
            }
            output.append("]\n");
        }
    }
}

class VoidRenderer implements Renderer {
    @Override
    public CharSequence render(TJContext tjContext, Object data) {
        return "";
    }
}


class ToStringRenderer extends NullValueAwareAbstractRenderer<Object> {
    @Override
    public void renderMeTo(TJContext tjContext, Object data, StringBuilder output) {
        output.append("@|yellow ");
        if ("".equals(data.toString())) {
            output.append("<empty string>");
        } else {
            output.append(data.toString());
        }
        output.append("|@");
    }
}

class DateRenderer extends NullValueAwareAbstractRenderer<Date> {
    @Override
    public void renderMeTo(TJContext tjContext, Date data, StringBuilder output) {
        output.append("@|yellow ");
        String dateFormat = (String) tjContext.getEnvironment().get("DATE_FORMAT");
        SimpleDateFormat sdf;
        if (dateFormat != null && dateFormat.trim().length() > 0) {
            sdf = new SimpleDateFormat(dateFormat);
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
        output.append(sdf.format(data));
        output.append("|@");
    }
}

class NullValueRenderer implements Renderer {
    @Override
    public CharSequence render(TJContext tjContext, Object data) {
        return "@|yellow <null>|@";
    }
}

abstract class NullValueAwareAbstractRenderer<T> implements Renderer {
    @Override
    @SuppressWarnings({"unchecked"})
    public CharSequence render(TJContext tjContext, Object data) {
        if (data == null) {
            return RendererFactory.NULL_VALUE_RENDERER.render(tjContext, data);
        } else {
            StringBuilder output = new StringBuilder();
            renderMeTo(tjContext, (T) data, output);
            return output;
        }
    }

    protected abstract void renderMeTo(TJContext tjContext, T data, StringBuilder output);
}

abstract class AbstractRendererMatcher {
    private final Renderer renderer;

    AbstractRendererMatcher(Renderer renderer) {
        this.renderer = renderer;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    abstract boolean matches(String typeName);
}

class EqRendererMatcher extends AbstractRendererMatcher {
    private final String[] eq;
    EqRendererMatcher(Renderer renderer, String[] eq) {
        super(renderer);
        this.eq = eq;
    }

    @Override
    boolean matches(String typeName) {
        for(String s : eq) {
            if (typeName.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
}

