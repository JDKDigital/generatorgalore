# Port: MC 1.21.1 -> 26.1.2

Branch `dev-26.1`, forked from the 1.21.1 line at 1.7.0.

---

# RESUME HERE

**State: SHIPPED.** `generatorgalore-26.1.2-1.7.0.jar` was built and uploaded on 2026-08-29. The
port is complete — nothing is excluded from the build, and every behaviour is verified in game
(dedicated server, client, singleplayer, datagen, and a real multiplayer join).

**Remaining work, all external:**

1. Dependency file IDs for pipez, productive-metalworks, powah-rearchitected, cloth-config and
   guideme as they publish for 26.1.2. Resolved so far: jei `8755208`, jade `8651070`,
   productivebees `8756739`.
2. **Watch for the NeoForge fix to conditions in `ReloadableServerRegistries`.** Until it lands, the
   six `has64x: false` generators each log one `Unknown registry key` parse error per datapack load
   in production. See the decision section below for the exact change to make when it is fixed.

**House rules:** no comments in code, ever (not even explanatory ones). Never run git mutations
(`git mv/rm/checkout/commit`) — use plain `rm`/`mv` and leave version control to Jais.

## `src/generated/**` — hand-patched, then REGENERATED and confirmed

The generated data was hand-edited to the 26.1 formats while `data/**` was excluded. `data/**` is now
restored and `./gradlew runData` **reproduces that output exactly** — same blockstates, same block
models, same conditions in the same places. The hand-patch is no longer load-bearing. What datagen
additionally produced, which the hand-patch could not:

- `assets/generatorgalore/items/<id>.json` dispatch files for all 45 generator variants. Without
  these every item renders missing-texture (Step 26). The 14 upgrade items ship hand-written models,
  so their dispatch files are hand-written under `src/main/resources/assets/generatorgalore/items/`.

## The loot-table condition trap (NeoForge 26.1.2.100)

`neoforge:registered` **silently evaluates false inside loot tables**, so every conditioned loot
table vanishes with no error logged. Loot tables load through `ReloadableServerRegistries`, which
passes a plain `RegistryOps` rather than a `ConditionalOps`; `ConditionalOps.retrieveContext()` then
falls back to `ICondition.IContext.EMPTY`, whose `registryAccess()` is `RegistryAccess.EMPTY`, so
`RegisteredCondition.test` can never find the item. Note `IContext.registryAccess()` is marked
`@Deprecated(forRemoval = true, since = "26.1.2")` — this is transitional upstream breakage.
26.1.2.100 was the newest build available; there is no newer one to upgrade to.

Verified by experiment: `mod_loaded=generatorgalore` on the same table loads, a bogus `mod_loaded`
correctly excludes, and `neoforge:registered` excludes a variant that demonstrably exists.
**Recipes, advancements and data maps are NOT affected** — each builds a proper `ConditionalOps`
(confirmed: stripping their conditions changes the loaded counts by zero).

### UPSTREAM INTEL (2026-08-29, from NeoForge, via Jais's bug report)

NeoForge confirmed the issue and warned it **gets bigger in 26.3, because recipes move into the same
reloadable registry as loot tables**. If the context plumbing is not fixed by then, `neoforge:registered`
goes silently false for recipes too.

For this mod that is severe. The condition GATES variants, so "always false" does not mean "the six
disabled variants drop out" — it means **all 59 conditioned recipes drop out**, and every generator
and upgrade becomes uncraftable with nothing whatsoever in the log. Loot tables not dropping is
survivable; a silently uncraftable mod is not.

Implication for the fix: a condition that never touches `ICondition.IContext` is immune to this
entire class of breakage regardless of which registry or loader path the data lands in. That is
empirically established, not just theory — `ItemRegisteredCondition` (testing
`BuiltInRegistries.ITEM.containsKey(id)`; static registries are frozen long before any datapack
reload) was verified working **in the loot path**, the exact path where `neoforge:registered` fails.

**Jais's call (2026-08-29): no workaround.** The issue is reported and upstream is expected to fix it
before 26.3 lands, so the mod stays on `neoforge:registered` for recipes/advancements/data maps and
carries no condition on loot tables. Nothing to change now.

