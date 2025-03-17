package io.homs.custache.eval;

import lombok.SneakyThrows;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

public class Evaluation {

    public Object evaluateToObject(Context context, List<String> idents) {

        if (idents.isEmpty()) {
            throw new RuntimeException("no idents");
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

    public Iterable<Object> evaluateToIterable(Context context, List<String> idents) {
        Object value = evaluateToObject(context, idents);
        return (Iterable<Object>) value;
    }

    public static boolean isTrue(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Boolean r) {
            return r;
        } else if (o instanceof Number r) {
            return r.doubleValue() > 0.0;
        } else if (o instanceof String r) {
            return !r.isEmpty();
        } else if (o instanceof Collection<?> r) {
            return !r.isEmpty();
        } else if (o.getClass().isArray()) {
            return Array.getLength(o) > 0;
        } else if (o instanceof Map) {
            return !((Map<?, ?>) o).isEmpty();
        } else {
            return true;
        }
    }

    @SneakyThrows
    protected static Object getByKey(Object o, String key) {

        if (o == null) {
            throw new RuntimeException();
        }
        if (o instanceof Map) {
            Map<String, ?> m = (Map<String, ?>) o;
            if (!m.containsKey(key)) {
                throw new RuntimeException("key not found: " + key);
            }
            return m.get(key);
        } else {

            Class<? extends Object> beanClass = o.getClass();

            /*
             * PROVA PROPIETAT DE JAVA BEAN
             */
            BeanInfo info = Introspector.getBeanInfo(beanClass);
            Optional<PropertyDescriptor> pdOpt = Arrays.stream(info.getPropertyDescriptors())
                    .filter((PropertyDescriptor pd) -> pd.getName().equals(key))
                    .findFirst();
            if (pdOpt.isPresent()) {
                return pdOpt.get().getReadMethod().invoke(o);
            }

            /*
             * PROVA METHOD
             */
            for (Method m : beanClass.getMethods()) {
                if (m.getName().equals(key) && m.getParameterTypes().length == 0) {
                    return m.invoke(o);
                }
            }

            throw new RuntimeException();
        }
    }

    public Object invokeMethod(Object bean, String methodName, Object argument) throws Exception {
        if (bean == null) {
            throw new IllegalArgumentException("Bean cannot be null");
        }
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }

        Class<?> clazz = bean.getClass();
        Method method = findMethod(clazz, methodName, argument.getClass());

        if (method == null) {
            throw new NoSuchMethodException("Method " + methodName + " with argument type " + argument.getClass() + " not found in " + clazz);
        }

        method.setAccessible(true); // Permite acceder a métodos privados
        return method.invoke(bean, argument);
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?> argumentType) {
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.getDeclaredMethod(methodName, argumentType);
        } catch (NoSuchMethodException e) {
            // Método no encontrado en la clase actual, buscar en la superclase
            return findMethod(clazz.getSuperclass(), methodName, argumentType);
        }
    }
}
