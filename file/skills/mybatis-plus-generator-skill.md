# MyBatis-Plus 代码生成器技能

## 1. 技能描述

本技能提供MyBatis-Plus代码自动生成功能，根据数据库表结构生成对应的实体类、Mapper接口和XML文件，遵循OneChat项目的代码结构规范。

## 2. 生成规则

### 2.1 目录结构

生成的代码将按照以下目录结构放置：

- **实体类**：`OneChat-api/OneChat-api-{module}/src/main/java/chat/aikf/{module}/api/domain/{EntityName}.java`
- **Mapper接口**：`OneChat-modules/OneChat-{module}/src/main/java/chat/aikf/{module}/mapper/{EntityName}Mapper.java`
- **XML文件**：`OneChat-modules/OneChat-{module}/src/main/resources/mapper/{module}/{EntityName}Mapper.xml`
- **Service接口**：`OneChat-modules/OneChat-{module}/src/main/java/chat/aikf/{module}/service/I{EntityName}Service.java`
- **Service实现类**：`OneChat-modules/OneChat-{module}/src/main/java/chat/aikf/{module}/service/impl/{EntityName}ServiceImpl.java`
- **Controller**：`OneChat-modules/OneChat-{module}/src/main/java/chat/aikf/{module}/controller/{EntityName}Controller.java`
- **接口文档**：`OneChat-modules/OneChat-{module}/src/main/resources/apiMd/{EntityName}Controller.md`
- **SQL文件**：`OneChat-modules/OneChat-{module}/src/main/resources/sql/{EntityName}.sql`

### 2.2 命名规范

- **实体类**：使用大驼峰命名法，如 `OneChatCategory`
- **Mapper接口**：实体名 + Mapper，如 `OneChatCategoryMapper`
- **XML文件**：与Mapper接口同名，如 `OneChatCategoryMapper.xml`
- **Service接口**：I + 实体名 + Service，如 `IOneChatCategoryService`
- **Service实现类**：实体名 + ServiceImpl，如 `OneChatCategoryServiceImpl`
- **SQL文件**：与实体名同名，如 `OneChatCategory.sql`
- **数据库表名**：使用小写字母加下划线，以 `one_chat` 开头，如 `one_chat_category`，避免使用MySQL关键词
- **字段名**：使用小写字母加下划线，避免使用MySQL关键词

## 3. 生成模板

### 3.1 实体类模板

```java
package chat.aikf.{module}.api.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName {tableName}
 */
@TableName(value ="{tableName}")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class {EntityName} extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private Long id;

    // 其他字段将根据数据库表结构自动生成

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

}
```

### 3.2 Mapper接口模板

```java
package chat.aikf.{module}.mapper;

import chat.aikf.{module}.api.domain.{EntityName};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author robin
* @description 针对表【{tableName}({tableComment})】的数据库操作Mapper
* @createDate {createDate}
* @Entity chat.aikf.{module}.api.domain.{EntityName}
*/
public interface {EntityName}Mapper extends BaseMapper<{EntityName}> {

}
```

### 3.3 XML文件模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="chat.aikf.{module}.mapper.{EntityName}Mapper">


</mapper>
```

### 3.4 Service接口模板

```java
package chat.aikf.{module}.service;

import chat.aikf.{module}.api.domain.{EntityName};
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【{tableName}({tableComment})】的数据库操作Service
* @createDate {createDate}
*/
public interface I{EntityName}Service extends IService<{EntityName}> {

    /**
     * 查询列表
     * @param {entityName}
     * @return
     */
    List<{EntityName}> findList({EntityName} {entityName});

}
```

### 3.5 Service实现类模板

```java
package chat.aikf.{module}.service.impl;

import chat.aifk.common.datascope.annotation.DataScope;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.{module}.api.domain.{EntityName};
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.{module}.service.I{EntityName}Service;
import chat.aikf.{module}.mapper.{EntityName}Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【{tableName}({tableComment})】的数据库操作Service实现
* @createDate {createDate}
*/
@Service
public class {EntityName}ServiceImpl extends ServiceImpl<{EntityName}Mapper, {EntityName}>
    implements I{EntityName}Service {

    @Override
    @DataScope
    public List<{EntityName}> findList({EntityName} {entityName}) {
        List<{EntityName}> {entityName}s = this.list(new LambdaQueryWrapper<{EntityName}>()
                .orderByDesc({EntityName}::getCreateTime));
        return {entityName}s;
    }
}
```

### 3.6 Controller模板

```java
package chat.aikf.{module}.controller;

