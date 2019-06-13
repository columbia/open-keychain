package org.sufficientlysecure.keychain.remote;

import android.content.Context;
import android.support.annotation.Nullable;

import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.daos.KeyWritableRepository;
import org.sufficientlysecure.keychain.operations.results.SaveKeyringResult;
import org.sufficientlysecure.keychain.pgp.UncachedKeyRing;
import org.sufficientlysecure.keychain.pgp.exception.PgpGeneralException;
import org.sufficientlysecure.keychain.daos.EncryptOnReceiptKeyDao;

import java.io.IOException;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created on 7/9/2018.
 *
 * @author koh
 */
public class EncryptOnReceiptInteractor {

    private EncryptOnReceiptKeyDao e3KeyDataDao;
    private KeyWritableRepository keyWritableRepository;

    public static EncryptOnReceiptInteractor getInstance(Context context, EncryptOnReceiptKeyDao e3KeyDataDao) {
        KeyWritableRepository keyWritableRepository = KeyWritableRepository.create(context);

        return new EncryptOnReceiptInteractor(e3KeyDataDao, keyWritableRepository);
    }

    private EncryptOnReceiptInteractor(EncryptOnReceiptKeyDao e3KeyDataDao,
                                KeyWritableRepository keyWritableRepository) {
        this.e3KeyDataDao = e3KeyDataDao;
        this.keyWritableRepository = keyWritableRepository;
    }

    public void updateEncryptOnReceiptKey(String packageName, byte[] keyData) {
        SaveKeyringResult saveKeyringResult = parseAndImportKeyData(keyData);
        if (saveKeyringResult == null) {
            return;
        }

        Timber.d("updateEncryptOnReceiptKey saveKeyringResult.savedMasterKeyId=%s", saveKeyringResult.savedMasterKeyId);

        Long newMasterKeyId = saveKeyringResult.savedMasterKeyId;
        e3KeyDataDao.updateKey(packageName, UUID.randomUUID().toString(), newMasterKeyId);
    }

    /**
     * Deletes EOR key.
     *
     * @param packageName
     * @param masterKeyId
     * @return null if fails, otherwise the master key ID
     */
    public Long deleteEncryptOnReceiptKey(String packageName, long masterKeyId) {
        if (keyWritableRepository.deleteKeyRing(masterKeyId)) {
            Timber.d("deleteEncryptOnReceiptKey deleting key with masterKeyId=%s", masterKeyId);
            e3KeyDataDao.deleteKey(packageName, masterKeyId);
            return masterKeyId;
        }

        Timber.d("deleteEncryptOnReceiptKey failed to delete key with masterKeyId=%s", masterKeyId);
        // failed to delete key
        return null;
    }

    @Nullable
    private SaveKeyringResult parseAndImportKeyData(byte[] keyData) {
        UncachedKeyRing uncachedKeyRing = parseKeyData(keyData);

        return uncachedKeyRing == null ? null : importKeyData(uncachedKeyRing);
    }

    @Nullable
    private SaveKeyringResult importKeyData(UncachedKeyRing uncachedKeyRing) {
        SaveKeyringResult saveKeyringResult = keyWritableRepository.savePublicKeyRing(uncachedKeyRing);
        if (!saveKeyringResult.success()) {
            Timber.e(Constants.TAG, "Error inserting key - ignoring!");
            return null;
        }
        return saveKeyringResult;
    }

    @Nullable
    private UncachedKeyRing parseKeyData(byte[] keyData) {
        UncachedKeyRing uncachedKeyRing;
        try {
            uncachedKeyRing = UncachedKeyRing.decodeFromData(keyData);
        } catch (IOException | PgpGeneralException e) {
            Timber.e(Constants.TAG, "Error parsing public key! - Ignoring");
            return null;
        }
        if (uncachedKeyRing.isSecret()) {
            Timber.e(Constants.TAG, "Found secret key in key data! - Ignoring");
            return null;
        }
        return uncachedKeyRing;
    }
}