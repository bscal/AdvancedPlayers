package me.bscal.advancedplayer.common.mechanics.body;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import me.bscal.advancedplayer.common.components.ComponentManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class EntityBodyComponent implements ComponentV3, AutoSyncedComponent, ServerTickingComponent
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

	public float CoreBodyValue;
	public final float MinCoreBodyValue, MaxCoreBodyValue, DefaultBodyValue;
	public final BodyPart[] BodyParts;
	public boolean IsDirty;

	protected final LivingEntity m_Provider;

	public EntityBodyComponent(@NotNull LivingEntity provider, int numOfBodyParts, float defaultBodyValue, float minCoreBodyValue, float maxCoreBodyValue)
	{
		Objects.requireNonNull(provider, "EntityBody LivingEntity is null");
		if (numOfBodyParts < 1) throw new IllegalArgumentException("EntityBody does not have any body parts. numOfBodyParts");
		if (defaultBodyValue < minCoreBodyValue || defaultBodyValue > maxCoreBodyValue)
			throw new IllegalArgumentException("EntityBody defaultBodyValue is outside min/max bounds.");
		if (minCoreBodyValue > maxCoreBodyValue) throw new IllegalArgumentException("EntityBody minCoreBodyValue is > maxCoreBodyValue");

		m_Provider = provider;
		CoreBodyValue = defaultBodyValue;
		DefaultBodyValue = defaultBodyValue;
		MinCoreBodyValue = minCoreBodyValue;
		MaxCoreBodyValue = maxCoreBodyValue;
		BodyParts = new BodyPart[numOfBodyParts];
		PopulateBodyMap();
		IsDirty = true;
	}

	public void SetCoreValueClampAndSync(float value)
	{
		CoreBodyValue = MathHelper.clamp(value, MinCoreBodyValue, MaxCoreBodyValue);
		IsDirty = true;
	}

	public void Reset()
	{
		CoreBodyValue = DefaultBodyValue;
		PopulateBodyMap();
		IsDirty = true;
	}

	public abstract void PopulateBodyMap();

	@Override
	public void writeToNbt(NbtCompound nbt)
	{
		nbt.putFloat("CoreBodyValue", CoreBodyValue);
		NbtList parts = new NbtList();
		for (var part : BodyParts)
			parts.add(part.ToNbt());
		nbt.put("BodyParts", parts);
	}

	@Override
	public void readFromNbt(NbtCompound nbt)
	{
		CoreBodyValue = nbt.getFloat("CoreBodyValue");
		// TODO better safer serialization
		var nbtBodyParts = nbt.getList("BodyParts", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < nbtBodyParts.size(); i++)
		{
			BodyParts[i].FromNbt((NbtCompound) nbtBodyParts.get(i));
		}
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player)
	{
		return player == m_Provider;
	}

	@Override
	public void serverTick()
	{
		if (IsDirty)
		{
			IsDirty = false;
			ComponentManager.BODY_TEMPERATURE.sync(m_Provider);
		}
	}
}