**But do not take the fix on faith when 26.3 arrives — re-test conditioned recipes first** — `/loot spawn` has an equivalent
canary for recipes only via actually crafting, so the cheap check is the recipe count in the log:
compare `Loaded N recipes` with and without `neoforge:conditions` present.

### DECISION: ship loot tables with NO condition

Jais's call (2026-08-29): **loot tables carry no `neoforge:conditions` at all.** `LootDataProvider`
calls plain `dropSelf(...)`; the `conditions` map and the `CONDITIONAL_TABLE_CODEC` write path are
left in place but unused, so re-enabling is a one-line change in `dropSelf`'s caller.

Why not `neoforge:registered`: its failure here is SILENT, not noisy. `ConditionalDecoder` tests the
condition before decoding and yields `Optional.empty()`, so there is no log line — the table simply
does not exist and **every generator drops nothing when broken**. That is strictly worse than the
noise it was meant to prevent.

**The accepted cost:** in production the six `has64x: false` generators (copper, diamond, emerald,
gold, iron, obsidian) ship a `_64x` loot table whose item is never registered, so their tables fail
to parse and log `Couldn't parse element ... Unknown registry key` once per datapack load. Six
errors, drops work everywhere else. This is the pre-1.7.0 behaviour, knowingly reinstated.

**When NeoForge fixes `ReloadableServerRegistries`**, restore the condition to remove that noise:
put back `NeoForgeConditions.itemRegistered(BuiltInRegistries.BLOCK.getKey(block))` into
`LootProvider.conditions` and re-run datagen. Canary for the fix — with a conditioned table in
place, `/loot spawn ~ ~ ~ loot generatorgalore:blocks/copper_generator` returning
"Dropped 1 [Copper Generator]" instead of "Can't find element" means it is fixed.

A custom `ItemRegisteredCondition` (testing `BuiltInRegistries.ITEM.containsKey(id)`, needing no
`IContext`) was written and verified working in both directions, then removed in favour of waiting.
See git history if it is ever wanted back.

## Verified in game (dedicated server AND client, 26.1.2.100, 0 errors)

- Config self-restore: deleting `run/config/generatorgalore/` regenerates all 15 files plus
  `defaults.lock` — this exercises the `JarContents.visitContent` rewrite that replaced the
  now-nonexistent `IModFile.findResource`
- Registration of every generator, variant, upgrade item and block entity
- 1574 recipes / 1676 advancements load; data maps load
- Fuel consumption: 5 coal -> 4 -> 3 across forced burns (the snapshot trap is genuinely fixed)
- Energy generation: climbs at the configured rate and caps exactly at `bufferCapacity`, no overflow
- Automation guard: a hopper beneath the generator extracted nothing over 8s
- Hopper insertion INTO the fuel slot works (capability plus the `isValid` accept path)
- Save/load round-trip: a generator kept 3 coal and 5000 FE across a full server restart
- Loot tables: every variant loads and drops, verified both via `/loot spawn` and by actually
  breaking a placed generator (dropped `generatorgalore:iron_generator_8x`)
- Block names resolve — `useBlockDescriptionPrefix()` is required on the hand-built `GeneratorBlockItem`
- Client launches into a world with 0 errors, 0 missing item models, 0 bake failures, 0 missing
  texture references
- Datagen (`runData`) runs clean and its output matches the hand-patched files
- JEI loads: plugin `generatorgalore:generatorgalore` registers its recipes, both categories
  construct, `Starting JEI took 976ms` with 0 errors
- Play-tested by Jais: magmatic generator GUI opens, the fluid gauge renders lava at the right level
  with the right tint, labels are visible, tooltips track the cursor, JEI fluid category is steady
- **Multiplayer join against a real dedicated server** — client connected, `RecipesReceivedEvent`
  fired, JEI restarted against synced server data, 0 errors on both sides. This is the only way to
  exercise the Step 22 recipe-sync trap (an empty `ItemStack` in a recipe `STREAM_CODEC` throws on
  the server's netty write thread during join, so it looks like "cannot connect" and neither a
  dedicated-server smoke test nor singleplayer catches it).
