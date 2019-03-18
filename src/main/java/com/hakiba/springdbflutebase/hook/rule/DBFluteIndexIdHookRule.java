package com.hakiba.springdbflutebase.hook.rule;

import com.hakiba.springdbflutebase.IndexIdInfo;
import com.hakiba.springdbflutebase.dbflute.DBFluteWhereCondition;
import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.dbmeta.info.ColumnInfo;

import java.util.Optional;
import java.util.Set;

/**
 * @author hakiba
 */
public interface DBFluteIndexIdHookRule<ENTITY extends Entity, ID> {

    boolean isTargetCommand(BehaviorCommandMeta cmdMeta);

    boolean needsSearch(ENTITY entity);

    boolean matchesCondition(ENTITY entity);

    ColumnInfo getTargetColumn();

    IndexIdInfo<ID> convertIndexIdInfo(ENTITY entity);

    Set<IndexIdInfo<ID>> extractIndexIdInfoOnlyMatchesCondition(BehaviorCommandMeta cmdMeta);

    Optional<DBFluteWhereCondition> extractSearchInfoOnlyNeedsSearch(BehaviorCommandMeta cmdMeta);

    Set<IndexIdInfo<ID>> findByWhereCondition(String tableName, DBFluteWhereCondition condition);
}
