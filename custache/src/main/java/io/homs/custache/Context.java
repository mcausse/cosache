package io.homs.custache;

import java.util.LinkedHashMap;
import java.util.Map;

public class Context {

    final Context parent;
    final Map<String, Object> variables;

    public Context(Context parent) {
        super();
        this.parent = parent;
        this.variables = new LinkedHashMap<>();
    }

    public Context() {
        this(null);
    }

    public Context find(String sym) {
        Context e = this;
        while (e != null) {
            if (e.variables.containsKey(sym)) {
                return e;
            }
            e = e.parent;
        }
        return null;
    }

    public void def(String sym, Object value) {
        if (this.variables.containsKey(sym)) {
            throw new RuntimeException("yet defined variable: '" + sym + "'; use set");
        }
        this.variables.put(sym, value);
    }

    public Object get(String sym) {
        Context e = find(sym);
        if (e == null) {
            throw new RuntimeException("variable not defined: '" + sym + "'");
        }
        return e.variables.get(sym);
    }

}
