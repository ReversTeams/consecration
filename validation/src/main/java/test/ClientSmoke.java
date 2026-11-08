package test;
import com.illusivesoulworks.consecration.api.ConsecrationApi;
import com.illusivesoulworks.consecration.common.entity.FireArrowEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="port_smoke", value=Dist.CLIENT)
public class ClientSmoke {
 private static int ticks; private static volatile int id=-1; private static boolean done;
 @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
  var mc=Minecraft.getInstance();
  if(done || mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
  ticks++;
  if(ticks==20)mc.getSingleplayerServer().execute(()->{
   var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();
   var zombie=EntityType.ZOMBIE.create(player.serverLevel());
   zombie.setPos(player.getX()+2,player.getY(),player.getZ());zombie.setNoAi(true);zombie.setPersistenceRequired();
   zombie.setCustomName(Component.literal("Port payload test"));player.serverLevel().addFreshEntity(zombie);id=zombie.getId();
   ConsecrationApi.getInstance().getUndying(zombie).orElseThrow().setVulnerableDuration(60);
  });
  if(ticks==70)mc.getSingleplayerServer().execute(()->{
   var zombie=(net.minecraft.world.entity.LivingEntity)mc.getSingleplayerServer().overworld().getEntity(id);
   if(zombie!=null)ConsecrationApi.getInstance().getUndying(zombie).orElseThrow().setVulnerableDuration(60);
  });
  if(ticks>=100) {
   done=true;
   try {
    var entity=mc.level.getEntity(id);
    if(!(entity instanceof net.minecraft.world.entity.LivingEntity living) || !ConsecrationApi.getInstance().getUndying(living).orElseThrow().isVulnerable())throw new AssertionError("Vulnerability payload did not reach client");
    var arrow=new FireArrowEntity(com.illusivesoulworks.consecration.common.registry.ConsecrationRegistry.FIRE_ARROW_TYPE.get(),mc.level);
    if(mc.getEntityRenderDispatcher().getRenderer(arrow)==null)throw new AssertionError("Missing fire arrow renderer");
    System.out.println("PORT_CLIENT_SUCCESS world entered, vulnerability payload received, fire arrow renderer loaded");
    java.nio.file.Files.writeString(java.nio.file.Path.of("port-client-result.txt"),"PASS integrated world, payload round trip, renderer");
   } catch(Throwable t) {t.printStackTrace();System.out.println("PORT_CLIENT_FAILURE "+t);}
   mc.stop();
  }
 }
}
