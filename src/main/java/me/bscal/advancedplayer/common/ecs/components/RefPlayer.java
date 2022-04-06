package me.bscal.advancedplayer.common.ecs.components;

import com.artemis.Component;
import com.artemis.annotations.Transient;
import net.minecraft.entity.player.PlayerEntity;

@Transient public class RefPlayer extends Component
{

	public PlayerEntity Player;

}
