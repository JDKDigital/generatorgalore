package cy.jdkdigital.generatorgalore;

import com.mojang.logging.LogUtils;
import cy.jdkdigital.generatorgalore.common.container.GeneratorScreen;
import cy.jdkdigital.generatorgalore.registry.GeneratorRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(GeneratorGalore.MODID)
public class GeneratorGalore
{
    public static final String MODID = "generatorgalore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GeneratorGalore.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GeneratorGalore.MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, GeneratorGalore.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, GeneratorGalore.MODID);

    public GeneratorGalore()
    {
        /**
         * Add tooltip with info
         * Add info to gen screen
         * Add culinary generator
         * Add measurement of current power drain
         */
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GeneratorRegistry.discoverGenerators();

        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CONTAINER_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                GeneratorRegistry.generators.forEach((resourceLocation, generatorObject) -> {
                    MenuScreens.register(generatorObject.getMenuType().get(), GeneratorScreen::new);
                    ItemBlockRenderTypes.setRenderLayer(generatorObject.getBlockSupplier().get(), RenderType.cutoutMipped());
                });
            });
        }
    }
}
