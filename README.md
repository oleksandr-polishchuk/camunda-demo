# Conditional Start Event + Custom Object (Serializable) Values Variables Issue

_This small demo was set up to demonstrate an issue with Conditional Start Event + Custom Object (Serializable) Values Variables usage_

Most of our Process Definitions can be start by some condition (Conditional Start Event) and we use the next code to trigger them: 

```java
...
runtimeService.createConditionEvaluation()
  .processInstanceBusinessKey(businessKey)
  .setVariables(variables)
  .evaluateStartConditions();
...
```

Recently, with adding new and new Process Definitions, we noticed that our DB size and especially `act_ge_bytearray` table is unusually growing.
After some investigation we have found some correlations related to these variable-related records in `act_ge_bytearray` linked to nothing.

```sql
-- variable-related records linked to nothing (some tables that may have reference to act_ge_bytearray are omited because we don't use them)
SELECT
    ba.name_ AS "variable name",
    count(*) AS "unlinked to variable records"
FROM
    act_ge_bytearray ba
LEFT JOIN
    act_ru_variable v on ba.id_ = v.bytearray_id_
LEFT JOIN
    act_hi_varinst hv on ba.id_ = hv.bytearray_id_
WHERE
    v.id_ IS NULL AND hv.id_ IS NULL AND ba.deployment_id_ IS NULL AND ba.name_ != 'job.exceptionByteArray'
GROUP BY
    ba.name_
ORDER BY count(*) DESC;
```

For each Primitive Values from variables the Camunda Engine will create a record in `act_ru_variable`, and will remove this record once the Process Instance execution is finished.
For each Custom Object (Serializable) Values from variables the Camunda Engine will create:

- a record in `act_ru_variable`, and will remove this record once the Process Instance execution is finished;
- a record in `act_ge_bytearray` linked to the one from act_ru_variable, and will remove this record once the Process Instance execution is finished;
- a record in `act_ge_bytearray` per each Conditional Start Event from the Process Definition; and will not remove this record(s) once the Process Instance execution is finished.

_`act_ge_bytearray` records linked to historical data records from `act_hi_varinst` were not taken into consideration._

So, usage Conditional Start Event + Custom Object (Serializable) Values Variables will produce a lot of extra records in `act_ge_bytearray` that will never be removed, and it will slow down the queries to this table that may cause other issues (i.e. with deployment) because `act_ge_bytearray` is used for various purposes.

---

There is a simple Process Definition + ProcessScenarioTest + VariableChecker (to take records from `act_hi_varinst` and `act_ge_bytearray`) that will help to reproduce such behaviour. The process is started by condition in `StartEvent`, log info about variables by `LoggerDelegate` and end:
- in the middle of the process execution: we have 1 record in `act_ru_variable` and 2 in `act_ge_bytearray` for variable `v1`
- after the process execution: 1 record from `act_ru_variable` and 1 from `act_ge_bytearray` for variable `v1` will be removed as part of cleanup after the process, but 1 record in `act_ge_bytearray` will be stays in the table forever.

```
=========================================================================================
=========================================================================================
V1: variables 1, bytearrays 2
ActRuVariable(id=9, rev=1, type=serializable, name=v1, executionId=7, procInstId=7, procDefId=camunda-demo:1:3, bytearrayId=8, text=null, text2=org.example.V, varScope=7, sequenceCounter=1, concurrentLocal=false, tenantId=null)
ActGeByteArray(id=6, rev=1, name=v1, deploymentId=null, generated=null, tenantId=null, type=2, createTime=2023-03-14, rootProcInstId=null, removalTime=null)
ActGeByteArray(id=8, rev=1, name=v1, deploymentId=null, generated=null, tenantId=null, type=2, createTime=2023-03-14, rootProcInstId=null, removalTime=null) -> linked to variable 9
=========================================================================================
LoggerDelegate was invoked
...
=========================================================================================
V1: variables 0, bytearrays 1
ActGeByteArray(id=6, rev=1, name=v1, deploymentId=null, generated=null, tenantId=null, type=2, createTime=2023-03-14, rootProcInstId=null, removalTime=null)
=========================================================================================

```

---
The issue reproduced for:
```
Spring-Boot:  (v2.7.3)
Camunda Platform: (v7.18.0)
Camunda Platform Spring Boot Starter: (v7.18.0)
```