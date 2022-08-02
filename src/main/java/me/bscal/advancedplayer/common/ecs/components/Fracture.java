package me.bscal.advancedplayer.common.ecs.components;

import com.artemis.Component;

import java.util.BitSet;

public class Fracture extends Component
{

    public static final int MAX_FRACTURES = 4;
    public static final int LEFT_LEG = 0;
    public static final int RIGHT_LEG = 1;
    public static final int LEFT_ARM = 2;
    public static final int RIGHT_ARM = 3;

    public BitSet Fractures = new BitSet();

    public boolean IsBodyPartFractured(BodyPart part)
    {
        return Fractures.get(part.ordinal());
    }

    public void SetFractures(int bodyPart, boolean value)
    {
        Fractures.set(bodyPart, value);
    }

    public int GetTotalFractures()
    {
        int count = 0;
        for (int i = Fractures.nextSetBit(0); i >= 0; i = Fractures.nextSetBit(++i))
        {
            if (i >= BodyPart.values().length) break;
            ++count;
        }
        return count;
    }

    public enum BodyPart
    {
        Head,
        Body,
        LeftArm,
        RightArm,
        LeftLeg,
        RightLeg,
        LeftFoot,
        RightFoot
    }

}
