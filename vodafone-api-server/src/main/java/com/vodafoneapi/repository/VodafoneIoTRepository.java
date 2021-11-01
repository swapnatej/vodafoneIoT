package com.vodafoneapi.repository;

import com.vodafoneapi.entity.VodafoneIoT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VodafoneIoTRepository extends JpaRepository<VodafoneIoT,Long> {

    @Query(value = "SELECT * FROM VODAFONE_IOT   " +
            "            WHERE PRODUCT_ID = :productId " +
            "            AND DATE_TIME <= (SELECT MAX(E.DATE_TIME ) FROM VODAFONE_IOT   E WHERE E.DATE_TIME <=:dateTime) " +
            "            ORDER BY DATE_TIME DESC  " +
            "            LIMIT 3 ", nativeQuery  =  true)
    List<VodafoneIoT> findByIdAndDateTimeList(@Param("productId") String productId,
                                         @Param("dateTime") LocalDateTime dateTime);
}
