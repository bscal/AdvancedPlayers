package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

public interface FValueComponent extends ComponentV3
{

	void SetClampedValue(float value);

	void SetClampedValueSynced(float value);

	float GetDefaultValue();

}
