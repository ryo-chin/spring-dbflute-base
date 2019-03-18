package com.hakiba.springdbflutebase.dbflute;

import com.hakiba.springdbflutebase.hook.rule.DBFluteIndexIdHookRule;
import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.UniqueInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author hakiba
 */
public class DBFluteQueryUpdateFamilyMetaInfoExtractor implements DBFluteMetaInfoExtractor {
    @Override
    public List<Entity> extractEntity(BehaviorCommandMeta cmdMeta) {
        return Optional.ofNullable(cmdMeta.getEntity())
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<DBFluteWhereCondition> extractWhereCondition(BehaviorCommandMeta cmdMeta, List<Entity> entityList, DBFluteIndexIdHookRule rule) {
        Optional<Entity> entityOpt = Optional.ofNullable(entityList.get(0)); // クエリ更新なので、1件だがNullable
        List<ColumnInfo> primaryColumnList = cmdMeta.getDBMeta().getPrimaryInfo().getPrimaryColumnList();
        Optional<ColumnInfo> foundPKColumnInfo = entityOpt.flatMap(entity ->
                primaryColumnList.stream().filter(pkInfo -> Objects.nonNull(pkInfo.read(entity))).findAny()
        );

        ColumnInfo targetColumnInfo = rule.getTargetColumn();
        boolean existsUpdateEntity = entityOpt.isPresent();
        boolean existsPKColumn = foundPKColumnInfo.isPresent();
        boolean existsTargetColumnInfo = entityOpt.map(e -> Objects.nonNull(targetColumnInfo.read(e))).orElse(false);
        boolean existsUniqueInfo = !cmdMeta.getDBMeta().getUniqueInfoList().isEmpty();
        boolean existsCB = cmdMeta.getConditionBean() != null;

        // PKを含む場合
        if (existsUpdateEntity && existsPKColumn) {
            // TODO: そもそもこの場合は考慮しなくてよさそう？
            Entity entity = entityOpt.get(); // NotNull
            ColumnInfo pkColumnInfo = foundPKColumnInfo.get();// 事前に値が存在することを確認しているのでNotNull
            DBFluteMetaSearchCondition.FindByQueryEqual condition =
                    new DBFluteMetaSearchCondition.FindByQueryEqual(pkColumnInfo.getColumnDbName(),
                            pkColumnInfo.read(entity));
            return Optional.of(condition);
        }
        // Unique制約を含む場合
        else if (existsUpdateEntity && existsUniqueInfo) {
            Entity entity = entityOpt.get(); // NotNull
            List<UniqueInfo> uniqueInfoList = cmdMeta.getDBMeta().getUniqueInfoList();
            return extractUniqueInfo(uniqueInfoList, entity)
                    .map(uniqueInfo -> extractUniqueInfoValue(entity, uniqueInfo))
                    .map(DBFluteMetaSearchCondition.FindByQueryEqual::new);
        }
        // IDを含む場合
        else if (existsUpdateEntity && existsTargetColumnInfo) {
            Entity entity = entityOpt.get(); // NotNull
            DBFluteMetaSearchCondition.FindByQueryEqual condition =
                    new DBFluteMetaSearchCondition.FindByQueryEqual(targetColumnInfo.getColumnDbName(),
                            targetColumnInfo.read(entity));
            return Optional.of(condition);
        }
        // CBが存在する場合
        else if (existsCB) {
            ConditionQuery cq = cmdMeta.getConditionBean().localCQ();
            Map<String, Map<String, Object>> columnAndQueryMap = cmdMeta.getDBMeta().getColumnInfoList().stream()
                    .collect(Collectors.toMap(
                            col -> col.getColumnDbName(),
                            col -> cq.invokeValue(col.getColumnDbName()).getFixedQuery(),
                            (before, after) -> after));
            return Optional.of(new DBFluteMetaSearchCondition.FindByCB(columnAndQueryMap));
        }

        return Optional.empty();
    }

    // TODO: 2019-04-05 重複してるのでまとめる
    private Optional<UniqueInfo> extractUniqueInfo(List<UniqueInfo> uniqueInfoList, Entity entity) {
        return uniqueInfoList.stream().filter(info -> info.getUniqueColumnList().stream().allMatch(columnInfo -> {
            return Objects.nonNull(columnInfo.read(entity));
        })).findFirst();
    }

    // TODO: 2019-04-05 重複してるのでまとめる
    private Map<String, Object> extractUniqueInfoValue(Entity entity, UniqueInfo uniqueInfo) {
        return uniqueInfo.getUniqueColumnList().stream()
                .collect(Collectors.toMap(col -> col.getColumnDbName(), col -> col.read(entity)));
    }
}