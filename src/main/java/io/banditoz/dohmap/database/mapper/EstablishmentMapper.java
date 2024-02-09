package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.Pin;
import io.banditoz.dohmap.model.Establishment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EstablishmentMapper {
    @Select("""
            SELECT id,
                   name,
                   address,
                   city,
                   state,
                   zip,
                   phone,
                   type,
                   last_seen
            FROM establishment
            WHERE id = #{id}::uuid""")
    Establishment getById(String id);

    @Select("""
            SELECT e.id,
                   name,
                   address,
                   city,
                   state,
                   zip,
                   phone,
                   type,
                   lat,
                   lng,
                   (SELECT rank FROM establishment_rank er WHERE er.establishment_id = e.id ORDER BY er.id DESC LIMIT 1) AS lastRank
            FROM establishment e
                     JOIN establishment_location el ON e.id = el.establishment_id
            WHERE e.type ILIKE '%Restaurants%' OR e.type ILIKE '%Beverage%' OR e.type ILIKE '%breakfast%'""")
    List<Pin> getPins();

    @Select("SELECT rank FROM establishment_rank er WHERE er.establishment_id = #{establishmentId}::uuid ORDER BY er.id DESC LIMIT 1")
    Integer getLastRankForEstablishment(String establishmentId);

    @Select("""
            SELECT id,
                   name,
                   address,
                   city,
                   state,
                   zip,
                   phone,
                   type,
                   last_seen
            FROM establishment
            WHERE name = #{name}
              AND address = #{address}
              AND city = #{city}
              AND state = #{state}
              AND zip = #{zip}
              AND phone = #{phone}
              AND type = #{type}""")
    Establishment getByEstablishment(Establishment establishment);

    @Insert("""
            INSERT INTO establishment
              (id, name, address, city, state, zip, phone, type)
            VALUES
              (#{id}::uuid, #{name}, #{address}, #{city}, #{state}, #{zip}, #{phone}, #{type})""")
    Integer insertEstablishment(Establishment establishment);

    @Update("UPDATE establishment SET last_seen = now() WHERE id = #{id}::uuid")
    Integer updateLastSeen(Establishment establishment);

    @Select("SELECT COUNT(*) FROM establishment;")
    int getCount();

    @Select("SELECT COUNT(*) FROM establishment e WHERE e.type ILIKE '%Restaurants%' OR e.type ILIKE '%Beverage%' OR e.type ILIKE '%breakfast%';")
    int getRestaurantBeverageCount();
}
