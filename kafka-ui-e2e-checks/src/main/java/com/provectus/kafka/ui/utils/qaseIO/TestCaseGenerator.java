package com.provectus.kafka.ui.utils.qaseIO;

import com.provectus.kafka.ui.utils.qaseIO.annotation.Suite;
import io.qase.api.QaseClient;
import io.qase.client.ApiClient;
import io.qase.client.api.CasesApi;
import io.qase.client.model.Filters;
import io.qase.client.model.TestCaseCreate;
import io.qase.client.model.TestCaseListResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

import static io.qase.api.QaseClient.getConfig;

@Slf4j
public class TestCaseGenerator implements TestExecutionListener {

    private static final ApiClient apiClient = QaseClient.getApiClient();
    private static final CasesApi casesApi = new CasesApi(apiClient);

    @SneakyThrows
    public static void createTestCaseIfNotExists(Method testMethod) {

        TestCaseCreate caseCreate = new TestCaseCreate();
        HashMap<Long, String> cases = getTestCasesTitleAndId();
        String testCaseTitle = getClassName(MethodSource.from(testMethod)) + "." + testMethod.getName() + " : " +
                MethodNameUtils.formatTestCaseTitle(testMethod.getName());

        if (!cases.containsValue(testCaseTitle)) {
            caseCreate.setTitle(testCaseTitle);
            if (testMethod.isAnnotationPresent(Suite.class)) {
                long suiteId = testMethod.getAnnotation(Suite.class).suiteId();
                caseCreate.suiteId(suiteId);
            }
            casesApi.createCase(getConfig().projectCode(), caseCreate);
            log.info("New test case = '" + testCaseTitle + "' created");
        }
    }

    @SneakyThrows
    public static HashMap<Long, String> getTestCasesTitleAndId() {
        HashMap<Long, String> map = new HashMap<>();
        TestCaseListResponse response =
                casesApi.getCases(getConfig().projectCode(), new Filters().status(Filters.SERIALIZED_NAME_STATUS), 100, 0);
        for (int i = 0; i < Objects.requireNonNull(Objects.requireNonNull(response.getResult()).getEntities()).size(); i++) {
            map.put(response.getResult().getEntities().get(i).getId(),
                    response.getResult().getEntities().get(i).getTitle());
        }
        return map;
    }

    public static String getClassName(MethodSource testSource) {
        Class<?> testClass;
        try {
            testClass = Class.forName(testSource.getClassName());
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            return null;
        }
        return testClass.getSimpleName();
    }
}
