package self.xf.excelprocess.eci.hnps.entity;

import lombok.Data;
import self.xf.excelprocess.inter.EntryKey;

import java.util.Date;

@Data
public class zmqd {
    @EntryKey
    private String AUTO_ID;
    private String HSCODE;
    private String HSNAME;
    private Date CREATE_DATE;
}
