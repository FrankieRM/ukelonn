/*
 * Copyright 2016-2019 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.ukelonn.db.liquibase.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import liquibase.Liquibase;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.StandardChangeLogHistoryService;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import no.priv.bang.ukelonn.db.liquibase.UkelonnLiquibase;

@Component(immediate=true, property = "name=ukelonndb")
public class TestLiquibaseRunner implements PreHook {
    private LogService logService;
    private boolean initialChangelog = false;

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Activate
    public void activate() {
        // Called when component is activated
    }

    @Override
    public void prepare(DataSource datasource) throws SQLException {
        UkelonnLiquibase liquibase = new UkelonnLiquibase();
        try(Connection connect = datasource.getConnection()) {
            try {
                liquibase.createInitialSchema(connect);
                insertMockData(datasource);
                liquibase.updateSchema(connect);
            } finally {
                // Liquibase sets Connection.autoCommit to false, set it back to true
                connect.setAutoCommit(true);
            }
        } catch (Exception e) {
            logService.log(LogService.LOG_ERROR, "Failed to create derby test database schema", e);
        }
    }

    public boolean insertMockData(DataSource datasource) {
        try(Connection connect = datasource.getConnection()) {
            DatabaseConnection databaseConnection = new JdbcConnection(connect);
            ClassLoaderResourceAccessor classLoaderResourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader());
            if (hasTable(connect, "user_roles")) {
                initialChangelog = false;
                Liquibase liquibase = new Liquibase("sql/data/db-changelog.xml", classLoaderResourceAccessor, databaseConnection);
                liquibase.update("");
            } else {
                // Schema before authservice schema applied
                initialChangelog = true;
                Liquibase liquibase = new Liquibase("sql/data/db-initial-changelog.xml", classLoaderResourceAccessor, databaseConnection);
                liquibase.update("");
            }
            return true;
        } catch (Exception e) {
            logService.log(LogService.LOG_ERROR, "Failed to fill derby test database with data.", e);
            return false;
        }
    }

    private boolean hasTable(Connection connection, String tablename) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet tables = metadata.getTables(null, null, "%", null);
        while(tables.next()) {
            if (tablename.equals(tables.getString(3))) {
                return true;
            }
        }

        return false;
    }

    public boolean rollbackMockData(DataSource datasource) {
        try(Connection connect = datasource.getConnection()) {
            DatabaseConnection databaseConnection = new JdbcConnection(connect);
            ClassLoaderResourceAccessor classLoaderResourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader());
            if (initialChangelog) {
                try(PreparedStatement statement = connect.prepareStatement("delete from user_roles")) {
                    statement.executeUpdate();
                }
                try(PreparedStatement statement = connect.prepareStatement("delete from users")) {
                    statement.executeUpdate();
                }
                Liquibase liquibase = new Liquibase("sql/data/db-initial-changelog.xml", classLoaderResourceAccessor, databaseConnection);
                liquibase.rollback(3, "");
            } else {
                Liquibase liquibase = new Liquibase("sql/data/db-changelog.xml", classLoaderResourceAccessor, databaseConnection);
                liquibase.rollback(5, ""); // Note this number must be increased if additional change lists are added
                // Note also that all of those change lists will need to implement rollback (at least those changing the schema)
            }
            return true;
        } catch (Exception e) {
            logService.log(LogService.LOG_ERROR, "Failed to roll back mock data from derby test database.", e);
            return false;
        }
    }

    /**
     * Package private method to let the unit test determine if the Liquibase changesets have
     * been run.
     *
     * @return A list of all changesets run by liqubase in the derby database
     * @throws DatabaseException
     * @throws SQLException
     */
    List<RanChangeSet> getChangeLogHistory(DataSource datasource) throws DatabaseException, SQLException {
        try(Connection connect = datasource.getConnection()) {
            DatabaseConnection databaseConnection = new JdbcConnection(connect);
            try {
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
                StandardChangeLogHistoryService logHistoryService = ((StandardChangeLogHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database));
                return logHistoryService.getRanChangeSets();
            } catch (Exception e) {
                logService.log(LogService.LOG_ERROR, "Failed to create derby test database schema", e);
            } finally {
                databaseConnection.close();
            }
        }

        return Collections.emptyList();
    }

}
