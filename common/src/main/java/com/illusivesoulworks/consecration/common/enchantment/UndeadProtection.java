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

package com.illusivesoulworks.consecration.common.enchantment;
import com.illusivesoulworks.consecration.api.ConsecrationApi;
import com.illusivesoulworks.consecration.common.registry.ConsecrationRegistry;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
/** Matches the original protection rule, including undead configured by other mods. */
public record UndeadProtection() implements LootItemCondition {
 public static final MapCodec<UndeadProtection> CODEC = MapCodec.unit(new UndeadProtection());
 @Override public LootItemConditionType getType() { return ConsecrationRegistry.UNDYING_ATTACKER.get(); }
 @Override public Set<LootContextParam<?>> getReferencedContextParams() { return Set.of(LootContextParams.DAMAGE_SOURCE); }
 @Override public boolean test(LootContext context) {
  var source = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
  return source != null && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
      && source.getEntity() instanceof LivingEntity attacker
      && ConsecrationApi.getInstance().getUndying(attacker).isPresent();
 }
}