- **Energy push to a neighbour (trap 4)** — verified against `productivebees:heated_centrifuge`
  placed beside a copper generator. The centrifuge climbed 6608 -> 9336 FE while the generator's own
  buffer stayed at **0** (it pushes everything out as it generates) and its coal count decremented.
  Measured ~8.2 FE/tick against copper's configured 8, so `transferTo` moves energy 1:1 — no
  duplication, no loss. Recipients are discovered on all 6 sides and re-scanned every 111 ticks, so
  a freshly placed neighbour can take ~5.5s to start receiving.
- Player pickup from the fuel slot (trap 2) — resolved by inspection rather than a click:
  `ResourceHandlerSlot.mayPickup` is the ONLY caller of `handler.extract` on the take path;
  `StackCopySlot.remove` goes through `getStackCopy()` + `set(...)`, which the automation guard does
  not block. With `ManualSlotItemHandler.mayPickup` overridden, players can take their own fuel while
  hoppers still cannot.

## Fixed during runtime verification

| Symptom | Cause | Fix |
|---|---|---|
| `NullPointerException: Block id not set`, then unbound `ResourceKey` for every generator | 26.1 requires blocks/items to know their registry id at construction; plain `DeferredRegister.register` never sets it | `DeferredRegister.createBlocks/createItems` plus `registerBlock(name, props -> ..., propsSupplier)` / `registerItem(...)` in `GeneratorCreator` |
| Server crashed one tick after a generator received fuel | `Item.getCraftingRemainder()` is `@Nullable` in 26.1 (was an empty template) and is deprecated | null-guard plus the stack-sensitive `fuelStack.getCraftingRemainder()` |
| 168 datapack errors | `neoforge:item_exists` renamed | `neoforge:registered` (field `value`, optional `registry` defaulting to `minecraft:item`) |
| 61 recipe parse errors | the ingredient object form was removed | bare `"id"` / `"#tag"` strings |
| Every conditioned loot table missing, no error logged | see the loot-table condition trap above | condition dropped from loot tables entirely |
| 59 `Missing item model` warnings, items render missing-texture | 1.21.4+ moved the item-model lookup root to `assets/<mod>/items/<id>.json` | datagen emits them via `blockModels.registerSimpleItemModel(block, modelId)`; hand-written items need hand-written dispatch files |
| 14 upgrade items: `Unable to bake item model` -> `Multiple atlases used in model` | 26.1 forbids ONE item model mixing atlases. The upgrade models combine vanilla **block** textures (`#base`) with a mod **item** texture (`#frame`) | moved `textures/item/upgrade_frame.png` -> `textures/block/upgrade_frame.png` so the whole model is on the block atlas |
| `Missing texture references ... particle` | 26.1 warns when a model declares no `particle` slot | added `"particle": "#base"` to `models/item/upgrade.json` |
| **Crash** on right-clicking any FLUID generator with an empty hand: `IllegalArgumentException: Expected stack to be non-empty` | 26.1 `ItemAccess.forStack(stack)` **throws** on an empty stack, where the old `stack.getCapability(...)` returned null. `useItemOn` fires on every right-click, empty hand included | `!pStack.isEmpty()` guard in `Generator.useItemOn`; same latent trap guarded in `GeneratorBlockEntity.isValid` |
| JEI fluid category flickers between a full and an empty tank | the `#minecraft:lava` tag holds BOTH `lava` and `flowing_lava`; JEI cycles the ingredient list and the flowing variant has no still sprite. **Pre-existing since 1.21.1**, not port damage | filter both fluid paths on `defaultFluidState().isSource()` |
| GUI tooltip renders far from the cursor, up and left by exactly the GUI origin | `setTooltipForNextFrame` is DEFERRED, so it takes screen coords and ignores the `extractLabels` pose translate. The old immediate-mode `renderTooltip` needed `- leftPos/- topPos`; carrying that offset over double-compensates | pass raw `mouseX, mouseY` |
| AT could hard-fail mod loading | `TextureSlot` moved | `net.minecraft.client.data.models.model.TextureSlot`, in BOTH the dot and descriptor forms. Both AT entries are redundant now anyway — `PotionBrewing.containers` and `TextureSlot.create` are already public in 26.1 |

