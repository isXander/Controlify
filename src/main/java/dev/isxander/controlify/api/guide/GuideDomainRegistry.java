package dev.isxander.controlify.api.guide;

public interface GuideDomainRegistry<T extends FactCtx> {
    void registerFact(Fact<? super T> fact);

    void registerDynamicRule(Rule rule);
}
