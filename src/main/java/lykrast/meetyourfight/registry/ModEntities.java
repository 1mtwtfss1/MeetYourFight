package lykrast.meetyourfight.registry;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.SwampMineEntity;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MeetYourFight.MODID)
public class ModEntities {
	//Bosses
	public static final EntityType<BellringerEntity> BELLRINGER = EntityType.Builder
			.<BellringerEntity>of(BellringerEntity::new, MobCategory.MONSTER)
			.sized(0.6f, 1.95f).setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true)
			.build("");
	public static final EntityType<DameFortunaEntity> DAME_FORTUNA = EntityType.Builder
			.<DameFortunaEntity>of(DameFortunaEntity::new, MobCategory.MONSTER)
			.sized(0.6f, 2.325f).setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true)
			.build("");
	public static final EntityType<SwampjawEntity> SWAMPJAW = EntityType.Builder
			.<SwampjawEntity>of(SwampjawEntity::new, MobCategory.MONSTER)
			.sized(2.6f, 1.6f).setUpdateInterval(2).setTrackingRange(128).setShouldReceiveVelocityUpdates(true)
			.build("");
	
	//Projectiles
	public static final EntityType<ProjectileLineEntity> PROJECTILE_LINE = EntityType.Builder
			.<ProjectileLineEntity>of(ProjectileLineEntity::new, MobCategory.MISC)
			.sized(0.3125f, 0.3125f).setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
			.build("");
	public static final EntityType<SwampMineEntity> SWAMP_MINE = EntityType.Builder
			.<SwampMineEntity>of(SwampMineEntity::new, MobCategory.MISC)
			.sized(1, 1).setUpdateInterval(1).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
			.build("");

	@SubscribeEvent
	public static void regsiterEntities(final RegistryEvent.Register<EntityType<?>> event) {
		IForgeRegistry<EntityType<?>> reg = event.getRegistry();
		
		BELLRINGER.setRegistryName(MeetYourFight.MODID, "bellringer");
		reg.register(BELLRINGER);
		DAME_FORTUNA.setRegistryName(MeetYourFight.MODID, "dame_fortuna");
		reg.register(DAME_FORTUNA);
		SWAMPJAW.setRegistryName(MeetYourFight.MODID, "swampjaw");
		reg.register(SWAMPJAW);
		
		PROJECTILE_LINE.setRegistryName(MeetYourFight.MODID, "projectile_line");
		reg.register(PROJECTILE_LINE);
		SWAMP_MINE.setRegistryName(MeetYourFight.MODID, "swamp_mine");
		reg.register(SWAMP_MINE);
	}

	@SubscribeEvent
	public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
		event.put(BELLRINGER, BellringerEntity.createAttributes().build());
		event.put(DAME_FORTUNA, DameFortunaEntity.createAttributes().build());
		event.put(SWAMPJAW, SwampjawEntity.createAttributes().build());
	}
}
