package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.scraper.model.Inspection;
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
                   score
            FROM inspection
            WHERE id = #{id}::uuid""")
    Inspection getById(String id);

    @Select("""
            SELECT id,
                   establishment_id,
                   inspection_date,
                   inspection_type,
                   score
            FROM inspection
            WHERE establishment_id = #{establishmentId}::uuid""")
    List<Inspection> getAllInspectionsByEstablishmentId(String establishmentId);

    @Select("""
            <script>
            SELECT id,
                   establishment_id,
                   inspection_date,
                   inspection_type,
                   score
            FROM inspection
            WHERE establishment_id = #{establishmentId}::uuid
              AND inspection_date = #{inspectionDate}
              AND <if test="inspectionType != null">inspection_type = #{inspectionType}</if> <if test="inspectionType == null">inspection_type IS NULL</if>
              AND <if test="score != null">score = #{score}</if> <if test="score == null">score IS NULL</if>
            </script>""")
    Inspection getByInspection(Inspection inspection);

    @Insert("""
            INSERT INTO inspection
              (id, establishment_id, inspection_date, inspection_type, score)
            VALUES
              (#{id}::uuid, #{establishmentId}::uuid, #{inspectionDate}, #{inspectionType}, #{score});""")
    Integer insertInspection(Inspection inspection);

    @Select("SELECT COUNT(*) FROM inspection;")
    int getCount();
}
