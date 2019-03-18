package com.hakiba.springdbflutebase.dbflute;

import com.hakiba.springdbflutebase.hook.rule.DBFluteIndexIdHookRule;
import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.PrimaryInfo;
import org.dbflute.dbmeta.info.UniqueInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hakiba.springdbflutebase.dbflute.DBFluteMetaSearchCondition.NOT_IGNORE_COLUMN_TYPE;
import static com.hakiba.springdbflutebase.dbflute.DBFluteMetaSearchCondition.REGISTER_DATETIME_COLUMN_DB_NAME;

/**
 * @author hakiba
 */
public class DBFluteBatchUpdateFamilyMetaInfoExtractor implements DBFluteMetaInfoExtractor {

    @Override
    public List<Entity> extractEntity(BehaviorCommandMeta cmdMeta) {
        return cmdMeta.getEntityList();
    }

    @Override
    public Optional<DBFluteWhereCondition> extractWhereCondition(BehaviorCommandMeta cmdMeta, List<Entity> entityList, DBFluteIndexIdHookRule rule) {
        Set<DBFluteWhereCondition> whereConditions = entityList.stream()
                .map(entity -> extractWhereConditionByEntity(cmdMeta.getDBMeta(), rule, entity))
                .collect(Collectors.toSet());
        return whereConditions.isEmpty()
                ? Optional.of(new DBFluteMetaSearchCondition.FindByOrScopeWhereCondition(whereConditions))
                : Optional.empty();
    }

    protected DBFluteWhereCondition extractWhereConditionByEntity(DBMeta dbMeta, DBFluteIndexIdHookRule rule, Entity entity) {
        ColumnInfo targetColumn = rule.getTargetColumn();
        PrimaryInfo primaryInfo = dbMeta.getPrimaryInfo();
        List<UniqueInfo> uniqueInfoList = dbMeta.getUniqueInfoList();

        Optional<ColumnInfo> pkOpt = extractNotNullPK(primaryInfo, entity);
        // PKを持っている場合
        if (pkOpt.isPresent()) {
            ColumnInfo pkColumn = pkOpt.get();
            return new DBFluteMetaSearchCondition.FindByPK(pkColumn.getColumnDbName(), pkColumn.read(entity));
        }

        // Unique制約を含む場合
        Map<String, Object> uqColumnAndValueMap = extractNotNullUQ(entity, uniqueInfoList);
        if (!uqColumnAndValueMap.isEmpty()) {
            return new DBFluteMetaSearchCondition.FindByUQ(uqColumnAndValueMap);
        }

        // IDを含む場合
        Object targetColumnValue = targetColumn.read(entity);
        if (targetColumnValue != null) {
            return new DBFluteMetaSearchCondition.FindByTargetColumn(targetColumn.getColumnDbName(), targetColumn.read(entity));
        }

        // NotNullカラムとInsert日時を利用する場合
        List<ColumnInfo> columnInfoList = dbMeta.getColumnInfoList();
        Map<String, Object> notNullColumnAndValueMap = extractNotNulColumn(entity, columnInfoList);
        Optional<ColumnInfo> insDatetimeColumnOpt = extractInsDatetimeColumn(dbMeta, entity);
        if (!notNullColumnAndValueMap.isEmpty() && insDatetimeColumnOpt.isPresent()) {
            ColumnInfo insDatetimeColumn = insDatetimeColumnOpt.get();
            return new DBFluteMetaSearchCondition.FindByNotNullColumnAndInsDatetime(notNullColumnAndValueMap, insDatetimeColumn.getColumnDbName(), insDatetimeColumn.read(entity));
        }

        return null; // いずれの条件も満たさない場合はnull
    }

    private Optional<ColumnInfo> extractInsDatetimeColumn(DBMeta dbMeta, Entity entity) {
        return Optional.of(dbMeta)
                .map(meta -> meta.findColumnInfo(REGISTER_DATETIME_COLUMN_DB_NAME))
                .filter(columnInfo -> columnInfo.read(entity));
    }

    private Map<String, Object> extractNotNulColumn(Entity entity, List<ColumnInfo> columnInfoList) {
        return columnInfoList.stream().filter(NOT_IGNORE_COLUMN_TYPE)
                .filter(columnInfo -> Objects.nonNull(columnInfo.read(entity)))
                .collect(Collectors.toMap(col -> col.getColumnDbName(), col -> col.read(entity), (v1, v2) -> v1));
    }

    private Map<String, Object> extractNotNullUQ(Entity entity, List<UniqueInfo> uniqueInfoList) {
        return uniqueInfoList.stream()
                .filter(uniqueInfo -> uniqueInfo.getUniqueColumnList().stream().allMatch(columnInfo -> Objects.nonNull(columnInfo.read(entity))))
                .findFirst()
                .map(uniqueInfo -> uniqueInfo.getUniqueColumnList().stream().collect(Collectors.toMap(col -> col.getColumnDbName(), col -> col.read(entity), (v1, v2) -> v1)))
                .orElse(Collections.emptyMap());
    }

    private boolean hasNotNullPKColumn(PrimaryInfo primaryInfo, Entity entity) {
        return extractNotNullPK(primaryInfo, entity).isPresent();
    }

    private Optional<ColumnInfo> extractNotNullPK(PrimaryInfo primaryInfo, Entity entity) {
        return primaryInfo.getPrimaryColumnList().stream().filter(columnInfo -> Objects.nonNull(columnInfo.read(entity))).findAny();
    }
}
