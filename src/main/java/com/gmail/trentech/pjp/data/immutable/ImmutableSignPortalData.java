package com.gmail.trentech.pjp.data.immutable;

import static com.gmail.trentech.pjp.data.Keys.PORTAL;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import com.gmail.trentech.pjp.data.mutable.SignPortalData;
import com.gmail.trentech.pjp.portal.Portal;

public class ImmutableSignPortalData extends AbstractImmutableSingleData<Portal, ImmutableSignPortalData, SignPortalData> {

	public ImmutableSignPortalData(Portal value) {
		super(PORTAL, value);
	}

	public ImmutableValue<Portal> sign() {
		return Sponge.getRegistry().getValueFactory().createValue(PORTAL, getValue(), getValue()).asImmutable();
	}

	@Override
	public <E> Optional<ImmutableSignPortalData> with(Key<? extends BaseValue<E>> key, E value) {
		if (this.supports(key)) {
			return Optional.of(asMutable().set(key, value).asImmutable());
		} else {
			return Optional.empty();
		}
	}

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	protected ImmutableValue<?> getValueGetter() {
		return Sponge.getRegistry().getValueFactory().createValue(PORTAL, getValue()).asImmutable();
	}

	@Override
	public SignPortalData asMutable() {
		return new SignPortalData(this.getValue());
	}

	@Override
	protected DataContainer fillContainer(DataContainer dataContainer) {
		return super.toContainer().set(PORTAL, getValue());
	}
}
