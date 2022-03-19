package me.bscal.advancedplayer.common.mechanics.ecs.components;

import com.artemis.Component;
import com.artemis.annotations.Transient;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

import java.util.List;

@Transient public class Traits extends Component
{

	public float TotalTraitWeight;
	public List<Trait> Traits;

	public static class Trait extends Component
	{
		public final float Weight;

		public Trait()
		{
			Weight = 0;
		}

		public Trait(float weight)
		{
			Weight = weight;
		}
	}

	public enum TraitType
	{
		Neutral("Neutral", ColorHelper.Argb.getArgb(255, 64, 64, 64)),
		Good("Good", ColorHelper.Argb.getArgb(255, 40, 200, 40)),
		Bad("Bad", ColorHelper.Argb.getArgb(255, 200, 40, 40));

		public final String Name;
		public final int Color;

		TraitType(String name, int color)
		{
			Name = name;
			Color = color;
		}
	}
}

