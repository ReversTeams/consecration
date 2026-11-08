package com.illusivesoulworks.consecration.client;
import com.illusivesoulworks.consecration.common.entity.FireArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
public class FireArrowRenderer extends ArrowRenderer<FireArrowEntity> {
 public FireArrowRenderer(EntityRendererProvider.Context context) { super(context); }
 @Override public ResourceLocation getTextureLocation(FireArrowEntity entity) {
  return ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
 }
}
