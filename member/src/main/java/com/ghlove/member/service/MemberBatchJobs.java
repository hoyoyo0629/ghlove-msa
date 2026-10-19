package com.ghlove.member.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * member 소유 배치 - 키는 AS-IS OP_BATCH_JOB.JOB_METHOD, 실행 여부·시각은 관리 화면 설정({@link BatchJobScheduler}).
 */
@Component
@RequiredArgsConstructor
public class MemberBatchJobs implements BatchJobSource {

    private final EntityManager entityManager;
    private final TransactionTemplate tx;

    @Override
    public Map<String, Supplier<String>> batchJobs() {
        Map<String, Supplier<String>> jobs = new LinkedHashMap<>();
        // AS-IS id 2000110 "사용자 연령데이터 통계 처리" 03:00 - AS-IS에서 중지 상태
        jobs.put("opUserBirthdayStatBatch", this::opUserBirthdayStat);
        return jobs;
    }

    /**
     * AS-IS {@code UserServiceImpl.setOpUserBirthdayStat}의 운영DB 부분(통계DB 적재는 AS-IS에서 주석처리돼 있다):
     * OP_USER_BIRTHDAY에 아직 없는(그 표의 최대 회원번호보다 큰) 정상(AS-IS '9' = ACTIVE) 회원의 가입일들을 뽑아,
     * 가입일마다 그날 가입한 정상 회원의 생년월일을 OP_USER_BIRTHDAY에 넣는다. 오늘 가입자가 있으면 오늘 날짜는 빼고
     * (AS-IS: 목록에 오늘이 있으면 마지막 하나를 건너뛴다) 다음 회차에 넣는다.
     */
    @SuppressWarnings("unchecked")
    private String opUserBirthdayStat() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> dates = entityManager.createNativeQuery("select substr(created_date, 1, 8) from member.op_user "
                + "where status_code = 'ACTIVE' and user_id > (select coalesce(max(user_id), 0) from member.op_user_birthday) "
                + "and created_date is not null group by substr(created_date, 1, 8) order by 1").getResultList();
        if (dates.isEmpty()) {
            return "적재 0건";
        }
        List<String> targets = dates.contains(today) ? dates.subList(0, dates.size() - 1) : dates;
        int inserted = 0;
        for (String d : targets) {
            Integer n = tx.execute(t -> entityManager.createNativeQuery("insert into member.op_user_birthday (user_id, birthday, created_date) "
                            + "select a.user_id, b.birthday, coalesce(a.created_date, '') from member.op_user a "
                            + "join member.op_user_detail b on a.user_id = b.user_id "
                            + "left join member.op_user_birthday c on a.user_id = c.user_id "
                            + "where a.status_code = 'ACTIVE' and b.birthday is not null and b.birthday != ' ' "
                            + "and a.created_date >= :from and :to >= a.created_date and c.user_id is null")
                    .setParameter("from", d + "000000").setParameter("to", d + "235959").executeUpdate());
            inserted += n == null ? 0 : n;
        }
        return "가입일 " + targets.size() + "일 / 적재 " + inserted + "건";
    }
}
