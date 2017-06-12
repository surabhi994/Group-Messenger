package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;

import static android.app.PendingIntent.getActivity;
import static org.apache.http.util.EncodingUtils.getString;

/**
 * GroupMessengerProvider is a key-value table.
 */
public class GroupMessengerProvider extends ContentProvider {
    HashMap hm = new HashMap();
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        return 0;
    }

    @Override
    public String getType(Uri uri) {

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {



    try {
        SharedPreferences sharedPref;
        sharedPref = getContext().getSharedPreferences("Your Pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPref.edit();
        ed.putString(values.getAsString("key"), values.getAsString("value"));
        ed.commit();
    }

catch(Exception e) {
            Log.v("insert", "fail");
        }

        getContext().getContentResolver().notifyChange(uri, null);


        Log.v("insert", values.toString());
        return uri;
    }



    @Override
    public boolean onCreate() {


        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        MatrixCursor cr = new MatrixCursor(new String[] { "key" , "value"});



        SharedPreferences sharedPref = getContext().getSharedPreferences("Your Pref",Context.MODE_PRIVATE);

        String val= sharedPref.getString(selection, null);


            MatrixCursor.RowBuilder builder = cr.newRow();
            builder.add("key", selection);
            builder.add("value", val);


        cr.setNotificationUri(getContext().getContentResolver(),uri);




        Log.v("query", selection);
        return cr;

    }
}
