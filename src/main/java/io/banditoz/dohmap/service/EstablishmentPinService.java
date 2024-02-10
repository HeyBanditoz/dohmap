package io.banditoz.dohmap.service;

import io.banditoz.dohmap.database.mapper.EstablishmentMapper;
import io.banditoz.dohmap.model.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.*;

@Service
public class EstablishmentPinService {
    private final EstablishmentMapper establishmentMapper;

    @Autowired
    public EstablishmentPinService(EstablishmentMapper establishmentMapper) {
        this.establishmentMapper = establishmentMapper;
    }

    public List<Pin> getPins() {
        Map<List<Double>, List<Pin>> coordinatePinPair = establishmentMapper.getPins()
                .stream()
                .sorted(Comparator.comparing(o -> o.establishment().name()))
                .collect(Collectors.groupingBy(pin -> List.of(pin.lat(), pin.lng())));
        coordinatePinPair.forEach((coords, pins) -> {
            if (pins.size() == 1) {
                return;
            }
            List<Pin> modifiedPins = circlizePins(pins);
            coordinatePinPair.put(coords, modifiedPins);
        });
        return coordinatePinPair.values().stream().flatMap(List::stream).toList();
    }

    /**
     * Given a {@link List} of {@link Pin pins} that have the same latitude and longitude, modify the list where each
     * {@link Pin} is drawn on equidistant points on a circle, to prevent pins from being layered on top of eachother
     * on the map. This creates a new {@link Pin} object with its <code>coordinatesModified</code> variable set to true.
     *
     * @param pins The list of pins to circlize.
     * @return A list of modified pins.
     */
    private List<Pin> circlizePins(List<Pin> pins) {
        double angleIncrement = 360.0 / pins.size();
        double radius = (0.00015 * pins.size()) / (2 * PI);

        List<Pin> modifiedPins = new ArrayList<>(pins.size());
        for (int i = 0; i < pins.size(); i++) {
            Pin pin = pins.get(i);
            double angle = i * angleIncrement;
            double angleRad = toRadians(angle);

            // Set new latitude and longitude based on circle equation
            double newLat = pin.lat() + radius * cos(angleRad);
            double newLng = pin.lng() + radius * sin(angleRad);

            modifiedPins.add(new Pin(pin.establishment(), newLat, newLng, pin.lastRank(), true, pin.possiblyGone()));
        }
        return modifiedPins;
    }
}
