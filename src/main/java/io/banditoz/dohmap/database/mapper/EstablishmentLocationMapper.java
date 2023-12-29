package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.EstablishmentLocation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EstablishmentLocationMapper {
    @Select("""
            SELECT id,
                   place_id,
                   establishment_id,
                   lat,
                   lng
            FROM establishment_location
            WHERE establishment_id = #{establishmentId}::uuid
            """)
    EstablishmentLocation getByEstablishmentId(String establishmentId);

    @Insert("""
            INSERT INTO establishment_location
              (id, place_id, establishment_id, lat, lng, raw_json)
            VALUES
              (#{el.id}::uuid, #{el.placeId}, #{el.establishmentId}::uuid, #{el.lat}, #{el.lng}, #{rawJson}::jsonb)""")
    Integer insert(EstablishmentLocation el, String rawJson);
}
