package net.querz.mcaselector.changer;

import net.querz.mcaselector.debug.Debug;
import java.util.ArrayList;
import java.util.List;

public enum FieldType {

	LIGHT_POPULATED("LightPopulated", LightPopulatedField.class, false, false),
	DATA_VERSION("DataVersion", DataVersionField.class, false, false),
	INHABITED_TIME("InhabitedTime", InhabitedTimeField.class, false, false),
	LAST_UPDATE("LastUpdate", LastUpdateField.class, false, false),
	STATUS("Status", StatusField.class, false, true),
	BIOME("Biome", BiomeField.class, false, false),
	DELETE_ENTITIES("DeleteEntities", DeleteEntitiesField.class, false, false),
	DELETE_SECTIONS("DeleteSections", DeleteSectionsField.class, false, true),
	STRUCTURE_REFERENCE("FixStructureReferences", ReferenceField.class, true, false);

	private String name;
	private Class<? extends Field<?>> clazz;
	private boolean headlessOnly;
	private boolean clearCache;

	private static FieldType[] uiValues;

	static {
		List<FieldType> uiValues = new ArrayList<>(8);
		for (FieldType fieldType : values()) {
			if (!fieldType.headlessOnly) {
				uiValues.add(fieldType);
			}
		}
		FieldType.uiValues = uiValues.toArray(new FieldType[0]);
	}


	FieldType(String name, Class<? extends Field<?>> clazz, boolean headlessOnly, boolean clearCache) {
		this.name = name;
		this.clazz = clazz;
		this.headlessOnly = headlessOnly;
		this.clearCache = clearCache;
	}

	public Field<?> newInstance() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			Debug.error(e);
			return null;
		}
	}

	public static FieldType getByName(String name) {
		for (FieldType f : FieldType.values()) {
			if (f.name.equals(name)) {
				return f;
			}
		}
		return null;
	}

	public boolean requiresClearCache() {
		return clearCache;
	}

	@Override
	public String toString() {
		return name;
	}

	public static FieldType[] uiValues() {
		return uiValues;
	}
}
