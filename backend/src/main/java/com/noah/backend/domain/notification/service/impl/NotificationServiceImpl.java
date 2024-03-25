package com.noah.backend.domain.notification.service.impl;

import com.noah.backend.domain.member.entity.Member;
import com.noah.backend.domain.member.repository.MemberRepository;
import com.noah.backend.domain.memberTravel.Repository.MemberTravelRepository;
import com.noah.backend.domain.memberTravel.entity.MemberTravel;
import com.noah.backend.domain.notification.dto.responseDto.NotificationGetDto;
import com.noah.backend.domain.notification.entity.Notification;
import com.noah.backend.domain.notification.repository.NotificationRepository;
import com.noah.backend.domain.notification.service.NotificationService;
import com.noah.backend.domain.travel.entity.Travel;
import com.noah.backend.domain.travel.repository.TravelRepository;
import com.noah.backend.global.exception.member.MemberNotFoundException;
import com.noah.backend.global.exception.notification.NotificationNotFoundException;
import com.noah.backend.global.exception.travel.TravelMemberNotFoundException;
import com.noah.backend.global.exception.travel.TravelNotFoundException;
import java.time.LocalDate;
import java.util.List;
import javax.management.NotificationFilter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final TravelRepository travelRepository;
    private final MemberTravelRepository memberTravelRepository;

    @Transactional
    @Override
    public void saveToken(String email, String token) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        member.setNotificationToken(token);
    }

    @Override
    public List<NotificationGetDto> getNotification(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        List<NotificationGetDto> notificationList = notificationRepository.getNotification(member.getId()).orElse(null);
        return notificationList;
    }

    @Transactional
    @Scheduled(cron = "${schedule.cron}")
    @Override
    public void paymentNotify() {
        System.out.println("납입일 알림을 보낼 시간");

        int todayDate = LocalDate.now().getDayOfMonth();
        List<Long> travelList = travelRepository.findTravelPaymentDateIsToday(todayDate).orElse(null);

        if(travelList == null){
            System.out.println("납입일이 오늘인 여행이 없음");
            return;
        }

        // 납입일이 오늘인 여행 반복
        for(int i = 0;i<travelList.size();i++){
            Long travelId = travelList.get(i);
            Travel travel = travelRepository.findById(travelId).orElseThrow(TravelNotFoundException::new);
            List<Long> memberList = memberRepository.findByTravelId(travelId).orElseThrow(TravelMemberNotFoundException::new);

            // 여행에 포함되어 있는 멤버 반복
            for(int j = 0;j<memberList.size();j++){
                Member member = memberRepository.findById(memberList.get(j)).orElseThrow(MemberNotFoundException::new);

                // 알림 테이블에 데이터 저장
                Notification notification = Notification.builder()
                    .type(2)
                    .travelId(travelId)
                    .travelTitle(travel.getTitle())
                    .build();

                notificationRepository.save(notification);

                // 파이어베이스로 푸쉬알림

            }
        }
    }

    @Transactional
    @Override
    public Long inviteAccept(String email, Long notificationId) {
        // 알림 아이디로 알림 찾아와서 여행 아이디 구하기
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);

        // 멤버랑 여행 엔티티 가져와서 멤버트래블 만들고 저장하기
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Travel travel = travelRepository.findById(notification.getTravelId()).orElseThrow(TravelNotFoundException::new);
        MemberTravel memberTravel = MemberTravel.builder()
            .member(member)
            .travel(travel)
            .payment_amount(0)
            .build();
        memberTravelRepository.save(memberTravel);

        // 초대알림은 삭제
        notificationRepository.delete(notification);

        return travel.getId();
    }

    @Override
    public void inviteRefuse(String email, Long notificationId) {
        // 알림 아이디로 알림 엔티티 찾아와서 삭제하기
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);
        notificationRepository.delete(notification);

    }


}
