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
                   last_seen,
                   sys_id,
                   source
            FROM establishment
            WHERE id = #{id}::uuid""")
    Establishment getById(String id);

    // TODO need to centralize last_seen logic below there...
    @Select("""
            SELECT e.id,
                   name,
                   address,
                   UPPER(city),
                   state,
                   zip,
                   phone,
                   type,
                   lat,
                   lng,
                   null,
                   -- (SELECT rank FROM establishment_rank er WHERE er.establishment_id = e.id ORDER BY er.id DESC LIMIT 1) AS lastRank,
                   (SELECT last_seen FROM establishment ORDER BY last_seen DESC LIMIT 1 OFFSET 50) - INTERVAL '4 day' > e.last_seen AS possiblyGone,
                   (SELECT TO_CHAR(inspection_date, 'YYYY-MM-DD') FROM inspection i WHERE i.establishment_id = e.id ORDER BY inspection_date DESC LIMIT 1) AS lastInspection
            FROM establishment e
                     JOIN establishment_location el ON e.id = el.establishment_id AND el.deleted_on IS NULL
            WHERE (e.type ILIKE '%Restaurants%' OR e.type ILIKE '%Beverage%' OR e.type ILIKE '%breakfast%' OR e.type ILIKE '%food permit%') AND e.type NOT ILIKE '%Trucks%'
            AND   city <> 'ajax' -- appears to be a test city that got pins, don't show it""")
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
                   last_seen,
                   sys_id,
                   source
            FROM establishment
            <if test="sysId == null">
            WHERE name = #{name}
              AND address = #{address}
              AND city = #{city}
              AND state = #{state}
              AND zip = #{zip}
              AND <if test="phone != null">phone = #{phone}</if> <if test="phone == null">phone IS NULL</if>
              AND type = #{type}
            </if>
            <if test="sysId != null">
            -- establishment has a sys_id which uniquely identifies it
            WHERE sys_id = #{sysId}
            </if>
            ORDER BY id DESC
            LIMIT 1; -- TODO remove once duplicate restaurants (those w/o phones) are fixed
            </script>""")
    Establishment getByEstablishment(Establishment establishment);

    @Insert("""
            INSERT INTO establishment
              (id, name, address, city, state, zip, phone, type, sys_id, source)
            VALUES
              (#{id}::uuid, #{name}, #{address}, #{city}, #{state}, #{zip}, #{phone}, #{type}, #{sysId}, #{source})""")
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
