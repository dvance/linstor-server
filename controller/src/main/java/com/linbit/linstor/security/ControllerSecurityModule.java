package com.linbit.linstor.security;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.linbit.ImplementationError;
import com.linbit.linstor.ControllerDatabase;
import com.linbit.linstor.InitializationException;
import com.linbit.linstor.annotation.SystemContext;
import com.linbit.linstor.dbcp.DbConnectionPool;
import com.linbit.linstor.logging.ErrorReporter;

import javax.inject.Singleton;
import java.security.NoSuchAlgorithmException;

public class ControllerSecurityModule extends AbstractModule
{
    @Override
    protected void configure()
    {
    }

    @Provides
    public SecurityLevelSetter securityLevelSetter(
        final DbConnectionPool dbConnectionPool,
        final DbAccessor securityDbDriver
    )
    {
        return (accCtx, newLevel) ->
            SecurityLevel.set(accCtx, newLevel, dbConnectionPool, securityDbDriver);
    }

    @Provides
    public MandatoryAuthSetter mandatoryAuthSetter(
        final DbConnectionPool dbConnectionPool,
        final DbAccessor securityDbDriver
    )
    {
        return (accCtx, newPolicy) ->
            Authentication.setRequired(accCtx, newPolicy, dbConnectionPool, securityDbDriver);
    }

    @Provides
    @Singleton
    public Authentication initializeAuthentication(
        @SystemContext AccessContext initCtx,
        ErrorReporter errorLogRef,
        ControllerDatabase dbConnPool,
        DbAccessor securityDbDriver
    )
        throws InitializationException
    {
        errorLogRef.logInfo("Initializing authentication subsystem");

        Authentication authentication;
        try
        {
            authentication = new Authentication(initCtx, dbConnPool, securityDbDriver, errorLogRef);
        }
        catch (AccessDeniedException accExc)
        {
            throw new ImplementationError(
                "The initialization security context does not have the necessary " +
                    "privileges to create the authentication subsystem",
                accExc
            );
        }
        catch (NoSuchAlgorithmException algoExc)
        {
            throw new InitializationException(
                "Initialization of the authentication subsystem failed because the " +
                    "required hashing algorithm is not supported on this platform",
                algoExc
            );
        }
        return authentication;
    }

    @Provides
    @Singleton
    public Authorization initializeAuthorization(
        @SystemContext AccessContext initCtx,
        ErrorReporter errorLogRef,
        ControllerDatabase dbConnPool,
        DbAccessor securityDbDriver
    )
    {
        errorLogRef.logInfo("Initializing authorization subsystem");

        Authorization authorization;
        try
        {
            authorization = new Authorization(initCtx, dbConnPool, securityDbDriver);
        }
        catch (AccessDeniedException accExc)
        {
            throw new ImplementationError(
                "The initialization security context does not have the necessary " +
                    "privileges to create the authorization subsystem",
                accExc
            );
        }
        return authorization;
    }
}
