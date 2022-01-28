package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import me.bscal.advancedplayer.common.mechanics.body.BodyPart;

import java.util.Map;

public interface EntityBodyComponent extends ComponentV3
{

	Map<String, BodyPart> GetBodyParts();

	BodyPart GetBodyPart(String part);

	void SetBodyPart(String name, BodyPart part);

	float GetCoreBodyValue();

	void SetCoreBodyValue(float value);

	void ResetBody();

	boolean IsDirty();

	void SetDirty(boolean dirty);

}
