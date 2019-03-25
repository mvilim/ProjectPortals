package com.gmail.trentech.pjp.data.mutable;

import static com.gmail.trentech.pjp.data.Keys.PORTALS;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractMappedData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.MapValue;

import com.gmail.trentech.pjp.data.immutable.ImmutableHomeData;
import com.gmail.trentech.pjp.portal.Portal;
import com.google.common.base.Preconditions;

public class HomeData extends AbstractMappedData<String, Portal, HomeData, ImmutableHomeData> {

	public HomeData(Map<String, Portal> value) {
		super(PORTALS, value);
	}

	public HomeData() {
		super(PORTALS, new HashMap<>());
	}

	public MapValue<String, Portal> portals() {
		return Sponge.getRegistry().getValueFactory().createMapValue(PORTALS, getValue());
	}

	@Override
	public Optional<Portal> get(String key) {
		if (getValue().containsKey(key)) {
			return Optional.of(getValue().get(key));
		}
		return Optional.empty();
	}

	@Override
	public Set<String> getMapKeys() {
		return getValue().keySet();
	}

	@Override
	public HomeData put(String key, Portal value) {
		getValue().put(key, value);
		return this;
	}

	@Override
	public HomeData putAll(Map<? extends String, ? extends Portal> map) {
		getValue().putAll(map);
		return this;
	}

	@Override
	public HomeData remove(String key) {
		getValue().remove(key);
		return this;
	}

	@Override
	public Optional<HomeData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
		HomeData homeData = Preconditions.checkNotNull(mergeFn).merge(copy(), dataHolder.get(HomeData.class).orElse(copy()));
		return Optional.of(set(PORTALS, homeData.get(PORTALS).get()));
	}

	@Override
	public Optional<HomeData> from(DataContainer container) {
		if (container.contains(PORTALS.getQuery())) {
			HashMap<String, Portal> homeList = new HashMap<>();

			DataView homes = container.getView(PORTALS.getQuery()).get();

			for (DataQuery home : homes.getKeys(false)) {
				Optional<Portal.Local> optionalLocal = homes.getSerializable(home, Portal.Local.class);
				
				if(optionalLocal.isPresent()) {
					homeList.put(home.toString(), optionalLocal.get());
				} else {
					homeList.put(home.toString(), homes.getSerializable(home, Portal.Server.class).get());
				}
			}
			return Optional.of(new HomeData(homeList));
		}
		return Optional.empty();
	}

	@Override
	public HomeData copy() {
		return new HomeData(getValue());
	}

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public ImmutableHomeData asImmutable() {
		return new ImmutableHomeData(getValue());
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(PORTALS, getValue());
	}
	
	public static class Builder extends AbstractDataBuilder<HomeData> implements DataManipulatorBuilder<HomeData, ImmutableHomeData> {

		public Builder() {
			super(HomeData.class, 0);
		}

		@Override
		public Optional<HomeData> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(PORTALS.getQuery())) {
				HashMap<String, Portal> homeList = new HashMap<>();

				DataView homes = container.getView(PORTALS.getQuery()).get();

				for (DataQuery home : homes.getKeys(false)) {
					Optional<Portal.Local> optionalLocal = homes.getSerializable(home, Portal.Local.class);
					
					if(optionalLocal.isPresent()) {
						homeList.put(home.toString(), optionalLocal.get());
					} else {
						homeList.put(home.toString(), homes.getSerializable(home, Portal.Server.class).get());
					}
				}
				return Optional.of(new HomeData(homeList));
			}
			return Optional.empty();
		}

		@Override
		public HomeData create() {
			return new HomeData(new HashMap<String, Portal>());
		}

		@Override
		public Optional<HomeData> createFrom(DataHolder dataHolder) {
			return create().fill(dataHolder);
		}

	}
}