package me.bscal.advancedplayer.common.mechanics.health;

import me.bscal.advancedplayer.common.mechanics.body.EntityBodyComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public class HealthBodyComponent extends EntityBodyComponent
{

	public int HeartRate;

	public HealthBodyComponent(@NotNull LivingEntity provider)
	{
		super(provider, BodyPartTypes.values().length);
	}

	public void DamageBodyPart(BodyPartTypes type, float damage)
	{

	}

	@Override
	public void CreateBodyParts()
	{
		BodyParts[BodyPartTypes.Head.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.Chest.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.LeftArm.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.RightArm.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.Groin.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.LeftLeg.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.RightLeg.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.LeftFoot.ordinal()] = new HealthBodyPart();
		BodyParts[BodyPartTypes.RightFoot.ordinal()] = new HealthBodyPart();
	}

	@Override
	public void Sync()
	{
	}

	@Override
	public void writeToNbt(NbtCompound nbt)
	{
		super.writeToNbt(nbt);
		nbt.putInt("HeartRate", HeartRate);
	}

	@Override
	public void readFromNbt(NbtCompound nbt)
	{
		super.readFromNbt(nbt);
		HeartRate = nbt.getInt("HeartRate");
	}
}
