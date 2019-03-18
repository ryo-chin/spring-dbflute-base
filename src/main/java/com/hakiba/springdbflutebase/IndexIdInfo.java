package com.hakiba.springdbflutebase;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hakiba
 */
@AllArgsConstructor
public class IndexIdInfo<ID> {
    @Getter
    private String targetTableName;
    @Getter
    private ID targetId;
}