package lykrast.meetyourfight.entity;

import java.util.EnumSet;
import java.util.List;

import lykrast.meetyourfight.MeetYourFight;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.StationaryAttack;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.entity.movement.VexMovementController;
import lykrast.meetyourfight.registry.ModEntities;
import lykrast.meetyourfight.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RosalyneEntity extends BossEntity {
	//Phase is lowest 3 bits, rest is animation 0xAAAAAPPP
	private static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(RosalyneEntity.class, EntityDataSerializers.BYTE);
	public static final int ENCASED = 0, BREAKING_OUT = 1, PHASE_1 = 2, SUMMONING = 3, PHASE_2 = 4, MADDENING = 5, PHASE_3 = 6;
	public static final int ANIM_NEUTRAL = 0, ANIM_ARM_UP = 1, ANIM_ARM_OUT = 2, ANIM_ARM_IN = 3;
	private static final int PHASE_MASK = 0b111, ANIMATION_MASK = ~PHASE_MASK;
	private final TargetingConditions spiritCountTargeting = TargetingConditions.forNonCombat().range(32).ignoreLineOfSight().ignoreInvisibilityTesting();
	
	public int attackCooldown;
	private int phase;
	
	public int clientAnim, prevAnim, animProg, animDur;
	
	public RosalyneEntity(EntityType<? extends RosalyneEntity> type, Level worldIn) {
		super(type, worldIn);
		moveControl = new VexMovementController(this);
		xpReward = 200;
		phase = 0;
		clientAnim = ANIM_NEUTRAL;
		prevAnim = ANIM_NEUTRAL;
		animProg = 1;
		animDur = 1;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PhaseTransition(this));
		goalSelector.addGoal(2, new MainAttack(this));
		goalSelector.addGoal(7, new MoveFrontOfTarget(this, 0.5));
		goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25));
		goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 600)
        		.add(Attributes.ARMOR, 10).add(Attributes.ARMOR_TOUGHNESS, 4)
        		.add(Attributes.KNOCKBACK_RESISTANCE, 1)
        		.add(Attributes.ATTACK_DAMAGE, 24)
        		.add(Attributes.FOLLOW_RANGE, 64);
    }

	@Override
	public void move(MoverType typeIn, Vec3 pos) {
		super.move(typeIn, pos);
		checkInsideBlocks();
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		noPhysics = false;
		setNoGravity(true);
		
		if (level.isClientSide()) {
			int newanim = getAnimation();
			if (clientAnim != newanim) {
				prevAnim = clientAnim;
				clientAnim = newanim;
				animProg = 0;
				animDur = 5;
			}
			else if (animProg < animDur) animProg++;
		}
	}
	
	public float getAnimProgress(float partial) {
		return Mth.clamp((animProg + partial) / animDur, 0, 1);
	}
	
	public static void spawn(Player player, Level world) {
		RandomSource rand = player.getRandom();
		RosalyneEntity dame = ModEntities.ROSALYNE.get().create(world);
		dame.moveTo(player.getX() + rand.nextInt(5) - 2, player.getY() + rand.nextInt(3) + 3, player.getZ() + rand.nextInt(5) - 2, rand.nextFloat() * 360 - 180, 0);
		dame.attackCooldown = 100;
		if (!player.getAbilities().instabuild) dame.setTarget(player);
		dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));

		dame.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
		world.addFreshEntity(dame);
		dame.createSpirits();
	}
	
	private void createSpirits() {
		for (int i = 0; i < 4; i++) {
			RoseSpiritEntity spirit = ModEntities.ROSE_SPIRIT.get().create(level);
			spirit.moveTo(getX() + (i/2)*4 - 2, getY(), getZ() + (i%2)*4 - 2);
			spirit.setOwner(this);
			spirit.attackCooldown = 80 + 60*i;
			spirit.finalizeSpawn((ServerLevel)level, level.getCurrentDifficultyAt(blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
			level.addFreshEntity(spirit);
		}
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source != DamageSource.OUT_OF_WORLD && getPhase() != PHASE_1 && getPhase() != PHASE_3) {
			if (amount > 0) playSound(SoundEvents.ANVIL_LAND, 1, 1);
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(STATUS, (byte)0);
	}
	
	public int getPhase() {
		return entityData.get(STATUS) & PHASE_MASK;
	}
	
	public void setPhase(int phase) {
		int anim = entityData.get(STATUS) & ANIMATION_MASK;
		entityData.set(STATUS, (byte)(anim | phase));
	}
	
	public int getAnimation() {
		return (entityData.get(STATUS) & ANIMATION_MASK) >> 3;
	}
	
	public void setAnimation(int anim) {
		int phase = entityData.get(STATUS) & PHASE_MASK;
		entityData.set(STATUS, (byte)((anim << 3) | phase));
	}
	
	public void swing() {
        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2, 0.2, 2))) {
        	if (!(entity instanceof RosalyneEntity) && !(entity instanceof RoseSpiritEntity) && entity.isAlive()) doHurtTarget(entity);
        }
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, 1);
        setAnimation(getAnimation() == ANIM_ARM_OUT ? ANIM_ARM_IN : ANIM_ARM_OUT);
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		if ((phase == ENCASED || phase == PHASE_2) && tickCount % 20 == 0) {
			List<RoseSpiritEntity> list = level.getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32));
			for (RoseSpiritEntity spirit : list) {
				spirit.setOwner(this);
			}
			if (!list.isEmpty()) setHealth(getHealth() + 1);
		}
	}
	
	@Override
	public void customServerAiStep() {
		if (attackCooldown > 0) attackCooldown--;
		if (phase != getPhase()) phase = getPhase();
		//Start phase transitions
		if ((phase == ENCASED || phase == PHASE_2) && tickCount % 10 == 0) {
			if (level.getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32)).isEmpty()) {
				if (phase == ENCASED) {
					setPhase(BREAKING_OUT);
					phase = BREAKING_OUT;
				}
				else if (phase == PHASE_2) {
					setPhase(MADDENING);
					phase = MADDENING;
				}
			}
		}
		else if (phase == PHASE_1 && getHealth() < getMaxHealth() / 2) {
			setPhase(SUMMONING);
			phase = SUMMONING;
		}
		super.customServerAiStep();
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("Cooldown")) attackCooldown = compound.getInt("Cooldown");
		if (compound.contains("Phase")) setPhase(compound.getInt("Phase"));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("Cooldown", attackCooldown);
		compound.putInt("Phase", getPhase());
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	@Override
	protected SoundEvent getMusic() {
		return ModSounds.musicMagnum.get();
	}
	
	@Override
	protected ResourceLocation getDefaultLootTable() {
		return MeetYourFight.rl("rosalyne");
	}
	
	private static class MainAttack extends Goal {
		private RosalyneEntity rosalyne;
		private int timer, swingsLeft, attackPhase;
		private double holdx, holdy, holdz;
		//attackPhase 0 = approaching, 1 = preparing to swing, 2 = swinging
		
		public MainAttack(RosalyneEntity rosalyne) {
			this.rosalyne = rosalyne;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}
		
		@Override
		public void start() {
			timer = 100;
			swingsLeft = 3;
			if (rosalyne.phase == PHASE_2) swingsLeft = 4 + rosalyne.random.nextInt(2);
			else if (rosalyne.phase == PHASE_3) swingsLeft = 8 + rosalyne.random.nextInt(5);
			attackPhase = 0;
			LivingEntity target = rosalyne.getTarget();
			rosalyne.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 4);
		}
		
		@Override
		public void tick() {
			timer--;
			LivingEntity target = rosalyne.getTarget();
			//Approaching target
			if (attackPhase == 0) {
				if (timer <= 0 || rosalyne.distanceToSqr(target) < 2) {
					holdx = rosalyne.getX();
					holdy = target.getY();
					holdz = rosalyne.getZ();
					rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
					attackPhase = 1;
					timer = 25;
					rosalyne.setAnimation(ANIM_ARM_OUT);
				}
				else {
					rosalyne.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 4);
				}
			}
			//Preparing to swing
			else if (attackPhase == 1) {
				rosalyne.moveControl.setWantedPosition(holdx, holdy, holdz, 4);
				if (timer <= 0) {
					attackPhase = 2;
					rosalyne.swing();
					swingsLeft--;
					timer = rosalyne.phase == PHASE_3 ? 12 : 20;
				}
			}
			//Swinging wildly
			else {
				rosalyne.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 0.5);
				if (timer <= 0) {
					if (swingsLeft > 0) {
						rosalyne.swing();
						swingsLeft--;
						timer = rosalyne.phase == PHASE_3 ? 12 : 20;
					}
				}
			}
		}
		
		@Override
		public void stop() {
			rosalyne.attackCooldown = 60 + rosalyne.random.nextInt(21);
			if (rosalyne.phase == PHASE_3) rosalyne.attackCooldown -= 40;
			rosalyne.setAnimation(ANIM_NEUTRAL);
		}

		@Override
		public boolean canUse() {
			return (rosalyne.phase == PHASE_1 || rosalyne.phase == PHASE_2 || rosalyne.phase == PHASE_3) && rosalyne.getTarget() != null && rosalyne.getTarget().isAlive() && rosalyne.attackCooldown <= 0;
		}
		
		@Override
		public boolean canContinueToUse() {
			return canUse() && (swingsLeft > 0 || timer > 0);
		}
		
	}
	
	private static class PhaseTransition extends StationaryAttack {
		private RosalyneEntity rosalyne;
		private int timer;

		public PhaseTransition(RosalyneEntity rosalyne) {
			super(rosalyne);
			this.rosalyne = rosalyne;
		}
		
		@Override
		public void start() {
			super.start();
			timer = 60;
		}

		@Override
		public void tick() {
			super.tick();
			timer--;
			if (timer <= 0) {
				switch (rosalyne.phase) {
					case BREAKING_OUT:
						rosalyne.setPhase(PHASE_1);
						rosalyne.level.explode(rosalyne, rosalyne.getX(), rosalyne.getY(), rosalyne.getZ(), 6, Explosion.BlockInteraction.NONE);
						break;
					case SUMMONING:
						rosalyne.setPhase(PHASE_2);
						rosalyne.createSpirits();
						break;
					case MADDENING:
						rosalyne.setPhase(PHASE_3);
						break;
				}
				rosalyne.attackCooldown = 100;
			}
		}

		@Override
		public boolean canUse() {
			return rosalyne.phase == BREAKING_OUT || rosalyne.phase == SUMMONING || rosalyne.phase == MADDENING;
		}
		
	}

}
