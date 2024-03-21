package com.noah.backend.domain.plan.service.impl;

import com.noah.backend.domain.datailPlan.repository.DetailPlanRepository;
import com.noah.backend.domain.plan.dto.requestDto.PlanPostDto;
import com.noah.backend.domain.plan.dto.requestDto.PlanUpdateDto;
import com.noah.backend.domain.plan.dto.responseDto.PlanGetDto;
import com.noah.backend.domain.plan.dto.responseDto.PlanListGetFromTravelDto;
import com.noah.backend.domain.plan.entity.Plan;
import com.noah.backend.domain.plan.repository.PlanRepository;
import com.noah.backend.domain.plan.service.PlanService;
import com.noah.backend.domain.review.repository.ReviewRepository;
import com.noah.backend.domain.ticket.repository.TicketRepository;
import com.noah.backend.domain.travel.entity.Travel;
import com.noah.backend.domain.travel.repository.TravelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class PlanServiceImpl implements PlanService {

    private final TicketRepository ticketRepository;
    private final PlanRepository planRepository;
    private final DetailPlanRepository detailPlanRepository;
    private final ReviewRepository reviewRepository;
    private final TravelRepository travelRepository;

    @Override
    public List<PlanListGetFromTravelDto> getPlanList(Long travelId) {
        return planRepository.getPlanList(travelId).orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다"));
    }

    @Override
    public PlanGetDto getPlanSelect(Long planId) {
        return planRepository.getPlanSelect(planId).orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다."));
    }

    @Override
    public Long createPlan(Long travelId, PlanPostDto planDto) {
        Travel travel = travelRepository.findById(travelId).orElseThrow(() -> new RuntimeException("여행 정보를 찾을 수 없어요"));
        Plan plan = Plan.builder()
                .startDate(planDto.getStart_date())
                .endDate(planDto.getEnd_date())
                .travelStart(planDto.isTravel_start())
                .country(planDto.getCountry())
                .travel(travel)
                .build();
        return planRepository.save(plan).getId();
    }

    @Override
    public Long updatePlan(Long planId, PlanUpdateDto planDto) {
        Plan currentPlan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("수정할 계획을 찾을 수 없습니다."));
        currentPlan.setStartDate(planDto.getStart_date());
        currentPlan.setEndDate(planDto.getEnd_date());
        currentPlan.setCountry(planDto.getCountry());

        planRepository.save(currentPlan);

        return currentPlan.getId();
    }

    public boolean changeStart(Long planId, PlanUpdateDto planDto){
        Plan currentPlan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("상세 계획을 찾을 수 없습니다."));
        boolean changePlanStart = !currentPlan.isTravelStart();
        currentPlan.setTravelStart(changePlanStart);

        planRepository.save(currentPlan);

        return currentPlan.isTravelStart();
    }

    @Override
    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
    }
}
