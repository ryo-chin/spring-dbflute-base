package com.hakiba.springdbflutebase;

import com.hakiba.springdbflutebase.dbflute.DBFluteWhereCondition;
import com.hakiba.springdbflutebase.hook.rule.DBFluteIndexIdHookRule;
import com.hakiba.springdbflutebase.hook.rule.FirstIndexIdRuleHolder;
import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hakiba
 */
@Component
public class FirstIndexIdExecutor {
    protected static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Autowired
    private FirstIndexIdRuleHolder RuleHolder;
    @Autowired
    private FirstIndexQueueService queueService;

    public void execute(BehaviorCommandMeta cmdMeta) {
        Class<? extends Entity> entityType = cmdMeta.getDBMeta().getEntityType();
        RuleHolder.getRule(entityType)
                .filter(rule -> rule.isTargetCommand(cmdMeta))
                .ifPresent(rule -> doExecute(cmdMeta, rule));
    }

    private void doExecute(BehaviorCommandMeta cmdMeta, DBFluteIndexIdHookRule<Entity, Long> rule) {
        Set<IndexIdInfo<Long>> matchedIndexInfos = rule.extractIndexIdInfoOnlyMatchesCondition(cmdMeta);
        Optional<DBFluteWhereCondition> foundNeedsSearchWhereCondition = rule.extractSearchInfoOnlyNeedsSearch(cmdMeta);

        registerAfterTransactionCompletionCallback(cmdMeta.getTableDbName(), rule, matchedIndexInfos, foundNeedsSearchWhereCondition);
    }

    private void registerAfterTransactionCompletionCallback(String tableDbName, DBFluteIndexIdHookRule<Entity, Long> rule, Set<IndexIdInfo<Long>> indexIdInfoByExtract, Optional<DBFluteWhereCondition> foundWhereCondition) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                super.afterCompletion(status);

                // commitされていない場合は何もしない
                if (status != 0) {
                    return;
                }

                CompletableFuture.supplyAsync(() -> {
                    // 再検索
                    Set<IndexIdInfo<Long>> indexIdInfoBySearch = foundWhereCondition
                            .map(condition -> rule.findByWhereCondition(tableDbName, condition))
                            .orElse(Collections.emptySet());
                    // エンキューできる形式に変換
                    List<String> enqueueInfo = Stream.concat(indexIdInfoByExtract.stream(), indexIdInfoBySearch.stream())
                            .map(longIndexIdInfo -> longIndexIdInfo.toString()) // TODO: jsonに変換する
                            .collect(Collectors.toList());
                    // エンキュー
                    queueService.enqueue(enqueueInfo);

                    return null; // TODO: supplyAsyncに調べた上でハンドリング
                }, executorService);
            }
        });
    }
}
