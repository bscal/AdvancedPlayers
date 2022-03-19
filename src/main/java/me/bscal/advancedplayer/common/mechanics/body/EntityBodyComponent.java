package me.bscal.advancedplayer.common.mechanics.body;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class EntityBodyComponent
{
	public static final int HEAD = 0; //"Head";
	public static final int CHEST = 1;//"Chest";
	public static final int LEFT_ARM = 2; //"LArm";
	public static final int RIGHT_ARM = 3; //"RArm";
	public static final int GROIN = 4;//"Groin";
	public static final int LEFT_LEG = 5; //"LLeg";
	public static final int RIGHT_LEG = 6; //"RLeg";
	public static final int LEFT_FOOT = 7;//"LFoot";
	public static final int RIGHT_FOOT = 8;//"RFoot";
	public static final int MAX_PARTS = 9;

	public BodyPart[] BodyParts;
	public boolean IsDirty;

	protected final LivingEntity m_Provider;

	public EntityBodyComponent(@NotNull LivingEntity provider, int numOfBodyParts)
	{
		Objects.requireNonNull(provider, "EntityBody LivingEntity is null");
		if (numOfBodyParts < 1) throw new IllegalArgumentException("EntityBody does not have any body parts. numOfBodyParts");

		m_Provider = provider;
		BodyParts = new BodyPart[numOfBodyParts];
		CreateBodyParts();
		IsDirty = true;
	}

	public void Reset()
	{
		CreateBodyParts();
		IsDirty = true;
	}

	public abstract void CreateBodyParts();

	public abstract void Sync();

	public enum BodyPartTypes
	{
		Head, Chest, LeftArm, RightArm, Groin, LeftLeg, RightLeg, LeftFoot, RightFoot;
	}
}
