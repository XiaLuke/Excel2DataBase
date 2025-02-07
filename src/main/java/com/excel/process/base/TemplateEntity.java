package com.excel.process.base;

import com.excel.process.inter.FilterIntroduction;
import lombok.Data;

@Data
public class TemplateEntity {
    @FilterIntroduction(description = "名字")
    private String typeString;
    @FilterIntroduction(description = "存在小数的数据")
    private Double typeDouble;
    @FilterIntroduction(description = "开关")
    private Boolean typeBoolean;
    @FilterIntroduction(description = "整数")
    private Integer typeInteger;
}
