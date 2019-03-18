package com.hakiba.springdbflutebase.hook.rule;

import com.hakiba.springdbflutebase.IndexIdInfo;
import com.hakiba.springdbflutebase.dbflute.DBFluteMatchConditionBuilder;
import com.hakiba.springdbflutebase.dbflute.DBFluteMetaInfoExtractor;
import com.hakiba.springdbflutebase.dbflute.DBFluteMetaInfoExtractorDispatcher;
import com.hakiba.springdbflutebase.dbflute.DBFluteWhereCondition;
import org.dbflute.Entity;
import org.dbflute.bhv.BehaviorReadable;
import org.dbflute.bhv.BehaviorSelector;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hakiba
 */
public abstract class AbstractDBFluteIndexIdHookRule<ENTITY extends Entity, ID> implements DBFluteIndexIdHookRule<ENTITY, ID> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private ColumnInfo targetColumnInfo;
    private DBFluteMatchConditionBuilder.DBFluteMatchCondition<ENTITY> matchCondition;
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @Autowired
    private BehaviorSelector behaviorSelector;

    // ===================================================================================
    //                                                                      Post Construct
    //                                                                      ==============
    @PostConstruct
    private void initialize() {
        // TODO: いずれかのGetterがnullだったときはExceptionを投げるようにする
        targetColumnInfo = prepareTargetColumnInfo();
        matchCondition = prepareMatchCondition(new DBFluteMatchConditionBuilder<ENTITY>());
    }

    protected abstract ColumnInfo prepareTargetColumnInfo();
    protected DBFluteMatchConditionBuilder.DBFluteMatchCondition<ENTITY> prepareMatchCondition(DBFluteMatchConditionBuilder<ENTITY> builder) {
        return builder.buildAllMatchCondition();
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    public boolean isTargetCommand(BehaviorCommandMeta cmdMeta) {
        return cmdMeta.isInsert() || cmdMeta.isUpdate();
    }

    @Override
    public boolean needsSearch(Entity entity) {
        return existsTargetColumn(entity) && matchCondition.hasAllMatchConditionColumn((ENTITY) entity);
    }

    @Override
    public boolean matchesCondition(Entity entity) {
        return matchCondition.matchesCondition((ENTITY) entity);
    }

    @Override
    public ColumnInfo getTargetColumn() {
        return targetColumnInfo;
    }

    @Override
    public Set<IndexIdInfo<ID>> extractIndexIdInfoOnlyMatchesCondition(BehaviorCommandMeta cmdMeta) {
        return Optional.ofNullable(DBFluteMetaInfoExtractorDispatcher.dispatch(cmdMeta))
                .map(extractor -> extractor.extractEntity(cmdMeta))
                .map(entities -> entities.stream().filter(entity -> !needsSearch(entity))) // 再検索が不要なEntityに絞る
                .map(entityStream -> entityStream.filter(this::matchesCondition)) // 条件に合致するものだけに絞り込む
                .map(entityStream -> entityStream.map(this::convertIndexIdInfo)) // インデックス用情報に変換
                .map(stream -> stream.collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Override
    public Optional<DBFluteWhereCondition> extractSearchInfoOnlyNeedsSearch(BehaviorCommandMeta cmdMeta) {
        // インデックスの対象外（=Extractorが見つからない）場合、Emptyでリターン
        Optional<DBFluteMetaInfoExtractor> foundExtractor =
                Optional.ofNullable(DBFluteMetaInfoExtractorDispatcher.dispatch(cmdMeta));
        if (!foundExtractor.isPresent()) {
            return Optional.empty();
        }

        DBFluteMetaInfoExtractor extractor = foundExtractor.get();
        List<Entity> needsSearchEntityList = extractor.extractEntity(cmdMeta).stream()
                .filter(entity -> needsSearch(entity)) // 再検索が必要なEntityに絞る
                .collect(Collectors.toList());
        return extractor.extractWhereCondition(cmdMeta, needsSearchEntityList, this);
    }

    @Override
    public IndexIdInfo<ID> convertIndexIdInfo(Entity entity) {
        String tableDbName = entity.asDBMeta().getTableDbName();
        ID targetId = targetColumnInfo.read(entity);
        return new IndexIdInfo<ID>(tableDbName, targetId);
    }

    @Override
    public Set<IndexIdInfo<ID>> findByWhereCondition(String tableName, DBFluteWhereCondition condition) {
        BehaviorReadable behavior = behaviorSelector.byName(tableName);
        ConditionBean cb = behavior.newConditionBean();
        cb.invokeSpecifyColumn(targetColumnInfo.getColumnDbName());
        matchCondition.getMatchConditionColumns().forEach(columnInfo -> cb.invokeSpecifyColumn(columnInfo.getColumnDbName()));
        return behavior.readList(cb).stream()
                .filter(this::matchesCondition) // 条件に合致するものだけに絞り込む
                .map(this::convertIndexIdInfo) // インデックス用情報に変換
                .collect(Collectors.toSet());
    }

    // ===================================================================================
    //                                                                              Helper
    //                                                                              ======
    private boolean existsTargetColumn(Entity entity) {
        return Objects.nonNull(targetColumnInfo.read(entity));
    }
}