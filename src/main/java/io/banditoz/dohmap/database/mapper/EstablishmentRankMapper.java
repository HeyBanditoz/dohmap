package io.banditoz.dohmap.database.mapper;

import io.banditoz.dohmap.model.EstablishmentRank;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EstablishmentRankMapper {
    @Insert("""
            INSERT INTO establishment_rank
              (id, establishment_id, rank)
            VALUES
              (#{id}::uuid, #{establishmentId}::uuid, #{rank})""")
    Integer insert(EstablishmentRank establishmentRank);
}
