package dev.isxander.controlify.contextual;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RuleSetManager<K, R extends Rule<K>> implements SimpleControlifyReloadListener<RuleSetManager.Preparations<K, R>> {
	public static final String DIRECTORY = "contextual/";

	private final Codec<RuleSet<K, R>> ruleSetCodec;
	private final String directory;
	private final FileToIdConverter converter;

	private volatile Map<Identifier, RuleEngine<K, R>> ruleEngineByDomain;

	public RuleSetManager(Codec<R> ruleCodec, String directory) {
		this.ruleSetCodec = RuleSet.createCodec(ruleCodec);
		this.directory = DIRECTORY + directory;
		this.converter = FileToIdConverter.json(this.directory);
		this.ruleEngineByDomain = Map.of();
	}

	public synchronized Optional<RuleEngine<K, R>> getRuleEngine(Identifier domainId) {
		return Optional.ofNullable(this.ruleEngineByDomain.get(domainId));
	}

	@Override
	public CompletableFuture<Preparations<K, R>> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, List<Resource>> ruleSetResourcesByDomain = converter.listMatchingResourceStacks(manager);

			var ruleEngineByDomain = new HashMap<Identifier, RuleEngine<K, R>>();
			for (var entry : ruleSetResourcesByDomain.entrySet()) {
				List<R> rules = parseRuleSets(entry.getValue());
				var ruleEngine = new RuleEngine<>(rules);
				ruleEngineByDomain.put(entry.getKey(), ruleEngine);
			}

			return new Preparations<>(ruleEngineByDomain);
		}, executor);
	}

	// highest priority resource pack's first rule should be the head of the resultant list.
	private List<R> parseRuleSets(List<Resource> ruleSetResources) {
		var rules = new ArrayList<R>();

		for (Resource ruleSetResource : ruleSetResources) {
			try (var reader = ruleSetResource.openAsReader()) {
				JsonElement json = JsonParser.parseReader(reader);
				DataResult<RuleSet<K, R>> ruleSetResult = this.ruleSetCodec.parse(JsonOps.INSTANCE, json);
				RuleSet<K, R> ruleSet = ruleSetResult.getOrThrow();

				if (ruleSet.replace()) {
					rules.clear();
				}

				rules.addAll(ruleSet.rules());
			} catch (IOException e) {
				throw new IllegalStateException("Could not parse rule set", e);
			}
		}

		return rules;
	}

	@Override
	public CompletableFuture<Void> apply(Preparations<K, R> data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.ruleEngineByDomain = data.ruleEngineByDomain();
		}, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("reload/" + this.directory);
	}

	public record Preparations<K, R extends Rule<K>>(
			Map<Identifier, RuleEngine<K, R>> ruleEngineByDomain
	) {}
}
