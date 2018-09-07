package org.sufficientlysecure.keychain.daos;

import android.content.Context;
import android.database.Cursor;

import com.squareup.sqldelight.SqlDelightQuery;

import org.sufficientlysecure.keychain.EncryptOnReceiptKeysModel.InsertKey;
import org.sufficientlysecure.keychain.EncryptOnReceiptKeysModel.UpdateKey;
import org.sufficientlysecure.keychain.KeychainDatabase;
import org.sufficientlysecure.keychain.model.EncryptOnReceiptKey;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * Created on 7/5/2018.
 *
 * @author koh
 */
public class EncryptOnReceiptKeyDao extends AbstractDao {

    public static EncryptOnReceiptKeyDao getInstance(Context context) {
        KeychainDatabase keychainDatabase = KeychainDatabase.getInstance(context);
        DatabaseNotifyManager databaseNotifyManager = DatabaseNotifyManager.create(context);

        return new EncryptOnReceiptKeyDao(keychainDatabase, databaseNotifyManager);
    }

    private EncryptOnReceiptKeyDao(KeychainDatabase database, DatabaseNotifyManager databaseNotifyManager) {
        super(database, databaseNotifyManager);
    }

    public void updateKey(String packageName, String identifier, long masterKeyId) {
        ensureEorKeyExists(packageName, identifier);

        UpdateKey updateStatement = new UpdateKey(getWritableDb());
        updateStatement.bind(packageName, identifier, masterKeyId);
        updateStatement.executeUpdateDelete();
        Timber.d("updateKey masterKeyId=%s, identifier=%s", masterKeyId, identifier);

        getDatabaseNotifyManager().notifyEncryptOnReceiptUpdate(masterKeyId);
    }

    public Set<Long> getMasterKeyIds(boolean verifiedOnly) {
        final Set<Long> keyIds = new HashSet<>();
        SqlDelightQuery query;

        if (verifiedOnly) {
            query = EncryptOnReceiptKey.FACTORY.getMasterKeyIdsVerified();
        } else {
            query = EncryptOnReceiptKey.FACTORY.getMasterKeyIds();
        }

        try (Cursor cursor = getReadableDb().query(query)) {
            Timber.d("Got cursor (count=%s)", cursor.getCount());

            while (cursor.moveToNext()) {
                Long masterKeyId = cursor.getLong(0);
                keyIds.add(masterKeyId);
                Timber.d("EORKeyDataAccessObject got master key ID %s", masterKeyId);
            }
        }

        return keyIds;
    }

    private void ensureEorKeyExists(String packageName, String identifier) {
        InsertKey insertStatement = new InsertKey(getWritableDb());
        insertStatement.bind(packageName, identifier, false);
        insertStatement.executeInsert();
    }
}
