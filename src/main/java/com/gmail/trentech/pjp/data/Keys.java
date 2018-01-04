package com.gmail.trentech.pjp.data;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;

import com.gmail.trentech.pjp.portal.Portal;
import com.google.common.reflect.TypeToken;

public class Keys {

	private static final TypeToken<MapValue<String, Portal>> PORTALS_MAP_VALUE_TOKEN = new TypeToken<MapValue<String, Portal>>() {
		private static final long serialVersionUID = -1;
	};
	private static final TypeToken<Value<Portal>> PORTAL_VALUE_TOKEN = new TypeToken<Value<Portal>>() {
		private static final long serialVersionUID = -1;
	};

	public static final Key<Value<Portal>> PORTAL = Key.builder().type(PORTAL_VALUE_TOKEN).id("pjp:portal").name("portal").query(DataQuery.of("portal")).build();
	public static final Key<MapValue<String, Portal>> PORTALS = Key.builder().type(PORTALS_MAP_VALUE_TOKEN).id("pjp:portals").name("portals").query(DataQuery.of("portals")).build();

}
