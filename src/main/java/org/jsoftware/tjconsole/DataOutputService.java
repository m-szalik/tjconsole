package org.jsoftware.tjconsole;

import javax.management.openmbean.CompositeDataSupport;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class DataOutputService {
    private static final DataOutputService defaultDataOutputService = new ToStringDataOutputService();
    private static final Map<String, DataOutputService> dataOutputServices;

    static {
        dataOutputServices = new HashMap<String, DataOutputService>();
        dataOutputServices.put(CompositeDataSupport.class.getName(), new CompositeDataOutputService());
        dataOutputServices.put("void", new VoidDataOutputService());
        dataOutputServices.put(Date.class.getName(), new DateDataOutputService());
    }


    public static DataOutputService get(String type) {
        DataOutputService dos = dataOutputServices.get(type);
        if (type.startsWith("[L")) {
            dos = new ArrayDataOutputService();
        }
        if (dos == null) {
            dos = defaultDataOutputService;
        }
        return dos;
    }

    public abstract void output(Object data, TJContext tjContext, Appendable output) throws IOException;
}


class ToStringDataOutputService extends DataOutputService {
    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        if (data == null) {
            output.append("null");
        } else {
            output.append(data.toString());
        }
    }

}

class CompositeDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        CompositeDataSupport cds = (CompositeDataSupport) data;
        output.append("CompositeData:").append(cds.getCompositeType().getTypeName()).append("{\n");
        for (Object o : cds.values()) {
            DataOutputService.get(o.getClass().getName()).output(o, tjContext, output);
            output.append('\n');
        }
        output.append("}\n");
    }
}

class VoidDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
    }
}

class ArrayDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        output.append("Array[\n");
        for (int i = 0; i < Array.getLength(data); i++) {
            Object o = Array.get(data, i);
            output.append("index:" + i).append(" = ");
            get(o.getClass().getName()).output(o, tjContext, output);
            output.append('\n');
        }
        output.append("]\n");
    }
}

class DateDataOutputService extends DataOutputService {

    @Override
    public void output(Object data, TJContext tjContext, Appendable output) throws IOException {
        if (data == null) {
            output.append("null");
        } else {
            String dateFormat = (String) tjContext.getEnvironment().get("DATE_FORMAT");
            SimpleDateFormat sdf;
            if (dateFormat != null && dateFormat.trim().length() > 0) {
                sdf = new SimpleDateFormat(dateFormat);
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            }
            output.append(sdf.format((Date) data));
        }
    }
}