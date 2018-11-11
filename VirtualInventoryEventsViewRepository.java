package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryEventsView;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualInventoryEventsViewRepository extends JpaRepository<VirtualInventoryEventsView,String>{

    List<VirtualInventoryEventsView> findTop500ByNdcIdOrderByEventDateDesc(UUID ndcId);

   List<VirtualInventoryEventsView> findTop500ByNdcIdAndEventDateLessThanEqualAndEventTypeIsNotLikeOrderByEventDateDesc(@Param("ndcId") UUID ndcId, @Param("eventAsOfDate") DateTime eventAsOfDate, @Param("eventType") String eventType);
}
