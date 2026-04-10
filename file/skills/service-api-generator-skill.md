# fegin调用服务接口生成器技能

## 1. 技能描述

本技能提供OneChat项目服务接口生成功能，用于为OneChat-modules下的模块生成对外调用接口，遵循项目的微服务架构规范。

## 2. 生成规则

### 2.1 目录结构

为OneChat-modules下的模块生成对外接口时，需要在OneChat-api目录下创建对应的OneChat-api-xxx模块，目录结构如下：

```
OneChat-api/
└── OneChat-api-xxx/          # 对应OneChat-modules/OneChat-xxx模块
    ├── pom.xml                # 模块依赖配置
    └── src/
        └── main/
            ├── java/
            │   └── chat/
            │       └── aikf/
            │           └── xxx/  # 模块名称
            │               └── api/
            │                   ├── domain/        # 数据传输对象
            │                   ├── factory/       # 服务降级处理
            │                   └── RemoteXxxService.java  # 远程服务调用接口
            └── resources/
                └── META-INF/
                    └── spring/
                        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports  # 自动配置文件
```

### 2.2 命名规范

- **模块名称**：OneChat-api-xxx，其中xxx对应OneChat-modules中的模块名称
- **接口名称**：RemoteXxxService，其中Xxx为模块名称的首字母大写形式
- **降级处理**：RemoteXxxFallbackFactory，其中Xxx为模块名称的首字母大写形式
- **数据传输对象**：与模块中的实体类对应，保持相同的命名

### 2.3 依赖配置

在OneChat-api-xxx模块的pom.xml中添加必要的依赖：

```xml
<dependencies>
    <!-- RuoYi Common Core-->
    <dependency>
        <groupId>chat.aikf</groupId>
        <artifactId>OneChat-common-core</artifactId>
    </dependency>
    
    <!-- Spring Cloud OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <!-- Hystrix 用于服务降级 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
    
    <!-- 其他依赖 ... -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

## 3. 生成模板

### 3.1 远程服务接口模板

```java
package chat.aikf.xxx.api;

import chat.aikf.common.core.domain.R;
import chat.aikf.xxx.api.domain.XxxEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Xxx服务远程调用接口
 */
@FeignClient(contextId = "remoteXxxService", value = "onechat-xxx", fallbackFactory = RemoteXxxFallbackFactory.class)
public interface RemoteXxxService {

    /**
     * 查询列表
     * @param xxxEntity 查询条件
     * @return 结果
     */
    @GetMapping("/xxx/list")
    R list(XxxEntity xxxEntity);

    /**
     * 根据ID查询
     * @param id ID
     * @return 结果
     */
    @GetMapping("/xxx/get/{id}")
    R get(@PathVariable Long id);

    /**
     * 创建
     * @param xxxEntity 实体
     * @return 结果
     */
    @PostMapping("/xxx/create")
    R create(@RequestBody XxxEntity xxxEntity);

    /**
     * 更新
     * @param xxxEntity 实体
     * @return 结果
     */
    @PutMapping("/xxx/update")
    R update(@RequestBody XxxEntity xxxEntity);

    /**
     * 删除
     * @param ids ID数组
     * @return 结果
     */
    @DeleteMapping("/xxx/{ids}")
    R delete(@PathVariable Long[] ids);

    // 其他自定义接口...
}
```

### 3.2 服务降级处理模板

```java
package chat.aikf.xxx.api.factory;

import chat.aikf.common.core.domain.R;
import chat.aikf.xxx.api.RemoteXxxService;
import chat.aikf.xxx.api.domain.XxxEntity;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xxx服务降级处理
 */
