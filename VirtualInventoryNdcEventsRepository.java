package com.capturerx.cumulus4.virtualinventory.repositories;

import com.capturerx.cumulus4.virtualinventory.models.VirtualInventoryNdcEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualInventoryNdcEventsRepository extends JpaRepository<VirtualInventoryNdcEvent, UUID>, JpaSpecificationExecutor {
    List<VirtualInventoryNdcEvent> findAllByVirtualInventoryNdcIdOrderByEventDateAsc(UUID ndcId);
    List<VirtualInventoryNdcEvent> findAllByVirtualInventoryNdcIdAndNdc(UUID ndcId, String ndc);
    VirtualInventoryNdcEvent findByIdAndVirtualInventoryNdcId(UUID id, UUID ndcId);

    @Query("select vide from VirtualInventoryNdcEvent vide order by vide.eventDate asc ")
    List<VirtualInventoryNdcEvent> findByDates();

    List<VirtualInventoryNdcEvent> findAllByCalculatedIsFalseAndVirtualInventoryNdcIdOrderByEventDateAsc(UUID ndcId);

    List<VirtualInventoryNdcEvent> findAllByCalculatedIsTrueAndVirtualInventoryEventTypeEventStatusAndOrderCountGreaterThanAndVirtualInventoryNdcIdOrderByEventDateAsc(String eventStatus, int orderCount , UUID ndcId);

    List<VirtualInventoryNdcEvent> findAllByCalculatedIsTrueAndOrderCountGreaterThanAndVirtualInventoryNdcIdOrderByEventDateAsc(int orderCount, UUID ndcId);

    List<VirtualInventoryNdcEvent> findAllByCalculatedIsTrueAndVirtualInventoryEventTypeEventStatusAndOrderCountGreaterThanAndVirtualInventoryNdcIdAndQuantityGreaterThanOrderByEventDateAsc(String eventStatus, int orderCount , UUID ndcId, int quantity);

    List<VirtualInventoryNdcEvent> findAllByCalculatedIsTrueAndVirtualInventoryEventTypeEventStatusAndOrderCountGreaterThanAndVirtualInventoryNdcIdAndQuantityLessThanOrderByEventDateAsc(String eventStatus, int orderCount , UUID ndcId, int quantity);

    VirtualInventoryNdcEvent findByDispenseIdAndVirtualInventoryEventTypeEventStatus(UUID dispenseId, String eventStatus);

    @Query("select vine from VirtualInventoryNdcEvent vine where vine.dispenseId=:dispenseId and vine.dispenseId is not null order by vine.eventDate asc")
    List<VirtualInventoryNdcEvent> findByDispenseIdIsNotNull(@Param("dispenseId") UUID dispenseId);

    List<VirtualInventoryNdcEvent> findAllByOrderIdOrderByEventDateAsc(UUID orderId);

    List<VirtualInventoryNdcEvent> findAllByOrderIdAndVirtualInventoryEventTypeEventStatusOrderByEventDateAsc(UUID orderId, String eventStatus);

    VirtualInventoryNdcEvent findByOrderIdAndVirtualInventoryEventTypeEventStatus(UUID orderId, String eventStatus);

    Boolean existsByDispenseIdAndVirtualInventoryEventTypeEventStatus(UUID dispenseId, String status);

    @Query("select vine from VirtualInventoryNdcEvent vine where vine.dispenseSetId=:dispenseSetId and vine.dispenseSetId is not null")
    List<VirtualInventoryNdcEvent> findAllByDispenseSetId(@Param("dispenseSetId") UUID dispenseSetId);

}
