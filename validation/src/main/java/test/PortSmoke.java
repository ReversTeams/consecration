package test;
import com.illusivesoulworks.consecration.api.ConsecrationApi;
import com.illusivesoulworks.consecration.common.registry.ConsecrationRegistry;
import com.illusivesoulworks.consecration.common.entity.FireArrowEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.reversteam.weighted_armors.handler.PhysicsHandler;
import net.reversteam.weighted_armors.util.ArmorHelper;
import net.reversteam.weighted_armors.WeightClass;
import net.reversteam.demolition_order.ai.DemolitionGoal;
import net.reversteam.demolition_order.config.DemolitionConfig;
@Mod("port_smoke")
public class PortSmoke {
 private static int checks;
 private static void check(boolean pass, String name) {
  if (!pass) throw new AssertionError(name);
  checks++; System.out.println("PORT_TEST_PASS " + name);
 }
 public PortSmoke() { NeoForge.EVENT_BUS.addListener(this::test); }
 private void test(ServerStartedEvent event) {
  var server=event.getServer();
  try {
   var level=server.overworld(); var api=ConsecrationApi.getInstance();
   var zombie=EntityType.ZOMBIE.create(level); var cow=EntityType.COW.create(level);
   zombie.setPos(0,100,0); cow.setPos(3,100,0);
   level.addFreshEntity(zombie); level.addFreshEntity(cow);
   var player=FakePlayerFactory.getMinecraft(level);
   check(api.getUndying(zombie).isPresent(), "zombie receives undying attachment");
   check(api.getUndying(cow).isEmpty(), "living animal not empowered");
   var undying=api.getUndying(zombie).orElseThrow();
   check(undying.onDamaged(level.damageSources().playerAttack(player), 10)<10, "undead resists ordinary player damage");
   check(undying.onDamaged(api.causeHolyDamage(level.registryAccess()),10)==10 && undying.isVulnerable(), "holy damage opens vulnerability");
   check(undying.onDamaged(level.damageSources().playerAttack(player),10)==10, "vulnerable undead takes full damage");
   undying.setVulnerableDuration(7);
   CompoundTag saved=new CompoundTag(); zombie.save(saved);
   var restored=(net.minecraft.world.entity.LivingEntity)EntityType.loadEntityRecursive(saved,level,e->e);
   check(restored!=null && api.getUndying(restored).orElseThrow().getVulnerableDuration()==7, "undying attachment survives save and load");
   var enchant=level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.parse("consecration:undead_protection")));
   ItemStack chest=new ItemStack(Items.DIAMOND_CHESTPLATE);chest.enchant(enchant,4);cow.setItemSlot(EquipmentSlot.CHEST,chest);
   check(EnchantmentHelper.getDamageProtection(level,cow,level.damageSources().mobAttack(zombie))==8, "undead protection IV gives eight protection points");
   check(EnchantmentHelper.getDamageProtection(level,cow,level.damageSources().playerAttack(player))==0, "undead protection excludes living attackers");
   ItemStack awkward=PotionContents.createItemStack(Items.POTION,Potions.AWKWARD);
   check(server.potionBrewing().hasMix(awkward,new ItemStack(Items.GOLDEN_APPLE)), "holy potion brewing recipe loaded");
   ItemStack potion=server.potionBrewing().mix(new ItemStack(Items.GOLDEN_APPLE),awkward);
   check(potion.get(DataComponents.POTION_CONTENTS).potion().orElseThrow().value()==ConsecrationRegistry.HOLY_POTION.get(), "golden apple brews holy potion");
   check(level.getRecipeManager().byKey(ResourceLocation.parse("consecration:fire_arrow")).isPresent(), "fire arrow crafting recipe loaded");
   check(level.getRecipeManager().byKey(ResourceLocation.parse("consecration:fire_stick")).isPresent(), "fire stick crafting recipe loaded");
   var arrow=new FireArrowEntity(level,player,new ItemStack(ConsecrationRegistry.FIRE_ARROW.get()),new ItemStack(Items.BOW));
   check(arrow.getType()==ConsecrationRegistry.FIRE_ARROW_TYPE.get() && arrow.getOwner()==player, "fire arrow preserves entity type and shooter");
   arrow.tick();check(arrow.isOnFire(), "fire arrow ignites");
   ((com.illusivesoulworks.consecration.common.item.FireStickItem)ConsecrationRegistry.FIRE_STICK.get()).onLeftClickEntity(new ItemStack(ConsecrationRegistry.FIRE_STICK.get()),player,cow);
   check(cow.isOnFire(), "fire stick ignites target");
   check(!DemolitionConfig.ORDERS.isEmpty(), "demolition orders loaded");
   check(zombie.goalSelector.getAvailableGoals().stream().anyMatch(g->g.getGoal() instanceof DemolitionGoal), "demolition goal assigned to zombie");
   var targetPos=new net.minecraft.core.BlockPos(1,100,0);
   level.setBlockAndUpdate(targetPos,net.minecraft.world.level.block.Blocks.CRAFTING_TABLE.defaultBlockState());
   zombie.setPos(0.5,100,0.5);
   var goal=new DemolitionGoal(zombie,DemolitionConfig.ORDERS.getFirst());
   boolean can=false;for(int i=0;i<100&&!can;i++)can=goal.canUse();
   check(can,"demolition selects configured crafting table");
   for(int i=0;i<100&&goal.canContinueToUse();i++)goal.tick();
   check(level.getBlockState(targetPos).isAir(),"demolition destroys selected target");
   check(ArmorHelper.getWeight(new ItemStack(Items.IRON_CHESTPLATE))==WeightClass.HEAVY, "iron armor uses migrated heavy tag");
   player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));player.setOnGround(true);
   PhysicsHandler.onPlayerTick(new PlayerTickEvent.Post(player));
   check(player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ResourceLocation.parse("weighted_armors:speed"))!=null, "heavy armor changes movement speed");
   player.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);PhysicsHandler.onPlayerTick(new PlayerTickEvent.Post(player));
   check(player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(ResourceLocation.parse("weighted_armors:speed"))==null, "removing armor restores movement speed");
   System.out.println("PORT_TEST_SUCCESS checks="+checks);
   java.nio.file.Files.writeString(java.nio.file.Path.of("port-test-result.txt"), "PASS " + checks + " checks");
  } catch(Throwable t) {
   t.printStackTrace();System.out.println("PORT_TEST_FAILURE " + t);
   try { java.nio.file.Files.writeString(java.nio.file.Path.of("port-test-result.txt"), "FAIL " + t); } catch(Exception ignored) {}
  } finally { if(server.isDedicatedServer())server.halt(false); }
 }
}
