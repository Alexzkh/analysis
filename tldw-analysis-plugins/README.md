# 关于插件化开发说明

**可插拔应用系统插件化开发的好处在于基于配置切换，功能、程序解耦。符合软件开发设计原则之单一职责原则（Single Responsibility Principle，缩写为SRP:A class or module should have a single responsibility）、接口隔离原则 （Interface Segregation Principle”，缩写为 ISP。Clients should not be forced to depend upon interfaces that they do not use。”）等原则。同时也满足“高内聚、低耦合、高扩展 、低成本维护“的原则。**

资金战法插件化开发主要针对"资金"相关，诸如调单个体聚合、来源去向、以及快进快出等等相关业务分析而构成组件在系统平台上使用。代码和业务低耦合，部署更加便捷。介绍主要集成方式。

## 1、插件在系统中的整体结构(开发环境)

```
-santaizi(tldw-analysis-service)
    - tldw-analysis-common
        - pom.xml
    - tldw-analysis-domain
        - pom.xml
    - tldw-aggregate-searching
        - pom.xml
    - tldw-analysis-app
        - pom.xml
    - tldw-analysis-server
        - pom.xml
    - tldw-analysis-plugin-common
        - pom.xml
    - tldw-analysis-runner
        - pom.xml
    - tldw-analysis-plugins-parent
        - pom.xml
    - tldw-analysis-plugins
        - tldw-funds-tactics-plugins
            - pom.xml
        - tldw-×××-plugins
            - pom.xml
        - pom.xml
    - pom.xml
```

**explain**:

- pom.xml 代表maven的pom.xml.
- santaizi为分析服务端总Maven目录.
- other-modules为分析服务其他基础子模块.
- tldw-analysis-server为分析服务main module.
- tldw-analysis-common主要存放聚合等其他模块公共类
- tldw-analysis-domain存放不同domain下的entity类
- tldw-aggregate-searching,聚合查询模块，主要实现Elasticsearch、HBase的操作，可扩展诸如Mongodb、Solr等其他关系型和非关系型数据库的实现。
- tldw-analysis-app主要存放接受数据操作层数据，对应springmvc中的service层。
- tldw-analysis-plugin-common对应插件的公共组件模块
- tldw-analysis-runner在运行环境下启动的模块，主要依赖tldw-analysis-server模块和插件包中使用到的依赖包，并且解决开发环境下无法找到依赖包的问题。可自行选择是否需要（可选）
- tldw-analysis-plugins-parent 该模块为插件模块的父模块，主要定义各个插件中公共使用到的依赖，以及插件的打包配置。
- tldw-analysis-plugins 文件夹下主要存储插件模块。上述模块中主要包括tldw-funds-tactics-plugins、tldw-×××-plugins等插件.
- tldw-funds-tactics-plugins、tldw-×××-plugins分别为两个插件Maven包。



## 2、插件开发

### 2.1、插件配置包XML配置说明

以<scope>provided</scope>的方式引入主程序包。即：

```
<dependency>
    <groupId>com.zqykj</groupId>
    <artifactId>tldw-analysis-server</artifactId>
    <version>${version}</version>
    <scope>provided</scope>
</dependency>
```

### 2.2、在插件`tldw-funds-tactics-plugins`    `  模块的  `   `resources`目录下新建文件 ``plugin.properties` 文件中新增如下内容：

```
plugin.id=tldw-funds-tactics-plugins
plugin.class=BasePlugin 的集成类全包路径
plugin.version=版本号
plugin.provider=alex(可选)
plugin.description=插件描述(可选)
plugin.requires=主程序需要的版本(可选)

explain:
plugin.id: 插件id
plugin.class: 插件实现类全包路径。也就是步骤三的类包名
plugin.version: 插件版本
plugin.provider: 插件作者(可选)
plugin.description: 插件描述(可选)
plugin.requires: 主程序需要的版本, 搭配主程序配置的 version 来校验插件是否可安装
```

### **2.3、继承`com.gitee.starblues.realize.BasePlugin`包**

```
import com.gitee.starblues.realize.BasePlugin;
import org.pf4j.PluginWrapper;


public class DefinePlugin extends BasePlugin {
    public DefinePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}
```

### 2.4、验证插件是否加载成功

```
@RestController
@RequestMapping(path = "plugin1")
public class HelloPlugin1 {

    @GetMapping()
    public String getConfig(){
        return "hello plugin1 example";
    }

}
explain test code :
 1、插件需要编译，确保target目录下，存在编译的class文件。
 2、启动主程序，控制台输出：
 -Plugin 'tldw-funds-tactics-plugins@0.0.1-SNAPSHOT' resolved
 -Start plugin 'tldw-funds-tactics-plugins@0.0.1-SNAPSHOT'
 -Plugins initialize success
 3、访问插件中HelloPlugin1 的controller进行验证
 浏览器访问：http://ip:port/plugins/tldw-funds-tactics-plugins/plugin1
 explain url : /plugins: 为插件地址前缀(可使用pluginRestPathPrefix进行配置)
 			   /tldw-funds-tactics-plugins: 为插件id
 			   /plugin1: 为插件中地址的接口地址
 4、响应并显示：hello plugin1 explample .
```

