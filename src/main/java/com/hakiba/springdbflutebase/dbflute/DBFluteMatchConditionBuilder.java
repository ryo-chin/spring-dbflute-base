package com.hakiba.springdbflutebase.dbflute;

import org.dbflute.Entity;
import org.dbflute.dbmeta.info.ColumnInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.hakiba.springdbflutebase.dbflute.DBFluteMatchConditionBuilder.DBFluteMatchConditionEntry.ConditionalOperator;

/**
 * @author hakiba
 */
public class DBFluteMatchConditionBuilder<ENTITY extends Entity> {
    public static class DBFluteMatchConditionEntry<ENTITY> {
        public enum ConditionalOperator{
            AND,
            OR
        }

        private ConditionalOperator conditionalOperator;

        private ColumnInfo column;

        private Predicate<ENTITY> checkFunction;

        public DBFluteMatchConditionEntry(ConditionalOperator conditionalOperator, ColumnInfo column,
                                          Predicate<ENTITY> checkFunction) {
            this.conditionalOperator = conditionalOperator;
            this.column = column;
            this.checkFunction = checkFunction;
        }

        public boolean matchesCondition(ENTITY entity) {
            return checkFunction.test(entity);
        }

        public ConditionalOperator getConditionalOperator() {
            return conditionalOperator;
        }

        public ColumnInfo getColumn() {
            return column;
        }
    }

    public static class DBFluteMatchCondition<ENTITY> {
        private final Set<ColumnInfo> needColumns;
        private final Set<DBFluteMatchConditionEntry<ENTITY>> andConditions;
        private final Set<DBFluteMatchConditionEntry<ENTITY>> orConditions;

        public DBFluteMatchCondition(Set<ColumnInfo> columns, Set<DBFluteMatchConditionEntry<ENTITY>> andConditions,
                                     Set<DBFluteMatchConditionEntry<ENTITY>> orConditions) {
            this.needColumns = Collections.unmodifiableSet(columns);
            this.andConditions = Collections.unmodifiableSet(andConditions);
            this.orConditions = Collections.unmodifiableSet(orConditions);
        }

        public boolean matchesCondition(ENTITY entity) {
            boolean matchesAllAndConditions =
                    !andConditions.isEmpty()
                            && andConditions.stream().allMatch(andCondition -> andCondition.matchesCondition(entity));
            boolean matchesAnyOrConditions =
                    !orConditions.isEmpty()
                            && orConditions.stream().anyMatch(orCondition -> orCondition.matchesCondition(entity));

            return matchesAllAndConditions || matchesAnyOrConditions;
        }

        public boolean hasAllMatchConditionColumn(ENTITY entity) {
            if (needColumns.isEmpty()) {
                return true;
            }
            return needColumns.stream().allMatch(columnInfo -> columnInfo.read((Entity) entity));
        }

        public Set<ColumnInfo> getMatchConditionColumns() {
            return needColumns;
        }
    }

    private Set<ColumnInfo> needColumns = new HashSet<>();
    private Set<DBFluteMatchConditionEntry<ENTITY>> andConditions = new HashSet<>();
    private Set<DBFluteMatchConditionEntry<ENTITY>> orConditions = new HashSet<>();

    public DBFluteMatchConditionBuilder<ENTITY> addAndCondition(ColumnInfo columnInfo, Predicate<ENTITY> checkFunction) {
        needColumns.add(columnInfo);
        DBFluteMatchConditionEntry<ENTITY> condition = new DBFluteMatchConditionEntry<>(ConditionalOperator.AND, columnInfo, checkFunction);
        andConditions.add(condition);
        return this;
    }

    public DBFluteMatchConditionBuilder<ENTITY> addOrCondition(ColumnInfo columnInfo, Predicate<ENTITY> checkFunction) {
        needColumns.add(columnInfo);
        DBFluteMatchConditionEntry<ENTITY> condition = new DBFluteMatchConditionEntry<>(ConditionalOperator.OR, columnInfo, checkFunction);
        orConditions.add(condition);
        return this;
    }

    public DBFluteMatchCondition<ENTITY> build() {
        return new DBFluteMatchCondition<ENTITY>(needColumns, andConditions, orConditions);
    }

    public DBFluteMatchCondition<ENTITY> buildAllMatchCondition() {
        addOrCondition(null, ignore -> true); // 確実にtrueを返す条件をOR条件で追加
        return new DBFluteMatchCondition<ENTITY>(needColumns, andConditions, orConditions);
    }
}