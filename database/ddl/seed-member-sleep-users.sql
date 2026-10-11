-- 휴면회원관리(admin 메뉴 4107, /admin/sleep-users) 확인용 시드 데이터.
-- member.op_user에 STATUS_CODE='DORMANT' 25건을 추가한다(기존 계정은 건드리지 않음).
-- 최종방문일(login_date)은 3~13개월 전으로 분산했다 - AS-IS 자체가 "휴면회원은 정의상
-- 로그인이 오래됐으므로 검색 기본값(오늘)으로는 보통 비어 있고, 운영자가 직접 기간을
-- 넓혀 조회하는 UX"라서(기존 메모 admin-member-area-port-progress.md 참고) 오늘 날짜로
-- 채우지 않았다 - 화면에서 가입일 버튼의 "전체" 또는 "1년"을 눌러야 전부 보인다.
BEGIN;

CREATE TEMP TABLE tmp_dormant_seed (
    login_id text, user_name text, address text, address_detail text,
    login_days_ago int, created_days_ago int
) ON COMMIT DROP;

INSERT INTO tmp_dormant_seed (login_id, user_name, address, address_detail, login_days_ago, created_days_ago) VALUES
('dormant01', '김민준', '서울특별시 강남구 역삼로 123', '5층 501호', 100, 520),
('dormant02', '이서연', '경기도 성남시 분당구 판교역로 45', '101동 302호', 110, 480),
('dormant03', '박도윤', '부산광역시 해운대구 센텀중앙로 78', '902호', 95, 610),
('dormant04', '최하윤', '대구광역시 수성구 범어로 12', '2층', 130, 400),
('dormant05', '정서준', '인천광역시 남동구 구월로 56', '301호', 150, 700),
('dormant06', '강지우', '광주광역시 서구 상무중앙로 34', '', 180, 365),
('dormant07', '조은우', '대전광역시 유성구 대학로 99', '1203호', 200, 900),
('dormant08', '윤수아', '울산광역시 남구 삼산로 21', '505호', 120, 450),
('dormant09', '임주원', '강원도 춘천시 중앙로 8', '', 250, 800),
('dormant10', '한지호', '충청북도 청주시 상당구 상당로 67', '202호', 170, 540),
('dormant11', '오하은', '충청남도 천안시 동남구 만남로 15', '701호', 220, 630),
('dormant12', '신예준', '전라북도 전주시 완산구 전주객사길 5', '', 300, 1000),
('dormant13', '권지안', '전라남도 여수시 여객선터미널로 9', '1동 101호', 280, 920),
('dormant14', '황시우', '경상북도 포항시 북구 중흥로 33', '', 160, 410),
('dormant15', '송예린', '경상남도 창원시 성산구 중앙대로 110', '803호', 140, 390),
('dormant16', '홍준서', '제주특별자치도 제주시 연삼로 20', '', 190, 470),
('dormant17', '안서윤', '서울특별시 마포구 월드컵로 88', '601호', 105, 500),
('dormant18', '유도현', '경기도 수원시 영통구 광교로 150', '1502동 1904호', 240, 760),
('dormant19', '전소율', '부산광역시 수영구 광안해변로 200', '301호', 330, 1100),
('dormant20', '배주안', '대구광역시 달서구 월배로 45', '', 115, 430),
('dormant21', '문가은', '인천광역시 연수구 송도국제대로 55', '2501호', 260, 850),
('dormant22', '서윤호', '광주광역시 북구 첨단과기로 30', '401호', 145, 520),
('dormant23', '남궁민', '대전광역시 서구 둔산로 70', '', 310, 980),
('dormant24', '류하람', '울산광역시 중구 성남로 10', '201호', 125, 460),
('dormant25', '백서현', '강원도 원주시 북원로 44', '1동 802호', 365, 1200);

WITH inserted AS (
    INSERT INTO member.op_user (
        login_id, password, user_name, status_code, login_count,
        login_date, created_date, updated_date,
        sbscrb_se_code, foreign_status_code, password_type, password_expired_date, login_fail_count
    )
    SELECT
        s.login_id,
        '$2a$10$sCgWAKMPhDqYiTRAiMwJRuWSMNS.COYSQOHzf2NlYa9EulhaYYT2.',
        s.user_name,
        'DORMANT',
        5 + (s.login_days_ago % 50),
        to_char(now() - (s.login_days_ago || ' days')::interval, 'YYYYMMDD') || '103000',
        to_char(now() - (s.created_days_ago || ' days')::interval, 'YYYYMMDD') || '091500',
        to_char(now() - (s.login_days_ago || ' days')::interval, 'YYYYMMDD') || '103000',
        'GENERAL', '0', 'N', '20270101', 0
    FROM tmp_dormant_seed s
    RETURNING user_id, login_id
)
INSERT INTO member.op_user_detail (user_id, level_id, address, address_detail, receive_email, receive_sms, receive_kakao, use_flag)
SELECT i.user_id, 1, s.address, s.address_detail, '0', '0', '1', 'Y'
FROM inserted i JOIN tmp_dormant_seed s ON s.login_id = i.login_id;

COMMIT;
