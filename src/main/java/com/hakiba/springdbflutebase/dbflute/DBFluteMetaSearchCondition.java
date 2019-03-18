package com.hakiba.springdbflutebase.dbflute;

import lombok.AllArgsConstructor;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.docksidestage.dbflute.bsentity.dbmeta.MemberDbm;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author hakiba
 */
public class DBFluteMetaSearchCondition {
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Set<Class> IGNORE_COLUMN_TYPE = new HashSet<>();
    {
        IGNORE_COLUMN_TYPE.add(Timestamp.class);
    }
    // TODO 共通カラムの設定などから取れるようにする
    public static final String REGISTER_DATETIME_COLUMN_DB_NAME = MemberDbm.getInstance().columnRegisterDatetime().getColumnDbName();
    // -----------------------------------------------------
    //                                              Function
    //                                              --------
    public static Predicate<ColumnInfo> NOT_IGNORE_COLUMN_TYPE = column -> IGNORE_COLUMN_TYPE.contains(column.getObjectNativeType());

    @AllArgsConstructor
    public static class FindByQueryEqual implements DBFluteWhereCondition {
        Map<String, Object> columnAndValueMap;

        public FindByQueryEqual(String columnDbName, Object value) {
            columnAndValueMap = Collections.singletonMap(columnDbName, value);
        }

        @Override
        public void accept(ConditionBean cb) {
            acceptConditionQuery(cb.localCQ());
        }

        @Override
        public void acceptConditionQuery(ConditionQuery cq) {
            columnAndValueMap.forEach((colName, value) -> {
                cq.invokeQueryEqual(colName, value);
            });
        }
    }

    @AllArgsConstructor
    public static class FindByPK implements DBFluteWhereCondition {
        String columnDbName;
        Object columnValue;

        @Override
        public void acceptConditionQuery(ConditionQuery cq) {
            cq.invokeQueryEqual(columnDbName, columnValue);
        }
    }

    @AllArgsConstructor
    public static class FindByUQ implements DBFluteWhereCondition {
        Map<String, Object> columnAndValueMap;

        @Override
        public void acceptConditionQuery(ConditionQuery cq) {
            columnAndValueMap.forEach((colName, value) -> {
                cq.invokeQueryEqual(colName, value);
            });
        }
    }

    @AllArgsConstructor
    public static class FindByTargetColumn implements DBFluteWhereCondition {
        String columnDbName;
        Object columnValue;

        @Override
        public void acceptConditionQuery(ConditionQuery cq) {
            cq.invokeQueryEqual(columnDbName, columnValue);
        }
        // TODO: 2019-04-05 1件しか取得しないことをアサートできるようにする
    }

    @AllArgsConstructor
    public static class FindByNotNullColumnAndInsDatetime implements DBFluteWhereCondition {
        Map<String, Object> columnAndValueMap;
        String insDatetimeColumnDbName;
        Object insDatetimeValue;

        @Override
        public void acceptConditionQuery(ConditionQuery cq) {
            columnAndValueMap.forEach((colName, value) -> {
                cq.invokeQueryEqual(colName, value);
            });
        }
    }

    @AllArgsConstructor
    public static class FindByCB implements DBFluteWhereCondition {
        Map<String, Map<String, Object>> columnAndQueryMap;

        @Override
        public void acceptConditionQuery(ConditionQuery cq) {
            columnAndQueryMap.forEach((colName, queryMap) -> {
                queryMap.forEach((queryKey, queryValue) -> {
                    cq.invokeQuery(colName, queryKey, queryValue);
                });
            });
        }
    }

    @AllArgsConstructor
    public static class FindByOrScopeWhereCondition implements DBFluteWhereCondition {
        Set<DBFluteWhereCondition> whereConditions;

        @Override
        public void accept(ConditionBean cb) {
            cb.invokeOrScopeQuery(orCB -> {
                whereConditions.forEach(condition -> {
                    condition.acceptConditionQuery(orCB.localCQ());
                });
            });
        }
    }
}
