package com.gmail.trentech.pjp.data.builder.data;

import static com.gmail.trentech.pjp.data.DataQueries.DESTINATION;
import static com.gmail.trentech.pjp.data.DataQueries.PRICE;
import static com.gmail.trentech.pjp.data.DataQueries.ROTATION;
import static com.gmail.trentech.pjp.data.DataQueries.BUNGEE;

import java.util.Optional;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import com.gmail.trentech.pjp.data.object.Button;
import com.gmail.trentech.pjp.utils.Rotation;

public class ButtonBuilder extends AbstractDataBuilder<Button> {

    public ButtonBuilder() {
        super(Button.class, 1);
    }

    @Override
    protected Optional<Button> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(DESTINATION, ROTATION, PRICE, BUNGEE)) {
        	String destination = container.getString(DESTINATION).get();
        	String rotation = container.getString(ROTATION).get();
        	Double price = container.getDouble(PRICE).get();
        	boolean bungee = container.getBoolean(BUNGEE).get();
        	
            return Optional.of(new Button(destination, Rotation.get(rotation).get(), price, bungee));
        }
        
        return Optional.empty();
    }
}
