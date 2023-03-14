package org.example;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.process_test_coverage.spring_test.ProcessEngineCoverageConfiguration;
import org.camunda.bpm.extension.process_test_coverage.spring_test.ProcessEngineCoverageTestExecutionListener;
import org.example.util.VariableChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import({ProcessEngineCoverageConfiguration.class})
@TestExecutionListeners(value = ProcessEngineCoverageTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ProcessScenarioTest {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private VariableChecker variableChecker;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    static {
        LogFactory.useSlf4jLogging(); // MyBatis
    }

    @Test
    @Deployment(resources = "process.bpmn")
    public void testHappyPath() throws InterruptedException {
        jdbcTemplate.execute("UPDATE ACT_GE_PROPERTY SET VALUE_ = '0', REV_ = '1' WHERE NAME_ = 'historyLevel'");

        var varMap = new HashMap<String, Object>();
        varMap.put("v1", V.val1);

        // Define scenarios by using camunda-bpm-assert-scenario:
        var processes = runtimeService.createConditionEvaluation()
                .setVariables(varMap)
                .evaluateStartConditions();
        variableChecker.check();

        runtimeService.setVariable(processes.get(0).getId(), "v1", V.val2);
        Thread.sleep(15_000);

        variableChecker.check();
    }
}