## Traps — all verified

Nothing outstanding. Trap 2 (player pickup) was resolved by inspection; trap 4 (energy transfer) is
confirmed in game against a real consumer — see the verified list.

## Client rendering port (26.1 specifics hit here)

- `GuiGraphics` -> **`GuiGraphicsExtractor`**. `drawString` -> `text(font, s, int x, int y, argb, shadow)`
  — note **int** coords, and the colour is ARGB, so the classic `4210752` (`0x404040`) is fully
  transparent. Vanilla uses `-12566464` (`0xFF404040`).
- `AbstractContainerScreen`: `renderBg(...)` -> `extractBackground(GuiGraphicsExtractor, int mouseX,
  int mouseY, float partialTick)`, `renderLabels(...)` -> `extractLabels(GuiGraphicsExtractor, int, int)`.
  The boilerplate `render()` override (renderBackground + super + renderTooltip) is now handled by the
  base `extractRenderState` — just delete it.
- `extractLabels` runs inside a `pose().translate(leftPos, topPos)`, `extractBackground` does NOT —
  so labels stay GUI-local and background blits stay absolute, same as before.
- `blit(texture, x, y, u, v, w, h)` -> `blit(RenderPipelines.GUI_TEXTURED, texture, x, y, float u,
  float v, w, h, texW, texH)`.
- `renderTooltip(font, list, x, y)` -> `setTooltipForNextFrame(list, x, y)`.
- `getGuiLeft/getGuiTop/getXSize/getYSize` are deprecated for removal -> `getLeftPos/getTopPos/
  getImageWidth/getImageHeight`.
- **Fluids in GUIs:** `IClientFluidTypeExtensions.getStillTexture/getTintColor` are GONE. Read the
  data-driven fluid model instead:
  `Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState())`
  -> `.stillMaterial().sprite()` and `.tintSource().color(state.createLegacyBlock())`. Tile it with
  `enableScissor` + per-tile `blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h, tint)` so
  partial edges crop instead of stretching. `productivelib`'s `FluidRenderHelper` +
  `FluidContainerUtil` on `dev-26.1` are the reference.
- **Particles:** `TextureSheetParticle` is GONE -> extend `SingleQuadParticle`, whose constructor
  takes the `TextureAtlasSprite` (get it with `spriteSet.get(level.getRandom())`).
  `getRenderType()` -> `protected Layer getLayer()` returning `SingleQuadParticle.Layer.OPAQUE`.
  `getLightColor(float)` -> `getLightCoords(float)`. `ParticleProvider.createParticle` gains a
  trailing `RandomSource`.

## Datagen port (26.1 specifics hit here)

- `GatherDataEvent` -> **`GatherDataEvent.Client`**; `ExistingFileHelper` is GONE (drop it from every
  provider constructor); `event.includeClient()/includeServer()` -> just `true`.
- `BlockStateProvider`/`ItemModelProvider` are GONE -> `extend net.minecraft.client.data.models.ModelProvider`
  with `registerModels(BlockModelGenerators, ItemModelGenerators)` + `getKnownBlocks()`/`getKnownItems()`.
  Build models with `new ModelTemplate(Optional.of(parentId), Optional.empty(), slots...)` and
  `.create(block, textureMapping, blockModels.modelOutput)`. `TextureMapping.put` takes a
  **`Material`**, not an `Identifier`. Custom slots via `TextureSlot.create("name")` (already public —
  the AT for it is redundant).
- Blockstates: `MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(PROP).select(...))`
  for model choice, then `.with(PropertyDispatch.modify(FACING).select(dir, VariantMutator.Y_ROT
  .withValue(Quadrant.R90)))` for rotation. `BlockModelGenerators.NOP` is the identity mutator.
- Block item models: `blockModels.registerSimpleItemModel(block, modelId)` — writes the `items/`
  dispatch file. No `models/item/` layer needed.
