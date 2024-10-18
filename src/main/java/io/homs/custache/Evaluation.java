package io.homs.custache;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Evaluation {

    protected Object evaluateToObject(Context context, List<String> idents) {

        if (idents.isEmpty()) {
            throw new RuntimeException("no idents ¿?");
        }
        String varName = idents.get(0);

        Object v = context.get(varName);
        for (int i = 1; i < idents.size(); i++) {
            v = getByKey(v, idents.get(i));
        }

        return v;
    }

    public String evaluateToString(Context context, List<String> idents) {
        Object value = evaluateToObject(context, idents);
        return String.valueOf(value);
    }

    public boolean evaluateToBoolean(Context context, List<String> idents) {
        Object value = evaluateToObject(context, idents);
        return isTrue(value);
    }

    public Iterable<?> evaluateToIterable(Context context, List<String> idents) {
        Object value = evaluateToObject(context, idents);
        return (Iterable<?>) value;
    }

    protected static boolean isTrue(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Boolean r) {
            return r;
        } else if (o instanceof Number r) {
            return r.doubleValue() > 0.0;
        } else if (o instanceof String r) {
            return r.length() > 0;
        } else if (o instanceof Collection<?> r) {
            return !r.isEmpty();
        } else if (o.getClass().isArray()) {
            return Array.getLength(o) > 0;
        } else if (o instanceof Map) {
            return !((Map<?, ?>) o).isEmpty();
        }
        return true;
    }

    protected static Object getByKey(Object o, Object key) {

        if (o == null) {
            throw new RuntimeException();
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) o;
            if (!m.containsKey(key)) {
                throw new RuntimeException("key not found: " + key);
            }
            return m.get(key);
        } else if (o instanceof Collection<?> c && key instanceof Number nindex) {
            int index = nindex.intValue();
            return new ArrayList<Object>(c).get(index);
        } else if (o.getClass().isArray() && key instanceof Number nindex) {
            int index = nindex.intValue();
            return Array.get(o, index);
        } else {
            if (!(key instanceof String)) {
                throw new RuntimeException();
            }

            Class<? extends Object> beanClass = o.getClass();

            /*
             * PROVA PROPIETAT DE JAVA BEAN
             */
            BeanInfo info;
            try {
                info = Introspector.getBeanInfo(beanClass);
            } catch (IntrospectionException e) {
                throw new RuntimeException("describing " + beanClass.getName(), e);
            }
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(key)) {
                    try {
                        return pd.getReadMethod().invoke(o);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            /*
             * PROVA METHOD
             */
            for (Method m : beanClass.getMethods()) {
                if (m.getName().equals(key) && m.getParameterTypes().length == 0) {
                    try {
                        return m.invoke(o);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            throw new RuntimeException();
        }

    }
}
