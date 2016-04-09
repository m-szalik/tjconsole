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
@SuppressWarnings({"unchecked", "unsafe"})
public class MyDateConverter implements Converter {
    private static MyDateConverter instance;
    private final Map<Pattern, DateFormat> patterns;
    private SimpleDateFormat custom;

    public static synchronized MyDateConverter getInstance() {
        if (instance == null) {
            instance = new MyDateConverter();
        }
        return instance;
    }

    private MyDateConverter() {
        patterns = new HashMap<Pattern, DateFormat>();
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2}"), new SimpleDateFormat("yyyy-MM-dd"));
        patterns.put(Pattern.compile("\\d{2}-\\d{2}-\\d{4}"), new SimpleDateFormat("dd-MM-yyyy"));
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"), new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        patterns.put(Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    }


    public Object convert(Class clazz, Object value) {
        if (value == null) {
            return null;
        }
        DateFormat dateFormat = null;
        for (Pattern pattern : patterns.keySet()) {
            if (pattern.matcher(value.toString()).matches()) {
                dateFormat = patterns.get(pattern);
                break;
            }
        }
        if (dateFormat == null) {
            try {
                if (custom == null) {
                    throw new IllegalArgumentException("Cannot convert do date - " + value);
                }
                return custom.parse(value.toString());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Cannot convert do date - " + value, e);
            }
        }
        try {
            return dateFormat.parse(value.toString());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setCustom(SimpleDateFormat custom) {
        this.custom = custom;
    }
}
