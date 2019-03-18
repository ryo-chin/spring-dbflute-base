package com.hakiba.springdbflutebase.dbflute;

import com.hakiba.springdbflutebase.hook.rule.DBFluteIndexIdHookRule;
import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;

import java.util.List;
import java.util.Optional;

/**
 * @author hakiba
 */
public interface DBFluteMetaInfoExtractor {
    List<Entity> extractEntity(BehaviorCommandMeta cmdMeta);
    Optional<DBFluteWhereCondition> extractWhereCondition(BehaviorCommandMeta cmdMeta, List<Entity> entityList, DBFluteIndexIdHookRule rule);
}
