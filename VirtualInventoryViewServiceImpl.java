package com.capturerx.cumulus4.virtualinventory.impls;


import com.capturerx.common.errors.PreConditionFailedException;
import com.capturerx.cumulus4.virtualinventory.configuration.VirtualInventorySecurityConfiguration;
import com.capturerx.cumulus4.virtualinventory.dtos.*;
import com.capturerx.cumulus4.virtualinventory.errors.AccessForbidenException;
import com.capturerx.cumulus4.virtualinventory.errors.ErrorConstants;
import com.capturerx.cumulus4.virtualinventory.errors.InternalServerException;
import com.capturerx.cumulus4.virtualinventory.models.*;
import com.capturerx.cumulus4.virtualinventory.repositories.*;
import com.capturerx.cumulus4.virtualinventory.security.KeycloakClientCredentialsConfig;
import com.capturerx.cumulus4.virtualinventory.security.KeycloakClientCredentialsRestTemplate;
import com.capturerx.cumulus4.virtualinventory.services.VirtualInventoryViewService;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryBillToViewMapper;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryEventsViewMapper;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryNdcViewMapper;
import com.capturerx.cumulus4.virtualinventory.services.mappers.VirtualInventoryShipToViewMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VirtualInventoryViewServiceImpl implements VirtualInventoryViewService {

    private static final Logger logger = LoggerFactory.getLogger(VirtualInventoryViewServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${ndc.size}")
    private int ndcSize;

    @Value("${apiservices.path}")
    private String apiPath;

    @Value("${externalapi.contracts}")
    private String contactsAPI;

    @Autowired
    private VirtualInventorySecurityConfiguration virtualInventorySecurityConfiguration;

    private static final String BILL_TO_ID = "billToId";
    private static final String SHIP_TO_ID = "shipToId";
    private static final String NDC = "ndc";
    private static final String PACKAGE_AVAILABLE_TO_ORDER="packageQuantity";
    private static final String LAST_CALCULATED_DATE ="lastCalculatedDate";
    private static final String DRUG_DESCRIPTION ="drugDescription";
    private static final String PERCENTAGE = "%";
    private static final String EMPTY = "";
    private static final String urlPath = "secureusers/user/";
    private static final SimpleGrantedAuthority C4_INTERNAL = new SimpleGrantedAuthority("ROLE_C4_INTERNAL");

    @Autowired
    KeycloakClientCredentialsConfig keycloakClientCredentialsConfig;

    private final VirtualInventoryNdcViewRepository virtualInventoryNdcViewRepository;
    private final VirtualInventoryEventsViewRepository virtualInventoryEventsViewRepository;
    private final VirtualInventoryBillToViewRepository virtualInventoryBillToViewRepository;
    private final VirtualInventoryShipToViewRepository virtualInventoryShipToViewRepository;
    private final VirtualInventoryHeaderViewRepository virtualInventoryHeaderViewRepository;
    private final VirtualInventoryNdcViewMapper virtualInventoryNdcViewMapper;
    private final VirtualInventoryBillToViewMapper virtualInventoryBillToViewMapper;
    private final VirtualInventoryShipToViewMapper virtualInventoryShipToViewMapper;
    private final VirtualInventoryEventsViewMapper virtualInventoryEventsViewMapper;

    public VirtualInventoryViewServiceImpl(VirtualInventoryNdcViewRepository virtualInventoryNdcViewRepository, VirtualInventoryEventsViewRepository virtualInventoryEventsViewRepository, VirtualInventoryBillToViewRepository virtualInventoryBillToViewRepository, VirtualInventoryShipToViewRepository virtualInventoryShipToViewRepository, VirtualInventoryHeaderViewRepository virtualInventoryHeaderViewRepository, VirtualInventoryNdcViewMapper virtualInventoryNdcViewMapper, VirtualInventoryBillToViewMapper virtualInventoryBillToViewMapper, VirtualInventoryShipToViewMapper virtualInventoryShipToViewMapper, VirtualInventoryEventsViewMapper virtualInventoryEventsViewMapper) {
        this.virtualInventoryNdcViewRepository = virtualInventoryNdcViewRepository;
        this.virtualInventoryEventsViewRepository = virtualInventoryEventsViewRepository;
        this.virtualInventoryBillToViewRepository = virtualInventoryBillToViewRepository;
        this.virtualInventoryShipToViewRepository = virtualInventoryShipToViewRepository;
        this.virtualInventoryHeaderViewRepository = virtualInventoryHeaderViewRepository;
        this.virtualInventoryNdcViewMapper = virtualInventoryNdcViewMapper;
        this.virtualInventoryBillToViewMapper = virtualInventoryBillToViewMapper;
        this.virtualInventoryShipToViewMapper = virtualInventoryShipToViewMapper;
        this.virtualInventoryEventsViewMapper = virtualInventoryEventsViewMapper;
    }

    private ResponseEntity<UserRepresentationDTO> getUserRepresentationDetails(String builderUrlPath)
    {
        Map entityMap = virtualInventorySecurityConfiguration.getEntity(builderUrlPath);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserRepresentationDTO> userRepresentationDTO = restTemplate.exchange(entityMap.get("builder").toString(), HttpMethod.GET,
                (HttpEntity) entityMap.get("entity"),UserRepresentationDTO.class);

        return userRepresentationDTO;
    }

    private Boolean getWholesalerAccounts(VirtualInventoryNdcUiViewDTO virtualInventoryNdcUiViewDTO){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<WholesalerAccountDTO[]> wholesalerAccountDTOResponseEntity = null;
        String wsauri = "wholesaler_accounts/billtoid/{billToId}/shiptoid/{shipToId}";
        Map wsaInfo = new HashMap();
        wsaInfo.put("billToId", virtualInventoryNdcUiViewDTO.getBillToId());
        wsaInfo.put("shipToId", virtualInventoryNdcUiViewDTO.getShipToId());
        Map entityMap = virtualInventorySecurityConfiguration.getEntity(apiPath, wsauri, wsaInfo);
        try {
            if (wsaInfo != null) {
                wholesalerAccountDTOResponseEntity = restTemplate.exchange(entityMap.get("builder").toString(), HttpMethod.GET,
                        (HttpEntity) entityMap.get("entity"), WholesalerAccountDTO[].class);

                List<WholesalerAccountDTO> wholesalerAccountDTOList = Arrays.asList(wholesalerAccountDTOResponseEntity.getBody());
                if (null != wholesalerAccountDTOList && !wholesalerAccountDTOList.isEmpty() && wholesalerAccountDTOList.size() > 0) {
                    return true;
                }
            }

        }  catch (HttpClientErrorException httpException) {
            logger.error(" Exception occured on VirtualInventoryViewServiceImpl:getWholesalerAccounts:- " + httpException.getResponseBodyAsString());
        }catch (Exception ex) {
            throw new PreConditionFailedException(ex.getMessage());
        }
        return false;
    }

    @Override
    public List<VirtualInventoryNdcUiViewDTO> getAllNdcs(UUID billToId, UUID shipToId) {
        List<VirtualInventoryNdcUiView> virtualInventoryNdcUiViews= virtualInventoryNdcViewRepository.findAllByBillToIdAndShipToId(billToId, shipToId);
        if(virtualInventoryNdcUiViews == null)
            throw new InternalServerException(String.format(ErrorConstants.VIRTUAL_INVENTORY_NDC_NOT_FOUND_FOR_HEADER, virtualInventoryNdcUiViews.get(1).getVirtualInventoryHeader().getId()));
        return virtualInventoryNdcUiViews.stream().map(virtualInventoryNdcViewMapper:: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryEventsView> getAllEvents() {
        return virtualInventoryEventsViewRepository.findAll();
    }

    @Override
    public List<VirtualInventoryEventsViewDTO> getEventsByNdcIdAndEventAsOfDate(UUID ndcId, DateTime eventAsOfDate) {
        List<VirtualInventoryEventsView> virtualInventoryEventsViews= virtualInventoryEventsViewRepository.findTop500ByNdcIdAndEventDateLessThanEqualAndEventTypeIsNotLikeOrderByEventDateDesc(ndcId, eventAsOfDate.plusDays(1), "Invoiced");
        List<VirtualInventoryEventsViewDTO> virtualInventoryEventsViewDTOS = virtualInventoryEventsViews.stream().map(virtualInventoryEventsViewMapper:: toDto).collect(Collectors.toList());
        List<VirtualInventoryEventsViewDTO> virtualInventoryEventsViewDTOS1 = new ArrayList<>();
        for (VirtualInventoryEventsViewDTO virtualInventoryEventsViewDTO: virtualInventoryEventsViewDTOS){
            if(("PreExist".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Approved".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Reversed".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Reverse Stabilizer".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("OrderImpact".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType()))){
                virtualInventoryEventsViewDTO.setReferenceNumber(true);
                virtualInventoryEventsViewDTO.setOrderNumber(false);
                virtualInventoryEventsViewDTO.setManualAdj(false);
            }
            else if(("Ordered".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Under Replenishment".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Acknowledgement".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("BackFill".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Stabilizer".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType()))){
                virtualInventoryEventsViewDTO.setReferenceNumber(true);
                virtualInventoryEventsViewDTO.setOrderNumber(true);
                virtualInventoryEventsViewDTO.setManualAdj(false);
            }
            else if(("Manual Adjustment".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType()))){
                virtualInventoryEventsViewDTO.setReferenceNumber(false);
                virtualInventoryEventsViewDTO.setOrderNumber(false);
                virtualInventoryEventsViewDTO.setManualAdj(true);
            }
            else if(("PreBuy".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType())) || ("Over Replenishment".equalsIgnoreCase(virtualInventoryEventsViewDTO.getEventType()))){
                virtualInventoryEventsViewDTO.setReferenceNumber(false);
                virtualInventoryEventsViewDTO.setOrderNumber(true);
                virtualInventoryEventsViewDTO.setManualAdj(false);
            }
            virtualInventoryEventsViewDTOS1.add(virtualInventoryEventsViewDTO);
        }
        return virtualInventoryEventsViewDTOS1;
    }

    @Override
    public List<VirtualInventoryBillToDTO> getAllBillTo() {
        List<VirtualInventoryBillToView> virtualInventoryBillToViews = virtualInventoryBillToViewRepository.findAllOrderByBillToName();
        return virtualInventoryBillToViews.stream().map(virtualInventoryBillToViewMapper:: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryShipToDTO> getAllShipTo() {
        List<VirtualInventoryShipToView> virtualInventoryShipToViews = virtualInventoryShipToViewRepository.findAllOrderByShipToName();
        return virtualInventoryShipToViews.stream().map(virtualInventoryShipToViewMapper:: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryNdcUiViewDTO> getAllByShipToIdAndPackageQuantityGreaterThanZero(UUID shipToId) {
        return virtualInventoryNdcViewRepository.findAllByShipToIdAndPackageQuantityGreaterThan(shipToId,0).stream().map(virtualInventoryNdcViewMapper :: toDto).collect(Collectors.toList());
    }

    @Override
    public List<VirtualInventoryNdcUiViewDTO> getNdcsByBillToAndShipTo(VirtualInventoryNdcUiViewDTO virtualInventoryNdcUiViewDTO) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        List<VirtualInventoryNdcUiView> virtualInventoryNdcUiViews;
        CriteriaQuery query = builder.createQuery(VirtualInventoryNdcUiView.class);
        Root<VirtualInventoryNdcUiView> virtualInventoryNdcUiViewRoot = query.from(VirtualInventoryNdcUiView.class);
        List<Predicate> predicates = new ArrayList<>();
        if(getWholesalerAccounts(virtualInventoryNdcUiViewDTO)) {
            predicates.add(builder.and(builder.equal(virtualInventoryNdcUiViewRoot.get(BILL_TO_ID), virtualInventoryNdcUiViewDTO.getBillToId())));
            predicates.add(builder.and(builder.equal(virtualInventoryNdcUiViewRoot.get(SHIP_TO_ID), virtualInventoryNdcUiViewDTO.getShipToId())));
            predicates.add(builder.and(builder.lessThanOrEqualTo(virtualInventoryNdcUiViewRoot.get(LAST_CALCULATED_DATE), virtualInventoryNdcUiViewDTO.getEventAsOfDate())));
            if (virtualInventoryNdcUiViewDTO.getNdc() != null) {
                predicates.add(builder.and(builder.equal(virtualInventoryNdcUiViewRoot.get(NDC), virtualInventoryNdcUiViewDTO.getNdc())));
            }
            if (virtualInventoryNdcUiViewDTO.isPackagesAvailableToOrder()) {
                predicates.add(builder.and(builder.greaterThan(virtualInventoryNdcUiViewRoot.get(PACKAGE_AVAILABLE_TO_ORDER), 0)));
            }
            String description = virtualInventoryNdcUiViewDTO.getDrugDescription();
            if (virtualInventoryNdcUiViewDTO.getDrugDescription() != null && !EMPTY.equalsIgnoreCase(description.trim())) {
                description = description.replace(" ", PERCENTAGE);
                predicates.add(builder.and(builder.like(builder.upper(virtualInventoryNdcUiViewRoot.get(DRUG_DESCRIPTION)), PERCENTAGE + description.toUpperCase() + PERCENTAGE)));
            }
            Predicate[] predicatesArray = new Predicate[predicates.size()];
            query.select(virtualInventoryNdcUiViewRoot).where(predicates.toArray(predicatesArray));
            virtualInventoryNdcUiViews = entityManager.createQuery(query).setMaxResults(ndcSize).getResultList();
        }
        else
            throw new AccessForbidenException(String.format(ErrorConstants.ACCESS_DENIED, virtualInventoryNdcUiViewDTO.getBillToId(), virtualInventoryNdcUiViewDTO.getShipToId()));
        return virtualInventoryNdcUiViews.stream().map(virtualInventoryNdcViewMapper::toDto).sorted(Comparator.comparing(VirtualInventoryNdcUiViewDTO::getEventAsOfDate).reversed()).collect(Collectors.toList());
    }

    public List<ContractsVirtualInventory> getContractsByShipToId(UUID shipToId){
        KeycloakClientCredentialsRestTemplate restTemplate = keycloakClientCredentialsConfig.createRestTemplate();
        String url = contactsAPI + "/{shipToId}";
        UriComponentsBuilder uriComponentsBuilder;
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("shipToId", shipToId.toString());
        uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
        URI eventsUri = uriComponentsBuilder.buildAndExpand(hashMap).toUri();
        ContractsVirtualInventory[] contractsVirtualInventories = restTemplate.getForObject(eventsUri,ContractsVirtualInventory[].class);
        return Arrays.asList(contractsVirtualInventories);
    }

    @Override
    public List<ReplenishmentVirtualInv> getContractsAndHeaders(UUID shipToId) {
        List<ReplenishmentVirtualInv> contractHeadersList = new ArrayList<>();
        List<ContractsVirtualInventory> contractsVirtualInventories = getContractsByShipToId(shipToId);
        List<VirtualInventoryNdcUiViewDTO> virtualInventoryHeaderDTOS = getAllByShipToIdAndPackageQuantityGreaterThanZero(shipToId);
        for(VirtualInventoryNdcUiViewDTO virtualInventoryHeaderDTO: virtualInventoryHeaderDTOS){
            ReplenishmentVirtualInv replenishmentVirtualInv = new ReplenishmentVirtualInv();
            replenishmentVirtualInv.setBillToTypeCode(virtualInventoryHeaderDTO.getBillToTypeCode());
            replenishmentVirtualInv.setHeaderId(virtualInventoryHeaderDTO.getVirtualInventoryHeaderId());
            replenishmentVirtualInv.setShipToTypeCode(virtualInventoryHeaderDTO.getShipToTypeCode());
            replenishmentVirtualInv.setBillToId(virtualInventoryHeaderDTO.getBillToId());
            replenishmentVirtualInv.setShipToId(virtualInventoryHeaderDTO.getShipToId());
            replenishmentVirtualInv.setBillToName(virtualInventoryHeaderDTO.getBillToName());
            replenishmentVirtualInv.setShipToName(virtualInventoryHeaderDTO.getShipToName());
            replenishmentVirtualInv.setPackageQuantity(virtualInventoryHeaderDTO.getPackageQuantity());
            replenishmentVirtualInv.setPackageSize(virtualInventoryHeaderDTO.getPackageSize());
            replenishmentVirtualInv.setNdc(virtualInventoryHeaderDTO.getNdc());
            replenishmentVirtualInv.setDrugDescription(virtualInventoryHeaderDTO.getDrugDescription());
            for(ContractsVirtualInventory contractsVirtualInventory:contractsVirtualInventories){
                if(virtualInventoryHeaderDTO.getShipToId().equals(contractsVirtualInventory.getShipToId())){
                    replenishmentVirtualInv.setContractId(contractsVirtualInventory.getContractId());
                    replenishmentVirtualInv.setEffectiveDate(contractsVirtualInventory.getEffectiveDate());
                    replenishmentVirtualInv.setTerminationDate(contractsVirtualInventory.getTerminationDate());
                    break;
                }
            }
            contractHeadersList.add(replenishmentVirtualInv);
        }
        return contractHeadersList;
    }

}