- `RecipeProvider` is now `(HolderLookup.Provider, RecipeOutput)` with `abstract void buildRecipes()`
  (no args) plus a `Runner` inner class the DataGenerator actually registers. `output` and
  `registries` are protected fields.
- `ShapedRecipeBuilder.shaped(...)` static -> inherited instance method `shaped(...)`.
  `.define(char, TagKey<Item>)` and `.define(char, ItemLike)` exist directly, so
  `Ingredient.of(tag)` is unnecessary — and `Ingredient.of(TagKey)` no longer compiles. For a real
  `Ingredient` from a tag: `Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(tag))`.
- `.save(output, Identifier)` -> `.save(output, ResourceKey.create(Registries.RECIPE, id))`.
- `IConditionBuilder` is GONE — drop the interface.
- Tag providers: `tag(...)` returns `TagAppender<T, T>`, so `addOptional(Identifier)` no longer
  compiles. Use `add(TagEntry.optionalElement(id))`.
- `FluidIngredient.tag(TagKey)` is GONE -> `FluidIngredient.of(provider.lookupOrThrow(Registries.FLUID)
  .getOrThrow(tagKey))`.
- `ResourceKey.location()` -> `ResourceKey.identifier()`.

## JEI 26.1 plugin port

Dep: `implementation "curse.maven:jei-238222:8755208"` (jei-26.1.2-neoforge-29.33.0.87, beta channel —
it was the newest 26.1.2 build).

- `mezz.jei.api.recipe.RecipeType` -> **`mezz.jei.api.recipe.types.IRecipeType`**;
  `RecipeType.create(ns, path, cls)` -> `IRecipeType.create(ns, path, cls)`.
- `registration.addRecipeCatalyst(stack, type)` -> `registration.addCraftingStation(type, stack)`
  (note the argument order flips).
- `IRecipeCategory.draw(..., GuiGraphics, ...)` -> `draw(..., GuiGraphicsExtractor, ...)`, and
  `guiGraphics.drawString(...)` -> `text(...)` with an **ARGB** colour (see the client-rendering notes).
- `guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack)` ->
  `guiHelper.createDrawableItemStack(stack)`.
- `FuelRecipeMaker.getFuelRecipes(ingredientManager)` gained a second arg:
  `getFuelRecipes(ingredientManager, RecipeType.SMELTING)`.
- `Ingredient#getItems()` -> `items()` returning `Stream<Holder<Item>>`.
- Vanilla-side fallout hit here too: `EnchantedBookItem` is gone (`EnchantmentHelper.createBook(
  new EnchantmentInstance(...))`), `stack.getItem().getFoodProperties(stack, null)` ->
  `stack.get(DataComponents.FOOD)`, `Registry#holders()` -> `listElements()`,
  `Registry#getTag(TagKey)` -> `getTagOrEmpty(TagKey)` (returns an `Iterable<Holder<T>>`, not an
  `Optional<HolderSet>`), and `Ingredient.of(ItemStack)` / `Ingredient.of(TagKey)` are both gone —
  use `Ingredient.of(ItemLike)` or `Ingredient.of(Stream<? extends ItemLike>)`.
- **JEI plugins only run on world load**, not at the title screen. A client run that stops at the
  menu proves nothing about the plugin — load a world (`--quickPlaySingleplayer <save>`) and look for
  `Registering recipes: <modid>:<plugin>` in the log.
- A stale `run/config/jei/*.ini` from the previous JEI version logs a handful of
  `'x' is not a valid config key` ERRORs. That is JEI's own config migration, not your mod — delete
  `run/config/jei` and they go away.

## Headless verification recipe (reusable)

All of the in-game verification above was done with no client at all, via RCON on the dev server:

- `run/server.properties` now has `enable-rcon=true`, `rcon.password=devtest`, `spawn-protection=0`
  and **`pause-when-empty-seconds=0`**. That last one is essential and non-obvious: since 1.21.2 a
  dedicated server stops ticking after 60s with no players, so hoppers and block entities freeze and
  every test silently reads a frozen world. Symptom: `time query gametime` returns the same value twice.
