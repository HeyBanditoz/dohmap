package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.model.EstablishmentInspectionDate;
import io.banditoz.dohmap.model.Inspection;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InspectionMapper {
    @Select("""
            SELECT id,
                   establishment_id,
                   inspection_date,
                   inspection_type,
                   score,
                   sys_id
            FROM inspection
            WHERE id = #{id}::uuid""")
    Inspection getById(String id);

    @Select("""
            SELECT id,
                   establishment_id,
                   inspection_date,
                   inspection_type,
                   score,
                   sys_id
            FROM inspection
            WHERE establishment_id = #{establishmentId}::uuid""")
    List<Inspection> getAllInspectionsByEstablishmentId(String establishmentId);

    @Select("""
            <script>
            SELECT id,
                   establishment_id,
                   inspection_date,
                   inspection_type,
                   score,
                   sys_id
            FROM inspection
            <if test="sysId == null">
            WHERE establishment_id = #{establishmentId}::uuid
              AND inspection_date = #{inspectionDate}
              AND <if test="inspectionType != null">inspection_type = #{inspectionType}</if> <if test="inspectionType == null">inspection_type IS NULL</if>
              AND <if test="score != null">score = #{score}</if> <if test="score == null">score IS NULL</if>
            </if>
            <if test="sysId != null">
            -- inspection has a sys_id which uniquely identifies it
            WHERE sys_id = #{sysId}
            </if>
            </script>""")
    Inspection getByInspection(Inspection inspection);

    @Insert("""
            INSERT INTO inspection
              (id, establishment_id, inspection_date, inspection_type, score, sys_id)
            VALUES
              (#{id}::uuid, #{establishmentId}::uuid, #{inspectionDate}, #{inspectionType}, #{score}, #{sysId});""")
    Integer insertInspection(Inspection inspection);

    @Select("SELECT COUNT(*) FROM inspection;")
    int getCount();

    @Select("""
            SELECT COUNT(*)
            FROM inspection i
                     JOIN establishment e ON e.id = i.establishment_id
            WHERE (e.type ILIKE '%Restaurants%'
                OR e.type ILIKE '%Beverage%'
                OR e.type ILIKE '%breakfast%'
                OR e.type ILIKE '%food permit%')
            AND e.type NOT ILIKE '%Trucks%';""")
    int getRestaurantBeverageCount();

    @Select("""
            SELECT e.id AS establishment, i.inspection_date AS inspectionDate, i.sys_id as inspectionSysId
            FROM inspection i
                     JOIN establishment e on e.id = i.establishment_id
            WHERE source = #{source};""")
    List<EstablishmentInspectionDate> getEstablishmentInspectionDates(DataSource source);
}
