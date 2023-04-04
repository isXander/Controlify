package dev.isxander.controlify.test;

import com.google.common.collect.Lists;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import static dev.isxander.controlify.test.ClientTestHelper.*;

public class ControlifyAutoTestClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Controlify Auto Test");

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

            Test.Entrypoint entrypoint = method.getAnnotation(Test.Entrypoint.class);
            if (entrypoint != null) {
                if (method.getParameterCount() > 0)
                    throw new RuntimeException("PreLoad test method " + method.getName() + " has parameters!");

                preLoadTests.add(new Test(() -> {
                    try {
                        method.invoke(testObject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, entrypoint.value()));
            }

            Test.TitleScreen titleScreen = method.getAnnotation(Test.TitleScreen.class);
            if (titleScreen != null) {
                if (method.getParameterCount() > 0)
                    throw new RuntimeException("PostLoad test method " + method.getName() + " has parameters!");

                postLoadTests.add(new Test(() -> {
                    try {
                        method.invoke(testObject);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, titleScreen.value()));
            }
        }

        boolean success = true;
        for (var test : preLoadTests) {
            success &= wrapTestExecution(test);
        }

        waitForLoadingComplete();
        submitConsumerAndWait(client -> client.setScreen(new TitleScreen()));

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
            test.runTest();
            LOGGER.info("\u001b[32mPassed test " + test.name() + "!");
            takeScreenshot(test.name());
            return true;
        } catch (Throwable t) {
            LOGGER.error("\u001b[31mFailed test " + test.name() + "!", t);
            takeScreenshot(test.name());
            return false;
        }
    }
}
