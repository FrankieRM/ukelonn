package no.priv.bang.ukelonn.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import no.priv.bang.ukelonn.UkelonnDatabase;
import no.priv.bang.ukelonn.UkelonnService;

public class CommonDatabaseMethods {

    public static UkelonnDatabase connectionCheck(Class<?> clazz) {
        String className = clazz.getSimpleName();
        UkelonnService ukelonnService = UkelonnServiceProvider.getInstance();
        if (ukelonnService == null) {
            throw new RuntimeException(className + " bean unable to find OSGi service Ukelonnservice, giving up");
        }

        UkelonnDatabase database = ukelonnService.getDatabase();
        if (database == null) {
            throw new RuntimeException(className + " bean unable to find OSGi service UkelonnDatabase, giving up");
        }

        return database;
    }

    public static Map<Integer, TransactionType> getTransactionTypesFromUkelonnDatabase(Class<?> clazz) {
        Map<Integer, TransactionType> transactiontypes = new Hashtable<Integer, TransactionType>();
        UkelonnDatabase database = connectionCheck(clazz);
        ResultSet resultSet = database.query("select * from transaction_types");
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    TransactionType transactiontype = mapTransactionType(resultSet);
                    transactiontypes.put(transactiontype.getId(), transactiontype);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return transactiontypes;
    }

    private static TransactionType mapTransactionType(ResultSet resultset) throws SQLException {
        TransactionType transactionType =
            new TransactionType(
                                resultset.getInt("transaction_type_id"),
                                resultset.getString("transaction_type_name"),
                                resultset.getDouble("transaction_amount"),
                                resultset.getBoolean("transaction_is_work"),
                                resultset.getBoolean("transaction_is_wage_payment")
                                );
        return transactionType;
    }

    public static List<TransactionType> getJobTypesFromTransactionTypes(Collection<TransactionType> transactionTypes) {
        ArrayList<TransactionType> jobTypes = new ArrayList<TransactionType>();
        for (TransactionType transactionType : transactionTypes) {
            if (transactionType.isTransactionIsWork()) {
                jobTypes.add(transactionType);
            }
        }

        return jobTypes;
    }

