package net.querz.mcaselector.filter;

import net.querz.mcaselector.io.mca.ChunkData;
import net.querz.mcaselector.text.TextHelper;
import net.querz.mcaselector.version.VersionController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaletteFilter extends TextFilter<List<String>> {

	public PaletteFilter() {
		this(Operator.AND, Comparator.CONTAINS, null);
	}

	private PaletteFilter(Operator operator, Comparator comparator, List<String> value) {
		super(FilterType.PALETTE, operator, comparator, value);
		setRawValue(String.join(",", value == null ? new ArrayList<>(0) : value));
	}

	@Override
	public boolean contains(List<String> value, ChunkData data) {
		if (data.getRegion() == null) {
			return false;
		}
		return VersionController.getChunkFilter(data.getRegion().getData().getInt("DataVersion"))
				.matchBlockNames(data.getRegion().getData(), value.toArray(new String[0]));
	}

	@Override
	public boolean containsNot(List<String> value, ChunkData data) {
		return !contains(value, data);
	}

	@Override
	public void setFilterValue(String raw) {
		String[] blockNames = TextHelper.parseBlockNames(raw);
		if (blockNames == null) {
			setValid(false);
			setValue(null);
		} else {
			setValid(true);
			setValue(Arrays.asList(blockNames));
			setRawValue(raw);
		}
	}

	@Override
	public String getFormatText() {
		return "<block>[,<block>,...]";
	}

	@Override
	public String toString() {
		return "Palette " + getComparator().getQueryString() + " \"" + getRawValue() + "\"";
	}

	@Override
	public PaletteFilter clone() {
		return new PaletteFilter(getOperator(), getComparator(), new ArrayList<>(value));
	}
}
