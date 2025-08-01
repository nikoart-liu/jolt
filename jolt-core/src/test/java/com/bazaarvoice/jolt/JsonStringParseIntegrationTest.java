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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class JsonStringParseIntegrationTest {

    @DataProvider
    public Object[][] getTestCases() {
        return new Object[][] {
            {"/json/jsonStringParse/simpleExample.json"},
            {"/json/jsonStringParse/chainrExample.json"}
        };
    }

    @Test(dataProvider = "getTestCases")
    public void testJsonStringParseTransform(String testCasePath) {
        Map<String, Object> testCase = (Map<String, Object>) JsonUtils.classpathToObject(testCasePath);
        
        Object input = testCase.get("input");
        Object expected = testCase.get("expected");
        
        Object actual;
        
        if (testCase.containsKey("spec")) {
            // Single transform test
            Object spec = testCase.get("spec");
            JsonStringParseTransform transform = new JsonStringParseTransform(spec);
            actual = transform.transform(input);
        } else if (testCase.containsKey("chainr_spec")) {
            // Chainr test
            List<Object> chainrSpec = (List<Object>) testCase.get("chainr_spec");
            Chainr chainr = Chainr.fromSpec(chainrSpec);
            actual = chainr.transform(input);
        } else {
            throw new RuntimeException("Test case must contain either 'spec' or 'chainr_spec'");
        }
        
        Diffy diffy = new Diffy();
        Diffy.Result result = diffy.diff(expected, actual);
        
        if (!result.isEmpty()) {
            System.out.println("Expected: " + JsonUtils.toPrettyJsonString(expected));
            System.out.println("Actual: " + JsonUtils.toPrettyJsonString(actual));
            System.out.println("Diff: " + result.toString());
            throw new AssertionError("Transform did not produce expected output");
        }
    }
    
    @Test
    public void testJsonStringParseInChainrWithMultipleTransforms() {
        // Test a more complex scenario with multiple transforms
        Object input = JsonUtils.classpathToObject("/json/jsonStringParse/chainrExample.json");
        Map<String, Object> testData = (Map<String, Object>) input;
        
        Object inputData = testData.get("input");
        List<Object> chainrSpec = (List<Object>) testData.get("chainr_spec");
        Object expected = testData.get("expected");
        
        Chainr chainr = Chainr.fromSpec(chainrSpec);
        Object actual = chainr.transform(inputData);
        
        Diffy diffy = new Diffy();
        Diffy.Result result = diffy.diff(expected, actual);
        
        if (!result.isEmpty()) {
            System.out.println("Expected: " + JsonUtils.toPrettyJsonString(expected));
            System.out.println("Actual: " + JsonUtils.toPrettyJsonString(actual));
            System.out.println("Diff: " + result.toString());
            throw new AssertionError("Chainr transform did not produce expected output");
        }
    }
}