public class RemoteXxxFallbackFactory implements FallbackFactory<RemoteXxxService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteXxxFallbackFactory.class);

    @Override
    public RemoteXxxService create(Throwable throwable) {
        log.error("Xxx服务调用失败：{}", throwable.getMessage());
        return new RemoteXxxService() {
            @Override
            public R list(XxxEntity xxxEntity) {
                return R.fail("查询Xxx列表失败：" + throwable.getMessage());
            }

            @Override
            public R get(Long id) {
                return R.fail("查询Xxx详情失败：" + throwable.getMessage());
            }

            @Override
            public R create(XxxEntity xxxEntity) {
                return R.fail("创建Xxx失败：" + throwable.getMessage());
            }

            @Override
            public R update(XxxEntity xxxEntity) {
                return R.fail("更新Xxx失败：" + throwable.getMessage());
            }

            @Override
            public R delete(Long[] ids) {
                return R.fail("删除Xxx失败：" + throwable.getMessage());
            }

            // 其他方法的降级处理...
        };
    }
}
```

### 3.3 数据传输对象模板

```java
package chat.aikf.xxx.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Xxx实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XxxEntity {

    /**
     * 主键
     */
    private Long id;

    // 其他字段...

}
```

### 3.4 自动配置文件模板

```
chat.aikf.xxx.api.RemoteXxxService
```

## 4. 生成流程

1. **模块验证**：检查OneChat-modules下的模块是否存在，如不存在则提示用户创建模块
2. **API模块创建**：检查OneChat-api下是否存在对应的OneChat-api-xxx模块，如不存在则创建
3. **目录结构创建**：创建必要的目录结构，包括api/domain、api/factory等
4. **生成远程服务接口**：根据模板生成RemoteXxxService接口
5. **生成服务降级处理**：根据模板生成RemoteXxxFallbackFactory类
6. **生成数据传输对象**：根据模块中的实体类生成对应的DTO
7. **生成自动配置文件**：创建org.springframework.boot.autoconfigure.AutoConfiguration.imports文件
8. **更新依赖配置**：更新pom.xml文件，添加必要的依赖

## 5. 使用方法

### 5.1 生成接口

使用技能生成OneChat-api-xxx模块和相关接口文件。

### 5.2 调用接口

在其他模块中注入RemoteXxxService接口，即可调用远程服务：

```java
import chat.aikf.xxx.api.RemoteXxxService;
import chat.aikf.xxx.api.domain.XxxEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class SomeService {

    @Resource
    private RemoteXxxService remoteXxxService;

    public void doSomething() {
        // 调用远程服务
        XxxEntity entity = new XxxEntity();
        // 设置参数
        remoteXxxService.create(entity);
    }
}
```

## 6. 注意事项

1. **服务名称**：FeignClient中的value属性应与模块的服务名称一致，通常为onechat-xxx
2. **路径映射**：接口中的路径应与模块Controller中的路径一致
3. **参数类型**：确保参数类型与模块Controller中的参数类型一致
4. **降级处理**：实现FallbackFactory接口，提供服务降级逻辑
5. **依赖管理**：确保添加了必要的依赖，包括Spring Cloud OpenFeign和Hystrix
6. **自动配置**：确保创建了正确的自动配置文件，以便Spring能够扫描到Feign客户端

## 7. 示例

### 7.1 为OneChat-file模块生成接口

1. 创建OneChat-api-file模块
2. 生成RemoteFileService接口
3. 生成RemoteFileFallbackFactory类
4. 生成SysFile数据传输对象
5. 创建自动配置文件

### 7.2 调用示例

```java
import chat.aikf.file.api.RemoteFileService;
import chat.aikf.file.api.domain.SysFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@Service
public class DocumentService {

    @Resource
    private RemoteFileService remoteFileService;

    public SysFile uploadFile(MultipartFile file) {
        return remoteFileService.upload(file).getData();
    }
}
```

## 8. 总结

本技能提供了一个标准化的服务接口生成方案，能够根据OneChat-modules下的模块生成对应的对外接口，遵循项目的微服务架构规范。使用本技能可以快速生成标准的接口结构，减少手动编写重复代码的工作量，同时确保接口风格的一致性。

通过本技能生成的接口，其他模块可以通过Feign客户端方便地调用远程服务，实现模块间的解耦和通信。