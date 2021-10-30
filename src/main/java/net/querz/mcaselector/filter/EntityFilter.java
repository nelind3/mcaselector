package net.querz.mcaselector.filter;

import net.querz.mcaselector.debug.Debug;
import net.querz.mcaselector.io.mca.ChunkData;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.LongArrayTag;
import net.querz.nbt.tag.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityFilter extends TextFilter<List<String>> {

	private static final Set<String> validNames = new HashSet<>();
	private static final Pattern entityNamePattern = Pattern.compile("^(?<space>[a-z_]*):?(?<id>[a-z_]*)$");

	static {
		try (BufferedReader bis = new BufferedReader(
				new InputStreamReader(Objects.requireNonNull(EntityFilter.class.getClassLoader().getResourceAsStream("mapping/all_entity_names.txt"))))) {
			String line;
			while ((line = bis.readLine()) != null) {
				validNames.add("minecraft:" + line);
			}
		} catch (IOException ex) {
			Debug.dumpException("error reading mapping/all_entity_names.txt", ex);
		}
	}

	public EntityFilter() {
		this(Operator.AND, Comparator.CONTAINS, null);
	}

	private EntityFilter(Operator operator, Comparator comparator, List<String> value) {
		super(FilterType.ENTITIES, operator, comparator, value);
		setRawValue(String.join(",", value == null ? new ArrayList<>(0) : value));
	}

	@Override
	public boolean contains(List<String> value, ChunkData data) {
		Tag<?> rawEntities = VersionController.getEntityFilter(data.getRegion().getData().getInt("DataVersion")).getEntities(data);
		if (rawEntities == null || rawEntities.getID() == LongArrayTag.ID) {
			return false;
		}
		ListTag<CompoundTag> entities = ((ListTag<?>) rawEntities).asCompoundTagList();
		nameLoop:
		for (String name : getFilterValue()) {
			for (CompoundTag entity : entities) {
				String id = entity.getString("id");
				if (name.equals(id)) {
					continue nameLoop;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean intersects(List<String> value, ChunkData data) {
		Tag<?> rawEntities = VersionController.getEntityFilter(data.getRegion().getData().getInt("DataVersion")).getEntities(data);
		if (rawEntities == null || rawEntities.getID() == LongArrayTag.ID) {
			return false;
		}
		ListTag<CompoundTag> entities = ((ListTag<?>) rawEntities).asCompoundTagList();
		for (String name : getFilterValue()) {
			for (CompoundTag entity : entities) {
				String id = entity.getString("id");
				if (name.equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsNot(List<String> value, ChunkData data) {
		return !contains(value, data);
	}

	@Override
	public void setFilterValue(String raw) {
		String[] rawEntityNames = raw.replace(" ", "").split(",");
		if (raw.isEmpty() || rawEntityNames.length == 0) {
			setValid(false);
			setValue(null);
		} else {
			for (int i = 0; i < rawEntityNames.length; i++) {
				String name = rawEntityNames[i];
				Matcher m = entityNamePattern.matcher(name);
				if (m.matches()) {
					if (m.group("id").isEmpty()) {
						name = "minecraft:" + m.group("space");
						rawEntityNames[i] = name;
					}
				}

				if (!validNames.contains(name)) {
					if (name.startsWith("'") && name.endsWith("'") && name.length() >= 2 && !name.contains("\"")) {
						rawEntityNames[i] = name.substring(1, name.length() - 1);
						continue;
					}
					setValue(null);
					setValid(false);
					return;
				}
			}
			setValid(true);
			setValue(Arrays.asList(rawEntityNames));
			setRawValue(raw);
		}
	}

	@Override
	public String getFormatText() {
		return "<entity>[,<entity>,...]";
	}

	@Override
	public String toString() {
		return "Entities " + getComparator().getQueryString() + " \"" + getRawValue() + "\"";
	}

	@Override
	public EntityFilter clone() {
		return new EntityFilter(getOperator(), getComparator(), new ArrayList<>(value));
	}
}
