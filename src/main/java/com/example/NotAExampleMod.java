package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.util.TriState;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(NotAExampleMod.MODID)
public class NotAExampleMod {
    public static final String MODID = "notaexamplemod";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, ServerBossEvent> ACTIVE_BOSS_BARS = new ConcurrentHashMap<>();
    private void updateBossBarProgress(Level level, BlockPos pos, int newHighlander) {
        float progress = newHighlander / 3.0f;
        ACTIVE_BOSS_BARS.forEach((uuid, bossBar) -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null && player.level() == level) {
                int lightLevel = newHighlander * 5;
                bossBar.setName(Component.literal("光照等级: " + lightLevel));
                bossBar.setProgress(progress);
            }
        });
    }
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredItem<Item> RAINDROP_MUSHROOM = ITEMS.register(
            "raindrop_mushroom",
            () -> new Item(new Item.Properties()
                    .durability(0)
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
            )
    );

    public static final DeferredItem<Item> INCREMENT_WAND = ITEMS.register(
            "increment_wand",
            () -> new Item(new Item.Properties()
                    .durability(0)
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
            )
    );

    public static final DeferredItem<Item> DECREMENT_ROD = ITEMS.register(
            "decrement_rod",
            () -> new Item(new Item.Properties()
                    .durability(0)
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
            )
    );

    public static final DeferredBlock<Block> LAMB_LAMP_BLOCK = BLOCKS.register(
            "lamb_lamp",
            () -> new CustomLampBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .lightLevel(state -> 0)
                            .requiresCorrectToolForDrops()
            )
    );
    public static final DeferredItem<BlockItem> LAMB_LAMP_ITEM = ITEMS.registerSimpleBlockItem(
            "lamb_lamp",
            LAMB_LAMP_BLOCK
    );

    public static final DeferredItem<Item> SHEPHERD_FLAIL_ITEM = ITEMS.register(
            "shepherd_flail",
            () -> new Item(new Item.Properties()
                    .durability(0)
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)
            )
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(
            "example_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.notaexamplemod"))
                    .icon(() -> LAMB_LAMP_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(LAMB_LAMP_ITEM.get());
                        output.accept(SHEPHERD_FLAIL_ITEM.get());
                        output.accept(INCREMENT_WAND.get());
                        output.accept(DECREMENT_ROD.get());
                        output.accept(RAINDROP_MUSHROOM.get());
                    })
                    .build()
    );

    public NotAExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("模组初始化完成");
    }

    //右击时的逻辑（九层if宝塔）
    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

        if (stack.getItem() == RAINDROP_MUSHROOM.get() && state.is(LAMB_LAMP_BLOCK.get())) {
            int highlander = state.getValue(CustomLampBlock.HIGHLANDER);
            float progress = highlander / 3.0f;
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                UUID playerId = serverPlayer.getUUID();
                ServerBossEvent bossBar = ACTIVE_BOSS_BARS.compute(playerId, (key, existingBar) -> {
                    if (existingBar != null) {
                        existingBar.removeAllPlayers();
                        return null;
                    } else {
                        int lightLevel = highlander * 5;
                        ServerBossEvent newBar = new ServerBossEvent(
                                Component.literal("光照等级: " + lightLevel),
                                BossEvent.BossBarColor.PURPLE,
                                BossEvent.BossBarOverlay.PROGRESS
                        );
                        newBar.setProgress(progress);
                        newBar.addPlayer(serverPlayer);
                        newBar.setVisible(true);
                        return newBar;
                    }
                });
            }
            event.setCanceled(true);
        }

        if (state.is(LAMB_LAMP_BLOCK.get())) {
            Item tool = stack.getItem();
            int current = state.getValue(CustomLampBlock.HIGHLANDER);
            boolean modified = false;
            int newValue = current;
            if (tool == SHEPHERD_FLAIL_ITEM.get()) {
                if (!level.isClientSide) {
                    BlockHitResult hitResult = (BlockHitResult) event.getHitVec();
                    Vec3 hitVec = hitResult.getLocation();
                    double relY = hitVec.y - pos.getY();

                    if (relY >= 0.0625 && relY < 0.25) {
                        newValue = 1;
                    } else if (relY >= 0.25 && relY < 0.75) {
                        newValue = 2;
                    } else if (relY >= 0.75 && relY <= 0.9375) {
                        newValue = 3;
                    }

                    if (current != newValue) {
                        level.setBlock(pos, state.setValue(CustomLampBlock.HIGHLANDER, newValue), Block.UPDATE_ALL);
                        LOGGER.info(" 摸摸 交互于位置 {}，当前值：{}", pos.toShortString(), newValue);
                        modified = true;
                    }
                }
            }
            else if (tool == INCREMENT_WAND.get()) {
                if (!level.isClientSide) {
                    newValue = Mth.clamp(current + 1, 0, 3);
                    if (current != newValue) {
                        level.setBlock(pos, state.setValue(CustomLampBlock.HIGHLANDER, newValue), Block.UPDATE_ALL);
                        LOGGER.info(" 高松灯 交互于位置 {}，当前值：{}", pos.toShortString(), newValue);
                        modified = true;
                    }
                }
            }
            else if (tool == DECREMENT_ROD.get()) {
                if (!level.isClientSide) {
                    newValue = Mth.clamp(current - 1, 0, 3);
                    if (current != newValue) {
                        level.setBlock(pos, state.setValue(CustomLampBlock.HIGHLANDER, newValue), Block.UPDATE_ALL);
                        LOGGER.info(" 低松灯 交互于位置 {}，当前值：{}", pos.toShortString(), newValue);
                        modified = true;
                    }
                }
            }

            if (modified) {

                BlockState newState = state.setValue(CustomLampBlock.HIGHLANDER, newValue);
                int newHighlander = newState.getValue(CustomLampBlock.HIGHLANDER);
                updateBossBarProgress(level, pos, newHighlander); // 实时更新
                level.setBlock(pos, newState, Block.UPDATE_ALL | Block.UPDATE_CLIENTS);

                if (!level.isClientSide) {
                    level.sendBlockUpdated(pos, state, newState, 3);
                }

                event.setCanceled(true);
                event.setUseBlock(TriState.FALSE);
                event.setUseItem(TriState.FALSE);
            }
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("客户端初始化完成");
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class PlayerEvents {
        @SubscribeEvent
        public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                UUID playerId = serverPlayer.getUUID();
                if (ACTIVE_BOSS_BARS.containsKey(playerId)) {
                    ACTIVE_BOSS_BARS.get(playerId).removeAllPlayers();
                    ACTIVE_BOSS_BARS.remove(playerId);
                }
            }
        }

        @SubscribeEvent
        public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                UUID playerId = serverPlayer.getUUID();
                if (ACTIVE_BOSS_BARS.containsKey(playerId)) {
                    ACTIVE_BOSS_BARS.get(playerId).removeAllPlayers();
                    ACTIVE_BOSS_BARS.remove(playerId);
                }
            }
        }
    }
}