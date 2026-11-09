package database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight, zero-dependency JSON parser and serializer for AIRSTRIKE.
 * Implemented using pure standard Java to eliminate external dependencies
 * while robustly parsing Supabase Auth and REST API payloads.
 */
public final class SimpleJson {

    private SimpleJson() {}

    /**
     * Parse any valid JSON text into standard Java types:
     * - JSON Object  -> Map<String, Object>
     * - JSON Array   -> List<Object>
     * - JSON String  -> String
     * - JSON Number  -> Long or Double
     * - JSON Boolean -> Boolean
     * - JSON null    -> null
     */
    public static Object parse(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.isEmpty()) return null;
        Parser parser = new Parser(json);
        return parser.parseValue();
    }

    /**
     * Parses JSON string specifically into a Map<String, Object>.
     * Returns empty map if input is null, empty, or not an object.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object result = parse(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        return new LinkedHashMap<>();
    }

    /**
     * Parses JSON string specifically into a List<Object>.
     * Returns empty list if input is null, empty, or not an array.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object result = parse(json);
        if (result instanceof List) {
            return (List<Object>) result;
        }
        return new ArrayList<>();
    }

    /**
     * Serializes any supported Java object (Map, List, String, Number, Boolean, null) to JSON string.
     */
    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        serialize(obj, sb);
        return sb.toString();
    }

    private static void serialize(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append('"').append(escapeString((String) obj)).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Map) {
            sb.append('{');
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escapeString(String.valueOf(entry.getKey()))).append("\":");
                serialize(entry.getValue(), sb);
            }
            sb.append('}');
        } else if (obj instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) obj) {
                if (!first) sb.append(',');
                first = false;
                serialize(item, sb);
            }
            sb.append(']');
        } else if (obj.getClass().isArray()) {
            sb.append('[');
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(',');
                serialize(java.lang.reflect.Array.get(obj, i), sb);
            }
            sb.append(']');
        } else {
            sb.append('"').append(escapeString(obj.toString())).append('"');
        }
    }

    public static String escapeString(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Safely extracts a String value from a Map.
     */
    public static String getString(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }

    /**
     * Safely extracts a nested value via dot notation, e.g. "user.id" or "user_metadata.username".
     */
    @SuppressWarnings("unchecked")
    public static Object getPath(Map<String, Object> map, String dotPath) {
        if (map == null || dotPath == null) return null;
        String[] tokens = dotPath.split("\\.");
        Object current = map;
        for (String token : tokens) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(token);
            } else {
                return null;
            }
        }
        return current;
    }

    public static String getStringPath(Map<String, Object> map, String dotPath) {
        Object val = getPath(map, dotPath);
        return val != null ? String.valueOf(val) : null;
    }

    public static int getInt(Map<String, Object> map, String key, int def) {
        if (map == null || key == null) return def;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public static long getLong(Map<String, Object> map, String key, long def) {
        if (map == null || key == null) return def;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        if (map == null || key == null) return def;
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return def;
    }

    // =========================================================================
    // Internal Recursive Descent Parser
    // =========================================================================

    private static class Parser {
        private final String src;
        private final int len;
        private int pos;

        public Parser(String src) {
            this.src = src;
            this.len = src.length();
            this.pos = 0;
        }

        private void skipWhitespace() {
            while (pos < len) {
                char c = src.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= len) return null;
            char c = src.charAt(pos);

            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();

            throw new IllegalArgumentException("Unexpected character at pos " + pos + ": " + c);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // consume '{'
            skipWhitespace();

            if (pos < len && src.charAt(pos) == '}') {
                pos++; // empty object
                return map;
            }

            while (pos < len) {
                skipWhitespace();
                if (pos >= len || src.charAt(pos) != '"') {
                    throw new IllegalArgumentException("Expected string key in object at pos " + pos);
                }
                String key = parseString();

                skipWhitespace();
                if (pos >= len || src.charAt(pos) != ':') {
                    throw new IllegalArgumentException("Expected ':' after key at pos " + pos);
                }
                pos++; // consume ':'

                Object value = parseValue();
                map.put(key, value);

                skipWhitespace();
                if (pos >= len) break;
                char next = src.charAt(pos);
                if (next == '}') {
                    pos++;
                    return map;
                } else if (next == ',') {
                    pos++;
                } else {
                    throw new IllegalArgumentException("Expected ',' or '}' in object at pos " + pos);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // consume '['
            skipWhitespace();

            if (pos < len && src.charAt(pos) == ']') {
                pos++; // empty array
                return list;
            }

            while (pos < len) {
                Object item = parseValue();
                list.add(item);

                skipWhitespace();
                if (pos >= len) break;
                char next = src.charAt(pos);
                if (next == ']') {
                    pos++;
                    return list;
                } else if (next == ',') {
                    pos++;
                } else {
                    throw new IllegalArgumentException("Expected ',' or ']' in array at pos " + pos);
                }
            }
            return list;
        }

        private String parseString() {
            pos++; // consume opening '"'
            StringBuilder sb = new StringBuilder();
            while (pos < len) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (pos >= len) break;
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 <= len) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            break;
                        default: sb.append(esc); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean literal at pos " + pos);
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null literal at pos " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') pos++;
            boolean isFloating = false;
            while (pos < len) {
                char c = src.charAt(pos);
                if (c >= '0' && c <= '9') {
                    pos++;
                } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                    isFloating = true;
                    pos++;
                } else {
                    break;
                }
            }
            String numStr = src.substring(start, pos);
            if (isFloating) {
                return Double.parseDouble(numStr);
            } else {
                return Long.parseLong(numStr);
            }
        }
    }
}

