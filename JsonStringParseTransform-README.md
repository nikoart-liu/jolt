# JsonStringParseTransform

## 概述

`JsonStringParseTransform` 是一个自定义的 Jolt Transform，用于将 JSON 数据中的 JSON 字符串字段转换为实际的 JSON 对象。这在处理来自 API 响应、数据库存储或其他系统的数据时非常有用，这些数据可能将复杂的 JSON 结构作为字符串存储。

## 使用场景

- **API 响应处理**: 当 API 返回的某些字段是 JSON 字符串格式时
- **数据库数据转换**: 从数据库中读取的 JSON 字段通常是字符串格式
- **消息队列处理**: 处理消息队列中的嵌套 JSON 数据
- **配置文件解析**: 解析包含 JSON 字符串配置的数据

## 规格说明 (Spec)

Transform 的规格是一个包含 `fields` 键的 Map，`fields` 的值是一个字符串列表，指定需要解析的字段路径。

```json
{
  "fields": ["field1", "nested.field2", "array.0", "deep.nested.field"]
}
```

### 字段路径格式

- **简单字段**: `"fieldName"`
- **嵌套字段**: `"parent.child.grandchild"`
- **数组元素**: `"arrayField.0"` (使用数字索引)
- **复杂路径**: `"data.items.0.config"`

## 使用示例

### 1. 基本使用

**输入数据**:
```json
{
  "user": {
    "name": "Alice",
    "profile": "{\"age\":25,\"city\":\"New York\"}"
  },
  "config": "{\"debug\":true,\"timeout\":5000}"
}
```

**Transform 规格**:
```json
{
  "fields": ["user.profile", "config"]
}
```

**输出结果**:
```json
{
  "user": {
    "name": "Alice",
    "profile": {
      "age": 25,
      "city": "New York"
    }
  },
  "config": {
    "debug": true,
    "timeout": 5000
  }
}
```

### 2. 在 Chainr 中使用

```json
[
  {
    "operation": "com.bazaarvoice.jolt.JsonStringParseTransform",
    "spec": {
      "fields": ["response.data", "metadata.config"]
    }
  },
  {
    "operation": "shift",
    "spec": {
      "response": {
        "data": {
          "user": {
            "id": "result.userId",
            "name": "result.userName"
          }
        }
      },
      "metadata": {
        "config": {
          "timeout": "result.settings.timeout"
        }
      }
    }
  }
]
```

### 3. 处理数组中的 JSON 字符串

**输入数据**:
```json
{
  "items": [
    "{\"id\":1,\"name\":\"Product A\"}",
    "{\"id\":2,\"name\":\"Product B\"}"
  ]
}
```

**Transform 规格**:
```json
{
  "fields": ["items.0", "items.1"]
}
```

**输出结果**:
```json
{
  "items": [
    {
      "id": 1,
      "name": "Product A"
    },
    {
      "id": 2,
      "name": "Product B"
    }
  ]
}
```

## Java 代码使用

### 单独使用

```java
import com.bazaarvoice.jolt.JsonStringParseTransform;
import com.bazaarvoice.jolt.JsonUtils;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

// 创建规格
Map<String, Object> spec = new HashMap<>();
spec.put("fields", Arrays.asList("data", "config.settings"));

// 创建 Transform
JsonStringParseTransform transform = new JsonStringParseTransform(spec);

// 执行转换
Object input = JsonUtils.jsonToObject(inputJsonString);
Object result = transform.transform(input);

// 输出结果
String outputJson = JsonUtils.toPrettyJsonString(result);
```

### 在 Chainr 中使用

```java
import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

// 定义 Chainr 规格
String chainrSpecJson = "[" +
  "{" +
    "\"operation\": \"com.bazaarvoice.jolt.JsonStringParseTransform\"," +
    "\"spec\": {" +
      "\"fields\": [\"response.data\", \"metadata.config\"]" +
    "}" +
  "}," +
  "{" +
    "\"operation\": \"shift\"," +
    "\"spec\": {" +
      "\"response\": {\"data\": \"result\"}" +
    "}" +
  "}" +
"]";

// 创建 Chainr
List<Object> chainrSpec = JsonUtils.jsonToList(chainrSpecJson);
Chainr chainr = Chainr.fromSpec(chainrSpec);

// 执行转换
Object input = JsonUtils.jsonToObject(inputJsonString);
Object result = chainr.transform(input);
```

## 错误处理

### 优雅的错误处理

- **无效的 JSON 字符串**: 如果字段值不是有效的 JSON，Transform 会保留原始字符串值，不会抛出异常
- **不存在的路径**: 如果指定的字段路径不存在，Transform 会跳过该路径，继续处理其他字段
- **类型不匹配**: 如果路径指向的不是字符串类型，Transform 会跳过该字段

### 异常情况

以下情况会抛出 `TransformException`：

- 规格为 `null`
- 规格不是 Map 类型
- 规格中缺少 `fields` 键
- `fields` 的值不是 List 类型

## 性能考虑

- Transform 会创建输入数据的深拷贝，避免修改原始数据
- JSON 解析使用 Jackson ObjectMapper，性能良好
- 对于大型数据集，建议只指定需要解析的字段，避免不必要的处理

## 测试

项目包含完整的单元测试和集成测试：

- `JsonStringParseTransformTest.java`: 单元测试
- `JsonStringParseIntegrationTest.java`: 集成测试
- 测试资源文件位于 `src/test/resources/json/jsonStringParse/`

运行测试：
```bash
mvn test -Dtest=JsonStringParseTransformTest
mvn test -Dtest=JsonStringParseIntegrationTest
```

## 扩展和自定义

如果需要更复杂的功能，可以扩展 `JsonStringParseTransform` 类：

- 添加自定义的 JSON 解析逻辑
- 支持更复杂的路径表达式
- 添加条件解析（只在满足特定条件时解析）
- 支持不同的数据格式（XML、YAML 等）

## 许可证

本代码遵循 Apache License 2.0 许可证。