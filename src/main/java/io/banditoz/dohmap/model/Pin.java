package io.banditoz.dohmap.model;

/**
 * @param coordinatesModified If this pin's coordinates have been modified versus what's stored in the database. This
 *                            happens when the code prevents pins from appearing layered on top of eachother.
 */
public record Pin(Establishment establishment, Double lat, Double lng, Integer lastRank, boolean coordinatesModified,
                  boolean possiblyGone, String lastInspection) { // should lastInspection just be an id? will require JS library
    @SuppressWarnings("unused") // for mybatis
    public Pin(String id, String name, String address, String city, String state, String zip, String phone, String type,
               Double lat, Double lng, Integer lastRank, boolean possiblyGone, String lastInspection) {
        this(new Establishment(id, name, address, city, state, zip, phone, type, null), lat, lng, lastRank, false, possiblyGone, lastInspection);
    }
}
