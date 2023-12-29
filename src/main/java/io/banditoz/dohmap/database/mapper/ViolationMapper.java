package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.scraper.model.Violation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ViolationMapper {
    @Select("""
            SELECT id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation
            WHERE id = #{id}::uuid""")
    Violation getById(String id);

    @Select("""
            <script>
            SELECT id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation
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
              (id, inspection_id, code, observed, points, critical, occurrences, corrected_on_site, public_health_rationale)
            VALUES
              (#{id}::uuid, #{inspectionId}::uuid, #{code}, #{observed}, #{points}, #{critical}, #{occurrences}, #{correctedOnSite}, #{publicHealthRationale})""")
    Integer insertInspection(Violation violation);

    @Select("""
            SELECT id,
                   inspection_id,
                   code,
                   observed,
                   points,
                   critical,
                   occurrences,
                   corrected_on_site,
                   public_health_rationale
            FROM violation
            WHERE inspection_id = #{inspectionId}::uuid
            ORDER BY id""")
    List<Violation> getViolationsByInspection(String inspectionId);

    @Select("SELECT COUNT(*) FROM violation;")
    int getCount();
}
