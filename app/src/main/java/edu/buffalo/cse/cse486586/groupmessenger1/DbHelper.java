package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * Created by shivang on 2/14/16.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MSGDATABASE";
    private static final String CREATE_QUERY = "CREATE TABLE "
            + GroupMessengerProvider.TableName
            + "(" + GroupMessengerProvider.KEY_FIELD +" TEXT PRIMARY KEY,"
            + GroupMessengerProvider.VALUE_FIELD +" TEXT);";

    public DbHelper(Context context){

        super(context,DATABASE_NAME,null,1);
        Log.e("DATABASE","DB Created");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_QUERY);
        Log.e("TABLE", "Table Created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
