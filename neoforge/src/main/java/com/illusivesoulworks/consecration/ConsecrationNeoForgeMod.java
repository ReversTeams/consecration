/*
 * Copyright (C) 2017-2023 Illusive Soulworks
 *
 * Consecration is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Consecration is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Consecration.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.consecration;

import com.illusivesoulworks.consecration.api.ConsecrationImc;
import com.illusivesoulworks.consecration.api.IUndying;
import com.illusivesoulworks.consecration.common.ConsecrationEvents;
import com.illusivesoulworks.consecration.common.capability.UndyingCapability;
import com.illusivesoulworks.consecration.common.config.ConsecrationConfig;
import com.illusivesoulworks.consecration.common.impl.HolySources;
import com.illusivesoulworks.consecration.common.integration.AbstractCompatibilityModule;
import com.illusivesoulworks.consecration.common.network.ConsecrationNetwork;
import com.illusivesoulworks.consecration.common.registry.ConsecrationRegistry;
import com.illusivesoulworks.consecration.common.trigger.SmiteTrigger;
import com.mojang.logging.LogUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.slf4j.Logger;

@Mod(ConsecrationConstants.MOD_ID)
public class ConsecrationNeoForgeMod {

  public static final Logger LOGGER = LogUtils.getLogger();

  public static final Map<String, Class<? extends AbstractCompatibilityModule>> MODULES =
      new HashMap<>();
  public static final List<AbstractCompatibilityModule> ACTIVE_MODULES = new ArrayList<>();

  static {
    MODULES.put("silentgear", com.illusivesoulworks.consecration.common.integration.SilentGearModule.class);
    MODULES.put("werewolves", com.illusivesoulworks.consecration.common.integration.WerewolvesModule.class);
  }

  public ConsecrationNeoForgeMod(IEventBus eventBus) {
    ConsecrationConfig.setup();
    ConsecrationCommonMod.setup();
    UndyingCapability.TYPES.register(eventBus);
    eventBus.addListener(this::setup);
    eventBus.addListener(ConsecrationNetwork::register);
    NeoForge.EVENT_BUS.addListener(this::brewing);
    eventBus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Loading e) -> { if (e.getConfig().getSpec() == ConsecrationConfig.CONFIG_SPEC) HolySources.reloadConfigs(); });
    eventBus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Reloading e) -> { if (e.getConfig().getSpec() == ConsecrationConfig.CONFIG_SPEC) HolySources.reloadConfigs(); });
    NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.TagsUpdatedEvent e) -> com.illusivesoulworks.consecration.common.impl.UndeadTypes.init());
    eventBus.addListener(this::imcEnqueue);
    eventBus.addListener(this::imcProcess);
    eventBus.addListener(this::creativeTab);
    MODULES.forEach((id, module) -> {
      if (ModList.get().isLoaded(id)) {
        try {
          ACTIVE_MODULES.add(module.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
          LOGGER.error("Error adding module for mod " + id);
        }
      }
    });
  }

  private void setup(final FMLCommonSetupEvent evt) {
    NeoForge.EVENT_BUS.register(new UndyingCapability());
    NeoForge.EVENT_BUS.addListener(this::makeCampfireArrow);
  }

  private void brewing(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event) {
    event.getBuilder().addMix(Potions.AWKWARD, Items.GOLDEN_APPLE, ConsecrationRegistry.HOLY_POTION.asHolder());
    event.getBuilder().addMix(ConsecrationRegistry.HOLY_POTION.asHolder(), Items.GLOWSTONE_DUST, ConsecrationRegistry.STRONG_HOLY_POTION.asHolder());
  }

  private void imcEnqueue(final InterModEnqueueEvent evt) {
    ACTIVE_MODULES.forEach(AbstractCompatibilityModule::enqueueImc);
  }

  private void imcProcess(final InterModProcessEvent evt) {
    evt.getIMCStream().forEach(imcMessage -> {
      Object message = imcMessage.messageSupplier().get();
      String method = imcMessage.method();

      if (message instanceof BiFunction) {

        if (method.equals(ConsecrationImc.HOLY_ATTACK.getId())) {
          HolySources.addHolyAttack((BiFunction<LivingEntity, DamageSource, Boolean>) message);
        } else if (method.equals(ConsecrationImc.HOLY_PROTECTION.getId())) {
          HolySources.addHolyProtection((BiFunction<LivingEntity, DamageSource, Integer>) message);
        }
      }
    });
  }


  private void makeCampfireArrow(final PlayerInteractEvent.RightClickBlock evt) {
    ConsecrationEvents.createCampfireArrow(evt.getEntity(), evt.getItemStack(), evt.getPos(),
        (player, stack) -> ItemHandlerHelper.giveItemToPlayer(player, stack,
            player.getInventory().selected));
  }

  private void creativeTab(final BuildCreativeModeTabContentsEvent evt) {
    ResourceKey<CreativeModeTab> tab = evt.getTabKey();

    if (tab == CreativeModeTabs.COMBAT) {
      evt.accept(ConsecrationRegistry.FIRE_STICK.get());
      evt.accept(ConsecrationRegistry.FIRE_ARROW.get());
    }
  }
}