- `scratchpad/rcon.py` is a ~40-line RCON client: `python rcon.py "cmd1" "cmd2"`.
- Rig: `/forceload add`, `/setblock` the generator, a `hopper[facing=down]` above it and a chest above
  that, then `/item replace block <chest> container.0 with minecraft:coal 8`. Read state back with
  `/data get block <pos>` — the BE serializes as
  `{inv:{stacks:[...]}, energy:{energy:N}, litTime, litDuration}`.
- `/data merge block <pos> {energy:{energy:0},litTime:0,inv:{stacks:[{count:5,id:"minecraft:coal"},{}]}}`
  sets up a controlled burn; that is how fuel consumption was pinned down precisely.
- `/loot spawn <pos> loot <table_id>` tests a loot table directly and distinguishes "table missing"
  from "table empty" — the only way to catch a silently-false condition.
- Edit `build/resources/main/**` then `/reload` to A/B a datapack change without restarting.
- Shut down with RCON `/stop`, then poll for the JVM by `fml.startup.Server` in its command line.
  Never TaskStop the gradle task — that orphans the JVM holding `session.lock`.

## 26.1 API cheat sheet (learned the hard way this session)

| Need | 26.1 answer |
|---|---|
| Item handler | `net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler` |
| Fluid handler | `...transfer.fluid.FluidStacksResourceHandler(size, capacity)` |
| Energy handler | `...transfer.energy.SimpleEnergyHandler`; `getAmountAsInt()`, `getCapacityAsInt()`, `set(int)`, `insert/extract(int, TransactionContext)` |
| Transactions | `...transfer.transaction.Transaction.openRoot()`, `tx.commit()`, try-with-resources |
| Read a slot | `...transfer.item.ItemUtil.getStack(handler, index)` (SNAPSHOT — never mutate it) |
| Write a slot | `handler.set(index, ItemResource.of(stack), count)`; empty is `ItemResource.EMPTY, 0` |
| Handler guards | override `isValid(int, T)` for insert, `extract(int, T, int, tx)` for extraction |
| Menu slot | `...transfer.item.ResourceHandlerSlot(handler, handler::set, index, x, y)` |
| Item capability | `ItemAccess.forStack(stack)` / `ItemAccess.forHandlerIndex(handler, i)` then `.getCapability(Capabilities.Energy.ITEM)` |
| BE save/load | `loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`; `input.getIntOr/getDoubleOr`, `output.child("k")` |
| BE packet | `onDataPacket(Connection, ValueInput)`, `getUpdateTag` returns `saveWithoutMetadata(provider)` |
| BE from tag | `TagValueInput.create(reporter, registries, tag)` + `loadWithComponents(...)` inside a `ProblemReporter.ScopedCollector` |
| Mod file resources | `IModFile.getContents()` -> `JarContents`: `visitContent(visitor)`, `getContentRoots()`, `openFile(name)` |
| Dev vs prod | `FMLEnvironment.isProduction()` (method, not a field) |
| Block registration | `DeferredRegister.createBlocks(id)` + `registerBlock(name, props -> new B(props), () -> Properties.ofFullCopy(...))`. Plain `register` leaves the id unset -> `NullPointerException: Block id not set` |
| Item registration | `DeferredRegister.createItems(id)` + `registerItem(name, props -> new I(props), () -> new Item.Properties())`. Hand-built `BlockItem`s must add `.useBlockDescriptionPrefix()` or the name resolves to `item.<modid>.<name>` |
| Conditions | `neoforge:item_exists` -> `neoforge:registered` (`value`, optional `registry`). Helper: `NeoForgeConditions.itemRegistered(id)`. SILENTLY FALSE inside loot tables - see the trap above |
| Ingredients in JSON | object form removed: `{"item":"x"}` -> `"x"`, `{"tag":"y"}` -> `"#y"`. Same for `FluidIngredient` |
| Registry get | returns `Optional<Holder.Reference<T>>` -> `.map(Holder::value).orElse(...)` |
| Food | `stack.get(DataComponents.FOOD)` |
| Burn time | `stack.getBurnTime(RecipeType.SMELTING, level.fuelValues())` |
| Crafting remainder | `stack.getCraftingRemainder()` (stack-sensitive) returns a **@Nullable** `ItemStackTemplate` -> `.create()`. `Item.getCraftingRemainder()` is deprecated AND nullable - null-check or it NPEs at runtime |
| Enchanted book | class gone -> `stack.is(Items.ENCHANTED_BOOK)` |
| Ingredient items | `ingredient.items()` -> `Stream<Holder<Item>>` |
| Component ingredient | `DataComponentIngredient(HolderSet<Item>, DataComponentPatch, boolean)`; accessors `itemSet()` / `components()` |
| Block removal | `affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos, boolean)` |
| Block tooltip | `Block.appendHoverText` gone -> custom `BlockItem` with `appendHoverText(stack, ctx, TooltipDisplay, Consumer<Component>, flag)` |
| Interaction | `ItemInteractionResult` gone; `useItemOn` returns `InteractionResult`, pass-through is `TRY_WITH_EMPTY_HAND` |
| Inventory size | `Inventory.INVENTORY_SIZE` (`items` is private) |

