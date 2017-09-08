package radim.alarmingLogger.logging;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;

import java.sql.Timestamp;

@Entity
public class LogEntry {
    public String toString() {
        return entry + "  |  " + id;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntry() {
        return this.entry;
    }
    @Property(nameInDb = "ENTRY")
    private String entry;
    @Id(autoincrement = true)
    private Long id;
    
    @Generated(hash = 610612513)
    public LogEntry(String entry, Long id) {
        this.entry = entry;
        this.id = id;
    }

    @Generated(hash = 1393228716)
    public LogEntry() {
    }

    @Keep
    public LogEntry(String entry){
        Timestamp tmstmp = new Timestamp(System.currentTimeMillis());
        String stamp = tmstmp.toString();
        this.entry = stamp +" | "+ entry;
    }

}
