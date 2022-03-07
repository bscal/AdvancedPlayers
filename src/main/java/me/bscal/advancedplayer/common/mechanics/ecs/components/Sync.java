package me.bscal.advancedplayer.common.mechanics.ecs.components;

import com.artemis.Component;
import com.artemis.annotations.Transient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A components used to handle syncing other components with the client.
 * Has some utility methods. Does not get saved.
 * TODO maybe look at performance of these classes?
 */
@Transient public class Sync extends Component
{

	/**
	 * Server and client entityId's are not the same.
	 * The NetworkId is the server's entityId. This way we can look up entities on the client
	 */
	public int NetworkId;

	/**
	 * List of AddContainers which hold the components instance, and components class name.
	 */
	public List<AddContainer> Components = new ArrayList<>();

	/**
	 * A Set to keep track of what classes are added. This isn't sent to client, just keeps track of added classes.
	 */
	transient public Set<ClassContainer> AddedComponents = new ObjectOpenHashSet<>();

	/**
	 * A set to keep track of what classes are removed. Sent to client to remove any classes.
	 */
	public Set<ClassContainer> RemovedComponents = new ObjectOpenHashSet<>();

	/**
	 * A utility method for adding a component. Some safety checks, and creates the containers.
	 */
	public void Add(Component component, Class<? extends Component> c)
	{

		var container = new ClassContainer(component);
		if (AddedComponents.add(container))
		{
			Components.add(new AddContainer(component));
		}
	}

	/**
	 * A utility method for removing a component.
	 */
	public void Remove(Class<? extends Component> clazz)
	{
		var container = new ClassContainer(clazz);
		RemovedComponents.add(container);
	}

	public void Clear()
	{
		Components.clear();
		AddedComponents.clear();
		RemovedComponents.clear();
	}

	public boolean IsAdding(Class<? extends Component> clazz)
	{
		var container = new ClassContainer(clazz);
		return AddedComponents.contains(container);
	}

	public boolean IsRemoving(Class<? extends Component> clazz)
	{
		var container = new ClassContainer(clazz);
		return RemovedComponents.contains(container);
	}

	/**
	 * This is my work around to sending the Component Data and its class name as a string.
	 * For some reason when the Component deserializes on the client, the component's class,
	 * is a different reference or something?
	 */
	public static class AddContainer extends Serializer<AddContainer>
	{
		public Component Component;
		public String ClassName;

		public AddContainer()
		{
		}

		AddContainer(Component component)
		{
			Component = component;
			ClassName = component.getClass().getName();
		}

		@Override
		public void write(Kryo kryo, Output output, AddContainer object)
		{
			output.writeString(object.ClassName);
			kryo.writeObject(output, object.Component);
		}

		@Override
		public AddContainer read(Kryo kryo, Input input, Class<AddContainer> type)
		{
			var className = input.readString();
			Class<?> clazz = null;
			try
			{
				clazz = Class.forName(className);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			var component = kryo.readObject(input, clazz);
			var container = new AddContainer();
			container.ClassName = className;
			container.Component = (com.artemis.Component) component;
			return container;
		}
	}

	/**
	 * Used because deserialization was not properly deserialization Classes.
	 * Not sure why this is, but using class.getName() and Class.forName() seems to work.
	 * I think it might be because Kryo caches writeClass() and since we have 2 different Kryo
	 * instances for client/server its possible it messes up?
	 */
	public static class ClassContainer extends Serializer<ClassContainer>
	{
		public Class<? extends Component> Clazz;

		public ClassContainer()
		{
		}

		public ClassContainer(Component component)
		{
			Clazz = component.getClass();
		}

		public ClassContainer(Class<? extends Component> clazz)
		{
			Clazz = clazz;
		}

		@Override
		public void write(Kryo kryo, Output output, ClassContainer object)
		{
			output.writeString(object.Clazz.getName());
		}

		@Override
		public ClassContainer read(Kryo kryo, Input input, Class<ClassContainer> type)
		{
			var className = input.readString();
			Class<?> clazz = null;
			try
			{
				clazz = Class.forName(className);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			return new ClassContainer((Class<? extends Component>) clazz);
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ClassContainer that = (ClassContainer) o;

			return Objects.equals(this.Clazz, that.Clazz);
		}

		@Override
		public int hashCode()
		{
			return Clazz != null ? Clazz.hashCode() : 0;
		}
	}

	/**
	 * TODO. Possible used to serialize classes by default?
	 */
	public static class ClassSerializer extends Serializer<Class<?>>
	{

		@Override
		public void write(Kryo kryo, Output output, Class<?> object)
		{
			output.writeString(object.getName());
		}

		@Override
		public Class<?> read(Kryo kryo, Input input, Class<Class<?>> type)
		{
			Class<?> clazz = null;
			try
			{
				clazz = Class.forName(input.readString());
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			return clazz;
		}
	}

}
