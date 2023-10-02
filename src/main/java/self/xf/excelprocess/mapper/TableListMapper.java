package self.xf.excelprocess.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface TableListMapper {
    @Select("select table_name from information_schema.tables where table_schema='test'")
    List<String> tableList();

    @Select("select column_name from information_schema.columns where table_name = #{tableName}")
    List<String> columnList(String tableName);

    // 获取建表语句
    @Select("show create table t_bank")
    Map<String,String> showCreateTable();


    // 执行创表语句
    @Update("${sql}")
    void createTable(@Param("sql") String sql);
}
