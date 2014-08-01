package org.jsoftware.tjconsole.command.cmd;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.DataOutputService;
import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.util.MyDateConverter;
import org.apache.commons.beanutils.ConvertUtils;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.*;

/**
 * Invoke operation - mxBean method - command
 *
 * @author szalik
 */
public class InvokeOperationCommand extends AbstractCommand implements Completer {
    private static final String PREFIX = "invoke";


    public InvokeOperationCommand(TJContext ctx, Output output) {
        super(ctx, output);
        ConvertUtils.deregister(Date.class);
        ConvertUtils.register(new MyDateConverter(), Date.class);
    }


    @Override
    public void action(String input) throws Exception {
        if (input.trim().equalsIgnoreCase(PREFIX)) {
            StringBuilder sb = new StringBuilder();
            for (MBeanOperationInfo oi : operations()) {
                sb.append(oi.getName()).append('(');
                for (int i = 0; i < oi.getSignature().length; i++) {
                    sb.append(oi.getSignature()[i].getType());
                    if (i + 1 < oi.getSignature().length) sb.append(',');
                }
                sb.append(")\n");
            }
            output.outMBeanOutput(sb.toString());
            return;
        }
        if (input.startsWith(PREFIX)) {
            input = input.substring(PREFIX.length()).trim();
        }
        String methodName = input;
        if (methodName.indexOf('(') > 0) {
            methodName = methodName.substring(0, methodName.indexOf('('));
        }
        methodName = methodName.trim();
        List<MBeanOperationInfo> list = operations();
        for (Iterator<MBeanOperationInfo> it = list.iterator(); it.hasNext(); ) {
            if (!it.next().getName().startsWith(methodName)) {
                it.remove();
            }
        }
        if (list.size() != 1)
            throw new IllegalArgumentException("Operations " + list + " for methodName=" + methodName);
        MBeanOperationInfo operation = list.get(0);
        Object[] params = new Object[operation.getSignature().length];
        String[] signature = new String[operation.getSignature().length];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = operation.getSignature()[i].getType();
            params[i] = getParameter(input, i, operation.getSignature()[i]);
        }
        Object returnValue = ctx.getServer().invoke(ctx.getObjectName(), operation.getName(), params, signature);
        StringBuilder sb = new StringBuilder();
        sb.append("Method result:(" + operation.getReturnType() + ") ");
        DataOutputService.get(operation.getReturnType()).output(returnValue, sb);
        sb.append('\n');
        output.outMBeanOutput(sb.toString());
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
    public CmdDescription getHelp() {
        return new CmdDescription("Invoke operation.", "invoke", PREFIX) {
            private static final long serialVersionUID = -3266997299961603873L;

            @Override
            public boolean isProper(TJContext ctx) {
                return ctx.isConnected() && ctx.getObjectName() != null;
            }
        };
    }


    @Override
    public final boolean matches(String input) {
        if (input.startsWith(PREFIX)) return true;
        String s = input.trim();
        if (s.length() == 0) {
            return false;
        }
        if (s.indexOf("(") > 0) {
            s = s.substring(0, s.indexOf("("));
        }
        try {
            for (MBeanOperationInfo oi : operations()) {
                if (oi.getName().startsWith(s)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates) {
        buffer = buffer.trim();
        if (!matches(buffer) && buffer.length() > 0) return -1;
        int rt = -1;
        if (buffer.startsWith(PREFIX)) {
            buffer = buffer.substring(PREFIX.length()).trim();
            rt = PREFIX.length();
        }
        ArrayList<String> myCandidates = new ArrayList<String>();
        try {
            for (MBeanOperationInfo oi : operations()) {
                if (!oi.getName().startsWith(buffer)) continue;
                StringBuilder mc = new StringBuilder(oi.getName());
                mc.append("(");
                for (int i = 0; i < oi.getSignature().length; i++) {
                    mc.append(oi.getSignature()[i].getType());
                    mc.append(" ");
                    mc.append(oi.getSignature()[i].getName());
                    if (i + 1 < oi.getSignature().length) mc.append(',');
                }
                mc.append(")");
                myCandidates.add(mc.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        candidates.addAll(myCandidates);
        if (candidates.isEmpty()) return -1;
        else return rt;
    }


    private List<MBeanOperationInfo> operations() throws Exception {
        if (ctx.isConnected() && ctx.getObjectName() != null) {
            ArrayList<MBeanOperationInfo> list = new ArrayList<MBeanOperationInfo>();
            for (MBeanOperationInfo oi : ctx.getServer().getMBeanInfo(ctx.getObjectName()).getOperations()) {
                list.add(oi);
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

}
