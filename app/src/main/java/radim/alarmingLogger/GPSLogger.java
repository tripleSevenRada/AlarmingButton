package radim.alarmingLogger;

import android.app.Application;
import org.greenrobot.greendao.database.Database;

import radim.alarmingLogger.logging.DaoMaster;
import radim.alarmingLogger.logging.DaoSession;


public class GPSLogger extends Application {
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
    public static final boolean ENCRYPTED = false;

    private DaoSession daoSession;

    private static final String MY_PROJECTS = "ALL_MY_PROJECTS";

    /**
     *
     *
     *
     */
    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? MY_PROJECTS +"-db-encrypted" : MY_PROJECTS +"-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

    }
    /**
     *
     *
     *
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
