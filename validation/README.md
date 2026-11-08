# Three-port regression smoke test

This separate development mod is not included in the Consecration release jar.
It loads the three release jars and exercises their behavior in a real dedicated server and integrated client world.

Check out the `port/1.21.1-neoforge` branches of `consecration`, `demolition-order`, and `weighted_armors` in sibling folders. Build all three with Java 21 first. From Consecration's root:

```powershell
.\validation\run-validation.ps1
```

The script creates an isolated test world under validation/run, runs the dedicated server checks, copies that test world for client Quick Play, and opens then closes a test client automatically. Server/player saves in other Minecraft instances are not used.

Expected: `PORT_TEST_SUCCESS checks=22` in each run and `PORT_CLIENT_SUCCESS` in the client log. The script also checks the result files; a Gradle success alone does not prove the behavior checks passed.

Optional Silent Gear and Werewolves integrations are not covered by this harness.
