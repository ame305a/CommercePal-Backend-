package com.commerce.pal.backend.module.transaction;

import com.commerce.pal.backend.common.ResponseCodes;
import com.commerce.pal.backend.models.transaction.AgentFloat;
import com.commerce.pal.backend.models.user.Agent;
import com.commerce.pal.backend.repo.transaction.AgentFloatRepository;
import com.commerce.pal.backend.repo.user.AgentRepository;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Log
@Service
@SuppressWarnings("Duplicates")
public class AgentFloatService {

    private final AgentFloatRepository agentFloatRepository;
    private final AgentRepository agentRepository;

    public AgentFloatService(AgentFloatRepository agentFloatRepository, AgentRepository agentRepository) {
        this.agentFloatRepository = agentFloatRepository;
        this.agentRepository = agentRepository;
    }

    //Retrieves a paginated list of AgentFloats with support for sorting, filtering, searching, and date range.
    public JSONObject getAllAgentFloats(
            int page,
            int size,
            Sort sort,
            Integer status,
            Timestamp startDate,
            Timestamp endDate
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AgentFloat> agentFloatPage = agentFloatRepository.findByDateAndStatus(status, startDate, endDate, pageable);

        List<JSONObject> agentFloats = new ArrayList<>();
        agentFloatPage.getContent().stream()
                .forEach(agentFloat -> {
                    JSONObject detail = new JSONObject();

                    String agentName = "";
                    Optional<Agent> optionalAgent = agentRepository.findById(agentFloat.getAgentId());
                    if (optionalAgent.isPresent()) {
                        Agent agent = optionalAgent.get();
                        agentName = agent.getAgentName();
                    }

                    detail.put("agent", agentName);
                    detail.put("amount", agentFloat.getAmount());
                    detail.put("comment", agentFloat.getComment());
                    detail.put("status", agentFloat.getStatus());
                    detail.put("requestDate", agentFloat.getRequestDate());
                    detail.put("reviewedBy", agentFloat.getReviewedBy());
                    detail.put("reviewDate", agentFloat.getReviewDate());
                    detail.put("processedDate", agentFloat.getProcessedDate());

                    agentFloats.add(detail);
                });

        JSONObject paginationInfo = new JSONObject();
        paginationInfo.put("pageNumber", agentFloatPage.getNumber())
                .put("pageSize", agentFloatPage.getSize())
                .put("totalElements", agentFloatPage.getTotalElements())
                .put("totalPages", agentFloatPage.getTotalPages());

        JSONObject data = new JSONObject();
        data.put("agentFloats", agentFloats).
                put("paginationInfo", paginationInfo);

        JSONObject response = new JSONObject();
        response.put("statusCode", ResponseCodes.SUCCESS)
                .put("statusDescription", "AgentFloat Passed")
                .put("statusMessage", "AgentFloat Passed")
                .put("data", data);

        return response;
    }

}
