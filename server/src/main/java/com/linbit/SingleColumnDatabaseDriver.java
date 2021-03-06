package com.linbit;

import java.sql.SQLException;

public interface SingleColumnDatabaseDriver<PARENT, COL_VALUE>
{
    void update(PARENT parent, COL_VALUE element) throws SQLException;
}
