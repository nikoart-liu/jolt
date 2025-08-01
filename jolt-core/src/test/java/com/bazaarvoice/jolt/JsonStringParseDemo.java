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

import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示JsonStringParseTransform的使用
 */
public class JsonStringParseDemo {

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 示例1: 基本使用
        System.out.println("=== 示例1: 基本使用 ===");
        
        // 输入数据 - 包含JSON字符串的对象
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "张三");
        input1.put("profile", "{\"age\": 25, \"city\": \"北京\"}");
        input1.put("settings", "{\"theme\": \"dark\", \"language\": \"zh-CN\"}");
        
        System.out.println("输入数据:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input1));
        
        // Transform配置
        Map<String, Object> spec1 = new HashMap<>();
        spec1.put("fields", Arrays.asList("profile", "settings"));
        
        // 执行转换
        JsonStringParseTransform transform1 = new JsonStringParseTransform(spec1);
        Object result1 = transform1.transform(input1);
        
        System.out.println("\n转换后:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result1));
        
        // 示例2: 在Chainr中使用
        System.out.println("\n\n=== 示例2: 在Chainr中使用 ===");
        
        // 输入数据
        Map<String, Object> input2 = new HashMap<>();
        input2.put("userId", "12345");
        input2.put("userData", "{\"name\": \"李四\", \"email\": \"lisi@example.com\"}");
        input2.put("preferences", "{\"notifications\": true, \"theme\": \"light\"}");
        
        System.out.println("输入数据:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input2));
        
        // Chainr配置 - 先解析JSON字符串，再重新组织结构
        String chainrSpec = "[\n" +
                "  {\n" +
                "    \"operation\": \"com.bazaarvoice.jolt.JsonStringParseTransform\",\n" +
                "    \"spec\": {\n" +
                "      \"fields\": [\"userData\", \"preferences\"]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"operation\": \"shift\",\n" +
                "    \"spec\": {\n" +
                "      \"userId\": \"user.id\",\n" +
                "      \"userData\": {\n" +
                "        \"name\": \"user.profile.name\",\n" +
                "        \"email\": \"user.profile.email\"\n" +
                "      },\n" +
                "      \"preferences\": \"user.settings\"\n" +
                "    }\n" +
                "  }\n" +
                "]";
        
        List<Object> chainrSpecList = JsonUtils.jsonToList(chainrSpec);
        Chainr chainr = Chainr.fromSpec(chainrSpecList);
        
        Object result2 = chainr.transform(input2);
        
        System.out.println("\nChainr转换后:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result2));
        
        System.out.println("\n=== 演示完成 ===");
    }
}