package dev.isxander.controlify.test;

import java.util.List;

public record DiscoveredTests(List<Test> entrypointTests, List<Test> titleScreenTests) {
    public boolean hasRanAllTests() {
        return entrypointTests.stream().allMatch(Test::hasRan)
                && titleScreenTests.stream().allMatch(Test::hasRan);
    }
}
