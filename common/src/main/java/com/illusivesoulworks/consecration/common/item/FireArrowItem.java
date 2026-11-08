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

package com.illusivesoulworks.consecration.common.item;

import com.illusivesoulworks.consecration.common.entity.FireArrowEntity;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FireArrowItem extends ArrowItem {

  public FireArrowItem() {
    super(new Item.Properties());
  }

  @Override
  public net.minecraft.world.entity.projectile.Projectile asProjectile(Level level, net.minecraft.core.Position pos, ItemStack stack, net.minecraft.core.Direction direction) {
    var arrow = new FireArrowEntity(level, pos.x(), pos.y(), pos.z());
    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
    return arrow;
  }

  @Nonnull
  @Override
  public AbstractArrow createArrow(@Nonnull Level worldIn, @Nonnull ItemStack stack,
                                   @Nonnull LivingEntity shooter, @javax.annotation.Nullable ItemStack weapon) {
    return new FireArrowEntity(worldIn, shooter, stack, weapon);
  }
}
