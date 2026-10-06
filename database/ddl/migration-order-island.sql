-- 배송비 정책 G4: 제주/도서산간 판정표(OP_ISLAND) - AS-IS saleson.model.Island 포팅.
-- 수취인 우편번호로 island_type(JEJU/ISLAND)을 조회해 추가배송비 부과 여부를 정한다.
-- AS-IS와 동일하게 운영 참조 데이터이며, 초기 데이터가 없으면(0건) 추가배송비는 발생하지 않는다.
CREATE TABLE IF NOT EXISTS ord.op_island (
    id          bigint       NOT NULL,
    zipcode     varchar(7),
    address     varchar(255),
    island_type varchar(20),
    CONSTRAINT op_island_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS ord.op_island_id_seq START 1;

-- 우편번호 조회 성능(REPLACE 매칭이라 직접 인덱스는 못 타지만 후보 축소용).
CREATE INDEX IF NOT EXISTS ix_op_island_zipcode ON ord.op_island (zipcode);
