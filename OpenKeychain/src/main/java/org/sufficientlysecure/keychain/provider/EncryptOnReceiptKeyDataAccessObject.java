package org.sufficientlysecure.keychain.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.sufficientlysecure.keychain.provider.KeychainContract.ApiEncryptOnReceiptKey;

import timber.log.Timber;

/**
 * Created on 7/5/2018.
 *
 * @author koh
 */
public class EncryptOnReceiptKeyDataAccessObject {
    private final SimpleContentResolverInterface queryInterface;
    private final String packageName;


    public EncryptOnReceiptKeyDataAccessObject(Context context, String packageName) {
        this.packageName = packageName;

        final ContentResolver contentResolver = context.getContentResolver();
        queryInterface = new SimpleContentResolverInterface() {
            @Override
            public Cursor query(Uri contentUri, String[] projection, String selection, String[] selectionArgs,
                                String sortOrder) {
                return contentResolver.query(contentUri, projection, selection, selectionArgs, sortOrder);
            }

            @Override
            public Uri insert(Uri contentUri, ContentValues values) {
                return contentResolver.insert(contentUri, values);
            }

            @Override
            public int update(Uri contentUri, ContentValues values, String where, String[] selectionArgs) {
                return contentResolver.update(contentUri, values, where, selectionArgs);
            }

            @Override
            public int delete(Uri contentUri, String where, String[] selectionArgs) {
                return contentResolver.delete(contentUri, where, selectionArgs);
            }
        };
    }

    public void updateKey(long keyId, long newMasterKeyId) {
        ContentValues cv = new ContentValues();
        cv.put(ApiEncryptOnReceiptKey.MASTER_KEY_ID, newMasterKeyId);

        Uri uri = ApiEncryptOnReceiptKey.buildByPackageNameAndKeyId(packageName, Long.toString(keyId));

        Timber.d("updateKey generated uri=%s", uri.getPath());

        queryInterface.update(uri, cv, null, null);
    }
}
