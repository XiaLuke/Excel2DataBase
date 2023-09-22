package self.xf.excelprocess.base;

import lombok.Data;
import self.xf.excelprocess.inter.FilterIntroduction;

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
