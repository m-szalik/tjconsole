package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.DataOutputService;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.apache.commons.beanutils.ConvertUtils;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.*;

/**
 * Invoke operation - mxBean method - command
 *
 * @author szalik
 */
public class InvokeOperationCommandDefinition extends AbstractCommandDefinition {

    public InvokeOperationCommandDefinition() {
        super("Invoke operation.", "invoke", "invoke", true);
    }


    @Override
    public CommandAction action(final String inputOrg) throws Exception {
        return new CommandAction() {

            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                String input = inputOrg;
                if (input.trim().equalsIgnoreCase(prefix)) {
                    StringBuilder sb = new StringBuilder();
                    for (MBeanOperationInfo oi : operations(ctx)) {
                        sb.append(oi.getName()).append('(');
                        for (int i = 0; i < oi.getSignature().length; i++) {
                            sb.append(oi.getSignature()[i].getType());
                            if (i + 1 < oi.getSignature().length) sb.append(',');
                        }
                        sb.append(")\n");
                    }
                    output.println(sb.toString());
                    return;
                }
                if (input.startsWith(prefix)) {
                    input = input.substring(prefix.length()).trim();
                }
                String methodName = input;
                if (methodName.indexOf('(') > 0) {
                    methodName = methodName.substring(0, methodName.indexOf('('));
                }
                methodName = methodName.trim();
                List<MBeanOperationInfo> list = operations(ctx);
                for (Iterator<MBeanOperationInfo> it = list.iterator(); it.hasNext(); ) {
                    if (!it.next().getName().startsWith(methodName)) {
                        it.remove();
                    }
                }
                if (list.size() != 1) {
                    throw new IllegalArgumentException("Operations " + list + " for methodName=" + methodName);
                }
                MBeanOperationInfo operation = list.get(0);
                Object[] params = new Object[operation.getSignature().length];
                String[] signature = new String[operation.getSignature().length];
                for (int i = 0; i < signature.length; i++) {
                    signature[i] = operation.getSignature()[i].getType();
                    params[i] = getParameter(input, i, operation.getSignature()[i]);
                }
                Object returnValue = ctx.getServer().invoke(ctx.getObjectName(), operation.getName(), params, signature);
                StringBuilder sb = new StringBuilder();
                sb.append("Method result:(").append(operation.getReturnType()).append(") ");
                DataOutputService.get(operation.getReturnType()).output(returnValue, sb);
                sb.append('\n');
                output.println(sb.toString());
            }
        };
    }


    private Object getParameter(String input, int index, MBeanParameterInfo beanParameterInfo) throws ClassNotFoundException {
        int i1 = input.indexOf('(');
        int i2 = input.lastIndexOf(')');
        String s = input.substring(i1, i2);
        List<String> params = mySplit(s);
        Class<?> clazz = Class.forName(beanParameterInfo.getType());
        return ConvertUtils.convert(params.get(index), clazz);
    }


    static List<String> mySplit(String input) {
        ArrayList<String> params = new ArrayList<String>();
        boolean inside = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case ',':
                    if (inside) buf.append(ch);
                    else {
                        params.add(buf.toString());
                        buf = new StringBuilder();
                    }
                    break;
                case '\"':
                    if (i > 0 && input.charAt(i - 1) == '\\') buf.append(ch);
                    else {
                        inside = !inside;
                    }
                    break;
                case ' ':
                    if (inside || buf.length() > 0) buf.append(ch);
                    break;
                default:
                    buf.append(ch);
                    break;
            }
        } // for
        if (buf.length() > 0) {
            params.add(buf.toString());
        }
        return params;
    }


    @Override
    public final boolean matches(String input) {
        if (input.startsWith(prefix)) return true;
        String s = input.trim();
        if (s.length() == 0) {
            return false;
        }
        return s.indexOf("(") > 0;
    }

    @Override
    public Completer getCompleter(final TJContext tjContext) {
        return new Completer() {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                buffer = buffer.trim();
                if (buffer.startsWith(prefix)) {
                    String name = buffer.substring(prefix.length()).trim();
                }
//                ArrayList<String> myCandidates = new ArrayList<String>();
//                try {
//                    for (MBeanOperationInfo oi : operations(tjContext)) {
//                        if (!oi.getName().startsWith(buffer)) continue;
//                        StringBuilder mc = new StringBuilder(oi.getName());
//                        mc.append("(");
//                        for (int i = 0; i < oi.getSignature().length; i++) {
//                            mc.append(oi.getSignature()[i].getType());
//                            mc.append(" ");
//                            mc.append(oi.getSignature()[i].getName());
//                            if (i + 1 < oi.getSignature().length) mc.append(',');
//                        }
//                        mc.append(")");
//                        myCandidates.add(mc.toString());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace(); // FIXME
//                }
//                candidates.addAll(myCandidates);
//                return candidates.isEmpty() ? -1 : rt;
                return -1;
            }
        };
    }



    private List<MBeanOperationInfo> operations(TJContext ctx) throws Exception {
        if (ctx.isConnected() && ctx.getObjectName() != null) {
            ArrayList<MBeanOperationInfo> list = new ArrayList<MBeanOperationInfo>();
            Collections.addAll(list, ctx.getServer().getMBeanInfo(ctx.getObjectName()).getOperations());
            return list;
        } else {
            return Collections.emptyList();
        }
    }

}