### 2.5 主程序自动扫描插件配置

```
@Configuration
@Import(AutoIntegrationConfiguration.class)
public class PluginBeanConfig {
    @Bean
    public PluginApplication pluginApplication(){
        // 实例化自动初始化插件的PluginApplication
        return new AutoPluginApplication();
    }
}

```

### 重要提示（！！！）：插件运行主程序前一定要手动编译插件模块, 确保`target`目录下存在编译的`class`文件。

## 3、其他功能

### 3.1、插件中的配置文件功能

插件中如需配置文件可使用以下注解

```
**
 * 插件配置对应的bean定义注解
 * 如果存在配置文件, 则进行属性自定义
 * 如果未依赖配置文件, 则直接定义注解即可
 * @version 2.4.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigDefinition {


    /**
     * 插件中的配置文件的名称, 新版本替换 value 值
     * @return String
     */
    String fileName() default "";

    /**
     * 开发环境下文件后缀
     * 如果文件名称为: xxx.yml, 根据当前配置(当前配置为-dev)在开发环境下文件后缀为: xxx-dev.yml
     * @return 开发环境下文件名称后缀, 比如 dev
     */
    String devSuffix() default "";

    /**
     * 生产环境下文件后缀
     * 如果文件名称为: xxx.yml, 根据当前配置(当前配置为-prod)在生产环境下文件后缀为: xxx-prod.yml
     * @return 生产环境下文件名称后缀, 比如 -prod
     */
    String prodSuffix() default "";

}
```

### 3.2、For Example :

在/resource目录下的 配置文件为：plugin-spring-dev.yml，那么此时在上述`2.3`的类上就要如下所示注解：

```
@ConfigDefinition(fileName = "plugin1-spring.yml", devSuffix = "-dev")
public class DefinePlugin extends BasePlugin {
    public DefinePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}
```

## 4 、插件配置详细说明（@see AutoIntegrationConfiguration.class）

| 配置项                       | 值类型  | 适用环境 | 配置说明                                                     |
| :--------------------------- | :------ | :------- | :----------------------------------------------------------- |
| runMode                      | String  | dev&prod | 运行模式, 开发环境: dev/development; 生产环境: prod(deployment) |
| enable                       | Boolean | dev&prod | 是否启用插件功能, 默认启用                                   |
| pluginPath                   | String  | dev&prod | 插件的路径, 开发环境下配置为插件模块上级目录; 生产环境下配置到插件jar包存放目录。建议配置绝对路径. 默认: /plugins |
| pluginConfigFilePath         | String  | prod     | 插件对应的配置文件存放目录, 只用于生产环境下. 默认: /plugin-config |
| pluginRestPathPrefix         | String  | dev&prod | 统一配置访问插件rest接口前缀. 默认: /plugins                 |
| enablePluginIdRestPathPrefix | Boolean | dev&prod | 是否启用插件id作为rest接口前缀, 默认为启用. 如果为启用, 则地址为 /pluginRestPathPrefix/pluginId, 其中pluginRestPathPrefix: 为pluginRestPathPrefix的配置值, pluginId: 为插件id |
| enableSwaggerRefresh         | Boolean | dev&prod | 是否启用Swagger刷新机制. 默认启用                            |
| backupPath                   | String  | prod     | 在卸载插件后, 备份插件的目录                                 |
| uploadTempPath               | String  | prod     | 上传的插件所存储的临时目录                                   |
| version                      | String  | dev&prod | 当前主程序的版本号, 用于校验插件是否可安装. 插件中可通过插件配置信息 requires 来指定可安装的主程序版本。如果为: 0.0.0 的话, 表示不校验 |
| exactVersionAllowed          | Boolean | dev&prod | 设置为true表示插件设置的requires的版本号完全匹配version版本号才可允许插件安装, 即: requires=x.y.z; 设置为false表示插件设置的requires的版本号小于等于version值, 插件就可安装, 即requires<=x.y.z。默认为false |
| enablePluginIds              | Set     | dev&prod | 启用的插件id                                                 |
| disablePluginIds             | Set     | dev&prod | 禁用的插件id, 禁用后系统不会启动该插件, 如果禁用所有插件, 则Set集合中返回一个字符: * |
| sortInitPluginIds            | Set     | dev&prod | 设置初始化时插件启动的顺序                                   |
| enableWebSocket              | Boolean | dev&prod | 是否启用webSocket的功能. 默认禁用                            |

 

***其他详细操作，参考官方文档：http://www.starblues.cn/***

