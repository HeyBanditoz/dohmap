package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.EstablishmentSearch;
import io.banditoz.dohmap.model.search.Search;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchMapper {
    @Select("""
            <script>
            SELECT e.id,
                   e.name,
                   e.address,
                   e.city,
                   e.state,
                   e.zip,
                   e.phone,
                   e.type,
                   e.last_seen,
                   e.sys_id,
                   e.source,
                   (SELECT COUNT(*) FROM inspection i WHERE i.establishment_id = e.id) AS inspectionCount,
                   (SELECT COUNT(*) FROM violation v JOIN inspection i ON v.inspection_id = i.id WHERE i.establishment_id = e.id) AS violationCount,
                   (SELECT MAX(inspection_date) FROM inspection i WHERE i.establishment_id = e.id) AS lastInspection
            FROM establishment e
            WHERE (SELECT COUNT(*) FROM inspection i WHERE i.establishment_id = e.id) >= #{search.minimumInspCount}
            <if test="search.search != null">
            AND e.fts @@ websearch_to_tsquery(#{search.search})
            </if>
            <if test="search.sources != null">
            AND
            <foreach item="source" index="idx" collection="search.sources" open="source IN (" separator="," close=")">
                #{source}
            </foreach>
            </if>
            <if test="search.cities != null">
            AND
            <foreach item="city" index="idx" collection="search.cities" open="UPPER(city) IN (" separator="," close=")">
                #{city}
            </foreach>
            </if>
            <choose>
            <when test="search.orderBy.name() == 'LAST_INSPECTION'">
                ORDER BY lastInspection DESC NULLS LAST
            </when>
            <when test="search.orderBy.name() == 'FIRST_SEEN'">
                ORDER BY id ASC
            </when>
            <when test="search.orderBy.name() == 'LAST_SEEN'">
                ORDER BY last_seen DESC
            </when>
            <when test="search.orderBy.name() == 'MOST_INSPECTIONS'">
                ORDER BY inspectionCount DESC
            </when>
            <when test="search.orderBy.name() == 'MOST_VIOLATIONS'">
                ORDER BY violationCount DESC
            </when>
            <when test="search.orderBy.name() == 'LEAST_VIOLATIONS'">
                ORDER BY violationCount ASC
            </when>
            -- TODO move to CTE?
            <when test="search.orderBy.name() == 'MOST_VIOLATIONS_AVG'">
                ORDER BY (
                    (SELECT COUNT(*) FROM violation v JOIN inspection i ON v.inspection_id = i.id WHERE i.establishment_id = e.id)
                        /
                    NULLIF(((SELECT COUNT(*) FROM inspection i WHERE i.establishment_id = e.id)), 0)
                ) DESC NULLS LAST
            </when>
            </choose>
            LIMIT #{limit}
            OFFSET #{offset}
            </script>""")
    List<EstablishmentSearch> getEstablishmentByWebSearchQuery(Search search, int limit, int offset);

    // Note, if there is one parameter passed to a mybatis mapper, you cannot qualify it by its name.
    @Select("""
            <script>
            SELECT COUNT(*)
            FROM establishment e
            WHERE (SELECT COUNT(*) FROM inspection i WHERE i.establishment_id = e.id) >= #{minimumInspCount}
            <if test="search != null">
            AND e.fts @@ websearch_to_tsquery(#{search})
            </if>
            <if test="sources != null">
            AND
            <foreach item="source" index="idx" collection="sources" open="source IN (" separator="," close=")">
                #{source}
            </foreach>
            </if>
            <if test="cities != null">
            AND
            <foreach item="city" index="idx" collection="cities" open="UPPER(city) IN (" separator="," close=")">
                #{city}
            </foreach>
            </if>
            </script>""")
    int countWebSearchQuery(Search search);

    @Select("SELECT DISTINCT(UPPER(city)) AS city FROM establishment ORDER BY city")
    List<String> getCities();
}
