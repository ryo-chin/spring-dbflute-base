package com.hakiba.springdbflutebase.hook;

import com.hakiba.springdbflutebase.FirstIndexIdExecutor;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hakiba
 */
@Component
public class DBFluteIndexIdHook implements DBFluteHook {
    @Autowired
    private FirstIndexIdExecutor firstIndexIdExecutor;

    @Override
    public void execute(BehaviorCommandMeta cmdMeta) {
        firstIndexIdExecutor.execute(cmdMeta);
    }
}
