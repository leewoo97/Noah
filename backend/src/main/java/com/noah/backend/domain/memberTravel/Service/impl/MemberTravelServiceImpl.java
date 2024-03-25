package com.noah.backend.domain.memberTravel.Service.impl;

import com.noah.backend.domain.member.entity.Member;
import com.noah.backend.domain.member.repository.MemberRepository;
import com.noah.backend.domain.memberTravel.Repository.MemberTravelRepository;
import com.noah.backend.domain.memberTravel.Service.MemberTravelService;
import com.noah.backend.domain.memberTravel.dto.Request.MemberTravelInviteDto;
import com.noah.backend.domain.memberTravel.dto.Request.MemberTravelPostDto;
import com.noah.backend.domain.memberTravel.dto.Request.MemberTravelUpdateDto;
import com.noah.backend.domain.memberTravel.entity.MemberTravel;
import com.noah.backend.domain.notification.entity.Notification;
import com.noah.backend.domain.notification.repository.NotificationRepository;
import com.noah.backend.domain.travel.entity.Travel;
import com.noah.backend.domain.travel.repository.TravelRepository;
import com.noah.backend.global.exception.member.MemberNotFoundException;
import com.noah.backend.global.exception.travel.TravelMemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberTravelServiceImpl implements MemberTravelService {

    private final MemberRepository memberReopsitory;
    private final TravelRepository travelRepository;
    private final MemberTravelRepository memberTravelRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public Long createMemberTravel(MemberTravelPostDto memberTravelPostDto) {

        Travel travel = travelRepository.findById(memberTravelPostDto.getTravel_id()).orElseThrow(() -> new RuntimeException("여행 id 안넣냐?"));
        Member member = memberReopsitory.findById(memberTravelPostDto.getMember_id()).orElseThrow(() -> new RuntimeException("멤버 id 안넣냐?"));

        MemberTravel memberTravel =  MemberTravel.builder()
                .payment_amount(memberTravelPostDto.getPayment_amount())
                .member(member)
                .travel(travel)
                .build();

        MemberTravel saveMemberTravel = memberTravelRepository.save(memberTravel);

        return saveMemberTravel.getId();
    }

    @Override
    public Long updateMemberTravel(Long memberTravelId, MemberTravelUpdateDto memberTravelUpdateDto) {
        MemberTravel updateMemberTravel = memberTravelRepository.findById(memberTravelId)
                .orElseThrow(() -> new NotFoundException("정보를 찾을 수 없는디"));
        updateMemberTravel.setPayment_amount(memberTravelUpdateDto.getPayment_amount());

        memberTravelRepository.save(updateMemberTravel);

        return updateMemberTravel.getId();
    }

    @Transactional
    @Override
    public Long inviteMember(MemberTravelInviteDto memberTravelInviteDto) {

        // 초대 요청을 보내기 = 알림 보내기
        // 멤버트래블 테이블에 데이터를 저장하는건 요청 받은 사람이 수락하면 저장할 것임
        Member receiver = memberReopsitory.findById(memberTravelInviteDto.getMember_id()).orElseThrow(MemberNotFoundException::new);
        Travel travel = travelRepository.findById(memberTravelInviteDto.getTravel_id()).orElseThrow(TravelMemberNotFoundException::new);

        Notification notification = Notification.builder()
            .receiver(receiver)
            .type(1)
            .travelId(memberTravelInviteDto.getTravel_id())
            .travelTitle(travel.getTitle())
            .build();

        Notification savedNotification = notificationRepository.save(notification);

        // 파이어베이스 푸쉬 알림




        // 준규오빠 코드
//        Member inviteMember = memberReopsitory.findById(memberTravelInviteDto.getTravel_id())
//                .orElseThrow(() -> new NotFoundException("멤버를 찾을 수 없으요"));
//
//        Travel inviteTravel = travelRepository.findById(memberTravelInviteDto.getMember_id())
//                .orElseThrow(() -> new NotFoundException("여행 정보를 찾을 수 없슈"));
//
//        MemberTravel newMemberTravel = MemberTravel.builder()
//                .member(inviteMember)
//                .travel(inviteTravel)
//                .payment_amount(0)
//                .build();

        return savedNotification.getId();
    }


    @Override
    public void deleteResistMember(Long memberTravelId) {

        memberTravelRepository.deleteById(memberTravelId);
    }
}
