package self.xf.excelprocess.test;

import lombok.Data;
import self.xf.excelprocess.inter.EntryKey;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Test11 {
    @EntryKey
    private String autoId;
    private String sCode;
    private String sName;
    private Date createDate;
    private BigDecimal num;
    private Integer used;
}
