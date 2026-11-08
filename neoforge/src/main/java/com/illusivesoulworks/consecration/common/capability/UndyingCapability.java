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

package com.illusivesoulworks.consecration.common.capability;

import com.illusivesoulworks.consecration.ConsecrationConstants;
import com.illusivesoulworks.consecration.api.ConsecrationApi;
import com.illusivesoulworks.consecration.api.IUndying;
import com.illusivesoulworks.consecration.common.ConsecrationEvents;
import com.illusivesoulworks.consecration.common.impl.Undying;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class UndyingCapability {

  public static final net.neoforged.neoforge.registries.DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> TYPES =
      net.neoforged.neoforge.registries.DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES, "consecration");
  public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<Undying>> INSTANCE =
      TYPES.register("undying", () -> net.neoforged.neoforge.attachment.AttachmentType.builder(holder -> new Undying((LivingEntity) holder))
          .serialize(new net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, Undying>() {
            public Undying read(net.neoforged.neoforge.attachment.IAttachmentHolder holder, CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
              Undying data = new Undying((LivingEntity) holder); data.readTag(tag); return data;
            }
            public CompoundTag write(Undying data, net.minecraft.core.HolderLookup.Provider registries) { return data.writeTag(); }
          }).build());

  @SubscribeEvent
  public void livingUpdate(final net.neoforged.neoforge.event.tick.EntityTickEvent.Pre evt) {
    if (evt.getEntity() instanceof LivingEntity living) ConsecrationApi.getInstance().getUndying(living).ifPresent(IUndying::tick);
  }

  @SubscribeEvent
  public void potionAdded(final MobEffectEvent.Added evt) {
    ConsecrationApi.getInstance().getUndying(evt.getEntity())
        .ifPresent(undying -> undying.onEffectAdded(evt.getEffectInstance()));
  }

  @SubscribeEvent
  public void startTracking(final PlayerEvent.StartTracking evt) {

    if (evt.getTarget() instanceof LivingEntity livingEntity) {
      ConsecrationApi.getInstance().getUndying(livingEntity).ifPresent(IUndying::sync);
    }
  }

  @SubscribeEvent
  public void livingDamage(final LivingDamageEvent.Pre evt) {
    evt.setNewDamage(ConsecrationEvents.onDamaged(evt.getEntity(), evt.getSource(), evt.getNewDamage()));
  }

}
