package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.Violation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ViolationMapper {
    @Select("""
            SELECT v.id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation v
            JOIN violation_code_phr vcp on vcp.id = v.violation_code_phr_id
            WHERE id = #{id}::uuid""")
    Violation getById(String id);

    @Select("""
            <script>
            SELECT v.id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation v
            JOIN violation_code_phr vcp on vcp.id = v.violation_code_phr_id
            WHERE inspection_id = #{inspectionId}::uuid
              AND <if test="code != null">code = #{code}</if> <if test="code == null">code IS NULL</if>
              AND <if test="observed != null">observed = #{observed}</if> <if test="observed == null">observed IS NULL</if>
              AND <if test="points != null">points = #{points}</if> <if test="points == null">points IS NULL</if>
              AND <if test="critical != null">critical = #{critical}</if> <if test="critical == null">critical IS NULL</if>
              AND <if test="occurrences != null">occurrences = #{occurrences}</if> <if test="occurrences == null">occurrences IS NULL</if>
              AND <if test="correctedOnSite != null">corrected_on_site = #{correctedOnSite}</if> <if test="correctedOnSite == null">corrected_on_site IS NULL</if>
              AND <if test="publicHealthRationale != null">public_health_rationale = #{publicHealthRationale}</if> <if test="publicHealthRationale == null">public_health_rationale IS NULL</if>
            </script>
            """)
    Violation getByViolation(Violation violation);

    @Insert("""
            INSERT INTO violation
              (id, inspection_id, observed, points, critical, occurrences, corrected_on_site, violation_code_phr_id)
            VALUES
              (#{v.id}::uuid, #{v.inspectionId}::uuid, #{v.observed}, #{v.points}, #{v.critical}, #{v.occurrences}, #{v.correctedOnSite}, #{cPhrId})""")
    Integer insertViolation(Violation v, int cPhrId);

    @Select("INSERT INTO violation_code_phr (code, public_health_rationale) VALUES (#{code}, #{publicHealthRationale}) RETURNING id")
    int insertViolationCodePublicHealthRationale(Violation violation);

    @Select("""
            <script>
            SELECT id
            FROM violation_code_phr
            WHERE <if test="code != null">code = #{code}</if> <if test="code == null">code IS NULL</if>
              AND <if test="publicHealthRationale != null">public_health_rationale = #{publicHealthRationale}</if> <if test="publicHealthRationale == null">public_health_rationale IS NULL</if>
            </script>""")
    Integer getViolationCodePublicHealthRationale(Violation violation);

    @Select("""
            SELECT v.id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation v
            JOIN violation_code_phr vcp on vcp.id = v.violation_code_phr_id
            WHERE inspection_id = #{inspectionId}::uuid
            ORDER BY v.id""")
    List<Violation> getViolationsByInspection(String inspectionId);

    @Select("SELECT COUNT(*) FROM violation;")
    int getCount();

    @Select("""
            SELECT COUNT(*)
            FROM violation v
                     JOIN inspection i ON i.id = v.inspection_id
                     JOIN establishment e on e.id = i.establishment_id
            WHERE e.type ILIKE '%Restaurants%'
               OR e.type ILIKE '%Beverage%'
               OR e.type ILIKE '%breakfast%';""")
    int getRestaurantBeverageCount();

    @Select("""
            SELECT v.id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation v
            JOIN violation_code_phr vcp ON vcp.id = v.violation_code_phr_id
            JOIN inspection i ON v.inspection_id = i.id
            JOIN establishment e ON i.establishment_id = e.id
            WHERE e.id = #{id}::uuid
            ORDER BY v.id""")
    List<Violation> getAllViolationsByEstablishment(String id);
}
