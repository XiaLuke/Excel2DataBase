package self.xf.excelprocess.hnps.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZmqdMapper {
    void save(@Param("requestList") List<Object> list);
}
