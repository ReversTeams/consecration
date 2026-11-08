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

package com.illusivesoulworks.consecration.common.entity;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.illusivesoulworks.consecration.common.registry.ConsecrationRegistry;

public class FireArrowEntity extends AbstractArrow {

  public FireArrowEntity(EntityType<? extends AbstractArrow> type, Level world) {
    super(type, world);
  }

  public FireArrowEntity(Level worldIn, double x, double y, double z) {
    super(ConsecrationRegistry.FIRE_ARROW_TYPE.get(), x, y, z, worldIn, new ItemStack(ConsecrationRegistry.FIRE_ARROW.get()), null);
  }

  public FireArrowEntity(Level worldIn, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
    super(ConsecrationRegistry.FIRE_ARROW_TYPE.get(), shooter, worldIn, stack.copyWithCount(1), weapon);
  }

  @Override
  public void tick() {
    super.tick();
    this.igniteForSeconds(100);
  }

  @Override
  @Nonnull
  protected ItemStack getDefaultPickupItem() {
    return new ItemStack(ConsecrationRegistry.FIRE_ARROW.get());
  }
}
