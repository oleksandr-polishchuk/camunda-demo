package org.example;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class ActGeByteArray {
    private String id;
    private Integer rev;
    private String name;
    private String deploymentId;
    private Boolean generated;
    private String tenantId;
    private Integer type;
    private LocalDate createTime;
    private String rootProcInstId;
    private LocalDate removalTime;
}
