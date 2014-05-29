package com.gruszecm.tjconsole;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeDataSupport;

public abstract class DataOutputService {
	private static final DataOutputService defaultDataOutputService = new ToStringDataOutputService();
	private static final Map<String, DataOutputService> dataOutputServices;
	
	static {
		dataOutputServices = new HashMap<String, DataOutputService>();
		dataOutputServices.put(CompositeDataSupport.class.getName(), new CompositeDataOutputService());
		dataOutputServices.put("void", new VoidDataOutputService());
	}
	
	
	public static DataOutputService get(String type) {
		DataOutputService dos = dataOutputServices.get(type);
		if (type.startsWith("[L")) dos = new ArrayDataOutputService();
		if (dos == null) dos = defaultDataOutputService;
		return dos;
	}
	
	public abstract void output(Object data, Appendable output) throws IOException;	
}


class ToStringDataOutputService extends DataOutputService {
	@Override
	public void output(Object data, Appendable output) throws IOException {
		if (data == null) output.append("null"); else output.append(data.toString());
	}

}

class CompositeDataOutputService extends DataOutputService {

	@Override
	public void output(Object data, Appendable output) throws IOException {
		CompositeDataSupport cds = (CompositeDataSupport) data;
		output.append("CompositeData:").append(cds.getCompositeType().getTypeName()).append("{\n");
		for(Object o : cds.values()) {
			DataOutputService.get(o.getClass().getName()).output(o, output);
			output.append('\n');
		}
		output.append("}\n");
	}	
}

class VoidDataOutputService extends DataOutputService {

	@Override
	public void output(Object data, Appendable output) throws IOException {	}	
}

class ArrayDataOutputService extends DataOutputService {

	@Override
	public void output(Object data, Appendable output) throws IOException {
		output.append("Array[\n");
		for(int i=0; i<Array.getLength(data); i++) {
			Object o = Array.get(data, i);
			output.append("index:" + i).append(" = ");
			get(o.getClass().getName()).output(o, output);
			output.append('\n');
		}
		output.append("]\n");
	}	
}