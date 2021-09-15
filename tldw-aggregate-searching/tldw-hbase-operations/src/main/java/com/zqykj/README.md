
# 使用方式
## 集成
在spring-boot项目的application.properties文件中加入spring.data.hbase.quorum,spring.data.hbase.rootDir,spring.data.hbase.nodeParent配置项，并赋予正确的值
## 使用
### query
1. 将上述配置项赋予正确的值
2. dto定义
```java
public class PeopleDto {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public PeopleDto setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public PeopleDto setAge(int age) {
        this.age = age;
        return this;
    }
}
```
3. RowMapper定义
```java
public class PeopleRowMapper implements RowMapper<PeopleDto> {

    private static byte[] COLUMNFAMILY = "f".getBytes();
    private static byte[] NAME = "name".getBytes();
    private static byte[] AGE = "age".getBytes();

    @Override
    public PeopleDto mapRow(Result result, int rowNum) throws Exception {
        PeopleDto dto = new PeopleDto();
        // TODO: 设置相关的属性值
        String name = Bytes.toString(result.getValue(COLUMNFAMILY, NAME));
        int age = Bytes.toInt(result.getValue(COLUMNFAMILY, AGE));

        return dto.setName(name).setAge(age);
    }
}
```
4. query操作
```java

public class QueryService {

    @Autowired
    private HbaseTemplate hbaseTemplate;

    public List<PeopleDto> query(String startRow, String stopRow) {
        Scan scan = new Scan(Bytes.toBytes(startRow), Bytes.toBytes(stopRow));
        scan.setCaching(5000);
        List<PeopleDto> dtos = this.hbaseTemplate.find("people_table", scan, new PeopleRowMapper());
        return dtos;
    }

    public PeopleDto query(String row) {
        PeopleDto dto = this.hbaseTemplate.get("people_table", row, new PeopleRowMapper());
        return dto;
    }
}
```
### update等
1. 将上述配置项赋予正确的值
2. update、delete、put操作
```java
@Service
public class QueryService {

    @Autowired
    private HbaseTemplate hbaseTemplate;

    public List<PeopleDto> query(String startRow, String stopRow) {
        Scan scan = new Scan(Bytes.toBytes(startRow), Bytes.toBytes(stopRow));
        scan.setCaching(5000);
        List<PeopleDto> dtos = this.hbaseTemplate.find("people_table", scan, new PeopleRowMapper());
        return dtos;
    }

    public PeopleDto query(String row) {
        PeopleDto dto = this.hbaseTemplate.get("people_table", row, new PeopleRowMapper());
        return dto;
    }

    public void saveOrUpdates() {
        List<Mutation> puts = new ArrayList<>();
        // 设值
        this.hbaseTemplate.saveOrUpdates("people_table", puts);
    }

    public void saveOrUpdate() {
        Mutation delete = new Delete(Bytes.toBytes(""));
        this.hbaseTemplate.saveOrUpdate("people_table", delete);
    }
    
    public void saveOrUpdate() {
        List<Mutation> saveOrUpdates = new ArrayList<>();
        Put put = new Put(Bytes.toBytes("135xxxxxx"));
        put.addColumn(Bytes.toBytes("people"), Bytes.toBytes("name"), Bytes.toBytes("JThink"));
        saveOrUpdates.add(put);

        Delete delete = new Delete(Bytes.toBytes("136xxxxxx"));
        saveOrUpdates.add(delete);

        // 继续add

        this.hbaseTemplate.saveOrUpdates("people_table", saveOrUpdates);
    }
}
```

### 其他
不可以满足需求的可以使用hbaseTemplate暴露出来的getConnection()方法
