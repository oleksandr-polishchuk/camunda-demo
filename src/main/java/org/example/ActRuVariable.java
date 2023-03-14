package org.example;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActRuVariable {
    private String id;
    private Integer rev;
    private String type;
    private String name;
    private String executionId;
    private String procInstId;
    private String procDefId;
    private String bytearrayId;
    private String text;
    private String text2;
    private String varScope;
    private Integer sequenceCounter;
    private Boolean concurrentLocal;
    private String tenantId;
}
