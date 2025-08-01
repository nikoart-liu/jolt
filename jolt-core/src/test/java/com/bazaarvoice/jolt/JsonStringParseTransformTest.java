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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class JsonStringParseTransformTest {

    @Test
    public void testSimpleJsonStringParsing() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("data")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        Map<String, Object> input = ImmutableMap.of(
            "data", "{\"name\":\"John\",\"age\":30}",
            "other", "regular string"
        );
        
        // Execute
        Object result = transform.transform(input);
        
        // Verify
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        Assert.assertEquals(resultMap.get("other"), "regular string");
        
        Object parsedData = resultMap.get("data");
        Assert.assertTrue(parsedData instanceof Map);
        
        Map<String, Object> parsedDataMap = (Map<String, Object>) parsedData;
        Assert.assertEquals(parsedDataMap.get("name"), "John");
        Assert.assertEquals(parsedDataMap.get("age"), 30);
    }
    
    @Test
    public void testNestedJsonStringParsing() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("metadata.config")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        Map<String, Object> input = ImmutableMap.of(
            "metadata", ImmutableMap.of(
                "config", "{\"enabled\":true,\"timeout\":5000}",
                "version", "1.0"
            ),
            "data", "some data"
        );
        
        // Execute
        Object result = transform.transform(input);
        
        // Verify
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        Assert.assertEquals(resultMap.get("data"), "some data");
        
        Map<String, Object> metadata = (Map<String, Object>) resultMap.get("metadata");
        Assert.assertEquals(metadata.get("version"), "1.0");
        
        Object parsedConfig = metadata.get("config");
        Assert.assertTrue(parsedConfig instanceof Map);
        
        Map<String, Object> configMap = (Map<String, Object>) parsedConfig;
        Assert.assertEquals(configMap.get("enabled"), true);
        Assert.assertEquals(configMap.get("timeout"), 5000);
    }
    
    @Test
    public void testArrayJsonStringParsing() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("items.0")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        Map<String, Object> input = ImmutableMap.of(
            "items", ImmutableList.of(
                "{\"id\":1,\"name\":\"item1\"}",
                "regular string"
            )
        );
        
        // Execute
        Object result = transform.transform(input);
        
        // Verify
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        List<Object> items = (List<Object>) resultMap.get("items");
        Assert.assertEquals(items.get(1), "regular string");
        
        Object parsedItem = items.get(0);
        Assert.assertTrue(parsedItem instanceof Map);
        
        Map<String, Object> itemMap = (Map<String, Object>) parsedItem;
        Assert.assertEquals(itemMap.get("id"), 1);
        Assert.assertEquals(itemMap.get("name"), "item1");
    }
    
    @Test
    public void testMultipleFieldsParsing() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("data", "config")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        Map<String, Object> input = ImmutableMap.of(
            "data", "{\"user\":\"alice\"}",
            "config", "{\"debug\":true}",
            "message", "hello world"
        );
        
        // Execute
        Object result = transform.transform(input);
        
        // Verify
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        Assert.assertEquals(resultMap.get("message"), "hello world");
        
        Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
        Assert.assertEquals(dataMap.get("user"), "alice");
        
        Map<String, Object> configMap = (Map<String, Object>) resultMap.get("config");
        Assert.assertEquals(configMap.get("debug"), true);
    }
    
    @Test
    public void testInvalidJsonStringHandling() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("data")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        Map<String, Object> input = ImmutableMap.of(
            "data", "invalid json string",
            "other", "regular string"
        );
        
        // Execute
        Object result = transform.transform(input);
        
        // Verify - invalid JSON should remain as string
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        Assert.assertEquals(resultMap.get("data"), "invalid json string");
        Assert.assertEquals(resultMap.get("other"), "regular string");
    }
    
    @Test
    public void testNonExistentFieldHandling() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("nonexistent.field")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        Map<String, Object> input = ImmutableMap.of(
            "data", "some data"
        );
        
        // Execute
        Object result = transform.transform(input);
        
        // Verify - should not throw exception and return original data
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals(resultMap.get("data"), "some data");
    }
    
    @Test
    public void testNullInputHandling() {
        // Setup
        Map<String, Object> spec = ImmutableMap.of(
            "fields", ImmutableList.of("data")
        );
        
        JsonStringParseTransform transform = new JsonStringParseTransform(spec);
        
        // Execute
        Object result = transform.transform(null);
        
        // Verify
        Assert.assertNull(result);
    }
    
    @Test(expectedExceptions = TransformException.class)
    public void testNullSpecHandling() {
        new JsonStringParseTransform(null);
    }
    
    @Test(expectedExceptions = TransformException.class)
    public void testInvalidSpecHandling() {
        new JsonStringParseTransform("invalid spec");
    }
    
    @Test(expectedExceptions = TransformException.class)
    public void testMissingFieldsInSpec() {
        Map<String, Object> spec = ImmutableMap.of(
            "other", "value"
        );
        new JsonStringParseTransform(spec);
    }
    
    @Test(expectedExceptions = TransformException.class)
    public void testInvalidFieldsTypeInSpec() {
        Map<String, Object> spec = ImmutableMap.of(
            "fields", "should be a list"
        );
        new JsonStringParseTransform(spec);
    }
}