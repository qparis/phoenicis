package org.phoenicis.scripts.engine;

import org.phoenicis.scripts.engine.implementation.JSAScriptEngine;
import org.phoenicis.scripts.engine.implementation.PhoenicisSandbox;
import org.phoenicis.scripts.engine.implementation.PhoenicisScriptEngine;
import org.phoenicis.scripts.engine.implementation.PolyglotScriptEngine;

import java.util.Map;

/**
 * The supported script engine types
 */
public enum ScriptEngineType {
    NASHORN("nashorn") {
        @Override
        public PhoenicisScriptEngine createScriptEngine(PhoenicisSandbox sandbox) {
            return new JSAScriptEngine("nashorn");
        }
    },

    GRAAL("graal.js") {
        @Override
        public PhoenicisScriptEngine createScriptEngine(PhoenicisSandbox sandbox) {
            return new PolyglotScriptEngine(sandbox, "js", Map.of("js.nashorn-compat", "true"));
        }
    };

    /**
     * The name of the script engine type
     */
    private final String name;

    /**
     * Constructor
     *
     * @param name The name of the script engine type
     */
    ScriptEngineType(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of the {@link ScriptEngineType}
     *
     * @return The new instance of the {@link ScriptEngineType}
     */
    public abstract PhoenicisScriptEngine createScriptEngine(PhoenicisSandbox phoenicisSandbox);

    @Override
    public String toString() {
        return this.name;
    }
}
