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

package com.illusivesoulworks.consecration.platform.services;

import com.illusivesoulworks.consecration.ConsecrationConstants;
import com.illusivesoulworks.consecration.api.UndeadType;
import com.illusivesoulworks.consecration.common.registry.RegistryObject;
import com.illusivesoulworks.consecration.common.registry.RegistryProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public class NeoForgeRegistryService implements IRegistryService {

  private static final TagKey<EntityType<?>> UNDEAD =
      TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, "undead"));
  private static final TagKey<EntityType<?>> FIRE_RESISTANT =
      TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, "fire_resistant"));
  private static final TagKey<EntityType<?>> HOLY_RESISTANT =
      TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, "holy_resistant"));
  private static final TagKey<EntityType<?>> RESISTANT =
      TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, "resistant"));

  private static final TagKey<Item> HOLY_ITEMS =
      TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, ConsecrationConstants.Registry.HOLY));
  private static final TagKey<MobEffect> HOLY_EFFECTS =
      TagKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, ConsecrationConstants.Registry.HOLY));
  private static final TagKey<Enchantment> HOLY_ENCHANTMENTS =
      TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, ConsecrationConstants.Registry.HOLY));
  private static final TagKey<EntityType<?>> HOLY_ENTITIES =
      TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ConsecrationConstants.MOD_ID, ConsecrationConstants.Registry.HOLY));

  private static final ItemAbility SMITE_ACTION = ItemAbility.get("smite");

  @Override
  public <T> RegistryProvider<T> create(ResourceKey<? extends Registry<T>> resourceKey,
                                        String modId) {
    final var containerOpt = ModList.get().getModContainerById(modId);
    if (containerOpt.isEmpty()) {
      throw new NullPointerException("Cannot find mod container for id " + modId);
    }
    final var cont = containerOpt.get();
    if (cont instanceof FMLModContainer fmlModContainer) {
      final var register = DeferredRegister.create(resourceKey, modId);
      register.register(fmlModContainer.getEventBus());
      return new Provider<>(modId, register);
    } else {
      throw new ClassCastException("The container of the mod " + modId + " is not a FML one!");
    }
  }

  @Override
  public void processUndeadTypes(BiConsumer<EntityType<?>, UndeadType> biConsumer) {
    for (EntityType<?> entity : BuiltInRegistries.ENTITY_TYPE) {
      var holder = entity.builtInRegistryHolder();
      UndeadType type = holder.is(UNDEAD) ? UndeadType.DEFAULT : holder.is(FIRE_RESISTANT) ? UndeadType.FIRE_RESISTANT :
          holder.is(HOLY_RESISTANT) ? UndeadType.HOLY_RESISTANT : holder.is(RESISTANT) ? UndeadType.RESISTANT : UndeadType.NOT;
      biConsumer.accept(entity, type);
    }
  }

  @Override
  public ResourceLocation getKey(Item item) {
    return BuiltInRegistries.ITEM.getKey(item);
  }

  @Override
  public boolean isHolyTag(Item item) {
    return item.builtInRegistryHolder().is(HOLY_ITEMS);
  }

  @Override
  public boolean isHolyTag(EntityType<?> entity) {
    return entity.builtInRegistryHolder().is(HOLY_ENTITIES);
  }

  @Override
  public boolean isHolyTag(MobEffect mobEffect) {
    return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect).is(HOLY_EFFECTS);
  }

  @Override
  public boolean isHolyTag(Holder<Enchantment> enchantment) {
    return enchantment.is(HOLY_ENCHANTMENTS);
  }

  @Override
  public boolean canSmite(ItemStack stack) {
    return stack.canPerformAction(SMITE_ACTION);
  }

  private static class Provider<T> implements RegistryProvider<T> {
    private final String modId;
    private final DeferredRegister<T> registry;

    private final Set<RegistryObject<T>> entries = new HashSet<>();
    private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(entries);

    private Provider(String modId, DeferredRegister<T> registry) {
      this.modId = modId;
      this.registry = registry;
    }

    @Override
    public String getModId() {
      return modId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
      final var obj = registry.<I>register(name, supplier);
      final var ro = new RegistryObject<I>() {

        @Override
        public ResourceKey<I> getResourceKey() {
          return (ResourceKey<I>) (ResourceKey<?>) obj.getKey();
        }

        @Override
        public ResourceLocation getId() {
          return obj.getId();
        }

        @Override
        public I get() {
          return obj.get();
        }

        @Override
        public Holder<I> asHolder() {
          return (Holder<I>) (Holder<?>) obj;
        }
      };
      entries.add((RegistryObject<T>) ro);
      return ro;
    }

    @Override
    public Set<RegistryObject<T>> getEntries() {
      return entriesView;
    }
  }
}
