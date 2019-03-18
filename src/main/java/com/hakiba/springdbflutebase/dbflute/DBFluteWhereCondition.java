package com.hakiba.springdbflutebase.dbflute;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;

/**
 * @author hakiba
 */
public interface DBFluteWhereCondition {
    default void accept(ConditionBean cb) {
        acceptConditionQuery(cb.localCQ());
    }
    default void acceptConditionQuery(ConditionQuery cq) {
        // doNothing
    }
}