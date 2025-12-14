package org.logoce.lmf.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapBuilder<K, V>
{
	private final List<Map.Entry<K, V>> entries = new ArrayList<>();

	public MapBuilder()
	{
	}

	public MapBuilder<K, V> add(K key, V value)
	{
		entries.add(Map.entry(key, value));
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Map<K, V> build()
	{
		final Map.Entry[] a = new Map.Entry[entries.size()];
		return Map.ofEntries(entries.toArray(a));
	}
}
