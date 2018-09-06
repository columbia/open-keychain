package org.sufficientlysecure.keychain.model;

import com.google.auto.value.AutoValue;

import org.sufficientlysecure.keychain.EncryptOnReceiptKeysModel;

/**
 * <p>
 * Created on 9/6/2018.
 *
 * @author koh
 */
@AutoValue
public abstract class EncryptOnReceiptKey implements EncryptOnReceiptKeysModel {
    public static final Factory<EncryptOnReceiptKey> FACTORY = new Factory<>(AutoValue_EncryptOnReceiptKey::new);
}
