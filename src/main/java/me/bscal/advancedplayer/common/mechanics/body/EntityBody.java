package me.bscal.advancedplayer.common.mechanics.body;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.components.EntityBodyComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public class EntityBody implements EntityBodyComponent, AutoSyncedComponent, ServerTickingComponent
{
	public static final String HEAD = "Head";    //0;
	public static final String CHEST = "Chest";    //1;
	public static final String LEFT_ARM = "LArm";    //2;
	public static final String RIGHT_ARM = "RArm";    //3;
	public static final String GROIN = "Groin";    //4;
	public static final String LEFT_LEG = "LLeg";    //5;
	public static final String RIGHT_LEG = "RLeg";    //6;
	public static final String LEFT_FOOT = "LFoot";    //7;
	public static final String RIGHT_FOOT = "RFoot";    //8;

	public static final int MAX_PARTS = 9;

	protected final LivingEntity m_Provider;
	protected final float m_MinCoreBodyValue, m_MaxCoreBodyValue;
	protected float m_CoreBodyValue;
	protected final Consumer<Map<String, BodyPart>> m_BodyBuilder;
	protected final Map<String, BodyPart> m_PartsMap;
	public boolean IsDirty;

	public EntityBody(@NotNull LivingEntity provider, float minCoreBodyValue, float maxCoreBodyValue, Consumer<Map<String, BodyPart>> bodyBuilder)
	{
		m_Provider = provider;
		m_MinCoreBodyValue = minCoreBodyValue;
		m_MaxCoreBodyValue = maxCoreBodyValue;
		m_CoreBodyValue = maxCoreBodyValue;
		m_BodyBuilder = bodyBuilder;
		m_PartsMap = new Object2ObjectOpenHashMap<>(EntityBody.MAX_PARTS);
		if (bodyBuilder != null)
			m_BodyBuilder.accept(m_PartsMap);
	}

	public Map<String, BodyPart> GetBodyParts()
	{
		return m_PartsMap;
	}

	public BodyPart GetBodyPart(String part)
	{
		return m_PartsMap.get(part);
	}

	public void SetBodyPart(String name, BodyPart part)
	{
		m_PartsMap.replace(name, part);
	}

	public float GetCoreBodyValue()
	{
		return m_CoreBodyValue;
	}

	public void SetCoreBodyValue(float value)
	{
		m_CoreBodyValue = MathHelper.clamp(value, m_MinCoreBodyValue, m_MaxCoreBodyValue);
	}

	public void ResetBody()
	{
		m_PartsMap.clear();
		if (m_BodyBuilder != null)
			m_BodyBuilder.accept(m_PartsMap);
	}

	@Override
	public boolean IsDirty()
	{
		return IsDirty;
	}

	@Override
	public void SetDirty(boolean dirty)
	{
		IsDirty = dirty;
	}

	@Override
	public void writeToNbt(NbtCompound nbt)
	{
		nbt.putFloat("CoreBodyValue", m_CoreBodyValue);
		for (var pair : m_PartsMap.entrySet())
		{
			nbt.put(pair.getKey(), pair.getValue().ToNbt());
		}
	}

	@Override
	public void readFromNbt(NbtCompound nbt)
	{
		m_CoreBodyValue = nbt.getFloat("CoreBodyValue");
		for (var key : nbt.getKeys())
		{
			BodyPart part = m_PartsMap.get(key);
			if (part != null)
				part.FromNbt(nbt.getCompound(key));
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
