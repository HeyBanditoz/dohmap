package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.Pin;
import io.banditoz.dohmap.model.Establishment;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
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

    // TODO need to centralize last_seen logic below there...
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
                   (SELECT rank FROM establishment_rank er WHERE er.establishment_id = e.id ORDER BY er.id DESC LIMIT 1) AS lastRank,
                   (SELECT last_seen FROM establishment ORDER BY last_seen DESC LIMIT 1 OFFSET 50) - INTERVAL '1 day' > e.last_seen AS possiblyGone
            FROM establishment e
                     JOIN establishment_location el ON e.id = el.establishment_id
            WHERE e.type ILIKE '%Restaurants%' OR e.type ILIKE '%Beverage%' OR e.type ILIKE '%breakfast%'""")
    List<Pin> getPins();

    @Select("SELECT rank FROM establishment_rank er WHERE er.establishment_id = #{establishmentId}::uuid ORDER BY er.id DESC LIMIT 1")
    Integer getLastRankForEstablishment(String establishmentId);

    @Select("""
            <script>
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
              AND <if test="phone != null">phone = #{phone}</if> <if test="phone == null">phone IS NULL</if>
              AND type = #{type}
            ORDER BY id DESC
            LIMIT 1; -- TODO remove once duplicate restaurants (those w/o phones) are fixed
            </script>""")
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

    @Select("SELECT last_seen FROM establishment ORDER BY last_seen DESC LIMIT 1 OFFSET 50")
    Instant get50thLatestLastSeen();
}
