package com.hakiba.springdbflutebase.hook;

import org.dbflute.bhv.core.BehaviorCommandMeta;

/**
 * @author hakiba
 */
public interface DBFluteHook {
    void execute(BehaviorCommandMeta cmdMeta);
}