## Where to look things up

- NeoForge sources: `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.100/*/neoforge-26.1.2.100-sources.jar`
  (`unzip -o -q <jar> "net/neoforged/neoforge/transfer/*"` into the scratchpad, then grep).
- Vanilla sources: `build/moddev/artifacts/minecraft-patched-26.1.2.100-sources.jar`.
- Loader API (`IModFile`, `JarContents`, `FMLEnvironment`): `~/.gradle/caches/.../fancymodloader/loader/11.0.15/loader-11.0.15.jar`, read with `javap -classpath`.
- **Ported sibling mod:** `d:/projects/productivelib` on branch `dev-26.1` has the same class names
  already ported (`CapabilityBlockEntity`, `AbstractBlockEntity`, `ManualSlotItemHandler`,
  `MultiFluidTank`, `InventoryHandlerHelper`). Copy its shapes rather than inventing them.
- The `mc-1211-to-261-port` skill has the full step list; the `mod-dev-runs` skill covers launching
  and shutting down runs.

---

## Excluded from the build

**Nothing.** `sourceSets.main.java` has no excludes left.

## Done so far

- Base `CapabilityBlockEntity` on `ValueInput`/`ValueOutput`, `onDataPacket(Connection, ValueInput)`
- `ControlledEnergyStorage` -> `SimpleEnergyHandler`, external insert blocked via `insert` override,
  machine-side generation goes through `insertInternal`
- `ManualItemHandler` -> `ItemStacksResourceHandler`, automation guard moved onto `extract`
  (the `ResourceHandler` override point, not a legacy bridge), internal consumption via `extractInternal`
- `RisingEnchantParticleType` moved `client.particle` -> `common.particle` (it is common; only the
  renderer is client) so `ModParticles` and `Generator` compile with `client/**` excluded
- `GeneratorScreen` + `FluidContainerUtil` added to the exclude list (pure client UI)

## Status: core compiles on 26.1.2

`./gradlew clean compileJava` is green with the client/datagen/JEI subsystems excluded.
Nothing has been run in game yet.

## Ported

| Area | What changed |
|---|---|
| Base BE | `ValueInput`/`ValueOutput`, `onDataPacket(Connection, ValueInput)`, `saveWithoutMetadata` |
| Energy | `ControlledEnergyStorage` -> `SimpleEnergyHandler`; external insert returns 0, generation uses `insertInternal`; menu sync uses `set(int)` |
| Items | `ManualItemHandler` -> `ItemStacksResourceHandler`; automation guard on `extract`, machine consumption via `extractInternal` |
| Fluids | `FluidTank` -> `FluidStacksResourceHandler`, drain via `extract` in a transaction |
| Fuel consumption | `fuelStack.shrink(1)` replaced with a transactional `extractInternal` (the snapshot trap) |
| Block | `useItemOn` returns `InteractionResult`, new `updateShape`, `affectNeighborsAfterRemoval`, `getAnalogOutputSignal` gained `Direction` |
| Tooltip | `Block.appendHoverText` is gone -> new `GeneratorBlockItem` carries it, wired in `GeneratorCreator` |
| Recipes | `RecipeSerializer` record, `group`/`showNotification`/`placementInfo`/`recipeBookCategory`, inner `Serializer` deleted, `ItemStack.OPTIONAL_STREAM_CODEC` |
| Slots | `SlotItemHandler` -> `ResourceHandlerSlot`; `mayPickup` overridden so the extract guard does not stop players taking their own fuel |
| Config restore | `IModFile.findResource` does not exist in 26.1 -> rewritten on `IModFile.getContents().visitContent(...)`, which enumerates dev roots and a packaged jar alike |
| Misc | `FMLEnvironment.isProduction()`, `BlockEntityType` constructor, registry `Optional` getters, `DataComponents.FOOD`, `getBurnTime(type, level.fuelValues())`, `DataComponentPatch` ingredient |

