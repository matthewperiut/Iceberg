// package com.anthonyhilyard.iceberg.registry;

// import java.lang.reflect.Field;
// import java.lang.reflect.ParameterizedType;
// import java.lang.reflect.TypeVariable;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Consumer;
// import java.util.function.Supplier;

// import com.anthonyhilyard.iceberg.Loader;

// import net.minecraft.world.entity.Entity;
// import net.minecraft.world.entity.EntityType;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
// import net.minecraft.world.item.Item;
// import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
// import net.minecraft.core.Registry;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.sounds.SoundEvent;
// import net.minecraftforge.event.RegistryEvent;
// import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
// import net.minecraftforge.registries.ForgeRegistries;
// import net.minecraftforge.registries.IForgeRegistry;
// import net.minecraftforge.registries.IForgeRegistryEntry;
// import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

// /**
//  * Extend this class to have all registerable fields be automatically registered in Forge.  Easy.  (Just no renderers.)
//  */
// public abstract class AutoRegistry
// {
// 	protected static String MODID = null;

// 	private static boolean entityCreationRegistered = false;

// 	private static Map<EntityType<?>, Supplier<AttributeSupplier.Builder>> entityAttributes = new HashMap<>();

// 	private static Map<String, EntityType<? extends Entity>> registeredEntityTypes = new HashMap<>();

// 	private static Map<Class<?>, Registry<?>> registryMap = new HashMap<>();

// 	public static void init(String ModID)
// 	{
// 		MODID = ModID;
// 	}

// 	@SuppressWarnings("unchecked")
// 	protected AutoRegistry()
// 	{
// 		try
// 		{
// 			// Iterate through every built-in registry...
// 			for (Field field : Registry.class.getDeclaredFields())
// 			{
// 				Object fieldObj = field.get(null);
// 				if (fieldObj instanceof Registry)
// 				{
// 					// Grab the registry's supertype and add a generic listener for registry events.
// 					Class<?> clazz = (Class<?>)((ParameterizedType)fieldObj.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
// 					registryMap.put(clazz, (Registry<?>)fieldObj);
// 					Loader.LOGGER.info("Adding to registry map: {}, {}", clazz.getName(), fieldObj.getClass().getName());
// 				}
// 			}
// 		}
// 		catch (Exception e)
// 		{
// 			throw new RuntimeException(e);
// 		}
// 	}

// 	public static boolean isEntityTypeRegistered(String name)
// 	{
// 		return registeredEntityTypes.containsKey(name);
// 	}

// 	@SuppressWarnings("unchecked")
// 	public static <T extends Entity> EntityType<T> getEntityType(String name)
// 	{
// 		return (EntityType<T>) registeredEntityTypes.getOrDefault(name, null);
// 	}

// 	private final class __<T> { private __() {} }

// 	@SuppressWarnings("unchecked")
// 	private final <T> void registerAllOfType(Class<Registry<T>> type)
// 	{
// 		try
// 		{
// 			// Loop through all fields we've declared and register them.
// 			for (Field field : this.getClass().getDeclaredFields())
// 			{
// 				// Grab the field and check if it is a Forge registry-compatible type.
// 				Object obj = field.get(this);
// 				if (type.isAssignableFrom(obj.getClass()))
// 				{
// 					// If this is an entity type field and we haven't already registered for the entity creation event, do so now.
// 					if (obj instanceof EntityType && !entityCreationRegistered)
// 					{
// 						//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEntityCreation);
// 						entityCreationRegistered = true;
// 					}

// 					// If this field has a registry name, register it now.
// 					T entry = (T)obj;
// 					if (entry != null)
// 					{
// 						Class<T> clazz = (Class<T>) new __<T>().getClass().getTypeParameters()[0].getClass();
// 						Registry<? super T> registry = (Registry<? super T>) registryMap.get(clazz);
// 						if (registry != null)
// 						{
// 							Registry.register(registry, "", entry);
// 						}
// 					}
// 				}
// 			}
// 		}
// 		catch (Exception e)
// 		{
// 			throw new RuntimeException(e);
// 		}
// 	}

// 	protected static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder)
// 	{
// 		return registerEntity(name, builder, (Supplier<AttributeSupplier.Builder>)null);
// 	}

// 	@SuppressWarnings("unchecked")
// 	protected static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder, Supplier<AttributeSupplier.Builder> attributes)
// 	{
// 		if (MODID == null)
// 		{
// 			throw new RuntimeException("AutoRegistry was not initialized with mod id!");
// 		}

// 		// Build the entity type.
// 		ResourceLocation resourceLocation = new ResourceLocation(MODID, name);
// 		EntityType<T> entityType = (EntityType<T>) builder.build(name).setRegistryName(resourceLocation);

// 		// Add this entity type to the registered hashmap.
// 		registeredEntityTypes.put(name, entityType);

// 		// Store mob attributes if provided.  These will be added in the attribute creation event below.
// 		if (attributes != null)
// 		{
// 			entityAttributes.put(entityType, attributes);
// 		}

// 		return entityType;
// 	}

// 	protected static SoundEvent registerSound(String name)
// 	{
// 		if (MODID == null)
// 		{
// 			throw new RuntimeException("AutoRegistry was not initialized with mod id!");
// 		}

// 		ResourceLocation resourceLocation = new ResourceLocation(MODID, name);
// 		return new SoundEvent(resourceLocation).setRegistryName(resourceLocation);
// 	}

// 	@SuppressWarnings("unchecked")
// 	private void onEntityCreation(EntityAttributeCreationEvent event)
// 	{
// 		for (Field field : this.getClass().getDeclaredFields())
// 		{
// 			try
// 			{
// 				// Grab the field and check if it is a Forge registry-compatible type.
// 				Object obj = field.get(this);
// 				if (EntityType.class.isAssignableFrom(obj.getClass()) && entityAttributes.containsKey(obj))
// 				{
// 					EntityType<? extends LivingEntity> entityType = (EntityType<? extends LivingEntity>) obj;
// 					if (entityType != null)
// 					{
// 						event.put(entityType, entityAttributes.get(obj).get().build());
// 					}
// 				}
// 			}
// 			catch (ClassCastException e)
// 			{
// 				// The class cast exception likely just means that we tried to convert an EntityType with generic type
// 				// parameter of something other than a LivingEntity subclass.  This is fine, so continue.
// 				continue;
// 			}
// 			catch (IllegalAccessException e)
// 			{
// 				throw new RuntimeException(e);
// 			}
// 		}
// 	}
// }