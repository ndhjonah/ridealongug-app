package com.ridealongug.backend.services.vehicle;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.VehicleCategoryModel;
import com.ridealongug.backend.repositories.VehicleCategoryRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleCategoryService extends BaseWebActionsService {

    private final VehicleCategoryRepository categoryRepository;

    private OperationReturnObject createCategory(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_PRICING", null);
        requires("category_name", request);

        VehicleCategoryModel category = VehicleCategoryModel.builder()
                .categoryName(request.getString("category_name"))
                .requiresDriverLicenseClass(request.getString("requires_driver_license_class"))
                .baseRatePerKm(request.getObject("base_rate_per_km", BigDecimal.class))
                .baseRatePerHour(request.getObject("base_rate_per_hour", BigDecimal.class))
                .build();

        VehicleCategoryModel saved = categoryRepository.save(category);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Category created successfully", saved);
        return res;
    }

    private OperationReturnObject updateCategory(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_PRICING", null);
        requires("id", request);

        VehicleCategoryModel category = categoryRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Category not found"));

        if (request.containsKey("category_name")) category.setCategoryName(request.getString("category_name"));
        if (request.containsKey("requires_driver_license_class")) category.setRequiresDriverLicenseClass(request.getString("requires_driver_license_class"));
        if (request.containsKey("base_rate_per_km")) category.setBaseRatePerKm(request.getObject("base_rate_per_km", BigDecimal.class));
        if (request.containsKey("base_rate_per_hour")) category.setBaseRatePerHour(request.getObject("base_rate_per_hour", BigDecimal.class));

        VehicleCategoryModel saved = categoryRepository.save(category);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Category updated successfully", saved);
        return res;
    }

    private OperationReturnObject listAll() {
        List<VehicleCategoryModel> categories = categoryRepository.findAll();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, categories);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "createCategory" -> createCategory(request);
            case "updateCategory" -> updateCategory(request);
            case "listAll" -> listAll();
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}
