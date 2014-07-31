package org.jsoftware.tjconsole.util;

import org.apache.commons.beanutils.Converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Date converter for multiple date formats
 *
 * @author szalik
 */
public class MyDateConverter implements Converter {
    private final Map<Pattern, DateFormat> patterns;

    public MyDateConverter() {
        patterns = new HashMap<Pattern, DateFormat>();
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2}"), new SimpleDateFormat("yyyy-MM-dd"));
        patterns.put(Pattern.compile("\\d{2}-\\d{2}-\\d{4}"), new SimpleDateFormat("dd-MM-yyyy"));
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"), new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }


    @SuppressWarnings("unchecked")
    public Object convert(Class clazz, Object value) {
        if (value == null) return null;
        DateFormat dateFormat = null;
        for (Pattern pattern : patterns.keySet()) {
            if (pattern.matcher(value.toString()).matches()) {
                dateFormat = patterns.get(pattern);
                break;
            }
        }
        if (dateFormat == null) {
            throw new IllegalArgumentException("Cannot convert do date - " + value);
        }
        try {
            return dateFormat.parse(value.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
