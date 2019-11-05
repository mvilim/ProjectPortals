package com.gmail.trentech.pjp.data.mutable;

import static com.gmail.trentech.pjp.data.Keys.PORTAL;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.Value;

import com.gmail.trentech.pjp.data.immutable.ImmutableSignPortalData;
import com.gmail.trentech.pjp.portal.Portal;
import com.google.common.base.Preconditions;

public class SignPortalData extends AbstractSingleData<Portal, SignPortalData, ImmutableSignPortalData> {

	public SignPortalData() {
		super(PORTAL, null);
	}

	public SignPortalData(Portal value) {
		super(PORTAL, value);
	}

	public Value<Portal> portal() {
		return Sponge.getRegistry().getValueFactory().createValue(PORTAL, getValue(), getValue());
	}

	@Override
	public SignPortalData copy() {
		return new SignPortalData(this.getValue());
	}

	@Override
	public Optional<SignPortalData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
		SignPortalData signData = Preconditions.checkNotNull(mergeFn).merge(copy(), dataHolder.get(SignPortalData.class).orElse(copy()));
		return Optional.of(set(PORTAL, signData.get(PORTAL).get()));
	}

	@Override
	public Optional<SignPortalData> from(DataContainer container) {
		if (container.contains(PORTAL.getQuery())) {
			Optional<Portal> portal = container.getSerializable(PORTAL.getQuery(), Portal.class);
			
			return Optional.of(new SignPortalData(portal.get()));
		}
		return Optional.empty();
	}

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public ImmutableSignPortalData asImmutable() {
		return new ImmutableSignPortalData(this.getValue());
	}

	@Override
	protected Value<Portal> getValueGetter() {
		return Sponge.getRegistry().getValueFactory().createValue(PORTAL, getValue(), getValue());
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(PORTAL, getValue());
	}
	
	public static class Builder extends AbstractDataBuilder<SignPortalData> implements DataManipulatorBuilder<SignPortalData, ImmutableSignPortalData> {

		public Builder() {
			super(SignPortalData.class, 0);
		}

		@Override
		public Optional<SignPortalData> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(PORTAL.getQuery())) {			
				Optional<Portal> portal = container.getSerializable(PORTAL.getQuery(), Portal.class);
				return Optional.of(new SignPortalData(portal.get()));
			}

			return Optional.empty();
		}

		@Override
		public SignPortalData create() {
			return new SignPortalData();
		}

		@Override
		public Optional<SignPortalData> createFrom(DataHolder dataHolder) {
			return create().fill(dataHolder);
		}

		public SignPortalData createFrom(Portal portal) {
			return new SignPortalData(portal);
		}
	}
}
