package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.EstablishmentLocation;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EstablishmentLocationMapper {
    @Select("""
            SELECT id,
                   place_id,
                   establishment_id,
                   lat,
                   lng,
                   source,
                   deleted_on
            FROM establishment_location
            WHERE establishment_id = #{establishmentId}::uuid
              AND deleted_on IS NULL
            """)
    EstablishmentLocation getByEstablishmentId(String establishmentId);

    @Insert("""
            INSERT INTO establishment_location
              (id, place_id, establishment_id, lat, lng, source, raw_json)
            VALUES
              (#{el.id}::uuid, #{el.placeId}, #{el.establishmentId}::uuid, #{el.lat}, #{el.lng}, #{el.source}, #{rawJson}::jsonb)""")
    Integer insert(EstablishmentLocation el, String rawJson);
}
