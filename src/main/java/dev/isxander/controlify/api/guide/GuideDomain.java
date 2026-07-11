package dev.isxander.controlify.api.guide;

public interface GuideDomain<T extends FactCtx> {
    void registerFact(Fact<? super T> fact);

    void registerDynamicRule(Rule rule);

    GuideInstance<T> createInstance();
}
