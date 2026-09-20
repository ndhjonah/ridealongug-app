package com.ridealongug.backend.services.dealer;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.DealerModel;
import com.ridealongug.backend.models.jpahelpers.sortingAndFiltering.SearchRequest;
import com.ridealongug.backend.models.jpahelpers.sortingAndFiltering.SearchSpecification;
import com.ridealongug.backend.repositories.DealerRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealerService extends BaseWebActionsService {

    private final DealerRepository dealerRepository;

    private OperationReturnObject createDealer(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_DEALERS", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("dealer_name");
        requiredFields.add("contact_person");
        requiredFields.add("phone_number");
        requires(requiredFields, request);

        DealerModel dealer = DealerModel.builder()
                .dealerName(request.getString("dealer_name"))
                .contactPerson(request.getString("contact_person"))
                .phoneNumber(request.getString("phone_number"))
                .email(request.getString("email"))
                .location(request.getString("location"))
                .bondRegistrationNumber(request.getString("bond_registration_number"))
                .isActive(true)
                .build();

        DealerModel saved = dealerRepository.save(dealer);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Dealer created successfully", saved);
        return res;
    }

    private OperationReturnObject updateDealer(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_DEALERS", null);
        requires("id", request);

        DealerModel dealer = dealerRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Dealer not found"));

        if (request.containsKey("dealer_name")) dealer.setDealerName(request.getString("dealer_name"));
        if (request.containsKey("contact_person")) dealer.setContactPerson(request.getString("contact_person"));
        if (request.containsKey("phone_number")) dealer.setPhoneNumber(request.getString("phone_number"));
        if (request.containsKey("email")) dealer.setEmail(request.getString("email"));
        if (request.containsKey("location")) dealer.setLocation(request.getString("location"));
        if (request.containsKey("bond_registration_number")) dealer.setBondRegistrationNumber(request.getString("bond_registration_number"));
        if (request.containsKey("is_active")) dealer.setIsActive(request.getBoolean("is_active"));

        DealerModel saved = dealerRepository.save(dealer);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Dealer updated successfully", saved);
        return res;
    }



    private OperationReturnObject registerOwnDealerProfile(JSONObject request) {
        requiresAuth();
        hasRole("VEHICLE_OWNER");

        Long userId = authenticatedUser().getId();
        if (dealerRepository.findFirstByUserId(userId).isPresent()) {
            throw new IllegalStateException("You already have a dealer/bond profile registered");
        }

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("dealer_name");
        requiredFields.add("contact_person");
        requiredFields.add("phone_number");
        requires(requiredFields, request);

        DealerModel dealer = DealerModel.builder()
                .userId(userId)
                .dealerName(request.getString("dealer_name"))
                .contactPerson(request.getString("contact_person"))
                .phoneNumber(request.getString("phone_number"))
                .email(request.getString("email"))
                .location(request.getString("location"))
                .bondRegistrationNumber(request.getString("bond_registration_number"))
                .isActive(true)
                .build();

        DealerModel saved = dealerRepository.save(dealer);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Dealer/bond profile registered - you can now submit vehicles under this business", saved);
        return res;
    }

    private OperationReturnObject myDealerProfile() {
        requiresAuth();
        Long userId = authenticatedUser().getId();
        DealerModel dealer = dealerRepository.findFirstByUserId(userId).orElse(null);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, dealer);
        return res;
    }

    private OperationReturnObject search(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_DEALERS", null);

        SearchRequest searchRequest = request.getObject("SEARCH", SearchRequest.class);
        if (searchRequest == null) {
            searchRequest = new SearchRequest();
        }
        Page<DealerModel> page = dealerRepository.findAll(
                new SearchSpecification<>(searchRequest),
                SearchSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize())
        );
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, page);
        return res;
    }

    private OperationReturnObject getOne(JSONObject request) {
        requires("id", request);
        DealerModel dealer = dealerRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Dealer not found"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, dealer);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "createDealer" -> createDealer(request);
            case "updateDealer" -> updateDealer(request);
            case "registerOwnDealerProfile" -> registerOwnDealerProfile(request);
            case "myDealerProfile" -> myDealerProfile();
            case "search" -> search(request);
            case "getOne" -> getOne(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}
