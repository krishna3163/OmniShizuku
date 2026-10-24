package com.rosan.dhizuku;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public class ForwardDhizukuProvider extends ContentProvider {

    public static final String OMNISHIZUKU_AUTHORITY = "com.krishna.omnishizuku.dhizuku_server.provider";
    public static final String SHEVERY_AUTHORITY = "com.hamondev.shevery.dhizuku_server.provider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        try {
            if (getContext() != null) {
                for (String auth : new String[]{OMNISHIZUKU_AUTHORITY, SHEVERY_AUTHORITY}) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Uri uri = Uri.parse("content://" + auth);
                            Bundle res = getContext().getContentResolver().call(uri, method, arg, extras);
                            if (res != null) return res;
                        } else {
                            Bundle res = getContext().getContentResolver().call(auth, method, arg, extras);
                            if (res != null) return res;
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
