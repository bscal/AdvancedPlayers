package me.bscal.advancedplayer.common;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.List;

public abstract class TraitsInstance implements Serializable
{

    public static byte[] Serialize(List<TraitsInstance> instanceList)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try
        {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(instanceList);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static List<TraitsInstance> Deserialize(byte[] data)
    {
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (List<TraitsInstance>) ois.readObject();
        } catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

}
