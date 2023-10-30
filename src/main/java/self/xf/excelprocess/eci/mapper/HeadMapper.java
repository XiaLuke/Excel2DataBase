package self.xf.excelprocess.eci.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HeadMapper {
    void save(@Param("requestList") List<Object> list);
}