import chat.aikf.common.core.domain.R;
import chat.aikf.{module}.api.domain.{EntityName};
import chat.aikf.{module}.service.I{EntityName}Service;
import chat.aikf.common.core.web.controller.BaseController;
import chat.aikf.common.core.web.page.TableDataInfo;
import chat.aikf.common.log.annotation.Log;
import chat.aikf.common.log.enums.BusinessType;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author robin
* @description 针对表【{tableName}({tableComment})】的控制器
* @createDate {createDate}
*/
@RestController
@RequestMapping("/{entityName}")
public class {EntityName}Controller extends BaseController {

    @Resource
    private I{EntityName}Service {entityName}Service;

    /**
     * 查询列表
     */
    @GetMapping("/list")
    public TableDataInfo list({EntityName} {entityName}) {
         startPage();
        List<{EntityName}> list = {entityName}Service.findList({entityName});
        return getDataTable(list);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/get/{id}")
    public R get(@PathVariable Long id) {
        return R.ok({entityName}Service.getById(id));
    }

    /**
     * 创建
     */
    @Log(title = "创建{entityName}", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R create(@RequestBody {EntityName} {entityName}) {
        {entityName}Service.save({entityName});
        return R.ok();
    }

    /**
     * 更新
     */
    @Log(title = "更新{entityName}", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R update(@RequestBody {EntityName} {entityName}) {
        {entityName}Service.updateById({entityName});
        return R.ok();
    }

    /**
     * 删除
     */
    @Log(title = "删除{entityName}", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable Long[] ids) {
         {entityName}Service.removeBatchByIds(Arrays.asList(ids));
        return R.ok();
    }
}
```

### 3.7 SQL文件模板

```sql
-- 创建表 {tableName}
-- 注意：表名和字段名避免使用MySQL关键词
CREATE TABLE `{tableName}` (
  `id` bigint NOT NULL COMMENT '主键',
  
  -- 其他字段将根据实体类属性自动生成
  -- 注意：字段名避免使用MySQL关键词
  
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='{tableComment}';

-- 示例数据（可选）
-- INSERT INTO `{tableName}` (`id`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
-- (1, 'admin', NOW(), 'admin', NOW());
```

## 4. 生成流程

1. **模块验证**：验证用户是否指定了模块，如未指定则提示用户必须指定模块；验证指定的模块是否存在，如不存在则提示用户创建模块
2. **检查目录结构**：检查目标模块是否存在，如不存在mapper、service、controller、apiMd、sql等文件夹则自动创建
3. **读取数据库表结构**：连接数据库，读取表结构信息
4. **生成实体类**：根据表结构生成对应的实体类，继承BaseEntity
5. **生成Mapper接口**：生成继承BaseMapper的Mapper接口
6. **生成XML文件**：生成对应的XML映射文件
7. **生成Service接口**：生成继承IService的Service接口
8. **生成Service实现类**：生成Service接口的实现类
9. **生成Controller**：生成继承BaseController的控制器
10. **生成接口文档**：生成对应的接口文档，放置在apiMd目录下
11. **生成SQL文件**：生成数据库表创建SQL语句
12. **验证生成结果**：检查生成的文件是否符合项目规范

## 5. 使用方法

### 5.1 命令格式

```bash
# 生成单个表的代码
java -jar mybatis-plus-generator.jar --module {module} --table {tableName}

# 生成多个表的代码
java -jar mybatis-plus-generator.jar --module {module} --tables {tableName1},{tableName2}

# 生成整个数据库的代码
java -jar mybatis-plus-generator.jar --module {module} --all
```

### 5.2 约束条件

- **模块必须指定**：使用代码生成器时必须通过 `--module` 参数指定具体的模块名称，如 `--module ai`、`--module ops` 等
- **未指定模块的处理**：如果未指定模块，生成器会直接提示用户必须指定模块，不会进行后续操作
- **模块验证**：生成器会验证指定的模块是否存在，如不存在会提示用户创建模块后再使用

### 5.3 配置参数

| 参数 | 描述 | 示例 |
|------|------|------|
| --module | 模块名称（如 ops, system, im 等） | --module ops |
| --table | 表名 | --table one_chat_category |
| --tables | 多个表名，用逗号分隔 | --tables one_chat_category,one_chat_tag |
| --all | 生成所有表的代码 | --all |
| --package | 基础包名 | --package chat.aikf |
| --author | 作者名称 | --author robin |

## 6. 注意事项

1. **数据库连接**：确保数据库连接配置正确，能够访问目标数据库
2. **模块存在性**：确保指定的模块已在项目中创建
3. **目录结构**：生成器会自动创建缺失的目录结构
4. **代码覆盖**：默认情况下，已存在的文件不会被覆盖，需要使用 --override 参数强制覆盖
5. **依赖关系**：确保项目中已添加MyBatis-Plus依赖
6. **命名规范**：表名和字段名避免使用MySQL关键词，如 `status`、`type`、`sort` 等，如需使用请添加前缀或使用同义词

## 7. 依赖要求

- **MyBatis-Plus**：3.5.10.1+
- **Lombok**：1.18.20+
- **Spring Boot**：3.3.5+

## 8. 示例

### 8.1 生成OneChatCategory相关代码

```bash
java -jar mybatis-plus-generator.jar --module ops --table one_chat_category
```

生成的文件：
- 实体类：`OneChat-api/OneChat-api-ops/src/main/java/chat/aikf/ops/api/domain/OneChatCategory.java`
- Mapper接口：`OneChat-modules/OneChat-ops/src/main/java/chat/aikf/ops/mapper/OneChatCategoryMapper.java`
- XML文件：`OneChat-modules/OneChat-ops/src/main/resources/mapper/ops/OneChatCategoryMapper.xml`
- Service接口：`OneChat-modules/OneChat-ops/src/main/java/chat/aikf/ops/service/IOneChatCategoryService.java`
- Service实现类：`OneChat-modules/OneChat-ops/src/main/java/chat/aikf/ops/service/impl/OneChatCategoryServiceImpl.java`
- Controller：`OneChat-modules/OneChat-ops/src/main/java/chat/aikf/ops/controller/OneChatCategoryController.java`
- 接口文档：`OneChat-modules/OneChat-ops/src/main/resources/apiMd/OneChatCategoryController.md`
- SQL文件：`OneChat-modules/OneChat-ops/src/main/resources/sql/OneChatCategory.sql`

### 8.2 生成多个表的代码

```bash
java -jar mybatis-plus-generator.jar --module ops --tables one_chat_category,one_chat_tag
```

生成的文件：
- `OneChatCategory.java`、`OneChatCategoryMapper.java`、`OneChatCategoryMapper.xml`
- `OneChatTag.java`、`OneChatTagMapper.java`、`OneChatTagMapper.xml`

## 9. 代码生成器实现

代码生成器基于MyBatis-Plus的AutoGenerator实现，通过自定义模板和配置，生成符合OneChat项目规范的代码。

### 9.1 核心配置

```java
// 数据源配置
DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(
    "jdbc:mysql://localhost:3306/onechat",
    "root",
    "password"
).build();

// 全局配置
GlobalConfig globalConfig = new GlobalConfig.Builder()
    .author("robin")
    .outputDir(System.getProperty("user.dir") + "/src/main/java")
    .build();

// 包配置
PackageConfig packageConfig = new PackageConfig.Builder()
    .parent("chat.aikf")
    .moduleName(module)
    .entity("api.domain")
    .mapper("mapper")
    .service("service")
    .serviceImpl("service.impl")
    .build();

// 策略配置
StrategyConfig strategyConfig = new StrategyConfig.Builder()
    .addInclude(tables)
    .entityBuilder()
        .superClass(BaseEntity.class)
        .enableLombok()
        .enableTableFieldAnnotation()
    .mapperBuilder()
        .superClass(BaseMapper.class)
        .build()
    .serviceBuilder()
        .formatServiceFileName("I%sService")
        .formatServiceImplFileName("%sServiceImpl")
        .build();

// 执行生成
new AutoGenerator(dataSourceConfig)
    .global(globalConfig)
    .packageInfo(packageConfig)
    .strategy(strategyConfig)
    .execute();
```

### 9.2 自定义模板

通过Velocity模板引擎自定义生成模板，确保生成的代码符合OneChat项目的代码规范。

## 10. 总结

本技能提供了一个标准化的MyBatis-Plus代码生成器，能够根据数据库表结构自动生成符合OneChat项目规范的实体类、Mapper接口和XML文件，提高开发效率，保证代码质量。

使用本技能可以快速生成标准的代码结构，减少手动编写重复代码的工作量，同时确保代码风格的一致性。