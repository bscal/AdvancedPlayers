package me.bscal.advancedplayer.common.ecs.components;

import com.artemis.Component;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class Traits extends Component
{

	public float TotalTraitWeight;
	public List<Trait> Traits;

	public Traits()
	{
		Traits = new ArrayList<>();
	}

	public void AddTrait(Trait trait)
	{
		if (Traits.contains(trait)) return;
		Traits.add(trait);
		TotalTraitWeight += trait.Weight;
	}

	public void RemoveTrait(Trait trait)
	{
		Traits.remove(trait);
		TotalTraitWeight -= trait.Weight;
	}

	public record Trait(String Name, String Desc, float Weight, TraitOnTickFunction TickFunction)
	{
		public static final Trait STRONG = new Trait("trait:strong:name", "trait:strong:desc", 6f, null);
	}

	public interface TraitOnTickFunction
	{
		void OnTick(LivingEntity entity, Trait trait);
	}

	public enum TraitType
	{
		Neutral("Neutral", ColorHelper.Argb.getArgb(255, 64, 64, 64)), Good("Good", ColorHelper.Argb.getArgb(255, 40, 200, 40)), Bad("Bad",
			ColorHelper.Argb.getArgb(255, 200, 40, 40));

		public final String Name;
		public final int Color;

		TraitType(String name, int color)
		{
			Name = name;
			Color = color;
		}
	}
}

