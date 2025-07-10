package cy.jdkdigital.generatorgalore;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import cy.jdkdigital.generatorgalore.common.conditions.GeneratorExistsCondition;
import cy.jdkdigital.generatorgalore.common.datamap.FluidFuelMap;
import cy.jdkdigital.generatorgalore.common.datamap.SolidFuelMap;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import cy.jdkdigital.generatorgalore.util.GeneratorUtil;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;

@Mod(GeneratorGalore.MODID)
public class GeneratorGalore
{
    public static final String MODID = "generatorgalore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, GeneratorGalore.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GeneratorGalore.MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(Registries.MENU, GeneratorGalore.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GeneratorGalore.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, GeneratorGalore.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, GeneratorGalore.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, GeneratorGalore.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GeneratorGalore.MODID);
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MODID);

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(MODID, () -> {
        return CreativeModeTab.builder()
                .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MODID, "iron_generator"))))
                .title(Component.literal("Generator Galore"))
                .build();
    });
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<GeneratorExistsCondition>> GENERATOR_EXISTS_CONDITION = CONDITION_CODECS.register("generator_exists", () -> GeneratorExistsCondition.CODEC);
    public static final DataMapType<Block, FluidFuelMap> FLUID_FUEL_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(MODID, "fluid_fuel_map"), Registries.BLOCK, FluidFuelMap.CODEC).synced(FluidFuelMap.CODEC, false).build();
    public static final DataMapType<Block, SolidFuelMap> SOLID_FUEL_MAP = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(MODID, "solid_fuel_map"), Registries.BLOCK, SolidFuelMap.CODEC).synced(SolidFuelMap.CODEC, false).build();

    public GeneratorGalore(IEventBus modEventBus, ModContainer modContainer) {
        GeneratorRegistry.discoverGenerators();

        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CONTAINER_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        CONDITION_CODECS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MODID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTab().equals(TAB.get())) {
                for (DeferredHolder<Item, ? extends Item> item: GeneratorGalore.ITEMS.getEntries()) {
                    event.accept(item.get());
                }
            }
        }

        @SubscribeEvent
        private static void registerDataMap(final RegisterDataMapTypesEvent event) {
            event.register(FLUID_FUEL_MAP);
            event.register(SOLID_FUEL_MAP);
        }

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            GeneratorRegistry.generators.values().forEach(generatorObject -> {
                event.registerBlockEntity(
                        Capabilities.ItemHandler.BLOCK,
                        generatorObject.getBlockEntityType().get(),
                        (myBlockEntity, side) -> myBlockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ? null : myBlockEntity.inventoryHandler
                );
                event.registerBlockEntity(
                        Capabilities.EnergyStorage.BLOCK,
                        generatorObject.getBlockEntityType().get(),
                        (myBlockEntity, side) -> myBlockEntity.energyHandler
                );
                event.registerBlockEntity(
                        Capabilities.FluidHandler.BLOCK,
                        generatorObject.getBlockEntityType().get(),
                        (myBlockEntity, side) -> myBlockEntity.generator.getFuelType().equals(GeneratorUtil.FuelType.FLUID) ? myBlockEntity.fluidInventory : null
                );
            });
        }
    }
}