## Next

- [x] Run the mod and check the log — dedicated server AND client, 0 errors
- [x] Verify fuel is actually consumed (the snapshot trap)
- [x] Verify config defaults regenerate (the `visitContent` rewrite)
- [x] `accesstransformer.cfg`: `TextureSlot` path fixed
- [x] Restore `client/**`, `GeneratorScreen`, `FluidContainerUtil`
- [x] Restore `data/**` and re-run datagen; output matches the hand-patched files
- [x] Player can take fuel back out of the slot (resolved by inspection)
- [x] Restore `integrations/**` — JEI 26.1.2 exists and the plugin loads clean
- [ ] **Play-test** the generator GUI, the fluid gauge, and the two JEI recipe categories
- [ ] Verify energy pushes to a neighbour (needs a 26.1 energy mod)
- [ ] Dependency file IDs for the 7 remaining curse mods when they publish 26.1.2
- [ ] **Watch for a NeoForge build that fixes conditions in `ReloadableServerRegistries`**, then
      restore the loot-table condition to silence the six production parse errors. See the decision
      section at the top for the exact change and the canary command.

## Core port steps

- [x] Bulk renames (ResourceLocation -> Identifier, RenderType, Util, ...)
- [x] `@EventBusSubscriber.Bus` removal
- [x] Step 1 block API + `DeferredRegister.Blocks` / `registerBlock`
- [x] Step 2 capability renames (`Capabilities.Item/Energy/Fluid`)
- [x] Step 3 ResourceHandler refactor — generator BEs hold item + fluid + energy
- [ ] Step 10/11 recipe API (`FluidFuelRecipe`, `SolidFuelRecipe` are JEI-only view types — confirm)
- [ ] Step 13 CompoundTag Optionals
- [ ] Step 19 AttachmentType, Step 20 INBTSerializable — check if used

## Known risks specific to this mod (all now resolved)

- ~~Generators are built from config JSON at construction time; registration happens inside
  `GeneratorCreator.create`~~ — this DID break: plain `register` does not set the block id in 26.1.
  Now on `registerBlock`/`registerItem`. Building generators from config in the mod constructor
  still works fine.
- ~~`IModFile.findResource` config restore~~ — rewritten on `JarContents.visitContent`, verified.
- ~~Data maps are synced; check `DataMapType` API~~ — unchanged, they load.
- ~~The 1.7.0 `neoforge:item_exists` conditions must survive the port~~ — they did not survive
  intact; see the loot-table condition trap at the top.

## Dependencies needing 26.1.2 file IDs

Resolved: **jei** -> `8755208`, **productivebees** -> `8756739` (added as `localRuntime`, since no
mod code references it — it exists purely to give runtime testing a real energy consumer).
productivelib does NOT need its own entry: pbees `jarJar`s it and FML extracts it
(`productivebees-...jar > productivelib-26.1.2-0.4.8.jar`).

Still commented out in `build.gradle`, pending upstream 26.1.2 builds:
jade, pipez, productive-metalworks, powah-rearchitected, cloth-config, guideme.

Resolve with the cfwidget proxy (no API key):
`curl -s https://api.cfwidget.com/minecraft/mc-mods/<slug>` then filter `files[]` for a `versions`
array containing both `26.1.2` and `NeoForge`, and take the highest `id`.
