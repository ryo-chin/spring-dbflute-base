package com.hakiba.springdbflutebase.dbflute;

import org.dbflute.bhv.core.BehaviorCommandMeta;

/**
 * @author hakiba
 */
public class DBFluteMetaInfoExtractorDispatcher {
    public static DBFluteMetaInfoExtractor dispatch(BehaviorCommandMeta cmdMeta) {
        if (cmdMeta.isEntityUpdateFamily()) {
            return new DBFluteEntityUpdateFamilyMetaInfoExtractor();
        } else if (cmdMeta.isQueryUpdateFamily()) {
            return new DBFluteQueryUpdateFamilyMetaInfoExtractor();
        } else if (cmdMeta.isBatchUpdateFamily()) {
            return new DBFluteBatchUpdateFamilyMetaInfoExtractor();
        } else {
            return null;
        }
    }
}
