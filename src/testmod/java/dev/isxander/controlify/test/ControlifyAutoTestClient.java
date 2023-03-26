package dev.isxander.controlify.test;

import com.google.common.collect.Lists;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import static dev.isxander.controlify.test.ClientTestHelper.*;

public class ControlifyAutoTestClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Controlify Auto Test");

    @Override
    public void onInitializeClient() {
        var thread = new Thread(() -> {
            try {
                runTests(ControlifyTests.class);
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });
        thread.setName("Controlify Auto Test Thread");
        thread.start();
    }

    private void runTests(Class<?> testClass) throws Exception {
        List<Test> preLoadTests = Lists.newArrayList();
        List<Test> postLoadTests = Lists.newArrayList();

        Object testObject = testClass.getConstructor().newInstance();

        Method[] methods = testClass.getDeclaredMethods();
        for (var method : methods) {
            method.setAccessible(true);

            Test.PreLoad preLoad = method.getAnnotation(Test.PreLoad.class);
            if (preLoad != null) {
                if (method.getParameterCount() > 0)
                    throw new RuntimeException("PreLoad test method " + method.getName() + " has parameters!");

                preLoadTests.add(new Test(() -> {
                    try {
                        method.invoke(testObject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, preLoad.value()));
            }

            Test.PostLoad postLoad = method.getAnnotation(Test.PostLoad.class);
            if (postLoad != null) {
                if (method.getParameterCount() > 0)
                    throw new RuntimeException("PostLoad test method " + method.getName() + " has parameters!");

                postLoadTests.add(new Test(() -> {
                    try {
                        method.invoke(testObject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, postLoad.value()));
            }
        }

        boolean success = true;
        for (var test : preLoadTests) {
            success &= wrapTestExecution(test);
        }

        waitForLoadingComplete();

        for (var test : postLoadTests) {
            success &= wrapTestExecution(test);
        }

        LOGGER.info("--------");
        if (success)
            LOGGER.info("\u001b[32mAll tests passed!");
        else
            LOGGER.error("\n\u001b[31mSome tests failed!");

        System.exit(success ? 0 : 1);
    }

    private boolean wrapTestExecution(Test test) {
        LOGGER.info("\u001b[36mRunning test " + test.name() + "...");
        try {
            test.method().run();
            LOGGER.info("\u001b[32mPassed test " + test.name() + "!");
            return true;
        } catch (Throwable t) {
            LOGGER.error("\u001b[31mFailed test " + test.name() + "!", t);
            return false;
        }
    }
}
