/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.TransformException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 * Transform that parses JSON strings within JSON objects and converts them to actual JSON objects.
 * 
 * This transform is useful when you have JSON data where some field values are JSON strings
 * that need to be parsed into actual JSON objects for further processing.
 * 
 * The spec should contain field paths that need to be parsed from JSON strings to JSON objects.
 * 
 * Example spec:
 * {
 *   "fields": ["data", "metadata.config", "response.body"]
 * }
 * 
 * This will parse the JSON strings at the specified paths and replace them with the parsed JSON objects.
 */
public class JsonStringParseTransform implements SpecDriven, Transform {

    private final List<String> fieldPaths;
    private final ObjectMapper objectMapper;

    @Inject
    public JsonStringParseTransform(Object spec) {
        if (spec == null) {
            throw new TransformException("JsonStringParseTransform requires a non-null spec");
        }
        
        if (!(spec instanceof Map)) {
            throw new TransformException("JsonStringParseTransform spec must be a Map");
        }
        
        Map<String, Object> specMap = (Map<String, Object>) spec;
        Object fieldsObj = specMap.get("fields");
        
        if (fieldsObj == null) {
            throw new TransformException("JsonStringParseTransform spec must contain 'fields' key");
        }
        
        if (!(fieldsObj instanceof List)) {
            throw new TransformException("JsonStringParseTransform spec 'fields' must be a List");
        }
        
        this.fieldPaths = (List<String>) fieldsObj;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object transform(Object input) {
        if (input == null) {
            return null;
        }
        
        // Make a deep copy to avoid mutating the original input
        Object result;
        try {
            String jsonString = objectMapper.writeValueAsString(input);
            result = objectMapper.readValue(jsonString, Object.class);
        } catch (IOException e) {
            throw new TransformException("Failed to clone input object", e);
        }
        
        // Process each field path
        for (String fieldPath : fieldPaths) {
            parseJsonStringAtPath(result, fieldPath);
        }
        
        return result;
    }
    
    /**
     * Parse JSON string at the specified path and replace it with the parsed JSON object.
     * 
     * @param data the JSON data to process
     * @param path the dot-separated path to the field (e.g., "metadata.config")
     */
    private void parseJsonStringAtPath(Object data, String path) {
        if (data == null || path == null || path.trim().isEmpty()) {
            return;
        }
        
        String[] pathParts = path.split("\\.");
        Object current = data;
        
        // Navigate to the parent of the target field
        for (int i = 0; i < pathParts.length - 1; i++) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(pathParts[i]);
            } else if (current instanceof List) {
                try {
                    int index = Integer.parseInt(pathParts[i]);
                    List<Object> list = (List<Object>) current;
                    if (index >= 0 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        return; // Index out of bounds, skip this path
                    }
                } catch (NumberFormatException e) {
                    return; // Invalid array index, skip this path
                }
            } else {
                return; // Cannot navigate further, skip this path
            }
            
            if (current == null) {
                return; // Path doesn't exist, skip
            }
        }
        
        // Process the target field
        String targetField = pathParts[pathParts.length - 1];
        
        if (current instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) current;
            Object value = map.get(targetField);
            
            if (value instanceof String) {
                try {
                    Object parsedValue = objectMapper.readValue((String) value, Object.class);
                    map.put(targetField, parsedValue);
                } catch (Exception e) {
                    // If parsing fails, leave the original string value
                    // This allows for graceful handling of non-JSON strings
                }
            }
        } else if (current instanceof List) {
            try {
                int index = Integer.parseInt(targetField);
                List<Object> list = (List<Object>) current;
                if (index >= 0 && index < list.size()) {
                    Object value = list.get(index);
                    if (value instanceof String) {
                        try {
                            Object parsedValue = objectMapper.readValue((String) value, Object.class);
                            list.set(index, parsedValue);
                        } catch (Exception e) {
                            // If parsing fails, leave the original string value
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid array index, skip
            }
        }
    }
}