package com.hakiba.springdbflutebase.dbflute;

import com.hakiba.springdbflutebase.hook.rule.DBFluteIndexIdHookRule;
import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;
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
public class DBFluteEntityUpdateFamilyMetaInfoExtractor implements DBFluteMetaInfoExtractor {

    @Override
    public List<Entity> extractEntity(BehaviorCommandMeta cmdMeta) {
        return Optional.ofNullable(cmdMeta.getEntity())
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<DBFluteWhereCondition> extractWhereCondition(BehaviorCommandMeta cmdMeta, List<Entity> entityList, DBFluteIndexIdHookRule rule) {
        Entity entity = entityList.get(0); // 単体更新なので1件のみ
        List<ColumnInfo> primaryColumnList = cmdMeta.getDBMeta().getPrimaryInfo().getPrimaryColumnList();
        Optional<ColumnInfo>
                foundPKColumnInfo = primaryColumnList.stream().filter(pkInfo -> Objects.nonNull(pkInfo.read(entity))).findAny();
        ColumnInfo targetColumnInfo = rule.getTargetColumn();
        boolean existsTargetColumn = Objects.nonNull(targetColumnInfo.read(entity));
        boolean existsPKColumn = foundPKColumnInfo.isPresent();
        boolean existsUniqueInfo = !cmdMeta.getDBMeta().getUniqueInfoList().isEmpty();

        // PKが存在する場合
        if (existsPKColumn) {
            ColumnInfo pkColumnInfo = foundPKColumnInfo.get();// 事前に値が存在することを確認しているのでNotNull
            DBFluteMetaSearchCondition.FindByQueryEqual condition =
                    new DBFluteMetaSearchCondition.FindByQueryEqual(pkColumnInfo.getColumnDbName(),
                            pkColumnInfo.read(entity));
            return Optional.of(condition);
        }

        // ユニーク制約が存在する場合
        if (existsUniqueInfo) {
            List<UniqueInfo> uniqueInfoList = cmdMeta.getDBMeta().getUniqueInfoList();

            return extractUniqueInfo(uniqueInfoList, entity)
                    .map(uniqueInfo -> extractUniqueInfoValue(entity, uniqueInfo))
                    .map(DBFluteMetaSearchCondition.FindByQueryEqual::new);
        }

        // 対象カラムが存在する場合
        if (existsTargetColumn) {
            DBFluteMetaSearchCondition.FindByQueryEqual condition =
                    new DBFluteMetaSearchCondition.FindByQueryEqual(targetColumnInfo.getColumnDbName(),
                            targetColumnInfo.read(entity));
            return Optional.of(condition);
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