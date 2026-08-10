# Config migration fixtures

Store input and expected Controlify JSON files in a directory named after the
regression, then compare the pure migration result in a unit test.

```java
@Test
void upgradesExampleConfig() throws IOException {
    ConfigMigrationTestHelper.assertProfileMigration(
        "/config-migrations/example/input.json",
        "/config-migrations/example/expected.json"
    );
}
```

Use `assertSharedMigration` or `assertProfileMigration` for split configs.
Use `assertLegacyMigration` for monolithic schema 0–2 configs, passing the
expected shared JSON followed by each expected profile JSON.
