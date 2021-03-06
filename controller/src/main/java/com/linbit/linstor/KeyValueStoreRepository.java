package com.linbit.linstor;

import com.linbit.linstor.core.CoreModule;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.security.ProtectedObject;

/**
 * Provides access to {@link KeyValueStore}s with automatic security checks.
 */
public interface KeyValueStoreRepository extends ProtectedObject
{
    void requireAccess(AccessContext accCtx, AccessType requested)
        throws AccessDeniedException;

    KeyValueStoreData get(AccessContext accCtx, KeyValueStoreName nameRef)
        throws AccessDeniedException;

    void put(AccessContext accCtx, KeyValueStoreName kvsName, KeyValueStoreData kvs)
        throws AccessDeniedException;

    void remove(AccessContext accCtx, KeyValueStoreName kvsName)
        throws AccessDeniedException;

    CoreModule.KeyValueStoreMap getMapForView(AccessContext accCtx)
        throws AccessDeniedException;
}
