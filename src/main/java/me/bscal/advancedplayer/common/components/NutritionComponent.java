package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.Set;

public class NutritionComponent implements ComponentV3, AutoSyncedComponent, ServerTickingComponent
{

	public int CaloriesEaten;
	public int Dairy;
	public int Vegetables;
	public int Fruits;
	public int Grains;
	public int Protein;
	public boolean IsDirty;
	public final Set<Identifier> FoodsEaten;

	private final LivingEntity m_Provider;

	public NutritionComponent(LivingEntity provider)
	{
		m_Provider = provider;
		FoodsEaten = new HashSet<>();
	}

	public void EatFood(Item item)
	{
		if (m_Provider.world.isClient) return;
		if (item.isFood())
		{
			var id = Registry.ITEM.getId(item);
			FoodsEaten.add(id);

			// TODO check total foods eaten

			FoodComponent component = item.getFoodComponent();
			// TODO lookup data about foods nutrition
		}
		IsDirty = true;
	}

	@Override
	public void serverTick()
	{
		if (IsDirty)
		{
			IsDirty = false;
			ComponentManager.NUTRITION.sync(m_Provider);
		}
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		CaloriesEaten = tag.getInt("Calories");
		Dairy = tag.getInt("Dairy");
		Vegetables = tag.getInt("Vegetables");
		Fruits = tag.getInt("Fruits");
		Grains = tag.getInt("Grains");
		Protein = tag.getInt("Protein");

		FoodsEaten.clear();
		NbtList foodsSet = tag.getList("FoodEatenSet", NbtElement.STRING_TYPE);
		for (NbtElement nbtElement : foodsSet)
		{
			NbtString id = (NbtString) nbtElement;
			FoodsEaten.add(Identifier.tryParse(id.asString()));
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putInt("Calories", CaloriesEaten);
		tag.putInt("Dairy", Dairy);
		tag.putInt("Vegetables", Vegetables);
		tag.putInt("Fruits", Fruits);
		tag.putInt("Grains", Grains);
		tag.putInt("Protein", Protein);

		NbtList foodsSet = new NbtList();
		FoodsEaten.forEach(food -> {
			foodsSet.add(NbtString.of(food.toString()));
		});
		tag.put("FoodEatenSet", foodsSet);
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player)
	{
		return player == m_Provider;
	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient)
	{
		AutoSyncedComponent.super.writeSyncPacket(buf, recipient);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf)
	{
		AutoSyncedComponent.super.applySyncPacket(buf);
	}
}