    public static List<Transaction> getTransactionsFromUkelonnDatabase(Class<?> clazz, Map<Integer, TransactionType> transactionTypes, int accountid) {
        List<Transaction> transactions = new ArrayList<Transaction>();
        UkelonnDatabase database = connectionCheck(clazz);
        String sql = String.format(
                                   getResourceAsString("/sql/query/transactions_last10.sql"),
                                   accountid,
                                   accountid
                                   );
        ResultSet resultSet = database.query(sql.toString());
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(transactionTypes, resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return transactions;
    }

    private static Transaction mapTransaction(Map<Integer, TransactionType> transactionTypes, ResultSet resultset) throws SQLException {
        Transaction transaction =
            new Transaction(
                            resultset.getInt("transaction_id"),
                            transactionTypes.get(resultset.getInt("transaction_type_id")),
                            resultset.getDate("transaction_time"),
                            resultset.getDouble("transaction_amount")
                            );
        return transaction;
    }

    public static void updateBalanseFromDatabase(Class<?> clazz, Account account) {
        UkelonnDatabase connection = connectionCheck(clazz);
        StringBuffer sql = new StringBuffer("select * from accounts_view where account_id=");
        sql.append(account.getAccountId());
        ResultSet results = connection.query(sql.toString());
        if (results != null) {
            try {
                while (results.next()) {
                    double balance = results.getDouble("balance");
                    account.setBalance(balance);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addNewPaymentToAccount(Class<?> clazz, Account account, TransactionType paymentType, double payment) {
        int accountId = account.getAccountId();
        int transactionTypeId = paymentType.getId();
        double amount = 0 - payment;
        StringBuffer sql = new StringBuffer("insert into transactions (account_id,transaction_type_id,transaction_amount) values (");
        sql.append(accountId);
        sql.append(",");
        sql.append(transactionTypeId);
        sql.append(",");
        sql.append(amount);
        sql.append(")");

        UkelonnDatabase database = connectionCheck(clazz);
        database.update(sql.toString());
    }

    public static Map<Integer, TransactionType> refreshAccount(Class<?> clazz, Account account) {
        updateBalanseFromDatabase(clazz, account);
        Map<Integer, TransactionType> transactionTypes = getTransactionTypesFromUkelonnDatabase(clazz);
        account.setTransactions(getTransactionsFromUkelonnDatabase(clazz, transactionTypes, account.getAccountId()));
        return transactionTypes;
    }

    public static Account getAccountInfoFromDatabase(Class<?> clazz, String username) {
        UkelonnDatabase database = connectionCheck(clazz);
        StringBuffer sql = new StringBuffer("select * from accounts_view where username='");
        sql.append(username);
        sql.append("'");
        ResultSet resultset = database.query(sql.toString());
        if (resultset != null) {
            try {
                if (resultset.next()) {
                    Account newaccount = MapAccount(resultset);
                    return newaccount;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static List<Account> getAccounts(Class<?> clazz) {
        ArrayList<Account> accounts = new ArrayList<Account>();
        UkelonnDatabase connection = connectionCheck(clazz);
        ResultSet results = connection.query("select * from accounts_view");
        if (results != null) {
            try {
                while(results.next()) {
                    Account newaccount = MapAccount(results);
                    accounts.add(newaccount);
                }
            } catch (SQLException e) {
                // Skip and continue
                e.printStackTrace();
            }
        }

        return accounts;
    }

    public static List<Transaction> getPaymentsFromAccount(Account account) {
        ArrayList<Transaction> payments = new ArrayList<Transaction>();
        if (account != null) {
            for (Transaction transaction : account.getTransactions()) {
                if (transaction.getTransactionType().isTransactionIsWagePayment()) {
                    // Make the displayed amounts be positive
                    double amount = Math.abs(transaction.getTransactionAmount());
                    transaction.setTransactionAmount(amount);
                    payments.add(transaction);
                }
            }
        }

        return payments;
    }

    public static List<Transaction> getJobsFromAccount(Account account) {
        ArrayList<Transaction> jobs = new ArrayList<Transaction>();
        if (account != null) {
            for (Transaction transaction : account.getTransactions()) {
                if (transaction.getTransactionType().isTransactionIsWork()) {
                    jobs.add(transaction);
                }
            }
        }

        return jobs;
    }

    public static Account MapAccount(ResultSet results) throws SQLException {
        return new Account(
                           results.getInt("account_id"),
                           results.getInt("user_id"),
                           results.getString("username"),
                           results.getString("first_name"),
                           results.getString("last_name"),
                           results.getDouble("balance")
                           );
    }

    public static Map<Integer, TransactionType> registerNewJobInDatabase(Class<?> clazz, Account account, int newJobTypeId, double newJobWages) {
        StringBuffer sql = new StringBuffer("insert into transactions (account_id,transaction_type_id,transaction_amount) values (");
        sql.append(account.getAccountId());
        sql.append(",");
        sql.append(newJobTypeId);
        sql.append(",");
        sql.append(newJobWages);
        sql.append(")");

        UkelonnDatabase database = connectionCheck(clazz);
        database.update(sql.toString());

        // Update the list of jobs and the updated balance from the DB
        Map<Integer, TransactionType> transactionTypes = refreshAccount(clazz, account);
        return transactionTypes;
    }

    public static void addJobTypeToDatabase(Class<?> clazz, String newPaymentTypeName, double newPaymentTypeAmount) {
        String sql = String.format(
                                   Locale.US, // Format the double correctly for SQL
                                   getResourceAsString("/sql/query/insert_new_job_type.sql"),
                                   newPaymentTypeName,
                                   newPaymentTypeAmount
                                   );

        UkelonnDatabase database = connectionCheck(clazz);
        database.update(sql);
    }

    public static void updateTransactionInDatabase(Class<?> clazz, TransactionType modifiedJobType) {
        String sql = String.format(
                                   Locale.US, // Format the double correctly for SQL
                                   getResourceAsString("/sql/query/update_transaction_type.sql"),
                                   modifiedJobType.getTransactionTypeName(),
                                   modifiedJobType.getTransactionAmount(),
                                   modifiedJobType.isTransactionIsWork() ? "true" : "false",
                                   modifiedJobType.isTransactionIsWagePayment() ? "true" : "false",
                                   modifiedJobType.getId()
                                   );

        UkelonnDatabase database = connectionCheck(clazz);
        database.update(sql);
    }

    private static String getResourceAsString(String resourceName) {
        ByteArrayOutputStream resource = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        InputStream resourceStream = CommonDatabaseMethods.class.getResourceAsStream(resourceName);
        try {
            while ((length = resourceStream.read(buffer)) != -1) {
                resource.write(buffer, 0, length);
            }

            return resource.toString("UTF-8");
        } catch (Exception e) {
        }

        return null;
    }

}
