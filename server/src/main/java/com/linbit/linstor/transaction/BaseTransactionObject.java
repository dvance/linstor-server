package com.linbit.linstor.transaction;

import java.util.List;

import javax.inject.Provider;

public abstract class BaseTransactionObject extends AbsTransactionObject
{
    protected List<TransactionObject> transObjs;

    public BaseTransactionObject(Provider<TransactionMgr> transMgrProvider)
    {
        super(transMgrProvider);
    }

    @Override
    public void commitImpl()
    {
        for (TransactionObject transObj : transObjs)
        {
            if (transObj.isDirty())
            {
                transObj.commit();
            }
        }
    }

    @Override
    public void rollbackImpl()
    {
        for (TransactionObject transObj : transObjs)
        {
            if (transObj.isDirty())
            {
                transObj.rollback();
            }
        }
    }

    @Override
    public boolean isDirty()
    {
        boolean dirty = false;
        for (TransactionObject transObj : transObjs)
        {
            if (transObj.isDirty())
            {
                dirty = true;
                break;
            }
        }
        return dirty;
    }

    @Override
    public boolean isDirtyWithoutTransMgr()
    {
        boolean dirty = false;
        if (hasTransMgr())
        {
            for (TransactionObject transObj : transObjs)
            {
                if (transObj.isDirtyWithoutTransMgr())
                {
                    dirty = true;
                    break;
                }
            }
        }
        return dirty;
    }
}
