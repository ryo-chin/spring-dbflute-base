package com.hakiba.springdbflutebase.hook.rule;

import org.dbflute.Entity;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.docksidestage.dbflute.bsentity.dbmeta.MemberAddressDbm;
import org.docksidestage.dbflute.bsentity.dbmeta.MemberDbm;
import org.docksidestage.dbflute.exentity.Member;
import org.docksidestage.dbflute.exentity.MemberAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class FirstIndexIdRuleHolder {

    private Map<Class<? extends Entity>, DBFluteIndexIdHookRule> ruleMap = new HashMap<>();

    @Autowired
    private MemberIndexIdHookRule memberRule;
    @Autowired
    private MemberAddressIndexIdHookRule memberAddressRule;

    public <ENTITY extends Entity> Optional<DBFluteIndexIdHookRule<Entity, Long>> getRule(Class<ENTITY> entityClass) {
        // TODO: タイプチェック処理
        return Optional.ofNullable(ruleMap.get(entityClass));
    }

    @PostConstruct
    public void postConstruct() {
        ruleMap.put(Member.class, memberRule);
        ruleMap.put(MemberAddress.class, memberAddressRule);
    }

    @Component
    public static class MemberIndexIdHookRule extends AbstractDBFluteIndexIdHookRule<Member, Integer> {

        @Override
        protected ColumnInfo prepareTargetColumnInfo() {
            return MemberDbm.getInstance().columnMemberId();
        }
    }

    @Component
    public static class MemberAddressIndexIdHookRule extends AbstractDBFluteIndexIdHookRule<MemberAddress, Integer> {

        @Override
        protected ColumnInfo prepareTargetColumnInfo() {
            return MemberAddressDbm.getInstance().columnMemberId();
        }
    }
}
