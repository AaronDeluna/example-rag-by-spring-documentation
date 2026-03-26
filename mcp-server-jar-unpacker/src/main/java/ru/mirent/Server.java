package ru.mirent;

import ru.mirent.logging.ToolLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    public static void main(String[] args) throws IOException {
        boolean enableUsageStatistics = parseArgs(args);
        ToolLogger.setUsageStatisticsEnabled(enableUsageStatistics);
        runMcpServer();
    }

    /**
     * Распарсить аргументы командной строки.
     * @param args аргументы командной строки
     * @return true если статистика использования включена (по умолчанию), false если отключена
     */
    static boolean parseArgs(String[] args) {
        if (args == null) {
            return true;
        }
        for (String arg : args) {
            if ("--no-usage-statistics".equals(arg)) {
                return false;
            }
        }
        return true; // по умолчанию включено
    }

    private static void runMcpServer() throws IOException {
        ToolRegistry registry = new DefaultToolRegistry();
        JsonRpcHandler handler = new JsonRpcHandler(registry);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            if (line.trim().isEmpty()) {
                continue;
            }

            try {
                JsonMessage response = handler.handle(line);
                if (response != null) {
                    writer.println(JsonUtils.toJson(response));
                    writer.flush();
                }
            } catch (Exception e) {
                JsonMessage error = new JsonMessage();
                error.id = 0;
                error.error = Map.of("code", -32603, "message", "Internal error: " + e.getMessage());
                writer.println(JsonUtils.toJson(error));
                writer.flush();
            }
        }
    }

    static class JsonUtils {
        static class Parsed {
            Object id;
            String method;
            Object params;
        }

        static Parsed parse(String json) throws IOException {
            Parsed parsed = new Parsed();
            json = json.trim();

            Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+|\\\"[^\\\"]+\\\")");
            Matcher idMatcher = idPattern.matcher(json);
            if (idMatcher.find()) {
                String idStr = idMatcher.group(1);
                if (idStr.startsWith("\"")) {
                    parsed.id = idStr.substring(1, idStr.length() - 1);
                } else {
                    parsed.id = Integer.parseInt(idStr);
                }
            }

            Pattern methodPattern = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]+)\"");
            Matcher methodMatcher = methodPattern.matcher(json);
            if (methodMatcher.find()) {
                parsed.method = methodMatcher.group(1);
            }

            int paramsStart = json.indexOf("\"params\"");
            if (paramsStart != -1) {
                int colonPos = json.indexOf(':', paramsStart);
                if (colonPos != -1) {
                    int objStart = colonPos + 1;
                    while (objStart < json.length() && Character.isWhitespace(json.charAt(objStart))) {
                        objStart++;
                    }

                    if (objStart < json.length() && json.charAt(objStart) == '{') {
                        String paramsStr = extractJsonObject(json, objStart);
                        parsed.params = parseJsonObject(paramsStr);
                    } else if (objStart < json.length() && json.charAt(objStart) == '[') {
                        String paramsStr = extractJsonArray(json, objStart);
                        parsed.params = parseJsonArray(paramsStr);
                    }
                }
            }

            return parsed;
        }

        private static String extractJsonObject(String json, int start) {
            int depth = 0;
            int i = start;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                    i++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                    i++;
                } else if (c == '"') {
                    i++;
                    while (i < json.length() && json.charAt(i) != '"') {
                        if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                            i++;
                        }
                        i++;
                    }
                    if (i < json.length()) {
                        i++;
                    }
                } else {
                    i++;
                }
            }
            return json.substring(start);
        }

        private static String extractJsonArray(String json, int start) {
            int depth = 0;
            int i = start;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '[') {
                    depth++;
                    i++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                    i++;
                } else if (c == '"') {
                    i++;
                    while (i < json.length() && json.charAt(i) != '"') {
                        if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                            i++;
                        }
                        i++;
                    }
                    if (i < json.length()) {
                        i++;
                    }
                } else {
                    i++;
                }
            }
            return json.substring(start);
        }

        private static Map<String, Object> parseJsonObject(String json) {
            Map<String, Object> result = new LinkedHashMap<>();
            json = json.trim();
            int i = 0;
            if (json.startsWith("{")) {
                i++;
            }

            while (i < json.length()) {
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                if (i >= json.length()) break;

                if (json.charAt(i) == '}') {
                    break;
                }

                if (json.charAt(i) != '"') {
                    break;
                }

                int keyStart = i + 1;
                i++;
                while (i < json.length() && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                        i++;
                    }
                    i++;
                }
                String key = json.substring(keyStart, i);

                i++;
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }

                if (i < json.length() && json.charAt(i) == ':') {
                    i++;
                    while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                        i++;
                    }

                    if (i < json.length()) {
                        char firstChar = json.charAt(i);
                        if (firstChar == '"') {
                            int valStart = i + 1;
                            i++;
                            while (i < json.length() && json.charAt(i) != '"') {
                                if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                                    i++;
                                }
                                i++;
                            }
                            result.put(key, json.substring(valStart, i));
                            i++;
                        } else if (firstChar == '{') {
                            String objStr = extractJsonObject(json, i);
                            result.put(key, parseJsonObject(objStr));
                            i += objStr.length();
                        } else if (firstChar == '[') {
                            String arrStr = extractJsonArray(json, i);
                            result.put(key, parseJsonArray(arrStr));
                            i += arrStr.length();
                        } else if (firstChar == 't') {
                            result.put(key, true);
                            i += 4;
                        } else if (firstChar == 'f') {
                            result.put(key, false);
                            i += 5;
                        } else if (firstChar == 'n') {
                            result.put(key, null);
                            i += 4;
                        } else if (Character.isDigit(firstChar) || firstChar == '-') {
                            int numStart = i;
                            while (i < json.length() && (Character.isDigit(json.charAt(i)) ||
                                    json.charAt(i) == '.' || json.charAt(i) == 'e' ||
                                    json.charAt(i) == 'E' || json.charAt(i) == '-' ||
                                    json.charAt(i) == '+')) {
                                i++;
                            }
                            String numStr = json.substring(numStart, i);
                            try {
                                if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                                    result.put(key, Double.parseDouble(numStr));
                                } else {
                                    result.put(key, Long.parseLong(numStr));
                                }
                            } catch (NumberFormatException e) {
                                result.put(key, numStr);
                            }
                        }
                    }
                }

                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }

                if (i < json.length() && json.charAt(i) == ',') {
                    i++;
                } else if (i < json.length() && json.charAt(i) == '}') {
                    break;
                }
            }

            return result;
        }

        private static List<Object> parseJsonArray(String json) {
            List<Object> result = new ArrayList<>();
            json = json.trim();
            int i = 0;
            if (json.startsWith("[")) {
                i++;
            }
            while (i < json.length()) {
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                if (i >= json.length()) break;

                char firstChar = json.charAt(i);
                if (firstChar == '"') {
                    int valStart = i + 1;
                    i++;
                    while (i < json.length() && json.charAt(i) != '"') {
                        if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                            i++;
                        }
                        i++;
                    }
                    result.add(json.substring(valStart, i));
                    i++;
                } else if (firstChar == '{') {
                    String objStr = extractJsonObject(json, i);
                    result.add(parseJsonObject(objStr));
                    i += objStr.length();
                } else if (firstChar == '[') {
                    String arrStr = extractJsonArray(json, i);
                    result.add(parseJsonArray(arrStr));
                    i += arrStr.length();
                } else if (firstChar == 't') {
                    result.add(true);
                    i += 4;
                } else if (firstChar == 'f') {
                    result.add(false);
                    i += 5;
                } else if (firstChar == 'n') {
                    result.add(null);
                    i += 4;
                } else if (Character.isDigit(firstChar) || firstChar == '-') {
                    int numStart = i;
                    while (i < json.length() && (Character.isDigit(json.charAt(i)) ||
                            json.charAt(i) == '.' || json.charAt(i) == 'e' ||
                            json.charAt(i) == 'E' || json.charAt(i) == '-' ||
                            json.charAt(i) == '+')) {
                        i++;
                    }
                    String numStr = json.substring(numStart, i);
                    try {
                        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                            result.add(Double.parseDouble(numStr));
                        } else {
                            result.add(Long.parseLong(numStr));
                        }
                    } catch (NumberFormatException e) {
                        result.add(numStr);
                    }
                }

                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }

                if (i < json.length() && json.charAt(i) == ',') {
                    i++;
                } else if (i < json.length() && json.charAt(i) == ']') {
                    break;
                }
            }

            return result;
        }

        static String toJson(Object object) {
            return new Gson().toJson(object);
        }
    }

    static class Gson {
        String toJson(Object obj) {
            if (obj == null) return "null";
            if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
            if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
            if (obj instanceof Map) return toJsonMap((Map<?, ?>) obj);
            if (obj instanceof List) return toJsonList((List<?>) obj);
            return "null";
        }

        private String toJsonMap(Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }

        private String toJsonList(List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }

        private String escapeJson(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}