package me.bscal.advancedplayer.common.ecs.components;

import com.artemis.Component;
import com.artemis.annotations.Transient;
import net.minecraft.entity.Entity;

@Transient
public class RefEntity extends Component
{

	transient public Entity Entity;

}
