package com.capturerx.cumulus4.virtualinventory.impls;

import com.capturerx.cumulus4.virtualinventory.configuration.VirtualInventorySecurityConfiguration;
import com.capturerx.cumulus4.virtualinventory.dtos.*;
import com.capturerx.cumulus4.virtualinventory.errors.AccessForbidenException;
import com.capturerx.cumulus4.virtualinventory.errors.ErrorConstants;
import com.capturerx.cumulus4.virtualinventory.errors.InternalServerException;
import com.capturerx.cumulus4.virtualinventory.messages.AcknowledgementMessageDTO;
import com.capturerx.cumulus4.virtualinventory.messages.PurchaseOrderMessage;
import com.capturerx.cumulus4.virtualinventory.messages.SummaryMessage;
import com.capturerx.cumulus4.virtualinventory.models.*;
import com.capturerx.cumulus4.virtualinventory.repositories.*;
import com.capturerx.cumulus4.virtualinventory.security.KeycloakClientCredentialsConfig;
import com.capturerx.cumulus4.virtualinventory.security.KeycloakClientCredentialsRestTemplate;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryEventTypeService;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryNdcEventsService;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryEventsViewMapper;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryNdcEventsMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class VirtualInventoryNdcEventsServiceImpl implements VirtualInventoryNdcEventsService {

    Logger logger = Logger.getLogger(VirtualInventoryNdcEventsServiceImpl.class.getName());

    private static final String PREEXIST = "PreExist";
    private static final String PREBUY = "PreBuy";
    private static final String OVER_REPLENISHMENT ="Over Replenishment";
    private static final String APPROVED = "Approved";
    private static final String REVERSED = "Reversed";
    private static final String ORDERED = "Ordered";
    private static final String BACKFILL = "BackFill";
    private static final String STABILIZER = "Stabilizer";
    private static final String ACKNOWLEDGEMENT = "Acknowledgement";
    private static final String INVOICE = "Order Invoiced";
    private static final String ORDER_INVOICED = "Invoiced";
    private static final String CREDIT_MEMO = "CreditMemo";
    private static final String REVERSE_STABILIZER = "Reverse Stabilizer";
    private static final String ORDER_IMPACT = "OrderImpact";
    private static final String UNDER_REPLENISHMENT = "Under Replenishment";
    private static final String MANUAL_ADJUSTMENT = "Manual Adjustment";
    private static final String SHIP_TO_CODE = "CF";

    private static final String EMPTY = " ";

    @Value("${externalapi.medispanapi}")
    private String medispanApi;

    @Autowired
    private VirtualInventorySecurityConfiguration virtualInventorySecurityConfiguration;
    private static final String URL_PATH = "secureusers/user/";

    @Autowired
    private KeycloakClientCredentialsConfig restTemplateFactory;
    private final VirtualInventoryNdcEventsRepository virtualInventoryEventsRepository;
    private final VirtualInventoryNdcEventsMapper virtualInventoryNdcEventsMapper;
    private final VirtualInventoryNdcRepository virtualInventoryNdcRepository;
    private final VirtualInventoryEventTypeService virtualInventoryEventTypeService;
    private final VirtualInventoryRepository virtualInventoryRepository;
    private final VirtualInventoryNdcViewRepository virtualInventoryNdcViewRepository;
    private final VirtualInventoryEventsViewMapper virtualInventoryEventsViewMapper;

    public VirtualInventoryNdcEventsServiceImpl(VirtualInventoryNdcEventsRepository virtualInventoryEventsRepository, VirtualInventoryRepository virtualInventoryRepository,
                                                VirtualInventoryNdcEventsMapper virtualInventoryNdcEventsMapper, VirtualInventoryNdcRepository virtualInventoryNdcRepository, VirtualInventoryEventTypeService virtualInventoryEventTypeService, VirtualInventoryNdcViewRepository virtualInventoryNdcViewRepository, VirtualInventoryEventsViewMapper virtualInventoryEventsViewMapper) {
        this.virtualInventoryEventsRepository = virtualInventoryEventsRepository;
        this.virtualInventoryNdcEventsMapper = virtualInventoryNdcEventsMapper;

        this.virtualInventoryNdcRepository = virtualInventoryNdcRepository;
        this.virtualInventoryEventTypeService = virtualInventoryEventTypeService;
        this.virtualInventoryRepository = virtualInventoryRepository;
        this.virtualInventoryNdcViewRepository = virtualInventoryNdcViewRepository;
        this.virtualInventoryEventsViewMapper = virtualInventoryEventsViewMapper;
    }

    public void updateEventsAndNdc(VirtualInventoryNdc virtualInventoryNdc){
        List<VirtualInventoryNdcEventDTO> eventDTOList = new ArrayList<>();
        List<VirtualInventoryNdcEventDTO> virtualInventoryNdcEventDTOS = getByNdcId(virtualInventoryNdc.getId());
        int total = virtualInventoryNdc.getRunningTotal();
        for (VirtualInventoryNdcEventDTO inventoryNdcEvents : virtualInventoryNdcEventDTOS) {
            if(!("Invoiced".equalsIgnoreCase(inventoryNdcEvents.getVirtualInventoryEventsTypeEventStatus()))) {
                total = total + inventoryNdcEvents.getQuantity();
                inventoryNdcEvents.setVirtualInventoryUnits(total);
                inventoryNdcEvents.setCalculated(true);
                eventDTOList.add(inventoryNdcEvents);
            }
        }
        if (virtualInventoryNdcEventDTOS.size() != 0) {
            updateList(eventDTOList);
        }
        if ((virtualInventoryNdc.getPackageSize() == 0) && (virtualInventoryNdc.getDosageForm() == null) && (virtualInventoryNdc.getDrugDescription() == null)) {
            MedispanDrug medispanDrug = getDrugInformationForNDC(virtualInventoryNdc.getNdc());
            virtualInventoryNdc.setPackageSize(Short.parseShort(medispanDrug.getPackageSize()));
            virtualInventoryNdc.setDosageForm(medispanDrug.getDosageForm());
            String description = medispanDrug.getDrugName() + EMPTY + medispanDrug.getDosageForm() + EMPTY + medispanDrug.getStrength() + EMPTY + medispanDrug.getStrengthUnitOfMeasure();
            virtualInventoryNdc.setDrugDescription(description);
        }
        int orderablePackages = 0;
        if (total > 0) {
            orderablePackages = total / virtualInventoryNdc.getPackageSize();
        }
        virtualInventoryNdc.setRunningTotal(total);
        virtualInventoryNdc.setLastCalculatedDate(DateTime.now(DateTimeZone.UTC));
        if (orderablePackages >= 0)
            virtualInventoryNdc.setPackageQuantity(orderablePackages);
        virtualInventoryNdcRepository.flush();
    }

    public MedispanDrug getDrugInformationForNDC(String ndc){
        MedispanDrug drug = null;
        try {
            KeycloakClientCredentialsRestTemplate restTemplate = restTemplateFactory.createRestTemplate();
            String url = medispanApi + "/{ndc}";
            UriComponentsBuilder uriComponentsBuilder;
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("ndc", ndc);
            uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
            URI medispanUri = uriComponentsBuilder.buildAndExpand(hashMap).toUri();
            drug = restTemplate.getForObject(medispanUri, MedispanDrug.class);
        } catch (Exception e){
            logger.info("Exception while getting drug details "+e);
        }
        return drug;
    }

    private ResponseEntity<UserRepresentationDTO> getUserRepresentationDetails(String builderUrlPath)
    {
        Map entityMap = virtualInventorySecurityConfiguration.getEntity(builderUrlPath);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserRepresentationDTO> userRepresentationDTO = restTemplate.exchange(entityMap.get("builder").toString(), HttpMethod.GET,
                (HttpEntity) entityMap.get("entity"),UserRepresentationDTO.class);

        return userRepresentationDTO;
    }

    private VirtualInventoryNdc getVirtualInventoryNdcByNdcAndBillToAndShipTo(String ndc, UUID billToId, UUID shipToId, String shipToCode){
        VirtualInventoryNdc virtualInventoryNdc;
        VirtualInventoryNdcUiView virtualInventoryNdcUiView = virtualInventoryNdcViewRepository.findByNdcAndBillToIdAndShipToId(ndc, billToId, shipToId);
        if (virtualInventoryNdcUiView == null) {
            virtualInventoryNdc = new VirtualInventoryNdc();
            VirtualInventoryHeader header;
            //validating the centralfill condition
            if(SHIP_TO_CODE.equalsIgnoreCase(shipToCode))
                header = virtualInventoryRepository.findByCoveredEntityBillToIdAndCentralFillShipToId(billToId, shipToId);
            else
                header = virtualInventoryRepository.findByCoveredEntityBillToIdAndPharmacyShipToId(billToId, shipToId);

            virtualInventoryNdc.setNdc(ndc);
            virtualInventoryNdc.setLastCalculatedDate(DateTime.now(DateTimeZone.UTC));
            virtualInventoryNdc.setVirtualInventoryHeader(header);
            virtualInventoryNdcRepository.save(virtualInventoryNdc);
        } else
            virtualInventoryNdc = virtualInventoryNdcUiView.getVirtualInventoryNdc();
        return virtualInventoryNdc;
    }

    private void reversalLogic(List<VirtualInventoryNdcEvent> result, SummaryMessage message, VirtualInventoryNdcEvent messageEvent){
        List<VirtualInventoryNdcEvent> dispenseEvents = getByDispenseId(message.getDispenseId());
        if(dispenseEvents == null || dispenseEvents.isEmpty())
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_FOUND_BY_DIPSENSE_ID, message.getDispenseId()));
        VirtualInventoryEventType stabilizerEventsType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(STABILIZER);
        VirtualInventoryEventType orderImpactEventsType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(ORDER_IMPACT);
        if (stabilizerEventsType == null || orderImpactEventsType == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE));
        int inc = 0;
        boolean canStabilize = false;
        VirtualInventoryNdcEvent orderEvent = null;
        for (VirtualInventoryNdcEvent event : dispenseEvents) {
            messageEvent.setQuantity(0 - message.getApprovedQuantity());
            messageEvent.setOrderCount(0);
            event.setOrderCount(0);
            if (event.getOrderFlag()) {
                messageEvent.setOrderFlag(true);
                //create order impact and stabilizer events
                if (ORDERED.equalsIgnoreCase(event.getVirtualInventoryEventType().getEventStatus()) || BACKFILL.equalsIgnoreCase(event.getVirtualInventoryEventType().getEventStatus())) {
                    orderEvent = event;
                    canStabilize = true;
                } else if ((ACKNOWLEDGEMENT.equalsIgnoreCase(event.getVirtualInventoryEventType().getEventStatus())) || (UNDER_REPLENISHMENT.equalsIgnoreCase(event.getVirtualInventoryEventType().getEventStatus()))) {
                    canStabilize = false;
                }
            } else
                messageEvent.setOrderFlag(false);
            result.add(event);
        }
        if (canStabilize) {
            VirtualInventoryNdcEvent stabilizerNdcEvent = new VirtualInventoryNdcEvent();
            VirtualInventoryNdcEvent orderImpactEvent = new VirtualInventoryNdcEvent();
            stabilizerNdcEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++inc));
            orderImpactEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++inc));
            List<VirtualInventoryNdcEvent> ndcEventsList = stabilizerEvents(orderEvent, stabilizerEventsType, orderImpactEventsType, stabilizerNdcEvent, orderImpactEvent);
            result.addAll(ndcEventsList);
        }
        if (!dispenseEvents.isEmpty()) {
            result.add(messageEvent);
        }
    }

    public List<VirtualInventoryNdcEventDTO> postMessage(SummaryMessage message) {
        if(message == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_SUMMARY_MESSAGE));
        List<VirtualInventoryNdcEvent> result = new ArrayList<>();
        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = new ArrayList<>();
        VirtualInventoryNdcEvent messageEvent = new VirtualInventoryNdcEvent();
        try {
            VirtualInventoryNdc virtualInventoryNdc = getVirtualInventoryNdcByNdcAndBillToAndShipTo(message.getNdcNumber(), message.getBillToId(), message.getShipToId(),message.getShipToCode());
            messageEvent.setVirtualInventoryNdc(virtualInventoryNdc);
            messageEvent.setDispenseId(message.getDispenseId());
            messageEvent.setNdc(message.getNdcNumber());
            messageEvent.setDispenseSetId(UUID.fromString(message.getDispenseSetId()));
            messageEvent.setReferenceNumber(message.getReferenceNumber());
            messageEvent.setEventDate(DateTime.now(DateTimeZone.UTC));
            messageEvent.setContractId(message.getContractId());
            messageEvent.setPayorType(message.getPrimaryPlanType());
            VirtualInventoryEventType eventTypeObj = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(message.getDispenseStatus());
            logger.info(eventTypeObj.toString());
            messageEvent.setVirtualInventoryEventType(eventTypeObj);
            messageEvent.setCalculated(false);

            if (APPROVED.equalsIgnoreCase(message.getDispenseStatus()) && (!virtualInventoryEventsRepository.existsByDispenseIdAndVirtualInventoryEventTypeEventStatus(message.getDispenseId(), APPROVED))) {
                messageEvent.setQuantity(message.getApprovedQuantity());
                messageEvent.setOrderCount(message.getApprovedQuantity());
                messageEvent.setApprovedDate(message.getDateCreated());
                messageEvent.setOrderFlag(false);
                result.add(messageEvent);
            } else if (REVERSED.equalsIgnoreCase(message.getDispenseStatus()) && (!virtualInventoryEventsRepository.existsByDispenseIdAndVirtualInventoryEventTypeEventStatus(message.getDispenseId(), REVERSED))) {
                reversalLogic(result, message, messageEvent);
            }
            virtualInventoryNdcEvents = virtualInventoryEventsRepository.save(result);
        }
        catch (Exception e){
            logger.info("Exception occurred while saving approved and reversal "+e);
        }
        if (virtualInventoryNdcEvents == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_SAVED, virtualInventoryNdcEvents));
        return virtualInventoryNdcEvents.stream().map(virtualInventoryNdcEventsMapper :: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryNdcEventDTO> invoiceLogic(AcknowledgementMessageDTO messageDTO){
        if(messageDTO == null){
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_SUMMARY_MESSAGE));
        }
        List<VirtualInventoryNdcEventDTO> ndcEventDTOS = new ArrayList<>();
        try {
            List<VirtualInventoryNdcEvent> result = new ArrayList<>();
            if(INVOICE.equalsIgnoreCase(messageDTO.getMsgStatus())) {
                List<VirtualInventoryNdcEvent> orderEvents = virtualInventoryEventsRepository.findAllByOrderIdAndVirtualInventoryEventTypeEventStatusOrderByEventDateAsc(messageDTO.getOrderId(), ORDERED);
                if(orderEvents == null)
                    throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_ORDERS, messageDTO.getNdc()));
                int totalQuantity = 0;
                VirtualInventoryNdcEvent invoiceEvent = virtualInventoryEventsRepository.findByOrderIdAndVirtualInventoryEventTypeEventStatus(messageDTO.getOrderId(), "Invoiced");
                for (VirtualInventoryNdcEvent virtualInventoryNdcEvent : orderEvents) {
                    totalQuantity = totalQuantity + Math.abs(virtualInventoryNdcEvent.getQuantity());
                }
                VirtualInventoryNdcUiView virtualInventoryNdcUiView = virtualInventoryNdcViewRepository.findByNdcAndBillToIdAndShipToId(messageDTO.getNdc(), messageDTO.getBillToId(), messageDTO.getShipToId());
                if(virtualInventoryNdcUiView == null)
                    throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_HEADER));
                int orderSize = totalQuantity / virtualInventoryNdcUiView.getPackageSize();
                VirtualInventoryEventType urEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(UNDER_REPLENISHMENT);
                if(urEventType == null)
                    throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE, urEventType));
                if (invoiceEvent!=null && !invoiceEvent.getOrderFlag()) {
                    if (orderSize > messageDTO.getBottleSize()) {
                        int addQuantity = (orderSize - messageDTO.getBottleSize()) * virtualInventoryNdcUiView.getPackageSize();
                        List<VirtualInventoryNdcEvent> events = createUnderReplenishment(orderEvents, totalQuantity, virtualInventoryNdcUiView.getVirtualInventoryNdc(), addQuantity, invoiceEvent, urEventType);
                        result.addAll(events);
                    } else if (orderSize < messageDTO.getBottleSize()) {
                        int removeQuantity = (messageDTO.getBottleSize() - orderSize) * virtualInventoryNdcUiView.getPackageSize();
                        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvent = createOverReplenishmentEvent(virtualInventoryNdcUiView.getVirtualInventoryNdc(), removeQuantity, invoiceEvent);
                        result.addAll(virtualInventoryNdcEvent);
                    }
                } else {
                    List<VirtualInventoryNdcEvent> virtualInventoryNdcEvent = createOverReplenishmentEvent(virtualInventoryNdcUiView.getVirtualInventoryNdc(), messageDTO.getBottleSize() * virtualInventoryNdcUiView.getPackageSize(), invoiceEvent);
                    result.addAll(virtualInventoryNdcEvent);
                }
            }
            else {
                List<VirtualInventoryNdcEvent> creditMemoEvents = createCreditMemoEvents(messageDTO);
                result.addAll(creditMemoEvents);
            }
            List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = virtualInventoryEventsRepository.save(result);
            if (virtualInventoryNdcEvents == null)
                throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_SAVED));
            ndcEventDTOS= virtualInventoryNdcEvents.stream().map(virtualInventoryNdcEventsMapper::toDto).collect(Collectors.toList());
        }
        catch (Exception e){
            logger.info("Exception while creating invoice events "+e);
        }
        return ndcEventDTOS;
    }

    private List<VirtualInventoryNdcEvent> createOverReplenishmentEvent(VirtualInventoryNdc virtualInventoryNdc, int removeQuantity, VirtualInventoryNdcEvent invoiceEvent) {
        List<VirtualInventoryNdcEvent> events = new ArrayList<>();
        VirtualInventoryEventType overReplenishmentEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(OVER_REPLENISHMENT);
        if(overReplenishmentEventType == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE, overReplenishmentEventType));
        VirtualInventoryNdcEvent overReplenishmentEvent = new VirtualInventoryNdcEvent();
        overReplenishmentEvent.setNdc(virtualInventoryNdc.getNdc());
        overReplenishmentEvent.setVirtualInventoryNdc(virtualInventoryNdc);
        overReplenishmentEvent.setOrderCount(removeQuantity);
        overReplenishmentEvent.setOrderId(invoiceEvent.getOrderId());
        overReplenishmentEvent.setCalculated(false);
        overReplenishmentEvent.setEventDate(DateTime.now(DateTimeZone.UTC));
        overReplenishmentEvent.setQuantity(0-removeQuantity);
        overReplenishmentEvent.setOrderFlag(true);
        overReplenishmentEvent.setVirtualInventoryEventType(overReplenishmentEventType);
        overReplenishmentEvent.setOrderNumber(invoiceEvent.getOrderNumber());
        if(!invoiceEvent.getOrderFlag()){
            invoiceEvent.setOrderFlag(true);
            events.add(invoiceEvent);
        }
        events.add(overReplenishmentEvent);
        return events;
    }

    @Override
    public List<VirtualInventoryNdcEvent> getSortedEvents(List<VirtualInventoryNdcEvent> approvedEvents, List<VirtualInventoryNdcEvent> urEvents) {
        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = new ArrayList<VirtualInventoryNdcEvent>();
        virtualInventoryNdcEvents.addAll(approvedEvents);
        virtualInventoryNdcEvents.addAll(urEvents);
        Collections.sort(virtualInventoryNdcEvents, new Comparator<VirtualInventoryNdcEvent>() {
            @Override
            public int compare(VirtualInventoryNdcEvent o1, VirtualInventoryNdcEvent o2) {
                return o1.getEventDate().compareTo(o2.getEventDate());
            }
        });
        return virtualInventoryNdcEvents;
    }

    @Override
    public List<VirtualInventoryNdcEventDTO> createAcknowledgementEvents(AcknowledgementMessageDTO messageDTO) {
        if(messageDTO == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_SUMMARY_MESSAGE));
        List<VirtualInventoryNdcEvent> resultAckEvents = new ArrayList<>();
        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = new ArrayList<>();
        try {
            VirtualInventoryNdcUiView virtualInventoryNdcUiView = virtualInventoryNdcViewRepository.findByNdcAndBillToIdAndShipToId(messageDTO.getNdc(), messageDTO.getBillToId(), messageDTO.getShipToId());
            if (virtualInventoryNdcUiView == null)
                throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_HEADER));
            List<VirtualInventoryNdcEvent> orderedEvents = virtualInventoryEventsRepository.findAllByOrderIdAndVirtualInventoryEventTypeEventStatusOrderByEventDateAsc(messageDTO.getOrderId(), ORDERED);
            if (orderedEvents == null)
                throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_ORDERS));
            VirtualInventoryEventType ackEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(ACKNOWLEDGEMENT);
            if (ackEventType == null)
                throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE, ackEventType));
            if (messageDTO.getBottleSize() == 0) {
                int i = 0;
                for (VirtualInventoryNdcEvent orderEvent : orderedEvents) {
                    VirtualInventoryNdcEvent ackEvent = new VirtualInventoryNdcEvent();
                    ackEvent.setOrderId(orderEvent.getOrderId());
                    ackEvent.setVirtualInventoryNdc(virtualInventoryNdcUiView.getVirtualInventoryNdc());
                    ackEvent.setNdc(orderEvent.getNdc());
                    ackEvent.setOrderCount(Math.abs(orderEvent.getQuantity()));
                    ackEvent.setQuantity(Math.abs(orderEvent.getQuantity()));
                    ackEvent.setCalculated(false);
                    ackEvent.setDispenseId(orderEvent.getDispenseId());
                    ackEvent.setDispenseSetId(orderEvent.getDispenseSetId());
                    ackEvent.setReferenceNumber(orderEvent.getReferenceNumber());
                    ackEvent.setOrderNumber(orderEvent.getOrderNumber());
                    ackEvent.setOrderFlag(true);
                    ackEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
                    ackEvent.setVirtualInventoryEventType(ackEventType);
                    resultAckEvents.add(ackEvent);
                }
            }
            virtualInventoryNdcEvents = virtualInventoryEventsRepository.save(resultAckEvents);
        } catch (Exception e){
            logger.info("Exception while creating acknowledgement events "+e);
        }
        if (virtualInventoryNdcEvents == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_SAVED, virtualInventoryNdcEvents));
        return virtualInventoryNdcEvents.stream().map(virtualInventoryNdcEventsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryNdcEventDTO> postPreExist(VirtualInventoryManualAdjustmentDTO virtualInventoryManualAdjustmentDTO) {
        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = new ArrayList<>();
        List<String> ndcList = new ArrayList<String>();
        for(VirtualInventoryManualEventDTO virtualInventoryManualEventDTO: virtualInventoryManualAdjustmentDTO.getVirtualInventoryNdcEvents()){
            ndcList.add(virtualInventoryManualEventDTO.getNdc());
        }
        VirtualInventoryEventType preExistEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(PREEXIST);
        if(preExistEventType == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE, preExistEventType));
        List<VirtualInventoryNdcUiView> ndcUiViews = virtualInventoryNdcViewRepository.findAllByBillToIdAndShipToIdAndNdcIn(virtualInventoryManualAdjustmentDTO.getBillToId(), virtualInventoryManualAdjustmentDTO.getShipToId(),ndcList);
        if(ndcUiViews == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_HEADER));
        int i=0;
        for(VirtualInventoryNdcUiView virtualInventoryNdcUiView: ndcUiViews){
            for(VirtualInventoryManualEventDTO virtualInventoryManualEventDTO: virtualInventoryManualAdjustmentDTO.getVirtualInventoryNdcEvents()){
                if(virtualInventoryNdcUiView.getNdc().equalsIgnoreCase(virtualInventoryManualEventDTO.getNdc())){
                    VirtualInventoryNdcEvent preExistEvent = new VirtualInventoryNdcEvent();
                    preExistEvent.setNdc(virtualInventoryManualEventDTO.getNdc());
                    preExistEvent.setQuantity(0-virtualInventoryManualEventDTO.getQuantity());
                    preExistEvent.setVirtualInventoryNdc(virtualInventoryNdcUiView.getVirtualInventoryNdc());
                    preExistEvent.setOrderCount(virtualInventoryManualEventDTO.getQuantity());
                    preExistEvent.setReferenceNumber(virtualInventoryManualEventDTO.getReason());
                    preExistEvent.setOrderFlag(false);
                    preExistEvent.setVirtualInventoryEventType(preExistEventType);
                    preExistEvent.setCalculated(false);
                    preExistEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
                    virtualInventoryNdcEvents.add(preExistEvent);
                    virtualInventoryManualAdjustmentDTO.getVirtualInventoryNdcEvents().remove(virtualInventoryManualEventDTO);
                    break;
                }
            }
            if(virtualInventoryManualAdjustmentDTO.getVirtualInventoryNdcEvents().isEmpty()){
                break;
            }
        }
        List<VirtualInventoryNdcEvent> preExistEvents = virtualInventoryEventsRepository.save(virtualInventoryNdcEvents);
        if(preExistEvents == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_SAVED));
        return preExistEvents.stream().map(virtualInventoryNdcEventsMapper :: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryNdcEvent> createCreditMemoEvents(AcknowledgementMessageDTO messageDTO) {
        if(messageDTO == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_SUMMARY_MESSAGE));
        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = new ArrayList<>();
        VirtualInventoryNdcUiView virtualInventoryNdcUiView = virtualInventoryNdcViewRepository.findByNdcAndBillToIdAndShipToId(messageDTO.getNdc(), messageDTO.getBillToId(), messageDTO.getShipToId());
        if(virtualInventoryNdcUiView == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_HEADER));
        List<VirtualInventoryNdcEvent> orderedEvents = virtualInventoryEventsRepository.findAllByOrderIdAndVirtualInventoryEventTypeEventStatusOrderByEventDateAsc(messageDTO.getOrderId(), ORDERED);
        if(orderedEvents == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_ORDERS, messageDTO.getNdc()));
        VirtualInventoryEventType creditMemoEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(CREDIT_MEMO);
        if(creditMemoEventType == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE, creditMemoEventType));
        if(messageDTO.getOrderId() == null){
            VirtualInventoryNdcEvent creditMemoEvent = new VirtualInventoryNdcEvent();
            creditMemoEvent.setNdc(messageDTO.getNdc());
            creditMemoEvent.setVirtualInventoryNdc(virtualInventoryNdcUiView.getVirtualInventoryNdc());
            creditMemoEvent.setQuantity((virtualInventoryNdcUiView.getPackageSize())*(messageDTO.getBottleSize()));
            creditMemoEvent.setEventDate(DateTime.now(DateTimeZone.UTC));
            creditMemoEvent.setOrderNumber(messageDTO.getPurchaseOrderNumber());
            creditMemoEvent.setVirtualInventoryEventType(creditMemoEventType);
            creditMemoEvent.setCalculated(false);
            creditMemoEvent.setOrderCount((virtualInventoryNdcUiView.getPackageSize())*(messageDTO.getBottleSize()));
            creditMemoEvent.setOrderFlag(false);
            virtualInventoryNdcEvents.add(creditMemoEvent);
        }
        else{
            int totalQuantity = 0;
            for(VirtualInventoryNdcEvent orderEvent: orderedEvents){
                totalQuantity = totalQuantity+Math.abs(orderEvent.getQuantity());
            }
            List<VirtualInventoryNdcEvent> creditMemoEvents = createUnderReplenishment(orderedEvents, totalQuantity, virtualInventoryNdcUiView.getVirtualInventoryNdc(), messageDTO.getBottleSize()*virtualInventoryNdcUiView.getPackageSize(), null, creditMemoEventType);
            virtualInventoryNdcEvents.addAll(creditMemoEvents);
        }
        return virtualInventoryNdcEvents;
    }

    @Override
    public List<VirtualInventoryNdcEventDTO> getAllByDispenseSetId(UUID dispenseSetId) {
        List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents = virtualInventoryEventsRepository.findAllByDispenseSetId(dispenseSetId);
        if(virtualInventoryNdcEvents == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_FOUND_FOR_NDC));
        return virtualInventoryNdcEvents.stream().map(virtualInventoryNdcEventsMapper :: toDto).collect(Collectors.toList());
    }

    private List<VirtualInventoryNdcEvent> createUnderReplenishment(List<VirtualInventoryNdcEvent> orderEvents, int totalQuantity, VirtualInventoryNdc virtualInventoryNdc, int addQuantity, VirtualInventoryNdcEvent invoiceEvent, VirtualInventoryEventType urEventType) {
        List<VirtualInventoryNdcEvent> resultEvents = new ArrayList<>();
        int dispenseQuantity, i=0;
        for (VirtualInventoryNdcEvent orderEvent : orderEvents) {
            if (Math.abs(totalQuantity-Math.abs(orderEvent.getQuantity())) < addQuantity) {
                dispenseQuantity = Math.abs(addQuantity-Math.abs(totalQuantity-Math.abs(orderEvent.getQuantity())));
                addQuantity = addQuantity - dispenseQuantity;
                VirtualInventoryNdcEvent underReplenishmentEvent = new VirtualInventoryNdcEvent();
                underReplenishmentEvent.setQuantity(dispenseQuantity);
                underReplenishmentEvent.setOrderCount(dispenseQuantity);
                underReplenishmentEvent.setVirtualInventoryNdc(virtualInventoryNdc);
                underReplenishmentEvent.setVirtualInventoryEventType(urEventType);
                underReplenishmentEvent.setDispenseId(orderEvent.getDispenseId());
                underReplenishmentEvent.setDispenseSetId(orderEvent.getDispenseSetId());
                underReplenishmentEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
                underReplenishmentEvent.setReferenceNumber(orderEvent.getReferenceNumber());
                underReplenishmentEvent.setNdc(orderEvent.getNdc());
                underReplenishmentEvent.setOrderNumber(orderEvent.getOrderNumber());
                underReplenishmentEvent.setOrderFlag(false);
                underReplenishmentEvent.setCalculated(false);
                resultEvents.add(underReplenishmentEvent);
                if(invoiceEvent != null) {
                    invoiceEvent.setOrderFlag(true);
                    resultEvents.add(invoiceEvent);
                }
            }
            totalQuantity = totalQuantity - Math.abs(orderEvent.getQuantity());
        }
        return resultEvents;
    }

    private List<VirtualInventoryNdcEvent> stabilizerEvents(VirtualInventoryNdcEvent virtualInventoryNdcEvent, VirtualInventoryEventType stabilizerEventsType, VirtualInventoryEventType orderImpactEventsType, VirtualInventoryNdcEvent stabilizerNdcEvent, VirtualInventoryNdcEvent orderImpactEvent) {
        //Set Stabilizer Event
        stabilizerNdcEvent.setVirtualInventoryNdc(virtualInventoryNdcEvent.getVirtualInventoryNdc());
        stabilizerNdcEvent.setNdc(virtualInventoryNdcEvent.getNdc());
        stabilizerNdcEvent.setCalculated(false);
        stabilizerNdcEvent.setOrderCount(Math.abs(virtualInventoryNdcEvent.getQuantity()));
        stabilizerNdcEvent.setOrderId(virtualInventoryNdcEvent.getOrderId());
        stabilizerNdcEvent.setQuantity(0-virtualInventoryNdcEvent.getQuantity());
        stabilizerNdcEvent.setOrderFlag(true);
        stabilizerNdcEvent.setReferenceNumber(virtualInventoryNdcEvent.getReferenceNumber());
        stabilizerNdcEvent.setDispenseId(virtualInventoryNdcEvent.getDispenseId());
        stabilizerNdcEvent.setDispenseSetId(virtualInventoryNdcEvent.getDispenseSetId());
        stabilizerNdcEvent.setVirtualInventoryEventType(stabilizerEventsType);
        stabilizerNdcEvent.setOrderNumber(virtualInventoryNdcEvent.getOrderNumber());
        //Set Order Impact Event
        orderImpactEvent.setVirtualInventoryNdc(virtualInventoryNdcEvent.getVirtualInventoryNdc());
        orderImpactEvent.setNdc(virtualInventoryNdcEvent.getNdc());
        orderImpactEvent.setCalculated(false);
        orderImpactEvent.setOrderCount(0);
        orderImpactEvent.setOrderId(virtualInventoryNdcEvent.getOrderId());
        orderImpactEvent.setQuantity(virtualInventoryNdcEvent.getQuantity());
        orderImpactEvent.setOrderFlag(true);
        orderImpactEvent.setReferenceNumber(virtualInventoryNdcEvent.getReferenceNumber());
        orderImpactEvent.setDispenseId(virtualInventoryNdcEvent.getDispenseId());
        orderImpactEvent.setDispenseSetId(virtualInventoryNdcEvent.getDispenseSetId());
        orderImpactEvent.setVirtualInventoryEventType(orderImpactEventsType);
        orderImpactEvent.setOrderNumber(virtualInventoryNdcEvent.getOrderNumber());
        return Arrays.asList(stabilizerNdcEvent, orderImpactEvent);
    }

    public List<VirtualInventoryNdcEventDTO> saveOrders(PurchaseOrderMessage purchaseOrderMessage){
        if(purchaseOrderMessage == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_SUMMARY_MESSAGE));
        List<VirtualInventoryNdcEvent> inventoryNdcEventsResult = new ArrayList<>();
        List<VirtualInventoryNdcEvent> result = new ArrayList<>();
        try {
            VirtualInventoryNdc virtualInventoryNdc = getVirtualInventoryNdcByNdcAndBillToAndShipTo(purchaseOrderMessage.getNdc(), purchaseOrderMessage.getBillTo(), purchaseOrderMessage.getShipTo(), null);
            updateEventsAndNdc(virtualInventoryNdc);
            int orderedQuantity = purchaseOrderMessage.getPackageQuantityAtOrder() * virtualInventoryNdc.getPackageSize();
            List<VirtualInventoryNdcEvent> allEvents = virtualInventoryEventsRepository.findAllByCalculatedIsTrueAndOrderCountGreaterThanAndVirtualInventoryNdcIdOrderByEventDateAsc(0, virtualInventoryNdc.getId());
            //Get all events with status approved, order_count>0, calculated is true
            List<VirtualInventoryNdcEvent> approvals = allEvents.stream().filter(virtualInventoryNdcEvent -> APPROVED.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> underReplenishments = allEvents.stream().filter(virtualInventoryNdcEvent -> UNDER_REPLENISHMENT.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> acknowledgements = allEvents.stream().filter(virtualInventoryNdcEvent -> ACKNOWLEDGEMENT.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> creditMemos = allEvents.stream().filter(virtualInventoryNdcEvent -> CREDIT_MEMO.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> greaterManualAdjEvents = virtualInventoryEventsRepository.findAllByCalculatedIsTrueAndVirtualInventoryEventTypeEventStatusAndOrderCountGreaterThanAndVirtualInventoryNdcIdAndQuantityGreaterThanOrderByEventDateAsc(MANUAL_ADJUSTMENT, 0, virtualInventoryNdc.getId(), 0);
            List<VirtualInventoryNdcEvent> orderingEvents = new ArrayList<>();
            orderingEvents.addAll(approvals);
            orderingEvents.addAll(underReplenishments);
            orderingEvents.addAll(acknowledgements);
            orderingEvents.addAll(creditMemos);
            orderingEvents.addAll(greaterManualAdjEvents);
            orderingEvents = orderingEvents.stream().sorted(Comparator.comparing(VirtualInventoryNdcEvent::getEventDate)).collect(Collectors.toList());

            List<VirtualInventoryNdcEvent> stabilizers = allEvents.stream().filter(virtualInventoryNdcEvent -> STABILIZER.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> overReplenishments = allEvents.stream().filter(virtualInventoryNdcEvent -> OVER_REPLENISHMENT.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> prebuys = allEvents.stream().filter(virtualInventoryNdcEvent -> PREBUY.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> preExists = allEvents.stream().filter(virtualInventoryNdcEvent -> PREEXIST.equalsIgnoreCase(virtualInventoryNdcEvent.getVirtualInventoryEventType().getEventStatus())).collect(Collectors.toList());
            List<VirtualInventoryNdcEvent> lesserManualAdjustmentEvents = virtualInventoryEventsRepository.findAllByCalculatedIsTrueAndVirtualInventoryEventTypeEventStatusAndOrderCountGreaterThanAndVirtualInventoryNdcIdAndQuantityLessThanOrderByEventDateAsc(MANUAL_ADJUSTMENT, 0, virtualInventoryNdc.getId(), 0);
            //All Events which need to be backfilled
            List<VirtualInventoryNdcEvent> sortedBackfillEvents = new ArrayList<>();
            sortedBackfillEvents.addAll(stabilizers);
            sortedBackfillEvents.addAll(overReplenishments);
            sortedBackfillEvents.addAll(prebuys);
            sortedBackfillEvents.addAll(preExists);
            sortedBackfillEvents.addAll(lesserManualAdjustmentEvents);
            sortedBackfillEvents = sortedBackfillEvents.stream().sorted(Comparator.comparing(VirtualInventoryNdcEvent::getEventDate)).collect(Collectors.toList());
            VirtualInventoryEventType eventTypeObj = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(purchaseOrderMessage.getMsgStatus());
            VirtualInventoryEventType backfillEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(BACKFILL);
            VirtualInventoryEventType reverseStabilizerEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(REVERSE_STABILIZER);
            VirtualInventoryNdcEvent orderEvent = virtualInventoryEventsRepository.findByOrderIdAndVirtualInventoryEventTypeEventStatus(purchaseOrderMessage.getHeaderId(), ORDERED);
            int total = 0;
            int approvalTotal = 0;
            if (orderEvent == null) {
                //Logic for orders splitting
                inventoryNdcEventsResult.add(createInvoiceEvent(purchaseOrderMessage.getPackageQuantityAtOrder(), virtualInventoryNdc, purchaseOrderMessage.getHeaderId(), purchaseOrderMessage.getPurchaseOrderNumber()));
                orderCreationAndFragment(orderingEvents, orderedQuantity, virtualInventoryNdc, total, inventoryNdcEventsResult, purchaseOrderMessage, eventTypeObj, sortedBackfillEvents, backfillEventType, reverseStabilizerEventType, approvalTotal);
            }
            result = virtualInventoryEventsRepository.save(inventoryNdcEventsResult);
        }
        catch (Exception e){
            logger.info("Exception while creating order events "+e);
        }
        if(result == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_SAVED, result));
        return result.stream().map(virtualInventoryNdcEventsMapper :: toDto).collect(Collectors.toList());
    }

    private VirtualInventoryNdcEvent createInvoiceEvent(int bottleSize, VirtualInventoryNdc virtualInventoryNdc, UUID orderId, String purchaseOrderNumber) {
        VirtualInventoryNdcEvent invoiceEvent = new VirtualInventoryNdcEvent();
        VirtualInventoryEventType invoiceEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(ORDER_INVOICED);
        if(invoiceEvent == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENT_TYPE, invoiceEvent));
        invoiceEvent.setVirtualInventoryNdc(virtualInventoryNdc);
        invoiceEvent.setNdc(virtualInventoryNdc.getNdc());
        invoiceEvent.setOrderId(orderId);
        invoiceEvent.setOrderFlag(false);
        invoiceEvent.setQuantity(bottleSize);
        invoiceEvent.setEventDate(DateTime.now(DateTimeZone.UTC));
        invoiceEvent.setCalculated(true);
        invoiceEvent.setOrderCount(0);
        invoiceEvent.setOrderNumber(purchaseOrderNumber);
        invoiceEvent.setVirtualInventoryEventType(invoiceEventType);
        return invoiceEvent;
    }

    private void orderCreationAndFragment(List<VirtualInventoryNdcEvent> virtualInventoryNdcEvents, int orderedQuantity, VirtualInventoryNdc virtualInventoryNdc, int total, List<VirtualInventoryNdcEvent> inventoryNdcEventsResult, PurchaseOrderMessage purchaseOrderMessage, VirtualInventoryEventType eventTypeObj, List<VirtualInventoryNdcEvent> stabilizerEvents, VirtualInventoryEventType backfillEventType, VirtualInventoryEventType reverseStabilizerEventType, int approvalTotal) {
        int i=0;
        Integer stabilizerTotal = 0;
        int approvalEventOrder = 0;
        VirtualInventoryEventType preBuyEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(PREBUY);
        for (VirtualInventoryNdcEvent approvalEvent : virtualInventoryNdcEvents) {
            if (approvalTotal == 0) {
                approvalTotal = approvalEvent.getOrderCount();
            }
            if (!stabilizerEvents.isEmpty()) {
                for (VirtualInventoryNdcEvent stabilizerEvent : stabilizerEvents) {
                    if (stabilizerTotal == 0) {
                        stabilizerTotal = stabilizerEvent.getOrderCount();
                    }
                    VirtualInventoryNdcEvent backfillEvent = new VirtualInventoryNdcEvent();
                    VirtualInventoryNdcEvent reverseStabilizerEvent = new VirtualInventoryNdcEvent();
                    createReverseStabilizerAndBackfill(backfillEvent, reverseStabilizerEvent, backfillEventType, reverseStabilizerEventType, approvalEvent, stabilizerEvent);
                    backfillEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
                    reverseStabilizerEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
                    //logic for stabilizer quantity == approval quantity
                    if (approvalTotal == stabilizerTotal) {
                        backfillEvent.setQuantity(approvalTotal);
                        inventoryNdcEventsResult.add(backfillEvent);
                        reverseStabilizerEvent.setQuantity(0 - approvalTotal);
                        inventoryNdcEventsResult.add(reverseStabilizerEvent);
                        stabilizerEvent.setOrderCount(0);
                        inventoryNdcEventsResult.add(stabilizerEvent);
                        approvalEvent.setOrderCount(0);
                        stabilizerTotal = 0;
                        approvalTotal = 0;
                        inventoryNdcEventsResult.add(approvalEvent);
                        stabilizerEvents.remove(stabilizerEvent);
                        break;
                    }
                    //logic for stabilizer total greater than approval total
                    else if (approvalTotal < stabilizerTotal) {
                        backfillEvent.setQuantity(approvalTotal);
                        inventoryNdcEventsResult.add(backfillEvent);
                        reverseStabilizerEvent.setQuantity(0 - approvalTotal);
                        inventoryNdcEventsResult.add(reverseStabilizerEvent);
                        stabilizerTotal = stabilizerTotal - approvalTotal;
                        approvalTotal = 0;
                        approvalEvent.setOrderCount(approvalTotal);
                        stabilizerEvent.setOrderCount(stabilizerTotal);
                        inventoryNdcEventsResult.add(approvalEvent);
                        inventoryNdcEventsResult.add(stabilizerEvent);
                        break;
                    } else if (approvalTotal > stabilizerTotal) {
                        backfillEvent.setQuantity(stabilizerTotal);
                        inventoryNdcEventsResult.add(backfillEvent);
                        reverseStabilizerEvent.setQuantity(0 - stabilizerTotal);
                        inventoryNdcEventsResult.add(reverseStabilizerEvent);
                        approvalTotal = approvalTotal - stabilizerTotal;
                        stabilizerTotal=0;
                        stabilizerEvent.setOrderCount(stabilizerTotal);
                        inventoryNdcEventsResult.add(stabilizerEvent);
                        approvalEventOrder = approvalTotal;
                    }
                }
            }
            if((approvalTotal != 0) && (stabilizerTotal == 0)) {
                approvalEvent.setOrderFlag(true);
                VirtualInventoryNdcEvent inventoryNdcEventsOrder = new VirtualInventoryNdcEvent();
                inventoryNdcEventsOrder.setVirtualInventoryNdc(virtualInventoryNdc);
                inventoryNdcEventsOrder.setNdc(purchaseOrderMessage.getNdc());
                if(approvalEventOrder == 0){
                    approvalEventOrder = approvalEvent.getOrderCount();
                }
                if (total < (purchaseOrderMessage.getPackageQuantityAtOrder() * virtualInventoryNdc.getPackageSize())) {
                    //Fragmenting based on OrderCount
                    orderedQuantity = orderedQuantity - approvalEventOrder;
                    total = total + approvalEventOrder;
                    //Setting Dispense, ndc details into database
                    inventoryNdcEventsOrder.setDispenseSetId(approvalEvent.getDispenseSetId());
                    inventoryNdcEventsOrder.setDispenseId(approvalEvent.getDispenseId());
                    inventoryNdcEventsOrder.setCalculated(false);
                    inventoryNdcEventsOrder.setOrderId(purchaseOrderMessage.getHeaderId());
                    inventoryNdcEventsOrder.setReferenceNumber(approvalEvent.getReferenceNumber());
                    inventoryNdcEventsOrder.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
                    inventoryNdcEventsOrder.setVirtualInventoryEventType(eventTypeObj);
                    inventoryNdcEventsOrder.setOrderNumber(purchaseOrderMessage.getPurchaseOrderNumber());
                    inventoryNdcEventsOrder.setOrderFlag(true);
                    //Save when complete quantity is fulfilled
                    if (orderedQuantity >= 0) {
                        inventoryNdcEventsOrder.setQuantity(0 - approvalEventOrder);
                        inventoryNdcEventsOrder.setOrderCount(0);
                        inventoryNdcEventsResult.add(inventoryNdcEventsOrder);
                        approvalEvent.setOrderCount(0);
                        approvalEventOrder = 0;
                        inventoryNdcEventsResult.add(approvalEvent);
                    } else {
                        inventoryNdcEventsOrder.setQuantity(0 - (orderedQuantity + approvalEventOrder));
                        inventoryNdcEventsOrder.setOrderCount(0);
                        inventoryNdcEventsResult.add(inventoryNdcEventsOrder);
                        approvalEvent.setOrderCount(Math.abs(orderedQuantity));
                        inventoryNdcEventsResult.add(approvalEvent);
                        break;
                    }
                }
            }
            if(stabilizerTotal == 0){
                stabilizerEvents.removeAll(stabilizerEvents);
            }
        }
        if(orderedQuantity>0){
            //create prebuy event for remaining quantity
            VirtualInventoryNdcEvent prebuyEvent = new VirtualInventoryNdcEvent();
            prebuyEvent.setEventDate(DateTime.now(DateTimeZone.UTC).plusMillis(++i));
            prebuyEvent.setOrderId(purchaseOrderMessage.getHeaderId());
            prebuyEvent.setOrderNumber(purchaseOrderMessage.getPurchaseOrderNumber());
            VirtualInventoryNdcEvent event = createPreBuyEvent(prebuyEvent, preBuyEventType, orderedQuantity, virtualInventoryNdc);
            inventoryNdcEventsResult.add(event);
        }
    }

    private VirtualInventoryNdcEvent createPreBuyEvent(VirtualInventoryNdcEvent prebuyEvent, VirtualInventoryEventType preBuyEventType, int orderedQuantity, VirtualInventoryNdc virtualInventoryNdc) {
        prebuyEvent.setVirtualInventoryEventType(preBuyEventType);
        prebuyEvent.setOrderFlag(false);
        prebuyEvent.setCalculated(false);
        prebuyEvent.setNdc(virtualInventoryNdc.getNdc());
        prebuyEvent.setVirtualInventoryNdc(virtualInventoryNdc);
        prebuyEvent.setQuantity(0-orderedQuantity);
        prebuyEvent.setOrderCount(orderedQuantity);
        return prebuyEvent;
    }

    private void createReverseStabilizerAndBackfill(VirtualInventoryNdcEvent backfillEvent, VirtualInventoryNdcEvent reverseStabilizerEvent, VirtualInventoryEventType backfillEventType, VirtualInventoryEventType reverseStabilizerEventType, VirtualInventoryNdcEvent approvalEvent, VirtualInventoryNdcEvent stabilizerEvent){
        //set backfill event
        backfillEvent.setVirtualInventoryNdc(approvalEvent.getVirtualInventoryNdc());
        backfillEvent.setDispenseId(approvalEvent.getDispenseId());
        backfillEvent.setDispenseSetId(approvalEvent.getDispenseSetId());
        backfillEvent.setReferenceNumber(approvalEvent.getReferenceNumber());
        backfillEvent.setCalculated(false);
        backfillEvent.setOrderCount(0);
        backfillEvent.setNdc(approvalEvent.getNdc());
        backfillEvent.setOrderFlag(true);
        backfillEvent.setOrderId(stabilizerEvent.getOrderId());
        backfillEvent.setVirtualInventoryEventType(backfillEventType);
        if(MANUAL_ADJUSTMENT.equalsIgnoreCase(stabilizerEvent.getVirtualInventoryEventType().getEventStatus())) {
            backfillEvent.setOrderNumber(stabilizerEvent.getReferenceNumber());
            reverseStabilizerEvent.setOrderNumber(stabilizerEvent.getReferenceNumber());
        }
        else {
            backfillEvent.setOrderNumber(stabilizerEvent.getOrderNumber());
            reverseStabilizerEvent.setOrderNumber(stabilizerEvent.getOrderNumber());
        }
        //set reverse stabilizer event
        reverseStabilizerEvent.setVirtualInventoryNdc(approvalEvent.getVirtualInventoryNdc());
        reverseStabilizerEvent.setDispenseId(approvalEvent.getDispenseId());
        reverseStabilizerEvent.setDispenseSetId(approvalEvent.getDispenseSetId());
        reverseStabilizerEvent.setReferenceNumber(approvalEvent.getReferenceNumber());
        reverseStabilizerEvent.setCalculated(false);
        reverseStabilizerEvent.setOrderCount(0);
        reverseStabilizerEvent.setNdc(approvalEvent.getNdc());
        reverseStabilizerEvent.setOrderFlag(true);
        reverseStabilizerEvent.setOrderId(stabilizerEvent.getOrderId());
        reverseStabilizerEvent.setVirtualInventoryEventType(reverseStabilizerEventType);
    }

    @Override
    public Boolean existsByBillToAndShipTo(UUID coveredEntityBillToId, UUID pharmacyShipToId) {
        if (virtualInventoryRepository.existsByCoveredEntityBillToIdAndPharmacyShipToId(coveredEntityBillToId, pharmacyShipToId))
            return true;
        else
            return false;
    }

    @Override
    public VirtualInventoryNdcEvent getByDispenseIdAndEventStatus(UUID dispenseId, String eventStatus) {
        return virtualInventoryEventsRepository.findByDispenseIdAndVirtualInventoryEventTypeEventStatus(dispenseId, eventStatus);
    }

    @Override
    public List<VirtualInventoryNdcEvent> getByDispenseId(UUID dispenseId) {
        return virtualInventoryEventsRepository.findByDispenseIdIsNotNull(dispenseId);
    }

    public VirtualInventoryNdcEventDTO add(VirtualInventoryEventsViewDTO virtualInventoryNdcEventDTO) {
        if(virtualInventoryNdcEventDTO.getQuantity() == 0)
            throw new InternalServerException(String.format(ErrorConstants.EVENT_QUANTITY_ZERO));
        int templateType = getUserRepresentationDetails(URL_PATH).getBody().getTemplateType();
        VirtualInventoryEventsView virtualInventoryEventsView = virtualInventoryEventsViewMapper.toEntity(virtualInventoryNdcEventDTO);
        VirtualInventoryNdcEvent virtualInventoryNdcEvent = new VirtualInventoryNdcEvent();
        if(templateType == 1) {
            VirtualInventoryNdc virtualInventoryNdc = virtualInventoryNdcRepository.getOne(virtualInventoryEventsView.getNdcId());
            virtualInventoryNdcEvent.setVirtualInventoryNdc(virtualInventoryNdc);
            VirtualInventoryEventType virtualInventoryEventType = virtualInventoryEventTypeService.getVirtualInventoryEventTypeByKey(MANUAL_ADJUSTMENT);
            virtualInventoryNdcEvent.setVirtualInventoryEventType(virtualInventoryEventType);
            virtualInventoryNdcEvent.setReferenceNumber("Entry Details");
            virtualInventoryNdcEvent.setReason(virtualInventoryEventsView.getReason());
            virtualInventoryNdcEvent.setNdc(virtualInventoryEventsView.getNdc());
            virtualInventoryNdcEvent.setQuantity(virtualInventoryEventsView.getQuantity());
            virtualInventoryNdcEvent.setOrderFlag(false);
            virtualInventoryNdcEvent.setCalculated(false);
            virtualInventoryNdcEvent.setOrderCount(Math.abs(virtualInventoryNdcEventDTO.getQuantity()));
            virtualInventoryNdcEvent.setEventDate(DateTime.now(DateTimeZone.UTC));

            virtualInventoryEventsRepository.save(virtualInventoryNdcEvent);
        }
        else
            throw new AccessForbidenException(String.format(ErrorConstants.ACCESS_DENIED));
        return virtualInventoryNdcEventsMapper.toDto(virtualInventoryNdcEvent);
    }

    public VirtualInventoryNdcEventDTO update(VirtualInventoryNdcEvent virtualInventoryNdcEvent) {
        VirtualInventoryNdcEvent virtualInventoryNdcEvent1 = virtualInventoryEventsRepository.getOne(virtualInventoryNdcEvent.getId());
        virtualInventoryNdcEvent1.setVirtualInventoryUnits(virtualInventoryNdcEvent.getVirtualInventoryUnits());
        virtualInventoryEventsRepository.flush();
        return virtualInventoryNdcEventsMapper.toDto(virtualInventoryNdcEvent1);
    }

    @Override
    public VirtualInventoryNdcEventDTO updateUnitsByNdcId(VirtualInventoryNdcEvent virtualInventoryNdcEvent, UUID ndcID) {
        VirtualInventoryNdcEvent virtualInventoryNdcEvent1 = virtualInventoryEventsRepository.findByIdAndVirtualInventoryNdcId(virtualInventoryNdcEvent.getId(), ndcID);
        virtualInventoryNdcEvent1.setVirtualInventoryUnits(virtualInventoryNdcEvent.getVirtualInventoryUnits());
        virtualInventoryEventsRepository.flush();
        return virtualInventoryNdcEventsMapper.toDto(virtualInventoryNdcEvent1);
    }

    @Override
    public List<VirtualInventoryNdcEventDTO> updateList(List<VirtualInventoryNdcEventDTO> ndcEventsDTOS) {
        List<VirtualInventoryNdcEvent> ndcEvents = ndcEventsDTOS.stream().map(virtualInventoryNdcEventsMapper::toEntity).collect(Collectors.toList());
        List<VirtualInventoryNdcEvent> events = virtualInventoryEventsRepository.save(ndcEvents);
        if (events == null || events.isEmpty())
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_EVENTS_NOT_SAVED, events));
        return events.stream().map(virtualInventoryNdcEventsMapper::toDto).collect(Collectors.toList());
    }


    public List<VirtualInventoryNdcEventDTO> getAll() {
        return virtualInventoryEventsRepository.findByDates().stream().map(virtualInventoryNdcEventsMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<VirtualInventoryNdcEventDTO> getByNdcId(UUID ndcId) {
        return virtualInventoryEventsRepository.findAllByCalculatedIsFalseAndVirtualInventoryNdcIdOrderByEventDateAsc(ndcId).stream().map(virtualInventoryNdcEventsMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<VirtualInventoryNdcEventDTO> getByNdcIdAndNdc(UUID ndcId, String ndc) {
        return virtualInventoryEventsRepository.findAllByVirtualInventoryNdcIdAndNdc(ndcId, ndc).stream()
                .map(virtualInventoryNdcEventsMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

}
