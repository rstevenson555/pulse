/*

 * Created on Oct 29, 2003

 *

 * To change the template for this generated file go to

 * Window>Preferences>Java>Code Generation>Code and Comments

 */

package com.bos.art.logParser.db;

import com.bos.art.logParser.records.AccessRecordsForeignKeys;
import com.bos.art.logParser.records.ExternalEventTiming;
import com.bos.art.logParser.records.ILiveLogParserRecord;
import com.bos.helper.SingletonInstanceHelper;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author I0360D3
 *         <p/>
 *         <p/>
 *         <p/>
 *         Hello World
 *         <p/>
 *         TEST 2
 *         <p/>
 *         To change the template for this generated type comment go to
 *         <p/>
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */

public class ExternalTimingPersistanceStrategy extends BasePersistanceStrategy implements PersistanceStrategy {

    protected static final Logger logger = (Logger) Logger.getLogger(ExternalTimingPersistanceStrategy.class.getName());

    private static final int BATCH_INSERT_SIZE = 2;
    private static SingletonInstanceHelper instance = new SingletonInstanceHelper<ExternalTimingPersistanceStrategy>(ExternalTimingPersistanceStrategy.class) {
        @Override
        public java.lang.Object createInstance() {
            return new ExternalTimingPersistanceStrategy();
        }
    };


    private static ThreadLocal threadLocalCon = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {
            try {
                return ConnectionPoolT.getConnection();
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };

    private static ThreadLocal threadLocalPstmt = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {

            try {
                return ((Connection) threadLocalCon.get()).prepareStatement(
                        "insert into ExternalAccessRecords "
                                + "(Machine_ID,App_ID,Classification_ID,Context_ID,Branch_Tag_ID,Time,LoadTime,Instance_ID) "
                                + "values (?,?,?,?,?,?,?,?)");
            } catch (SQLException se) {
                logger.error("SQL Exception ", se);
            }
            return null;
        }
    };

    private static ThreadLocal<AtomicInteger> threadLocalInserts = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {
            return new AtomicInteger(0);
        }
    };
    private ExternalTimingPersistanceStrategy() {

    }

    public static ExternalTimingPersistanceStrategy getInstance() {
        return (ExternalTimingPersistanceStrategy)instance.getInstance();
    }

    public void resetThreadLocalPstmt() {

        logger.info("Resetting the Pstmt!");
        PreparedStatement ps = (PreparedStatement) threadLocalPstmt.get();
        Connection con = (Connection) threadLocalCon.get();

        try {

            try {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                if (con != null) {
                    con.close();
                    con = null;
                }

            } catch (SQLException se) {
                logger.error("Exception resetting the ThreadLocal PreparedStatement", se);
            }
            con = ConnectionPoolT.getConnection();
            ps =
                    con.prepareStatement(
                            "insert into ExternalAccessRecords "
                                    + "(Machine_ID,App_ID,Classification_ID,Context_ID,Branch_Tag_ID,Time,LoadTime,Instance_ID) "
                                    + "values (?,?,?,?,?,?,?,?)");

            threadLocalCon.set(con);
            threadLocalPstmt.set(ps);

        } catch (Exception e) {
            logger.error("Exception ", e);
        }
    }

    public void blockInsert(PreparedStatement pstmt) {

        try {
            pstmt.addBatch();
            AtomicInteger count =  threadLocalInserts.get();
            int icount = count.incrementAndGet();
            if (icount % BATCH_INSERT_SIZE == 0) {
                pstmt.executeBatch();
            }

        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
        }

    }

    /* (non-Javadoc)

     * @see com.bos.art.logParser.db.PersistanceStrategy#writeToDatabase(com.bos.art.logParser.records.ILiveLogParserRecord)

     */

    public boolean writeToDatabase(ILiveLogParserRecord record) {

        AccessRecordsForeignKeys fk = ((ExternalEventTiming) record).obtainForeignKeys();
        ExternalEventTiming eet = ((ExternalEventTiming) record);

        fk.fkMachineID =
                ForeignKeyStore.getInstance().getForeignKey(
                        fk,
                        record.getServerName(),
                        ForeignKeyStore.FK_MACHINES_MACHINE_ID,
                        this);

        if (record.getInstance() != null) {
            fk.fkInstanceID =
                    ForeignKeyStore.getInstance().getForeignKey(
                            fk,
                            record.getInstance(),
                            ForeignKeyStore.FK_INSTANCES_INSTANCE_ID,
                            this);
        }

        fk.fkAppID =
                ForeignKeyStore.getInstance().getForeignKey(
                        fk,
                        record.getAppName(),
                        ForeignKeyStore.FK_DEPLOYEDAPPS_APP_ID,
                        this);

        fk.fkBranchTagID =
                ForeignKeyStore.getInstance().getForeignKey(fk, eet.getBranchName(), ForeignKeyStore.FK_BRANCH_TAG_ID, this);

        if (eet.getContext() != null) {
            fk.fkContextID =
                    ForeignKeyStore.getInstance().getForeignKey(
                            fk,
                            eet.getContext(),
                            ForeignKeyStore.FK_CONTEXTS_CONTEXT_ID,
                            this);

        } else {
            fk.fkContextID = 0;
        }

        Connection con = null;

        try {
            PreparedStatement pstmt = (PreparedStatement) threadLocalPstmt.get();
            pstmt.setInt(1, fk.fkMachineID);
            pstmt.setInt(2, fk.fkAppID);
            pstmt.setInt(3, eet.getClassification());
            pstmt.setInt(4, fk.fkContextID);
            pstmt.setInt(5, fk.fkBranchTagID);
            pstmt.setTimestamp(6, new java.sql.Timestamp(record.getEventTime().getTimeInMillis()));
            pstmt.setInt(7, eet.getLoadTime());
            pstmt.setInt(8, fk.fkInstanceID);

            blockInsert(pstmt);

        } catch (SQLException se) {
            logger.error("Exception", se);
            resetThreadLocalPstmt();
            return false;

        } finally {
            //  Removed because of Thread Local.
        }

        return true;

    }

}

